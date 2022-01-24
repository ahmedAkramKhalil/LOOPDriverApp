package com.taxifgo.driver;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.os.PersistableBundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.text.Html;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.adapter.files.ViewMultiDeliveryDetailRecyclerAdapter;
import com.fragments.CustomSupportMapFragment;
import com.general.files.AppFunctions;
import com.general.files.ConfigPubNub;
import com.general.files.ExecuteWebServerUrl;
import com.general.files.GeneralFunctions;
import com.general.files.MyApp;
import com.general.files.MyScrollView;
import com.general.files.SinchService;
import com.general.files.StartActProcess;
import com.general.files.UploadProfileImage;
import com.model.Delivery_Data;
import com.model.Trip_Status;
import com.sinch.android.rtc.calling.Call;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.utils.CommonUtilities;
import com.utils.Utils;
import com.view.CreateRoundedView;
import com.view.ErrorView;
import com.view.MButton;
import com.view.MTextView;
import com.view.MaterialRippleLayout;
import com.view.SelectableRoundedImageView;
import com.view.editBox.MaterialEditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import static com.utils.Utils.generateImageParams;
/**
 * Created by Admin on 03-11-2017.
 */

public class ViewMultiDeliveryDetailsActivity extends BaseActivity implements ViewMultiDeliveryDetailRecyclerAdapter.OnItemClickList {

    private RecyclerView deliveryDetailSummuryRecyclerView;
    private LinearLayout signatureArea, mainArea, buttonArea, totalfareTitleTxtLayout;
    private LinearLayout verificationCodeArea;
    private LinearLayout mainSignCodeArea;
    private MTextView submitCodeBtn;
    private MaterialEditText verificationCodeBox;
    private MTextView submitBtn, cancelBtn, clearBtn, signatureTxt;
    private ViewMultiDeliveryDetailRecyclerAdapter deliveryDetailSummaryAdapter;

    private MTextView paymentDetailsTitleTxt, paymentTypeTitleTxt, payByTitleTxt, totalfareTitleTxt, senderDetailsTitleTxt, phoneTitleTxt;
    private MTextView titleTxt;
    private MTextView paymentTypeTxt, payByTxt, totalfareTxt;
    private MTextView senderNameValTxt, senderPhoneValTxt;
    private SelectableRoundedImageView userProfileImgView;
    private ImageView profileimageback, backImgView;

    private LinearLayout payementDetailArea, chatArea, callArea,callMsgArea;
    private RelativeLayout senderDetailArea;
    private ProgressBar loading;
    private ErrorView errorView;

    private GeneralFunctions generalFunc;
    ArrayList<Trip_Status> recipientDetailList = new ArrayList<>();
    String data_message;
    private String onGoingTripLocationId;
    String last_trip_data = "";
    String riderImage = "";
    String iUserId = "";

    String vImage = "";
    String vName = "";

    private String DELIVERY_VERIFICATION_METHOD;

    //Signature
    private boolean isSignatureView = false;

    signature mSignature;
    Bitmap bitmap;
    private LinearLayout mContent;
    private LinearLayout mView;

    private static final String IMAGE_DIRECTORY_NAME = "Temp";
    public static final int MEDIA_TYPE_IMAGE = 1;
    private Uri fileUri;
    private boolean noSign = false;
    ConfigPubNub configPubNub;
    MyScrollView scrollView;

    private CustomSupportMapFragment.OnTouchListener mListener;
    androidx.appcompat.app.AlertDialog collectPaymentFailedDialog = null;
    private Parcelable recyclerViewState;
    private boolean isIndividualFare = false;
    HashMap<String, String> data_trip;

    //    private android.support.v7.app.AlertDialog alert_showFare_detail;
    Dialog alert_showFare_detail;
    private View convertView = null;
    JSONObject last_trip_fare_data;
    String userprofileJson = "";
    String filePath = "";

