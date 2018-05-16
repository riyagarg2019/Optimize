package com.example.riyagarg.optimize;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.adapter.DestinationRecyclerAdapter;
import com.data.AppDatabase;
import com.data.Destination;
import com.data.DistanceToDestination;
import com.data.directions.DirectionResult;
import com.github.jorgecastilloprz.FABProgressCircle;
import com.network.DirectionsAPI;
import com.task.AsyncTSPTask;
import com.touch.DestinationTouchHelperCallback;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    public static final String KEY_FIRST = "KEY_FIRST";
    public static final String TAG = "DEBUG";
    private final String URL_BASE = "https://maps.googleapis.com";
    private DestinationRecyclerAdapter destinationRecyclerAdapter;
    public final String LIST = "LIST";
    private Map<Destination, List<DistanceToDestination>> destAdjList;
    private Retrofit retrofit;
    private DirectionsAPI directionsAPI;
    private int remainingDirectionsAPICalls;
    private List<Destination> optPath;
    private float optSum;
    private FABProgressCircle pcFAB;
    private Destination currentDestination;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        initNavigationDrawer(toolbar);

        setRecyclerView();
        saveThatItWasStarted();

        destAdjList = new HashMap<>();
        pcFAB = findViewById(R.id.fabProgressCircle);
        currentDestination = (Destination) getIntent().getSerializableExtra("CURRENT_LOC");

        initRetrofit();
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("entered here", "!");
                pcFAB.show();
                buildDestAdjList();
            }
        });
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

    private void buildDestAdjList() {
        List<Destination> destinationList = destinationRecyclerAdapter.getDestinationList();
        destinationList.add(currentDestination);
        remainingDirectionsAPICalls = destinationList.size() * (destinationList.size() - 1);

        for (int i = 0; i < destinationList.size(); i++) {
            destAdjList.put(destinationList.get(i), new ArrayList<DistanceToDestination>());
            for (int j = 0; j < destinationList.size(); j++) {
                if(i != j) {
                    queryRetrofit(destinationList.get(i), destinationList.get(j));
                }
            }
        }
    }

    @NonNull
    private void initRetrofit() {
        retrofit = new Retrofit.Builder()
                    .baseUrl(URL_BASE)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

        directionsAPI = retrofit.create(DirectionsAPI.class);
    }



    private void queryRetrofit(final Destination source, final Destination stop) {
        String sourceLatLng = String.format(Locale.getDefault(), "%f,%f",source.getLat(),source.getLng());
        String destLatLng = String.format(Locale.getDefault(), "%f,%f",stop.getLat(),stop.getLng());


        directionsAPI.getWeatherData(sourceLatLng, destLatLng, getString(R.string.api_key))
                .enqueue(new Callback<DirectionResult>() {
                    @Override
                    public void onResponse(Call<DirectionResult> call, Response<DirectionResult> response) {

                        if(response.body().getRoutes().size() > 0) {

                            int distanceMetric = response.body().getRoutes().get(0).getLegs().get(0).getDuration().getValue();
                            destAdjList.get(source).add(new DistanceToDestination(stop, distanceMetric));
                            remainingDirectionsAPICalls--;
                        }

                        if(remainingDirectionsAPICalls == 0) {
                            printAdjList();
                            new MainActivity.AsyncTSPTask().execute();
                        }
                    }

                    @Override
                    public void onFailure(Call<DirectionResult> call, Throwable t) {
                        Toast.makeText(MainActivity.this, "Something went wrong. Check the logs", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "onFailure: " + t.getMessage());
                    }
                });
    }

    private void printAdjList() {
        String print = "";
        for (Destination d: destAdjList.keySet()) {
            print += d.getLocation() + "{ ";
            for (DistanceToDestination u: destAdjList.get(d)) {
                print += u.getStop().getLocation() + ", ";
            }
            print += "}\n";
        }
        Log.d("PRINT ADJ", "printAdjList: " + print);
    }

    private void setRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.recycler);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        initDestinations(recyclerView);
    }

    public boolean isFirstRun() {
        return PreferenceManager.getDefaultSharedPreferences(this).getBoolean(KEY_FIRST, true);
    }

    public void saveThatItWasStarted() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(KEY_FIRST, false);
        editor.commit();
    }

    public void initDestinations(final RecyclerView recyclerView) {
        new Thread() {
            @Override
            public void run() {
                final List<Destination> dests =
                        AppDatabase.getAppDatabase(MainActivity.this).destinationDao().getAll();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        destinationRecyclerAdapter = new DestinationRecyclerAdapter(dests, MainActivity.this);
                        recyclerView.setAdapter(destinationRecyclerAdapter);
                        ItemTouchHelper.Callback callback =
                                new DestinationTouchHelperCallback(destinationRecyclerAdapter);
                        ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
                        touchHelper.attachToRecyclerView(recyclerView);
                    }
                });
            }
        }.start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        if (id == R.id.delete_all) {
            onDeleteDestinations();
        }
        return super.onOptionsItemSelected(item);
    }

    public void onNewDestinationCreated(final String location, final Double lat, final Double lng,
                                  final String description) {
        new Thread(){
            @Override
            public void run() {

                final Destination dest = new Destination(location, lat, lng);
                long id = AppDatabase.getAppDatabase(MainActivity.this).destinationDao().insertDestination(dest);
                dest.setDestinationId(id);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        destinationRecyclerAdapter.addDestination(dest);
                    }
                });
            }
        }.start();
    }

    public void onDestinationUpdated(final Destination dest) {
        new Thread() {
            public void run() {
                AppDatabase.getAppDatabase(MainActivity.this).destinationDao().update(dest);
                runOnUiThread(new Runnable() {
                    public void run() {
                        destinationRecyclerAdapter.updateDestination(dest);
                    }
                });
            }
        }.start();
    }

    public void onDeleteDestinations() {
        destinationRecyclerAdapter.deleteDestinations();
        new Thread() {
            public void run() {
                AppDatabase.getAppDatabase(MainActivity.this).destinationDao().nukeTable();
                runOnUiThread(new Runnable() {
                    public void run() {
                        destinationRecyclerAdapter.deleteDestinations();
                    }
                });
            }
        }.start();
    }

    public void optimizeFromAllPossiblePaths() {
        optSum = Float.MAX_VALUE;
        for (Destination s: destAdjList.keySet()) {
            for (DistanceToDestination d: destAdjList.get(s)) {
                if (d.getStop() != currentDestination) {
                    optimizeSinglePathSetup(s, d.getStop());
                }
            }
        }
        Log.w("optSum", String.valueOf(optSum));
    }

    public void optimizeSinglePathSetup(Destination s, Destination d)
    {
        List<Destination> visited = new LinkedList<>();
        List<Destination> pathList = new LinkedList<>();

        float sum = 0;
        //add source to path[]
        pathList.add(s);
        StringBuilder sb = new StringBuilder("");

        //Call recursive utility
        optimizeSinglePath(s, d, visited, pathList, sb, sum);

        Log.w("local paths", "optimizeSinglePath: " + sb.toString());
    }

    private void optimizeSinglePath(Destination u, Destination d, List<Destination> visited,
                                    List<Destination> localPath, StringBuilder path, float sum) {
        visited.add(u);

        if (u.getLocation().equals(d.getLocation()) && localPath.size() == destAdjList.keySet().size()
                && localPath.get(0) == currentDestination)
        {
            path.append(localPath.toString());
            path.append(sum);
            path.append("\n");
            if (sum < optSum) {
                optSum = sum;
                optPath = new LinkedList<>(localPath);
            }
            sum = 0;
        }

        for (DistanceToDestination dest : destAdjList.get(u))
        {
            if (!visited.contains(dest.getStop())) {
                localPath.add(dest.getStop());
                sum += dest.getDistance();

                optimizeSinglePath(dest.getStop(), d, visited, localPath, path, sum);

                localPath.remove(dest.getStop());
                sum = sum - dest.getDistance();
            }
        }

        visited.remove(u);
    }
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_to_main_activity) {
            item.setVisible(false);
        } else if(id == R.id.nav_to_add_destination) {
            Intent intent = new Intent(this, MapActivity.class);
            startActivity(intent);
        } else if(id == R.id.nav_to_optimize) {
            //Start async task
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

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

    private class AsyncTSPTask extends AsyncTask<Void, Void, Void> {


        @Override
        protected Void doInBackground(Void... voids) {
            optimizeFromAllPossiblePaths();
            Log.d(TAG, "doInBackground: finished path optimization");
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            pcFAB.hide();
            Intent intent = new Intent(MainActivity.this, ResultsActivity.class);
            intent.putExtra(LIST, (Serializable) optPath);
            startActivity(intent);
        }
    }
}
