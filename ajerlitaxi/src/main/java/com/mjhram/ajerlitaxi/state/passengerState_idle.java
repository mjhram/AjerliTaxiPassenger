package com.mjhram.ajerlitaxi.state;

import android.location.Location;
import android.text.InputType;
import android.view.View;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.mjhram.ajerlitaxi.GpsMainActivity;
import com.mjhram.ajerlitaxi.R;
import com.mjhram.ajerlitaxi.common.AppSettings;
import com.mjhram.ajerlitaxi.common.DriverInfo;
import com.mjhram.ajerlitaxi.helper.UploadClass;

/**
 * Created by mohammad.haider on 9/25/2016.
 */
public class passengerState_idle implements passengerState {
    private passengerState_context stateContext;
    private GpsMainActivity mainActivity;
    private static passengerState_idle stateIdle;

    public static passengerState_idle getInstance(GpsMainActivity theActivity, passengerState_context aStateContext) {
        if(stateIdle == null) {
            stateIdle = new passengerState_idle();
        }
        stateIdle.mainActivity = theActivity;
        stateIdle.stateContext = aStateContext;
        //stateIdle.onEnter();
        return stateIdle;
    }

    public void onEnter(){
        if(mainActivity.countDownTimer != null) {
            mainActivity.countDownTimer.cancel();
            mainActivity.countDownTimer=null;
        }

        AppSettings.requestId = -1;

        mainActivity.btnPickDrop.setVisibility(View.VISIBLE);
        mainActivity.btnPickDrop.setText(mainActivity.getString(R.string.gpsMainBtnPickFrom));
        mainActivity.driverInfoLayout.setVisibility(View.INVISIBLE);
        if(mainActivity.fromMarker != null) {
            mainActivity.fromMarker.remove();
            mainActivity.fromMarker = null;
        }
        if(mainActivity.toMarker != null) {
            mainActivity.toMarker.remove();
            mainActivity.toMarker = null;
        }
        if(mainActivity.driverMarker != null) {
            mainActivity.driverMarker.remove();
            mainActivity.driverMarker = null;
        }
    }

    public void btnClicked(){
        if (mainActivity.mGoogleApiClient.isConnected()) {
            Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mainActivity.mGoogleApiClient);
            if(mLastLocation == null) {
                Toast.makeText(mainActivity,mainActivity.getString(R.string.str_noLocation)
                        , Toast.LENGTH_LONG).show();
                return;
            }
            LatLng currentPosition = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
            //LatLng currentPosition = googleMap.getCameraPosition().target;
            if(mainActivity.fromMarker == null) {
                MarkerOptions markerOptions = new MarkerOptions()
                        .position(currentPosition)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                        //.draggable(true);
                        ;
                mainActivity.fromMarker = mainActivity.googleMap.addMarker(markerOptions);
            } else {
                mainActivity.fromMarker.setPosition(currentPosition);
            }
        }
        {
            MaterialDialog alertDialog = new MaterialDialog.Builder(mainActivity)
                    .title(mainActivity.getString(R.string.app_name))
                    .content(mainActivity.getString(R.string.fromDesc))
                    .inputType(InputType.TYPE_CLASS_TEXT)
                    .input(mainActivity.getString(R.string.fromDescHint), null, new MaterialDialog.InputCallback() {
                        @Override
                        public void onInput(MaterialDialog dialog, CharSequence input) {
                            mainActivity.fromDesc = input.toString();
                        }
                    })
                    .build();
            alertDialog.show();
        }

        stateContext.setState(passengerState_pickFrom.getInstance(mainActivity, stateContext));
    }
    public void mapClicked(){
        Location location = LocationServices.FusedLocationApi.getLastLocation(
                mainActivity.mGoogleApiClient);
        if (location != null) {
            UploadClass uc = new UploadClass(mainActivity);
            uc.getNearbyDrivers(location);
        } else {
            Toast.makeText(mainActivity,mainActivity.getString(R.string.str_noLocation)
                    , Toast.LENGTH_LONG).show();
        }
    }

    public boolean markerClicked(Marker marker){
        DriverInfo tReq = mainActivity.mMarkerInfoHash.get(marker);
        if(tReq == null) {
            return false;
        }
        if(mainActivity.lastMarker == null){
            marker.showInfoWindow();
            mainActivity.lastMarker = marker;
            tReq.infoWindowIsShown=true;
        }else if (marker.getId().equals(mainActivity.lastMarker.getId())) {
            if (tReq.infoWindowIsShown) {
                marker.hideInfoWindow();
                tReq.infoWindowIsShown = false;
            } else {
                marker.showInfoWindow();
                tReq.infoWindowIsShown = true;
            }
        } else{
            if (tReq.infoWindowIsShown) {//если открыто инфовиндов предыдущего маркера, скрываем его
                mainActivity.lastMarker.hideInfoWindow();
                DriverInfo lastTReq = mainActivity.mMarkerInfoHash.get(mainActivity.lastMarker);
                if(lastTReq != null) {
                    lastTReq.infoWindowIsShown = false;
                }
                marker.showInfoWindow();
                tReq.infoWindowIsShown = true;
                mainActivity.lastMarker = marker;
            } else {
                DriverInfo lastTReq = mainActivity.mMarkerInfoHash.get(mainActivity.lastMarker);
                if(lastTReq != null) {
                    lastTReq.infoWindowIsShown = false;
                }

                marker.showInfoWindow();
                tReq.infoWindowIsShown = true;
                mainActivity.lastMarker = marker;
            }
        }

        return true;
    }

    public boolean backPressed(){
        return false;
    }
}

