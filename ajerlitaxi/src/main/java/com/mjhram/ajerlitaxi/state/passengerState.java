package com.mjhram.ajerlitaxi.state;

/**
 * Created by mohammad.haider on 9/25/2016.
 */
public interface passengerState {
    void onEnter();
    void btnClicked();
    void mapClicked();
    boolean backPressed();
}
