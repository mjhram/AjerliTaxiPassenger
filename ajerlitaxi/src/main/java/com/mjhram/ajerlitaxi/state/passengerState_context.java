package com.mjhram.ajerlitaxi.state;

import com.google.android.gms.maps.model.Marker;
import com.mjhram.ajerlitaxi.GpsMainActivity;

/**
 * Created by mohammad.haider on 9/25/2016.
 */
public class passengerState_context implements passengerState {
    private passengerState state;
    public GpsMainActivity mainActivity;

    public void setState(passengerState aState) {
        state  = aState;
        state.onEnter();
    }
    public passengerState_context(GpsMainActivity theActivity) {
        mainActivity = theActivity;
        setState(passengerState_idle.getInstance(mainActivity, this));
    }
    public void onEnter(){
        state.onEnter();
    }

    public void btnClicked(){
        state.btnClicked();
    }
    public void mapClicked(){
        state.mapClicked();
    }
    public boolean markerClicked(Marker marker){return state.markerClicked(marker);}
    public boolean backPressed(){
        return state.backPressed();
    }

}
