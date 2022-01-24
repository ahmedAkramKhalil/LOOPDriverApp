package com.general.files;

import android.content.Context;
import android.location.Location;
import androidx.appcompat.app.AlertDialog;

import com.taxifgo.driver.ActiveTripActivity;
import com.utils.Utils;

import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created by Admin on 21-07-2016.
 */
public class CancelTripDialog {

    Context mContext;
    GeneralFunctions generalFunc;
    boolean isTripStart = false;
    HashMap<String, String> data_trip;
    androidx.appcompat.app.AlertDialog alertDialog;
    Location userLocation;

    public CancelTripDialog(Context mContext, HashMap<String, String> data_trip, GeneralFunctions generalFunc, boolean isTripStart) {
        this.mContext = mContext;
        this.generalFunc = generalFunc;
        this.data_trip = data_trip;
        this.isTripStart = isTripStart;
        //show();
    }

    public CancelTripDialog(Context mContext, HashMap<String, String> data_trip, GeneralFunctions generalFunc, String iCancelReasonId, String comment, boolean isTripStart, String reason) {
        this.mContext = mContext;
        this.generalFunc = generalFunc;
        this.data_trip = data_trip;

        this.isTripStart = isTripStart;

        if (isTripStart == false) {
            cancelTrip(iCancelReasonId, comment, reason);
        } else {
            ((ActiveTripActivity) mContext).cancelTrip(reason, comment);
        }

    }


    public CancelTripDialog(Context mContext, HashMap<String, String> data_trip, GeneralFunctions generalFunc, String iCancelReasonId, String comment, boolean isTripStart, String reason, Location userLocation) {
        this.mContext = mContext;
        this.generalFunc = generalFunc;
        this.userLocation = userLocation;
        this.data_trip = data_trip;

        this.isTripStart = isTripStart;

        if (isTripStart == false) {
            cancelTrip(iCancelReasonId, comment, reason);
        } else {
            ((ActiveTripActivity) mContext).cancelTrip(reason, comment);
        }

    }

//    public void show() {
//        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(mContext);
//
//        MTextView titleTxtView = new MTextView(mContext);
//
//        if (data_trip.get("REQUEST_TYPE").equals(Utils.CabGeneralType_Deliver)) {
//            titleTxtView.setText(generalFunc.retrieveLangLBl("Cancel Delivery", "LBL_CANCEL_DELIVERY"));
//        } else if (data_trip.get("REQUEST_TYPE").equalsIgnoreCase(Utils.CabGeneralType_UberX)) {
//            titleTxtView.setText(generalFunc.retrieveLangLBl("", "LBL_CANCEL_JOB"));
//        } else {
//            titleTxtView.setText(generalFunc.retrieveLangLBl("", "LBL_CANCEL_TRIP"));
//        }
//
//        titleTxtView.setPadding(Utils.dipToPixels(mContext, 15), Utils.dipToPixels(mContext, 15), Utils.dipToPixels(mContext, 15), Utils.dipToPixels(mContext, 15));
//        titleTxtView.setGravity(Gravity.CENTER);
//        titleTxtView.setTextColor(Color.parseColor("#1c1c1c"));
//        titleTxtView.setTextSize(22);
//
//        builder.setCustomTitle(titleTxtView);
//
//        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//        View dialogView = inflater.inflate(R.layout.input_box_view, null);
//
//
//        final MaterialEditText reasonBox = (MaterialEditText) dialogView.findViewById(R.id.editBox);
//        final MaterialEditText commentBox = (MaterialEditText) inflater.inflate(R.layout.editbox_form_design, null);
//        commentBox.setLayoutParams(reasonBox.getLayoutParams());
//        commentBox.setId(Utils.generateViewId());
//
//        commentBox.setSingleLine(false);
//        commentBox.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
//        commentBox.setGravity(Gravity.TOP);
//        commentBox.setFloatingLabel(MaterialEditText.FLOATING_LABEL_HIGHLIGHT);
//
//        commentBox.setBothText(generalFunc.retrieveLangLBl("", "LBL_COMMENT_TXT"), generalFunc.retrieveLangLBl("", "LBL_WRITE_COMMENT_HINT_TXT"));
//        reasonBox.setBothText(generalFunc.retrieveLangLBl("Reason", "LBL_REASON"), generalFunc.retrieveLangLBl("Enter your reason", "LBL_ENTER_REASON"));
//
//        ((LinearLayout) dialogView).addView(commentBox);
//
//
////        String posBtnMsg = "", negBtnMsg = "";
////        if (data_trip.get("REQUEST_TYPE").equals(Utils.CabGeneralType_Deliver)) {
////            posBtnMsg = generalFunc.retrieveLangLBl("", "LBL_CANCEL_DELIVERY_NOW");
////            negBtnMsg = generalFunc.retrieveLangLBl("", "LBL_CONTINUE_DELIVERY");
////        } else if (data_trip.get("REQUEST_TYPE").equals(Utils.CabGeneralType_UberX)) {
////            posBtnMsg = generalFunc.retrieveLangLBl("", "LBL_CANCEL_JOB_NOW");
////            negBtnMsg = generalFunc.retrieveLangLBl("", "LBL_CONTINUE_JOB");
////        } else {
////            posBtnMsg = generalFunc.retrieveLangLBl("", "LBL_CANCEL_TRIP_NOW");
////            negBtnMsg = generalFunc.retrieveLangLBl("", "LBL_CONTINUE_TRIP_TXT");
////        }
//        builder.setView(dialogView);
//        builder.setPositiveButton(generalFunc.retrieveLangLBl("","LBL_YES"), new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//
//            }
//        });
//        builder.setNegativeButton(generalFunc.retrieveLangLBl("","LBL_NO"), new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//            }
//        });
//
//        alertDialog = builder.create();
//        if (generalFunc.isRTLmode() == true) {
//            generalFunc.forceRTLIfSupported(alertDialog);
//        }
//        alertDialog.show();
////        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.parseColor("#1C1C1C"));
////        alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.parseColor("#909090"));
//        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//
//                if (Utils.checkText(reasonBox) == false) {
//                    reasonBox.setError(generalFunc.retrieveLangLBl("", "LBL_FEILD_REQUIRD_ERROR_TXT"));
//                    return;
//                }
//
//                alertDialog.dismiss();
//
//                if (isTripStart == false) {
//                    cancelTrip(Utils.getText(reasonBox), Utils.getText(commentBox));
//                } else {
//                    ((ActiveTripActivity) mContext).cancelTrip(Utils.getText(reasonBox), Utils.getText(commentBox));
//                }
//
//            }
//        });
//
//        alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                alertDialog.dismiss();
//            }
//        });
//    }

