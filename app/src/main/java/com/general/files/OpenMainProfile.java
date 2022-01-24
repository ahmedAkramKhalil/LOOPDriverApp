package com.general.files;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import androidx.core.app.ActivityCompat;

import com.taxifgo.driver.AccountverificationActivity;
import com.taxifgo.driver.ActiveTripActivity;
import com.taxifgo.driver.AdditionalChargeActivity;
import com.taxifgo.driver.CollectPaymentActivity;
import com.taxifgo.driver.DriverArrivedActivity;
import com.taxifgo.driver.MainActivity;
import com.taxifgo.driver.SuspendedDriver_Activity;
import com.taxifgo.driver.TripRatingActivity;
import com.taxifgo.driver.ViewMultiDeliveryDetailsActivity;
import com.taxifgo.driver.deliverAll.LiveTaskListActivity;
import com.utils.AnimateMarker;
import com.utils.Utils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Admin on 29-06-2016.
 */
public class OpenMainProfile {
    private final JSONObject userProfileJsonObj;
    Context mContext;
    String responseString;
    boolean isCloseOnError;
    GeneralFunctions generalFun;
    boolean isnotification = false;
    AnimateMarker animateMarker;

    public OpenMainProfile(Context mContext, String responseString, boolean isCloseOnError, GeneralFunctions generalFun) {
        this.mContext = mContext;
        //this.responseString = responseString;
        this.isCloseOnError = isCloseOnError;
        this.generalFun = generalFun;

        this.responseString = generalFun.retrieveValue(Utils.USER_PROFILE_JSON);

        userProfileJsonObj = generalFun.getJsonObject(this.responseString);
        animateMarker = new AnimateMarker();

    }

    public OpenMainProfile(Context mContext, String responseString, boolean isCloseOnError, GeneralFunctions generalFun, boolean isnotification) {
        this.mContext = mContext;
        //this.responseString = responseString;
        this.isCloseOnError = isCloseOnError;
        this.generalFun = generalFun;
        this.isnotification = isnotification;

        this.responseString = generalFun.retrieveValue(Utils.USER_PROFILE_JSON);

        userProfileJsonObj = generalFun.getJsonObject(this.responseString);
        animateMarker = new AnimateMarker();

//        HashMap<String, String> storeData = new HashMap<>();
//        storeData.put(Utils.DefaultCountry, generalFun.getJsonValueStr("vDefaultCountry", userProfileJsonObj));
//        storeData.put(Utils.DefaultCountryCode, generalFun.getJsonValueStr("vDefaultCountryCode", userProfileJsonObj));
//        storeData.put(Utils.DefaultPhoneCode, generalFun.getJsonValueStr("vDefaultPhoneCode", userProfileJsonObj));
//        storeData.put(Utils.DefaultCountryImage, generalFun.getJsonValueStr("vDefaultCountryImage", userProfileJsonObj));
//        generalFun.storeData(storeData);

    }

