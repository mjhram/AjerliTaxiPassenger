package com.mjhram.ajerlitaxi;

import android.app.Activity;
import android.graphics.Color;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.mjhram.ajerlitaxi.common.AppSettings;
import com.mjhram.ajerlitaxi.common.DriverInfo;
import com.mjhram.ajerlitaxi.helper.Constants;
import com.mjhram.ajerlitaxi.views.component.MarkerNetworkImageView;

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

        String title = marker.getTitle();
        TextView titleUi = ((TextView) view.findViewById(R.id.markertitle));
        if (title != null) {
            // Spannable string allows us to edit the formatting of the text.
            SpannableString titleText = new SpannableString("\u200e"+title);
            titleText.setSpan(new ForegroundColorSpan(Color.RED), 0, titleText.length(), 0);
            titleUi.setText(titleText);
        } else {
            titleUi.setText("");
        }

        TextView snippetUi = ((TextView) view.findViewById(R.id.markersnippet));
        String snippet = view.getContext().getString(R.string.str_callDriver);//drvInfo.phone;//marker.getSnippet();
        //if (snippet != null && snippet.length() > 0)
        if(drvInfo.phone != null && !(drvInfo.phone.isEmpty()))
        {
            snippetUi.setVisibility(View.VISIBLE);
            SpannableString snippetText = new SpannableString("\u200e"+snippet);
            //snippetText.setSpan(new ForegroundColorSpan(Color.MAGENTA), 0, 10, 0);
            snippetText.setSpan(new ForegroundColorSpan(Color.BLUE), 0, snippetText.length(), 0);
            snippetUi.setText(snippetText);
        }
        else {
            snippetUi.setVisibility(View.GONE);
            snippetUi.setText("");
        }

        TextView locTimeUi = ((TextView) view.findViewById(R.id.locTime));
        String updatedAtTime = "\u200e"+/*view.getContext().getString(R.string.lastSeen) +*/ drvInfo.updatedAtTime;//marker.getSnippet();
        if(drvInfo.updatedAtTime != null && !(drvInfo.updatedAtTime.isEmpty()))
        {
            locTimeUi.setVisibility(View.VISIBLE);
            //SpannableString locTimeText = new SpannableString("\u200e"+updatedAtTime);
            //locTimeText.setSpan(new ForegroundColorSpan(Color.BLUE), 0, locTimeText.length(), 0);
            locTimeUi.setText(updatedAtTime);
        }
        else {
            locTimeUi.setVisibility(View.GONE);
            locTimeUi.setText("");
        }
        //image:
        MarkerNetworkImageView markerNwIv = ((MarkerNetworkImageView) view.findViewById(R.id.idInfoWndDrvImage));
        if(drvInfo.imageId == 0) {
            markerNwIv.setVisibility(View.GONE);
        } else {
            markerNwIv.setVisibility(View.VISIBLE);
            markerNwIv.setDefaultImageResId(R.drawable.portraitplaceholder);
                //final String IMAGE_URL = "http://developer.android.com/images/training/system-ui.png";
                ImageLoader mImageLoader = AppSettings.getInstance().getImageLoader();
                markerNwIv.setMarkerAndImageUrl(marker, Constants.URL_downloadUserPhoto + drvInfo.imageId, mImageLoader);
        }
        return true;
    }
}
