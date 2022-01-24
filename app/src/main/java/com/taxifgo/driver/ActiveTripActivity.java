package com.taxifgo.driver;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.core.widget.NestedScrollView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.text.InputType;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RelativeLayout;

import com.adapter.files.OnGoingTripDetailAdapter;
import com.dialogs.OpenListView;
import com.general.files.AppFunctions;
import com.general.files.CancelTripDialog;
import com.general.files.ConfigPubNub;
import com.general.files.ExecuteWebServerUrl;
import com.general.files.GeneralFunctions;
import com.general.files.GetAddressFromLocation;
import com.general.files.GetLocationUpdates;
import com.general.files.ImageFilePath;
import com.general.files.InternetConnection;
import com.general.files.MyApp;
import com.general.files.OpenPassengerDetailDialog;
import com.general.files.OpenUserInstructionDialog;
import com.general.files.SinchService;
import com.general.files.SlideButton;
import com.general.files.StartActProcess;
import com.general.files.UpdateDirections;
import com.general.files.UpdateFrequentTask;
import com.general.files.UploadProfileImage;
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
import com.google.android.gms.maps.model.Polyline;
import com.sinch.android.rtc.calling.Call;
import com.squareup.picasso.Picasso;
import com.utils.AnimateMarker;
import com.utils.CommonUtilities;
import com.utils.Logger;
import com.utils.NavigationSensor;
import com.utils.Utils;
import com.view.CreateRoundedView;
import com.view.GenerateAlertBox;
import com.view.MButton;
import com.view.MTextView;
import com.view.MaterialRippleLayout;
import com.view.MyProgressDialog;
import com.view.SelectableRoundedImageView;
import com.view.editBox.MaterialEditText;
import com.view.simpleratingbar.SimpleRatingBar;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class ActiveTripActivity extends BaseActivity implements OnMapReadyCallback, GetLocationUpdates.LocationUpdatesListener {

    public static final int MEDIA_TYPE_IMAGE = 1;
    private static final String IMAGE_DIRECTORY_NAME = "Temp";
    private static final int SELECT_PICTURE = 2;
    private static final int CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 100;
    public Location userLocation;
    public ImageView emeTapImgView;
    public MTextView timeTxt, distanceTxt;
    GeneralFunctions generalFunc;
    MTextView titleTxt;
    String tripId = "";
    String eType = "";
    public HashMap<String, String> data_trip;
    SupportMapFragment map;
    GoogleMap gMap;
    boolean isFirstLocation = true;
    //Intent startLocationUpdateService;
    MTextView addressTxt;
    boolean isDestinationAdded = false;
    double destLocLatitude = 0.0;
    double destLocLongitude = 0.0;
    Marker destLocMarker = null;
    Polyline route_polyLine;
    LinearLayout timerarea;
    boolean isTripCancelPressed = false;
    boolean isTripStart = false;
    String reason = "";
    String comment = "";
    String REQUEST_TYPE = "";
    String deliveryVerificationCode = "";
    androidx.appcompat.app.AlertDialog deliveryEndDialog;
    String SITE_TYPE = "";
    String imageType = "";
    String isFrom = "";
    Dialog uploadServicePicAlertBox = null;
    LinearLayout destLocSearchArea;
    UpdateFrequentTask timerrequesttask;
    ArrayList<HashMap<String, String>> list;
    ArrayList<HashMap<String, String>> tripDetail;
    HashMap<String, String> tempMap;
    OnGoingTripDetailAdapter onGoingTripDetailAdapter;
    RecyclerView onGoingTripsDetailListRecyclerView;
    SimpleRatingBar ratingBar, ratingBar_ufx;
    SelectableRoundedImageView user_img;
    ArrayList<Double> additonallist = new ArrayList<>();
    String currencetprice = "0.00";
    MTextView userNameTxt, userAddressTxt, progressHinttext, timerHinttext, tollTxtView;
    MTextView txt_TimerHour, txt_TimerMinute, txt_TimerSecond;
    LinearLayout timerlayoutarea;
    RelativeLayout timerlayoutMainarea;
    String required_str = "";
    String invalid_str = "";
    androidx.appcompat.app.AlertDialog alertDialog;
    boolean isresume = false;
    int i = 0;
    View slideback;
    ImageView imageslide;
    androidx.appcompat.app.AlertDialog list_navigation;
    NestedScrollView scrollview;
    Menu menu;
    boolean isendslide = false;
    UpdateDirections updateDirections;
    Marker driverMarker;
    boolean isnotification = false;
    ImageView googleImage;
    InternetConnection intCheck;
    double finaltotal = 0.00;

    double miscfee = 0.00;
    private MTextView tvHour, tvMinute, tvSecond, btntimer;
    //# Transit Shoppping System
    private MTextView newtvHour, newtvMinute, newtvSecond, newbtn_timer;
    ImageView userTapImgView;
    LinearLayout holdWaitArea;
    //# Transit Shoppping System

    private String selectedImagePath = "";
    private String pathForCameraImage = "";
    private Uri fileUri;
    private String TripTimeId = "";
    JSONObject userProfileJsonObj = null;
    AnimateMarker animateMarker;

    LinearLayout uploadImgArea;
    boolean isCurrentLocationFocused = false;

    boolean isufx = false;

    String eConfirmByUser = "No";
    String payableAmount = "";

    String latitude = "";
    String longitirude = "";
    String address = "";
    double tollamount = 0.0;
    String tollcurrancy = "";
    boolean istollIgnore = false;
    androidx.appcompat.app.AlertDialog tolltax_dialog;

    String eTollConfirmByUser = "";

    FrameLayout bottomArea;

    String ENABLE_DIRECTION_SOURCE_DESTINATION_DRIVER_APP = "";

    androidx.appcompat.app.AlertDialog alertDialog_surgeConfirm;

    /*Multi Delivery View*/
    private ImageView viewDetailsImgView;
    private MTextView subTitleTxt;
    LinearLayout deliveryDetailsArea;
    MTextView pickupTxt;
    MTextView pickupNameTxt, recipientTxt;
    private ImageView iv_callRicipient;
    private String iTripDeliveryLocationId;

    AlertDialog dialog_declineOrder;
    String vImage = "";
    String vName = "";
    MTextView personTxt;
    boolean isPoolRide = false;

    boolean isFirstMapMove = true;
    /*Multistop over*/
    boolean isDropAll = false;
    private RelativeLayout dropAllIconArea;
    Animation anim;
    int currentStopOverPoint;
    int totalStopOverPoint;
    /*Multistop over*/

    String LBL_WAIT = "", LBL_REACH_TXT = "";
    String LBL_CONFIRM_STOPOVER_1 = "", LBL_CONFIRM_STOPOVER_2 = "";
    String LBL_BTN_SLIDE_END_TRIP_TXT = "";
    String APP_TYPE;
    private boolean eFly;

    SlideButton startTripSlideButton, endTripSlideButton;
    RelativeLayout wayBillImgView, deliveryInfoView;
    ImageView callArea, chatArea, navigateAreaUP, dropAllAreaUP, dropCancel;
    RelativeLayout chatview, callview;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_active_trip);
        Toolbar toolbar = findViewById(R.id.toolbar);
        new AppFunctions(getApplicationContext()).setOverflowButtonColor(toolbar, getResources().getColor(R.color.white));

        animateMarker = new AnimateMarker();
        generalFunc = MyApp.getInstance().getGeneralFun(getActContext());
        LBL_WAIT = generalFunc.retrieveLangLBl("Wait", "LBL_WAIT");
        animateMarker = new AnimateMarker();
        userProfileJsonObj = generalFunc.getJsonObject(generalFunc.retrieveValue(Utils.USER_PROFILE_JSON));
        APP_TYPE = generalFunc.retrieveValue(Utils.APP_TYPE);

        ENABLE_DIRECTION_SOURCE_DESTINATION_DRIVER_APP = generalFunc.getJsonValueStr("ENABLE_DIRECTION_SOURCE_DESTINATION_DRIVER_APP", userProfileJsonObj);

        isnotification = getIntent().getBooleanExtra("isnotification", isnotification);

        defaultAddtionalprice();

        intCheck = new InternetConnection(getActContext());

        HashMap<String, String> data = (HashMap<String, String>) getIntent().getSerializableExtra("TRIP_DATA");
        this.data_trip = data;

        if (data_trip.get("eHailTrip").equalsIgnoreCase("Yes")) {
            generalFunc.storeData(Utils.DRIVER_ONLINE_KEY, "false");
        }
        currentStopOverPoint = generalFunc.parseIntegerValue(0, data_trip.get("currentStopOverPoint"));
        totalStopOverPoint = generalFunc.parseIntegerValue(0, data_trip.get("totalStopOverPoint"));
        distanceTxt = (MTextView) findViewById(R.id.distanceTxt);
        eFly = data_trip.get("eFly").equalsIgnoreCase("Yes");
        //gps view declaration start
        if (data_trip.get("eHailTrip").equalsIgnoreCase("Yes") || eFly) {
            generalFunc.storeData(Utils.DRIVER_ONLINE_KEY, "false");
        }

        bottomArea = (FrameLayout) findViewById(R.id.bottomArea);

        subTitleTxt = (MTextView) findViewById(R.id.subTitleTxt);

        //gps view declaration end

        scrollview = (NestedScrollView) findViewById(R.id.scrollview);
        titleTxt = (MTextView) findViewById(R.id.titleTxt);
        onGoingTripsDetailListRecyclerView = (RecyclerView) findViewById(R.id.onGoingTripsDetailListRecyclerView);
        userNameTxt = (MTextView) findViewById(R.id.userNameTxt);
        userAddressTxt = (MTextView) findViewById(R.id.userAddressTxt);
        ratingBar = (SimpleRatingBar) findViewById(R.id.ratingBar);
        ratingBar_ufx = (SimpleRatingBar) findViewById(R.id.ratingBar_ufx);
        tvHour = (MTextView) findViewById(R.id.txtTimerHour);
        tvMinute = (MTextView) findViewById(R.id.txtTimerMinute);
        tvSecond = (MTextView) findViewById(R.id.txtTimerSecond);

        //# Transit Shoppping System
        newtvHour = (MTextView) findViewById(R.id.newtxtTimerHour);
        newtvMinute = (MTextView) findViewById(R.id.newtxtTimerMinute);
        newtvSecond = (MTextView) findViewById(R.id.newtxtTimerSecond);
        newbtn_timer = (MTextView) findViewById(R.id.newbtn_timer);
        newbtn_timer.setOnClickListener(new setOnClickAct());
        userTapImgView = (ImageView) findViewById(R.id.userTapImgView);
        userTapImgView.setOnClickListener(new setOnClickList());
        //#Transit Shoppping System
        addressTxt = (MTextView) findViewById(R.id.addressTxt);
        progressHinttext = (MTextView) findViewById(R.id.progressHinttext);
        timerHinttext = (MTextView) findViewById(R.id.timerHinttext);
        btntimer = (MTextView) findViewById(R.id.btn_timer);
        btntimer.setOnClickListener(new setOnClickAct());
        holdWaitArea = (LinearLayout) findViewById(R.id.holdWaitArea);




        map = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapV2);

        timerarea = (LinearLayout) findViewById(R.id.timerarea);
        timerlayoutarea = (LinearLayout) findViewById(R.id.timerlayoutarea);
        timerlayoutMainarea = (RelativeLayout) findViewById(R.id.timerlayoutMainarea);

        destLocSearchArea = (LinearLayout) findViewById(R.id.destLocSearchArea);
        timeTxt = (MTextView) findViewById(R.id.timeTxt);
        timeTxt.setVisibility(View.GONE);
        googleImage = (ImageView) findViewById(R.id.googleImage);

        txt_TimerHour = (MTextView) findViewById(R.id.txt_TimerHour);
        txt_TimerMinute = (MTextView) findViewById(R.id.txt_TimerMinute);
        txt_TimerSecond = (MTextView) findViewById(R.id.txt_TimerSecond);
        tollTxtView = (MTextView) findViewById(R.id.tollTxtView);

        user_img = (SelectableRoundedImageView) findViewById(R.id.user_img);

        emeTapImgView = (ImageView) findViewById(R.id.emeTapImgView);
        emeTapImgView.setOnClickListener(new setOnClickList());

        slideback = (View) findViewById(R.id.slideback);
        imageslide = (ImageView) findViewById(R.id.imageslide);

        (findViewById(R.id.backImgView)).setVisibility(View.GONE);

        callArea = (ImageView) findViewById(R.id.callArea);
        chatArea = (ImageView) findViewById(R.id.chatArea);
        chatview = (RelativeLayout) findViewById(R.id.chatview);
        callview = (RelativeLayout) findViewById(R.id.callview);
        dropCancel = (ImageView) findViewById(R.id.dropCancel);
        navigateAreaUP = (ImageView) findViewById(R.id.navigateAreaUP);
        dropAllAreaUP = (ImageView) findViewById(R.id.dropAllAreaUP);
        wayBillImgView = (RelativeLayout) findViewById(R.id.wayBillImgView);
        deliveryInfoView = (RelativeLayout) findViewById(R.id.deliveryInfoView);

        callArea.setBackground(getRoundBG("#3cca59"));
        chatArea.setBackground(getRoundBG("#027bff"));
        navigateAreaUP.setBackground(getRoundBG("#ffa60a"));

        dropCancel.setBackground(getRoundBG("#d20000"));
        dropAllAreaUP.setBackground(getRoundBG("#d20000"));

        callArea.setOnClickListener(new setOnClickAct());
        chatArea.setOnClickListener(new setOnClickAct());

        // Multi delivery View
        deliveryDetailsArea = (LinearLayout) findViewById(R.id.deliveryDetailsArea);
        pickupTxt = (MTextView) findViewById(R.id.pickupTxt);
        pickupNameTxt = (MTextView) findViewById(R.id.pickupNameTxt);
        recipientTxt = (MTextView) findViewById(R.id.recipientTxt);
        personTxt = (MTextView) findViewById(R.id.personTxt);
        iv_callRicipient = (ImageView) findViewById(R.id.iv_callRicipient);
        iv_callRicipient.setOnClickListener(new setOnClickList());
        viewDetailsImgView = (ImageView) findViewById(R.id.viewDetailsImgView);
        viewDetailsImgView.setOnClickListener(new setOnClickList());
        generalFunc.storeData(Utils.IsTripStarted, "No");

        currencetprice = data_trip.get("fVisitFee");

        new CreateRoundedView(getResources().getColor(android.R.color.transparent), Utils.dipToPixels(getActContext(), 15), 0,
                Color.parseColor("#00000000"), user_img);

        /*Multitop over*/
        dropAllIconArea = (RelativeLayout) findViewById(R.id.dropAllArea);
        dropAllIconArea.setOnClickListener(new setOnClickList());
        /*Multistop over*/

        setTripButton();
        setLabels();
        setData();

        String last_trip_data = generalFunc.getJsonValue("TripDetails", userProfileJsonObj.toString());
        if (REQUEST_TYPE.equalsIgnoreCase(Utils.eType_Multi_Delivery)) {
            // googleImage.setVisibility(View.GONE);
            // ((ImageView) findViewById(R.id.logoutImageview)).setVisibility(View.VISIBLE);
            // ((ImageView) findViewById(R.id.logoutImageview)).setImageDrawable(getActContext().getResources().getDrawable(R.drawable.ic_waybill));
            //  ((ImageView) findViewById(R.id.logoutImageview)).setOnClickListener(new setOnClickAct());


            wayBillImgView.setVisibility(View.VISIBLE);
            deliveryInfoView.setVisibility(View.VISIBLE);
            wayBillImgView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Bundle bn4 = new Bundle();
                    bn4.putSerializable("data_trip", data_trip);
                    new StartActProcess(getActContext()).startActWithData(WayBillActivity.class, bn4);
                }
            });

            chatArea.setVisibility(View.GONE);
            chatview.setVisibility(View.GONE);

        } else if (!REQUEST_TYPE.equalsIgnoreCase(Utils.eType_Multi_Delivery)) {

            if (REQUEST_TYPE.equalsIgnoreCase(Utils.CabGeneralType_Ride) && Utils.checkText(generalFunc.getJsonValue("iStopId", last_trip_data))) {
                deliveryInfoView.setVisibility(View.VISIBLE);
            }
            Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(mToolbar);
        }


        deliveryInfoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (REQUEST_TYPE.equalsIgnoreCase(Utils.CabGeneralType_Ride) && Utils.checkText(data_trip.get("iStopId"))) {
                    Bundle bn = new Bundle();
                    bn.putString("TripId", data_trip.get("TripId"));
                    bn.putString("Status", "activeTrip");
                    bn.putSerializable("TRIP_DATA", data_trip);
                    new StartActProcess(getActContext()).startActWithData(ViewStopOverDetailsActivity.class, bn);

                } else {
                    Bundle bn = new Bundle();
                    bn.putString("TripId", data_trip.get("TripId"));
                    bn.putString("Status", "activeTrip");
                    bn.putSerializable("TRIP_DATA", data_trip);
                    new StartActProcess(getActContext()).startActWithData(ViewMultiDeliveryDetailsActivity.class, bn);
                }

            }
        });


        int appThemeColor_2 = getActContext().getResources().getColor(R.color.appThemeColor_2);

        new CreateRoundedView(appThemeColor_2, Utils.dipToPixels(getActContext(), 40), 0, appThemeColor_2, findViewById(R.id.driverImgView));

        new CreateRoundedView(Color.parseColor("#000000"), Utils.dipToPixels(getActContext(), 60), 2, 0, findViewById(R.id.slideback));

        map.getMapAsync(this);

        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) titleTxt.getLayoutParams();
        params.setMargins(Utils.dipToPixels(getActContext(), 20), 0, 0, 0);
        titleTxt.setLayoutParams(params);


        if (savedInstanceState != null) {
            // Restore value of members from saved state
            String restratValue_str = savedInstanceState.getString("RESTART_STATE");

            if (restratValue_str != null && !restratValue_str.equals("") && restratValue_str.trim().equals("true")) {
                generalFunc.restartApp();
            }
        }
        if (generalFunc.isRTLmode()) {
            (findViewById(R.id.navStripImgView)).setRotation(180);
            (findViewById(R.id.imageslide)).setRotationY(180);
        }

        String OPEN_CHAT = generalFunc.retrieveValue("OPEN_CHAT");
        if (Utils.checkText(OPEN_CHAT)) {
            JSONObject OPEN_CHAT_DATA_OBJ = generalFunc.getJsonObject(OPEN_CHAT);
            generalFunc.removeValue("OPEN_CHAT");

            if (OPEN_CHAT_DATA_OBJ != null) {
                new StartActProcess(getActContext()).startActWithData(ChatActivity.class, generalFunc.createChatBundle(OPEN_CHAT_DATA_OBJ));
            }
        }
        GetLocationUpdates.getInstance().setTripStartValue(true, true, data_trip.get("TripId"));


    }

    private void setTripButton() {
        startTripSlideButton = findViewById(R.id.startTripSlideButton);
        endTripSlideButton = findViewById(R.id.endTripSlideButton);

        startTripSlideButton.setBackgroundColor(getResources().getColor(R.color.appThemeColor_1));
        endTripSlideButton.setBackgroundColor(getResources().getColor(R.color.red));

        startTripSlideButton.onClickListener(isCompleted -> {
            if (isCompleted) {
                if (data_trip != null && data_trip.get("eBeforeUpload").equalsIgnoreCase("Yes")) {
                    takeAndUploadPic(getActContext(), "before");
                } else {
                    setTripStart();
                }
            }
        });

        endTripSlideButton.onClickListener(isCompleted -> {
            if (isCompleted) {
                if (REQUEST_TYPE.equals("Deliver")) {
                    buildMsgOnDeliveryEnd();
                } else {
                    if (data_trip != null && data_trip.get("eAfterUpload").equalsIgnoreCase("Yes")) {
                        takeAndUploadPic(getActContext(), "after");
                    } else {
                        if (eType.equals("UberX")) {
                            endTrip();
                        } else {
                            if (!eType.equals("")) {
                                if (eType.equals("UberX")) {
                                    endTrip();
                                } else {
                                    endTrip();
                                }
                            } else {
                                endTrip();
                            }
                        }
                    }
                }
            }
        });
    }

    public void setTimetext(String distance, String time) {
        try {
            JSONObject userProfileJsonObj = generalFunc.getJsonObject(generalFunc.retrieveValue(Utils.USER_PROFILE_JSON));

            if (!APP_TYPE.equalsIgnoreCase("UberX")) {

                timeTxt.setVisibility(View.VISIBLE);
                if (userProfileJsonObj != null && !generalFunc.getJsonValueStr("eUnit", userProfileJsonObj).equalsIgnoreCase("KMs")) {
                    distanceTxt.setText(generalFunc.convertNumberWithRTL(distance) + " " + generalFunc.retrieveLangLBl("", "LBL_MILE_DISTANCE_TXT") + " ");
                    timeTxt.setText(generalFunc.convertNumberWithRTL(time) + " ");


                    //   timeTxt.setText(time + " " + LBL_REACH_TXT + " & " + distance + " " + generalFunc.retrieveLangLBl("", "LBL_MILE_DISTANCE_TXT") + " " + //generalFunc.retrieveLangLBl("away", "LBL_AWAY_TXT"));
                } else {
                    distanceTxt.setText(generalFunc.convertNumberWithRTL(distance) + " " + generalFunc.retrieveLangLBl("", "LBL_KM_DISTANCE_TXT") + " ");
                    timeTxt.setText(generalFunc.convertNumberWithRTL(time) + " ");

                    //  timeTxt.setText(time + " " + LBL_REACH_TXT + " & " + distance + " " + generalFunc.retrieveLangLBl("", "LBL_KM_DISTANCE_TXT") + " " + //generalFunc.retrieveLangLBl("away", "LBL_AWAY_TXT"));
                }

            } else {
                if (data_trip.get("eFareType").equalsIgnoreCase(Utils.CabFaretypeRegular)) {
                    timeTxt.setVisibility(View.VISIBLE);
                } else {
                    timeTxt.setVisibility(View.GONE);
                }
            }
        } catch (Exception e) {

        }

    }

    public void handleNoLocationDial() {
        if (generalFunc.isLocationEnabled()) {
            resetData();
        }
    }

    private void resetData() {
        if (intCheck.isNetworkConnected() && intCheck.check_int() && addressTxt.getText().equals(generalFunc.retrieveLangLBl("Loading address", "LBL_LOAD_ADDRESS"))) {
            setData();
        }

        if (!isCurrentLocationFocused) {
            setData();
            checkUserLocation();
        } else {
            checkUserLocation();
        }
    }

    public void internetIsBack() {
        if (updateDirections != null) {
            updateDirections.scheduleDirectionUpdate();
        }
    }

    public void checkUserLocation() {
        if (generalFunc.isLocationEnabled() && (userLocation == null || userLocation.getLatitude() == 0.0 || userLocation.getLongitude() == 0.0)) {
            showprogress();
        } else {
            hideprogress();
        }
    }

    public void showprogress() {
        isCurrentLocationFocused = false;
        findViewById(R.id.errorLocArea).setVisibility(View.VISIBLE);
        // googleImage.setVisibility(View.GONE);


        findViewById(R.id.mProgressBar).setVisibility(View.VISIBLE);
        ((ProgressBar) findViewById(R.id.mProgressBar)).setIndeterminate(true);
        ((ProgressBar) findViewById(R.id.mProgressBar)).getIndeterminateDrawable().setColorFilter(
                getActContext().getResources().getColor(R.color.appThemeColor_1), android.graphics.PorterDuff.Mode.SRC_IN);

    }

    public void hideprogress() {

        findViewById(R.id.errorLocArea).setVisibility(View.GONE);

        if (findViewById(R.id.mProgressBar) != null) {
            findViewById(R.id.mProgressBar).setVisibility(View.GONE);
        }

        if (data_trip != null && (data_trip.get("eFareType").equals(Utils.CabFaretypeFixed) || data_trip.get("eFareType").equals(Utils.CabFaretypeHourly))) {
            googleImage.setVisibility(View.GONE);
        } else {
            if (eType.equalsIgnoreCase(Utils.CabGeneralType_Ride)) {
                if (!isPoolRide && !Utils.checkText(data_trip.get("iStopId"))) {
                    //googleImage.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {

        outState.putString("RESTART_STATE", "true");
        outState.putParcelable("file_uri", fileUri);
        super.onSaveInstanceState(outState);
    }

    public void setLabels() {

        LBL_REACH_TXT = generalFunc.retrieveLangLBl("to reach", "LBL_REACH_TXT");
        LBL_CONFIRM_STOPOVER_1 = generalFunc.retrieveLangLBl("", "LBL_CONFIRM_STOPOVER_1");
        LBL_CONFIRM_STOPOVER_2 = generalFunc.retrieveLangLBl("", "LBL_CONFIRM_STOPOVER_2");
        LBL_BTN_SLIDE_END_TRIP_TXT = generalFunc.retrieveLangLBl("", "LBL_BTN_SLIDE_END_TRIP_TXT");

        titleTxt.setText(generalFunc.retrieveLangLBl("En Route", "LBL_EN_ROUTE_TXT"));
        timeTxt.setText("--" + LBL_REACH_TXT);
        required_str = generalFunc.retrieveLangLBl("", "LBL_FEILD_REQUIRD");
        invalid_str = generalFunc.retrieveLangLBl("Invalid value", "LBL_DIGIT_REQUIRE");

        ((MTextView) findViewById(R.id.placeTxtView)).setText(generalFunc.retrieveLangLBl("", "LBL_ADD_DESTINATION_BTN_TXT"));
        ((MTextView) findViewById(R.id.navigateTxt)).setText(generalFunc.retrieveLangLBl("Navigate", "LBL_NAVIGATE"));

        timerHinttext.setText(generalFunc.retrieveLangLBl("JOB TIMER", "LBL_JOB_TIMER_HINT"));
        progressHinttext.setText(generalFunc.retrieveLangLBl("JOB PROGRESS", "LBL_JOB_PROGRESS"));

        txt_TimerHour.setText(generalFunc.retrieveLangLBl("", "LBL_HOUR_TXT"));
        txt_TimerMinute.setText(generalFunc.retrieveLangLBl("", "LBL_MINUTES_TXT"));
        txt_TimerSecond.setText(generalFunc.retrieveLangLBl("", "LBL_SECONDS_TXT"));

        tollTxtView.setText(generalFunc.retrieveLangLBl("", "LBL_TOLL_SKIP_HELP"));

        //  newbtn_timer.setText(LBL_WAIT);


        if (eType.equalsIgnoreCase(Utils.CabGeneralType_Ride)) {

            startTripSlideButton.setButtonText(generalFunc.retrieveLangLBl("", "LBL_BTN_SLIDE_BEGIN_TRIP_TXT"));

            if (Utils.checkText(data_trip.get("iStopId")) && currentStopOverPoint < totalStopOverPoint) {
                endTripSlideButton.setButtonText(LBL_CONFIRM_STOPOVER_1 + " " + LBL_CONFIRM_STOPOVER_2 + " " + generalFunc.convertNumberWithRTL(data_trip.get("currentStopOverPoint")));
            } else {
                endTripSlideButton.setButtonText(LBL_BTN_SLIDE_END_TRIP_TXT);
            }
        } else if (eType.equalsIgnoreCase(Utils.CabGeneralType_UberX)) {
            startTripSlideButton.setButtonText(generalFunc.retrieveLangLBl("", "LBL_BTN_SLIDE_BEGIN_JOB_TXT"));
            endTripSlideButton.setButtonText(generalFunc.retrieveLangLBl("", "LBL_BTN_SLIDE_END_JOB_TXT"));
            bottomArea.setVisibility(View.GONE);
        } else {
            startTripSlideButton.setButtonText(generalFunc.retrieveLangLBl("", "LBL_SLIDE_BEGIN_DELIVERY"));
            if (Utils.checkText(data_trip.get("iStopId")) && currentStopOverPoint < totalStopOverPoint) {
                endTripSlideButton.setButtonText((LBL_CONFIRM_STOPOVER_1 + " " + LBL_CONFIRM_STOPOVER_2 + " " + generalFunc.convertNumberWithRTL(data_trip.get("currentStopOverPoint"))));
            } else {
                endTripSlideButton.setButtonText(generalFunc.retrieveLangLBl("", "LBL_SLIDE_END_DELIVERY"));
            }
            bottomArea.setVisibility(View.GONE);
            googleImage.setVisibility(View.GONE);
        }

        setButtonName();
        ((MTextView) findViewById(R.id.errorTitleTxt)).setText(generalFunc.retrieveLangLBl("Waiting for your location.", "LBL_LOCATION_FATCH_ERROR_TXT"));

        ((MTextView) findViewById(R.id.errorSubTitleTxt)).setText(generalFunc.retrieveLangLBl("Try to fetch  your accurate location. \"If you still face the problem, go to open sky instead of closed area\".", "LBL_NO_LOC_GPS_TXT"));
    }

    public void setButtonName() {
        String last_trip_data = generalFunc.getJsonValue("TripDetails", userProfileJsonObj.toString());
        currentStopOverPoint = generalFunc.parseIntegerValue(0, generalFunc.getJsonValue("currentStopOverPoint", last_trip_data));
        totalStopOverPoint = generalFunc.parseIntegerValue(0, generalFunc.getJsonValue("totalStopOverPoint", last_trip_data));
        if (generalFunc.getJsonValue("eServiceLocation", last_trip_data) != null && generalFunc.getJsonValue("eServiceLocation", last_trip_data).equalsIgnoreCase("Driver")) {
            (findViewById(R.id.navigationViewArea)).setVisibility(View.GONE);
        }


        if (REQUEST_TYPE.equalsIgnoreCase(Utils.eType_Multi_Delivery) || REQUEST_TYPE.equalsIgnoreCase(Utils.CabGeneralType_Ride) && Utils.checkText(generalFunc.getJsonValue("iStopId", last_trip_data))) {
            slideback.setVisibility(View.GONE);
            imageslide.setVisibility(View.GONE);

            if (REQUEST_TYPE.equalsIgnoreCase(Utils.CabGeneralType_Ride) && Utils.checkText(generalFunc.getJsonValue("iStopId", last_trip_data))) {
                findViewById(R.id.iv_callRicipient).setVisibility(View.GONE);
                pickupTxt.setVisibility(View.VISIBLE);
                pickupTxt.setText(data_trip.get("PName") + " ");
                pickupNameTxt.setText(generalFunc.retrieveLangLBl("", "LBL_STOP_OVER_TITLE_TXT") + " " +
                        generalFunc.convertNumberWithRTL(""+currentStopOverPoint)
                        + " " + generalFunc.retrieveLangLBl("", "LBL_STOP_OVER_OUT_OF") + " " + generalFunc.convertNumberWithRTL(""+totalStopOverPoint));

                pickupNameTxt.setVisibility(View.VISIBLE);
                ratingBar.setRating(generalFunc.parseFloatValue(0, data_trip.get("PRating")));
                ratingBar.setVisibility(View.VISIBLE);

            }
        } else if (generalFunc.getJsonValue("ePoolRide", last_trip_data).equalsIgnoreCase("Yes")) {
            pickupTxt.setText(generalFunc.retrieveLangLBl("", "LBL_ADMIN_DROPOFF"));
            pickupTxt.setVisibility(View.VISIBLE);
            pickupNameTxt.setText(data_trip.get("PName") + " " + data_trip.get("vLastName"));
            findViewById(R.id.iv_callRicipient).setVisibility(View.GONE);
            personTxt.setVisibility(View.VISIBLE);
            personTxt.setText(generalFunc.convertNumberWithRTL(generalFunc.getJsonValue("iPersonSize", last_trip_data)) + " " + generalFunc.retrieveLangLBl("", "LBL_PERSON"));
            personTxt.setVisibility(View.VISIBLE);
            isPoolRide = true;
            deliveryDetailsArea.setVisibility(View.VISIBLE);
            ConfigPubNub.getInstance().subscribeToCabRequestChannel();
            slideback.setVisibility(View.GONE);
            imageslide.setVisibility(View.GONE);
            googleImage.setVisibility(View.GONE);


        }

        if (REQUEST_TYPE.equalsIgnoreCase(Utils.CabGeneralType_Ride)) {
            startTripSlideButton.setButtonText(generalFunc.retrieveLangLBl("", "LBL_BTN_SLIDE_BEGIN_TRIP_TXT"));
            if (Utils.checkText(data_trip.get("iStopId")) && currentStopOverPoint < totalStopOverPoint) {
                endTripSlideButton.setButtonText(LBL_CONFIRM_STOPOVER_1 + " " + LBL_CONFIRM_STOPOVER_2 + " " + generalFunc.convertNumberWithRTL(data_trip.get("currentStopOverPoint")));
            } else {
                endTripSlideButton.setButtonText(LBL_BTN_SLIDE_END_TRIP_TXT);
            }
        } else if (REQUEST_TYPE.equalsIgnoreCase(Utils.CabGeneralType_UberX)) {
            startTripSlideButton.setButtonText(generalFunc.retrieveLangLBl("", "LBL_BTN_SLIDE_BEGIN_JOB_TXT"));
            endTripSlideButton.setButtonText(generalFunc.retrieveLangLBl("", "LBL_BTN_SLIDE_END_JOB_TXT"));
        } else {
            startTripSlideButton.setButtonText(generalFunc.retrieveLangLBl("", "LBL_SLIDE_BEGIN_DELIVERY"));
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        this.gMap = googleMap;
        if (generalFunc.checkLocationPermission(true) == true) {
            getMap().setMyLocationEnabled(false);
        }

        if (generalFunc.isRTLmode()) {
            getMap().setPadding(13, 0, 0, Utils.dpToPx(150, getActContext()));
        } else {
            //getMap().setPadding(13, 0, 150, 0);
            getMap().setPadding(13, 0, 0, Utils.dpToPx(150, getActContext()));

        }

//        if(eType.equalsIgnoreCase(Utils.CabGeneralType_Ride)) {
//
//
//            if (generalFunc.isRTLmode()) {
//                getMap().setPadding(13, Utils.dipToPixels(getActContext(),200), 0, 0);
//            } else {
//                getMap().setPadding(13, 0, 0, Utils.dipToPixels(getActContext(),150));
//
//            }
//        }


        getMap().getUiSettings().setTiltGesturesEnabled(false);
        getMap().getUiSettings().setCompassEnabled(false);
        getMap().getUiSettings().setMyLocationButtonEnabled(false);

        if (isDestinationAdded == true) {
            addDestinationMarker();
        }

        if (isDestinationAdded == true && userLocation != null && route_polyLine == null) {
//            drawRoute("" + destLocLatitude, "" + destLocLongitude);
            if (updateDirections != null) {
                Location destLoc = new Location("gps");
                destLoc.setLatitude(destLocLatitude);
                destLoc.setLongitude(destLocLongitude);
                updateDirections.changeUserLocation(destLoc);
            }
        }

        getMap().setOnMarkerClickListener(marker -> {
            marker.hideInfoWindow();
            return true;
        });

        checkUserLocation();

        if (GetLocationUpdates.retrieveInstance() != null) {
            GetLocationUpdates.getInstance().stopLocationUpdates(this);
        }
        GetLocationUpdates.getInstance().setTripStartValue(true, true, data_trip.get("TripId"));
        GetLocationUpdates.getInstance().startLocationUpdates(this, this);
    }

    public void addDestinationMarker() {
        try {
            if (getMap() == null) {
                return;
            }
            if (destLocMarker != null) {
                destLocMarker.remove();

            }
            if (route_polyLine != null) {
                route_polyLine.remove();
            }

            MarkerOptions markerOptions_destLocation = new MarkerOptions();
            markerOptions_destLocation.position(new LatLng(destLocLatitude, destLocLongitude));
            markerOptions_destLocation.icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_dest_marker)).anchor(0.5f,
                    0.5f);
            destLocMarker = getMap().addMarker(markerOptions_destLocation);
        } catch (Exception e) {

        }
    }

    public void addSourceMarker() {
        if (getMap() == null) {
            return;
        }
        double latitude = generalFunc.parseDoubleValue(0.0, data_trip.get("sourceLatitude"));
        double longitude = generalFunc.parseDoubleValue(0.0, data_trip.get("sourceLongitude"));
        MarkerOptions markerOptions_destLocation = new MarkerOptions();
        markerOptions_destLocation.position(new LatLng(latitude, longitude));
        markerOptions_destLocation.icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_source_marker)).anchor(0.5f,
                0.5f);
        getMap().addMarker(markerOptions_destLocation);
    }

    public GoogleMap getMap() {
        return this.gMap;
    }

    private void setDriverDetail() {

        String image_url = CommonUtilities.USER_PHOTO_PATH + tripDetail.get(0).get("iDriverId") + "/"
                + tripDetail.get(0).get("driverImage");


        Picasso.get()
                .load(image_url)
                .placeholder(R.mipmap.ic_no_pic_user)
                .error(R.mipmap.ic_no_pic_user)
                .into(((ImageView) findViewById(R.id.user_img)));

        userNameTxt.setText(tripDetail.get(0).get("driverName"));
        userAddressTxt.setText(tripDetail.get(0).get("tSaddress"));
        float ratinguser = generalFunc.parseFloatValue(0, tripDetail.get(0).get("driverRating"));
        Log.d("ratinguser", "setDriverDetail: " + ratinguser);
        ratingBar_ufx.setRating(generalFunc.parseFloatValue(0, tripDetail.get(0).get("driverRating")));

    }

    public void setData() {

        tripId = data_trip.get("TripId");
        eType = data_trip.get("REQUEST_TYPE");
        if (generalFunc.getJsonValue("ENABLE_INTRANSIT_SHOPPING_SYSTEM", userProfileJsonObj).equals("Yes") && eType.equalsIgnoreCase(Utils.CabGeneralType_Ride) &&
                !data_trip.get("vTripStatus").equals("Arrived") && !data_trip.get("eRental").equalsIgnoreCase("Yes") && !data_trip.get("ePoolRide").equalsIgnoreCase("Yes")
                && data_trip.get("eTransit").equalsIgnoreCase("Yes")) {

            transitConfigTripStartView();

        }

        if (!data_trip.get("PPicName").equals("")) {
            vImage = CommonUtilities.USER_PHOTO_PATH + data_trip.get("PassengerId") + "/"
                    + data_trip.get("PPicName");
        }
        vName = data_trip.get("PName");


        deliveryVerificationCode = data_trip.get("vDeliveryConfirmCode");

        if (eType.equalsIgnoreCase(Utils.eType_Multi_Delivery)) {
            recipientTxt.setText(data_trip.get("Running_Delivery_Txt"));
            pickupNameTxt.setText(data_trip.get("vReceiverName"));
            pickupNameTxt.setVisibility(View.VISIBLE);
            recipientTxt.setVisibility(View.VISIBLE);
            iTripDeliveryLocationId = data_trip.get("iTripDeliveryLocationId");
        }
        if (eType.equalsIgnoreCase("Deliver")) {
            pickupNameTxt.setText(data_trip.get("PName") + " ");
            pickupNameTxt.setVisibility(View.VISIBLE);
            ratingBar.setRating(generalFunc.parseFloatValue(0, data_trip.get("PRating")));
            ratingBar.setVisibility(View.VISIBLE);
            deliveryDetailsArea.setVisibility(View.VISIBLE);

        }
        if (eType.equalsIgnoreCase(Utils.CabGeneralType_Ride)) {
            pickupNameTxt.setText(data_trip.get("PName") + " ");
            pickupNameTxt.setVisibility(View.VISIBLE);
            ratingBar.setRating(generalFunc.parseFloatValue(0, data_trip.get("PRating")));
            ratingBar.setVisibility(View.VISIBLE);
            deliveryDetailsArea.setVisibility(View.VISIBLE);
        }


        String DestLocLatitude = data_trip.get("DestLocLatitude");
        String DestLocLongitude = data_trip.get("DestLocLongitude");
        if (!DestLocLatitude.equals("") && !DestLocLatitude.equals("0") && !DestLocLongitude.equals("") && !DestLocLongitude.equals("0")) {
            setDestinationPoint(DestLocLatitude, DestLocLongitude, data_trip.get("DestLocAddress"), true);
            (findViewById(R.id.destLocSearchArea)).setVisibility(View.GONE);

        } else {
            (findViewById(R.id.destLocSearchArea)).setOnClickListener(new setOnClickAct());
            (findViewById(R.id.destLocSearchArea)).setVisibility(View.VISIBLE);

            tollTxtView.setVisibility(View.GONE);
            if (data_trip.get("REQUEST_TYPE").equalsIgnoreCase("UberX")) {
                destLocSearchArea.setVisibility(View.GONE);
                deliveryDetailsArea.setVisibility(View.GONE);
            } else {
                (findViewById(R.id.navigationViewArea)).setVisibility(View.VISIBLE);
            }
        }

        if (APP_TYPE.equalsIgnoreCase("UberX")) {
            (findViewById(R.id.destLocSearchArea)).setVisibility(View.GONE);
            (findViewById(R.id.navigationViewArea)).setVisibility(View.GONE);
            tollTxtView.setVisibility(View.GONE);
            deliveryDetailsArea.setVisibility(View.GONE);
            slideback.setVisibility(View.GONE);
            imageslide.setVisibility(View.GONE);
        }

        if (!data_trip.get("vTripStatus").equals("Arrived")) {
            startTripSlideButton.setVisibility(View.GONE);
            endTripSlideButton.setVisibility(View.VISIBLE);

            if (eType.equalsIgnoreCase(Utils.CabGeneralType_Ride) && Utils.checkText(data_trip.get("iStopId")) && currentStopOverPoint < totalStopOverPoint) {
                dropAllIconArea.setVisibility(View.VISIBLE);
            }
            // (findViewById(R.id.navigateArea)).setVisibility(View.VISIBLE);
            isendslide = true;
            invalidateOptionsMenu();
            imageslide.setImageResource(R.mipmap.ic_trip_btn);

            configTripStartView();

            if (data_trip.get("eFareType").equals(Utils.CabFaretypeHourly)) {

                Log.e("countdownstartCalled", ":: 1");
                countDownStart();
                btntimer.setVisibility(View.VISIBLE);
                if (data_trip.get("TimeState") != null && !data_trip.get("TimeState").equals("")) {
                    if (data_trip.get("TimeState").equalsIgnoreCase("Resume")) {

                        isresume = true;
                        btntimer.setText(generalFunc.retrieveLangLBl("pause", "LBL_PAUSE_TEXT"));
                        btntimer.setVisibility(View.VISIBLE);

                    } else {
                        if (timerrequesttask != null) {
                            timerrequesttask.stopRepeatingTask();
                            timerrequesttask = null;
                        }

                        isresume = false;
                        btntimer.setText(generalFunc.retrieveLangLBl("resume", "LBL_RESUME_TEXT"));
                        btntimer.setVisibility(View.VISIBLE);

                    }
                }

                if (data_trip.get("TotalSeconds") != null && !data_trip.get("TotalSeconds").equals("")) {
                    i = Integer.parseInt(data_trip.get("TotalSeconds"));
                    setTimerValues();

                }
                if (data_trip.get("iTripTimeId") != null && !data_trip.get("iTripTimeId").equals("")) {
                    TripTimeId = data_trip.get("iTripTimeId");
                    //  countDownStart();
                }
            }

        }

        REQUEST_TYPE = data_trip.get("REQUEST_TYPE");
        SITE_TYPE = data_trip.get("SITE_TYPE");
        deliveryVerificationCode = data_trip.get("vDeliveryConfirmCode");

        setButtonName();
        if (data_trip.get("REQUEST_TYPE").equalsIgnoreCase(Utils.CabGeneralType_UberX)) {
            getTripDeliveryLocations();
            if (data_trip.get("eFareType").equals(Utils.CabFaretypeRegular)) {
                timerarea.setVisibility(View.GONE);
                scrollview.setVisibility(View.GONE);
                timerlayoutarea.setVisibility(View.GONE);
                timerlayoutMainarea.setVisibility(View.GONE);
            } else if (data_trip.get("eFareType").equals(Utils.CabFaretypeFixed)) {
                timerarea.setVisibility(View.VISIBLE);
                googleImage.setVisibility(View.GONE);
                scrollview.setVisibility(View.VISIBLE);
                timerlayoutarea.setVisibility(View.GONE);
                timerlayoutMainarea.setVisibility(View.GONE);
                emeTapImgView.setVisibility(View.GONE);
                // btntimer.setVisibility(View.GONE);
            } else if (data_trip.get("eFareType").equals(Utils.CabFaretypeHourly)) {
                // btntimer.setVisibility(View.VISIBLE);
                timerarea.setVisibility(View.VISIBLE);
                googleImage.setVisibility(View.GONE);
                scrollview.setVisibility(View.VISIBLE);
                emeTapImgView.setVisibility(View.GONE);
                timerlayoutarea.setVisibility(View.VISIBLE);
                timerlayoutMainarea.setVisibility(View.VISIBLE);

            } else {
                timerarea.setVisibility(View.GONE);
            }
        } else {
            try {

                timerarea.setVisibility(View.GONE);
                scrollview.setVisibility(View.GONE);
                timerlayoutarea.setVisibility(View.GONE);
                timerlayoutMainarea.setVisibility(View.GONE);
                emeTapImgView.setVisibility(View.VISIBLE);
            } catch (Exception e) {

            }
        }
    }

    @Override
    public void onLocationUpdate(Location location) {

        if (location == null) {
            return;
        }
        if (gMap == null) {
            this.userLocation = location;
            return;
        }

        updateDriverMarker(new LatLng(location.getLatitude(), location.getLongitude()));

        this.userLocation = location;


        String DestLocLatitude = data_trip.get("DestLocLatitude");
        String DestLocLongitude = data_trip.get("DestLocLongitude");


        if (!ENABLE_DIRECTION_SOURCE_DESTINATION_DRIVER_APP.equalsIgnoreCase("Yes")) {
            if (!DestLocLatitude.equals("") && !DestLocLatitude.equals("0") && !DestLocLongitude.equals("") && !DestLocLongitude.equals("0")) {
                double passenger_lat = GeneralFunctions.parseDoubleValue(0.0, DestLocLatitude);
                double passenger_lon = GeneralFunctions.parseDoubleValue(0.0, DestLocLongitude);
                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                builder.include(new LatLng(userLocation.getLatitude(), userLocation.getLongitude()));
                builder.include(new LatLng(passenger_lat, passenger_lon));
                gMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), Utils.dipToPixels(getActContext(), 40)));
            }
        }

        if (ENABLE_DIRECTION_SOURCE_DESTINATION_DRIVER_APP.equalsIgnoreCase("Yes") || (DestLocLatitude.equals("") || DestLocLatitude.equals("0") || DestLocLongitude.equals("") || DestLocLongitude.equals("0"))) {
            try {
                if (isFirstMapMove) {
                    //getMap().moveCamera(CameraUpdateFactory.newCameraPosition(cameraForUserPosition(location, true)));
                    getMap().moveCamera((new AppFunctions(getActContext())).getCameraPosition(location, gMap));
                    isFirstMapMove = false;
                } else {
                    // getMap().animateCamera(CameraUpdateFactory.newCameraPosition(cameraForUserPosition(location, false)), 1000, null);
                    getMap().animateCamera((new AppFunctions(getActContext())).getCameraPosition(location, gMap), 1000, null);
                }
            } catch (Exception e) {

            }
        }

        checkUserLocation();

        if (data_trip.get("REQUEST_TYPE").equalsIgnoreCase(Utils.CabGeneralType_UberX)) {
            String eFareType = data_trip.get("eFareType");
            if (eFareType.equals(Utils.CabFaretypeRegular)) {
                if (updateDirections == null) {
                    Location destLoc = new Location("temp");
                    destLoc.setLatitude(destLocLatitude);
                    destLoc.setLongitude(destLocLongitude);
                    updateDirections = new UpdateDirections(getActContext(), gMap, userLocation, destLoc);
                  //  updateDirections.scheduleDirectionUpdate();
                }

            } else if (eFareType.equals(Utils.CabFaretypeFixed)) {
                //    timeTxt.setVisibility(View.GONE);
                return;

            } else if (eFareType.equals(Utils.CabFaretypeHourly)) {
                // timeTxt.setVisibility(View.GONE);
                return;
            } else {
                if (updateDirections == null) {
                    Location destLoc = new Location("temp");
                    destLoc.setLatitude(destLocLatitude);
                    destLoc.setLongitude(destLocLongitude);


                    updateDirections = new UpdateDirections(getActContext(), gMap, userLocation, destLoc);
                    updateDirections.scheduleDirectionUpdate();
                }

            }
        } else {
            if (updateDirections == null) {
                Location destLoc = new Location("temp");
                destLoc.setLatitude(destLocLatitude);
                destLoc.setLongitude(destLocLongitude);


                updateDirections = new UpdateDirections(getActContext(), gMap, userLocation, destLoc);
                if (eFly && updateDirections != null) {
                    double sourcelatitude = GeneralFunctions.parseDoubleValue(0.0, data_trip.get("sourceLatitude"));
                    double sourcelongitude = GeneralFunctions.parseDoubleValue(0.0, data_trip.get("sourceLongitude"));

                    updateDirections.iseFly(eFly, new LatLng(sourcelatitude, sourcelongitude));
                }
                updateDirections.scheduleDirectionUpdate();
            }
        }

        if (updateDirections != null) {
            updateDirections.changeUserLocation(location);
        }

    }

    public static Bitmap createDrawableFromView(Context context, View view) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        view.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT));
        view.measure(displayMetrics.widthPixels, displayMetrics.heightPixels);
        view.layout(0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels);
        view.buildDrawingCache();
        Bitmap bitmap = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(), Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);

        return bitmap;
    }


    public void updateDriverMarker(final LatLng newLocation) {

        if (MyApp.getInstance().isMyAppInBackGround() || gMap == null) {
            return;
        }

        String APP_TYPE = generalFunc.getJsonValueStr("APP_TYPE", userProfileJsonObj);
        boolean isUberX = APP_TYPE.equalsIgnoreCase("UberX") || eType.equalsIgnoreCase(Utils.CabGeneralType_UberX);
        if (driverMarker == null) {

            if (isUberX) {

                String image_url = CommonUtilities.PROVIDER_PHOTO_PATH + generalFunc.getMemberId() + "/" + generalFunc.getJsonValueStr("vImage", userProfileJsonObj);
                View marker_view = ((LayoutInflater) getActContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                        .inflate(R.layout.uberx_provider_maker_design, null);
                SelectableRoundedImageView providerImgView = (SelectableRoundedImageView) marker_view
                        .findViewById(R.id.providerImgView);

                final View finalMarker_view = marker_view;

                providerImgView.setImageResource(R.mipmap.ic_no_pic_user);

                if (Utils.checkText(generalFunc.getJsonValueStr("vImage", userProfileJsonObj))) {

                    MarkerOptions markerOptions_driver = new MarkerOptions();
                    markerOptions_driver.position(newLocation);
                    markerOptions_driver.icon(BitmapDescriptorFactory.fromBitmap(createDrawableFromView(getActContext(), finalMarker_view))).anchor(0.5f,
                            0.5f).flat(true);
                    driverMarker = gMap.addMarker(markerOptions_driver);

                    Picasso.get()
                            .load(image_url/*"http://www.hellocle.com/wp-content/themes/hello/images/hello-logo-stone.png"*/)
                            .placeholder(R.mipmap.ic_no_pic_user).error(R.mipmap.ic_no_pic_user)
                            .into(providerImgView, new com.squareup.picasso.Callback() {
                                @Override
                                public void onSuccess() {
                                    driverMarker.setIcon(BitmapDescriptorFactory.fromBitmap(createDrawableFromView(getActContext(), finalMarker_view)));
                                }

                                @Override
                                public void onError(Exception e) {
                                    driverMarker.setIcon(BitmapDescriptorFactory.fromBitmap(createDrawableFromView(getActContext(), finalMarker_view)));
                                }
                            });

                    driverMarker.setFlat(false);
                    driverMarker.setAnchor(0.5f, 1);
                    driverMarker.setTitle(generalFunc.getMemberId());
                } else {
                    MarkerOptions markerOptions_driver = new MarkerOptions();
                    markerOptions_driver.position(newLocation);
                    markerOptions_driver.icon(BitmapDescriptorFactory.fromBitmap(createDrawableFromView(getActContext(), finalMarker_view))).anchor(0.5f,
                            1.5f).flat(true);
                    driverMarker = gMap.addMarker(markerOptions_driver);
                    driverMarker.setFlat(false);
                    driverMarker.setAnchor(0.5f, 1);
                    driverMarker.setTitle(generalFunc.getMemberId());

                }
            } else {
                int iconId = R.mipmap.car_driver;

                String vVehicleType = data_trip.containsKey("vVehicleType") ? data_trip.get("vVehicleType") : "";

                if (Utils.checkText(vVehicleType)) {
                    if (vVehicleType.equalsIgnoreCase("Bike")) {
                        iconId = R.mipmap.car_driver_1;
                    } else if (vVehicleType.equalsIgnoreCase("Cycle")) {
                        iconId = R.mipmap.car_driver_2;
                    } else if (vVehicleType.equalsIgnoreCase("Truck")) {
                        iconId = R.mipmap.car_driver_4;
                    } else if (vVehicleType.equalsIgnoreCase("Fly")) {
                        iconId = R.mipmap.ic_fly_icon;
                    }
                }

                MarkerOptions markerOptions_driver = new MarkerOptions();
                markerOptions_driver.position(newLocation);
                markerOptions_driver.icon(BitmapDescriptorFactory.fromResource(iconId)).anchor(0.5f, 0.5f).flat(true);

                driverMarker = gMap.addMarker(markerOptions_driver);
                driverMarker.setTitle(generalFunc.getMemberId());
            }

        }


        if (this.userLocation != null && newLocation != null) {
            LatLng currentLatLng = new LatLng(this.userLocation.getLatitude(), this.userLocation.getLongitude());
            float rotation = driverMarker == null ? 0 : driverMarker.getRotation();

            if (animateMarker.currentLng != null) {
                rotation = (float) animateMarker.bearingBetweenLocations(animateMarker.currentLng, newLocation);
            } else {
                rotation = (float) animateMarker.bearingBetweenLocations(currentLatLng, newLocation);
            }

            if (isUberX) {
                rotation = 0;
            }


            if (driverMarker != null) {
                driverMarker.setTitle(generalFunc.getMemberId());
            }

            HashMap<String, String> previousItemOfMarker = animateMarker.getLastLocationDataOfMarker(driverMarker);

            HashMap<String, String> data_map = new HashMap<>();
            double vLatitude = newLocation.latitude;
            double vLongitude = newLocation.longitude;
            data_map.put("vLatitude", "" + vLatitude);
            data_map.put("vLongitude", "" + vLongitude);
            data_map.put("iDriverId", "" + generalFunc.getMemberId());
            data_map.put("RotationAngle", "" + rotation);
            data_map.put("LocTime", "" + System.currentTimeMillis());

            Location location = new Location("marker");
            location.setLatitude(vLatitude);
            location.setLongitude(vLongitude);


            String prevLocTime = previousItemOfMarker.get("LocTime");
            String LocTime = data_map.get("LocTime");

            if (animateMarker.toPositionLat.get("" + vLatitude) == null || animateMarker.toPositionLong.get("" + vLongitude) == null) {
                if (prevLocTime != null && !prevLocTime.equals("")) {

                    long previousLocTime = GeneralFunctions.parseLongValue(0, prevLocTime);
                    long newLocTime = GeneralFunctions.parseLongValue(0, LocTime);

                    if (previousLocTime != 0 && newLocTime != 0) {

                        if ((newLocTime - previousLocTime) > 0 && animateMarker.driverMarkerAnimFinished == false) {
                            animateMarker.addToListAndStartNext(driverMarker, this.gMap, location, rotation, 850, tripId, LocTime);
                        } else if ((newLocTime - previousLocTime) > 0) {
                            animateMarker.animateMarker(driverMarker, this.gMap, location, rotation, 850, tripId, LocTime);
                        }

                    } else if ((previousLocTime == 0 || newLocTime == 0) && animateMarker.driverMarkerAnimFinished == false) {
                        animateMarker.addToListAndStartNext(driverMarker, this.gMap, location, rotation, 850, tripId, LocTime);
                    } else {
                        animateMarker.animateMarker(driverMarker, this.gMap, location, rotation, 850, tripId, LocTime);
                    }
                } else if (animateMarker.driverMarkerAnimFinished == false) {
                    animateMarker.addToListAndStartNext(driverMarker, this.gMap, location, rotation, 850, tripId, LocTime);
                } else {
                    animateMarker.animateMarker(driverMarker, this.gMap, location, rotation, 850, tripId, LocTime);
                }
            }
        }
    }

    public CameraPosition cameraForUserPosition(Location location, boolean isFirst) {
        double currentZoomLevel = getMap().getCameraPosition().zoom;

        if (isFirst) {
            currentZoomLevel = Utils.defaultZomLevel;
        }
        CameraPosition cameraPosition = new CameraPosition.Builder().target(new LatLng(location.getLatitude(), location.getLongitude())).bearing(getMap().getCameraPosition().bearing)
                .zoom((float) currentZoomLevel).build();

        return cameraPosition;
    }


    public void tripCancelled(String msg) {

        final GenerateAlertBox generateAlert = new GenerateAlertBox(getActContext());
        generateAlert.setCancelable(false);
        generateAlert.setBtnClickList(btn_id -> {
            generateAlert.closeAlertBox();
            generalFunc.saveGoOnlineInfo();
            MyApp.getInstance().restartWithGetDataApp();
        });
        generateAlert.setContentMessage("", msg);
        generateAlert.setPositiveBtn(generalFunc.retrieveLangLBl("", "LBL_BTN_OK_TXT"));
        generateAlert.showAlertBox();
    }

    public void getTripDeliveryLocations() {

        final HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "getTripDeliveryLocations");
        parameters.put("iTripId", data_trip.get("iTripId"));
        parameters.put("userType", "Driver");

        final ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(), parameters);
        exeWebServer.setDataResponseListener(responseString -> {

            if (responseString != null && !responseString.equals("")) {


                if (generalFunc.checkDataAvail(Utils.action_str, responseString) == true) {
                    list = new ArrayList<>();

                    String message = generalFunc.getJsonValue(Utils.message_str, responseString);


                    tripDetail = new ArrayList<HashMap<String, String>>();
                    JSONArray tripLocations = generalFunc.getJsonArray("States", message);
                    String driverdetails = generalFunc.getJsonValue("driverDetails", message);
                    tempMap = new HashMap<>();
                    tempMap.put("driverImage", generalFunc.getJsonValue("riderImage", driverdetails));
                    tempMap.put("driverName", generalFunc.getJsonValue("riderName", driverdetails));
                    tempMap.put("driverRating", generalFunc.getJsonValue("riderRating", driverdetails));
                    tempMap.put("tSaddress", generalFunc.getJsonValue("tSaddress", driverdetails));
                    tempMap.put("iDriverId", generalFunc.getJsonValue("iUserId", driverdetails));

                    tripDetail.add(tempMap);


                    list.clear();

                    String LBL_BOOKING = generalFunc.retrieveLangLBl("", "LBL_BOOKING");
                    if (tripLocations != null)
                        for (int i = 0; i < tripLocations.length(); i++) {
                            tempMap = new HashMap<>();

                            JSONObject jobject1 = generalFunc.getJsonObject(tripLocations, i);
                            tempMap.put("status", generalFunc.getJsonValue("type", jobject1.toString()));
                            tempMap.put("iTripId", generalFunc.getJsonValue("text", jobject1.toString()));

                            tempMap.put("value", generalFunc.getJsonValue("timediff", jobject1.toString()));
                            tempMap.put("Booking_LBL", LBL_BOOKING);
                            tempMap.put("time", generalFunc.getJsonValue("time", jobject1.toString()));
                            tempMap.put("msg", generalFunc.getJsonValue("text", jobject1.toString()));
                            tempMap.put("time", generalFunc.convertNumberWithRTL(generalFunc.getDateFormatedType(generalFunc.getJsonValue("dateOrig", jobject1.toString()), Utils.OriginalDateFormate,
                                    "hh:mm")));
                            tempMap.put("timeampm", generalFunc.convertNumberWithRTL(generalFunc.getDateFormatedType(generalFunc.getJsonValue("dateOrig", jobject1.toString()), Utils.OriginalDateFormate, "aa")));
                            list.add(tempMap);
                        }
                    setView();

                    setDriverDetail();
                } else {

                }
            } else {

            }
        });
        exeWebServer.execute();
    }

    public void getMaskNumber() {

        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "getCallMaskNumber");
        parameters.put("iTripid", data_trip.get("iTripId"));
        parameters.put("UserType", Utils.userType);
        parameters.put("iMemberId", generalFunc.getMemberId());

        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(), parameters);
        exeWebServer.setLoaderConfig(getActContext(), true, generalFunc);

        exeWebServer.setDataResponseListener(responseString -> {

            if (responseString != null && !responseString.equals("")) {

                boolean isDataAvail = GeneralFunctions.checkDataAvail(Utils.action_str, responseString);

                if (isDataAvail == true) {
                    String message = generalFunc.getJsonValue(Utils.message_str, responseString);
                    call(message);
                } else {
                    call(data_trip.get("PPhone"));

                }
            } else {
                generalFunc.showError();
            }
        });
        exeWebServer.execute();
    }

    public void call(String phoneNumber) {
        try {

            Intent callIntent = new Intent(Intent.ACTION_DIAL);
            callIntent.setData(Uri.parse("tel:" + phoneNumber));
            startActivity(callIntent);

        } catch (Exception e) {
            // TODO: handle exception
        }
    }

    public void buildMsgOnDeliveryEnd() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getActContext());
        builder.setTitle(generalFunc.retrieveLangLBl("Delivery Confirmation", "LBL_DELIVERY_CONFIRM"));
        builder.setCancelable(false);
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View dialogView = inflater.inflate(R.layout.dialog_end_delivery_design, null);

        final MaterialEditText verificationCodeBox = (MaterialEditText) dialogView.findViewById(R.id.editBox);
        String contentMsg = generalFunc.retrieveLangLBl("Please enter the confirmation code received from recipient.", "LBL_DELIVERY_END_NOTE");
        if (SITE_TYPE.equalsIgnoreCase("Demo")) {
            contentMsg = contentMsg + " \n" +
                    generalFunc.retrieveLangLBl("For demo purpose, please enter confirmation code in text box as shown below.", "LBL_DELIVERY_END_NOTE_DEMO")
                    + " \n" + generalFunc.retrieveLangLBl("Confirmation Code", "LBL_CONFIRMATION_CODE") + ": " + deliveryVerificationCode;
        }

        ((MTextView) dialogView.findViewById(R.id.contentMsgTxt)).setText(contentMsg);

        builder.setView(dialogView);

        builder.setPositiveButton(generalFunc.retrieveLangLBl("", "LBL_BTN_OK_TXT"), (dialog, which) -> {

        });
        builder.setNegativeButton(generalFunc.retrieveLangLBl("", "LBL_CANCEL_TXT"), (dialog, which) -> {
        });

        deliveryEndDialog = builder.create();
        if (generalFunc.isRTLmode() == true) {
            generalFunc.forceRTLIfSupported(deliveryEndDialog);
        }
        deliveryEndDialog.show();

        deliveryEndDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (Utils.checkText(verificationCodeBox) == false) {
                    verificationCodeBox.setError(generalFunc.retrieveLangLBl("", "LBL_FEILD_REQUIRD"));
                    return;
                }

                if (!Utils.getText(verificationCodeBox).equals(deliveryVerificationCode)) {
                    verificationCodeBox.setError(generalFunc.retrieveLangLBl("Invalid code", "LBL_INVALID_DELIVERY_CONFIRM_CODE"));
                    return;
                }

                deliveryEndDialog.dismiss();

                if (APP_TYPE.equalsIgnoreCase("UberX") || eType.equalsIgnoreCase(Utils.CabGeneralType_UberX) &&
                        data_trip != null && data_trip.get("eAfterUpload").equalsIgnoreCase("Yes")) {
                    //&& generalFunc.retrieveValue(Utils.PHOTO_UPLOAD_SERVICE_ENABLE_KEY).equalsIgnoreCase("Yes")) {
                    takeAndUploadPic(getActContext(), "after");
                } else {
                    endTrip();
                }

            }
        });

        deliveryEndDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(v -> {
            deliveryEndDialog.dismiss();
            endTripSlideButton.resetButtonView(endTripSlideButton.btnText.getText().toString());
        });
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;

        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.trip_accept_menu, menu);

        if (REQUEST_TYPE.equals("Deliver")) {

            menu.findItem(R.id.menu_passenger_detail).setTitle(generalFunc.retrieveLangLBl("View Delivery Details", "LBL_VIEW_DELIVERY_DETAILS"));
            if (!isendslide) {
                menu.findItem(R.id.menu_cancel_trip).setTitle(generalFunc.retrieveLangLBl("Cancel Delivery", "LBL_CANCEL_DELIVERY"));
            } else {
                MenuItem item = menu.findItem(R.id.menu_cancel_trip);
                item.setVisible(false);

            }
        } else {
            try {
                if (data_trip.get("eHailTrip").equalsIgnoreCase("Yes")) {
                    menu.findItem(R.id.menu_passenger_detail).setTitle(generalFunc.retrieveLangLBl("View passenger detail", "LBL_VIEW_PASSENGER_DETAIL")).setVisible(false);
                    menu.findItem(R.id.menu_call).setTitle(generalFunc.retrieveLangLBl("Call", "LBL_CALL_ACTIVE_TRIP")).setVisible(false);
                    menu.findItem(R.id.menu_message).setTitle(generalFunc.retrieveLangLBl("Message", "LBL_MESSAGE_ACTIVE_TRIP")).setVisible(false);

                    chatview.setVisibility(View.GONE);
                    callview.setVisibility(View.GONE);
                    ratingBar.setVisibility(View.GONE);

                } else {
                    menu.findItem(R.id.menu_passenger_detail).setTitle(generalFunc.retrieveLangLBl("View passenger detail", "LBL_VIEW_PASSENGER_DETAIL")).setVisible(false);
                }
            } catch (Exception e) {
                menu.findItem(R.id.menu_passenger_detail).setTitle(generalFunc.retrieveLangLBl("View passenger detail", "LBL_VIEW_PASSENGER_DETAIL")).setVisible(false);
            }
            menu.findItem(R.id.menu_cancel_trip).setTitle(generalFunc.retrieveLangLBl("Cancel trip", "LBL_CANCEL_TRIP"));
        }

        if (REQUEST_TYPE.equalsIgnoreCase(Utils.CabGeneralType_UberX)) {
            menu.findItem(R.id.menu_cancel_trip).setTitle(generalFunc.retrieveLangLBl("", "LBL_CANCEL_JOB"));
        }

        String last_trip_data = generalFunc.getJsonValue("TripDetails", userProfileJsonObj.toString());
        if (!generalFunc.getJsonValue("moreServices", last_trip_data).equalsIgnoreCase("") && generalFunc.getJsonValue("moreServices", last_trip_data).equalsIgnoreCase("Yes")) {
            menu.findItem(R.id.menu_specialInstruction).setTitle(generalFunc.retrieveLangLBl("Special Instruction", "LBL_TITLE_REQUESTED_SERVICES"));
        } else {

            menu.findItem(R.id.menu_specialInstruction).setTitle(generalFunc.retrieveLangLBl("Special Instruction", "LBL_SPECIAL_INSTRUCTION_TXT"));
        }
        menu.findItem(R.id.menu_call).setTitle(generalFunc.retrieveLangLBl("Call", "LBL_CALL_ACTIVE_TRIP"));
        menu.findItem(R.id.menu_message).setTitle(generalFunc.retrieveLangLBl("Message", "LBL_MESSAGE_ACTIVE_TRIP"));
        menu.findItem(R.id.menu_sos).setTitle(generalFunc.retrieveLangLBl("Emergency or SOS", "LBL_EMERGENCY_SOS_TXT"));


        String LBL_MENU_WAY_BILL = generalFunc.retrieveLangLBl("Way Bill", "LBL_MENU_WAY_BILL");
        if (REQUEST_TYPE.equals(Utils.CabGeneralType_UberX)) {


            menu.findItem(R.id.menu_specialInstruction).setVisible(true);
            // if (!data_trip.get("eFareType").equals(Utils.CabFaretypeRegular)) {
            menu.findItem(R.id.menu_waybill_trip).setTitle(LBL_MENU_WAY_BILL).setVisible
                    (false);

            // }

            if (data_trip.get("eFareType").equals(Utils.CabFaretypeRegular)) {
                menu.findItem(R.id.menu_passenger_detail).setTitle(generalFunc.retrieveLangLBl("View User detail", "LBL_VIEW_USER_DETAIL")).setVisible(true);
                menu.findItem(R.id.menu_sos).setVisible(false);
                menu.findItem(R.id.menu_call).setVisible(false);
                menu.findItem(R.id.menu_message).setVisible(false);
            } else {
                menu.findItem(R.id.menu_passenger_detail).setVisible(false);
                menu.findItem(R.id.menu_call).setVisible(true);
                menu.findItem(R.id.menu_message).setVisible(true);
                menu.findItem(R.id.menu_sos).setVisible(true);
            }


        } else {

            boolean eFlyEnabled= data_trip.get("eFly").equalsIgnoreCase("Yes");
            boolean isWayBillEnabled = generalFunc.getJsonValue("WAYBILL_ENABLE", userProfileJsonObj) != null && generalFunc.getJsonValueStr("WAYBILL_ENABLE", userProfileJsonObj).equalsIgnoreCase("yes");
            if (!data_trip.get("eHailTrip").equalsIgnoreCase("Yes")) {
                menu.findItem(R.id.menu_passenger_detail).setVisible(true);
                menu.findItem(R.id.menu_call).setVisible(false);
                menu.findItem(R.id.menu_message).setVisible(false);
                menu.findItem(R.id.menu_sos).setVisible(false);
                if (isWayBillEnabled && !eFlyEnabled) {
                    menu.findItem(R.id.menu_waybill_trip).setTitle(LBL_MENU_WAY_BILL).setVisible(true);
                } else {
                    menu.findItem(R.id.menu_waybill_trip).setTitle(LBL_MENU_WAY_BILL).setVisible
                            (false);


                }
            } else {
                menu.findItem(R.id.menu_passenger_detail).setVisible(false);
                menu.findItem(R.id.menu_call).setVisible(false);
                menu.findItem(R.id.menu_message).setVisible(false);
                menu.findItem(R.id.menu_sos).setVisible(false);
                if (isWayBillEnabled && !eFlyEnabled) {
                    menu.findItem(R.id.menu_waybill_trip).setTitle(LBL_MENU_WAY_BILL).setVisible(true);
                } else {
                    menu.findItem(R.id.menu_waybill_trip).setTitle(LBL_MENU_WAY_BILL).setVisible
                            (false);


                }

            }


        }

        Utils.setMenuTextColor(menu.findItem(R.id.menu_passenger_detail), getResources().getColor(R.color.appThemeColor_TXT_1));
        Utils.setMenuTextColor(menu.findItem(R.id.menu_cancel_trip), getResources().getColor(R.color.appThemeColor_TXT_1));
        Utils.setMenuTextColor(menu.findItem(R.id.menu_waybill_trip), getResources().getColor(R.color.appThemeColor_TXT_1));
        Utils.setMenuTextColor(menu.findItem(R.id.menu_sos), getResources().getColor(R.color.appThemeColor_TXT_1));
        Utils.setMenuTextColor(menu.findItem(R.id.menu_call), getResources().getColor(R.color.appThemeColor_TXT_1));
        Utils.setMenuTextColor(menu.findItem(R.id.menu_message), getResources().getColor(R.color.appThemeColor_TXT_1));
        Utils.setMenuTextColor(menu.findItem(R.id.menu_specialInstruction), getResources().getColor(R.color.appThemeColor_TXT_1));
        return true;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU) {

            return true;
        }

        // let the system handle all other key events
        return super.onKeyDown(keyCode, event);
    }

    public void getDeclineReasonsList() {
        HashMap<String, String> parameters = new HashMap<>();

        parameters.put("type", "GetCancelReasons");
        parameters.put("iTripId", tripId);
        parameters.put("iMemberId", generalFunc.getMemberId());
        parameters.put("eUserType", Utils.app_type);

        ExecuteWebServerUrl exeServerTask = new ExecuteWebServerUrl(getActContext(), parameters);
        exeServerTask.setLoaderConfig(getActContext(), true, generalFunc);
        exeServerTask.setDataResponseListener(responseString -> {

            if (!responseString.equals("")) {
                JSONObject responseStringObj = generalFunc.getJsonObject(responseString);
                boolean isDataAvail = generalFunc.checkDataAvail(Utils.action_str, responseString);

                if (isDataAvail) {
                    showDeclineReasonsAlert(responseStringObj);
                } else {
                    generalFunc.showGeneralMessage("", generalFunc.retrieveLangLBl("", generalFunc.getJsonValue(Utils.message_str, responseString)));
                }

            } else {
                generalFunc.showError();
            }

        });
        exeServerTask.execute();
    }

    String selectedItemId = "";

    String titleDailog = "";
    int selCurrentPosition = -1;

    public void showDeclineReasonsAlert(JSONObject responseString) {
        selCurrentPosition = -1;
        if (data_trip.get("eType").equalsIgnoreCase(Utils.CabGeneralType_Ride)) {
            titleDailog = generalFunc.retrieveLangLBl("", "LBL_CANCEL_TRIP");
        } else if (data_trip.get("eType").equalsIgnoreCase(Utils.CabGeneralType_UberX)) {
            titleDailog = generalFunc.retrieveLangLBl("", "LBL_CANCEL_BOOKING");
        } else {
            titleDailog = generalFunc.retrieveLangLBl("", "LBL_CANCEL_DELIVERY");
        }
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getActContext());

        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.decline_order_dialog_design, null);
        builder.setView(dialogView);

        MaterialEditText reasonBox = (MaterialEditText) dialogView.findViewById(R.id.inputBox);
        RelativeLayout commentArea = (RelativeLayout) dialogView.findViewById(R.id.commentArea);
        reasonBox.setHideUnderline(true);
        if (generalFunc.isRTLmode()) {
            reasonBox.setPaddings(0, 0, (int) getResources().getDimension(R.dimen._10sdp), 0);
        } else {
            reasonBox.setPaddings((int) getResources().getDimension(R.dimen._10sdp), 0, 0, 0);
        }

        reasonBox.setSingleLine(false);
        reasonBox.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        reasonBox.setGravity(Gravity.TOP);
        if (generalFunc.isRTLmode()) {
            reasonBox.setPaddings(0, 0, (int) getResources().getDimension(R.dimen._10sdp), 0);
        } else {
            reasonBox.setPaddings((int) getResources().getDimension(R.dimen._10sdp), 0, 0, 0);
        }
        reasonBox.setVisibility(View.GONE);
        commentArea.setVisibility(View.GONE);
        new CreateRoundedView(Color.parseColor("#ffffff"), 5, 1, Color.parseColor("#C5C3C3"), commentArea);
        reasonBox.setBothText("", generalFunc.retrieveLangLBl("", "LBL_ENTER_REASON"));


        ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();
        //   HashMap<String, String> map = new HashMap<>();
        //   map.put("title", "-- " + generalFunc.retrieveLangLBl("Select Reason", "LBL_SELECT_CANCEL_REASON") + " --");
        //  map.put("id", "");
        //   list.add(map);
        JSONArray arr_msg = generalFunc.getJsonArray(Utils.message_str, responseString);
        if (arr_msg != null) {

            for (int i = 0; i < arr_msg.length(); i++) {

                JSONObject obj_tmp = generalFunc.getJsonObject(arr_msg, i);


                HashMap<String, String> datamap = new HashMap<>();
                datamap.put("title", generalFunc.getJsonValueStr("vTitle", obj_tmp));
                datamap.put("id", generalFunc.getJsonValueStr("iCancelReasonId", obj_tmp));
                list.add(datamap);
            }

            HashMap<String, String> othermap = new HashMap<>();
            othermap.put("title", generalFunc.retrieveLangLBl("", "LBL_OTHER_TXT"));
            othermap.put("id", "");
            list.add(othermap);

            // AppCompatSpinner spinner = (AppCompatSpinner) dialogView.findViewById(R.id.declineReasonsSpinner);
            MTextView cancelTxt = (MTextView) dialogView.findViewById(R.id.cancelTxt);
            MTextView submitTxt = (MTextView) dialogView.findViewById(R.id.submitTxt);
            MTextView subTitleTxt = (MTextView) dialogView.findViewById(R.id.subTitleTxt);
            ImageView cancelImg = (ImageView) dialogView.findViewById(R.id.cancelImg);
            subTitleTxt.setText(titleDailog);

            submitTxt.setText(generalFunc.retrieveLangLBl("", "LBL_YES"));
            cancelTxt.setText(generalFunc.retrieveLangLBl("", "LBL_NO"));
            MTextView declinereasonBox = (MTextView) dialogView.findViewById(R.id.declinereasonBox);
            declinereasonBox.setText(generalFunc.retrieveLangLBl("Select Reason", "LBL_SELECT_CANCEL_REASON"));
            submitTxt.setClickable(false);
            submitTxt.setTextColor(getResources().getColor(R.color.gray_holo_light));

            submitTxt.setOnClickListener(v -> {

                if (selCurrentPosition == -1) {
                    return;
                }

                if (Utils.checkText(reasonBox) == false && selCurrentPosition == (list.size() - 1)) {
                    reasonBox.setError(generalFunc.retrieveLangLBl("", "LBL_FEILD_REQUIRD"));
                    return;
                }

                new CancelTripDialog(getActContext(), data_trip, generalFunc, list.get(selCurrentPosition).get("id"), Utils.getText(reasonBox), isTripStart, reasonBox.getText().toString().trim(), userLocation != null ? userLocation : GetLocationUpdates.getInstance().getLastLocation());

            });
            cancelTxt.setOnClickListener(v -> {
                Utils.hideKeyboard(getActContext());
                dialog_declineOrder.dismiss();
            });

            cancelImg.setOnClickListener(v -> {
                Utils.hideKeyboard(getActContext());
                dialog_declineOrder.dismiss();
            });
           /* CustSpinnerAdapter adapter = new CustSpinnerAdapter(getActContext(), list);
            spinner.setAdapter(adapter);


            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                    if (spinner.getSelectedItemPosition() == (list.size() - 1)) {
                        reasonBox.setVisibility(View.VISIBLE);
                        commentArea.setVisibility(View.VISIBLE);
                    } else if (spinner.getSelectedItemPosition() == 0) {
                        reasonBox.setVisibility(View.GONE);
                        commentArea.setVisibility(View.GONE);
                    } else {
                        reasonBox.setVisibility(View.GONE);
                        commentArea.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });*/


            declinereasonBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    OpenListView.getInstance(getActContext(), generalFunc.retrieveLangLBl("", "LBL_SELECT_REASON"), list, OpenListView.OpenDirection.CENTER, true, position -> {


                        selCurrentPosition = position;
                        HashMap<String, String> mapData = list.get(position);
                        declinereasonBox.setText(mapData.get("title"));
                        if (selCurrentPosition == (list.size() - 1)) {
                            reasonBox.setVisibility(View.VISIBLE);
                            commentArea.setVisibility(View.VISIBLE);
                        } else {
                            reasonBox.setVisibility(View.GONE);
                            commentArea.setVisibility(View.GONE);
                        }

                        submitTxt.setClickable(true);
                        submitTxt.setTextColor(getResources().getColor(R.color.white));


                    }).show(selCurrentPosition, "title");
                }
            });


            dialog_declineOrder = builder.create();
            dialog_declineOrder.setCancelable(false);
            dialog_declineOrder.getWindow().setBackgroundDrawable(getActContext().getResources().getDrawable(R.drawable.all_roundcurve_card));
            if (generalFunc.isRTLmode()) {
                dialog_declineOrder.getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
            }
            dialog_declineOrder.show();
        } else {
            generalFunc.showGeneralMessage("", generalFunc.retrieveLangLBl("", "LBL_NO_DATA_AVAIL"));
        }
    }

    public void sinchCall() {


        if (MyApp.getInstance().getCurrentAct() != null) {

            if (generalFunc.isCallPermissionGranted(false) == false) {
                generalFunc.isCallPermissionGranted(true);
            } else {
                ActiveTripActivity activity = (ActiveTripActivity) MyApp.getInstance().getCurrentAct();

                HashMap<String, String> hashMap = new HashMap<>();
                hashMap.put("Id", generalFunc.getMemberId());
                hashMap.put("Name", generalFunc.getJsonValueStr("vName", userProfileJsonObj));
                hashMap.put("PImage", generalFunc.getJsonValueStr("vImage", userProfileJsonObj));
                hashMap.put("type", Utils.userType);


                getSinchServiceInterface().getSinchClient().setPushNotificationDisplayName(generalFunc.retrieveLangLBl("", "LBL_INCOMING_CALL"));
                Call call;
                if (Utils.checkText(data_trip.get("iGcmRegId_U"))) {
                    call = getSinchServiceInterface().callUser(Utils.CALLTOPASSENGER + "_" + data_trip.get("PassengerId"), hashMap);
                } else {
                    call = getSinchServiceInterface().callPhoneNumber(data_trip.get("vPhone_U"));
                }

                String callId = call.getCallId();

                Intent callScreen = new Intent(getActContext(), CallScreenActivity.class);
                callScreen.putExtra(SinchService.CALL_ID, callId);
                callScreen.putExtra("vImage", vImage);
                callScreen.putExtra("vName", vName);
                startActivity(callScreen);

            }


        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.menu_passenger_detail:

                if (REQUEST_TYPE.equals("Deliver")) {
                    Bundle bn = new Bundle();
                    bn.putString("TripId", data_trip.get("TripId"));
                    bn.putSerializable("data_trip", data_trip);
                    new StartActProcess(getActContext()).startActWithData(ViewDeliveryDetailsActivity.class, bn);
                } else {
                    new OpenPassengerDetailDialog(getActContext(), data_trip, generalFunc, false);
                }

                return true;

            case R.id.menu_cancel_trip:
                getDeclineReasonsList();
//                new CancelTripDialog(getActContext(), data_trip, generalFunc, isTripStart);
                return true;

            case R.id.menu_waybill_trip:
                Bundle bn4 = new Bundle();
                bn4.putSerializable("data_trip", data_trip);
                new StartActProcess(getActContext()).startActWithData(WayBillActivity.class, bn4);
                return true;

            case R.id.menu_sos:
                Bundle bn = new Bundle();

                bn.putString("TripId", tripId);
                new StartActProcess(getActContext()).startActWithData(ConfirmEmergencyTapActivity.class, bn);

                return true;


            case R.id.menu_call:
                if (REQUEST_TYPE.equals("Deliver")) {
                    Bundle bn1 = new Bundle();
                    bn1.putString("TripId", data_trip.get("TripId"));
                    bn1.putSerializable("data_trip", data_trip);
                    new StartActProcess(getActContext()).startActWithData(ViewDeliveryDetailsActivity.class, bn1);
                } else {
                    try {
                        if (generalFunc.getJsonValue("RIDE_DRIVER_CALLING_METHOD", userProfileJsonObj).equals("Voip")) {
                            sinchCall();
                        } else {
                            getMaskNumber();
                        }


                    } catch (Exception e) {
                    }
                }


                return true;
            case R.id.menu_message:

                if (REQUEST_TYPE.equals("Deliver")) {
                    Bundle bn2 = new Bundle();
                    bn2.putString("TripId", data_trip.get("TripId"));
                    bn2.putSerializable("data_trip", data_trip);
                    new StartActProcess(getActContext()).startActWithData(ViewDeliveryDetailsActivity.class, bn2);
                }
            {

                Bundle bnChat = new Bundle();

                bnChat.putString("iFromMemberId", data_trip.get("PassengerId"));
                bnChat.putString("FromMemberImageName", data_trip.get("PPicName"));
                bnChat.putString("iTripId", data_trip.get("iTripId"));
                bnChat.putString("FromMemberName", data_trip.get("PName"));
                bnChat.putString("vBookingNo", data_trip.get("vRideNo"));
                new StartActProcess(getActContext()).startActWithData(ChatActivity.class, bnChat);
            }
            return true;

            case R.id.menu_specialInstruction:


                String last_trip_data = generalFunc.getJsonValue("TripDetails", userProfileJsonObj.toString());
                String moreServices = generalFunc.getJsonValue("moreServices", last_trip_data);
                if (!moreServices.equalsIgnoreCase("") && moreServices.equalsIgnoreCase("Yes")) {
                    Bundle bundle = new Bundle();
                    bundle.putString("iTripId", data_trip.get("iTripId"));
                    new StartActProcess(getActContext()).startActWithData(MoreServiceInfoActivity.class, bundle);

                } else {
                    String tUserComment = data_trip.get("tUserComment");
                    if (Utils.checkText(tUserComment)) {
                        generalFunc.showGeneralMessage(generalFunc.retrieveLangLBl("Special Instruction", "LBL_SPECIAL_INSTRUCTION_TXT"), tUserComment);
                    } else {
                        generalFunc.showGeneralMessage(generalFunc.retrieveLangLBl("Special Instruction", "LBL_SPECIAL_INSTRUCTION_TXT"), generalFunc.retrieveLangLBl("", "LBL_NO_SPECIAL_INSTRUCTION"));

                    }
                }


                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public Context getActContext() {
        return ActiveTripActivity.this; // Must be context of activity not application
    }

    public void addDestination(final String latitude, final String longitude, final String address) {
        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "addDestination");
        //  parameters.put("iDriverId", generalFunc.getMemberId());
        parameters.put("Latitude", latitude);
        parameters.put("Longitude", longitude);
        parameters.put("Address", address);
        //   parameters.put("UserId", data_trip.get("PassengerId"));
        parameters.put("eConfirmByUser", eConfirmByUser);
        parameters.put("iMemberId", generalFunc.getMemberId());
        parameters.put("UserType", Utils.userType);
        parameters.put("TripId", tripId);
        parameters.put("eTollConfirmByUser", eTollConfirmByUser);
        parameters.put("fTollPrice", tollamount + "");
        parameters.put("vTollPriceCurrencyCode", tollcurrancy);
        String tollskiptxt = "";
        if (istollIgnore) {
            tollamount = 0;
            tollskiptxt = "Yes";

        } else {
            tollskiptxt = "No";
        }
        parameters.put("eTollSkipped", tollskiptxt);
        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(), parameters);
        exeWebServer.setLoaderConfig(getActContext(), true, generalFunc);
        exeWebServer.setDataResponseListener(responseString -> {

            if (responseString != null && !responseString.equals("")) {

                boolean isDataAvail = GeneralFunctions.checkDataAvail(Utils.action_str, responseString);

                if (isDataAvail == true) {

                    if (istollIgnore) {
                        MyApp.getInstance().restartWithGetDataApp();
                        return;
                    }

                    setDestinationPoint(latitude, longitude, address, true);

                    Location destLoc = new Location("gps");
                    destLoc.setLatitude(GeneralFunctions.parseDoubleValue(0.0, latitude));
                    destLoc.setLongitude(GeneralFunctions.parseDoubleValue(0.0, longitude));

                    if (updateDirections == null) {
                        updateDirections = new UpdateDirections(getActContext(), gMap, userLocation, destLoc);
                        updateDirections.scheduleDirectionUpdate();
                    } else {
                        updateDirections.changeDestLoc(destLoc);
                        updateDirections.updateDirections();

                    }
                    addDestinationMarker();
                } else {

                    String msg_str = generalFunc.getJsonValue(Utils.message_str, responseString);


                    if (msg_str.equalsIgnoreCase("LBL_DROP_LOCATION_NOT_ALLOW")) {
                        tollamount = 0.0;
                        generalFunc.showGeneralMessage("", generalFunc.retrieveLangLBl("", "LBL_DROP_LOCATION_NOT_ALLOW"));
                        return;
                    }

                    if (msg_str.equalsIgnoreCase("Yes")) {
                        if (generalFunc.getJsonValue("SurgePrice", responseString).equalsIgnoreCase("")) {
                            openFixChargeDialog(responseString, false);
                        } else {
                            openFixChargeDialog(responseString, true);
                        }
                        return;
                    }

                    if (tollamount != 0.0 && tollamount != 0 && tollamount != 0.00) {

                        if (generalFunc.getJsonValue("SurgePrice", responseString).equalsIgnoreCase("")) {
                            TollTaxDialog();
                        } else {
                            TollTaxDialog();
                        }

                        return;
                    }


                    if (msg_str.equals(Utils.GCM_FAILED_KEY) || msg_str.equals(Utils.APNS_FAILED_KEY) || msg_str.equals("LBL_SERVER_COMM_ERROR")) {
                        generalFunc.restartApp();
                    } else {
                        generalFunc.showGeneralMessage("",
                                generalFunc.retrieveLangLBl("", msg_str));
                    }
                }
            } else {
                generalFunc.showError();
            }
        });
        exeWebServer.execute();
    }

    public void openFixChargeDialog(String responseString, boolean isSurCharge) {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getActContext());
        builder.setTitle("");
        builder.setCancelable(false);
        LayoutInflater inflater = (LayoutInflater) getActContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View dialogView = inflater.inflate(R.layout.surge_confirm_design, null);
        builder.setView(dialogView);
        MTextView payableAmountTxt;
        MTextView payableTxt;

        ((MTextView) dialogView.findViewById(R.id.headerMsgTxt)).setText(generalFunc.retrieveLangLBl("", generalFunc.retrieveLangLBl("", "LBL_FIX_FARE_HEADER")));


        ((MTextView) dialogView.findViewById(R.id.tryLaterTxt)).setText(generalFunc.retrieveLangLBl("", "LBL_TRY_LATER"));
        payableTxt = (MTextView) dialogView.findViewById(R.id.payableTxt);
        payableAmountTxt = (MTextView) dialogView.findViewById(R.id.payableAmountTxt);
        if (!generalFunc.getJsonValue("fFlatTripPricewithsymbol", responseString).equalsIgnoreCase("")) {
            payableAmountTxt.setVisibility(View.VISIBLE);
            payableTxt.setVisibility(View.GONE);

            if (isSurCharge) {
                payableAmount = generalFunc.getJsonValue("fFlatTripPricewithsymbol", responseString) + " " + "(" + generalFunc.retrieveLangLBl("", "LBL_AT_TXT") + " " +
                        generalFunc.convertNumberWithRTL(generalFunc.getJsonValue("SurgePrice", responseString)) + ")";
                ((MTextView) dialogView.findViewById(R.id.surgePriceTxt)).setText(generalFunc.convertNumberWithRTL(payableAmount));
            } else {
                payableAmount = generalFunc.getJsonValue("fFlatTripPricewithsymbol", responseString);
                ((MTextView) dialogView.findViewById(R.id.surgePriceTxt)).setText(generalFunc.convertNumberWithRTL(payableAmount));

            }
        } else {
            payableAmountTxt.setVisibility(View.GONE);
            payableTxt.setVisibility(View.VISIBLE);

        }

        MButton btn_type2 = ((MaterialRippleLayout) dialogView.findViewById(R.id.btn_type2)).getChildView();
        btn_type2.setText(generalFunc.retrieveLangLBl("", "LBL_ACCEPT_TXT"));
        btn_type2.setId(Utils.generateViewId());

        btn_type2.setOnClickListener(view -> {
            alertDialog_surgeConfirm.dismiss();
            eConfirmByUser = "Yes";
            addDestination(latitude, longitirude, address);
        });
        (dialogView.findViewById(R.id.tryLaterTxt)).setOnClickListener(view -> {
            tollamount = 0.0;
            alertDialog_surgeConfirm.dismiss();

        });

        alertDialog_surgeConfirm = builder.create();
        alertDialog_surgeConfirm.setCancelable(false);
        alertDialog_surgeConfirm.setCanceledOnTouchOutside(false);
        if (generalFunc.isRTLmode() == true) {
            generalFunc.forceRTLIfSupported(alertDialog_surgeConfirm);
        }

        alertDialog_surgeConfirm.show();
    }

    public void setDestinationPoint(String latitude, String longitude, String address, boolean isDestinationAdded) {
        double dest_lat = generalFunc.parseDoubleValue(0.0, latitude);
        double dest_lon = generalFunc.parseDoubleValue(0.0, longitude);

        (findViewById(R.id.destLocSearchArea)).setVisibility(View.GONE);
        (findViewById(R.id.navigationViewArea)).setVisibility(View.VISIBLE);
        (findViewById(R.id.navigateArea)).setVisibility(eFly ? View.GONE : View.VISIBLE);
        try {
            if (data_trip.get("eTollSkipped").equalsIgnoreCase("yes")) {
                tollTxtView.setVisibility(View.VISIBLE);
            }
        } catch (Exception e) {

        }

        if (address.equals("")) {
            addressTxt.setText(generalFunc.retrieveLangLBl("Loading address", "LBL_LOAD_ADDRESS"));
            GetAddressFromLocation getAddressFromLocation = new GetAddressFromLocation(getActContext(), generalFunc);
            getAddressFromLocation.setLocation(dest_lat, dest_lon);
            getAddressFromLocation.setAddressList((address1, latitude1, longitude1,geocodeobject) -> addressTxt.setText(address1));

            getAddressFromLocation.execute();
        } else {
            addressTxt.setText(address);
        }

        navigateAreaUP.setOnClickListener(new setOnClickAct("" + dest_lat, "" + dest_lon));

        this.isDestinationAdded = isDestinationAdded;
        this.destLocLatitude = dest_lat;
        this.destLocLongitude = dest_lon;
    }

    public void setTripStart() {

        if (!TextUtils.isEmpty(isFrom) && imageType.equalsIgnoreCase("before")) {

            ArrayList<String[]> paramsList = new ArrayList<>();
            paramsList.add(generalFunc.generateImageParams("type", "StartTrip"));
            paramsList.add(generalFunc.generateImageParams("iDriverId", generalFunc.getMemberId()));
            paramsList.add(generalFunc.generateImageParams("TripID", tripId));

            if (userLocation != null) {
                paramsList.add(generalFunc.generateImageParams("vLatitude", "" + userLocation.getLatitude()));
                paramsList.add(generalFunc.generateImageParams("vLongitude", "" + userLocation.getLongitude()));
            } else if (GetLocationUpdates.getInstance().getLastLocation() != null) {
                Location lastLocation = GetLocationUpdates.getInstance().getLastLocation();
                paramsList.add(generalFunc.generateImageParams("vLatitude", "" + lastLocation.getLatitude()));
                paramsList.add(generalFunc.generateImageParams("vLongitude", "" + lastLocation.getLongitude()));

            }


            paramsList.add(generalFunc.generateImageParams("iUserId", data_trip.get("PassengerId")));
            paramsList.add(generalFunc.generateImageParams("UserType", Utils.app_type));
            paramsList.add(Utils.generateImageParams("iMemberId", generalFunc.getMemberId()));
            paramsList.add(Utils.generateImageParams("MemberType", Utils.app_type));
            paramsList.add(Utils.generateImageParams("tSessionId", generalFunc.getMemberId().equals("") ? "" : generalFunc.retrieveValue(Utils.SESSION_ID_KEY)));
            paramsList.add(Utils.generateImageParams("GeneralUserType", Utils.app_type));
            paramsList.add(Utils.generateImageParams("GeneralMemberId", generalFunc.getMemberId()));

            new UploadProfileImage(ActiveTripActivity.this, selectedImagePath, Utils.TempProfileImageName, paramsList, imageType).execute();

        } else {


            HashMap<String, String> parameters = new HashMap<String, String>();
            parameters.put("type", "StartTrip");
            parameters.put("iDriverId", generalFunc.getMemberId());
            parameters.put("TripID", tripId);

            if (userLocation != null) {
                parameters.put("vLatitude", "" + userLocation.getLatitude());
                parameters.put("vLongitude", "" + userLocation.getLongitude());
            } else if (GetLocationUpdates.getInstance().getLastLocation() != null) {
                parameters.put("vLatitude", "" + GetLocationUpdates.getInstance().getLastLocation().getLatitude());
                parameters.put("vLongitude", "" + GetLocationUpdates.getInstance().getLastLocation().getLongitude());
            }

            parameters.put("iUserId", data_trip.get("PassengerId"));
            parameters.put("UserType", Utils.app_type);
            if (eType.equalsIgnoreCase(Utils.eType_Multi_Delivery)) {
                parameters.put("iTripDeliveryLocationId", iTripDeliveryLocationId);
            }
            if (isPoolRide) {
                MyApp.getInstance().ispoolRequest = true;
            }

            ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(), parameters);
            exeWebServer.setLoaderConfig(getActContext(), true, generalFunc);
            exeWebServer.setCancelAble(false);
            exeWebServer.setDataResponseListener(responseString -> startTripResponse(responseString));
            exeWebServer.execute();
        }
    }

    private void startTripResponse(String responseString) {
        MyApp.getInstance().ispoolRequest = false;
        if (responseString != null && !responseString.equals("")) {

            if (eType.equals("UberX")) {
                getTripDeliveryLocations();

            } else {
                try {
                    String eFareType = data_trip.get("eFareType");
                    if (eFareType != null && !eFareType.equals("")) {
                        if (eFareType.equals(Utils.CabFaretypeFixed)) {
                            getTripDeliveryLocations();
                        } else if (eFareType.equals(Utils.CabFaretypeHourly)) {
                            btntimer.setVisibility(View.VISIBLE);
                            getTripDeliveryLocations();

                        }
                    }
                } catch (Exception e) {
                    Logger.e("ExceptionResponse", "::" + e.toString());

                }
            }

            boolean isDataAvail = GeneralFunctions.checkDataAvail(Utils.action_str, responseString);

            if (isDataAvail == true) {
                closeuploadServicePicAlertBox();

                currencetprice = generalFunc.getJsonValue("fVisitFee", responseString);
                if (REQUEST_TYPE.equals("Deliver")) {
                    SITE_TYPE = generalFunc.getJsonValue("SITE_TYPE", responseString);
                    deliveryVerificationCode = generalFunc.getJsonValue(Utils.message_str, responseString);
                }
                if (data_trip.get("eFareType").equals(Utils.CabFaretypeHourly)) {
                    TripTimeId = generalFunc.getJsonValue("iTripTimeId", responseString);
//                    callsetTimeApi(true);
                    btntimer.setVisibility(View.VISIBLE);
                    Log.e("countdownstartCalled", ":: 2");
                    countDownStart();
                }
                configTripStartView();
                //endTripSlideButton.setVisibility(View.VISIBLE);
                if (generalFunc.getJsonValue("ENABLE_INTRANSIT_SHOPPING_SYSTEM", userProfileJsonObj).equals("Yes") && eType.equalsIgnoreCase(Utils.CabGeneralType_Ride) &&
                        !data_trip.get("eRental").equalsIgnoreCase("Yes") && !data_trip.get("ePoolRide").equalsIgnoreCase("Yes") && data_trip.get("eTransit").equalsIgnoreCase("Yes")) {
                    transitConfigTripStartView();

                }

            } else {
                String msg_str = generalFunc.getJsonValue(Utils.message_str, responseString);
                if (msg_str.equals(Utils.GCM_FAILED_KEY) || msg_str.equals(Utils.APNS_FAILED_KEY) || msg_str.equals("LBL_SERVER_COMM_ERROR")) {
                    generalFunc.restartApp();
                } else {
                    startTripSlideButton.resetButtonView(startTripSlideButton.btnText.getText().toString());
                    generalFunc.showGeneralMessage("",
                            generalFunc.retrieveLangLBl("", msg_str));
                }

            }
        } else {
            generalFunc.showError();
            startTripSlideButton.resetButtonView(startTripSlideButton.btnText.getText().toString());
        }

    }

    public void transitConfigTripStartView() {

        newbtn_timer.setVisibility(View.VISIBLE);
//        btntimer.performClick();
        holdWaitArea.setVisibility(View.VISIBLE);
//        callsetTimeApi(false);
        //countDownStart();
        transitCountDownStart();

        String TimeState = data_trip.get("TimeState");
        if (TimeState != null && !TimeState.equals("")) {
            if (TimeState.equalsIgnoreCase("Resume")) {

                isresume = true;
                newbtn_timer.setText(generalFunc.retrieveLangLBl("stop", "LBL_STOP"));
                newbtn_timer.setVisibility(View.VISIBLE);

            } else {
                if (timerrequesttask != null) {
                    timerrequesttask.stopRepeatingTask();
                    timerrequesttask = null;
                }

                isresume = false;
                newbtn_timer.setText(LBL_WAIT);
                newbtn_timer.setVisibility(View.VISIBLE);

            }
        } else {
            newbtn_timer.setText(LBL_WAIT);
        }

        String TotalSeconds = data_trip.get("TotalSeconds");
        if (TotalSeconds != null && !TotalSeconds.equals("")) {
            i = Integer.parseInt(TotalSeconds);
            setTransitTimerValues();
        }

        String iTripTimeId = data_trip.get("iTripTimeId");
        if (iTripTimeId != null && !iTripTimeId.equals("")) {
            TripTimeId = iTripTimeId;
            //  countDownStart();
        }
        isTripStart = true;
        startTripSlideButton.setVisibility(View.GONE);
        endTripSlideButton.setVisibility(View.VISIBLE);

        if (eType.equalsIgnoreCase(Utils.CabGeneralType_Ride) && Utils.checkText(data_trip.get("iStopId")) && currentStopOverPoint < totalStopOverPoint) {
            dropAllIconArea.setVisibility(View.VISIBLE);
        }
        (findViewById(R.id.navigateArea)).setVisibility(eFly ? View.GONE : View.VISIBLE);
        isendslide = true;
        invalidateOptionsMenu();
        imageslide.setImageResource(R.mipmap.ic_trip_btn);
    }

    public void configTripStartView() {

        isresume = true;
        //  btntimer.setVisibility(View.VISIBLE);
        //countDownStart();

        isTripStart = true;
        startTripSlideButton.setVisibility(View.GONE);
        endTripSlideButton.setVisibility(View.VISIBLE);

        if (eType.equalsIgnoreCase(Utils.CabGeneralType_Ride) && Utils.checkText(data_trip.get("iStopId")) && currentStopOverPoint < totalStopOverPoint) {
            dropAllIconArea.setVisibility(View.VISIBLE);
        }
        (findViewById(R.id.navigateArea)).setVisibility(eFly ? View.GONE : View.VISIBLE);
        isendslide = true;
        invalidateOptionsMenu();
        imageslide.setImageResource(R.mipmap.ic_trip_btn);
    }

    public void cancelTrip(String reason, String comment) {
        isTripCancelPressed = true;
        this.reason = reason;
        this.comment = comment;

        if (eType.equals("UberX") && data_trip.get("eAfterUpload").equalsIgnoreCase("Yes")
                && data_trip != null && data_trip.get("eAfterUpload").equalsIgnoreCase("Yes")) {
            //&& generalFunc.retrieveValue(Utils.PHOTO_UPLOAD_SERVICE_ENABLE_KEY).equalsIgnoreCase("Yes")) {
            takeAndUploadPic(getActContext(), "after");
        } else {
            if (eType.equals("UberX")) {
                //  getCurrentPriceApi();
                endTrip();

            } else {
                endTrip();
            }
        }
        //endTrip();
    }

    public void endTrip() {

        if (!REQUEST_TYPE.equals(Utils.eType_Multi_Delivery)) {
           /* if (eType.equalsIgnoreCase(Utils.CabGeneralType_Ride) && Utils.checkText(data_trip.get("iStopId")) && isDropAll) {
                buildMsgOnDropAllBtn();
            }else {*/
            endTripFinal();
            //}
        } else {
            if (data_trip.containsKey("ePaymentByReceiver") && data_trip.get("ePaymentByReceiver").trim().equalsIgnoreCase("Yes")) {
                buildMsgOnEndBtn();
            } else {
                endTripFinal();
            }
        }


    }

    public void buildMsgOnDropAllBtn() {
        final GenerateAlertBox generateAlert = new GenerateAlertBox(getActContext());
        generateAlert.setCancelable(false);
        generateAlert.setBtnClickList(btn_id -> {
            if (btn_id == 0) {
                generateAlert.closeAlertBox();
            } else {
                endTripFinal();
                generateAlert.closeAlertBox();
            }

        });
        generateAlert.setContentMessage("", generalFunc.retrieveLangLBl("", "LBL_MULTI_DROP_ALL_CONFIRM_TXT"));
        generateAlert.setPositiveBtn(generalFunc.retrieveLangLBl("", "LBL_BTN_OK_TXT"));
        generateAlert.setNegativeBtn(generalFunc.retrieveLangLBl("Cancel", "LBL_CANCEL_TXT"));

        generateAlert.showAlertBox();
    }

    public void buildMsgOnEndBtn() {
        final GenerateAlertBox generateAlert = new GenerateAlertBox(getActContext());
        generateAlert.setCancelable(false);
        generateAlert.setBtnClickList(btn_id -> {
            if (btn_id == 0) {
                generateAlert.closeAlertBox();
                endTripSlideButton.resetButtonView(endTripSlideButton.btnText.getText().toString());
            } else {
                endTripFinal();
                generateAlert.closeAlertBox();
            }

        });
        generateAlert.setContentMessage("", generalFunc.retrieveLangLBl("", "LBL_MULTI_PAYMENT_COLLECTED_MSG_TXT"));
        generateAlert.setPositiveBtn(generalFunc.retrieveLangLBl("", "LBL_BTN_OK_TXT"));
        generateAlert.setNegativeBtn(generalFunc.retrieveLangLBl("Cancel", "LBL_CANCEL_TXT"));

        generateAlert.showAlertBox();
    }

    private void endTripFinal() {

        if (userLocation == null) {
            generalFunc.showMessage(generalFunc.getCurrentView(ActiveTripActivity.this), generalFunc.retrieveLangLBl("", "LBL_NO_LOCATION_FOUND_TXT"));
            return;
        }

        ArrayList<Location> store_locations = new ArrayList<>();
        ArrayList<String> store_locations_latitude = new ArrayList<String>();
        ArrayList<String> store_locations_longitude = new ArrayList<String>();

        store_locations.addAll(GetLocationUpdates.getInstance().getListOfTripLocations());

        if (store_locations.size() > 0) {

            for (int i = 0; i < store_locations.size(); i++) {

                Location location = store_locations.get(i);

                store_locations_latitude.add("" + location.getLatitude());
                store_locations_longitude.add("" + location.getLongitude());
            }

        }

        if (userLocation != null) {
            getDestinationAddress(store_locations_latitude, store_locations_longitude, "" + userLocation.getLatitude(), "" + userLocation.getLongitude());
        }
    }

    public void getDestinationAddress(final ArrayList<String> store_locations_latitude, final ArrayList<String> store_locations_longitude,
                                      String endLatitude, String endLongitude) {

        final MyProgressDialog myPDialog = showLoader();

        GetAddressFromLocation getAddressFromLocation = new GetAddressFromLocation(getActContext(), generalFunc);
        getAddressFromLocation.setLocation(generalFunc.parseDoubleValue(0.0, endLatitude), generalFunc.parseDoubleValue(0.0, endLongitude));
        getAddressFromLocation.setIsDestination(true);
        getAddressFromLocation.setAddressList((address, latitude, longitude,geocodeobject) -> {

            closeLoader(myPDialog);

            if (address.equals("")) {
                generalFunc.showError();
                endTripSlideButton.resetButtonView(endTripSlideButton.btnText.getText().toString());
            } else {
                setTripEnd(store_locations_latitude, store_locations_longitude,
                        "" + userLocation.getLatitude(), "" + userLocation.getLongitude(), address);
            }
        });
        getAddressFromLocation.execute();
    }

    public MyProgressDialog showLoader() {
        MyProgressDialog myPDialog = new MyProgressDialog(getActContext(), false, generalFunc.retrieveLangLBl("Loading", "LBL_LOADING_TXT"));
        myPDialog.show();

        return myPDialog;
    }

    public void closeLoader(MyProgressDialog myPDialog) {
        myPDialog.close();
    }

    public void setTripEnd(ArrayList<String> store_locations_latitude, ArrayList<String> store_locations_longitude, String endLatitude, String endLongitude, String destAddress) {

        if (!TextUtils.isEmpty(isFrom) && imageType.equalsIgnoreCase("after")) {

            ArrayList<String[]> paramsList = new ArrayList<>();
            paramsList.add(generalFunc.generateImageParams("type", "ProcessEndTrip"));
            paramsList.add(generalFunc.generateImageParams("TripId", tripId));
            paramsList.add(generalFunc.generateImageParams("latList", store_locations_latitude.toString().replace("[", "").replace("]", "")));
            paramsList.add(generalFunc.generateImageParams("lonList", store_locations_longitude.toString().replace("[", "").replace("]", "")));
            paramsList.add(generalFunc.generateImageParams("PassengerId", data_trip.get("PassengerId")));
            paramsList.add(generalFunc.generateImageParams("DriverId", generalFunc.getMemberId()));
            paramsList.add(generalFunc.generateImageParams("dAddress", destAddress));
            paramsList.add(generalFunc.generateImageParams("dest_lat", endLatitude));
            paramsList.add(generalFunc.generateImageParams("dest_lon", endLongitude));
            paramsList.add(generalFunc.generateImageParams("waitingTime", "" + getWaitingTime()));
            paramsList.add(generalFunc.generateImageParams("fMaterialFee", additonallist.get(0).toString()));
            paramsList.add(generalFunc.generateImageParams("fMiscFee", additonallist.get(1).toString()));
            paramsList.add(generalFunc.generateImageParams("fDriverDiscount", additonallist.get(2).toString()));
            paramsList.add(Utils.generateImageParams("iMemberId", generalFunc.getMemberId()));
            paramsList.add(Utils.generateImageParams("MemberType", Utils.app_type));
            paramsList.add(Utils.generateImageParams("tSessionId", generalFunc.getMemberId().equals("") ? "" : generalFunc.retrieveValue(Utils.SESSION_ID_KEY)));
            paramsList.add(Utils.generateImageParams("GeneralUserType", Utils.app_type));
            paramsList.add(Utils.generateImageParams("GeneralMemberId", generalFunc.getMemberId()));
            if (isTripCancelPressed == true) {
                paramsList.add(generalFunc.generateImageParams("isTripCanceled", "true"));
                paramsList.add(generalFunc.generateImageParams("Comment", comment));
                //paramsList.add(generalFunc.generateImageParams("Reason", reason));

                paramsList.add(generalFunc.generateImageParams("iCancelReasonId", selectedItemId));
            }

            new UploadProfileImage(ActiveTripActivity.this, selectedImagePath, Utils.TempProfileImageName, paramsList, imageType).execute();

        } else {
            HashMap<String, String> parameters = new HashMap<String, String>();
            parameters.put("type", "ProcessEndTrip");
            parameters.put("TripId", tripId);
            parameters.put("latList", store_locations_latitude.toString().replace("[", "").replace("]", ""));
            parameters.put("lonList", store_locations_longitude.toString().replace("[", "").replace("]", ""));
            parameters.put("PassengerId", data_trip.get("PassengerId"));
            parameters.put("DriverId", generalFunc.getMemberId());
            parameters.put("dAddress", destAddress);
            parameters.put("dest_lat", endLatitude);
            parameters.put("dest_lon", endLongitude);
            parameters.put("waitingTime", "" + getWaitingTime());

            parameters.put("fMaterialFee", additonallist.get(0).toString());
            parameters.put("fMiscFee", additonallist.get(1).toString());
            parameters.put("fDriverDiscount", additonallist.get(2).toString());
            if (eType.equalsIgnoreCase(Utils.eType_Multi_Delivery)) {
                parameters.put("iTripDeliveryLocationId", iTripDeliveryLocationId);
            }
            /*Multistop over*/
            boolean isMspTrip = eType.equalsIgnoreCase(Utils.CabGeneralType_Ride) && Utils.checkText(data_trip.get("iStopId"));
            if (isMspTrip) {
                parameters.put("iStopId", data_trip.get("iStopId"));

                if (isDropAll) {
                    parameters.put("isDropAll", "" + isDropAll);
                }

            }
            if (isTripCancelPressed == true) {
                parameters.put("isTripCanceled", "true");
                parameters.put("Comment", comment);
                // parameters.put("Reason", reason);
                parameters.put("iCancelReasonId", selectedItemId);
            }

            ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(), parameters, isMspTrip);
            exeWebServer.setLoaderConfig(getActContext(), true, generalFunc);
            exeWebServer.setDataResponseListener(responseString -> endTripResponse(responseString));
            exeWebServer.execute();
        }
    }

    private void endTripResponse(String responseString) {

        if (responseString != null && !responseString.equals("")) {
            boolean isDataAvail = GeneralFunctions.checkDataAvail(Utils.action_str, responseString);

            if (isDataAvail == true) {
                generalFunc.saveGoOnlineInfo();
                if (timerrequesttask != null) {
                    try {
                        timerrequesttask.stopRepeatingTask();
                        timerrequesttask = null;
                    } catch (Exception e) {

                    }
                }
                closeuploadServicePicAlertBox();
                stopProcess();

                GetLocationUpdates.getInstance().setTripStartValue(false, false, "");

                if (REQUEST_TYPE.equalsIgnoreCase(Utils.eType_Multi_Delivery)) {
                    MyApp.getInstance().restartWithGetDataApp(false);
                } else {
                    MyApp.getInstance().restartWithGetDataApp();
                }

            } else {
                String msg_str = generalFunc.getJsonValue(Utils.message_str, responseString);
                //Multi StopOver
                boolean isMspTrip = REQUEST_TYPE.equalsIgnoreCase(Utils.CabGeneralType_Ride) && Utils.checkText(data_trip.get("iStopId"));
                if (msg_str.equalsIgnoreCase("DO_RESTART") && isMspTrip) {
                    MyApp.getInstance().restartWithGetDataApp(false);
                } else {
                    if (msg_str.equals(Utils.GCM_FAILED_KEY) || msg_str.equals(Utils.APNS_FAILED_KEY) || msg_str.equals("LBL_SERVER_COMM_ERROR")) {
                        generalFunc.restartApp();
                    } else {

                        endTripSlideButton.resetButtonView(endTripSlideButton.btnText.getText().toString());
                        GetLocationUpdates.getInstance().setTripStartValue(true, true, data_trip.get("TripId"));
                        generalFunc.showGeneralMessage("",
                                generalFunc.retrieveLangLBl("", generalFunc.getJsonValue(Utils.message_str, responseString)));
                    }
                }

            }
        } else {
            GetLocationUpdates.getInstance().setTripStartValue(true, true, data_trip.get("TripId"));
            generalFunc.showError();
            endTripSlideButton.resetButtonView(endTripSlideButton.btnText.getText().toString());
        }
    }

    private long getWaitingTime() {
        long waitingTime = 0;
        if (generalFunc != null && generalFunc.containsKey(Utils.DriverWaitingTime)) {
            waitingTime = GeneralFunctions.parseLongValue(0, generalFunc.retrieveValue(Utils.DriverWaitingTime)) / 60000;
        }
        return waitingTime;
    }

    @Override
    public void onBackPressed() {
        return;
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (this.userLocation != null) {
            onLocationUpdate(this.userLocation);
        }

        if (updateDirections != null) {
            updateDirections.scheduleDirectionUpdate();
        }

        NavigationSensor.getInstance().configSensor(true);
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (updateDirections != null) {
            updateDirections.releaseTask();
        }

        NavigationSensor.getInstance().configSensor(false);
    }

    public void stopProcess() {
        if (updateDirections != null) {
            updateDirections.releaseTask();
            updateDirections = null;
        }

        if (GetLocationUpdates.retrieveInstance() != null) {
            GetLocationUpdates.retrieveInstance().stopLocationUpdates(this);
        }
        Utils.runGC();
    }

    @Override
    protected void onDestroy() {
        stopAllProcess();
        super.onDestroy();
    }

    private void stopAllProcess() {
        stopProcess();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (startTripSlideButton.getVisibility() == View.VISIBLE) {
            startTripSlideButton.resetButtonView(startTripSlideButton.btnText.getText().toString());
        }
        if (endTripSlideButton.getVisibility() == View.VISIBLE) {
            endTripSlideButton.resetButtonView(endTripSlideButton.btnText.getText().toString());
        }
    }

    public void takeAndUploadPic(final Context mContext, final String picType) {

        boolean isStoragePermissionAvail = generalFunc.isCameraStoragePermissionGranted();
        if (!isStoragePermissionAvail) {
            return;
        }
        generalFunc.isCameraStoragePermissionGranted();
        imageType = picType;
        isFrom = "";
        selectedImagePath = "";

        uploadServicePicAlertBox = new Dialog(mContext, R.style.Theme_Dialog);
        uploadServicePicAlertBox.requestWindowFeature(Window.FEATURE_NO_TITLE);
        uploadServicePicAlertBox.setCancelable(false);
        uploadServicePicAlertBox.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        uploadServicePicAlertBox.setContentView(R.layout.design_upload_service_pic);

        MTextView titleTxt = (MTextView) uploadServicePicAlertBox.findViewById(R.id.titleTxt);
        final MTextView uploadStatusTxt = (MTextView) uploadServicePicAlertBox.findViewById(R.id.uploadStatusTxt);
        MTextView uploadTitleTxt = (MTextView) uploadServicePicAlertBox.findViewById(R.id.uploadTitleTxt);
        ImageView backImgView = (ImageView) uploadServicePicAlertBox.findViewById(R.id.backImgView);

        if (generalFunc.isRTLmode()) {
            backImgView.setRotation(180);
        }
        MTextView skipTxt = (MTextView) uploadServicePicAlertBox.findViewById(R.id.skipTxt);
        final ImageView uploadImgVIew = (ImageView) uploadServicePicAlertBox.findViewById(R.id.uploadImgVIew);
        LinearLayout uploadImgArea = (LinearLayout) uploadServicePicAlertBox.findViewById(R.id.uploadImgArea);
        MButton btn_type2 = ((MaterialRippleLayout) uploadServicePicAlertBox.findViewById(R.id.btn_type2)).getChildView();

        titleTxt.setText(generalFunc.retrieveLangLBl("", "LBL_UPLOAD_IMAGE_SERVICE"));
        skipTxt.setText(generalFunc.retrieveLangLBl("", "LBL_SKIP_TXT"));

        if (picType.equalsIgnoreCase("before")) {
            uploadTitleTxt.setText(generalFunc.retrieveLangLBl("Click and upload photo of your car before your service", "LBL_UPLOAD_SERVICE_BEFORE_TXT"));
            btn_type2.setText(generalFunc.retrieveLangLBl("Save Photo", "LBL_SAVE_PHOTO_START_SERVICE_TXT"));
        } else {
            uploadTitleTxt.setText(generalFunc.retrieveLangLBl("Click and upload photo of your car after your service", "LBL_UPLOAD_SERVICE_AFTER_TXT"));
            btn_type2.setText(generalFunc.retrieveLangLBl("Save Photo", "LBL_SAVE_PHOTO_END_SERVICE_TXT"));
        }

        btn_type2.setId(Utils.generateViewId());
        btn_type2.setTextSize(16);
        uploadImgArea.setOnClickListener(view -> {

            if (generalFunc.isCameraPermissionGranted()) {
                uploadServicePicAlertBox.findViewById(R.id.uploadStatusTxt).setVisibility(View.GONE);
                new ImageSourceDialog().run();
            } else {
                uploadStatusTxt.setVisibility(View.VISIBLE);
                generalFunc.showMessage(uploadStatusTxt, "Allow this app to use camera.");
            }
        });
        btn_type2.setOnClickListener(view -> {

            if (TextUtils.isEmpty(selectedImagePath)) {
                uploadStatusTxt.setVisibility(View.VISIBLE);
                generalFunc.showMessage(uploadStatusTxt, "Please select image");

            } else if (picType.equalsIgnoreCase("after")) {
                uploadStatusTxt.setVisibility(View.GONE);
                if (eType.equals("UberX")) {
                    endTrip();
                } else {
                    endTrip();
                }
            } else {
                uploadStatusTxt.setVisibility(View.GONE);
                setTripStart();
            }
        });

        skipTxt.setOnClickListener(view -> {

            isFrom = "";
            selectedImagePath = "";
            uploadImgVIew.setImageURI(null);

            if (picType.equalsIgnoreCase("after")) {
                if (eType.equals("UberX")) {
                    endTrip();
                } else {
                    endTrip();
                }
            } else {
                setTripStart();
            }

        });

        backImgView.setOnClickListener(v -> {
            closeuploadServicePicAlertBox();
            if (startTripSlideButton.getVisibility() == View.VISIBLE) {
                startTripSlideButton.resetButtonView(startTripSlideButton.btnText.getText().toString());
            }
            if (endTripSlideButton.getVisibility() == View.VISIBLE) {
                endTripSlideButton.resetButtonView(endTripSlideButton.btnText.getText().toString());
            }
        });

        if (generalFunc.isRTLmode()) {
            generalFunc.forceRTLIfSupported(uploadServicePicAlertBox);
        }

        uploadServicePicAlertBox.show();

    }

    public void chooseFromCamera() {

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);

        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);

        startActivityForResult(intent, CAMERA_CAPTURE_IMAGE_REQUEST_CODE);
    }

