package com.example.recommendmeaplace;

import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import androidx.annotation.NonNull;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.view.menu.MenuPopupHelper;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity
        implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener, PlaceSelectionListener {

    static final String TAG = "MAP_ACTIVITY";

    public static final String API_KEY = "Your_Key_Here";

    public static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;

    public static final int SORT_BY_ID = 2;

    public static final int SORT_BY_RATE = 3;

    static final String INTENT_LIST_UPDATED = BuildConfig.APPLICATION_ID + ".LIST_UPDATED";

    GoogleMap map;
    SupportMapFragment supportMapFragment;
    RemoteDataBaseHelper remoteDataBaseHelper;
    LocalDataBaseHelper localDataBaseHelper;
    Dialog dialog;
    Intent intent;
    boolean isTerrain = false, mLocationPermissionGranted = false;
    FusedLocationProviderClient mFusedLocationProviderClient;

    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;

    ArrayList<MyPlace> myPlaces;
    RecyclerView.Adapter placesAdapter;

    ListUpdater listUpdater;

    RelativeLayout layoutBottomSheet;
    BottomSheetBehavior sheetBehavior;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        startSyncService();

        intent = getIntent();

        localDataBaseHelper = new LocalDataBaseHelper(getApplication());
        localDataBaseHelper.init();

        remoteDataBaseHelper = new RemoteDataBaseHelper(this);
        remoteDataBaseHelper.synchronize();

        // PlacesAutoComplete (Search Bar)
        Places.initialize(getApplicationContext(), API_KEY);

        supportMapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        supportMapFragment.getMapAsync(this);

        AutocompleteSupportFragment autocompleteFragment =
                (AutocompleteSupportFragment)
                        getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);

        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.LAT_LNG, Place.Field.NAME));

        autocompleteFragment.setOnPlaceSelectedListener(this);

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        final FloatingActionButton terrain = findViewById(R.id.terrain);

        terrain.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (isTerrain) {
                            map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                            terrain.setImageDrawable(
                                    getResources()
                                            .getDrawable(R.drawable.ic_google_earth_unselected));
                        } else {
                            map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                            terrain.setImageDrawable(
                                    getResources()
                                            .getDrawable(R.drawable.ic_google_earth_selected));
                        }
                        isTerrain ^= true;
                    }
                });

        final FloatingActionButton gps = findViewById(R.id.gps);

        gps.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        getDeviceLocation();
                    }
                });

        localDataBaseHelper = new LocalDataBaseHelper(this.getApplication());
        localDataBaseHelper.init();

        layoutBottomSheet = findViewById(R.id.lll);

        sheetBehavior = BottomSheetBehavior.from(layoutBottomSheet);

        final FloatingActionButton list = findViewById(R.id.list);

        list.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (sheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED) {
                            sheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                        } else {
                            sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                        }
                    }
                });

        listUpdater = new ListUpdater();

        LocalBroadcastManager.getInstance(this)
                .registerReceiver(listUpdater, new IntentFilter(INTENT_LIST_UPDATED));

        recyclerView = findViewById(R.id.places_recycler_view);

        showList();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.setOnMapLongClickListener(this);

        ArrayList<MyPlace> places = new LocalDataBaseHelper(this).getPlaces();

        for (MyPlace place : places) {
            map.addMarker(
                    new MarkerOptions()
                            .position(new LatLng(place.getLat(), place.getLng()))
                            .title(place.getName())
                            .snippet(place.getAddress()));
        }

        if (intent.hasExtra("Lat")) {
            double lat = intent.getDoubleExtra("Lat", 0.0), lng = intent.getDoubleExtra("Lng", 0.0);
            LatLng pos = new LatLng(lat, lng);
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, 17));
        } else {
            getDeviceLocation();
        }
    }

    public void onMapLongClick(final LatLng latLng) {

        Log.e(TAG, "Long click : " + latLng.toString());

        dialog = new Dialog(this);
        dialog.setContentView(R.layout.name_dialog);
        dialog.setTitle("Title");

        Button add = dialog.findViewById(R.id.add);
        add.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String name =
                                ((EditText) dialog.findViewById(R.id.name)).getText().toString();

                        if (name == null || name.equals("")) {
                            dialog.dismiss();
                            return;
                        }

                        name = name.substring(0, 1).toUpperCase() + name.substring(1);
                        String address = "none";
                        try {
                            Geocoder geocoder =
                                    new Geocoder(MapsActivity.this, Locale.getDefault());

                            List<Address> addresses =
                                    geocoder.getFromLocation(
                                            latLng.latitude,
                                            latLng.longitude,
                                            1); // Only retrieve 1 address
                            address =
                                    addresses
                                            .get(0)
                                            .getAddressLine(
                                                    addresses.get(0).getMaxAddressLineIndex());

                            Log.e(TAG, address);
                        } catch (Exception e) {
                            Log.e(TAG, e.getMessage());
                        }
                        remoteDataBaseHelper.addPlace(
                                new MyPlace(
                                        name,
                                        latLng.latitude,
                                        latLng.longitude,
                                        address,
                                        null,
                                        0.0,
                                        0));
                        dialog.dismiss();
                    }
                });

        dialog.show();
    }

    @Override
    public void onPlaceSelected(@NonNull Place place) {
        map.addMarker(new MarkerOptions().position(place.getLatLng()));
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(), 15));
    }

    @Override
    public void onError(@NonNull Status status) {
        Log.e(TAG, "Searched place error: " + status.getStatusMessage());
    }

    private void getDeviceLocation() {
        try {
            if (!mLocationPermissionGranted) {
                getLocationPermission();
            }

            if (mLocationPermissionGranted) {
                Task locationResult = mFusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(
                        this,
                        new OnCompleteListener() {
                            @Override
                            public void onComplete(@NonNull Task task) {
                                if (task.isSuccessful()) {
                                    Location mLastKnownLocation = (Location) task.getResult();
                                    map.addMarker(
                                                    new MarkerOptions()
                                                            .position(
                                                                    new LatLng(
                                                                            mLastKnownLocation
                                                                                    .getLatitude(),
                                                                            mLastKnownLocation
                                                                                    .getLongitude()))
                                                            .title("You're here")
                                                            .icon(getMarkerIcon("#5352ED")))
                                            .showInfoWindow();
                                    map.animateCamera(
                                            CameraUpdateFactory.newLatLngZoom(
                                                    new LatLng(
                                                            mLastKnownLocation.getLatitude(),
                                                            mLastKnownLocation.getLongitude()),
                                                    17));
                                } else {
                                    Log.d(TAG, "Current location is null. Using defaults.");
                                    Log.e(TAG, "Exception: %s", task.getException());
                                }
                            }
                        });
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    private void getLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                        this.getApplicationContext(),
                        android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(
                    this,
                    new String[] {android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION:
                {
                    if (grantResults.length > 0
                            && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        mLocationPermissionGranted = true;
                    }
                }
        }
    }

    private void showList() {
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        myPlaces = localDataBaseHelper.getPlaces();

        placesAdapter = new PlacesListAdapter(this, myPlaces);
        recyclerView.setAdapter(placesAdapter);
    }

    public BitmapDescriptor getMarkerIcon(String color) {
        float[] hsv = new float[3];
        Color.colorToHSV(Color.parseColor(color), hsv);
        return BitmapDescriptorFactory.defaultMarker(hsv[0]);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(listUpdater);
    }

    public void startSyncService() {
        AlarmManager alarmMgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, SyncService.class);
        PendingIntent alarmIntent =
                PendingIntent.getService(this, 1, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        alarmMgr.setRepeating(
                AlarmManager.RTC,
                SystemClock.elapsedRealtime() + 2000,
                SyncService.HALF_DAY_INTERVAL,
                alarmIntent);
    }

    public void updateList() {
        myPlaces.clear();
        myPlaces.addAll(localDataBaseHelper.getPlaces());
        placesAdapter.notifyDataSetChanged();
    }

    class ListUpdater extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String intentAction = intent.getAction();
            if (intentAction.equals(INTENT_LIST_UPDATED)) {
                updateList();
            } else if (intentAction.equals(Intent.ACTION_BOOT_COMPLETED)) {
                startSyncService();
            }
        }
    }

    public void showPopup(View v) {

        MenuBuilder menuBuilder = new MenuBuilder(this);
        MenuInflater inflater = new MenuInflater(this);
        inflater.inflate(R.menu.sort_menu, menuBuilder);
        MenuPopupHelper optionsMenu = new MenuPopupHelper(this, menuBuilder, v);
        optionsMenu.setForceShowIcon(true);
        menuBuilder.setCallback(
                new MenuBuilder.Callback() {
                    @Override
                    public boolean onMenuItemSelected(MenuBuilder menu, MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.sort_by_id:
                                Log.e(TAG, "id clicked");
                                sort(SORT_BY_ID);
                                return true;
                            case R.id.sort_by_rate:
                                Log.e(TAG, "rate clicked");
                                sort(SORT_BY_RATE);
                                return true;
                            default:
                                return false;
                        }
                    }

                    @Override
                    public void onMenuModeChange(MenuBuilder menu) {}
                });

        optionsMenu.show();
    }

    public void sort(int key) {
        Comparator<MyPlace> c = null;
        if (key == SORT_BY_ID) {
            c =
                    new Comparator<MyPlace>() {
                        @Override
                        public int compare(MyPlace o1, MyPlace o2) {
                            return o2.getId() - o1.getId();
                        }
                    };
        } else {
            c =
                    new Comparator<MyPlace>() {
                        @Override
                        public int compare(MyPlace o1, MyPlace o2) {
                            double diff = o2.getRating() - o1.getRating();
                            if (diff > 0) return 1;
                            if (diff < 0) return -1;
                            return 0;
                        }
                    };
        }

        Collections.sort(myPlaces, c);
        placesAdapter.notifyDataSetChanged();
    }
}
