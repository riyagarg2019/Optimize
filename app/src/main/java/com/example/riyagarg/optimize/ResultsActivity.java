package com.example.riyagarg.optimize;

import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import com.adapter.ResultsRecyclerAdapter;
import com.data.Destination;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class ResultsActivity extends AppCompatActivity implements LocationListener,
        OnMapReadyCallback {

    private GoogleMap mMap;
    private List<Destination> destinationList;
    private List<Marker> markerList = new LinkedList<>();
    private List<LatLng> positionList = new LinkedList<>();
    private PolylineOptions polyLineOpts;
    private LatLngBounds.Builder builder = new LatLngBounds.Builder();
    private ResultsRecyclerAdapter resultsRecyclerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);
        destinationList = (List<Destination>) getIntent().getSerializableExtra(MainActivity.LIST);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(ResultsActivity.this);
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
        mMap = googleMap;


        if(destinationList.size() != 0) {
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

            setLatLngBounds();

            mMap.setTrafficEnabled(true);
        }

        mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {

            }

            @Override
            public void onMarkerDrag(Marker marker) {

            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
            }
        });
    }

    private void setLatLngBounds() {
        LatLngBounds bounds = builder.build();
        int width = getResources().getDisplayMetrics().widthPixels;
        int height = getResources().getDisplayMetrics().heightPixels;
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds,width, height, 20));
    }

    private void setRecyclerView() {
        RecyclerView recyclerViewResults = findViewById(R.id.recycler);
        recyclerViewResults.setHasFixedSize(true);
        recyclerViewResults.setLayoutManager(new LinearLayoutManager(this));
        initDestinations(recyclerViewResults);

        RecyclerView.ItemDecoration itemDecoration = new DividerItemDecoration(recyclerViewResults.getContext(),
                LinearLayoutManager.VERTICAL);

        recyclerViewResults.addItemDecoration(itemDecoration);
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
                    }
                });
            }
        }.start();
    }



}
