package com.mjhram.ajerlitaxi.helper;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.location.Location;
import android.util.Log;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.android.volley.toolbox.StringRequest;
import com.mjhram.ajerlitaxi.R;
import com.mjhram.ajerlitaxi.RideHistoryActivity;
import com.mjhram.ajerlitaxi.common.AppSettings;
import com.mjhram.ajerlitaxi.common.DriverInfo;
import com.mjhram.ajerlitaxi.common.TRequestObj;
import com.mjhram.ajerlitaxi.common.UserInfo;
import com.mjhram.ajerlitaxi.common.Utilities;
import com.mjhram.ajerlitaxi.common.events.ServiceEvents;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import de.greenrobot.event.EventBus;

/**
 * Created by mohammad.haider on 10/8/2015.
 */
public class UploadClass {
    private ProgressDialog pDialog;
    private Context cx;
    private static final String URL_addTRequest = Constants.SERVER_URL + "/addTRequest.php";
    private static final String URL_getPassangerState = Constants.SERVER_URL + "/getPassangerState.php";
    private static final String URL_getUserProfile = Constants.SERVER_URL + "/getuser.php";
    private static final String URL_forgotPassword = Constants.SERVER_URL+"/forgotpassword.php?action=password";

    private static final String TAG = UploadClass.class.getSimpleName();
    private phpErrorMessages phpErrorMsgs;

    public UploadClass(Context theCx) {
        cx = theCx;
        phpErrorMsgs = AppSettings.getInstance().getPhpErrorMsg();
        if(cx == null) {
            pDialog = null;
        }else{
            pDialog = new ProgressDialog(cx);
            pDialog.setCancelable(false);
        }

    }

    public static void updateRegId(final String userId, final String regId) {
        String tag_string_req = "regId_update";

        //pDialog.setMessage(cx.getString(R.string.gpsMainDlgMsgUpdating));
        //showDialog();

        StringRequest strReq = new StringRequest(Request.Method.POST,
                Constants.URL_UpdateRegId, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(AppSettings.TAG, "update reg id Response: " + response.toString());
                //hideDialog();
                AppSettings.setRegId(userId);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(AppSettings.TAG, "Update Error: " + error.getMessage());
                //Toast.makeText(cx,
                //        error.getMessage(), Toast.LENGTH_LONG).show();
                //hideDialog();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting parameters to login url
                Map<String, String> params = new HashMap<String, String>();
                params.put("tag", "updateRegId");
                params.put("userId", userId);
                params.put("regId", regId);
                return params;
            }

        };

