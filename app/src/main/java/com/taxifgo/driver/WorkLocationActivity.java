package com.taxifgo.driver;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import android.text.InputFilter;
import android.text.InputType;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.dialogs.OpenListView;
import com.general.files.ExecuteWebServerUrl;
import com.general.files.GeneralFunctions;
import com.general.files.GetAddressFromLocation;
import com.general.files.GetLocationUpdates;
import com.general.files.MyApp;
import com.general.files.StartActProcess;
import com.utils.Utils;
import com.view.GenerateAlertBox;
import com.view.MButton;
import com.view.MTextView;
import com.view.MaterialRippleLayout;
import com.view.editBox.MaterialEditText;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class WorkLocationActivity extends AppCompatActivity implements GetAddressFromLocation.AddressFound, GetLocationUpdates.LocationUpdatesListener {


    GeneralFunctions generalFunc;

    MTextView titleTxt;
    ImageView backImgView;

    FrameLayout locWorkSelectArea;
    String userProfileJson;
    ArrayList<String> items_work_location = new ArrayList<String>();
    ArrayList<String> real_items_work_location = new ArrayList<String>();
    ArrayList<HashMap<String, String>> items_work_radius = new ArrayList<HashMap<String, String>>();
    ArrayList<String> real_items_work_radius = new ArrayList<String>();
    MaterialEditText locationWorkBox, radiusWorkBox, otherBox;
    String selected_work_location = "";
    String selected_work_radius = "";
    androidx.appcompat.app.AlertDialog list_work_location;
    androidx.appcompat.app.AlertDialog list_work_radius;
    MTextView addressTxt, workradiusTitleTxt, workLocTitleTxt;
    GetAddressFromLocation getAddressFromLocation;
    GetLocationUpdates getLastLocation;
    Location location;
    ImageView editLocation;
    String eSelectWorkLocation = "";
    String vCountryUnitDriver = "";
    LinearLayout otherArea;
    MButton btn_type2;
    int submitBtnId;
    String required_str;

    MTextView demonoteText, demoText;
    MTextView noteText, noteDetailsText;
    LinearLayout workLocationArea;
    String SERVICE_PROVIDER_FLOW = "";
    private static final int ADD_ADDRESS = 006;
    CheckBox checkboxWork;
    LinearLayout addressArea;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_work_location);

        generalFunc = MyApp.getInstance().getGeneralFun(getActContext());
        userProfileJson = generalFunc.retrieveValue(Utils.USER_PROFILE_JSON);
        SERVICE_PROVIDER_FLOW = generalFunc.getJsonValue("SERVICE_PROVIDER_FLOW", userProfileJson);
        getAddressFromLocation = new GetAddressFromLocation(getActContext(), generalFunc);
        getAddressFromLocation.setAddressList(this);
        initViews();
        getDetails();

        if (generalFunc.getJsonValue("APP_TYPE", userProfileJson).equalsIgnoreCase(Utils.CabGeneralType_UberX) && findViewById(R.id.screenNoteAreaView) != null) {
            (findViewById(R.id.screenNoteAreaView)).setVisibility(View.GONE);
        }
    }


    public void handleWorkAddress() {



        if (generalFunc.getJsonValue("PROVIDER_AVAIL_LOC_CUSTOMIZE", userProfileJson).equalsIgnoreCase("Yes")) {
            if (eSelectWorkLocation.equalsIgnoreCase("")) {
                return;
            }

            if (eSelectWorkLocation.equalsIgnoreCase("Fixed")) {
                editLocation.setVisibility(View.VISIBLE);

                String WORKLOCATION=generalFunc.retrieveValue(Utils.WORKLOCATION);
                if (!WORKLOCATION.equals("")) {
                    addressTxt.setText(WORKLOCATION);
                } else {
                    if (location != null) {
                        getAddressFromLocation.setLocation(location.getLatitude(), location.getLongitude());
                        getAddressFromLocation.execute();
                    }
                }
            } else {
                editLocation.setVisibility(View.GONE);
                if (location != null) {
                    getAddressFromLocation.setLocation(location.getLatitude(), location.getLongitude());
                    getAddressFromLocation.execute();
                }
            }
        } else {

            editLocation.setVisibility(View.GONE);

            if (generalFunc.getJsonValue("ENABLE_SERVICE_AT_USER_LOC", userProfileJson).equalsIgnoreCase("Yes")) {
                checkboxWork.setVisibility(View.VISIBLE);
            }

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        handleWorkAddress();

        try {
            new Handler().postDelayed(() -> (findViewById(R.id.nesScrollView)).setPadding(0, 0, 0, (findViewById(R.id.screenNoteAreaView)).getHeight() + (((findViewById(R.id.screenNoteAreaView)).getHeight() * 10) / 100)), 500);
        } catch (Exception e) {

        }

    }

    public void setLabel() {
        titleTxt.setText(generalFunc.retrieveLangLBl("", "LBL_MANAGE_WORK_LOCATION"));

        addressTxt.setText(generalFunc.retrieveLangLBl("", "LBL_LOAD_ADDRESS"));
        workradiusTitleTxt.setText(generalFunc.retrieveLangLBl("", "LBL_RADIUS"));
        workLocTitleTxt.setText(generalFunc.retrieveLangLBl("", "LBL_YOUR_JOB_LOCATION_TXT"));
        required_str = generalFunc.retrieveLangLBl("", "LBL_FEILD_REQUIRD");

        demonoteText.setText(generalFunc.retrieveLangLBl("", "LBL_NOTE") + ":");

        demoText.setText(generalFunc.retrieveLangLBl("", "LBL_WORK_LOCATION_NOTE"));
        noteText.setText(generalFunc.retrieveLangLBl("", "LBL_NOTE") + ":");
    }

    public void initViews() {

        backImgView = (ImageView) findViewById(R.id.backImgView);
        checkboxWork = (CheckBox) findViewById(R.id.checkboxWork);
        titleTxt = (MTextView) findViewById(R.id.titleTxt);
        backImgView.setOnClickListener(new setOnClickList());
        locWorkSelectArea = (FrameLayout) findViewById(R.id.locWorkSelectArea);
        locationWorkBox = (MaterialEditText) findViewById(R.id.locationWorkBox);
        otherBox = (MaterialEditText) findViewById(R.id.otherBox);
        workradiusTitleTxt = (MTextView) findViewById(R.id.workradiusTitleTxt);
        workLocTitleTxt = (MTextView) findViewById(R.id.workLocTitleTxt);
        demonoteText = (MTextView) findViewById(R.id.demonoteText);
        noteDetailsText = (MTextView) findViewById(R.id.noteDetailsText);
        workLocationArea = (LinearLayout) findViewById(R.id.workLocationArea);
        noteText = (MTextView) findViewById(R.id.noteText);
        demoText = (MTextView) findViewById(R.id.demoText);
        addressArea = (LinearLayout) findViewById(R.id.addressArea);
        otherBox.setInputType(InputType.TYPE_CLASS_NUMBER);
        otherBox.setFilters(new InputFilter[]{new InputFilter.LengthFilter(4)});
        otherBox.setBothText("", generalFunc.retrieveLangLBl("", "LBL_ENTER_RADIUS_HINT"));
        if (userProfileJson != null && generalFunc.getJsonValue("eUnit", userProfileJson).equalsIgnoreCase("KMs")) {
            otherBox.setBothText("", generalFunc.retrieveLangLBl("", "LBL_ENTER_RADIUS_PER_KMS"));
        } else {
            otherBox.setBothText("", generalFunc.retrieveLangLBl("", "LBL_ENTER_RADIUS_PER_MILE"));
        }

        checkboxWork.setText(generalFunc.retrieveLangLBl("", "LBL_NOTE_ENABLE_SERVICE_AT_PROVIDER_LOC"));

        if (SERVICE_PROVIDER_FLOW.equalsIgnoreCase("Provider")) {
            checkboxWork.setVisibility(View.VISIBLE);
            if (generalFunc.getJsonValue("eEnableServiceAtLocation", userProfileJson).equalsIgnoreCase("Yes")) {
                checkboxWork.setChecked(true);
            }
        }

        checkboxWork.setOnClickListener(new setOnClickList());

        btn_type2 = ((MaterialRippleLayout) findViewById(R.id.btn_type2)).getChildView();
        btn_type2.setText(generalFunc.retrieveLangLBl("", "LBL_SUBMIT_TXT"));

        radiusWorkBox = (MaterialEditText) findViewById(R.id.radiusWorkBox);
        addressTxt = (MTextView) findViewById(R.id.addressTxt);
        editLocation = (ImageView) findViewById(R.id.editLocation);
        otherArea = (LinearLayout) findViewById(R.id.otherArea);


        submitBtnId = Utils.generateViewId();
        btn_type2.setId(submitBtnId);

        btn_type2.setOnClickListener(new setOnClickList());


        setLabel();

        locationWorkBox.getLabelFocusAnimator().start();
        radiusWorkBox.getLabelFocusAnimator().start();

        Utils.removeInput(locationWorkBox);
        Utils.removeInput(radiusWorkBox);
        locationWorkBox.setOnTouchListener(new setOnTouchList());
        radiusWorkBox.setOnTouchListener(new setOnTouchList());
        locationWorkBox.setOnClickListener(new setOnClickList());
        radiusWorkBox.setOnClickListener(new setOnClickList());
        editLocation.setOnClickListener(new setOnClickList());


        if (generalFunc.getJsonValue("PROVIDER_AVAIL_LOC_CUSTOMIZE", userProfileJson).equalsIgnoreCase("yes")) {
            items_work_location.add(generalFunc.retrieveLangLBl("Specified Location", "LBL_SPECIFIED_LOCATION"));
            items_work_location.add(generalFunc.retrieveLangLBl("Any Location", "LBL_ANY_LOCATION"));
            real_items_work_location.add("Fixed");
            real_items_work_location.add("Dynamic");
            workLocationArea.setVisibility(View.VISIBLE);

            noteDetailsText.setText(generalFunc.retrieveLangLBl("", "LBL_INFO_WORK_LOCATION") + "\n\n" +
                    generalFunc.retrieveLangLBl("", "LBL_INFO_WORK_RADIUS"));

        } else {
            workLocationArea.setVisibility(View.GONE);
            noteDetailsText.setText(generalFunc.retrieveLangLBl("", "LBL_INFO_WORK_RADIUS"));
        }

        if (SERVICE_PROVIDER_FLOW.equalsIgnoreCase("PROVIDER")) {
            noteDetailsText.setText(noteDetailsText.getText().toString() + "\n\n" + generalFunc.retrieveLangLBl("", "LBL_UFX_PROVIDER_LOC_NOTE"));
        }

        String eSelectWorkLocation=generalFunc.getJsonValue("eSelectWorkLocation", userProfileJson);
        if (eSelectWorkLocation != null && !eSelectWorkLocation.equalsIgnoreCase("")) {
            selected_work_location = eSelectWorkLocation;

            if (selected_work_location.equalsIgnoreCase("Fixed")) {
                selCurrentPositionWork=0;
                locationWorkBox.setText(generalFunc.retrieveLangLBl("Specified Location", "LBL_SPECIFIED_LOCATION"));

            } else {
                selCurrentPositionWork=1;
                locationWorkBox.setText(generalFunc.retrieveLangLBl("Any Location", "LBL_ANY_LOCATION"));

            }
        }
        if (GetLocationUpdates.retrieveInstance() != null) {
            GetLocationUpdates.getInstance().stopLocationUpdates(this);
        }
        GetLocationUpdates.getInstance().startLocationUpdates(this, this);
    }


    public void updateuserRadius(final String val) {
        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "UpdateRadius");
        parameters.put("iDriverId", generalFunc.getMemberId());
        parameters.put("vWorkLocationRadius", val);


        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(), parameters);
        exeWebServer.setLoaderConfig(getActContext(), true, generalFunc);
        exeWebServer.setIsDeviceTokenGenerate(true, "vDeviceToken", generalFunc);
        exeWebServer.setDataResponseListener(responseString -> {

            if (responseString != null && !responseString.equals("")) {

                boolean isDataAvail = GeneralFunctions.checkDataAvail(Utils.action_str, responseString);

                if (isDataAvail) {


                    final GenerateAlertBox generateAlert = new GenerateAlertBox(getActContext());
                    generateAlert.setCancelable(false);
                    generateAlert.setBtnClickList(btn_id -> {
                        generateAlert.closeAlertBox();

                        otherArea.setVisibility(View.GONE);
                        otherBox.setText("");
                        getDetails();
                    });

                    generateAlert.setContentMessage("", generalFunc.retrieveLangLBl("", generalFunc.getJsonValue("message1", responseString)));
                    generateAlert.setPositiveBtn(generalFunc.retrieveLangLBl("Ok", "LBL_BTN_OK_TXT"));

                    generateAlert.showAlertBox();


                } else {
                    generalFunc.showGeneralMessage("", generalFunc.retrieveLangLBl("", generalFunc.getJsonValue(Utils.message_str, responseString)));
                }
            } else {
                generalFunc.showError();
            }
        });
        exeWebServer.execute();
    }

    @Override
    public void onAddressFound(String address, double latitude, double longitude, String geocodeobject) {

        if (generalFunc.getJsonValue("PROVIDER_AVAIL_LOC_CUSTOMIZE", userProfileJson).equalsIgnoreCase("Yes") && generalFunc.getJsonValue("eSelectWorkLocation", userProfileJson).equalsIgnoreCase("Fixed")) {
            String WORKLOCATION=generalFunc.retrieveValue(Utils.WORKLOCATION);
            if (!WORKLOCATION.equals("")) {
                addressTxt.setText(WORKLOCATION);
            } else {
                addressTxt.setText(address);
            }
        } else {
            addressTxt.setText(address);
        }

    }

    @Override
    public void onLocationUpdate(Location location) {
        this.location = location;
    }


    public class setOnTouchList implements View.OnTouchListener {

        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (motionEvent.getAction() == MotionEvent.ACTION_UP && !view.hasFocus()) {
                view.performClick();
            }
            return true;
        }
    }

    public void MnageServiceLocation(Boolean ischeck) {
        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "configureProviderServiceLocation");
        parameters.put("iDriverId", generalFunc.getMemberId());
        parameters.put("eEnableServiceAtLocation", ischeck ? "Yes" : "No");

        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(), parameters);
        exeWebServer.setLoaderConfig(getActContext(), true, generalFunc);
        exeWebServer.setDataResponseListener(responseString -> {

            if (responseString != null && !responseString.equals("")) {

                boolean isDataAvail = GeneralFunctions.checkDataAvail(Utils.action_str, responseString);

                if (isDataAvail) {
                    generalFunc.storeData(Utils.USER_PROFILE_JSON, generalFunc.getJsonValue(Utils.message_str, responseString));
                    userProfileJson = generalFunc.retrieveValue(Utils.USER_PROFILE_JSON);
                    if (generalFunc.getJsonValue("eEnableServiceAtLocation", userProfileJson).equalsIgnoreCase("Yes")) {
                        checkboxWork.setChecked(true);
                    } else {
                        checkboxWork.setChecked(false);
                    }


                } else {
                    if (generalFunc.getJsonValue("eEnableServiceAtLocation", userProfileJson).equalsIgnoreCase("Yes")) {
                        checkboxWork.setChecked(true);
                    } else {
                        checkboxWork.setChecked(false);
                    }
                    generalFunc.showGeneralMessage("",
                            generalFunc.retrieveLangLBl("", generalFunc.getJsonValue(Utils.message_str, responseString)));
                }
            } else {
                generalFunc.showError();
            }
        });
        exeWebServer.execute();
    }

    public class setOnClickList implements View.OnClickListener {

        @Override
        public void onClick(View view) {

            if (view.getId() == submitBtnId) {
                if (otherBox.getText().toString().length() > 0) {

                    if (GeneralFunctions.parseIntegerValue(0, otherBox.getText().toString()) > 0) {
                        updateuserRadius(otherBox.getText().toString());
                    } else {
                        Utils.setErrorFields(otherBox, generalFunc.retrieveLangLBl("", "LBL_FILL_PROPER_DETAILS"));
                    }
                } else {
                    Utils.setErrorFields(otherBox, required_str);
                }

            } else if (view == checkboxWork) {
                MnageServiceLocation(checkboxWork.isChecked());
                return;
            }

            switch (view.getId()) {
                case R.id.backImgView:
                    WorkLocationActivity.super.onBackPressed();
                    break;

                case R.id.locationWorkBox:
                    buildLocationWorkList();
                    break;

                case R.id.radiusWorkBox:
                    buildWorkRadiusList();
                    break;

                case R.id.editLocation:

                    if (SERVICE_PROVIDER_FLOW.equalsIgnoreCase("Provider")) {
                        Bundle bn = new Bundle();

                        bn.putString("latitude", location.getLatitude() + "");
                        bn.putString("longitude", location.getLongitude() + "");
                        bn.putString("address", Utils.getText(addressTxt));
                        new StartActProcess(getActContext()).startActForResult(AddAddressActivity.class, bn, ADD_ADDRESS);

                    } else {
                        Bundle bn = new Bundle();
                        bn.putString("locationArea", "dest");
                        if (location != null) {
                            bn.putDouble("lat", location.getLatitude());
                            bn.putDouble("long", location.getLongitude());
                        }
                        new StartActProcess(getActContext()).startActForResult(SearchLocationActivity.class, bn, Utils.SEARCH_PICKUP_LOC_REQ_CODE);
                    }
                    break;


            }
        }
    }

    int selCurrentPositionWork=-1;
    public void buildLocationWorkList() {

       /*CharSequence[] wk_location_txt = items_work_location.toArray(new CharSequence[items_work_location.size()]);

         android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(getActContext());
        builder.setTitle(generalFunc.retrieveLangLBl("", "LBL_WORKLOCATION"));

        builder.setItems(wk_location_txt, (dialog, item) -> {

            if (list_work_location != null) {
                list_work_location.dismiss();
            }
            selected_work_location = real_items_work_location.get(item);

            if (selected_work_location.equalsIgnoreCase("Fixed")) {

                if (generalFunc.retrieveValue(Utils.WORKLOCATION).equals("")) {
                    editLocation.performClick();
                    return;
                }

                locationWorkBox.setText(generalFunc.retrieveLangLBl("Specified Location", "LBL_SPECIFIED_LOCATION"));
            } else {
                locationWorkBox.setText(generalFunc.retrieveLangLBl("Any Location", "LBL_ANY_LOCATION"));
                if (SERVICE_PROVIDER_FLOW.equalsIgnoreCase("Provider")) {
                    checkboxWork.setVisibility(View.VISIBLE);
                }
            }

            updateWorkLocationSelection();

        });

        list_work_location = builder.create();

        if (generalFunc.isRTLmode()) {
            generalFunc.forceRTLIfSupported(list_work_location);
        }

        list_work_location.show();*/

        ArrayList<HashMap<String,String>>  itemsList = new ArrayList<>();
        int loctaionlistsize = items_work_location.size();
        for (int i = 0; i < loctaionlistsize; i++) {
            HashMap<String,String> map = new HashMap<>();
            map.put("location",items_work_location.get(i));
            itemsList.add(map);
        }


        OpenListView.getInstance(getActContext(), generalFunc.retrieveLangLBl("", "LBL_WORKLOCATION"), itemsList, OpenListView.OpenDirection.CENTER, true, position -> {


            selCurrentPositionWork = position;
            HashMap<String, String> mapData = itemsList.get(position);
            //  selected_work_location = mapData.get("location");
            selected_work_location = real_items_work_location.get(position);
            if (selected_work_location.equalsIgnoreCase("Fixed")) {

                if (generalFunc.retrieveValue(Utils.WORKLOCATION).equals("")) {
                    editLocation.performClick();
                    return;
                }

                locationWorkBox.setText(generalFunc.retrieveLangLBl("Specified Location", "LBL_SPECIFIED_LOCATION"));
            } else {
                locationWorkBox.setText(generalFunc.retrieveLangLBl("Any Location", "LBL_ANY_LOCATION"));
                if (SERVICE_PROVIDER_FLOW.equalsIgnoreCase("Provider")) {
                    checkboxWork.setVisibility(View.VISIBLE);
                }
            }

            updateWorkLocationSelection();




        }).show(selCurrentPositionWork, "location");



    }

    int selCurrentPositionRadius=-1;
    public void buildWorkRadiusList() {

/*
        ArrayList<String> items = new ArrayList<String>();
        for (int i = 0; i < items_work_radius.size(); i++) {
            if (!items_work_radius.get(i).get("value").equalsIgnoreCase("other")) {
                if (items_work_radius.get(i).get("eUnit").equalsIgnoreCase("Miles")) {
                    items.add(generalFunc.convertNumberWithRTL(items_work_radius.get(i).get("value")) + " " + generalFunc.retrieveLangLBl("", "LBL_MILE_DISTANCE_TXT"));
                } else {
                    items.add(generalFunc.convertNumberWithRTL(items_work_radius.get(i).get("value")) + " " + generalFunc.retrieveLangLBl("", "LBL_KM_DISTANCE_TXT"));
                }

            } else {
                items.add(generalFunc.convertNumberWithRTL(items_work_radius.get(i).get("name")));
            }
        }

        CharSequence[] wk_location_txt = items.toArray(new CharSequence[items.size()]);

        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(getActContext());
        builder.setTitle(generalFunc.retrieveLangLBl("", "LBL_RADIUS"));

        builder.setItems(wk_location_txt, (dialog, item) -> {

            if (list_work_radius != null) {
                list_work_radius.dismiss();
            }

            if (items_work_radius.get(item).get("value").equalsIgnoreCase("other")) {
                radiusWorkBox.setText(generalFunc.convertNumberWithRTL(items_work_radius.get(item).get("name")));
                otherArea.setVisibility(View.VISIBLE);
                return;
            }
            otherArea.setVisibility(View.GONE);
            selected_work_radius = items_work_radius.get(item).get("name");
            radiusWorkBox.setText(generalFunc.convertNumberWithRTL(items.get(item)));

            updateuserRadius(selected_work_radius);

        });

        list_work_radius = builder.create();

        if (generalFunc.isRTLmode()) {
            generalFunc.forceRTLIfSupported(list_work_radius);
        }

        list_work_radius.show();*/

        ArrayList<HashMap<String,String>>  itemsList = new ArrayList<>();

        for (int i = 0; i < items_work_radius.size(); i++) {
            HashMap<String,String> map = new HashMap<>();
            if (!items_work_radius.get(i).get("value").equalsIgnoreCase("other")) {
                if (items_work_radius.get(i).get("eUnit").equalsIgnoreCase("Miles")) {

                    map.put("radius",(generalFunc.convertNumberWithRTL(items_work_radius.get(i).get("value")) + " " + generalFunc.retrieveLangLBl("", "LBL_MILE_DISTANCE_TXT")));
                } else {
                    map.put("radius",(generalFunc.convertNumberWithRTL(items_work_radius.get(i).get("value")) + " " + generalFunc.retrieveLangLBl("", "LBL_KM_DISTANCE_TXT")));
                }

            } else {
                map.put("radius",(generalFunc.convertNumberWithRTL(items_work_radius.get(i).get("name"))));
            }
            itemsList.add(map);
        }


        OpenListView.getInstance(getActContext(), generalFunc.retrieveLangLBl("", "LBL_RADIUS"), itemsList, OpenListView.OpenDirection.CENTER, true, position -> {


            selCurrentPositionRadius = position;
            HashMap<String, String> mapData = itemsList.get(position);
            radiusWorkBox.setText(mapData.get("radius"));
            selected_work_radius = mapData.get("radius");
            if (items_work_radius.get(position).get("value").equalsIgnoreCase("other")) {
                radiusWorkBox.setText(generalFunc.convertNumberWithRTL(items_work_radius.get(position).get("name")));
                otherArea.setVisibility(View.VISIBLE);
                return;
            }
            updateuserRadius(selected_work_radius);

        }).show(selCurrentPositionRadius, "radius");


    }

    public void getDetails() {
        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "getDriverWorkLocationUFX");
        parameters.put("iDriverId", generalFunc.getMemberId());

        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(), parameters);
        exeWebServer.setLoaderConfig(getActContext(), true, generalFunc);
        exeWebServer.setDataResponseListener(responseString -> {

            if (responseString != null && !responseString.equals("")) {

                boolean isDataAvail = GeneralFunctions.checkDataAvail(Utils.action_str, responseString);

                if (isDataAvail == true) {
                    items_work_radius.clear();

                    String message = generalFunc.getJsonValue(Utils.message_str, responseString);

                    eSelectWorkLocation = generalFunc.getJsonValue("eSelectWorkLocation", message);
                    selected_work_location = eSelectWorkLocation;
                    vCountryUnitDriver = generalFunc.getJsonValue("vCountryUnitDriver", message);

                    generalFunc.storeData(Utils.WORKLOCATION, generalFunc.getJsonValue("vWorkLocation", message));

                    JSONArray radiusArray = generalFunc.getJsonArray("RadiusList", message);

                    for (int i = 0; i < radiusArray.length(); i++) {
                        JSONObject jsonObject = generalFunc.getJsonObject(radiusArray, i);

                        HashMap<String, String> map = new HashMap<>();
                        map.put("value", generalFunc.getJsonValue("value", jsonObject.toString()));
                        map.put("name", generalFunc.getJsonValue("value", jsonObject.toString()));
                        map.put("eUnit", generalFunc.getJsonValue("eUnit", jsonObject.toString()));
                        map.put("eSelected", generalFunc.getJsonValue("eSelected", jsonObject.toString()));

                        items_work_radius.add(map);
                    }

                    HashMap<String, String> map = new HashMap<>();
                    map.put("value", "Other");
                    map.put("name", generalFunc.retrieveLangLBl("", "LBL_OTHER_TXT"));
                    map.put("eUnit", "");
                    map.put("eSelected", "");

                    items_work_radius.add(map);

                    handleWorkRadius();
                    handleWorkAddress();
                } else {
                    generalFunc.showGeneralMessage("",
                            generalFunc.retrieveLangLBl("", generalFunc.getJsonValue(Utils.message_str, responseString)));
                }
            } else {
                generalFunc.showError();
            }
        });
        exeWebServer.execute();
    }


    public void handleWorkRadius() {
        if (items_work_radius != null) {

            for (int i = 0; i < items_work_radius.size(); i++) {
                HashMap<String, String> workData = items_work_radius.get(i);
                if (workData.get("eSelected").equalsIgnoreCase("Yes")) {
                    selected_work_radius = workData.get("value");
                    radiusWorkBox.setText(generalFunc.convertNumberWithRTL(selected_work_radius) + " " + workData.get("eUnit"));

                    selCurrentPositionRadius = i;
                    if (workData.get("eUnit").equalsIgnoreCase("Miles")) {
                        selCurrentPositionRadius = i;
                        radiusWorkBox.setText(generalFunc.convertNumberWithRTL(selected_work_radius) + " " + generalFunc.retrieveLangLBl("", "LBL_MILE_DISTANCE_TXT"));
                    } else {
                        selCurrentPositionRadius = i;
                        radiusWorkBox.setText(generalFunc.convertNumberWithRTL(selected_work_radius) + " " + generalFunc.retrieveLangLBl("", "LBL_KM_DISTANCE_TXT"));
                    }

                }
            }

        }
    }

    public void updateWorkLocationSelection() {

        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "UpdateDriverWorkLocationSelectionUFX");
        parameters.put("iDriverId", generalFunc.getMemberId());
        parameters.put("eSelectWorkLocation", selected_work_location);
        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(), parameters);
        exeWebServer.setLoaderConfig(getActContext(), true, generalFunc);
        exeWebServer.setDataResponseListener(responseString -> {

            if (responseString != null && !responseString.equals("")) {

                boolean isDataAvail = GeneralFunctions.checkDataAvail(Utils.action_str, responseString);

                if (isDataAvail) {
                    generalFunc.showGeneralMessage("", generalFunc.retrieveLangLBl("", generalFunc.getJsonValue(Utils.message_str_one, responseString)));
                    String message = generalFunc.getJsonValue(Utils.message_str, responseString);
                    generalFunc.storeData(Utils.USER_PROFILE_JSON, message);
                    userProfileJson = generalFunc.retrieveValue(Utils.USER_PROFILE_JSON);
                    eSelectWorkLocation = selected_work_location;
                    handleWorkAddress();

                }
            } else {
                generalFunc.showError();
            }
        });
        exeWebServer.execute();

    }

    public void updateWorkLocation(String worklat, String worklong, String workaddress) {

        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "UpdateDriverWorkLocationUFX");
        parameters.put("iDriverId", generalFunc.getMemberId());
        parameters.put("vWorkLocationLatitude", worklat);
        parameters.put("vWorkLocationLongitude", worklong);
        parameters.put("vWorkLocation", workaddress);

        if (generalFunc.retrieveValue(Utils.WORKLOCATION).equals("")) {
            parameters.put("eSelectWorkLocation", eSelectWorkLocation);
        }

        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(), parameters);
        exeWebServer.setLoaderConfig(getActContext(), true, generalFunc);
        exeWebServer.setDataResponseListener(responseString -> {

            if (responseString != null && !responseString.equals("")) {

                boolean isDataAvail = GeneralFunctions.checkDataAvail(Utils.action_str, responseString);

                if (isDataAvail == true) {
                    if (generalFunc.retrieveValue(Utils.WORKLOCATION).equals("")) {
                        eSelectWorkLocation = "Fixed";
                        parameters.put("eSelectWorkLocation", eSelectWorkLocation);
                        selCurrentPositionWork=0;
                        locationWorkBox.setText(generalFunc.retrieveLangLBl("Specified Location", "LBL_SPECIFIED_LOCATION"));
                    }
                    addressTxt.setText(workaddress);
                    generalFunc.storeData(Utils.WORKLOCATION, workaddress);
                    handleWorkAddress();
                }
            } else {
                generalFunc.showError();
            }
        });
        exeWebServer.execute();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Utils.SEARCH_PICKUP_LOC_REQ_CODE) {
            if (resultCode == RESULT_OK) {
                String worklat = data.getStringExtra("Latitude");
                String worklong = data.getStringExtra("Longitude");
                String workadddress = data.getStringExtra("Address");

                updateWorkLocation(worklat, worklong, workadddress);
            }
        } else if (requestCode == ADD_ADDRESS) {
            if (resultCode == RESULT_OK) {
                String worklat = data.getStringExtra("Latitude");
                String worklong = data.getStringExtra("Longitude");
                String workadddress = data.getStringExtra("Address");

                updateWorkLocation(worklat, worklong, workadddress);
            }
        }
    }

    public Context getActContext() {
        return WorkLocationActivity.this;
    }
}
