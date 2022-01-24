package com.taxifgo.driver;

import android.app.Activity;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;

import androidx.annotation.NonNull;

import com.general.files.AppFunctions;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.text.Html;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.drawRoute.DirectionsJSONParser;
import com.general.files.ConfigPubNub;
import com.general.files.ExecuteWebServerUrl;
import com.general.files.GeneralFunctions;
import com.general.files.GetLocationUpdates;
import com.general.files.MapAnimator;
import com.general.files.MapDelegate;
import com.general.files.MapServiceApi;
import com.general.files.MyApp;
import com.general.files.StartActProcess;
import com.general.files.StopOverPointsDataParser;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.SphericalUtil;
import com.model.Stop_Over_Points_Data;
import com.trafi.anchorbottomsheetbehavior.AnchorBottomSheetBehavior;
import com.utils.CabRequestStatus;
import com.utils.Logger;
import com.utils.Utils;
import com.view.CreateRoundedView;
import com.view.GenerateAlertBox;
import com.view.MTextView;
import com.view.simpleratingbar.SimpleRatingBar;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.general.files.UpdateDirections.formatHoursAndMinutes;

@SuppressWarnings("ResourceType")
public class CabRequestedActivity extends AppCompatActivity implements GenerateAlertBox.HandleAlertBtnClick, OnMapReadyCallback, GetLocationUpdates.LocationUpdatesListener, ViewTreeObserver.OnGlobalLayoutListener, MapDelegate {

    public GeneralFunctions generalFunc;

    ProgressBar mProgressBar, progressbar_dialog;
    RelativeLayout progressLayout;
    String message_str;
    String msgCode;
    MTextView locationAddressTxt, ufxlocationAddressTxt;
    MTextView destAddressTxt;
    LinearLayout viewDetailsArea;
    String pickUpAddress = "";
    String destinationAddress = "";
    boolean eFly = false;

    GenerateAlertBox generateAlert;
    int maxProgressValue = 30;
    MediaPlayer mp = new MediaPlayer();
    private MTextView textViewShowTime, ufxtvTimeCount, tvTimeCount_dialog; // will show the time
    private CountDownTimer countDownTimer; // built in android class
    // CountDownTimer
    private long totalTimeCountInMilliseconds = maxProgressValue * 1 * 1000; // total count down time in
    // milliseconds
    private long timeBlinkInMilliseconds = 10 * 1000; // start time of start blinking
    private boolean blink; // controls the blinking .. on and off

    private MTextView locationAddressHintTxt, ufxlocationAddressHintTxt;
    private MTextView destAddressHintTxt;
    private MTextView serviceType, ufxserviceType;


    boolean istimerfinish = false;
    boolean isloadedAddress = false;
    FrameLayout progressLayout_frame, progressLayout_frame_dialog;
    MTextView specialHintTxt, specialValTxt;
    String specialUserComment = "";

    boolean isUfx = false;
    ImageView backImageView;
    MTextView pkgType;
    private Location userLocation;
    MTextView moreSeriveTxt;

    String LBL_REQUEST, LBL_DELIVERY, LBL_RIDE, LBL_JOB_TXT, LBL_RENTAL_RIDE_REQUEST, LBL_RENTAL_AIRCRAFT_REQUEST;
    String LBL_RECIPIENT, LBL_PAYMENT_MODE_TXT, LBL_TOTAL_DISTANCE, LBL_Total_Fare_TXT, LBL_POOL_REQUEST, LBL_PERSON, LBL_FLY_REQUEST;
    String REQUEST_TYPE = "";
    long milliLeft;
    String iCabRequestId = "";
    String iOrderId = "";

    MTextView addressTxt, etaTxt;
    public Marker sourceMarker, destMarker, sourceDotMarker, destDotMarker;
    MarkerOptions source_dot_option, dest_dot_option;
    GoogleMap gMap;
    LatLngBounds.Builder builder = new LatLngBounds.Builder();
    int DRIVER_ARRIVED_MIN_TIME_PER_MINUTE = 3;
    Polyline route_polyLine;
    public ArrayList<Stop_Over_Points_Data> stopOverPointsList = new ArrayList<>();
    public String distance = "";
    public String time = "";
    public LatLng sourceLocation = null;
    public LatLng destLocation = null;
    BottomSheetDialog userInfoDialog;
    String requestTypeVal = "";
    private final static double DEFAULT_CURVE_ROUTE_CURVATURE = 0.5f;
    private final static int DEFAULT_CURVE_POINTS = 60;
    MTextView deliveryDetailsBtn;

