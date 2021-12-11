package com.thorpen.trekconnect;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavAction;
import androidx.navigation.NavController;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "TrekConnect";

    //firebase account
    String userName = "Anonymous";

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

        // Test button
        Button button = findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, MapsActivity.class);
                startActivity(intent);
            }
        });


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
            case R.id.nav_settings:
                Toast.makeText(this, "nav settings", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_findTrekkers:
                Toast.makeText(this, "nav find trekkers", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_chat:
                Toast.makeText(this, "nav chat", Toast.LENGTH_SHORT).show();
                intent = new Intent(MainActivity.this, ChatActivity.class);
                startActivity(intent);
                break;
            case R.id.nav_signout:
                Toast.makeText(this, "nav sign out", Toast.LENGTH_SHORT).show();
                break;
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    public void setNavigationViewListener() {
        NavigationView navigationView = findViewById(R.id.nav_controller);
        navigationView.setNavigationItemSelectedListener(this);
    }



}