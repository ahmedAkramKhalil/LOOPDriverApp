package com.general.files;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.IBinder;
import androidx.annotation.Nullable;

import com.utils.Logger;
import com.utils.Utils;

import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created by Admin on 20-07-2016.
 */
public class UpdateDriverLocationService extends Service implements UpdateFrequentTask.OnTaskRunCalled, GetLocationUpdates.LocationUpdatesListener {

    Location driverLocation;
    Location lastPublishedLocation;
    String iDriverId = "";
    ExecuteWebServerUrl currentExeTask;

    GeneralFunctions generalFunc;
    Location lastPublishedLoc = null;
    double PUBSUB_PUBLISH_DRIVER_LOC_DISTANCE_LIMIT = 5;
    DispatchDemoLocations dispatchDemoLoc;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        iDriverId = (MyApp.getInstance().getGeneralFun(this)).getMemberId();

        generalFunc = MyApp.getInstance().getGeneralFun(this);
        PUBSUB_PUBLISH_DRIVER_LOC_DISTANCE_LIMIT = GeneralFunctions.parseDoubleValue(5, generalFunc.retrieveValue(Utils.PUBSUB_PUBLISH_DRIVER_LOC_DISTANCE_LIMIT));

        if (GetLocationUpdates.retrieveInstance() != null) {
            GetLocationUpdates.getInstance().stopLocationUpdates(this);
        }

        GetLocationUpdates.getInstance().startLocationUpdates(this, this);
        JSONObject userProfileJsonObj = generalFunc.getJsonObject(generalFunc.retrieveValue(Utils.USER_PROFILE_JSON));

        if (generalFunc.getJsonValueStr("eEnableDemoLocDispatch", userProfileJsonObj).equalsIgnoreCase("Yes")) {
            dispatchDemoLoc = new DispatchDemoLocations(this);
            dispatchDemoLoc.startDispatchingLocations((latitude, longitude) -> {
                Location loc = new Location("gps");
                loc.setLatitude(latitude);
                loc.setLongitude(longitude);
                onLocationUpdate(loc);
            });
        }

        return Service.START_STICKY;
    }

    @Override
    public void onTaskRun() {
        updateDriverLocations();
    }

    public void updateDriverLocations() {
        if (driverLocation == null) {
            return;
        }

        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "updateDriverLocations");
        parameters.put("iDriverId", iDriverId);
        parameters.put("latitude", "" + driverLocation.getLatitude());
        parameters.put("longitude", "" + driverLocation.getLongitude());

        if (this.currentExeTask != null) {
            this.currentExeTask.cancel(true);
            this.currentExeTask = null;
            Utils.runGC();
        }

        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getApplicationContext(), parameters);
        this.currentExeTask = exeWebServer;
        exeWebServer.setDataResponseListener(responseString -> Logger.d("Api", "Update Locations Response ::" + responseString));
        exeWebServer.execute();
    }

    @Override
    public void onLocationUpdate(Location location) {
        this.driverLocation = location;

        if (generalFunc != null && driverLocation != null) {

            if (lastPublishedLocation == null || (lastPublishedLocation.distanceTo(driverLocation) > 2)) {
                lastPublishedLocation = driverLocation;
                if (lastPublishedLoc != null) {

                    if (driverLocation.distanceTo(lastPublishedLoc) < PUBSUB_PUBLISH_DRIVER_LOC_DISTANCE_LIMIT) {
                        return;
                    } else {
                        lastPublishedLoc = driverLocation;
                    }

                } else {
                    lastPublishedLoc = driverLocation;
                }


                ConfigPubNub.getInstance().publishMsg(generalFunc.getLocationUpdateChannel(), generalFunc.buildLocationJson(driverLocation, "LocationUpdateOnTrip"));
            }
        }
    }


    public void stopFreqTask() {
        if (GetLocationUpdates.retrieveInstance() != null) {
            GetLocationUpdates.getInstance().stopLocationUpdates(this);
        }
        if (dispatchDemoLoc != null) {
            dispatchDemoLoc.stopDispatchingDemoLocations();
            dispatchDemoLoc = null;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopFreqTask();
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        stopFreqTask();
    }
}
