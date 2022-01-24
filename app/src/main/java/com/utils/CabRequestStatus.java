package com.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.general.files.ExecuteWebServerUrl;
import com.general.files.GeneralFunctions;

import java.util.HashMap;
import java.util.Map;

public class CabRequestStatus {
    public static String DRIVER_REQUEST_STATUS = "DRIVER_REQUEST_STATUS_";

    Context mContext;
    GeneralFunctions generalFunc;

    public CabRequestStatus(Context mContext) {
        this.mContext = mContext;
        generalFunc = new GeneralFunctions(mContext);
    }

    public void updateDriverRequestStatus(int repeatCount, String PassengerId, String UpdatedStatus, String ReceiverByscriptName, String vMsgCode) {

        /* store data*/
        String currentTime = System.currentTimeMillis() + "";
        String Data = DRIVER_REQUEST_STATUS + vMsgCode + "_" + currentTime + "_" + PassengerId + "_" + UpdatedStatus;
        String finalString = Data;
        if (!ReceiverByscriptName.equals("")) {
            finalString = Data + "_" + ReceiverByscriptName;
        }
        generalFunc.storeData(finalString, "Yes");

        /* store data finished*/

        if (repeatCount > 3) {
            return;
        }

        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "updateDriverRequestStatus");
        parameters.put("iDriverId", generalFunc.getMemberId());
        parameters.put("PassengerId", PassengerId);
        parameters.put("UpdatedStatus", UpdatedStatus);
        parameters.put("ReceiverByscriptName", ReceiverByscriptName);
        parameters.put("vMsgCode", vMsgCode);

        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(mContext, parameters);
        exeWebServer.setDataResponseListener(responseString -> {
            if (responseString != null && !responseString.equals("")) {

            } else {
                int rCount = repeatCount + 1;
                updateDriverRequestStatus(rCount, PassengerId, UpdatedStatus, ReceiverByscriptName, vMsgCode);
            }
        });
        exeWebServer.execute();
    }

    public void removeOldRequestsData() {
        try {
            SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
            Map<String, ?> keys = mPrefs.getAll();

            for (Map.Entry<String, ?> entry : keys.entrySet()) {
                if (entry.getKey().contains(DRIVER_REQUEST_STATUS)) {
                    generalFunc.removeValue(entry.getKey());
                }
            }
        } catch (Exception e) {

        }
    }

    public HashMap<String, String> getAllStatusParam() {
        HashMap<String, String> parameters = new HashMap<String, String>();
        try {
            SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
            Map<String, ?> keys = mPrefs.getAll();
            for (Map.Entry<String, ?> entry : keys.entrySet()) {
                if (entry.getKey().contains(CabRequestStatus.DRIVER_REQUEST_STATUS)) {
                    generalFunc.retrieveValue(entry.getKey());
                    String key = entry.getKey();
                    String value=generalFunc.retrieveValue(entry.getKey())+ "";

                    parameters.put(key, value);
                }
            }
        } catch (Exception e) {

        }

        return parameters;
    }

}
