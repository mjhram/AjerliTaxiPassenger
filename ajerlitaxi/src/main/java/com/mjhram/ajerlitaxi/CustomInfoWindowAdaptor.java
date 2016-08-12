package com.mjhram.ajerlitaxi;

import android.app.Activity;
import android.graphics.Color;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.mjhram.ajerlitaxi.common.DriverInfo;

import java.util.HashMap;

/**
 * Created by mohammad.haider on 8/12/2016.
 */
class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {
    private final View mContents;
    private HashMap<Marker, DriverInfo> markerInfoHash;

    CustomInfoWindowAdapter(Activity theActivity, HashMap<Marker, DriverInfo> aMarkerInfoHash) {
        mContents = theActivity.getLayoutInflater().inflate(R.layout.custom_info_contents, null);
        markerInfoHash = aMarkerInfoHash;
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {
        if(render(marker, mContents) == false)
            return null;
        return mContents;
    }

    private boolean render(Marker marker, View view) {
        DriverInfo drvInfo = markerInfoHash.get(marker);
        if(drvInfo == null) {
            return false;
        }

        //((ImageView) view.findViewById(R.id.idDrvImage)).setImageResource(badge);
        String title = marker.getTitle();
        TextView titleUi = ((TextView) view.findViewById(R.id.markertitle));
        if (title != null) {
            // Spannable string allows us to edit the formatting of the text.
            SpannableString titleText = new SpannableString(title);
            titleText.setSpan(new ForegroundColorSpan(Color.RED), 0, titleText.length(), 0);
            titleUi.setText("\u200e"+titleText);
        } else {
            titleUi.setText("");
        }

        String snippet = drvInfo.phone;//marker.getSnippet();
        TextView snippetUi = ((TextView) view.findViewById(R.id.markersnippet));
        if (snippet != null && snippet.length() > 0) {
            SpannableString snippetText = new SpannableString(snippet);
            //snippetText.setSpan(new ForegroundColorSpan(Color.MAGENTA), 0, 10, 0);
            snippetText.setSpan(new ForegroundColorSpan(Color.BLUE), 0, snippet.length(), 0);
            snippetUi.setText(snippetText);
        } else {
            snippetUi.setText("");
        }
        return true;
    }
}