        // Adding request to request queue
        AppSettings tmp = AppSettings.getInstance();
        tmp.addToRequestQueue(strReq, tag_string_req);
    }

    public void uploadFeedback(final int idx, final String feedback) {
        // Tag used to cancel the request
        String tag_string_req = "uploadFeeback";

        pDialog.setMessage(cx.getString(R.string.uploadDlgMsgUpdatingRqst));
        showDialog();

        StringRequest strReq = new StringRequest(Request.Method.POST,
                Constants.URL_uploadFeedback, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "uploadFeedback Response: " + response);
                hideDialog();
                //rideHistoryActivity.showList(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "uploadFeedback Error: " + error.getMessage());
                Toast.makeText(cx,
                        error.getMessage(), Toast.LENGTH_LONG).show();
                hideDialog();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting params to register url
                Map<String, String> params = new HashMap<String, String>();
                params.put("tag", "uploadFeedback");
                params.put("idx", Integer.toString(idx));
                params.put("rate", "5");
                params.put("feedback", feedback);
                return params;
            }
        };
        // Adding request to request queue
        AppSettings ac = AppSettings.getInstance();
        ac.addToRequestQueue(strReq, tag_string_req);
    }

    public void getRideHistory(final RideHistoryActivity rideHistoryActivity) {
        // Tag used to cancel the request
        String tag_string_req = "getRideHsitory";

        pDialog.setMessage(cx.getString(R.string.uploadDlgMsgUpdatingRqst));
        showDialog();

        StringRequest strReq = new StringRequest(Request.Method.POST,
                Constants.URL_getRideHistory, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "getDrivers Response: " + response);
                hideDialog();

                rideHistoryActivity.showList(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "getRideHistory Error: " + error.getMessage());
                Toast.makeText(cx,
                        error.getMessage(), Toast.LENGTH_LONG).show();
                hideDialog();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting params to register url
                Map<String, String> params = new HashMap<String, String>();
                params.put("tag", "getRideHistory");
                params.put("userId", AppSettings.getUid());
                return params;
            }
        };
        // Adding request to request queue
        AppSettings ac = AppSettings.getInstance();
        ac.addToRequestQueue(strReq, tag_string_req);
    }

    public void getUserProfile(final String userId) {
        // Tag used to cancel the request
        String tag_string_req = "updatePassangerState";

        pDialog.setMessage(cx.getString(R.string.uploadDlgMsgUpdatingInfo));
        showDialog();

        StringRequest strReq = new StringRequest(Request.Method.POST,
                URL_getUserProfile, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "getUser Profile Response: " + response);
                hideDialog();

                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");
                    if (!error) {
                        String tmp;
                        tmp = jObj.getString("requests");
                        if(tmp.equalsIgnoreCase("{}")/*requests.length() == 0*/) {
                            //idle state: no requests
                            //EventBus.getDefault().post(new ServiceEvents.UpdateStateEvent(null));
                        } else {
                            JSONObject res = new JSONObject(tmp);
                            //in a task state
                            // show info: from/to/driver
                            UserInfo user = new UserInfo();
                            user.name = res.getString(Constants.ProfileName);
                            user.email = res.getString(Constants.ProfileEmail);
                            user.phone = res.getString(Constants.ProfilePhone);
                            user.image_id = res.getString(Constants.ProfileImageId);
                            user.licenseState = res.getString(Constants.ProfileLicenseState);


                            EventBus.getDefault().post(new ServiceEvents.UpdateProfile(user));


                        }
                    } else {
                        //AppSettings.requestId = -1;
                        // Error occurred in registration. Get the error
                        // message
                        String errorMsg = jObj.getString("error_msg");//Error always false
                        Toast.makeText(cx,
                                errorMsg, Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                hideDialog();
                Log.e(TAG, "getUserProfile Error: " + error.getMessage());
                Toast.makeText(cx,
                        error.getMessage(), Toast.LENGTH_LONG).show();
                //EventBus.getDefault().post(new ServiceEvents.ErrorConnectionEvent());
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("tag", "getUserProfile");
                params.put("userId", userId);
                params.put("type", "Pas");
                return params;
            }
        };
        // Adding request to request queue
        AppSettings ac = AppSettings.getInstance();
        ac.addToRequestQueue(strReq, tag_string_req);
    }

    public void forgotPassword(final String email) {
        // Tag used to cancel the request
        String tag_string_req = "forgotPass";

        pDialog.setMessage("Please wait...");
        showDialog();

        StringRequest strReq = new StringRequest(Request.Method.POST,
                URL_forgotPassword, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Reset Password response: " + response);
                hideDialog();

                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");
                    if (!error) {
                        //reset linke was sent to your
                        new MaterialDialog.Builder(cx)
                                .title(R.string.app_name)
                                .content(cx.getString(R.string.forgotPasswordReset))
                                .positiveText(R.string.ok)
                                .show();
                    } else {
                        // Error occurred in registration. Get the error
                        // message
                        //String errorMsg = jObj.getString("error_msg");
                        int errorno = jObj.getInt("error_no");
                        String errorMsg = phpErrorMsgs.msgMap.get(errorno);
                        if(errorMsg != null) {
                            Toast.makeText(cx, errorMsg, Toast.LENGTH_LONG).show();
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Resetting password Failed " + error.getMessage());
                Toast.makeText(cx,
                        "Resseting Password Failed. Try again later", Toast.LENGTH_LONG).show();
                hideDialog();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting params to register url
                Map<String, String> params = new HashMap<String, String>();
                params.put("tag", "forgotpass");
                params.put("email", email);

                return params;
            }
        };
        // Adding request to request queue
        AppSettings ac = AppSettings.getInstance();
        ac.addToRequestQueue(strReq, tag_string_req);
    }

    public void updateUserPhoto(final Bitmap bitmap, final NetworkImageView networkImageViewUser) {
        // Tag used to cancel the request
        final String tag_string = "modifyUserPhoto";
        final String TAG = tag_string;
        final ProgressDialog pDialog = new ProgressDialog(cx);
        pDialog.setCancelable(false);

        pDialog.setMessage(cx.getString(R.string.uploadDlgMsgUpdatingInfo));
        showDialog();

        final String uploadImage = Utilities.getStringImage(bitmap);
        //AppSettings.setPhoto(uploadImage);
        StringRequest strReq = new StringRequest(Request.Method.POST,
                Constants.URL_uploadImage, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "updatePhoto Response: " + response);
                hideDialog();

                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");
                    if (!error) {
                        String newId = jObj.getString("newid");
                        AppSettings.setPhotoId(newId);
                        {
                            //final String IMAGE_URL = "http://developer.android.com/images/training/system-ui.png";
                            ImageLoader mImageLoader = AppSettings.getInstance().getImageLoader();
                            networkImageViewUser.setImageUrl(Constants.URL_downloadUserPhoto+newId, mImageLoader);
                        }
                        //photoImageView.setImageBitmap(bitmap);
                    } else {
                        // Error occurred in registration. Get the error
                        // message
                        //String errorMsg = jObj.getString("error_msg");
                        int errorno = jObj.getInt("error_no");
                        phpErrorMessages phpErrorMsgs = AppSettings.getInstance().getPhpErrorMsg();
                        String errorMsg = phpErrorMsgs.msgMap.get(errorno);
                        Toast.makeText(cx,
                                errorMsg, Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "update User Info Error: " + error.getMessage());
                Toast.makeText(cx,
                        error.getMessage(), Toast.LENGTH_LONG).show();
                hideDialog();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting params to register url
                Map<String, String> params = new HashMap<String, String>();
                params.put("tag", tag_string);
                params.put("image", uploadImage);
                params.put("uid", AppSettings.getUid());
                return params;
            }
        };
        // Adding request to request queue
        AppSettings ac = AppSettings.getInstance();
        ac.addToRequestQueue(strReq, tag_string);
    }

    public void updateUserInfo(final String username, final String useremail, final String userphone) {
        // Tag used to cancel the request
        String tag_string = "modifyUserInfo";

        pDialog.setMessage(cx.getString(R.string.uploadDlgMsgUpdatingInfo));
        showDialog();

        StringRequest strReq = new StringRequest(Request.Method.POST,
                Constants.URL_updateUserInfo, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "updateTReq Response: " + response);
                hideDialog();

                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");
                    if (!error) {
                        AppSettings.setPhone(userphone);
                        AppSettings.setEmail(useremail);
                        AppSettings.setName(username);
                    } else {
                        // Error occurred in registration. Get the error
                        // message
                        //String errorMsg = jObj.getString("error_msg");
                        int errorno = jObj.getInt("error_no");
                        String errorMsg = phpErrorMsgs.msgMap.get(errorno);
                        Toast.makeText(cx,
                                errorMsg, Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "update User Info Error: " + error.getMessage());
                Toast.makeText(cx,
                        error.getMessage(), Toast.LENGTH_LONG).show();
                hideDialog();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting params to register url
                Map<String, String> params = new HashMap<String, String>();
                params.put("tag", "updateUserInfo");
                params.put("username", username);
                params.put("useremail", useremail);
                params.put("userphone", userphone);
                params.put("uid", AppSettings.getUid());
                return params;
            }
        };
        // Adding request to request queue
        AppSettings ac = AppSettings.getInstance();
        ac.addToRequestQueue(strReq, tag_string);
    }

    public void getPassangerState(final String passangerId) {
        // Tag used to cancel the request
        String tag_string_req = "updatePassangerState";

        pDialog.setMessage(cx.getString(R.string.uploadDlgMsgUpdating));
        showDialog();

        StringRequest strReq = new StringRequest(Request.Method.POST,
                URL_getPassangerState, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "getPassangerState Response: " + response);
                hideDialog();

                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");
                    if (!error) {
                        String tmp = jObj.getString("stats");
                        String countDrv="";
                        String countPas="";
                        if(!tmp.equalsIgnoreCase("{}")) {
                            JSONObject stats = new JSONObject(tmp);
                            countDrv = stats.getString("Drv");
                            countPas = stats.getString("Pas");
                        }

                        String tmpAnn = jObj.getString("announcements");
                        String annImage = "", annText="", ver="";
                        if(!tmpAnn.equalsIgnoreCase("{}")){
                            JSONObject anns = new JSONObject(tmpAnn);
                            annImage = anns.getString("annimage");
                            annText = anns.getString("anntext");
                            ver = anns.getString("ver");
                        }
                        EventBus.getDefault().post(new ServiceEvents.UpdateAnnouncement(ver, annImage, annText, countDrv, countPas));

                        tmp = jObj.getString("requests");
                        if(tmp.equalsIgnoreCase("{}")/*requests.length() == 0*/) {
                            //idle state: no requests
                            EventBus.getDefault().post(new ServiceEvents.UpdateStateEvent(null));
                        } else {
                            JSONObject requests = new JSONObject(tmp);
                            //in a task state
                            // show info: from/to/driver
                            JSONObject c = requests;//.getJSONObject(0);
                            TRequestObj treq = new TRequestObj();
                            treq.idx = c.getInt(Constants.RequestsIdx);
                            treq.passangerName = c.getString(Constants.RequestsPassangerName);
                            treq.passanger_id = passangerId;//c.getString(Constants.RequestsPassangerId);
                            treq.fromLat = c.getDouble(Constants.RequestsFromLat);
                            treq.fromLong = c.getDouble(Constants.RequestsFromLong);
                            treq.toLat = c.getDouble(Constants.RequestsToLat);
                            treq.toLong = c.getDouble(Constants.RequestsToLong);
                            treq.fromDesc = c.getString(Constants.RequestsFromDesc);
                            treq.toDesc = c.getString(Constants.RequestsToDesc);

                            treq.driverId = c.getString(Constants.RequestsDriverId);
                            treq.status = c.getString(Constants.RequestsStatus);
                            treq.time = c.getString(Constants.RequestsTime);
                            treq.secondsToNow = c.getString(Constants.RequestsSecondsToNow);
                            treq.driverName = c.getString(Constants.RequestsDriverName);
                            treq.driverPhotoUrl = Constants.URL_downloadUserPhoto + c.getString(Constants.RequestsDriverPhotoId);
                            treq.driverInfo = cx.getString(R.string.uploadDriverInfo) + c.getString(Constants.RequestsDriverEmail);
                            treq.driverPhone = c.getString(Constants.RequestsDriverPhone);
                            treq.suggestedFee = c.getString(Constants.RequestsSuggestedFee);
                            treq.noOfPassangers = c.getString(Constants.RequestsNoOfPassangers);
                            treq.additionalNotes = c.getString(Constants.RequestsAdditionalNotes);

                            EventBus.getDefault().post(new ServiceEvents.UpdateStateEvent(treq));

                            DriverInfo driverInfo = new DriverInfo();
                            driverInfo.latitude = c.getDouble("drvLat");
                            driverInfo.longitude = c.getDouble("drvLng");
                            if(driverInfo.latitude != -1) {
                                EventBus.getDefault().post(new ServiceEvents.DriverLocationUpdate(driverInfo));
                            }
                        }
                    } else {
                        //AppSettings.requestId = -1;
                        // Error occurred in registration. Get the error
                        // message
                        int errorNo = jObj.getInt("error_no");
                        if(errorNo == 1001) {
                            EventBus.getDefault().post(new ServiceEvents.forceLogout());
                        }
                        String errorMsg = jObj.getString("error_msg");//Error always false
                        Toast.makeText(cx,
                                errorMsg, Toast.LENGTH_LONG).show();

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    EventBus.getDefault().post(new ServiceEvents.ErrorConnectionEvent());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "getPassangerState Error: " + error.getMessage());
                Toast.makeText(cx,
                        error.getMessage(), Toast.LENGTH_LONG).show();
                hideDialog();
                EventBus.getDefault().post(new ServiceEvents.ErrorConnectionEvent());
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("tag", "getPassangerState");
                params.put("passangerId", passangerId);
                String tmpLang = cx.getResources().getString(R.string.applanguage);
                params.put("lang", tmpLang);
                return params;
            }
        };
        // Adding request to request queue
        AppSettings ac = AppSettings.getInstance();
        ac.addToRequestQueue(strReq, tag_string_req);
    }

    public void setTRequestState(final String requestId, final String state) {
        // Tag used to cancel the request
        String tag_string_req = "setTRequestState";

        pDialog.setMessage(cx.getString(R.string.uploadDlgMsgUpdatingRqst));
        showDialog();

        StringRequest strReq = new StringRequest(Request.Method.POST,
                Constants.URL_updateTRequest, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "updateTReq Response: " + response);
                hideDialog();

                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");
                    if (!error) {

                    } else {
                        // Error occurred in registration. Get the error
                        // message
                        //String errorMsg = jObj.getString("error_msg");
                        int errorno = jObj.getInt("error_no");
                        String errorMsg = phpErrorMsgs.msgMap.get(errorno);
                        Toast.makeText(cx,
                                errorMsg, Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "addRequest Error: " + error.getMessage());
                Toast.makeText(cx,
                        error.getMessage(), Toast.LENGTH_LONG).show();
                hideDialog();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting params to register url
                Map<String, String> params = new HashMap<String, String>();
                params.put("tag", "updateTRequestByPassenger");
                params.put("requestId", requestId);
                params.put("state", state);
                params.put("drvId", "-1");

                return params;
            }
        };
        // Adding request to request queue
        AppSettings ac = AppSettings.getInstance();
        ac.addToRequestQueue(strReq, tag_string_req);
    }

    public void addTRequest(final String passangerId, final String email,
                            final String lat1, final String long1, final String lat2, final String long2,
                            final String fromDescription, final String toDescription,
                            final String treqPhone, final String suggestedFee, final String noOfPassangers, final String additionalNotes)
    {
        // Tag used to cancel the request
        String tag_string_req = "addTRequest";

        pDialog.setMessage(cx.getString(R.string.uploadDlgMsgUploadingRqst));
        showDialog();

        StringRequest strReq = new StringRequest(Request.Method.POST,
                URL_addTRequest, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "AddTReq Response: " + response);
                hideDialog();

                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");
                    if (!error) {
                        AppSettings.requestId = jObj.getInt("requestId");
                    } else {
                        AppSettings.requestId = -1;
                        // Error occurred in registration. Get the error
                        // message
                        //String errorMsg = jObj.getString("error_msg");
                        int errorno = jObj.getInt("error_no");
                        if(errorno==11) {
                            EventBus.getDefault().post(new ServiceEvents.GetPassengerStateEvent());
                        } else {
                            String errorMsg = phpErrorMsgs.msgMap.get(errorno);
                            //Toast.makeText(cx,
                            //errorMsg, Toast.LENGTH_LONG).show();
                            hideDialog();
                            EventBus.getDefault().post(new ServiceEvents.CancelTRequests(errorMsg));
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    hideDialog();
                    EventBus.getDefault().post(new ServiceEvents.CancelTRequests(e.getMessage()));
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "addRequest Error: " + error.getMessage());
                //Toast.makeText(cx,
                  //      error.getMessage(), Toast.LENGTH_LONG).show();
                hideDialog();
                EventBus.getDefault().post(new ServiceEvents.CancelTRequests(error.getMessage()));
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting params to register url
                Map<String, String> params = new HashMap<String, String>();
                params.put("tag", "addTRequest");
                params.put("email", email);
                params.put("passangerId", passangerId);
                params.put("lat", lat1);
                params.put("long", long1);
                params.put("lat2", lat2);
                params.put("long2", long2);
                params.put("fromDesc", fromDescription);
                params.put("toDesc", toDescription);
                params.put("treqPhone", treqPhone);
                params.put("suggestedFee", suggestedFee);
                params.put("noOfPassangers", noOfPassangers);
                params.put("additionalNotes", additionalNotes);
                return params;
            }
        };
        // Adding request to request queue
        AppSettings ac = AppSettings.getInstance();
                ac.addToRequestQueue(strReq, tag_string_req);
    }

    public void getNearbyDrivers(final Location loc) {
        // Tag used to cancel the request
        String tag_string_req = "getNearbyDrivers";
        final double lat, lng;
        lat = loc.getLatitude();
        lng = loc.getLongitude();

        pDialog.setMessage(cx.getString(R.string.uploadDlgMsgUpdatingRqst));
        showDialog();

        StringRequest strReq = new StringRequest(Request.Method.POST,
                Constants.URL_getDrivers, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "getDrivers Response: " + response);
                hideDialog();

                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");
                    if (!error) {
                        JSONArray requests = jObj.getJSONArray("results");
                        //double  drvLat[], drvLong[];
                        int     drvCount = requests.length();
                        //drvLat = new double[drvCount];
                        //drvLong = new double[drvCount];

                        DriverInfo[] drvInfo = new DriverInfo[drvCount];
                        for(int i=0;i<drvCount;i++){
                            DriverInfo tmpDrvInfo = new DriverInfo();

                            JSONObject c = requests.getJSONObject(i);
                            tmpDrvInfo.driverId = c.getInt("drvId");
                            tmpDrvInfo.updatedAtTime = c.getString("loc_time");
                            tmpDrvInfo.name = c.getString("name");
                            tmpDrvInfo.phone = c.getString("phone");
                            tmpDrvInfo.imageId = c.getInt("imageId");
                            tmpDrvInfo.latitude = c.getDouble("lat");
                            tmpDrvInfo.longitude = c.getDouble("long");
                            drvInfo[i] = tmpDrvInfo;
                        }
                        hideDialog();
                        double lng_d = jObj.getDouble("lng_d");
                        double lat_d = jObj.getDouble("lat_d");

                        EventBus.getDefault().post(new ServiceEvents.updateDrivers(drvCount, drvInfo, lat_d, lng_d, loc));
                    } else {
                        Toast.makeText(cx, cx.getString(R.string.str_noDrivers)
                                , Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "getDrivers Error: " + error.getMessage());
                Toast.makeText(cx,
                        error.getMessage(), Toast.LENGTH_LONG).show();
                hideDialog();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting params to register url
                Map<String, String> params = new HashMap<String, String>();
                params.put("tag", "getDrivers");
                params.put("pass_long", Double.toString(lng));
                params.put("pass_lat", Double.toString(lat));

                return params;
            }
        };
        // Adding request to request queue
        AppSettings ac = AppSettings.getInstance();
        ac.addToRequestQueue(strReq, tag_string_req);
    }

    private void showDialog() {
        if(Utilities.checkContextIsFinishing(cx)) {
            return;
        }
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }
}
