package com.general.files;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;

import com.taxifgo.driver.BuildConfig;
import com.rest.RestClient;
import com.utils.CommonUtilities;
import com.utils.DeviceData;
import com.utils.Logger;
import com.utils.Utils;
import com.view.MyProgressDialog;

import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Admin on 22-02-2016.
 */
public class ExecuteWebServerUrl /*extends AsyncTask<String, String, String>*/ {

    public static String CUSTOM_APP_TYPE = "";
    public static String DELIVERALL = "";
    public static String ONLYDELIVERALL = "";

    SetDataResponse setDataRes;

    HashMap<String, String> parameters;

    GeneralFunctions generalFunc;

    String responseString = "";

    boolean directUrl_value = false;
    String directUrl = "";

    boolean isLoaderShown = false;
    Context mContext;

    MyProgressDialog myPDialog;

    boolean isGenerateDeviceToken = false;
    String key_DeviceToken_param;
    InternetConnection intCheck;
    boolean isSetCancelable = true;

    boolean isTaskKilled = false;
    boolean ignoreDorestartHandling = false;

    Call<Object> currentCall;

    public ExecuteWebServerUrl(Context mContext, HashMap<String, String> parameters) {
        this.parameters = parameters;
        this.mContext = mContext;
    }

    public ExecuteWebServerUrl(Context mContext, HashMap<String, String> parameters,boolean ignoreDorestartHandling) {
        this.parameters = parameters;
        this.mContext = mContext;
        this.ignoreDorestartHandling = ignoreDorestartHandling;
    }

    public ExecuteWebServerUrl(Context mContext, String directUrl, boolean directUrl_value) {
        this.directUrl = directUrl;
        this.directUrl_value = directUrl_value;
        this.mContext = mContext;
    }

    public void setLoaderConfig(Context mContext, boolean isLoaderShown, GeneralFunctions generalFunc) {
        this.isLoaderShown = isLoaderShown;
        this.generalFunc = generalFunc;
        this.mContext = mContext;
    }


    public void setIsDeviceTokenGenerate(boolean isGenerateDeviceToken, String key_DeviceToken_param, GeneralFunctions generalFunc) {
        this.isGenerateDeviceToken = isGenerateDeviceToken;
        this.key_DeviceToken_param = key_DeviceToken_param;
        this.generalFunc = generalFunc;
    }

    public void setCancelAble(boolean isSetCancelable) {
        this.isSetCancelable = isSetCancelable;
    }

    public void execute() {
        Utils.runGC();
        intCheck = new InternetConnection(mContext);

        if (!intCheck.isNetworkConnected() && !intCheck.check_int()) {
            fireResponse();
            return;
        }

        if (isLoaderShown == true) {
            myPDialog = new MyProgressDialog(mContext, isSetCancelable, generalFunc.retrieveLangLBl("Loading", "LBL_LOADING_TXT"));
            isSetCancelable = true;
            try {
                myPDialog.show();
            } catch (Exception e) {

            }
        }

        if (parameters != null) {
            GeneralFunctions generalFunc = MyApp.getInstance().getAppLevelGeneralFunc();
            String userProfileJson = generalFunc.retrieveValue(Utils.USER_PROFILE_JSON);
            parameters.put("tSessionId", generalFunc.getMemberId().equals("") ? "" : generalFunc.retrieveValue(Utils.SESSION_ID_KEY));
            parameters.put("deviceHeight", Utils.getScreenPixelHeight(mContext) + "");
            parameters.put("deviceWidth", Utils.getScreenPixelWidth(mContext) + "");

            parameters.put("GeneralUserType", Utils.app_type);
            parameters.put("GeneralMemberId", generalFunc.getMemberId());
            parameters.put("GeneralDeviceType", "" + Utils.deviceType);
            parameters.put("GeneralAppVersion", BuildConfig.VERSION_NAME);
            parameters.put("GeneralAppVersionCode", "" + BuildConfig.VERSION_CODE);
            parameters.put("vTimeZone", generalFunc.getTimezone());
            parameters.put("vUserDeviceCountry", Utils.getUserDeviceCountryCode(mContext));
            parameters.put("APP_TYPE", CUSTOM_APP_TYPE);
            parameters.put("DELIVERALL", DELIVERALL);
            parameters.put("ONLYDELIVERALL", ONLYDELIVERALL);
            parameters.put("vGeneralLang", generalFunc.retrieveValue(Utils.LANGUAGE_CODE_KEY));


            if (generalFunc.isDeliverOnlyEnabled()) {
                parameters.put("eSystem", "DeliverAll");
            } else if (!generalFunc.isAnyDeliverOptionEnabled()) {
                parameters.put("eSystem", "");
            }

            try {
                if (parameters.get("type") != null && (parameters.get("type").equalsIgnoreCase("getDetail") || parameters.get("type").equalsIgnoreCase("signIn") || parameters.get("type").equalsIgnoreCase("signup") || parameters.get("type").equalsIgnoreCase("LoginWithFB"))) {
                    parameters.put("DEVICE_DATA", DeviceData.getDeviceData());
                }
            } catch (Exception e) {

            }

            try {
                if (parameters.get("type") != null && parameters.get("type").equalsIgnoreCase("generalConfigData")) {
                    parameters.putAll(GetFeatureClassList.getAllGeneralClasses());
                }
            } catch (Exception e) {

            }
        }

        if (generalFunc != null) {
            GetDeviceToken getDeviceToken = new GetDeviceToken(generalFunc);

            getDeviceToken.setDataResponseListener(vDeviceToken -> {

                if (isGenerateDeviceToken) {
                    if (!vDeviceToken.equals("")) {
                        if (parameters != null) {
                            parameters.put(key_DeviceToken_param, "" + vDeviceToken);
                            parameters.put("vFirebaseDeviceToken", vDeviceToken);
                        }
                        performPostCall();
                    } else {
                        responseString = "";
                        fireResponse();
                    }
                } else {
                    if (parameters != null) {
                        parameters.put("vFirebaseDeviceToken", vDeviceToken);
                    }
                    performPostCall();

                }

            });
            getDeviceToken.execute();

        } else {
            performPostCall();
        }
    }

