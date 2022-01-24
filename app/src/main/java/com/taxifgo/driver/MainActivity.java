package com.taxifgo.driver;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.adapter.files.ManageVehicleListAdapter;
import com.fragments.InactiveFragment;
import com.fragments.MyBookingFragment;
import com.fragments.MyProfileFragment;
import com.fragments.MyWalletFragment;
import com.general.files.AddBottomBar;
import com.general.files.AlarmReceiver;
import com.general.files.AppFunctions;
import com.general.files.Closure;
import com.general.files.ConfigPubNub;
import com.general.files.CustomDialog;
import com.general.files.ExecuteWebServerUrl;
import com.general.files.FireTripStatusMsg;
import com.general.files.GeneralFunctions;
import com.general.files.GetAddressFromLocation;
import com.general.files.GetLocationUpdates;
import com.general.files.InternetConnection;
import com.general.files.MapAnimator;
import com.general.files.MyApp;
import com.general.files.NotificationScheduler;
import com.general.files.OpenAdvertisementDialog;
import com.general.files.SlideButton;
import com.general.files.StartActProcess;
import com.general.files.UpdateDirections;
import com.general.files.UpdateDriverStatus;
import com.general.files.UpdateFrequentTask;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.gms.maps.model.VisibleRegion;
import com.google.maps.android.heatmaps.Gradient;
import com.google.maps.android.heatmaps.HeatmapTileProvider;
import com.kyleduo.switchbutton.SwitchButton;
import com.pubnub.api.enums.PNStatusCategory;
import com.utils.Logger;
import com.utils.Utils;
import com.view.GenerateAlertBox;
import com.view.MButton;
import com.view.MTextView;
import com.view.MaterialRippleLayout;
import com.view.SelectableRoundedImageView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends BaseActivity implements OnMapReadyCallback, GetLocationUpdates.LocationUpdatesListener, GoogleMap.OnCameraChangeListener, UpdateFrequentTask.OnTaskRunCalled,
        ManageVehicleListAdapter.OnItemClickList, GetAddressFromLocation.AddressFound {

    public GeneralFunctions generalFunc;
    public Location userLocation;


    SupportMapFragment map;
    GoogleMap gMap;
    boolean isFirstLocation = true;
    ImageView userLocBtnImgView;


    MTextView onlineOfflineTxtView;
    MTextView ufxonlineOfflineTxtView, ufxTitleonlineOfflineTxtView;
    MTextView carNumPlateTxt;
    MTextView carNameTxt;
    MTextView changeCarTxt;
    MTextView addressTxtView, addressTxtViewufx;
    SwitchButton onlineOfflineSwitch, ufxonlineOfflineSwitch;
    ImageView refreshImgView;

    boolean isOnlineOfflineSwitchCalled = false;

    public boolean isDriverOnline = false;

    Intent startUpdatingStatus;

    String radiusval = "0";

    ArrayList<String> items_txt_car = new ArrayList<String>();
    ArrayList<String> items_txt_car_json = new ArrayList<String>();
    ArrayList<String> items_isHail_json = new ArrayList<String>();
    ArrayList<String> items_car_id = new ArrayList<String>();

    androidx.appcompat.app.AlertDialog list_car;
    androidx.appcompat.app.AlertDialog gender;

    MTextView joblocHTxtView, joblocHTxtViewufx;

    boolean isOnlineAvoid = false;

    String assignedTripId = "";
    String ENABLE_HAIL_RIDES = "";

    GetAddressFromLocation getAddressFromLocation;

    ExecuteWebServerUrl heatMapAsyncTask;
    HashMap<String, String> onlinePassengerLocList = new HashMap<String, String>();
    HashMap<String, String> historyLocList = new HashMap<String, String>();
    ArrayList<TileOverlay> mapOverlayList = new ArrayList<>();

    double radius_map = 0;

    Boolean isShowNearByPassengers = false;

    public String app_type = "Ride";
    int currentRequestPositions = 0;
    UpdateFrequentTask updateRequest;
    //BackgroundAppReceiver bgAppReceiver;

    boolean isCurrentReqHandled = false;


    public String selectedcar = "";

    LinearLayout mapbottomviewarea;
    RelativeLayout mapviewarea;
    boolean iswallet = false;


    InternetConnection intCheck;
    boolean isrefresh = false;

    private String getState = "GPS";
    ImageView menuufxImgView;

    RelativeLayout rideviewarea, ufxarea;
    MTextView ufxDrivername;

    boolean isFirstAddressLoaded = false;
    RelativeLayout pendingarea, upcomginarea;

    MTextView pendingjobHTxtView, pendingjobValTxtView, upcomingjobHTxtView, upcomingjobValTxtView;
    LinearLayout pendingMainArea;
    LinearLayout botomarea;
    MTextView radiusTxtView, radiusTxtViewufx;
    ImageView headerLogo, imageradiusufx, headerLogoride;

    RelativeLayout activearea;
    private JSONObject obj_userProfile;

    String HailEnableOnDriverStatus = "";

    boolean isBtnClick = false;

    boolean isCarChangeTxt = false;
    LinearLayout joblocareaufx;
    LinearLayout workArea;
    View workAreaLine;
    MTextView workTxt;
    MTextView btn_edit;
    boolean isfirstZoom = false;
    Location lastPublishedLoc = null;
    double PUBSUB_PUBLISH_DRIVER_LOC_DISTANCE_LIMIT = 5;
    private static final int SEL_CARD = 004;
    public static final int TRANSFER_MONEY = 87;


    AlertDialog cashBalAlertDialog;

    /*EndOfTheDay Trip view declaration start*/

    Marker sourceMarker, destMarker;
    private AlertDialog confirmDialog;
    private BottomSheetDialog faredialog;
    LinearLayout eodLocationArea;
    LinearLayout removeEodTripArea;
    MTextView addressTxt;
    int height;
    UpdateDirections updateDirections;
    /*EndOfTheDay Trip view declaration end*/
    CustomDialog customDialog;

    String LBL_LOAD_ADDRESS = "", LBL_GO_ONLINE_TXT = "", LBL_GO_OFFLINE_TXT = "";
    String LBL_ONLINE = "", LBL_OFFLINE = "";
    ImageView carImage;

    FloatingActionButton heat_action, heat_actionRTL;
    FloatingActionButton hail_action, hail_actionRTL;
    FloatingActionsMenu menuMultipleActions, multiple_actionsRTL;
    FloatingActionButton return_action, return_actionRTL;
    FloatingActionButton location_action, location_actionRTL;
    RelativeLayout selCarArea;
    MyProfileFragment myProfileFragment = null;
    MyWalletFragment myWalletFragment = null;
    public MyBookingFragment myBookingFragment = null;
    RelativeLayout Toolbar;
    ImageView notificationImg;

    View shadowView;
    AddBottomBar addBottomBar;
    FrameLayout containerufx;
    FrameLayout MainHeaderLayout;
    boolean iswalletFragemnt = false;
    boolean isbookingFragemnt = false;
    boolean isProfileFragment = false;
    public boolean isUfxServicesEnabled = true;
    int bottomBtnpos = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        generalFunc = MyApp.getInstance().getGeneralFun(getActContext());
        intCheck = new InternetConnection(this);
        isCarChangeTxt = true;
        obj_userProfile = generalFunc.getJsonObject(generalFunc.retrieveValue(Utils.USER_PROFILE_JSON));
        changeObj();
        addBottomBar = new AddBottomBar(getActContext(), obj_userProfile);

        String advertise_banner_data = generalFunc.getJsonValueStr("advertise_banner_data", obj_userProfile);
        if (advertise_banner_data != null && !advertise_banner_data.equalsIgnoreCase("")) {
            if (generalFunc.getJsonValue("image_url", advertise_banner_data) != null && !generalFunc.getJsonValue("image_url", advertise_banner_data).equalsIgnoreCase("")) {
                HashMap<String, String> map = new HashMap<>();
                map.put("image_url", generalFunc.getJsonValue("image_url", advertise_banner_data));
                map.put("tRedirectUrl", generalFunc.getJsonValue("tRedirectUrl", advertise_banner_data));
                map.put("vImageWidth", generalFunc.getJsonValue("vImageWidth", advertise_banner_data));
                map.put("vImageHeight", generalFunc.getJsonValue("vImageHeight", advertise_banner_data));
                new OpenAdvertisementDialog(getActContext(), map, generalFunc);
            }
        }
        shadowView = (View) findViewById(R.id.shadowView);
        menuMultipleActions = (FloatingActionsMenu) findViewById(R.id.multiple_actions);
        multiple_actionsRTL = (FloatingActionsMenu) findViewById(R.id.multiple_actionsRTL);


        if (generalFunc.isRTLmode()) {
            multiple_actionsRTL.setVisibility(View.VISIBLE);
            menuMultipleActions.setVisibility(View.GONE);
        } else {
            menuMultipleActions.setVisibility(View.VISIBLE);
            multiple_actionsRTL.setVisibility(View.GONE);
        }
        menuMultipleActions.setOnFloatingActionsMenuUpdateListener(new FloatingActionsMenu.OnFloatingActionsMenuUpdateListener() {
            @Override
            public void onMenuExpanded() {
                shadowView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onMenuCollapsed() {
                shadowView.setVisibility(View.GONE);

            }
        });

        if (generalFunc.isDeliverOnlyEnabled()) {
            menuMultipleActions.setVisibility(View.GONE);
            multiple_actionsRTL.setVisibility(View.GONE);
        }

        multiple_actionsRTL.setOnFloatingActionsMenuUpdateListener(new FloatingActionsMenu.OnFloatingActionsMenuUpdateListener() {
            @Override
            public void onMenuExpanded() {
                shadowView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onMenuCollapsed() {
                shadowView.setVisibility(View.GONE);

            }
        });

        if (generalFunc.isDeliverOnlyEnabled()) {
            multiple_actionsRTL.setVisibility(View.GONE);
        }

        MainHeaderLayout = (FrameLayout) findViewById(R.id.MainHeaderLayout);
        containerufx = (FrameLayout) findViewById(R.id.containerufx);
        notificationImg = (ImageView) findViewById(R.id.notificationImg);
        notificationImg.setOnClickListener(new setOnClickList());
        Toolbar = (RelativeLayout) findViewById(R.id.Toolbar);
        selCarArea = (RelativeLayout) findViewById(R.id.selCarArea);
        hail_action = (FloatingActionButton) findViewById(R.id.hail_action);
        hail_actionRTL = (FloatingActionButton) findViewById(R.id.hail_actionRTL);
        hail_action.setTitle(generalFunc.retrieveLangLBl("", "LBL_TAXI_HAIL"));
        hail_actionRTL.setTitle(generalFunc.retrieveLangLBl("", "LBL_TAXI_HAIL"));
        hail_action.setVisibility(View.GONE);
        hail_actionRTL.setVisibility(View.GONE);
        hail_action.setOnClickListener(new setOnClickList());
        hail_actionRTL.setOnClickListener(new setOnClickList());
        heat_action = (FloatingActionButton) findViewById(R.id.heat_action);
        heat_actionRTL = (FloatingActionButton) findViewById(R.id.heat_actionRTL);
        heat_action.setTitle(generalFunc.retrieveLangLBl("", "LBL_HEAT"));
        heat_actionRTL.setTitle(generalFunc.retrieveLangLBl("", "LBL_HEAT"));
        heat_action.setOnClickListener(new setOnClickList());
        heat_actionRTL.setOnClickListener(new setOnClickList());
        return_action = (FloatingActionButton) findViewById(R.id.return_action);
        return_actionRTL = (FloatingActionButton) findViewById(R.id.return_actionRTL);
        return_action.setTitle(generalFunc.retrieveLangLBl("", "LBL_RETURN"));
        return_actionRTL.setTitle(generalFunc.retrieveLangLBl("", "LBL_RETURN"));
        return_action.setVisibility(View.GONE);
        return_actionRTL.setVisibility(View.GONE);
        return_action.setOnClickListener(new setOnClickList());
        return_actionRTL.setOnClickListener(new setOnClickList());
        location_action = (FloatingActionButton) findViewById(R.id.location_action);
        location_actionRTL = (FloatingActionButton) findViewById(R.id.location_actionRTL);
        location_action.setVisibility(View.GONE);
        location_actionRTL.setVisibility(View.GONE);
        location_action.setTitle(generalFunc.retrieveLangLBl("", "LBL_LOCATIONS_TXT"));
        location_actionRTL.setTitle(generalFunc.retrieveLangLBl("", "LBL_LOCATIONS_TXT"));
        location_action.setOnClickListener(new setOnClickList());
        location_actionRTL.setOnClickListener(new setOnClickList());


        if (generalFunc.getJsonValueStr("ENABLE_NEWS_SECTION", obj_userProfile) != null && generalFunc.getJsonValueStr("ENABLE_NEWS_SECTION", obj_userProfile).equalsIgnoreCase("yes")) {
            notificationImg.setVisibility(View.VISIBLE);

        } else {
            notificationImg.setVisibility(View.GONE);
        }


        PUBSUB_PUBLISH_DRIVER_LOC_DISTANCE_LIMIT = GeneralFunctions.parseDoubleValue(5, generalFunc.retrieveValue(Utils.PUBSUB_PUBLISH_DRIVER_LOC_DISTANCE_LIMIT));

        getAddressFromLocation = new GetAddressFromLocation(getActContext(), generalFunc);
        getAddressFromLocation.setAddressList(this);


        refreshImgView = (ImageView) findViewById(R.id.refreshImgView);

        pendingarea = (RelativeLayout) findViewById(R.id.pendingarea);
        upcomginarea = (RelativeLayout) findViewById(R.id.upcomginarea);
        pendingarea.setOnClickListener(new setOnClickList());
        upcomginarea.setOnClickListener(new setOnClickList());
        rideviewarea = (RelativeLayout) findViewById(R.id.rideviewarea);
        pendingjobHTxtView = (MTextView) findViewById(R.id.pendingjobHTxtView);
        pendingjobValTxtView = (MTextView) findViewById(R.id.pendingjobValTxtView);
        upcomingjobHTxtView = (MTextView) findViewById(R.id.upcomingjobHTxtView);
        upcomingjobValTxtView = (MTextView) findViewById(R.id.upcomingjobValTxtView);
        radiusTxtView = (MTextView) findViewById(R.id.radiusTxtView);
        radiusTxtViewufx = (MTextView) findViewById(R.id.radiusTxtViewufx);
        imageradiusufx = (ImageView) findViewById(R.id.imageradiusufx);
        headerLogo = (ImageView) findViewById(R.id.headerLogo1);
        headerLogoride = (ImageView) findViewById(R.id.headerLogo);
        activearea = (RelativeLayout) findViewById(R.id.activearea);
        joblocareaufx = (LinearLayout) findViewById(R.id.joblocareaufx);
        workArea = (LinearLayout) findViewById(R.id.workArea);
        workAreaLine = (View) findViewById(R.id.workAreaLine);
        workTxt = (MTextView) findViewById(R.id.workTxt);
        btn_edit = (MTextView) findViewById(R.id.btn_edit);
        btn_edit.setOnClickListener(new setOnClickList());


        //  radiusTxtView.setOnClickListener(new setOnClickList());
        radiusTxtViewufx.setOnClickListener(new setOnClickList());


        imageradiusufx.setOnClickListener(new setOnClickList());
        refreshImgView.setOnClickListener(new setOnClickList());
        ufxarea = (RelativeLayout) findViewById(R.id.ufxarea);
        if (app_type.equals(Utils.CabGeneralType_UberX)) {
            rideviewarea.setVisibility(View.GONE);
            ufxarea.setVisibility(View.VISIBLE);
            setRadiusVal();
        } else {
            rideviewarea.setVisibility(View.VISIBLE);
            ufxarea.setVisibility(View.GONE);
        }

        if (app_type.equalsIgnoreCase(Utils.CabGeneralTypeRide_Delivery_UberX)) {
            setRadiusVal();
        }


        userLocBtnImgView = (ImageView) findViewById(R.id.userLocBtnImgView);

        menuufxImgView = (ImageView) findViewById(R.id.menuufxImgView);
        joblocHTxtView = (MTextView) findViewById(R.id.joblocHTxtView);
        joblocHTxtViewufx = (MTextView) findViewById(R.id.joblocHTxtViewufx);
        addressTxtView = (MTextView) findViewById(R.id.addressTxtView);
        addressTxtViewufx = (MTextView) findViewById(R.id.addressTxtViewufx);
        menuufxImgView.setOnClickListener(new setOnClickList());
        ufxDrivername = (MTextView) findViewById(R.id.ufxDrivername);
        pendingMainArea = (LinearLayout) findViewById(R.id.pendingMainArea);
        botomarea = (LinearLayout) findViewById(R.id.botomarea);

        pendingjobHTxtView.setText(generalFunc.retrieveLangLBl("Pending Jobs", "LBL_PENDING_JOBS"));
        upcomingjobHTxtView.setText(generalFunc.retrieveLangLBl("Upcoming Jobs", "LBL_UPCOMING_JOBS"));

        joblocHTxtView.setText(generalFunc.retrieveLangLBl("Your Job Location", "LBL_YOUR_JOB_LOCATION_TXT"));
        joblocHTxtViewufx.setText(generalFunc.retrieveLangLBl("Your Job Location", "LBL_YOUR_JOB_LOCATION_TXT"));

        LBL_LOAD_ADDRESS = generalFunc.retrieveLangLBl("", "LBL_LOAD_ADDRESS");
        LBL_GO_ONLINE_TXT = generalFunc.retrieveLangLBl("", "LBL_GO_ONLINE_TXT");
        LBL_GO_OFFLINE_TXT = generalFunc.retrieveLangLBl("", "LBL_GO_OFFLINE_TXT");
        LBL_ONLINE = generalFunc.retrieveLangLBl("", "LBL_ONLINE");
        LBL_OFFLINE = generalFunc.retrieveLangLBl("", "LBL_OFFLINE");

        addressTxtView.setText(LBL_LOAD_ADDRESS);
        addressTxtViewufx.setText(LBL_LOAD_ADDRESS);
        btn_edit.setText(generalFunc.retrieveLangLBl("", "LBL_EDIT"));
        workTxt.setText(generalFunc.retrieveLangLBl("", "LBL_EDIT_WORK_LOCATION"));
        handleWorkAddress();


        showHeatMap();


        /*EndOfTheDay Trip view initialization start*/
        ((MTextView) findViewById(R.id.destinationModeHintTxt)).setText(generalFunc.retrieveLangLBl("", "LBL_DESTINATION_MODE_ON_TXT"));

        eodLocationArea = (LinearLayout) findViewById(R.id.eodLocationArea);
        removeEodTripArea = (LinearLayout) findViewById(R.id.removeEodTripArea);
        removeEodTripArea.setOnClickListener(new setOnClickList());
        addressTxt = (MTextView) findViewById(R.id.addressTxt);


        mapviewarea = (RelativeLayout) findViewById(R.id.mapviewarea);
        mapbottomviewarea = (LinearLayout) findViewById(R.id.mapbottomviewarea);


        onlineOfflineTxtView = (MTextView) findViewById(R.id.onlineOfflineTxtView);
        ufxonlineOfflineTxtView = (MTextView) findViewById(R.id.ufxonlineOfflineTxtView);
        ufxTitleonlineOfflineTxtView = (MTextView) findViewById(R.id.ufxTitleonlineOfflineTxtView);
        carNumPlateTxt = (MTextView) findViewById(R.id.carNumPlateTxt);
        carNameTxt = (MTextView) findViewById(R.id.carNameTxt);
        carImage = (ImageView) findViewById(R.id.carImage);
        changeCarTxt = (MTextView) findViewById(R.id.changeCarTxt);
        onlineOfflineSwitch = (SwitchButton) findViewById(R.id.onlineOfflineSwitch);
        onlineOfflineSwitch.setText(LBL_ONLINE, LBL_OFFLINE);
        onlineOfflineSwitch.setTextColor(getActContext().getResources().getColor(R.color.appThemeColor_1));

        ufxonlineOfflineSwitch = (SwitchButton) findViewById(R.id.ufxonlineOfflineSwitch);
        ufxonlineOfflineSwitch.setText(LBL_ONLINE, LBL_OFFLINE);
        ufxonlineOfflineSwitch.setTextColor(getActContext().getResources().getColor(R.color.appThemeColor_1));

        map = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapV2);


        startUpdatingStatus = new Intent(getApplicationContext(), UpdateDriverStatus.class);

        if (generalFunc.isRTLmode()) {
            addressTxt.setBackgroundResource(R.drawable.ic_shape_rtl);
        }

        //bgAppReceiver = new BackgroundAppReceiver(getActContext());


        ufxDrivername.setText(generalFunc.getJsonValueStr("vName", obj_userProfile) + " "
                + generalFunc.getJsonValueStr("vLastName", obj_userProfile));


        setGeneralData();

        // buildMenu();

        setUserInfo();

        if (generalFunc.getJsonValueStr("RIDE_LATER_BOOKING_ENABLED", obj_userProfile).equalsIgnoreCase("Yes")) {
            pendingMainArea.setVisibility(View.VISIBLE);
            botomarea.setVisibility(View.VISIBLE);
        } else {
            pendingMainArea.setVisibility(View.GONE);
            botomarea.setVisibility(View.GONE);
        }

        map.getMapAsync(MainActivity.this);


        changeCarTxt.setOnClickListener(new setOnClickList());

        userLocBtnImgView.setOnClickListener(new setOnClickList());


        if (app_type.equals(Utils.CabGeneralType_UberX)) {

            ufxonlineOfflineSwitch.setOnTouchListener((v, event) -> {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_UP:
                        isOnlineOfflineSwitchCalled = true;
                        break;
                }
                return false;
            });

            ufxonlineOfflineSwitch.setOnCheckedChangeListener((compoundButton, b) -> {


                if (!intCheck.isNetworkConnected()) {
                    isOnlineOfflineSwitchCalled = false;
                    generalFunc.showGeneralMessage("",
                            generalFunc.retrieveLangLBl("No Internet Connection", "LBL_NO_INTERNET_TXT"));
                    return;
                }

                if (b) {
                    ufxonlineOfflineSwitch.setThumbColorRes(R.color.Green);
                    ufxonlineOfflineSwitch.setBackColorRes(R.color.white);
                } else {
                    ufxonlineOfflineSwitch.setThumbColorRes(android.R.color.holo_red_dark);
                    ufxonlineOfflineSwitch.setBackColorRes(android.R.color.white);
                }


                if (isOnlineAvoid) {
                    isOnlineAvoid = false;
                    isOnlineOfflineSwitchCalled = false;
                    return;
                }

                goOnlineOffline(b, true);
                isOnlineOfflineSwitchCalled = false;
            });

        } else {
            onlineOfflineSwitch.setOnTouchListener((v, event) -> {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_UP:
                        isOnlineOfflineSwitchCalled = true;
                        break;
                }
                return false;
            });

            onlineOfflineSwitch.setOnCheckedChangeListener((compoundButton, b) -> {

                multiple_actionsRTL.collapse();
                menuMultipleActions.collapse();
                shadowView.setVisibility(View.GONE);

                if (!intCheck.isNetworkConnected()) {
                    isOnlineOfflineSwitchCalled = false;
                    generalFunc.showGeneralMessage("",
                            generalFunc.retrieveLangLBl("No Internet Connection", "LBL_NO_INTERNET_TXT"));
                    return;
                }

                if (b) {
                    onlineOfflineSwitch.setThumbColorRes(R.color.Green);
                    onlineOfflineSwitch.setBackColorRes(android.R.color.white);
                } else {
                    onlineOfflineSwitch.setThumbColorRes(android.R.color.holo_red_dark);
                    onlineOfflineSwitch.setBackColorRes(android.R.color.white);
                }

                if (isOnlineAvoid) {
                    isOnlineAvoid = false;
                    isOnlineOfflineSwitchCalled = false;
                    return;
                }

                goOnlineOffline(b, true);
                isOnlineOfflineSwitchCalled = false;
                MainActivity.super.onResume();
            });

        }

        if (savedInstanceState != null) {
            String restratValue_str = savedInstanceState.getString("RESTART_STATE");

            if (restratValue_str != null && !restratValue_str.equals("") && restratValue_str.trim().equals("true")) {
                generalFunc.restartApp();
            }
        }

        generalFunc.storeData(Utils.DRIVER_CURRENT_REQ_OPEN_KEY, "false");

        JSONArray arr_CurrentRequests = generalFunc.getJsonArray("CurrentRequests", obj_userProfile);

        if (arr_CurrentRequests != null && arr_CurrentRequests.length() > 0) {
            updateRequest = new UpdateFrequentTask(5 * 1000);
//            this.updateRequest = updateRequest;
            updateRequest.setTaskRunListener(this);
            updateRequest.startRepeatingTask();
        } else {
            removeOldRequestsCode();
            isCurrentReqHandled = true;
        }

        //registerBackgroundAppReceiver();

        // if (generalFunc.getJsonValue("APP_TYPE", userProfileJson).equalsIgnoreCase("UberX")) {
        if (generalFunc.getJsonValueStr("APP_TYPE", obj_userProfile).equalsIgnoreCase("UberX")) {
            changeCarTxt.setText(generalFunc.getJsonValueStr("vName", obj_userProfile) + " "
                    + generalFunc.getJsonValueStr("vLastName", obj_userProfile));
            changeCarTxt.setOnClickListener(null);

            carNumPlateTxt.setVisibility(View.GONE);
            carNameTxt.setVisibility(View.GONE);
            carImage.setVisibility(View.GONE);
        }

        String eStatus = generalFunc.getJsonValueStr("eStatus", obj_userProfile);

        if (eStatus.equalsIgnoreCase("inactive")) {
            mapbottomviewarea.setVisibility(View.GONE);
            mapviewarea.setVisibility(View.GONE);
            menuMultipleActions.setVisibility(View.GONE);
            multiple_actionsRTL.setVisibility(View.GONE);
            return_action.setVisibility(View.GONE);
            return_actionRTL.setVisibility(View.GONE);
            return_action.setVisibility(View.GONE);
            return_actionRTL.setVisibility(View.GONE);
            headerLogo.setVisibility(View.VISIBLE);
            onlineOfflineSwitch.setVisibility(View.GONE);
            selCarArea.setVisibility(View.GONE);
            headerLogoride.setVisibility(View.VISIBLE);
//            notificationImg.setVisibility(View.GONE);
            ((SelectableRoundedImageView) findViewById(R.id.userPicImgView)).setVisibility(View.GONE);
            InactiveFragment inactiveFragment = new InactiveFragment();
            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            if (app_type.equals(Utils.CabGeneralType_UberX)) {
                activearea.setVisibility(View.GONE);
                ft.replace(R.id.containerufx, inactiveFragment);
                ft.commit();

            } else {
                ft.replace(R.id.container, inactiveFragment);
                ft.commit();
            }
        } else {
            onlineOfflineSwitch.setVisibility(View.VISIBLE);
            selCarArea.setVisibility(View.VISIBLE);
            headerLogoride.setVisibility(View.GONE);
            ((SelectableRoundedImageView) findViewById(R.id.userPicImgView)).setVisibility(View.VISIBLE);

            if (app_type.equalsIgnoreCase(Utils.CabGeneralTypeRide_Delivery_UberX)) {
                joblocareaufx.setVisibility(View.GONE);
            }

            if (app_type.equals(Utils.CabGeneralType_UberX)) {

                refreshImgView.setVisibility(View.VISIBLE);

            }

            headerLogo.setVisibility(View.GONE);

            if (isDriverOnline) {
                isHailRideOptionEnabled();
            }
            mapbottomviewarea.setVisibility(View.VISIBLE);
            mapviewarea.setVisibility(View.VISIBLE);


            handleNoLocationDial();

        }

        generalFunc.deleteTripStatusMessages();

        GetLocationUpdates.getInstance().setTripStartValue(false, false, "");

