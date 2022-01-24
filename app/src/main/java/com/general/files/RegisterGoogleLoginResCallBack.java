package com.general.files;

import android.content.Context;
import androidx.annotation.NonNull;

import com.taxifgo.driver.AppLoignRegisterActivity;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.utils.Logger;
import com.utils.Utils;
import com.view.MyProgressDialog;

import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created by Admin on 29-06-2016.
 */
public class RegisterGoogleLoginResCallBack implements GoogleApiClient.OnConnectionFailedListener {
    Context mContext;
    GeneralFunctions generalFunc;

    MyProgressDialog myPDialog;
    AppLoignRegisterActivity appLoginAct;

    public RegisterGoogleLoginResCallBack(Context mContext) {
        this.mContext = mContext;

        generalFunc = MyApp.getInstance().getGeneralFun(mContext);
        appLoginAct = (AppLoignRegisterActivity) mContext;

    }

    public void handleSignInResult(GoogleSignInResult result) {

        if (result.isSuccess()) {
            GoogleSignInAccount acct = result.getSignInAccount();
            String personName = acct.getDisplayName();
            String email = acct.getEmail();
            String id = acct.getId();
            String imageUrl = acct.getPhotoUrl() + "";
            Logger.d("imageUrl", "::" + imageUrl);
            registergoogleUser(email, personName, "", id, imageUrl);
        }
    }

    public void registergoogleUser(final String email, final String fName, final String lName, final String fbId, String imageUrl) {

        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "LoginWithFB");
        parameters.put("vFirstName", fName);
        parameters.put("vLastName", lName);
        parameters.put("vEmail", email);
        parameters.put("iFBId", fbId);
        parameters.put("eLoginType", "Google");
        parameters.put("vDeviceType", Utils.deviceType);
        parameters.put("UserType", Utils.userType);
        parameters.put("vCurrency", generalFunc.retrieveValue(Utils.DEFAULT_CURRENCY_VALUE));
        parameters.put("vLang", generalFunc.retrieveValue(Utils.LANGUAGE_CODE_KEY));
        parameters.put("vImageURL", imageUrl);
        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(mContext, parameters);
        //   exeWebServer.setLoaderConfig(mContext, true, generalFunc);
        exeWebServer.setIsDeviceTokenGenerate(true, "vDeviceToken", generalFunc);
        exeWebServer.setDataResponseListener(new ExecuteWebServerUrl.SetDataResponse() {
            @Override
            public void setResponse(String responseString) {
                JSONObject responseStringObject=generalFunc.getJsonObject(responseString);

                Logger.d("Response", "::" + responseStringObject);

                if (responseStringObject != null && !responseStringObject.equals("")) {

                    boolean isDataAvail = GeneralFunctions.checkDataAvail(Utils.action_str, responseStringObject);

                    if (isDataAvail == true) {


                        new SetUserData(responseString, generalFunc, mContext, true);
                        appLoginAct.manageSinchClient();
                        generalFunc.storeData(Utils.USER_PROFILE_JSON, generalFunc.getJsonValueStr(Utils.message_str, responseStringObject));
                        new OpenMainProfile(mContext,
                                generalFunc.getJsonValueStr(Utils.message_str, responseStringObject), false, generalFunc).startProcess();
                    } else {
                        if (!generalFunc.getJsonValue(Utils.message_str, responseStringObject).equals("DO_REGISTER")) {
                            generalFunc.showGeneralMessage("",
                                    generalFunc.retrieveLangLBl("", generalFunc.getJsonValueStr(Utils.message_str, responseStringObject)));
                        } else {

                            signupUser(email, fName, lName, fbId, imageUrl);
                        }

                    }
                } else {
                    generalFunc.showError();
                }
            }
        });
        exeWebServer.execute();
    }


    public void signupUser(final String email, final String fName, final String lName, final String fbId, String imageUrl) {

        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "signup");
        parameters.put("vFirstName", fName);
        parameters.put("vLastName", lName);
        parameters.put("vEmail", email);
        parameters.put("vFbId", fbId);
        parameters.put("vDeviceType", Utils.deviceType);
        parameters.put("UserType", Utils.userType);
        parameters.put("vCurrency", generalFunc.retrieveValue(Utils.DEFAULT_CURRENCY_VALUE));
        parameters.put("vLang", generalFunc.retrieveValue(Utils.LANGUAGE_CODE_KEY));
        parameters.put("eSignUpType", "Google");
        parameters.put("vImageURL", imageUrl);

        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(mContext, parameters);
        //   exeWebServer.setLoaderConfig(mContext, true, generalFunc);
        exeWebServer.setIsDeviceTokenGenerate(true, "vDeviceToken", generalFunc);
        exeWebServer.setDataResponseListener(new ExecuteWebServerUrl.SetDataResponse() {
            @Override
            public void setResponse(String responseString) {
                JSONObject responseStringObject=generalFunc.getJsonObject(responseString);

                Logger.d("Response", "::" + responseString);

                if (responseStringObject != null && !responseStringObject.equals("")) {

                    boolean isDataAvail = GeneralFunctions.checkDataAvail(Utils.action_str, responseStringObject);

                    if (isDataAvail == true) {
                        new SetUserData(responseString, generalFunc, mContext, true);
                        appLoginAct.manageSinchClient();
                        generalFunc.storeData(Utils.USER_PROFILE_JSON, generalFunc.getJsonValueStr(Utils.message_str, responseStringObject));
                        new OpenMainProfile(mContext,
                                generalFunc.getJsonValueStr(Utils.message_str, responseStringObject), false, generalFunc).startProcess();
                    } else {


                    }
                } else {
                    generalFunc.showError();
                }
            }
        });
        exeWebServer.execute();
    }


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        closeDialog();
    }

    public void closeDialog() {
        if (myPDialog != null) {
            myPDialog.close();
        }
    }

}
