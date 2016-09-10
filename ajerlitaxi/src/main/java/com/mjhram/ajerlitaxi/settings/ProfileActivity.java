package com.mjhram.ajerlitaxi.settings;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.mjhram.ajerlitaxi.R;
import com.mjhram.ajerlitaxi.common.AppSettings;
import com.mjhram.ajerlitaxi.common.EventBusHook;
import com.mjhram.ajerlitaxi.common.UserInfo;
import com.mjhram.ajerlitaxi.common.events.ServiceEvents;
import com.mjhram.ajerlitaxi.helper.Constants;
import com.mjhram.ajerlitaxi.helper.UploadClass;

import java.io.IOException;

import de.greenrobot.event.EventBus;

public class ProfileActivity extends AppCompatActivity {
    TextView    tv_username, tv_useremail;
    EditText     edit_phone;
    //ImageView   photoImageView;
    private NetworkImageView networkImageViewUser;

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save the user's current game state
        savedInstanceState.putString("USER_NAME", tv_username.getText().toString());
        savedInstanceState.putString("EMAIL", tv_useremail.getText().toString());
        String tmp = edit_phone.getText().toString();
        savedInstanceState.putString("PHONE", tmp);
        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        // Restore UI state from the savedInstanceState.
        // This bundle has also been passed to onCreate.
        String tmp;
        tmp = savedInstanceState.getString("USER_NAME");
        tv_username.setText(tmp);
        tmp = savedInstanceState.getString("EMAIL");
        tv_useremail.setText(tmp);
        tmp = savedInstanceState.getString("PHONE");
        edit_phone.setText(tmp);
    }

    @Override
    protected void onPause() {
        EventBus.getDefault().unregister(this);
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                        */
                UploadClass uc = new UploadClass(ProfileActivity.this);
                uc.updateUserInfo(tv_username.getText().toString(), tv_useremail.getText().toString(), edit_phone.getText().toString());
            }
        });

        tv_username = (TextView) findViewById(R.id.profile_username);
        tv_useremail =(TextView) findViewById(R.id.profile_email);
        edit_phone =(EditText) findViewById(R.id.profile_phone);
        networkImageViewUser = (NetworkImageView) findViewById(R.id.profilePhoto);

        networkImageViewUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showFileChooser();
            }
        });

        /*photoImageView=(ImageView) findViewById(R.id.profilePhoto);
        photoImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showFileChooser();
            }
        });*/
        tv_username.setText(AppSettings.getName());
        tv_useremail.setText(AppSettings.getEmail());
        edit_phone.setText(AppSettings.getPhone());
        {
            //final String IMAGE_URL = "http://developer.android.com/images/training/system-ui.png";
            ImageLoader mImageLoader = AppSettings.getInstance().getImageLoader();
            networkImageViewUser.setImageUrl(Constants.URL_downloadUserPhoto+AppSettings.getPhotoId(), mImageLoader);
        }
        UploadClass uc = new UploadClass(this);
        uc.getUserProfile(AppSettings.getUid());
    }



    /**
     * @param encodedString
     * @return bitmap (from given string)
     */
    private Bitmap getImageFromString(String encodedString){
        try{
            byte [] encodeByte=Base64.decode(encodedString,Base64.DEFAULT);
            Bitmap bitmap= BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
            return bitmap;
        }catch(Exception e){
            e.getMessage();
            return null;
        }
    }



    private int PICK_IMAGE_REQUEST = 1;
    private void showFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Photo"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {

            Uri filePath = data.getData();
            try {
                Bitmap profilePhotoBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                UploadClass uc = new UploadClass(this);
                uc.updateUserPhoto(profilePhotoBitmap, networkImageViewUser);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void showDialog(ProgressDialog pDialog) {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog(ProgressDialog pDialog) {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }

    @EventBusHook
    public void onEventMainThread(ServiceEvents.UpdateProfile updateProfileEvent){
        UserInfo user = updateProfileEvent.user;
        tv_username.setText(user.name);
        tv_useremail.setText(user.email);
        edit_phone.setText(user.phone);
        {
            ImageLoader mImageLoader = AppSettings.getInstance().getImageLoader();
            networkImageViewUser.setImageUrl(Constants.URL_downloadUserPhoto+user.image_id, mImageLoader);
        }
        AppSettings.setEmail(user.email);
        AppSettings.setPhone(user.phone);
        AppSettings.setName(user.name);
    }

}