//        boolean isEmailVerified = generalFunc.getJsonValueStr("eEmailVerified", obj_userProfile).equalsIgnoreCase("YES");
        boolean isEmailVerified = true;
        boolean isPhoneVerified = generalFunc.getJsonValueStr("ePhoneVerified", obj_userProfile).equalsIgnoreCase("YES");

        if (!isEmailVerified ||
                !isPhoneVerified) {

            Bundle bn = new Bundle();
            if (!isEmailVerified &&
                    !isPhoneVerified) {
                bn.putString("msg", "DO_EMAIL_PHONE_VERIFY");
            } else if (!isEmailVerified) {
                bn.putString("msg", "DO_EMAIL_VERIFY");
            } else if (!isPhoneVerified) {
                bn.putString("msg", "DO_PHONE_VERIFY");
            }

            if (!eStatus.equalsIgnoreCase("inactive")) {
                //  bn.putString("UserProfileJson", userProfileJson);
                showMessageWithAction(onlineOfflineTxtView, generalFunc.retrieveLangLBl("", "LBL_ACCOUNT_VERIFY_ALERT_TXT"), bn);
            }
        }

        if ((app_type.equalsIgnoreCase(Utils.CabGeneralTypeRide_Delivery_UberX) && generalFunc.getJsonValueStr("eShowVehicles", obj_userProfile).equalsIgnoreCase("No"))) {
            (findViewById(R.id.changeCarArea)).setVisibility(View.GONE);
        }

    }

    private void changeObj() {
        app_type = generalFunc.getJsonValueStr("APP_TYPE", obj_userProfile);
        String UFX_SERVICE_AVAILABLE = generalFunc.getJsonValueStr("UFX_SERVICE_AVAILABLE", obj_userProfile);
        isUfxServicesEnabled = !Utils.checkText(UFX_SERVICE_AVAILABLE) || (UFX_SERVICE_AVAILABLE!=null &&UFX_SERVICE_AVAILABLE.equalsIgnoreCase("Yes"));
    }

    public void openProfileFragment() {
        iswalletFragemnt = false;
        isbookingFragemnt = false;
        isProfileFragment = true;
        multiple_actionsRTL.collapse();


//        if (myProfileFragment != null) {
//            myProfileFragment = null;
//            Utils.runGC();
//        }

        menuMultipleActions.collapse();
        shadowView.setVisibility(View.GONE);
        eodLocationArea.setVisibility(View.GONE);

        mapbottomviewarea.setVisibility(View.GONE);
        selCarArea.setVisibility(View.GONE);
        mapviewarea.setVisibility(View.GONE);
        menuMultipleActions.setVisibility(View.GONE);
        multiple_actionsRTL.setVisibility(View.GONE);
        Toolbar.setVisibility(View.GONE);
        refreshImgView.setVisibility(View.GONE);

        if (myProfileFragment == null) {
            myProfileFragment = new MyProfileFragment();
        }


        if (app_type.equals(Utils.CabGeneralType_UberX)) {
            //activearea.setVisibility(View.GONE);
            containerufx.setVisibility(View.VISIBLE);
            MainHeaderLayout.setVisibility(View.GONE);
//            getSupportFragmentManager().beginTransaction()
//                    .replace(R.id.containerufx, myProfileFragment).commit();
        }
//        else {
//            getSupportFragmentManager().beginTransaction()
//                    .replace(R.id.container, myProfileFragment).commit();
//        }


        openPageFrag(4, myProfileFragment);

        bottomBtnpos = 4;


    }


    public void openPageFrag(int position, Fragment fragToOpen) {
        int leftAnim = bottomBtnpos > position ? R.anim.slide_from_left : R.anim.slide_from_right;
        int rightAnim = bottomBtnpos > position ? R.anim.slide_to_right : R.anim.slide_to_left;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations(leftAnim,rightAnim)
                    .replace(R.id.container, fragToOpen)
                    .commit();
        }else {
            getSupportFragmentManager()
                    .beginTransaction()
                    /*.setCustomAnimations(leftAnim,rightAnim)*/
                    .replace(R.id.container, fragToOpen)
                    .commit();
        }
    }

    public void openWalletFrgament() {
        iswalletFragemnt = true;
        isbookingFragemnt = false;
        isProfileFragment = false;
        eodLocationArea.setVisibility(View.GONE);
        menuMultipleActions.collapse();
        multiple_actionsRTL.collapse();

        shadowView.setVisibility(View.GONE);

        mapbottomviewarea.setVisibility(View.GONE);
        selCarArea.setVisibility(View.GONE);
        mapviewarea.setVisibility(View.GONE);
        menuMultipleActions.setVisibility(View.GONE);
        multiple_actionsRTL.setVisibility(View.GONE);
        Toolbar.setVisibility(View.GONE);
        refreshImgView.setVisibility(View.GONE);

        if (myWalletFragment == null) {
            myWalletFragment = new MyWalletFragment();
        }


        if (app_type.equals(Utils.CabGeneralType_UberX)) {
            //activearea.setVisibility(View.GONE);
            containerufx.setVisibility(View.VISIBLE);
            MainHeaderLayout.setVisibility(View.GONE);
//            getSupportFragmentManager().beginTransaction()
//                    .replace(R.id.containerufx, myWalletFragment).commit();
        }
//        else {
//            getSupportFragmentManager().beginTransaction()
//                    .replace(R.id.container, myWalletFragment).commit();
//        }

        openPageFrag(3, myWalletFragment);

        bottomBtnpos = 3;
    }


    public void openBookingFrgament() {
        Logger.d("HistoryFragment", ":: openBookingFrgament");
        isbookingFragemnt = true;
        iswalletFragemnt = false;
        isProfileFragment = false;
        menuMultipleActions.collapse();
        multiple_actionsRTL.collapse();

        eodLocationArea.setVisibility(View.GONE);

        shadowView.setVisibility(View.GONE);

        mapbottomviewarea.setVisibility(View.GONE);
        selCarArea.setVisibility(View.GONE);
        mapviewarea.setVisibility(View.GONE);
        menuMultipleActions.setVisibility(View.GONE);
        multiple_actionsRTL.setVisibility(View.GONE);
        Toolbar.setVisibility(View.GONE);
        refreshImgView.setVisibility(View.GONE);

        if (myBookingFragment == null) {
            myBookingFragment = new MyBookingFragment();
        } else {
            myBookingFragment.onDestroy();
            myBookingFragment = new MyBookingFragment();
        }


        if (app_type.equals(Utils.CabGeneralType_UberX)) {
            //activearea.setVisibility(View.GONE);
            containerufx.setVisibility(View.VISIBLE);
            MainHeaderLayout.setVisibility(View.GONE);
//            getSupportFragmentManager().beginTransaction()
//                    .replace(R.id.containerufx, myBookingFragment).commit();
        }
//        else {
//            getSupportFragmentManager().beginTransaction()
//                    .replace(R.id.container, myBookingFragment).commit();
//        }

        openPageFrag(2, myBookingFragment);

        bottomBtnpos = 2;
    }

    private void showHeatMap() {
        if (app_type.equalsIgnoreCase(Utils.CabGeneralType_UberX) || generalFunc.isDeliverOnlyEnabled()) {
            heat_action.setVisibility(View.GONE);
            heat_actionRTL.setVisibility(View.GONE);
        } else {
            heat_action.setVisibility(View.VISIBLE);
            heat_actionRTL.setVisibility(View.VISIBLE);
        }
    }


    public void handleWorkAddress() {
        if (generalFunc.getJsonValueStr("PROVIDER_AVAIL_LOC_CUSTOMIZE", obj_userProfile).equalsIgnoreCase("Yes")) {

            if (generalFunc.getJsonValueStr("eSelectWorkLocation", obj_userProfile).equalsIgnoreCase("Fixed")) {
                String WORKLOCATION = generalFunc.retrieveValue(Utils.WORKLOCATION);
                if (!WORKLOCATION.equals("")) {
                    addressTxtView.setText(WORKLOCATION);
                    addressTxtViewufx.setText(WORKLOCATION);
                } else {
                    if (userLocation != null) {
                        getAddressFromLocation.setLocation(userLocation.getLatitude(), userLocation.getLongitude());
                        getAddressFromLocation.execute();
                        addressTxtView.setText(LBL_LOAD_ADDRESS);
                        addressTxtViewufx.setText(LBL_LOAD_ADDRESS);
                    }
                }
            } else {
                if (userLocation != null) {
                    getAddressFromLocation.setLocation(userLocation.getLatitude(), userLocation.getLongitude());
                    getAddressFromLocation.execute();
                    addressTxtView.setText(LBL_LOAD_ADDRESS);
                    addressTxtViewufx.setText(LBL_LOAD_ADDRESS);

                }

            }
        }
    }

    public void setRadiusVal() {

        if (obj_userProfile != null && !generalFunc.getJsonValueStr("eUnit", obj_userProfile).equalsIgnoreCase("KMs")) {

            radiusTxtView.setText(generalFunc.retrieveLangLBl("Within", "LBL_WITHIN") + " " + radiusval + " " + generalFunc.retrieveLangLBl("", "LBL_MILE_DISTANCE_TXT") + " " +
                    generalFunc.retrieveLangLBl("Work Radius", "LBL_RADIUS"));
            radiusTxtViewufx.setText(generalFunc.retrieveLangLBl("Within", "LBL_WITHIN") + " " + radiusval + " " + generalFunc.retrieveLangLBl("", "LBL_MILE_DISTANCE_TXT") + " " +
                    generalFunc.retrieveLangLBl("Work Radius", "LBL_RADIUS"));
        } else {
            radiusTxtView.setText(generalFunc.retrieveLangLBl("Within", "LBL_WITHIN") + " " + radiusval + " " + generalFunc.retrieveLangLBl("", "LBL_KM_DISTANCE_TXT") + " " +
                    generalFunc.retrieveLangLBl("Work Radius", "LBL_RADIUS"));
            radiusTxtViewufx.setText(generalFunc.retrieveLangLBl("Within", "LBL_WITHIN") + " " + radiusval + " " + generalFunc.retrieveLangLBl("", "LBL_KM_DISTANCE_TXT") + " " +
                    generalFunc.retrieveLangLBl("Work Radius", "LBL_RADIUS"));
        }

    }


    private void isHailRideOptionEnabled() {

        if ((faredialog != null && faredialog.isShowing()) || eodLocationArea.getVisibility() == View.VISIBLE) {

            return_action.setVisibility(View.GONE);
            return_actionRTL.setVisibility(View.GONE);
            return;
        }

        enableEOD();

        boolean eDestinationMode = generalFunc.getJsonValueStr("eDestinationMode", obj_userProfile).equalsIgnoreCase("Yes");
        if (eDestinationMode) {
            return;
        }

        if (!HailEnableOnDriverStatus.equalsIgnoreCase("") && HailEnableOnDriverStatus.equalsIgnoreCase("No")) {
            hail_action.setVisibility(View.GONE);
            hail_actionRTL.setVisibility(View.GONE);
            return;
        }
        String eStatus = generalFunc.getJsonValueStr("eStatus", obj_userProfile);

        if (!eStatus.equalsIgnoreCase("inactive")) {
            ENABLE_HAIL_RIDES = generalFunc.getJsonValueStr("ENABLE_HAIL_RIDES", obj_userProfile);
            if (ENABLE_HAIL_RIDES.equalsIgnoreCase("Yes")
                    && HailEnableOnDriverStatus.equalsIgnoreCase("Yes") && !generalFunc.isDeliverOnlyEnabled()) {
                hail_action.setVisibility(View.VISIBLE);
                hail_actionRTL.setVisibility(View.VISIBLE);
            } else {
                hail_action.setVisibility(View.GONE);
                hail_actionRTL.setVisibility(View.GONE);
            }
        } else {
            hail_action.setVisibility(View.GONE);
            hail_actionRTL.setVisibility(View.GONE);
        }


    }

    public void removeOldRequestsCode() {

        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(getActContext());
        Map<String, ?> keys = mPrefs.getAll();

        for (Map.Entry<String, ?> entry : keys.entrySet()) {

            if (entry.getKey().contains(Utils.DRIVER_REQ_CODE_PREFIX_KEY)) {
                //generalFunc.removeValue(entry.getKey());
                Long CURRENTmILLI = System.currentTimeMillis() - (1000 * 60 * 60 * 24 * 1);
                String value_ = generalFunc.retrieveValue(entry.getKey()) + "";
                long value = generalFunc.parseLongValue(0, value_);
                if (CURRENTmILLI >= value) {
                    generalFunc.removeValue(entry.getKey());
                }
            }
        }
    }


    boolean isFirstRunTaskSkipped = false;

    @Override
    public void onTaskRun() {
        if (isFirstRunTaskSkipped == false) {
            isFirstRunTaskSkipped = true;
            return;
        }
        if (generalFunc.retrieveValue(Utils.DRIVER_CURRENT_REQ_OPEN_KEY).equals("true")) {
            return;
        }

        JSONArray arr_CurrentRequests = generalFunc.getJsonArray("CurrentRequests", obj_userProfile);

        if (currentRequestPositions < arr_CurrentRequests.length()) {
            JSONObject obj_temp = generalFunc.getJsonObject(arr_CurrentRequests, currentRequestPositions);

            String message_str = generalFunc.getJsonValueStr("tMessage", obj_temp).replace("\\\"", "\"");

            String MsgCode = generalFunc.getJsonValue("MsgCode", message_str);
            String codeKey = Utils.DRIVER_REQ_CODE_PREFIX_KEY + MsgCode;

            if (generalFunc.retrieveValue(codeKey).equals("") && !generalFunc.containsKey(Utils.DRIVER_REQ_COMPLETED_MSG_CODE_KEY + MsgCode)) {
                generalFunc.storeData(codeKey, "true");

                generalFunc.storeData(Utils.DRIVER_CURRENT_REQ_OPEN_KEY, "true");

                (new FireTripStatusMsg(getActContext(), "Script")).fireTripMsg(message_str);
            }

            currentRequestPositions++;
        } else if (updateRequest != null) {
            updateRequest.stopRepeatingTask();
            updateRequest = null;

            isCurrentReqHandled = true;
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {

        outState.putString("RESTART_STATE", "true");
        super.onSaveInstanceState(outState);
    }


    public void setUserInfo() {
        obj_userProfile = generalFunc.getJsonObject(generalFunc.retrieveValue(Utils.USER_PROFILE_JSON));
        changeObj();

        String DRIVER_ONLINE_KEY = generalFunc.retrieveValue(Utils.DRIVER_ONLINE_KEY);

        if (app_type.equals(Utils.CabGeneralType_UberX)) {

            if (DRIVER_ONLINE_KEY != null && DRIVER_ONLINE_KEY.equalsIgnoreCase("true")) {
                ufxonlineOfflineTxtView.setText(LBL_GO_OFFLINE_TXT);
                ufxTitleonlineOfflineTxtView.setText(LBL_ONLINE);
            } else {
                ufxonlineOfflineTxtView.setText(LBL_GO_ONLINE_TXT);
                ufxTitleonlineOfflineTxtView.setText(LBL_OFFLINE);
            }


            (new AppFunctions(getActContext())).checkProfileImage((SelectableRoundedImageView) findViewById(R.id.driverImgView), obj_userProfile.toString(), "vImage");

        } else {

            (new AppFunctions(getActContext())).checkProfileImage((SelectableRoundedImageView) findViewById(R.id.userPicImgView), obj_userProfile.toString(), "vImage");
            if (DRIVER_ONLINE_KEY != null && DRIVER_ONLINE_KEY.equalsIgnoreCase("true")) {
                onlineOfflineTxtView.setText(LBL_GO_OFFLINE_TXT);

            } else {
                onlineOfflineTxtView.setText(LBL_GO_ONLINE_TXT);

            }

        }
        // (new AppFunctions(getActContext())).checkProfileImage((SelectableRoundedImageView) findViewById(R.id.userImgView), obj_userProfile.toString(), "vImage");


        if (app_type.equals(Utils.CabGeneralType_UberX)) {
            changeCarTxt.setText(generalFunc.getJsonValueStr("vName", obj_userProfile) + " "
                    + generalFunc.getJsonValueStr("vLastName", obj_userProfile));
            changeCarTxt.setOnClickListener(null);
            carNumPlateTxt.setVisibility(View.GONE);
            carNameTxt.setVisibility(View.GONE);
            carImage.setVisibility(View.GONE);
        } else {
            changeCarTxt.setText(generalFunc.retrieveLangLBl("", "LBL_CHANGE"));
        }

        if (isCarChangeTxt) {
            String iDriverVehicleId = generalFunc.getJsonValueStr("iDriverVehicleId", obj_userProfile);
            setCarInfo(iDriverVehicleId);
        }


    }

    public void showMessageWithAction(View view, String message, final Bundle bn) {
        Snackbar snackbar = Snackbar
                .make(view, message, Snackbar.LENGTH_INDEFINITE).setAction(generalFunc.retrieveLangLBl("", "LBL_BTN_VERIFY_TXT"), new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
//                        bn.putString("msg", "DO_PHONE_VERIFY");
                        new StartActProcess(getActContext()).startActForResult(VerifyInfoActivity.class, bn, Utils.VERIFY_INFO_REQ_CODE);

                    }
                });
        snackbar.setActionTextColor(getActContext().getResources().getColor(R.color.verfiybtncolor));
        snackbar.setDuration(10000);
        snackbar.show();
    }


    public void setGeneralData() {
        HashMap<String, String> storeData = new HashMap<>();
        storeData.put(Utils.MOBILE_VERIFICATION_ENABLE_KEY, generalFunc.getJsonValueStr("MOBILE_VERIFICATION_ENABLE", obj_userProfile));
        storeData.put("LOCATION_ACCURACY_METERS", generalFunc.getJsonValueStr("LOCATION_ACCURACY_METERS", obj_userProfile));
        storeData.put("DRIVER_LOC_UPDATE_TIME_INTERVAL", generalFunc.getJsonValueStr("DRIVER_LOC_UPDATE_TIME_INTERVAL", obj_userProfile));
        storeData.put(Utils.REFERRAL_SCHEME_ENABLE, generalFunc.getJsonValueStr("REFERRAL_SCHEME_ENABLE", obj_userProfile));

        storeData.put(Utils.WALLET_ENABLE, generalFunc.getJsonValueStr("WALLET_ENABLE", obj_userProfile));
        storeData.put(Utils.REFERRAL_SCHEME_ENABLE, generalFunc.getJsonValueStr("REFERRAL_SCHEME_ENABLE", obj_userProfile));
        storeData.put(Utils.SMS_BODY_KEY, generalFunc.getJsonValueStr(Utils.SMS_BODY_KEY, obj_userProfile));
        storeData.put(Utils.PUBSUB_PUBLISH_DRIVER_LOC_DISTANCE_LIMIT, generalFunc.getJsonValueStr("PUBSUB_PUBLISH_DRIVER_LOC_DISTANCE_LIMIT", obj_userProfile));
        generalFunc.storeData(storeData);
    }

    public void setCarInfo(String iDriverVehicleId) {
        if (!iDriverVehicleId.equals("") && !iDriverVehicleId.equals("0")) {
            String vLicencePlateNo = generalFunc.getJsonValueStr("vLicencePlateNo", obj_userProfile);
            carNumPlateTxt.setText(vLicencePlateNo);
            carNumPlateTxt.setVisibility(View.VISIBLE);

            String vMake = generalFunc.getJsonValueStr("vMake", obj_userProfile);
            String vModel = generalFunc.getJsonValueStr("vModel", obj_userProfile);

            selectedcar = iDriverVehicleId;

            if (app_type.equals(Utils.CabGeneralType_UberX)) {
                changeCarTxt.setText(generalFunc.getJsonValueStr("vName", obj_userProfile) + " "
                        + generalFunc.getJsonValueStr("vLastName", obj_userProfile));
                changeCarTxt.setOnClickListener(null);
                carNumPlateTxt.setVisibility(View.GONE);
                carNameTxt.setVisibility(View.GONE);
                carImage.setVisibility(View.GONE);
            } else {
                carNameTxt.setText(vMake + " " + vModel);
                carNameTxt.setVisibility(View.VISIBLE);
                carImage.setVisibility(View.VISIBLE);
                changeCarTxt.setText(generalFunc.retrieveLangLBl("", "LBL_CHANGE"));
            }
        } else {
            if (app_type.equals(Utils.CabGeneralType_UberX)) {
                changeCarTxt.setText(generalFunc.getJsonValueStr("vName", obj_userProfile) + " "
                        + generalFunc.getJsonValueStr("vLastName", obj_userProfile));
                changeCarTxt.setOnClickListener(null);
                carNumPlateTxt.setVisibility(View.GONE);
                carNameTxt.setVisibility(View.GONE);
                carImage.setVisibility(View.GONE);
            } else {
                changeCarTxt.setText(generalFunc.retrieveLangLBl("Choose car", "LBL_CHOOSE_CAR"));
            }

        }
    }

    @Override
    public void onCameraChange(CameraPosition cameraPosition) {

        if (this.userLocation == null || isShowNearByPassengers == false) {
            return;
        }

        VisibleRegion vr = getMap().getProjection().getVisibleRegion();
        final LatLng mainCenter = vr.latLngBounds.getCenter();
        final LatLng northeast = vr.latLngBounds.northeast;
        final LatLng southwest = vr.latLngBounds.southwest;

        final double radius_map = GeneralFunctions.calculationByLocation(mainCenter.latitude, mainCenter.longitude, southwest.latitude, southwest.longitude, "KM");

        boolean isWithin1m = radius_map > this.radius_map + 0.001;

        if (isWithin1m == true)
            getNearByPassenger(String.valueOf(radius_map), mainCenter.latitude, mainCenter.longitude);

        this.radius_map = radius_map;
    }

    public void configHeatMapView(boolean isShowNearByPassengers) {
        this.isShowNearByPassengers = isShowNearByPassengers;

        if (mapOverlayList.size() > 0) {
            for (int i = 0; i < mapOverlayList.size(); i++) {
                if (mapOverlayList.get(i) != null) {

                    mapOverlayList.get(i).setVisible(isShowNearByPassengers);

                    if (isShowNearByPassengers) {

                        //handle heat map view
                        if (isfirstZoom) {
                            isfirstZoom = false;
                            getMap().moveCamera(CameraUpdateFactory.zoomTo(14f));
                        }
                    } else {
                        userLocBtnImgView.performClick();
                    }
                }

            }
        }

        //if (cameraForUserPosition() != null)
        onCameraChange(cameraForUserPosition());
        //onCameraChange((new AppFunctions(getActContext()).getCameraPosition(userLocation, gMap)));

    }

    public void onMapReady(GoogleMap googleMap) {

        (findViewById(R.id.LoadingMapProgressBar)).setVisibility(View.GONE);

        this.gMap = googleMap;

        if (generalFunc.checkLocationPermission(true)) {
            getMap().setMyLocationEnabled(false);
            //  getMap().setPadding(0, 0, 0, Utils.dipToPixels(getActContext(), 50));
            getMap().getUiSettings().setTiltGesturesEnabled(false);
            getMap().getUiSettings().setZoomControlsEnabled(false);
            getMap().getUiSettings().setCompassEnabled(false);
            getMap().getUiSettings().setMyLocationButtonEnabled(false);
        }
        getMap().setOnCameraChangeListener(this);

        getMap().setOnMarkerClickListener(marker -> {
            marker.hideInfoWindow();
            return true;
        });

        if (GetLocationUpdates.retrieveInstance() != null) {
            GetLocationUpdates.getInstance().stopLocationUpdates(this);
        }

        GetLocationUpdates.getInstance().setTripStartValue(false, false, "");
        GetLocationUpdates.getInstance().startLocationUpdates(this, this);


    }

    public GoogleMap getMap() {
        return this.gMap;
    }

    public void callgederApi(String egender) {
        HashMap<String, String> parameters = new HashMap<>();
        parameters.put("type", "updateUserGender");
        parameters.put("UserType", Utils.userType);
        parameters.put("iMemberId", generalFunc.getMemberId());
        parameters.put("eGender", egender);


        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(), parameters);
        exeWebServer.setLoaderConfig(getActContext(), true, generalFunc);
        exeWebServer.setDataResponseListener(responseString -> {

            boolean isDataAvail = GeneralFunctions.checkDataAvail(Utils.action_str, responseString);

            String message = generalFunc.getJsonValue(Utils.message_str, responseString);
            if (isDataAvail) {
                generalFunc.storeData(Utils.USER_PROFILE_JSON, message);
                obj_userProfile = generalFunc.getJsonObject(generalFunc.retrieveValue(Utils.USER_PROFILE_JSON));
                changeObj();

            }
        });
        exeWebServer.execute();
    }

    public void genderDailog() {


        final Dialog builder = new Dialog(getActContext(), R.style.Theme_Dialog);
        builder.requestWindowFeature(Window.FEATURE_NO_TITLE);
        builder.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        builder.setContentView(R.layout.gender_view);
        builder.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);

        final MTextView genderTitleTxt = (MTextView) builder.findViewById(R.id.genderTitleTxt);
        final MTextView maleTxt = (MTextView) builder.findViewById(R.id.maleTxt);
        final MTextView femaleTxt = (MTextView) builder.findViewById(R.id.femaleTxt);
        final ImageView gendercancel = (ImageView) builder.findViewById(R.id.gendercancel);
        final ImageView gendermale = (ImageView) builder.findViewById(R.id.gendermale);
        final ImageView genderfemale = (ImageView) builder.findViewById(R.id.genderfemale);
        final LinearLayout male_area = (LinearLayout) builder.findViewById(R.id.male_area);
        final LinearLayout female_area = (LinearLayout) builder.findViewById(R.id.female_area);

        genderTitleTxt.setText(generalFunc.retrieveLangLBl("Select your gender to continue", "LBL_SELECT_GENDER"));
        maleTxt.setText(generalFunc.retrieveLangLBl("Male", "LBL_MALE_TXT"));
        femaleTxt.setText(generalFunc.retrieveLangLBl("FeMale", "LBL_FEMALE_TXT"));

        gendercancel.setOnClickListener(v -> builder.dismiss());

        male_area.setOnClickListener(v -> {
            callgederApi("Male");
            builder.dismiss();

        });
        female_area.setOnClickListener(v -> {
            callgederApi("Female");
            builder.dismiss();

        });

        builder.show();

    }

    public void goOnlineOffline(final boolean isGoOnline, final boolean isMessageShown) {

        handleNoLocationDial();
        if (isGoOnline && (userLocation == null || userLocation.getLatitude() == 0.0 || userLocation.getLongitude() == 0.0)) {
            generalFunc.showGeneralMessage("", generalFunc.retrieveLangLBl("Application is not able to get your accurate location. Please try again. \n" +
                    "If you still face the problem, please try again in open sky instead of closed area.", "LBL_NO_LOC_GPS_GENERAL"));
            onlineOfflineSwitch.setChecked(false);
            onlineOfflineSwitch.setThumbColorRes(android.R.color.holo_red_dark);
            onlineOfflineSwitch.setBackColorRes(android.R.color.white);

            ufxonlineOfflineSwitch.setChecked(false);
            ufxonlineOfflineSwitch.setThumbColorRes(android.R.color.holo_red_dark);
            ufxonlineOfflineSwitch.setBackColorRes(android.R.color.white);
            setOfflineState();
            return;
        }
        isHailRideOptionEnabled();

        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "updateDriverStatus");
        parameters.put("iDriverId", generalFunc.getMemberId());

        if (isGoOnline) {
            parameters.put("Status", "Available");
            parameters.put("isUpdateOnlineDate", "true");
        } else {
            parameters.put("Status", "Not Available");
        }
        if (userLocation != null) {
            parameters.put("latitude", "" + userLocation.getLatitude());
            parameters.put("longitude", "" + userLocation.getLongitude());
        }

        parameters.put("isOnlineSwitchPressed", isOnlineOfflineSwitchCalled ? "true" : "");

        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(), parameters);
        exeWebServer.setCancelAble(false);

        if (isMessageShown) {
            exeWebServer.setLoaderConfig(getActContext(), true, generalFunc);
        }

        exeWebServer.setIsDeviceTokenGenerate(true, "vDeviceToken", generalFunc);
        exeWebServer.setDataResponseListener(responseString -> {

            if (!isMessageShown) {
                return;
            }

            if (responseString != null && !responseString.equals("")) {

                boolean isDataAvail = GeneralFunctions.checkDataAvail(Utils.action_str, responseString);
                String message = generalFunc.getJsonValue(Utils.message_str, responseString);

                if (message.equals("SESSION_OUT")) {
                    MyApp.getInstance().notifySessionTimeOut();
                    Utils.runGC();
                    return;
                }
                HashMap<String, String> storeData = new HashMap<>();
                storeData.put(Utils.DRIVER_DESTINATION_AVAILABLE_KEY, generalFunc.getJsonValue(Utils.DRIVER_DESTINATION_AVAILABLE_KEY, responseString));
                storeData.put(Utils.ENABLE_DRIVER_DESTINATIONS_KEY, generalFunc.getJsonValue(Utils.ENABLE_DRIVER_DESTINATIONS_KEY, responseString));
                generalFunc.storeData(storeData);

                if (isDataAvail) {

                    HailEnableOnDriverStatus = generalFunc.getJsonValue("Enable_Hailtrip", responseString);


                    if (isGoOnline) {

                        if (generalFunc.getJsonValue("isExistUberXServices", responseString).equalsIgnoreCase("Yes") && !generalFunc.isDeliverOnlyEnabled()) {
                            // workArea.setVisibility(View.VISIBLE);
                            //workAreaLine.setVisibility(View.VISIBLE);
                            location_action.setVisibility(View.VISIBLE);
                            location_actionRTL.setVisibility(View.VISIBLE);


                        } else {
                            workArea.setVisibility(View.GONE);
                            workAreaLine.setVisibility(View.GONE);
                            location_action.setVisibility(View.GONE);
                            location_actionRTL.setVisibility(View.GONE);


                        }

                        if (message.equals("REQUIRED_MINIMUM_BALNCE")) {
                            isHailRideOptionEnabled();

                            Bundle bn = new Bundle();
                            bn.putString("UserProfileJson", obj_userProfile.toString());
                            buildLowBalanceMessage(getActContext(), generalFunc.getJsonValue("Msg", responseString), bn);
                        }
                        setOnlineState();
                        generalFunc.showMessage(generalFunc.getCurrentView((Activity) getActContext()), generalFunc.retrieveLangLBl("", "LBL_ONLINE_HEADER_TXT"));


                    } else {
                        workArea.setVisibility(View.GONE);
                        workAreaLine.setVisibility(View.GONE);
                        location_action.setVisibility(View.GONE);
                        location_actionRTL.setVisibility(View.GONE);
                        setOfflineState();
                        generalFunc.showMessage(generalFunc.getCurrentView((Activity) getActContext()), generalFunc.retrieveLangLBl("", "LBL_OFFLINE_HEADER_TXT"));

                    }

                    if (generalFunc.getJsonValue("UberX_message", responseString) != null && !generalFunc.getJsonValue("UberX_message", responseString).equalsIgnoreCase("")) {
                        generalFunc.showGeneralMessage("", generalFunc.retrieveLangLBl("", generalFunc.getJsonValue("UberX_message", responseString)));
                    }
                } else {

                    if (generalFunc.getJsonValue("Enable_Hailtrip", responseString) != null & !generalFunc.getJsonValue("Enable_Hailtrip", responseString).equalsIgnoreCase("")) {

                        HailEnableOnDriverStatus = generalFunc.getJsonValue("Enable_Hailtrip", responseString);
                    }

                    Logger.d("SUBSCRIPTION", "1");

                    isOnlineAvoid = true;
                    if (app_type.equals(Utils.CabGeneralType_UberX)) {

                        if (isGoOnline) {
                            ufxonlineOfflineSwitch.setChecked(false);
                        } else {
                            ufxonlineOfflineSwitch.setChecked(true);
                        }

                    } else {
                        if (isGoOnline) {
                            onlineOfflineSwitch.setChecked(false);
                        } else {
                            onlineOfflineSwitch.setChecked(true);
                        }
                    }


                    Bundle bn = new Bundle();
                    bn.putString("msg", "" + message);
                    String eStatus = generalFunc.getJsonValueStr("eStatus", obj_userProfile);

                    if (!eStatus.equalsIgnoreCase("inactive")) {
                        Logger.d("SUBSCRIPTION", "2");

                        if (message.equals("DO_EMAIL_PHONE_VERIFY") || message.equals("DO_PHONE_VERIFY") || message.equals("DO_EMAIL_VERIFY")) {
                            accountVerificationAlert(generalFunc.retrieveLangLBl("", "LBL_ACCOUNT_VERIFY_ALERT_TXT"), bn);
                            return;
                        }
                    }

                    if (message.equalsIgnoreCase("LBL_DRIVER_DOC_EXPIRED")) {
                        generalFunc.showGeneralMessage("",
                                generalFunc.retrieveLangLBl(generalFunc.getJsonValue(Utils.message_str, responseString),
                                        generalFunc.getJsonValue(Utils.message_str, responseString)));

                        goOnlineOffline(false, false);

                        return;
                    }

                    Logger.d("SUBSCRIPTION", "4");

                    if (isGoOnline && !message.equalsIgnoreCase("PENDING_SUBSCRIPTION")) {
                        isHailRideOptionEnabled();
                    } else {
                        //menuMultipleActions.setVisibility(View.GONE);

                        return_action.setVisibility(View.GONE);
                        return_actionRTL.setVisibility(View.GONE);
                    }

                    if (Utils.checkText(message) && message.equals("PENDING_SUBSCRIPTION") && isGoOnline) {
                        Logger.d("SUBSCRIPTION", "3" + isGoOnline);
                        showSubscriptionStatusDialog(false, message);
                        return;
                    }

                    if (Utils.checkText(message) && message.equals("REQUIRED_MINIMUM_BALNCE") && isGoOnline) {

                        isHailRideOptionEnabled();
                        bn.putString("UserProfileJson", obj_userProfile.toString());

                        buildLowBalanceMessage(getActContext(), generalFunc.getJsonValue("Msg", responseString), bn);
                        return;
                    }


                    if (Utils.checkText(message) && !message.equals("PENDING_SUBSCRIPTION")) {

                        if (message.equalsIgnoreCase("LBL_INACTIVE_CARS_MESSAGE_TXT")) {
                            Logger.d("SUBSCRIPTION", "5");
                            hail_action.setVisibility(View.GONE);
                            hail_actionRTL.setVisibility(View.GONE);

                            return_action.setVisibility(View.GONE);
                            return_actionRTL.setVisibility(View.GONE);
                            GenerateAlertBox alertBox = new GenerateAlertBox(getActContext());
                            alertBox.setContentMessage("", generalFunc.retrieveLangLBl("", message));
                            alertBox.setPositiveBtn(generalFunc.retrieveLangLBl("", "LBL_BTN_OK_TXT"));
                            alertBox.setNegativeBtn(generalFunc.retrieveLangLBl("", "LBL_CONTACT_US_TXT"));
                            alertBox.setBtnClickList(btn_id -> {

                                alertBox.closeAlertBox();
                                if (btn_id == 0) {
                                    new StartActProcess(getActContext()).startAct(ContactUsActivity.class);
                                }
                            });
                            alertBox.showAlertBox();
                        } else {
                            if (generalFunc.getJsonValue("isShowContactUs", responseString) != null && generalFunc.getJsonValue("isShowContactUs", responseString).equalsIgnoreCase("Yes")) {
                                Logger.d("SUBSCRIPTION", "6");
                                final GenerateAlertBox generateAlert = new GenerateAlertBox(getActContext());
                                generateAlert.setCancelable(false);
                                generateAlert.setBtnClickList(btn_id -> {
                                    if (btn_id == 0) {


                                    } else if (btn_id == 1) {
                                        Intent intent = new Intent(MainActivity.this, ContactUsActivity.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        startActivity(intent);
                                        // finish();

                                    }
                                });

                                generateAlert.setContentMessage("", generalFunc.retrieveLangLBl("", generalFunc.getJsonValue(Utils.message_str, responseString)));
                                generateAlert.setNegativeBtn(generalFunc.retrieveLangLBl("", "LBL_BTN_OK_TXT"));
                                generateAlert.setPositiveBtn(generalFunc.retrieveLangLBl("", "LBL_CONTACT_US_TXT"));

                                generateAlert.showAlertBox();


                            } else {

                                if (message.equalsIgnoreCase("LBL_PENDING_MIXSUBSCRIPTION")) {
                                    hail_action.setVisibility(View.GONE);
                                    hail_actionRTL.setVisibility(View.GONE);

                                    return_action.setVisibility(View.GONE);
                                    return_actionRTL.setVisibility(View.GONE);

                                    showSubscriptionStatusDialog(false, message);
                                } else {
                                    Logger.d("SUBSCRIPTION", "7");
                                    generalFunc.showGeneralMessage("",
                                            generalFunc.retrieveLangLBl(generalFunc.getJsonValue(Utils.message_str, responseString),
                                                    generalFunc.getJsonValue(Utils.message_str, responseString)));
                                }
                            }
                        }
                    }
                }
            } else {

                if (intCheck.isNetworkConnected()) {
                    isOnlineAvoid = true;

                    if (app_type.equals(Utils.CabGeneralType_UberX)) {

                        if (isGoOnline == true) {
                            ufxonlineOfflineSwitch.setChecked(false);
                        } else {
                            ufxonlineOfflineSwitch.setChecked(true);
                        }

                    } else {
                        if (isGoOnline == true) {
                            onlineOfflineSwitch.setChecked(false);
                        } else {
                            onlineOfflineSwitch.setChecked(true);
                        }
                    }
                }
                Logger.d("SUBSCRIPTION", "8");

                generalFunc.showError();

            }
        });
        exeWebServer.execute();
    }

    public void showSubscriptionStatusDialog(boolean checkOnlineAvailability, String message) {

        String messageStr = message.equalsIgnoreCase("LBL_PENDING_MIXSUBSCRIPTION") ? message : "LBL_SUBSCRIPTION_REQ_SH_LBL";

        if (checkOnlineAvailability) {
            if (isDriverOnline) {

                setOfflineState();
                isOnlineAvoid = true;
                if (app_type.equals(Utils.CabGeneralType_UberX)) {
                    ufxonlineOfflineSwitch.setChecked(false);
                    ufxonlineOfflineSwitch.setThumbColorRes(android.R.color.holo_red_dark);
                    ufxonlineOfflineSwitch.setBackColorRes(android.R.color.white);


                } else {
                    onlineOfflineSwitch.setChecked(false);
                    onlineOfflineSwitch.setThumbColorRes(android.R.color.holo_red_dark);
                    onlineOfflineSwitch.setBackColorRes(android.R.color.white);


                }

            } else {
               // return;
            }
        }

        if (customDialog != null) {
            customDialog.closeDialog();
        }


        customDialog = new CustomDialog(getActContext());
        customDialog.setDetails(generalFunc.retrieveLangLBl("", "LBL_SUBSCRIPTION_REQ_H_LBL"), generalFunc.retrieveLangLBl("", messageStr), generalFunc.retrieveLangLBl("", "LBL_SUBSCRIBE"), generalFunc.retrieveLangLBl("", "LBL_CANCEL_TXT"), false, R.mipmap.ic_menu_subscription, true, 1);
        customDialog.setRoundedViewBackgroundColor(R.color.white);
        customDialog.setIconTintColor(R.color.appThemeColor_1);
        customDialog.setBtnRadius(10);
        customDialog.setTitleTxtColor(R.color.appThemeColor_1);
        customDialog.setPositiveBtnBackColor(R.color.appThemeColor_1_Light);
        customDialog.setNegativeBtnBackColor(R.color.appThemeColor_1_Light);
        customDialog.createDialog();
        customDialog.setPositiveButtonClick(new Closure() {
            @Override
            public void exec() {
                new StartActProcess(getActContext()).startAct(SubscriptionActivity.class);
            }
        });
        customDialog.setNegativeButtonClick(new Closure() {
            @Override
            public void exec() {

            }
        });
        customDialog.show();
    }

    public void setOfflineState() {
        isDriverOnline = false;
        if (app_type.equals(Utils.CabGeneralType_UberX)) {
            ufxonlineOfflineTxtView.setText(LBL_GO_ONLINE_TXT);
            ufxTitleonlineOfflineTxtView.setText(LBL_OFFLINE);
        } else {
            onlineOfflineTxtView.setText(LBL_GO_ONLINE_TXT);

        }

        hail_action.setVisibility(View.GONE);
        hail_actionRTL.setVisibility(View.GONE);

        return_action.setVisibility(View.GONE);
        return_actionRTL.setVisibility(View.GONE);
        removeEODTripData(false);
        stopService(startUpdatingStatus);

        generalFunc.storeData(Utils.DRIVER_ONLINE_KEY, "false");

        ConfigPubNub.getInstance().unSubscribeToCabRequestChannel();

        NotificationScheduler.cancelReminder(MyApp.getInstance().getCurrentAct(), AlarmReceiver.class);
    }

    public void setOnlineState() {

        isHailRideOptionEnabled();
        isDriverOnline = true;
        if (app_type.equals(Utils.CabGeneralType_UberX)) {
            ufxonlineOfflineTxtView.setText(LBL_GO_OFFLINE_TXT);
            ufxTitleonlineOfflineTxtView.setText(LBL_ONLINE);

        } else {
            onlineOfflineTxtView.setText(LBL_GO_OFFLINE_TXT);
        }


        if (!generalFunc.isServiceRunning(UpdateDriverStatus.class)) {
            startService(startUpdatingStatus);
        }

        generalFunc.storeData(Utils.DRIVER_ONLINE_KEY, "true");

        updateLocationToPubNub();

        ConfigPubNub.getInstance().subscribeToCabRequestChannel();

    }

    public void accountVerificationAlert(String message, final Bundle bn) {
        final GenerateAlertBox generateAlert = new GenerateAlertBox(getActContext());
        generateAlert.setCancelable(false);
        generateAlert.setBtnClickList(btn_id -> {
            if (btn_id == 1) {
                generateAlert.closeAlertBox();
//                bn.putString("msg", "DO_PHONE_VERIFY");
                (new StartActProcess(getActContext())).startActForResult(VerifyInfoActivity.class, bn, Utils.VERIFY_INFO_REQ_CODE);
            } else if (btn_id == 0) {
                generateAlert.closeAlertBox();
            }
        });
        generateAlert.setContentMessage("", message);
        generateAlert.setPositiveBtn(generalFunc.retrieveLangLBl("", "LBL_BTN_OK_TXT"));
        generateAlert.setNegativeBtn(generalFunc.retrieveLangLBl("", "LBL_BTN_CANCEL_TRIP_TXT"));
        generateAlert.showAlertBox();
    }

    public void updateLocationToPubNub() {
        if (isDriverOnline == true && userLocation != null && userLocation.getLongitude() != 0.0 && userLocation.getLatitude() != 0.0) {
            if (lastPublishedLoc != null) {

                if (userLocation.distanceTo(lastPublishedLoc) < PUBSUB_PUBLISH_DRIVER_LOC_DISTANCE_LIMIT) {
                    return;
                } else {
                    lastPublishedLoc = userLocation;
                }

            } else {
                lastPublishedLoc = userLocation;
            }


            ConfigPubNub.getInstance().publishMsg(generalFunc.getLocationUpdateChannel(), generalFunc.buildLocationJson(userLocation));
        }
    }

    public void getNearByPassenger(String radius, double center_lat, double center_long) {

        if (heatMapAsyncTask != null) {
            heatMapAsyncTask.cancel(true);
            heatMapAsyncTask = null;
        }

        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "loadPassengersLocation");
        parameters.put("Radius", radius);
        parameters.put("Latitude", String.valueOf(center_lat));
        parameters.put("Longitude", String.valueOf(center_long));
        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(), parameters);
        this.heatMapAsyncTask = exeWebServer;

        exeWebServer.setDataResponseListener(responseString -> {

            if (responseString != null && !responseString.equals("")) {

                boolean isDataAvail = GeneralFunctions.checkDataAvail(Utils.action_str, responseString);

                if (isDataAvail) {
                    JSONArray dataLocArr = generalFunc.getJsonArray(Utils.message_str, responseString);

                    ArrayList<LatLng> listTemp = new ArrayList<LatLng>();
                    ArrayList<LatLng> Online_listTemp = new ArrayList<LatLng>();
                    for (int i = 0; i < dataLocArr.length(); i++) {
                        JSONObject obj_temp = generalFunc.getJsonObject(dataLocArr, i);

                        String type = generalFunc.getJsonValueStr("Type", obj_temp);

                        double lat = GeneralFunctions.parseDoubleValue(0.0, generalFunc.getJsonValueStr("Latitude", obj_temp));
                        double longi = GeneralFunctions.parseDoubleValue(0.0, generalFunc.getJsonValueStr("Longitude", obj_temp));


                        if (type.equalsIgnoreCase("Online")) {

                            String iUserId = generalFunc.getJsonValueStr("iUserId", obj_temp);

                            if (!onlinePassengerLocList.containsKey("ID_" + type + "_" + iUserId)) {
                                onlinePassengerLocList.put("ID_" + type + "_" + iUserId, "True");

                                Online_listTemp.add(new LatLng(lat, longi));
                            }


                        } else {
                            String iTripId = generalFunc.getJsonValueStr("iTripId", obj_temp);
                            if (!historyLocList.containsKey("ID_" + type + "_" + iTripId)) {
                                historyLocList.put("ID_" + type + "_" + iTripId, "True");

                                listTemp.add(new LatLng(lat, longi));
                            }
                        }
                    }

                    if (listTemp.size() > 0) {
                        mapOverlayList.add(getMap().addTileOverlay(new TileOverlayOptions().tileProvider(
                                new HeatmapTileProvider.Builder().gradient(new Gradient(new int[]{Color.rgb(153, 0, 0), Color.WHITE}, new float[]{0.2f, 1.5f})).data(listTemp).build())));
                    }
                    if (Online_listTemp.size() > 0) {
                        mapOverlayList.add(getMap().addTileOverlay(new TileOverlayOptions().tileProvider(
                                new HeatmapTileProvider.Builder().gradient(new Gradient(new int[]{Color.rgb(0, 51, 0), Color.WHITE}, new float[]{0.2f, 1.5f}, 1000)).data(Online_listTemp).build())));
                    }
                    if (!isShowNearByPassengers) {

                        configHeatMapView(false);
                    } else {
                        configHeatMapView(true);
                    }
                }
            } else {
                generalFunc.showError();
            }
        });
        exeWebServer.execute();
    }

    public void configCarList(final boolean isCarUpdate, final String selectedCarId,
                              final int position) {
        final HashMap<String, String> parameters = new HashMap<String, String>();
        if (!isCarUpdate) {
            parameters.put("type", "LoadAvailableCars");
        } else {
            parameters.put("type", "SetDriverCarID");
            parameters.put("iDriverVehicleId", selectedCarId);
        }
        parameters.put("iDriverId", generalFunc.getMemberId());

        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(), parameters);
        exeWebServer.setLoaderConfig(getActContext(), true, generalFunc);
        exeWebServer.setDataResponseListener(responseString -> {

            if (responseString != null && !responseString.equals("")) {

                boolean isDataAvail = GeneralFunctions.checkDataAvail(Utils.action_str, responseString);

                if (isDataAvail) {

                    if (!isCarUpdate) {
                        LoadCarList(generalFunc.getJsonArray(Utils.message_str, responseString));
                    } else {

                        String vLicencePlateNo = generalFunc.getJsonValue("vLicencePlate", items_txt_car_json.get(position));
                        carNumPlateTxt.setText(vLicencePlateNo);
                        carNumPlateTxt.setVisibility(View.VISIBLE);

                        if (items_isHail_json.get(position).equalsIgnoreCase("Yes")) {
                            if (isDriverOnline) {
                                HailEnableOnDriverStatus = "Yes";
                                isHailRideOptionEnabled();
                            } else {
                                hail_action.setVisibility(View.GONE);
                                hail_actionRTL.setVisibility(View.GONE);
                            }

                        } else {
                            HailEnableOnDriverStatus = "No";
                            hail_action.setVisibility(View.GONE);
                            hail_actionRTL.setVisibility(View.GONE);
                        }
                        if (isDriverOnline) {
                            enableEOD();
                        } else {

                            return_action.setVisibility(View.GONE);
                            return_actionRTL.setVisibility(View.GONE);
                        }

                        String vMake = generalFunc.getJsonValue("vMake", items_txt_car_json.get(position));
                        String vModel = generalFunc.getJsonValue("vTitle", items_txt_car_json.get(position));

                        carNameTxt.setText(vMake + " " + vModel);
                        selectedcar = selectedCarId;
                        carNameTxt.setVisibility(View.VISIBLE);
                        carImage.setVisibility(View.VISIBLE);
                        changeCarTxt.setText(generalFunc.retrieveLangLBl("", "LBL_CHANGE"));

                        generalFunc.showMessage(generalFunc.getCurrentView(MainActivity.this), generalFunc.retrieveLangLBl("", "LBL_INFO_UPDATED_TXT"));
                    }

                } else {
                    String msg = generalFunc.getJsonValue(Utils.message_str, responseString);
                    if (msg.equalsIgnoreCase("LBL_INACTIVE_CARS_MESSAGE_TXT")) {
                        GenerateAlertBox alertBox = new GenerateAlertBox(getActContext());
                        alertBox.setContentMessage("", generalFunc.retrieveLangLBl("", msg));
                        alertBox.setPositiveBtn(generalFunc.retrieveLangLBl("", "LBL_BTN_OK_TXT"));
                        alertBox.setNegativeBtn(generalFunc.retrieveLangLBl("", "LBL_CONTACT_US_TXT"));
                        alertBox.setBtnClickList(btn_id -> {

                            alertBox.closeAlertBox();
                            if (btn_id == 0) {
                                new StartActProcess(getActContext()).startAct(ContactUsActivity.class);
                            }
                        });
                        alertBox.showAlertBox();
                    } else {

                        if ((msg.equalsIgnoreCase("PENDING_SUBSCRIPTION") || msg.equalsIgnoreCase("LBL_PENDING_MIXSUBSCRIPTION"))) {

                            showSubscriptionStatusDialog(true, msg);
                        } else {
                            generalFunc.showGeneralMessage("",
                                    generalFunc.retrieveLangLBl("", msg));
                        }
                    }
                }
            } else {
                generalFunc.showError();
            }
        });
        exeWebServer.execute();
    }


    public void LoadCarList(JSONArray array) {

        items_txt_car.clear();
        items_car_id.clear();
        items_txt_car_json.clear();
        items_isHail_json.clear();
        final ArrayList list = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            JSONObject obj_temp = generalFunc.getJsonObject(array, i);

            items_txt_car.add(generalFunc.getJsonValue("vMake", obj_temp) + " " + generalFunc.getJsonValue("vTitle", obj_temp));

            items_car_id.add(generalFunc.getJsonValueStr("iDriverVehicleId", obj_temp));
            items_txt_car_json.add(obj_temp.toString());
            items_isHail_json.add(generalFunc.getJsonValueStr("Enable_Hailtrip", obj_temp));

            HashMap<String, String> map = new HashMap<String, String>();
            map.put("car", items_txt_car.get(i).toString());
            map.put("iDriverVehicleId", items_car_id.get(i).toString());
            map.put("vLicencePlate", generalFunc.getJsonValueStr("vLicencePlate", obj_temp));
            list.add(map);
        }

        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getActContext());

        LayoutInflater inflater = (LayoutInflater) getActContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View dialogView = inflater.inflate(R.layout.dialog_selectcar_view, null);

        final MTextView vehTitleTxt = (MTextView) dialogView.findViewById(R.id.VehiclesTitleTxt);
        final MTextView mangeVehiclesTxt = (MTextView) dialogView.findViewById(R.id.mangeVehiclesTxt);
        final MTextView addVehiclesTxt = (MTextView) dialogView.findViewById(R.id.addVehiclesTxt);
        final ImageView cancel = (ImageView) dialogView.findViewById(R.id.cancel);
        final RecyclerView vehiclesRecyclerView = (RecyclerView) dialogView.findViewById(R.id.vehiclesRecyclerView);

        cancel.setOnClickListener(v -> list_car.dismiss());

