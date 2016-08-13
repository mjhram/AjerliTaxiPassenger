package com.mjhram.ajerlitaxi.views.component;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.google.android.gms.maps.model.Marker;

/**
 * Created by mohammad.haider on 8/13/2016.
 */
public class MarkerNetworkImageView extends NetworkImageView {
    private int mErrorResId;
    private Marker marker;

    public MarkerNetworkImageView(Context context) {
        super(context);
    }

    public MarkerNetworkImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MarkerNetworkImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setMarkerAndImageUrl(Marker aMarker, String url, ImageLoader imageLoader) {
        setImageUrl(url, imageLoader);
        marker = aMarker;
    }

    @Override
    public void setErrorImageResId(int errorImage) {
        mErrorResId = errorImage;
        super.setErrorImageResId(errorImage);
    }

    @Override
    public void setImageResource(int resId) {
        if (resId == mErrorResId) {
            // TODO Handle the error here
        }
        super.setImageResource(resId);
    }

    @Override
    public void setImageBitmap(Bitmap bm) {
        // TODO Handle the success here
        super.setImageBitmap(bm);
        if(marker == null)
            return;
        if(marker.isInfoWindowShown ()) {
            marker.showInfoWindow();
        }
    }
}
