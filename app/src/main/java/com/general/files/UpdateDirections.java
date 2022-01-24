package com.general.files;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.text.TextUtils;
import android.view.View;

import com.taxifgo.driver.ActiveTripActivity;
import com.taxifgo.driver.DriverArrivedActivity;

import com.taxifgo.driver.MainActivity;
import com.taxifgo.driver.R;
import com.taxifgo.driver.deliverAll.TrackOrderActivity;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Dash;
import com.google.android.gms.maps.model.Gap;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.PatternItem;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.RoundCap;
import com.google.maps.android.SphericalUtil;
import com.utils.CommonUtilities;
import com.utils.Logger;
import com.utils.Utils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Admin on 02-08-2017.
 */

//public class UpdateDirections implements GetLocationUpdates.LocationUpdates, UpdateFrequentTask.OnTaskRunCalled {
public class UpdateDirections implements UpdateFrequentTask.OnTaskRunCalled, MapDelegate {

    public GoogleMap googleMap;
    public Location destinationLocation;
    public LatLng sourceLocation;
    public Context mcontext;
    public Location userLocation;
    public Intent data;

    GeneralFunctions generalFunctions;

    String serverKey;
    Polyline route_polyLine;

    UpdateFrequentTask updateFreqTask;

    String gMapLngCode = "en";
    JSONObject userProfileJsonObj;
    String eUnit = "KMs";
    int DRIVER_ARRIVED_MIN_TIME_PER_MINUTE = 3;

    /*Deliver all*/
    Marker placeMarker, driverMarker;
    boolean isCalledFromDeliverAll;
    public List<Double> lattitudeList = new ArrayList<>();
    public List<Double> longitudeList = new ArrayList<>();
    private boolean eFly;
    private final static double DEFAULT_CURVE_ROUTE_CURVATURE = 0.5f;
    private final static int DEFAULT_CURVE_POINTS = 60;

    public UpdateDirections(Context mcontext, GoogleMap googleMap, Location userLocation, Location destinationLocation) {
        this.googleMap = googleMap;
        this.destinationLocation = destinationLocation;
        this.mcontext = mcontext;
        this.userLocation = userLocation;

        generalFunctions = MyApp.getInstance().getGeneralFun(mcontext);

        serverKey = generalFunctions.retrieveValue(Utils.GOOGLE_SERVER_ANDROID_DRIVER_APP_KEY);

        gMapLngCode = generalFunctions.retrieveValue(Utils.GOOGLE_MAP_LANGUAGE_CODE_KEY);

        userProfileJsonObj = generalFunctions.getJsonObject(generalFunctions.retrieveValue(Utils.USER_PROFILE_JSON));
        eUnit = generalFunctions.getJsonValueStr("eUnit", userProfileJsonObj);
        DRIVER_ARRIVED_MIN_TIME_PER_MINUTE = generalFunctions.parseIntegerValue(3, generalFunctions.getJsonValueStr("DRIVER_ARRIVED_MIN_TIME_PER_MINUTE", userProfileJsonObj));
        lattitudeList.clear();
        longitudeList.clear();
    }

    public void iseFly(boolean eFly, LatLng sourceLocation) {
        this.eFly = eFly;
        this.sourceLocation = sourceLocation;

    }

    public void isDeliverAll(boolean isFromDeliverAll) {
        this.isCalledFromDeliverAll = isFromDeliverAll;
    }

    public void setMarkers(Marker placeMarker, Marker driverMarker) {
        this.driverMarker = driverMarker;
        this.placeMarker = placeMarker;

    }

    public void scheduleDirectionUpdate() {

        releaseTask();
        String DESTINATION_UPDATE_TIME_INTERVAL = generalFunctions.retrieveValue("DESTINATION_UPDATE_TIME_INTERVAL");
        updateFreqTask = new UpdateFrequentTask((int) (generalFunctions.parseDoubleValue(2, DESTINATION_UPDATE_TIME_INTERVAL) * 60 * 1000));
        updateFreqTask.setTaskRunListener(this);
        updateFreqTask.startRepeatingTask();

    }

    public void releaseTask() {
        Logger.d("Task", "::releaseTask called");
        if (updateFreqTask != null) {
            updateFreqTask.stopRepeatingTask();
            updateFreqTask = null;
        }

        Utils.runGC();


    }

