package com.thorpen.trekconnect;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
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

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "TrekConnect";

    ActivityResultLauncher<Intent> launcher;

    //firebase account
    String userName = "Anonymous";
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

    //Notification tools
    public String CHANNEL_ID = "TC01";
    public int notificationId = 01; //MUST BE DIFFERENT FOR EACH NOTIFICATION
    NotificationCompat.Builder notificationBuilder;
    // Create an explicit intent for an Activity in your app
    Intent intent;
    PendingIntent pendingIntent;
    NotificationManagerCompat notificationManagerCompat;

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

        /* for notifications */
        notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID);
        createNotificationChannel();
        createNotification();
        intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        notificationManagerCompat = NotificationManagerCompat.from(this);
        notificationManagerCompat.notify(notificationId, notificationBuilder.build());

        launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            Toast.makeText(MainActivity.this, "You are now signed in", Toast.LENGTH_SHORT).show();
                        }
                        else if (result.getResultCode() == Activity.RESULT_CANCELED) {
                            // they backed out of the sign in activity
                            // let's exit
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
                // dataSnapshot stores the ChatMessage
                Log.d(TAG, "onChildAdded: " + s);
                ChatMessage chatMessage =
                        dataSnapshot.getValue(ChatMessage.class);
                // add it to our list and notify our adapter
//                ChatListActivity.chatMessageList.add(chatMessage);
//                ChatListActivity.adapter.notifyDataSetChanged();
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


        // server side setup
        // 1. enable authentication providers like
        // email or google or facebook etc.
        // today we will do email and google
        // 2. return the default values for db
        // read and write to be authenticated
        // client side setup
        // 3. declare a FirebaseAuth.AuthStateListener
        // listens for authentication events
        // signed in and signed out are our two states
        // 4. if the user is signed in...
        // let's get their user name
        // wire up our childeventlistener mMessagesChildEventListener
        // 5. if the user is not signed in...
        // start an activity using FirebaseUI to
        // log our user in
        // 6. wire up the AuthStateListener in onResume()
        // and detach it onPause()
        // 7. add support for the user logging out
        // with an options menu action

        mFirebaseAuth = FirebaseAuth.getInstance();
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                // we have two auth states, signed in and signed out
                // get the get current user, if there is one
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // user is signed in
                    // step 4
                    setupUserSignedIn(user);
                } else {
                    // user is signed out
                    // step 5
                    // we need an intent
                    // the firebaseUI Github repo README.md
                    // we have used builders before in this class
                    // AlertDialog.Builder
                    // return instance to support chaining
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
        // remove it
        mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
//        chatMessageList.clear();
    }

    private void setupUserSignedIn(FirebaseUser user) {
        // get the user's name
        userName = user.getDisplayName();
        // listen for database changes with childeventlistener
        // wire it up!
        mMessagesDatabaseReference.addChildEventListener(mMessagesChildEventListener);


        welcomeTextView.setText("Welcome, " + userName + "!");
    }



    //override to implement the item click listener callback to open and close drawer when icon is clicked
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void createNotification() {
        //change icon later
        notificationBuilder.setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("TrekConnect")
                .setContentText("Someone wants to connect with you!")
                .setPriority(NotificationCompat.PRIORITY_MAX)
                //set the intent that will fire when user taps notification
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel,
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }


    /* handles button clicks in drawer layout */
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        Intent intent;

        switch (item.getItemId()) {
            case R.id.nav_account:
                Toast.makeText(this, "nav account", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_find_nearby_trails:
                Toast.makeText(this, "nav find trails", Toast.LENGTH_SHORT).show();
                intent = new Intent(MainActivity.this, MapsActivity.class);
                startActivity(intent);
                break;
            case R.id.nav_chat:
                Toast.makeText(this, "nav chat", Toast.LENGTH_SHORT).show();
                intent = new Intent(MainActivity.this, ChatListActivity.class);
                startChatListActivity(intent);
                break;
            case R.id.nav_signout:
                Toast.makeText(this, "nav sign out", Toast.LENGTH_SHORT).show();
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


}