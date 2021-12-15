package com.thorpen.trekconnect;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Arrays;

/*
Photo Credits:
1. https://unsplash.com/photos/xfngap_DToE
2. https://www.wayfair.com/decor-pillows/pdp/millwood-pines-madelynn-33-l-x-204-w-wallpaper-roll-w000825602.html
 */

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "TrekConnect";

    ActivityResultLauncher<Intent> launcher;

    //firebase account
    String userName = "Anonymous";
    String email = "user@email.com";
    TextView welcomeTextView;

    // firebase fields
    FirebaseDatabase mFirebaseDatabase;
    // we are going to add an object called messages
    DatabaseReference mMessagesDatabaseReference;
    ChildEventListener mMessagesChildEventListener;
    // firebase authentication fields
    FirebaseAuth mFirebaseAuth;
    FirebaseAuth.AuthStateListener mAuthStateListener;

    //Navigation Drawer tools
    DrawerLayout drawerLayout;
    ActionBarDrawerToggle actionBarDrawerToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setNavigationViewListener();

        welcomeTextView = findViewById(R.id.welcomeTextView);

        /* for navigation drawer */
        // drawer layout instance to toggle the menu icon to open drawer and back button to close
        drawerLayout = findViewById(R.id.drawerLayout);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.nav_open, R.string.nav_close);
        //pass open and close toggle for the drawer layout listener to toggle button
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        //make the navigation drawer icon appear on the action bar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            Toast.makeText(MainActivity.this, "You are now signed in", Toast.LENGTH_SHORT).show();
                        }
                        else if (result.getResultCode() == Activity.RESULT_CANCELED) {
                            finish();
                        }
                    }
                });
        setupFirebase();
    }

    private void setupFirebase() {
        // initialize the firebase references
        FirebaseApp.initializeApp(this);
        mFirebaseDatabase =
                FirebaseDatabase.getInstance();
        mMessagesDatabaseReference =
                mFirebaseDatabase.getReference()
                        .child("messages");
        mMessagesChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                // called for each message already in our db
                // called for each new message add to our db
                Log.d(TAG, "onChildAdded: " + s);
                ChatMessage chatMessage =
                        dataSnapshot.getValue(ChatMessage.class);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };

        mFirebaseAuth = FirebaseAuth.getInstance();
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    setupUserSignedIn(user);
                } else {
                    Intent intent = AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .setIsSmartLockEnabled(false)
                            .setAvailableProviders(
                                    Arrays.asList(
                                            new AuthUI.IdpConfig.EmailBuilder().build(),
                                            new AuthUI.IdpConfig.GoogleBuilder().build()
                                    )
                            ).build();
                    launcher.launch(intent);
                }
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();
        // attach the authstatelistener
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
    }

    private void setupUserSignedIn(FirebaseUser user) {
        userName = user.getDisplayName();
        email = user.getEmail();
        // listen for database changes with childeventlistener
        mMessagesDatabaseReference.addChildEventListener(mMessagesChildEventListener);
        welcomeTextView.setText("Welcome, " + userName + "!");
    }



    //override to implement the item click listener
    // callback to open and close drawer when icon is clicked
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    /* handles button clicks in drawer layout */
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        Intent intent;

        switch (item.getItemId()) {
            case R.id.nav_account:
                intent = new Intent(MainActivity.this, ProfileActivity.class);
                startProfileActivity(intent);
                break;
            case R.id.nav_find_nearby_trails:
                intent = new Intent(MainActivity.this, MapsActivity.class);
                startActivity(intent);
                break;
            case R.id.nav_chat:
                intent = new Intent(MainActivity.this, ChatListActivity.class);
                startChatListActivity(intent);
                break;
            case R.id.nav_signout:
                AuthUI.getInstance().signOut(this);
                mMessagesDatabaseReference.removeEventListener(mMessagesChildEventListener);
                break;
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    public void setNavigationViewListener() {
        NavigationView navigationView = findViewById(R.id.nav_controller);
        navigationView.setNavigationItemSelectedListener(this);
    }

    public void startChatListActivity(Intent intent) {
        intent.putExtra("userName", userName);
        startActivity(intent);
    }

    public void startProfileActivity(Intent intent) {
        intent.putExtra("name", userName);
        intent.putExtra("email", email);
        startActivity(intent);
    }

    @Nullable
    @Override
    public ActionBar getSupportActionBar() {
        return super.getSupportActionBar();
    }


}