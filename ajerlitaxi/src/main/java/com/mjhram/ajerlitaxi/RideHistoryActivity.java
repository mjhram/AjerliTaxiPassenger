package com.mjhram.ajerlitaxi;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.mjhram.ajerlitaxi.common.ReqHistoryAdaptor;
import com.mjhram.ajerlitaxi.common.TRequestObj;
import com.mjhram.ajerlitaxi.helper.Constants;
import com.mjhram.ajerlitaxi.helper.UploadClass;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class RideHistoryActivity extends AppCompatActivity {
    private ListView rideHistoryListView;
    private ArrayList<TRequestObj> rideHistoryListArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ride_history);

        rideHistoryListView = (ListView) findViewById(R.id.listViewRideHistory);
        rideHistoryListArray = new ArrayList<>();
        rideHistoryListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final TRequestObj tReq = rideHistoryListArray.get(position);

                if(tReq != null && Integer.parseInt(tReq.feedbackId) == -1) {
                    //show feedback or rating dialog
                    MaterialDialog alertDialog = new MaterialDialog.Builder(RideHistoryActivity.this)
                            .title(getString(R.string.app_name))
                            .content(R.string.rideHistoryReview)
                            .inputRange(0, 150)
                            .inputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE)
                            .input(null, null, new MaterialDialog.InputCallback() {
                                @Override
                                public void onInput(MaterialDialog dialog, CharSequence input) {
                                    UploadClass uc = new UploadClass(RideHistoryActivity.this);
                                    String feedback = input.toString();
                                    uc.uploadFeedback(tReq.idx, feedback);
                                }
                            })
                            .build();
                    alertDialog.show();
                }
            }
        });
        UploadClass uc = new UploadClass(this);
        uc.getRideHistory(this);
    }

    public void showList(String response) {
        if(response.isEmpty()) {
            //clear the list
            rideHistoryListArray.clear();
            rideHistoryListView.setAdapter((ListAdapter) null);
            return;
        } else {
        }
        try {
            JSONObject jObj = new JSONObject(response);
            boolean error = jObj.getBoolean("error");
            rideHistoryListArray.clear();
            // Check for error node in json
            if (!error) {
                JSONArray requests = jObj.getJSONArray("requests");
                //int selectedItem = -1;
                for(int i=0;i<requests.length();i++){
                    JSONObject c = requests.getJSONObject(i);
                    TRequestObj treq = new TRequestObj();
                    treq.idx = c.getInt(Constants.RequestsIdx);
                    treq.time = c.getString(Constants.RequestsTime);

                    treq.fromLat = c.getDouble(Constants.RequestsFromLat);
                    treq.fromLong = c.getDouble(Constants.RequestsFromLong);
                    treq.toLat = c.getDouble(Constants.RequestsToLat);
                    treq.toLong = c.getDouble(Constants.RequestsToLong);
                    treq.fromDesc = c.getString(Constants.RequestsFromDesc);
                    treq.toDesc = c.getString(Constants.RequestsToDesc);
                    treq.suggestedFee = c.getString(Constants.RequestsSuggestedFee);
                    treq.noOfPassangers = c.getString(Constants.RequestsNoOfPassangers);
                    treq.additionalNotes = c.getString(Constants.RequestsAdditionalNotes);

                    treq.driverId = c.getString(Constants.RequestsDriverId);
                    treq.driverName = c.getString(Constants.RequestsDriverName);
                    String tmp = c.getString(Constants.RequestsDriverPhotoId);
                    if(tmp.equalsIgnoreCase("0")) {
                        treq.driverPhotoUrl="";
                    } else {
                        treq.driverPhotoUrl = Constants.URL_downloadUserPhoto + tmp;
                    }
                    treq.feedbackId = c.getString(Constants.RequestsFeedbackId);
                    treq.feedback = c.getString(Constants.RequestsFeedback);

                    rideHistoryListArray.add(treq);
                }

                ReqHistoryAdaptor adapter = new ReqHistoryAdaptor(RideHistoryActivity.this, rideHistoryListArray);
                rideHistoryListView.setAdapter(adapter);
            } else {
                rideHistoryListView.setAdapter((ListAdapter) null);
                // Error in login. Get the error message
                String errorMsg = jObj.getString("error_msg");
                Toast.makeText(getApplicationContext(),
                        errorMsg, Toast.LENGTH_LONG).show();
            }
        } catch (JSONException e) {
            rideHistoryListArray.clear();
            rideHistoryListView.setAdapter((ListAdapter) null);
            // JSON error
            e.printStackTrace();
        }
    }
}