//    OVER UPLOAD SERVICE PIC AREA

    private boolean isDeviceSupportCamera() {
        if (getApplicationContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            return false;
        }
    }

    public Uri getOutputMediaFileUri(int type) {
        return FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".provider", getOutputMediaFile(type));
    }

    private File getOutputMediaFile(int type) {

        // External sdcard location
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                IMAGE_DIRECTORY_NAME);

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator + "IMG_" + timeStamp + ".jpg");
            pathForCameraImage = mediaFile.getAbsolutePath();
        } else {
            return null;
        }

        return mediaFile;
    }

    public void chooseFromGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_PICTURE);
    }

    public void closeuploadServicePicAlertBox() {
        if (uploadServicePicAlertBox != null) {
            uploadServicePicAlertBox.dismiss();
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        // get the file url
        fileUri = savedInstanceState.getParcelable("file_uri");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if (requestCode == Utils.SEARCH_DEST_LOC_REQ_CODE && resultCode == RESULT_OK && data != null) {
            latitude = data.getStringExtra("Latitude");
            longitirude = data.getStringExtra("Longitude");
            address = data.getStringExtra("Address");

            //addDestination(latitude, longitirude, address);
            getTollcostValue();
        } else {


            if (requestCode == CAMERA_CAPTURE_IMAGE_REQUEST_CODE && resultCode == RESULT_OK) {
                boolean isStoragePermissionAvail = generalFunc.isCameraStoragePermissionGranted();
                if (!isStoragePermissionAvail) {
                    return;
                }

                ArrayList<String[]> paramsList = new ArrayList<>();
                paramsList.add(generalFunc.generateImageParams("iMemberId", generalFunc.getMemberId()));
                paramsList.add(generalFunc.generateImageParams("MemberType", Utils.app_type));
                paramsList.add(generalFunc.generateImageParams("type", "uploadImage"));

                if (isStoragePermissionAvail) {

                    isFrom = "Camera";
                    if (fileUri != null && uploadServicePicAlertBox != null) {
//                        selectedImagePath = ImageFilePath.getPath(getApplicationContext(), fileUri);

                        if (pathForCameraImage.equalsIgnoreCase("")) {
                            selectedImagePath = ImageFilePath.getPath(getActContext(), fileUri);
                        } else {
                            selectedImagePath = pathForCameraImage;
                        }

                        try {
                            BitmapFactory.Options options = new BitmapFactory.Options();
                            options.inJustDecodeBounds = true;
                            BitmapFactory.decodeFile(selectedImagePath, options);

                            int imageHeight = options.outHeight;
                            int imageWidth = options.outWidth;

                            double ratioOfImage = (double) imageWidth / (double) imageHeight;
                            double widthOfImage = ratioOfImage * Utils.dipToPixels(getActContext(), 200);

                            Picasso.get().load(fileUri).resize((int) widthOfImage, Utils.dipToPixels(getActContext(), 200)).into(((ImageView) uploadServicePicAlertBox.findViewById(R.id.uploadImgVIew)));
                        } catch (Exception e) {
                            Picasso.get().load(fileUri).resize(Utils.dipToPixels(getActContext(), 400), Utils.dipToPixels(getActContext(), 200)).into(((ImageView) uploadServicePicAlertBox.findViewById(R.id.uploadImgVIew)));
                        }

                        uploadServicePicAlertBox.findViewById(R.id.camImgVIew).setVisibility(View.GONE);
                        uploadServicePicAlertBox.findViewById(R.id.ic_add).setVisibility(View.GONE);
                    }
                }
            } else if (resultCode == RESULT_CANCELED) {

            } else if (requestCode == SELECT_PICTURE && resultCode == RESULT_OK) {
                boolean isStoragePermissionAvail = generalFunc.isCameraStoragePermissionGranted();
                if (!isStoragePermissionAvail) {
                    return;
                }

                ArrayList<String[]> paramsList = new ArrayList<>();
                paramsList.add(generalFunc.generateImageParams("iMemberId", generalFunc.getMemberId()));
                paramsList.add(generalFunc.generateImageParams("type", "uploadImage"));
                paramsList.add(generalFunc.generateImageParams("MemberType", Utils.app_type));

                Uri selectedImageUri = data.getData();

                selectedImagePath = ImageFilePath.getPath(getApplicationContext(), selectedImageUri);

                if (selectedImagePath == null || selectedImagePath.equalsIgnoreCase("")) {
                    selectedImagePath = "";
                    try {
                        if (uploadServicePicAlertBox != null) {
                            uploadServicePicAlertBox.dismiss();
                        }
                    } catch (Exception e) {

                    }
                    generalFunc.showMessage(generalFunc.getCurrentView((Activity) getActContext()), generalFunc.retrieveLangLBl("Can't read selected image. Please try again.", "LBL_IMAGE_READ_FAILED"));
                    return;
                }

                if (isStoragePermissionAvail) {
                    isFrom = "Gallary";
                    if (selectedImageUri != null && uploadServicePicAlertBox != null) {

                        try {
                            BitmapFactory.Options options = new BitmapFactory.Options();
                            options.inJustDecodeBounds = true;
                            BitmapFactory.decodeFile(selectedImagePath, options);

                            int imageHeight = options.outHeight;
                            int imageWidth = options.outWidth;

                            double ratioOfImage = (double) imageWidth / (double) imageHeight;
                            double widthOfImage = ratioOfImage * Utils.dipToPixels(getActContext(), 200);

                            Picasso.get().load(selectedImageUri).resize((int) widthOfImage, Utils.dipToPixels(getActContext(), 200)).into(((ImageView) uploadServicePicAlertBox.findViewById(R.id.uploadImgVIew)));


                        } catch (Exception e) {
                            Picasso.get().load(selectedImageUri).resize(Utils.dipToPixels(getActContext(), 400), Utils.dipToPixels(getActContext(), 200)).into(((ImageView) uploadServicePicAlertBox.findViewById(R.id.uploadImgVIew)));
                        }

                        uploadServicePicAlertBox.findViewById(R.id.camImgVIew).setVisibility(View.GONE);
                        uploadServicePicAlertBox.findViewById(R.id.ic_add).setVisibility(View.GONE);
                    }
                }
            } else if (requestCode == Utils.REQUEST_CODE_GPS_ON) {
                handleNoLocationDial();
            }
        }
    }

    public void getTollcostValue() {

        if (generalFunc.retrieveValue(Utils.ENABLE_TOLL_COST).equalsIgnoreCase("Yes")) {

            double sourcelatitude = GeneralFunctions.parseDoubleValue(0.0, data_trip.get("sourceLatitude"));
            double sourcelongitude = GeneralFunctions.parseDoubleValue(0.0, data_trip.get("sourceLongitude"));

            String url = CommonUtilities.TOLLURL + generalFunc.retrieveValue(Utils.TOLL_COST_APP_ID)
                    + "&app_code=" + generalFunc.retrieveValue(Utils.TOLL_COST_APP_CODE) + "&waypoint0=" + sourcelatitude
                    + "," + sourcelongitude + "&waypoint1=" + latitude + "," + longitirude + "&mode=fastest;car";

            ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(), url, true);
            exeWebServer.setLoaderConfig(getActContext(), true, generalFunc);
            exeWebServer.setDataResponseListener(responseString -> {

                if (responseString != null && !responseString.equals("")) {

                    if (generalFunc.getJsonValue("onError", responseString).equalsIgnoreCase("FALSE")) {
                        try {

                            String costs = generalFunc.getJsonValue("costs", responseString);

                            String currency = generalFunc.getJsonValue("currency", costs);
                            String details = generalFunc.getJsonValue("details", costs);
                            String tollCost = generalFunc.getJsonValue("tollCost", details);
                            if (currency != null && !currency.equals("")) {
                                tollcurrancy = currency;
                            }
                            if (tollCost != null && !tollCost.equals("") && !tollCost.equals("0.0")) {
                                tollamount = GeneralFunctions.parseDoubleValue(0.0, tollCost);
                            }

                            addDestination(latitude, longitirude, address);
                        } catch (Exception e) {
                            tollcurrancy = "";
                            tollamount = 0.0;
                            tollcurrancy = "";
                            addDestination(latitude, longitirude, address);
                        }
                    } else {
                        tollcurrancy = "";
                        tollamount = 0.0;
                        tollcurrancy = "";
                        addDestination(latitude, longitirude, address);
                    }
                } else {
                    tollcurrancy = "";
                    tollamount = 0.0;
                    tollcurrancy = "";
                    addDestination(latitude, longitirude, address);
                }

            });
            exeWebServer.execute();


        } else {
            addDestination(latitude, longitirude, address);
        }

    }

    public void TollTaxDialog() {

        if (tollamount != 0.0 && tollamount != 0 && tollamount != 0.00) {
            androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getActContext());

            LayoutInflater inflater = (LayoutInflater) getActContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View dialogView = inflater.inflate(R.layout.dialog_tolltax, null);

            final MTextView tolltaxTitle = (MTextView) dialogView.findViewById(R.id.tolltaxTitle);
            final MTextView tollTaxMsg = (MTextView) dialogView.findViewById(R.id.tollTaxMsg);
            final MTextView tollTaxpriceTxt = (MTextView) dialogView.findViewById(R.id.tollTaxpriceTxt);
            final MTextView cancelTxt = (MTextView) dialogView.findViewById(R.id.cancelTxt);

            final CheckBox checkboxTolltax = (CheckBox) dialogView.findViewById(R.id.checkboxTolltax);

            checkboxTolltax.setOnCheckedChangeListener((buttonView, isChecked) -> {

                if (checkboxTolltax.isChecked()) {
                    istollIgnore = true;
                } else {
                    istollIgnore = false;
                }

            });


            MButton btn_type2 = ((MaterialRippleLayout) dialogView.findViewById(R.id.btn_type2)).getChildView();
            int submitBtnId = Utils.generateViewId();
            btn_type2.setId(submitBtnId);
            btn_type2.setText(generalFunc.retrieveLangLBl("", "LBL_CONTINUE_BTN"));
            btn_type2.setOnClickListener(v -> {
                tolltax_dialog.dismiss();
                eTollConfirmByUser = "Yes";

                addDestination(latitude, longitirude, address);


            });


            builder.setView(dialogView);
            tolltaxTitle.setText(generalFunc.retrieveLangLBl("", "LBL_TOLL_ROUTE"));
            tollTaxMsg.setText(generalFunc.retrieveLangLBl("", "LBL_TOLL_PRICE_DESC"));

            tollTaxMsg.setText(generalFunc.retrieveLangLBl("", "LBL_TOLL_PRICE_DESC"));

            tollTaxpriceTxt.setText(
                    generalFunc.retrieveLangLBl("Total toll price", "LBL_TOLL_PRICE_TOTAL") + ": " + tollcurrancy + " " + tollamount);

            checkboxTolltax.setText(generalFunc.retrieveLangLBl("", "LBL_IGNORE_TOLL_ROUTE"));
            cancelTxt.setText(generalFunc.retrieveLangLBl("", "LBL_CANCEL_TXT"));

            cancelTxt.setOnClickListener(v -> {
                tolltax_dialog.dismiss();
                istollIgnore = false;
            });

            tolltax_dialog = builder.create();
            if (generalFunc.isRTLmode()) {
                generalFunc.forceRTLIfSupported(tolltax_dialog);
            }
            tolltax_dialog.show();
        } else {
            addDestination(latitude, longitirude, address);
        }

    }

    public void handleImgUploadResponse(String responseString, String imageUploadedType) {

        if (responseString != null && !responseString.equals("")) {

            if (imageType.equalsIgnoreCase("after")) {
                endTripResponse(responseString);
            } else if (imageType.equalsIgnoreCase(imageUploadedType)) {
                startTripResponse(responseString);
            }
        } else {
            generalFunc.showError();
        }
    }

    public void countDownStop() {
        if (timerrequesttask != null) {
            callsetTimeApi(false);
        }
    }

    public void countDownStart() {
        if (timerrequesttask != null) {
            timerrequesttask.stopRepeatingTask();
            timerrequesttask = null;
        }

        timerrequesttask = new UpdateFrequentTask(1000);
        timerrequesttask.startRepeatingTask();
        timerrequesttask.setTaskRunListener(() -> {
            i++;
            setTimerValues();
        });

    }

    public void transitCountDownStart() {
        if (timerrequesttask != null) {
            timerrequesttask.stopRepeatingTask();
            timerrequesttask = null;
        }

        timerrequesttask = new UpdateFrequentTask(1000);
        timerrequesttask.startRepeatingTask();
        timerrequesttask.setTaskRunListener(() -> {
            i++;
            setTransitTimerValues();
        });

    }

    private void setTransitTimerValues() {
        newtvHour.setText("" + String.format("%02d", i / 3600));
        newtvMinute.setText("" + String.format("%02d", (i % 3600) / 60));
        newtvSecond.setText("" + String.format("%02d", i % 60));


        Logger.d("setTransitTimerValues", "::" + String.format("%02d", i / 3600) + "::" + String.format("%02d", i % 60));
    }

    private void setTimerValues() {
        tvHour.setText(generalFunc.convertNumberWithRTL("" + String.format("%02d", i / 3600)));
        tvMinute.setText(generalFunc.convertNumberWithRTL("" + String.format("%02d", (i % 3600) / 60)));
        tvSecond.setText(generalFunc.convertNumberWithRTL("" + String.format("%02d", i % 60)));
    }

    private void setView() {
        onGoingTripDetailAdapter = new OnGoingTripDetailAdapter(getActContext(), list, generalFunc);
        onGoingTripsDetailListRecyclerView.setAdapter(onGoingTripDetailAdapter);
        onGoingTripDetailAdapter.notifyDataSetChanged();

        // RecyclerView.ItemDecoration itemDecoration = new DividerItemDecoration(getActContext(), DividerItemDecoration.VERTICAL_LIST);
        //onGoingTripsDetailListRecyclerView.addItemDecoration(itemDecoration);
    }

    private void callsetTimeApi(final boolean isresumeGet) {
        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "SetTimeForTrips");
        parameters.put("eType", eType);
        parameters.put("iUserId", data_trip.get("PassengerId"));
        parameters.put("iTripId", tripId);
        if (!isresumeGet) {
            parameters.put("iTripTimeId", TripTimeId);
        }

        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(), parameters);
        exeWebServer.setLoaderConfig(getActContext(), true, generalFunc);
        exeWebServer.setDataResponseListener(responseString -> {

            boolean isDataAvail = GeneralFunctions.checkDataAvail(Utils.action_str, responseString);

            if (isDataAvail) {
                String msg_str = generalFunc.getJsonValue(Utils.message_str, responseString);

                if (msg_str != null && !msg_str.equals("true") && !msg_str.equals("")) {
                    TripTimeId = msg_str;
                }
                String temptime = generalFunc.getJsonValue("totalTime", responseString);
                i = Integer.parseInt(temptime);
                setTimerValues();

                if (isresumeGet) {
                    if (eType.equalsIgnoreCase(Utils.CabGeneralType_UberX)) {
                        btntimer.setVisibility(View.VISIBLE);
                        countDownStart();
                    } else {

                        transitCountDownStart();
                    }
                    btntimer.setText(generalFunc.retrieveLangLBl("pause", "LBL_PAUSE_TEXT"));
                } else {
                    if (timerrequesttask != null) {
                        timerrequesttask.stopRepeatingTask();
                        timerrequesttask = null;
                    }
                }

            }
        });
        exeWebServer.execute();
    }

    public void openNavigationDialog(final String dest_lat, final String dest_lon) {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getActContext());

        LayoutInflater inflater = (LayoutInflater) getActContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View dialogView = inflater.inflate(R.layout.dialog_selectnavigation_view, null);

        final MTextView NavigationTitleTxt = (MTextView) dialogView.findViewById(R.id.NavigationTitleTxt);
        final MTextView wazemapTxtView = (MTextView) dialogView.findViewById(R.id.wazemapTxtView);
        final MTextView googlemmapTxtView = (MTextView) dialogView.findViewById(R.id.googlemmapTxtView);
        final RadioButton radiogmap = (RadioButton) dialogView.findViewById(R.id.radiogmap);
        final RadioButton radiowazemap = (RadioButton) dialogView.findViewById(R.id.radiowazemap);
        ImageView cancelImg = (ImageView) dialogView.findViewById(R.id.cancelImg);
        radiogmap.setOnClickListener(v -> googlemmapTxtView.performClick());
        radiowazemap.setOnClickListener(v -> wazemapTxtView.performClick());

        builder.setView(dialogView);
        NavigationTitleTxt.setText(generalFunc.retrieveLangLBl("Choose Option", "LBL_CHOOSE_OPTION"));
        googlemmapTxtView.setText(generalFunc.retrieveLangLBl("Google map navigation", "LBL_NAVIGATION_GOOGLE_MAP"));
        wazemapTxtView.setText(generalFunc.retrieveLangLBl("Waze navigation", "LBL_NAVIGATION_WAZE"));

        cancelImg.setOnClickListener(v -> {

            list_navigation.dismiss();
        });
        googlemmapTxtView.setOnClickListener(v -> {
            try {
                list_navigation.dismiss();
                String url_view = "http://maps.google.com/maps?daddr=" + dest_lat + "," + dest_lon;
                (new StartActProcess(getActContext())).openURL(url_view, "com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
            } catch (Exception e) {
                generalFunc.showMessage(wazemapTxtView, generalFunc.retrieveLangLBl("Please install Google Maps in your device.", "LBL_INSTALL_GOOGLE_MAPS"));
            }
        });

        wazemapTxtView.setOnClickListener(v -> {
            try {

                String uri = "waze://?ll=" + dest_lat + "," + dest_lon + "&navigate=yes";
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                startActivity(intent);
                list_navigation.dismiss();

            } catch (Exception e) {
                generalFunc.showMessage(wazemapTxtView, generalFunc.retrieveLangLBl("Please install Waze navigation app in your device.", "LBL_INSTALL_WAZE"));
            }
        });


        list_navigation = builder.create();
        if (generalFunc.isRTLmode()) {
            generalFunc.forceRTLIfSupported(list_navigation);
        }
        list_navigation.getWindow().setBackgroundDrawable(getResources().getDrawable(R.drawable.all_roundcurve_card));
        list_navigation.show();
        list_navigation.setOnCancelListener(dialogInterface -> Utils.hideKeyboard(getActContext()));
    }

    public class setOnClickList implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            Utils.hideKeyboard(ActiveTripActivity.this);
            if (view.getId() == emeTapImgView.getId()) {
                Bundle bn = new Bundle();
                bn.putString("TripId", tripId);
                new StartActProcess(getActContext()).startActWithData(ConfirmEmergencyTapActivity.class, bn);
            } else if (view.getId() == viewDetailsImgView.getId()) {
                if (REQUEST_TYPE.equalsIgnoreCase(Utils.CabGeneralType_Ride) && Utils.checkText(data_trip.get("iStopId"))) {
                    Bundle bn = new Bundle();
                    bn.putString("TripId", data_trip.get("TripId"));
                    bn.putString("Status", "activeTrip");
                    bn.putSerializable("TRIP_DATA", data_trip);
                    new StartActProcess(getActContext()).startActWithData(ViewStopOverDetailsActivity.class, bn);

                } else {
                    Bundle bn = new Bundle();
                    bn.putString("TripId", data_trip.get("TripId"));
                    bn.putString("Status", "activeTrip");
                    bn.putSerializable("TRIP_DATA", data_trip);
                    new StartActProcess(getActContext()).startActWithData(ViewMultiDeliveryDetailsActivity.class, bn);
                }
            } else if (view.getId() == iv_callRicipient.getId()) {

                if (generalFunc.getJsonValue("RIDE_DRIVER_CALLING_METHOD", userProfileJsonObj).equals("Voip")) {
                    if (!generalFunc.isCallPermissionGranted(false)) {
                        generalFunc.isCallPermissionGranted(true);
                    } else {
                        ActiveTripActivity activity = (ActiveTripActivity) MyApp.getInstance().getCurrentAct();

                        if (new AppFunctions(getActContext()).checkSinchInstance(activity != null ? activity.getSinchServiceInterface() : null)) {
                            getSinchServiceInterface().getSinchClient().setPushNotificationDisplayName(generalFunc.retrieveLangLBl("", "LBL_INCOMING_CALL"));

                            Call call = activity.getSinchServiceInterface().callPhoneNumber(data_trip.get("vReceiverMobile"));
//                        Call call = activity.getSinchServiceInterface().callPhoneNumber(data_trip.get("vReceiverMobileOriginal"));
                            String callId = call.getCallId();
                            Intent callScreen = new Intent(getActContext(), CallScreenActivity.class);
                            callScreen.putExtra(SinchService.CALL_ID, callId);
                            callScreen.putExtra("vImage", "");
                            callScreen.putExtra("vName", data_trip.get("vReceiverName"));
                            startActivity(callScreen);
                        }
                    }
                } else {
                    call("+" + data_trip.get("vReceiverMobile"));
                }

            } else if (view.getId() == userTapImgView.getId()) {
                new OpenUserInstructionDialog(getActContext(), data_trip, generalFunc, false);
            }   /*Multistop over start*/ else if (view.getId() == dropAllIconArea.getId()) {
                //MTextView v = ((MTextView) findViewById(R.id.endTripTxt));
                Logger.d("BLINK", "" + isDropAll);
                startBlink(endTripSlideButton.btnText, !isDropAll);
            }
            /*Multistop over end*/
        }
    }

    private void startBlink(MTextView v, boolean startBlink) {
        if (Utils.checkText(data_trip.get("iStopId")) && currentStopOverPoint < totalStopOverPoint && !startBlink) {
            v.setText(LBL_CONFIRM_STOPOVER_1 + " " + LBL_CONFIRM_STOPOVER_2 + " " + generalFunc.convertNumberWithRTL(data_trip.get("currentStopOverPoint")));
        } else {
            v.setText(LBL_BTN_SLIDE_END_TRIP_TXT);
        }

        if (anim != null && !startBlink) {
            v.clearAnimation();
            /*if (canCancelAnimation()) {
                v.animate().cancel();
            }*/
            anim.setAnimationListener(null);
            anim = null;
            showView(v, startBlink);
            isDropAll = startBlink;
            return;
        }

        anim = new AlphaAnimation(0.0f, 1.0f);
        anim.setDuration(120); //You can manage the time of the blink with this parameter
        anim.setStartOffset(60);
        anim.setRepeatMode(Animation.REVERSE);
        anim.setRepeatCount(Animation.INFINITE);
        v.startAnimation(anim);
        anim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                showView(null, startBlink);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                showView(v, startBlink);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
    }

    private void showView(MTextView v, boolean startBlink) {
        if (v != null) {
            v.setVisibility(View.VISIBLE);
        } else {
            isDropAll = startBlink;
        }

        findViewById(R.id.dropCancel).setVisibility(startBlink ? View.VISIBLE : View.GONE);
        dropAllAreaUP.setVisibility(startBlink ? View.GONE : View.VISIBLE);

        if (startBlink) {
            generalFunc.showMessage(generalFunc.getCurrentView(ActiveTripActivity.this), generalFunc.retrieveLangLBl("", "LBL_MULTI_DROP_ALL_CONFIRM_TXT"));
        }

    }

    public static boolean canCancelAnimation() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH;
    }

    public class setOnClickAct implements View.OnClickListener {

        String dest_lat = "";
        String dest_lon = "";

        public setOnClickAct() {
        }


        public setOnClickAct(String dest_lat, String dest_lon) {
            this.dest_lat = dest_lat;
            this.dest_lon = dest_lon;
        }

        @Override
        public void onClick(View view) {
            int i = view.getId();
            if (i == R.id.navigateArea) {
                if (!isTripStart) {
                    String REQUEST_TYPE = data_trip.get("REQUEST_TYPE");
                    if (REQUEST_TYPE.equalsIgnoreCase(Utils.CabGeneralType_Ride)) {
                        generalFunc.showGeneralMessage("", generalFunc.retrieveLangLBl("", "LBL_NAVIGATION_ALERT"));
                    } else if (REQUEST_TYPE.equalsIgnoreCase(Utils.CabGeneralType_UberX)) {
                        generalFunc.showGeneralMessage("", generalFunc.retrieveLangLBl("", "LBL_NAVIGATION_BOOKING_ALERT"));
                    } else {
                        generalFunc.showGeneralMessage("", generalFunc.retrieveLangLBl("", "LBL_NAVIGATION_DELIVERY_ALERT"));
                    }
                } else {
                    openNavigationDialog(dest_lat, dest_lon);
                }
            } else if (i == R.id.destLocSearchArea) {


                if (data_trip.get("vTripPaymentMode").equalsIgnoreCase("Card") && data_trip.get("ePayWallet").equalsIgnoreCase("Yes")
                        && !generalFunc.getJsonValueStr("SYSTEM_PAYMENT_FLOW", userProfileJsonObj).equalsIgnoreCase("Method-1")) {
                    generalFunc.showGeneralMessage("", generalFunc.retrieveLangLBl("", "LBL_NOTE_ADD_DEST_FROM_DRIVER"));
                    return;


                }

                Bundle bn = new Bundle();
                bn.putString("isPickUpLoc", "false");

                if (userLocation != null) {
                    bn.putString("PickUpLatitude", "" + userLocation.getLatitude());
                    bn.putString("PickUpLongitude", "" + userLocation.getLongitude());
                }
                new StartActProcess(getActContext()).startActForResult(SearchPickupLocationActivity.class,
                        bn, Utils.SEARCH_DEST_LOC_REQ_CODE);
            } /*else if (i == tripStartBtnArea.getId()) {
                imageType = "before";

                if ((APP_TYPE.equalsIgnoreCase("UberX") || eType.equalsIgnoreCase(Utils.CabGeneralType_UberX)) &&
                        data_trip != null && data_trip.get("eBeforeUpload").equalsIgnoreCase("Yes")) {
                    takeAndUploadPic(getActContext(), "before");
                } else {
                    setTripStart();
                }
            }*/ /*else if (i == tripEndBtnArea.getId()) {
                imageType = "after";
                isTripCancelPressed = false;
                reason = "";
                comment = "";

                if (REQUEST_TYPE.equals("Deliver")) {
                    buildMsgOnDeliveryEnd();
                } else {
                    if (APP_TYPE.equalsIgnoreCase("UberX") || eType.equalsIgnoreCase(Utils.CabGeneralType_UberX) &&
                            data_trip != null && data_trip.get("eAfterUpload").equalsIgnoreCase("Yes")) {
                        takeAndUploadPic(getActContext(), "after");
                    } else {
                        if (eType.equals("UberX")) {
                            endTrip();
                        } else {
                            endTrip();
                        }
                    }
                }

            } */ else if (i == btntimer.getId()) {
                if (!isresume) {
                    callsetTimeApi(true);
                    btntimer.setText(generalFunc.retrieveLangLBl("pause", "LBL_PAUSE_TEXT"));
                    isresume = true;
                } else {
                    countDownStop();
                    btntimer.setText(generalFunc.retrieveLangLBl("resume", "LBL_RESUME_TEXT"));
                    isresume = false;
                }
            } else if (i == findViewById(R.id.logoutImageview).getId()) {
                Bundle bn4 = new Bundle();
                bn4.putSerializable("data_trip", data_trip);
                bn4.putSerializable("iTripDeliveryLocationId", data_trip.get("iTripDeliveryLocationId"));
                new StartActProcess(getActContext()).startActWithData(WayBillActivity.class, bn4);
            } else if (i == findViewById(R.id.newbtn_timer).getId()) {
                if (!isresume) {
                    callsetTimeApi(true);
                    newbtn_timer.setText(generalFunc.retrieveLangLBl("stop", "LBL_STOP"));
                    isresume = true;

                } else {
                    countDownStop();
                    newbtn_timer.setText(LBL_WAIT);
                    isresume = false;
                }

            } else if (i == R.id.callArea) {
                if (REQUEST_TYPE.equals("Deliver")) {
                    Bundle bn1 = new Bundle();
                    bn1.putString("TripId", data_trip.get("TripId"));
                    bn1.putSerializable("data_trip", data_trip);
                    new StartActProcess(getActContext()).startActWithData(ViewDeliveryDetailsActivity.class, bn1);
                } else {
                    try {
                        if (generalFunc.getJsonValue("RIDE_DRIVER_CALLING_METHOD", userProfileJsonObj).equals("Voip") && !data_trip.get("eBookingFrom").equalsIgnoreCase("Kiosk")) {
                            sinchCall();
                        } else {
                            getMaskNumber();
                        }


                    } catch (Exception e) {
                    }
                }

            } else if (i == R.id.chatArea) {
                if (data_trip.get("eBookingFrom").equalsIgnoreCase("Kiosk")) {
                    Intent intent = new Intent(Intent.ACTION_SENDTO);
                    intent.setData(Uri.parse("smsto:" + Uri.encode(data_trip.get("PPhone"))));
                    startActivity(intent);

                } else if (REQUEST_TYPE.equals("Deliver")) {

                    Bundle bn2 = new Bundle();
                    bn2.putString("TripId", data_trip.get("TripId"));
                    bn2.putSerializable("data_trip", data_trip);
                    new StartActProcess(getActContext()).startActWithData(ViewDeliveryDetailsActivity.class, bn2);
                }
                else
                {

                    Bundle bnChat = new Bundle();

                    bnChat.putString("iFromMemberId", data_trip.get("PassengerId"));
                    bnChat.putString("FromMemberImageName", data_trip.get("PPicName"));
                    bnChat.putString("iTripId", data_trip.get("iTripId"));
                    bnChat.putString("FromMemberName", data_trip.get("PName"));
                    bnChat.putString("vBookingNo", data_trip.get("vRideNo"));
                    new StartActProcess(getActContext()).startActWithData(ChatActivity.class, bnChat);
                }
            } else if (i == R.id.navigateAreaUP) {
                if (!isTripStart) {
                    String REQUEST_TYPE = data_trip.get("REQUEST_TYPE");
                    if (REQUEST_TYPE.equalsIgnoreCase(Utils.CabGeneralType_Ride)) {
                        generalFunc.showGeneralMessage("", generalFunc.retrieveLangLBl("", "LBL_NAVIGATION_ALERT"));
                    } else if (REQUEST_TYPE.equalsIgnoreCase(Utils.CabGeneralType_UberX)) {
                        generalFunc.showGeneralMessage("", generalFunc.retrieveLangLBl("", "LBL_NAVIGATION_BOOKING_ALERT"));
                    } else {
                        generalFunc.showGeneralMessage("", generalFunc.retrieveLangLBl("", "LBL_NAVIGATION_DELIVERY_ALERT"));
                    }
                } else {
                    openNavigationDialog(dest_lat, dest_lon);
                }
            }
        }
    }

    public class setOnTouchList implements View.OnTouchListener {
        float x1, x2, y1, y2, startX, movedX;

        DisplayMetrics display = getResources().getDisplayMetrics();

        final int width = display.widthPixels;

        @Override
        public boolean onTouch(View view, MotionEvent event) {
            switch (event.getAction()) {
                // when user first touches the screen we get x and y coordinate
                case MotionEvent.ACTION_DOWN: {
                    x1 = event.getX();
                    y1 = event.getY();

                    startX = event.getRawX();
                    break;
                }
                case MotionEvent.ACTION_UP: {
                    x2 = event.getX();
                    y2 = event.getY();
                    movedX = generalFunc.isRTLmode() ? startX - event.getRawX() : event.getRawX() - startX;

                    if (movedX > width / 2) {

                        if (generalFunc.isRTLmode() ? (x1 > x2) : (x1 < x2)) {

                            isTripCancelPressed = false;

                            /*if (view.getId() == tripStartBtnArea.getId()) {
                                // Trip start btn called
                                if (data_trip != null && data_trip.get("eBeforeUpload").equalsIgnoreCase("Yes")) {
                                    takeAndUploadPic(getActContext(), "before");
                                } else {
                                    setTripStart();
                                }
                            } else if (view.getId() == tripEndBtnArea.getId()) {
                                // Trip end btn called

                                if (REQUEST_TYPE.equals("Deliver")) {
                                    buildMsgOnDeliveryEnd();
                                } else {

                                    if (data_trip != null && data_trip.get("eAfterUpload").equalsIgnoreCase("Yes")) {
                                        takeAndUploadPic(getActContext(), "after");
                                    } else {

                                        if (eType.equals("UberX")) {
                                            // getCurrentPriceApi();
                                            endTrip();
                                        } else {
                                            if (!eType.equals("")) {
                                                if (eType.equals("UberX")) {
                                                    //   getCurrentPriceApi();
                                                    endTrip();
                                                } else {
                                                    endTrip();
                                                }

                                            } else {
                                                endTrip();
                                            }
                                        }
                                    }
                                }
                            }*/

                        }
                    }

                    break;
                }
            }
            return false;
        }
    }

    class ImageSourceDialog implements Runnable {

        @Override
        public void run() {
            final Dialog dialog_img_update = new Dialog(getActContext(), R.style.ImageSourceDialogStyle);
            dialog_img_update.setContentView(R.layout.design_image_source_select);

            MTextView chooseImgHTxt = (MTextView) dialog_img_update.findViewById(R.id.chooseImgHTxt);
            MTextView cameraTxt = (MTextView) dialog_img_update.findViewById(R.id.cameraTxt);
            MTextView galleryTxt = (MTextView) dialog_img_update.findViewById(R.id.galleryTxt);
            chooseImgHTxt.setText(generalFunc.retrieveLangLBl("Choose option", "LBL_CHOOSE_OPTION"));
            SelectableRoundedImageView cameraIconImgView = (SelectableRoundedImageView) dialog_img_update.findViewById(R.id.cameraIconImgView);
            SelectableRoundedImageView galleryIconImgView = (SelectableRoundedImageView) dialog_img_update.findViewById(R.id.galleryIconImgView);
            ImageView closeDialogImgView = (ImageView) dialog_img_update.findViewById(R.id.closeDialogImgView);

            MButton btn_type2 = ((MaterialRippleLayout) dialog_img_update.findViewById(R.id.btn_type2)).getChildView();
            btn_type2.setText(generalFunc.retrieveLangLBl("", "LBL_CANCEL_TXT"));

            cameraTxt.setText(generalFunc.retrieveLangLBl("", "LBL_CAMERA"));
            galleryTxt.setText(generalFunc.retrieveLangLBl("", "LBL_GALLERY"));

            closeDialogImgView.setOnClickListener(v -> {


                if (dialog_img_update != null) {
                    dialog_img_update.cancel();
                }
            });

            //  int backColor = getResources().getColor(R.color.appThemeColor_Dark_1);
            //  int strokeColor = Color.parseColor("#00000000");
            //  int colorFilter = getResources().getColor(R.color.appThemeColor_TXT_1);
            // int radius = Utils.dipToPixels(getActContext(), 25);

            // new CreateRoundedView(backColor, radius, 0, strokeColor, cameraIconImgView);
            //  cameraIconImgView.setColorFilter(colorFilter);
            // new CreateRoundedView(backColor, radius, 0, strokeColor, galleryIconImgView);
            //  galleryIconImgView.setColorFilter(colorFilter);

            cameraIconImgView.setOnClickListener(v -> {
                if (dialog_img_update != null) {
                    dialog_img_update.cancel();
                }
                if (!isDeviceSupportCamera()) {
                    generalFunc.showMessage(generalFunc.getCurrentView(ActiveTripActivity.this), generalFunc.retrieveLangLBl("", "LBL_NOT_SUPPORT_CAMERA_TXT"));
                } else {
                    chooseFromCamera();
                }
            });

            galleryIconImgView.setOnClickListener(v -> {
                if (dialog_img_update != null) {
                    dialog_img_update.cancel();
                }
                chooseFromGallery();
            });
            dialog_img_update.setCanceledOnTouchOutside(true);
            Window window = dialog_img_update.getWindow();
            window.setGravity(Gravity.BOTTOM);
            window.setLayout(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            dialog_img_update.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            if (generalFunc.isRTLmode()) {
                dialog_img_update.getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
            }
            dialog_img_update.show();
          /*  dialog_img_update.setCanceledOnTouchOutside(true);
            Window window = dialog_img_update.getWindow();
            window.setGravity(Gravity.BOTTOM);
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            dialog_img_update.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            dialog_img_update.show();*/
        }
    }

    private void defaultAddtionalprice() {
        additonallist.add(0, 0.00);
        additonallist.add(1, 0.00);
        additonallist.add(2, 0.00);
    }

    private GradientDrawable getRoundBG(String color) {

        int strokeWidth = 2;
        int strokeColor = Color.parseColor("#CCCACA");
        int fillColor = Color.parseColor(color);
        GradientDrawable gD = new GradientDrawable();
        gD.setColor(fillColor);
        gD.setShape(GradientDrawable.RECTANGLE);
        gD.setCornerRadius(100);
        gD.setStroke(strokeWidth, strokeColor);
        return gD;
    }
}