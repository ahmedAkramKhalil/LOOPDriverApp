package com.taxifgo.driver.deliverAll;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.location.Location;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.taxifgo.driver.R;
import com.general.files.ConfigPubNub;
import com.general.files.ExecuteWebServerUrl;
import com.general.files.GeneralFunctions;
import com.general.files.GetLocationUpdates;
import com.general.files.MyApp;
import com.utils.Utils;
import com.view.GenerateAlertBox;
import com.view.MTextView;
import com.view.simpleratingbar.SimpleRatingBar;

import org.apache.commons.lang3.text.WordUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;


public class DeliverAllCabRequestedActivity extends AppCompatActivity implements GenerateAlertBox.HandleAlertBtnClick, GetLocationUpdates.LocationUpdatesListener {

    public GeneralFunctions generalFunc;
    MTextView leftTitleTxt;
    MTextView rightTitleTxt;
    ProgressBar mProgressBar;
    String message_str, msgCode = "";
    MTextView pNameTxtView;
    MTextView locationAddressTxt;
    MTextView restaurantAddressHintTxt;
    MTextView restaurantAddressTxt;
    MTextView destAddressTxt;
    String pickUpAddress = "";
    String destinationAddress = "";
    ConfigPubNub configPubNub;
    GenerateAlertBox generateAlert;
    int maxProgressValue = 30;
    MediaPlayer mp = new MediaPlayer();
    private MTextView tvTimeCount; // will show the time
    private CountDownTimer countDownTimer; // built in android class
    // CountDownTimer
    private long totalTimeCountInMilliseconds = maxProgressValue * 1 * 1000; // total count down time in
    // milliseconds
    private long timeBlinkInMilliseconds = 10 * 1000; // start time of start blinking
    private boolean blink; // controls the blinking .. on and off

    private MTextView locationAddressHintTxt;
    private MTextView destAddressHintTxt;

    SimpleRatingBar ratingBar;
    boolean istimerfinish = false;
    String iOrderId = "";
    boolean isloadedAddress = false;
    MTextView specialHintTxt, specialValTxt;
    String specialUserComment = "";

    ImageView backImageView;
    private Location userLocation;

