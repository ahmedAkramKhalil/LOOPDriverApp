package com.general.files;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.taxifgo.driver.R;
import com.utils.CommonUtilities;
import com.utils.Utils;
import com.view.anim.loader.AVLoadingIndicatorView;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;

public class OpenLinkedinDialog {

    Context mContext;
    HashMap<String, String> data;
    GeneralFunctions generalFunc;

    androidx.appcompat.app.AlertDialog alertDialog;

    ProgressBar LoadingProgressBar;

    public OpenLinkedinDialog(Context mContext, GeneralFunctions generalFunc) {
        this.mContext = mContext;
        this.generalFunc = generalFunc;

        show();
    }

    AVLoadingIndicatorView loaderView;

    public void show() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(mContext);
        builder.setTitle("");

        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View dialogView = inflater.inflate(R.layout.linkedin_dailog, null);
        dialogView.setBackgroundColor(mContext.getResources().getColor(android.R.color.transparent));
        builder.setView(dialogView);


        LoadingProgressBar = ((ProgressBar) dialogView.findViewById(R.id.LoadingProgressBar));

        WebView mWebView = (WebView) dialogView.findViewById(R.id.linkedinWebview);
        loaderView = (AVLoadingIndicatorView) dialogView.findViewById(R.id.loaderView);
        mWebView.setWebViewClient(new myWebClient());
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.loadUrl(CommonUtilities.LINKEDINLOGINLINK);
        mWebView.setFocusable(true);


        (dialogView.findViewById(R.id.cancelBtn)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                if (alertDialog != null) {
                    alertDialog.dismiss();
                }

            }
        });


        alertDialog = builder.create();
        if (generalFunc.isRTLmode()) {
            generalFunc.forceRTLIfSupported(alertDialog);
        }
        alertDialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT);

        alertDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        alertDialog.show();

    }

    public class myWebClient extends WebViewClient {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }


        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            String data = url;
            data = data.substring(data.indexOf("data") + 5, data.length());
            try {
                String datajson = URLDecoder.decode(data, "UTF-8");
                loaderView.setVisibility(View.VISIBLE);

                view.setOnTouchListener(null);

                if (url.contains("status=1")) {
                    registerLinkedinUser(generalFunc.getJsonValue("emailAddress", datajson), generalFunc.getJsonValue("firstName", datajson), generalFunc.getJsonValue("lastName", datajson), generalFunc.getJsonValue("id", datajson),
                            generalFunc.getJsonValue("pictureUrl", datajson), datajson);
                }
                if (url.contains("status=2")) {
                    if (alertDialog != null) {
                        alertDialog.dismiss();
                    }
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

        }


        @Override
        public void onReceivedError(WebView view, int errorCode,
                                    String description, String failingUrl) {

            generalFunc.showError();
            loaderView.setVisibility(View.GONE);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            loaderView.setVisibility(View.GONE);

            view.setOnTouchListener((v, event) -> {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                    case MotionEvent.ACTION_UP:
                        if (!v.hasFocus()) {
                            v.requestFocus();
                        }
                        break;
                }
                return false;
            });

        }
    }

    public void registerLinkedinUser(final String email, final String fName, final String lName, final String fbId, String imageUrl, String object) {

        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "LoginWithFB");
        parameters.put("vFirstName", fName);
        parameters.put("vLastName", lName);
        parameters.put("vEmail", email);
        parameters.put("iFBId", fbId);
        parameters.put("eLoginType", "LinkedIn");
        parameters.put("vDeviceType", Utils.deviceType);
        parameters.put("UserType", Utils.userType);
        parameters.put("vCurrency", generalFunc.retrieveValue(Utils.DEFAULT_CURRENCY_VALUE));
        parameters.put("vLang", generalFunc.retrieveValue(Utils.LANGUAGE_CODE_KEY));
        parameters.put("vImageURL", imageUrl);
        parameters.put("socialData", object);
        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(mContext, parameters);
        //   exeWebServer.setLoaderConfig(mContext, true, generalFunc);
        exeWebServer.setIsDeviceTokenGenerate(true, "vDeviceToken", generalFunc);
        exeWebServer.setDataResponseListener(responseString -> {
            JSONObject responseStringObject=generalFunc.getJsonObject(responseString);

            if (responseStringObject != null && !responseStringObject.equals("")) {

                boolean isDataAvail = GeneralFunctions.checkDataAvail(Utils.action_str, responseStringObject);

                if (isDataAvail) {
                    new SetUserData(responseString, generalFunc, mContext, true);
                    generalFunc.storeData(Utils.USER_PROFILE_JSON, generalFunc.getJsonValueStr(Utils.message_str, responseStringObject));
                    new OpenMainProfile(mContext,
                            generalFunc.getJsonValueStr(Utils.message_str, responseStringObject), false, generalFunc).startProcess();
                } else {
                    if (!generalFunc.getJsonValueStr(Utils.message_str, responseStringObject).equals("DO_REGISTER")) {
                        generalFunc.showGeneralMessage("",
                                generalFunc.retrieveLangLBl("", generalFunc.getJsonValueStr(Utils.message_str, responseStringObject)));
                    } else {

                        signupUser(email, fName, lName, fbId, imageUrl, object);
                    }

                }
            } else {
                generalFunc.showError();
            }
        });
        exeWebServer.execute();
    }


    public void signupUser(final String email, final String fName, final String lName, final String fbId, String imageUrl, String object) {

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
        parameters.put("eSignUpType", "LinkedIn");
        parameters.put("vImageURL", imageUrl);
        parameters.put("socialData", object);

        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(mContext, parameters);
        //   exeWebServer.setLoaderConfig(mContext, true, generalFunc);
        exeWebServer.setIsDeviceTokenGenerate(true, "vDeviceToken", generalFunc);
        exeWebServer.setDataResponseListener(responseString -> {
            JSONObject responseStringObject=generalFunc.getJsonObject(responseString);

            if (responseStringObject != null && !responseStringObject.equals("")) {

                boolean isDataAvail = GeneralFunctions.checkDataAvail(Utils.action_str, responseStringObject);

                if (isDataAvail) {
                    new SetUserData(responseString, generalFunc, mContext, true);

                    generalFunc.storeData(Utils.USER_PROFILE_JSON, generalFunc.getJsonValueStr(Utils.message_str, responseStringObject));
                    new OpenMainProfile(mContext,
                            generalFunc.getJsonValueStr(Utils.message_str, responseStringObject), false, generalFunc).startProcess();
                }
            } else {
                generalFunc.showError();
            }
        });
        exeWebServer.execute();
    }

}