    SupportMapFragment fm;
    boolean enableGoogleDirection = false;
    String userProfileJson;
    JSONObject userProfileJsonObj;
    boolean isFindRoute = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON);
        setContentView(R.layout.activity_cab_requested);

        generalFunc = MyApp.getInstance().getGeneralFun(getActContext());
        generalFunc.removeValue(Utils.DRIVER_ACTIVE_REQ_MSG_KEY);


        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        MyApp.getInstance().stopAlertService();

        message_str = getIntent().getStringExtra("Message");
        msgCode = generalFunc.getJsonValue("MsgCode", message_str);


        String iCabRequestId_ = generalFunc.getJsonValue("iCabRequestId", message_str);
        if (iCabRequestId_ != null && !iCabRequestId_.equalsIgnoreCase("")) {
            iCabRequestId = iCabRequestId_;
        }

        String iOrderId_ = generalFunc.getJsonValue("iOrderId", message_str);
        if (iOrderId_ != null && !iOrderId_.equalsIgnoreCase("")) {
            iOrderId = iOrderId_;
        }

        if (generalFunc.containsKey(Utils.DRIVER_REQ_COMPLETED_MSG_CODE_KEY + msgCode)) {
            // generalFunc.restartApp();
            // MyApp.getInstance().restartWithGetDataApp();
            finish();
            return;
        } else {
            generalFunc.storeData(Utils.DRIVER_REQ_COMPLETED_MSG_CODE_KEY + msgCode, "true");
            generalFunc.storeData(Utils.DRIVER_REQ_COMPLETED_MSG_CODE_KEY + msgCode, "" + System.currentTimeMillis());
        }
        generalFunc.storeData(Utils.DRIVER_CURRENT_REQ_OPEN_KEY, "true");

        (new CabRequestStatus(getActContext())).updateDriverRequestStatus(1, generalFunc.getJsonValue("PassengerId", message_str), "Open", "", msgCode);

        if (GetLocationUpdates.getInstance() != null) {
            GetLocationUpdates.getInstance().startLocationUpdates(this, this);
        }

        deliveryDetailsBtn = findViewById(R.id.deliveryDetailsBtn);
        moreSeriveTxt = (MTextView) findViewById(R.id.moreServiceBtn);
        bottom_sheet = findViewById(R.id.bottom_sheet);

        locationAddressTxt = (MTextView) findViewById(R.id.locationAddressTxt);
        ufxlocationAddressTxt = (MTextView) findViewById(R.id.ufxlocationAddressTxt);
        locationAddressHintTxt = (MTextView) findViewById(R.id.locationAddressHintTxt);
        ufxlocationAddressHintTxt = (MTextView) findViewById(R.id.ufxlocationAddressHintTxt);
        destAddressHintTxt = (MTextView) findViewById(R.id.destAddressHintTxt);
        destAddressTxt = (MTextView) findViewById(R.id.destAddressTxt);
        viewDetailsArea = (LinearLayout) findViewById(R.id.viewDetailsArea);
        progressLayout = (RelativeLayout) findViewById(R.id.progressLayout);
        specialHintTxt = (MTextView) findViewById(R.id.specialHintTxt);
        specialValTxt = (MTextView) findViewById(R.id.specialValTxt);
        backImageView = (ImageView) findViewById(R.id.backImageView);
        pkgType = (MTextView) findViewById(R.id.pkgType);
        backImageView.setVisibility(View.GONE);

        progressLayout_frame = (FrameLayout) findViewById(R.id.progressLayout_frame);
        progressLayout_frame_dialog = (FrameLayout) findViewById(R.id.progressLayout_frame_dialog);


        mProgressBar = (ProgressBar) findViewById(R.id.progressbar);
        progressbar_dialog = (ProgressBar) findViewById(R.id.progressbar_dialog);


        textViewShowTime = (MTextView) findViewById(R.id.tvTimeCount);
        tvTimeCount_dialog = (MTextView) findViewById(R.id.tvTimeCount_dialog);
        ufxtvTimeCount = (MTextView) findViewById(R.id.ufxtvTimeCount);
        serviceType = (MTextView) findViewById(R.id.serviceType);
        ufxserviceType = (MTextView) findViewById(R.id.ufxserviceType);

        (findViewById(R.id.menuImgView)).setVisibility(View.GONE);


        maxProgressValue = GeneralFunctions.parseIntegerValue(30, generalFunc.retrieveValue("RIDER_REQUEST_ACCEPT_TIME"));
        totalTimeCountInMilliseconds = maxProgressValue * 1 * 1000; // total count down time in
        textViewShowTime.setText(maxProgressValue + ":" + "00");
        tvTimeCount_dialog.setText(maxProgressValue + ":" + "00");
        mProgressBar.setMax(maxProgressValue);
        mProgressBar.setProgress(maxProgressValue);
        progressbar_dialog.setMax(maxProgressValue);
        progressbar_dialog.setProgress(maxProgressValue);


        generateAlert = new GenerateAlertBox(getActContext());
        generateAlert.setBtnClickList(this);
        generateAlert.setCancelable(false);

        REQUEST_TYPE = generalFunc.getJsonValue("REQUEST_TYPE", message_str);

        fm = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapV2_calling_driver);

        fm.getMapAsync(this);


        deliveryDetailsBtn.setText(generalFunc.retrieveLangLBl("", "LBL_VIEW_DELIVERY_DETAILS"));
        deliveryDetailsBtn.setOnClickListener(new setOnClickList());
        LBL_REQUEST = generalFunc.retrieveLangLBl("Request", "LBL_REQUEST");
        LBL_DELIVERY = generalFunc.retrieveLangLBl("Delivery", "LBL_DELIVERY");
        LBL_RIDE = generalFunc.retrieveLangLBl("Ride", "LBL_RIDE");
        LBL_JOB_TXT = generalFunc.retrieveLangLBl("Job", "LBL_JOB_TXT");
        LBL_RENTAL_RIDE_REQUEST = generalFunc.retrieveLangLBl("", "LBL_RENTAL_RIDE_REQUEST");
        LBL_RECIPIENT = generalFunc.retrieveLangLBl("", "LBL_RECIPIENT");
        LBL_PAYMENT_MODE_TXT = generalFunc.retrieveLangLBl("", "LBL_PAYMENT_MODE_TXT");
        LBL_TOTAL_DISTANCE = generalFunc.retrieveLangLBl("", "LBL_TOTAL_DISTANCE");
        LBL_Total_Fare_TXT = generalFunc.retrieveLangLBl("", "LBL_Total_Fare_TXT");
        LBL_POOL_REQUEST = generalFunc.retrieveLangLBl("Ride", "LBL_POOL_REQUEST");
        LBL_PERSON = generalFunc.retrieveLangLBl("", "LBL_PERSON");
        LBL_FLY_REQUEST = generalFunc.retrieveLangLBl("", "LBL_FLY_REQUEST");
        LBL_RENTAL_AIRCRAFT_REQUEST = generalFunc.retrieveLangLBl("", "LBL_RENTAL_AIRCRAFT_REQUEST");

        userProfileJsonObj = generalFunc.getJsonObject(generalFunc.retrieveValue(Utils.USER_PROFILE_JSON));
        userProfileJson = generalFunc.retrieveValue(Utils.USER_PROFILE_JSON);
        if (generalFunc.getJsonValue("IS_SHOW_ROUTE_ON_REQUEST", userProfileJson).equalsIgnoreCase("yes")) {
            enableGoogleDirection = true;
        }

        setData();
        setLabels();


        startTimer(totalTimeCountInMilliseconds);

        progressLayout.setOnClickListener(new setOnClickList());

        viewDetailsArea.setOnClickListener(new setOnClickList());
        moreSeriveTxt.setOnClickListener(new setOnClickList());

    }

    public PolylineOptions createCurveRoute(LatLng origin, LatLng dest) {

        double distance = SphericalUtil.computeDistanceBetween(origin, dest);
        double heading = SphericalUtil.computeHeading(origin, dest);
        double halfDistance = distance > 0 ? (distance / 2) : (distance * DEFAULT_CURVE_ROUTE_CURVATURE);

        // Calculate midpoint position
        LatLng midPoint = SphericalUtil.computeOffset(origin, halfDistance, heading);

        // Calculate position of the curve center point
        double sqrCurvature = DEFAULT_CURVE_ROUTE_CURVATURE * DEFAULT_CURVE_ROUTE_CURVATURE;
        double extraParam = distance / (4 * DEFAULT_CURVE_ROUTE_CURVATURE);
        double midPerpendicularLength = (1 - sqrCurvature) * extraParam;
        double r = (1 + sqrCurvature) * extraParam;

        LatLng circleCenterPoint = SphericalUtil.computeOffset(midPoint, midPerpendicularLength, heading + 90.0);

        // Calculate heading between circle center and two points
        double headingToOrigin = SphericalUtil.computeHeading(circleCenterPoint, origin);

        // Calculate positions of points on the curve
        double step = Math.toDegrees(Math.atan(halfDistance / midPerpendicularLength)) * 2 / DEFAULT_CURVE_POINTS;
        //Polyline options
        PolylineOptions options = new PolylineOptions();

        for (int i = 0; i < DEFAULT_CURVE_POINTS; ++i) {
            LatLng pi = SphericalUtil.computeOffset(circleCenterPoint, r, headingToOrigin + i * step);
            options.add(pi);
        }
        return options;
    }

    public void setLabels() {
        /*Multi Delivery Lables*/

        ((MTextView) findViewById(R.id.recipientHintTxt)).setText(generalFunc.isRTLmode() ? ":" + LBL_RECIPIENT : LBL_RECIPIENT + ":");

        ((MTextView) findViewById(R.id.paymentModeHintTxt)).setText(generalFunc.isRTLmode() ? ":" + LBL_PAYMENT_MODE_TXT : LBL_PAYMENT_MODE_TXT + ":");

        ((MTextView) findViewById(R.id.totalMilesHintTxt)).setText(generalFunc.isRTLmode() ? ":" + LBL_TOTAL_DISTANCE : LBL_TOTAL_DISTANCE + ":");

        ((MTextView) findViewById(R.id.totalFareHintTxt)).setText(generalFunc.isRTLmode() ? ":" + LBL_Total_Fare_TXT : LBL_Total_Fare_TXT + ":");


        if (REQUEST_TYPE.equalsIgnoreCase(Utils.CabGeneralType_Ride)) {
            locationAddressHintTxt.setText(generalFunc.retrieveLangLBl("", "LBL_PICKUP_LOCATION_HEADER_TXT"));
            destAddressHintTxt.setText(generalFunc.retrieveLangLBl("", "LBL_DEST_ADD_TXT"));
        } else {
            locationAddressHintTxt.setText(generalFunc.retrieveLangLBl("", "LBL_SENDER_LOCATION"));
            destAddressHintTxt.setText(generalFunc.retrieveLangLBl("", "LBL_RECEIVER_LOCATION"));
        }
        ufxlocationAddressHintTxt.setText(generalFunc.retrieveLangLBl("Job Location", "LBL_JOB_LOCATION_TXT"));

        ((MTextView) findViewById(R.id.hintTxt)).setText(generalFunc.retrieveLangLBl("", "LBL_HINT_TAP_TXT"));
        specialHintTxt.setText(generalFunc.retrieveLangLBl("Special Instruction", "LBL_SPECIAL_INSTRUCTION_TXT"));
        moreSeriveTxt.setText(generalFunc.retrieveLangLBl("", "LBL_VIEW_REQUESTED_SERVICES"));


    }

    public void setData() {
        HashMap<String, String> hashMap = new HashMap<>();


        new CreateRoundedView(Color.parseColor("#000000"), Utils.dipToPixels(getActContext(), 122), 0, Color.parseColor("#FFFFFF"), findViewById(R.id.bgCircle));
        new CreateRoundedView(Color.parseColor("#000000"), Utils.dipToPixels(getActContext(), 122), 0, Color.parseColor("#FFFFFF"), findViewById(R.id.bgCircle_dialog));


        double pickupLat = GeneralFunctions.parseDoubleValue(0.0, generalFunc.getJsonValue("sourceLatitude", message_str));
        double pickupLog = GeneralFunctions.parseDoubleValue(0.0, generalFunc.getJsonValue("sourceLongitude", message_str));

        double desLat = 0.0;
        double destLog = 0.0;

        String destLatitude = generalFunc.getJsonValue("destLatitude", message_str);
        String destLongitude = generalFunc.getJsonValue("destLongitude", message_str);
        if (!destLatitude.isEmpty() && !destLongitude.isEmpty()) {

            desLat = GeneralFunctions.parseDoubleValue(0.0, destLatitude);
            destLog = GeneralFunctions.parseDoubleValue(0.0, destLongitude);

            if (desLat == 0.0 && destLog == 0.0) {
                destAddressTxt.setVisibility(View.GONE);
                destAddressHintTxt.setVisibility(View.GONE);
            } else {
                destAddressTxt.setVisibility(View.VISIBLE);
                destAddressHintTxt.setVisibility(View.VISIBLE);
            }
        }
        String serverKey = generalFunc.retrieveValue(Utils.GOOGLE_SERVER_ANDROID_DRIVER_APP_KEY);
        String url_str = "https://maps.googleapis.com/maps/api/directions/json?origin=" + pickupLat + "," + pickupLog + "&" + "destination=" + (desLat != 0.0 ? desLat : pickupLat) + "," + (destLog != 0.0 ? destLog : pickupLog) + "&sensor=true&key=" + serverKey + "&language=" + generalFunc.retrieveValue(Utils.GOOGLE_MAP_LANGUAGE_CODE_KEY) + "&sensor=true";


        hashMap.put("s_latitude", pickupLat + "");
        hashMap.put("s_longitude", pickupLog + "");
        hashMap.put("d_latitude", destLatitude + "");
        hashMap.put("d_longitude", destLongitude + "");
        String parameters = "origin=" + desLat + "&destination=" + destLog;
        hashMap.put("parameters", parameters);


        if ((iCabRequestId != null && !iCabRequestId.equals("")) || (iOrderId != null && !iOrderId.equals(""))) {
            getAddressFormServer();
        } else {
            //findAddressByDirectionAPI(url_str);

            MapServiceApi.getDirectionservice(getActContext(), hashMap, this, false);

        }

        LinearLayout packageInfoArea = (LinearLayout) findViewById(R.id.packageInfoArea);

        String VehicleTypeName = generalFunc.getJsonValue("VehicleTypeName", message_str);
        String SelectedTypeName = generalFunc.getJsonValue("SelectedTypeName", message_str);
        eFly = generalFunc.getJsonValue("eFly", message_str).equalsIgnoreCase("Yes");

        if (REQUEST_TYPE.equalsIgnoreCase(Utils.CabGeneralType_UberX)) {
            isUfx = true;
            progressLayout_frame.setVisibility(View.GONE);
            locationAddressTxt.setVisibility(View.GONE);
            locationAddressHintTxt.setVisibility(View.GONE);
            destAddressHintTxt.setVisibility(View.GONE);
            destAddressTxt.setVisibility(View.GONE);
            ufxlocationAddressTxt.setVisibility(View.VISIBLE);
            ufxlocationAddressHintTxt.setVisibility(View.VISIBLE);
            progressLayout_frame.setVisibility(View.VISIBLE);
            specialHintTxt.setVisibility(View.VISIBLE);
            specialValTxt.setVisibility(View.VISIBLE);

            requestTypeVal = LBL_JOB_TXT + "  " + LBL_REQUEST;

            (findViewById(R.id.ufxserviceType)).setVisibility(View.VISIBLE);
            ufxserviceType.setText(SelectedTypeName);
            packageInfoArea.setVisibility(View.GONE);
        } else if (REQUEST_TYPE.equalsIgnoreCase(Utils.CabGeneralTypeRide_Delivery_UberX)) {
            requestTypeVal = LBL_JOB_TXT + "  " + LBL_REQUEST;
            (findViewById(R.id.serviceType)).setVisibility(View.VISIBLE);
            serviceType.setText(SelectedTypeName);
            packageInfoArea.setVisibility(View.GONE);
        } else if (REQUEST_TYPE.equals("Deliver")) {
            (findViewById(R.id.packageInfoArea)).setVisibility(View.VISIBLE);
            ((MTextView) findViewById(R.id.packageInfoTxt)).setText(generalFunc.getJsonValue("PACKAGE_TYPE", message_str));

            if (VehicleTypeName != null && !VehicleTypeName.equalsIgnoreCase("")) {
                requestTypeVal = LBL_DELIVERY + " " + LBL_REQUEST + " (" + VehicleTypeName + ")";

            } else {
                requestTypeVal = LBL_DELIVERY + " " + LBL_REQUEST;
            }
        } else if (REQUEST_TYPE.equalsIgnoreCase(Utils.eType_Multi_Delivery)) {
            destAddressHintTxt.setVisibility(View.GONE);
            destAddressTxt.setVisibility(View.GONE);
            (findViewById(R.id.packageInfoArea)).setVisibility(View.GONE);
            (findViewById(R.id.deliver_Area)).setVisibility(View.VISIBLE);

            if (VehicleTypeName != null && !VehicleTypeName.equalsIgnoreCase("")) {
                requestTypeVal = LBL_DELIVERY + " " + LBL_REQUEST + " (" + VehicleTypeName + ")";

            } else {
                requestTypeVal = LBL_DELIVERY + " " + LBL_REQUEST;
            }

        } else {
            (findViewById(R.id.packageInfoArea)).setVisibility(View.GONE);

            if (VehicleTypeName != null && !VehicleTypeName.equalsIgnoreCase("")) {
                requestTypeVal = (eFly ? LBL_FLY_REQUEST : (LBL_RIDE + " " + LBL_REQUEST)) + " (" + VehicleTypeName + ")";

            } else {
                requestTypeVal = (eFly ? LBL_FLY_REQUEST : (LBL_RIDE + " " + LBL_REQUEST));
            }

        }
        String ePoolRequest = generalFunc.getJsonValue("ePoolRequest", message_str);
        if (ePoolRequest != null && ePoolRequest.equalsIgnoreCase("Yes")) {
            requestTypeVal =
                    LBL_POOL_REQUEST + " ( " + generalFunc.convertNumberWithRTL(generalFunc.getJsonValue("iPersonSize", message_str)) + " " +
                            LBL_PERSON + " )";
        }
    }


    public void manageEta() {
        double lowestKM = 0.0;
        boolean isFirst_lowestKM = true;

        double distance = Utils.CalculationByLocation(userLocation.getLatitude(), userLocation.getLongitude(), user_lat, user_lon, "");

        if (isFirst_lowestKM) {
            lowestKM = distance;
            isFirst_lowestKM = false;
        } else {
            if (distance < lowestKM) {
                lowestKM = distance;
            }
        }


        int lowestTime = ((int) (lowestKM * DRIVER_ARRIVED_MIN_TIME_PER_MINUTE));

        if (lowestTime < 1) {
            lowestTime = 1;
        }


        // handleSourceMarker("" + lowestTime + "\n" + generalFunc.retrieveLangLBl("", "LBL_MIN_SMALL_TXT"));


        if (!isSkip) {
            findRoute("" + lowestTime + "\n" + generalFunc.retrieveLangLBl("", "LBL_MIN_SMALL_TXT"));
        } else {
            handleSourceMarker("" + lowestTime + "\n" + generalFunc.retrieveLangLBl("", "LBL_MIN_SMALL_TXT"));
        }
    }

    public void getAddressFormServer() {

        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "getCabRequestAddress");
        if (!iCabRequestId.equalsIgnoreCase("")) {
            parameters.put("iCabRequestId", iCabRequestId);
        }
        if (!iOrderId.equalsIgnoreCase("")) {
            parameters.put("iOrderId", iOrderId);
            parameters.put("eSystem", Utils.eSystem_Type);
        }


        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(), parameters);

        exeWebServer.setDataResponseListener(responseString -> {
            JSONObject responseStringObj = generalFunc.getJsonObject(responseString);

            if (responseStringObj != null && !responseStringObj.equals("")) {

                boolean isDataAvail = GeneralFunctions.checkDataAvail(Utils.action_str, responseStringObj);

                if (isDataAvail) {

                    String MessageJson = generalFunc.getJsonValueStr(Utils.message_str, responseStringObj);
                    pickUpAddress = generalFunc.getJsonValue("tSourceAddress", MessageJson);
                    destinationAddress = generalFunc.getJsonValue("tDestAddress", MessageJson);
                    eFly = generalFunc.getJsonValue("eFly", MessageJson).equalsIgnoreCase("Yes");
                    if (isUfx) {
                        String tUserComment = generalFunc.getJsonValue("tUserComment", MessageJson);

                        if (tUserComment != null && !tUserComment.equals("")) {
                            specialUserComment = tUserComment;
                            specialValTxt.setText(tUserComment);
                        } else {
                            specialValTxt.setText("------------");
                        }
                    }

                    REQUEST_TYPE = generalFunc.getJsonValue("eType", MessageJson);


                    user_lat = GeneralFunctions.parseDoubleValue(0.0, generalFunc.getJsonValue("sourceLatitude", MessageJson));
                    user_lon = GeneralFunctions.parseDoubleValue(0.0, generalFunc.getJsonValue("sourceLongitude", MessageJson));

                    String destLatitude = generalFunc.getJsonValue("destLatitude", MessageJson);
                    if (destLatitude != null && !destLatitude.equalsIgnoreCase("")) {
                        user_destLat = GeneralFunctions.parseDoubleValue(0.0, generalFunc.getJsonValue("destLatitude", MessageJson));
                        user_destLon = GeneralFunctions.parseDoubleValue(0.0, generalFunc.getJsonValue("destLongitude", MessageJson));
                        DestLnt = new LatLng(user_destLat, user_destLon);
                    } else {
                        isSkip = true;
                    }

                    fromLnt = new LatLng(user_lat, user_lon);


                    String moreServices = generalFunc.getJsonValue("moreServices", MessageJson);
                    if (!moreServices.equals("") && moreServices.equals("Yes")) {
                        specialValTxt.setVisibility(View.GONE);
                        specialHintTxt.setVisibility(View.GONE);
                        moreSeriveTxt.setVisibility(View.VISIBLE);
                    }

                    String VehicleTypeName = generalFunc.getJsonValue("VehicleTypeName", MessageJson);

                    if (REQUEST_TYPE.equalsIgnoreCase(Utils.CabGeneralType_UberX)) {
                        requestTypeVal = LBL_JOB_TXT + "  " + LBL_REQUEST;
                    } else if (REQUEST_TYPE.equalsIgnoreCase(Utils.eType_Multi_Delivery)) {

                        if (VehicleTypeName != null && !VehicleTypeName.equalsIgnoreCase("")) {
                            requestTypeVal = LBL_DELIVERY + " " + LBL_REQUEST + " (" + VehicleTypeName + ")";

                        } else {
                            requestTypeVal = LBL_DELIVERY + " " + LBL_REQUEST;
                        }

                        if (REQUEST_TYPE.equalsIgnoreCase(Utils.eType_Multi_Delivery) || generalFunc.getJsonValue("eType", MessageJson).equalsIgnoreCase(Utils.eType_Multi_Delivery)) {
                            deliveryDetailsBtn.setVisibility(View.VISIBLE);
                            int Total_Delivery = GeneralFunctions.parseIntegerValue(0, generalFunc.getJsonValue("Total_Delivery", MessageJson));
                            String ePayType = generalFunc.getJsonValue("ePayType", MessageJson);
                            String fTripGenerateFare = generalFunc.getJsonValue("fTripGenerateFare", MessageJson);
                            String fDistance = generalFunc.getJsonValue("fDistance", MessageJson);

                            if (Total_Delivery == 1) {
                                ((MTextView) findViewById(R.id.recipientHintTxt)).setText(LBL_RECIPIENT + ":");
                            } else {
                                ((MTextView) findViewById(R.id.recipientHintTxt)).setText(generalFunc.retrieveLangLBl("", "LBL_MULTI_RECIPIENTS") + ":");
                            }

                            ((MTextView) findViewById(R.id.recipientValTxt)).setText(Utils.checkText("" + Total_Delivery) ? " " + ("" + Total_Delivery).trim() : "");

                            ((MTextView) findViewById(R.id.paymentModeValTxt)).setText(Utils.checkText(ePayType) ? " " + ePayType.trim() : "");
                            if (generalFunc.getJsonValue("ePayWallet", MessageJson).equalsIgnoreCase("Yes")) {
                                ((MTextView) findViewById(R.id.paymentModeValTxt)).setText(generalFunc.retrieveLangLBl("Wallet", "LBL_WALLET_TXT"));
                            }

                            ((MTextView) findViewById(R.id.totalMilesValTxt)).setText(Utils.checkText(fDistance) ? " " + fDistance.trim() : "");

                            ((MTextView) findViewById(R.id.totalFareValTxt)).setText(Utils.checkText(fTripGenerateFare) ? " " + fTripGenerateFare.trim() : "");

                            destAddressHintTxt.setVisibility(View.GONE);
                            destAddressTxt.setVisibility(View.GONE);
                            (findViewById(R.id.packageInfoArea)).setVisibility(View.GONE);
                        }

                    } else if (REQUEST_TYPE.equals("Deliver") || REQUEST_TYPE.equalsIgnoreCase(Utils.eType_Multi_Delivery)) {


                        if (VehicleTypeName != null && !VehicleTypeName.equalsIgnoreCase("")) {
                            requestTypeVal = LBL_DELIVERY + " " + LBL_REQUEST + " (" + VehicleTypeName + ")";
                        } else {
                            requestTypeVal = LBL_DELIVERY + " " + LBL_REQUEST;
                        }
                    } else if (REQUEST_TYPE.equalsIgnoreCase(Utils.CabGeneralType_Ride)) {

                        if (VehicleTypeName != null && !VehicleTypeName.equalsIgnoreCase("")) {
                            requestTypeVal = (eFly ? LBL_FLY_REQUEST : (LBL_RIDE + " " + LBL_REQUEST)) + " (" + VehicleTypeName + ")";
                        } else {
                            requestTypeVal = (eFly ? LBL_FLY_REQUEST : (LBL_RIDE + " " + LBL_REQUEST));
                        }

                        String PackageName = generalFunc.getJsonValue("PackageName", MessageJson);
                        if (PackageName != null && !PackageName.equalsIgnoreCase("")) {
                            pkgType.setVisibility(View.VISIBLE);
                            pkgType.setText(PackageName);

                            if (VehicleTypeName != null && !VehicleTypeName.equalsIgnoreCase("")) {
                                requestTypeVal = (eFly ? LBL_RENTAL_AIRCRAFT_REQUEST : LBL_RENTAL_RIDE_REQUEST) + " (" + VehicleTypeName + ")";
                            } else {
                                requestTypeVal = (eFly ? LBL_RENTAL_AIRCRAFT_REQUEST : LBL_RENTAL_RIDE_REQUEST);
                            }
                        }

                    } else if (REQUEST_TYPE.equalsIgnoreCase("DeliverAll")) {

                        requestTypeVal = generalFunc.retrieveLangLBl("Delivery", "LBL_DELIVERY") + " " + generalFunc.retrieveLangLBl("Request", "LBL_REQUEST");

                    }

                    isloadedAddress = true;

                    if (destinationAddress.equalsIgnoreCase("")) {
                        destinationAddress = "----";
                    }

                    destAddressTxt.setText(destinationAddress);
                    locationAddressTxt.setText(pickUpAddress);
                    ufxlocationAddressTxt.setText(pickUpAddress);

                    String ePoolRequest = generalFunc.getJsonValue("ePoolRequest", message_str);

                    if (ePoolRequest != null && ePoolRequest.equalsIgnoreCase("Yes")) {
                        requestTypeVal =
                                LBL_POOL_REQUEST + " ( " + generalFunc.convertNumberWithRTL(generalFunc.getJsonValue("iPersonSize", message_str)) + " " +
                                        LBL_PERSON + " )";
                    }

                    //manageEta();

                    if (GetLocationUpdates.getInstance() != null && GetLocationUpdates.getInstance().getLastLocation() != null) {
                        Location lastLocation = GetLocationUpdates.getInstance().getLastLocation();
                        onLocationUpdate(lastLocation);
                    }

                    manageBottomDialog();


                } else {
                    new Handler().postDelayed(() -> getAddressFormServer(), 2000);

                }
            } else {
                new Handler().postDelayed(() -> getAddressFormServer(), 2000);
            }
        });
        exeWebServer.execute();
    }

    public void findAddressByDirectionAPI(final String url) {
        final ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(), url, true);
        exeWebServer.setDataResponseListener(responseString -> {
            if (responseString != null && !responseString.equals("")) {
                String status = generalFunc.getJsonValue("status", responseString);
                if (status.equals("OK")) {

                    JSONArray obj_routes = generalFunc.getJsonArray("routes", responseString);
                    if (obj_routes != null && obj_routes.length() > 0) {
                        JSONObject obj_legs = generalFunc.getJsonObject(generalFunc.getJsonArray("legs", generalFunc.getJsonObject(obj_routes, 0).toString()), 0);

                        pickUpAddress = generalFunc.getJsonValue("start_address", obj_legs.toString());
                        destinationAddress = generalFunc.getJsonValue("end_address", obj_legs.toString());
                    }
                    isloadedAddress = true;
                    if (destinationAddress.equalsIgnoreCase("")) {
                        destinationAddress = "----";
                    }
                    destAddressTxt.setText(destinationAddress);
                    locationAddressTxt.setText(pickUpAddress);
                    ufxlocationAddressTxt.setText(pickUpAddress);
                } else {
                    new Handler().postDelayed(() -> findAddressByDirectionAPI(url), 2000);
                }
            } else {
                new Handler().postDelayed(() -> findAddressByDirectionAPI(url), 2000);
            }
        });
        exeWebServer.execute();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (istimerfinish) {
            trimCache(getActContext());
            istimerfinish = false;
            backImageView.setVisibility(View.VISIBLE);
            finish();
        }
    }

    public static void trimCache(Context context) {
        try {
            File dir = context.getCacheDir();
            if (dir != null && dir.isDirectory()) {
                deleteDir(dir);
            }
        } catch (Exception e) {
            // TODO: handle exception
        }
    }

    public static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        return dir.delete();
    }

    @Override
    protected void onDestroy() {
        if (GetLocationUpdates.retrieveInstance() != null) {
            GetLocationUpdates.getInstance().stopLocationUpdates(this);
        }

        super.onDestroy();
        removeCustoNotiSound();
    }

    @Override
    protected void onPause() {
        super.onPause();
        removeSound();
    }

    @Override
    public void handleBtnClick(int btn_id) {
        Utils.hideKeyboard(CabRequestedActivity.this);

        cancelRequest();
    }

    public void acceptRequest() {
        /*Stop Timer*/
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        progressLayout.setClickable(false);
        generateTrip();
    }

    public void generateTrip() {

        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(), generateTripParams());
        exeWebServer.setLoaderConfig(getActContext(), true, generalFunc);
        exeWebServer.setCancelAble(false);
        exeWebServer.setDataResponseListener(responseString -> {
            JSONObject responseStringObj = generalFunc.getJsonObject(responseString);

            if (responseStringObj != null && !responseStringObj.equals("")) {

                boolean isDataAvail = GeneralFunctions.checkDataAvail(Utils.action_str, responseStringObj);

                if (isDataAvail) {

                    if (GetLocationUpdates.retrieveInstance() != null) {
                        GetLocationUpdates.getInstance().stopLocationUpdates(this);
                    }

                    if (countDownTimer != null) {
                        countDownTimer.cancel();
                    }

                    removeCustoNotiSound();

                    MyApp.getInstance().restartWithGetDataApp();

                } else {
                    final String msg_str = generalFunc.getJsonValueStr(Utils.message_str, responseStringObj);

                    if (countDownTimer != null) {
                        countDownTimer.cancel();
                    }

                    removeCustoNotiSound();

                    GenerateAlertBox alertBox = generalFunc.notifyRestartApp("", generalFunc.retrieveLangLBl("", msg_str));
                    alertBox.setCancelable(false);
                    alertBox.setBtnClickList(btn_id -> {
                        if (msg_str.equals(Utils.GCM_FAILED_KEY) || msg_str.equals(Utils.APNS_FAILED_KEY) || msg_str.equals("LBL_SERVER_COMM_ERROR") || msg_str.equals("DO_RESTART")) {
                            MyApp.getInstance().restartWithGetDataApp();
                        } else {
                            CabRequestedActivity.super.onBackPressed();
                        }
                    });
                }
            } else {

                //startTimer(milliLeft); // start Timer From Paused Seconds - if required in future
                generalFunc.showError(i -> MyApp.getInstance().restartWithGetDataApp());
            }
        });
        exeWebServer.execute();
    }

    public void declineTripRequest() {
        (new CabRequestStatus(getActContext())).updateDriverRequestStatus(1, generalFunc.getJsonValue("PassengerId", message_str), "Decline", "", msgCode);

        HashMap<String, String> parameters = new HashMap<>();
        parameters.put("type", "DeclineTripRequest");
        parameters.put("DriverID", generalFunc.getMemberId());
        parameters.put("PassengerID", generalFunc.getJsonValue("PassengerId", message_str));
        parameters.put("vMsgCode", msgCode);

        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(), parameters);
        exeWebServer.setLoaderConfig(getActContext(), true, generalFunc);
        exeWebServer.setCancelAble(false);
        exeWebServer.setDataResponseListener(responseString -> {
            cancelRequest();
        });
        exeWebServer.execute();
    }

    public HashMap<String, String> generateTripParams() {
        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "GenerateTrip");


        parameters.put("PassengerID", generalFunc.getJsonValue("PassengerId", message_str));
        parameters.put("start_lat", generalFunc.getJsonValue("sourceLatitude", message_str));
        parameters.put("start_lon", generalFunc.getJsonValue("sourceLongitude", message_str));
        parameters.put("iCabBookingId", generalFunc.getJsonValue("iBookingId", message_str));

        if (!iCabRequestId.equalsIgnoreCase("")) {
            parameters.put("iCabRequestId", iCabRequestId);
            parameters.put("DriverID", generalFunc.getMemberId());
        }
        if (!iOrderId.equalsIgnoreCase("")) {
            parameters.put("iOrderId", iOrderId);
            parameters.put("eSystem", Utils.eSystem_Type);
            parameters.put("iDriverId", generalFunc.getMemberId());
        }
        parameters.put("sAddress", pickUpAddress);
        parameters.put("GoogleServerKey", generalFunc.retrieveValue(Utils.GOOGLE_SERVER_ANDROID_DRIVER_APP_KEY));
        parameters.put("vMsgCode", msgCode);
        parameters.put("UserType", Utils.app_type);

        if (userLocation != null) {
            parameters.put("vLatitude", "" + userLocation.getLatitude());
            parameters.put("vLongitude", "" + userLocation.getLongitude());
        } else if (GetLocationUpdates.getInstance() != null && GetLocationUpdates.getInstance().getLastLocation() != null) {
            Location lastLocation = GetLocationUpdates.getInstance().getLastLocation();

            parameters.put("vLatitude", "" + lastLocation.getLatitude());
            parameters.put("vLongitude", "" + lastLocation.getLongitude());
        }

        parameters.put("REQUEST_TYPE", REQUEST_TYPE);

        if (REQUEST_TYPE.equalsIgnoreCase(Utils.eType_Multi_Delivery)) {
            parameters.put("ride_type", REQUEST_TYPE);
        }

        return parameters;
    }

    public void cancelRequest() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        generalFunc.storeData(Utils.DRIVER_CURRENT_REQ_OPEN_KEY, "false");

        cancelCabReq();

        try {
            CabRequestedActivity.super.onBackPressed();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void startTimer(long totalTimeCountInMilliseconds) {

        countDownTimer = new CountDownTimer(totalTimeCountInMilliseconds, 1000) {
            // 1000 means, onTick function will be called at every 1000
            // milliseconds

            @Override
            public void onTick(long leftTimeInMilliseconds) {
                milliLeft = leftTimeInMilliseconds;
                long seconds = leftTimeInMilliseconds / 1000;
                // i++;
                // Setting the Progress Bar to decrease wih the timer
                mProgressBar.setProgress((int) (leftTimeInMilliseconds / 1000));
                progressbar_dialog.setProgress((int) (leftTimeInMilliseconds / 1000));
                textViewShowTime.setTextAppearance(getActContext(), android.R.color.holo_green_dark);
                tvTimeCount_dialog.setTextAppearance(getActContext(), android.R.color.holo_green_dark);

                if ((seconds % 5) == 0) {
                    try {
//                        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
//                        Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
//                        r.play();

                        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                       /* mp = MediaPlayer.create(getApplicationContext(), notification);
                        mp.start();*/
                        if (generalFunc.getJsonValue("DIAL_NOTIFICATION", userProfileJson).equalsIgnoreCase("dial_notification_1.mp3")) {
                            notification = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + getActContext().getPackageName() + "/" + R.raw.notification_1);
                        } else if (generalFunc.getJsonValue("DIAL_NOTIFICATION", userProfileJson).equalsIgnoreCase("dial_notification_2.mp3")) {
                            notification = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + getActContext().getPackageName() + "/" + R.raw.notification_2);
                        } else if (generalFunc.getJsonValue("DIAL_NOTIFICATION", userProfileJson).equalsIgnoreCase("dial_notification_3.mp3")) {
                            notification = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + getActContext().getPackageName() + "/" + R.raw.notification_3);
                        } else if (generalFunc.getJsonValue("DIAL_NOTIFICATION", userProfileJson).equalsIgnoreCase("dial_notification_4.mp3")) {
                            notification = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + getActContext().getPackageName() + "/" + R.raw.notification_4);
                        } else if (generalFunc.getJsonValue("DIAL_NOTIFICATION", userProfileJson).equalsIgnoreCase("dial_notification_5.mp3")) {
                            notification = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + getActContext().getPackageName() + "/" + R.raw.notification_5);
                        } else if (generalFunc.getJsonValue("DIAL_NOTIFICATION", userProfileJson).equalsIgnoreCase("dial_notification_6.mp3")) {
                            notification = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + getActContext().getPackageName() + "/" + R.raw.notification_6);
                        } else if (generalFunc.getJsonValue("DIAL_NOTIFICATION", userProfileJson).equalsIgnoreCase("dial_notification_7.mp3")) {
                            notification = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + getActContext().getPackageName() + "/" + R.raw.notification_7);
                        } else if (generalFunc.getJsonValue("DIAL_NOTIFICATION", userProfileJson).equalsIgnoreCase("dial_notification_8.mp3")) {
                            notification = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + getActContext().getPackageName() + "/" + R.raw.notification_8);
                        } else if (generalFunc.getJsonValue("DIAL_NOTIFICATION", userProfileJson).equalsIgnoreCase("dial_notification_9.mp3")) {
                            notification = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + getActContext().getPackageName() + "/" + R.raw.notification_9);
                        } else if (generalFunc.getJsonValue("DIAL_NOTIFICATION", userProfileJson).equalsIgnoreCase("dial_notification_10.mp3")) {
                            notification = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + getActContext().getPackageName() + "/" + R.raw.notification_10);
                        }
                        mp = MediaPlayer.create(getApplicationContext(), notification);
                        mp.start();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (leftTimeInMilliseconds < timeBlinkInMilliseconds) {

                    if (blink) {
                        textViewShowTime.setVisibility(View.VISIBLE);
                        tvTimeCount_dialog.setVisibility(View.VISIBLE);
                        ufxtvTimeCount.setVisibility(View.VISIBLE);
                    } else {
                        textViewShowTime.setVisibility(View.INVISIBLE);
                        tvTimeCount_dialog.setVisibility(View.INVISIBLE);
                        ufxtvTimeCount.setVisibility(View.INVISIBLE);
                    }

                    blink = !blink;
                }

                textViewShowTime
                        .setText(String.format("%02d", seconds / 60) + ":" + String.format("%02d", seconds % 60));
                ufxtvTimeCount
                        .setText(String.format("%02d", seconds / 60) + ":" + String.format("%02d", seconds % 60));
                tvTimeCount_dialog
                        .setText(String.format("%02d", seconds / 60) + ":" + String.format("%02d", seconds % 60));

            }

            @Override
            public void onFinish() {
                istimerfinish = true;

                (new CabRequestStatus(getActContext())).updateDriverRequestStatus(1, generalFunc.getJsonValue("PassengerId", message_str), "Timeout", "", msgCode);

                textViewShowTime.setVisibility(View.VISIBLE);
                tvTimeCount_dialog.setVisibility(View.VISIBLE);
                progressLayout.setClickable(false);
                //  rightTitleTxt.setEnabled(false);
                cancelRequest();
            }

        }.start();

    }

    public void playMedia() {
        removeSound();
        try {
            mp = new MediaPlayer();
            AssetFileDescriptor afd;
            afd = getAssets().openFd("ringtone.mp3");
            mp.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            mp.prepare();
            mp.setLooping(true);
            mp.start();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void removeCustoNotiSound() {
        if (mp != null) {
            mp.stop();
            mp = null;
        }

        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }

    public void removeSound() {
        if (mp != null) {
            mp.stop();
        }
    }

    public void cancelCabReq() {
        String PassengerId = generalFunc.getJsonValue("PassengerId", message_str);
        ConfigPubNub.getInstance().publishMsg("PASSENGER_" + PassengerId,
                generalFunc.buildRequestCancelJson(PassengerId, msgCode));
        generalFunc.storeData(Utils.DRIVER_CURRENT_REQ_OPEN_KEY, "false");
    }

    public Context getActContext() {
        return CabRequestedActivity.this;
    }

    @Override
    public void onBackPressed() {
        cancelCabReq();
        removeCustoNotiSound();
        super.onBackPressed();
    }

    View marker_view;
    View marker_view1;

    @Override
    public void onMapReady(GoogleMap googleMap) {
        gMap = googleMap;
        if (GetLocationUpdates.retrieveInstance() != null) {
            GetLocationUpdates.getInstance().stopLocationUpdates(this);
        }
        GetLocationUpdates.getInstance().startLocationUpdates(this, this);
        double user_lat = GeneralFunctions.parseDoubleValue(0.0, generalFunc.getJsonValue("sourceLatitude", message_str));
        double user_lon = GeneralFunctions.parseDoubleValue(0.0, generalFunc.getJsonValue("sourceLongitude", message_str));
        googleMap.getUiSettings().setZoomControlsEnabled(false);
        googleMap.getUiSettings().setTiltGesturesEnabled(false);
        googleMap.getUiSettings().setCompassEnabled(false);
        googleMap.getUiSettings().setMyLocationButtonEnabled(false);
        googleMap.getUiSettings().setMapToolbarEnabled(false);
    }

    double user_lat = 0;
    double user_lon = 0;
    double user_destLat = 0;
    double user_destLon = 0;

    boolean isSkip = false;
    LatLng fromLnt;
    LatLng DestLnt;

    public void handleSourceMarker(String etaVal) {
        try {


            if (marker_view == null) {
                marker_view = ((LayoutInflater) getActContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                        .inflate(R.layout.custom_marker, null);
                addressTxt = (MTextView) marker_view
                        .findViewById(R.id.addressTxt);
                etaTxt = (MTextView) marker_view.findViewById(R.id.etaTxt);
            }

            if (marker_view != null) {
                etaTxt = (MTextView) marker_view.findViewById(R.id.etaTxt);
            }

            addressTxt.setTextColor(getActContext().getResources().getColor(R.color.sourceAddressTxt));

            if (isSkip) {

                if (destMarker != null) {
                    destMarker.remove();
                    destMarker = null;
                }
                if (destDotMarker != null) {
                    destDotMarker.remove();
                }
                if (route_polyLine != null) {
                    route_polyLine.remove();
                }

            }


            etaTxt.setVisibility(View.VISIBLE);
            etaTxt.setText(etaVal);

            if (sourceMarker != null) {
                sourceMarker.remove();
                sourceMarker = null;
            }

            if (source_dot_option != null) {
                sourceDotMarker.remove();
                sourceDotMarker = null;
                source_dot_option = null;
            }

            source_dot_option = new MarkerOptions().position(fromLnt).icon(bitmapDescriptorFromVector(getActContext(), R.drawable.ic_source_locate));

            sourceDotMarker = gMap.addMarker(source_dot_option);

            addressTxt.setText(AppFunctions.fromHtml(pickUpAddress));
            MarkerOptions marker_opt_source = new MarkerOptions().position(fromLnt).icon(BitmapDescriptorFactory.fromBitmap(createDrawableFromView(getActContext(), marker_view))).anchor(0.00f, 0.20f);
            if (gMap != null) {
                sourceMarker = gMap.addMarker(marker_opt_source);
                sourceMarker.setTag("1");
            }

            buildBuilder(-1);


        } catch (Exception e) {
            // Backpress done by user then app crashes
            e.printStackTrace();
        }
    }

    public void handleMapAnimation(String responseString, LatLng sourceLocation, LatLng destLocation, String etaVal, String timeVal) {
        try {
            if (isSkip) {
                MapAnimator.getInstance().stopRouteAnim();
                if (route_polyLine != null) {
                    route_polyLine.remove();
                    route_polyLine = null;
                }
                return;
            }

            MapAnimator.getInstance().stopRouteAnim();

            LatLng fromLnt = new LatLng(sourceLocation.latitude, sourceLocation.longitude);
            LatLng toLnt = new LatLng(destLocation.latitude, destLocation.longitude);


            if (marker_view == null) {

                marker_view = ((LayoutInflater) getActContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                        .inflate(R.layout.custom_marker, null);
                addressTxt = (MTextView) marker_view
                        .findViewById(R.id.addressTxt);
                etaTxt = (MTextView) marker_view.findViewById(R.id.etaTxt);
            }

            addressTxt.setTextColor(getActContext().getResources().getColor(R.color.destAddressTxt));


            addressTxt.setText(destinationAddress);

            MarkerOptions marker_opt_dest = new MarkerOptions().position(toLnt);
            etaTxt.setVisibility(View.GONE);

            marker_opt_dest.icon(BitmapDescriptorFactory.fromBitmap(createDrawableFromView(getActContext(), marker_view))).anchor(0.00f, 0.20f);
            if (dest_dot_option != null) {
                destDotMarker.remove();
            }
            dest_dot_option = new MarkerOptions().position(toLnt).icon(bitmapDescriptorFromVector(getActContext(), R.drawable.ic_dest_locate));
            destDotMarker = gMap.addMarker(dest_dot_option);

            if (destMarker != null) {
                destMarker.remove();
            }
            destMarker = gMap.addMarker(marker_opt_dest);
            destMarker.setTag("2");

            handleSourceMarker(etaVal);

            JSONArray obj_routes1 = generalFunc.getJsonArray("routes", responseString);

            if (obj_routes1 != null && obj_routes1.length() > 0) {
                PolylineOptions lineOptions = null;

                if (enableGoogleDirection && !eFly) {
                    if (isGoogle) {
                        HashMap<String, String> routeMap = new HashMap<>();
                        routeMap.put("routes", generalFunc.getJsonArray("routes", responseString).toString());
                        responseString = routeMap.toString();
                        lineOptions = generalFunc.getGoogleRouteOptions(responseString, Utils.dipToPixels(getActContext(), 5), getActContext().getResources().getColor(R.color.black));
                    } else {
                        HashMap<String, String> routeMap = new HashMap<>();
                        routeMap.put("routes", generalFunc.getJsonArray("routes", responseString).toString());
                        responseString = routeMap.toString();
                        lineOptions = getGoogleRouteOptionsHandle(responseString, Utils.dipToPixels(getActContext(), 5), getActContext().getResources().getColor(R.color.black));
                    }
                } else {
                    lineOptions = createCurveRoute(new LatLng(sourceLocation.latitude, sourceLocation.longitude), new LatLng(destLocation.latitude, destLocation.longitude));
                }

                if (lineOptions != null) {
                    if (route_polyLine != null) {
                        route_polyLine.remove();
                        route_polyLine = null;
                    }
                    route_polyLine = gMap.addPolyline(lineOptions);
                    route_polyLine.remove();
                }
            }

            DisplayMetrics metrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(metrics);
            int width = metrics.widthPixels;

            if (route_polyLine != null && route_polyLine.getPoints().size() > 1) {
                MapAnimator.getInstance().animateRoute(gMap, route_polyLine.getPoints(), getActContext());
            }

            gMap.setOnCameraMoveListener(() -> {
                DisplayMetrics displaymetrics = new DisplayMetrics();
                getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
                int height = displaymetrics.heightPixels;
                int width1 = displaymetrics.widthPixels;
            });

            runOnUiThread(() -> {
                if (route_polyLine != null && route_polyLine.getPoints().size() > 1 && !isSkip) {
                    if (marker_view1 == null) {
                        marker_view1 = ((LayoutInflater) getActContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.custom_marker_eta, null);
                        MTextView timeText = (MTextView) marker_view1.findViewById(R.id.etaTxt);
                        timeText.setText(timeVal);
                    }
                    MarkerOptions op = new MarkerOptions()
                            .position(route_polyLine.getPoints().get(route_polyLine.getPoints().size() / 2))
                            .icon(BitmapDescriptorFactory.fromBitmap(createDrawableFromView(getActContext(), marker_view1)))
                            .anchor(0.00f, 0.20f);
                    gMap.addMarker(op);
                }
            });

        } catch (Exception e) {
            Logger.d("http://192.168.1.131:3001/", "::Exception::" + e.toString());
            e.printStackTrace();
        }
    }

    public PolylineOptions getGoogleRouteOptionsHandle(String directionJson, int width, int color) {
        PolylineOptions lineOptions = new PolylineOptions();


        try {
            JSONArray obj_routes1 = generalFunc.getJsonArray("routes", directionJson);

            ArrayList<LatLng> points = new ArrayList<LatLng>();

            if (obj_routes1.length() > 0) {
                // Fetching i-th route
                // Fetching all the points in i-th route
                for (int j = 0; j < obj_routes1.length(); j++) {

                    JSONObject point = generalFunc.getJsonObject(obj_routes1, j);

                    LatLng position = new LatLng(GeneralFunctions.parseDoubleValue(0, generalFunc.getJsonValue("latitude", point).toString()), GeneralFunctions.parseDoubleValue(0, generalFunc.getJsonValue("longitude", point).toString()));


                    points.add(position);

                }


                lineOptions.addAll(points);
                lineOptions.width(width);
                lineOptions.color(color);

                return lineOptions;
            } else {
                return null;
            }
        } catch (Exception e) {
            return null;
        }

    }


    private BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorResId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        vectorDrawable.setBounds(0, 0, Utils.dpToPx(25, getActContext()), Utils.dpToPx(25, getActContext()));
        Bitmap bitmap = Bitmap.createBitmap(Utils.dpToPx(25, getActContext()), Utils.dpToPx(25, getActContext()), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    boolean isRouteFail = false;
    boolean isGoogle = false;

    public void findRoute(String etaVal) {
        isFindRoute = true;
        HashMap<String, String> hashMap = new HashMap<>();
        String serverKey = generalFunc.retrieveValue(Utils.GOOGLE_SERVER_ANDROID_DRIVER_APP_KEY);
        String parameters = "";
        String originLoc = fromLnt.latitude + "," + fromLnt.longitude;
        String destLoc = null;
        destLoc = DestLnt.latitude + "," + DestLnt.longitude;
        parameters = "origin=" + originLoc + "&destination=" + destLoc;


        hashMap.put("s_latitude", fromLnt.latitude + "");
        hashMap.put("s_longitude", fromLnt.longitude + "");
        hashMap.put("d_latitude", DestLnt.latitude + "");
        hashMap.put("d_longitude", DestLnt.longitude + "");

        hashMap.put("parameters", parameters);
        MapServiceApi.getDirectionservice(getActContext(), hashMap, this, false);

        String url = "https://maps.googleapis.com/maps/api/directions/json?" + parameters + "&key=" + serverKey + "&language=" + generalFunc.retrieveValue(Utils.GOOGLE_MAP_LANGUAGE_CODE_KEY) + "&sensor=true";


//        Logger.d("url__", "" + url);
//        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(), url, true);
//        exeWebServer.setDataResponseListener(responseString -> {
//
//            if (responseString != null && !responseString.equals("")) {
//                String status = generalFunc.getJsonValue("status", responseString);
//                if (status.equals("OK")) {
//                    isRouteFail = false;
//                    JSONArray obj_routes = generalFunc.getJsonArray("routes", responseString);
//                    if (obj_routes != null && obj_routes.length() > 0) {
//
//                        JSONObject obj_legs = generalFunc.getJsonObject(generalFunc.getJsonArray("legs", generalFunc.getJsonObject(obj_routes, 0).toString()), 0);
//
//                        distance = "" + (GeneralFunctions.parseDoubleValue(0, generalFunc.getJsonValue("value",
//                                generalFunc.getJsonValue("distance", obj_legs.toString()).toString())));
//
//                        time = "" + (GeneralFunctions.parseDoubleValue(0, generalFunc.getJsonValue("value", generalFunc.getJsonValue("duration", obj_legs.toString()).toString())));
//
//                        sourceLocation = new LatLng(GeneralFunctions.parseDoubleValue(0.0, generalFunc.getJsonValue("lat", generalFunc.getJsonValue("start_location", obj_legs.toString()))),
//                                GeneralFunctions.parseDoubleValue(0.0, generalFunc.getJsonValue("lng", generalFunc.getJsonValue("start_location", obj_legs.toString()))));
//
//                        destLocation = new LatLng(GeneralFunctions.parseDoubleValue(0.0, generalFunc.getJsonValue("lat", generalFunc.getJsonValue("end_location", obj_legs.toString()))),
//                                GeneralFunctions.parseDoubleValue(0.0, generalFunc.getJsonValue("lng", generalFunc.getJsonValue("end_location", obj_legs.toString()))));
//                        //temp animation test
//                        String time1 = generalFunc.getJsonValue("text", generalFunc.getJsonValue("duration", obj_legs.toString()));
//                        handleMapAnimation(responseString, sourceLocation, destLocation, getRouteDistance() /*etaVal*/, time1);
//                    }
//                }
//            }
//        });
        //  exeWebServer.execute();
    }

    public String getTimeTxt(int duration) {

        if (duration < 1) {
            duration = 1;
        }
        String durationTxt = "";
        String timeToreach = duration == 0 ? "--" : "" + duration;

        timeToreach = duration > 60 ? formatHoursAndMinutes(duration) : timeToreach;


        durationTxt = (duration < 60 ? generalFunc.retrieveLangLBl("", "LBL_MINS_SMALL") : generalFunc.retrieveLangLBl("", "LBL_HOUR_TXT"));

        durationTxt = duration == 1 ? generalFunc.retrieveLangLBl("", "LBL_MIN_SMALL") : durationTxt;
        durationTxt = duration > 120 ? generalFunc.retrieveLangLBl("", "LBL_HOURS_TXT") : durationTxt;

        return timeToreach + " " + durationTxt;
    }

    private String getRouteDistance() {
        double distance = GeneralFunctions.calculationByLocation(user_lat, user_lon, user_destLat, user_destLon, "KM");


        String eUnit = generalFunc.getJsonValueStr("eUnit", userProfileJsonObj);
        if (!eUnit.equalsIgnoreCase("KMs")) {
            distance = distance * 0.000621371;
        }
        distance = generalFunc.round(distance, 2);
        if (eUnit.equalsIgnoreCase("KMs")) {
            return distance + "\n" + generalFunc.retrieveLangLBl("", "LBL_KM_DISTANCE_TXT");
        } else {
            return distance + "\n " + generalFunc.retrieveLangLBl("", "LBL_MILE_DISTANCE_TXT");
        }
    }

    private String getTime() {
        double distance = GeneralFunctions.calculationByLocation(user_lat, user_lon, user_destLat, user_destLon, "KM");
        int lowestTime = ((int) (distance * DRIVER_ARRIVED_MIN_TIME_PER_MINUTE));

        if (lowestTime < 1) {
            lowestTime = 1;
        }

        return getTimeTxt(lowestTime);
    }

    // add route polyline line
    public PolylineOptions getGoogleRouteOptions(String directionJson, int width, int color, Context mContext, ArrayList<Stop_Over_Points_Data> list, ArrayList<Stop_Over_Points_Data> wayPointslist, ArrayList<Stop_Over_Points_Data> destPointlist, ArrayList<Stop_Over_Points_Data> finalPointlist, GoogleMap gMap, LatLngBounds.Builder builder) {
        PolylineOptions lineOptions = new PolylineOptions();


        StopOverPointsDataParser parser = new StopOverPointsDataParser(mContext, list, wayPointslist, destPointlist, finalPointlist, gMap, builder);
        List<List<HashMap<String, String>>> routes_list = parser.parse(generalFunc.getJsonObject(directionJson));
        ArrayList<LatLng> points = new ArrayList<LatLng>();

        if (routes_list.size() > 0) {
            // Fetching i-th route
            List<HashMap<String, String>> path = routes_list.get(0);

            // Fetching all the points in i-th route
            for (int j = 0; j < path.size(); j++) {
                HashMap<String, String> point = path.get(j);

                double lat = Double.parseDouble(point.get("lat"));
                double lng = Double.parseDouble(point.get("lng"));
                LatLng position = new LatLng(lat, lng);
                points.add(position);

            }

            lineOptions.addAll(points);
            lineOptions.width(width);
            lineOptions.color(color);

            return lineOptions;
        } else {
            return null;
        }
    }

    public PolylineOptions getGoogleRouteOptions(String directionJson, int width, int color) {
        PolylineOptions lineOptions = new PolylineOptions();

        try {
            DirectionsJSONParser parser = new DirectionsJSONParser();
            List<List<HashMap<String, String>>> routes_list = parser.parse(new JSONObject(directionJson));

            ArrayList<LatLng> points = new ArrayList<LatLng>();

            if (routes_list.size() > 0) {
                // Fetching i-th route
                List<HashMap<String, String>> path = routes_list.get(0);

                // Fetching all the points in i-th route
                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);

                }

                lineOptions.addAll(points);
                lineOptions.width(width);
                lineOptions.color(color);

                return lineOptions;
            } else {
                return null;
            }
        } catch (Exception e) {
            return null;
        }
    }

    public void buildBuilder(int paddingVal) {

        if (sourceMarker != null && (destMarker == null)) {

            builder = new LatLngBounds.Builder();

            builder.include(sourceMarker.getPosition());

            DisplayMetrics metrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(metrics);

            int width = metrics.widthPixels;
            int height = metrics.heightPixels;
            //  int padding = (mainAct != null && mainAct.isMultiDelivery()) ? (width != 0 ? (int) (width * 0.35) : 0) : 0; // offset from edges of the map in pixels

            LatLngBounds bounds = builder.build();
            LatLng center = bounds.getCenter();
            LatLng northEast = SphericalUtil.computeOffset(center, 30 * Math.sqrt(2.0), SphericalUtil.computeHeading(center, bounds.northeast));
            LatLng southWest = SphericalUtil.computeOffset(center, 30 * Math.sqrt(2.0), (180 + (180 + SphericalUtil.computeHeading(center, bounds.southwest))));
            builder.include(southWest);
            builder.include(northEast);
            int padding = (int) (width * 0.25);

            gMap.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), padding));
            if (paddingVal == -1) {
                int extraVal = 75;
                if (getResources().getDisplayMetrics().density == 3) {
                    extraVal = 75;
                } else if (getResources().getDisplayMetrics().density == 2) {
                    extraVal = Utils.dpToPx(75, getActContext());
                }
                gMap.setPadding(0, 0, 0, bottom_sheet.getTop()/*Utils.dpToPx(getActContext(), ((bottom_sheet.getHeight() / 3) + extraVal)) - (bottom_sheet.getTop())*/);
            } else {
//                if (paddingVal > bottom_sheet.getHeight() / 1.5) {
//                    return;
//                }
                gMap.setPadding(0, 0, 0, paddingVal);
            }
        } else if (gMap != null) {
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
                LatLngBounds bounds = builder.build();

                LatLng center = bounds.getCenter();
                LatLng northEast = SphericalUtil.computeOffset(center, 10 * Math.sqrt(2.0), SphericalUtil.computeHeading(center, bounds.northeast));
                LatLng southWest = SphericalUtil.computeOffset(center, 10 * Math.sqrt(2.0), (180 + (180 + SphericalUtil.computeHeading(center, bounds.southwest))));

                builder.include(southWest);
                builder.include(northEast);

                /*  Method 1 */
//                            mainAct.getMap().moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding));

                DisplayMetrics metrics = new DisplayMetrics();
                getWindowManager().getDefaultDisplay().getMetrics(metrics);

                int width = metrics.widthPixels;
                int height = metrics.heightPixels;
                // Set Padding according to included bounds
                int padding = (int) (width * 0.25); // offset from edges of the map 10% of screen

                /*  Method 2 */
                            /*Logger.e("MapHeight","newLatLngZoom");
                            mainAct.getMap().moveCamera(CameraUpdateFactory.newLatLngZoom(builder.build().getCenter(),16));*/

                gMap.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), padding));
                if (paddingVal == -1) {
                    int extraVal = 75;
                    if (getResources().getDisplayMetrics().density == 3) {
                        extraVal = 75;
                    } else if (getResources().getDisplayMetrics().density == 2) {
                        extraVal = Utils.dpToPx(75, getActContext());
                    }
                    gMap.setPadding(0, 0, 0, bottom_sheet.getTop()/*Utils.dpToPx(getActContext(), (bottom_sheet.getHeight() / 3) + extraVal)*/);
                } else {
//                    if (paddingVal > bottom_sheet.getHeight() / 1.5) {
//                        return;
//                    }
                    gMap.setPadding(0, 0, 0, paddingVal);
                }
            }
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

    @Override
    public void onLocationUpdate(Location location) {
        this.userLocation = location;

        if (fromLnt != null) {
            Logger.d("onLocationUpdate", ":: called");
            manageEta();
        }
    }

    @Override
    public void onGlobalLayout() {

    }

    FrameLayout bottom_sheet;

    public void manageBottomDialog() {
        setButtons();

        MTextView requestType = findViewById(R.id.requestType);
        requestType.setSelected(true);
        MTextView pNameTxtView = findViewById(R.id.pNameTxtView);
        MTextView declinebtnTxt = findViewById(R.id.declinebtnTxt);
        MTextView AcceptbtnTxt = findViewById(R.id.AcceptbtnTxt);
        MTextView sourceAddressHTxt = findViewById(R.id.sourceAddressHTxt);
        MTextView sourceAddressTxt = findViewById(R.id.sourceAddressTxt);
        MTextView destAddressHTxt = findViewById(R.id.destAddressHTxt);
        MTextView destAddressTxt = findViewById(R.id.destAddressTxt);

        ImageView imagedest = findViewById(R.id.imagedest);
        ImageView dashImage = findViewById(R.id.dashImage);
        LinearLayout btnArea = findViewById(R.id.btnArea);
        ImageView btnImg = findViewById(R.id.btnImg);
        LinearLayout sourcearea = findViewById(R.id.sourcearea);
        LinearLayout destarea = findViewById(R.id.destarea);
        SimpleRatingBar ratingBar = findViewById(R.id.ratingBar);

        if (generalFunc.isRTLmode()) {
            btnImg.setRotation(180);
            btnArea.setBackground(getActContext().getResources().getDrawable(R.drawable.login_border_rtl));
        }

        btnArea.setOnClickListener(v -> {
            acceptRequest();
        });
        declinebtnTxt.setOnClickListener(v -> {
            declineTripRequest();
        });

        if (REQUEST_TYPE.equalsIgnoreCase(Utils.CabGeneralType_Ride)) {
            sourceAddressHTxt.setText(generalFunc.retrieveLangLBl("", "LBL_PICKUP_LOCATION_HEADER_TXT"));
            destAddressHTxt.setText(generalFunc.retrieveLangLBl("", "LBL_DEST_ADD_TXT"));

            if (eFly) {
                destarea.setVisibility(View.GONE);
                imagedest.setVisibility(View.GONE);
                dashImage.setVisibility(View.GONE);
            }

        } else if (REQUEST_TYPE.equalsIgnoreCase(Utils.CabGeneralType_UberX)) {
            destarea.setVisibility(View.GONE);
            imagedest.setVisibility(View.GONE);
            dashImage.setVisibility(View.GONE);
            sourceAddressHTxt.setText(generalFunc.retrieveLangLBl("Job Location", "LBL_JOB_LOCATION_TXT"));
        } else if (REQUEST_TYPE.equals("Deliver") || REQUEST_TYPE.equalsIgnoreCase(Utils.eType_Multi_Delivery)) {
            sourceAddressHTxt.setText(generalFunc.retrieveLangLBl("", "LBL_SENDER_LOCATION"));
            destAddressHTxt.setText(generalFunc.retrieveLangLBl("", "LBL_RECEIVER_LOCATION"));

            if (REQUEST_TYPE.equalsIgnoreCase(Utils.eType_Multi_Delivery)) {
                destarea.setVisibility(View.GONE);
                imagedest.setVisibility(View.GONE);
                dashImage.setVisibility(View.GONE);
            }
        } else {
            sourceAddressHTxt.setText(generalFunc.retrieveLangLBl("", "LBL_LOCATION_FOR_FRONT"));
            destAddressHTxt.setText(generalFunc.retrieveLangLBl("", "LBL_DELIVERY_LOCATION_TXT"));
        }

        destAddressTxt.setText(destinationAddress);
        sourceAddressTxt.setText(pickUpAddress);

        if (isSkip) {
            destarea.setVisibility(View.GONE);
            imagedest.setVisibility(View.GONE);
            dashImage.setVisibility(View.GONE);
        }

        declinebtnTxt.setText(generalFunc.retrieveLangLBl("", "LBL_DECLINE_TXT"));
        AcceptbtnTxt.setText(generalFunc.retrieveLangLBl("", "LBL_ACCEPT_TXT"));

        requestType.setText(requestTypeVal);
        pNameTxtView.setText(generalFunc.getJsonValue("PName", message_str));
        ratingBar.setRating(GeneralFunctions.parseFloatValue(0, generalFunc.getJsonValue("PRating", message_str)));

        AnchorBottomSheetBehavior behavior = AnchorBottomSheetBehavior.from(bottom_sheet);
        behavior.setState(AnchorBottomSheetBehavior.STATE_COLLAPSED);

        progressLayout_frame.setVisibility(View.VISIBLE);
        progressLayout_frame_dialog.setVisibility(View.GONE);

        behavior.setPeekHeight(getResources().getDimensionPixelSize(R.dimen._100sdp));
        behavior.addBottomSheetCallback(new AnchorBottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View view, int i, int i1) {

                if (i1 == AnchorBottomSheetBehavior.STATE_COLLAPSED) {
                    progressLayout_frame_dialog.setVisibility(View.GONE);
                    progressLayout_frame.setVisibility(View.VISIBLE);
                    behavior.setPeekHeight(getResources().getDimensionPixelSize(R.dimen._100sdp));
                } else if (i1 == AnchorBottomSheetBehavior.STATE_EXPANDED || i1 == AnchorBottomSheetBehavior.STATE_DRAGGING) {
                    progressLayout_frame_dialog.setVisibility(View.VISIBLE);
                    progressLayout_frame.setVisibility(View.GONE);
                    behavior.setPeekHeight(getResources().getDimensionPixelSize(R.dimen._165sdp));

                }


            }

            @Override
            public void onSlide(@NonNull View view, float v) {


                int extraVal = 75;

                if (getResources().getDisplayMetrics().density == 3) {
                    extraVal = 75;
                } else if (getResources().getDisplayMetrics().density == 2) {
                    extraVal = Utils.dpToPx(75, getActContext());
                }


                buildBuilder(Utils.dpToPx(getActContext(), ((bottom_sheet.getHeight() / 4) + extraVal)) - (view.getTop()));

            }
        });

    }

    private void setButtons() {

        MTextView declinebtnTxt1 = findViewById(R.id.declinebtnTxt1);
        MTextView AcceptbtnTxt1 = findViewById(R.id.AcceptbtnTxt1);
        ImageView btnImg = findViewById(R.id.btnImg1);
        LinearLayout btnArea1 = findViewById(R.id.btnArea1);
        if (generalFunc.isRTLmode()) {
            btnImg.setRotation(180);
            btnArea1.setBackground(getResources().getDrawable(R.drawable.login_border_rtl));


        }

        declinebtnTxt1.setText(generalFunc.retrieveLangLBl("", "LBL_DECLINE_TXT"));
        AcceptbtnTxt1.setText(generalFunc.retrieveLangLBl("", "LBL_ACCEPT_TXT"));

        btnArea1.setOnClickListener(v -> {
            acceptRequest();
        });
        declinebtnTxt1.setOnClickListener(v -> {
            declineTripRequest();
        });
    }

    public void setCancelable(Dialog dialogview, boolean cancelable) {
        final Dialog dialog = dialogview;
        View touchOutsideView = dialog.getWindow().getDecorView().findViewById(R.id.touch_outside);
        View bottomSheetView = dialog.getWindow().getDecorView().findViewById(R.id.design_bottom_sheet);

        if (cancelable) {
            touchOutsideView.setOnClickListener(v -> {
                if (dialog.isShowing()) {
                    dialog.cancel();
                }
            });
            BottomSheetBehavior.from(bottomSheetView).setHideable(true);
        } else {
            touchOutsideView.setOnClickListener(null);
            BottomSheetBehavior.from(bottomSheetView).setHideable(false);
        }
    }

    @Override
    public void searchResult(ArrayList<HashMap<String, String>> placelist, int selectedPos, String input) {

    }

    @Override
    public void resetOrAddDest(int selPos, String address, double latitude, double longitude, String isSkip) {

    }

    @Override
    public void directionResult(HashMap<String, String> directionlist) {

        String responseString = directionlist.get("routes");

        if (!isFindRoute) {

            if (responseString != null && !responseString.equalsIgnoreCase("")) {
                if (responseString != null && !responseString.equalsIgnoreCase("") && directionlist.get("distance") == null) {

                    responseString = generalFunc.getJsonValue("routes", responseString);

                    JSONArray obj_routes = generalFunc.getJsonArray(responseString);
                    if (obj_routes != null && obj_routes.length() > 0) {
                        JSONObject obj_legs = generalFunc.getJsonObject(generalFunc.getJsonArray("legs", generalFunc.getJsonObject(obj_routes, 0).toString()), 0);

                        pickUpAddress = generalFunc.getJsonValue("start_address", obj_legs.toString());
                        destinationAddress = generalFunc.getJsonValue("end_address", obj_legs.toString());
                    }
                    isloadedAddress = true;
                    if (destinationAddress.equalsIgnoreCase("")) {
                        destinationAddress = "----";
                    }
                    destAddressTxt.setText(destinationAddress);
                    locationAddressTxt.setText(pickUpAddress);
                    ufxlocationAddressTxt.setText(pickUpAddress);
                }

            }
        } else {
            if (responseString != null && !responseString.equalsIgnoreCase("")) {
                isRouteFail = false;

                if (responseString != null && !responseString.equalsIgnoreCase("") && directionlist.get("distance") == null) {
                    isGoogle = true;

//                    JSONArray obj_routes = generalFunc.getJsonArray(responseString);
                    JSONArray obj_routes = generalFunc.getJsonArray("routes", responseString);

                    if (obj_routes != null && obj_routes.length() > 0) {

                        JSONObject obj_legs = generalFunc.getJsonObject(generalFunc.getJsonArray("legs", generalFunc.getJsonObject(obj_routes, 0).toString()), 0);

                        distance = "" + (GeneralFunctions.parseDoubleValue(0, generalFunc.getJsonValue("value",
                                generalFunc.getJsonValue("distance", obj_legs.toString()).toString())));

                        time = "" + (GeneralFunctions.parseDoubleValue(0, generalFunc.getJsonValue("value", generalFunc.getJsonValue("duration", obj_legs.toString()).toString())));

                        sourceLocation = new LatLng(GeneralFunctions.parseDoubleValue(0.0, generalFunc.getJsonValue("lat", generalFunc.getJsonValue("start_location", obj_legs.toString()))),
                                GeneralFunctions.parseDoubleValue(0.0, generalFunc.getJsonValue("lng", generalFunc.getJsonValue("start_location", obj_legs.toString()))));

                        destLocation = new LatLng(GeneralFunctions.parseDoubleValue(0.0, generalFunc.getJsonValue("lat", generalFunc.getJsonValue("end_location", obj_legs.toString()))),
                                GeneralFunctions.parseDoubleValue(0.0, generalFunc.getJsonValue("lng", generalFunc.getJsonValue("end_location", obj_legs.toString()))));
                        //temp animation test
                        String time1 = generalFunc.getJsonValue("text", generalFunc.getJsonValue("duration", obj_legs.toString()));


                        handleMapAnimation(responseString, sourceLocation, destLocation, getRouteDistance() /*etaVal*/, time1);
                    }

                } else {
                    isGoogle = false;
                    distance = directionlist.get("distance");
                    time = directionlist.get("duration");

                    sourceLocation = new LatLng(GeneralFunctions.parseDoubleValue(0.0, directionlist.get("s_latitude")), GeneralFunctions.parseDoubleValue(0.0, directionlist.get("s_longitude"))
                    );

                    destLocation = new LatLng(GeneralFunctions.parseDoubleValue(0.0, directionlist.get("d_latitude")), GeneralFunctions.parseDoubleValue(0.0, directionlist.get("d_longitude"))
                    );


                    String timeToreach = "1";
                    int duration = (int) Math.round((generalFunc.parseDoubleValue(0.0,
                            directionlist.get("duration")) / 60));
                    if (duration < 1) {
                        duration = 1;
                    }

                    timeToreach = duration > 60 ? formatHoursAndMinutes(duration) : timeToreach;

                    String durationTxt = (duration < 60 ? generalFunc.retrieveLangLBl("", "LBL_MINS_SMALL") : generalFunc.retrieveLangLBl("", "LBL_HOUR_TXT"));


                    handleMapAnimation(getRouteDetails(directionlist), sourceLocation, destLocation, getRouteDistance() /*etaVal*/, getTimeTxt((int) (GeneralFunctions.parseDoubleValue(0, time) / 60)));
                }


            }
        }

    }

    public String getRouteDetails(HashMap<String, String> directionlist)
    {
        HashMap<String, String> routeMap = new HashMap<>();
        routeMap.put("routes", directionlist.get("routes"));
        return routeMap.toString();
    }

    public static String formatHoursAndMinutes(int totalMinutes) {
        String minutes = Integer.toString(totalMinutes % 60);
        minutes = minutes.length() == 1 ? "0" + minutes : minutes;
        return (totalMinutes / 60) + ":" + minutes;
    }


    @Override
    public void geoCodeAddressFound(String address, double latitude, double longitude, String geocodeobject) {

    }

    public class setOnClickList implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            Utils.hideKeyboard(CabRequestedActivity.this);
            switch (view.getId()) {
                case R.id.progressLayout:
                    //  acceptRequest();
                    break;

                case R.id.deliveryDetailsBtn:
                    Bundle bn = new Bundle();
                    bn.putString("TripId", "");
                    bn.putString("iCabBookingId", generalFunc.getJsonValue("iBookingId", message_str));
                    bn.putString("iCabRequestId", iCabRequestId);
                    bn.putString("Status", "cabRequestScreen");
                    new StartActProcess(getActContext()).startActWithData(ViewMultiDeliveryDetailsActivity.class, bn);
                    break;
                case R.id.moreServiceBtn:
                    Bundle bundle = new Bundle();
                    bundle.putString("iCabRequestId", iCabRequestId);
                    new StartActProcess(getActContext()).startActWithData(MoreServiceInfoActivity.class, bundle);
                    break;
            }
        }
    }

}