    public void startProcess() {
        generalFun.sendHeartBeat();

        // responseString = generalFun.retrieveValue(Utils.USER_PROFILE_JSON);
        setGeneralData();


        animateMarker.driverMarkerAnimFinished = true;


        Bundle bn = new Bundle();
        // bn.putString("USER_PROFILE_JSON", responseString);
        bn.putString("IsAppReStart", "true"); // flag for retrieving data to en route trip pages

        String vTripStatus = generalFun.getJsonValueStr("vTripStatus", userProfileJsonObj);

        boolean lastTripExist = false;
        String Ratings_From_Driver = "";

        if (generalFun.getJsonValueStr("eSystem", userProfileJsonObj).equalsIgnoreCase(Utils.eSystem_Type) || generalFun.isDeliverOnlyEnabled()) {
            Ratings_From_Driver = generalFun.getJsonValueStr("Ratings_From_Driver", userProfileJsonObj);

            String ratings_From_Driver_str = generalFun.getJsonValueStr("Ratings_From_Driver", userProfileJsonObj);

            if (ratings_From_Driver_str != null && Utils.checkText(ratings_From_Driver_str) && !ratings_From_Driver_str.equals("Done")) {
                lastTripExist = true;
            }

        } else {
            if (vTripStatus.contains("Not Active")) {

                String ratings_From_Driver_str = generalFun.getJsonValueStr("Ratings_From_Driver", userProfileJsonObj);

                if (!ratings_From_Driver_str.equals("Done")) {
                    lastTripExist = true;
                }
            }
        }

        if (generalFun.getJsonValue("vPhone", userProfileJsonObj).equals("") || generalFun.getJsonValue("vEmail", userProfileJsonObj).equals("")) {
            if (generalFun.getMemberId() != null && !generalFun.getMemberId().equals("")) {
                new StartActProcess(mContext).startActWithData(AccountverificationActivity.class, bn);
            }
        } else if (generalFun.getJsonValueStr("eSystem", userProfileJsonObj).equalsIgnoreCase(Utils.eSystem_Type) || generalFun.isDeliverOnlyEnabled()) {
            HashMap<String, String> map = setMapData();
            if (vTripStatus.contains("Finished") && lastTripExist == true) {
                bn.putSerializable("TRIP_DATA", map);
                // new StartActProcess(mContext).startActWithData(DeliverAllRatingActivity.class, bn);
                new StartActProcess(mContext).startActWithData(TripRatingActivity.class, bn);
            } else if (vTripStatus != null && !vTripStatus.equals("NONE") && !vTripStatus.equals("Cancelled")
                    && (vTripStatus.trim().equals("Active") || vTripStatus.contains("On Going Trip")
                    || vTripStatus.contains("Arrived") || lastTripExist == true)) {

                if (Utils.checkText(Ratings_From_Driver) && !Ratings_From_Driver.contains("Done") && lastTripExist == true) {
                    // Open rating page
                    bn.putSerializable("TRIP_DATA", map);
                    // new StartActProcess(mContext).startActWithData(DeliverAllRatingActivity.class, bn);
                    new StartActProcess(mContext).startActWithData(TripRatingActivity.class, bn);
                } else {

                    bn.putSerializable("TRIP_DATA", map);
                    bn.putBoolean("isnotification", isnotification);
                    new StartActProcess(mContext).startActWithData(LiveTaskListActivity.class, bn);
                }

            } else {

                String eStatus = generalFun.getJsonValueStr("eStatus", userProfileJsonObj);

                if (eStatus.equalsIgnoreCase("suspend")) {
                    new StartActProcess(mContext).startAct(SuspendedDriver_Activity.class);
                } else {
                    new StartActProcess(mContext).startActWithData(MainActivity.class, bn);

                }
            }

        } else if (vTripStatus != null && !vTripStatus.equals("NONE") && !vTripStatus.equals("Cancelled")
                && (vTripStatus.trim().equals("Active") || vTripStatus.contains("On Going Trip")
                || vTripStatus.contains("Arrived") || lastTripExist == true)) {

            HashMap<String, String> map = setMapData();
            JSONObject last_trip_data = generalFun.getJsonObject("TripDetails", userProfileJsonObj);

            if (generalFun.getJsonValueStr("eType", last_trip_data).equalsIgnoreCase(Utils.eType_Multi_Delivery)) {
                if (generalFun.getJsonValueStr("IS_OPEN_SIGN_VERIFY", last_trip_data).equalsIgnoreCase("Yes") && generalFun.getJsonValueStr("IS_OPEN_FOR_SENDER", last_trip_data).equalsIgnoreCase("Yes")) {
                    bn.putSerializable("TRIP_DATA", map);
                    map.put("vTripStatus", "Arrived");
                    bn.putSerializable("TripId", generalFun.getJsonValueStr("iTripId", last_trip_data));
                    bn.putString("CheckFor", "Sender");
                    new StartActProcess(mContext).startActWithData(ViewMultiDeliveryDetailsActivity.class, bn);
                    return;
                } else if (generalFun.getJsonValueStr("IS_OPEN_SIGN_VERIFY", last_trip_data).equalsIgnoreCase("Yes") && generalFun.getJsonValueStr("IS_OPEN_FOR_SENDER", last_trip_data).equalsIgnoreCase("No")) {
                    bn.putSerializable("TRIP_DATA", map);
                    map.put("vTripStatus", "EN_ROUTE");
                    bn.putSerializable("TripId", generalFun.getJsonValueStr("iTripId", last_trip_data));
                    bn.putString("CheckFor", "Receipent");
                    new StartActProcess(mContext).startActWithData(ViewMultiDeliveryDetailsActivity.class, bn);
                    return;
                }
            }


            if (vTripStatus.contains("Not Active") && lastTripExist == true) {
                // Open rating page
                bn.putSerializable("TRIP_DATA", map);

                String ePaymentCollect = generalFun.getJsonValueStr("ePaymentCollect", last_trip_data);
                String eBookingFrom = generalFun.getJsonValueStr("eBookingFrom", last_trip_data);
                if (ePaymentCollect.equals("No")) {
                    new StartActProcess(mContext).startActWithData(CollectPaymentActivity.class, bn);
                } else {
                    if (Utils.checkText(eBookingFrom) && eBookingFrom.equalsIgnoreCase(Utils.eSystem_Type_KIOSK)) {
                        new StartActProcess(mContext).startActWithData(MainActivity.class, bn);
                    } else {
                        new StartActProcess(mContext).startActWithData(TripRatingActivity.class, bn);
                    }
                }

            } else {

                if (vTripStatus.contains("Arrived")) {

                    //                    if (!generalFun.isLocationEnabled()) {
//                        generalFun.restartApp();
//                        return;
//
//                    }
                    // Open active trip page
                    map.put("vTripStatus", "Arrived");
                    bn.putSerializable("TRIP_DATA", map);
                    bn.putBoolean("isnotification", isnotification);

                    new StartActProcess(mContext).startActWithData(ActiveTripActivity.class, bn);
                } else if (!vTripStatus.contains("Arrived") && vTripStatus.contains("On Going Trip")) {
                    // Open active trip page

                    if (generalFun.getJsonValueStr("eType", last_trip_data).equalsIgnoreCase("UberX") &&
                            generalFun.getJsonValueStr("eServiceEnd", last_trip_data).equalsIgnoreCase("Yes") &&
                            generalFun.getJsonValueStr("eFareGenerated", last_trip_data).equalsIgnoreCase("No")) {
                        map.put("vTripStatus", "EN_ROUTE");
                        bn.putSerializable("TRIP_DATA", map);
                        bn.putBoolean("isnotification", isnotification);
                        new StartActProcess(mContext).startActWithData(AdditionalChargeActivity.class, bn);
                    } else {

                        map.put("vTripStatus", "EN_ROUTE");
                        bn.putSerializable("TRIP_DATA", map);
                        bn.putBoolean("isnotification", isnotification);
                        new StartActProcess(mContext).startActWithData(ActiveTripActivity.class, bn);
                    }


                } else if (!vTripStatus.contains("Arrived") && vTripStatus.contains("Active")) {
                    // Open cubejek arrived page

                    bn.putSerializable("TRIP_DATA", map);
                    bn.putBoolean("isnotification", isnotification);

                    new StartActProcess(mContext).startActWithData(DriverArrivedActivity.class, bn);
                }
            }

        } else {

            String eStatus = generalFun.getJsonValueStr("eStatus", userProfileJsonObj);

            if (eStatus.equalsIgnoreCase("suspend")) {
                new StartActProcess(mContext).startAct(SuspendedDriver_Activity.class);
            } else {
                new StartActProcess(mContext).startActWithData(MainActivity.class, bn);

            }

        }

        ActivityCompat.finishAffinity((Activity) mContext);
    }

