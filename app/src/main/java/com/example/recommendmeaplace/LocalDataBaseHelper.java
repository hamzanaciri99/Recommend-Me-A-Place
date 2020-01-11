package com.example.recommendmeaplace;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.io.File;
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
            db.execSQL("CREATE TABLE IF NOT EXISTS places(`name` varchar(30), `lat` float, `lng` float, `ccode` varchar(30), `address` text );");
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    public void synchronize(ArrayList<MyPlace> places) {
        try {
            db.execSQL("DROP TABLE IF EXISTS places;");
            db.execSQL("CREATE TABLE IF NOT EXISTS places(`name` varchar(30), `lat` float, `lng` float, `ccode` varchar(30) , `address` text );");

            for(MyPlace place : places) {
                String sql = "INSERT INTO places(name, lat, lng, ccode, address) VALUES('" + place.getName() + "'," +
                        " " + place.getLat() + ", " + place.getLng() + ", '" + place.getCcode() + "', '" + place.getAddress() + "');";
                Log.e(TAG, sql);
                db.execSQL(sql);
            }

            Intent listUpdatedBroadcastIntent = new Intent(MainActivity.INTENT_LIST_UPDATED);
            LocalBroadcastManager.getInstance(context).sendBroadcast(listUpdatedBroadcastIntent);

        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    public ArrayList<MyPlace> getPlaces() {
        ArrayList<MyPlace> places = new ArrayList<>();

        try {
            String[] cols = {"name", "lat", "lng", "ccode", "address"};
            Cursor cursor = db.query("places", cols, null, null, null, null, null);

            cursor.moveToPosition(-1);
            while (cursor.moveToNext()) {
                String name = cursor.getString(cursor.getColumnIndex("name")),
                        ccode = cursor.getString(cursor.getColumnIndex("ccode")),
                        address = cursor.getString(cursor.getColumnIndex("address"));
                double lat = cursor.getDouble(cursor.getColumnIndex("lat")),
                        lng = cursor.getDouble(cursor.getColumnIndex("lng"));

                lat = Double.parseDouble(String.format("%.4f", lat));
                lng = Double.parseDouble(String.format("%.4f", lng));

                places.add(new MyPlace(name, lat, lng, ccode, address));
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }

        return places;
    }

    public void close() {
        db.close();
    }
}