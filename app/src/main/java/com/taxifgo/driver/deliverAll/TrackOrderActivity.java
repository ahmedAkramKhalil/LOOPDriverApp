package com.taxifgo.driver.deliverAll;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RelativeLayout;

import com.taxifgo.driver.BaseActivity;
import com.taxifgo.driver.CallScreenActivity;
import com.taxifgo.driver.R;
import com.taxifgo.driver.WayBillActivity;
import com.general.files.AppFunctions;
import com.general.files.ExecuteWebServerUrl;
import com.general.files.GeneralFunctions;
import com.general.files.GetAddressFromLocation;
import com.general.files.GetLocationUpdates;
import com.general.files.InternetConnection;
import com.general.files.MyApp;
import com.general.files.SinchService;
import com.general.files.StartActProcess;
import com.general.files.UpdateDirections;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.maps.android.SphericalUtil;
import com.model.deliverAll.liveTaskListDataModel;
import com.model.deliverAll.orderDetailDataModel;
import com.sinch.android.rtc.calling.Call;
import com.utils.AnimateMarker;
import com.utils.Logger;
import com.utils.NavigationSensor;
import com.utils.Utils;
import com.view.MTextView;
import com.view.MyProgressDialog;
import com.view.simpleratingbar.SimpleRatingBar;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class TrackOrderActivity extends BaseActivity implements OnMapReadyCallback, GetLocationUpdates.LocationUpdatesListener, NavigationSensor.DeviceAngleChangeListener {


    public Location userLocation;
    public MTextView timeTxt, distanceTxt;
    GeneralFunctions generalFunc;
    MTextView titleTxt;
    String iOrderId = "";
    String tripId = "";
    SupportMapFragment map;
    GoogleMap gMap;

    MTextView addressTxt;
    boolean isDestinationAdded = false;
    double destLocLatitude = 0.0;
    double destLocLongitude = 0.0;
    double placeLocLatitude = 0.0;
    double placeLocLongitude = 0.0;
    Marker placeLocMarker = null;


    ArrayList<HashMap<String, String>> list;
    String required_str = "";
    String invalid_str = "";
    int i = 0;
    AlertDialog list_navigation;
    Menu menu;
    UpdateDirections updateDirections;
    Marker driverMarker;
    Marker destMapMarker;
    boolean isnotification = false;
    InternetConnection intCheck;
    // Gps Dialoge inside view
    JSONObject userProfileJsonObj;
    AnimateMarker animateMarker;
    boolean isCurrentLocationFocused = false;

    private String selectedType = "";
    private String vPhoneNo = "";
    private String vName = "";
    private HashMap<String, String> data_trip;
    private LatLng sourceLatLng;
    private LatLng destLatLng;
    Polyline route_polyLine;
    RelativeLayout wayBillImgView, deliveryInfoView;
    MTextView pickupNameTxt, recipientTxt;
    SimpleRatingBar ratingBar;
    ImageView callArea, navigateAreaUP;
    liveTaskListDataModel currentTaskData;
    orderDetailDataModel currentTaskData1;
    int icon;
    String address;
    String RestaurantLongitude = "", RestaurantLattitude = "", RestaurantAddress = "", RestaurantName = "", RestaurantNumber = "";
    String UserLongitude = "", UserLattitude = "", UserAddress = "", UserName = "", UserNumber = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_track_driver_location);
        Toolbar toolbar = findViewById(R.id.toolbar);
        new AppFunctions(getApplicationContext()).setOverflowButtonColor(toolbar, getResources().getColor(R.color.white));

        if (getIntent().getSerializableExtra("currentTaskData") instanceof liveTaskListDataModel) {
            currentTaskData = (liveTaskListDataModel) getIntent().getSerializableExtra("currentTaskData");
        } else {
            currentTaskData1 = (orderDetailDataModel) getIntent().getSerializableExtra("currentTaskData");
        }


        RestaurantLongitude = (currentTaskData != null ? currentTaskData.getRestaurantLongitude() : currentTaskData1.getRestaurantLongitude());
        UserLongitude = (currentTaskData != null ? currentTaskData.getUserLongitude() : currentTaskData1.getUserLongitude());
        UserLattitude = (currentTaskData != null ? currentTaskData.getUserLattitude() : currentTaskData1.getUserLatitude());
        RestaurantLattitude = (currentTaskData != null ? currentTaskData.getRestaurantLattitude() : currentTaskData1.getRestaurantLattitude());
        UserAddress = (currentTaskData != null ? currentTaskData.getUserAddress() : currentTaskData1.getUserAddress());
        RestaurantAddress = (currentTaskData != null ? currentTaskData.getRestaurantAddress() : currentTaskData1.getRestaurantAddress());
        RestaurantName = (currentTaskData != null ? currentTaskData.getRestaurantName() : currentTaskData1.getRestaurantName());
        UserName = (currentTaskData != null ? currentTaskData.getUserName() : currentTaskData1.getUserName());
        UserNumber = (currentTaskData != null ? currentTaskData.getUserNumber() : currentTaskData1.getUserPhone());
        RestaurantNumber = (currentTaskData != null ? currentTaskData.getRestaurantNumber() : currentTaskData1.getRestaurantNumber());

        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        animateMarker = new AnimateMarker();
        generalFunc = MyApp.getInstance().getGeneralFun(getActContext());

        animateMarker = new AnimateMarker();
        userProfileJsonObj = generalFunc.getJsonObject(generalFunc.retrieveValue(Utils.USER_PROFILE_JSON));

        isnotification = getIntent().getBooleanExtra("isnotification", isnotification);

        intCheck = new InternetConnection(getActContext());

        initView();

        setLabels();
        setData();


        map.getMapAsync(this);


        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) titleTxt.getLayoutParams();
        params.setMargins(Utils.dipToPixels(getActContext(), 50), 0, 0, 0);
       // titleTxt.setLayoutParams(params);

        titleTxt.setPaddingRelative(Utils.dipToPixels(getActContext(), 10), 0, 0, 0);


        if (savedInstanceState != null) {
            // Restore value of members from saved state
            String restratValue_str = savedInstanceState.getString("RESTART_STATE");

            if (restratValue_str != null && !restratValue_str.equals("") && restratValue_str.trim().equals("true")) {
                generalFunc.restartApp();
            }
        }

        GetLocationUpdates.getInstance().setTripStartValue(true, true, data_trip.get("iTripId"));
    }

    private void initView() {
        //gps view declaration end
        titleTxt = (MTextView) findViewById(R.id.titleTxt);
        addressTxt = (MTextView) findViewById(R.id.addressTxt);
        map = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapV2);
        timeTxt = (MTextView) findViewById(R.id.timeTxt);
        distanceTxt = (MTextView) findViewById(R.id.distanceTxt);
        ratingBar = (SimpleRatingBar) findViewById(R.id.ratingBar);
        (findViewById(R.id.backImgView)).setVisibility(View.VISIBLE);
        (findViewById(R.id.backImgView)).setOnClickListener(new setOnClickList());


        callArea = (ImageView) findViewById(R.id.callArea);

        navigateAreaUP = (ImageView) findViewById(R.id.navigateAreaUP);
        wayBillImgView = (RelativeLayout) findViewById(R.id.wayBillImgView);
        deliveryInfoView = (RelativeLayout) findViewById(R.id.deliveryInfoView);

        callArea.setBackground(getRoundBG("#3cca59"));

        navigateAreaUP.setBackground(getRoundBG("#ffa60a"));


        callArea.setOnClickListener(new setOnClickAct());
        wayBillImgView.setOnClickListener(new setOnClickAct());
        recipientTxt = (MTextView) findViewById(R.id.recipientTxt);
        pickupNameTxt = (MTextView) findViewById(R.id.nameTxt);

    }

    public void setTimetext(String distance, String time) {
        try {
            String userProfileJson = generalFunc.retrieveValue(Utils.USER_PROFILE_JSON);

            timeTxt.setVisibility(View.VISIBLE);
            distanceTxt.setVisibility(View.VISIBLE);

            if (userProfileJson != null && !generalFunc.getJsonValue("eUnit", userProfileJson).equalsIgnoreCase("KMs")) {
                distanceTxt.setText(generalFunc.convertNumberWithRTL(distance) + " " + generalFunc.retrieveLangLBl("", "LBL_MILE_DISTANCE_TXT") + " ");
                timeTxt.setText(generalFunc.convertNumberWithRTL(time) + " ");
            } else {
                distanceTxt.setText(generalFunc.convertNumberWithRTL(distance) + " " + generalFunc.retrieveLangLBl("", "LBL_KM_DISTANCE_TXT") + " ");
                timeTxt.setText(generalFunc.convertNumberWithRTL(time) + " ");
            }

        } catch (Exception e) {

        }
    }


    public void handleNoLocationDial() {
        if (generalFunc.isLocationEnabled()) {
            resetData();
        }
    }

    private void resetData() {
        if (intCheck.isNetworkConnected() && intCheck.check_int() && addressTxt.getText().equals(generalFunc.retrieveLangLBl("Loading address", "LBL_LOAD_ADDRESS"))) {
            setData();
        }

        if (!isCurrentLocationFocused) {
            setData();
            checkUserLocation();
        } else {
            checkUserLocation();
        }

        if (gMap == null && map != null && intCheck.isNetworkConnected() && intCheck.check_int())
            map.getMapAsync(this);
    }

    public void checkUserLocation() {

        if (generalFunc.isLocationEnabled() && (userLocation == null || userLocation.getLatitude() == 0.0 || userLocation.getLongitude() == 0.0)) {

            showprogress();

        } else {

            hideprogress();
        }
    }

    public void showprogress() {
        isCurrentLocationFocused = false;
        findViewById(R.id.errorLocArea).setVisibility(View.VISIBLE);
        findViewById(R.id.mProgressBar).setVisibility(View.VISIBLE);
        ((ProgressBar) findViewById(R.id.mProgressBar)).setIndeterminate(true);
        ((ProgressBar) findViewById(R.id.mProgressBar)).getIndeterminateDrawable().setColorFilter(
                getActContext().getResources().getColor(R.color.appThemeColor_1), android.graphics.PorterDuff.Mode.SRC_IN);
    }

    public void hideprogress() {
        findViewById(R.id.errorLocArea).setVisibility(View.GONE);
        if (findViewById(R.id.mProgressBar) != null) {
            findViewById(R.id.mProgressBar).setVisibility(View.GONE);
        }
    }


    public void setLabels() {
        titleTxt.setText(generalFunc.retrieveLangLBl("En Route", "LBL_EN_ROUTE_TXT"));
        timeTxt.setText("--" + generalFunc.retrieveLangLBl("to reach", "LBL_REACH_TXT"));
        required_str = generalFunc.retrieveLangLBl("", "LBL_FEILD_REQUIRD");
        invalid_str = generalFunc.retrieveLangLBl("Invalid value", "LBL_DIGIT_REQUIRE");

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        this.gMap = googleMap;

        if (generalFunc.checkLocationPermission(true)) {
            getMap().setMyLocationEnabled(true);
        } else {
            getMap().setMyLocationEnabled(false);
        }

        if (generalFunc.isRTLmode()) {
            getMap().setPadding(13, 0, 0, 0);
        } else {
            getMap().setPadding(13, 0, 150, 0);
        }


        getMap().getUiSettings().setTiltGesturesEnabled(false);
        getMap().getUiSettings().setCompassEnabled(false);
        getMap().getUiSettings().setMyLocationButtonEnabled(false);


        getMap().setOnMarkerClickListener(marker -> {
            marker.hideInfoWindow();
            return true;
        });

        checkUserLocation();

        if (userLocation != null && route_polyLine == null) {
            if (updateDirections != null) {
                Location destLoc = new Location("gps");
                destLoc.setLatitude(destLocLatitude);
                destLoc.setLongitude(destLocLongitude);
                updateDirections.changeUserLocation(destLoc);
            }
        }

        if (GetLocationUpdates.retrieveInstance() != null) {
            GetLocationUpdates.getInstance().stopLocationUpdates(this);
        }
        GetLocationUpdates.getInstance().setTripStartValue(true, true, data_trip.get("iTripId"));
        GetLocationUpdates.getInstance().startLocationUpdates(this, this);
    }


    public void setMapMarkerIcons() {

        if (getMap() == null) {
            return;
        }

        if (placeLocMarker != null) {
            placeLocMarker.remove();
        }

        if (destMapMarker != null) {
            destMapMarker.remove();
        }

        boolean isRest = selectedType.equalsIgnoreCase("trackRest");

        String address1 = isRest ? RestaurantAddress : UserAddress;
        int icon1 = isRest ? R.mipmap.ic_track_restaurant : R.mipmap.ic_track_user;

        destLatLng = new LatLng(destLocLatitude, destLocLongitude);
        sourceLatLng = new LatLng(placeLocLatitude, placeLocLongitude);


        LayoutInflater inflater = (LayoutInflater) getActContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View placeMarkerView = inflater.inflate(R.layout.deliverall_marker_view, null);

        ImageView placePinImgView = (ImageView) placeMarkerView.findViewById(R.id.pinImgView);
        placePinImgView.setImageResource(icon1);
        MTextView placeMarkerTxtView = (MTextView) placeMarkerView.findViewById(R.id.addressTxtView);
        placeMarkerTxtView.setText(address1);


        View userMarkerView = inflater.inflate(R.layout.deliverall_marker_view, null);
        ImageView userPinImgView = (ImageView) userMarkerView.findViewById(R.id.pinImgView);
        userPinImgView.setImageResource(icon);

        MTextView userMarkerTxtView = (MTextView) userMarkerView.findViewById(R.id.addressTxtView);
        userMarkerTxtView.setText(address);

        userMarkerTxtView.setVisibility(View.GONE);

        Marker sourceMarker = gMap.addMarker(new MarkerOptions().position(sourceLatLng).icon(BitmapDescriptorFactory.fromBitmap(Utils.getBitmapFromView(placeMarkerView))));
        placeLocMarker = sourceMarker;


        Marker destMarker = gMap.addMarker(new MarkerOptions().position(destLatLng).icon(BitmapDescriptorFactory.fromBitmap(Utils.getBitmapFromView(userMarkerView))));
        destMapMarker = destMarker;

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(sourceMarker.getPosition());
        if (destMarker != null) {
            builder.include(destMarker.getPosition());
        }

        gMap.animateCamera(getCameraUpdateFactory(builder));

    }

    private CameraUpdate getCameraUpdateFactory(LatLngBounds.Builder builder) {
        LatLngBounds bounds = builder.build();
        LatLng center = bounds.getCenter();
        LatLng northEast = SphericalUtil.computeOffset(center, 10 * Math.sqrt(2.0), SphericalUtil.computeHeading(center, bounds.northeast));
        LatLng southWest = SphericalUtil.computeOffset(center, 10 * Math.sqrt(2.0), (180 + (180 + SphericalUtil.computeHeading(center, bounds.southwest))));
        builder.include(southWest);
        builder.include(northEast);

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int width = metrics.widthPixels;
        int height = metrics.heightPixels;
        int padding = (int) (width * 0.32); // offset from edges of the map 10% of screen

        return CameraUpdateFactory.newLatLngBounds(bounds, width, height, padding);
    }

    public GoogleMap getMap() {
        return this.gMap;
    }

    public void setData() {


        HashMap<String, String> data = (HashMap<String, String>) getIntent().getSerializableExtra("TRIP_DATA");
        this.data_trip = data;

        iOrderId = data_trip.get("iOrderId");
        tripId = data_trip.get("iTripId");


        if (getIntent().hasExtra("type")) {
            selectedType = getIntent().getStringExtra("type");

            boolean isRest = selectedType.equalsIgnoreCase("trackRest");

            double vLattitude = GeneralFunctions.parseDoubleValue(0.0, isRest ? UserLattitude : RestaurantLattitude);
            double vLongitude = GeneralFunctions.parseDoubleValue(0.0, isRest ? UserLongitude : RestaurantLongitude);
            address = !isRest ? UserAddress : RestaurantAddress;
            vName = !isRest ? UserName : RestaurantName;
            vPhoneNo = !isRest ? UserNumber : RestaurantNumber;
            icon = isRest ? R.mipmap.ic_track_user : R.mipmap.ic_track_restaurant;

            pickupNameTxt.setText(vName);
            pickupNameTxt.setVisibility(View.VISIBLE);

            placeLocLatitude = GeneralFunctions.parseDoubleValue(0.0, isRest ? RestaurantLattitude : UserLattitude);
            placeLocLongitude = GeneralFunctions.parseDoubleValue(0.0, isRest ? RestaurantLongitude : UserLongitude);

            if (vLattitude != 0 && vLongitude != 0) {
                setDestinationPoint("" + vLattitude, "" + vLongitude, address, true);
            }
        }

        setMapMarkerIcons();

        if (generalFunc.isRTLmode()) {
            findViewById(R.id.navStripImgView).setRotation(180);
        }
        ((MTextView) findViewById(R.id.navigateTxt)).setText(generalFunc.retrieveLangLBl("", "LBL_NAVIGATE"));
    }


    @Override
    public void onLocationUpdate(Location location) {

        if (location == null) {
            return;
        }

        Logger.d("Api", "User's Current location" + location);

        if (userProfileJsonObj != null && generalFunc.getJsonValueStr("ENABLE_DIRECTION_SOURCE_DESTINATION_DRIVER_APP", userProfileJsonObj).equalsIgnoreCase("Yes")) {
            if (this.userLocation == null || !isCurrentLocationFocused) {
                isCurrentLocationFocused = true;
                this.userLocation = location;
             //   CameraPosition cameraPosition = cameraForUserPosition(true);
               // getMap().moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                getMap().moveCamera((new AppFunctions(getActContext())).getCameraPosition(location,gMap));
            } else {
                isCurrentLocationFocused = true;
                CameraPosition cameraPosition = cameraForUserPosition(false);
              //  getMap().animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), 1500, null);
                getMap().animateCamera((new AppFunctions(getActContext())).getCameraPosition(location,gMap),1500,null);
            }
        } else {

            try {
                this.userLocation = location;
                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                builder.include(new LatLng(userLocation.getLatitude(), userLocation.getLongitude()));
                builder.include(new LatLng(placeLocLatitude, placeLocLongitude));
                builder.include(new LatLng(destLocLatitude, destLocLongitude));

                getMap().animateCamera(getCameraUpdateFactory(builder));
            } catch (Exception e) {
                Logger.d("Exception", "::" + e.toString());

            }

        }

        updateDriverMarker(new LatLng(location.getLatitude(), location.getLongitude()));

        this.userLocation = location;
        checkUserLocation();

        if (updateDirections == null) {
            Location destLoc = new Location("gps");
            destLoc.setLatitude(placeLocLatitude);
            destLoc.setLongitude(placeLocLongitude);
            updateDirections = new UpdateDirections(getActContext(), gMap, userLocation, destLoc);
            updateDirections.setMarkers(placeLocMarker, destMapMarker);
            updateDirections.isDeliverAll(selectedType.equalsIgnoreCase("trackRest"));
            updateDirections.scheduleDirectionUpdate();

        }

        if (updateDirections != null) {
            updateDirections.changeUserLocation(location);
            updateDirections.setMarkers(placeLocMarker, destMapMarker);
        }

        if (sourceLatLng == null || destMapMarker == null) {
            setMapMarkerIcons();
        }

    }

    public void updateDriverMarker(final LatLng newLocation) {

        if (driverMarker == null) {

            int iconId = R.mipmap.car_driver_main;

            MarkerOptions markerOptions_driver = new MarkerOptions();
            markerOptions_driver.position(newLocation);
            markerOptions_driver.icon(BitmapDescriptorFactory.fromResource(iconId)).anchor(0.5f, 0.5f).flat(true);

            driverMarker = gMap.addMarker(markerOptions_driver);
            driverMarker.setTitle(generalFunc.getMemberId());
        }


        if (this.userLocation != null && newLocation != null) {
            LatLng currentLatLng = new LatLng(this.userLocation.getLatitude(), this.userLocation.getLongitude());
            float rotation = driverMarker == null ? 0 : driverMarker.getRotation();

            if (animateMarker.currentLng != null) {
                rotation = (float) animateMarker.bearingBetweenLocations(animateMarker.currentLng, newLocation);
            } else {
                rotation = (float) animateMarker.bearingBetweenLocations(currentLatLng, newLocation);
            }

            if (generalFunc.getJsonValueStr("APP_TYPE", userProfileJsonObj).equalsIgnoreCase("UberX")) {
                rotation = 0;
            }


            if (driverMarker != null) {
                driverMarker.setTitle(generalFunc.getMemberId());
            }

            HashMap<String, String> previousItemOfMarker = animateMarker.getLastLocationDataOfMarker(driverMarker);

            HashMap<String, String> data_map = new HashMap<>();
            data_map.put("vLatitude", "" + newLocation.latitude);
            data_map.put("vLongitude", "" + newLocation.longitude);
            data_map.put("iDriverId", "" + generalFunc.getMemberId());
            data_map.put("RotationAngle", "" + rotation);
            data_map.put("LocTime", "" + System.currentTimeMillis());

            Location location = new Location("marker");
            location.setLatitude(newLocation.latitude);
            location.setLongitude(newLocation.longitude);


            if (animateMarker.toPositionLat.get("" + newLocation.latitude) == null && animateMarker.toPositionLat.get("" + newLocation.longitude) == null) {
                if (previousItemOfMarker.get("LocTime") != null && !previousItemOfMarker.get("LocTime").equals("")) {

                    long previousLocTime = GeneralFunctions.parseLongValue(0, previousItemOfMarker.get("LocTime"));
                    long newLocTime = GeneralFunctions.parseLongValue(0, data_map.get("LocTime"));

                    if (previousLocTime != 0 && newLocTime != 0) {

                        if ((newLocTime - previousLocTime) > 0 && !animateMarker.driverMarkerAnimFinished) {
                            animateMarker.addToListAndStartNext(driverMarker, this.gMap, location, rotation, 1200, iOrderId, data_map.get("LocTime"));
                        } else if ((newLocTime - previousLocTime) > 0) {
                            animateMarker.animateMarker(driverMarker, this.gMap, location, rotation, 1200, iOrderId, data_map.get("LocTime"));
                        }

                    } else if ((previousLocTime == 0 || newLocTime == 0) && !animateMarker.driverMarkerAnimFinished) {
                        animateMarker.addToListAndStartNext(driverMarker, this.gMap, location, rotation, 1200, iOrderId, data_map.get("LocTime"));
                    } else {
                        animateMarker.animateMarker(driverMarker, this.gMap, location, rotation, 1200, iOrderId, data_map.get("LocTime"));
                    }
                } else if (!animateMarker.driverMarkerAnimFinished) {
                    animateMarker.addToListAndStartNext(driverMarker, this.gMap, location, rotation, 1200, iOrderId, data_map.get("LocTime"));
                } else {
                    animateMarker.animateMarker(driverMarker, this.gMap, location, rotation, 1200, iOrderId, data_map.get("LocTime"));
                }
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Utils.REQUEST_CODE_GPS_ON) {
            handleNoLocationDial();
        }
    }


    public CameraPosition cameraForUserPosition(boolean isFirst) {
        double currentZoomLevel = getMap().getCameraPosition().zoom;

        if (isFirst) {
            isCurrentLocationFocused = true;
            currentZoomLevel = Utils.defaultZomLevel;
        }
        CameraPosition cameraPosition = new CameraPosition.Builder().target(new LatLng(this.userLocation.getLatitude(), this.userLocation.getLongitude())).bearing(getMap().getCameraPosition().bearing)
                .zoom((float) currentZoomLevel).build();

        return cameraPosition;
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;

        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.trip_accept_menu, menu);

        menu.findItem(R.id.menu_message).setTitle(generalFunc.retrieveLangLBl("Message", "LBL_MESSAGE_ACTIVE_TRIP"));

        menu.findItem(R.id.menu_specialInstruction).setTitle(generalFunc.retrieveLangLBl("Special Instruction", "LBL_SPECIAL_INSTRUCTION_TXT"));

        menu.findItem(R.id.menu_sos).setTitle(generalFunc.retrieveLangLBl("Emergency or SOS", "LBL_EMERGENCY_SOS_TXT"));
        menu.findItem(R.id.menu_waybill_trip).setTitle(generalFunc.retrieveLangLBl("Way Bill", "LBL_MENU_WAY_BILL"));

        menu.findItem(R.id.menu_call).setVisible(true);
        menu.findItem(R.id.menu_call).setTitle(generalFunc.retrieveLangLBl("", "LBL_CALL_TXT"));

        menu.findItem(R.id.menu_passenger_detail).setVisible(false);
        menu.findItem(R.id.menu_message).setVisible(false);
        menu.findItem(R.id.menu_sos).setVisible(false);
        menu.findItem(R.id.menu_cancel_trip).setVisible(false);
        menu.findItem(R.id.menu_specialInstruction).setVisible(false);
        menu.findItem(R.id.menu_waybill_trip).setVisible(true);

        Utils.setMenuTextColor(menu.findItem(R.id.menu_passenger_detail), getResources().getColor(R.color.appThemeColor_TXT_1));
        Utils.setMenuTextColor(menu.findItem(R.id.menu_cancel_trip), getResources().getColor(R.color.appThemeColor_TXT_1));
        Utils.setMenuTextColor(menu.findItem(R.id.menu_waybill_trip), getResources().getColor(R.color.appThemeColor_TXT_1));
        Utils.setMenuTextColor(menu.findItem(R.id.menu_sos), getResources().getColor(R.color.appThemeColor_TXT_1));
        Utils.setMenuTextColor(menu.findItem(R.id.menu_call), getResources().getColor(R.color.appThemeColor_TXT_1));
        Utils.setMenuTextColor(menu.findItem(R.id.menu_message), getResources().getColor(R.color.appThemeColor_TXT_1));
        Utils.setMenuTextColor(menu.findItem(R.id.menu_specialInstruction), getResources().getColor(R.color.appThemeColor_TXT_1));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.menu_passenger_detail:
                return true;

            case R.id.menu_cancel_trip:
                return true;

            case R.id.menu_waybill_trip:
                Bundle bn4 = new Bundle();
                bn4.putSerializable("iOrderId", iOrderId);
                bn4.putSerializable("tripId", tripId);
                bn4.putString("eSystem", "yes");
                new StartActProcess(getActContext()).startActWithData(WayBillActivity.class, bn4);
                return true;

            case R.id.menu_sos:
                return true;

            case R.id.menu_call:
                if (generalFunc.getJsonValueStr("RIDE_DRIVER_CALLING_METHOD", userProfileJsonObj).equals("Voip")) {
                    sinchCall(getIntent().getBooleanExtra("isStore", false));
                } else {
                    getMaskNumber(vPhoneNo);

                }
                return true;

            case R.id.menu_message:
                return true;

            case R.id.menu_specialInstruction:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void sinchCall(boolean isStore) {


        if (!generalFunc.isCallPermissionGranted(false)) {
            generalFunc.isCallPermissionGranted(true);
        } else {
            if (new AppFunctions(getActContext()).checkSinchInstance(getSinchServiceInterface())) {
                HashMap<String, String> hashMap = new HashMap<>();


                hashMap.put("Id", generalFunc.getMemberId());
                hashMap.put("Name", generalFunc.getJsonValueStr("vName", userProfileJsonObj));
                hashMap.put("PImage", generalFunc.getJsonValueStr("vImage", userProfileJsonObj));
                hashMap.put("type", Utils.userType);
                hashMap.put("isDriver", "Yes");


                getSinchServiceInterface().getSinchClient().setPushNotificationDisplayName(generalFunc.retrieveLangLBl("", "LBL_INCOMING_CALL"));
                if (isStore) {
                    Call call = getSinchServiceInterface().callUser(Utils.CALLTOSTORE + "_" + getIntent().getStringExtra("callid"), hashMap);
                    String callId = call.getCallId();
                    Intent callScreen = new Intent(getActContext(), CallScreenActivity.class);
                    callScreen.putExtra(SinchService.CALL_ID, callId);
                    callScreen.putExtra("vImage", getIntent().getStringExtra("vImage"));
                    callScreen.putExtra("vName", RestaurantName);
                    startActivity(callScreen);

                } else {
                    Call call = getSinchServiceInterface().callUser(Utils.CALLTOPASSENGER + "_" + getIntent().getStringExtra("callid"), hashMap);
                    String callId = call.getCallId();
                    Intent callScreen = new Intent(getActContext(), CallScreenActivity.class);
                    callScreen.putExtra(SinchService.CALL_ID, callId);
                    callScreen.putExtra("vImage", getIntent().getStringExtra("vImage"));
                    callScreen.putExtra("vName", UserName);
                    startActivity(callScreen);

                }
            }

        }
    }

    public void getMaskNumber(String number) {
        if (generalFunc.getJsonValueStr("CALLMASKING_ENABLED", userProfileJsonObj).equalsIgnoreCase("Yes")) {
            HashMap<String, String> parameters = new HashMap<String, String>();
            parameters.put("type", "getCallMaskNumber");
            parameters.put("iOrderId", iOrderId);
            parameters.put("iTripid", tripId);
            parameters.put("UserType", Utils.userType);
            parameters.put("iMemberId", generalFunc.getMemberId());
            parameters.put("eSystem", Utils.eSystem_Type);

            ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(), parameters);
            exeWebServer.setLoaderConfig(getActContext(), true, generalFunc);

            exeWebServer.setDataResponseListener(responseString -> {
                JSONObject responseStringObject = generalFunc.getJsonObject(responseString);
                if (responseStringObject != null && !responseStringObject.equals("")) {

                    boolean isDataAvail = GeneralFunctions.checkDataAvail(Utils.action_str, responseStringObject);
                    if (isDataAvail) {
                        String message = generalFunc.getJsonValueStr(Utils.message_str, responseStringObject);
                        call(generalFunc.getJsonValue("PhoneNo", message));
                    } else {
                        call(number);
                    }

                } else {
                    generalFunc.showError();
                }
            });
            exeWebServer.execute();
        } else {
            call(number);
        }
    }

    public void call(String phoneNumber) {
        try {

            Intent callIntent = new Intent(Intent.ACTION_DIAL);
            callIntent.setData(Uri.parse("tel:" + phoneNumber));
            startActivity(callIntent);

        } catch (Exception e) {
            // TODO: handle exception
        }
    }

    public Context getActContext() {
        return TrackOrderActivity.this; // Must be context of activity not application
    }

    public void setDestinationPoint(String latitude, String longitude, String address, boolean isDestinationAdded) {
        double dest_lat = GeneralFunctions.parseDoubleValue(0.0, latitude);
        double dest_lon = GeneralFunctions.parseDoubleValue(0.0, longitude);

        (findViewById(R.id.navigationViewArea)).setVisibility(View.VISIBLE);
        // (findViewById(R.id.navigateArea)).setVisibility(View.VISIBLE);

        if (address.equals("")) {
            addressTxt.setText(generalFunc.retrieveLangLBl("Loading address", "LBL_LOAD_ADDRESS"));
            GetAddressFromLocation getAddressFromLocation = new GetAddressFromLocation(getActContext(), generalFunc);
            getAddressFromLocation.setLocation(dest_lat, dest_lon);
            getAddressFromLocation.setAddressList((address1, latitude1, longitude1,geocodeobject) -> addressTxt.setText(address1));
            getAddressFromLocation.execute();
        } else {
            addressTxt.setText(address);
        }

        // (findViewById(R.id.navigateArea)).setOnClickListener(new setOnClickAct("" + dest_lat, "" + dest_lon));

        this.isDestinationAdded = isDestinationAdded;
        this.destLocLatitude = dest_lat;
        this.destLocLongitude = dest_lon;
        navigateAreaUP.setOnClickListener(new setOnClickAct("" + dest_lat, "" + dest_lon));
        setMapMarkerIcons();
    }

    public MyProgressDialog showLoader() {
        MyProgressDialog myPDialog = new MyProgressDialog(getActContext(), false, generalFunc.retrieveLangLBl("Loading", "LBL_LOADING_TXT"));
        myPDialog.show();

        return myPDialog;
    }

    public void closeLoader(MyProgressDialog myPDialog) {
        myPDialog.close();
    }

    @Override
    protected void onResume() {
        super.onResume();

        NavigationSensor.getInstance().configSensor(true);
    }

    @Override
    protected void onPause() {
        super.onPause();

        NavigationSensor.getInstance().configSensor(false);
    }

    public void stopProcess() {
        if (updateDirections != null) {
            updateDirections.releaseTask();
            updateDirections = null;
        }

        if (GetLocationUpdates.retrieveInstance() != null) {
            GetLocationUpdates.retrieveInstance().stopLocationUpdates(this);
        }
        Utils.runGC();
    }


    private void stopAllProcess() {

        stopProcess();
    }

    public void openNavigationDialog(final String dest_lat, final String dest_lon) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActContext());

        LayoutInflater inflater = (LayoutInflater) getActContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View dialogView = inflater.inflate(R.layout.dialog_selectnavigation_view, null);

        final MTextView NavigationTitleTxt = (MTextView) dialogView.findViewById(R.id.NavigationTitleTxt);
        final MTextView wazemapTxtView = (MTextView) dialogView.findViewById(R.id.wazemapTxtView);
        final MTextView googlemmapTxtView = (MTextView) dialogView.findViewById(R.id.googlemmapTxtView);
        final RadioButton radiogmap = (RadioButton) dialogView.findViewById(R.id.radiogmap);
        final RadioButton radiowazemap = (RadioButton) dialogView.findViewById(R.id.radiowazemap);
        ImageView cancelImg = (ImageView) dialogView.findViewById(R.id.cancelImg);
        cancelImg.setOnClickListener(v -> {

            list_navigation.dismiss();
        });
        radiogmap.setOnClickListener(v -> googlemmapTxtView.performClick());

        radiowazemap.setOnClickListener(v -> wazemapTxtView.performClick());

        builder.setView(dialogView);
        NavigationTitleTxt.setText(generalFunc.retrieveLangLBl("Choose Option", "LBL_CHOOSE_OPTION"));
        googlemmapTxtView.setText(generalFunc.retrieveLangLBl("Google map navigation", "LBL_NAVIGATION_GOOGLE_MAP"));
        wazemapTxtView.setText(generalFunc.retrieveLangLBl("Waze navigation", "LBL_NAVIGATION_WAZE"));


        googlemmapTxtView.setOnClickListener(v -> {

            try {
                String url_view = "http://maps.google.com/maps?daddr=" + dest_lat + "," + dest_lon;
                (new StartActProcess(getActContext())).openURL(url_view, "com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
                list_navigation.dismiss();
            } catch (Exception e) {
                generalFunc.showMessage(wazemapTxtView, generalFunc.retrieveLangLBl("Please install Google Maps in your device.", "LBL_INSTALL_GOOGLE_MAPS"));
            }

        });

        wazemapTxtView.setOnClickListener(v -> {
            try {

                String uri = "waze://?ll=" + dest_lat + "," + dest_lon + "&navigate=yes";
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                startActivity(intent);
                list_navigation.dismiss();
            } catch (Exception e) {

                generalFunc.showMessage(wazemapTxtView, generalFunc.retrieveLangLBl("Please install Waze navigation app in your device.", "LBL_INSTALL_WAZE"));
            }
        });


        list_navigation = builder.create();
        if (generalFunc.isRTLmode() == true) {
            generalFunc.forceRTLIfSupported(list_navigation);
        }
        list_navigation.getWindow().setBackgroundDrawable(getResources().getDrawable(R.drawable.all_roundcurve_card));
        list_navigation.show();
        list_navigation.setOnCancelListener(dialogInterface -> Utils.hideKeyboard(getActContext()));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopAllProcess();
    }

    public class setOnClickList implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            Utils.hideKeyboard(TrackOrderActivity.this);

            Utils.hideKeyboard(TrackOrderActivity.this);
            if (view.getId() == R.id.backImgView) {
                TrackOrderActivity.super.onBackPressed();
//                generalFunc.restartApp();
            }
        }
    }

    public class setOnClickAct implements View.OnClickListener {

        String dest_lat = "";
        String dest_lon = "";

        public setOnClickAct(String dest_lat, String dest_lon) {
            this.dest_lat = dest_lat;
            this.dest_lon = dest_lon;
        }

        public setOnClickAct() {

        }

        @Override
        public void onClick(View view) {
            int i = view.getId();
            if (i == R.id.navigateAreaUP) {
                openNavigationDialog(dest_lat, dest_lon);
            } else if (i == R.id.callArea) {

                if (generalFunc.getJsonValueStr("RIDE_DRIVER_CALLING_METHOD", userProfileJsonObj).equals("Voip")) {
                    sinchCall(getIntent().getBooleanExtra("isStore", false));
                } else {
                    getMaskNumber(vPhoneNo);

                }
            } else if (i == R.id.wayBillImgView) {
                Bundle bn4 = new Bundle();
                bn4.putSerializable("iOrderId", iOrderId);
                bn4.putSerializable("tripId", tripId);
                bn4.putString("eSystem", "yes");
                new StartActProcess(getActContext()).startActWithData(WayBillActivity.class, bn4);
            }
        }
    }


    private GradientDrawable getRoundBG(String color) {

        int strokeWidth = 2;
        int strokeColor = Color.parseColor("#CCCACA");
        int fillColor = Color.parseColor(color);
        GradientDrawable gD = new GradientDrawable();
        gD.setColor(fillColor);
        gD.setShape(GradientDrawable.RECTANGLE);
        gD.setCornerRadius(100);
        gD.setStroke(strokeWidth, strokeColor);
        return gD;
    }

    @Override
    public void onDeviceAngleChanged(float azimuth) {

        if (gMap == null) {
            return;
        }

        getMap().animateCamera(CameraUpdateFactory.newCameraPosition(CameraPosition.builder(getMap().getCameraPosition()).bearing(azimuth).build()));
    }
}