    Toolbar toolbar;
    MTextView callTxt, message_text;
    private Dialog signatureImageDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multi_delivery_details);
      //  overridePendingTransition(R.anim.slide_out, R.anim.slide_in);

        generalFunc = MyApp.getInstance().getGeneralFun(getActContext());
        userprofileJson = generalFunc.retrieveValue(Utils.USER_PROFILE_JSON);

        if (getIntent().hasExtra("TRIP_DATA")) {
            HashMap<String, String> data = (HashMap<String, String>) getIntent().getSerializableExtra("TRIP_DATA");
            this.data_trip = data;
            vName = data_trip.get("PName");
        }


        init();
        setLables();
        setView();
        getTripDeliveryLocations();

        String OPEN_CHAT = generalFunc.retrieveValue("OPEN_CHAT");

        if (Utils.checkText(OPEN_CHAT)) {
            JSONObject OPEN_CHAT_DATA_OBJ = generalFunc.getJsonObject(OPEN_CHAT);
            generalFunc.removeValue("OPEN_CHAT");
            if (OPEN_CHAT_DATA_OBJ != null)
                new StartActProcess(getActContext()).startActWithData(ChatActivity.class, generalFunc.createChatBundle(OPEN_CHAT_DATA_OBJ));
        }

    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save the user's current state
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putParcelable("file_uri", fileUri);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        // get the file url
        fileUri = savedInstanceState.getParcelable("file_uri");
    }


    private String getOutputMediaFilePath() {

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

        return mediaStorageDir.getPath() + File.separator + "IMG_" + timeStamp + ".jpg";
    }


    private void setData(String sender_message, JSONObject responseString) {
        data_message = sender_message;

        senderNameValTxt.setText(generalFunc.getJsonValue("vName", sender_message));

        String vTripPaymentMode = generalFunc.getJsonValue("vTripPaymentMode", sender_message);
        String payType = Utils.checkText(vTripPaymentMode) ? vTripPaymentMode : generalFunc.getJsonValue("ePayType", sender_message);
        paymentTypeTxt.setText(payType);

//        paymentTypeTxt.setText(generalFunc.getJsonValue("vTripPaymentMode", sender_message));
        payByTxt.setText("" + generalFunc.getJsonValue("PaymentPerson", responseString));
        totalfareTxt.setText("" + generalFunc.getJsonValue("DriverPaymentAmount", responseString));

        if (generalFunc.isJSONkeyAvail("FareDetailsNewArr", last_trip_fare_data.toString())) {
            // findViewById(R.id.fareDetailArea).setVisibility(View.VISIBLE);
            // findViewById(R.id.fareDetailArea).setOnClickListener(new setOnClickList());
            totalfareTitleTxtLayout.setOnClickListener(new setOnClickList());
        }

        String ePaymentBy = generalFunc.getJsonValueStr("ePaymentBy", responseString);
        if (ePaymentBy.equalsIgnoreCase("Individual")) {
            isIndividualFare = true;
            ((MTextView) findViewById(R.id.indifareTxt)).setText("" + generalFunc.getJsonValue("Fare_Payable", responseString));
            ((MTextView) findViewById(R.id.indifareTitleTxt)).setText("" + generalFunc.retrieveLangLBl("Payable amount", "LBL_MULTI_PAYBALE_AMOUNT") + ":");
            ((LinearLayout) findViewById(R.id.totalFareArea)).setBackgroundColor(getActContext().getResources().getColor(R.color.appThemeColor_bg_parent_1));
        }

        totalfareTxt.setText("" + generalFunc.getJsonValue("DriverPaymentAmount", responseString));


        senderPhoneValTxt.setText("+" + generalFunc.getJsonValue("vCode", data_message) + " " + generalFunc.getJsonValue("vMobile", sender_message));
        riderImage = generalFunc.getJsonValue("vImage", sender_message);
        iUserId = generalFunc.getJsonValue("iUserId", sender_message);
        vName = generalFunc.getJsonValue("vName", sender_message);
        String image_url = CommonUtilities.SERVER_URL_PHOTOS + "upload/Passenger/" + generalFunc.getJsonValue("iUserId", sender_message) + "/"
                + riderImage;


        vImage = image_url;


        Target target = new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                if (profileimageback != null) {
                    Utils.setBlurImage(bitmap, profileimageback);
                }

                userProfileImgView.setImageBitmap(bitmap);
            }

            @Override
            public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                userProfileImgView.setImageResource(R.mipmap.ic_no_pic_user);
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {
                userProfileImgView.setImageResource(R.mipmap.ic_no_pic_user);

            }
        };

        Picasso.get()
                .load(image_url)
                .placeholder(R.mipmap.ic_no_pic_user)
                .error(R.mipmap.ic_no_pic_user)
                .into(target);


    }


    private void setLables() {
        titleTxt.setText(generalFunc.retrieveLangLBl("Delivery Details", "LBL_DELIVERY_DETAILS"));
        paymentDetailsTitleTxt.setText(generalFunc.retrieveLangLBl("PAYMENT DETAIL", "LBL_PAYMENT_HEADER_TXT"));
        paymentTypeTitleTxt.setText(generalFunc.retrieveLangLBl("Payment Type", "LBL_PAYMENT_TYPE_TXT") + ":");
        payByTitleTxt.setText(generalFunc.retrieveLangLBl("Pay By", "LBL_MULTI_PAY_BY_TXT") + ":");
        totalfareTitleTxt.setText(generalFunc.retrieveLangLBl("Total Fare", "LBL_TOTAL_TXT") + ":");
        senderDetailsTitleTxt.setText(generalFunc.retrieveLangLBl("Sender Details", "LBL_MULTI_SENDER_DETAILS_TXT"));
        phoneTitleTxt.setText(generalFunc.retrieveLangLBl("", "LBL_PHONE"));
        callTxt.setText(generalFunc.retrieveLangLBl("", "LBL_CALL_TXT"));
        message_text.setText(generalFunc.retrieveLangLBl("", "LBL_MESSAGE_TXT"));

        clearBtn.setText(generalFunc.retrieveLangLBl("Clear", "LBL_MULTI_CLEAR_TXT"));
        submitBtn.setText(generalFunc.retrieveLangLBl("Submit", "LBL_BTN_SUBMIT_TXT"));
        cancelBtn.setText(generalFunc.retrieveLangLBl("Cancel", "LBL_BTN_CANCEL_TXT"));
        signatureTxt.setText(generalFunc.retrieveLangLBl("Signature", "LBL_SIGN_ABOVE"));

        String contentMsg = generalFunc.retrieveLangLBl("Please enter the confirmation code.", "LBL_MULTI_VERIFICATION_CODE_MSG_TXT");
        ((MTextView) (findViewById(R.id.contentMsgTxt))).setText(contentMsg);
        verificationCodeBox.setBothText(generalFunc.retrieveLangLBl("", "LBL_CONFIRMATION_CODE"), generalFunc.retrieveLangLBl("", "LBL_CONFIRMATION_CODE"));
        verificationCodeBox.setImeOptions(EditorInfo.IME_ACTION_DONE);
    }

    public Context getActContext() {
        return ViewMultiDeliveryDetailsActivity.this;
    }

    private void init() {

        toolbar = findViewById(R.id.toolbar);
        toolbar.setBackgroundColor(getResources().getColor(R.color.mdtp_transparent_full));
        scrollView = (MyScrollView) findViewById(R.id.mainScroll);
        deliveryDetailSummuryRecyclerView = (RecyclerView) findViewById(R.id.deliveryDetailSummuryRecyclerView);
        payementDetailArea = (LinearLayout) findViewById(R.id.payementDetailArea);

        verificationCodeArea = (LinearLayout) findViewById(R.id.verificationCodeArea);
        mainSignCodeArea = (LinearLayout) findViewById(R.id.mainSignCodeArea);
        submitCodeBtn = (MTextView) findViewById(R.id.submitCodeBtn);
        verificationCodeBox = (MaterialEditText) findViewById(R.id.editBox);

        signatureArea = (LinearLayout) findViewById(R.id.signatureArea);
        totalfareTitleTxtLayout = (LinearLayout) findViewById(R.id.totalfareTitleTxtLayout);
        buttonArea = (LinearLayout) findViewById(R.id.buttonArea);
        mainArea = (LinearLayout) findViewById(R.id.mainArea);
        submitBtn = (MTextView) findViewById(R.id.submitBtn);
        cancelBtn = (MTextView) findViewById(R.id.cancelBtn);
        clearBtn = (MTextView) findViewById(R.id.clearBtn);
        signatureTxt = (MTextView) findViewById(R.id.signatureTxt);
        callTxt = (MTextView) findViewById(R.id.callTxt);
        message_text = (MTextView) findViewById(R.id.message_text);

        senderDetailArea = (RelativeLayout) findViewById(R.id.senderDetailArea);
        chatArea = (LinearLayout) findViewById(R.id.chatArea);
        callArea = (LinearLayout) findViewById(R.id.callArea);
        callMsgArea = (LinearLayout) findViewById(R.id.callMsgArea);
        paymentDetailsTitleTxt = (MTextView) findViewById(R.id.paymentDetailsTitleTxt);
        paymentTypeTitleTxt = (MTextView) findViewById(R.id.paymentTypeTitleTxt);
        paymentTypeTxt = (MTextView) findViewById(R.id.paymentTypeTxt);
        payByTitleTxt = (MTextView) findViewById(R.id.payByTitleTxt);
        payByTxt = (MTextView) findViewById(R.id.payByTxt);
        totalfareTitleTxt = (MTextView) findViewById(R.id.totalfareTitleTxt);
        totalfareTxt = (MTextView) findViewById(R.id.totalfareTxt);
        senderDetailsTitleTxt = (MTextView) findViewById(R.id.senderDetailsTitleTxt);
        senderNameValTxt = (MTextView) findViewById(R.id.senderNameValTxt);
        phoneTitleTxt = (MTextView) findViewById(R.id.phoneTitleTxt);
        senderPhoneValTxt = (MTextView) findViewById(R.id.senderPhoneValTxt);
        profileimageback = (ImageView) findViewById(R.id.profileimageback);
        userProfileImgView = (SelectableRoundedImageView) findViewById(R.id.userProfileImgView);
        loading = (ProgressBar) findViewById(R.id.loading);
        errorView = (ErrorView) findViewById(R.id.errorView);
        titleTxt = (MTextView) findViewById(R.id.titleTxt);
        backImgView = (ImageView) findViewById(R.id.backImgView);

        if (getIntent().hasExtra("Status") && getIntent().getStringExtra("Status").equalsIgnoreCase("cabRequestScreen")) {
            senderDetailArea.setVisibility(View.GONE);
            findViewById(R.id.DetailsContainer).setVisibility(View.GONE);
        } else if (getIntent().hasExtra("Status") && getIntent().getStringExtra("Status").equalsIgnoreCase("showHistoryScreen")) {
            senderDetailArea.setVisibility(View.VISIBLE);
            findViewById(R.id.DetailsContainer).setVisibility(View.GONE);
            callMsgArea.setVisibility(View.GONE);
        } else {
            senderDetailArea.setVisibility(View.VISIBLE);
        }

        chatArea.setOnClickListener(new setOnClickList());
        callArea.setOnClickListener(new setOnClickList());

        if (getIntent().hasExtra("CheckFor")) {
            backImgView.setVisibility(View.GONE);
        }


        backImgView.setOnClickListener(new setOnClickList());


        cancelBtn.setOnClickListener(new setOnClickList());
        clearBtn.setOnClickListener(new setOnClickList());
        submitBtn.setOnClickListener(new setOnClickList());

        verificationCodeBox.setOnClickListener(new setOnClickList());
        submitCodeBtn.setOnClickListener(new setOnClickList());

        // make rounded corner

        int backColor = getActContext().getResources().getColor(R.color.appThemeColor_1);
        int backColor2 = getActContext().getResources().getColor(R.color.mdtp_transparent_full);
        int strokeColor2 = getActContext().getResources().getColor(R.color.white);
        int cornorRadius = Utils.dipToPixels(getActContext(), 5);
        int strokeWidth = Utils.dipToPixels(getActContext(), 1);
        int strokeColor = backColor;

        //  new CreateRoundedView(backColor, cornorRadius, strokeWidth, strokeColor, submitBtn);
        new CreateRoundedView(backColor, cornorRadius, strokeWidth, strokeColor, submitCodeBtn);
        // new CreateRoundedView(backColor, cornorRadius, strokeWidth, strokeColor, clearBtn);
        new CreateRoundedView(backColor2, cornorRadius, strokeWidth, strokeColor2, chatArea);
        new CreateRoundedView(strokeColor2, cornorRadius, strokeWidth, strokeColor2, callArea);

    }


    public void getTripDeliveryLocations() {
        if (errorView.getVisibility() == View.VISIBLE) {
            errorView.setVisibility(View.GONE);
        }
        if (deliveryDetailSummuryRecyclerView.getVisibility() == View.VISIBLE) {
            deliveryDetailSummuryRecyclerView.setVisibility(View.GONE);
        }
        if (loading.getVisibility() != View.VISIBLE) {
            loading.setVisibility(View.VISIBLE);
        }
        recipientDetailList.clear();
        deliveryDetailSummaryAdapter.notifyDataSetChanged();

        final HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "getTripDeliveryDetails");
        parameters.put("iCabBookingId", "");

        String iCabBookingId = getIntent().hasExtra("iCabBookingId") ? getIntent().getStringExtra("iCabBookingId") : "";
        if (Utils.checkText(iCabBookingId)) {
            parameters.put("iCabBookingId", iCabBookingId);
        }

        String iCabRequestId = getIntent().hasExtra("iCabRequestId") ? getIntent().getStringExtra("iCabRequestId") : "";
        if (Utils.checkText(iCabRequestId)) {
            parameters.put("iCabRequestId", iCabRequestId);
        }
        parameters.put("iTripId", getIntent().getStringExtra("TripId"));
        parameters.put("userType", Utils.userType);
        parameters.put("iDriverId", generalFunc.getMemberId());

        final ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(), parameters);
        exeWebServer.setDataResponseListener(responseString -> {
            JSONObject responseStringObj = generalFunc.getJsonObject(responseString);

            if (responseStringObj != null && !responseStringObj.equals("")) {

                closeLoader();
                String msg_str = generalFunc.getJsonValueStr(Utils.message_str, responseStringObj);

                if (generalFunc.checkDataAvail(Utils.action_str, responseStringObj)) {

                    if (Utils.checkText(msg_str)) {
                        JSONObject jobject = generalFunc.getJsonObject("MemberDetails", msg_str);
                        DELIVERY_VERIFICATION_METHOD = generalFunc.getJsonValueStr("DELIVERY_VERIFICATION_METHOD", responseStringObj);
                        last_trip_data = generalFunc.getJsonValueStr("TripDetails", responseStringObj);
                        last_trip_fare_data = responseStringObj;

                        if (jobject != null) {
                            setData(jobject.toString(), responseStringObj);
                        }


                        JSONArray deliveries = generalFunc.getJsonArray("Deliveries", msg_str);
                        if (deliveries != null) {

                            String LBL_RECIPIENT = "", LBL_Status = "", LBL_CANCELLED="",LBL_CANCELED_TRIP_TXT = "", LBL_FINISHED_TXT = "", LBL_MULTI_AMOUNT_COLLECT_TXT = "", LBL_PICK_UP_INS = "", LBL_DELIVERY_INS = "", LBL_PACKAGE_DETAILS = "", LBL_CALL_TXT = "", LBL_VIEW_SIGN_TXT = "", LBL_MESSAGE_ACTIVE_TRIP = "", LBL_MULTI_RESPONSIBLE_FOR_PAYMENT_TXT = "";

                            if (deliveries.length() > 0) {
                                LBL_RECIPIENT = generalFunc.retrieveLangLBl("", "LBL_RECIPIENT");
                                LBL_Status = generalFunc.retrieveLangLBl("", "LBL_Status");
//                                LBL_CANCELED_TRIP_TXT = generalFunc.retrieveLangLBl("", "LBL_CANCELED_TRIP_TXT");
                                LBL_CANCELLED = generalFunc.retrieveLangLBl("", "LBL_CANCELLED");
                                LBL_FINISHED_TXT = generalFunc.retrieveLangLBl("", "LBL_FINISHED_TXT");
                                LBL_MULTI_AMOUNT_COLLECT_TXT = generalFunc.retrieveLangLBl("", "LBL_MULTI_AMOUNT_COLLECT_TXT");
                                LBL_PICK_UP_INS = generalFunc.retrieveLangLBl("", "LBL_PICK_UP_INS");
                                LBL_DELIVERY_INS = generalFunc.retrieveLangLBl("", "LBL_DELIVERY_INS");
                                LBL_PACKAGE_DETAILS = generalFunc.retrieveLangLBl("", "LBL_PACKAGE_DETAILS");
                                LBL_CALL_TXT = generalFunc.retrieveLangLBl("", "LBL_CALL_TXT");
                                LBL_VIEW_SIGN_TXT = generalFunc.retrieveLangLBl("", "LBL_VIEW_SIGN_TXT");
                                LBL_MESSAGE_ACTIVE_TRIP = generalFunc.retrieveLangLBl("", "LBL_MESSAGE_ACTIVE_TRIP");
                                LBL_MULTI_RESPONSIBLE_FOR_PAYMENT_TXT = generalFunc.retrieveLangLBl("Responsible for payment", "LBL_MULTI_RESPONSIBLE_FOR_PAYMENT_TXT");
                            }


                            for (int i = 0; i < deliveries.length(); i++) {
                                Trip_Status recipientDetailMap1 = new Trip_Status();
                                recipientDetailMap1.setePaymentBy(generalFunc.getJsonValueStr("ePaymentBy", responseStringObj));
                                recipientDetailMap1.setFare_Payable(generalFunc.getJsonValueStr("Fare_Payable", responseStringObj));

                                JSONArray deliveriesArray = generalFunc.getJsonArray(deliveries, i);

                                if (deliveriesArray != null && deliveriesArray.length() > 0) {


                                    ArrayList<Delivery_Data> subrecipientDetailList = new ArrayList<>();

                                    for (int j = 0; j < deliveriesArray.length(); j++) {

                                        JSONObject jobject1 = generalFunc.getJsonObject(deliveriesArray, j);
                                        Delivery_Data recipientDetailMap = new Delivery_Data();

                                        String vValue = generalFunc.getJsonValueStr("vValue", jobject1);
                                        String vFieldName = generalFunc.getJsonValueStr("vFieldName", jobject1);


                                        recipientDetailMap.setvValue(vValue);

                                        if (vFieldName.equalsIgnoreCase("Recepient Name") || (generalFunc.getJsonValueStr("iDeliveryFieldId", jobject1).equalsIgnoreCase("2"))) {
                                            recipientDetailMap1.setRecepientName(vValue);
                                        } else if (vFieldName.equalsIgnoreCase("Mobile Number") || (generalFunc.getJsonValueStr("iDeliveryFieldId", jobject1).equalsIgnoreCase("3"))) {
                                            recipientDetailMap1.setRecepientNum(vValue);
                                            recipientDetailMap1.setRecepientMaskNum(generalFunc.getJsonValueStr("vMaskValue", jobject1));
                                        } else if (vFieldName.equalsIgnoreCase("Address")) {
                                            recipientDetailMap1.setePaymentByReceiver(generalFunc.getJsonValueStr("ePaymentByReceiver", jobject1));
                                            recipientDetailMap1.setRecepientAddress(AppFunctions.fromHtml(generalFunc.getJsonValue("tDaddress", jobject1.toString())).toString());
                                            recipientDetailMap.setiTripDeliveryLocationId(AppFunctions.fromHtml(generalFunc.getJsonValue("iTripDeliveryLocationId", jobject1.toString())).toString());

                                            recipientDetailMap.setvValue(generalFunc.getJsonValueStr("tDaddress", jobject1));
                                            recipientDetailMap1.setReceipent_Signature(generalFunc.getJsonValueStr("Receipent_Signature", jobject1));

                                            recipientDetailMap1.setiTripDeliveryLocationId(generalFunc.getJsonValueStr("iTripDeliveryLocationId", jobject1));

                                            recipientDetailMap1.setiActive(generalFunc.getJsonValueStr("iActive", jobject1));
                                        }

                                        recipientDetailMap.setvFieldName(vFieldName);

                                        recipientDetailMap.setiDeliveryFieldId(generalFunc.getJsonValueStr("iDeliveryFieldId", jobject1));

                                        recipientDetailMap.settSaddress(generalFunc.getJsonValueStr("tSaddress", jobject1));

                                        recipientDetailMap.settStartLat(GeneralFunctions.parseDoubleValue(0.0, generalFunc.getJsonValueStr("tStartLat", jobject1)));

                                        recipientDetailMap.settStartLong(GeneralFunctions.parseDoubleValue(0.0, generalFunc.getJsonValueStr("tStartLong", jobject1)));


                                        recipientDetailMap.settDaddress(generalFunc.getJsonValueStr("tDaddress", jobject1));


                                        recipientDetailMap.settDestLat(GeneralFunctions.parseDoubleValue(0.0, generalFunc.getJsonValueStr("tEndLat", jobject1)));

                                        recipientDetailMap.settDestLong(GeneralFunctions.parseDoubleValue(0.0, generalFunc.getJsonValueStr("tEndLong", jobject1)));

                                        recipientDetailMap.setePaymentByReceiver(generalFunc.getJsonValueStr("ePaymentByReceiver", jobject1));
                                        recipientDetailMap.setShowDetails(false);
                                        if (!vFieldName.equalsIgnoreCase("Address") && (!vFieldName.equalsIgnoreCase("Mobile Number") && !(generalFunc.getJsonValueStr("iDeliveryFieldId", jobject1).equalsIgnoreCase("3"))) && (!vFieldName.equalsIgnoreCase("Recepient Name") && !(generalFunc.getJsonValueStr("iDeliveryFieldId", jobject1).equalsIgnoreCase("2"))) /*&& Utils.checkText(generalFunc.getJsonValue("vValue", jobject1))*/) {

                                            recipientDetailMap.setShowDetails(true);
                                            subrecipientDetailList.add(recipientDetailMap);

                                        }
                                    }

                                    String status = getIntent().hasExtra("Status") ? getIntent().getStringExtra("Status") : "";
                                    if (status.equalsIgnoreCase("activeTrip")) {
                                        recipientDetailMap1.setShowUpcomingLocArea("Yes");
                                    } else {
                                        recipientDetailMap1.setShowUpcomingLocArea("No");
                                    }
                                    if (status.equalsIgnoreCase("cabRequestScreen")) {
                                        recipientDetailMap1.setShowMobile("No");
                                    } else {
                                        recipientDetailMap1.setShowMobile("Yes");
                                    }

                                    recipientDetailMap1.setLBL_RECIPIENT(LBL_RECIPIENT);
                                    recipientDetailMap1.setLBL_MULTI_AMOUNT_COLLECT_TXT(LBL_MULTI_AMOUNT_COLLECT_TXT);
                                    recipientDetailMap1.setLBL_Status(LBL_Status);
//                                    recipientDetailMap1.setLBL_CANCELED_TRIP_TXT(LBL_CANCELED_TRIP_TXT);
                                    recipientDetailMap1.setLBL_CANCELED_TRIP_TXT(LBL_CANCELLED);
                                    recipientDetailMap1.setLBL_FINISHED_TRIP_TXT(LBL_FINISHED_TXT);

                                    recipientDetailMap1.setLBL_PACKAGE_DETAILS(LBL_PICK_UP_INS);
                                    recipientDetailMap1.setLBL_DELIVERY_INS(LBL_DELIVERY_INS);
                                    recipientDetailMap1.setLBL_PACKAGE_DETAILS(LBL_PACKAGE_DETAILS);
                                    recipientDetailMap1.setLBL_CALL_TXT(LBL_CALL_TXT);
                                    recipientDetailMap1.setLBL_MESSAGE_ACTIVE_TRIP(LBL_MESSAGE_ACTIVE_TRIP);

                                    recipientDetailMap1.setLBL_RESPONSIBLE_FOR_PAYMENT_TXT(LBL_MULTI_RESPONSIBLE_FOR_PAYMENT_TXT);
                                    recipientDetailMap1.setLBL_VIEW_SIGN_TXT(LBL_VIEW_SIGN_TXT);

                                    recipientDetailMap1.setListOfDeliveryItems(subrecipientDetailList);
                                    recipientDetailList.add(recipientDetailMap1);
                                }
                            }

                        }

                    }

                    if (getIntent().hasExtra("CheckFor") && getIntent().getStringExtra("CheckFor").equals("Sender")) {
                        enableSignatureView();
                    } else if ((getIntent().hasExtra("CheckFor") && DELIVERY_VERIFICATION_METHOD.equalsIgnoreCase("Signature"))) {
                        enableSignatureView();

                    } else if (getIntent().hasExtra("CheckFor") && !DELIVERY_VERIFICATION_METHOD.equalsIgnoreCase("Signature")) {
                        enableConfirmationView();

                    } else {
                        resetView();
                    }

                    deliveryDetailSummaryAdapter.notifyDataSetChanged();


                } else {
                    generalFunc.showGeneralMessage(generalFunc.retrieveLangLBl("Error", "LBL_ERROR_TXT"),
                            generalFunc.retrieveLangLBl("", msg_str));
                    deliveryDetailSummaryAdapter.notifyDataSetChanged();

                }
            } else {
                generateErrorView();
                deliveryDetailSummaryAdapter.notifyDataSetChanged();
            }
        });
        exeWebServer.execute();
    }

    private void resetView() {
        isSignatureView = false;
        signatureArea.setVisibility(View.GONE);
        buttonArea.setVisibility(View.GONE);
        verificationCodeArea.setVisibility(View.GONE);
        mainSignCodeArea.setVisibility(View.GONE);
        deliveryDetailSummuryRecyclerView.setVisibility(View.VISIBLE);
        findViewById(R.id.indiFareArea).setVisibility(View.GONE);
        setView();
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);

        recyclerViewState = deliveryDetailSummuryRecyclerView.getLayoutManager().onSaveInstanceState();


    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onRestoreInstanceState(savedInstanceState, persistentState);
        deliveryDetailSummuryRecyclerView.getLayoutManager().onRestoreInstanceState(recyclerViewState);

    }


    private void enableConfirmationView() {
        isSignatureView = false;
        signatureArea.setVisibility(View.GONE);
        buttonArea.setVisibility(View.GONE);
        verificationCodeArea.setVisibility(View.VISIBLE);
        mainSignCodeArea.setVisibility(View.VISIBLE);
        deliveryDetailSummuryRecyclerView.setVisibility(View.GONE);


        if (isIndividualFare) {
            findViewById(R.id.indiFareArea).setVisibility(View.VISIBLE);
        }

        titleTxt.setText(generalFunc.retrieveLangLBl("Booking Summary", "LBL_VERIFICATION_PAGE_HEADER"));

        String contentMsg = generalFunc.retrieveLangLBl("Please enter the confirmation code received from recipient.", "LBL_DELIVERY_END_NOTE");
    }

    private void enableSignatureView() {
        isSignatureView = true;
        signatureArea.setVisibility(View.VISIBLE);
        buttonArea.setVisibility(View.VISIBLE);
        verificationCodeArea.setVisibility(View.GONE);
        mainSignCodeArea.setVisibility(View.VISIBLE);

        if (isIndividualFare) {
            findViewById(R.id.indiFareArea).setVisibility(View.VISIBLE);
        }

        deliveryDetailSummuryRecyclerView.setVisibility(View.GONE);
        assignSignatureView();
        titleTxt.setText(generalFunc.retrieveLangLBl("Booking Summary", "LBL_VERIFICATION_PAGE_HEADER"));

        //  scrollView.setScrolling(true);


        mainArea.setOnTouchListener((view, event) -> {
            scrollView.setScrolling(true);
            return false;
        });


    }


    private void assignSignatureView() {
        mContent = (LinearLayout) findViewById(R.id.linearLayout);
        mSignature = new signature(getApplicationContext(), null);
        mSignature.setBackgroundColor(Color.WHITE);
        // Dynamically generating Layout through java code
        mContent.addView(mSignature, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        mView = mContent;
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
                getTripDeliveryLocations();
            }
        });
    }


    public void setView() {
        mainSignCodeArea.setVisibility(View.GONE);
        deliveryDetailSummuryRecyclerView.setVisibility(View.VISIBLE);
        deliveryDetailSummaryAdapter = new ViewMultiDeliveryDetailRecyclerAdapter(getActContext(), ViewMultiDeliveryDetailsActivity.this, recipientDetailList, generalFunc);
        deliveryDetailSummaryAdapter.isFromHistory(getIntent().hasExtra("Status") && getIntent().getStringExtra("Status").equalsIgnoreCase("showHistoryScreen"));
        deliveryDetailSummuryRecyclerView.setItemAnimator(new DefaultItemAnimator());
        deliveryDetailSummuryRecyclerView.setAdapter(deliveryDetailSummaryAdapter);
        deliveryDetailSummaryAdapter.notifyDataSetChanged();
        deliveryDetailSummaryAdapter.setOnItemClickList(this);
    }

    public void closeLoader() {
        if (loading.getVisibility() == View.VISIBLE) {
            loading.setVisibility(View.GONE);
        }
    }

    @Override
    public void onBackPressed() {

        if (backImgView.getVisibility() == View.VISIBLE) {
            super.onBackPressed();

//        overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
//            overridePendingTransition(R.anim.slide_in, R.anim.slide_out);


        }
    }

    @Override
    public void onItemClick(String data, String type, int position) {

        if (type.equalsIgnoreCase("call")) {

            if (generalFunc.getJsonValue("RIDE_DRIVER_CALLING_METHOD", userprofileJson).equals("Voip")) {

                if (!generalFunc.isCallPermissionGranted(false)) {
                    generalFunc.isCallPermissionGranted(true);
                } else {
                    ViewMultiDeliveryDetailsActivity activity = (ViewMultiDeliveryDetailsActivity) MyApp.getInstance().getCurrentAct();
                    activity.getSinchServiceInterface().getSinchClient().setPushNotificationDisplayName(generalFunc.retrieveLangLBl("", "LBL_INCOMING_CALL"));
                    Call call = activity.getSinchServiceInterface().callPhoneNumber(data);

                    String callId = call.getCallId();
                    Intent callScreen = new Intent(getActContext(), CallScreenActivity.class);
                    callScreen.putExtra(SinchService.CALL_ID, callId);
                    callScreen.putExtra("vImage", "");
                    callScreen.putExtra("vName", recipientDetailList.get(position).getRecepientName());
                    startActivity(callScreen);
                }

            } else {
                call(data, "Receiver");
            }
        } else if (type.equalsIgnoreCase("msg")) {
            sendMsg(data, "Receiver");
        }

    }

    @Override
    public void onItemClick(String type, int position) {
        showSignatureImage(generalFunc.retrieveLangLBl("", "LBL_RECIPIENT_NAME_HEADER_TXT") + " : " + recipientDetailList.get(position).getRecepientName(), recipientDetailList.get(position).getReceipent_Signature(), false);
    }

    public void showSignatureImage(String Name, String image_url, boolean isSender) {
        signatureImageDialog = new Dialog(getActContext(), R.style.Theme_Dialog);
        signatureImageDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        signatureImageDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        signatureImageDialog.setContentView(R.layout.multi_show_sign_design);

        final ProgressBar LoadingProgressBar = ((ProgressBar) signatureImageDialog.findViewById(R.id.LoadingProgressBar));

        ((MTextView) signatureImageDialog.findViewById(R.id.nameTxt)).setText(" " + Name);

        if (isSender) {
            ((MTextView) signatureImageDialog.findViewById(R.id.passengerDTxt)).setText(generalFunc.retrieveLangLBl("Sender Signature", "LBL_SENDER_SIGN"));
            ((MTextView) signatureImageDialog.findViewById(R.id.nameTxt)).setVisibility(View.GONE);

        } else {
            ((MTextView) signatureImageDialog.findViewById(R.id.passengerDTxt)).setText(generalFunc.retrieveLangLBl("Receiver Signature", "LBL_RECEIVER_SIGN"));
            ((MTextView) signatureImageDialog.findViewById(R.id.nameTxt)).setVisibility(View.VISIBLE);

        }

        if (Utils.checkText(image_url)) {

            Picasso.get()
                    .load(image_url)
                    .into(((ImageView) signatureImageDialog.findViewById(R.id.passengerImgView)), new com.squareup.picasso.Callback() {
                        @Override
                        public void onSuccess() {
                            LoadingProgressBar.setVisibility(View.GONE);
                            ((ImageView) signatureImageDialog.findViewById(R.id.passengerImgView)).setVisibility(View.VISIBLE);
                        }

                        @Override
                        public void onError(Exception e) {
                            LoadingProgressBar.setVisibility(View.VISIBLE);
                            ((ImageView) signatureImageDialog.findViewById(R.id.passengerImgView)).setVisibility(View.GONE);

                        }
                    });
        } else {
            LoadingProgressBar.setVisibility(View.VISIBLE);
            ((ImageView) signatureImageDialog.findViewById(R.id.passengerImgView)).setVisibility(View.GONE);

        }
        (signatureImageDialog.findViewById(R.id.cancelArea)).setOnClickListener(view -> {

            if (signatureImageDialog != null) {
                signatureImageDialog.dismiss();
            }
        });

        signatureImageDialog.setCancelable(false);
        signatureImageDialog.setCanceledOnTouchOutside(false);

        if (generalFunc.isRTLmode()) {
            generalFunc.forceRTLIfSupported(signatureImageDialog);
        }
        signatureImageDialog.show();

    }

    public class setOnClickList implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.backImgView:
                    ViewMultiDeliveryDetailsActivity.super.onBackPressed();
                    break;

                case R.id.callArea:
                    String userprofileJson = generalFunc.retrieveValue(Utils.USER_PROFILE_JSON);
                    if (generalFunc.getJsonValue("RIDE_DRIVER_CALLING_METHOD", userprofileJson).equals("Voip")) {
                        sinchCall();
                    } else {
                        getMaskNumber();
                    }

                    break;
                case R.id.chatArea:
                    sendMsg((Utils.getText(senderPhoneValTxt).toString().replaceAll("\\s+", "")), "sender");
                    break;
                case R.id.clearBtn:
                    mSignature.clear();
                    filePath = "";
                    //assignSignatureView();
                    break;
                case R.id.submitBtn:

                    if (generalFunc.isStoragePermissionGranted() && noSign) {
                        mView.setDrawingCacheEnabled(true);
                        mSignature.save(mView);
                    }
                    //  Toast.makeText(getApplicationContext(), "Successfully Saved", Toast.LENGTH_SHORT).show();
                    // Calling the same class
                    break;
                case R.id.cancelArea:
                    Log.v("log_tag", "Panel Canceled");
                    // Calling the same class
                    recreate();
                    break;

              /*  case R.id.fareDetailArea:
                    if (alert_showFare_detail != null) {
                        showFareDetails();
                    } else {
                        loadFareDetails();
                    }
                    break;*/
                case R.id.totalfareTitleTxtLayout:
                    if (alert_showFare_detail != null) {
                        showFareDetails();
                    } else {
                        loadFareDetails();
                    }
                    break;

                case R.id.submitCodeBtn:

                    if (Utils.checkText(verificationCodeBox) == false) {
                        verificationCodeBox.setError(generalFunc.retrieveLangLBl("", "LBL_FEILD_REQUIRD"));
                        return;
                    }

                    if (data_trip != null && Utils.checkText(data_trip.get("vDeliveryConfirmCode")) && !Utils.getText(verificationCodeBox).equals(data_trip.get("vDeliveryConfirmCode"))) {

                        verificationCodeBox.setError(generalFunc.retrieveLangLBl("Invalid code", "LBL_INVALID_DELIVERY_CONFIRM_CODE"));
                        return;

                    }

                    confirmDeliveryStatus();

                    break;

            }
        }
    }

    public void showFareDetails() {
        if (alert_showFare_detail != null) {
            alert_showFare_detail.show();
        }
    }

    public void loadFareDetails() {
//        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(getActContext());
//        LayoutInflater inflater = (LayoutInflater) getActContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//        View dialogView = inflater.inflate(R.layout.multi_design_fare_detail_cell, null);

        alert_showFare_detail = new Dialog(getActContext(), R.style.Theme_Dialog1);
        alert_showFare_detail.requestWindowFeature(Window.FEATURE_NO_TITLE);


        //  v.setBackgroundResource(android.R.color.transparent);
        alert_showFare_detail.getWindow().setBackgroundDrawable(getResources().getDrawable(R.drawable.all_roundcurve_card));

        alert_showFare_detail.setContentView(R.layout.multi_design_fare_detail_cell);


//        builder.setView(dialogView);

        final MTextView cartypeTxt = (MTextView) alert_showFare_detail.findViewById(R.id.cartypeTxt);
        final MTextView titleTxt = (MTextView) alert_showFare_detail.findViewById(R.id.titleTxt);
        final LinearLayout fareDetailDisplayArea = (LinearLayout) alert_showFare_detail.findViewById(R.id.fareDetailDisplayArea);
        final MButton btn_type2 = ((MaterialRippleLayout) alert_showFare_detail.findViewById(R.id.btn_type2)).getChildView();

        addFareDetailLayout(cartypeTxt, fareDetailDisplayArea);

        btn_type2.setText(generalFunc.retrieveLangLBl("", "LBL_OK"));
        titleTxt.setText(generalFunc.retrieveLangLBl("Fare Details", "LBL_FARE_DETAILS"));

        btn_type2.setOnClickListener(view -> alert_showFare_detail.dismiss());
        alert_showFare_detail.setCanceledOnTouchOutside(false);
        alert_showFare_detail.setCancelable(false);
        if (generalFunc.isRTLmode() == true) {
            generalFunc.forceRTLIfSupported(alert_showFare_detail);
        }

        if (alert_showFare_detail != null) {

            alert_showFare_detail.show();
        }
    }

    private void addFareDetailLayout(MTextView cartypeTxt, LinearLayout fareDetailDisplayArea) {

        if (fareDetailDisplayArea.getChildCount() > 0) {
            fareDetailDisplayArea.removeAllViewsInLayout();
        }


        cartypeTxt.setText(generalFunc.getJsonValueStr("carTypeName", last_trip_fare_data));

        boolean FareDetailsArrNew = generalFunc.isJSONkeyAvail("FareDetailsNewArr", last_trip_fare_data != null ? last_trip_fare_data.toString() : "");

        JSONArray FareDetailsArrNewObj = null;
        if (FareDetailsArrNew == true) {
            FareDetailsArrNewObj = generalFunc.getJsonArray("FareDetailsNewArr", last_trip_fare_data != null ? last_trip_fare_data.toString() : "");
        }

        for (int i = 0; i < FareDetailsArrNewObj.length(); i++) {
            JSONObject jobject = generalFunc.getJsonObject(FareDetailsArrNewObj, i);
            try {

                String data = jobject.names().getString(0);

                addFareDetailRow(fareDetailDisplayArea, data, jobject.get(data).toString(), (FareDetailsArrNewObj.length() - 1) == i ? true : false);

                //addFareDetailRow(fareDetailDisplayArea,jobject.names().getString(0), jobject.get(jobject.names().getString(0)).toString(), FareDetailsArrNewObj.length() - 1 == i ? true : false);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }

    private void addFareDetailRow(LinearLayout fareDetailDisplayArea, String row_name, String row_value, boolean isLast) {
        View convertView = null;
        if (row_name.equalsIgnoreCase("eDisplaySeperator")) {
            convertView = new View(getActContext());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Utils.dipToPixels(getActContext(), 1));
            params.setMarginStart(Utils.dipToPixels(getActContext(), 10));
            params.setMarginEnd(Utils.dipToPixels(getActContext(), 10));
            convertView.setBackgroundColor(Color.parseColor("#dedede"));
            convertView.setLayoutParams(params);
        } else {
            LayoutInflater infalInflater = (LayoutInflater) getActContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.design_fare_deatil_row, null);

            convertView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

            convertView.setMinimumHeight(Utils.dipToPixels(getActContext(), 30));

            MTextView titleHTxt = (MTextView) convertView.findViewById(R.id.titleHTxt);
            MTextView titleVTxt = (MTextView) convertView.findViewById(R.id.titleVTxt);

            titleHTxt.setText(generalFunc.convertNumberWithRTL(row_name));
            titleVTxt.setText(generalFunc.convertNumberWithRTL(row_value));

            if (isLast) {
                titleHTxt.setTextColor(getResources().getColor(R.color.black));
                titleHTxt.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
                Typeface face = Typeface.createFromAsset(getAssets(), "fonts/Poppins_SemiBold.ttf");
                titleHTxt.setTypeface(face);
                titleVTxt.setTypeface(face);
                titleVTxt.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
                titleVTxt.setTextColor(getResources().getColor(R.color.appThemeColor_1));
            }


        }

        if (convertView != null)
            fareDetailDisplayArea.addView(convertView);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case GeneralFunctions.MY_PERMISSIONS_REQUEST: {
                if (generalFunc.isPermisionGranted()) {
                    submitBtn.performClick();
                }
                break;

            }
        }
    }

    private void confirmDeliveryStatus() {

        if (isSignatureView) {
            ArrayList<String[]> paramsList = new ArrayList<>();
            paramsList.add(generalFunc.generateImageParams("type", "ConfirmDelivery"));
            paramsList.add(generalFunc.generateImageParams("iTripId", getIntent().getStringExtra("TripId")));
            paramsList.add(generalFunc.generateImageParams("UserType", Utils.userType));
            paramsList.add(generalFunc.generateImageParams("CheckFor", getIntent().getStringExtra("CheckFor")));
            paramsList.add(generalFunc.generateImageParams("vDeliveryConfirmCode", Utils.getText(verificationCodeBox)));

            paramsList.add(generateImageParams("tSessionId", generalFunc.getMemberId().equals("") ? "" : generalFunc.retrieveValue(Utils.SESSION_ID_KEY)));
            paramsList.add(generateImageParams("GeneralUserType", Utils.app_type));
            paramsList.add(generateImageParams("GeneralMemberId", generalFunc.getMemberId()));

            new UploadProfileImage(ViewMultiDeliveryDetailsActivity.this, filePath, Utils.TempProfileImageName, paramsList, "Signature").execute();
        } else {
            HashMap<String, String> parameters = new HashMap<String, String>();
            parameters.put("type", "ConfirmDelivery");
            parameters.put("iTripId", getIntent().getStringExtra("TripId"));
            parameters.put("UserType", Utils.userType);
            parameters.put("CheckFor", getIntent().getStringExtra("CheckFor"));
            parameters.put("vDeliveryConfirmCode", Utils.getText(verificationCodeBox));


            ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(), parameters);
            exeWebServer.setLoaderConfig(getActContext(), true, generalFunc);
            exeWebServer.setDataResponseListener(responseString -> tripResponse(responseString));
            exeWebServer.execute();

        }


    }

    public void handleImgUploadResponse(String responseString, String imageUploadedType) {

        if (responseString != null && !responseString.equals("")) {
            if (imageUploadedType.equalsIgnoreCase("Signature")) {
                tripResponse(responseString);
            }

        } else {
            generalFunc.showError();
        }
    }


    private void tripResponse(String responseString) {

        if (responseString != null && !responseString.equals("")) {

            boolean isDataAvail = GeneralFunctions.checkDataAvail(Utils.action_str, responseString);

            if (isDataAvail == true) {
                MyApp.getInstance().restartWithGetDataApp(false);
            } else {
                String msg_str = generalFunc.getJsonValue(Utils.message_str, responseString);
                if (msg_str.equals(Utils.GCM_FAILED_KEY) || msg_str.equals(Utils.APNS_FAILED_KEY) || msg_str.equals("LBL_SERVER_COMM_ERROR")) {
                    generalFunc.restartApp();
                } else {
                    String CheckFor = getIntent().hasExtra("CheckFor") ? getIntent().getStringExtra("CheckFor") : "";

                    if (CheckFor.equals("Sender")) {
                        buildPaymentCollectFailedMessage(generalFunc.retrieveLangLBl("",
                                generalFunc.getJsonValue(Utils.message_str, responseString)), "");

                    } else {
                        generalFunc.showGeneralMessage("",
                                generalFunc.retrieveLangLBl("", generalFunc.getJsonValue(Utils.message_str, responseString)));
                    }
                }

            }
        } else {
            generalFunc.showError();
        }
    }


    public void buildPaymentCollectFailedMessage(String msg, final String from) {

        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getActContext(), R.style.StackedAlertDialogStyle);
        builder.setTitle("");
        builder.setCancelable(false);

        builder.setMessage(msg);

        builder.setPositiveButton(generalFunc.retrieveLangLBl("", "LBL_RETRY_TXT"), (dialog, which) -> {
            collectPaymentFailedDialog.dismiss();
            if (from.equalsIgnoreCase("collectCash")) {
                collectPayment("true");
            } else {
                confirmDeliveryStatus();
            }
        });
        builder.setNegativeButton(generalFunc.retrieveLangLBl("Collect Cash", "LBL_COLLECT_CASH"), (dialog, which) -> {
            collectPaymentFailedDialog.dismiss();
            collectPayment("true");
        });

        collectPaymentFailedDialog = builder.create();

        collectPaymentFailedDialog.setOnShowListener(dialog -> {

            ((Button) ((AlertDialog) dialog).getButton(Dialog.BUTTON_POSITIVE)).setTypeface(null, Typeface.BOLD_ITALIC);

            //Personalizamos
            Resources res = getActContext().getResources();
            int posBtnTxtColor = res.getColor(android.R.color.white);
            int posBtnBackColor = res.getColor(R.color.appThemeColor_1);

            //Buttons
            ((Button) ((AlertDialog) dialog).getButton(Dialog.BUTTON_POSITIVE)).setTextColor(posBtnTxtColor);
            ((Button) ((AlertDialog) dialog).getButton(Dialog.BUTTON_POSITIVE)).setBackgroundColor(posBtnBackColor);

            //Buttons
            Button negButton = ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_NEGATIVE);

            ((Button) ((AlertDialog) dialog).getButton(Dialog.BUTTON_NEGATIVE)).setTextColor(posBtnTxtColor);
            ((Button) ((AlertDialog) dialog).getButton(Dialog.BUTTON_NEGATIVE)).setBackgroundColor(posBtnBackColor);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(10, 0, 10, 0);

            negButton.setLayoutParams(params);

        });
        collectPaymentFailedDialog.setCancelable(false);
        collectPaymentFailedDialog.setCanceledOnTouchOutside(false);
        collectPaymentFailedDialog.show();
    }

    public void collectPayment(String isCollectCash) {
        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "CollectPayment");
        parameters.put("iTripId", getIntent().getStringExtra("TripId"));
        if (!isCollectCash.equals("")) {
            parameters.put("isCollectCash", isCollectCash);
        }

        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(), parameters);
        exeWebServer.setLoaderConfig(getActContext(), true, generalFunc);
        exeWebServer.setDataResponseListener(responseString -> {
            JSONObject responseStringObj = generalFunc.getJsonObject(responseString);

            if (responseStringObj != null && !responseStringObj.equals("")) {

                boolean isDataAvail = GeneralFunctions.checkDataAvail(Utils.action_str, responseStringObj);

                if (isDataAvail) {
                    MyApp.getInstance().restartWithGetDataApp(false);
                } else {
                    buildPaymentCollectFailedMessage(generalFunc.retrieveLangLBl("",
                            generalFunc.getJsonValueStr(Utils.message_str, responseStringObj)), "collectCash");

                }
            } else {
                generalFunc.showError();
            }
        });
        exeWebServer.execute();
    }

    public void sinchCall() {


        if (MyApp.getInstance().getCurrentAct() != null) {
            String userProfileJsonObj = generalFunc.retrieveValue(Utils.USER_PROFILE_JSON);

            if (!generalFunc.isCallPermissionGranted(false)) {
                generalFunc.isCallPermissionGranted(true);
            } else {
                ViewMultiDeliveryDetailsActivity activity = (ViewMultiDeliveryDetailsActivity) MyApp.getInstance().getCurrentAct();
                if (new AppFunctions(getActContext()).checkSinchInstance(activity != null ? activity.getSinchServiceInterface() : null)) {
                    HashMap<String, String> hashMap = new HashMap<>();
                    hashMap.put("Id", generalFunc.getMemberId());

                    hashMap.put("Name", generalFunc.getJsonValue("vName", userProfileJsonObj));
                    hashMap.put("PImage", generalFunc.getJsonValue("vImage", userProfileJsonObj));
                    hashMap.put("type", Utils.userType);

                    activity.getSinchServiceInterface().getSinchClient().setPushNotificationDisplayName(generalFunc.retrieveLangLBl("", "LBL_INCOMING_CALL"));
//                    Call call = activity.getSinchServiceInterface().callUser(Utils.CALLTOPASSENGER + "_" + data_trip.get("PassengerId"), hashMap);

                    Call call;
                    // Whenever Manual Booking For Multi Delivery Added In Admin
                    if (Utils.checkText(data_trip.get("iGcmRegId_U"))) {
                        call = activity.getSinchServiceInterface().callUser(Utils.CALLTOPASSENGER + "_" + data_trip.get("PassengerId"), hashMap);
                    } else {
                        call = activity.getSinchServiceInterface().callPhoneNumber(data_trip.get("vPhone_U"));
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


    }


    public void getMaskNumber() {

        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "getCallMaskNumber");
        parameters.put("iTripid", getIntent().getStringExtra("TripId"));
        parameters.put("UserType", Utils.userType);
        parameters.put("iMemberId", generalFunc.getMemberId());

        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(), parameters);
        exeWebServer.setLoaderConfig(getActContext(), true, generalFunc);

        exeWebServer.setDataResponseListener(responseString -> {
            JSONObject responseStringObj = generalFunc.getJsonObject(responseString);

            if (responseStringObj != null && !responseStringObj.equals("")) {

                boolean isDataAvail = GeneralFunctions.checkDataAvail(Utils.action_str, responseStringObj);

                if (isDataAvail == true) {
                    String message = generalFunc.getJsonValueStr(Utils.message_str, responseStringObj);
                    call(message, "sender");
                } else {
                    call((Utils.getText(senderPhoneValTxt).toString()), "sender");

                }
            } else {
                generalFunc.showError();
            }
        });
        exeWebServer.execute();
    }

    public void call(String phoneNumber, String isFrom) {
        try {

            Intent callIntent = new Intent(Intent.ACTION_DIAL);
            callIntent.setData(Uri.parse("tel:" + phoneNumber.replaceAll("\\s+", "")));
            startActivity(callIntent);

        } catch (Exception e) {
            // TODO: handle exception
        }
    }

    public void sendMsg(String phoneNumber, String isFrom) {
        try {

            if (!isFrom.equalsIgnoreCase("sender")) {
                Intent smsIntent = new Intent(Intent.ACTION_VIEW);
                smsIntent.setType("vnd.android-dir/mms-sms");
                smsIntent.putExtra("address", "" + phoneNumber);
                startActivity(smsIntent);
            } else {

                HashMap<String, String> trip_data = (HashMap<String, String>) getIntent().getSerializableExtra("TRIP_DATA");
                Bundle bnChat = new Bundle();
                bnChat.putString("iTripId", getIntent().getStringExtra("TripId"));
                bnChat.putString("iFromMemberId", iUserId);
                bnChat.putString("FromMemberImageName", riderImage);
                bnChat.putString("FromMemberName", vName);
                bnChat.putString("vBookingNo", trip_data.get("vRideNo"));
                new StartActProcess(getActContext()).startActWithData(ChatActivity.class, bnChat);
            }


        } catch (Exception e) {
            // TODO: handle exception
        }
    }

    // signature View
    public class signature extends View {

        private static final float STROKE_WIDTH = 5f;
        private static final float HALF_STROKE_WIDTH = STROKE_WIDTH / 2;
        private Paint paint = new Paint();
        private Path path = new Path();

        private float lastTouchX;
        private float lastTouchY;
        private final RectF dirtyRect = new RectF();
        private int width, height;

        public signature(Context context, AttributeSet attrs) {
            super(context, attrs);
            filePath = "";
            paint.setAntiAlias(true);
            paint.setColor(Color.BLACK);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeJoin(Paint.Join.ROUND);
            paint.setStrokeWidth(STROKE_WIDTH);
        }


        public void save(View v) {
            if (bitmap == null) {
                bitmap = Bitmap.createBitmap(mContent.getWidth(), mContent.getHeight(), Bitmap.Config.RGB_565);
            }
            Canvas canvas = new Canvas(bitmap);
            try {
                // Output the file
                filePath = getOutputMediaFilePath();
                FileOutputStream mFileOutStream = new FileOutputStream(filePath);
                v.draw(canvas);
                // Convert the output file to Image such as .png
                bitmap.compress(Bitmap.CompressFormat.PNG, 90, mFileOutStream);
                mFileOutStream.flush();
                mFileOutStream.close();

                confirmDeliveryStatus();

            } catch (Exception e) {
                Log.v("log_tag", e.toString());
            }
        }

        public void clear() {
            noSign = false;
            path.reset();

            onSizeChanged(width, height, width, height);
            filePath = "";
            invalidate();

            setDrawingCacheEnabled(false);
        }

        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            super.onSizeChanged(w, h, oldw, oldh);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            canvas.drawPath(path, paint);
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            float eventX = event.getX();
            float eventY = event.getY();

            scrollView.setScrolling(false);

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    noSign = true;

                    path.moveTo(eventX, eventY);
                    lastTouchX = eventX;
                    lastTouchY = eventY;
                    return true;

                case MotionEvent.ACTION_MOVE:


                case MotionEvent.ACTION_UP:
                    noSign = true;
                    resetDirtyRect(eventX, eventY);
                    int historySize = event.getHistorySize();
                    for (int i = 0; i < historySize; i++) {
                        float historicalX = event.getHistoricalX(i);
                        float historicalY = event.getHistoricalY(i);
                        expandDirtyRect(historicalX, historicalY);
                        path.lineTo(historicalX, historicalY);
                    }
                    path.lineTo(eventX, eventY);
                    break;

                default:
                    scrollView.setScrolling(true);
                    debug("Ignored touch event: " + event.toString());
                    return false;
            }

            invalidate((int) (dirtyRect.left - HALF_STROKE_WIDTH),
                    (int) (dirtyRect.top - HALF_STROKE_WIDTH),
                    (int) (dirtyRect.right + HALF_STROKE_WIDTH),
                    (int) (dirtyRect.bottom + HALF_STROKE_WIDTH));

            lastTouchX = eventX;
            lastTouchY = eventY;

            return true;
        }

        private void debug(String string) {
            Log.v("log_tag", string);
        }

        private void expandDirtyRect(float historicalX, float historicalY) {
            if (historicalX < dirtyRect.left) {
                dirtyRect.left = historicalX;
            } else if (historicalX > dirtyRect.right) {
                dirtyRect.right = historicalX;
            }

            if (historicalY < dirtyRect.top) {
                dirtyRect.top = historicalY;
            } else if (historicalY > dirtyRect.bottom) {
                dirtyRect.bottom = historicalY;
            }
        }

        private void resetDirtyRect(float eventX, float eventY) {
            dirtyRect.left = Math.min(lastTouchX, eventX);
            dirtyRect.right = Math.max(lastTouchX, eventX);
            dirtyRect.top = Math.min(lastTouchY, eventY);
            dirtyRect.bottom = Math.max(lastTouchY, eventY);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
