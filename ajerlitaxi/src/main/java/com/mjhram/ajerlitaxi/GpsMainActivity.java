/*******************************************************************************
 * This file is part of GPSLogger for Android.
 *
 * GPSLogger for Android is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * GPSLogger for Android is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with GPSLogger for Android.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/

package com.mjhram.ajerlitaxi;


import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.firebase.iid.FirebaseInstanceId;
import com.heinrichreimersoftware.materialdrawer.DrawerView;
import com.heinrichreimersoftware.materialdrawer.structure.DrawerItem;
import com.mjhram.ajerlitaxi.Faq.Faqtivity;
import com.mjhram.ajerlitaxi.common.AppSettings;
import com.mjhram.ajerlitaxi.common.DriverInfo;
import com.mjhram.ajerlitaxi.common.EventBusHook;
import com.mjhram.ajerlitaxi.common.Session;
import com.mjhram.ajerlitaxi.common.TRequestObj;
import com.mjhram.ajerlitaxi.common.Utilities;
import com.mjhram.ajerlitaxi.common.events.CommandEvents;
import com.mjhram.ajerlitaxi.common.events.ServiceEvents;
import com.mjhram.ajerlitaxi.common.slf4j.SessionLogcatAppender;
import com.mjhram.ajerlitaxi.helper.Constants;
import com.mjhram.ajerlitaxi.helper.UploadClass;
import com.mjhram.ajerlitaxi.login_register.LoginActivity;
import com.mjhram.ajerlitaxi.state.passengerState_assigned;
import com.mjhram.ajerlitaxi.state.passengerState_context;
import com.mjhram.ajerlitaxi.state.passengerState_idle;
import com.mjhram.ajerlitaxi.state.passengerState_reconnect;
import com.mjhram.ajerlitaxi.views.GenericViewFragment;

import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

import de.greenrobot.event.EventBus;


public class GpsMainActivity extends GenericViewFragment
        implements
        //Toolbar.OnMenuItemClickListener,
        //ActionBar.OnNavigationListener,
        GoogleApiClient.ConnectionCallbacks,
        LocationListener,
        OnMapReadyCallback
        {

    public static AtomicInteger activitiesLaunched = new AtomicInteger(0);

    private static boolean userInvokedUpload;
    private static Intent serviceIntent;
    private ActionBarDrawerToggle drawerToggle;
    private org.slf4j.Logger tracer;
    public Button btnPickDrop;
    //private static int pickdropState = 0;//1=pick, 2=drop,
    //public static android.support.v4.app.FragmentManager fragmentManager;
    public GoogleMap googleMap;
    private  int    countOfDrivers=0;
    private Marker[] nearbyDrivers = null;
    //private Circle mapsSearchCircle = null;
    private Polygon searchPolygon;
    public HashMap<Marker, DriverInfo> mMarkerInfoHash = new HashMap<>();

    //private ActionProcessButton actionButton;
    public GoogleApiClient mGoogleApiClient;
    //private GoogleMap map;
    public Marker fromMarker, toMarker, driverMarker;
    public String fromDesc, toDesc;
    public CountDownTimer countDownTimer;//
    //private BroadcastReceiver mRegistrationBroadcastReceiver;
    public RelativeLayout driverInfoLayout;
    public TextView txtDriverName;
    public TextView txtDriverInfo;
    public TextView btnDriverPhone;
    public NetworkImageView networkImageViewDriver;

    private RelativeLayout relativeLayoutAds;
    private ImageView btnAdsX;
    private NetworkImageView networkivAds;
    private TextView textviewAds;
    public  RelativeLayout helpOverlayLayout;

    public String treqPhone, suggestedFee, noOfPassangers, additionalNotes;

    public passengerState_context passengerStateContext;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (activitiesLaunched.incrementAndGet() > 1) {
            finish();
        }
        super.onCreate(savedInstanceState);

        tracer = LoggerFactory.getLogger(GpsMainActivity.class.getSimpleName());

        loadPresetProperties();

        setContentView(R.layout.activity_gps_main);

        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        btnPickDrop = (Button) findViewById(R.id.btnPickDrop);
        driverInfoLayout = (RelativeLayout) findViewById(R.id.driverLayout);
        driverInfoLayout.setVisibility(View.INVISIBLE);
        txtDriverName = (TextView) findViewById(R.id.textViewDriverName);
        txtDriverInfo = (TextView) findViewById(R.id.textViewDriverInfo);
        networkImageViewDriver = (NetworkImageView) findViewById(R.id.imageViewDriver);
        helpOverlayLayout = (RelativeLayout) findViewById(R.id.helpoverly_layout);
        helpOverlayLayout.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                //helpOverlayLayout.setVisibility(View.GONE);
                return false;
            }
        });
        if(AppSettings.isShowHelpOverly()) {
            helpOverlayLayout.setVisibility(View.VISIBLE);
        } else {
            helpOverlayLayout.setVisibility(View.GONE);
        }
        btnDriverPhone = (Button) findViewById(R.id.btnDriverPhone);
        btnDriverPhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri number = Uri.parse("tel:"+btnDriverPhone.getText());
                Intent callIntent = new Intent(Intent.ACTION_DIAL, number);
                startActivity(callIntent);
            }
        });

        /*BottomBar bottomBarMain = (BottomBar) findViewById(R.id.bottomBarMain);

        bottomBarMain.setOnTabSelectListener(new OnTabSelectListener() {
            @Override
            public void onTabSelected(@IdRes int tabId) {
                tabSelected(tabId);

            }

        });

        bottomBarMain.setOnTabReselectListener(new OnTabReselectListener() {
            @Override
            public void onTabReSelected(@IdRes int tabId) {
                tabSelected(tabId);
            }
        });*/

        relativeLayoutAds = (RelativeLayout) findViewById(R.id.relativeLayoutAds);
        btnAdsX = (ImageButton) findViewById(R.id.btnAdsX);
        btnAdsX.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                relativeLayoutAds.setVisibility(View.GONE);
            }
        });
        textviewAds = (TextView) findViewById(R.id.textview_ads);

        networkivAds = (NetworkImageView) findViewById(R.id.networkivAds);
        /*{
            //final String IMAGE_URL = "http://developer.android.com/images/training/system-ui.png";
            ImageLoader mImageLoader = AppSettings.getInstance().getImageLoader();
            networkivAds.setImageUrl(Constants.URL_ads+".jpg", mImageLoader);
        }

        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                //new TReq arrived
                int drvId = intent.getIntExtra("drvId", -1);
                //updateRequests(treqId);
            }
        };*/
        buildGoogleApiClient();
        /*mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                //.addConnectionCallbacks(this)
                //.addOnConnectionFailedListener(this)
                .build();*/

        SetUpToolbar();
        SetUpNavigationDrawer();
        /*StartAndBindService();
        if(AppSettings.shouldStartLoggingOnAppLaunch()){
            tracer.debug("Start logging on app launch");
            EventBus.getDefault().postSticky(new CommandEvents.RequestStartStop(true));
        }*/
        passengerStateContext = new passengerState_context(this);

        String regId = AppSettings.getRegId();
        if(regId == null || regId.isEmpty()) {
            String token = FirebaseInstanceId.getInstance().getToken();
            AppSettings.setRegId(token);
            regId = token;
        }
        if(AppSettings.shouldUploadRegId && regId.isEmpty()==false) {
            AppSettings.shouldUploadRegId = false;
            UploadClass uc = new UploadClass(this);
            uc.updateRegId(AppSettings.getUid(), regId);
        }
        checkGpsEnabled();
        //UploadClass uc = new UploadClass(this);
        //uc.getPassangerState(AppSettings.getUid());
    }

            @Override
    public void onMapReady(GoogleMap map) {
        map.setMyLocationEnabled(true);
        googleMap = map;
        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                                            @Override
                                            public void onMapClick(LatLng point) {
                                                Log.d("Map","Map clicked");
                                                passengerStateContext.mapClicked();

                                                /*if(pickdropState==0) {
                                                    Location location = LocationServices.FusedLocationApi.getLastLocation(
                                                            mGoogleApiClient);
                                                    if (location != null) {
                                                        UploadClass uc = new UploadClass(GpsMainActivity.this);
                                                        uc.getNearbyDrivers(location);
                                                    } else {
                                                        Toast.makeText(getApplicationContext(),getString(R.string.str_noLocation)
                                                                , Toast.LENGTH_LONG).show();
                                                    }
                                                }*/
                                            }
                                        }

        );
        googleMap.setInfoWindowAdapter(new CustomInfoWindowAdapter(this, mMarkerInfoHash));
        googleMap.setOnMarkerClickListener(new MarkerClickListener());
        googleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                DriverInfo tmpDrvInfo = mMarkerInfoHash.get(marker);
                if(tmpDrvInfo != null && tmpDrvInfo != null && !(tmpDrvInfo.phone.isEmpty())) {
                    Intent intent = new Intent(Intent.ACTION_DIAL);

                    intent.setData(Uri.parse("tel:" + tmpDrvInfo.phone));
                    startActivity(intent);
                } else {
                    Toast.makeText(GpsMainActivity.this, GpsMainActivity.this.getString(R.string.str_noPhone), Toast.LENGTH_LONG).show();
                }
            }
        });
        //int h = Utilities.dpToPx(this, 50);//btnPickDrop.getHeight();
        //map.setPadding(0,0,0,h);
        map.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
                //helpOverlayLayout.setVisibility(View.GONE);
                Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                if(location == null) {
                    Toast.makeText(getApplicationContext(), getString(R.string.str_noLocation)
                            , Toast.LENGTH_LONG).show();
                    helpOverlayLayout.setVisibility(View.VISIBLE);
                } else {
                    //helpOverlayLayout.setVisibility(View.GONE);
                }
                return false;
            }
        });
        /*Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (location != null)
        {
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(location.getLatitude(), location.getLongitude()), 13));
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(new LatLng(location.getLatitude(), location.getLongitude()))      // Sets the center of the map to location user
                    .zoom(17)                   // Sets the zoom
                    .bearing(90)                // Sets the orientation of the camera to east
                    .tilt(40)                   // Sets the tilt of the camera to 30 degrees
                    .build();                   // Creates a CameraPosition from the builder
            map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

        }*/
    }

    public Marker lastMarker;

    private class MarkerClickListener implements GoogleMap.OnMarkerClickListener {
        @Override
        public boolean onMarkerClick(Marker marker) {
            return passengerStateContext.markerClicked(marker);
        }
    }

            @Override
            public void onConnected(Bundle connectionHint) {
                Location location = LocationServices.FusedLocationApi.getLastLocation(
                        mGoogleApiClient);
                if (location != null && AppSettings.firstZooming) {
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                            new LatLng(location.getLatitude(), location.getLongitude()), 13));
                    AppSettings.firstZooming = false;
                } else {
                    LocationRequest mLocationRequest = new LocationRequest();
                    mLocationRequest.setInterval(10000);
                    mLocationRequest.setFastestInterval(5000);
                    mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

                    LocationServices.FusedLocationApi.requestLocationUpdates(
                            mGoogleApiClient, mLocationRequest, this);
                }

            }

            @Override
            public void onConnectionSuspended(int cause) {
                // The connection has been interrupted.
                // Disable any UI components that depend on Google APIs
                // until onConnected() is called.
            }
            protected synchronized void buildGoogleApiClient() {
                FragmentManager fm = getSupportFragmentManager();
                SupportMapFragment mapFragment =
                        (SupportMapFragment) fm.findFragmentById(R.id.location_map);
                mapFragment.getMapAsync(this);
                mGoogleApiClient = new GoogleApiClient.Builder(this)
                        .addConnectionCallbacks(this)
                        .addApi(LocationServices.API)
                        .build();
            }

    @Override
    protected void onStart() {
        //setActionButtonStop();
        super.onStart();
        mGoogleApiClient.connect();
        //StartAndBindService();
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    protected void onResume() {

        super.onResume();
        EventBus.getDefault().post(new ServiceEvents.GetPassengerStateEvent());
        //LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
        //        new IntentFilter(Constants.UPDATE_REQ));
        mGoogleApiClient.connect();
        //StartAndBindService();


    }

    @Override
    protected void onPause() {
        //StopAndUnbindServiceIfRequired();
        //LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
        super.onPause();
        //mGoogleApiClient.connect();
    }

    @Override
    public void onDestroy() {
        activitiesLaunched.getAndDecrement();
        //StopAndUnbindServiceIfRequired();
        //UnregisterEventBus();
        super.onDestroy();

    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }


    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            ToggleDrawer();
        }

        return super.onKeyUp(keyCode, event);
    }

    /**
     * Handles the hardware back-button press
     */
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && Session.isBoundToService()) {
            StopAndUnbindServiceIfRequired();
        }

        if(keyCode == KeyEvent.KEYCODE_BACK){
            DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
            if(drawerLayout.isDrawerOpen(Gravity.LEFT)){
                ToggleDrawer();
                return true;
            }
        }

        return super.onKeyDown(keyCode, event);
    }

    /*private void WriteToFile(MyInfo info) {
        Session.setAddNewTrackSegment(false);
        try {
            tracer.debug("Calling file writers");
            FileLoggerFactory.Write(getApplicationContext(), info);
        }
        catch(Exception e){
            tracer.error(getString(R.string.could_not_write_to_file), e);
        }
    }*/

    private void loadPresetProperties() {

        //Either look for /<appfolder>/ajerlitaxipassenger.properties or /sdcard/ajerlitaxipassenger.properties
        File file =  new File(Utilities.GetDefaultStorageFolder(getApplicationContext()) + "/ajerlitaxipassenger.properties");
        if(!file.exists()){
            file = new File(Environment.getExternalStorageDirectory() + "/ajerlitaxipassenger.properties");
            if(!file.exists()){
                return;
            }
        }

        try {
            Properties props = new Properties();
            InputStreamReader reader = new InputStreamReader(new FileInputStream(file));
            props.load(reader);

            AppSettings.SetPreferenceFromProperties(props);

        } catch (Exception e) {
            tracer.error("Could not load preset properties", e);
        }
    }


    /**
     * Helper method, launches activity in a delayed handler, less stutter
     */
    private void LaunchPreferenceScreen(final String whichFragment) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent targetActivity = new Intent(getApplicationContext(), MainPreferenceActivity.class);
                targetActivity.putExtra("preference_fragment", whichFragment);
                startActivity(targetActivity);
            }
        }, 250);
    }



    public Toolbar GetToolbar(){
        return (Toolbar)findViewById(R.id.toolbar);
    }

    public void SetUpToolbar(){
        try{
            Toolbar toolbar = GetToolbar();
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayShowTitleEnabled(false);

            //Deprecated in Lollipop but required if targeting 4.x
            //SpinnerAdapter spinnerAdapter = ArrayAdapter.createFromResource(getApplicationContext(), R.array.gps_main_views, R.layout.spinner_dropdown_item);
            //getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
            //getSupportActionBar().setListNavigationCallbacks(spinnerAdapter, this);
            //getSupportActionBar().setSelectedNavigationItem(GetUserSelectedNavigationItem());

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Window window = getWindow();
                window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            }
        }
        catch(Exception ex){
            //http://stackoverflow.com/questions/26657348/appcompat-v7-v21-0-0-causing-crash-on-samsung-devices-with-android-v4-2-2
            tracer.error("Thanks for this, Samsung", ex);
        }

    }

    public void SetUpNavigationDrawer() {

        final DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        final DrawerView drawer = (DrawerView) findViewById(R.id.drawer);

        drawerToggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                GetToolbar(),
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        ){

            public void onDrawerClosed(View view) {
                invalidateOptionsMenu();
            }

            public void onDrawerOpened(View drawerView) {
                invalidateOptionsMenu();
            }
        };

        drawerLayout.setStatusBarBackgroundColor(getResources().getColor(R.color.primaryColorDark));
        drawerLayout.setDrawerListener(drawerToggle);
        drawerLayout.closeDrawer(drawer);

        //drawer.addDivider();
        drawer.addItem(new DrawerItem()
                        .setId(1000)
                        .setImage(ContextCompat.getDrawable(this, R.drawable.settings))
                        .setTextPrimary(getString(R.string.pref_general_title))
                        .setTextSecondary(getString(R.string.pref_general_summary))
        );

        drawer.addItem(new DrawerItem()
                .setId(3)
                .setImage(ContextCompat.getDrawable(this, R.drawable.history))
                .setTextPrimary(getString(R.string.menu_rideHistory)));


        drawer.addItem(new DrawerItem()
                        .setId(2)
                        .setImage(ContextCompat.getDrawable(this, R.drawable.performance))
                        .setTextPrimary(getString(R.string.pref_performance_title))
                        .setTextSecondary(getString(R.string.pref_performance_summary))
        );



        drawer.addItem(new DrawerItem()
                        .setId(11)
                        .setImage(ContextCompat.getDrawable(this, R.drawable.helpfaq))
                        .setTextPrimary(getString(R.string.menu_faq))
        );

        drawer.addDivider();

        drawer.addItem(new DrawerItem()
                        .setId(13)
                        .setImage(ContextCompat.getDrawable(this, R.drawable.about))
                        .setTextPrimary(getString(R.string.menu_about)));

        drawer.addDivider();

        drawer.addItem(new DrawerItem()
                .setId(14)
                .setImage(ContextCompat.getDrawable(this, R.drawable.logout))
                .setTextPrimary(getString(R.string.menu_logout)));

        drawer.addItem(new DrawerItem()
                .setId(12)
                .setImage(ContextCompat.getDrawable(this, R.drawable.exit))
                .setTextPrimary(getString(R.string.menu_exit)));

        drawer.setOnItemClickListener(new DrawerItem.OnItemClickListener() {
            @Override
            public void onClick(DrawerItem drawerItem, long id, int position) {
                //drawer.selectItem(3);
                drawerLayout.closeDrawer(drawer);

                switch((int)id){
                    case 1000:
                        LaunchPreferenceScreen(MainPreferenceActivity.PreferenceConstants.GENERAL);
                        break;

                    case 2:
                        LaunchPreferenceScreen(MainPreferenceActivity.PreferenceConstants.PERFORMANCE);
                        break;
                    case 3:
                        Intent aActivity = new Intent(getApplicationContext(), RideHistoryActivity.class);
                        startActivity(aActivity);
                        break;

                    case 11:
                        Intent faqtivity = new Intent(getApplicationContext(), Faqtivity.class);
                        startActivity(faqtivity);
                        break;
                    case 13://about
                        new MaterialDialog.Builder(GpsMainActivity.this)
                                .title(R.string.menu_about)
                                .content(getString(R.string.appAbout)+BuildConfig.VERSION_NAME)
                                .positiveText(R.string.ok)
                                .show();
                        break;
                    case 14://logout
                        logout();
                        break;
                    case 12://exit
                        EventBus.getDefault().post(new CommandEvents.RequestStartStop(false));
                        finish();
                        break;
                }
            }
        });

        ImageButton helpButton = (ImageButton) findViewById(R.id.imgHelp);
        helpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent faqtivity = new Intent(getApplicationContext(), Faqtivity.class);
                startActivity(faqtivity);
            }
        });

    }

    private void logout() {
        EventBus.getDefault().post(new CommandEvents.RequestStartStop(false));
        AppSettings.logout();
        Intent loginActivity = new Intent(getApplicationContext(), LoginActivity.class);
        startActivity(loginActivity);
        finish();
    }

    public void ToggleDrawer(){
        DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        if(drawerLayout.isDrawerOpen(Gravity.LEFT)){
            drawerLayout.closeDrawer(Gravity.LEFT);
        }
        else {
            drawerLayout.openDrawer(Gravity.LEFT);
        }
    }

    /*private int GetUserSelectedNavigationItem(){
        return AppSettings.getUserSelectedNavigationItem();
    }


    @Override
    public boolean onNavigationItemSelected(int position, long itemId) {
        AppSettings.setUserSelectedNavigationItem(position);
        //LoadFragmentView(position);
        Session.availabilityState = position;
        EventBus.getDefault().postSticky(new CommandEvents.RequestStartStop(true));

        MyInfo info = new MyInfo(null);
        info.updateStateOnly = true;
        WriteToFile(info);
        info.updateStateOnly = false;

        return true;
    }
*/
    /**
     * Provides a connection to the GPS Logging Service
     */
    private final ServiceConnection gpsServiceConnection = new ServiceConnection() {

        public void onServiceDisconnected(ComponentName name) {
            tracer.debug("Disconnected from GPSLoggingService from MainActivity");
            //loggingService = null;
        }

        public void onServiceConnected(ComponentName name, IBinder service) {
            tracer.debug("Connected to GPSLoggingService from MainActivity");
            //loggingService = ((GpsLoggingService.GpsLoggingBinder) service).getService();
        }
    };


    /**
     * Starts the service and binds the activity to it.

    private void StartAndBindService() {
        serviceIntent = new Intent(this, GpsLoggingService.class);
        // Start the service in case it isn't already running
        //serviceIntent.putExtra("availabilityState",GetUserSelectedNavigationItem());
        startService(serviceIntent);
        // Now bind to service
        bindService(serviceIntent, gpsServiceConnection, Context.BIND_AUTO_CREATE);
        Session.setBoundToService(true);
    }*/


    /**
     * Stops the service if it isn't logging. Also unbinds.
     */
    private void StopAndUnbindServiceIfRequired() {
        if (Session.isBoundToService()) {

            try {
                unbindService(gpsServiceConnection);
                Session.setBoundToService(false);
            } catch (Exception e) {
                tracer.warn(SessionLogcatAppender.MARKER_INTERNAL, "Could not unbind service", e);
            }
        }

        if (!Session.isStarted()) {
            tracer.debug("Stopping the service");
            try {
                stopService(serviceIntent);
            } catch (Exception e) {
                tracer.error("Could not stop the service", e);
            }
        }
    }
            /*private NotificationCompat.Builder nfc = null;
            private static NotificationManager notificationManager;
            private static int NOTIFICATION_ID = 8675309;

            private void ShowNotification(int idSmall, int idLarge) {
                Intent stopLoggingIntent = new Intent(this, GpsLoggingService.class);
                stopLoggingIntent.setAction("NotificationButton_STOP");
                stopLoggingIntent.putExtra(IntentConstants.IMMEDIATE_STOP, true);
                PendingIntent piStop = PendingIntent.getService(this, 0, stopLoggingIntent, 0);

                Intent annotateIntent = new Intent(this, NotificationAnnotationActivity.class);
                annotateIntent.setAction("com.mendhak.gpslogger.NOTIFICATION_BUTTON");
                PendingIntent piAnnotate = PendingIntent.getActivity(this,0, annotateIntent,0);

                // What happens when the notification item is clicked
                Intent contentIntent = new Intent(this, GpsMainActivity.class);

                TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
                stackBuilder.addNextIntent(contentIntent);

                PendingIntent pending = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);


                NumberFormat nf = new DecimalFormat("###.#####");

                String contentText = getString(R.string.gpslogger_still_running);
                long notificationTime = System.currentTimeMillis();

                if (Session.hasValidLocation()) {
                    contentText = getString(R.string.txt_latitude_short) + ": " + nf.format(Session.getCurrentLatitude()) + ", "
                            + getString(R.string.txt_longitude_short) + ": " + nf.format(Session.getCurrentLongitude());

                    notificationTime = Session.getCurrentLocationInfo().getTime();
                }

                //if (nfc == null)
                {
                    //int idSmall = R.drawable.availablesmall;
                    //int idLarge = R.drawable.availablelarge;

                    nfc = new NotificationCompat.Builder(getApplicationContext())
                            .setSmallIcon(idSmall)
                            .setLargeIcon(BitmapFactory.decodeResource(getResources(), idLarge))
                            .setPriority(Notification.PRIORITY_MAX)
                            .setContentTitle(contentText)
                            .setOngoing(true)
                            .setContentIntent(pending);

                    if(!AppSettings.shouldHideNotificationButtons()){
                        nfc
                                .addAction(R.drawable.annotate2, getString(R.string.menu_annotate), piAnnotate)
                                .addAction(android.R.drawable.ic_menu_close_clear_cancel, getString(R.string.shortcut_stop), piStop);
                    }
                }



                nfc.setContentTitle(contentText);
                nfc.setContentText(getString(R.string.app_name));
                nfc.setWhen(notificationTime);

                notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                notificationManager.notify(NOTIFICATION_ID, nfc.build());
            }*/
    private void SetBulbStatus(boolean started) {
        ImageView bulb = (ImageView) findViewById(R.id.notification_bulb);
        bulb.setImageResource(started ? R.drawable.circle_green : R.drawable.circle_none);
    }

    /*public void SetAnnotationReady() {
        Session.setAnnotationMarked(true);
        enableDisableMenuItems();
    }

    public void SetAnnotationDone() {
        Session.setAnnotationMarked(false);
        enableDisableMenuItems();
    }*/

    public void OnWaitingForLocation(boolean inProgress) {
        ProgressBar fixBar = (ProgressBar) findViewById(R.id.progressBarGpsFix);
        fixBar.setVisibility(inProgress ? View.VISIBLE : View.INVISIBLE);

        //MJH: part2
        tracer.debug(inProgress + "");
        /*if(!Session.isStarted()){
            actionButton.setProgress(0);
            setActionButtonStart();
            return;
        }

        if(inProgress){
            actionButton.setProgress(1);
            setActionButtonStop();
        }
        else {
            actionButton.setProgress(0);
            setActionButtonStop();
        }*/
    }

    /*private void setStateToIdle() {
        if(countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer=null;
        }

        AppSettings.requestId = -1;
        pickdropState=0;

        btnPickDrop.setVisibility(View.VISIBLE);
        btnPickDrop.setText(getString(R.string.gpsMainBtnPickFrom));
        driverInfoLayout.setVisibility(View.INVISIBLE);
        if(fromMarker != null) {
            fromMarker.remove();
            fromMarker = null;
        }
        if(toMarker != null) {
            toMarker.remove();
            toMarker = null;
        }
        if(driverMarker != null) {
            driverMarker.remove();
            driverMarker = null;
        }
    }

    void setStateTo(TRequestObj tRequestObj) {
        helpOverlayLayout.setVisibility(View.GONE);
        btnPickDrop.setVisibility(View.GONE);
        driverInfoLayout.setVisibility(View.VISIBLE);
        txtDriverName.setText(tRequestObj.driverName);
        txtDriverInfo.setText(tRequestObj.driverInfo);
        btnDriverPhone.setText(tRequestObj.driverPhone);
        {
            //final String IMAGE_URL = "http://developer.android.com/images/training/system-ui.png";
            ImageLoader mImageLoader = AppSettings.getInstance().getImageLoader();
            networkImageViewDriver.setImageUrl(tRequestObj.driverPhotoUrl, mImageLoader);
        }
        //driver not assigned yet & 15min elapsed => neglect it
        if(Integer.parseInt(tRequestObj.driverId) == -1) {
            int remainingSeconds = 900 - Integer.parseInt(tRequestObj.secondsToNow);
            if(remainingSeconds < 5) {
                setStateToIdle();
                return;
            } else {
                //set timer to remainingSeconds
                startCounter(remainingSeconds);
                AppSettings.requestId = tRequestObj.idx;
                pickdropState=3;
                btnPickDrop.setVisibility(View.VISIBLE);
                driverInfoLayout.setVisibility(View.INVISIBLE);
            }
        }

        //2. driver assigned or passanger picked
        LatLng currentPosition = new LatLng(tRequestObj.fromLat, tRequestObj.fromLong);
        if(fromMarker == null) {
            MarkerOptions markerOptions = new MarkerOptions()
                    .position(currentPosition)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                    //.draggable(true)
                    ;
            ;
            fromMarker = googleMap.addMarker(markerOptions);
        } else {
            fromMarker.setPosition(currentPosition);
        }

        currentPosition = new LatLng(tRequestObj.toLat, tRequestObj.toLong);
        if (toMarker == null) {
            MarkerOptions markerOptions = new MarkerOptions()
                    .position(currentPosition)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                    //.draggable(false)
                    ;
            toMarker = googleMap.addMarker(markerOptions);
        } else {
            toMarker.setPosition(currentPosition);
        }
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        {
            builder.include(fromMarker.getPosition());
            builder.include(toMarker.getPosition());
        }
        LatLngBounds bounds = builder.build();
        int padding = 120; // offset from edges of the map in pixels
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
        googleMap.animateCamera(cu);
    }*/

    public void clearDriversMarkers() {
        mMarkerInfoHash.clear();
        if(nearbyDrivers != null) {
            for (int i = 0; i < countOfDrivers; i++) {
                nearbyDrivers[i].remove();
            }
            nearbyDrivers = null;
        }

        if(searchPolygon != null) {
            searchPolygon.remove();
            searchPolygon = null;
        }

        /*if(mapsSearchCircle != null) {
            mapsSearchCircle.remove();
            mapsSearchCircle = null;
        }
        */

    }

    @EventBusHook
    public void onEventMainThread(ServiceEvents.forceLogout tmp){
        logout();
    }

    @EventBusHook
    public void onEventMainThread(ServiceEvents.GetPassengerStateEvent tmp){
        UploadClass uc = new UploadClass(GpsMainActivity.this);
        uc.getPassangerState(AppSettings.getUid());
    }

    @EventBusHook
    public void onEventMainThread(ServiceEvents.updateDrivers updateDriversEvent){
        tracer.debug("updating nearby driver");
        DriverInfo[] driverInfo = updateDriversEvent.drvInfo;
        double lat_dw = updateDriversEvent.lat_d;
        double lng_dw = updateDriversEvent.lng_d;

        Location loc = updateDriversEvent.location;
        LatLng center = new LatLng(loc.getLatitude(), loc.getLongitude());

        //double radiusInMeters = Utilities.toRadiusMeters(new LatLng(0.0, 0.0), new LatLng(radius, radius));
        //1. remove previous markers
        clearDriversMarkers();
        //2.a add scan circle
        int circleStrokeWidth = 3;
        int mStrokeColor = Color.BLACK;
        int mFillColor1 = 20;
        int mAlpha = 20;
        int mFillColor = Color.HSVToColor(
                mAlpha, new float[] {mFillColor1, 1, 1});
        /*CircleOptions opt = new CircleOptions()
                .center(center)
                .radius(radiusInMeters)
                .strokeWidth(circleStrokeWidth)
                .strokeColor(mStrokeColor)
                .fillColor(mFillColor);
        mapsSearchCircle = googleMap.addCircle(opt);*/
        //Polygon [or rectangle]
        PolygonOptions options = new PolygonOptions().addAll(Utilities.createRectangle(center, lat_dw, lng_dw));
        searchPolygon = googleMap.addPolygon(options
                .strokeWidth(circleStrokeWidth)
                .strokeColor(Color.BLACK)
                .fillColor(mFillColor));
        //2.b add new markers
        countOfDrivers = updateDriversEvent.drvCount;
        nearbyDrivers = new Marker[countOfDrivers];
        for(int i=0; i<updateDriversEvent.drvCount;i++) {
            LatLng driverPosition = new LatLng(driverInfo[i].latitude, driverInfo[i].longitude);
            MarkerOptions markerOptions = new MarkerOptions()
                    .position(driverPosition)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.taximarker))
                    .title(driverInfo[i].name)
                    //.snippet(driverInfo[i].phone)
                    //.anchor(0.5f, 0.5f)
                    //.draggable(true);
                    ;

            nearbyDrivers[i] = googleMap.addMarker(markerOptions);
            mMarkerInfoHash.put(nearbyDrivers[i], driverInfo[i]);
        }
    }

    @EventBusHook
    public void onEventMainThread(ServiceEvents.DriverLocationUpdate driverLocationUpdate){
        tracer.debug("driver location update");

        LatLng driverPosition = new LatLng(driverLocationUpdate.driverInfo.latitude, driverLocationUpdate.driverInfo.longitude);
        if(driverMarker == null) {
            MarkerOptions markerOptions = new MarkerOptions()
                    .position(driverPosition)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.taximarker))
                    //.anchor(0.5f, 0.5f)
                    //.draggable(true);
            ;
            driverMarker = googleMap.addMarker(markerOptions);
        } else {
            driverMarker.setPosition(driverPosition);
        }

    }

    @EventBusHook
    public void onEventMainThread(ServiceEvents.UpdateAnnouncement updateAnnEvent){
        String ver = updateAnnEvent.ver;
        String imageName = updateAnnEvent.annImage;
        String tmpText = updateAnnEvent.annText;
        String countDrv = updateAnnEvent.countOfDrivers;
        String countPas = updateAnnEvent.countOfPassengers;

        if(imageName.isEmpty() && tmpText.isEmpty() && countDrv.isEmpty() && countPas.isEmpty()) {
            relativeLayoutAds.setVisibility(View.GONE);
        } else {
            if(imageName.isEmpty()) {
                networkivAds.setVisibility(View.GONE);
            } else {
                //networkivAds = (NetworkImageView) findViewById(R.id.networkivAds);
                networkivAds.setVisibility(View.VISIBLE);
                {
                    //final String IMAGE_URL = "http://developer.android.com/images/training/system-ui.png";
                    ImageLoader mImageLoader = AppSettings.getInstance().getImageLoader();
                    String tmp = Constants.URL_ads + imageName;
                    networkivAds.setImageUrl(tmp, mImageLoader);
                }
            }
            if(tmpText.isEmpty() && countDrv.isEmpty() && countPas.isEmpty()) {
                textviewAds.setVisibility(View.GONE);
            } else {
                String s =  tmpText.replaceAll("\\\\n", "\\\n");
                String tmp="";
                if(!(countDrv.isEmpty() && countPas.isEmpty()))  {
                    //tmp = String.format("%s:%s - %s:%s",getResources().getString(R.string.Drivers), countDrv, getResources().getString(R.string.Passengers), countPas);
                    //s += "\n" + tmp;
                    //textviewAds.setText(s);
                    s="<html>" + s + "</html>";
                    textviewAds.setText(Html.fromHtml(s));
                    textviewAds.setMovementMethod(LinkMovementMethod.getInstance());
                }
            }
        }

        if(ver.equalsIgnoreCase(Constants.ver) == false) {
            // request for update => exit app
            new MaterialDialog.Builder(this)
                    .cancelable(false)
                    .autoDismiss(false)
                    .title(R.string.loginUpdateAppTitle)
                    .content(getString(R.string.loginUpdateApp))
                    .positiveText(R.string.ok)
                    .callback(new MaterialDialog.ButtonCallback() {
                        @Override
                        public void onPositive(MaterialDialog dialog) {
                            String url = "market://details?id=" + getPackageName();//getString(R.string.appGoogleLink);
                            if(AppSettings.getChosenLanguage() == "ar") {
                                url += "&hl=ar";
                            }

                            dialog.dismiss();

                            Intent i = new Intent(Intent.ACTION_VIEW);
                            i.setData(Uri.parse(url));
                            startActivity(i);

                            GpsMainActivity.this.finish();
                            return;
                        }
                    })
                    .show();
            //finish();
        }
    }

    @EventBusHook
    public void onEventMainThread(ServiceEvents.ErrorConnectionEvent erroConnectionEvent){
        tracer.debug("error getting state");
        /*btnPickDrop.setText(getResources().getString(R.string.gpsMainBtnReconnect));
        btnPickDrop.setVisibility(View.VISIBLE);
        driverInfoLayout.setVisibility(View.INVISIBLE);
        pickdropState = 20;*/
        passengerStateContext.setState(passengerState_reconnect.getInstance(this, passengerStateContext));
    }

    @EventBusHook
    public void onEventMainThread(ServiceEvents.UpdateStateEvent updateStateEvent){
        TRequestObj tRequestObj = updateStateEvent.treqObj;
        if(tRequestObj == null) {
            //idle:
            passengerStateContext.setState(passengerState_idle.getInstance(this, passengerStateContext));
            //setStateToIdle();
        } else {
            passengerStateContext.setState(passengerState_assigned.getInstance(this, passengerStateContext, tRequestObj));
            //setStateTo(tRequestObj);
        }
    }

    @EventBusHook
    public void onEventMainThread(ServiceEvents.TRequestUpdated tRequestUpdatedEvent){
        //picked and done states
        String state = tRequestUpdatedEvent.treqState;
        AppSettings.requestId = -1;//even if the state is dropped
        cancelTRequest(null);//requestId =-1, so that not to upload state
        if(state.equalsIgnoreCase("assigned")) {
            btnPickDrop.setVisibility(View.GONE);
            driverInfoLayout.setVisibility(View.VISIBLE);
        } else if(state.equalsIgnoreCase("picked")) {
            btnPickDrop.setVisibility(View.GONE);
            driverInfoLayout.setVisibility(View.VISIBLE);
        }else {//done
            btnPickDrop.setVisibility(View.VISIBLE);
            driverInfoLayout.setVisibility(View.INVISIBLE);
        }
    }

    @EventBusHook
    public void onEventMainThread(ServiceEvents.TRequestAccepted trequestAcceptedEvent){
        //request accepted: driver assigned
        int driverId = trequestAcceptedEvent.drvId;
        /*AppSettings.requestId = -1;
        cancelTRequest(null);*/

        UploadClass uc = new UploadClass(this);
        uc.getPassangerState(AppSettings.getUid());
    }

    @EventBusHook
    public void onEventMainThread(ServiceEvents.CancelTRequests cancelTRequests){
        tracer.debug("cancel TRequest");
        Toast.makeText(this, getResources().getString(R.string.gpsMainMsgRequestCanceled), Toast.LENGTH_LONG).show();
        //Utilities.MsgBox(getResources().getString(R.string.gpsMainMsgRequestCanceled), cancelTRequests.msg, this);
        //cancelTRequest(Constants.TRequest_Canceled);
        //pickdropState=0;
        btnPickDrop.setText(getString(R.string.gpsMainBtnPickFrom));
        //countDownTimer.cancel();

        UploadClass uc = new UploadClass(this);
        uc.getPassangerState(AppSettings.getUid());
        /*if(pickdropState != 0) {
            pickdropState=0;
            btnPickDrop.setText("Pick From...");
            countDownTimer.cancel();
        }*/

    }

    /*@EventBusHook
    public void onEventMainThread(UploadEvents.OpenGTS upload){
        tracer.debug("Open GTS Event completed, success: " + upload.success);
        Utilities.HideProgress();

        if(!upload.success){
            tracer.error(getString(R.string.opengts_setup_title)
                    + "-"
                    + getString(R.string.upload_failure));

            if(userInvokedUpload){
                Utilities.MsgBox(getString(R.string.sorry),getString(R.string.upload_failure), this);
                userInvokedUpload = false;
            }
        }
    }

    @EventBusHook
    public void onEventMainThread(UploadEvents.AutoEmail upload){
        tracer.debug("Auto Email Event completed, success: " + upload.success);
        Utilities.HideProgress();

        if(!upload.success){
            tracer.error(getString(R.string.autoemail_title)
                    + "-"
                    + getString(R.string.upload_failure));
            if(userInvokedUpload){
                Utilities.MsgBox(getString(R.string.sorry),getString(R.string.upload_failure), this);
                userInvokedUpload = false;
            }
        }
    }

    @EventBusHook
    public void onEventMainThread(UploadEvents.OpenStreetMap upload){
        tracer.debug("OSM Event completed, success: " + upload.success);
        Utilities.HideProgress();

        if(!upload.success){
            tracer.error(getString(R.string.osm_setup_title)
                    + "-"
                    + getString(R.string.upload_failure));
            if(userInvokedUpload){
                Utilities.MsgBox(getString(R.string.sorry),getString(R.string.upload_failure), this);
                userInvokedUpload = false;
            }
        }
    }

    @EventBusHook
    public void onEventMainThread(UploadEvents.Dropbox upload){
        tracer.debug("Dropbox Event completed, success: " + upload.success);
        Utilities.HideProgress();

        if(!upload.success){
            tracer.error(getString(R.string.dropbox_setup_title)
                    + "-"
                    + getString(R.string.upload_failure));
            if(userInvokedUpload){
                Utilities.MsgBox(getString(R.string.sorry),getString(R.string.upload_failure), this);
                userInvokedUpload = false;
            }
        }
    }

    @EventBusHook
    public void onEventMainThread(UploadEvents.GDocs upload){
        tracer.debug("GDocs Event completed, success: " + upload.success);
        Utilities.HideProgress();

        if(!upload.success){
            tracer.error(getString(R.string.gdocs_setup_title)
                    + "-"
                    + getString(R.string.upload_failure));
            if(userInvokedUpload){
                Utilities.MsgBox(getString(R.string.sorry),getString(R.string.upload_failure), this);
                userInvokedUpload = false;
            }
        }
    }

    @EventBusHook
    public void onEventMainThread(UploadEvents.Ftp upload){
        tracer.debug("FTP Event completed, success: " + upload.success);
        Utilities.HideProgress();

        if(!upload.success){
            tracer.error(getString(R.string.autoftp_setup_title)
                    + "-"
                    + getString(R.string.upload_failure));
            if(userInvokedUpload){
                Utilities.MsgBox(getString(R.string.sorry),getString(R.string.upload_failure), this);
                userInvokedUpload = false;
            }
        }
    }


    @EventBusHook
    public void onEventMainThread(UploadEvents.OwnCloud upload){
        tracer.debug("OwnCloud Event completed, success: " + upload.success);
        Utilities.HideProgress();

        if(!upload.success){
            tracer.error(getString(R.string.owncloud_setup_title)
                    + "-"
                    + getString(R.string.upload_failure));

            if(userInvokedUpload){
                Utilities.MsgBox(getString(R.string.sorry),getString(R.string.upload_failure), this);
                userInvokedUpload = false;
            }
        }
    }*/

    private void checkGpsEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){

        } else {
            EventBus.getDefault().post(new ServiceEvents.LocationServicesUnavailable());
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        if(AppSettings.firstZooming) {
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(location.getLatitude(), location.getLongitude()), 13));
            AppSettings.firstZooming = false;
            /*LocationServices.FusedLocationApi.removeLocationUpdates(
                    mGoogleApiClient, this);*/
        }
    }

    @EventBusHook
    public void onEventMainThread(ServiceEvents.LocationUpdate locationUpdate){
        //DisplayLocationInfo(locationUpdate.location);
        if(AppSettings.firstZooming) {
            Location location = locationUpdate.location;
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(location.getLatitude(), location.getLongitude()), 13));
            AppSettings.firstZooming = false;
        }
    }



    @EventBusHook
    public void onEventMainThread(ServiceEvents.FileNamed fileNamed){
        //showCurrentFileName(fileNamed.newFileName);
    }

    @EventBusHook
    public void onEventMainThread(ServiceEvents.WaitingForLocation waitingForLocation){
        OnWaitingForLocation(waitingForLocation.waiting);
    }

    /*@EventBusHook
    public void onEventMainThread(ServiceEvents.AnnotationStatus annotationStatus){
        if(annotationStatus.annotationWritten){
            //SetAnnotationDone();
        }
        else {
            //SetAnnotationReady();
        }
    }*/

    @EventBusHook
    public void onEventMainThread(ServiceEvents.LoggingStatus loggingStatus){
        /*if(loggingStatus.loggingStarted){
            showPreferencesSummary();
            setActionButtonStop();
        }
        else {
            setActionButtonStart();
        }*/
        //enableDisableMenuItems();
    }

    /*private void setActionButtonStart(){
        actionButton.setText(R.string.btn_start_logging);
        actionButton.setBackgroundColor(getResources().getColor(R.color.accentColor));
        actionButton.setAlpha(0.8f);
    }

    private void setActionButtonStop(){
        actionButton.setText(R.string.btn_stop_logging);
        actionButton.setBackgroundColor(getResources().getColor(R.color.accentColorComplementary));
        actionButton.setAlpha(0.8f);
    }*/

    /*private void showPreferencesSummary() {
        //showCurrentFileName(Session.getCurrentFileName());

        ImageView imgGpx = (ImageView) findViewById(R.id.simpleview_imgGpx);
        ImageView imgKml = (ImageView) findViewById(R.id.simpleview_imgKml);
        ImageView imgCsv = (ImageView) findViewById(R.id.simpleview_imgCsv);
        ImageView imgNmea = (ImageView) findViewById(R.id.simpleview_imgNmea);
        ImageView imgLink = (ImageView) findViewById(R.id.simpleview_imgLink);

        if (AppSettings.shouldLogToGpx()) {

            imgGpx.setVisibility(View.VISIBLE);
        } else {
            imgGpx.setVisibility(View.GONE);
        }

        if (AppSettings.shouldLogToKml()) {

            imgKml.setVisibility(View.VISIBLE);
        } else {
            imgKml.setVisibility(View.GONE);
        }

        if (AppSettings.shouldLogToNmea()) {
            imgNmea.setVisibility(View.VISIBLE);
        } else {
            imgNmea.setVisibility(View.GONE);
        }

        if (AppSettings.shouldLogToPlainText()) {

            imgCsv.setVisibility(View.VISIBLE);
        } else {
            imgCsv.setVisibility(View.GONE);
        }

        if (AppSettings.shouldLogToCustomUrl()) {
            imgLink.setVisibility(View.VISIBLE);
        } else {
            imgLink.setVisibility(View.GONE);
        }

        if (!AppSettings.shouldLogToGpx() && !AppSettings.shouldLogToKml()
                && !AppSettings.shouldLogToPlainText()) {
            showCurrentFileName(null);
        }

    }

    private void showCurrentFileName(String newFileName) {
        TextView txtFilename = (TextView) findViewById(R.id.simpleview_txtfilepath);
        if (newFileName == null || newFileName.length() <= 0) {
            txtFilename.setText("");
            txtFilename.setVisibility(View.INVISIBLE);
            return;
        }

        txtFilename.setVisibility(View.VISIBLE);
        txtFilename.setText(Html.fromHtml("<em>" + AppSettings.getGpsLoggerFolder() + "/<strong><br />" + Session.getCurrentFileName() + "</strong></em>"));

        Utilities.SetFileExplorerLink(txtFilename,
                Html.fromHtml("<em><font color='blue'><u>" + AppSettings.getGpsLoggerFolder() + "</u></font>" + "/<strong><br />" + Session.getCurrentFileName() + "</strong></em>" ),
                AppSettings.getGpsLoggerFolder(),
                this.getApplicationContext());

    }
*/
    private enum IconColorIndicator {
        Good,
        Warning,
        Bad,
        Inactive
    }

    private Toast getToast(String message) {
        return Toast.makeText(this, message, Toast.LENGTH_SHORT);
    }

    /*public void DisplayLocationInfo(Location locationInfo){
        showPreferencesSummary();
    }*/

    public void startCounter(int counterTimer) {
        if(countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer=null;
        }
        if(counterTimer>100000) {
            counterTimer = 100000;
        }
        countDownTimer = new CountDownTimer(counterTimer*1000, 1000) {
            public void onTick(long millisUntilFinished) {
                long minutes = (millisUntilFinished) / (60*1000);
                long seconds = (millisUntilFinished/1000) % 60;
                //String timeString = String.format("%02d:%02d", minutes, seconds);
                btnPickDrop.setText(getString(R.string.gpsMainBtnCancel,minutes, seconds));
            }
            public void onFinish() {
                //cancel the T-request
                cancelTRequest(Constants.TRequest_Expired);
            }
        }.start();
    }

    @Override
    public void onBackPressed() {
        if(!passengerStateContext.backPressed()){
            super.onBackPressed();
        }

        /*switch(pickdropState) {
            case 1:
                setStateToIdle();
                break;
            case 2:
                pickdropState=1;
                btnPickDrop.setText(getString(R.string.gpsMainBtnDropto));
                if(toMarker != null) {
                    toMarker.remove();
                    toMarker = null;
                }
                break;
            default:
                super.onBackPressed();
                break;
        }*/
    }

    public void onCloseHelpClicked(View v) {
        helpOverlayLayout.setVisibility(View.GONE);
    }
    public void onPickDropClick(View v) {
        passengerStateContext.btnClicked();
    }


    /*void setAdditionalFee(String fee, String noOfPass, String notes) {
        suggestedFee = fee;
        noOfPassangers = noOfPass;
        additionalNotes = notes;
    }*/

    public void cancelTRequest(String tReqState) {
        //pickdropState=0;
        //btnPickDrop.setText(getString(R.string.gpsMainBtnPickFrom));
        if(AppSettings.requestId != -1) {
            UploadClass upload = new UploadClass(GpsMainActivity.this);
            upload.setTRequestState(Integer.toString(AppSettings.requestId), tReqState);
            AppSettings.requestId = -1;
        }
        passengerStateContext.setState(passengerState_idle.getInstance(this, passengerStateContext));
        //setStateToIdle();
    }

    private ProgressDialog pDialog;
    private void showDialog() {
        if(Utilities.checkContextIsFinishing(this)) {
            return;
        }
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }



    /*private void tabSelected(@IdRes int tabId) {
        switch(tabId){
            case R.id.tab_profile:
                //startActivity(new Intent(this, ProfileActivity.class));
                break;
            case R.id.tab_history:
                Intent aActivity = new Intent(this, RideHistoryActivity.class);
                startActivity(aActivity);
                break;
            case R.id.tab_ridenow:

                break;
        }
    }*/

    /*public interface passengerState {
        void btnClicked();
        void mapClicked();
        boolean backPressed();
    }

    public class passengerState_context implements passengerState {
        private passengerState state;
        public void setState(passengerState aState) {
            state  = aState;
        }
        public passengerState_context() {
            setState(new passengerState_idle(this));
        }

        public void btnClicked(){
            state.btnClicked();
        }
        public void mapClicked(){
            state.mapClicked();
        }
        public boolean backPressed(){
            return state.backPressed();
        }

    }

    public class passengerState_idle implements passengerState {
        private passengerState_context stateContext;

        public passengerState_idle(passengerState_context aStateContext){
            stateContext = aStateContext;

            if(countDownTimer != null) {
                countDownTimer.cancel();
                countDownTimer=null;
            }

            AppSettings.requestId = -1;
            pickdropState=0;

            btnPickDrop.setVisibility(View.VISIBLE);
            btnPickDrop.setText(getString(R.string.gpsMainBtnPickFrom));
            driverInfoLayout.setVisibility(View.INVISIBLE);
            if(fromMarker != null) {
                fromMarker.remove();
                fromMarker = null;
            }
            if(toMarker != null) {
                toMarker.remove();
                toMarker = null;
            }
            if(driverMarker != null) {
                driverMarker.remove();
                driverMarker = null;
            }
        }

        public void btnClicked(){
            if (mGoogleApiClient.isConnected()) {
                Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                if(mLastLocation == null) {
                    Toast.makeText(getApplicationContext(),getString(R.string.str_noLocation)
                            , Toast.LENGTH_LONG).show();
                    return;
                }
                LatLng currentPosition = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                //LatLng currentPosition = googleMap.getCameraPosition().target;
                if(fromMarker == null) {
                    MarkerOptions markerOptions = new MarkerOptions()
                            .position(currentPosition)
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                            //.draggable(true);
                            ;
                    fromMarker = googleMap.addMarker(markerOptions);
                } else {
                    fromMarker.setPosition(currentPosition);
                }
            }
            {
                MaterialDialog alertDialog = new MaterialDialog.Builder(GpsMainActivity.this)
                        .title(getString(R.string.app_name))
                        .content(getString(R.string.fromDesc))
                        .inputType(InputType.TYPE_CLASS_TEXT)
                        .input(getString(R.string.fromDescHint), null, new MaterialDialog.InputCallback() {
                            @Override
                            public void onInput(MaterialDialog dialog, CharSequence input) {
                                fromDesc = input.toString();
                            }
                        })
                        .build();
                alertDialog.show();
            }

            stateContext.setState(new passengerState_pickFrom(stateContext));
        }
        public void mapClicked(){
            Location location = LocationServices.FusedLocationApi.getLastLocation(
                    mGoogleApiClient);
            if (location != null) {
                UploadClass uc = new UploadClass(GpsMainActivity.this);
                uc.getNearbyDrivers(location);
            } else {
                Toast.makeText(getApplicationContext(),getString(R.string.str_noLocation)
                        , Toast.LENGTH_LONG).show();
            }
        }
        public boolean backPressed(){
            return false;
        }
    }

    public class passengerState_pickFrom implements passengerState {
        private passengerState_context stateContext;

        public passengerState_pickFrom(passengerState_context aStateContext) {
            pickdropState=1;
            btnPickDrop.setText(getString(R.string.gpsMainBtnDropto));
            if(toMarker != null) {
                toMarker.remove();
                toMarker = null;
            }
            stateContext = aStateContext;
        }

        public void btnClicked(){
            pickdropState = 2;
            if (mGoogleApiClient.isConnected()) {
                //Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                LatLng currentPosition = googleMap.getCameraPosition().target;//new LatLng(fromMarker.getPosition().latitude, fromMarker.getPosition().longitude+.003);
                if(toMarker == null) {
                    MarkerOptions markerOptions = new MarkerOptions()
                            .position(currentPosition)
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                            //.draggable(true)
                            ;
                    toMarker = googleMap.addMarker(markerOptions);
                } else {
                    toMarker.setPosition(currentPosition);
                }
            }
            {
                MaterialDialog alertDialog = new MaterialDialog.Builder(GpsMainActivity.this)
                        .title(getString(R.string.app_name))
                        .content(getString(R.string.toDesc))
                        .inputType(InputType.TYPE_CLASS_TEXT)
                        .input(getString(R.string.toDescHint), null, new MaterialDialog.InputCallback() {
                            @Override
                            public void onInput(MaterialDialog dialog, CharSequence input) {
                                toDesc = input.toString();
                            }
                        })
                        .build();
                alertDialog.show();
            }
            btnPickDrop.setText(getString(R.string.gpsMainBtnConfirm));

            stateContext.setState(new passengerState_dropTo(stateContext));
        }
        public void mapClicked(){}
        public boolean backPressed(){
            stateContext.setState(new passengerState_idle(stateContext));
            return true;
        }
    }

    public class passengerState_dropTo implements passengerState {
        private passengerState_context stateContext;

        public passengerState_dropTo(passengerState_context aStateContext) {
            pickdropState=2;
            stateContext = aStateContext;
        }

        public void btnClicked(){
            boolean wrapInScrollView = true;
            MaterialDialog dialog = new MaterialDialog.Builder(GpsMainActivity.this)
                    .title(getString(R.string.gpsMainFeeDlgTitle))
                    .customView(R.layout.dlg_fee, wrapInScrollView)
                    .positiveText(getString(R.string.gpsMainFeeDlgPositive))
                    .callback(new MaterialDialog.ButtonCallback() {
                        @Override
                        public void onPositive(MaterialDialog dialog) {
                            super.onPositive(dialog);
                            EditText editTextPhone = (EditText) dialog.getCustomView().findViewById(R.id.editTextPhone);
                            EditText editTextFee = (EditText) dialog.getCustomView().findViewById(R.id.editTextFee);
                            EditText editTextNoOfPassangers = (EditText) dialog.getCustomView().findViewById(R.id.editTextNoOfPassangers);
                            EditText editTextAdditionalNotes = (EditText) dialog.getCustomView().findViewById(R.id.editTextAdditionalNotes);

                            treqPhone = editTextPhone.getText().toString();
                            suggestedFee = editTextFee.getText().toString();
                            noOfPassangers = editTextNoOfPassangers.getText().toString();
                            additionalNotes = editTextAdditionalNotes.getText().toString();


                            UploadClass upload = new UploadClass(GpsMainActivity.this);
                            String lat1 = Double.toString(fromMarker.getPosition().latitude);
                            String long1 = Double.toString(fromMarker.getPosition().longitude);
                            String lat2 = Double.toString(toMarker.getPosition().latitude);
                            String long2 = Double.toString(toMarker.getPosition().longitude);
                            if(fromDesc ==null) {
                                fromDesc="";
                            }
                            if(toDesc ==null) {
                                toDesc="";
                            }
                            upload.addTRequest(AppSettings.getUid(), AppSettings.getEmail(),lat1, long1,
                                    lat2,long2,
                                    fromDesc, toDesc,
                                    treqPhone, suggestedFee, noOfPassangers, additionalNotes);

                            stateContext.setState(new passengerState_cancel(stateContext, 15*60));
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
            stateContext.setState(new passengerState_pickFrom(stateContext));
            //pickdropState=1;
            //btnPickDrop.setText(getString(R.string.gpsMainBtnDropto));
            return true;
        }
    }

    public class passengerState_cancel implements passengerState {
        private passengerState_context stateContext;

        public passengerState_cancel(passengerState_context aStateContext, int counter) {
            pickdropState=3;
            btnPickDrop.setVisibility(View.VISIBLE);
            driverInfoLayout.setVisibility(View.INVISIBLE);
            clearDriversMarkers();
            startCounter(counter);
            stateContext = aStateContext;
        }

        public void btnClicked(){
            cancelTRequest(Constants.TRequest_Canceled);

            //stateContext.setState(new passengerState_idle(stateContext));
        }

        public void mapClicked(){}
        public boolean backPressed(){
            return false;
        }
    }

    public class passengerState_reconnect implements passengerState {
        private passengerState_context stateContext;

        public passengerState_reconnect(passengerState_context aStateContext) {
            pickdropState=20;
            btnPickDrop.setText(getResources().getString(R.string.gpsMainBtnReconnect));
            btnPickDrop.setVisibility(View.VISIBLE);
            driverInfoLayout.setVisibility(View.INVISIBLE);

            stateContext = aStateContext;
        }
        public void btnClicked(){
            UploadClass uc = new UploadClass(GpsMainActivity.this);
            uc.getPassangerState(AppSettings.getUid());
        }
        public void mapClicked(){}
        public boolean backPressed(){
            return false;
        }
    }

    public class passengerState_assigned implements passengerState {
        private passengerState_context stateContext;

        public passengerState_assigned(passengerState_context aStateContext, TRequestObj tRequestObj) {
            pickdropState=30;
            stateContext = aStateContext;

            helpOverlayLayout.setVisibility(View.GONE);
            btnPickDrop.setVisibility(View.GONE);
            driverInfoLayout.setVisibility(View.VISIBLE);
            txtDriverName.setText(tRequestObj.driverName);
            txtDriverInfo.setText(tRequestObj.driverInfo);
            btnDriverPhone.setText(tRequestObj.driverPhone);
            {
                //final String IMAGE_URL = "http://developer.android.com/images/training/system-ui.png";
                ImageLoader mImageLoader = AppSettings.getInstance().getImageLoader();
                networkImageViewDriver.setImageUrl(tRequestObj.driverPhotoUrl, mImageLoader);
            }
            //driver not assigned yet & 15min elapsed => neglect it
            if(Integer.parseInt(tRequestObj.driverId) == -1) {
                int remainingSeconds = 900 - Integer.parseInt(tRequestObj.secondsToNow);
                if(remainingSeconds < 5) {
                    stateContext.setState(new passengerState_idle(stateContext));
                    return;
                } else {
                    //set timer to remainingSeconds
                    //startCounter(remainingSeconds);
                    stateContext.setState(new passengerState_cancel(stateContext, remainingSeconds));
                    AppSettings.requestId = tRequestObj.idx;
                    //pickdropState=3;
                    //btnPickDrop.setVisibility(View.VISIBLE);
                    //driverInfoLayout.setVisibility(View.INVISIBLE);
                }
            }
            //2. driver assigned or passanger picked
            zoom2TReqObj(tRequestObj);
        }
        public void btnClicked(){}
        public void mapClicked(){}
        public boolean backPressed(){
            return false;
        }
    }
    */
    public void zoom2TReqObj(TRequestObj tRequestObj) {
        LatLng currentPosition = new LatLng(tRequestObj.fromLat, tRequestObj.fromLong);
        if(fromMarker == null) {
            MarkerOptions markerOptions = new MarkerOptions()
                    .position(currentPosition)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                    //.draggable(true)
                    ;
            ;
            fromMarker = googleMap.addMarker(markerOptions);
        } else {
            fromMarker.setPosition(currentPosition);
        }

        currentPosition = new LatLng(tRequestObj.toLat, tRequestObj.toLong);
        if (toMarker == null) {
            MarkerOptions markerOptions = new MarkerOptions()
                    .position(currentPosition)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                    //.draggable(false)
                    ;
            toMarker = googleMap.addMarker(markerOptions);
        } else {
            toMarker.setPosition(currentPosition);
        }
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        {
            builder.include(fromMarker.getPosition());
            builder.include(toMarker.getPosition());
        }
        LatLngBounds bounds = builder.build();
        int padding = 120; // offset from edges of the map in pixels
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
        googleMap.animateCamera(cu);
    }
}