    public void changeDestLoc(Location destinationLocation) {
        this.destinationLocation = destinationLocation;

    }

    public static String formatHoursAndMinutes(int totalMinutes) {
        String minutes = Integer.toString(totalMinutes % 60);
        minutes = minutes.length() == 1 ? "0" + minutes : minutes;
        return (totalMinutes / 60) + ":" + minutes;
    }


    public String getTimeTxt(int duration) {

        if (duration < 1) {
            duration = 1;
        }
        String durationTxt = "";
        String timeToreach = duration == 0 ? "--" : "" + duration;

        timeToreach = duration > 60 ? formatHoursAndMinutes(duration) : timeToreach;


        durationTxt = (duration < 60 ? generalFunctions.retrieveLangLBl("", "LBL_MINS_SMALL") : generalFunctions.retrieveLangLBl("", "LBL_HOUR_TXT"));

        durationTxt = duration == 1 ? generalFunctions.retrieveLangLBl("", "LBL_MIN_SMALL") : durationTxt;
        durationTxt = duration > 120 ? generalFunctions.retrieveLangLBl("", "LBL_HOURS_TXT") : durationTxt;

        return timeToreach + " " + durationTxt;
    }

    public void updateDirections() {

        if (userLocation == null || destinationLocation == null) {
            return;
        }

        if (userProfileJsonObj != null && (!generalFunctions.getJsonValueStr("ENABLE_DIRECTION_SOURCE_DESTINATION_DRIVER_APP", userProfileJsonObj).equalsIgnoreCase("Yes") || eFly)) {


            if (destinationLocation != null) {

                if (mcontext instanceof MainActivity) {
                    MainActivity mainActivity = (MainActivity) mcontext;
                    mainActivity.isRouteDrawn();
                }


                double distance = GeneralFunctions.calculationByLocation(userLocation.getLatitude(), userLocation.getLongitude(), destinationLocation.getLatitude(), destinationLocation.getLongitude(), "KM");

                Logger.d("Checkdistance","::"+distance);



                if (userProfileJsonObj != null && !generalFunctions.getJsonValueStr("eUnit", userProfileJsonObj).equalsIgnoreCase("KMs")) {
                    distance = distance * 0.000621371;
                }

                distance = generalFunctions.round(distance, 2);

                int lowestTime = ((int) (distance * DRIVER_ARRIVED_MIN_TIME_PER_MINUTE));

                if (lowestTime < 1) {
                    lowestTime = 1;
                }
                if (mcontext instanceof DriverArrivedActivity) {
                    DriverArrivedActivity driverArrivedActivity = (DriverArrivedActivity) mcontext;
//                                driverArrivedActivity.setTimetext(String.format("%.2f", (float) distance_final) + "", time);
                    driverArrivedActivity.setTimetext(generalFunctions.formatUpto2Digit(distance) + "", getTimeTxt(lowestTime));
                    driverArrivedActivity.getMap().setPadding(15, 15, 15, 15);
                } else if (mcontext instanceof ActiveTripActivity) {
                    if (destinationLocation.getLatitude() > 0) {
                        ActiveTripActivity activeTripActivity = (ActiveTripActivity) mcontext;

//                                activeTripActivity.setTimetext(String.format("%.2f", (float) distance_final) + "", time);
                        activeTripActivity.setTimetext(generalFunctions.formatUpto2Digit(distance) + "", getTimeTxt(lowestTime));
                        activeTripActivity.getMap().setPadding(15, 15, 15, 15);
                    }
                } else if (mcontext instanceof TrackOrderActivity) {
                    TrackOrderActivity trackOrderActivity = (TrackOrderActivity) mcontext;

                    trackOrderActivity.setTimetext(generalFunctions.formatUpto2Digit(distance) + "", getTimeTxt(lowestTime));
                }


            }


            if (!eFly) {
                return;
            }

        }


        String originLoc = userLocation.getLatitude() + "," + userLocation.getLongitude();
        String destLoc = destinationLocation.getLatitude() + "," + destinationLocation.getLongitude();

        Logger.d("CheckFly","::"+eFly);
        if (eFly) {
            PolylineOptions lineOptions = new PolylineOptions();
           /* ArrayList<LatLng> points = new ArrayList<LatLng>();
            points.add(sourceLocation);
            points.add(new LatLng(userLocation.getLatitude(), userLocation.getLongitude()));
            points.add(new LatLng(destinationLocation.getLatitude(), destinationLocation.getLongitude()));
            lineOptions.addAll(points);*/

            lineOptions = createCurveRoute(new LatLng(userLocation.getLatitude(), userLocation.getLongitude()), new LatLng(destinationLocation.getLatitude(), destinationLocation.getLongitude()));
            lineOptions.width(Utils.dipToPixels(mcontext, 4));
            lineOptions.color(R.color.black);

            if (lineOptions != null) {
                if (route_polyLine != null) {
                    route_polyLine.remove();
                }
                route_polyLine = googleMap.addPolyline(lineOptions);
                route_polyLine.setColor(R.color.black);
            }

           /* if (route_polyLine != null && route_polyLine.getPoints().size() > 1) {

                if (mcontext instanceof ActiveTripActivity) {
                    ActiveTripActivity activeTripActivity = (ActiveTripActivity) mcontext;
                    MapAnimator.getInstance().animateRoute(activeTripActivity.getMap(), route_polyLine.getPoints(), activeTripActivity.getActContext());
                }

            }*/

            return;
        }


        //  String directionURL = "https://maps.googleapis.com/maps/api/directions/json?origin=" + originLoc + "&destination=" + destLoc + "&sensor=true&key=" + serverKey + "&language=" + gMapLngCode + "&sensor=true";
        HashMap<String, String> hashMap = new HashMap<>();


        hashMap.put("s_latitude", userLocation.getLatitude() + "");
        hashMap.put("s_longitude", userLocation.getLongitude() + "");
        hashMap.put("d_latitude", destinationLocation.getLatitude() + "");
        hashMap.put("d_longitude", destinationLocation.getLongitude() + "");


//        if (userProfileJson != null && !eUnit.equalsIgnoreCase("KMs")) {
//            directionURL = directionURL + "&units=imperial";
//        }

        String trip_data = generalFunctions.getJsonValueStr("TripDetails", userProfileJsonObj);

        String eTollSkipped = generalFunctions.getJsonValue("eTollSkipped", trip_data);

        if (eTollSkipped == "Yes") {
            //  directionURL = directionURL + "&avoid=tolls";
            hashMap.put("toll_avoid", "Yes");
        }

        String parameters = "origin=" + originLoc + "&destination=" + destLoc;

        hashMap.put("parameters", parameters);

        Activity activity = (Activity) mcontext;
        if (activity instanceof MainActivity) {
            MapServiceApi.getDirectionservice(mcontext, hashMap, this, true);

        } else {
            MapServiceApi.getDirectionservice(mcontext, hashMap, this, false);

        }

//        if (userProfileJsonObj != null && !eUnit.equalsIgnoreCase("KMs")) {
//            directionURL = directionURL + "&units=imperial";
//        }
//
//        String trip_data = generalFunctions.getJsonValueStr("TripDetails", userProfileJsonObj);
//
//        String eTollSkipped = generalFunctions.getJsonValue("eTollSkipped", trip_data);
//
//        if (eTollSkipped == "Yes") {
//            directionURL = directionURL + "&avoid=tolls";
//        }
//
//        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(mcontext, directionURL, true);
//        exeWebServer.setDataResponseListener(responseString -> {
//
//            if (responseString != null && !responseString.equals("")) {
//
//                String status = generalFunctions.getJsonValue("status", responseString);
//
//                if (status.equals("OK")) {
//
//                    JSONArray obj_routes = generalFunctions.getJsonArray("routes", responseString);
//                    if (obj_routes != null && obj_routes.length() > 0) {
//                        JSONObject obj_legs = generalFunctions.getJsonObject(generalFunctions.getJsonArray("legs", generalFunctions.getJsonObject(obj_routes, 0).toString()), 0);
//
//                        if (mcontext instanceof MainActivity) {
//                            MainActivity mainActivity = (MainActivity) mcontext;
//                            mainActivity.isRouteDrawn();
//                        }
//
//                        String distance = "" + generalFunctions.getJsonValue("value",
//                                generalFunctions.getJsonValue("distance", obj_legs.toString()).toString());
//                        String time = "" + generalFunctions.getJsonValue("value",
//                                generalFunctions.getJsonValue("duration", obj_legs.toString()).toString());
//
//                        double distance_final = generalFunctions.parseDoubleValue(0.0, distance);
//
//
//                        if (userProfileJsonObj != null && !generalFunctions.getJsonValueStr("eUnit", userProfileJsonObj).equalsIgnoreCase("KMs")) {
//                            distance_final = distance_final * 0.000621371;
//                        } else {
//                            distance_final = distance_final * 0.00099999969062399994;
//                        }
//
//                        distance_final = generalFunctions.round(distance_final, 2);
//
//
//                        if (mcontext instanceof DriverArrivedActivity) {
//                            DriverArrivedActivity driverArrivedActivity = (DriverArrivedActivity) mcontext;
////                                driverArrivedActivity.setTimetext(String.format("%.2f", (float) distance_final) + "", time);
//
//                            driverArrivedActivity.setTimetext(generalFunctions.formatUpto2Digit(distance_final) + "", getTimeTxt((GeneralFunctions.parseIntegerValue(0, time) / 60)));
//                        } else if (mcontext instanceof ActiveTripActivity) {
//                            ActiveTripActivity activeTripActivity = (ActiveTripActivity) mcontext;
////                                activeTripActivity.setTimetext(String.format("%.2f", (float) distance_final) + "", time);
//                            activeTripActivity.setTimetext(generalFunctions.formatUpto2Digit(distance_final) + "", getTimeTxt((GeneralFunctions.parseIntegerValue(0, time) / 60)));
//                        } else if (mcontext instanceof TrackOrderActivity) {
//                            TrackOrderActivity trackOrderActivity = (TrackOrderActivity) mcontext;
//                            trackOrderActivity.setTimetext(generalFunctions.formatUpto2Digit(distance_final) + "", getTimeTxt((GeneralFunctions.parseIntegerValue(0, time) / 60)));
//                        }
//                    }
//
//
//                    PolylineOptions lineOptions = generalFunctions.getGoogleRouteOptions(responseString, Utils.dipToPixels(mcontext, 5), mcontext.getResources().getColor(R.color.black));
//
//                    if (mcontext instanceof MainActivity) {
//                        lattitudeList = new ArrayList<>();
//                        longitudeList = new ArrayList<>();
//
//
//                        for (int i = 0; i < lineOptions.getPoints().size(); i++) {
//                            lattitudeList.add(lineOptions.getPoints().get(i).latitude);
//                            longitudeList.add(lineOptions.getPoints().get(i).longitude);
//                        }
//
//                    }
//
//                    if (isCalledFromDeliverAll && driverMarker != null && placeMarker != null) {
//                        lineOptions
//                                .jointType(JointType.ROUND)
//                                .pattern(Arrays.asList(new Gap(20), new Dash(20)))
//                                .add(driverMarker.getPosition())
//                                .add(placeMarker.getPosition());
//                        lineOptions.width(Utils.dipToPixels(mcontext, 4));
//                    }
//
//                    if (lineOptions != null) {
//                        if (route_polyLine != null) {
//                            route_polyLine.remove();
//                        }
//                        route_polyLine = googleMap.addPolyline(lineOptions);
//
//                        if (isCalledFromDeliverAll) {
//                            route_polyLine.setColor(Color.parseColor("#cecece"));
//                            route_polyLine.setStartCap(new RoundCap());
//                            route_polyLine.setEndCap(new RoundCap());
//
//                            buildArcLine(new LatLng(userLocation.getLatitude(), userLocation.getLongitude()), new LatLng(destinationLocation.getLatitude(), destinationLocation.getLongitude()), 0.050);
//                        }
//
//                    }
//
//                }
//
//            }
//        });
//        exeWebServer.execute();
    }


