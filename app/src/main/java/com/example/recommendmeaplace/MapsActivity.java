package com.example.recommendmeaplace;

import android.app.Dialog;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleMap.OnMapLongClickListener, PlaceSelectionListener {

    static final String TAG = "MAP_ACTIVITY";

    public static final String API_KEY = "YOUR_API_HERE";

    GoogleMap map;
    SupportMapFragment supportMapFragment;
    RemoteDataBaseHelper remoteDataBaseHelper;
    Dialog dialog;
    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        intent = getIntent();

        remoteDataBaseHelper = new RemoteDataBaseHelper(this);

        //PlacesAutoComplete (Search Bar)
        Places.initialize(getApplicationContext(), API_KEY);

        supportMapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        supportMapFragment.getMapAsync(this);

        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);

        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.LAT_LNG, Place.Field.NAME));

        autocompleteFragment.setOnPlaceSelectedListener(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.setOnMapLongClickListener(this);

        UiSettings ui = map.getUiSettings();
        ui.setZoomControlsEnabled(true);

        if(intent.hasExtra("Lat")) {
            double lat = intent.getDoubleExtra("Lat", 0.0),
                    lng = intent.getDoubleExtra("Lng", 0.0);
            LatLng pos = new LatLng(lat, lng);
            String title = intent.getStringExtra("Title");

            Marker marker = map.addMarker(new MarkerOptions().position(pos).title(title));
            marker.showInfoWindow();
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, 7));
        }
    }

    public void onMapLongClick(final LatLng latLng) {

        Log.e(TAG, "Long click : " + latLng.toString());

        dialog = new Dialog(this);
        dialog.setContentView(R.layout.name_dialog);
        dialog.setTitle("Title");

        Button add = dialog.findViewById(R.id.add);
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = ((EditText) dialog.findViewById(R.id.name)).getText().toString();

                if(name == null || name.equals("")) {
                    dialog.dismiss();
                    return;
                }

                name = name.substring(0, 1).toUpperCase() + name.substring(1);
                String ccode = "N/A", address = "none";
                try {
                    Geocoder geocoder = new Geocoder(MapsActivity.this, Locale.getDefault());

                    List<Address> addresses = geocoder.getFromLocation(
                            latLng.latitude,
                            latLng.longitude,
                            1); // Only retrieve 1 address
                    ccode = addresses.get(0).getCountryCode();
                    address = addresses.get(0).getAddressLine(addresses.get(0).getMaxAddressLineIndex());

                    Log.e(TAG, address);
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage());
                }
                remoteDataBaseHelper.addPlace(new MyPlace(name, latLng.latitude, latLng.longitude, ccode, address));
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
}