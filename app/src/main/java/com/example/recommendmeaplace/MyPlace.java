package com.example.recommendmeaplace;

import android.graphics.Bitmap;

public class MyPlace {

    static MyPlace current = null;

    private String name, address;
    private double lat, lng, rating;
    private int id;
    private Bitmap image;

    public MyPlace(String name, double lat, double lng, String address, Bitmap image,  double rating, int id) {
        this.name = name;
        this.lat = lat;
        this.lng = lng;
        this.address = address;
        this.rating = rating;
        this.id = id;
        this.image = image;
    }


    public String getName() {
        return name;
    }

    public double getLat() {
        return lat;
    }

    public double getLng() {
        return lng;
    }


    public String getAddress() {
        return address;
    }

    public double getRating() {
        return rating;
    }

    public int getId() {
        return id;
    }

    public Bitmap getImage() { return image; }
}