    String sourceLatitude = "", sourceLongitude = "";
    String destLatitude = "", destLongitude = "";
    long milliLeft;
    private String LBL_STORE_NAME = "", PName = "";
    String userProfileJson = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON);
        setContentView(R.layout.activity_deliver_all_cab_requested);

        generalFunc = MyApp.getInstance().getGeneralFun(getActContext());
        userProfileJson = generalFunc.retrieveValue(Utils.USER_PROFILE_JSON);
        generalFunc.removeValue(Utils.DRIVER_ACTIVE_REQ_MSG_KEY);

        if (GetLocationUpdates.retrieveInstance() != null) {
            GetLocationUpdates.getInstance().startLocationUpdates(this, this);
        }

        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        MyApp.getInstance().stopAlertService();

        message_str = getIntent().getStringExtra("Message");
        msgCode = generalFunc.getJsonValue("MsgCode", message_str);
        String PName = generalFunc.getJsonValue("PName", message_str);

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

        leftTitleTxt = (MTextView) findViewById(R.id.leftTitleTxt);
        rightTitleTxt = (MTextView) findViewById(R.id.rightTitleTxt);
        pNameTxtView = (MTextView) findViewById(R.id.pNameTxtView);
        locationAddressTxt = (MTextView) findViewById(R.id.locationAddressTxt);
        restaurantAddressHintTxt = (MTextView) findViewById(R.id.restaurantAddressHintTxt);
        locationAddressHintTxt = (MTextView) findViewById(R.id.locationAddressHintTxt);
        destAddressHintTxt = (MTextView) findViewById(R.id.destAddressHintTxt);
        destAddressTxt = (MTextView) findViewById(R.id.destAddressTxt);
        restaurantAddressTxt = (MTextView) findViewById(R.id.restaurantAddressTxt);
        specialHintTxt = (MTextView) findViewById(R.id.specialHintTxt);
        specialValTxt = (MTextView) findViewById(R.id.specialValTxt);
        backImageView = (ImageView) findViewById(R.id.backImageView);
        backImageView.setVisibility(View.GONE);


        mProgressBar = (ProgressBar) findViewById(R.id.progressbar);
        ratingBar = (SimpleRatingBar) findViewById(R.id.ratingBar);
        tvTimeCount = (MTextView) findViewById(R.id.tvTimeCount);
        (findViewById(R.id.menuImgView)).setVisibility(View.GONE);
        leftTitleTxt.setVisibility(View.VISIBLE);
        rightTitleTxt.setVisibility(View.VISIBLE);


        maxProgressValue = GeneralFunctions.parseIntegerValue(30, generalFunc.retrieveValue("RIDER_REQUEST_ACCEPT_TIME"));
        totalTimeCountInMilliseconds = maxProgressValue * 1 * 1000; // total count down time in
        tvTimeCount.setText(maxProgressValue + ":" + "00");
        mProgressBar.setMax(maxProgressValue);
        mProgressBar.setProgress(maxProgressValue);

        setLabels();

        generateAlert = new GenerateAlertBox(getActContext());
        generateAlert.setBtnClickList(this);
        generateAlert.setCancelable(false);

        setData();

        startTimer(totalTimeCountInMilliseconds);
        leftTitleTxt.setOnClickListener(new setOnClickList());
        rightTitleTxt.setOnClickListener(new setOnClickList());
        findViewById(R.id.requestTimerArea).setOnClickListener(new setOnClickList());
    }

    public void setLabels() {
        LBL_STORE_NAME = generalFunc.retrieveLangLBl("Store Name", "LBL_STORE_NAME");
        leftTitleTxt.setText(generalFunc.retrieveLangLBl("", "LBL_DECLINE_TXT"));
        rightTitleTxt.setText(generalFunc.retrieveLangLBl("", "LBL_ACCEPT_TXT"));
        locationAddressHintTxt.setText(generalFunc.retrieveLangLBl("Store Location", "LBL_LOCATION_FOR_FRONT"));
        destAddressHintTxt.setText(generalFunc.retrieveLangLBl("Delivery Location", "LBL_DELIVERY_LOCATION_TXT"));
        restaurantAddressHintTxt.setText(LBL_STORE_NAME);
        ((MTextView) findViewById(R.id.hintTxt)).setText(generalFunc.retrieveLangLBl("", "LBL_HINT_TAP_TXT"));
        specialHintTxt.setText(generalFunc.retrieveLangLBl("Special Instruction", "LBL_SPECIAL_INSTRUCTION_TXT"));
    }

    public void setData() {
        sourceLatitude = generalFunc.getJsonValue("sourceLatitude", message_str);
        destLatitude = generalFunc.getJsonValue("destLatitude", message_str);
        destLongitude = generalFunc.getJsonValue("destLongitude", message_str);
        sourceLongitude = generalFunc.getJsonValue("sourceLongitude", message_str);


        pNameTxtView.setText(PName);
        ratingBar.setRating(GeneralFunctions.parseFloatValue(0, generalFunc.getJsonValue("PRating", message_str)));

        double pickupLat = GeneralFunctions.parseDoubleValue(0.0, sourceLatitude);
        double pickupLog = GeneralFunctions.parseDoubleValue(0.0, sourceLatitude);

        iOrderId = generalFunc.getJsonValue("iOrderId", message_str);

        double desLat = 0.0;
        double destLog = 0.0;

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


        if (iOrderId != null && !iOrderId.equals("")) {
            // api call
            getAddressFormServer();
        } else {

            findAddressByDirectionAPI(url_str);

        }

        String REQUEST_TYPE = generalFunc.getJsonValue("REQUEST_TYPE", message_str);

        ((MTextView) findViewById(R.id.requestType)).setText(generalFunc.retrieveLangLBl("Delivery", "LBL_DELIVERY") + " " + generalFunc.retrieveLangLBl("Request", "LBL_REQUEST"));
    }

    public void getAddressFormServer() {

        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "getCabRequestAddress");
        parameters.put("iOrderId", iOrderId);
        parameters.put("eSystem", Utils.eSystem_Type);

        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(), parameters);

        exeWebServer.setDataResponseListener(responseString -> {
            JSONObject responseStringObject = generalFunc.getJsonObject(responseString);

            if (responseStringObject != null && !responseStringObject.equals("")) {

                boolean isDataAvail = GeneralFunctions.checkDataAvail(Utils.action_str, responseStringObject);

                if (isDataAvail) {

                    String MessageJson = generalFunc.getJsonValueStr(Utils.message_str, responseStringObject);


                    String tSourceAddress = generalFunc.getJsonValue("tSourceAddress", MessageJson);

                    if (Utils.checkText(tSourceAddress)) {
                        pickUpAddress = WordUtils.capitalize(tSourceAddress);
                    }
                    String tDestAddress = generalFunc.getJsonValue("tDestAddress", MessageJson);

                    if (Utils.checkText(tDestAddress)) {
                        destinationAddress = WordUtils.capitalize(tDestAddress);
                    }

                    String tUserComment = generalFunc.getJsonValue("tUserComment", MessageJson);

                    if (tUserComment != null && !tUserComment.equals("")) {
                        specialHintTxt.setVisibility(View.VISIBLE);
                        specialValTxt.setVisibility(View.VISIBLE);
                        specialUserComment = tUserComment;
                        specialValTxt.setText(tUserComment);
                    } else {
                        specialValTxt.setText("------------");
                    }


                    isloadedAddress = true;

                    if (destinationAddress.equalsIgnoreCase("")) {
                        destinationAddress = "----";
                    }

                    String restName = "";
                    if (Utils.checkText(PName)) {
                        restName = WordUtils.capitalize(PName);
                    }

                    restaurantAddressTxt.setText(restName);
                    destAddressTxt.setText(destinationAddress);

                    locationAddressTxt.setText(restName + "\n" + pickUpAddress);


                } else {
                    callgetAddress();
                }
            } else {
                callgetAddress();
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

                        String start_address = generalFunc.getJsonValueStr("start_address", obj_legs);
                        if (Utils.checkText(start_address)) {
                            pickUpAddress = WordUtils.capitalize(start_address);
                        }

                        String end_address = generalFunc.getJsonValueStr("end_address", obj_legs);

                        if (Utils.checkText(end_address)) {
                            destinationAddress = WordUtils.capitalize(end_address);
                        }

                    }
                    isloadedAddress = true;

                    if (destinationAddress.equalsIgnoreCase("")) {
                        destinationAddress = "----";
                    }

                    String restName = "";
                    if (Utils.checkText(PName)) {
                        restName = LBL_STORE_NAME + " : " + WordUtils.capitalize(PName);
                    }

                    restaurantAddressTxt.setText(restName);
                    destAddressTxt.setText(destinationAddress);
                    locationAddressTxt.setText(restName + "\n" + pickUpAddress);


                } else {
                    callDirectionsApi(url);


                }

            } else {
                callDirectionsApi(url);
            }
        });
        exeWebServer.execute();

    }

    private void callDirectionsApi(String url) {
        new Handler().postDelayed(() -> findAddressByDirectionAPI(url), 2000);
    }

    private void callgetAddress() {
        new Handler().postDelayed(() -> getAddressFormServer(), 2000);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (istimerfinish) {

            runOnUiThread(() -> backImageView.setVisibility(View.VISIBLE));

            finish();
            trimCache(getActContext());
            istimerfinish = false;
        }
    }

    public static void trimCache(Context context) {
        try {
            File dir = context.getCacheDir();
            if (dir != null && dir.isDirectory()) {
                deleteDir(dir);
            }
        } catch (Exception e) {

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
        // The directory is now empty so delete it
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
        Utils.hideKeyboard(DeliverAllCabRequestedActivity.this);

        cancelRequest();
    }

    public void acceptRequest() {
        /*Stop Timer*/
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        rightTitleTxt.setEnabled(false);
        leftTitleTxt.setEnabled(false);
        generateTrip();
    }

    public void generateTrip() {

        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(), generateTripParams());
        exeWebServer.setLoaderConfig(getActContext(), true, generalFunc);
        exeWebServer.setIsDeviceTokenGenerate(true, "vDeviceToken", generalFunc);
        exeWebServer.setCancelAble(false);
        exeWebServer.setDataResponseListener(new ExecuteWebServerUrl.SetDataResponse() {
            @Override
            public void setResponse(String responseString) {
                JSONObject responseStringObject = generalFunc.getJsonObject(responseString);

                if (responseStringObject != null && !responseStringObject.equals("")) {

                    boolean isDataAvail = GeneralFunctions.checkDataAvail(Utils.action_str, responseStringObject);

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

                        final String msg_str = generalFunc.getJsonValueStr(Utils.message_str, responseStringObject);

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
                                DeliverAllCabRequestedActivity.super.onBackPressed();
                            }
                        });
                    }
                } else {
                    rightTitleTxt.setEnabled(true);
                    leftTitleTxt.setEnabled(true);
                    //startTimer(milliLeft); // start Timer From Paused Seconds - if required in future
                    generalFunc.showError(i -> MyApp.getInstance().restartWithGetDataApp());
                }
            }
        });
        exeWebServer.execute();
    }

    public void declineTripRequest() {

        HashMap<String, String> parameters = new HashMap<>();
        parameters.put("type", "DeclineTripRequest");
        parameters.put("iDriverId", generalFunc.getMemberId());
        parameters.put("iOrderId", iOrderId);
        parameters.put("vMsgCode", msgCode);
        parameters.put("eSystem", Utils.eSystem_Type);

        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(), parameters);
        exeWebServer.setLoaderConfig(getActContext(), true, generalFunc);
        exeWebServer.setCancelAble(false);
        exeWebServer.setDataResponseListener(responseString -> cancelRequest());
        exeWebServer.execute();
    }

    public HashMap<String, String> generateTripParams() {
        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "GenerateTrip");

        parameters.put("iDriverId", generalFunc.getMemberId());
        parameters.put("iOrderId", iOrderId);

        parameters.put("tSourceLat", sourceLatitude);
        parameters.put("tSourceLong", sourceLongitude);
        parameters.put("tSourceAddress", pickUpAddress);

        parameters.put("tDestLatitude", destLatitude);
        parameters.put("tDestLongitude", destLongitude);
        parameters.put("tDestAddress", destinationAddress);

        parameters.put("GoogleServerKey", generalFunc.retrieveValue(Utils.GOOGLE_SERVER_ANDROID_DRIVER_APP_KEY));
        parameters.put("vMsgCode", msgCode);
        parameters.put("UserType", Utils.app_type);
