package com.mjhram.ajerlitaxi.state;

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
    public boolean backPressed(){
        return state.backPressed();
    }

}
