package com.mjhram.ajerlitaxi.state;

import android.view.View;

import com.google.android.gms.maps.model.Marker;
import com.mjhram.ajerlitaxi.GpsMainActivity;
import com.mjhram.ajerlitaxi.R;
import com.mjhram.ajerlitaxi.common.AppSettings;
import com.mjhram.ajerlitaxi.helper.UploadClass;

/**
 * Created by mohammad.haider on 9/25/2016.
 */
public class passengerState_reconnect implements passengerState {
    private static passengerState_reconnect reconnectState;
    private passengerState_context stateContext;
    private GpsMainActivity mainActivity;

    public static passengerState_reconnect getInstance(GpsMainActivity theActivity, passengerState_context aStateContext) {
        if (reconnectState == null) {
            reconnectState = new passengerState_reconnect();
        }
        reconnectState.mainActivity = theActivity;
        reconnectState.stateContext = aStateContext;
        //reconnectState.onEnter();
        return reconnectState;
    }

    public void onEnter() {
        mainActivity.btnPickDrop.setText(mainActivity.getResources().getString(R.string.gpsMainBtnReconnect));
        mainActivity.btnPickDrop.setVisibility(View.VISIBLE);
        mainActivity.driverInfoLayout.setVisibility(View.INVISIBLE);
    }

    public void btnClicked(){
        UploadClass uc = new UploadClass(mainActivity);
        uc.getPassangerState(AppSettings.getUid());
    }
    public void mapClicked(){}
    public boolean markerClicked(Marker marker){
        return true;
    }
    public boolean backPressed(){
        return false;
    }
}

