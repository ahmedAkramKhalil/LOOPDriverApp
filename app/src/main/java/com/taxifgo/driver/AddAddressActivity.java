package com.taxifgo.driver;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.general.files.GeneralFunctions;
import com.general.files.GetAddressFromLocation;
import com.general.files.GetLocationUpdates;
import com.general.files.MyApp;
import com.general.files.StartActProcess;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.utils.Utils;
import com.view.MButton;
import com.view.MTextView;
import com.view.MaterialRippleLayout;
import com.view.editBox.MaterialEditText;

public class AddAddressActivity extends AppCompatActivity  implements OnMapReadyCallback, GetLocationUpdates.LocationUpdatesListener, GetAddressFromLocation.AddressFound, GoogleMap.OnCameraMoveStartedListener, GoogleMap.OnCameraIdleListener {



    GeneralFunctions generalFunc;
    ImageView backImgView;
    MTextView titleTxt;

    MaterialEditText buildingBox;
    MaterialEditText landmarkBox;
    MaterialEditText addrtypeBox;
    MaterialEditText apartmentLocNameBox;
    MaterialEditText companyBox;
    MaterialEditText postCodeBox;
    MaterialEditText addr2Box;
    MaterialEditText deliveryIntructionBox;
    MaterialEditText vContryBox;

    MTextView locationImage;
    String addresslatitude="";
    String addresslongitude;
    String address="";
    String tempAddress;
    MTextView locAddrTxtView;
    MTextView serviceAddrHederTxtView;
  //  MTextView AddrareaTxtView;
    MButton btn_type2;
    LinearLayout loc_area;

    String required_str = "";
    String type = "";
    String iUserAddressId;
    boolean isclick = false;

    String iCompanyId;
    GetAddressFromLocation getAddressFromLocation;

    public SupportMapFragment map;
    GoogleMap gMap;
    ImageView pinImgView;
    boolean isPlaceSelected = false;
    LatLng placeLocation;
    public boolean isAddressEnable = false;
    private boolean isFirstLocation = true;
    GetLocationUpdates getLastLocation;
    private Location userLocation;
    private AddAddressActivity listener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_address);

        generalFunc = MyApp.getInstance().getGeneralFun(getActContext());


        backImgView = (ImageView) findViewById(R.id.backImgView);
        titleTxt = (MTextView) findViewById(R.id.titleTxt);
        loc_area = (LinearLayout) findViewById(R.id.loc_area);


        map = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapV2);
        pinImgView = (ImageView) findViewById(R.id.pinImgView);

        buildingBox = (MaterialEditText) findViewById(R.id.buildingBox);
        landmarkBox = (MaterialEditText) findViewById(R.id.landmarkBox);
        addrtypeBox = (MaterialEditText) findViewById(R.id.addrtypeBox);
        apartmentLocNameBox = (MaterialEditText) findViewById(R.id.apartmentLocNameBox);
        companyBox = (MaterialEditText) findViewById(R.id.companyBox);
        postCodeBox = (MaterialEditText) findViewById(R.id.postCodeBox);
        addr2Box = (MaterialEditText) findViewById(R.id.addr2Box);
        deliveryIntructionBox = (MaterialEditText) findViewById(R.id.deliveryIntructionBox);
        vContryBox = (MaterialEditText) findViewById(R.id.vContryBox);
        locationImage = (MTextView) findViewById(R.id.locationImage);
        locAddrTxtView = (MTextView) findViewById(R.id.locAddrTxtView);
        serviceAddrHederTxtView = (MTextView) findViewById(R.id.serviceAddrHederTxtView);
      //  AddrareaTxtView = (MTextView) findViewById(R.id.AddrareaTxtView);
        btn_type2 = ((MaterialRippleLayout) findViewById(R.id.btn_type2)).getChildView();
        btn_type2.setOnClickListener(new setOnClick());
        loc_area.setOnClickListener(new setOnClick());



            addresslatitude = getIntent().getStringExtra("latitude");
            addresslongitude = getIntent().getStringExtra("longitude");
         //   address = getIntent().getStringExtra("address");
