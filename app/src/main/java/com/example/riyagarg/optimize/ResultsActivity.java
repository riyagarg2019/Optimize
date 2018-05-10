package com.example.riyagarg.optimize;

import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class ResultsActivity extends AppCompatActivity implements LocationListener,
        OnMapReadyCallback {

    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(ResultsActivity.this);


        //mMap = ((MapFragment) getFragmentManager().
                        //findFragmentById(R.id.map)).getMap(); mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID); mMap.setTrafficEnabled(true);

        /*mMap.addMarker(new MarkerOptions()
                .position(new LatLng(19, 35))
                .title("Hello Android team!"));*/
    }

    @Override
    public void onLocationChanged(Location location) {

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
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        LatLng hungary = new LatLng(47, 19);
        Marker myMarker = mMap.addMarker(
                new MarkerOptions().
                        position(hungary).
                        title("Marker in Hungary").
                        snippet("This is a marker"));
        myMarker.setDraggable(true);


        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                try {
                    Geocoder gc = new Geocoder(ResultsActivity.this, Locale.getDefault());
                    List<Address> addrs = null; //set this equal to our list of addresses

                    addrs = gc.getFromLocation(latLng.latitude, latLng.longitude, 2);

                    mMap.addMarker(
                            new MarkerOptions().
                                    position(latLng).
                                    title("Marker").
                                    snippet(
                                            addrs.get(0).getAddressLine(0)+"\n"+
                                                    addrs.get(0).getAddressLine(1)+"\n"+
                                                    addrs.get(0).getAddressLine(2)
                                    ));

                }catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });




        mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {

            }

            @Override
            public void onMarkerDrag(Marker marker) {

            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                //marker.getPosition().latitude
            }
        });


        mMap.moveCamera(CameraUpdateFactory.newLatLng(hungary));

        mMap.setTrafficEnabled(true);
        //mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);

    }
}
