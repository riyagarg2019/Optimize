package com.example.riyagarg.optimize;

import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.widget.Toast;

import com.adapter.DestinationRecyclerAdapter;
import com.adapter.ResultsRecyclerAdapter;
import com.data.AppDatabase;
import com.data.Destination;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.touch.DestinationTouchHelperCallback;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

public class ResultsActivity extends AppCompatActivity implements LocationListener,
        OnMapReadyCallback {

    private List<Destination> destinationList;
    public List<Marker> markerList = new LinkedList<>();
    private List<LatLng> positionList = new LinkedList<>();
    private PolylineOptions polyLineOpts;
    private LatLngBounds.Builder builder = new LatLngBounds.Builder();
    private ResultsRecyclerAdapter resultsRecyclerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);

        destinationList = (List<Destination>) getIntent().getSerializableExtra("LIST");

        //MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);

        //mapFragment.getMapAsync(ResultsActivity.this);


        setRecyclerView();

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
        GoogleMap mMap = googleMap;
        mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(47, 19)));



            for (int i = 0; i < destinationList.size(); i++) {
                LatLng position = new LatLng(destinationList.get(i).getLat(), destinationList.get(i).getLng());
                positionList.add(position);
                Marker myMarker = mMap.addMarker(new MarkerOptions().position(position).title(destinationList.get(i).getLocation()));
                markerList.add(myMarker);
            }

            for (int i = 0; i < destinationList.size(); i++) {
                if (i != destinationList.size() - 1) {
                    polyLineOpts = new PolylineOptions().add(positionList.get(i), positionList.get(i + 1)); ////draw lines based on optimized positions
                } else {
                    polyLineOpts = new PolylineOptions().add(positionList.get(destinationList.size() - 1), positionList.get(0));
                }
                Polyline polyline = mMap.addPolyline(polyLineOpts);
                polyline.setColor(Color.GREEN);
                builder.include(positionList.get(i));
            }

            if(positionList.size() != 0) {
                LatLngBounds bounds = builder.build();

                mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 20));
                mMap.setTrafficEnabled(true);
            }




        /*mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
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
        });*/




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


       // mMap.moveCamera(CameraUpdateFactory.newLatLng(position));

    }

    private void setRecyclerView() {
        RecyclerView recyclerViewResults = findViewById(R.id.recycler);
        recyclerViewResults.setHasFixedSize(true);
        recyclerViewResults.setLayoutManager(new LinearLayoutManager(this));
        initDestinations(recyclerViewResults);
    }

    public void initDestinations(final RecyclerView recyclerView) {
        new Thread() {
            @Override
            public void run() {
                final List<Destination> dests = destinationList;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        resultsRecyclerAdapter = new ResultsRecyclerAdapter(dests, ResultsActivity.this);
                        recyclerView.setAdapter(resultsRecyclerAdapter);
                        ItemTouchHelper.Callback callback =
                                new DestinationTouchHelperCallback(resultsRecyclerAdapter);
                        ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
                        touchHelper.attachToRecyclerView(recyclerView);
                    }
                });
            }
        }.start();
    }



}
