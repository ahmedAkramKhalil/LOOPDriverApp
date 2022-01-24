package com.taxifgo.driver;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import android.text.Html;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.general.files.AppFunctions;
import com.general.files.ExecuteWebServerUrl;
import com.general.files.GeneralFunctions;
import com.general.files.MapDelegate;
import com.general.files.MapServiceApi;
import com.general.files.MyApp;
import com.general.files.StartActProcess;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.model.Delivery_Data;
import com.model.Trip_Status;
import com.squareup.picasso.Picasso;
import com.utils.Utilities;
import com.utils.Utils;
import com.view.ErrorView;
import com.view.MTextView;
import com.view.SelectableRoundedImageView;
import com.view.simpleratingbar.SimpleRatingBar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class RideHistoryDetailActivity extends AppCompatActivity implements OnMapReadyCallback, MapDelegate {

    MTextView titleTxt;
    MTextView subTitleTxt;
    ImageView backImgView;

    public GeneralFunctions generalFunc;

    GoogleMap gMap;

    LinearLayout fareDetailDisplayArea;
    private View convertView = null;

    LinearLayout beforeServiceArea, afterServiceArea;
    String before_serviceImg_url = "";
    String after_serviceImg_url = "";
    MTextView cartypeTxt;
    MTextView tipHTxt, tipamtTxt, tipmsgTxt;
    CardView tiparea;
    LinearLayout profilearea;
    ImageView tipPluseImage;
    MTextView vReasonTitleTxt;
    /*Multi Delivery Rlated fields*/
    private ArrayList<Trip_Status> recipientDetailList = new ArrayList<>();
    private Dialog signatureImageDialog;
    private String senderImage;
    private String tripData;
    private String userProfileJson;

    MTextView viewReqServicesTxtView;

    ProgressBar loading, progresdefault;
    ErrorView errorView;
    RelativeLayout container;
    String headerLable = "", noVal = "", driverhVal = "";
    FrameLayout paymentMainArea;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ride_history_detail);

        generalFunc = MyApp.getInstance().getGeneralFun(getActContext());
        userProfileJson = generalFunc.retrieveValue(Utils.USER_PROFILE_JSON);
        paymentMainArea = findViewById(R.id.paymentMainArea);
        titleTxt = (MTextView) findViewById(R.id.titleTxt);
        profilearea = (LinearLayout) findViewById(R.id.profilearea);
        subTitleTxt = (MTextView) findViewById(R.id.subTitleTxt);
        backImgView = (ImageView) findViewById(R.id.backImgView);
        fareDetailDisplayArea = (LinearLayout) findViewById(R.id.fareDetailDisplayArea);
        afterServiceArea = (LinearLayout) findViewById(R.id.afterServiceArea);
        beforeServiceArea = (LinearLayout) findViewById(R.id.beforeServiceArea);
        tipPluseImage = (ImageView) findViewById(R.id.tipPluseImage);
        cartypeTxt = (MTextView) findViewById(R.id.cartypeTxt);
        vReasonTitleTxt = (MTextView) findViewById(R.id.vReasonTitleTxt);
        viewReqServicesTxtView = (MTextView) findViewById(R.id.viewReqServicesTxtView);

        loading = (ProgressBar) findViewById(R.id.loading);
        progresdefault = (ProgressBar) findViewById(R.id.progresdefault);
        progresdefault.setVisibility(View.VISIBLE);
        errorView = (ErrorView) findViewById(R.id.errorView);
        container = (RelativeLayout) findViewById(R.id.container);

        tipHTxt = (MTextView) findViewById(R.id.tipHTxt);
        tipamtTxt = (MTextView) findViewById(R.id.tipamtTxt);
        tipmsgTxt = (MTextView) findViewById(R.id.tipmsgTxt);

        tiparea = (CardView) findViewById(R.id.tiparea);

        //setLabels();
        // setData();

        getMemberBookings();


        backImgView.setOnClickListener(new setOnClickList());
        subTitleTxt.setOnClickListener(new setOnClickList());
        afterServiceArea.setOnClickListener(new setOnClickList());
        beforeServiceArea.setOnClickListener(new setOnClickList());
        viewReqServicesTxtView.setOnClickListener(new setOnClickList());
    }

    private void getMemberBookings() {
        if (errorView.getVisibility() == View.VISIBLE) {
            errorView.setVisibility(View.GONE);
        }
        if (container.getVisibility() == View.VISIBLE) {
            container.setVisibility(View.GONE);
        }
        if (paymentMainArea.getVisibility() == View.VISIBLE) {
            paymentMainArea.setVisibility(View.GONE);
        }
        if (loading.getVisibility() != View.VISIBLE) {
            loading.setVisibility(View.VISIBLE);
        }


        final HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "getMemberBookings");
        parameters.put("memberId", generalFunc.getMemberId());
        parameters.put("UserType", Utils.app_type);
        parameters.put("memberType", Utils.app_type);
        if (getIntent().hasExtra("iTripId")) {
            parameters.put("iTripId", getIntent().getExtras().getString("iTripId"));
        }
        if (getIntent().hasExtra("iCabBookingId")) {
            parameters.put("iCabBookingId", getIntent().getExtras().getString("iCabBookingId"));
        }

        final ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(), parameters);
        exeWebServer.setDataResponseListener(responseString -> {
            JSONObject responseObj = generalFunc.getJsonObject(responseString);

            if (responseObj != null && !responseObj.equals("")) {
                closeLoader();

                if (GeneralFunctions.checkDataAvail(Utils.action_str, responseObj)) {

                    JSONArray arr_rides = generalFunc.getJsonArray(Utils.message_str, responseObj);

                    if (arr_rides != null && arr_rides.length() > 0) {
                        for (int i = 0; i < arr_rides.length(); i++) {
                            JSONObject obj_temp = generalFunc.getJsonObject(arr_rides, i);
                            HashMap<String, String> map = new HashMap<String, String>();
                            map.put("tTripRequestDateOrig", generalFunc.getJsonValueStr("tTripRequestDateOrig", obj_temp));
                            map.put("CurrencySymbol", generalFunc.getJsonValueStr("CurrencySymbol", obj_temp));
                            map.put("tSaddress", generalFunc.getJsonValueStr("tSaddress", obj_temp));
                            map.put("tDaddress", generalFunc.getJsonValueStr("tDaddress", obj_temp));
                            map.put("vRideNo", generalFunc.getJsonValueStr("vRideNo", obj_temp));
                            map.put("is_rating", generalFunc.getJsonValueStr("is_rating", obj_temp));
                            map.put("iTripId", generalFunc.getJsonValueStr("iTripId", obj_temp));
                            map.put("eFly", generalFunc.getJsonValueStr("eFly", obj_temp));

                            if (generalFunc.getJsonValueStr("eType", obj_temp).equalsIgnoreCase("deliver") || generalFunc.getJsonValue("eType", obj_temp).equals(Utils.eType_Multi_Delivery)) {
                                map.put("eType", generalFunc.retrieveLangLBl("Delivery", "LBL_DELIVERY"));
                                map.put("LBL_PICK_UP_LOCATION", generalFunc.retrieveLangLBl("Sender Location", "LBL_SENDER_LOCATION"));
                                map.put("LBL_DEST_LOCATION", generalFunc.retrieveLangLBl("Receiver's Location", "LBL_RECEIVER_LOCATION"));
                            } else {
                                map.put("LBL_PICK_UP_LOCATION", generalFunc.retrieveLangLBl("", "LBL_PICK_UP_LOCATION"));
                                map.put("eType", generalFunc.getJsonValueStr("eType", obj_temp));
                                map.put("LBL_DEST_LOCATION", generalFunc.retrieveLangLBl("", "LBL_DEST_LOCATION"));
                            }
                            map.put("eFareType", generalFunc.getJsonValueStr("eFareType", obj_temp));
                            map.put("appType", generalFunc.getJsonValue("APP_TYPE", userProfileJson));

                            senderImage = generalFunc.getJsonValueStr("vSignImage", obj_temp);

                            if (Utils.checkText(senderImage)) {
                                findViewById(R.id.signArea).setVisibility(View.VISIBLE);
                                findViewById(R.id.signArea).setOnClickListener(new setOnClickList());
                            }


                            if (generalFunc.getJsonValueStr("eCancelled", obj_temp).equals("Yes")) {
                                map.put("iActive", generalFunc.retrieveLangLBl("", "LBL_CANCELED_TXT"));
                            } else {
                                if (generalFunc.getJsonValueStr("iActive", obj_temp).equals("Canceled")) {
                                    map.put("iActive", generalFunc.retrieveLangLBl("", "LBL_CANCELED_TXT"));
                                } else if (generalFunc.getJsonValueStr("iActive", obj_temp).equals("Finished")) {
                                    map.put("iActive", generalFunc.retrieveLangLBl("", "LBL_FINISHED_TXT"));
                                } else {
                                    map.put("iActive", generalFunc.getJsonValueStr("iActive", obj_temp));
                                }
                            }

                            if (generalFunc.retrieveValue(Utils.APP_DESTINATION_MODE).equalsIgnoreCase(Utils.NONE_DESTINATION)) {
                                map.put("DESTINATION", "No");
                            } else {
                                map.put("DESTINATION", "Yes");
                            }


                            map.put("JSON", obj_temp.toString());
                            map.put("APP_TYPE", generalFunc.getJsonValue("APP_TYPE", userProfileJson));

                            if (generalFunc.getJsonValueStr("eType", obj_temp).equals(Utils.CabGeneralType_UberX) &&
                                    !generalFunc.getJsonValueStr("eFareType", obj_temp).equalsIgnoreCase(Utils.CabFaretypeRegular)) {

                                map.put("SelectedVehicle", generalFunc.getJsonValueStr("carTypeName", obj_temp));
                                map.put("SelectedCategory", generalFunc.getJsonValueStr("vVehicleCategory", obj_temp));


                            }
                            map.put("moreServices", generalFunc.getJsonValueStr("moreServices", obj_temp));
                            if (generalFunc.getJsonValueStr("eFareType", obj_temp).equalsIgnoreCase(Utils.CabFaretypeFixed) && generalFunc.getJsonValueStr("moreServices", obj_temp).equalsIgnoreCase("No")) {
                                map.put("SelectedCategory", generalFunc.getJsonValueStr("vCategory", obj_temp));

                            }

                            map.put("LBL_BOOKING_NO", generalFunc.retrieveLangLBl("Delivery No", "LBL_DELIVERY_NO"));
                            map.put("LBL_CANCEL_BOOKING", generalFunc.retrieveLangLBl("Cancel Delivery", "LBL_CANCEL_DELIVERY"));
                            map.put("LBL_BOOKING_NO", generalFunc.retrieveLangLBl("", "LBL_BOOKING"));
                            map.put("LBL_Status", generalFunc.retrieveLangLBl("", "LBL_Status"));
                            map.put("LBL_JOB_LOCATION_TXT", generalFunc.retrieveLangLBl("", "LBL_JOB_LOCATION_TXT"));

                            String paymentDoneByDetail = generalFunc.getJsonValueStr("PaymentPerson", obj_temp);

                            if (Utils.checkText(paymentDoneByDetail)) {
                                ((MTextView) findViewById(R.id.recipientTxt)).setVisibility(View.VISIBLE);
                                ((MTextView) findViewById(R.id.recipientTxt)).setText(" " + generalFunc.retrieveLangLBl("Paid By", "LBL_PAID_BY_TXT") + " " + paymentDoneByDetail);
                            }


                            if (map != null) {
                                tripData = map.get("JSON");
                                setLabels(tripData);
                                setData(tripData);
                            }

                        }
                    }


                } else {
                    generateErrorView();
                }
            } else {
                generateErrorView();
            }
        });
        exeWebServer.execute();
    }

    public void closeLoader() {
        progresdefault.setVisibility(View.GONE);
        if (loading.getVisibility() == View.VISIBLE) {
            loading.setVisibility(View.GONE);
        }

        if (container.getVisibility() == View.GONE) {
            container.setVisibility(View.VISIBLE);
        }
        if (paymentMainArea.getVisibility() == View.GONE) {
            paymentMainArea.setVisibility(View.VISIBLE);
        }
    }

    public void generateErrorView() {

        closeLoader();

        generalFunc.generateErrorView(errorView, "LBL_ERROR_TXT", "LBL_NO_INTERNET_TXT");

        if (errorView.getVisibility() != View.VISIBLE) {
            errorView.setVisibility(View.VISIBLE);
        }

        if (container.getVisibility() == View.VISIBLE) {
            container.setVisibility(View.GONE);
        }
        if (paymentMainArea.getVisibility() == View.VISIBLE) {
            paymentMainArea.setVisibility(View.GONE);
        }

        errorView.setOnRetryListener(() -> getMemberBookings());
    }


    public void setLabels(String tripData) {
        /*Multi related new lable*/
        ((MTextView) findViewById(R.id.viewSingleDeliveryTitleTxt)).setText(generalFunc.retrieveLangLBl("", "LBL_DELIVERY_DETAILS"));

        ((MTextView) findViewById(R.id.passengerSignTxt)).setText(generalFunc.retrieveLangLBl("View Signature", "LBL_VIEW_MULTI_SENDER_SIGN"));


//        tripData = getIntent().getStringExtra("TripData");
        boolean eFly = generalFunc.getJsonValue("eFly", tripData).equalsIgnoreCase("Yes");

        titleTxt.setText(generalFunc.retrieveLangLBl("", "LBL_RECEIPT_HEADER_TXT"));
        subTitleTxt.setText(generalFunc.retrieveLangLBl("", "LBL_GET_RECEIPT_TXT"));
        viewReqServicesTxtView.setText(generalFunc.retrieveLangLBl("", "LBL_VIEW_REQUESTED_SERVICES"));


        if (generalFunc.getJsonValue("eType", tripData).equals(Utils.CabGeneralType_UberX)) {
//            headerLable = generalFunc.retrieveLangLBl("", "LBL_THANKS_UFX_DRIVER");
            headerLable = generalFunc.retrieveLangLBl("", "LBL_THANKS_TXT");
            noVal = generalFunc.retrieveLangLBl("", "LBL_SERVICES") + "#";
            driverhVal = generalFunc.retrieveLangLBl("", "LBL_USER");
        } else if (generalFunc.getJsonValue("eType", tripData).equals("Deliver") || generalFunc.getJsonValue("eType", tripData).equals(Utils.eType_Multi_Delivery)) {
//            headerLable = generalFunc.retrieveLangLBl("", "LBL_THANKS_DELIVERY_DRIVER");
            headerLable = generalFunc.retrieveLangLBl("", "LBL_THANKS_TXT");
            noVal = generalFunc.retrieveLangLBl("", "LBL_DELIVERY") + "#";
            driverhVal = generalFunc.retrieveLangLBl("", "LBL_SENDER");
        } else {
//            headerLable = generalFunc.retrieveLangLBl("", "LBL_THANKS_RIDING_DRIVER");
            headerLable = generalFunc.retrieveLangLBl("", "LBL_THANKS_TXT");
            noVal = generalFunc.retrieveLangLBl("", eFly ? "LBL_HEADER_RDU_FLY_RIDE" : "LBL_RIDE") + "#";
            driverhVal = generalFunc.retrieveLangLBl("", "LBL_PASSENGER_TXT");
        }

        ((MTextView) findViewById(R.id.headerTxt)).setText(generalFunc.retrieveLangLBl("", headerLable));

        ((MTextView) findViewById(R.id.rideNoHTxt)).setText(noVal);
//        ((MTextView) findViewById(R.id.ratingDriverHTxt)).setText(generalFunc.retrieveLangLBl("", "LBL_RATING"));
//        ((MTextView) findViewById(R.id.passengerHTxt)).setText(driverhVal);
        String dateLable = "";
        String pickupHval = "";

        if (generalFunc.getJsonValue("eType", tripData).equals(Utils.CabGeneralType_UberX)) {
            dateLable = generalFunc.retrieveLangLBl("", "LBL_JOB_REQ_DATE");
            pickupHval = generalFunc.retrieveLangLBl("", "LBL_JOB_LOCATION_TXT");
            tipmsgTxt.setText(generalFunc.retrieveLangLBl("Congratulation! You got a tip from the passenger for this trip.", "LBL_TIP_INFO_SHOW_PROVIDER"));
        } else if (generalFunc.getJsonValue("eType", tripData).equals("Deliver") || generalFunc.getJsonValue("eType", tripData).equals(Utils.eType_Multi_Delivery)) {
            dateLable = generalFunc.retrieveLangLBl("", "LBL_DELIVERY_REQUEST_DATE");
            pickupHval = generalFunc.retrieveLangLBl("", "LBL_SENDER_LOCATION");
            tipmsgTxt.setText(generalFunc.retrieveLangLBl("Congratulation! You got a tip from the passenger for this trip.", "LBL_TIP_INFO_SHOW_CARRIER"));
        } else {
            dateLable = generalFunc.retrieveLangLBl("", "LBL_TRIP_REQUEST_DATE_TXT");
            pickupHval = generalFunc.retrieveLangLBl("", "LBL_PICKUP_LOCATION_TXT");
            tipmsgTxt.setText(generalFunc.retrieveLangLBl("Congratulation! You got a tip from the passenger for this trip.", "LBL_TIP_INFO_SHOW_DRIVER"));
        }

//        ((MTextView) findViewById(R.id.tripdateHTxt)).setText(generalFunc.retrieveLangLBl("", dateLable));
        ((MTextView) findViewById(R.id.pickUpHTxt)).setText(pickupHval);
        ((MTextView) findViewById(R.id.pickUpAddressHTxt)).setText(pickupHval);

        if (generalFunc.getJsonValue("eType", tripData).equals("Deliver") || generalFunc.getJsonValue("eType", tripData).equals(Utils.eType_Multi_Delivery)) {
            ((MTextView) findViewById(R.id.dropOffHTxt)).setText(generalFunc.retrieveLangLBl("", "LBL_DELIVERY_DETAILS_TXT"));
        } else if (generalFunc.getJsonValue("eType", tripData).equals(Utils.CabGeneralType_Ride)) {
            ((MTextView) findViewById(R.id.dropOffHTxt)).setText(generalFunc.retrieveLangLBl("", "LBL_DEST_LOCATION"));
        } else {
            ((MTextView) findViewById(R.id.dropOffHTxt)).setText(generalFunc.retrieveLangLBl("", "LBL_DEST_LOCATION"));

        }
        ((MTextView) findViewById(R.id.chargesHTxt)).setText(generalFunc.retrieveLangLBl("", "LBL_CHARGES_TXT"));
        ((MTextView) findViewById(R.id.serviceHTxt)).setText(generalFunc.retrieveLangLBl("", "LBL_SERVICE_TXT"));

        tipHTxt.setText(generalFunc.retrieveLangLBl("Tip Amount", "LBL_TIP_AMOUNT"));


    }

    public void setData(String tripData) {
        //String tripData = getIntent().getStringExtra("TripData");


        if (generalFunc.getJsonValue("vReasonTitle", tripData) != null && !generalFunc.getJsonValue("vReasonTitle", tripData).equalsIgnoreCase("")) {
            vReasonTitleTxt.setVisibility(View.VISIBLE);
            vReasonTitleTxt.setText(generalFunc.getJsonValue("vReasonTitle", tripData));
        }

        if (generalFunc.getJsonValue("eHailTrip", tripData).equalsIgnoreCase("yes")) {
            profilearea.setVisibility(View.GONE);
        } else {
            profilearea.setVisibility(View.VISIBLE);
        }

        ((MTextView) findViewById(R.id.rideNoVTxt)).setText(generalFunc.convertNumberWithRTL(generalFunc.getJsonValue("vRideNo", tripData)));
        ((MTextView) findViewById(R.id.namePassengerVTxt)).setText(generalFunc.getJsonValue("vName", tripData) + " " +
                generalFunc.getJsonValue("vLastName", tripData) + " (" + driverhVal + ")");
        ((MTextView) findViewById(R.id.tripdateVTxt)).setText(generalFunc.convertNumberWithRTL(generalFunc.getDateFormatedType(generalFunc.getJsonValue("tTripRequestDateOrig", tripData), Utils.OriginalDateFormate, Utils.getDetailDateFormat(getActContext()))));
        ((MTextView) findViewById(R.id.pickUpVTxt)).setText(generalFunc.getJsonValue("tSaddress", tripData));
        ((MTextView) findViewById(R.id.pickUpAddressVTxt)).setText(generalFunc.getJsonValue("tSaddress", tripData));

        if (generalFunc.getJsonValue("eChargeViewShow", tripData) != null && generalFunc.getJsonValue("eChargeViewShow", tripData).equalsIgnoreCase("No")) {
            if (!generalFunc.getJsonValue("iActive", tripData).equalsIgnoreCase("Canceled")) {
                findViewById(R.id.headerTxt).setVisibility(View.GONE);
            }
            findViewById(R.id.chargeArea).setVisibility(View.GONE);
            findViewById(R.id.paymentarea).setVisibility(View.GONE);
            findViewById(R.id.helpTxt).setVisibility(View.GONE);
            ((MTextView) findViewById(R.id.rideNoVTxt)).setText(generalFunc.convertNumberWithRTL(generalFunc.getJsonValue("vRideNo", tripData)));
            LinearLayout.LayoutParams txtParam = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            txtParam.setMargins(2, 10, 2, 0);
            ((MTextView) findViewById(R.id.rideNoVTxt)).setLayoutParams(txtParam);
            ((MTextView) findViewById(R.id.rideNoHTxt)).setLayoutParams(txtParam);

        }

        if (generalFunc.getJsonValue("eType", tripData).equals("Deliver")) {

            (findViewById(R.id.viewDeliveryDetailsArea)).setVisibility(View.VISIBLE);
            (findViewById(R.id.addressArea)).setVisibility(View.GONE);
            (findViewById(R.id.sourceLocCardArea)).setVisibility(View.VISIBLE);
            ((MTextView) findViewById(R.id.viewSingleDeliveryTitleTxt)).setVisibility(View.VISIBLE);
            ((MTextView) findViewById(R.id.viewSingleDeliveryDetails)).setVisibility(View.VISIBLE);
            ((MTextView) findViewById(R.id.viewDeliveryDetails)).setVisibility(View.GONE);
            ((MTextView) findViewById(R.id.viewSingleDeliveryDetails)).setText(generalFunc.retrieveLangLBl("", "LBL_RECEIVER_NAME") + ": " + generalFunc.getJsonValue("vReceiverName", tripData) + "\n\n" +
                    generalFunc.retrieveLangLBl("", "LBL_RECEIVER_LOCATION") + ": " + generalFunc.getJsonValue("tDaddress", tripData) + "\n\n" +
                    generalFunc.retrieveLangLBl("", "LBL_PACKAGE_TYPE_TXT") + ": " + generalFunc.getJsonValue("PackageType", tripData) + "\n\n" +
                    generalFunc.retrieveLangLBl("", "LBL_PACKAGE_DETAILS") + ": " + generalFunc.getJsonValue("tPackageDetails", tripData)
            );
        } else {
            ((MTextView) findViewById(R.id.dropOffVTxt)).setText(generalFunc.getJsonValue("tDaddress", tripData));
        }


        cartypeTxt.setText(generalFunc.getJsonValue("vServiceDetailTitle", tripData));

        if (!generalFunc.getJsonValue("tDaddress", tripData).equals("")) {
            (findViewById(R.id.destarea)).setVisibility(View.VISIBLE);
            (findViewById(R.id.aboveLine)).setVisibility(View.VISIBLE);
        }else
        {
            (findViewById(R.id.addressArea)).setVisibility(View.GONE);
            (findViewById(R.id.sourceLocCardArea)).setVisibility(View.VISIBLE);
        }

        if (!generalFunc.getJsonValue("fTipPrice", tripData).equals("0") && !generalFunc.getJsonValue("fTipPrice", tripData).equals("0.0") &&
                !generalFunc.getJsonValue("fTipPrice", tripData).equals("0.00") &&
                !generalFunc.getJsonValue("fTipPrice", tripData).equals("")) {
            tiparea.setVisibility(View.VISIBLE);
            tipPluseImage.setVisibility(View.VISIBLE);

            tipamtTxt.setText(generalFunc.getJsonValue("fTipPrice", tripData));
        } else {
            tiparea.setVisibility(View.GONE);
            tipPluseImage.setVisibility(View.GONE);
        }

        String trip_status_str = generalFunc.getJsonValue("iActive", tripData);
        if (trip_status_str.contains("Canceled")) {
            String cancelLable = "";
            String cancelableReason = generalFunc.getJsonValue("vCancelReason", tripData);

            if (generalFunc.getJsonValue("eCancelledBy", tripData).equalsIgnoreCase("DRIVER")) {
                if (generalFunc.getJsonValue("eType", tripData).equals(Utils.CabGeneralType_UberX)) {
                    cancelLable = generalFunc.retrieveLangLBl("", "LBL_CANCELED_JOB");
                } else if (generalFunc.getJsonValue("eType", tripData).equals("Deliver") || generalFunc.getJsonValue("eType", tripData).equals(Utils.eType_Multi_Delivery)) {
                    cancelLable = generalFunc.retrieveLangLBl("", "LBL_CANCELED_DELIVERY_TXT");
                } else {
                    cancelLable = generalFunc.retrieveLangLBl("", "LBL_CANCELED_TRIP_TXT");
                }
            } else {
                if (generalFunc.getJsonValue("eType", tripData).equals(Utils.CabGeneralType_UberX)) {
                    cancelLable = generalFunc.retrieveLangLBl("", "LBL_USER_CANCEL_JOB_TXT");
                } else if (generalFunc.getJsonValue("eType", tripData).equals("Deliver") || generalFunc.getJsonValue("eType", tripData).equals(Utils.eType_Multi_Delivery)) {
                    cancelLable = generalFunc.retrieveLangLBl("", "LBL_SENDER_CANCEL_DELIVERY_TXT");
                } else {
                    cancelLable = generalFunc.retrieveLangLBl("", "LBL_PASSENGER_CANCEL_TRIP_TXT");
                }

            }
            (findViewById(R.id.cancelReasonArea)).setVisibility(View.VISIBLE);
            (findViewById(R.id.tripStatusArea)).setVisibility(View.GONE);
            ((MTextView) findViewById(R.id.vReasonHTxt)).setText(cancelLable);
            ((MTextView) findViewById(R.id.vReasonVTxt)).setText(cancelableReason);

            if (!generalFunc.getJsonValue("tDaddress", tripData).equals("")) {
                (findViewById(R.id.destarea)).setVisibility(View.VISIBLE);
                (findViewById(R.id.aboveLine)).setVisibility(View.VISIBLE);
            }

        } else if (trip_status_str.contains("Finished")) {
            String finishLable = "";
            if (generalFunc.getJsonValue("eType", tripData).equals(Utils.CabGeneralType_UberX)) {
                finishLable = generalFunc.retrieveLangLBl("", "LBL_FINISHED_JOB_TXT");
            } else if (generalFunc.getJsonValue("eType", tripData).equals("Deliver") || generalFunc.getJsonValue("eType", tripData).equals(Utils.eType_Multi_Delivery)) {
                finishLable = generalFunc.retrieveLangLBl("", "LBL_FINISHED_DELIVERY_TXT");
            } else {
                finishLable = generalFunc.retrieveLangLBl("", "LBL_FINISHED_TRIP_TXT");
            }

            ((MTextView) findViewById(R.id.tripStatusTxt)).setText(generalFunc.retrieveLangLBl("", finishLable));

            if (!generalFunc.getJsonValue("tDaddress", tripData).equals("")) {
                (findViewById(R.id.destarea)).setVisibility(View.VISIBLE);
                (findViewById(R.id.aboveLine)).setVisibility(View.VISIBLE);
            }

            subTitleTxt.setVisibility(View.VISIBLE);
        } else {
            ((MTextView) findViewById(R.id.tripStatusTxt)).setText(trip_status_str);
        }

        if (generalFunc.getJsonValue("vTripPaymentMode", tripData).equals("Cash")) {
            ((ImageView) findViewById(R.id.paymentTypeImgeView)).setImageResource(R.drawable.ic_cash_new);
            ((MTextView) findViewById(R.id.paymentTypeTxt)).setText(generalFunc.retrieveLangLBl("", "LBL_CASH_PAYMENT_TXT"));
        } else {
            ((MTextView) findViewById(R.id.paymentTypeTxt)).setText(generalFunc.retrieveLangLBl("Card Payment", "LBL_CARD_PAYMENT"));
            ((ImageView) findViewById(R.id.paymentTypeImgeView)).setImageResource(R.mipmap.ic_card_new);
        }

        if (generalFunc.getJsonValue("vTripPaymentMode", tripData).equalsIgnoreCase("Organization")) {
            ((MTextView) findViewById(R.id.paymentTypeTxt)).setText(generalFunc.retrieveLangLBl("", "LBL_PAYMENT_BY_TXT") + " " + generalFunc.getJsonValue("OrganizationName", tripData));
            ((ImageView) findViewById(R.id.paymentTypeImgeView)).setImageResource(R.drawable.ic_business_pay);
            ((ImageView) findViewById(R.id.paymentTypeImgeView)).setColorFilter(getResources().getColor(R.color.appThemeColor_1), PorterDuff.Mode.SRC_IN);
        }

        if (generalFunc.getJsonValue("ePayWallet", tripData).equals("Yes")) {
            ((MTextView) findViewById(R.id.paymentTypeTxt)).setText(generalFunc.retrieveLangLBl("Paid By Wallet", "LBL_PAID_VIA_WALLET"));
            ((ImageView) findViewById(R.id.paymentTypeImgeView)).setImageResource(R.mipmap.ic_menu_wallet);
        }


        if (generalFunc.getJsonValue("eCancelled", tripData).equals("Yes")) {
            subTitleTxt.setVisibility(View.GONE);
            String cancelledLable = "";
            String cancelableReason = generalFunc.getJsonValue("vCancelReason", tripData);

            if (generalFunc.getJsonValue("eCancelledBy", tripData).equalsIgnoreCase("DRIVER")) {
                if (generalFunc.getJsonValue("eType", tripData).equals(Utils.CabGeneralType_UberX)) {
                    cancelledLable = generalFunc.retrieveLangLBl("", "LBL_PREFIX_JOB_CANCEL_YOU") + " " + cancelableReason;
                } else if (generalFunc.getJsonValue("eType", tripData).equals("Deliver") || generalFunc.getJsonValue("eType", tripData).equals(Utils.eType_Multi_Delivery)) {
                    cancelledLable = generalFunc.retrieveLangLBl("", "LBL_PREFIX_DELIVERY_CANCEL_YOU") + " " + cancelableReason;
                } else {
                    cancelledLable = generalFunc.retrieveLangLBl("", "LBL_PREFIX_TRIP_CANCEL_YOU") + " " + cancelableReason;
                }
            } else {
                if (generalFunc.getJsonValue("eType", tripData).equals(Utils.CabGeneralType_UberX)) {
                    cancelledLable = generalFunc.retrieveLangLBl("", "LBL_USER_CANCEL_JOB_TXT");
                } else if (generalFunc.getJsonValue("eType", tripData).equals("Deliver") || generalFunc.getJsonValue("eType", tripData).equals(Utils.eType_Multi_Delivery)) {
                    cancelledLable = generalFunc.retrieveLangLBl("", "LBL_SENDER_CANCEL_DELIVERY_TXT");
                } else {
                    cancelledLable = generalFunc.retrieveLangLBl("", "LBL_PASSENGER_CANCEL_TRIP_TXT");
                }
            }

            ((MTextView) findViewById(R.id.tripStatusTxt)).setText(generalFunc.retrieveLangLBl("", cancelledLable));


        }

        ((SimpleRatingBar) findViewById(R.id.ratingBar)).setRating(generalFunc.parseFloatValue(0, generalFunc.getJsonValue("TripRating", tripData)));

        final ImageView driverImageview = (SelectableRoundedImageView) findViewById(R.id.driverImgView);
        String vImage = generalFunc.getJsonValue("vImage", tripData);
        if (vImage == null || vImage.equals("") || vImage.equals("NONE")) {
            (driverImageview).setImageResource(R.mipmap.ic_no_pic_user);
        } else {
            Picasso.get()
                    .load(vImage)
                    .placeholder(R.mipmap.ic_no_pic_user)
                    .error(R.mipmap.ic_no_pic_user)
                    .into(driverImageview);
        }

        if (generalFunc.getJsonValue("eType", tripData).equalsIgnoreCase("UberX") && generalFunc.getJsonValue("SERVICE_PROVIDER_FLOW", userProfileJson).equalsIgnoreCase("Provider")) {
            viewReqServicesTxtView.setVisibility(View.VISIBLE);
        }

        if (generalFunc.getJsonValue("eType", tripData).equalsIgnoreCase("UberX") || generalFunc.getJsonValue("eFareType", tripData).equalsIgnoreCase("Fixed")) {
            findViewById(R.id.card_service_area).setVisibility(View.VISIBLE);
            findViewById(R.id.serviceHTxt).setVisibility(View.GONE);
            findViewById(R.id.photoArea).setVisibility(View.VISIBLE);

            ((MTextView) findViewById(R.id.beforeImgHTxt)).setText(generalFunc.retrieveLangLBl("", "LBL_BEFORE_SERVICE"));
            ((MTextView) findViewById(R.id.afterImgHTxt)).setText(generalFunc.retrieveLangLBl("", "LBL_AFTER_SERVICE"));

            if (!TextUtils.isEmpty(generalFunc.getJsonValue("vBeforeImage", tripData))) {
                findViewById(R.id.beforeServiceArea).setVisibility(View.VISIBLE);
                before_serviceImg_url = generalFunc.getJsonValue("vBeforeImage", tripData);

                String vBeforeImage = Utilities.getResizeImgURL(getActContext(), before_serviceImg_url, getResources().getDimensionPixelSize(R.dimen.before_after_img_size), getResources().getDimensionPixelSize(R.dimen.before_after_img_size));

                displayPic(vBeforeImage, (ImageView) findViewById(R.id.iv_before_img), "before");

                findViewById(R.id.iv_before_img).setOnClickListener(v -> (new StartActProcess(getActContext())).openURL(before_serviceImg_url));
            } else {
                findViewById(R.id.beforeServiceArea).setVisibility(View.GONE);
            }

            if (!TextUtils.isEmpty(generalFunc.getJsonValue("vAfterImage", tripData))) {
                findViewById(R.id.afterServiceArea).setVisibility(View.VISIBLE);
                after_serviceImg_url = generalFunc.getJsonValue("vAfterImage", tripData);
                String vAfterImage = Utilities.getResizeImgURL(getActContext(), after_serviceImg_url, getResources().getDimensionPixelSize(R.dimen.before_after_img_size), getResources().getDimensionPixelSize(R.dimen.before_after_img_size));
                displayPic(vAfterImage, (ImageView) findViewById(R.id.iv_after_img), "after");
                findViewById(R.id.iv_after_img).setOnClickListener(v -> (new StartActProcess(getActContext())).openURL(after_serviceImg_url));
            } else {
                findViewById(R.id.afterServiceArea).setVisibility(View.GONE);
            }

            if (TextUtils.isEmpty(generalFunc.getJsonValue("vBeforeImage", tripData)) && TextUtils.isEmpty(generalFunc.getJsonValue("vAfterImage", tripData))) {
                findViewById(R.id.photoArea).setVisibility(View.GONE);
            }


            ((MTextView) findViewById(R.id.pickUpVTxt)).setText(generalFunc.getJsonValue("tSaddress", tripData));
            ((MTextView) findViewById(R.id.pickUpAddressVTxt)).setText(generalFunc.getJsonValue("tSaddress", tripData));

           /* if (generalFunc.getJsonValue("SERVICE_PROVIDER_FLOW", userProfileJson).equalsIgnoreCase("Provider") &&
                    generalFunc.getJsonValue("eFareType", tripData).equalsIgnoreCase("Fixed")) {
                ((MTextView) findViewById(R.id.serviceTypeVTxt)).setText(generalFunc.getJsonValue("vVehicleCategory", tripData));
            } else {
                ((MTextView) findViewById(R.id.serviceTypeVTxt)).setText(generalFunc.getJsonValue("vVehicleCategory", tripData) + " - " + generalFunc.getJsonValue("vVehicleType", tripData));
            }

            ((MTextView) findViewById(R.id.serviceTypeHTxt)).setText(generalFunc.retrieveLangLBl("", "LBL_Car_Type"));*/


        } else {
            if (!generalFunc.getJsonValue("tDaddress", tripData).equals("")) {
                (findViewById(R.id.destarea)).setVisibility(View.VISIBLE);
                (findViewById(R.id.aboveLine)).setVisibility(View.VISIBLE);
            }
            findViewById(R.id.service_area).setVisibility(View.GONE);
            findViewById(R.id.card_service_area).setVisibility(View.GONE);
            findViewById(R.id.serviceHTxt).setVisibility(View.GONE);
            findViewById(R.id.photoArea).setVisibility(View.GONE);
        }


        /*Show Multi Delivery Details*/
        if (generalFunc.getJsonValue("eType", tripData).equals(Utils.eType_Multi_Delivery)) {

            (findViewById(R.id.addressArea)).setVisibility(View.GONE);
            (findViewById(R.id.sourceLocCardArea)).setVisibility(View.VISIBLE);
            (findViewById(R.id.viewDeliveryDetailsArea)).setVisibility(View.VISIBLE);
            (findViewById(R.id.viewDeliveryDetailsArea)).setOnClickListener(new setOnClickList());
        }


        boolean FareDetailsArrNew = generalFunc.isJSONkeyAvail("HistoryFareDetailsNewArr", tripData);

        JSONArray FareDetailsArrNewObj = null;
        if (FareDetailsArrNew) {
            FareDetailsArrNewObj = generalFunc.getJsonArray("HistoryFareDetailsNewArr", tripData);
        }
        if (FareDetailsArrNewObj != null)
            addFareDetailLayout(FareDetailsArrNewObj);

        subTitleTxt.setVisibility(View.GONE);
    }

    /*Start of set delivery details*/
    public void getTripDeliveryLocations(String tripData) {

        final HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "getTripDeliveryDetails");
        parameters.put("iTripId", generalFunc.getJsonValue("iTripId", tripData));
        parameters.put("iCabBookingId", "");
        parameters.put("userType", Utils.app_type);

        final ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(this, parameters);
        exeWebServer.setDataResponseListener(responseString -> {
            JSONObject responseStringObject = generalFunc.getJsonObject(responseString);

            if (responseStringObject != null && !responseStringObject.equals("")) {

                String msg_str = generalFunc.getJsonValueStr(Utils.message_str, responseStringObject);

                if (GeneralFunctions.checkDataAvail(Utils.action_str, responseStringObject)) {

                    String paymentDoneByDetail = generalFunc.getJsonValueStr("PaymentPerson", responseStringObject);

                    if (Utils.checkText(paymentDoneByDetail)) {
                        if (generalFunc.getJsonValue("ePayWallet", tripData).equals("Yes")) {
                            ((MTextView) findViewById(R.id.paymentTypeTxt)).setText(generalFunc.retrieveLangLBl("Paid By Wallet", "LBL_PAY_BY_WALLET_TXT"));
                            ((ImageView) findViewById(R.id.paymentTypeImgeView)).setImageResource(R.mipmap.ic_menu_wallet);
                        }

                        ((MTextView) findViewById(R.id.paymentTypeTxt)).append(" " + generalFunc.retrieveLangLBl("Paid By", "LBL_PAID_BY_TXT") + " " + paymentDoneByDetail);

                    }

                    if (Utils.checkText(msg_str)) {
                        JSONObject jobject = generalFunc.getJsonObject("MemberDetails", msg_str);

                        if (jobject != null) {
                            senderImage = generalFunc.getJsonValue("Sender_Signature", jobject.toString());
                        }

                        JSONArray tripLocations = generalFunc.getJsonArray("Deliveries", msg_str);
                        if (tripLocations != null) {

                            String LBL_RECIPIENT = "", LBL_Status = "", LBL_CANCELED_TRIP_TXT = "", LBL_FINISHED_TXT = "", LBL_DROP_OFF_LOCATION_TXT = "", LBL_MULTI_AMOUNT_COLLECT_TXT = "", LBL_PICK_UP_INS = "", LBL_DELIVERY_INS = "", LBL_PACKAGE_DETAILS = "", LBL_CALL_TXT = "", LBL_VIEW_SIGN_TXT = "", LBL_MESSAGE_ACTIVE_TRIP = "", LBL_RESPONSIBLE_FOR_PAYMENT_TXT = "", LBL_DELIVERY_STATUS_TXT = "";

                            if (tripLocations.length() > 0) {
                                LBL_RECIPIENT = generalFunc.retrieveLangLBl("", "LBL_RECIPIENT");
                                LBL_Status = generalFunc.retrieveLangLBl("", "LBL_Status");
                                LBL_CANCELED_TRIP_TXT = generalFunc.retrieveLangLBl("", "LBL_CANCELED_TRIP_TXT");
                                LBL_FINISHED_TXT = generalFunc.retrieveLangLBl("", "LBL_FINISHED_TXT");
                                LBL_DROP_OFF_LOCATION_TXT = generalFunc.retrieveLangLBl("", "LBL_DROP_OFF_LOCATION_TXT");
                                LBL_MULTI_AMOUNT_COLLECT_TXT = generalFunc.retrieveLangLBl("", "LBL_MULTI_AMOUNT_COLLECT_TXT");
                                LBL_PICK_UP_INS = generalFunc.retrieveLangLBl("", "LBL_PICK_UP_INS");
                                LBL_DELIVERY_INS = generalFunc.retrieveLangLBl("", "LBL_DELIVERY_INS");
                                LBL_PACKAGE_DETAILS = generalFunc.retrieveLangLBl("", "LBL_PACKAGE_DETAILS");
                                LBL_CALL_TXT = generalFunc.retrieveLangLBl("", "LBL_CALL_TXT");
                                LBL_VIEW_SIGN_TXT = generalFunc.retrieveLangLBl("", "LBL_VIEW_SIGN_TXT");
                                LBL_MESSAGE_ACTIVE_TRIP = generalFunc.retrieveLangLBl("", LBL_MESSAGE_ACTIVE_TRIP);
                                LBL_RESPONSIBLE_FOR_PAYMENT_TXT = generalFunc.retrieveLangLBl("Responsible for payment", "LBL_RESPONSIBLE_FOR_PAYMENT_TXT");
                                LBL_DELIVERY_STATUS_TXT = generalFunc.retrieveLangLBl("", LBL_DELIVERY_STATUS_TXT);
                            }


                            for (int i = 0; i < tripLocations.length(); i++) {
                                Trip_Status recipientDetailMap1 = new Trip_Status();
                                JSONArray jsonArray1 = generalFunc.getJsonArray(tripLocations, i);

                                ArrayList<Delivery_Data> subrecipientDetailList = new ArrayList<>();

                                for (int j = 0; j < jsonArray1.length(); j++) {
                                    JSONObject jobject1 = generalFunc.getJsonObject(jsonArray1, j);
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

                                        recipientDetailMap1.setReceipent_Signature(generalFunc.getJsonValueStr("Receipent_Signature", jobject1));

                                        recipientDetailMap1.setiTripDeliveryLocationId(generalFunc.getJsonValueStr("iTripDeliveryLocationId", jobject1));


                                        recipientDetailMap.setvValue(generalFunc.getJsonValueStr("tDaddress", jobject1));
                                        recipientDetailMap.setiTripDeliveryLocationId(AppFunctions.fromHtml(generalFunc.getJsonValue("iTripDeliveryLocationId", jobject1.toString())).toString());
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

                                    if ((!vFieldName.equalsIgnoreCase("Mobile Number") && !(generalFunc.getJsonValueStr("iDeliveryFieldId", jobject1).equalsIgnoreCase("3"))) && (!vFieldName.equalsIgnoreCase("Recepient Name") && !(generalFunc.getJsonValueStr("iDeliveryFieldId", jobject1).equalsIgnoreCase("2")))/*&& Utils.checkText(generalFunc.getJsonValue("vValue", jobject1))*/) {
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

                                recipientDetailMap1.setLBL_Status(LBL_Status);
                                recipientDetailMap1.setLBL_CANCELED_TRIP_TXT(LBL_CANCELED_TRIP_TXT);
                                recipientDetailMap1.setLBL_FINISHED_TRIP_TXT(LBL_FINISHED_TXT);
                                recipientDetailMap1.setLBL_DROP_OFF_LOCATION_TXT(LBL_DROP_OFF_LOCATION_TXT);
                                recipientDetailMap1.setLBL_MULTI_AMOUNT_COLLECT_TXT(LBL_MULTI_AMOUNT_COLLECT_TXT);
                                recipientDetailMap1.setLBL_PACKAGE_DETAILS(LBL_PICK_UP_INS);
                                recipientDetailMap1.setLBL_DELIVERY_INS(LBL_DELIVERY_INS);
                                recipientDetailMap1.setLBL_PACKAGE_DETAILS(LBL_PACKAGE_DETAILS);
                                recipientDetailMap1.setLBL_CALL_TXT(LBL_CALL_TXT);
                                recipientDetailMap1.setLBL_VIEW_SIGN_TXT(LBL_VIEW_SIGN_TXT);
                                recipientDetailMap1.setLBL_MESSAGE_ACTIVE_TRIP(LBL_MESSAGE_ACTIVE_TRIP);
                                recipientDetailMap1.setLBL_RESPONSIBLE_FOR_PAYMENT_TXT(LBL_RESPONSIBLE_FOR_PAYMENT_TXT);
                                recipientDetailMap1.setLBL_DELIVERY_STATUS_TXT(LBL_DELIVERY_STATUS_TXT);
                                recipientDetailMap1.setListOfDeliveryItems(subrecipientDetailList);
                                recipientDetailList.add(recipientDetailMap1);

                                setRecyclerView();
                            }
                        }
                    } else {
                        generalFunc.showGeneralMessage(generalFunc.retrieveLangLBl("Error", "LBL_ERROR_TXT"),
                                generalFunc.retrieveLangLBl("", msg_str));

                    }
                }
            }
        });
        exeWebServer.execute();

    }

    private void setRecyclerView() {

        if (((LinearLayout) findViewById(R.id.deliveryArea)).getChildCount() > 0) {
            ((LinearLayout) findViewById(R.id.deliveryArea)).removeAllViewsInLayout();
        }

        if (Utils.checkText(senderImage)) {
            findViewById(R.id.signArea).setVisibility(View.VISIBLE);
            findViewById(R.id.signArea).setOnClickListener(new setOnClickList());
        }

        for (int i = 0; i < recipientDetailList.size(); i++) {

            LayoutInflater infalInflater = (LayoutInflater) getActContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.multi_history_recipient_list_design, null);

            MTextView recipientNoTxt, recipeientMobileTxt, recipeientNameTxt, dropOffVTxt;
            MTextView dropOffHTxt, ricipientSignTxt, tripStatusLblTxt, tripStatusTxt;
            RecyclerView deliveryDetailsList;
            LinearLayout deliveryDetailsArea;
            View line;

            recipientNoTxt = (MTextView) convertView.findViewById(R.id.recipientNoTxt);
            recipeientNameTxt = (MTextView) convertView.findViewById(R.id.recipeientNameTxt);
            recipeientMobileTxt = (MTextView) convertView.findViewById(R.id.recipeientMobileTxt);
            dropOffVTxt = (MTextView) convertView.findViewById(R.id.dropOffVTxt);
            dropOffHTxt = (MTextView) convertView.findViewById(R.id.dropOffHTxt);
            ricipientSignTxt = (MTextView) convertView.findViewById(R.id.ricipientSignTxt);
            deliveryDetailsList = (RecyclerView) convertView.findViewById(R.id.deliveryDetailsList);
            tripStatusLblTxt = (MTextView) convertView.findViewById(R.id.tripStatusLblTxt);
            tripStatusTxt = (MTextView) convertView.findViewById(R.id.tripStatusTxt);
            deliveryDetailsArea = (LinearLayout) convertView.findViewById(R.id.deliveryDetailsArea);
            line = (View) convertView.findViewById(R.id.line);

            final Trip_Status item = recipientDetailList.get(i);

            if (i == recipientDetailList.size() - 1) {
                line.setVisibility(View.GONE);
            } else {
                line.setVisibility(View.VISIBLE);
            }

            recipientNoTxt.setText("" + item.getLBL_RECIPIENT() + " " + (i + 1));
            recipeientNameTxt.setText(item.getRecepientName());
            recipeientMobileTxt.setText(item.getRecepientMaskNum());

            tripStatusLblTxt.setText(item.getLBL_DELIVERY_STATUS_TXT());
            tripStatusTxt.setText(item.getiActive());
            ricipientSignTxt.setText(item.getLBL_VIEW_SIGN_TXT());
            ricipientSignTxt.setTag(i);
            if (Utils.checkText(item.getReceipent_Signature())) {
                ricipientSignTxt.setVisibility(View.VISIBLE);
            } else {
                ricipientSignTxt.setVisibility(View.GONE);
            }

            ricipientSignTxt.setOnClickListener(view -> {
                showSignatureImage(generalFunc.retrieveLangLBl("", "LBL_RECIPIENT_NAME_HEADER_TXT") + " : " + recipientDetailList.get((int) view.getTag()).getRecepientName(), item.getReceipent_Signature(), false);
            });

            dropOffHTxt.setText(item.getLBL_DROP_OFF_LOCATION_TXT());
            dropOffVTxt.setText(item.getRecepientAddress());

            if (deliveryDetailsArea.getChildCount() <= 0) {

                ArrayList<Delivery_Data> listOfData = recipientDetailList.get(i).getListOfDeliveryItems();
                for (int i1 = 0; i1 < listOfData.size(); i1++) {
                    if (listOfData.get(i1).getvFieldName().equalsIgnoreCase("Address")) {
                        dropOffHTxt.setText(item.getLBL_DROP_OFF_LOCATION_TXT());
                        dropOffVTxt.setText(listOfData.get(i1).gettDaddress());

                    } else {
                        setdeliveriesDetails(listOfData.get(i1).getvFieldName(), listOfData.get(i1).getvValue(), i, i1, listOfData.size(), deliveryDetailsArea);
                    }
                }
            }


            if (convertView != null)
                ((LinearLayout) findViewById(R.id.deliveryArea)).addView(convertView);

        }

        ((LinearLayout) findViewById(R.id.deliveryArea)).setVisibility(View.VISIBLE);

        findViewById(R.id.deliveryItemListRecycleview).setVisibility(View.GONE);

    }

    private void setdeliveriesDetails(String vFieldName, String vValue, int checkItemPos, int listSize, int noRecipient, LinearLayout deliveryDetailsArea) {
        LayoutInflater infalInflater = (LayoutInflater) getActContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = infalInflater.inflate(R.layout.multi_delivery_details_design, null);

        MTextView itemTitleTxt, itemValueTxt;
        LinearLayout itemDetailArea;
        View bottomLine;

        itemTitleTxt = (MTextView) v.findViewById(R.id.itemTitleTxt);
        itemDetailArea = (LinearLayout) v.findViewById(R.id.itemDetailArea);
        itemValueTxt = (MTextView) v.findViewById(R.id.itemValueTxt);
        bottomLine = (View) v.findViewById(R.id.bottomLine);

        itemDetailArea.setTag(noRecipient); // This line is important.
        itemTitleTxt.setText(vFieldName);
        itemValueTxt.setText(Utils.checkText(vValue) ? vValue : "--");

        if (noRecipient == listSize - 1) {
            bottomLine.setVisibility(View.GONE);
        } else {
            bottomLine.setVisibility(View.GONE);
        }

        deliveryDetailsArea.addView(v);
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

    /*End of set delivery details*/

    public void displayPic(String image_url, ImageView view, final String imgType) {

        Picasso.get()
                .load(image_url)
                .placeholder(R.mipmap.ic_no_icon)
                .into(view, new com.squareup.picasso.Callback() {
                    @Override
                    public void onSuccess() {
                        if (imgType.equalsIgnoreCase("before")) {
                            findViewById(R.id.before_loading).setVisibility(View.GONE);
                            findViewById(R.id.iv_before_img).setVisibility(View.VISIBLE);
                        } else if (imgType.equalsIgnoreCase("after")) {
                            findViewById(R.id.after_loading).setVisibility(View.GONE);
                            findViewById(R.id.iv_after_img).setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onError(Exception e) {
                        if (imgType.equalsIgnoreCase("before")) {
                            findViewById(R.id.before_loading).setVisibility(View.VISIBLE);
                            findViewById(R.id.iv_before_img).setVisibility(View.GONE);
                        } else if (imgType.equalsIgnoreCase("after")) {
                            findViewById(R.id.after_loading).setVisibility(View.VISIBLE);
                            findViewById(R.id.iv_after_img).setVisibility(View.GONE);
                        }
                    }
                });

    }

    private void addFareDetailLayout(JSONArray jobjArray) {

        if (fareDetailDisplayArea.getChildCount() > 0) {
            fareDetailDisplayArea.removeAllViewsInLayout();
        }

        for (int i = 0; i < jobjArray.length(); i++) {
            JSONObject jobject = generalFunc.getJsonObject(jobjArray, i);
            try {
                String data = jobject.names().getString(0);

                addFareDetailRow(data, jobject.get(data).toString(), jobjArray.length() - 1 == i ? true : false);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }

    private void addFareDetailRow(String row_name, String row_value, boolean isLast) {
        View convertView = null;
        if (row_name.equalsIgnoreCase("eDisplaySeperator")) {
            convertView = new View(getActContext());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Utils.dipToPixels(getActContext(), 1));
            params.setMargins(0, 0, 0, (int) getResources().getDimension(R.dimen._5sdp));
            convertView.setBackgroundColor(Color.parseColor("#dedede"));
            convertView.setLayoutParams(params);
        } else {
            LayoutInflater infalInflater = (LayoutInflater) getActContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.design_fare_deatil_row, null);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.setMargins(0, (int) getResources().getDimension(R.dimen._10sdp), 0, isLast ? (int) getResources().getDimension(R.dimen._10sdp) : 0);
            convertView.setLayoutParams(params);

            MTextView titleHTxt = (MTextView) convertView.findViewById(R.id.titleHTxt);
            MTextView titleVTxt = (MTextView) convertView.findViewById(R.id.titleVTxt);

            titleHTxt.setText(generalFunc.convertNumberWithRTL(row_name));
            titleVTxt.setText(generalFunc.convertNumberWithRTL(row_value));

            if (isLast) {
                // convertView.setMinimumHeight(Utils.dipToPixels(getActContext(), 40));

                // CALCULATE individual fare & show
                titleHTxt.setTextColor(getResources().getColor(R.color.black));
                titleHTxt.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
                Typeface face = Typeface.createFromAsset(getAssets(), "fonts/Poppins_SemiBold.ttf");
                titleHTxt.setTypeface(face);
                titleVTxt.setTypeface(face);
                titleVTxt.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
                titleVTxt.setTextColor(getResources().getColor(R.color.appThemeColor_1));

            }

            fareDetailDisplayArea.addView(convertView);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.gMap = googleMap;

//        String tripData = getIntent().getStringExtra("TripData");

        String tStartLat = generalFunc.getJsonValue("tStartLat", tripData);
        String tStartLong = generalFunc.getJsonValue("tStartLong", tripData);
        String tEndLat = generalFunc.getJsonValue("tEndLat", tripData);
        String tEndLong = generalFunc.getJsonValue("tEndLong", tripData);

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        Marker pickUpMarker = null;
        Marker destMarker = null;
        if (!tStartLat.equals("") && !tStartLat.equals("0.0") && !tStartLong.equals("") && !tStartLong.equals("0.0")) {
            LatLng pickUpLoc = new LatLng(GeneralFunctions.parseDoubleValue(0.0, tStartLat), GeneralFunctions.parseDoubleValue(0.0, tStartLong));
            MarkerOptions marker_opt = new MarkerOptions().position(pickUpLoc);
            marker_opt.icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_source_marker)).anchor(0.5f, 0.5f);
            pickUpMarker = this.gMap.addMarker(marker_opt);

            builder.include(pickUpLoc);

            gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pickUpLoc, Utils.defaultZomLevel));
        }

        if (generalFunc.getJsonValue("iActive", tripData).equals("Finished")) {
            if (!tEndLat.equals("") && !tEndLat.equals("0.0") && !tEndLong.equals("") && !tEndLong.equals("0.0")) {
                LatLng destLoc = new LatLng(GeneralFunctions.parseDoubleValue(0.0, tEndLat), GeneralFunctions.parseDoubleValue(0.0, tEndLong));
                MarkerOptions marker_opt = new MarkerOptions().position(destLoc);
                marker_opt.icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_dest_marker)).anchor(0.5f, 0.5f);
                destMarker = this.gMap.addMarker(marker_opt);

                builder.include(destLoc);

                gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(destLoc, 10));
            }
        }
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int width = metrics.widthPixels;

        gMap.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), width, Utils.dipToPixels(getActContext(), 200), 100));
        gMap.setOnMarkerClickListener(marker -> {

            marker.hideInfoWindow();
            return true;
        });

        if (pickUpMarker != null && destMarker != null) {
            drawRoute(pickUpMarker.getPosition(), destMarker.getPosition());
        }

    }

    public void drawRoute(LatLng pickUpLoc, LatLng destinationLoc) {
        HashMap<String, String> hashMap = new HashMap<>();


        // String originLoc = pickUpLoc.latitude + "," + pickUpLoc.longitude;
        // String destLoc = destinationLoc.latitude + "," + destinationLoc.longitude;
        // String serverKey = data.get(Utils.GOOGLE_SERVER_ANDROID_DRIVER_APP_KEY);

        hashMap.put("s_latitude", pickUpLoc.latitude + "");
        hashMap.put("s_longitude", pickUpLoc.longitude + "");
        hashMap.put("d_latitude", destinationLoc.latitude + "");
        hashMap.put("d_longitude", destinationLoc.longitude + "");

        MapServiceApi.getDirectionservice(getActContext(), hashMap, this,false);
//        String url = "https://maps.googleapis.com/maps/api/directions/json?origin=" + originLoc + "&destination=" + destLoc + "&sensor=true&key=" + serverKey + "&language=" + data.get(Utils.GOOGLE_MAP_LANGUAGE_CODE_KEY) + "&sensor=true";
//
//        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(), url, true);
//
//        exeWebServer.setDataResponseListener(responseString -> {
//
//            if (responseString != null && !responseString.equals("")) {
//
//                String status = generalFunc.getJsonValue("status", responseString);
//
//                if (status.equals("OK")) {
//
//                    JSONArray obj_routes = generalFunc.getJsonArray("routes", responseString);
//                    if (obj_routes != null && obj_routes.length() > 0) {
//
//                        PolylineOptions lineOptions = generalFunc.getGoogleRouteOptions(responseString, Utils.dipToPixels(getActContext(), 5), getActContext().getResources().getColor(R.color.black));
//
//                        if (lineOptions != null) {
//                            gMap.addPolyline(lineOptions);
//                        }
//                    }
//
//                }
//
//            }
//        });
//        exeWebServer.execute();
    }

    public void sendReceipt() {

        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "getReceipt");
        parameters.put("UserType", Utils.app_type);
        parameters.put("iTripId", generalFunc.getJsonValue("iTripId", tripData));

        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(), parameters);
        exeWebServer.setLoaderConfig(getActContext(), true, generalFunc);
        exeWebServer.setDataResponseListener(responseString -> {
            JSONObject responseStringObj = generalFunc.getJsonObject(responseString);

            if (responseStringObj != null && !responseStringObj.equals("")) {

                boolean isDataAvail = GeneralFunctions.checkDataAvail(Utils.action_str, responseStringObj);

                if (isDataAvail) {
                    generalFunc.showGeneralMessage("",
                            generalFunc.retrieveLangLBl("", generalFunc.getJsonValueStr(Utils.message_str, responseStringObj)));
                } else {
                    generalFunc.showGeneralMessage("",
                            generalFunc.retrieveLangLBl("", generalFunc.getJsonValueStr(Utils.message_str, responseStringObj)));
                }
            } else {
                generalFunc.showError();
            }
        });
        exeWebServer.execute();
    }

    public Context getActContext() {
        return RideHistoryDetailActivity.this;
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

        if (responseString != null) {


            JSONArray obj_routes = generalFunc.getJsonArray(responseString);
            if (obj_routes != null && obj_routes.length() > 0) {

                PolylineOptions lineOptions = generalFunc.getGoogleRouteOptions(responseString, Utils.dipToPixels(getActContext(), 5), getActContext().getResources().getColor(R.color.black));

                if (lineOptions != null) {
                    gMap.addPolyline(lineOptions);
                }
            }


        }
    }

    @Override
    public void geoCodeAddressFound(String address, double latitude, double longitude, String geocodeobject) {

    }

    public class setOnClickList implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            Utils.hideKeyboard(RideHistoryDetailActivity.this);
            switch (view.getId()) {
                case R.id.backImgView:
                    RideHistoryDetailActivity.super.onBackPressed();
                    break;
                case R.id.viewReqServicesTxtView:
                    Bundle bundle = new Bundle();
                    bundle.putString("iTripId", generalFunc.getJsonValue("iTripId", tripData));
                    new StartActProcess(getActContext()).startActWithData(MoreServiceInfoActivity.class, bundle);
                    break;
                case R.id.subTitleTxt:
                    sendReceipt();
                    break;
                case R.id.signArea:
                    showSignatureImage(generalFunc.getJsonValue("vName", tripData) + " " +
                            generalFunc.getJsonValue("vLastName", tripData), senderImage, true);
                    break;
                case R.id.viewDeliveryDetailsArea:
                    Bundle bn = new Bundle();
                    bn.putString("TripId", generalFunc.getJsonValue("iTripId", tripData));
                    bn.putString("Status", "showHistoryScreen");
                    new StartActProcess(getActContext()).startActWithData(ViewMultiDeliveryDetailsActivity.class, bn);
                    break;
            }
        }
    }
}
