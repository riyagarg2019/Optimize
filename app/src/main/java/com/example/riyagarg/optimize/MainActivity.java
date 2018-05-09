package com.example.riyagarg.optimize;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import com.adapter.PlaceRecyclerAdapter;
import com.data.AppDatabase;
import com.data.Place;
import com.touch.PlaceTouchHelperCallback;

import java.util.List;

public class MainActivity extends AppCompatActivity implements MapActivity.PlaceHandler {

    public static final String KEY_FIRST = "KEY_FIRST";
    private PlaceRecyclerAdapter placeRecyclerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setRecyclerView();
        saveThatItWasStarted();
    }

    private void setRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.recycler);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        initPlaces(recyclerView);
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

    public void initPlaces(final RecyclerView recyclerView) {
        new Thread() {
            @Override
            public void run() {
                final List<Place> places =
                        AppDatabase.getAppDatabase(MainActivity.this).placeDao().getAll();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        placeRecyclerAdapter = new PlaceRecyclerAdapter(places, MainActivity.this);
                        recyclerView.setAdapter(placeRecyclerAdapter);
                        ItemTouchHelper.Callback callback =
                                new PlaceTouchHelperCallback(placeRecyclerAdapter);
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
            onDeletePlaces();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onNewPlaceCreated(final String location, final Double lat, final Double lng,
                                  final String description) {
        new Thread(){
            @Override
            public void run() {

                final Place place=  new Place(location, lat, lng, description);
                long id = AppDatabase.getAppDatabase(MainActivity.this).placeDao().insertPlace(place);
                place.setPlaceId(id);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        placeRecyclerAdapter.addPlace(place);
                    }
                });
            }
        }.start();
    }

    @Override
    public void onPlaceUpdated(final Place place) {
        new Thread() {
            public void run() {
                AppDatabase.getAppDatabase(MainActivity.this).placeDao().update(place);
                runOnUiThread(new Runnable() {
                    public void run() {
                        placeRecyclerAdapter.updatePlace(place);
                    }
                });
            }
        }.start();
    }

    @Override
    public void onDeletePlaces() {
        placeRecyclerAdapter.deletePlaces();
        new Thread() {
            public void run() {
                AppDatabase.getAppDatabase(MainActivity.this).placeDao().nukeTable();
                runOnUiThread(new Runnable() {
                    public void run() {
                        placeRecyclerAdapter.deletePlaces();
                    }
                });
            }
        }.start();
    }
/*
    public void editPlace(Place place) {

        CreateTobuyDialog editDialog = new CreateTobuyDialog();
        Bundle bundle = new Bundle();
        bundle.putSerializable(KEY_TOBUY_TO_EDIT, tobuy);
        editDialog.setArguments(bundle);
        editDialog.show(getSupportFragmentManager(), getString(R.string.edit_dialog));
    } */
}
