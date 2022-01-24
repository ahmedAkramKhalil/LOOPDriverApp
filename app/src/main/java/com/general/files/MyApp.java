package com.general.files;

import android.Manifest;
import android.app.Activity;
import android.app.Application;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import androidx.multidex.MultiDex;
import androidx.appcompat.app.AppCompatDelegate;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.taxifgo.driver.ActiveTripActivity;
import com.taxifgo.driver.BuildConfig;
import com.taxifgo.driver.DriverArrivedActivity;
import com.taxifgo.driver.LauncherActivity;
import com.taxifgo.driver.MainActivity;
import com.taxifgo.driver.NetworkChangeReceiver;
import com.taxifgo.driver.R;
import com.taxifgo.driver.deliverAll.LiveTaskListActivity;
import com.facebook.appevents.AppEventsLogger;
import com.squareup.picasso.Picasso;
import com.utils.CommonUtilities;
import com.utils.Logger;
import com.utils.NavigationSensor;
import com.utils.Utils;

import com.utils.WeViewFontConfig;
import com.view.GenerateAlertBox;

import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

public class MyApp extends Application {
    GeneralFunctions generalFun;
    private GpsReceiver mGpsReceiver;

    private static MyApp mMyApp;

    boolean isAppInBackground = true;

    private Activity currentAct = null;

    public MainActivity mainAct;
    public DriverArrivedActivity driverArrivedAct;
    public ActiveTripActivity activeTripAct;
    public LiveTaskListActivity liveTaskListAct;
    private NetworkChangeReceiver mNetWorkReceiver = null;
    public boolean ispoolRequest = false;

    GenerateAlertBox generateSessionAlert;
    GenerateAlertBox drawOverlayAppAlert;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    private  static ArrayList<String> requestPermissions = new ArrayList<>();

    @Override
    public void onCreate() {
        super.onCreate();

     //   Utils.SERVER_CONNECTION_URL = CommonUtilities.SERVER_URL;
        Utils.SERVER_CONNECTION_URL = "http://192.168.1.141/";
        Utils.IS_APP_IN_DEBUG_MODE = BuildConfig.DEBUG ? "Yes" : "No";
        Utils.userType = BuildConfig.USER_TYPE;
        Utils.app_type = BuildConfig.USER_TYPE;
        Utils.USER_ID_KEY = BuildConfig.USER_ID_KEY;
        Utils.IS_OPTIMIZE_MODE_ENABLE = true;


        ExecuteWebServerUrl.CUSTOM_APP_TYPE = "Ride-Delivery";
        ExecuteWebServerUrl.DELIVERALL = "";
        ExecuteWebServerUrl.ONLYDELIVERALL = "";

        HashMap<String, String> storeData = new HashMap<>();
        storeData.put("SERVERURL", CommonUtilities.SERVER_URL);
        storeData.put("SERVERWEBSERVICEPATH", CommonUtilities.SERVER_WEBSERVICE_PATH);
        storeData.put("USERTYPE", BuildConfig.USER_TYPE);
        GeneralFunctions.storeData(storeData, this);
        WeViewFontConfig.ASSETS_FONT_NAME = getResources().getString(R.string.systemRegular);
        WeViewFontConfig.FONT_FAMILY_NAME = getResources().getString(R.string.systemRegular_name);
        WeViewFontConfig.FONT_COLOR = "#343434";
        WeViewFontConfig.FONT_SIZE = "14px";

        try {
            Picasso.Builder builder = new Picasso.Builder(this);
//            builder.downloader(new OkHttp3Downloader(this, Integer.MAX_VALUE));
            Picasso built = builder.build();
            built.setIndicatorsEnabled(false); //green (memory, best performance),blue (disk, good performance),red (network, worst performance).
            built.setLoggingEnabled(false);
        /* set the global instance to use this Picasso object
           all following Picasso (with Picasso.with(Context context) requests will use this Picasso object
           you can only use the setSingletonInstance() method once!*/
            Picasso.setSingletonInstance(built);
        } catch (Exception e) {
            e.printStackTrace();
        }


        setScreenOrientation();

        mMyApp = (MyApp) this.getApplicationContext();

        try {
            AppEventsLogger.activateApp(this);
        } catch (Exception e) {
            Logger.d("FBError", "::" + e.toString());
        }

        generalFun = MyApp.getInstance().getGeneralFun(this);

        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);

        if (mGpsReceiver == null) {
            registerReceiver();
        }

