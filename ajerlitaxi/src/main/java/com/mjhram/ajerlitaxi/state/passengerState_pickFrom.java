package com.mjhram.ajerlitaxi.state;

import android.text.InputType;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.mjhram.ajerlitaxi.GpsMainActivity;
import com.mjhram.ajerlitaxi.R;

/**
 * Created by mohammad.haider on 9/25/2016.
 */
public class passengerState_pickFrom implements passengerState {
    private static passengerState_pickFrom pickState;
    private passengerState_context stateContext;
    private GpsMainActivity mainActivity;

    public static passengerState_pickFrom getInstance(GpsMainActivity theActivity, passengerState_context aStateContext) {
        if (pickState == null) {
            pickState = new passengerState_pickFrom();
        }
        pickState.mainActivity = theActivity;
        pickState.stateContext = aStateContext;
        //pickState.onEnter();
        return pickState;
    }

    public void onEnter(){
        mainActivity.btnPickDrop.setText(mainActivity.getString(R.string.gpsMainBtnDropto));
        if(mainActivity.toMarker != null) {
            mainActivity.toMarker.remove();
            mainActivity.toMarker = null;
        }
        if(mainActivity.driverMarker != null) {
            mainActivity.driverMarker.remove();
            mainActivity.driverMarker = null;
        }
    }

    public boolean markerClicked(Marker marker){
        return true;
    }
    public void btnClicked(){
        //pickdropState = 2;
        if (mainActivity.mGoogleApiClient.isConnected()) {
            //Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            LatLng currentPosition = mainActivity.googleMap.getCameraPosition().target;//new LatLng(fromMarker.getPosition().latitude, fromMarker.getPosition().longitude+.003);
            if(mainActivity.toMarker == null) {
                MarkerOptions markerOptions = new MarkerOptions()
                        .position(currentPosition)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                        //.draggable(true)
                        ;
                mainActivity.toMarker = mainActivity.googleMap.addMarker(markerOptions);
            } else {
                mainActivity.toMarker.setPosition(currentPosition);
            }
        }
        {
            MaterialDialog alertDialog = new MaterialDialog.Builder(mainActivity)
                    .title(mainActivity.getString(R.string.app_name))
                    .content(mainActivity.getString(R.string.toDesc))
                    .inputType(InputType.TYPE_CLASS_TEXT)
                    .input(mainActivity.getString(R.string.toDescHint), null, new MaterialDialog.InputCallback() {
                        @Override
                        public void onInput(MaterialDialog dialog, CharSequence input) {
                            mainActivity.toDesc = input.toString();
                        }
                    })
                    .build();
            alertDialog.show();
        }
        mainActivity.btnPickDrop.setText(mainActivity.getString(R.string.gpsMainBtnConfirm));

        stateContext.setState(passengerState_dropTo.getInstance(mainActivity, stateContext));
    }
    public void mapClicked(){}
    public boolean backPressed(){
        stateContext.setState(passengerState_idle.getInstance(mainActivity, stateContext));
        return true;
    }
}


