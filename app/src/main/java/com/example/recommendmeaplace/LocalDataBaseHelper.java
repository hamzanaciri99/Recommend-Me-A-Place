package com.example.recommendmeaplace;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class LocalDataBaseHelper {

    static final String TAG = "LOCAL DATABASE HELPER";

    private SQLiteDatabase db;
    private Context context;

    public LocalDataBaseHelper(Context context) {
        this.context = context;
        openDatabase();
    }

    private void openDatabase() {
        try {
            File storagePath = context.getFilesDir();
            String myDbPath = storagePath + "/" + "recommendmeaplace.db";

            db = SQLiteDatabase.openDatabase(myDbPath, null,
                    SQLiteDatabase.CREATE_IF_NECESSARY);

        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    public void init() {
        try {
            db.execSQL("CREATE TABLE IF NOT EXISTS " +
                    "places(`id` int CONSTRAINT primary_key PRIMARY KEY, `name` varchar(30), `lat` float, `lng` float, `address` text," +
                    " `image` blob, `rating` float);");
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    public void synchronize(ArrayList<MyPlace> places) {
        try {
            db.execSQL("DROP TABLE IF EXISTS places;");
            db.execSQL("CREATE TABLE IF NOT EXISTS places(`id` int CONSTRAINT primary_key PRIMARY KEY, `name` varchar(30), `lat` float, " +
                    "`lng` float, `address` text,`image` blob, `rating` float);");

            for(MyPlace place : places) {
                Bitmap bmp = getBitmapImage(place.getLat(), place.getLng());
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                bmp.compress(Bitmap.CompressFormat.PNG, 100, bos);
                byte[] img = bos.toByteArray();

                ContentValues values = new ContentValues();
                values.put("image", img);
                values.put("id", place.getId());
                values.put("name", place.getName());
                values.put("lat", place.getLat());
                values.put("lng", place.getLng());
                values.put("address", place.getAddress());
                values.put("rating", place.getRating());

                String sql = "INSERT INTO places(id, name, lat, lng, rating, address, image) VALUES(" + place.getId() +
                        ", '" + place.getName() + "', " + place.getLat() + ", " + place.getLng() +
                        ", '" + place.getRating() + "', '" + place.getAddress() + "'," + img + ");";
                Log.e(TAG, sql);
                db.insert("places", null, values);
            }

            Intent listUpdatedBroadcastIntent = new Intent(MapsActivity.INTENT_LIST_UPDATED);
            LocalBroadcastManager.getInstance(context).sendBroadcast(listUpdatedBroadcastIntent);

        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    public ArrayList<MyPlace> getPlaces() {
        ArrayList<MyPlace> places = new ArrayList<>();

        try {
            String[] cols = {"id", "name", "lat", "lng", "address", "rating", "image"};
            Cursor cursor = db.query("places", cols, null, null, null, null, null);

            cursor.moveToPosition(-1);
            while (cursor.moveToNext()) {
                String name = cursor.getString(cursor.getColumnIndex("name")),
                        address = cursor.getString(cursor.getColumnIndex("address"));
                double lat = cursor.getDouble(cursor.getColumnIndex("lat")),
                        lng = cursor.getDouble(cursor.getColumnIndex("lng")),
                        rating = cursor.getDouble(cursor.getColumnIndex("rating"));
                int id = cursor.getInt(cursor.getColumnIndex("id"));
                byte[] bmp = cursor.getBlob(cursor.getColumnIndex("image"));
                Bitmap image =BitmapFactory.decodeByteArray(bmp, 0, bmp.length);

                lat = Double.parseDouble(String.format("%.4f", lat));
                lng = Double.parseDouble(String.format("%.4f", lng));
                rating = Double.parseDouble(String.format("%.1f", rating));

                places.add(new MyPlace(name, lat, lng, address, image, rating, id));
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }

        return places;
    }

    public MyPlace getPlace(int pid) {
        MyPlace place = null;

        try {
            String[] cols = {"id", "name", "lat", "lng", "address", "rating", "image"};
            String[] whereArgs = {""+pid};
            Cursor cursor = db.query("places", cols, "id = ?", whereArgs, null, null, null);

            cursor.moveToPosition(-1);
            while (cursor.moveToNext()) {
                String name = cursor.getString(cursor.getColumnIndex("name")),
                        address = cursor.getString(cursor.getColumnIndex("address"));
                double lat = cursor.getDouble(cursor.getColumnIndex("lat")),
                        lng = cursor.getDouble(cursor.getColumnIndex("lng")),
                        rating = cursor.getDouble(cursor.getColumnIndex("rating"));
                int id = cursor.getInt(cursor.getColumnIndex("id"));
                byte[] bmp = cursor.getBlob(cursor.getColumnIndex("image"));
                Bitmap image =BitmapFactory.decodeByteArray(bmp, 0, bmp.length);

                lat = Double.parseDouble(String.format("%.4f", lat));
                lng = Double.parseDouble(String.format("%.4f", lng));
                rating = Double.parseDouble(String.format("%.1f", rating));

                place = new MyPlace(name, lat, lng, address, image, rating, id);
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }

        return place;
    }


    public Bitmap getBitmapImage(double lat, double lng) {
        Bitmap bmp = null;
        InputStream inputStream = null;
        try {
            URL mapUrl = new URL("https://maps.googleapis.com/maps/api/staticmap?center="
                    + lat + "," + lng +
                    "&zoom=17&size=600x600" +
                    "&markers=color:blue%7C"+ lat + "," + lng +
                    "&sensor=false&key=AIzaSyCyFi0XzVU_EapWGRx6W_Xd58tyDIPDt2M");

            HttpURLConnection httpURLConnection = (HttpURLConnection) mapUrl.openConnection();

            inputStream = new BufferedInputStream(httpURLConnection.getInputStream());

            bmp = BitmapFactory.decodeStream(inputStream);

            inputStream.close();
            httpURLConnection.disconnect();

        } catch (IllegalStateException e) {
            Log.e(TAG, e.toString());
        } catch (IOException e) {
            Log.e(TAG, e.toString());
        }

        return bmp;
    }

    public void close() {
        db.close();
    }
}