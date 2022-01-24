package com.taxifgo.driver;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.adapter.files.ViewStopOverDetailRecyclerAdapter;
import com.general.files.ExecuteWebServerUrl;
import com.general.files.GeneralFunctions;
import com.general.files.MyApp;
import com.utils.Utils;
import com.view.ErrorView;
import com.view.MTextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;


/**
 * Created by Admin on 03-11-2017.
 */

public class ViewStopOverDetailsActivity extends BaseActivity implements ViewStopOverDetailRecyclerAdapter.OnItemClickList {

    private ViewStopOverDetailRecyclerAdapter stopOverDetailRecyclerAdapter;
    private MTextView titleTxt;
    private ProgressBar loading;
    private ErrorView errorView;
    private GeneralFunctions generalFunc;
    ArrayList<HashMap<String, String>> stopOverDetailList = new ArrayList<>();
    HashMap<String, String> data_trip;
    String userprofileJson = "";
    private ImageView backImgView;
    private RecyclerView stopOverPointsRecyclerView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stop_over_details);

//        overridePendingTransition(R.anim.slide_out, R.anim.slide_in);

        generalFunc = MyApp.getInstance().getGeneralFun(getActContext());
        userprofileJson = generalFunc.retrieveValue(Utils.USER_PROFILE_JSON);

        if (getIntent().hasExtra("TRIP_DATA")) {
            HashMap<String, String> data = (HashMap<String, String>) getIntent().getSerializableExtra("TRIP_DATA");
            this.data_trip = data;
        }


        init();
        setLables();
        setView();
        getTripDeliveryLocations();

    }


    private void setLables() {
        titleTxt.setText(generalFunc.retrieveLangLBl("Trips", "LBL_TRIP_PLANNER_TXT"));
    }

    public Context getActContext() {
        return ViewStopOverDetailsActivity.this;
    }

    private void init() {
        loading = (ProgressBar) findViewById(R.id.loading);
        errorView = (ErrorView) findViewById(R.id.errorView);
        titleTxt = (MTextView) findViewById(R.id.titleTxt);
        backImgView = (ImageView) findViewById(R.id.backImgView);
        stopOverPointsRecyclerView = (RecyclerView) findViewById(R.id.stopOverPointsRecyclerView);
        backImgView.setOnClickListener(new setOnClickList());
    }


    public void getTripDeliveryLocations() {
        if (errorView.getVisibility() == View.VISIBLE) {
            errorView.setVisibility(View.GONE);
        }

        if (loading.getVisibility() != View.VISIBLE) {
            loading.setVisibility(View.VISIBLE);
        }
        stopOverDetailList.clear();
        stopOverDetailRecyclerAdapter.notifyDataSetChanged();

        final HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "GetStopOverPoint");
        parameters.put("iCabBookingId", "");

        String iCabBookingId=getIntent().hasExtra("iCabBookingId")?getIntent().getStringExtra("iCabBookingId"):"";
        if (Utils.checkText(iCabBookingId)) {
            parameters.put("iCabBookingId", iCabBookingId);
        }

        String iCabRequestId=getIntent().hasExtra("iCabRequestId")?getIntent().getStringExtra("iCabRequestId"):"";
        if (Utils.checkText(iCabRequestId)) {
            parameters.put("iCabRequestId", iCabRequestId);
        }
        parameters.put("iTripId", getIntent().getStringExtra("TripId"));
        parameters.put("userType", Utils.userType);
        parameters.put("iDriverId", generalFunc.getMemberId());

        final ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(), parameters);
        exeWebServer.setDataResponseListener(responseString -> {
            JSONObject responseStringObj=generalFunc.getJsonObject(responseString);

            if (responseStringObj != null && !responseStringObj.equals("")) {

                closeLoader();

                if (generalFunc.checkDataAvail(Utils.action_str, responseStringObj)) {

                    JSONArray messageArray = generalFunc.getJsonArray(Utils.message_str, responseStringObj);
                    if (messageArray != null && messageArray.length()>0) {

                        for (int i = 0; i < messageArray.length(); i++) {
                            JSONObject obj_temp = generalFunc.getJsonObject(messageArray, i);
                            HashMap<String, String> map = new HashMap<>();
                            map.put("tDAddress", generalFunc.getJsonValueStr("tDAddress", obj_temp));
                            map.put("tDestLatitude", generalFunc.getJsonValueStr("tDestLatitude", obj_temp));
                            map.put("tDestLongitude", generalFunc.getJsonValueStr("tDestLongitude", obj_temp));
                            map.put("LBL_STOPOVER_POINT", generalFunc.retrieveLangLBl("", "LBL_STOPOVER_POINT"));
                            map.put("LBL_NEXT_STOP_OVER_POINT", generalFunc.retrieveLangLBl("", "LBL_NEXT_STOP_OVER_POINT"));
                            map.put("eReached", generalFunc.getJsonValueStr("eReached", obj_temp));
                            map.put("eCanceled", generalFunc.getJsonValueStr("eCanceled", obj_temp));
                            map.put("iStopId", generalFunc.getJsonValueStr("iStopId", obj_temp));

                            stopOverDetailList.add(map);

                        }
                        stopOverDetailRecyclerAdapter.notifyDataSetChanged();

                    }
                    stopOverDetailRecyclerAdapter.notifyDataSetChanged();

                } else {
                    String msg_str = generalFunc.getJsonValueStr(Utils.message_str, responseStringObj);
                    generalFunc.showGeneralMessage(generalFunc.retrieveLangLBl("Error", "LBL_ERROR_TXT"),
                            generalFunc.retrieveLangLBl("", msg_str));
                    stopOverDetailRecyclerAdapter.notifyDataSetChanged();

                }
            } else {
                generateErrorView();
                stopOverDetailRecyclerAdapter.notifyDataSetChanged();
            }
        });
        exeWebServer.execute();
    }

    public void generateErrorView() {

        closeLoader();

        generalFunc.generateErrorView(errorView, "LBL_ERROR_TXT", "LBL_NO_INTERNET_TXT");

        if (errorView.getVisibility() != View.VISIBLE) {
            errorView.setVisibility(View.VISIBLE);
        }
        errorView.setOnRetryListener(new ErrorView.RetryListener() {
            @Override
            public void onRetry() {
                getTripDeliveryLocations();
            }
        });
    }


    public void setView() {
        stopOverDetailRecyclerAdapter = new ViewStopOverDetailRecyclerAdapter(getActContext(), ViewStopOverDetailsActivity.this, stopOverDetailList, generalFunc);
//        stopOverPointsRecyclerView.setItemAnimator(new DefaultItemAnimator());
        stopOverPointsRecyclerView.setAdapter(stopOverDetailRecyclerAdapter);
        stopOverDetailRecyclerAdapter.notifyDataSetChanged();
        stopOverDetailRecyclerAdapter.setOnItemClickList(this);
    }

    public void closeLoader() {
        if (loading.getVisibility() == View.VISIBLE) {
            loading.setVisibility(View.GONE);
        }
    }

    @Override
    public void onBackPressed() {

        if (backImgView.getVisibility() == View.VISIBLE) {
            super.onBackPressed();

//        overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
//            overridePendingTransition(R.anim.slide_in, R.anim.slide_out);


        }
    }

    @Override
    public void onItemClick(String data, String type, int position) {

    }

    public class setOnClickList implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.backImgView:
                    ViewStopOverDetailsActivity.super.onBackPressed();
                    break;

            }
        }

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
