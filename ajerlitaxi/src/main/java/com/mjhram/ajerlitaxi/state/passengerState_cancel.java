package com.mjhram.ajerlitaxi.state;

import android.view.View;

import com.google.android.gms.maps.model.Marker;
import com.mjhram.ajerlitaxi.GpsMainActivity;
import com.mjhram.ajerlitaxi.helper.Constants;

/**
 * Created by mohammad.haider on 9/25/2016.
 */
public class passengerState_cancel implements passengerState {
    private static passengerState_cancel stateCancel;
    private passengerState_context stateContext;
    private GpsMainActivity mainActivity;
    int counter;

    public static passengerState_cancel getInstance(GpsMainActivity theActivity, passengerState_context aStateContext, int theCounter) {
        if(stateCancel == null) {
            stateCancel = new passengerState_cancel();
        }
        //pickdropState=3;
        stateCancel.counter = theCounter;
        stateCancel.mainActivity = theActivity;
        stateCancel.stateContext = aStateContext;
        //stateCancel.onEnter();
        return stateCancel;
    }


    public void onEnter(){
        mainActivity.btnPickDrop.setVisibility(View.VISIBLE);
        mainActivity.driverInfoLayout.setVisibility(View.INVISIBLE);
        mainActivity.clearDriversMarkers();
        mainActivity.startCounter(counter);

        if(mainActivity.driverMarker != null) {
            mainActivity.driverMarker.remove();
            mainActivity.driverMarker = null;
        }
    }

    public void btnClicked(){
        mainActivity.cancelTRequest(Constants.TRequest_Canceled);

        //stateContext.setState(new passengerState_idle(stateContext));
    }

    public void mapClicked(){}
    public boolean markerClicked(Marker marker){
        return true;
    }
    public boolean backPressed(){
        return false;
    }
}

