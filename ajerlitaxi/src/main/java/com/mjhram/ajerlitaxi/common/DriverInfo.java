package com.mjhram.ajerlitaxi.common;

/**
 * Created by mohammad.haider on 11/12/2015.
 */
public class DriverInfo {
    public int driverId;
    public double latitude, longitude;
    //used to hold nearby drivers info
    public String updatedAtTime, name, phone;
    public int imageId;
    //for related marker
    public boolean infoWindowIsShown;
}