//        parameters.put("TimeZone", generalFunc.getTimezone());
        parameters.put("eSystem", Utils.eSystem_Type);

        if (userLocation != null) {
            parameters.put("vLatitude", "" + userLocation.getLatitude());
            parameters.put("vLongitude", "" + userLocation.getLongitude());
        } else if (GetLocationUpdates.getInstance() != null && GetLocationUpdates.getInstance().getLastLocation() != null) {
            Location lastLocation = GetLocationUpdates.getInstance().getLastLocation();

            parameters.put("vLatitude", "" + lastLocation.getLatitude());
            parameters.put("vLongitude", "" + lastLocation.getLongitude());
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
            DeliverAllCabRequestedActivity.super.onBackPressed();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void startTimer(long totalTimeCountInMilliseconds) {
//        playMedia();
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

                if ((seconds % 5) == 0) {
                    try {
                        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

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
                        tvTimeCount.setVisibility(View.VISIBLE);
                    } else {
                        tvTimeCount.setVisibility(View.INVISIBLE);
                    }

                    blink = !blink;
                }

                tvTimeCount
                        .setText(String.format("%02d", seconds / 60) + ":" + String.format("%02d", seconds % 60));

            }

            @Override
            public void onFinish() {
                istimerfinish = true;
                tvTimeCount.setVisibility(View.VISIBLE);
                rightTitleTxt.setEnabled(false);
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

        //milan code for working all app

//        try {
//            mp = MediaPlayer.create(getActContext(), R.raw.ringdriver); mp.setLooping(true); mp.start(); }
//        catch (IllegalStateException e) { } catch (Exception e) { }
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
        if (configPubNub != null) {
            configPubNub.publishMsg("PASSENGER_" + generalFunc.getJsonValue("PassengerId", message_str),
                    generalFunc.buildRequestCancelJson(generalFunc.getJsonValue("PassengerId", message_str), msgCode));
            configPubNub = null;
        }
        generalFunc.storeData(Utils.DRIVER_CURRENT_REQ_OPEN_KEY, "false");
    }

    public Context getActContext() {
        return DeliverAllCabRequestedActivity.this;
    }

    @Override
    public void onBackPressed() {
        cancelCabReq();
        removeCustoNotiSound();
        super.onBackPressed();


    }

    public void onLocationUpdate(Location location) {
        this.userLocation = location;
    }


    public class setOnClickList implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            Utils.hideKeyboard(DeliverAllCabRequestedActivity.this);
            switch (view.getId()) {
                case R.id.requestTimerArea:
                    acceptRequest();
                    break;
                case R.id.leftTitleTxt:
                    //cancelRequest();
                    declineTripRequest();
                    break;
                case R.id.rightTitleTxt:
                    acceptRequest();
                    break;
            }
        }
    }

}
