package com.taxifgo.driver;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.general.files.ExecuteWebServerUrl;
import com.general.files.GeneralFunctions;
import com.general.files.MyApp;
import com.general.files.StartActProcess;
import com.utils.Utils;
import com.view.ErrorView;
import com.view.MTextView;
import com.view.simpleratingbar.SimpleRatingBar;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class SelectedDayHistoryActivity extends AppCompatActivity {

    MTextView titleTxt;
    ImageView backImgView;

    GeneralFunctions generalFunc;

    ProgressBar loading;
    ErrorView errorView;
    LinearLayout dataContainer;
    LinearLayout listContainer, listCancelContainer;

    ArrayList<String> list_item;
    ArrayList<String> cancel_list_item;
    MTextView fareHTxt;
    String selecteddate = "";
    MTextView tripsCountTxt, tripCancelTxt;
    ArrayList<HashMap<String, String>> filterlist;
    ImageView filterImageview;
    androidx.appcompat.app.AlertDialog list_type;
    String selFilterType = "";

    JSONObject userProfileJsonObj;
    String app_type = "Ride";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selected_day_history);

        generalFunc = MyApp.getInstance().getGeneralFun(getActContext());
        userProfileJsonObj = generalFunc.getJsonObject(generalFunc.retrieveValue(Utils.USER_PROFILE_JSON));
        app_type = generalFunc.getJsonValueStr("APP_TYPE", userProfileJsonObj);

        titleTxt = (MTextView) findViewById(R.id.titleTxt);
        backImgView = (ImageView) findViewById(R.id.backImgView);
        errorView = (ErrorView) findViewById(R.id.errorView);
        loading = (ProgressBar) findViewById(R.id.loading);
        dataContainer = (LinearLayout) findViewById(R.id.dataContainer);
        listContainer = (LinearLayout) findViewById(R.id.listContainer);
        listCancelContainer = (LinearLayout) findViewById(R.id.listCancelContainer);
        fareHTxt = (MTextView) findViewById(R.id.fareHTxt);
        tripsCountTxt = (MTextView) findViewById(R.id.tripsCountTxt);
        tripCancelTxt = (MTextView) findViewById(R.id.tripCancelTxt);
        filterImageview = (ImageView) findViewById(R.id.filterImageview);
        filterImageview.setOnClickListener(new setOnClickList());


        backImgView.setOnClickListener(new setOnClickList());

        setLabels();

        try {
            titleTxt.setText(generalFunc.getDateFormatedType(getIntent().getStringExtra("SELECTED_DATE"), Utils.DefaultDatefromate, Utils.dateFormateInHeaderBar));
        } catch (Exception e) {
            e.printStackTrace();
        }
        getDetails();
    }

    public void setLabels() {
        ((MTextView) findViewById(R.id.tripsCompletedTxt)).setText(generalFunc.retrieveLangLBl("Completed Trips", "LBL_TOTAL_SERVICES"));
        ((MTextView) findViewById(R.id.tripEarningTxt)).setText(generalFunc.retrieveLangLBl("Trip Earning", "LBL_MY_EARNING"));
        ((MTextView) findViewById(R.id.avgRatingTxt)).setText(generalFunc.retrieveLangLBl("Avg. Rating", "LBL_AVG_RATING"));
        fareHTxt.setText(generalFunc.retrieveLangLBl("", "LBL_Total_Fare"));
        tripCancelTxt.setText(generalFunc.retrieveLangLBl("", "LBL_CANCEL_TRIP"));
    }

    public Context getActContext() {
        return SelectedDayHistoryActivity.this;
    }

    public void getDetails() {
        if (errorView.getVisibility() == View.VISIBLE) {
            errorView.setVisibility(View.GONE);
        }
        if (dataContainer.getVisibility() == View.VISIBLE) {
            dataContainer.setVisibility(View.GONE);
        }
        if (loading.getVisibility() != View.VISIBLE) {
            loading.setVisibility(View.VISIBLE);
        }

        final HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "getDriverRideHistory");
        parameters.put("iDriverId", generalFunc.getMemberId());
        parameters.put("vFilterParam", selFilterType);
        parameters.put("date", getIntent().getStringExtra("SELECTED_DATE"));

        final ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(), parameters);
        exeWebServer.setDataResponseListener(responseString -> {
            JSONObject responseStringObject=generalFunc.getJsonObject(responseString);

            if (responseStringObject != null && !responseStringObject.equals("")) {

                closeLoader();

                if (GeneralFunctions.checkDataAvail(Utils.action_str, responseStringObject)) {
                    setData(true, responseStringObject);
                } else {
                    setData(false, responseStringObject);
                }
            } else {
                generateErrorView();
            }
        });
        exeWebServer.execute();
    }

    public void setData(boolean isDataAvail, JSONObject responseString) {

        String currencySymbol = generalFunc.getJsonValueStr("CurrencySymbol", responseString);
        if (listContainer != null) {
            listContainer.removeAllViews();
        }

        if (isDataAvail) {
            (findViewById(R.id.noRidesFound)).setVisibility(View.GONE);
            if (list_item != null) {
                list_item.clear();
                list_item = null;
            }
            if (cancel_list_item != null) {
                cancel_list_item.clear();
                cancel_list_item = null;
            }


            list_item = new ArrayList<>();
            cancel_list_item = new ArrayList<>();
            if (listContainer != null) {
                listContainer.removeAllViews();
            }

            JSONArray msgArr = generalFunc.getJsonArray(Utils.message_str, responseString);
            if (msgArr != null) {

                for (int i = 0; i < msgArr.length(); i++) {
                    JSONObject obj_temp = generalFunc.getJsonObject(msgArr, i);

                    LayoutInflater inflater = (LayoutInflater) getActContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    View customView = inflater.inflate(R.layout.selected_day_trip_history_item, null);

                    ((MTextView) customView.findViewById(R.id.timeTxt)).setText(generalFunc.convertNumberWithRTL(generalFunc.getDateFormatedType(generalFunc.getJsonValueStr("tTripRequestDateOrig", obj_temp), Utils.OriginalDateFormate, Utils.dateFormateTimeOnly)));
                    ((MTextView) customView.findViewById(R.id.fareTxt)).setText(currencySymbol + generalFunc.convertNumberWithRTL(generalFunc.getJsonValueStr("iFare", obj_temp)));

                    (customView.findViewById(R.id.typeTxt)).setVisibility(View.VISIBLE);

                    if (generalFunc.isRTLmode()) {
                        ((ImageView) customView.findViewById(R.id.arrowImgView)).setRotation(-180);
                    }

                    ((MTextView) customView.findViewById(R.id.typeTxt)).setText(generalFunc.getJsonValueStr("vServiceTitle", obj_temp));

                    ((ImageView) customView.findViewById(R.id.arrowImgView)).setColorFilter(Color.parseColor("#2F2F2F"));

                    (customView.findViewById(R.id.tripItem)).setOnClickListener(new setOnClickList(true, i, false));
                    listContainer.addView(customView);

                    list_item.add(obj_temp.toString());
                }

                JSONArray arr_type_filter = generalFunc.getJsonArray("AppTypeFilterArr", responseString);


                if (arr_type_filter != null && arr_type_filter.length() > 0) {
                    filterlist = new ArrayList<>();
                    for (int i = 0; i < arr_type_filter.length(); i++) {
                        JSONObject obj_temp = generalFunc.getJsonObject(arr_type_filter, i);

                        HashMap<String, String> map = new HashMap<String, String>();
                        map.put("vTitle", generalFunc.getJsonValueStr("vTitle", obj_temp));
                        map.put("vFilterParam", generalFunc.getJsonValueStr("vFilterParam", obj_temp));
                        filterlist.add(map);
                    }
                    filterImageview.setVisibility(View.VISIBLE);
                }

            }

            if (cancel_list_item.size() > 0) {
                tripCancelTxt.setVisibility(View.VISIBLE);
            } else {
                tripCancelTxt.setVisibility(View.GONE);

            }
            ((MTextView) findViewById(R.id.tripEarningTxt)).setVisibility(View.VISIBLE);
        } else {
            tripCancelTxt.setVisibility(View.GONE);
            ((MTextView) findViewById(R.id.noRidesFound)).setText(generalFunc.retrieveLangLBl("", generalFunc.getJsonValueStr(Utils.message_str, responseString)));
            (findViewById(R.id.noRidesFound)).setVisibility(View.VISIBLE);
            ((MTextView) findViewById(R.id.tripEarningTxt)).setVisibility(View.GONE);
        }


        tripsCountTxt.setText(generalFunc.convertNumberWithRTL(generalFunc.getJsonValueStr("TripCount", responseString)));


        ((MTextView) findViewById(R.id.fareTxt)).setText(currencySymbol +
                generalFunc.convertNumberWithRTL(generalFunc.getJsonValueStr("TotalEarning", responseString)));
        ((SimpleRatingBar) findViewById(R.id.ratingBar)).setRating(GeneralFunctions.parseFloatValue(0, generalFunc.getJsonValueStr("AvgRating", responseString)));
        ((MTextView) findViewById(R.id.avgRatingCalcTxt)).setText("( " + GeneralFunctions.parseFloatValue(0, generalFunc.getJsonValueStr("AvgRating", responseString)) + " )");

        dataContainer.setVisibility(View.VISIBLE);


    }

    public void closeLoader() {
        if (loading.getVisibility() == View.VISIBLE) {
            loading.setVisibility(View.GONE);
        }
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
                getDetails();
            }
        });
    }

    public void BuildType() {

        ArrayList<String> typeNameList = new ArrayList<>();
        for (int i = 0; i < filterlist.size(); i++) {
            typeNameList.add((filterlist.get(i).get("vTitle")));
        }
        CharSequence[] cs_currency_txt = typeNameList.toArray(new CharSequence[typeNameList.size()]);
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getActContext());
        builder.setTitle(generalFunc.retrieveLangLBl("Select Type", "LBL_SELECT_TYPE"));
        builder.setItems(cs_currency_txt, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                // Do something with the selection
                if (list_type != null) {
                    list_type.dismiss();
                }
                selFilterType = filterlist.get(item).get("vFilterParam");
                getDetails();
            }
        });

        list_type = builder.create();

        if (generalFunc.isRTLmode() == true) {
            generalFunc.forceRTLIfSupported(list_type);
        }

        list_type.show();


    }

    public class setOnClickList implements View.OnClickListener {
        boolean isTripItemClick = false;
        int tripItemPosition = 0;
        boolean isTripItemCancle = false;

        public setOnClickList() {
        }

        public setOnClickList(boolean isTripItemClick, int tripItemPosition, boolean isTripItemCancle) {
            this.isTripItemClick = isTripItemClick;
            this.tripItemPosition = tripItemPosition;
            this.isTripItemCancle = isTripItemCancle;
        }

        @Override
        public void onClick(View view) {
            Utils.hideKeyboard(SelectedDayHistoryActivity.this);

            if (isTripItemClick == true) {
                Bundle bn = new Bundle();
                if (isTripItemCancle) {
                    bn.putString("TripData", cancel_list_item.get(tripItemPosition));
                } else {
                    bn.putString("TripData", list_item.get(tripItemPosition));
                }
                new StartActProcess(getActContext()).startActWithData(RideHistoryDetailActivity.class, bn);
            } else {
                int i = view.getId();
                if (i == R.id.backImgView) {
                    SelectedDayHistoryActivity.super.onBackPressed();
                } else if (i == R.id.filterImageview) {
                    BuildType();
                }
            }

        }
    }
}
