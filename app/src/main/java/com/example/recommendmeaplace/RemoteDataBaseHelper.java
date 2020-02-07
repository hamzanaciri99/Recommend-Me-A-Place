package com.example.recommendmeaplace;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class RemoteDataBaseHelper {

    public static final String TAG = "REMOTE DATABASE HELPER";

    public static final String POST_URL = "https://map-app-api.000webhostapp.com/addPlace.php";

    public static final String RATE_URL = "https://map-app-api.000webhostapp.com/rate.php";

    public static final String GET_URL = "https://map-app-api.000webhostapp.com/getPlaces.php";

    public static long androidId;

    Context context;
    ProgressDialog dialog;

    public RemoteDataBaseHelper(Context context) {
        this.context = context;
        dialog = new ProgressDialog(context);
        androidId = new BigInteger(Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID), 16).longValue();
    }

    public RemoteDataBaseHelper(Context context, Boolean isService) {
        this.context = context;
    }

    public void addPlace(MyPlace place) {

        new AsyncTask<MyPlace, Void, Boolean>() {

            @Override
            protected void onPreExecute() {
                dialog.setMessage("Adding Place to our database..\nPlease wait..");
                dialog.show();
            }

            @Override
            protected Boolean doInBackground(MyPlace... myPlaces) {
                boolean status = false;
                try {

                    String postParams = "name=" + myPlaces[0].getName() + "&lat=" +
                            myPlaces[0].getLat() + "&lng=" + myPlaces[0].getLng()
                            + "&address=" + myPlaces[0].getAddress();

                    URL url = new URL(POST_URL);
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestProperty("User-Agent", "");
                    urlConnection.setRequestMethod("POST");

                    urlConnection.setDoInput(true);
                    urlConnection.connect();
                    try {
                        OutputStream out = urlConnection.getOutputStream();
                        out.write(postParams.getBytes());
                        out.flush();
                        out.close();
                    } catch (IOException e) {
                        Log.e(TAG, e.getMessage());
                    }

                    int responseCode = urlConnection.getResponseCode();
                    if(responseCode == HttpURLConnection.HTTP_OK) {
                        BufferedReader in = new BufferedReader(new InputStreamReader(
                                urlConnection.getInputStream()));
                        String inputLine;
                        StringBuffer response = new StringBuffer();

                        while ((inputLine = in.readLine()) != null) {
                            response.append(inputLine);
                        }
                        in.close();

                        JsonElement jelement = new JsonParser().parse(response.toString());
                        status = jelement.getAsJsonObject().get("status").toString().substring(1).startsWith("success");

                        urlConnection.disconnect();
                    }
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage());
                }
                return status;
            }

            @Override
            protected void onPostExecute(Boolean status) {
                synchronize();
                String message = (status) ? "Added successfully" : "An error occurred";
                dialog.dismiss();
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
            }
        }.execute(place);
    }

    public void rate(int placeID, double rating) {

        new AsyncTask<Double, Void, Boolean>() {

            @Override
            protected void onPreExecute() {
                dialog.setMessage("Adding Place to our database..\nPlease wait..");
                dialog.show();
            }

            @Override
            protected Boolean doInBackground(Double... params) {
                boolean status = false;
                try {


                    String postParams = "place_id=" + ((int) Math.floor(params[0])) +
                            "&rating=" + params[1] +
                            "&user_id=" + androidId;

                    Log.e(TAG, postParams);

                    URL url = new URL(RATE_URL);
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestProperty("User-Agent", "");
                    urlConnection.setRequestMethod("POST");

                    urlConnection.setDoInput(true);
                    urlConnection.connect();
                    try {
                        OutputStream out = urlConnection.getOutputStream();
                        out.write(postParams.getBytes());
                        out.flush();
                        out.close();
                    } catch (IOException e) {
                        Log.e(TAG, e.getMessage());
                    }

                    int responseCode = urlConnection.getResponseCode();
                    if(responseCode == HttpURLConnection.HTTP_OK) {
                        BufferedReader in = new BufferedReader(new InputStreamReader(
                                urlConnection.getInputStream()));
                        String inputLine;
                        StringBuffer response = new StringBuffer();

                        while ((inputLine = in.readLine()) != null) {
                            response.append(inputLine);
                        }
                        in.close();

                        JsonElement jelement = new JsonParser().parse(response.toString());
                        status = jelement.getAsJsonObject().get("status").toString().substring(1).startsWith("success");

                        urlConnection.disconnect();
                    }
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage());
                }
                return status;
            }

            @Override
            protected void onPostExecute(Boolean status) {
                synchronize();
                String message = (status) ? "Your vote has been added successfully" : "An error occurred";
                dialog.dismiss();
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
            }
        }.execute(placeID * 1d, rating);
    }

    public void synchronize() {
        new Thread() {
            @Override
            public void run() {
                try {
                    URL url = new URL(GET_URL);
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestProperty("User-Agent", "");
                    urlConnection.setRequestMethod("GET");
                    urlConnection.setDoInput(true);
                    urlConnection.connect();

                    BufferedReader in = new BufferedReader(new InputStreamReader(
                            urlConnection.getInputStream()));
                    String inputLine;
                    StringBuffer response = new StringBuffer();

                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();

                    Gson gson = new Gson();
                    Type listType = new TypeToken<ArrayList<MyPlace>>() { }.getType();
                    ArrayList<MyPlace> placesList = gson.fromJson(response.toString(), listType);

                    LocalDataBaseHelper localDataBaseHelper = new LocalDataBaseHelper(context);
                    localDataBaseHelper.synchronize(placesList);
                    localDataBaseHelper.close();

                    urlConnection.disconnect();

                } catch (Exception e) {
                    Log.e(TAG, e.toString());
                }
            }
        }.start();
    }

}
