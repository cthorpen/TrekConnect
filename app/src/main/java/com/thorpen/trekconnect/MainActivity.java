package com.thorpen.trekconnect;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.ConnectionsClient;
import com.google.android.gms.nearby.connection.Strategy;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "TrekConnect";

    // Our handle to Nearby Connections, and other Nearby Connections tools
//    private ConnectionsClient connectionsClient;
//    private static final String[] REQUIRED_PERMISSIONS =
//            new String[] {
//                    Manifest.permission.BLUETOOTH,
//                    Manifest.permission.BLUETOOTH_ADMIN,
//                    Manifest.permission.ACCESS_WIFI_STATE,
//                    Manifest.permission.CHANGE_WIFI_STATE,
//                    Manifest.permission.ACCESS_COARSE_LOCATION,
//            };
//    private static final int REQUEST_CODE_REQUIRED_PERMISSIONS = 1;
//    private static final Strategy STRATEGY = Strategy.P2P_CLUSTER;

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

        //for Nearby Connections
//        connectionsClient = Nearby.getConnectionsClient(this);
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
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
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

}