package com.example.recommendmeaplace;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class MainActivity extends Activity {

    static final String TAG = "MAIN ACTIVITY";
    static final String INTENT_LIST_UPDATED = BuildConfig.APPLICATION_ID + ".LIST_UPDATED";

    private FloatingActionButton floatingActionButton;
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;

    RemoteDataBaseHelper remoteDataBaseHelper;
    LocalDataBaseHelper localDataBaseHelper;

    ArrayList<MyPlace> myPlaces;
    RecyclerView.Adapter placesAdapter;

    ListUpdater listUpdater;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        startSyncService();

        recyclerView = (RecyclerView) findViewById(R.id.places_recycler_view);
        floatingActionButton = findViewById(R.id.floating_action_button);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, MapsActivity.class);
                startActivityForResult(intent, 1);
            }
        });

        localDataBaseHelper = new LocalDataBaseHelper(this.getApplication());
        localDataBaseHelper.init();

        remoteDataBaseHelper = new RemoteDataBaseHelper(this);
        remoteDataBaseHelper.synchronize();

        listUpdater = new ListUpdater();

        LocalBroadcastManager.getInstance(this).registerReceiver(listUpdater,
                        new IntentFilter(INTENT_LIST_UPDATED));

        showList();
    }

    private void showList() {
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        myPlaces = localDataBaseHelper.getPlaces();

        placesAdapter = new PlacesListAdapter(this, myPlaces);
        recyclerView.setAdapter(placesAdapter);
    }

    public void updateList() {
        myPlaces.clear();
        myPlaces.addAll(localDataBaseHelper.getPlaces());
        placesAdapter.notifyDataSetChanged();
    }

    public void startSyncService() {
        final long INTERVAL = 1000 * 10;

        startForegroundService(new Intent(this, SyncService.class));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(listUpdater);
        localDataBaseHelper.close();
    }

    class ListUpdater extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String intentAction = intent.getAction();
            if(intentAction.equals(INTENT_LIST_UPDATED)) {
                updateList();
            }
        }
    }

}
