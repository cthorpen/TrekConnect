package com.thorpen.trekconnect;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.thorpen.trekconnect.databinding.ActivityMapsBinding;

import java.io.IOException;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    static final int LOCATION_REQUEST_CODE = 1;

    private GoogleMap mMap;
    private ActivityMapsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // game plan:
        // 1. set the map type
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);

        // 2. add a GU marker (plus geocoding)
        //addGUMarker();

        // 3. set up the my location blue dot
        enableMyLocation();
    }

    private void addGUMarker(){
        String gonzagaStr = "Gonzaga University";
        // need the lat, long for Gonzaga University
        // 2 ways
        // 1. hardcode the lat long
//        LatLng guLatLng = new LatLng(47.6670357,-117.403623);
        // 2. use geocoder
        LatLng guLatLng = getLatLngUsingGeocoding(gonzagaStr);
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.title(gonzagaStr);
        markerOptions.snippet("We are here");
        markerOptions.position(guLatLng);
        mMap.addMarker(markerOptions);

        // move camera to gonzaga
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(guLatLng, 15.0f);
        mMap.moveCamera(cameraUpdate);
    }

    private LatLng getLatLngUsingGeocoding(String addressString){
        // geocoding address -> coordinates
        LatLng latLng = null;
        Geocoder geocoder = new Geocoder(this);
        try {
            List<Address> addressList = geocoder.getFromLocationName(addressString, 1);
            if(addressList != null && addressList.size() > 0){
                Address address = addressList.get(0);
                latLng = new LatLng(address.getLatitude(), address.getLongitude());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return latLng;
    }

    private void enableMyLocation(){
        // need to get user permission to access their FINE LOCATION
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED){
            // if this then we have permission!
            mMap.setMyLocationEnabled(true);
        }else{
            // we need to request permission
            // creates an alert dialog and prompts the user to choose grant or deny
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // this callback executes once the user has made their choice in the alert dialog
        if(requestCode == LOCATION_REQUEST_CODE){
            // only request one permission
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                // we finally have user permission
                enableMyLocation();
            }else{
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}