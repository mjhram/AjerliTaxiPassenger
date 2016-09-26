package com.mjhram.ajerlitaxi.state;

import android.widget.EditText;

import com.afollestad.materialdialogs.MaterialDialog;
import com.mjhram.ajerlitaxi.GpsMainActivity;
import com.mjhram.ajerlitaxi.R;
import com.mjhram.ajerlitaxi.common.AppSettings;
import com.mjhram.ajerlitaxi.helper.UploadClass;

/**
 * Created by mohammad.haider on 9/25/2016.
 */
public class passengerState_dropTo implements passengerState {
    private static passengerState_dropTo dropState;
    private passengerState_context stateContext;
    private GpsMainActivity mainActivity;

    public static passengerState_dropTo getInstance(GpsMainActivity theActivity, passengerState_context aStateContext) {
        if (dropState == null) {
            dropState = new passengerState_dropTo();
        }
        dropState.stateContext = aStateContext;
        dropState.mainActivity = theActivity;
        dropState.onEnter();
        return dropState;
    }

    public void onEnter(){
        if(mainActivity.driverMarker != null) {
            mainActivity.driverMarker.remove();
            mainActivity.driverMarker = null;
        }
    }

    public void btnClicked(){
        boolean wrapInScrollView = true;
        MaterialDialog dialog = new MaterialDialog.Builder(mainActivity)
                .title(mainActivity.getString(R.string.gpsMainFeeDlgTitle))
                .customView(R.layout.dlg_fee, wrapInScrollView)
                .positiveText(mainActivity.getString(R.string.gpsMainFeeDlgPositive))
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        super.onPositive(dialog);
                        EditText editTextPhone = (EditText) dialog.getCustomView().findViewById(R.id.editTextPhone);
                        EditText editTextFee = (EditText) dialog.getCustomView().findViewById(R.id.editTextFee);
                        EditText editTextNoOfPassangers = (EditText) dialog.getCustomView().findViewById(R.id.editTextNoOfPassangers);
                        EditText editTextAdditionalNotes = (EditText) dialog.getCustomView().findViewById(R.id.editTextAdditionalNotes);

                        mainActivity.treqPhone = editTextPhone.getText().toString();
                        mainActivity.suggestedFee = editTextFee.getText().toString();
                        mainActivity.noOfPassangers = editTextNoOfPassangers.getText().toString();
                        mainActivity.additionalNotes = editTextAdditionalNotes.getText().toString();


                        UploadClass upload = new UploadClass(mainActivity);
                        String lat1 = Double.toString(mainActivity.fromMarker.getPosition().latitude);
                        String long1 = Double.toString(mainActivity.fromMarker.getPosition().longitude);
                        String lat2 = Double.toString(mainActivity.toMarker.getPosition().latitude);
                        String long2 = Double.toString(mainActivity.toMarker.getPosition().longitude);
                        if(mainActivity.fromDesc ==null) {
                            mainActivity.fromDesc="";
                        }
                        if(mainActivity.toDesc ==null) {
                            mainActivity.toDesc="";
                        }
                        upload.addTRequest(AppSettings.getUid(), AppSettings.getEmail(),lat1, long1,
                                lat2,long2,
                                mainActivity.fromDesc, mainActivity.toDesc,
                                mainActivity.treqPhone, mainActivity.suggestedFee, mainActivity.noOfPassangers, mainActivity.additionalNotes);

                        stateContext.setState(passengerState_cancel.getInstance(mainActivity, stateContext, 15*60));
                    }
                })
                .build();
        EditText editTextPhone = (EditText) dialog.getCustomView().findViewById(R.id.editTextPhone);
        String usrPhone = AppSettings.getPhone();
        if(usrPhone!=null && !usrPhone.isEmpty()) {
            editTextPhone.setText(usrPhone);
        }
        dialog.show();
    }
    public void mapClicked(){}
    public boolean backPressed(){
        stateContext.setState(passengerState_pickFrom.getInstance(mainActivity, stateContext));
        //pickdropState=1;
        //btnPickDrop.setText(getString(R.string.gpsMainBtnDropto));
        return true;
    }
}


