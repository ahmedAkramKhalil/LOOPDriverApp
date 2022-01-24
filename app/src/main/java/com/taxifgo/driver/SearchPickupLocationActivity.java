package com.taxifgo.driver;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;

import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.general.files.ExecuteWebServerUrl;
import com.general.files.GeneralFunctions;
import com.general.files.GetAddressFromLocation;
import com.general.files.GetLocationUpdates;
import com.general.files.MyApp;
import com.general.files.StartActProcess;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.utils.Logger;
import com.utils.Utils;
import com.view.MButton;
import com.view.MTextView;
import com.view.MaterialRippleLayout;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;

public class SearchPickupLocationActivity extends AppCompatActivity implements OnMapReadyCallback, GetAddressFromLocation.AddressFound,
        GetLocationUpdates.LocationUpdatesListener, GoogleMap.OnCameraMoveStartedListener, GoogleMap.OnCameraIdleListener {

    MTextView titleTxt;
    ImageView backImgView;
    GeneralFunctions generalFunc;
    MButton btn_type2;
    int btnId;
    MTextView placeTxtView;
    boolean isPlaceSelected = false;
    LatLng placeLocation;
    SupportMapFragment map;
    GoogleMap gMap;
    GetAddressFromLocation getAddressFromLocation;
    private String TAG = SearchPickupLocationActivity.class.getSimpleName();
    public boolean isAddressEnable = false;
    private boolean isFirstLocation = true;
    private Location userLocation;
    boolean isListnersettoNull = false;
    boolean isAddressStore = false;
    boolean isbtnclick = false;


    MTextView homePlaceTxt;
    MTextView workPlaceTxt;

    String userHomeLocationLatitude_str;
    String userHomeLocationLongitude_str;
    String userWorkLocationLatitude_str;
    String userWorkLocationLongitude_str;
    String home_address_str;
    String work_address_str;
    JSONArray userFavouriteAddressArr;
    JSONObject userProfileJsonObj;

    String iUserFavAddressId = "";
    String eType = "";
    LinearLayout placeArea;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_pickup_location);

        generalFunc = MyApp.getInstance().getGeneralFun(getActContext());
        userProfileJsonObj = generalFunc.getJsonObject(generalFunc.retrieveValue(Utils.USER_PROFILE_JSON));
        userFavouriteAddressArr = generalFunc.getJsonArray("UserFavouriteAddress", userProfileJsonObj);


        titleTxt = (MTextView) findViewById(R.id.titleTxt);
        backImgView = (ImageView) findViewById(R.id.backImgView);
        btn_type2 = ((MaterialRippleLayout) findViewById(R.id.btn_type2)).getChildView();
        placeTxtView = (MTextView) findViewById(R.id.placeTxtView);

        map = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapV2);

        getAddressFromLocation = new GetAddressFromLocation(getActContext(), generalFunc);
        getAddressFromLocation.setAddressList(this);

        setLabels();

        titleTxt.setEllipsize(TextUtils.TruncateAt.MARQUEE);
        titleTxt.setSelected(true);
        titleTxt.setSingleLine(true);

        map.getMapAsync(SearchPickupLocationActivity.this);

        backImgView.setOnClickListener(new setOnClickAct());
        btnId = Utils.generateViewId();
        btn_type2.setId(btnId);

        btn_type2.setOnClickListener(new setOnClickAct());
        (findViewById(R.id.pickUpLocSearchArea)).setOnClickListener(new setOnClickAct());

        homePlaceTxt = (MTextView) findViewById(R.id.homePlaceTxt);
        workPlaceTxt = (MTextView) findViewById(R.id.workPlaceTxt);
        placeArea = (LinearLayout) findViewById(R.id.placeArea);


        checkLocations();

        if (userFavouriteAddressArr != null && userFavouriteAddressArr.length() > 0) {

            for (int i = 0; i < userFavouriteAddressArr.length(); i++) {
                JSONObject dataItem = generalFunc.getJsonObject(userFavouriteAddressArr, i);

                if (generalFunc.getJsonValueStr("eType", dataItem).equalsIgnoreCase(eType)) {
                    iUserFavAddressId = generalFunc.getJsonValueStr("iUserFavAddressId", dataItem);
                }
            }
        }
    }

    public void checkLocations() {

        HashMap<String,String> data=new HashMap<>();
        data.put("userHomeLocationAddress","");
        data.put("userHomeLocationLatitude","");
        data.put("userHomeLocationLongitude","");
        data.put("userWorkLocationAddress","");
        data.put("userWorkLocationLatitude","");
        data.put("userWorkLocationLongitude","");
        data=generalFunc.retrieveValue(data);

        home_address_str = data.get("userHomeLocationAddress");

        userHomeLocationLatitude_str = data.get("userHomeLocationLatitude");
        userHomeLocationLongitude_str = data.get("userHomeLocationLongitude");

        work_address_str = data.get("userWorkLocationAddress");
        userWorkLocationLatitude_str = data.get("userWorkLocationLatitude");
        userWorkLocationLongitude_str = data.get("userWorkLocationLongitude");

        if (getIntent().getStringExtra("isHome") != null && getIntent().getStringExtra("isHome").equals("true")) {

            eType = "HOME";
            if (home_address_str != null && !home_address_str.equals("")) {
                homePlaceTxt.setVisibility(View.VISIBLE);
                placeArea.setVisibility(View.GONE);
                (findViewById(R.id.seperationLine)).setVisibility(View.VISIBLE);
            }
        }
        if (getIntent().getStringExtra("isWork") != null && getIntent().getStringExtra("isWork").equals("true")) {

            eType = "WORK";
            if (work_address_str != null && !work_address_str.equals("")) {
                workPlaceTxt.setVisibility(View.VISIBLE);
                placeArea.setVisibility(View.GONE);
                (findViewById(R.id.seperationLine)).setVisibility(View.VISIBLE);
            }
        }
    }


    public void setLabels() {
        String isPickUpLoc=getIntent().getStringExtra("isPickUpLoc");
        if (isPickUpLoc != null && isPickUpLoc.equals("true")) {
            titleTxt.setText(generalFunc.retrieveLangLBl("", "LBL_SET_PICK_UP_LOCATION_TXT"));
        } else if (getIntent().getStringExtra("isHome") != null && getIntent().getStringExtra("isHome").equals("true")) {
            isAddressStore = true;
            titleTxt.setText(generalFunc.retrieveLangLBl("", "LBL_ADD_HOME_BIG_TXT"));
        } else if (getIntent().getStringExtra("isWork") != null && getIntent().getStringExtra("isWork").equals("true")) {
            isAddressStore = true;
            titleTxt.setText(generalFunc.retrieveLangLBl("", "LBL_ADD_WORK_HEADER_TXT"));
        } else {
            titleTxt.setText(generalFunc.retrieveLangLBl("", "LBL_SELECT_DESTINATION_HEADER_TXT"));
        }

        btn_type2.setText(generalFunc.retrieveLangLBl("", "LBL_ADD_LOC"));
        placeTxtView.setText(generalFunc.retrieveLangLBl("", "LBL_SEARCH_PLACE_HINT_TXT"));
    }

    @Override
    public void onAddressFound(String address, double latitude, double longitude, String geocodeobject){
        placeTxtView.setText(address);
        isPlaceSelected = true;
        this.placeLocation = new LatLng(latitude, longitude);

        CameraUpdate cu = CameraUpdateFactory.newLatLngZoom(this.placeLocation, 14.0f);

        if (gMap != null) {
            gMap.clear();
            if (isFirstLocation) {
                gMap.moveCamera(cu);
            }
            isFirstLocation = false;
            setGoogleMapCameraListener(this);
        }

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        this.gMap = googleMap;


        new Handler().postDelayed(() -> {
            setGoogleMapCameraListener(this);

            if (GetLocationUpdates.retrieveInstance() != null) {
                GetLocationUpdates.getInstance().stopLocationUpdates(this);
            }

            GetLocationUpdates.getInstance().startLocationUpdates(this, this);

        }, 2000);

    }


    @Override
    public void onLocationUpdate(Location location) {
        if (location == null) {
            return;
        }

        if (gMap == null) {
            this.userLocation = location;
            return;
        }

        setGoogleMapCameraListener(this);

        if (GetLocationUpdates.retrieveInstance() != null) {
            GetLocationUpdates.getInstance().stopLocationUpdates(this);
        }
        userLocation = location;

        if (isFirstLocation == true) {
            boolean isCameraPosChanged=false;
            placeLocation = getLocationLatLng();
            if (placeLocation != null) {
                Logger.d("PLACE_LAT >>", placeLocation.latitude + "\n" + placeLocation.longitude);
                isCameraPosChanged=setCameraPosition(new LatLng(placeLocation.latitude, placeLocation.longitude));
            } else {
                Logger.d("PLACE_LAT >", location.getLatitude() + "\n" + location.getLongitude());
                isCameraPosChanged= setCameraPosition(new LatLng(location.getLatitude(), location.getLongitude()));
            }
            if (isCameraPosChanged)
            isFirstLocation = false;
        }

    }

    private LatLng getLocationLatLng() {

        if (getIntent().hasExtra("PickUpLatitude") && getIntent().hasExtra("PickUpLongitude")) {
            placeLocation = new LatLng(generalFunc.parseDoubleValue(0.0, getIntent().getStringExtra("PickUpLatitude")),
                    generalFunc.parseDoubleValue(0.0, getIntent().getStringExtra("PickUpLongitude")));

            if (getIntent().hasExtra("PickUpAddress") && Utils.checkText(getIntent().getStringExtra("PickUpAddress"))) {
                isAddressEnable = true;
                isPlaceSelected = true;
                placeTxtView.setText("" + getIntent().getStringExtra("PickUpAddress"));
            }


        } else if (getIntent().getStringExtra("isDestLoc") != null && getIntent().hasExtra("DestLatitude") && getIntent().hasExtra("DestLongitude")) {
            placeLocation = new LatLng(generalFunc.parseDoubleValue(0.0, getIntent().getStringExtra("DestLatitude")),
                    generalFunc.parseDoubleValue(0.0, getIntent().getStringExtra("DestLongitude")));


            if (getIntent().hasExtra("DestAddress") && Utils.checkText(getIntent().getStringExtra("DestAddress"))) {
                isAddressEnable = true;
                isPlaceSelected = true;
                placeTxtView.setText("" + getIntent().getStringExtra("DestAddress"));
            }
        } else if (getIntent().getStringExtra("isHome") != null && getIntent().getStringExtra("isHome").equals("true")) {
            if (Utils.checkText(generalFunc.retrieveValue("userHomeLocationLatitude"))&& Utils.checkText(generalFunc.retrieveValue("userHomeLocationLongitude"))) {
                placeLocation = new LatLng(generalFunc.parseDoubleValue(0.0, generalFunc.retrieveValue("userHomeLocationLatitude")),
                        generalFunc.parseDoubleValue(0.0, generalFunc.retrieveValue("userHomeLocationLongitude")));

            }

            String home_address_str = generalFunc.retrieveValue("userHomeLocationAddress");

            if (home_address_str != null && Utils.checkText(home_address_str)) {
                isAddressEnable = true;
                isPlaceSelected = true;
                placeTxtView.setText("" + home_address_str);
            }
        } else if (userLocation != null) {
            placeLocation = new LatLng(generalFunc.parseDoubleValue(0.0, "" + userLocation.getLatitude()),
                    generalFunc.parseDoubleValue(0.0, "" + userLocation.getLongitude()));

        }

        return placeLocation;
    }

    private boolean setCameraPosition(LatLng location) {
        boolean isCameraPosChanged = false;
        if (gMap != null) {
            CameraPosition cameraPosition = new CameraPosition.Builder().target(
                    new LatLng(location.latitude,
                            location.longitude))
                    .zoom(Utils.defaultZomLevel).build();
            gMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            isCameraPosChanged = true;
        }
        return isCameraPosChanged;
    }

    @Override
    public void onCameraIdle() {

        if (gMap == null) {
            return;
        }

        if (getAddressFromLocation == null) {
            return;
        }

        LatLng center = null;
        if (gMap != null && gMap.getCameraPosition() != null) {
            center = gMap.getCameraPosition().target;
        }

        if (center == null) {
            return;
        }


        if (!isAddressEnable) {
            setGoogleMapCameraListener(null);
            getAddressFromLocation.setLocation(center.latitude, center.longitude);
            getAddressFromLocation.setLoaderEnable(true);
            getAddressFromLocation.execute();
        } else {
            isAddressEnable = false;
        }


    }


    public void setGoogleMapCameraListener(SearchPickupLocationActivity act) {
        isListnersettoNull = act == null ? true : false;
        if (gMap != null) {
            this.gMap.setOnCameraMoveStartedListener(act);
            this.gMap.setOnCameraIdleListener(act);
        }
    }

    @Override
    public void onCameraMoveStarted(int i) {

        if (!isAddressEnable) {
            placeTxtView.setText(generalFunc.retrieveLangLBl("", "LBL_SELECTING_LOCATION_TXT"));
        }

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

//        if (requestCode == Utils.PLACE_AUTOCOMPLETE_REQUEST_CODE) {
////            if (resultCode == RESULT_OK) {
////                Place place = PlaceAutocomplete.getPlace(this, data);
////                Logger.d(TAG, "Place:" + place.toString());
////
////                placeTxtView.setText(place.getAddress());
////                isPlaceSelected = true;
////                LatLng placeLocation = place.getLatLng();
////
////                this.placeLocation = placeLocation;
////
////                CameraUpdate cu = CameraUpdateFactory.newLatLngZoom(placeLocation, 14.0f);
////
////                if (gMap != null) {
////                    gMap.clear();
////                    placeMarker = gMap.addMarker(new MarkerOptions().position(placeLocation).title("" + place.getAddress()));
////                    gMap.moveCamera(cu);
////                }
////
////            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
////                Status status = PlaceAutocomplete.getStatus(this, data);
////                Logger.d(TAG, status.getStatusMessage());
////
////                generalFunc.showMessage(generalFunc.getCurrentView(SearchPickupLocationActivity.this),
////                        status.getStatusMessage());
////            } else if (requestCode == RESULT_CANCELED) {
////
////
//
//
//            if (resultCode == RESULT_OK) {
//                Bundle bn = new Bundle();
//                bn.putString("Latitude", data.getStringExtra("Latitude"));
//                bn.putString("Longitude", "" + data.getStringExtra("Longitude"));
//                bn.putString("Address", "" + data.getStringExtra("Address"));
//
//                bn.putBoolean("isSkip", false);
//                new StartActProcess(getActContext()).setOkResult(bn);
//                finish();
//            }
//        }


        if (requestCode == Utils.PLACE_AUTOCOMPLETE_REQUEST_CODE) {

            if (resultCode == RESULT_OK) {
                placeTxtView.setText(data.getStringExtra("Address"));
                isPlaceSelected = true;
                isAddressEnable = true;
                LatLng placeLocation = new LatLng(generalFunc.parseDoubleValue(0.0, data.getStringExtra("Latitude")), generalFunc.parseDoubleValue(0.0, data.getStringExtra("Longitude")));

                this.placeLocation = placeLocation;

                CameraUpdate cu = CameraUpdateFactory.newLatLngZoom(placeLocation, Utils.defaultZomLevel);

                if (gMap != null && placeLocation != null) {
                    gMap.clear();
                    gMap.moveCamera(cu);
                }

            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);

                generalFunc.showMessage(generalFunc.getCurrentView(SearchPickupLocationActivity.this),
                        status.getStatusMessage());
            } else if (requestCode == RESULT_CANCELED) {

            }
        }
    }

    public Context getActContext() {
        return SearchPickupLocationActivity.this;
    }

    public class setOnClickAct implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            int i = view.getId();
            Utils.hideKeyboard(SearchPickupLocationActivity.this);

            if (i == R.id.backImgView) {
                SearchPickupLocationActivity.super.onBackPressed();

            } else if (i == R.id.pickUpLocSearchArea) {

                try {
                    LatLngBounds bounds = null;

                    if (getIntent().hasExtra("PickUpLatitude") && getIntent().hasExtra("PickUpLongitude")) {

                        LatLng pickupPlaceLocation = new LatLng(generalFunc.parseDoubleValue(0.0, getIntent().getStringExtra("PickUpLatitude")),
                                generalFunc.parseDoubleValue(0.0, getIntent().getStringExtra("PickUpLongitude")));
                        bounds = new LatLngBounds(pickupPlaceLocation, pickupPlaceLocation);
                    }

//                    Intent intent = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY)
//                            .setBoundsBias(bounds)
//                            .build(SearchPickupLocationActivity.this);
//                    startActivityForResult(intent, Utils.PLACE_AUTOCOMPLETE_REQUEST_CODE);

                    Bundle bn = new Bundle();
                    bn.putString("locationArea", "dest");
                    bn.putDouble("lat", generalFunc.parseDoubleValue(0.0, getIntent().getStringExtra("PickUpLatitude")));
                    bn.putDouble("long", generalFunc.parseDoubleValue(0.0, getIntent().getStringExtra("PickUpLongitude")));
                    new StartActProcess(getActContext()).startActForResult(SearchLocationActivity.class, bn,
                            Utils.PLACE_AUTOCOMPLETE_REQUEST_CODE);

                } catch (Exception e) {

                }
            } else if (i == btnId) {

                if (!isbtnclick) {


                    if (isPlaceSelected == false) {
                        generalFunc.showMessage(generalFunc.getCurrentView(SearchPickupLocationActivity.this),
                                generalFunc.retrieveLangLBl("Please set location.", "LBL_SET_LOCATION"));
                        return;
                    }

                    isbtnclick = true;

                    if (isAddressStore) {
                        addHomeWorkAddress(placeTxtView.getText().toString(), placeLocation.latitude + "", placeLocation.longitude + "");
                    } else {
                        Bundle bn = new Bundle();
                        bn.putString("Address", placeTxtView.getText().toString());
                        bn.putString("Latitude", "" + placeLocation.latitude);
                        bn.putString("Longitude", "" + placeLocation.longitude);

                        if (getIntent().hasExtra("isFromMulti")) {
                            bn.putBoolean("isFromMulti", true);
                            bn.putInt("pos", getIntent().getIntExtra("pos", -1));
                        }

                        new StartActProcess(getActContext()).setOkResult(bn);
                        backImgView.performClick();

                    }
                }

            }
        }
    }

    public void addHomeWorkAddress(String vAddress, String vLatitude, String vLongitude) {

        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "UpdateUserFavouriteAddress");
        parameters.put("iUserId", generalFunc.getMemberId());
        parameters.put("vAddress", vAddress);
        parameters.put("vLatitude", vLatitude);
        parameters.put("vLongitude", vLongitude);
        parameters.put("eType", eType);
        parameters.put("eUserType", Utils.userType);
        parameters.put("iUserFavAddressId", iUserFavAddressId);

        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(), parameters);
        exeWebServer.setLoaderConfig(getActContext(), true, generalFunc);

        exeWebServer.setDataResponseListener(new ExecuteWebServerUrl.SetDataResponse() {
            @Override
            public void setResponse(String responseString) {

                Logger.d("Response", "::" + responseString);
                btn_type2.setEnabled(true);
                if (responseString != null && !responseString.equals("")) {

                    boolean isDataAvail = GeneralFunctions.checkDataAvail(Utils.action_str, responseString);

                    if (isDataAvail == true) {

                        String messgeJson = generalFunc.getJsonValue(Utils.message_str, responseString);
                        generalFunc.storeData(Utils.USER_PROFILE_JSON, messgeJson);

                        Bundle bn = new Bundle();
                        bn.putString("Address", placeTxtView.getText().toString());
                        bn.putString("Latitude", "" + placeLocation.latitude);
                        bn.putString("Longitude", "" + placeLocation.longitude);

                        new StartActProcess(getActContext()).setOkResult(bn);
                        backImgView.performClick();

                    } else {
                        generalFunc.showGeneralMessage("",
                                generalFunc.retrieveLangLBl("", generalFunc.getJsonValue(Utils.message_str, responseString)));
                    }
                } else {
                    isbtnclick = false;
                    generalFunc.showError();
                }
            }
        });
        exeWebServer.execute();

    }

    @Override
    protected void onDestroy() {
        releaseResources();
        super.onDestroy();
    }

    public void releaseResources() {
        removeLocationUpdates();
        setGoogleMapCameraListener(null);
        this.gMap = null;
        getAddressFromLocation.setAddressList(null);
        getAddressFromLocation = null;
    }


    public void removeLocationUpdates() {

        if (GetLocationUpdates.retrieveInstance() != null) {
            GetLocationUpdates.getInstance().stopLocationUpdates(this);
        }

        this.userLocation = null;
    }

}