    @Override
    public void onTaskRun() {
        Utils.runGC();
        Logger.d("Task", "::onTask called");
        updateDirections();
    }

    public void changeUserLocation(Location location) {
        if (location != null) {
            this.userLocation = location;
        }
    }

    public void setIntentData(Intent data) {

        this.data = data;

    }


    private void buildArcLine(LatLng p1, LatLng p2, double arcCurvature) {
        //Calculate distance and heading between two points
        double d = SphericalUtil.computeDistanceBetween(p1, p2);
        double h = SphericalUtil.computeHeading(p1, p2);

        if (h < 0) {
            LatLng tmpP1 = p1;
            p1 = p2;
            p2 = tmpP1;

            d = SphericalUtil.computeDistanceBetween(p1, p2);
            h = SphericalUtil.computeHeading(p1, p2);
        }

        //Midpoint position
        LatLng midPointLnt = SphericalUtil.computeOffset(p1, d * 0.5, h);

        //Apply some mathematics to calculate position of the circle center
        double x = (1 - arcCurvature * arcCurvature) * d * 0.5 / (2 * arcCurvature);
        double r = (1 + arcCurvature * arcCurvature) * d * 0.5 / (2 * arcCurvature);

        LatLng centerLnt = SphericalUtil.computeOffset(midPointLnt, x, h + 90.0);

        //Polyline options
        PolylineOptions options = new PolylineOptions();
        List<PatternItem> pattern = Arrays.<PatternItem>asList(new Dash(30), new Gap(20));

        //Calculate heading between circle center and two points
        double h1 = SphericalUtil.computeHeading(centerLnt, p1);
        double h2 = SphericalUtil.computeHeading(centerLnt, p2);

        //Calculate positions of points on circle border and add them to polyline options
        int numPoints = 100;
        double step = (h2 - h1) / numPoints;

        for (int i = 0; i < numPoints; i++) {
            LatLng middlePointTemp = SphericalUtil.computeOffset(centerLnt, r, h1 + i * step);
            options.add(middlePointTemp);
        }

//        if (!eDisplayDottedLine.equalsIgnoreCase("") && eDisplayDottedLine.equalsIgnoreCase("Yes")) {
        //Draw polyline ic_track_restaurant

        if (route_polyLine != null) {
            route_polyLine.remove();
            route_polyLine = null;
        }

        route_polyLine = googleMap.addPolyline(options.width(10).color(R.color.black).geodesic(false).pattern(pattern));

//        } else {
//            if (polyline != null) {
//                polyline.remove();
//                polyline = null;
//                gMap.clear();
//                if (restLatLng != null && userLatLng != null) {
//                    generateMapLocations(restLatLng.latitude, restLatLng.longitude, userLatLng.latitude, userLatLng.longitude);
//                }
//            }
//
//        }
    }

