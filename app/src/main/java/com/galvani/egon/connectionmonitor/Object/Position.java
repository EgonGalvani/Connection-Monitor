package com.galvani.egon.connectionmonitor.Object;

import java.io.Serializable;

/*
 * @author Egon Galvani
 * @description Class to store details about ip position (data is retrieve by Ip2Location DB)
 * */
public class Position implements Serializable {

    private String country;
    private String region;
    private String city;

    private float latitude;
    private float longitude;

    public Position() {
    }

    public Position(String country, String region, String city, float latitude, float longitude) {
        this.country = country;
        this.region = region;
        this.city = city;

        this.latitude = latitude;
        this.longitude = longitude;
    }

    public float getLatitude() {
        return latitude;
    }

    public float getLongitude() {
        return longitude;
    }

    public String getCountry() {
        return country;
    }

    public String getRegion() {
        return region;
    }

    public String getCity() {
        return city;
    }
}
