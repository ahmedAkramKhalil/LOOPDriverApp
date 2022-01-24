package com.taxifgo.driver;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import com.google.android.material.snackbar.Snackbar;
import androidx.core.app.ActivityCompat;
import android.view.View;
import android.widget.RelativeLayout;

import com.general.files.ExecuteWebServerUrl;
import com.general.files.GeneralFunctions;
import com.general.files.GetDeviceToken;
import com.general.files.InternetConnection;
import com.general.files.MyApp;
import com.general.files.OnClearFromRecentService;
import com.general.files.OpenMainProfile;
import com.general.files.SetGeneralData;
import com.general.files.SetUserData;
import com.general.files.StartActProcess;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.security.ProviderInstaller;
import com.utils.CabRequestStatus;
import com.utils.Logger;
import com.utils.Utils;
import com.view.GenerateAlertBox;
import com.view.MTextView;
import com.view.anim.loader.AVLoadingIndicatorView;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;

public class LauncherActivity extends BaseActivity implements ProviderInstaller.ProviderInstallListener {

    AVLoadingIndicatorView loaderView;
    InternetConnection intCheck;
    GeneralFunctions generalFunc;

    long autoLoginStartTime = 0;
    boolean isnotification = false;

    /*4.4 lower Device SSl CERTIFICATE ISSUE*/

    private static final int ERROR_DIALOG_REQUEST_CODE = 1;
    private boolean mRetryProviderInstall;
    RelativeLayout rlContentArea;
    MTextView drawOverMsgTxtView;

    GenerateAlertBox currentAlertBox;

    String LBL_BTN_OK_TXT,LBL_CANCEL_TXT,LBL_RETRY_TXT,LBL_TRY_AGAIN_TXT;