    public PolylineOptions createCurveRoute(LatLng origin, LatLng dest) {

        double distance = SphericalUtil.computeDistanceBetween(origin, dest);
        double heading = SphericalUtil.computeHeading(origin, dest);
        double halfDistance = distance > 0 ? (distance / 2) : (distance * DEFAULT_CURVE_ROUTE_CURVATURE);

        // Calculate midpoint position
        LatLng midPoint = SphericalUtil.computeOffset(origin, halfDistance, heading);

        // Calculate position of the curve center point
        double sqrCurvature = DEFAULT_CURVE_ROUTE_CURVATURE * DEFAULT_CURVE_ROUTE_CURVATURE;
        double extraParam = distance / (4 * DEFAULT_CURVE_ROUTE_CURVATURE);
        double midPerpendicularLength = (1 - sqrCurvature) * extraParam;
        double r = (1 + sqrCurvature) * extraParam;

        LatLng circleCenterPoint = SphericalUtil.computeOffset(midPoint, midPerpendicularLength, heading + 90.0);

        // Calculate heading between circle center and two points
        double headingToOrigin = SphericalUtil.computeHeading(circleCenterPoint, origin);

        // Calculate positions of points on the curve
        double step = Math.toDegrees(Math.atan(halfDistance / midPerpendicularLength)) * 2 / DEFAULT_CURVE_POINTS;
        //Polyline options
        PolylineOptions options = new PolylineOptions();

        for (int i = 0; i < DEFAULT_CURVE_POINTS; ++i) {
            LatLng pi = SphericalUtil.computeOffset(circleCenterPoint, r, headingToOrigin + i * step);
            options.add(pi);
        }
        return options;
    }

