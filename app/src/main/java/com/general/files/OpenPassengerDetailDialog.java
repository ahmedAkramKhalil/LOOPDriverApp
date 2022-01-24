package com.general.files;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.taxifgo.driver.ActiveTripActivity;
import com.taxifgo.driver.CallScreenActivity;
import com.taxifgo.driver.ChatActivity;
import com.taxifgo.driver.DriverArrivedActivity;
import com.taxifgo.driver.R;
import com.sinch.android.rtc.calling.Call;
import com.squareup.picasso.Picasso;
import com.utils.CommonUtilities;
import com.utils.Logger;
import com.utils.Utils;
import com.view.MTextView;
import com.view.SelectableRoundedImageView;
import com.view.simpleratingbar.SimpleRatingBar;

import org.json.JSONObject;

import java.util.HashMap;

public class OpenPassengerDetailDialog {

    Context mContext;
    HashMap<String, String> data_trip;
    GeneralFunctions generalFunc;

    androidx.appcompat.app.AlertDialog alertDialog;

    ProgressBar LoadingProgressBar;
    boolean isnotification;
    String vImage = "";
    String vName = "";

    public OpenPassengerDetailDialog(Context mContext, HashMap<String, String> data_trip, GeneralFunctions generalFunc, boolean isnotification) {
        this.mContext = mContext;
        this.data_trip = data_trip;
        this.generalFunc = generalFunc;
        this.isnotification = isnotification;

        show();
    }

    public void sinchCall() {
        if (MyApp.getInstance().getCurrentAct() != null && (MyApp.getInstance().getCurrentAct() instanceof DriverArrivedActivity ||
                MyApp.getInstance().getCurrentAct() instanceof ActiveTripActivity)) {
            String userProfileJsonObj = generalFunc.retrieveValue(Utils.USER_PROFILE_JSON);

            if (generalFunc.isCallPermissionGranted(false) == false) {
                generalFunc.isCallPermissionGranted(true);
            } else {
                HashMap<String, String> hashMap = new HashMap<>();
                hashMap.put("Id", generalFunc.getMemberId());

                hashMap.put("Name", generalFunc.getJsonValue("vName", userProfileJsonObj));
                hashMap.put("PImage", generalFunc.getJsonValue("vImage", userProfileJsonObj));
                hashMap.put("type", Utils.userType);


                if (MyApp.getInstance().getCurrentAct() instanceof DriverArrivedActivity) {
                    DriverArrivedActivity activity = (DriverArrivedActivity) MyApp.getInstance().getCurrentAct();
                    if (new AppFunctions(mContext).checkSinchInstance(activity.getSinchServiceInterface())) {

                        activity.getSinchServiceInterface().getSinchClient().setPushNotificationDisplayName(generalFunc.retrieveLangLBl("", "LBL_INCOMING_CALL"));
//                        Call call = activity.getSinchServiceInterface().callUser(Utils.CALLTOPASSENGER + "_" + data_trip.get("PassengerId"), hashMap);

                        Call call;
                        if (Utils.checkText(data_trip.get("iGcmRegId_U"))) {
                            Logger.d("SINCH_CALL", "" + 4);
                            call = activity.getSinchServiceInterface().callUser(Utils.CALLTOPASSENGER + "_" + data_trip.get("PassengerId"), hashMap);
                        } else {
                            Logger.d("SINCH_CALL", "" + 5);

                            call = activity.getSinchServiceInterface().callPhoneNumber(data_trip.get("vPhone_U"));
                        }

                        String callId = call.getCallId();

                        Intent callScreen = new Intent(mContext, CallScreenActivity.class);
                        callScreen.putExtra(SinchService.CALL_ID, callId);
                        callScreen.putExtra("vImage", vImage);
                        callScreen.putExtra("vName", vName);
                        mContext.startActivity(callScreen);
                    }
                } else {


                    ActiveTripActivity activity = (ActiveTripActivity) MyApp.getInstance().getCurrentAct();
                    if (new AppFunctions(mContext).checkSinchInstance(activity.getSinchServiceInterface())) {

                        activity.getSinchServiceInterface().getSinchClient().setPushNotificationDisplayName(generalFunc.retrieveLangLBl("", "LBL_INCOMING_CALL"));
//                        Call call = activity.getSinchServiceInterface().callUser(Utils.CALLTOPASSENGER + "_" + data_trip.get("PassengerId"), hashMap);


                        Call call;
                        if (Utils.checkText(data_trip.get("iGcmRegId_U"))) {
                            Logger.d("SINCH_CALL", "" + 4);
                            call = activity.getSinchServiceInterface().callUser(Utils.CALLTOPASSENGER + "_" + data_trip.get("PassengerId"), hashMap);
                        } else {
                            Logger.d("SINCH_CALL", "" + 5);

                            call = activity.getSinchServiceInterface().callPhoneNumber(data_trip.get("vPhone_U"));
                        }


                        String callId = call.getCallId();

                        Intent callScreen = new Intent(mContext, CallScreenActivity.class);
                        callScreen.putExtra(SinchService.CALL_ID, callId);
                        callScreen.putExtra("vImage", vImage);
                        callScreen.putExtra("vName", vName);
                        mContext.startActivity(callScreen);
                    }
                }


            }


        }
    }

    public void show() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(mContext);
        builder.setTitle("");

        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View dialogView = inflater.inflate(R.layout.design_passenger_detail_dialog, null);
        builder.setView(dialogView);