//            type = getIntent().getStringExtra("type");

        locAddrTxtView.setText(generalFunc.retrieveLangLBl("","LBL_SELECT_ADDRESS_TITLE_TXT"));

        backImgView.setOnClickListener(new setOnClick());
        locationImage.setOnClickListener(new setOnClick());
        setLabel();

        getAddressFromLocation = new GetAddressFromLocation(getActContext(), generalFunc);
        getAddressFromLocation.setAddressList(this);

        map.getMapAsync(this);

    }

    private void setLabel() {
        titleTxt.setText(generalFunc.retrieveLangLBl("Add New Address", "LBL_WORKLOCATION"));
        buildingBox.setBothText(generalFunc.retrieveLangLBl("Building/House/Flat No.", "LBL_JOB_LOCATION_HINT_INFO"));
        landmarkBox.setBothText(generalFunc.retrieveLangLBl("Landmark(e.g hospital,park etc.)", "LBL_LANDMARK_HINT_INFO"));
        addrtypeBox.setBothText(generalFunc.retrieveLangLBl("Nickname(optional-home,office etc.)", " LBL_ADDRESSTYPE_HINT_INFO"));
        serviceAddrHederTxtView.setText(generalFunc.retrieveLangLBl("Service address", "LBL_SERVICE_ADDRESS_HINT_INFO"));
      //  AddrareaTxtView.setText(generalFunc.retrieveLangLBl("Area of service", "LBL_AREA_SERVICE_HINT_INFO"));
        btn_type2.setText(generalFunc.retrieveLangLBl("Save", "LBL_BTN_SUBMIT_TXT"));
        required_str = generalFunc.retrieveLangLBl("", "LBL_FEILD_REQUIRD");
        locationImage.setText(generalFunc.retrieveLangLBl("", "LBL_CHANGE"));
    }

    public void checkValues() {
        boolean buildingDataenterd = Utils.checkText(buildingBox) ? true
                : Utils.setErrorFields(buildingBox, required_str);
        boolean landmarkDataenterd = Utils.checkText(landmarkBox) ? true
                : Utils.setErrorFields(landmarkBox, required_str);

        if (buildingDataenterd == false || landmarkDataenterd == false) {
            return;

        }


        Bundle bn = new Bundle();
        bn.putString("Latitude", addresslatitude);
        bn.putString("Longitude", addresslongitude);
        if(Utils.checkText(addrtypeBox))
        {
            address = Utils.getText(buildingBox) + ", " +  Utils.getText(landmarkBox) + ", " + Utils.getText(addrtypeBox)+", "+ address;
        }
        else {
            address =  Utils.getText(buildingBox) + ", " +  Utils.getText(landmarkBox) + ", " + address;
        }
        bn.putString("Address", address);


        (new StartActProcess(getActContext())).setOkResult(bn);
        finish();

    }



    public Context getActContext() {
        return AddAddressActivity.this;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        this.gMap = googleMap;


        getLastLocation = new GetLocationUpdates();


        new Handler().postDelayed(() -> {
            setGoogleMapCameraListener(this);

            if (GetLocationUpdates.retrieveInstance() != null) {
                GetLocationUpdates.getInstance().stopLocationUpdates(this);
            }

            getLastLocation.startLocationUpdates(this, this);

        }, 2000);



    }

    @Override
    protected void onDestroy() {
        if (getLastLocation != null) {
            getLastLocation.stopLocationUpdates(this);
        }
        releaseResources();
        super.onDestroy();
    }

    @Override
    public void onLocationUpdate(Location location) {
        if (location == null) {
            return;
        }

        if (isFirstLocation == true) {
            LatLng placeLocation = getLocationLatLng(true);
            if (isAddressEnable && listener==null)
            {
                setGoogleMapCameraListener(this);
            }
            if (placeLocation != null) {
                setCameraPosition(new LatLng(placeLocation.latitude, placeLocation.longitude));
            } else {
                setCameraPosition(new LatLng(location.getLatitude(), location.getLongitude()));
            }

            pinImgView.setVisibility(View.VISIBLE);
            isFirstLocation = false;
        }

        userLocation = location;
    }


    private void setCameraPosition(LatLng location) {
        CameraPosition cameraPosition = new CameraPosition.Builder().target(
                new LatLng(location.latitude,
                        location.longitude))
                .zoom(Utils.defaultZomLevel).build();
        gMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

    }

    private LatLng getLocationLatLng(boolean setText) {
        LatLng placeLocation = null;

        String CURRENT_ADDRESS = generalFunc.retrieveValue(Utils.CURRENT_ADDRESSS);

        if (getIntent().hasExtra("iCompanyId") && CURRENT_ADDRESS != null && !CURRENT_ADDRESS.equalsIgnoreCase("")) {
            address = CURRENT_ADDRESS;
            addresslatitude = generalFunc.retrieveValue(Utils.CURRENT_LATITUDE);
            addresslongitude = generalFunc.retrieveValue(Utils.CURRENT_LONGITUDE);
            placeLocation = new LatLng(generalFunc.parseDoubleValue(0.0, addresslatitude),
                    generalFunc.parseDoubleValue(0.0, addresslongitude));

            if (iCompanyId != null && iCompanyId.equalsIgnoreCase("-1")) {
                addresslatitude = getIntent().getStringExtra("latitude");
                addresslongitude = getIntent().getStringExtra("longitude");
                address = getIntent().getStringExtra("address");
            }
            isAddressEnable = true;
            pinImgView.setVisibility(View.VISIBLE);
            locAddrTxtView.setText(address);
        } /*else if (getIntent().hasExtra("latitude") && getIntent().hasExtra("longitude") && setText && getIntent().hasExtra("address")) {

            isAddressEnable = true;
            placeLocation = new LatLng(generalFunc.parseDoubleValue(0.0, getIntent().getStringExtra("latitude")),
                    generalFunc.parseDoubleValue(0.0, getIntent().getStringExtra("longitude")));

            pinImgView.setVisibility(View.VISIBLE);
            locAddrTxtView.setText("" + getIntent().getStringExtra("address"));
            address = "" + getIntent().getStringExtra("address");
        } */else if (userLocation != null) {
            placeLocation = new LatLng(generalFunc.parseDoubleValue(0.0, "" + userLocation.getLatitude()),
                    generalFunc.parseDoubleValue(0.0, "" + userLocation.getLongitude()));

        } else {
            LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            locationManager = (LocationManager) getSystemService
                    (Context.LOCATION_SERVICE);
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return placeLocation;
            }
            Location getLastLocation = locationManager.getLastKnownLocation
                    (LocationManager.PASSIVE_PROVIDER);
            if (getLastLocation != null) {
                LatLng UserLocation = new LatLng(generalFunc.parseDoubleValue(0.0, "" + getLastLocation.getLatitude()),
                        generalFunc.parseDoubleValue(0.0, "" + getLastLocation.getLongitude()));
                if (UserLocation != null) {
                    placeLocation = UserLocation;
                }
            }
        }


        return placeLocation;
    }

    public void releaseResources() {
        setGoogleMapCameraListener(null);
        this.gMap = null;
        getAddressFromLocation.setAddressList(null);
        getAddressFromLocation = null;
    }

    public void setGoogleMapCameraListener(AddAddressActivity act) {
        listener =act;
        this.gMap.setOnCameraMoveStartedListener(act);
        this.gMap.setOnCameraIdleListener(act);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Utils.SEARCH_PICKUP_LOC_REQ_CODE && resultCode == RESULT_OK && data != null) {

//            mainAct.configPickUpDrag(true, false, false);

            if (resultCode == RESULT_OK) {


                Place place = PlaceAutocomplete.getPlace(getActContext(), data);
                LatLng placeLocation = place.getLatLng();
                locAddrTxtView.setText(place.getAddress().toString());
                addresslatitude = placeLocation.latitude + "";
                addresslongitude = placeLocation.longitude + "";
                address = place.getAddress().toString();


            }

        } else if (requestCode == Utils.UBER_X_SEARCH_PICKUP_LOC_REQ_CODE && resultCode == RESULT_OK && data != null) {
            address = data.getStringExtra("Address");
            locAddrTxtView.setText(address);

            String Latitude=data.getStringExtra("Latitude");
            String Longitude=data.getStringExtra("Longitude");

            addresslatitude = Latitude == null ? "0.0" : Latitude;
            addresslongitude = Longitude == null ? "0.0" : Longitude;
        }
    }

    @Override
    public void onAddressFound(String address, double latitude, double longitude, String geocodeobject) {
        locAddrTxtView.setText(address);
        this.address = address;
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
    public void onCameraIdle() {
        if (getAddressFromLocation == null) {
            return;
        }

        if (pinImgView.getVisibility() == View.GONE) {
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

    @Override
    public void onCameraMoveStarted(int i) {
        if (pinImgView.getVisibility() == View.VISIBLE) {
            if (!isAddressEnable) {
                locAddrTxtView.setText(generalFunc.retrieveLangLBl("", "LBL_SELECTING_LOCATION_TXT"));
            }
        }

    }

    public class setOnClick implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            int i = view.getId();
            if (i == R.id.backImgView) {
                AddAddressActivity.super.onBackPressed();
            } else if (i == R.id.loc_area) {

                if (generalFunc.isLocationEnabled()) {
                    Bundle bn = new Bundle();
                    bn.putString("locationArea", "source");
                    bn.putBoolean("isaddressview", true);
                    bn.putString("hideSetMapLoc", "");
                    if (getIntent().hasExtra("iCompanyId")) {
                        bn.putString("eSystem", Utils.eSystem_Type);
                    }
                    new StartActProcess(getActContext()).startActForResult(SearchLocationActivity.class,
                            bn, Utils.UBER_X_SEARCH_PICKUP_LOC_REQ_CODE);
                } else {
                    try {
                        LatLngBounds bounds = null;


                        Intent intent = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN)
                                .setBoundsBias(bounds)
                                .build(AddAddressActivity.this);
                        startActivityForResult(intent, Utils.SEARCH_PICKUP_LOC_REQ_CODE);
                    } catch (Exception e) {

                    }
                }


            } else if (i == locationImage.getId()) {
                loc_area.performClick();
            } else if (i == btn_type2.getId()) {
                if(Utils.checkText(address))
                {
                    checkValues();

                }
                else
                {
                    generalFunc.showMessage(backImgView,generalFunc.retrieveLangLBl("","LBL_SELECT_ADDRESS_TITLE_TXT"));

                }

            }
        }
    }
}