//        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(vehiclesRecyclerView.getContext(),
//                DividerItemDecoration.VERTICAL_LIST);
//        vehiclesRecyclerView.addItemDecoration(dividerItemDecoration);

        builder.setView(dialogView);
        vehTitleTxt.setText(generalFunc.retrieveLangLBl("Select Your Vehicles", "LBL_SELECT_CAR_TXT"));
        if (!app_type.equalsIgnoreCase(Utils.CabGeneralTypeRide_Delivery_UberX)) {
            mangeVehiclesTxt.setText(generalFunc.retrieveLangLBl("", "LBL_MANAGE"));
        } else {
            mangeVehiclesTxt.setText(generalFunc.retrieveLangLBl("", "LBL_MANAGE"));
        }
        addVehiclesTxt.setText(generalFunc.retrieveLangLBl("ADD NEW", "LBL_ADD_VEHICLES"));

        ManageVehicleListAdapter adapter = new ManageVehicleListAdapter(getActContext(), list, generalFunc, selectedcar);
        vehiclesRecyclerView.setAdapter(adapter);
        adapter.setOnItemClickList(this);

        mangeVehiclesTxt.setOnClickListener(v -> {
            list_car.dismiss();

//            Bundle bn = new Bundle();
//            bn.putString("app_type", app_type);
//            bn.putString("iDriverVehicleId", generalFunc.getJsonValueStr("iDriverVehicleId", obj_userProfile));
//
//            if (app_type.equalsIgnoreCase(Utils.CabGeneralTypeRide_Delivery_UberX)) {
//                bn.putString("selView", "vehicle");
//                bn.putString("apptype", app_type);
//                bn.putInt("totalVehicles", 1);
//                bn.putString("UBERX_PARENT_CAT_ID", generalFunc.getJsonValueStr("UBERX_PARENT_CAT_ID", obj_userProfile));
//                new StartActProcess(getActContext()).startActWithData(UploadDocTypeWiseActivity.class, bn);
//            } else {
//                new StartActProcess(getActContext()).startActWithData(ManageVehiclesActivity.class, bn);
//            }

//
            Bundle bn = new Bundle();
            bn.putString("app_type", app_type);
            bn.putString("iDriverVehicleId", generalFunc.getJsonValueStr("iDriverVehicleId", obj_userProfile));

            new StartActProcess(getActContext()).startActWithData(ManageVehiclesActivity.class, bn);
        });

        addVehiclesTxt.setOnClickListener(v -> {
            list_car.dismiss();
            Bundle bn = new Bundle();
            bn.putString("app_type", app_type);
            new StartActProcess(getActContext()).startActWithData(AddVehicleActivity.class, bn);
        });

