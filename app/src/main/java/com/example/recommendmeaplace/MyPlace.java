package com.example.recommendmeaplace;

public class MyPlace {
    public String name, ccode, address;
    public double lat, lng;

    public MyPlace(String name, double lat, double lng, String ccode, String address) {
        this.name = name;
        this.lat = lat;
        this.lng = lng;
        this.ccode = ccode;
        this.address = address;
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

    public String getCcode() {
        return ccode;
    }

    public String getAddress() {
        return address;
    }
}
