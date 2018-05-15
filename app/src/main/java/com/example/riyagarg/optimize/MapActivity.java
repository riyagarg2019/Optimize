package com.example.riyagarg.optimize;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;

import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.data.AppDatabase;
import com.data.Destination;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class MapActivity extends AppCompatActivity
        implements android.location.LocationListener, NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback {

    private GoogleMap googleMap;
    private Place currentPlace;
    private int destCount;
    private LocationManager locationManager;
    private Location currentLocation;
    private Destination currentDestination;
    private boolean initLocationInMapCallback = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        requestNeededPermission();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MapActivity.this, MainActivity.class);

                currentDestination = new Destination("Current location",
                        currentLocation.getLongitude(),
                        currentLocation.getLongitude());
                Bundle extras = intent.getExtras();
                extras.putSerializable("CURRENT_LOC", currentDestination);
                startActivity(intent);
            }
        });
        initMap();
        initPlaceSearch();
        new Thread() {
            public void run() {
                destCount = AppDatabase.getAppDatabase(MapActivity.this).destinationDao().getNumberOfRows();
            }
        }.start();
    }

    private void requestNeededPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                // Toast...
            }

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    101);
        } else {
            startLocationMonitoring();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 101) {
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                Toast.makeText(this, "Permission granted, jupeee!", Toast.LENGTH_SHORT).show();
                //Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

                startLocationMonitoring();
            } else {
                Toast.makeText(this, "Permission not granted :(", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        new Thread() {
            public void run() {
                destCount = AppDatabase.getAppDatabase(MapActivity.this).destinationDao().getNumberOfRows();
            }
        }.start();
    }

    private void initMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private void initPlaceSearch() {
        PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                if (destCount > 9) {
                    Toast.makeText(getApplicationContext(), "Max 10 destinations: delete to add new", Toast.LENGTH_LONG).show();
                } else {
                    currentPlace = place;
                    updateMap();
                    Destination newDestination = new Destination(currentPlace.getName().toString(),
                            currentPlace.getLatLng().latitude,
                            currentPlace.getLatLng().longitude);
                    Bundle dest = new Bundle();
                    dest.putSerializable("DEST", newDestination);
                    dest.putString("ADDR", place.getAddress().toString());
                    DialogFragment newDialog = new AddDestinationDialog();
                    newDialog.setArguments(dest);
                    newDialog.show(getSupportFragmentManager(), "Add Destination");
                }
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
            }
        });
    }

    public void addDestinationToDatabase(final Destination destination) {
        destCount++;
        new Thread() {
            @Override
            public void run() {
                long id = AppDatabase.getAppDatabase(MapActivity.this).destinationDao().insertDestination(destination);
                destination.setDestinationId(id);
            }
        }.start();
    }

    private void updateMap() {
        googleMap.addMarker(new MarkerOptions().position(currentPlace.getLatLng()).title(currentPlace.getAddress().toString()));
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentPlace.getLatLng(), 7.0f));
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.map, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_to_main_activity) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        this.googleMap = googleMap;

        if(initLocationInMapCallback) {
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), 12.0f
            ));
        }

        //googleMap.addMarker(new MarkerOptions().position(currentLatLng).title("current"));
        //googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 7.0f));
    }

    private void startLocationMonitoring() {
        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
            //locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
        }catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    private void stopLocationMonitoring() {
        locationManager.removeUpdates(this);
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            currentLocation = location;

            Log.d("DEBUG", "onLocationChanged: " + location.getLatitude() + location.getLongitude());

            if(googleMap != null) {
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                        new LatLng(location.getLatitude(), location.getLongitude()), 12.0f
                ));
                googleMap.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(), location.getLongitude())).title("Your location"));
            } else {
                initLocationInMapCallback = true;
            }
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    protected void onStop() {
        super.onStop();
        stopLocationMonitoring();
    }
}