//        builder.setNegativeButton(generalFunc.retrieveLangLBl("", "LBL_MANAGE_VEHICLES"), (dialog, which) -> {
//            dialog.cancel();
//
//            Bundle bn = new Bundle();
//            bn.putString("app_type", app_type);
//            bn.putString("iDriverVehicleId", generalFunc.getJsonValueStr("iDriverVehicleId", obj_userProfile));
//
//            new StartActProcess(getActContext()).startActWithData(ManageVehiclesActivity.class, bn);
//
//        });
//
//        builder.setPositiveButton(generalFunc.retrieveLangLBl("ADD NEW", "LBL_ADD_VEHICLES"), (dialog, which) -> {
//
//            dialog.cancel();
//            Bundle bn = new Bundle();
//            bn.putString("app_type", app_type);
//            (new StartActProcess(getActContext())).startActWithData(AddVehicleActivity.class, bn);
//        });


        list_car = builder.create();
        if (generalFunc.isRTLmode()) {
            generalFunc.forceRTLIfSupported(list_car);
        }
        list_car.getWindow().setBackgroundDrawable(getActContext().getResources().getDrawable(R.drawable.all_roundcurve_card));
        list_car.show();

        list_car.setCancelable(false);
        final Button positiveButton = list_car.getButton(AlertDialog.BUTTON_POSITIVE);
        positiveButton.setTextColor(getResources().getColor(R.color.appThemeColor_1));
        final Button negativeButton = list_car.getButton(AlertDialog.BUTTON_NEGATIVE);
        negativeButton.setTextColor(getResources().getColor(R.color.black));
        list_car.setOnCancelListener(dialogInterface -> Utils.hideKeyboard(getActContext()));
    }

    @Override
    public void onLocationUpdate(Location location) {

        try {
            if (location == null) {
                return;
            }

            if (isShowNearByPassengers) {
                return;
            }
            if (generalFunc.checkLocationPermission(true) && getMap() != null && !getMap().isMyLocationEnabled()) {
                getMap().setMyLocationEnabled(true);
            }

            this.userLocation = location;

            if (updateDirections != null) {
                updateDirections.changeUserLocation(location);
            }

            // CameraPosition cameraPosition = cameraForUserPosition();
            CameraUpdate cameraPosition = (new AppFunctions(getActContext()).getCameraPosition(userLocation, gMap));

            if (cameraPosition != null)
                getMap().moveCamera(cameraPosition);

            if (!isFirstAddressLoaded) {
                getAddressFromLocation.setLocation(userLocation.getLatitude(), userLocation.getLongitude());
                getAddressFromLocation.execute();
                isFirstAddressLoaded = true;
            }

            if (isFirstLocation &&
                    generalFunc.getJsonValueStr("ePhoneVerified", obj_userProfile).equalsIgnoreCase("YES")) {

                isFirstLocation = false;

                String isGoOnline = generalFunc.retrieveValue(Utils.GO_ONLINE_KEY);

                if ((isGoOnline != null && !isGoOnline.equals("") && isGoOnline.equals("Yes"))) {
                    long lastTripTime = GeneralFunctions.parseLongValue(0, generalFunc.retrieveValue(Utils.LAST_FINISH_TRIP_TIME_KEY));
                    long currentTime = Calendar.getInstance().getTimeInMillis();

                    if ((currentTime - lastTripTime) < 25000) {
                        if (generalFunc.isLocationEnabled()) {
                            isOnlineOfflineSwitchCalled = true;
                            if (app_type.equals(Utils.CabGeneralType_UberX)) {
                                ufxonlineOfflineSwitch.setChecked(true);
                            } else {
                                onlineOfflineSwitch.setChecked(true);
                            }
                        }
                    }
                    HashMap<String, String> storeData = new HashMap<>();
                    storeData.put(Utils.GO_ONLINE_KEY, "No");
                    storeData.put(Utils.LAST_FINISH_TRIP_TIME_KEY, "0");
                    generalFunc.storeData(storeData);

                }

                if (generalFunc.isLocationEnabled() && generalFunc.getJsonValueStr("vAvailability", obj_userProfile).equals("Available") && !isDriverOnline) {
                    isOnlineOfflineSwitchCalled = true;
                    if (app_type.equals(Utils.CabGeneralType_UberX)) {
                        ufxonlineOfflineSwitch.setChecked(true);
                    } else {
                        onlineOfflineSwitch.setChecked(true);
                    }
                }
            }
        } catch (Exception e) {

        }
    }

    public void pubNubStatus(PNStatusCategory status) {

    }

    public CameraPosition cameraForUserPosition() {
        double currentZoomLevel = getMap().getCameraPosition().zoom;

        // if (Utils.defaultZomLevel > currentZoomLevel) {
        currentZoomLevel = Utils.defaultZomLevel;
        //}
        if (userLocation != null) {
            CameraPosition cameraPosition = new CameraPosition.Builder().target(new LatLng(this.userLocation.getLatitude(), this.userLocation.getLongitude())).bearing(getMap().getCameraPosition().bearing)
                    .zoom((float) currentZoomLevel).build();

            return cameraPosition;
        } else {
            return null;
        }
    }

    public void openMenuProfile() {
        Bundle bn = new Bundle();
        // bn.putString("UserProfileJson", userProfileJson);
        bn.putBoolean("isDriverOnline", isDriverOnline);
        new StartActProcess(getActContext()).startActForResult(MyProfileActivity.class, bn, Utils.MY_PROFILE_REQ_CODE);
    }


    public Context getActContext() {
        return MainActivity.this;
    }

    public void checkIsDriverOnline() {
        if (isDriverOnline) {
            stopService(startUpdatingStatus);

            for (int i = 0; i < 1000; i++) {
            }
        }
    }


    /*public void registerBackgroundAppReceiver() {

        unRegisterBackgroundAppReceiver();

        IntentFilter filter = new IntentFilter();
        filter.addAction(Utils.BACKGROUND_APP_RECEIVER_INTENT_ACTION);

        registerReceiver(bgAppReceiver, filter);
    }

    public void unRegisterBackgroundAppReceiver() {
        if (bgAppReceiver != null) {
            try {
                unregisterReceiver(bgAppReceiver);
            } catch (Exception e) {

            }
        }
    }*/

    public void getWalletBalDetails() {

        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "GetMemberWalletBalance");
        parameters.put("iUserId", generalFunc.getMemberId());
        parameters.put("UserType", Utils.app_type);

        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(), parameters);
        exeWebServer.setLoaderConfig(getActContext(), false, generalFunc);
        exeWebServer.setDataResponseListener(responseString -> {

            if (responseString != null && !responseString.equals("")) {

                boolean isDataAvail = GeneralFunctions.checkDataAvail(Utils.action_str, responseString);

                if (isDataAvail) {
                    try {
                        JSONObject userProfileObj = generalFunc.getJsonObject(generalFunc.retrieveValue(Utils.USER_PROFILE_JSON));
                        userProfileObj.put("user_available_balance", generalFunc.getJsonValue("MemberBalance", responseString));
                        generalFunc.storeData(Utils.USER_PROFILE_JSON, userProfileObj.toString());

                        obj_userProfile = generalFunc.getJsonObject(generalFunc.retrieveValue(Utils.USER_PROFILE_JSON));
                        changeObj();
                    } catch (Exception e) {

                    }
                }
            }
        });
        exeWebServer.execute();
    }

    @Override
    protected void onResume() {
        super.onResume();
        isCarChangeTxt = false;
        getWalletBalDetails();
        obj_userProfile = generalFunc.getJsonObject(generalFunc.retrieveValue(Utils.USER_PROFILE_JSON));
        changeObj();

        handleWorkAddress();

        if (isDriverOnline) {
            isHailRideOptionEnabled();
        }

        obj_userProfile = generalFunc.getJsonObject(generalFunc.retrieveValue(Utils.USER_PROFILE_JSON));
        changeObj();

        if (app_type.equals(Utils.CabGeneralType_UberX) || app_type.equalsIgnoreCase(Utils.CabGeneralTypeRide_Delivery_UberX)) {
            getUserstatus();
        }

        setUserInfo();
        if (iswallet) {
            obj_userProfile = generalFunc.getJsonObject(generalFunc.retrieveValue(Utils.USER_PROFILE_JSON));
            changeObj();
            iswallet = false;
        }

        if (generalFunc.retrieveValue(Utils.DRIVER_ONLINE_KEY).equals("false") && isDriverOnline) {
            setOfflineState();
            isOnlineAvoid = true;
            if (app_type.equals(Utils.CabGeneralType_UberX)) {
                ufxonlineOfflineSwitch.setChecked(false);
            } else {
                onlineOfflineSwitch.setChecked(false);
            }

        }

        if (myWalletFragment != null && iswalletFragemnt) {
           // myWalletFragment.onResume();
        }

        if (myBookingFragment != null && isbookingFragemnt) {
            Logger.d("HistoryFragment", ":: mainActonResume called");
            myBookingFragment.onResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

//        if (getLastLocation != null) {
//            getLastLocation.stopLocationUpdates();
//        }
    }

    public MyApp getApp() {
        return ((MyApp) getApplication());
    }

    public void configBackground() {

        if (!isCurrentReqHandled) {
            generalFunc.removeValue(Utils.DRIVER_ACTIVE_REQ_MSG_KEY);
            return;
        }

        if (!getApp().isMyAppInBackGround()) {
            if (!getApp().isMyAppInBackGround() && isDriverOnline) {
                setOnlineState();
            }

            if (generalFunc.containsKey(Utils.DRIVER_ACTIVE_REQ_MSG_KEY)) {

                String msg = generalFunc.retrieveValue(Utils.DRIVER_ACTIVE_REQ_MSG_KEY);

                generalFunc.removeValue(Utils.DRIVER_ACTIVE_REQ_MSG_KEY);

                generalFunc.storeData(Utils.DRIVER_CURRENT_REQ_OPEN_KEY, "true");

                (new FireTripStatusMsg(getActContext(), "PubSub")).fireTripMsg(msg);

            }
        }
    }


    public void removeLocationUpdates() {

        if (GetLocationUpdates.retrieveInstance() != null) {
            GetLocationUpdates.getInstance().stopLocationUpdates(this);
        }

        this.userLocation = null;
    }

    @Override
    protected void onDestroy() {
        try {
            checkIsDriverOnline();
            removeLocationUpdates();
            //unRegisterBackgroundAppReceiver();

            if (getAddressFromLocation != null) {
                getAddressFromLocation.setAddressList(null);
                getAddressFromLocation = null;
            }

            if (gMap != null) {
                this.gMap.setOnCameraChangeListener(null);
                this.gMap = null;
            }

            if (heatMapAsyncTask != null) {
                heatMapAsyncTask.cancel(true);
                heatMapAsyncTask = null;
            }

            if (updateRequest != null) {
                updateRequest.stopRepeatingTask();
                updateRequest = null;
            }

            Utils.runGC();
        } catch (Exception e) {

        }
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {


        if (faredialog != null && faredialog.isShowing()) {
            return;
        }


        final GenerateAlertBox generateAlert = new GenerateAlertBox(getActContext());
        generateAlert.setCancelable(false);
        generateAlert.setBtnClickList(btn_id -> {
            if (btn_id == 0) {
                generateAlert.closeAlertBox();
            } else {
                generateAlert.closeAlertBox();
                MyApp.getInstance().onTerminate();
                MainActivity.super.onBackPressed();
            }
        });

        generateAlert.setContentMessage(generalFunc.retrieveLangLBl("Exit App", "LBL_EXIT_APP_TITLE_TXT"), generalFunc.retrieveLangLBl("Are you sure you want to exit?", "LBL_WANT_EXIT_APP_TXT"));
        generateAlert.setPositiveBtn(generalFunc.retrieveLangLBl("", "LBL_YES"));
        generateAlert.setNegativeBtn(generalFunc.retrieveLangLBl("", "LBL_NO"));
        generateAlert.showAlertBox();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Utils.MY_PROFILE_REQ_CODE && resultCode == RESULT_OK && data != null) {
            // String userProfileJson = data.getStringExtra("UserProfileJson");
            obj_userProfile = generalFunc.getJsonObject(generalFunc.retrieveValue(Utils.USER_PROFILE_JSON));
            changeObj();

            setUserInfo();

        } else if (requestCode == Utils.VERIFY_INFO_REQ_CODE && resultCode == RESULT_OK && data != null) {
            String msgType = data.getStringExtra("MSG_TYPE");
            if (msgType.equalsIgnoreCase("EDIT_PROFILE")) {
                //openMenuProfile();

            }

            obj_userProfile = generalFunc.getJsonObject(generalFunc.retrieveValue(Utils.USER_PROFILE_JSON));
            changeObj();
        } else if (requestCode == Utils.VERIFY_INFO_REQ_CODE) {

            obj_userProfile = generalFunc.getJsonObject(generalFunc.retrieveValue(Utils.USER_PROFILE_JSON));
            changeObj();

            //buildMenu();
        } else if (requestCode == Utils.CARD_PAYMENT_REQ_CODE && resultCode == RESULT_OK && data != null) {

            obj_userProfile = generalFunc.getJsonObject(generalFunc.retrieveValue(Utils.USER_PROFILE_JSON));
            changeObj();

        } else if (requestCode == Utils.REQUEST_CODE_GPS_ON) {
            handleNoLocationDial();
        } else if (requestCode == Utils.REQUEST_CODE_NETWOEK_ON) {
            handleNoNetworkDial();
        } else if (resultCode == RESULT_OK && data != null && data.hasExtra("isMoneyAddedOrTransferred")) {

            if (isDriverOnline) {
                if (app_type.equals(Utils.CabGeneralType_UberX)) {
                    ufxonlineOfflineSwitch.setChecked(true);

                } else {
                    onlineOfflineSwitch.setChecked(true);
                }
            }
        }
        /*EndOfTheDay view click event*/
        else if (requestCode == Utils.SEARCH_PICKUP_LOC_REQ_CODE && resultCode == RESULT_OK && data != null && gMap != null) {
            drawRoute(data);

        } else if (resultCode == RESULT_OK && requestCode == SEL_CARD) {

            if (myWalletFragment != null) {
                myWalletFragment.onActivityResult(requestCode, resultCode, data);
            }


        } else if (resultCode == RESULT_OK && requestCode == TRANSFER_MONEY) {
            if (myWalletFragment != null) {
                myWalletFragment.onActivityResult(requestCode, resultCode, data);
            }


        }

    }

    /*EndOfTheDay Trip Implementation Start */

    public void isRouteDrawn() {
        hail_action.setVisibility(View.GONE);
        hail_actionRTL.setVisibility(View.GONE);

        return_action.setVisibility(View.GONE);
        return_actionRTL.setVisibility(View.GONE);
        heat_action.setVisibility(View.GONE);
        heat_actionRTL.setVisibility(View.GONE);

        multiple_actionsRTL.setVisibility(View.GONE);
        menuMultipleActions.setVisibility(View.GONE);

        handleMapAnimation();

        if (!updateDirections.data.hasExtra("eDestinationMode")) {
            if (faredialog == null) {
                openDestinationConfirmationDialog();
            } else if (faredialog != null && !faredialog.isShowing()) {
                ((MTextView) faredialog.findViewById(R.id.locationName)).setText(updateDirections.data.getStringExtra("Address"));
                ((MTextView) faredialog.findViewById(R.id.remainingDestTxt)).setText(getRemaningDest());
                faredialog.show();
            }
        } else {
            if (eodLocationArea.getVisibility() == View.GONE) {
                if (!iswalletFragemnt && !isbookingFragemnt && !isProfileFragment) {
                    eodLocationArea.setVisibility(View.VISIBLE);
                    RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                            RelativeLayout.LayoutParams.MATCH_PARENT,
                            RelativeLayout.LayoutParams.WRAP_CONTENT
                    );
                    params.setMargins(0, Utils.dpToPx(90,getActContext()), 0, 0);

                    mapviewarea.setLayoutParams(params);

                }
                addressTxt.setText(updateDirections.data.getStringExtra("Address"));
            }

        }
    }


    private void enableEOD() {
        boolean eDestinationMode = generalFunc.getJsonValueStr("eDestinationMode", obj_userProfile).equalsIgnoreCase("Yes");
        boolean ENABLE_DRIVER_DESTINATIONS = generalFunc.retrieveValue(Utils.ENABLE_DRIVER_DESTINATIONS_KEY).equalsIgnoreCase("Yes") && !eDestinationMode;
        return_action.setVisibility(ENABLE_DRIVER_DESTINATIONS ? View.VISIBLE : View.GONE);
        return_actionRTL.setVisibility(ENABLE_DRIVER_DESTINATIONS ? View.VISIBLE : View.GONE);
        JSONObject DriverDestinationData_obj = generalFunc.getJsonObject("DriverDestinationData", obj_userProfile);

        if (eDestinationMode && DriverDestinationData_obj != null && DriverDestinationData_obj.length() > 0) {
            Intent data = new Intent();
            data.putExtra("Latitude", generalFunc.getJsonValueStr("tDestinationStartedLatitude", DriverDestinationData_obj));
            data.putExtra("Longitude", generalFunc.getJsonValueStr("tDestinationStartedLongitude", DriverDestinationData_obj));
            data.putExtra("Address", generalFunc.getJsonValueStr("tDestinationStartedAddress", DriverDestinationData_obj));
            data.putExtra("eDestinationMode", generalFunc.getJsonValueStr("eDestinationMode", obj_userProfile));

            drawRoute(data);
        }

    }

    private void drawRoute(Intent data) {
        String destlat = data.getStringExtra("Latitude");
        String destlong = data.getStringExtra("Longitude");

        Location destLoc = new Location("temp");
        destLoc.setLatitude(generalFunc.parseDoubleValue(0.0, destlat));
        destLoc.setLongitude(generalFunc.parseDoubleValue(0.0, destlong));

        if (updateDirections == null) {
            updateDirections = new UpdateDirections(getActContext(), gMap, userLocation, destLoc);

        }
        if (updateDirections != null) {
            updateDirections.changeUserLocation(userLocation);
            updateDirections.setIntentData(data);
            if (!data.hasExtra("eDestinationMode")) {
                updateDirections.scheduleDirectionUpdate();
            } else {
                updateDirections.updateDirections();
            }
        }
    }


    public void handleMapAnimation() {

        try {
            LatLng sourceLocation = new LatLng(updateDirections.userLocation.getLatitude(), updateDirections.userLocation.getLongitude());
            LatLng destLocation = new LatLng(updateDirections.destinationLocation.getLatitude(), updateDirections.destinationLocation.getLongitude());

            MapAnimator.getInstance().stopRouteAnim();

            LatLng fromLnt = new LatLng(sourceLocation.latitude, sourceLocation.longitude);
            LatLng toLnt = new LatLng(destLocation.latitude, destLocation.longitude);

            if (destMarker != null) {
                destMarker.remove();
                destMarker = null;
            }
            MarkerOptions markerOptions_destLocation = new MarkerOptions();
            markerOptions_destLocation.position(toLnt);
            markerOptions_destLocation.icon(BitmapDescriptorFactory.fromResource(R.drawable.pin_dest_select)).anchor(0.5f,
                    0.5f);
            destMarker = getMap().addMarker(markerOptions_destLocation);

            if (sourceMarker != null) {
                sourceMarker.remove();
                sourceMarker = null;
            }
            MarkerOptions markerOptions_sourceLocation = new MarkerOptions();
            markerOptions_sourceLocation.position(fromLnt);
            markerOptions_sourceLocation.icon(BitmapDescriptorFactory.fromResource(R.drawable.pin_source_select)).anchor(0.5f,
                    0.5f);
            sourceMarker = getMap().addMarker(markerOptions_sourceLocation);

            buildMarkers();

        } catch (Exception e) {
            // Backpress done by user then app crashes

            e.printStackTrace();
        }

    }

    private void buildMarkers() {
        {
            map.getView().getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @SuppressLint("NewApi") // We check which build version we are using.
                @Override
                public void onGlobalLayout() {

                    boolean isBoundIncluded = false;

                    LatLngBounds.Builder builder = new LatLngBounds.Builder();

                    if (sourceMarker != null) {
                        isBoundIncluded = true;
                        builder.include(sourceMarker.getPosition());
                    }


                    if (destMarker != null) {
                        isBoundIncluded = true;
                        builder.include(destMarker.getPosition());
                    }


                    if (isBoundIncluded) {

                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                            map.getView().getViewTreeObserver().removeGlobalOnLayoutListener(this);
                        } else {
                            map.getView().getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        }

                        LatLngBounds bounds = builder.build();

                        DisplayMetrics metrics = new DisplayMetrics();
                        getWindowManager().getDefaultDisplay().getMetrics(metrics);
                        int height_ = metrics.heightPixels;
                        int width = metrics.widthPixels;
                        // Set Padding according to included bounds
                        int padding = 25; // offset from edges of the map in pixels
                        int height_NW;
                        if (faredialog != null && faredialog.isShowing()) {
                            height_NW = (height_ - height) - Utils.dipToPixels(getActContext(), 80);
                            Logger.d("HEIGHT", "" + height);
                            Logger.d("height_NW", "" + height_NW);
                        } else {
                            height_NW = height_ - Utils.dipToPixels(getActContext(), 140) - Utils.dipToPixels(getActContext(), 80);
                            Logger.d("height_NW", "" + height_NW);
                        }


                        try {
                            /*  Method 3 */
                            padding = (int) (((height_NW + 5) * 0.100) / 2);
                            Logger.e("MapHeight", "cameraUpdate" + padding);
                            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(builder.build(),
                                    width, (height_NW + 5), padding);
                            getMap().animateCamera(cameraUpdate);
                        } catch (Exception e) {
                            e.printStackTrace();
                            /*  Method 1 */
                            getMap().moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), width, (height_NW + 5), padding));
                        }


                    }

                }
            });
        }
    }

    public ProgressBar mProgressBarEOD;
    public SlideButton slideButtonEOD;
    public void openDestinationConfirmationDialog() {
        if (faredialog != null) {
            faredialog.dismiss();
        }

        faredialog = new BottomSheetDialog(getActContext());

        View contentView = View.inflate(getActContext(), R.layout.design_end_day_start_trip, null);
        faredialog.setContentView(contentView);
        BottomSheetBehavior mBehavior = BottomSheetBehavior.from((View) contentView.getParent());
        View bottomSheetView = faredialog.getWindow().getDecorView().findViewById(R.id.design_bottom_sheet);
        BottomSheetBehavior.from(bottomSheetView).setHideable(false);
        setCancelable(faredialog, false);


        mProgressBarEOD = (ProgressBar) faredialog.findViewById(R.id.mProgressBar);
//        View shadowView = (View) faredialog.findViewById(R.id.shadowView);
//        View leftSeperationLine = (View) faredialog.findViewById(R.id.leftSeperationLine);
        MTextView locationName = (MTextView) faredialog.findViewById(R.id.locationName);
        MTextView remainingDestTxt = (MTextView) faredialog.findViewById(R.id.remainingDestTxt);
        MTextView destDescriptionText = (MTextView) faredialog.findViewById(R.id.destDescriptionText);
        ImageView iv_close = (ImageView) faredialog.findViewById(R.id.iv_close);
        MButton btn_type2 = ((MaterialRippleLayout) faredialog.findViewById(R.id.btn_type2)).getChildView();
        int submitBtnId = Utils.generateViewId();
        btn_type2.setId(submitBtnId);
        btn_type2.setText(generalFunc.retrieveLangLBl("", "LBL_START_DEST_MODE_TXT"));
        btn_type2.setVisibility(View.GONE);

        destDescriptionText.setText(generalFunc.retrieveLangLBl("", "LBL_START_DESTINATION_TRIP"));
         slideButtonEOD = faredialog.findViewById(R.id.slideButton);
        slideButtonEOD.setButtonText("  "+generalFunc.retrieveLangLBl("", "LBL_START_DEST_MODE_TXT"));
        slideButtonEOD.setBackgroundColor(getResources().getColor(R.color.appThemeColor_1));
        slideButtonEOD.onClickListener(isCompleted -> {
            if (isCompleted) {
                startDriverDestination(faredialog, updateDirections.data);
                new Handler().postDelayed(() -> slideButtonEOD.resetButtonView(slideButtonEOD.btnText.getText().toString()), 2000);
            }
        });

        LinearLayout layout = (LinearLayout) faredialog.findViewById(R.id.mainArea);
        height = Utils.dpToPx(380, getActContext());
        mBehavior.setPeekHeight(height);

        mProgressBarEOD.getIndeterminateDrawable().setColorFilter(
                getActContext().getResources().getColor(R.color.appThemeColor_2), android.graphics.PorterDuff.Mode.SRC_IN);
        mProgressBarEOD.setIndeterminate(true);
        mProgressBarEOD.setVisibility(View.VISIBLE);
        locationName.setText(updateDirections.data.getStringExtra("Address"));
        remainingDestTxt.setText(getRemaningDest());

        btn_type2.setOnClickListener(v -> confirmDestination(faredialog));

        iv_close.setOnClickListener(v -> {
            faredialog.dismiss();
            removeEODTripData(true);
        });

        faredialog.setOnDismissListener(dialog -> {
        });
        // faredialog.getWindow().setBackgroundDrawable(getActContext().getResources().getDrawable(R.drawable.all_roundcurve_card));

        faredialog.show();
    }

    private String getRemaningDest() {
        String destAddressSHLbl = "";
        int MAX_DRIVER_DESTINATIONS = GeneralFunctions.parseIntegerValue(0, generalFunc.getJsonValueStr("MAX_DRIVER_DESTINATIONS", obj_userProfile));
        int iDestinationCount = GeneralFunctions.parseIntegerValue(0, generalFunc.getJsonValueStr("iDestinationCount", obj_userProfile));
        String remainingDestCount = generalFunc.convertNumberWithRTL("" + (MAX_DRIVER_DESTINATIONS - iDestinationCount));
        destAddressSHLbl = generalFunc.retrieveLangLBl("", "LBL_DESTINATION") + ": " + remainingDestCount + " " + generalFunc.retrieveLangLBl("", "LBL_REMAINIG_TXT");
        return destAddressSHLbl;

    }

    private void removeEODTripData(boolean resetHail) {
        if (sourceMarker != null) {
            sourceMarker.remove();
            sourceMarker = null;
        }

        if (destMarker != null) {
            destMarker.remove();
            destMarker = null;
        }


        if (updateDirections != null) {
            updateDirections.releaseTask();
            updateDirections = null;
        }

        eodLocationArea.setVisibility(View.GONE);
        showHeatMap();

        if (resetHail) {
            isHailRideOptionEnabled();
        }

        if (gMap != null)
            gMap.clear();

        if (generalFunc.isRTLmode()) {
            multiple_actionsRTL.setVisibility(View.VISIBLE);
            menuMultipleActions.setVisibility(View.GONE);
        } else {
            menuMultipleActions.setVisibility(View.VISIBLE);
            multiple_actionsRTL.setVisibility(View.GONE);
        }

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, 0);

        mapviewarea.setLayoutParams(params);

    }

    public void confirmDestination(BottomSheetDialog dialog1) {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getActContext());
        builder.setTitle("");
        String message = generalFunc.retrieveLangLBl("", "LBL_START_DESTINATION_TRIP");
        builder.setMessage(message);

        builder.setPositiveButton(generalFunc.retrieveLangLBl("", "LBL_BTN_YES_TXT"), (dialog, which) -> {

        });
        builder.setNegativeButton(generalFunc.retrieveLangLBl("", "LBL_NO"), (dialog, which) -> {
        });

        confirmDialog = builder.create();
        if (generalFunc.isRTLmode() == true) {
            generalFunc.forceRTLIfSupported(confirmDialog);
        }
        confirmDialog.show();

        confirmDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(view -> startDriverDestination(dialog1, updateDirections.data));

        confirmDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(view -> confirmDialog.dismiss());
    }

    public void startDriverDestination(BottomSheetDialog dialog1, Intent data) {
        String destlat = data.getStringExtra("Latitude");
        String destlong = data.getStringExtra("Longitude");
        String destAddress = data.getStringExtra("Address");

        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "startDriverDestination");
        parameters.put("iDriverId", generalFunc.getMemberId());
        parameters.put("tRootDestLatitudes", updateDirections != null ? TextUtils.join(",", updateDirections.lattitudeList) : "");
        parameters.put("tRootDestLongitudes", updateDirections != null ? TextUtils.join(",", updateDirections.longitudeList) : "");
        parameters.put("tAdress", destAddress);
        parameters.put("eStatus", "Active");
        parameters.put("tDriverDestLatitude", destlat);
        parameters.put("tDriverDestLongitude", destlong);

        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(), parameters);
        exeWebServer.setLoaderConfig(getActContext(), true, generalFunc);
        exeWebServer.setIsDeviceTokenGenerate(true, "vDeviceToken", generalFunc);
        exeWebServer.setDataResponseListener(responseString -> {

            faredialog.dismiss();

            if (responseString != null && !responseString.equals("")) {

                boolean isDataAvail = GeneralFunctions.checkDataAvail(Utils.action_str, responseString);

                if (isDataAvail == true) {
                    String message = generalFunc.getJsonValue(Utils.message_str, responseString);
                    generalFunc.storeData(Utils.USER_PROFILE_JSON, message);
                    obj_userProfile = generalFunc.getJsonObject(generalFunc.retrieveValue(Utils.USER_PROFILE_JSON));
                    changeObj();
                    dialog1.dismiss();

                    JSONObject DriverDestinationData_obj = generalFunc.getJsonObject("DriverDestinationData", obj_userProfile);
                    data.putExtra("Latitude", generalFunc.getJsonValueStr("tDestinationStartedLatitude", DriverDestinationData_obj));
                    data.putExtra("Longitude", generalFunc.getJsonValueStr("tDestinationStartedLongitude", DriverDestinationData_obj));
                    data.putExtra("Address", generalFunc.getJsonValueStr("tDestinationStartedAddress", DriverDestinationData_obj));
                    data.putExtra("eDestinationMode", generalFunc.getJsonValueStr("eDestinationMode", obj_userProfile));


                    if (updateDirections != null) {
                        updateDirections.setIntentData(data);
                        updateDirections.scheduleDirectionUpdate();
                    }

                    addressTxt.setText(data.getStringExtra("Address"));

                    if (!iswalletFragemnt && !isbookingFragemnt && !isProfileFragment) {
                        eodLocationArea.setVisibility(View.VISIBLE);
                        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                                RelativeLayout.LayoutParams.MATCH_PARENT,
                                RelativeLayout.LayoutParams.WRAP_CONTENT
                        );
                        params.setMargins(0, Utils.dpToPx(90,getActContext()), 0, 0);

                        mapviewarea.setLayoutParams(params);


                    }
                    handleMapAnimation();

                    if (faredialog != null) {
                        faredialog.dismiss();
                        faredialog=null;
                    }

                } else {

                    String message_str = generalFunc.getJsonValue(Utils.message_str, responseString);
                    String message = generalFunc.retrieveLangLBl(message_str, message_str);
                    generalFunc.showGeneralMessage("", message);
                }
            } else {
                generalFunc.showError();
            }
        });
        exeWebServer.execute();
    }

    public void buildMsgOnEODCancelRequests() {
        final GenerateAlertBox generateAlert = new GenerateAlertBox(getActContext());
        generateAlert.setCancelable(false);
        generateAlert.setBtnClickList(btn_id -> {
            if (btn_id == 0) {
                generateAlert.closeAlertBox();
            } else {
                CancelDriverDestination();
            }

        });

        generateAlert.setContentMessage("", generalFunc.retrieveLangLBl("", "LBL_END_DESTINATION_TRIP"));

        generateAlert.setPositiveBtn(generalFunc.retrieveLangLBl("", "LBL_BTN_YES_TXT"));
        generateAlert.setNegativeBtn(generalFunc.retrieveLangLBl("", "LBL_BTN_NO_TXT"));
        generateAlert.showAlertBox();
    }

    public void CancelDriverDestination() {

        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "CancelDriverDestination");
        parameters.put("iDriverId", generalFunc.getMemberId());

        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(), parameters);
        exeWebServer.setLoaderConfig(getActContext(), true, generalFunc);
        exeWebServer.setIsDeviceTokenGenerate(true, "vDeviceToken", generalFunc);
        exeWebServer.setDataResponseListener(responseString -> {

            if (responseString != null && !responseString.equals("")) {

                boolean isDataAvail = GeneralFunctions.checkDataAvail(Utils.action_str, responseString);

                if (isDataAvail == true) {
                    String message = generalFunc.getJsonValue(Utils.message_str, responseString);
                    generalFunc.storeData(Utils.USER_PROFILE_JSON, message);
                    obj_userProfile = generalFunc.getJsonObject(generalFunc.retrieveValue(Utils.USER_PROFILE_JSON));
                    changeObj();

                    generalFunc.storeData(Utils.DRIVER_DESTINATION_AVAILABLE_KEY, generalFunc.getJsonValue(Utils.DRIVER_DESTINATION_AVAILABLE_KEY, message));

                    removeEODTripData(true);
                } else {
                    generalFunc.showError();
                }
            } else {
                generalFunc.showError();
            }
        });
        exeWebServer.execute();
    }


    public void setCancelable(Dialog dialogview, boolean cancelable) {
        final Dialog dialog = dialogview;
        View touchOutsideView = dialog.getWindow().getDecorView().findViewById(R.id.touch_outside);
        View bottomSheetView = dialog.getWindow().getDecorView().findViewById(R.id.design_bottom_sheet);
        dialog.setCancelable(cancelable);

        if (cancelable) {
            touchOutsideView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (dialog.isShowing()) {
                        dialog.cancel();
                    }
                }
            });
            BottomSheetBehavior.from(bottomSheetView).setHideable(true);
        } else {
            touchOutsideView.setOnClickListener(null);
            BottomSheetBehavior.from(bottomSheetView).setHideable(false);
        }
    }

    /*EndOfTheDay Trip Implementation End */

    @Override
    public void onItemClick(int position, int viewClickId) {
        list_car.dismiss();

        String selected_carId = items_car_id.get(position);

        configCarList(true, selected_carId, position);
    }


    public void handleNoNetworkDial() {
        String eStatus = generalFunc.getJsonValueStr("eStatus", obj_userProfile);
        if (!eStatus.equalsIgnoreCase("inactive")) {

            if (intCheck.isNetworkConnected() && intCheck.check_int()) {

            }

            if (!intCheck.isNetworkConnected() && !intCheck.check_int()) {
            } else {
                handleNoLocationDial();
            }
        }
    }

    public void handleNoLocationDial() {
        try {
            if (!generalFunc.isLocationEnabled() && isDriverOnline == true) {
                if (app_type.equals(Utils.CabGeneralType_UberX)) {
                    ufxonlineOfflineSwitch.setChecked(false);
                } else {
                    onlineOfflineSwitch.setChecked(false);
                }
            }
        } catch (Exception e) {

        }
    }


    @Override
    public void onAddressFound(String address, double latitude, double longitude, String geocodeobject){

        if (generalFunc.getJsonValueStr("PROVIDER_AVAIL_LOC_CUSTOMIZE", obj_userProfile).equalsIgnoreCase("Yes") && generalFunc.getJsonValueStr("eSelectWorkLocation", obj_userProfile).equalsIgnoreCase("Fixed")) {
            String WORKLOCATION = generalFunc.retrieveValue(Utils.WORKLOCATION);
            if (!WORKLOCATION.equals("")) {
                addressTxtView.setText(WORKLOCATION);
            } else {
                addressTxtView.setText(address);
                addressTxtViewufx.setText(address);
            }
        } else {
            addressTxtView.setText(address);
            addressTxtViewufx.setText(address);
        }
    }


    public class setOnClickList implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            Utils.hideKeyboard(MainActivity.this);
            if (view.getId() == userLocBtnImgView.getId()) {
                if (userLocation == null) {
                    return;
                }
                //    CameraPosition cameraPosition = cameraForUserPosition();
                CameraUpdate cameraPosition = (new AppFunctions(getActContext()).getCameraPosition(userLocation, gMap));
                if (cameraPosition != null)
                    getMap().animateCamera(cameraPosition);
            } else if (view.getId() == heat_action.getId() || view.getId() == heat_actionRTL.getId()) {
                menuMultipleActions.collapse();
                multiple_actionsRTL.collapse();
                if (userLocation == null) {
                    return;
                }
                isfirstZoom = true;
                configHeatMapView(isShowNearByPassengers ? false : true);
            } else if (view.getId() == changeCarTxt.getId()) {
                configCarList(false, "", 0);
            } else if (view.getId() == hail_action.getId() || view.getId() == hail_actionRTL.getId()) {
                menuMultipleActions.collapse();
                multiple_actionsRTL.collapse();
                if (!intCheck.isNetworkConnected() && !intCheck.check_int()) {
                    generalFunc.showMessage(userLocBtnImgView, generalFunc.retrieveLangLBl("No Internet Connection", "LBL_NO_INTERNET_TXT"));
                } else {
                    if (!isBtnClick) {
                        isBtnClick = true;
                        checkHailType();
                    }
                }
            } else if (view.getId() == menuufxImgView.getId()) {
//                checkDrawerState();

            } else if (view.getId() == pendingarea.getId()) {
                Bundle bn = new Bundle();
                bn.putBoolean("ispending", true);
                new StartActProcess(getActContext()).startActWithData(BookingsActivity.class, bn);
            } else if (view.getId() == upcomginarea.getId()) {

                Bundle bn = new Bundle();
                bn.putBoolean("isupcoming", true);
                new StartActProcess(getActContext()).startActWithData(BookingsActivity.class, bn);

            } else if (view.getId() == location_action.getId() || view.getId() == location_actionRTL.getId()) {
                menuMultipleActions.collapse();
                multiple_actionsRTL.collapse();


                new StartActProcess(getActContext()).startAct(WorkLocationActivity.class);
            } else if (view.getId() == refreshImgView.getId()) {
                isFirstAddressLoaded = false;
                onLocationUpdate(GetLocationUpdates.getInstance().getLastLocation());
                getUserstatus();
            } else if (view.getId() == imageradiusufx.getId()) {
                new StartActProcess(getActContext()).startAct(WorkLocationActivity.class);
            } else if (view.getId() == btn_edit.getId()) {
                new StartActProcess(getActContext()).startAct(WorkLocationActivity.class);
            }
            /*EndOfTheDay Click events */
            else if (view.getId() == removeEodTripArea.getId()) {
                setBounceAnimation(removeEodTripArea, () -> {
                    buildMsgOnEODCancelRequests();
                });

            } else if (view.getId() == return_action.getId() || view.getId() == return_actionRTL.getId()) {

                menuMultipleActions.collapse();
                multiple_actionsRTL.collapse();

                if (generalFunc.retrieveValue(Utils.DRIVER_DESTINATION_AVAILABLE_KEY).equalsIgnoreCase("Yes")) {
                    Bundle bn = new Bundle();
                    bn.putString("requestType", "endOfDayTrip");
                    bn.putString("locationArea", "dest");

                    if (userLocation != null) {
                        bn.putDouble("lat", userLocation.getLatitude());
                        bn.putDouble("long", userLocation.getLongitude());
                    }


                    new StartActProcess(getActContext()).startActForResult(SearchLocationActivity.class, bn, Utils.SEARCH_PICKUP_LOC_REQ_CODE);

                } else {
                    String message = generalFunc.retrieveLangLBl("", "LBL_DRIVER_DEST_LIMIT_REACHED") + " " + generalFunc.parseIntegerValue(0, generalFunc.getJsonValueStr("MAX_DRIVER_DESTINATIONS", obj_userProfile)) + " " + generalFunc.retrieveLangLBl("", "LBL_FOR_A_DAY");
                    Snackbar snackbar = Snackbar.make(view, message, Snackbar.LENGTH_INDEFINITE);
                    snackbar.setActionTextColor(getActContext().getResources().getColor(R.color.verfiybtncolor));
                    snackbar.setDuration(10000);
                    snackbar.show();
                }

            } else if (view.getId() == notificationImg.getId()) {
                multiple_actionsRTL.collapse();
                menuMultipleActions.collapse();
                shadowView.setVisibility(View.GONE);
                new StartActProcess(getActContext()).startAct(NotificationActivity.class);
            }
        }
    }


    public void manageHome() {
        iswalletFragemnt = false;
        isbookingFragemnt = false;
        isProfileFragment = false;

        String eStatus = generalFunc.getJsonValueStr("eStatus", obj_userProfile);
        if (eStatus.equalsIgnoreCase("inactive")) {
            mapbottomviewarea.setVisibility(View.GONE);
            Toolbar.setVisibility(View.VISIBLE);
            mapviewarea.setVisibility(View.GONE);
            menuMultipleActions.setVisibility(View.GONE);
            multiple_actionsRTL.setVisibility(View.GONE);
            return_action.setVisibility(View.GONE);
            return_actionRTL.setVisibility(View.GONE);
            headerLogo.setVisibility(View.VISIBLE);
            onlineOfflineSwitch.setVisibility(View.GONE);
            selCarArea.setVisibility(View.GONE);
            headerLogoride.setVisibility(View.VISIBLE);
            // ((SelectableRoundedImageView) findViewById(R.id.userPicImgView)).setVisibility(View.GONE);
            InactiveFragment inactiveFragment = new InactiveFragment();
            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            if (app_type.equals(Utils.CabGeneralType_UberX)) {
                activearea.setVisibility(View.GONE);
                MainHeaderLayout.setVisibility(View.VISIBLE);
//                ft.replace(R.id.containerufx, inactiveFragment);
//                ft.commit();

            }
//            else {
//                ft.replace(R.id.container, inactiveFragment);
//                ft.commit();
//            }

            openPageFrag(1, inactiveFragment);

            bottomBtnpos = 1;
        } else {


            mapviewarea.setVisibility(View.VISIBLE);
            if (generalFunc.isRTLmode()) {
                multiple_actionsRTL.setVisibility(View.VISIBLE);
                menuMultipleActions.setVisibility(View.GONE);
            } else {
                menuMultipleActions.setVisibility(View.VISIBLE);
                multiple_actionsRTL.setVisibility(View.GONE);
            }

            if(generalFunc.retrieveValue(Utils.ONLYDELIVERALL_KEY).equalsIgnoreCase("Yes"))
            {
                menuMultipleActions.setVisibility(View.GONE);
                multiple_actionsRTL.setVisibility(View.GONE);
            }

            Toolbar.setVisibility(View.VISIBLE);
            onlineOfflineSwitch.setVisibility(View.VISIBLE);
            selCarArea.setVisibility(View.VISIBLE);
            headerLogoride.setVisibility(View.GONE);
            if (app_type.equals(Utils.CabGeneralType_UberX)) {
                ufxarea.setVisibility(View.VISIBLE);
            }
            ((SelectableRoundedImageView) findViewById(R.id.userPicImgView)).setVisibility(View.VISIBLE);

            if (app_type.equalsIgnoreCase(Utils.CabGeneralTypeRide_Delivery_UberX)) {
                joblocareaufx.setVisibility(View.GONE);

            }

            if (app_type.equals(Utils.CabGeneralType_UberX)) {
                containerufx.setVisibility(View.GONE);
                MainHeaderLayout.setVisibility(View.VISIBLE);

                refreshImgView.setVisibility(View.VISIBLE);
            }

            headerLogo.setVisibility(View.GONE);

            if (isDriverOnline) {
                isHailRideOptionEnabled();
            }
            mapbottomviewarea.setVisibility(View.VISIBLE);
            mapviewarea.setVisibility(View.VISIBLE);


            handleNoLocationDial();
            if (updateDirections != null) {
                isRouteDrawn();
            }
            bottomBtnpos = 1;
        }

    }

    private void setBounceAnimation(View view, BounceAnimListener bounceAnimListener) {
        Animation anim = AnimationUtils.loadAnimation(getActContext(), R.anim.bounce_interpolator);
        anim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {

                if (bounceAnimListener != null) {
                    bounceAnimListener.onAnimationFinished();
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        view.startAnimation(anim);
    }

    private interface BounceAnimListener {
        void onAnimationFinished();
    }

    public void getUserstatus() {
        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "GetUserStats");
        parameters.put("iDriverId", generalFunc.getMemberId());

        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(), parameters);
        exeWebServer.setLoaderConfig(getActContext(), false, generalFunc);
        exeWebServer.setIsDeviceTokenGenerate(true, "vDeviceToken", generalFunc);
        exeWebServer.setDataResponseListener(responseString -> {

            if (responseString != null && !responseString.equals("")) {

                boolean isDataAvail = GeneralFunctions.checkDataAvail(Utils.action_str, responseString);

                if (isDataAvail == true) {

                    pendingjobValTxtView.setText(generalFunc.getJsonValue("Pending_Count", responseString));

                    upcomingjobValTxtView.setText(generalFunc.getJsonValue("Upcoming_Count", responseString));

                    radiusval = generalFunc.getJsonValue("Radius", responseString);
                    setRadiusVal();

                }
            }
        });
        exeWebServer.execute();
    }


    private void checkHailType() {
        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "CheckVehicleEligibleForHail");
        parameters.put("iDriverId", generalFunc.getMemberId());

        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(), parameters);
        exeWebServer.setLoaderConfig(getActContext(), true, generalFunc);

        exeWebServer.setDataResponseListener(responseString -> {

            if (responseString != null && !responseString.equals("")) {

                boolean isDataAvail = GeneralFunctions.checkDataAvail(Utils.action_str, responseString);

                if (isDataAvail == true) {

                    isBtnClick = false;
                    Bundle bn = new Bundle();
                    bn.putString("userLocation", userLocation + "");
                    bn.putDouble("lat", userLocation.getLatitude());
                    bn.putDouble("long", userLocation.getLongitude());
                    new StartActProcess(getActContext()).startActWithData(HailActivity.class, bn);
                } else {
                    isBtnClick = false;

                    String message = generalFunc.getJsonValue(Utils.message_str, responseString);

                    if (message.equals("REQUIRED_MINIMUM_BALNCE")) {
                        isHailRideOptionEnabled();
                        Bundle bn = new Bundle();
                        bn.putString("UserProfileJson", obj_userProfile.toString());
                        buildLowBalanceMessage(getActContext(), generalFunc.getJsonValue("Msg", responseString), bn);
                        return;
                    }
                    generalFunc.showGeneralMessage("",
                            generalFunc.retrieveLangLBl("", generalFunc.getJsonValue(Utils.message_str, responseString)));

                }
            } else {
                isBtnClick = false;
            }
        });
        exeWebServer.execute();

    }

    public interface OnAlertButtonClickListener {
        void onAlertButtonClick(int buttonId);
    }

    public void buildLowBalanceMessage(final Context context, String message, final Bundle bn) {

        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(context);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View dialogView = inflater.inflate(R.layout.design_cash_balance_dialoge, null);
        builder.setView(dialogView);

        final MTextView addNowTxtArea = (MTextView) dialogView.findViewById(R.id.addNowTxtArea);
        final MTextView msgTxt = (MTextView) dialogView.findViewById(R.id.msgTxt);
        final MTextView skipTxtArea = (MTextView) dialogView.findViewById(R.id.skipTxtArea);
        final MTextView titileTxt = (MTextView) dialogView.findViewById(R.id.titileTxt);
        titileTxt.setText(generalFunc.retrieveLangLBl("", "LBL_LOW_BALANCE"));

        boolean isCash = generalFunc.getJsonValue("APP_PAYMENT_MODE", bn.getString("UserProfileJson")).equalsIgnoreCase("Cash");

        if (isCash) {
            addNowTxtArea.setText(generalFunc.retrieveLangLBl("", "LBL_CONTACT_US_TXT"));
        } else {
            addNowTxtArea.setText(generalFunc.retrieveLangLBl("", "LBL_ADD_NOW"));
        }


        skipTxtArea.setText(generalFunc.retrieveLangLBl("", "LBL_BTN_OK_TXT"));
        msgTxt.setText(message);


        skipTxtArea.setOnClickListener(view -> cashBalAlertDialog.dismiss());

        addNowTxtArea.setOnClickListener(view -> {
            cashBalAlertDialog.dismiss();
            if (isCash) {
                new StartActProcess(context).startAct(ContactUsActivity.class);

            } else {
                new StartActProcess(context).startActWithData(MyWalletActivity.class, bn);
            }

        });
        cashBalAlertDialog = builder.create();
        cashBalAlertDialog.setCancelable(false);
        if (generalFunc.isRTLmode() == true) {
            generalFunc.forceRTLIfSupported(cashBalAlertDialog);
        }
        cashBalAlertDialog.getWindow().setBackgroundDrawable(getActContext().getResources().getDrawable(R.drawable.all_roundcurve_card));
        cashBalAlertDialog.show();
    }
}
