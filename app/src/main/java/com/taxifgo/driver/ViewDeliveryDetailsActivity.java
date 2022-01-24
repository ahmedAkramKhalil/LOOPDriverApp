package com.taxifgo.driver;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.general.files.AppFunctions;
import com.general.files.ExecuteWebServerUrl;
import com.general.files.GeneralFunctions;
import com.general.files.MyApp;
import com.general.files.SinchService;
import com.general.files.StartActProcess;
import com.sinch.android.rtc.calling.Call;
import com.squareup.picasso.Picasso;
import com.utils.CommonUtilities;
import com.utils.Utils;
import com.view.CreateRoundedView;
import com.view.ErrorView;
import com.view.MTextView;
import com.view.SelectableRoundedImageView;

import org.json.JSONObject;

import java.util.HashMap;

public class ViewDeliveryDetailsActivity extends BaseActivity {

    public GeneralFunctions generalFunc;
    MTextView titleTxt;
    ImageView backImgView;
    ErrorView errorView;
    ProgressBar loading;

    View contentArea;

    String data_message;

    HashMap<String, String> trip_data;
    String vImage = "";
    String vName = "";
    SelectableRoundedImageView userProfileImgView;
    private LinearLayout  chatArea, callArea,receiverCallArea,receiverMsgArea;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_delivery_details);

        generalFunc = MyApp.getInstance().getGeneralFun(getActContext());

        titleTxt = (MTextView) findViewById(R.id.titleTxt);
        backImgView = (ImageView) findViewById(R.id.backImgView);
        contentArea = findViewById(R.id.contentArea);
        errorView = (ErrorView) findViewById(R.id.errorView);
        loading = (ProgressBar) findViewById(R.id.loading);

        backImgView.setOnClickListener(new setOnClickList());

        setLabels();

        getDeliveryData();
        userProfileImgView = findViewById(R.id.userProfileImgView);


        chatArea = (LinearLayout) findViewById(R.id.chatArea);
        callArea = (LinearLayout) findViewById(R.id.callArea);

        chatArea.setOnClickListener(new setOnClickList());
        callArea.setOnClickListener(new setOnClickList());


        receiverCallArea = (LinearLayout) findViewById(R.id.receiverCallArea);
        receiverMsgArea = (LinearLayout) findViewById(R.id.receiverMsgArea);

        receiverCallArea.setOnClickListener(new setOnClickList());
        receiverMsgArea.setOnClickListener(new setOnClickList());


        int apptheme = getActContext().getResources().getColor(R.color.appThemeColor_1);
        int transpenrent = getActContext().getResources().getColor(R.color.mdtp_transparent_full);
        int white = getActContext().getResources().getColor(R.color.white);
        int bordercolor = getActContext().getResources().getColor(R.color.gray_holo_light);
        int cornorRadius = Utils.dipToPixels(getActContext(), 5);
        int strokeWidth = Utils.dipToPixels(getActContext(), 1);

        new CreateRoundedView(transpenrent, cornorRadius, strokeWidth, white, chatArea);
        new CreateRoundedView(white, cornorRadius, strokeWidth, white, callArea);

         new CreateRoundedView(white, cornorRadius, strokeWidth, bordercolor, receiverCallArea);
        new CreateRoundedView(white, cornorRadius, strokeWidth, bordercolor, receiverMsgArea);


        trip_data = (HashMap<String, String>) getIntent().getSerializableExtra("data_trip");


        vName = trip_data.get("PName");

        if (!vName.equals("")) {
            vImage = CommonUtilities.USER_PHOTO_PATH + trip_data.get("PassengerId") + "/"
                    + vName;
        }

    }

    public void setLabels() {
        titleTxt.setText(generalFunc.retrieveLangLBl("Delivery Details", "LBL_DELIVERY_DETAILS"));

       // ((MTextView) findViewById(R.id.senderHTxt)).setText(generalFunc.retrieveLangLBl("Sender", "LBL_SENDER"));
        ((MTextView) findViewById(R.id.senderCallTxt)).setText(generalFunc.retrieveLangLBl("", "LBL_CALL_TXT"));
        ((MTextView) findViewById(R.id.senderMsgTxt)).setText(generalFunc.retrieveLangLBl("Message", "LBL_MESSAGE_TXT"));
//        ((MTextView) findViewById(R.id.senderMsgTxt)).setText(generalFunc.retrieveLangLBl("", "LBL_MESSAGE_TXT"));
        ((MTextView) findViewById(R.id.receiverHTxt)).setText(generalFunc.retrieveLangLBl("Recipient", "LBL_RECIPIENT"));
        ((MTextView) findViewById(R.id.receiverCallTxt)).setText(generalFunc.retrieveLangLBl("", "LBL_CALL_TXT"));
        ((MTextView) findViewById(R.id.receiverMsgTxt)).setText(generalFunc.retrieveLangLBl("", "LBL_MESSAGE_TXT"));
        ((MTextView) findViewById(R.id.packageTypeHTxt)).setText(generalFunc.retrieveLangLBl("Package Type", "LBL_PACKAGE_TYPE"));
        ((MTextView) findViewById(R.id.packageDetailsHTxt)).setText(generalFunc.retrieveLangLBl("Package Details", "LBL_PACKAGE_DETAILS"));
        ((MTextView) findViewById(R.id.pickUpInsHTxt)).setText(generalFunc.retrieveLangLBl("Pickup instruction", "LBL_PICK_UP_INS"));
        ((MTextView) findViewById(R.id.deliveryInsHTxt)).setText(generalFunc.retrieveLangLBl("Delivery instruction", "LBL_DELIVERY_INS"));


    }


    public void sendMsg(String phoneNumber, boolean isdefaultMsg) {
        try {

            if (isdefaultMsg) {
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("smsto:" + Uri.encode(phoneNumber)));
                startActivity(intent);
               /* Intent smsIntent = new Intent(Intent.ACTION_VIEW);
                smsIntent.setType("vnd.android-dir/mms-sms");
                smsIntent.putExtra("address", "" + phoneNumber);
                startActivity(smsIntent);*/
            } else {

                trip_data = (HashMap<String, String>) getIntent().getSerializableExtra("data_trip");
                Bundle bnChat = new Bundle();
                bnChat.putString("iFromMemberId", trip_data.get("PassengerId"));
                bnChat.putString("FromMemberImageName", trip_data.get("PPicName"));
                bnChat.putString("iTripId", trip_data.get("iTripId"));
                bnChat.putString("FromMemberName", trip_data.get("PName"));
                bnChat.putString("vBookingNo", trip_data.get("vRideNo"));
                new StartActProcess(getActContext()).startActWithData(ChatActivity.class, bnChat);
            }

        } catch (Exception e) {
            // TODO: handle exception
        }
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


    public void getMaskNumber() {

        trip_data = (HashMap<String, String>) getIntent().getSerializableExtra("data_trip");

        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "getCallMaskNumber");
        parameters.put("iTripid", trip_data.get("iTripId"));
        parameters.put("UserType", Utils.userType);
        parameters.put("iMemberId", generalFunc.getMemberId());

        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(), parameters);
        exeWebServer.setLoaderConfig(getActContext(), true, generalFunc);

        exeWebServer.setDataResponseListener(responseString -> {
            JSONObject responseStringObj=generalFunc.getJsonObject(responseString);

            if (responseStringObj != null && !responseStringObj.equals("")) {

                boolean isDataAvail = GeneralFunctions.checkDataAvail(Utils.action_str, responseStringObj);

                if (isDataAvail == true) {
                    String message = generalFunc.getJsonValueStr(Utils.message_str, responseStringObj);
                    call(message);
                } else {
                    call(generalFunc.getJsonValue("senderMobile", data_message));

                }
            } else {
                generalFunc.showError();
            }
        });
        exeWebServer.execute();
    }

    public void getDeliveryData() {
        if (errorView.getVisibility() == View.VISIBLE) {
            errorView.setVisibility(View.GONE);
        }
        if (contentArea.getVisibility() == View.VISIBLE) {
            contentArea.setVisibility(View.GONE);
        }
        if (loading.getVisibility() != View.VISIBLE) {
            loading.setVisibility(View.VISIBLE);
        }

        final HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "loadDeliveryDetails");
        parameters.put("iDriverId", generalFunc.getMemberId());
        parameters.put("iTripId", getIntent().getStringExtra("TripId"));
        parameters.put("appType", Utils.app_type);

        final ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(), parameters);
        exeWebServer.setDataResponseListener(new ExecuteWebServerUrl.SetDataResponse() {
            @Override
            public void setResponse(String responseString) {

                if (responseString != null && !responseString.equals("")) {

                    closeLoader();

                    if (generalFunc.checkDataAvail(Utils.action_str, responseString) == true) {

                        setData(generalFunc.getJsonValue(Utils.message_str, responseString));
                    } else {
                        generateErrorView();
                    }
                } else {
                    generateErrorView();
                }
            }
        });
        exeWebServer.execute();
    }

    public void closeLoader() {
        if (loading.getVisibility() == View.VISIBLE) {
            loading.setVisibility(View.GONE);
        }
    }

    public void setData(String message) {

        this.data_message = message;

        ((MTextView) findViewById(R.id.senderNameTxt)).setText(generalFunc.getJsonValue("senderName", message));
        ((MTextView) findViewById(R.id.senderMobileTxt)).setText(generalFunc.getJsonValue("senderMobile", message));
        ((MTextView) findViewById(R.id.receiverNameTxt)).setText(generalFunc.getJsonValue("vReceiverName", message));
        ((MTextView) findViewById(R.id.receiverMobileTxt)).setText(generalFunc.getJsonValue("vReceiverMobile", message));
        ((MTextView) findViewById(R.id.packageTypeVTxt)).setText(generalFunc.getJsonValue("packageType", message));
        ((MTextView) findViewById(R.id.packageDetailsVTxt)).setText(generalFunc.getJsonValue("tPackageDetails", message));
        ((MTextView) findViewById(R.id.pickUpInsVTxt)).setText(generalFunc.getJsonValue("tPickUpIns", message));
        ((MTextView) findViewById(R.id.deliveryInsVTxt)).setText(generalFunc.getJsonValue("tDeliveryIns", message));

        String imagePath = generalFunc.getJsonValue("vImage", message);
        String vImage = Utils.checkText(imagePath) ? imagePath : "https";
        Picasso.get().load(vImage).placeholder(R.mipmap.ic_no_pic_user).error(R.mipmap.ic_no_pic_user).into(userProfileImgView);

        contentArea.setVisibility(View.VISIBLE);
    }

    public void generateErrorView() {

        closeLoader();

        generalFunc.generateErrorView(errorView, "LBL_ERROR_TXT", "LBL_NO_INTERNET_TXT");

        if (errorView.getVisibility() != View.VISIBLE) {
            errorView.setVisibility(View.VISIBLE);
        }
        errorView.setOnRetryListener(new ErrorView.RetryListener() {
            @Override
            public void onRetry() {
                getDeliveryData();
            }
        });
    }

    public void sinchCall() {


        if (MyApp.getInstance().getCurrentAct() != null && MyApp.getInstance().getCurrentAct() instanceof ViewDeliveryDetailsActivity) {

            String userProfileJsonObj = generalFunc.retrieveValue(Utils.USER_PROFILE_JSON);
            if (generalFunc.isCallPermissionGranted(false) == false) {
                generalFunc.isCallPermissionGranted(true);
            } else {
                trip_data = (HashMap<String, String>) getIntent().getSerializableExtra("data_trip");
                ViewDeliveryDetailsActivity activity = (ViewDeliveryDetailsActivity) MyApp.getInstance().getCurrentAct();
                getSinchServiceInterface().getSinchClient().setPushNotificationDisplayName(generalFunc.retrieveLangLBl("", "LBL_INCOMING_CALL"));
                HashMap<String, String> hashMap = new HashMap<>();
                hashMap.put("Id", generalFunc.getMemberId());
                hashMap.put("Name", generalFunc.getJsonValue("vName", userProfileJsonObj));
                hashMap.put("PImage", generalFunc.getJsonValue("vImage", userProfileJsonObj));
                hashMap.put("type", Utils.userType);
//                Call call = activity.getSinchServiceInterface().callUser(Utils.CALLTOPASSENGER + "_" + trip_data.get("PassengerId"), hashMap);
                Call call;
                if (Utils.checkText(trip_data.get("iGcmRegId_U"))) {
                    call = activity.getSinchServiceInterface().callUser(Utils.CALLTOPASSENGER + "_" + trip_data.get("PassengerId"), hashMap);
                }
                else {
                    call = activity.getSinchServiceInterface().callPhoneNumber(trip_data.get("vPhone_U"));
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

    public Context getActContext() {
        return ViewDeliveryDetailsActivity.this;
    }

    public class setOnClickList implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            Utils.hideKeyboard(ViewDeliveryDetailsActivity.this);
            String userprofileJson = generalFunc.retrieveValue(Utils.USER_PROFILE_JSON);
            switch (view.getId()) {
                case R.id.backImgView:
                    ViewDeliveryDetailsActivity.super.onBackPressed();
                    break;
                case R.id.callArea:
                    // call(generalFunc.getJsonValue("senderMobile", data_message));
                    //getMaskNumber();


                    if (generalFunc.getJsonValue("RIDE_DRIVER_CALLING_METHOD", userprofileJson).equals("Voip")) {
                        sinchCall();
                    } else {
                        getMaskNumber();
                    }


                    break;
                case R.id.chatArea:
                    sendMsg(generalFunc.getJsonValue("senderMobile", data_message), false);
                    break;
                case R.id.receiverCallArea:


                    if (generalFunc.getJsonValue("RIDE_DRIVER_CALLING_METHOD", userprofileJson).equals("Voip")) {
                        if (generalFunc.isCallPermissionGranted(false) == false) {
                            generalFunc.isCallPermissionGranted(true);
                        } else {
                            if (new AppFunctions(getActContext()).checkSinchInstance(getSinchServiceInterface())) {
                                getSinchServiceInterface().getSinchClient().setPushNotificationDisplayName(generalFunc.retrieveLangLBl("", "LBL_INCOMING_CALL"));

                                ViewDeliveryDetailsActivity activity = (ViewDeliveryDetailsActivity) MyApp.getInstance().getCurrentAct();
                                Call call = activity.getSinchServiceInterface().callPhoneNumber(generalFunc.getJsonValue("vReceiverMobileOriginal", data_message));
                                String callId = call.getCallId();
                                Intent callScreen = new Intent(getActContext(), CallScreenActivity.class);
                                callScreen.putExtra(SinchService.CALL_ID, callId);
                                callScreen.putExtra("vImage", "");
                                callScreen.putExtra("vName", Utils.getText(((MTextView) findViewById(R.id.receiverNameTxt))));
                                startActivity(callScreen);
                            }
                        }

                    } else {
                        call(generalFunc.getJsonValue("vReceiverMobileOriginal", data_message));
                    }
                    break;
                case R.id.receiverMsgArea:
                    sendMsg(generalFunc.getJsonValue("vReceiverMobileOriginal", data_message), true);
                    break;

            }
        }
    }
}