    boolean isPermissionShown_general;
    String response_str_generalConfigData = "";
    String response_str_autologin = "";
    private  static ArrayList<String> requestPermissions = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);

        requestPermissions.add(android.Manifest.permission.ACCESS_FINE_LOCATION);
        requestPermissions.add(android.Manifest.permission.ACCESS_COARSE_LOCATION);
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            requestPermissions.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION);
        }


        generalFunc = MyApp.getInstance().getGeneralFun(getActContext());
        // getLastLocation = new GetLocationUpdates(getActContext(), 2, false);
        intCheck = new InternetConnection(getActContext());
        generalFunc.storeData("isInLauncher", "true");

        loaderView = (AVLoadingIndicatorView) findViewById(R.id.loaderView);
        rlContentArea = (RelativeLayout) findViewById(R.id.rlContentArea);
        drawOverMsgTxtView = (MTextView) findViewById(R.id.drawOverMsgTxtView);



        drawOverMsgTxtView.setText(generalFunc.retrieveLangLBl("Please wait while we are checking app's configuration. This will take few seconds.", "LBL_DRAW_OVER_APP_NOTE"));

        startService(new Intent(getBaseContext(), OnClearFromRecentService.class));

        LBL_RETRY_TXT=generalFunc.retrieveLangLBl("Retry", "LBL_RETRY_TXT");
        LBL_CANCEL_TXT=generalFunc.retrieveLangLBl("Cancel", "LBL_CANCEL_TXT");
        LBL_BTN_OK_TXT=generalFunc.retrieveLangLBl("Ok", "LBL_BTN_OK_TXT");
        LBL_TRY_AGAIN_TXT=generalFunc.retrieveLangLBl("Please try again.", "LBL_TRY_AGAIN_TXT");

        ProviderInstaller.installIfNeededAsync(this, this);

       // new StartActProcess(getActContext()).startService(MyBackGroundService.class);
    }

    public void checkConfigurations(boolean isPermissionShown) {
        drawOverMsgTxtView.setVisibility(View.GONE);

        isPermissionShown_general = isPermissionShown;

        closeAlert();

        int status = (GoogleApiAvailability.getInstance()).isGooglePlayServicesAvailable(getActContext());

        if (status == ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED) {
            showErrorOnPlayServiceDialog(generalFunc.retrieveLangLBl("This application requires updated google play service. " +
                    "Please install Or update it from play store", "LBL_UPDATE_PLAY_SERVICE_NOTE"));
            return;
        } else if (status != ConnectionResult.SUCCESS) {
            showErrorOnPlayServiceDialog(generalFunc.retrieveLangLBl("This application requires updated google play service. " +
                    "Please install Or update it from play store", "LBL_UPDATE_PLAY_SERVICE_NOTE"));
            return;
        }

      /*  if (!generalFunc.isAllPermissionGranted(isPermissionShown)) {
            showNoPermission();
            return;
        }*/
        if (!intCheck.isNetworkConnected() && !intCheck.check_int()) {
            showNoInternetDialog();
            return;
        }

        if (!generalFunc.canDrawOverlayViews(getActContext())) {
            GenerateAlertBox generateAlert = new GenerateAlertBox(getActContext());
            generateAlert.setContentMessage("",
                    generalFunc.retrieveLangLBl("Please enable draw over app permission.", "LBL_ENABLE_DRWA_OVER_APP"));
            generateAlert.setPositiveBtn(generalFunc.retrieveLangLBl("Allow", "LBL_ALLOW"));
            generateAlert.showAlertBox();
            generateAlert.setCancelable(false);
            generateAlert.setBtnClickList(btn_id -> {
                if (btn_id == 1) {
                    (new StartActProcess(getActContext())).requestOverlayPermission(Utils.OVERLAY_PERMISSION_REQ_CODE);
                }
            });

            return;
        }
        continueProcess();
    }

    public void continueProcess() {
        closeAlert();
        showLoader();

        Utils.setAppLocal(getActContext());

        boolean isLanguageLabelsAvail = generalFunc.isLanguageLabelsAvail();

        if (generalFunc.isUserLoggedIn() && Utils.checkText(generalFunc.getMemberId())) {


            if (getSinchServiceInterface() == null && !generalFunc.retrieveValue(Utils.SINCH_APP_KEY).equalsIgnoreCase("")) {
                new Handler().postDelayed(() -> continueProcess(), 1500);
            } else if (getSinchServiceInterface() != null) {
                autoLogin();
                if (!getSinchServiceInterface().isStarted()) {
                    getSinchServiceInterface().startClient(Utils.userType + "_" + generalFunc.getMemberId());
                    GetDeviceToken getDeviceToken = new GetDeviceToken(generalFunc);
                    getDeviceToken.setDataResponseListener(vDeviceToken -> {
                        if (!vDeviceToken.equals("")) {
                            try {
                                getSinchServiceInterface().getSinchClient().registerPushNotificationData(vDeviceToken.getBytes());
                            } catch (Exception ignored) {

                            }
                        }
                    });
                    getDeviceToken.execute();
                }
            } else {
                if (this.response_str_autologin.trim().equalsIgnoreCase("")) {
                    autoLogin();
                } else {

                    continueAutoLogin(this.response_str_autologin);
                }
            }
        } else {
            if (this.response_str_generalConfigData.trim().equalsIgnoreCase("")) {
                downloadGeneralData();
            } else {

                continueDownloadGeneralData(this.response_str_generalConfigData);
            }
        }
    }

    public void restartAppDailog() {
        closeAlert();
        generalFunc.showGeneralMessage("", LBL_TRY_AGAIN_TXT, LBL_BTN_OK_TXT, "", buttonId -> generalFunc.restartApp());
    }

    public void downloadGeneralData() {
        closeAlert();
        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "generalConfigData");
        parameters.put("UserType", Utils.app_type);
        parameters.put("AppVersion", BuildConfig.VERSION_NAME);
        parameters.put("vLang", generalFunc.retrieveValue(Utils.LANGUAGE_CODE_KEY));
        parameters.put("vCurrency", generalFunc.retrieveValue(Utils.DEFAULT_CURRENCY_VALUE));

        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(), parameters);
        exeWebServer.setDataResponseListener(responseString -> {

            JSONObject responseObj=generalFunc.getJsonObject(responseString);
            if (isFinishing()) {
                restartAppDailog();
                return;
            }

            if (responseObj != null && !responseObj.equals("")) {
                boolean isDataAvail = GeneralFunctions.checkDataAvail(Utils.action_str, responseObj);
                if (isDataAvail) {
                    new SetGeneralData(generalFunc,responseObj);
                    generalFunc.storeData("TSITE_DB", generalFunc.getJsonValue("TSITE_DB", responseString));
                    generalFunc.storeData("GOOGLE_API_REPLACEMENT_URL", generalFunc.getJsonValue("GOOGLE_API_REPLACEMENT_URL", responseString));
                    String PACKAGE_TYPE = generalFunc.getJsonValue("PACKAGE_TYPE", responseString);
                    if (!PACKAGE_TYPE.equalsIgnoreCase("STANDARD")){
                        requestPermissions.add(android.Manifest.permission.RECORD_AUDIO);
                        requestPermissions.add(Manifest.permission.READ_PHONE_STATE);
                    }


                    String[] strings =   (String[])requestPermissions.toArray(new String[requestPermissions.size()]);
                    Logger.d("permissiossss", Arrays.toString(strings));
                    if (!generalFunc.isAllPermissionGranted(isPermissionShown_general,requestPermissions)) {
                        response_str_generalConfigData = responseString;
                        showNoPermission();
                        return;
                    }

                    continueDownloadGeneralData(responseString);

                    Logger.d("PACKAGE_TYPEGET_GC",""+PACKAGE_TYPE);


                } else {
                    if (!generalFunc.getJsonValueStr("isAppUpdate", responseObj).trim().equals("")
                            && generalFunc.getJsonValueStr("isAppUpdate", responseObj).equals("true")) {

                        showAppUpdateDialog(generalFunc.retrieveLangLBl("New update is available to download. " +
                                        "Downloading the latest update, you will get latest features, improvements and bug fixes.",
                                generalFunc.getJsonValueStr(Utils.message_str, responseObj)));
                    } else {
                        showError();
                    }
                }
            } else {
                showError();
            }

        });
        exeWebServer.execute();
    }

    public void continueDownloadGeneralData(String responseString) {
        JSONObject responseObj=generalFunc.getJsonObject(responseString);

                    new SetGeneralData(generalFunc,responseObj);
                    generalFunc.storeData("TSITE_DB", generalFunc.getJsonValue("TSITE_DB", responseString));
                    generalFunc.storeData("GOOGLE_API_REPLACEMENT_URL", generalFunc.getJsonValue("GOOGLE_API_REPLACEMENT_URL", responseString));

        Utils.setAppLocal(getActContext());

        closeLoader();

        if (generalFunc.getJsonValueStr("SERVER_MAINTENANCE_ENABLE", responseObj).equalsIgnoreCase("Yes")) {
            new StartActProcess(getActContext()).startAct(MaintenanceActivity.class);
            finish();
            return;
        }


        if(!generalFunc.isAllPermissionGranted(true,requestPermissions))
        {
            showNoPermission();
            return;
        }


        new Handler().postDelayed(() -> {
            new StartActProcess(getActContext()).startAct(AppLoginActivity.class);
            try {
                ActivityCompat.finishAffinity(LauncherActivity.this);
            } catch (Exception ignored) {

            }
        }, 2000);

    }




    public void autoLogin() {
        closeAlert();
        autoLoginStartTime = Calendar.getInstance().getTimeInMillis();

        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "getDetail");
        parameters.put("iUserId", generalFunc.getMemberId());
        parameters.put("vDeviceType", Utils.deviceType);
        parameters.put("UserType", Utils.app_type);
        parameters.put("AppVersion", BuildConfig.VERSION_NAME);
        if (!generalFunc.retrieveValue(Utils.LANGUAGE_CODE_KEY).equalsIgnoreCase("")) {
            parameters.put("vLang", generalFunc.retrieveValue(Utils.LANGUAGE_CODE_KEY));
        }

        parameters.putAll((new CabRequestStatus(getActContext())).getAllStatusParam());

        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(), parameters);
        exeWebServer.setIsDeviceTokenGenerate(true, "vDeviceToken", generalFunc);
        exeWebServer.setDataResponseListener(responseString -> {

            closeLoader();
            if (isFinishing()) {
                return;
            }

            JSONObject responseObj=generalFunc.getJsonObject(responseString);

            if (responseObj != null && !responseObj.equals("")) {

                boolean isDataAvail = GeneralFunctions.checkDataAvail(Utils.action_str, responseObj);

                if (generalFunc.getJsonValueStr("changeLangCode", responseObj).equalsIgnoreCase("Yes")) {
                    new SetUserData(responseString, generalFunc, getActContext(), false);
                }

                final String message = generalFunc.getJsonValueStr(Utils.message_str, responseObj);

                if (message.equals("SESSION_OUT")) {
                    autoLoginStartTime = 0;
                    MyApp.getInstance().notifySessionTimeOut();
                    Utils.runGC();
                    return;
                }

                if (isDataAvail) {
                    generalFunc.storeData("TSITE_DB", generalFunc.getJsonValue("TSITE_DB", responseString));
                    generalFunc.storeData("GOOGLE_API_REPLACEMENT_URL", generalFunc.getJsonValue("GOOGLE_API_REPLACEMENT_URL", responseString));
                    String PACKAGE_TYPE = generalFunc.getJsonValue("PACKAGE_TYPE", message);
                    if (!PACKAGE_TYPE.equalsIgnoreCase("STANDARD")){
                        requestPermissions.add(Manifest.permission.RECORD_AUDIO);
                        requestPermissions.add(Manifest.permission.READ_PHONE_STATE);
                    }


                    if (!generalFunc.isAllPermissionGranted(isPermissionShown_general,requestPermissions)) {
                        response_str_autologin = responseString;
                        showNoPermission();
                        return;
                    }

                    Logger.d("PACKAGE_TYPEGET_GD",""+PACKAGE_TYPE);
                    continueAutoLogin(responseString);

                } else {
                    autoLoginStartTime = 0;
                    if (!generalFunc.getJsonValueStr("isAppUpdate", responseObj).trim().equals("")
                            && generalFunc.getJsonValueStr("isAppUpdate", responseObj).equals("true")) {

                        showAppUpdateDialog(generalFunc.retrieveLangLBl("New update is available to download. " +
                                        "Downloading the latest update, you will get latest features, improvements and bug fixes.",
                                generalFunc.getJsonValueStr(Utils.message_str, responseObj)));
                    } else {

                        if (generalFunc.getJsonValueStr(Utils.message_str, responseObj).equalsIgnoreCase("LBL_CONTACT_US_STATUS_NOTACTIVE_COMPANY") ||
                                generalFunc.getJsonValueStr(Utils.message_str, responseObj).equalsIgnoreCase("LBL_ACC_DELETE_TXT") ||
                                generalFunc.getJsonValueStr(Utils.message_str, responseObj).equalsIgnoreCase("LBL_CONTACT_US_STATUS_NOTACTIVE_DRIVER")) {

                            showContactUs(generalFunc.retrieveLangLBl("", generalFunc.getJsonValueStr(Utils.message_str, responseObj)));

                            return;
                        }


                        showError("",
                                generalFunc.retrieveLangLBl("", generalFunc.getJsonValueStr(Utils.message_str, responseObj)));
                    }
                }
            } else {
                autoLoginStartTime = 0;
                showError();
            }
        });
        exeWebServer.execute();
    }


    public void continueAutoLogin(String responseString) {
        JSONObject responseObj=generalFunc.getJsonObject(responseString);

        final String message = generalFunc.getJsonValueStr(Utils.message_str, responseObj);

        ((new CabRequestStatus(getActContext()))).removeOldRequestsData();

        if (generalFunc.getJsonValue("SERVER_MAINTENANCE_ENABLE", message).equalsIgnoreCase("Yes")) {
            new StartActProcess(getActContext()).startAct(MaintenanceActivity.class);
            finish();
            return;
        }

        generalFunc.storeData(Utils.USER_PROFILE_JSON, message);

        generalFunc.storeData(Utils.SESSION_ID_KEY, generalFunc.getJsonValue("tSessionId", message));
        generalFunc.storeData(Utils.DEVICE_SESSION_ID_KEY, generalFunc.getJsonValue("tDeviceSessionId", message));
        generalFunc.storeData(Utils.WORKLOCATION, generalFunc.getJsonValue("vWorkLocation", message));

        if (Calendar.getInstance().getTimeInMillis() - autoLoginStartTime < 2000) {
            new Handler().postDelayed(() -> {
                String vTripStatus = generalFunc.getJsonValue("vTripStatus", message);
                if (!vTripStatus.equalsIgnoreCase("Not Active")) {
                    if (vTripStatus.contains("Arrived") || vTripStatus.contains("Active") || vTripStatus.contains("On Going Trip")) {
                        new OpenMainProfile(getActContext(),
                                generalFunc.getJsonValueStr(Utils.message_str, responseObj), true, generalFunc, isnotification).startProcess();
                    } else {
                        new OpenMainProfile(getActContext(),
                                generalFunc.getJsonValueStr(Utils.message_str, responseObj), true, generalFunc, isnotification).startProcess();
                    }
                } else {
                    new OpenMainProfile(getActContext(),
                            generalFunc.getJsonValueStr(Utils.message_str, responseObj), true, generalFunc, isnotification).startProcess();
                }
            }, 2000);
        } else {
            String vTripStatus = generalFunc.getJsonValue("vTripStatus", message);
            if (vTripStatus.contains("Arrived") || vTripStatus.contains("Active") || vTripStatus.contains("On Going Trip")) {
                new OpenMainProfile(getActContext(),
                        generalFunc.getJsonValueStr(Utils.message_str, responseObj), true, generalFunc, isnotification).startProcess();
            } else {
                new OpenMainProfile(getActContext(),
                        generalFunc.getJsonValueStr(Utils.message_str, responseObj), true, generalFunc, isnotification).startProcess();
            }
        }

    }

    public void showLoader() {
        loaderView.setVisibility(View.VISIBLE);
    }

    public void closeLoader() {
        loaderView.setVisibility(View.GONE);
    }

    private void closeAlert() {
        try {
            if (currentAlertBox != null) {
                currentAlertBox.closeAlertBox();
                currentAlertBox = null;
            }
        } catch (Exception e) {

        }
    }

    public void showContactUs(String content) {
        closeAlert();
        currentAlertBox = generalFunc.showGeneralMessage("", content, generalFunc.retrieveLangLBl("Contact Us", "LBL_CONTACT_US_TXT"), LBL_BTN_OK_TXT, buttonId -> {
            if (buttonId == 0) {
                new StartActProcess(getActContext()).startAct(ContactUsActivity.class);
                showContactUs(content);
            } else if (buttonId == 1) {
                MyApp.getInstance().logOutFromDevice(true);
            }
        });
    }

    public void showError() {
        closeAlert();
        currentAlertBox = generalFunc.showGeneralMessage("", LBL_TRY_AGAIN_TXT, LBL_CANCEL_TXT, LBL_RETRY_TXT, buttonId -> handleBtnClick(buttonId, "ERROR"));
    }

    public void showError(String title, String contentMsg) {
        closeAlert();
        currentAlertBox = generalFunc.showGeneralMessage(title, contentMsg, LBL_CANCEL_TXT, LBL_RETRY_TXT, buttonId -> handleBtnClick(buttonId, "ERROR"));
    }

    public void showNoInternetDialog() {
        closeAlert();
        currentAlertBox = generalFunc.showGeneralMessage("", generalFunc.retrieveLangLBl("No Internet Connection", "LBL_NO_INTERNET_TXT"), LBL_CANCEL_TXT, LBL_RETRY_TXT, buttonId -> handleBtnClick(buttonId, "NO_INTERNET"));
    }

    public void showNoGPSDialog() {
        closeAlert();
        currentAlertBox = generalFunc.showGeneralMessage("", generalFunc.retrieveLangLBl("Your GPS seems to be disabled, do you want to enable it?", "LBL_ENABLE_GPS"), LBL_CANCEL_TXT, LBL_BTN_OK_TXT, buttonId -> handleBtnClick(buttonId, "NO_GPS"));
    }

    public void showNoPermission() {
        currentAlertBox = generalFunc.showGeneralMessage("", generalFunc.retrieveLangLBl("Application requires some permission to be granted to work. Please allow it.",
                "LBL_ALLOW_PERMISSIONS_APP"), LBL_CANCEL_TXT, generalFunc.retrieveLangLBl("Allow All", "LBL_ALLOW_ALL_TXT"), buttonId -> handleBtnClick(buttonId, "NO_PERMISSION"));
    }

    public void showErrorOnPlayServiceDialog(String content) {
        closeAlert();
        currentAlertBox = generalFunc.showGeneralMessage("", content, LBL_RETRY_TXT, generalFunc.retrieveLangLBl("Update", "LBL_UPDATE"), buttonId -> handleBtnClick(buttonId, "NO_PLAY_SERVICE"));
    }

    public void showAppUpdateDialog(String content) {
        closeAlert();
        currentAlertBox = generalFunc.showGeneralMessage(generalFunc.retrieveLangLBl("New update available", "LBL_NEW_UPDATE_AVAIL"), content, LBL_RETRY_TXT, generalFunc.retrieveLangLBl("Update", "LBL_UPDATE"), buttonId -> handleBtnClick(buttonId, "APP_UPDATE"));
    }

    public void showNoLocationDialog() {
        closeAlert();
        currentAlertBox = generalFunc.showGeneralMessage("", generalFunc.retrieveLangLBl("Location not found. Please try later.", "LBL_NO_LOCATION_FOUND_TXT"), LBL_CANCEL_TXT, LBL_RETRY_TXT, buttonId -> handleBtnClick(buttonId, "NO_LOCATION"));
    }

    public Context getActContext() {
        return LauncherActivity.this;
    }

    public void handleBtnClick(int buttonId, String alertType) {

        if (buttonId == 0) {
            if (!alertType.equals("NO_PLAY_SERVICE") && !alertType.equals("APP_UPDATE")) {
                finish();
            } else {
                checkConfigurations(false);
            }

        } else if (alertType.equals("APP_UPDATE")) {
            boolean isSuccessfulOpen = new StartActProcess(getActContext()).openURL("market://details?id=" + BuildConfig.APPLICATION_ID);
            if (!isSuccessfulOpen) {
                new StartActProcess(getActContext()).openURL("http://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID);
            }
            checkConfigurations(false);
        } else if (alertType.equals("NO_PERMISSION")) {

            generalFunc.openSettings();
           /* if (!ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.ACCESS_FINE_LOCATION) || !ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)) {
                generalFunc.openSettings();
                checkConfigurations(false);
            } else if (!generalFunc.isAllPermissionGranted(false)) {
                generalFunc.isAllPermissionGranted(true);
                checkConfigurations(false);
            } else {
                checkConfigurations(true);
            }*/

        } else {
            if (alertType.equals("NO_PLAY_SERVICE")) {
                boolean isSuccessfulOpen = new StartActProcess(getActContext()).openURL("market://details?id=com.google.android.gms");
                if (!isSuccessfulOpen) {
                    new StartActProcess(getActContext()).openURL("http://play.google.com/store/apps/details?id=com.google.android.gms");
                }
                checkConfigurations(false);
            } else if (!alertType.equals("NO_GPS")) {
                checkConfigurations(false);
            } else {
                new StartActProcess(getActContext()).
                        startActForResult(Settings.ACTION_LOCATION_SOURCE_SETTINGS, Utils.REQUEST_CODE_GPS_ON);
                checkConfigurations(false);
            }

        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        generalFunc.storeData("isInLauncher", "false");
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case Utils.REQUEST_CODE_GPS_ON:
                checkConfigurations(false);
                break;
            case GeneralFunctions.MY_SETTINGS_REQUEST:
                checkConfigurations(false);
                break;
            case Utils.OVERLAY_PERMISSION_REQ_CODE:
                drawOverMsgTxtView.setVisibility(View.GONE);
                if (!generalFunc.canDrawOverlayViews(getActContext())) {
                    drawOverMsgTxtView.setVisibility(View.VISIBLE);
                    new Handler().postDelayed(() -> {
                        checkConfigurations(true);
                    }, 15000);
                } else {
                    checkConfigurations(true);
                }
                break;
            case ERROR_DIALOG_REQUEST_CODE:
                mRetryProviderInstall = true;
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case GeneralFunctions.MY_PERMISSIONS_REQUEST:
                if(!generalFunc.isAllPermissionGranted(false,requestPermissions))
                {
                    // showNoPermission();
                    return;
                }
                checkConfigurations(false);
                break;
        }
    }

    @Override
    public void onProviderInstalled() {
        checkConfigurations(true);
    }

    @Override
    public void onProviderInstallFailed(int errorCode, Intent intent) {
        if (GooglePlayServicesUtil.isUserRecoverableError(errorCode)) {
            GooglePlayServicesUtil.showErrorDialogFragment(
                    errorCode,
                    this,
                    ERROR_DIALOG_REQUEST_CODE,
                    dialog -> {
                        onProviderInstallerNotAvailable();
                    });
        } else {
            onProviderInstallerNotAvailable();
        }
    }

    private void onProviderInstallerNotAvailable() {
        checkConfigurations(true);
        showMessageWithAction(rlContentArea, generalFunc.retrieveLangLBl("provider cannot be updated for some reason.", "LBL_PROVIDER_NOT_AVALIABLE_TXT"));
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();

        if (mRetryProviderInstall) {
            ProviderInstaller.installIfNeededAsync(this, this);
        }
        mRetryProviderInstall = false;
    }

    public void showMessageWithAction(View view, String message) {
        Snackbar snackbar = Snackbar.make(view, message, Snackbar.LENGTH_INDEFINITE);
        snackbar.setDuration(10000);
        snackbar.show();
    }
}