    @Override
    public void searchResult(ArrayList<HashMap<String, String>> placelist, int selectedPos, String input) {

    }

    @Override
    public void resetOrAddDest(int selPos, String address, double latitude, double longitude, String isSkip) {

    }

    boolean isGoogle = false;

    @Override
    public void directionResult(HashMap<String, String> directionlist) {

        String responseString = directionlist.get("routes");


        if (responseString != null && !responseString.equalsIgnoreCase("") && directionlist.get("distance") == null) {
            isGoogle = true;

//            JSONArray obj_routes = generalFunctions.getJsonArray(responseString);
            JSONArray obj_routes = generalFunctions.getJsonArray("routes", responseString);
            if (obj_routes != null && obj_routes.length() > 0) {
                JSONObject obj_legs = generalFunctions.getJsonObject(generalFunctions.getJsonArray("legs", generalFunctions.getJsonObject(obj_routes, 0).toString()), 0);


                if (mcontext instanceof MainActivity) {
                    MainActivity mainActivity = (MainActivity) mcontext;
                    mainActivity.isRouteDrawn();
                }
                String distance = "" + generalFunctions.getJsonValue("value",
                        generalFunctions.getJsonValue("distance", obj_legs.toString()).toString());
                String time = "" + generalFunctions.getJsonValue("value",
                        generalFunctions.getJsonValue("duration", obj_legs.toString()).toString());

                double distance_final = generalFunctions.parseDoubleValue(0.0, distance);


                if (userProfileJsonObj != null && !generalFunctions.getJsonValueStr("eUnit", userProfileJsonObj).equalsIgnoreCase("KMs")) {

                    distance_final = distance_final * 0.000621371;
                } else {
                    distance_final = distance_final * 0.00099999969062399994;
                }

                distance_final = generalFunctions.round(distance_final, 2);


                if (mcontext instanceof DriverArrivedActivity) {
                    DriverArrivedActivity driverArrivedActivity = (DriverArrivedActivity) mcontext;
//                                driverArrivedActivity.setTimetext(String.format("%.2f", (float) distance_final) + "", time);

                    driverArrivedActivity.setTimetext(generalFunctions.formatUpto2Digit(distance_final) + "", getTimeTxt((int)(GeneralFunctions.parseDoubleValue(0, time) / 60)));
                } else if (mcontext instanceof ActiveTripActivity) {
                    ActiveTripActivity activeTripActivity = (ActiveTripActivity) mcontext;
////                                activeTripActivity.setTimetext(String.format("%.2f", (float) distance_final) + "", time);
                    activeTripActivity.setTimetext(generalFunctions.formatUpto2Digit(distance_final) + "", getTimeTxt((int)(GeneralFunctions.parseDoubleValue(0, time) / 60)));
                } else if (mcontext instanceof TrackOrderActivity) {
                    TrackOrderActivity trackOrderActivity = (TrackOrderActivity) mcontext;
                    trackOrderActivity.setTimetext(generalFunctions.formatUpto2Digit(distance_final) + "", getTimeTxt((int)(GeneralFunctions.parseDoubleValue(0, time) / 60)));
                }


            }


            if (googleMap != null) {


                PolylineOptions lineOptions = generalFunctions.getGoogleRouteOptions(responseString, Utils.dipToPixels(mcontext, 5), mcontext.getResources().getColor(R.color.black));

                if (lineOptions != null) {
                    if (route_polyLine != null) {
                        route_polyLine.remove();
                    }
                    route_polyLine = googleMap.addPolyline(lineOptions);
                    if (mcontext instanceof MainActivity) {
                        MainActivity mainActivity = (MainActivity) mcontext;
                        if (mainActivity.mProgressBarEOD!=null){
                            mainActivity.mProgressBarEOD.setVisibility(View.GONE);
                        }

                        if (mainActivity.slideButtonEOD!=null){
                            mainActivity.slideButtonEOD.setVisibility(View.VISIBLE);
                        }

                        lattitudeList = new ArrayList<>();
                        longitudeList = new ArrayList<>();
                        for (int i = 0; i < lineOptions.getPoints().size(); i++) {
                            lattitudeList.add(lineOptions.getPoints().get(i).latitude);
                            longitudeList.add(lineOptions.getPoints().get(i).longitude);
                        }


                }
            }
        }





        } else {
            isGoogle = false;


            if (mcontext instanceof MainActivity) {
                MainActivity mainActivity = (MainActivity) mcontext;
                mainActivity.isRouteDrawn();
            }
            double distance_final = generalFunctions.parseDoubleValue(0.0, directionlist.get("distance"));

            if (userProfileJsonObj != null && !generalFunctions.getJsonValueStr("eUnit", userProfileJsonObj).equalsIgnoreCase("KMs")) {
                distance_final = distance_final * 0.000621371;
            } else {
                distance_final = distance_final * 0.00099999969062399994;
            }
            distance_final = generalFunctions.round(distance_final, 2);

            String time = directionlist.get("duration");

            int duration = (int) Math.round((generalFunctions.parseDoubleValue(0.0,
                    time) / 60));
            if (mcontext instanceof DriverArrivedActivity) {
                DriverArrivedActivity driverArrivedActivity = (DriverArrivedActivity) mcontext;
//                                driverArrivedActivity.setTimetext(String.format("%.2f", (float) distance_final) + "", time);



                driverArrivedActivity.setTimetext(generalFunctions.formatUpto2Digit(distance_final) + "", getTimeTxt(duration));
            } else if (mcontext instanceof ActiveTripActivity) {
                ActiveTripActivity activeTripActivity = (ActiveTripActivity) mcontext;
////                                activeTripActivity.setTimetext(String.format("%.2f", (float) distance_final) + "", time);


                activeTripActivity.setTimetext(generalFunctions.formatUpto2Digit(distance_final) + "", getTimeTxt(duration));
            } else if (mcontext instanceof TrackOrderActivity) {
                TrackOrderActivity trackOrderActivity = (TrackOrderActivity) mcontext;
                trackOrderActivity.setTimetext(generalFunctions.formatUpto2Digit(distance_final) + "", getTimeTxt(duration));
            }


            if (googleMap != null) {


                PolylineOptions lineOptions;
                if (isGoogle) {
                    lineOptions = generalFunctions.getGoogleRouteOptions(responseString, Utils.dipToPixels(mcontext, 5), mcontext.getResources().getColor(R.color.black));
                } else {

                    lineOptions = getGoogleRouteOptionsHandle(getRouteDetails(directionlist), Utils.dipToPixels(mcontext, 5), mcontext.getResources().getColor(R.color.black));
                }

                if (lineOptions != null) {
                    if (route_polyLine != null) {
                        route_polyLine.remove();
                    }
                    route_polyLine = googleMap.addPolyline(lineOptions);

                }
            }

        }


    }

