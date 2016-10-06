package com.mjhram.ajerlitaxi.state;

import com.google.android.gms.maps.model.Marker;

/**
 * Created by mohammad.haider on 9/25/2016.
 */
public interface passengerState {
    void onEnter();
    void btnClicked();
    void mapClicked();
    boolean markerClicked(Marker marker);
    boolean backPressed();
}
