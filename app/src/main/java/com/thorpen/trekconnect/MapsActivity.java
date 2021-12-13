package com.thorpen.trekconnect;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.thorpen.trekconnect.databinding.ActivityMapsBinding;

import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    static final String TAG = "MapsActivity";
    static final int LOCATION_REQUEST_CODE = 1;
    private double currLat;
    private double currLng;
    List<Place> placeList = new ArrayList<>();

    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private LocationCallback locationCallback;

    // Set up API
    GoogleAPI googleAPI = new GoogleAPI(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Gets location
        mFusedLocationProviderClient =
                LocationServices.getFusedLocationProviderClient(this);
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                for (Location location : locationResult.getLocations()) {
                    currLat = location.getLatitude();
                    currLng = location.getLongitude();
                    googleAPI.fetchNearMeLocations();
                }
            }
        };
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        enableMyLocation();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setupLastKnownLocation();
        setupUserLocationUpdates();
    }

    // Finds last known location
    private void setupLastKnownLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[] {Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_REQUEST_CODE);
        } else {
            Task<Location> locationTask = mFusedLocationProviderClient.getLastLocation();
            locationTask.addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        currLat = location.getLatitude();
                        currLng = location.getLongitude();
                    }
                }
            });
        }
    }

    private void populateMapMarkers(){
        for (Place place:placeList) {
            addMarker(place.getName(), place.getLatitude(), place.getLongitude());
        }
    }


    // adds custom markers to each found location
    private void addMarker(String name, String lat, String lng){
        LatLng tempLatLng = new LatLng(Double.parseDouble(lat), Double.parseDouble(lng));
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.title(name);

        // scaling size found here https://stackoverflow.com/questions/35718103/how-to-specify-the-size-of-the-icon-on-the-marker-in-google-maps-v2-android
        // hiking man icon found at https://www.iconfinder.com/icons/351587/user_hiking_person_man_walking_stick_icon
        int height = 125;
        int width = 125;
        BitmapDrawable bitmapDrawable = (BitmapDrawable)getResources().getDrawable(R.drawable.hikingman);
        Bitmap b = bitmapDrawable.getBitmap();
        Bitmap smallerHiker = Bitmap.createScaledBitmap(b, width, height, false);
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(smallerHiker));

        markerOptions.position(tempLatLng);
        mMap.addMarker(markerOptions);
    }

    // move camera to current lat lng
    public void moveCamera(float zoom){
        LatLng currLatLng = new LatLng(currLat, currLng);
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(currLatLng, zoom);
        mMap.moveCamera(cameraUpdate);
    }

    // shows location on map
    private void enableMyLocation(){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED){
            mMap.setMyLocationEnabled(true);
        }else{
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == LOCATION_REQUEST_CODE){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                enableMyLocation();
                setupLastKnownLocation();
                setupUserLocationUpdates();
            }else{
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Constant user location updates
    private void setupUserLocationUpdates() {
        final com.google.android.gms.location.LocationRequest locationRequest = com.google.android.gms.location.LocationRequest.create();
        locationRequest.setInterval(100000); // update every 100 seconds
        locationRequest.setFastestInterval(60000); // handle at most updates every 60 seconds
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(locationRequest);
        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

        task.addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                if (ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MapsActivity.this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            LOCATION_REQUEST_CODE);
                } else {
                    mFusedLocationProviderClient.requestLocationUpdates(locationRequest,
                            locationCallback, null);
                }
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        mFusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }

    public void receivedPlaces(List<Place> placeList) {
        this.placeList = placeList;
        populateMapMarkers();
        moveCamera(12.0f);
    }

    public String getCurrLat() {
        return String.valueOf(currLat);
    }

    public String getCurrLng() {
        return String.valueOf(currLng);
    }
}