    private HashMap<String, String> setMapData() {
        // String last_trip_data = generalFun.getJsonValue("TripDetails", userProfileJsonObj);
        JSONObject last_trip_data = generalFun.getJsonObject("TripDetails", userProfileJsonObj);
        // String passenger_data = generalFun.getJsonValue("PassengerDetails", userProfileJsonObj);
        JSONObject passenger_data = generalFun.getJsonObject("PassengerDetails", userProfileJsonObj);
        HashMap<String, String> map = new HashMap<>();

        map.put("TotalSeconds", generalFun.getJsonValueStr("TotalSeconds", userProfileJsonObj));
        map.put("TimeState", generalFun.getJsonValueStr("TimeState", userProfileJsonObj));
        map.put("iTripTimeId", generalFun.getJsonValueStr("iTripTimeId", userProfileJsonObj));

        map.put("Message", "CabRequested");
        map.put("sourceLatitude", generalFun.getJsonValueStr("tStartLat", last_trip_data));
        map.put("sourceLongitude", generalFun.getJsonValueStr("tStartLong", last_trip_data));
        map.put("eBookingFrom", generalFun.getJsonValueStr("eBookingFrom", last_trip_data));

        map.put("tSaddress", generalFun.getJsonValueStr("tSaddress", last_trip_data));
        map.put("eRental", generalFun.getJsonValueStr("eRental", last_trip_data));
        map.put("ePoolRide", generalFun.getJsonValueStr("ePoolRide", last_trip_data));
        map.put("eTransit", generalFun.getJsonValueStr("eTransit", last_trip_data));
        map.put("drivervName", generalFun.getJsonValue("vName", responseString));
        map.put("drivervLastName", generalFun.getJsonValue("vLastName", responseString));

        map.put("PassengerId", generalFun.getJsonValueStr("iUserId", last_trip_data));
        map.put("PName", generalFun.getJsonValueStr("vName", passenger_data));
        map.put("vLastName", generalFun.getJsonValueStr("vLastName", passenger_data));
        map.put("iGcmRegId_U", generalFun.getJsonValueStr("iGcmRegId", passenger_data));
        map.put("vPhone_U", generalFun.getJsonValueStr("vPhone", passenger_data));
        map.put("PPicName", generalFun.getJsonValueStr("vImgName", passenger_data));
        map.put("PFId", generalFun.getJsonValueStr("vFbId", passenger_data));
        map.put("PRating", generalFun.getJsonValueStr("vAvgRating", passenger_data));
        map.put("PPhone", generalFun.getJsonValueStr("vPhone", passenger_data));
        map.put("PPhoneC", generalFun.getJsonValueStr("vPhoneCode", passenger_data));
        map.put("PAppVersion", generalFun.getJsonValueStr("iAppVersion", passenger_data));
        map.put("eFly", generalFun.getJsonValueStr("eFly", last_trip_data));

        /*Deliver All Fields*/
        map.put("iOrderId", generalFun.getJsonValueStr("iOrderId", last_trip_data));
        map.put("LastOrderAmount", generalFun.getJsonValueStr("LastOrderAmount", userProfileJsonObj));
        map.put("LastOrderUserName", generalFun.getJsonValueStr("LastOrderUserName", userProfileJsonObj));
        map.put("LastOrderNo", generalFun.getJsonValueStr("LastOrderNo", userProfileJsonObj));
        map.put("LastOrderId", generalFun.getJsonValueStr("LastOrderId", userProfileJsonObj));


        map.put("TripId", generalFun.getJsonValueStr("iTripId", last_trip_data));
        map.put("DestLocLatitude", generalFun.getJsonValueStr("tEndLat", last_trip_data));
        map.put("DestLocLongitude", generalFun.getJsonValueStr("tEndLong", last_trip_data));
        map.put("DestLocAddress", generalFun.getJsonValueStr("tDaddress", last_trip_data));
        map.put("REQUEST_TYPE", generalFun.getJsonValueStr("eType", last_trip_data));
        map.put("eFareType", generalFun.getJsonValueStr("eFareType", last_trip_data));
        map.put("iTripId", generalFun.getJsonValueStr("iTripId", last_trip_data));
        map.put("fVisitFee", generalFun.getJsonValueStr("fVisitFee", last_trip_data));
        map.put("eHailTrip", generalFun.getJsonValueStr("eHailTrip", last_trip_data));
        map.put("iActive", generalFun.getJsonValueStr("iActive", last_trip_data));
        map.put("eTollSkipped", generalFun.getJsonValueStr("eTollSkipped", last_trip_data));

        map.put("vVehicleType", generalFun.getJsonValueStr("vVehicleType", last_trip_data));
        map.put("vVehicleType", generalFun.getJsonValueStr("eIconType", last_trip_data));
        map.put("eType", generalFun.getJsonValueStr("eType", last_trip_data));
        map.put("eBookingFrom", generalFun.getJsonValueStr("eBookingFrom", last_trip_data));

        map.put("eAfterUpload", generalFun.getJsonValueStr("eAfterUpload", last_trip_data));
        map.put("eBeforeUpload", generalFun.getJsonValueStr("eBeforeUpload", last_trip_data));

        /*Multi StopOver*/
        map.put("currentStopOverPoint", generalFun.getJsonValueStr("currentStopOverPoint", last_trip_data));
        map.put("totalStopOverPoint", generalFun.getJsonValueStr("totalStopOverPoint", last_trip_data));
        map.put("iStopId", generalFun.getJsonValueStr("iStopId", last_trip_data));
        /*Multi StopOver*/

        map.put("vDeliveryConfirmCode", generalFun.getJsonValueStr("vDeliveryConfirmCode", last_trip_data));
        map.put("SITE_TYPE", generalFun.getJsonValueStr("SITE_TYPE", userProfileJsonObj));

        if (generalFun.getJsonValueStr("APP_TYPE", userProfileJsonObj).equalsIgnoreCase(Utils.CabGeneralType_UberX)) {
            map.put("PPetName", generalFun.getJsonValue("PetName", generalFun.getJsonValueStr("PetDetails", last_trip_data)));
            map.put("PPetId", generalFun.getJsonValueStr("iUserPetId", last_trip_data));
            map.put("PPetTypeName", generalFun.getJsonValue("PetTypeName", generalFun.getJsonValueStr("PetDetails", last_trip_data)));
            map.put("tUserComment", generalFun.getJsonValueStr("tUserComment", last_trip_data));
        }

        // Multi Delivery Data
        map.put("Running_Delivery_Txt", generalFun.getJsonValueStr("Running_Delivery_Txt", last_trip_data));
        map.put("vReceiverName", generalFun.getJsonValueStr("vReceiverName", last_trip_data));
        map.put("vReceiverMobile", generalFun.getJsonValueStr("vReceiverMobile", last_trip_data));
        map.put("iTripDeliveryLocationId", generalFun.getJsonValueStr("iTripDeliveryLocationId", last_trip_data));
        map.put("ePaymentByReceiver", generalFun.getJsonValueStr("ePaymentByReceiverForDelivery", last_trip_data));
        map.put("vRideNo", generalFun.getJsonValueStr("vRideNo", last_trip_data));
        map.put("vTripPaymentMode", generalFun.getJsonValueStr("vTripPaymentMode", last_trip_data));
        map.put("ePayWallet", generalFun.getJsonValueStr("ePayWallet", last_trip_data));

        if (generalFun.getJsonValueStr("tUserComment", last_trip_data) != null && !generalFun.getJsonValueStr("tUserComment", last_trip_data).equalsIgnoreCase("")) {
            map.put("tUserComment", generalFun.getJsonValueStr("tUserComment", last_trip_data));
        }
        if (generalFun.getJsonValueStr("APP_TYPE", userProfileJsonObj).equalsIgnoreCase(Utils.CabGeneralTypeRide_Delivery_UberX)) {
            map.put("PPetName", generalFun.getJsonValue("PetName", generalFun.getJsonValueStr("PetDetails", last_trip_data)));
            map.put("PPetId", generalFun.getJsonValueStr("iUserPetId", last_trip_data));
            map.put("PPetTypeName", generalFun.getJsonValue("PetTypeName", generalFun.getJsonValueStr("PetDetails", last_trip_data)));
        }

        map.put("TotalFareUberX", generalFun.getJsonValueStr("TotalFareUberX", userProfileJsonObj));
        map.put("TotalFareUberXValue", generalFun.getJsonValueStr("TotalFareUberXValue", userProfileJsonObj));
        map.put("UberXFareCurrencySymbol", generalFun.getJsonValueStr("UberXFareCurrencySymbol", userProfileJsonObj));

        return map;

    }


