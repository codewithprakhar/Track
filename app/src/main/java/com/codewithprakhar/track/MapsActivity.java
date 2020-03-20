package com.codewithprakhar.track;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.nio.DoubleBuffer;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener
{

    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private Location mLocation;
    private LocationManager mLocationManager;
    private LocationRequest mLocationRequest;
    private com.google.android.gms.location.LocationListener locationListener;
    private long UPDATE_INTERVAL=2000;
    private long FASTEST_INTERVAL=4000;
    private LocationManager locationManager;
    private LatLng latLng;
    private boolean isPermission;
    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    String userID;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        if (requestSinglePermission()) {
            // Obtain the SupportMapFragment and get notified when the map is ready to be used.
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);
            mGoogleApiClient=new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
            mLocationManager=(LocationManager)this.getSystemService(Context.LOCATION_SERVICE);
                    checkLocation();



        }
    }

    private boolean checkLocation() {
        if(!isLocationEnable()) {
            showAlart();

        }
        return isLocationEnable();

    }

    private void showAlart() {
        final AlertDialog.Builder dialog=new AlertDialog.Builder(this);
        dialog.setTitle("Enable Location")
                .setMessage("Your Location Setting is set to 'off'.\n Please enable Location to"+"use this app")
                .setPositiveButton("Location Setting", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent myIntent=new Intent(Settings.ACTION_LOCALE_SETTINGS);
                        startActivity(myIntent);
                    }
                })
                .setNegativeButton("Cancle", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        dialog.show();
    }

    private boolean isLocationEnable() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

    }

    private boolean requestSinglePermission(){
        Dexter.withActivity(this)
                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        isPermission=true;
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        if(response.isPermanentlyDenied())
                        {
                            isPermission=false;
                        }

                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {

                    }
                }).check();

        return isPermission;

}
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;


        if(latLng!=null) {
            mMap.addMarker(new MarkerOptions().position(latLng).title("prakhar"));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,14f));
        }


    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION) !=
                        PackageManager.PERMISSION_GRANTED) {
             return;

        }
        startLocationUpdate();
        mLocation=LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if(mLocation==null)
        {
            startLocationUpdate();
        }
        else {
            Toast.makeText(this,"Location not Detected",Toast.LENGTH_SHORT).show();
        }


    }

    private void startLocationUpdate() {
        mLocationRequest=LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL)
                .setFastestInterval(FASTEST_INTERVAL);
        if(ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION)!=
                PackageManager.PERMISSION_GRANTED&&
                ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION)!=
                        PackageManager.PERMISSION_GRANTED){
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,mLocationRequest,this);


    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(final Location location) {
        String msg="Update Location:" +
                Double.toString(location.getLatitude()) +"," +
                Double.toString(location.getLongitude());
        Toast.makeText(this,msg, Toast.LENGTH_SHORT).show();

        final LocationHelper helper=new LocationHelper(
                location.getLatitude(),
                location.getLongitude()
        );
                FirebaseDatabase.getInstance().getReference("Current Location")
                .setValue(helper).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful())
                {
                    Toast.makeText(MapsActivity.this,"Location Saved", Toast.LENGTH_SHORT);

                }
                else
                {
                    Toast.makeText(MapsActivity.this,"Location Not Saved",Toast.LENGTH_SHORT);
                }
            }
        });



        latLng=new LatLng(location.getLatitude(),location.getLongitude());
        SupportMapFragment mapFragment= (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }

    @Override
    protected void onStart() {
        super.onStart();
        if(mGoogleApiClient!=null) {
        mGoogleApiClient.connect();

        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(mGoogleApiClient.isConnected())
        {
            mGoogleApiClient.disconnect();
        }
    }

}