        ///Fabric.with(this, new Crashlytics());
    }

    public GeneralFunctions getAppLevelGeneralFunc() {
        if (generalFun == null) {
            generalFun = new GeneralFunctions(this);
        }
        return generalFun;
    }

    public GeneralFunctions getGeneralFun(Context mContext) {
        return new GeneralFunctions(mContext, R.id.backImgView);
    }

    public void handleUncaughtException(Thread thread, Throwable e) {
        e.printStackTrace(); // not all Android versions will print the stack trace automatically
        try {
            extractLogToFile();

        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }

    public static synchronized MyApp getInstance() {
        return mMyApp;
    }


    public void stopAlertService() {
        stopService(new Intent(getBaseContext(), ChatHeadService.class));
    }

    public boolean isMyAppInBackGround() {
        return this.isAppInBackground;
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        Logger.d("Api", "Object Destroyed >> MYAPP onLowMemory");
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        Logger.d("Api", "Object Destroyed >> MYAPP onTrimMemory");
    }


    @Override
    public void onTerminate() {
        super.onTerminate();
        Logger.d("Api", "Object Destroyed >> MYAPP onTerminate");
        removePubSub();
        removeLocationUpdates();

        NotificationManager nMgr = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        nMgr.cancelAll();

        removeVoIpSettings();

        NavigationSensor.destroySensor();
    }

    private void removeVoIpSettings() {
        try {
            if (MyApp.getInstance().getCurrentAct() instanceof MainActivity) {
                MainActivity mainActivity = (MainActivity) MyApp.getInstance().getCurrentAct();
                if (new AppFunctions(mainActivity).checkSinchInstance(mainActivity != null ? mainActivity.getSinchServiceInterface() : null)) {
                    mainActivity.getSinchServiceInterface().getSinchClient().setSupportPushNotifications(false);
                    mainActivity.getSinchServiceInterface().getSinchClient().setSupportManagedPush(false);
                    mainActivity.getSinchServiceInterface().getSinchClient().unregisterManagedPush();
                    mainActivity.getSinchServiceInterface().getSinchClient().unregisterPushNotificationData();
                }
            }
        } catch (Exception e) {

        }
    }

    private void removeLocationUpdates() {
        try {
            if (GetLocationUpdates.retrieveInstance() != null) {
                GetLocationUpdates.getInstance().destroyLocUpdates(MyApp.this);
            }
        } catch (Exception e) {

        }
    }

    public void removePubSub() {
        releaseGpsReceiver();
        removeAllRunningInstances();
        terminatePuSubInstance();
    }


    private void removeAllRunningInstances() {
        connectReceiver(false);
    }

    private void releaseGpsReceiver() {
        if (mGpsReceiver != null)
            this.unregisterReceiver(mGpsReceiver);
        this.mGpsReceiver = null;

    }

    private void registerReceiver() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.FROYO) {
            IntentFilter mIntentFilter = new IntentFilter();
            mIntentFilter.addAction(LocationManager.PROVIDERS_CHANGED_ACTION);
            this.mGpsReceiver = new GpsReceiver();
            this.registerReceiver(this.mGpsReceiver, mIntentFilter);
        }
    }

    private void registerNetWorkReceiver() {

        if (mNetWorkReceiver == null) {
            try {
                IntentFilter mIntentFilter = new IntentFilter();
                mIntentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
                mIntentFilter.addAction(ConnectivityManager.EXTRA_NO_CONNECTIVITY);
                /*Extra Filter Started */
                mIntentFilter.addAction(ConnectivityManager.EXTRA_IS_FAILOVER);
                mIntentFilter.addAction(ConnectivityManager.EXTRA_REASON);
                mIntentFilter.addAction(ConnectivityManager.EXTRA_EXTRA_INFO);
                /*Extra Filter Ended */
//                mIntentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
//                mIntentFilter.addAction("android.net.wifi.WIFI_STATE_CHANGED");
                this.mNetWorkReceiver = new NetworkChangeReceiver();
                this.registerReceiver(this.mNetWorkReceiver, mIntentFilter);
            } catch (Exception e) {
                Logger.e("NetWorkDemo", "Network connectivity register error occurred");
            }
        }
    }

    private void unregisterNetWorkReceiver() {

        if (mNetWorkReceiver != null)
            try {
                this.unregisterReceiver(mNetWorkReceiver);
                this.mNetWorkReceiver = null;
            } catch (Exception e) {
                e.printStackTrace();
            }

    }

    public static boolean isAppInstanceAvailable() {
        try {
            if (MyApp.getInstance() == null || MyApp.getInstance().getApplicationContext() == null || MyApp.getInstance().getApplicationContext().getPackageManager() == null || MyApp.getInstance().getApplicationContext().getPackageName() == null) {
                return false;
            }
        } catch (Exception e) {
            return false;
        }

        return true;
    }


    public void setScreenOrientation() {
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {

            @Override
            public void onActivityCreated(Activity activity,
                                          Bundle savedInstanceState) {
                try {
                    activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                } catch (Exception e) {

                }
                activity.setTitle(getResources().getString(R.string.app_name));

                setCurrentAct(activity);
                Utils.runGC();

                activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
                activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

                if (activity instanceof MainActivity || activity instanceof DriverArrivedActivity || activity instanceof ActiveTripActivity || activity instanceof LiveTaskListActivity) {
                    //Reset PubNub instance
                    configPuSubInstance();
                }
            }

            @Override
            public void onActivityStarted(Activity activity) {
                Utils.runGC();
            }

            @Override
            public void onActivityResumed(Activity activity) {

                setCurrentAct(activity);
                isAppInBackground = false;
                Utils.runGC();
                Utils.sendBroadCast(getApplicationContext(), Utils.BACKGROUND_APP_RECEIVER_INTENT_ACTION);
                LocalNotification.clearAllNotifications();

                if (currentAct instanceof MainActivity || currentAct instanceof DriverArrivedActivity || currentAct instanceof ActiveTripActivity || currentAct instanceof LiveTaskListActivity) {
                    ViewGroup viewGroup = (ViewGroup) currentAct.findViewById(android.R.id.content);
                    new Handler().postDelayed(() -> {
                        OpenNoLocationView.getInstance(currentAct, viewGroup).configView(false);
                    }, 1000);

                    checkForOverlay(activity);
                }

                configureAppBadgeFloat();


                if (generalFun.isUserLoggedIn()){

                 JSONObject   userProfileJsonObj = generalFun.getJsonObject(generalFun.retrieveValue(Utils.USER_PROFILE_JSON));

                    if (!requestPermissions.contains(android.Manifest.permission.ACCESS_FINE_LOCATION))
                        requestPermissions.add(android.Manifest.permission.ACCESS_FINE_LOCATION);
                    if (!requestPermissions.contains(android.Manifest.permission.ACCESS_COARSE_LOCATION))
                        requestPermissions.add(android.Manifest.permission.ACCESS_COARSE_LOCATION);
                    if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        if (!requestPermissions.contains(android.Manifest.permission.ACCESS_BACKGROUND_LOCATION))
                            requestPermissions.add(android.Manifest.permission.ACCESS_BACKGROUND_LOCATION);
                    }


                    String Packagetype = generalFun.getJsonValueStr("PACKAGE_TYPE", userProfileJsonObj);

                    if (!Packagetype.equalsIgnoreCase("STANDARD")){
                        if (!requestPermissions.contains(android.Manifest.permission.RECORD_AUDIO))
                            requestPermissions.add(android.Manifest.permission.RECORD_AUDIO);
                        if (!requestPermissions.contains(android.Manifest.permission.READ_PHONE_STATE))
                            requestPermissions.add(android.Manifest.permission.READ_PHONE_STATE);
                    }
                    if(!generalFun.isAllPermissionGranted(false,requestPermissions))
                    {
                        if (activity instanceof LauncherActivity){

                        }else {
                            new StartActProcess(activity).startAct(LauncherActivity.class);
                            activity.finish();
                        }

                    }
                }

            }

            @Override
            public void onActivityPaused(Activity activity) {

                isAppInBackground = true;
                Utils.runGC();
                Utils.sendBroadCast(getApplicationContext(), Utils.BACKGROUND_APP_RECEIVER_INTENT_ACTION);

                configureAppBadgeFloat();
            }

            @Override
            public void onActivityStopped(Activity activity) {
                Logger.d("AppBackground", "onStop");
                Utils.runGC();
            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {
                removeAllRunningInstances();
            }

            @Override
            public void onActivityDestroyed(Activity activity) {
                Utils.hideKeyboard(activity);
                Utils.runGC();

//                connectReceiver(false);

                if (activity instanceof DriverArrivedActivity && activity
                        == driverArrivedAct) {
                    driverArrivedAct = null;
                }
                if (activity instanceof MainActivity && activity == mainAct) {
                    mainAct = null;
                }
                if (activity instanceof ActiveTripActivity && activity == activeTripAct) {
                    activeTripAct = null;
                }

                if (activity instanceof LiveTaskListActivity && activity == liveTaskListAct) {
                    liveTaskListAct = null;
                }

                if ((activity instanceof DriverArrivedActivity && activity == driverArrivedAct) || (activity instanceof LiveTaskListActivity && activity == liveTaskListAct) || (activity instanceof MainActivity && activity == mainAct) || (activity instanceof ActiveTripActivity) && activity == activeTripAct) {
                    terminatePuSubInstance();
                }

            }


        });
    }

    private void connectReceiver(boolean isConnect) {
        if (isConnect && mNetWorkReceiver == null) {
            registerNetWorkReceiver();
        } else if (!isConnect && mNetWorkReceiver != null) {
            unregisterNetWorkReceiver();
        }
    }

    public Activity getCurrentAct() {
        return currentAct;
    }

    private void setCurrentAct(Activity currentAct) {
        this.currentAct = currentAct;

        if (currentAct instanceof LauncherActivity) {
            mainAct = null;
            driverArrivedAct = null;
            activeTripAct = null;
            liveTaskListAct = null;
        }

        if (currentAct instanceof MainActivity) {
            activeTripAct = null;
            driverArrivedAct = null;
            liveTaskListAct = null;
            mainAct = (MainActivity) currentAct;
        }

        if (currentAct instanceof DriverArrivedActivity) {
            mainAct = null;
            activeTripAct = null;
            liveTaskListAct = null;
            driverArrivedAct = (DriverArrivedActivity) currentAct;
        }

        if (currentAct instanceof ActiveTripActivity) {
            mainAct = null;
            driverArrivedAct = null;
            liveTaskListAct = null;
            activeTripAct = (ActiveTripActivity) currentAct;
        }
        if (currentAct instanceof LiveTaskListActivity) {
            activeTripAct = null;
            driverArrivedAct = null;
            mainAct = null;
            liveTaskListAct = (LiveTaskListActivity) currentAct;
        }
        connectReceiver(true);
    }

    private String extractLogToFile() {
        PackageManager manager = this.getPackageManager();
        PackageInfo info = null;
        try {
            info = manager.getPackageInfo(this.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e2) {
        }
        String model = Build.MODEL;
        if (!model.startsWith(Build.MANUFACTURER))
            model = Build.MANUFACTURER + " " + model;

        // Make file name - file must be saved to external storage or it wont be readable by
        // the email app.
        String path = Environment.getExternalStorageDirectory() + "/" + "MyApp/";
        String fullName = path + "Log";
        Logger.d("Api", "fullName" + fullName);
        // Extract to file.
        File file = new File(fullName);
        InputStreamReader reader = null;
        FileWriter writer = null;
        try {
            // For Android 4.0 and earlier, you will get all app's log output, so filter it to
            // mostly limit it to your app's output.  In later versions, the filtering isn't needed.
            String cmd = (Build.VERSION.SDK_INT <= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) ?
                    "logcat -d -v time MyApp:v dalvikvm:v System.err:v *:s" :
                    "logcat -d -v time";

            // get input stream
            Process process = Runtime.getRuntime().exec(cmd);
            reader = new InputStreamReader(process.getInputStream());

            // write output stream
            writer = new FileWriter(file);
            writer.write("Android version: " + Build.VERSION.SDK_INT + "\n");
            writer.write("Device: " + model + "\n");
            writer.write("App version: " + (info == null ? "(null)" : info.versionCode) + "\n");

            char[] buffer = new char[10000];
            do {
                int n = reader.read(buffer, 0, buffer.length);
                if (n == -1)
                    break;
                writer.write(buffer, 0, n);
            } while (true);

            reader.close();
            writer.close();
        } catch (IOException e) {
            if (writer != null)
                try {
                    writer.close();
                } catch (IOException e1) {
                }
            if (reader != null)
                try {
                    reader.close();
                } catch (IOException e1) {
                }

            // You might want to write a failure message to the log here.
            return null;
        }

        return fullName;
    }

    private void configPuSubInstance() {
        ConfigPubNub.getInstance(true).buildPubSub();
    }

    private void terminatePuSubInstance() {
        if (ConfigPubNub.retrieveInstance() != null) {
            ConfigPubNub.getInstance().releasePubSubInstance();
        }
    }

    public void restartWithGetDataApp() {
        GetUserData objRefresh = new GetUserData(generalFun, MyApp.getInstance().getCurrentAct());
        objRefresh.getData();
    }
    public void refreshWithConfigData()
    {
        GetUserData objRefresh = new GetUserData(generalFun, MyApp.getInstance().getCurrentAct());
        objRefresh.GetConfigData();
    }

    public void restartWithGetDataApp(boolean releaseCurrActInstance) {
        GetUserData objRefresh = new GetUserData(generalFun, MyApp.getInstance().getCurrentAct(), releaseCurrActInstance);
        objRefresh.getData();
    }

    private void configureAppBadgeFloat() {
        if (GetLocationUpdates.retrieveInstance() == null) {
            return;
        }

        new Handler().postDelayed(() -> {
            if (GetLocationUpdates.retrieveInstance() != null) {
                if (isMyAppInBackGround()) {
                    GetLocationUpdates.retrieveInstance().showAppBadgeFloat();
                } else {
                    GetLocationUpdates.retrieveInstance().hideAppBadgeFloat();
                }
            }
        }, 1000);
    }

    private void checkForOverlay(Activity act) {
        if (!generalFun.canDrawOverlayViews(act)) {
            if (drawOverlayAppAlert != null) {
                drawOverlayAppAlert.closeAlertBox();
                drawOverlayAppAlert = null;
            }

            GenerateAlertBox alertBox = new GenerateAlertBox(getCurrentAct(), false);
            drawOverlayAppAlert = alertBox;
            alertBox.setContentMessage(null, generalFun.retrieveLangLBl("Please enable draw over app permission.", "LBL_ENABLE_DRWA_OVER_APP"));
            alertBox.setPositiveBtn(generalFun.retrieveLangLBl("Allow", "LBL_ALLOW"));
            alertBox.setNegativeBtn(generalFun.retrieveLangLBl("Retry", "LBL_RETRY_TXT"));
            alertBox.setCancelable(false);
            alertBox.setBtnClickList(btn_id -> {
                if (btn_id == 1) {
                    (new StartActProcess(act)).requestOverlayPermission(Utils.OVERLAY_PERMISSION_REQ_CODE);
                } else {
                    checkForOverlay(act);
                }

            });
            alertBox.showAlertBox();
        }
    }

    public void notifySessionTimeOut() {
        if (generateSessionAlert != null) {
            return;
        }

        generateSessionAlert = new GenerateAlertBox(MyApp.getInstance().getCurrentAct());

        generateSessionAlert.setContentMessage(generalFun.retrieveLangLBl("", "LBL_BTN_TRIP_CANCEL_CONFIRM_TXT"),
                generalFun.retrieveLangLBl("Your session is expired. Please login again.", "LBL_SESSION_TIME_OUT"));
        generateSessionAlert.setPositiveBtn(generalFun.retrieveLangLBl("Ok", "LBL_BTN_OK_TXT"));
        generateSessionAlert.setCancelable(false);
        generateSessionAlert.setBtnClickList(btn_id -> {

            if (btn_id == 1) {
                onTerminate();
                generalFun.logOutUser(MyApp.this);

                try {
                    generateSessionAlert = null;
                } catch (Exception e) {

                }

                (new GeneralFunctions(getCurrentAct())).restartApp();
            }
        });

        generateSessionAlert.showSessionOutAlertBox();
    }

    public void logOutFromDevice(boolean isForceLogout) {

        if (generalFun != null) {
            final HashMap<String, String> parameters = new HashMap<String, String>();

            parameters.put("type", "callOnLogout");
            parameters.put("iMemberId", generalFun.getMemberId());
            parameters.put("UserType", Utils.userType);

            ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getCurrentAct(), parameters);
            exeWebServer.setLoaderConfig(getCurrentAct(), true, generalFun);

            exeWebServer.setDataResponseListener(responseString -> {
                JSONObject responseStringObject = generalFun.getJsonObject(responseString);

                if (responseStringObject != null && !responseStringObject.equals("")) {

                    boolean isDataAvail = GeneralFunctions.checkDataAvail(Utils.action_str, responseStringObject);

                    if (isDataAvail) {
                        onTerminate();
                        generalFun.logOutUser(MyApp.this);
                        (new GeneralFunctions(getCurrentAct())).restartApp();
                    } else {
                        if (isForceLogout) {
                            generalFun.showGeneralMessage("",
                                    generalFun.retrieveLangLBl("", generalFun.getJsonValueStr(Utils.message_str, responseStringObject)), buttonId -> (new GeneralFunctions(getCurrentAct())).restartApp());
                        } else {
                            generalFun.showGeneralMessage("",
                                    generalFun.retrieveLangLBl("", generalFun.getJsonValueStr(Utils.message_str, responseStringObject)));
                        }
                    }
                } else {
                    if (isForceLogout) {
                        generalFun.showError(buttonId -> (new GeneralFunctions(getCurrentAct())).restartApp());
                    } else {
                        generalFun.showError();
                    }
                }
            });
            exeWebServer.execute();
        }
    }
}
