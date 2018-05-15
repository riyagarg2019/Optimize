package com.example.riyagarg.optimize;

import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.adapter.DestinationRecyclerAdapter;
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

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

public class ResultsActivity extends AppCompatActivity implements LocationListener,
        OnMapReadyCallback {

    private GoogleMap mMap;
    private List<Destination> destinationList;
    private List<Marker> markerList = new LinkedList<>();
    private List<LatLng> positionList = new LinkedList<>();
    private PolylineOptions polyLineOpts;
    private LatLngBounds.Builder builder = new LatLngBounds.Builder();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);
        destinationList = (List<Destination>) getIntent().getSerializableExtra("LIST");

        Log.d("Results", "onCreate: " + destinationList);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(ResultsActivity.this);
        //destinationList = getIntent().getStringArrayExtra(MainActivity.DESTINATION_LIST);



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
        //destinationList = getParentActivityIntent()


        //destinationList = destinationRecyclerAdapter.getDestinationList(); //not a real line
        /*Destination eger = new Destination("Eger", 47.9, 20.37);
        Destination budapest = new Destination("Budapest", 47.5, 19.04);
        Destination bratislava = new Destination("Bratislava", 48.15, 17.11);*/


        for(int i = 0; i < destinationList.size(); i++){
            LatLng position = new LatLng(destinationList.get(i).getLat(), destinationList.get(i).getLng());
            positionList.add(position);
            Marker myMarker = mMap.addMarker(new MarkerOptions().position(position).title(destinationList.get(i).getLocation()));
            markerList.add(myMarker);
        }

        for (int i = 0; i < destinationList.size(); i++){
            if (i != destinationList.size()-1) {
                polyLineOpts = new PolylineOptions().add(positionList.get(i), positionList.get(i + 1)); ////draw lines based on optimized positions
            } else {
                polyLineOpts = new PolylineOptions().add(positionList.get(destinationList.size()-1), positionList.get(0));
            }
            Polyline polyline = mMap.addPolyline(polyLineOpts);
            polyline.setColor(Color.GREEN);
            builder.include(positionList.get(i));
         }

        //LatLngBounds bounds = builder.build();
        //bounds
        //Map.animateCamera(CameraUpdateFactory.newLatLngBounds(new LatLngBounds(positionList.get(0),
                //positionList.get(positionList.size()-1)), 20));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(positionList.get(0), 7.0f));

        mMap.setTrafficEnabled(true);


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
}
