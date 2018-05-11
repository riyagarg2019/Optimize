package com.example.riyagarg.optimize;

import android.content.Intent;
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

import com.adapter.DestinationRecyclerAdapter;
import com.data.AppDatabase;
import com.data.Destination;
import com.touch.DestinationTouchHelperCallback;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static final String KEY_FIRST = "KEY_FIRST";
    private DestinationRecyclerAdapter destinationRecyclerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setRecyclerView();
        saveThatItWasStarted();

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                Intent intent = new Intent(MainActivity.this, ResultsActivity.class);
                startActivity(intent);
            }
        });
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
/*
    public void editPlace(Place place) {

        CreateTobuyDialog editDialog = new CreateTobuyDialog();
        Bundle bundle = new Bundle();
        bundle.putSerializable(KEY_TOBUY_TO_EDIT, tobuy);
        editDialog.setArguments(bundle);
        editDialog.show(getSupportFragmentManager(), getString(R.string.edit_dialog));
    } */
}