        LoadingProgressBar = ((ProgressBar) dialogView.findViewById(R.id.LoadingProgressBar));

        ((MTextView) dialogView.findViewById(R.id.rateTxt)).setText(generalFunc.convertNumberWithRTL(data_trip.get("PRating")));
        ((MTextView) dialogView.findViewById(R.id.nameTxt)).setText(data_trip.get("PName"));

        ImageView cancelUpload = dialogView.findViewById(R.id.cancelUpload);
        cancelUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (alertDialog != null) {
                    alertDialog.dismiss();
                }
            }
        });


        vName = data_trip.get("PName");

        String msg = "";
        if (data_trip.get("REQUEST_TYPE").equalsIgnoreCase(Utils.CabGeneralType_UberX)) {
            msg = generalFunc.retrieveLangLBl("", "LBL_USER_DETAIL");
        } else {
            msg = generalFunc.retrieveLangLBl("", "LBL_PASSENGER_DETAIL");
        }

        ((MTextView) dialogView.findViewById(R.id.passengerDTxt)).setText(msg);
        ((MTextView) dialogView.findViewById(R.id.callTxt)).setText(generalFunc.retrieveLangLBl("", "LBL_CALL_TXT"));
        ((MTextView) dialogView.findViewById(R.id.msgTxt)).setText(generalFunc.retrieveLangLBl("", "LBL_MESSAGE_TXT"));
        ((SimpleRatingBar) dialogView.findViewById(R.id.ratingBar)).setRating(generalFunc.parseFloatValue(0, data_trip.get("PRating")));
//        if (data_trip.containsKey("eBookingFrom") && Utils.checkText(data_trip.get("eBookingFrom")) && data_trip.get("eBookingFrom").equalsIgnoreCase(Utils.eSystem_Type_KIOSK))
//        {
//            (dialogView.findViewById(R.id.msgArea)).setVisibility(View.GONE);
//        }
//        else
//        {
//            (dialogView.findViewById(R.id.msgArea)).setVisibility(View.VISIBLE);
//        }
        String image_url = CommonUtilities.USER_PHOTO_PATH + data_trip.get("PassengerId") + "/"
                + data_trip.get("PPicName");


        if (!data_trip.get("PPicName").equals("")) {
            vImage = image_url;
        }

        Picasso.get()
                .load(image_url)
                .placeholder(R.mipmap.ic_no_pic_user)
                .error(R.mipmap.ic_no_pic_user)
                .into(((SelectableRoundedImageView) dialogView.findViewById(R.id.passengerImgView)));

        (dialogView.findViewById(R.id.callArea)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (alertDialog != null) {
                    alertDialog.dismiss();
                }
                ///getMaskNumber();
                String userprofileJson = generalFunc.retrieveValue(Utils.USER_PROFILE_JSON);
                if (generalFunc.getJsonValue("RIDE_DRIVER_CALLING_METHOD", userprofileJson).equals("Voip") && !data_trip.get("eBookingFrom").equalsIgnoreCase("Kiosk")) {
                    sinchCall();
                } else {
                    getMaskNumber();
                }
            }
        });


        (dialogView.findViewById(R.id.msgArea)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (alertDialog != null) {
                    alertDialog.dismiss();
                }

                if (data_trip.get("eBookingFrom").equalsIgnoreCase("Kiosk")) {
                    Intent intent = new Intent(Intent.ACTION_SENDTO);
                    intent.setData(Uri.parse("smsto:" + Uri.encode(data_trip.get("PPhone"))));
                    mContext.startActivity(intent);
                    return;

                }

                Bundle bnChat = new Bundle();

                bnChat.putString("iFromMemberId", data_trip.get("PassengerId"));
                bnChat.putString("FromMemberImageName", data_trip.get("PPicName"));
                bnChat.putString("iTripId", data_trip.get("iTripId"));
                bnChat.putString("FromMemberName", data_trip.get("PName"));

                new StartActProcess(mContext).startActWithData(ChatActivity.class, bnChat);

            }
        });

        (dialogView.findViewById(R.id.closeImg)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (alertDialog != null) {
                    alertDialog.dismiss();
                }
            }
        });


        alertDialog = builder.create();
        if (generalFunc.isRTLmode() == true) {
            generalFunc.forceRTLIfSupported(alertDialog);
        }
        alertDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        alertDialog.show();
        if (isnotification) {
            isnotification = false;
            dialogView.findViewById(R.id.msgArea).performClick();
        }
    }

    public void getMaskNumber() {

        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "getCallMaskNumber");
        parameters.put("iTripid", data_trip.get("iTripId"));
        parameters.put("UserType", Utils.userType);
        parameters.put("iMemberId", generalFunc.getMemberId());

        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(mContext, parameters);
        exeWebServer.setLoaderConfig(mContext, true, generalFunc);

        exeWebServer.setDataResponseListener(responseString -> {
            JSONObject responseStringObject = generalFunc.getJsonObject(responseString);
            if (responseStringObject != null && !responseStringObject.equals("")) {

                boolean isDataAvail = GeneralFunctions.checkDataAvail(Utils.action_str, responseStringObject);

                if (isDataAvail == true) {
                    String message = generalFunc.getJsonValueStr(Utils.message_str, responseStringObject);
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
            // callIntent.setData(Uri.parse("tel:" + data_trip.get("PPhoneC") + "" + data_trip.get("PPhone")));
            callIntent.setData(Uri.parse("tel:" + phoneNumber));
            mContext.startActivity(callIntent);

        } catch (Exception e) {
        }
    }

}
