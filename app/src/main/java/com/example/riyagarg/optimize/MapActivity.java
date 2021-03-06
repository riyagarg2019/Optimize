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
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.Serializable;


public class MapActivity extends AppCompatActivity
        implements android.location.LocationListener, NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback {

    public static final String CURRENT_LOC = "CURRENT_LOC";
    public static final String DEST = "DEST";
    public static final String ADDR = "ADDR";
    private GoogleMap googleMap;
    private Place currentPlace;
    private int destCount;
    private LocationManager locationManager;
    private Location currentLocation;
    private Destination currentDestination;
    private boolean isCurrentDestSet = false;
    private boolean initLocationInMapCallback = false;
    private FusedLocationProviderClient mFusedLocationClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        requestNeededPermission();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        initNavigationDrawer(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchMainActivity();
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

    private void launchMainActivity() {
        final Intent intent = new Intent(MapActivity.this, MainActivity.class);

        if(currentDestination == null &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_DENIED) {
            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
            mFusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if(location != null) {
                                Log.d("DEBUG", "onSuccess: found location via fused");
                                Bundle extras = new Bundle();
                                extras.putSerializable(CURRENT_LOC, (Serializable) new Destination("Current Location",
                                        location.getLatitude(), location.getLongitude()));
                                intent.putExtras(extras);
                                startActivity(intent);
                            } else {
                                Log.d("DEBUG", "launchMainActivity: fused location");

                                showLocationError();
                            }
                        }
                    });
        } else if(currentDestination != null) {
            Bundle extras = new Bundle();
            extras.putSerializable(CURRENT_LOC, (Serializable) currentDestination);
            intent.putExtras(extras);
            startActivity(intent);
        } else {
            Log.d("DEBUG", "launchMainActivity: final else");
            showLocationError();
        }

    }

    private void showLocationError() {
        Toast.makeText(this, R.string.location_error, Toast.LENGTH_LONG).show();
    }

    private void initNavigationDrawer(Toolbar toolbar) {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    private void requestNeededPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                // Toast...
            }

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION},
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

                Toast.makeText(this, R.string.permission_granted, Toast.LENGTH_SHORT).show();
                //Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

                startLocationMonitoring();
            } else {
                Toast.makeText(this, R.string.permission_not_granted, Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(getApplicationContext(), R.string.max_ten, Toast.LENGTH_LONG).show();
                } else {
                    currentPlace = place;
                    updateMap();
                    Destination newDestination = new Destination(currentPlace.getName().toString(),
                            currentPlace.getLatLng().latitude,
                            currentPlace.getLatLng().longitude);
                    Bundle dest = new Bundle();
                    dest.putSerializable(DEST, newDestination);
                    dest.putString(ADDR, place.getAddress().toString());
                    DialogFragment newDialog = new AddDestinationDialog();
                    newDialog.setArguments(dest);
                    newDialog.show(getSupportFragmentManager(), getString(R.string.add_destination));
                }
            }

            @Override
            public void onError(Status status) {

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
        getMenuInflater().inflate(R.menu.map, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
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
            launchMainActivity();
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

            googleMap.addMarker(new MarkerOptions().position(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude())).title(getString(R.string.your_location)));
        }
    }

    private void startLocationMonitoring() {
        try {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
        }catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    private void stopLocationMonitoring() {
        locationManager.removeUpdates(this);
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d("DEBUT", "onLocationChanged: ");
        if (location != null) {
            currentLocation = location;
            currentDestination = new Destination(getString(R.string.current_location),
                    location.getLatitude(),
                    location.getLongitude());

            locationManager.removeUpdates(this);
            zoomOnInitLocation(location);
        }
    }

    private void zoomOnInitLocation(Location location) {
        if(googleMap != null && !isCurrentDestSet) {
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(location.getLatitude(), location.getLongitude()), 12.0f
            ));
            isCurrentDestSet = true;
            googleMap.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(), location.getLongitude())).title(getString(R.string.your_location)));
        } else {
            initLocationInMapCallback = true;
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