    public String getRouteDetails(HashMap<String, String> directionlist)
    {
        HashMap<String, String> routeMap = new HashMap<>();
        routeMap.put("routes", directionlist.get("routes"));
        return routeMap.toString();
    }

    public PolylineOptions getGoogleRouteOptionsHandle(String directionJson, int width, int color) {
        PolylineOptions lineOptions = new PolylineOptions();


        try {
            JSONArray obj_routes1 = generalFunctions.getJsonArray("routes", directionJson);

            ArrayList<LatLng> points = new ArrayList<LatLng>();

            if (obj_routes1.length() > 0) {
                // Fetching i-th route
                // Fetching all the points in i-th route
                for (int j = 0; j < obj_routes1.length(); j++) {

                    JSONObject point = generalFunctions.getJsonObject(obj_routes1, j);

                    LatLng position = new LatLng(GeneralFunctions.parseDoubleValue(0, generalFunctions.getJsonValue("latitude", point).toString()), GeneralFunctions.parseDoubleValue(0, generalFunctions.getJsonValue("longitude", point).toString()));


                    points.add(position);

                }


                lineOptions.addAll(points);
                lineOptions.width(width);
                lineOptions.color(color);

                return lineOptions;
            } else {
                return null;
            }
        } catch (Exception e) {
            return null;
        }

    }

    @Override
    public void geoCodeAddressFound(String address, double latitude, double longitude, String geocodeobject) {

    }
}