    public void performGetCall(String directUrl) {
        Call<Object> call = RestClient.getClient("GET", CommonUtilities.SERVER).getResponse(directUrl);
        currentCall = call;
        call.enqueue(new Callback<Object>() {

            @Override
            public void onResponse(Call<Object> call, Response<Object> response) {

                if (response.isSuccessful()) {
                    responseString = RestClient.getGSONBuilder().toJson(response.body());
                    fireResponse();
                } else {
                    if (response.errorBody() != null) {
                        try {
                            responseString = RestClient.getGSONBuilder().toJson(response.errorBody().string());
                        } catch (Exception e) {
                            responseString = "";
                        }
                    } else {
                        responseString = "";
                    }
                    fireResponse();
                }
            }

            @Override
            public void onFailure(Call<Object> call, Throwable t) {
                Logger.d("DataError", "::" + t.getMessage());
                responseString = "";
                fireResponse();
            }

        });
    }

    public void performPostCall() {
        if (directUrl_value) {
            performGetCall(directUrl);
            return;
        }

        Call<Object> call = RestClient.getClient("POST", CommonUtilities.SERVER,mContext).getResponse(CommonUtilities.SERVER_WEBSERVICE_PATH, parameters);
        currentCall = call;
        call.enqueue(new Callback<Object>() {
            @Override
            public void onResponse(Call<Object> call, Response<Object> response) {
                if (response.isSuccessful()) {
                    responseString = RestClient.getGSONBuilder().toJson(response.body());
                    fireResponse();
                } else {
                    if (response.errorBody() != null) {
                        try {
                            responseString = RestClient.getGSONBuilder().toJson(response.errorBody().string());
                        } catch (Exception e) {
                            responseString = "";
                        }
                    } else {
                        responseString = "";
                    }
                    fireResponse();
                }
            }

            @Override
            public void onFailure(Call<Object> call, Throwable t) {
                Logger.d("DataError", "::" + t.getMessage());
                responseString = "";
                fireResponse();
            }

        });
    }

    public void fireResponse() {
        if (myPDialog != null) {
            myPDialog.close();
        }

        if (setDataRes != null && !isTaskKilled) {

            GeneralFunctions generalFunc = MyApp.getInstance().getAppLevelGeneralFunc();

            String message = Utils.checkText(responseString) ? generalFunc.getJsonValue(Utils.message_str, responseString) : null;

            if (message != null && message.equals("DO_RESTART") && !ignoreDorestartHandling) {
                generalFunc.restartApp();
                Utils.runGC();
                return;
            } else {
                try {
                    if (mContext != null && mContext instanceof Activity) {
                        Activity act = (Activity) mContext;
                        if (!act.isFinishing()) {
                            if (message != null && message.equals("SESSION_OUT")) {
                                MyApp.getInstance().notifySessionTimeOut();
                                Utils.runGC();
                                return;
                            }
                        }
                    }
                } catch (Exception e) {

                }
                setDataRes.setResponse(responseString);
            }
        }
    }

    public void cancel(boolean value) {

        this.isTaskKilled = value;
        if (currentCall != null) {

            new AsyncTask<String, String, String>() {
                @Override
                protected String doInBackground(String... params) {
                    currentCall.cancel();
                    return "";
                }
            }.execute();
        }
    }

    public void setDataResponseListener(SetDataResponse setDataRes) {
        this.setDataRes = setDataRes;
    }

    public interface SetDataResponse {
        void setResponse(String responseString);
    }
}
