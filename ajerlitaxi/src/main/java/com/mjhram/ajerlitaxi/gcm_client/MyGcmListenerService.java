/**
 * Copyright 2015 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mjhram.ajerlitaxi.gcm_client;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.mjhram.ajerlitaxi.GpsMainActivity;
import com.mjhram.ajerlitaxi.R;
import com.mjhram.ajerlitaxi.common.DriverInfo;
import com.mjhram.ajerlitaxi.common.events.ServiceEvents;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

import de.greenrobot.event.EventBus;

public class MyGcmListenerService extends FirebaseMessagingService {

    private static final String TAG = "MyGcmListenerService";
    @Override
    public void onMessageReceived(RemoteMessage rMessage){
        String from = rMessage.getFrom();
        Map data = rMessage.getData();

        String message = data.get("message").toString();

        if (from !=null && from.startsWith("/topics/")) {
            Log.d(TAG, "From: " + from);
            // message received from some topic.
        }
        if(message != null){
            Log.d(TAG, "Message: " + message);
            try {
                String tag;
                JSONObject msgObj;

                msgObj = new JSONObject(message);
                tag = msgObj.getString("tag");

                if (tag.equalsIgnoreCase("drvId")) {
                    int drvId = msgObj.getInt("data");//Integer.parseInt(message);
                    EventBus.getDefault().post(new ServiceEvents.TRequestAccepted(drvId));
                    message = getString(R.string.gcmTReqAccepted); //Integer.toString(drvId);
                } else if (tag.equalsIgnoreCase("treqState")) {
                    String state = msgObj.getString("data");//Integer.parseInt(message);
                    EventBus.getDefault().post(new ServiceEvents.TRequestUpdated(state));
                    message = getString(R.string.gcmTReqUpdated);//getString(R.string.gcmClientReqState) + state;
                } else if (tag.equalsIgnoreCase("drvLoc")) {
                    DriverInfo driverInfo = new DriverInfo();
                    driverInfo.driverId = msgObj.getInt("drvId");
                    driverInfo.latitude = msgObj.getDouble("lat");
                    driverInfo.longitude = msgObj.getDouble("lng");

                    EventBus.getDefault().post(new ServiceEvents.DriverLocationUpdate(driverInfo));
                    return;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        if(message != null) {
            sendNotification(message);
        }
    }

    /**
     * Called when message is received.
     *
     * @param from SenderID of the sender.
     * @param data Data bundle containing message data as key/value pairs.
     *             For Set of keys use data.keySet().

    // [START receive_message]
    @Override
    public void onMessageReceived(String from, Bundle data) {
        String message = data.getString("message");

        if (from !=null && from.startsWith("/topics/")) {
            Log.d(TAG, "From: " + from);
            // message received from some topic.
        }
        if(message != null){
            Log.d(TAG, "Message: " + message);
            try {
                String tag;
                JSONObject msgObj;

                msgObj = new JSONObject(message);
                tag = msgObj.getString("tag");

                if (tag.equalsIgnoreCase("drvId")) {
                    int drvId = msgObj.getInt("data");//Integer.parseInt(message);
                    EventBus.getDefault().post(new ServiceEvents.TRequestAccepted(drvId));
                    message = getString(R.string.gcmTReqAccepted); //Integer.toString(drvId);
                } else if (tag.equalsIgnoreCase("treqState")) {
                    String state = msgObj.getString("data");//Integer.parseInt(message);
                    EventBus.getDefault().post(new ServiceEvents.TRequestUpdated(state));
                    message = getString(R.string.gcmTReqUpdated);//getString(R.string.gcmClientReqState) + state;
                } else if (tag.equalsIgnoreCase("drvLoc")) {
                    DriverInfo driverInfo = new DriverInfo();
                    driverInfo.driverId = msgObj.getInt("drvId");
                    driverInfo.latitude = msgObj.getDouble("lat");
                    driverInfo.longitude = msgObj.getDouble("lng");

                    EventBus.getDefault().post(new ServiceEvents.DriverLocationUpdate(driverInfo));
                    return;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        if(message != null) {
            sendNotification(message);
        }
        // [END_EXCLUDE]
    }
    // [END receive_message]
*/
    /**
     * Create and show a simple notification containing the received GCM message.
     *
     * @param message GCM message received.
     */
    private void sendNotification(String message) {
        Intent intent = new Intent(this, GpsMainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.circle_green)
                .setContentTitle(getString(R.string.gcmClientGcmMsg))
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }
}
