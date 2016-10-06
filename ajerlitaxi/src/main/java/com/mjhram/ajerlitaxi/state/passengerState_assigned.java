package com.mjhram.ajerlitaxi.state;

import android.view.View;

import com.android.volley.toolbox.ImageLoader;
import com.google.android.gms.maps.model.Marker;
import com.mjhram.ajerlitaxi.GpsMainActivity;
import com.mjhram.ajerlitaxi.common.AppSettings;
import com.mjhram.ajerlitaxi.common.TRequestObj;

/**
 * Created by mohammad.haider on 9/25/2016.
 */
public class passengerState_assigned implements passengerState {
    private static passengerState_assigned assignedState;
    private passengerState_context stateContext;
    private GpsMainActivity mainActivity;
    private TRequestObj tRequestObj;

    public static passengerState_assigned getInstance(GpsMainActivity theActivity, passengerState_context aStateContext, TRequestObj tReqObj) {
        if (assignedState == null) {
            assignedState = new passengerState_assigned();
        }
        assignedState.mainActivity = theActivity;
        assignedState.stateContext = aStateContext;
        assignedState.tRequestObj = tReqObj;
        //assignedState.onEnter();
        return assignedState;
    }
    public void onEnter() {
        //pickdropState=30;
        mainActivity.helpOverlayLayout.setVisibility(View.GONE);
        mainActivity.btnPickDrop.setVisibility(View.GONE);
        mainActivity.driverInfoLayout.setVisibility(View.VISIBLE);
        mainActivity.txtDriverName.setText(tRequestObj.driverName);
        mainActivity.txtDriverInfo.setText(tRequestObj.driverInfo);
        mainActivity.btnDriverPhone.setText(tRequestObj.driverPhone);
        {
            //final String IMAGE_URL = "http://developer.android.com/images/training/system-ui.png";
            ImageLoader mImageLoader = AppSettings.getInstance().getImageLoader();
            mainActivity.networkImageViewDriver.setImageUrl(tRequestObj.driverPhotoUrl, mImageLoader);
        }
        //driver not assigned yet & 15min elapsed => neglect it
        if(Integer.parseInt(tRequestObj.driverId) == -1) {
            int remainingSeconds = 900 - Integer.parseInt(tRequestObj.secondsToNow);
            if(remainingSeconds < 5) {
                stateContext.setState(passengerState_idle.getInstance(mainActivity, stateContext));
                return;
            } else {
                //set timer to remainingSeconds
                //startCounter(remainingSeconds);
                stateContext.setState(passengerState_cancel.getInstance(mainActivity, stateContext, remainingSeconds));
                AppSettings.requestId = tRequestObj.idx;
                //pickdropState=3;
                //btnPickDrop.setVisibility(View.VISIBLE);
                //driverInfoLayout.setVisibility(View.INVISIBLE);
            }
        }
        //2. driver assigned or passanger picked
        mainActivity.zoom2TReqObj(tRequestObj);
    }
    public void btnClicked(){}
    public void mapClicked(){}
    public boolean markerClicked(Marker marker){
        return true;
    }
    public boolean backPressed(){
        return false;
    }
}

