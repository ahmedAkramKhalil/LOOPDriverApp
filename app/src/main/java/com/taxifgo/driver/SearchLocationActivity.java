package com.taxifgo.driver;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.adapter.files.PlacesAdapter;
import com.adapter.files.RecentLocationAdpater;
import com.general.files.ExecuteWebServerUrl;
import com.general.files.GeneralFunctions;
import com.general.files.InternetConnection;
import com.general.files.MapDelegate;
import com.general.files.MapServiceApi;
import com.general.files.MyApp;
import com.general.files.StartActProcess;
import com.general.files.UpdateFrequentTask;
import com.google.android.gms.maps.model.LatLng;
import com.model.Stop_Over_Points_Data;
import com.utils.Logger;
import com.utils.Utils;
import com.view.MTextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class SearchLocationActivity extends AppCompatActivity implements PlacesAdapter.setRecentLocClickList, MapDelegate {


    public boolean isAddressEnable;
    MTextView titleTxt;
    ImageView backImgView;
    GeneralFunctions generalFunc;
    JSONObject userProfileJsonObj;
    String whichLocation = "";
    MTextView cancelTxt;
    RecyclerView placesRecyclerView;
    EditText searchTxt;
    ArrayList<HashMap<String, String>> placelist;
    PlacesAdapter placesAdapter;
    ImageView imageCancel;
    MTextView noPlacedata;
    InternetConnection intCheck;
    ImageView googleimagearea;

    String session_token = "";
    int MIN_CHAR_REQ_GOOGLE_AUTO_COMPLETE = 2;
    String currentSearchQuery = "";
    UpdateFrequentTask sessionTokenFreqTask = null;

    LinearLayout mapLocArea, sourceLocationView, destLocationView;
    MTextView mapLocTxt, homePlaceTxt, homePlaceHTxt;
    LinearLayout homeLocArea;
    MTextView placesTxt, recentLocHTxtView;
    LinearLayout placearea, placesarea;
    LinearLayout placesInfoArea;
    ImageView homeActionImgView, ivRightArrow2;

    JSONArray SourceLocations_arr;
    JSONArray DestinationLocations_arr;
    RecentLocationAdpater recentLocationAdpater;
    ArrayList<HashMap<String, String>> recentLocList = new ArrayList<>();
    ArrayList<HashMap<String, String>> colorHasmap = new ArrayList<HashMap<String, String>>();
    ImageView homeroundImage, workroundImage;
    LinearLayout homeImgBack, workImgBack;
    // Handler handler = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_location);

        generalFunc = MyApp.getInstance().getGeneralFun(getActContext());
        userProfileJsonObj = generalFunc.getJsonObject(generalFunc.retrieveValue(Utils.USER_PROFILE_JSON));

        intCheck = new InternetConnection(getActContext());
        if (generalFunc.getJsonArray("RANDOM_COLORS_KEY_VAL_ARR", userProfileJsonObj) != null) {
            JSONArray jsonArray = generalFunc.getJsonArray("RANDOM_COLORS_KEY_VAL_ARR", userProfileJsonObj);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = generalFunc.getJsonObject(jsonArray, i);
                HashMap<String, String> colorMap = new HashMap<>();
                colorMap.put("BG_COLOR", generalFunc.getJsonValueStr("BG_COLOR", jsonObject));
                colorMap.put("TEXT_COLOR", generalFunc.getJsonValueStr("TEXT_COLOR", jsonObject));
                colorHasmap.add(colorMap);
            }
        }

        googleimagearea = (ImageView) findViewById(R.id.googleimagearea);
        cancelTxt = (MTextView) findViewById(R.id.cancelTxt);
        cancelTxt.setText(generalFunc.retrieveLangLBl("", "LBL_CANCEL_TXT"));

        placesRecyclerView = (RecyclerView) findViewById(R.id.placesRecyclerView);
        placesRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                Utils.hideKeyboard(getActContext());
            }
        });
        searchTxt = (EditText) findViewById(R.id.searchTxt);
        searchTxt.setHint(generalFunc.retrieveLangLBl("Search", "LBL_Search"));

        cancelTxt.setOnClickListener(new setOnClickList());
        imageCancel = (ImageView) findViewById(R.id.imageCancel);
        noPlacedata = (MTextView) findViewById(R.id.noPlacedata);
        imageCancel.setOnClickListener(new setOnClickList());

        homeroundImage = (ImageView) findViewById(R.id.homeroundImage);
        homeImgBack = (LinearLayout) findViewById(R.id.homeImgBack);

        workroundImage = (ImageView) findViewById(R.id.workroundImage);
        workImgBack = (LinearLayout) findViewById(R.id.workImgBack);

        homeLocArea = (LinearLayout) findViewById(R.id.homeLocArea);
        placesInfoArea = (LinearLayout) findViewById(R.id.placesInfoArea);
        placearea = (LinearLayout) findViewById(R.id.placearea);
        placesarea = (LinearLayout) findViewById(R.id.placesarea);
        homeActionImgView = (ImageView) findViewById(R.id.homeActionImgView);
        ivRightArrow2 = (ImageView) findViewById(R.id.ivRightArrow2);
        placesTxt = (MTextView) findViewById(R.id.locPlacesTxt);
        homePlaceTxt = (MTextView) findViewById(R.id.homePlaceTxt);
        homePlaceHTxt = (MTextView) findViewById(R.id.homePlaceHTxt);
        recentLocHTxtView = (MTextView) findViewById(R.id.recentLocHTxtView);
        mapLocArea = (LinearLayout) findViewById(R.id.mapLocArea);
        mapLocArea.setOnClickListener(new setOnClickList());
        mapLocTxt = (MTextView) findViewById(R.id.mapLocTxt);
        destLocationView = (LinearLayout) findViewById(R.id.destLocationView);
        sourceLocationView = (LinearLayout) findViewById(R.id.sourceLocationView);

        homeLocArea.setOnClickListener(new setOnClickList());
        placesTxt.setOnClickListener(new setOnClickList());
        homeActionImgView.setOnClickListener(new setOnClickList());

        if (generalFunc.isRTLmode()) {
            ivRightArrow2.setRotation(180);
        }

        setLables();

        showAddHomeAddressArea();
        placelist = new ArrayList<>();
        MIN_CHAR_REQ_GOOGLE_AUTO_COMPLETE = GeneralFunctions.parseIntegerValue(2, generalFunc.getJsonValueStr("MIN_CHAR_REQ_GOOGLE_AUTO_COMPLETE", userProfileJsonObj));


        searchTxt.setOnFocusChangeListener((v, hasFocus) -> {

            if (!hasFocus) {
                Utils.hideSoftKeyboard((Activity) getActContext(), searchTxt);
            } else {
                Utils.showSoftKeyboard((Activity) getActContext(), searchTxt);
            }
        });

        searchTxt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                if (currentSearchQuery.equals(s.toString().trim())) {
                    return;
                }

                currentSearchQuery = searchTxt.getText().toString();

                if (s.length() >= MIN_CHAR_REQ_GOOGLE_AUTO_COMPLETE) {
                    if (session_token.trim().equalsIgnoreCase("")) {
                        session_token = Utils.userType + "_" + generalFunc.getMemberId() + "_" + System.currentTimeMillis();
                        initializeSessionRegeneration();
                    }

                    placesRecyclerView.setVisibility(View.VISIBLE);

                    if (getIntent().hasExtra("eSystem")) {
                        googleimagearea.setVisibility(View.VISIBLE);
                    }
                    placesarea.setVisibility(View.GONE);
                    getGooglePlaces(currentSearchQuery);
                } else {
                    if (getIntent().getBooleanExtra("isPlaceAreaShow", true)) {
                        placesarea.setVisibility(View.VISIBLE);
                    }
                    googleimagearea.setVisibility(View.GONE);
                    placesRecyclerView.setVisibility(View.GONE);
                    noPlacedata.setVisibility(View.GONE);
                }
            }
        });

        searchTxt.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                // getSearchGooglePlace(v.getText().toString());
                getGooglePlaces(v.getText().toString());
                return true;
            }
            return false;
        });

        if (getIntent().hasExtra("hideSetMapLoc")) {
            mapLocArea.setVisibility(View.GONE);
//            placesarea.setVisibility(View.GONE);
        } else {
            mapLocArea.setVisibility(View.VISIBLE);
        }

        if (getIntent().hasExtra("eSystem")) {
            mapLocArea.setVisibility(View.GONE);
        }

        placesRecyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        placesRecyclerView.setLayoutManager(mLayoutManager);
        // placesRecyclerView.setItemAnimator(new DefaultItemAnimator());

        if (getCallingActivity() != null && getCallingActivity().getClassName().equals(AddAddressActivity.class.getName())) {
            (findViewById(R.id.recentScrollView)).setVisibility(View.GONE);
            (findViewById(R.id.recentLocHTxtView)).setVisibility(View.GONE);
        }

    }

    long tempTime = 0;

    boolean checkTime() {
        boolean isAllow = false;
        Logger.d("CheckTime", "::" + System.currentTimeMillis() + "::" + tempTime);
        if (System.currentTimeMillis() - tempTime > 750) {
            return true;
        }

        return isAllow;
    }


    private void showAddHomeAddressArea() {
        if (getIntent().hasExtra("requestType")) {
            placesarea.setVisibility(View.VISIBLE);
            placesRecyclerView.setVisibility(View.GONE);
            googleimagearea.setVisibility(View.GONE);
            placesInfoArea.setVisibility(View.VISIBLE);
            setWhichLocationAreaSelected(getIntent().getStringExtra("locationArea"));
        } else {
            placesarea.setVisibility(View.GONE);
            placesRecyclerView.setVisibility(View.VISIBLE);
            placesInfoArea.setVisibility(View.GONE);
            googleimagearea.setVisibility(View.VISIBLE);
        }
    }

    private void setLables() {
        homePlaceTxt.setText(generalFunc.retrieveLangLBl("", "LBL_ADD_HOME_PLACE_TXT"));
        homePlaceHTxt.setText(generalFunc.retrieveLangLBl("", "LBL_HOME_PLACE"));
        mapLocTxt.setText(generalFunc.retrieveLangLBl("Set location on map", "LBL_SET_LOC_ON_MAP"));

        placesTxt.setText(generalFunc.retrieveLangLBl("", "LBL_FAV_LOCATIONS"));
        recentLocHTxtView.setText(generalFunc.retrieveLangLBl("", "LBL_RECENT_LOCATIONS"));
        setRandomColor();

    }

    public void setRandomColor() {
        if (getRandomColor() != null) {
            HashMap<String, String> colorMap = getRandomColor();

            homeImgBack.getBackground().setColorFilter(Color.parseColor(colorMap.get("BG_COLOR")), PorterDuff.Mode.SRC_ATOP);
            homeroundImage.setColorFilter(Color.parseColor(colorMap.get("TEXT_COLOR")), PorterDuff.Mode.SRC_IN);
            colorMap = getRandomColor();
            workImgBack.getBackground().setColorFilter(Color.parseColor(colorMap.get("BG_COLOR")), PorterDuff.Mode.SRC_ATOP);
            workroundImage.setColorFilter(Color.parseColor(colorMap.get("TEXT_COLOR")), PorterDuff.Mode.SRC_IN);

        }


    }


    public void checkPlaces(final String whichLocationArea) {

        String home_address_str = generalFunc.retrieveValue("userHomeLocationAddress");
//        if(home_address_str.equalsIgnoreCase("")){
//            home_address_str = "----";
//        }
//        String work_address_str = mpref_place.getString("userWorkLocationAddress", "");
//        if(work_address_str.equalsIgnoreCase("")){
//            work_address_str = "----";
//        }

        if (home_address_str != null && !home_address_str.equalsIgnoreCase("")) {

            homePlaceTxt.setText(generalFunc.retrieveLangLBl("", "LBL_HOME_PLACE"));
            homePlaceTxt.setTextColor(getResources().getColor(R.color.black));
            homePlaceHTxt.setText("" + home_address_str);
            homePlaceHTxt.setVisibility(View.VISIBLE);
            homePlaceTxt.setVisibility(View.VISIBLE);
            homePlaceHTxt.setTextColor(Color.parseColor("#909090"));
            homeActionImgView.setImageResource(R.mipmap.ic_edit);

        } else {
            homePlaceHTxt.setText(generalFunc.retrieveLangLBl("", "LBL_HOME_PLACE"));
            homePlaceTxt.setText("" + generalFunc.retrieveLangLBl("", "LBL_ADD_HOME_PLACE_TXT"));
            homePlaceTxt.setTextColor(Color.parseColor("#909090"));
            homeActionImgView.setImageResource(R.mipmap.ic_pluse);
        }


        if (home_address_str != null && home_address_str.equalsIgnoreCase("")) {
            homePlaceHTxt.setText(generalFunc.retrieveLangLBl("", "LBL_ADD_HOME_PLACE_TXT"));
            homePlaceTxt.setText("----");
            homePlaceTxt.setVisibility(View.GONE);
            homePlaceHTxt.setTextColor(getResources().getColor(R.color.black));

            homePlaceTxt.setTextColor(Color.parseColor("#909090"));

            homePlaceHTxt.setVisibility(View.VISIBLE);
            homeActionImgView.setImageResource(R.mipmap.ic_pluse);
        }

    }

    private void getRecentLocations(final String whichView) {
        final LayoutInflater mInflater = (LayoutInflater)
                getActContext().getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

        DestinationLocations_arr = generalFunc.getJsonArray("DestinationLocations", userProfileJsonObj);
        SourceLocations_arr = generalFunc.getJsonArray("SourceLocations", userProfileJsonObj);

        if (DestinationLocations_arr != null || SourceLocations_arr != null) {

            if (whichView.equals("dest")) {

                if (destLocationView != null) {
                    destLocationView.removeAllViews();
                    recentLocList.clear();
                }
                for (int i = 0; i < DestinationLocations_arr.length(); i++) {
                    final View view = mInflater.inflate(R.layout.item_recent_loc_design, null);
                    JSONObject destLoc_obj = generalFunc.getJsonObject(DestinationLocations_arr, i);

                    MTextView recentAddrTxtView = (MTextView) view.findViewById(R.id.recentAddrTxtView);
                    LinearLayout recentAdapterView = (LinearLayout) view.findViewById(R.id.recentAdapterView);

                    LinearLayout imageabackArea = (LinearLayout) view.findViewById(R.id.imageabackArea);
                    ImageView roundImage = (ImageView) view.findViewById(R.id.roundImage);
                    final String tEndLat = generalFunc.getJsonValueStr("tDestLatitude", destLoc_obj);
                    final String tEndLong = generalFunc.getJsonValueStr("tDestLongitude", destLoc_obj);
                    final String tDaddress = generalFunc.getJsonValueStr("tDaddress", destLoc_obj);

                    recentAddrTxtView.setText(tDaddress);

                    HashMap<String, String> map = new HashMap<>();
                    map.put("tLat", tEndLat);
                    map.put("tLong", tEndLong);
                    map.put("taddress", tDaddress);

                    if (getRandomColor() != null) {
                        HashMap<String, String> colorMap = getRandomColor();
                        map.put("BG_COLOR", colorMap.get("BG_COLOR"));
                        map.put("TEXT_COLOR", colorMap.get("TEXT_COLOR"));
                        imageabackArea.getBackground().setColorFilter(Color.parseColor(colorMap.get("BG_COLOR")), PorterDuff.Mode.SRC_ATOP);
                        roundImage.setColorFilter(Color.parseColor(colorMap.get("TEXT_COLOR")), PorterDuff.Mode.SRC_IN);
                    }

                    recentLocList.add(map);
                    recentAdapterView.setOnClickListener(view1 -> {
                        if (whichView != null) {
                            if (whichView.equals("dest")) {

                                Bundle bn = new Bundle();
                                bn.putString("Address", tDaddress);
                                bn.putString("Latitude", "" + tEndLat);
                                bn.putString("Longitude", "" + tEndLong);
                                bn.putBoolean("isSkip", false);
                                new StartActProcess(getActContext()).setOkResult(bn);

                                finish();
                            }

                        } else {

                        }
                    });
                    destLocationView.addView(view);
                    destLocationView.setVisibility(View.VISIBLE);
                }
            } else {
                if (sourceLocationView != null) {
                    sourceLocationView.removeAllViews();
                    recentLocList.clear();
                }
                for (int i = 0; i < SourceLocations_arr.length(); i++) {

                    final View view = mInflater.inflate(R.layout.item_recent_loc_design, null);
                    JSONObject loc_obj = generalFunc.getJsonObject(SourceLocations_arr, i);

                    MTextView recentAddrTxtView = (MTextView) view.findViewById(R.id.recentAddrTxtView);
                    LinearLayout recentAdapterView = (LinearLayout) view.findViewById(R.id.recentAdapterView);
                    LinearLayout imageabackArea = (LinearLayout) view.findViewById(R.id.imageabackArea);
                    ImageView roundImage = (ImageView) view.findViewById(R.id.roundImage);

                    final String tStartLat = generalFunc.getJsonValueStr("tStartLat", loc_obj);
                    final String tStartLong = generalFunc.getJsonValueStr("tStartLong", loc_obj);
                    final String tSaddress = generalFunc.getJsonValueStr("tSaddress", loc_obj);

                    recentAddrTxtView.setText(tSaddress);
                    HashMap<String, String> map = new HashMap<>();
                    map.put("tLat", tStartLat);
                    map.put("tLong", tStartLong);
                    map.put("taddress", tSaddress);

                    if (getRandomColor() != null) {
                        HashMap<String, String> colorMap = getRandomColor();
                        map.put("BG_COLOR", colorMap.get("BG_COLOR"));
                        map.put("TEXT_COLOR", colorMap.get("TEXT_COLOR"));
                        imageabackArea.getBackground().setColorFilter(Color.parseColor(colorMap.get("BG_COLOR")), PorterDuff.Mode.SRC_ATOP);
                        roundImage.setColorFilter(Color.parseColor(colorMap.get("TEXT_COLOR")), PorterDuff.Mode.SRC_IN);
                    }

                    recentLocList.add(map);
                    recentAdapterView.setOnClickListener(view12 -> {
                        if (whichView != null) {
                            if (whichView.equals("source")) {

                                Bundle bn = new Bundle();
                                bn.putString("Address", tSaddress);
                                bn.putString("Latitude", "" + tStartLat);
                                bn.putString("Longitude", "" + tStartLong);

                                new StartActProcess(getActContext()).setOkResult(bn);

                                finish();

                            }


                        } else {

                        }
                    });
                    sourceLocationView.addView(view);
                    sourceLocationView.setVisibility(View.VISIBLE);
                }
            }

        } else {
            destLocationView.setVisibility(View.GONE);
            sourceLocationView.setVisibility(View.GONE);
            recentLocHTxtView.setVisibility(View.GONE);
        }
    }



    private void searchResult(JSONObject responseStringObj) {
        if (generalFunc.getJsonValueStr("status", responseStringObj).equals("OK")) {
            JSONArray predictionsArr = generalFunc.getJsonArray("predictions", responseStringObj);

            if (searchTxt.getText().toString().length() == 0) {
                placesRecyclerView.setVisibility(View.GONE);
                noPlacedata.setVisibility(View.GONE);
                return;
            }

            placelist.clear();
            for (int i = 0; i < predictionsArr.length(); i++) {
                JSONObject item = generalFunc.getJsonObject(predictionsArr, i);

                if (!generalFunc.getJsonValue("place_id", item.toString()).equals("")) {

                    HashMap<String, String> map = new HashMap<String, String>();

                    JSONObject structured_formatting = generalFunc.getJsonObject("structured_formatting", item);
                    map.put("main_text", generalFunc.getJsonValueStr("main_text", structured_formatting));
                    map.put("secondary_text", generalFunc.getJsonValueStr("secondary_text", structured_formatting));
                    map.put("place_id", generalFunc.getJsonValueStr("place_id", item));
                    map.put("description", generalFunc.getJsonValueStr("description", item));
                    map.put("session_token", session_token);

                    placelist.add(map);

                }
            }
            if (placelist.size() > 0) {
                placesRecyclerView.setVisibility(View.VISIBLE);

                if (placesAdapter == null) {
                    placesAdapter = new PlacesAdapter(getActContext(), placelist);
                    placesRecyclerView.setAdapter(placesAdapter);
                    placesAdapter.itemRecentLocClick(SearchLocationActivity.this);

                } else {
                    placesAdapter.notifyDataSetChanged();
                }
            }
        } else if (generalFunc.getJsonValueStr("status", responseStringObj).equals("ZERO_RESULTS")) {
            placelist.clear();
            if (placesAdapter != null) {
                placesAdapter.notifyDataSetChanged();
            }

            String msg = generalFunc.retrieveLangLBl("We didn't find any places matched to your entered place. Please try again with another text.", "LBL_NO_PLACES_FOUND");
            noPlacedata.setText(msg);
            placesRecyclerView.setVisibility(View.VISIBLE);

            noPlacedata.setVisibility(View.VISIBLE);

        } else {
            placelist.clear();
            if (placesAdapter != null) {
                placesAdapter.notifyDataSetChanged();
            }
            String msg = "";
            if (!intCheck.isNetworkConnected() && !intCheck.check_int()) {
                msg = generalFunc.retrieveLangLBl("No Internet Connection", "LBL_NO_INTERNET_TXT");

            } else {
                msg = generalFunc.retrieveLangLBl("Error occurred while searching nearest places. Please try again later.", "LBL_PLACE_SEARCH_ERROR");

            }

            noPlacedata.setText(msg);
            placesRecyclerView.setVisibility(View.VISIBLE);
            noPlacedata.setVisibility(View.VISIBLE);
        }
    }

    public void initializeSessionRegeneration() {

        if (sessionTokenFreqTask != null) {
            sessionTokenFreqTask.stopRepeatingTask();
        }
        sessionTokenFreqTask = new UpdateFrequentTask(170000);
        sessionTokenFreqTask.setTaskRunListener(() -> session_token = Utils.userType + "_" + generalFunc.getMemberId() + "_" + System.currentTimeMillis());

        sessionTokenFreqTask.startRepeatingTask();
    }


    @Override
    public void itemRecentLocClick(int position) {

        //getSelectAddresLatLong(placelist.get(position).get("place_id"), placelist.get(position).get("description"));
        //   getSelectAddresLatLong(placelist.get(position).get("place_id"), placelist.get(position).get("description"), placelist.get(position).get("session_token"), placelist.get(position).get("lat"), placelist.get(position).get("lng"));
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("Place_id", placelist.get(position).get("Place_id"));
        hashMap.put("description", placelist.get(position).get("description"));
        hashMap.put("session_token", placelist.get(position).get("session_token"));


        if (getIntent().getDoubleExtra("long", 0.0) != 0.0) {

            hashMap.put("latitude", placelist.get(position).get("lat"));
            hashMap.put("longitude", placelist.get(position).get("lng"));
        } else {
            hashMap.put("latitude", "");
            hashMap.put("longitude", "");
        }
        // hashMap.put("selectedPos", selectedPos + "");


        if (placelist.get(position).get("Place_id") == null || placelist.get(position).get("Place_id").equals("")) {
            resetOrAddDest(0, placelist.get(position).get("description"), GeneralFunctions.parseDoubleValue(0, placelist.get(position).get("latitude")), GeneralFunctions.parseDoubleValue(0, placelist.get(position).get("longitude")), "" + false);
        } else {
            MapServiceApi.getPlaceDetailsService(getActContext(), hashMap, this);
        }

    }

    public void setWhichLocationAreaSelected(String locationArea) {
        this.whichLocation = locationArea;

        if (locationArea.equals("dest")) {
            destLocationView.setVisibility(View.VISIBLE);
            sourceLocationView.setVisibility(View.GONE);
            getRecentLocations("dest");
            checkPlaces(locationArea);

        } else if (locationArea.equals("source")) {
            destLocationView.setVisibility(View.GONE);
            sourceLocationView.setVisibility(View.VISIBLE);
            getRecentLocations("source");
            checkPlaces(locationArea);
        }

    }

    public Context getActContext() {
        return SearchLocationActivity.this;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Utils.ADD_HOME_LOC_REQ_CODE && resultCode == RESULT_OK && data != null) {
            HashMap<String, String> storeData = new HashMap<>();
            storeData.put("userHomeLocationLatitude", "" + data.getStringExtra("Latitude"));
            storeData.put("userHomeLocationLongitude", "" + data.getStringExtra("Longitude"));
            storeData.put("userHomeLocationAddress", "" + data.getStringExtra("Address"));
            generalFunc.storeData(storeData);

            homePlaceTxt.setText(data.getStringExtra("Address"));
            checkPlaces(whichLocation);


            Bundle bn = new Bundle();
            bn.putString("Latitude", data.getStringExtra("Latitude"));
            bn.putString("Longitude", "" + data.getStringExtra("Longitude"));
            bn.putString("Address", "" + data.getStringExtra("Address"));
            new StartActProcess(getActContext()).setOkResult(bn);
            finish();

        } else if (requestCode == Utils.ADD_MAP_LOC_REQ_CODE && resultCode == RESULT_OK && data != null) {

            Bundle bn = new Bundle();
            bn.putString("Latitude", data.getStringExtra("Latitude"));
            bn.putString("Longitude", "" + data.getStringExtra("Longitude"));
            bn.putString("Address", "" + data.getStringExtra("Address"));


            new StartActProcess(getActContext()).setOkResult(bn);
            finish();

        }
    }

    public void getGooglePlaces(String input) {

        String session_token = this.session_token;


        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("input", input);

        if (getIntent().getDoubleExtra("long", 0.0) != 0.0) {

            hashMap.put("latitude", getIntent().getDoubleExtra("lat", 0.0) + "");
            hashMap.put("longitude", getIntent().getDoubleExtra("long", 0.0) + "");
        } else {
            hashMap.put("latitude", "");
            hashMap.put("longitude", "");
        }
        hashMap.put("session_token", session_token);

        MapServiceApi.getAutoCompleteService(getActContext(), hashMap, this);


        noPlacedata.setVisibility(View.GONE);

    }


    @Override
    public void searchResult(ArrayList<HashMap<String, String>> placelist, int selectedPos, String input) {
        this.placelist.clear();
        this.placelist.addAll(placelist);
        imageCancel.setVisibility(View.VISIBLE);


        if (currentSearchQuery.length() == 0) {
            placesRecyclerView.setVisibility(View.GONE);
            noPlacedata.setVisibility(View.GONE);

            return;
        }


        if (placelist.size() > 0) {
            noPlacedata.setVisibility(View.GONE);
            placesRecyclerView.setVisibility(View.VISIBLE);
            if (placesAdapter == null) {
                placesAdapter = new PlacesAdapter(getActContext(), this.placelist);
                placesRecyclerView.setAdapter(placesAdapter);
                placesAdapter.itemRecentLocClick(SearchLocationActivity.this);

            } else {
                placesAdapter.notifyDataSetChanged();
            }
        } else if (currentSearchQuery.length() == 0) {
            placelist.clear();
            if (placesAdapter != null) {
                placesAdapter.notifyDataSetChanged();
            }

            String msg = generalFunc.retrieveLangLBl("We didn't find any places matched to your entered place. Please try again with another text.", "LBL_NO_PLACES_FOUND");
            noPlacedata.setText(msg);
            placesRecyclerView.setVisibility(View.VISIBLE);

            noPlacedata.setVisibility(View.VISIBLE);

            return;
        } else {

            placelist.clear();
            if (placesAdapter != null) {
                placesAdapter.notifyDataSetChanged();
            }
            String msg = "";
            if (!intCheck.isNetworkConnected() && !intCheck.check_int()) {
                msg = generalFunc.retrieveLangLBl("No Internet Connection", "LBL_NO_INTERNET_TXT");

            } else {
                msg = generalFunc.retrieveLangLBl("Error occurred while searching nearest places. Please try again later.", "LBL_PLACE_SEARCH_ERROR");

            }

            noPlacedata.setText(msg);
            placesRecyclerView.setVisibility(View.VISIBLE);
            noPlacedata.setVisibility(View.VISIBLE);

            //} else if (generalFunc.getJsonValue("status", responseString).equals("ZERO_RESULTS")) {
        }


    }

    @Override
    public void resetOrAddDest(int selPos, String address, double latitude, double longitude, String isSkip) {
        Bundle bn = new Bundle();
        bn.putString("Address", address);
        bn.putString("Latitude", "" + latitude);
        bn.putString("Longitude", "" + longitude);
        if (Utils.checkText(isSkip)) {
            bn.putBoolean("isSkip", isSkip.equalsIgnoreCase("true") ? true : false);
        }

        Utils.hideKeyboard(this);

        new StartActProcess(getActContext()).setOkResult(bn);


        finish();


    }

    @Override
    public void directionResult(HashMap<String, String> directionlist) {

    }

    @Override
    public void geoCodeAddressFound(String address, double latitude, double longitude, String geocodeobject) {

    }

    public class setOnClickList implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            int i = view.getId();
            Bundle bndl = new Bundle();

            if (i == R.id.cancelTxt) {
                finish();

            } else if (i == R.id.imageCancel) {
                placesRecyclerView.setVisibility(View.GONE);
                searchTxt.setText("");
                noPlacedata.setVisibility(View.GONE);
            } else if (i == R.id.homeLocArea) {

//                if (mpref_place != null) {

                final String home_address_str = generalFunc.retrieveValue("userHomeLocationAddress");
                final String home_addr_latitude = generalFunc.retrieveValue("userHomeLocationLatitude");
                final String home_addr_longitude = generalFunc.retrieveValue("userHomeLocationLongitude");

                if (home_address_str != null && !home_address_str.equalsIgnoreCase("")) {

                    if (whichLocation.equals("dest")) {


                        LatLng placeLocation = new LatLng(generalFunc.parseDoubleValue(0.0, home_addr_latitude), generalFunc.parseDoubleValue(0.0, home_addr_longitude));


                        Bundle bn = new Bundle();
                        bn.putString("Address", home_address_str);
                        bn.putString("Latitude", "" + placeLocation.latitude);
                        bn.putString("Longitude", "" + placeLocation.longitude);

                        bn.putBoolean("isSkip", false);
                        new StartActProcess(getActContext()).setOkResult(bn);
                        finish();
                    } else {

                        LatLng placeLocation = new LatLng(generalFunc.parseDoubleValue(0.0, home_addr_latitude), generalFunc.parseDoubleValue(0.0, home_addr_longitude));

                        Bundle bn = new Bundle();
                        bn.putString("Address", home_address_str);
                        bn.putString("Latitude", "" + placeLocation.latitude);
                        bn.putString("Longitude", "" + placeLocation.longitude);
                        bn.putBoolean("isSkip", false);
                        new StartActProcess(getActContext()).setOkResult(bn);
                        finish();
                    }
                } else {
                    bndl.putString("isHome", "true");
                    new StartActProcess(getActContext()).startActForResult(SearchPickupLocationActivity.class,
                            bndl, Utils.ADD_HOME_LOC_REQ_CODE);
                }
               /* }else {
                    bndl.putString("isHome", "true");
                    new StartActProcess(getActContext()).startActForResult(SearchPickupLocationActivity.class,
                            bndl, Utils.ADD_HOME_LOC_REQ_CODE);
                }*/

            } else if (i == R.id.homeActionImgView) {
                if (intCheck.isNetworkConnected()) {
                    Bundle bn = new Bundle();
                    bn.putString("isHome", "true");
                    new StartActProcess(getActContext()).startActForResult(SearchPickupLocationActivity.class,
                            bn, Utils.ADD_HOME_LOC_REQ_CODE);
                } else {
                    generalFunc.showMessage(mapLocArea, generalFunc.retrieveLangLBl("", "LBL_NO_INTERNET_TXT"));
                }
            } else if (i == R.id.mapLocArea) {
                bndl.putString("locationArea", getIntent().getStringExtra("locationArea"));
                String from = !whichLocation.equals("dest") ? "isPickUpLoc" : "isDestLoc";
                String lati = !whichLocation.equals("dest") ? "PickUpLatitude" : "DestLatitude";
                String longi = !whichLocation.equals("dest") ? "PickUpLongitude" : "DestLongitude";
                String address = !whichLocation.equals("dest") ? "PickUpAddress" : "DestAddress";


                bndl.putString(from, "true");
                if (getIntent().getDoubleExtra("lat", 0.0) != 0.0 && getIntent().getDoubleExtra("long", 0.0) != 0.0) {
                    bndl.putString(lati, "" + getIntent().getDoubleExtra("lat", 0.0));
                    bndl.putString(longi, "" + getIntent().getDoubleExtra("long", 0.0));
                    if (getIntent().hasExtra("address") && Utils.checkText(getIntent().getStringExtra("address"))) {
                        bndl.putString(address, "" + getIntent().getStringExtra("address"));
                    } else {
                        bndl.putString(address, "");
                    }

                }

                bndl.putString("IS_FROM_SELECT_LOC", "Yes");

                new StartActProcess(getActContext()).startActForResult(SearchPickupLocationActivity.class,
                        bndl, Utils.ADD_MAP_LOC_REQ_CODE);


            }

        }
    }

    @Override
    protected void onDestroy() {
        if (sessionTokenFreqTask != null) {
            sessionTokenFreqTask.stopRepeatingTask();
        }
        super.onDestroy();
    }

    public HashMap<String, String> getRandomColor() {
        if (colorHasmap.size() > 0) {
            int randomIndex = new Random().nextInt(colorHasmap.size());

            return colorHasmap.get(randomIndex);
        }
        return null;
    }
}
