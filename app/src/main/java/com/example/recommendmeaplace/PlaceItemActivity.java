package com.example.recommendmeaplace;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

public class PlaceItemActivity extends Activity {

    static final String TAG = "Place Item Activity";

    Dialog dialog;
    RemoteDataBaseHelper remoteDataBaseHelper;
    LocalDataBaseHelper localDataBaseHelper;

    Intent intent;
    MyPlace place;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_item);

        intent = getIntent();

        localDataBaseHelper = new LocalDataBaseHelper(this);
        remoteDataBaseHelper = new RemoteDataBaseHelper(this);

        place = localDataBaseHelper.getPlace(intent.getIntExtra("id", 0));

        ((Button) findViewById(R.id.view_in_map))
                .setOnClickListener(
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent =
                                        new Intent(PlaceItemActivity.this, MapsActivity.class);
                                intent.putExtra("Lat", place.getLat());
                                intent.putExtra("Lng", place.getLng());
                                startActivity(intent);
                            }
                        });

        ((Button) findViewById(R.id.rate_this_place))
                .setOnClickListener(
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                showRatingDialog();
                            }
                        });

        if (place.getImage() != null)
            ((ImageView) findViewById(R.id.imageView)).setImageBitmap(place.getImage());
        ((RatingBar) findViewById(R.id.ratingBar))
                .setRating(Float.parseFloat(place.getRating() + ""));
        ((TextView) findViewById(R.id.rating)).setText(place.getRating() + "");
        ((TextView) findViewById(R.id.address)).setText(place.getAddress());
        ((TextView) findViewById(R.id.title)).setText(place.getName());
    }

    public void showRatingDialog() {
        dialog = new Dialog(this);
        dialog.setContentView(R.layout.rate_dialog);

        Button add = dialog.findViewById(R.id.add);
        add.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        RatingBar ratingBar = dialog.findViewById(R.id.ratingBar2);
                        double rating = ratingBar.getRating();

                        remoteDataBaseHelper.rate(place.getId(), rating);
                        dialog.dismiss();
                    }
                });

        dialog.show();
    }
}