    public void cancelTrip(String iCancelReasonId, String comment, String reason) {
        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "cancelTrip");
        parameters.put("iDriverId", generalFunc.getMemberId());
        parameters.put("iUserId", data_trip.get("PassengerId"));
        parameters.put("iTripId", data_trip.get("TripId"));
        parameters.put("UserType", Utils.app_type);
        parameters.put("Reason", reason);
        parameters.put("Comment", comment);
        parameters.put("iCancelReasonId", iCancelReasonId);
        if (userLocation != null) {
            parameters.put("vLatitude", "" + userLocation.getLatitude());
            parameters.put("vLongitude", "" + userLocation.getLongitude());
        }

        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(mContext, parameters);
        exeWebServer.setLoaderConfig(mContext, true, generalFunc);
        exeWebServer.setDataResponseListener(responseString -> {
            JSONObject responseStringObject=generalFunc.getJsonObject(responseString);

            if (responseStringObject != null && !responseStringObject.equals("")) {

                boolean isDataAvail = GeneralFunctions.checkDataAvail(Utils.action_str, responseStringObject);

                if (isDataAvail == true) {
                    generalFunc.saveGoOnlineInfo();
                    // generalFunc.restartApp();
                    MyApp.getInstance().restartWithGetDataApp();
                } else {
                    generalFunc.showGeneralMessage("",
                            generalFunc.retrieveLangLBl("", generalFunc.getJsonValueStr(Utils.message_str, responseStringObject)));
                }
            } else {
                generalFunc.showError();
            }
        });
        exeWebServer.execute();
    }
}
