package com.general.files;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.IBinder;
import androidx.annotation.Nullable;

import com.utils.Logger;
import com.utils.Utils;

import java.util.HashMap;

/**
 * Created by Admin on 18-07-2016.
 */
public class UpdateDriverStatus extends Service implements UpdateFrequentTask.OnTaskRunCalled, GetLocationUpdates.LocationUpdatesListener {

    UpdateFrequentTask updateDriverStatusTask;
    Location driverLocation;
    String iDriverId = "";
    ExecuteWebServerUrl currentExeTask;
    GeneralFunctions generalFunc;
    Location lastPublishedLoc;
    double PUBSUB_PUBLISH_DRIVER_LOC_DISTANCE_LIMIT = 5;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        iDriverId = (MyApp.getInstance().getGeneralFun(this)).getMemberId();
        generalFunc = MyApp.getInstance().getAppLevelGeneralFunc();
        PUBSUB_PUBLISH_DRIVER_LOC_DISTANCE_LIMIT = GeneralFunctions.parseDoubleValue(5, generalFunc.retrieveValue(Utils.PUBSUB_PUBLISH_DRIVER_LOC_DISTANCE_LIMIT));
        updateDriverStatusTask = new UpdateFrequentTask(2 * 60 * 1000);
        updateDriverStatusTask.setTaskRunListener(this);
        updateDriverStatusTask.startRepeatingTask();

        Logger.d("GetLocationUpdates","::UpdateDriverStatus");

        if (GetLocationUpdates.retrieveInstance() != null) {
            GetLocationUpdates.getInstance().stopLocationUpdates(this);
        }

        GetLocationUpdates.getInstance().startLocationUpdates(this, this);

        return Service.START_STICKY;
    }

    @Override
    public void onTaskRun() {
        updateOnlineAvailability("");
    }

    public void updateOnlineAvailability(String status) {
        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "updateDriverStatus");
        parameters.put("iDriverId", iDriverId);

        if (driverLocation != null) {
            parameters.put("latitude", "" + driverLocation.getLatitude());
            parameters.put("longitude", "" + driverLocation.getLongitude());
        }

        if (status.equals("Not Available")) {
            parameters.put("Status", "Not Available");
        }

        if (this.currentExeTask != null) {
            this.currentExeTask.cancel(true);
            this.currentExeTask = null;
            Utils.runGC();
        }

        parameters.put("isUpdateOnlineDate", "true");

        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getApplicationContext(), parameters);
        this.currentExeTask = exeWebServer;
        exeWebServer.setDataResponseListener(responseString -> {

        });
        exeWebServer.execute();
    }

    @Override
    public void onLocationUpdate(Location location) {
        this.driverLocation = location;

        updateLocationToPubNubBeforeTrip();
    }

    public void stopFreqTask() {

        if (updateDriverStatusTask != null) {
            updateDriverStatusTask.stopRepeatingTask();
            updateDriverStatusTask = null;
        }
        Logger.d("GetLocationUpdates","::UpdateDriverStatusStop");
        if (GetLocationUpdates.retrieveInstance() != null) {
            GetLocationUpdates.getInstance().stopLocationUpdates(this);
        }


        Utils.runGC();
    }


    public void updateLocationToPubNubBeforeTrip() {
        if (driverLocation != null && driverLocation.getLongitude() != 0.0 && driverLocation.getLatitude() != 0.0) {


            if (lastPublishedLoc != null) {

                if (driverLocation.distanceTo(lastPublishedLoc) < PUBSUB_PUBLISH_DRIVER_LOC_DISTANCE_LIMIT) {
                    return;
                } else {
                    lastPublishedLoc = driverLocation;
                }

            } else {
                lastPublishedLoc = driverLocation;
            }


            ConfigPubNub.getInstance().publishMsg(generalFunc.getLocationUpdateChannel(), generalFunc.buildLocationJson(driverLocation));
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

        updateOnlineAvailability("Not Available");

        for (int i = 0; i < 100; i++) {

        }

        stopFreqTask();
    }
}
