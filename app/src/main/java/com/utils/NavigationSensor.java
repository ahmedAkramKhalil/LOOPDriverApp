package com.utils;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;

import com.general.files.GetLocationUpdates;
import com.general.files.MyApp;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.HashMap;

public class NavigationSensor implements SensorEventListener, GetLocationUpdates.LocationUpdatesListener {

    public static String ENABLE_NAVIGATION_MODE_DRIVER_APP = "No";
    public static String DRIVER_APP_NAVIGATION_MODE_STRATEGY = "DeviceSensor";
    public static int MINIMUM_DIFF_BW_AZIMUTH = 10;

    private static NavigationSensor instance;
    private static SensorManager mSensorManager;
    private Sensor accelerometer;
    private Sensor magnetometer;

    private float[] mGravity;
    private float[] mGeomagnetic;

    private static HashMap<Object, DeviceAngleChangeListener> listOfListener = new HashMap<>();

    private float mAzimuth = -1;

    private AnimateMarker animateMarker;

    private Location currentLocation = null;

    private boolean activateDispatch = false;
    private boolean isLocationModeStrategy = false;

    private NavigationSensor() {

        if (!ENABLE_NAVIGATION_MODE_DRIVER_APP.equalsIgnoreCase("Yes")) {

            try {
                listOfListener.clear();
                if (mSensorManager != null) {
                    mSensorManager.unregisterListener(this);
                    mSensorManager = null;
                }
            } catch (Exception ignored) {

            }

            return;
        }

        if (DRIVER_APP_NAVIGATION_MODE_STRATEGY.equalsIgnoreCase("Location")) {
            isLocationModeStrategy = true;
            return;
        }

        if (mSensorManager == null) {
            mSensorManager = (SensorManager) MyApp.getInstance().getSystemService(Context.SENSOR_SERVICE);
        }

        if (accelerometer == null) {
            accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }

        if (magnetometer == null) {
            magnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        }

        try {
            if (mSensorManager != null) {
                mSensorManager.unregisterListener(this);
            }
        } catch (Exception ignored) {

        }

        try {
            if (mSensorManager != null && accelerometer != null) {
                mSensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
            }
        } catch (Exception ignored) {

        }

        try {
            if (mSensorManager != null && magnetometer != null) {
                mSensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_NORMAL);
            }
        } catch (Exception ignored) {

        }
    }

    public static NavigationSensor getInstance() {
        if (instance == null) {
            instance = new NavigationSensor();
        }
        return instance;
    }

    public static NavigationSensor retrieveInstance() {
        return instance;
    }

    public void activateSensor(Object obj, DeviceAngleChangeListener mDeviceAngleChangeListener) {
        if (obj == null || mDeviceAngleChangeListener == null) {
            return;
        }

        if (DRIVER_APP_NAVIGATION_MODE_STRATEGY.equalsIgnoreCase("Location")) {
            GetLocationUpdates.getInstance().startLocationUpdates(this, this);
        }

        listOfListener.put(obj, mDeviceAngleChangeListener);

        if (mAzimuth != -1) {
            mDeviceAngleChangeListener.onDeviceAngleChanged(mAzimuth);
        }
    }

    public void deActivateSensor(Object obj) {
        if (NavigationSensor.retrieveInstance() == null || obj == null) {
            return;
        }
        listOfListener.remove(obj);
        GetLocationUpdates.getInstance().stopLocationUpdates(this);
    }

    public void configSensor(boolean activateDispatch) {
        this.activateDispatch = activateDispatch;

        if (activateDispatch) {
            if (GetLocationUpdates.retrieveInstance() != null) {
                GetLocationUpdates.getInstance().startLocationUpdates(this, this);
            }
        } else {
            if (GetLocationUpdates.retrieveInstance() != null) {
                GetLocationUpdates.getInstance().stopLocationUpdates(this);
            }
        }
    }

    public static void destroySensor() {
        if (NavigationSensor.retrieveInstance() == null) {
            return;
        }

        NavigationSensor.listOfListener.clear();

        try {
            if (GetLocationUpdates.retrieveInstance() != null) {
                GetLocationUpdates.getInstance().stopLocationUpdates(NavigationSensor.retrieveInstance());
            }
        } catch (Exception ignored) {

        }

        try {
            listOfListener.clear();
            if (mSensorManager != null) {
                mSensorManager.unregisterListener(NavigationSensor.retrieveInstance());
                mSensorManager = null;
            }
        } catch (Exception ignored) {

        }
        instance = null;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            mGravity = event.values;
        }

        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            mGeomagnetic = event.values;
        }

        if (mGravity != null && mGeomagnetic != null) {
            float[] R = new float[9];
            float[] I = new float[9];
            boolean success = SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic);
            if (success) {
                float[] orientation = new float[3];
                SensorManager.getOrientation(R, orientation);

                float azimuthInRadians = orientation[0];
                float azimuthInDegrees = ((float) Math.toDegrees(azimuthInRadians) + 360);

                if (Math.abs(this.mAzimuth - azimuthInDegrees) > MINIMUM_DIFF_BW_AZIMUTH) {
                    this.mAzimuth = azimuthInDegrees;
                    dispatchAzimuth();
                }

            }
        }
    }

    private void dispatchAzimuth() {
        ArrayList<Object> keyOfListenerList = new ArrayList<>();
        for (Object currentKey : listOfListener.keySet()) {
            try {
                if (listOfListener.get(currentKey) != null) {
                    DeviceAngleChangeListener listener = listOfListener.get(currentKey);
                    if (listener != null) {
                        listener.onDeviceAngleChanged(this.mAzimuth);
                    }
                }
            } catch (Exception e) {
                try {
                    keyOfListenerList.add(currentKey);
                } catch (Exception ignored) {
                }
            }
        }

        try {

            if (keyOfListenerList.size() > 0) {
                for (int i = 0; i < keyOfListenerList.size(); i++) {
                    listOfListener.remove(keyOfListenerList.get(i));
                }
            }
        } catch (Exception ignored) {
        }
    }

    public float getCurrentBearing() {
        if (!activateDispatch) {
            return -1;
        }
        return this.mAzimuth;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public void onLocationUpdate(Location location) {
        if (location == null) {
            return;
        }

        if (animateMarker == null) {
            animateMarker = new AnimateMarker();
        }

        if (currentLocation == null) {
            currentLocation = location;
        }

        if (isLocationModeStrategy && currentLocation.distanceTo(location) > MINIMUM_DIFF_BW_AZIMUTH) {
            this.mAzimuth = (float) animateMarker.bearingBetweenLocations(new LatLng(this.currentLocation.getLatitude(), this.currentLocation.getLongitude()), new LatLng(location.getLatitude(), location.getLongitude()));
            this.currentLocation = location;
            dispatchAzimuth();
        }
    }

    public interface DeviceAngleChangeListener {
        void onDeviceAngleChanged(float azimuth);
    }
}