    public void setGeneralData() {
        HashMap<String, String> storeData = new HashMap<>();
        ArrayList<String> removeData = new ArrayList<>();
        new SetGeneralData(generalFun, userProfileJsonObj);

//        storeData.put(Utils.SESSION_ID_KEY, generalFun.getJsonValueStr("tSessionId", userProfileJsonObj));
//        storeData.put(Utils.DEVICE_SESSION_ID_KEY, generalFun.getJsonValueStr("tDeviceSessionId", userProfileJsonObj));
//        storeData.put(Utils.PUBSUB_PUBLISH_DRIVER_LOC_DISTANCE_LIMIT, generalFun.getJsonValueStr("PUBSUB_PUBLISH_DRIVER_LOC_DISTANCE_LIMIT", userProfileJsonObj));
//
//        storeData.put(Utils.SMS_BODY_KEY, generalFun.getJsonValueStr(Utils.SMS_BODY_KEY, userProfileJsonObj));
//
//        storeData.put(Utils.FETCH_TRIP_STATUS_TIME_INTERVAL_KEY, generalFun.getJsonValueStr("FETCH_TRIP_STATUS_TIME_INTERVAL", userProfileJsonObj));
//
//        storeData.put(Utils.DELIVERALL_KEY, generalFun.getJsonValueStr(Utils.DELIVERALL_KEY, userProfileJsonObj));
//        storeData.put(Utils.ONLYDELIVERALL_KEY, generalFun.getJsonValueStr(Utils.ONLYDELIVERALL_KEY, userProfileJsonObj));
//
//        storeData.put(Utils.VERIFICATION_CODE_RESEND_TIME_IN_SECONDS_KEY, generalFun.getJsonValueStr(Utils.VERIFICATION_CODE_RESEND_TIME_IN_SECONDS_KEY, userProfileJsonObj));
//        storeData.put(Utils.VERIFICATION_CODE_RESEND_COUNT_KEY, generalFun.getJsonValueStr(Utils.VERIFICATION_CODE_RESEND_COUNT_KEY, userProfileJsonObj));
//        storeData.put(Utils.VERIFICATION_CODE_RESEND_COUNT_RESTRICTION_KEY, generalFun.getJsonValueStr(Utils.VERIFICATION_CODE_RESEND_COUNT_RESTRICTION_KEY, userProfileJsonObj));
//
//        storeData.put("DESTINATION_UPDATE_TIME_INTERVAL", generalFun.getJsonValueStr("DESTINATION_UPDATE_TIME_INTERVAL", userProfileJsonObj));
//        storeData.put("showCountryList", generalFun.getJsonValueStr("showCountryList", userProfileJsonObj));
//        storeData.put("ENABLE_EDIT_DRIVER_PROFILE", generalFun.getJsonValueStr("ENABLE_EDIT_DRIVER_PROFILE", userProfileJsonObj));
//
//        storeData.put(Utils.PUBNUB_PUB_KEY, generalFun.getJsonValueStr("PUBNUB_PUBLISH_KEY", userProfileJsonObj));
//        storeData.put(Utils.PUBNUB_SUB_KEY, generalFun.getJsonValueStr("PUBNUB_SUBSCRIBE_KEY", userProfileJsonObj));
//        storeData.put(Utils.PUBNUB_SEC_KEY, generalFun.getJsonValueStr("PUBNUB_SECRET_KEY", userProfileJsonObj));
//        storeData.put(Utils.SITE_TYPE_KEY, generalFun.getJsonValueStr("SITE_TYPE", userProfileJsonObj));
//
//        storeData.put(Utils.PUBSUB_TECHNIQUE, generalFun.getJsonValueStr("PUBSUB_TECHNIQUE", userProfileJsonObj));
//        storeData.put(Utils.SC_CONNECT_URL_KEY, generalFun.getJsonValueStr("SC_CONNECT_URL", userProfileJsonObj));
//        storeData.put(Utils.YALGAAR_CLIENT_KEY, generalFun.getJsonValueStr("YALGAAR_CLIENT_KEY", userProfileJsonObj));
//
//        storeData.put(Utils.MOBILE_VERIFICATION_ENABLE_KEY, generalFun.getJsonValueStr("MOBILE_VERIFICATION_ENABLE", userProfileJsonObj));
//        storeData.put("LOCATION_ACCURACY_METERS", generalFun.getJsonValueStr("LOCATION_ACCURACY_METERS", userProfileJsonObj));
//        storeData.put("DRIVER_LOC_UPDATE_TIME_INTERVAL", generalFun.getJsonValueStr("DRIVER_LOC_UPDATE_TIME_INTERVAL", userProfileJsonObj));
//        storeData.put("RIDER_REQUEST_ACCEPT_TIME", generalFun.getJsonValueStr("RIDER_REQUEST_ACCEPT_TIME", userProfileJsonObj));
//        storeData.put(Utils.PHOTO_UPLOAD_SERVICE_ENABLE_KEY, generalFun.getJsonValueStr(Utils.PHOTO_UPLOAD_SERVICE_ENABLE_KEY, userProfileJsonObj));
//
//        storeData.put(Utils.ENABLE_TOLL_COST, generalFun.getJsonValueStr("ENABLE_TOLL_COST", userProfileJsonObj));
//        storeData.put(Utils.TOLL_COST_APP_ID, generalFun.getJsonValueStr("TOLL_COST_APP_ID", userProfileJsonObj));
//        storeData.put(Utils.TOLL_COST_APP_CODE, generalFun.getJsonValueStr("TOLL_COST_APP_CODE", userProfileJsonObj));
//
//        storeData.put(Utils.WALLET_ENABLE, generalFun.getJsonValueStr("WALLET_ENABLE", userProfileJsonObj));
//        storeData.put(Utils.REFERRAL_SCHEME_ENABLE, generalFun.getJsonValueStr("REFERRAL_SCHEME_ENABLE", userProfileJsonObj));
//
//        storeData.put(Utils.APP_DESTINATION_MODE, generalFun.getJsonValueStr("APP_DESTINATION_MODE", userProfileJsonObj));
//        storeData.put(Utils.APP_TYPE, generalFun.getJsonValueStr("APP_TYPE", userProfileJsonObj));
//        storeData.put(Utils.HANDICAP_ACCESSIBILITY_OPTION, generalFun.getJsonValueStr("HANDICAP_ACCESSIBILITY_OPTION", userProfileJsonObj));
//        storeData.put(Utils.FEMALE_RIDE_REQ_ENABLE, generalFun.getJsonValueStr("FEMALE_RIDE_REQ_ENABLE", userProfileJsonObj));
//        storeData.put(Utils.GOOGLE_SERVER_ANDROID_DRIVER_APP_KEY, generalFun.getJsonValueStr("GOOGLE_SERVER_ANDROID_DRIVER_APP_KEY", userProfileJsonObj));
//
//        storeData.put(Utils.SINCH_APP_KEY, generalFun.getJsonValueStr(Utils.SINCH_APP_KEY, userProfileJsonObj));
//        storeData.put(Utils.SINCH_APP_SECRET_KEY, generalFun.getJsonValueStr(Utils.SINCH_APP_SECRET_KEY, userProfileJsonObj));
//        storeData.put(Utils.SINCH_APP_ENVIRONMENT_HOST, generalFun.getJsonValueStr(Utils.SINCH_APP_ENVIRONMENT_HOST, userProfileJsonObj));
//
//        // GoJek - go Pay
//        storeData.put(Utils.ENABLE_GOPAY_KEY, generalFun.getJsonValueStr(Utils.ENABLE_GOPAY_KEY, userProfileJsonObj));
//        storeData.put("UFX_SERVICE_AVAILABLE", generalFun.getJsonValueStr("UFX_SERVICE_AVAILABLE", userProfileJsonObj));
//
//        storeData.put(Utils.LINKDIN_LOGIN, generalFun.getJsonValue("LINKEDIN_LOGIN", responseString));
//        if (!generalFun.getJsonValueStr("vAvailability", userProfileJsonObj).equalsIgnoreCase("Available")) {
//            storeData.put(Utils.DRIVER_ONLINE_KEY, "false");
//        }
//
////        storeData.put()(Utils.ENABLE_DRIVER_DESTINATIONS_KEY, generalFun.getJsonValueStr(Utils.ENABLE_DRIVER_DESTINATIONS_KEY, userProfileJsonObj));
//        storeData.put(Utils.DRIVER_DESTINATION_AVAILABLE_KEY, generalFun.getJsonValueStr(Utils.DRIVER_DESTINATION_AVAILABLE_KEY, userProfileJsonObj));
//        storeData.put(Utils.DRIVER_SUBSCRIPTION_ENABLE_KEY, generalFun.getJsonValueStr(Utils.DRIVER_SUBSCRIPTION_ENABLE_KEY, userProfileJsonObj));

        removeData.add("userHomeLocationLatitude");
        removeData.add("userHomeLocationLongitude");
        removeData.add("userHomeLocationAddress");
        removeData.add("userWorkLocationLatitude");
        removeData.add("userWorkLocationLongitude");
        removeData.add("userWorkLocationAddress");
        generalFun.removeValue(removeData);

        JSONArray userFavouriteAddressArr = generalFun.getJsonArray("UserFavouriteAddress", responseString);
        if (userFavouriteAddressArr != null && userFavouriteAddressArr.length() > 0) {

            for (int i = 0; i < userFavouriteAddressArr.length(); i++) {
                JSONObject dataItem = generalFun.getJsonObject(userFavouriteAddressArr, i);

                if (generalFun.getJsonValueStr("eType", dataItem).equalsIgnoreCase("HOME")) {
                    storeData.put("userHomeLocationLatitude", generalFun.getJsonValueStr("vLatitude", dataItem));
                    storeData.put("userHomeLocationLongitude", generalFun.getJsonValueStr("vLongitude", dataItem));
                    storeData.put("userHomeLocationAddress", generalFun.getJsonValueStr("vAddress", dataItem));
                } else if (generalFun.getJsonValueStr("eType", dataItem).equalsIgnoreCase("WORK")) {
                    storeData.put("userWorkLocationLatitude", generalFun.getJsonValueStr("vLatitude", dataItem));
                    storeData.put("userWorkLocationLongitude", generalFun.getJsonValueStr("vLongitude", dataItem));
                    storeData.put("userWorkLocationAddress", generalFun.getJsonValueStr("vAddress", dataItem));
                }
            }
        }

        generalFun.storeData(storeData);

    }
}
