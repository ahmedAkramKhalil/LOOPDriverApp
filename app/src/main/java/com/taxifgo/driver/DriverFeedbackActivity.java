package com.taxifgo.driver;

import android.content.Context;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.adapter.files.DriverFeedbackRecycleAdapter;
import com.general.files.ExecuteWebServerUrl;
import com.general.files.GeneralFunctions;
import com.general.files.MyApp;
import com.utils.CommonUtilities;
import com.utils.Logger;
import com.utils.Utils;
import com.view.ErrorView;
import com.view.MTextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class DriverFeedbackActivity extends AppCompatActivity {

    MTextView titleTxt;
    MTextView vAvgRatingTxt;
    ImageView backImgView;

    GeneralFunctions generalFunc;

    ProgressBar loading_ride_history;
    MTextView noRidesTxt;

    RecyclerView historyRecyclerView;
    ErrorView errorView;

    DriverFeedbackRecycleAdapter feedbackRecyclerAdapter;

    ArrayList<HashMap<String, String>> list;

    boolean mIsLoading = false;
    boolean isNextPageAvailable = false;

    String next_page_str = "";
    String vAvgRating = "";
    LinearLayout avgRatingArea;
    private JSONObject obj_userProfile;
    String app_type;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_feedback);

        generalFunc = MyApp.getInstance().getGeneralFun(getActContext());

        titleTxt = (MTextView) findViewById(R.id.titleTxt);
        backImgView = (ImageView) findViewById(R.id.backImgView);
        vAvgRatingTxt = (MTextView) findViewById(R.id.vAvgRatingTxt);

        loading_ride_history = (ProgressBar) findViewById(R.id.loading_ride_history);
        noRidesTxt = (MTextView) findViewById(R.id.noRidesTxt);
        historyRecyclerView = (RecyclerView) findViewById(R.id.historyRecyclerView);
        historyRecyclerView.setClipToPadding(false);
        avgRatingArea = (LinearLayout) findViewById(R.id.avgRatingArea);
        errorView = (ErrorView) findViewById(R.id.errorView);

        obj_userProfile = generalFunc.getJsonObject(generalFunc.retrieveValue(Utils.USER_PROFILE_JSON));

        vAvgRating = generalFunc.getJsonValueStr("vAvgRating", obj_userProfile);
        vAvgRatingTxt.setText(generalFunc.retrieveLangLBl("", "LBL_AVERAGE_RATING_TXT") + " : " + vAvgRating);

        list = new ArrayList<>();
        feedbackRecyclerAdapter = new DriverFeedbackRecycleAdapter(getActContext(), list, generalFunc, false);
        historyRecyclerView.setAdapter(feedbackRecyclerAdapter);
        backImgView.setOnClickListener(new setOnClickList());
        app_type = generalFunc.getJsonValueStr("APP_TYPE", obj_userProfile);
        setLabels();

        historyRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                int visibleItemCount = recyclerView.getLayoutManager().getChildCount();
                int totalItemCount = recyclerView.getLayoutManager().getItemCount();
                int firstVisibleItemPosition = ((LinearLayoutManager) recyclerView.getLayoutManager()).findFirstVisibleItemPosition();

                int lastInScreen = firstVisibleItemPosition + visibleItemCount;
                if ((lastInScreen == totalItemCount) && !(mIsLoading) && isNextPageAvailable == true) {
                    mIsLoading = true;
                    feedbackRecyclerAdapter.addFooterView();
                    getFeedback(true);
                }
            }
        });

        getFeedback(false);
    }

    public void setLabels() {
        if (app_type.equalsIgnoreCase(Utils.CabGeneralType_Ride) && !generalFunc.isDeliverOnlyEnabled()) {
            titleTxt.setText(generalFunc.retrieveLangLBl("Rider Feedback", "LBL_RIDER_FEEDBACK"));
        } else if (app_type.equalsIgnoreCase("Delivery")) {
            titleTxt.setText(generalFunc.retrieveLangLBl("", "LBL_SENDER_fEEDBACK"));
        } else {
            titleTxt.setText(generalFunc.retrieveLangLBl("", "LBL_USER_FEEDBACK"));
        }
    }

    public void getFeedback(final boolean isLoadMore) {
        if (errorView.getVisibility() == View.VISIBLE) {
            errorView.setVisibility(View.GONE);
        }
        if (loading_ride_history.getVisibility() != View.VISIBLE && isLoadMore == false) {
            loading_ride_history.setVisibility(View.VISIBLE);
        }

        final HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "loadDriverFeedBack");
        parameters.put("iDriverId", generalFunc.getMemberId());

        Logger.d("next_page_str", ":" + next_page_str);
        if (isLoadMore == true) {
            parameters.put("page", next_page_str);
        }

        noRidesTxt.setVisibility(View.GONE);

        final ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(), parameters);
        exeWebServer.setDataResponseListener(new ExecuteWebServerUrl.SetDataResponse() {
            @Override
            public void setResponse(String responseString) {

                noRidesTxt.setVisibility(View.GONE);

                if (responseString != null && !responseString.equals("")) {

                    closeLoader();
                    if (generalFunc.checkDataAvail(Utils.action_str, responseString)) {

                        String nextPage = generalFunc.getJsonValue("NextPage", responseString);
                        vAvgRating = generalFunc.getJsonValue("vAvgRating", responseString);

                        vAvgRatingTxt.setText(generalFunc.retrieveLangLBl("", "LBL_AVERAGE_RATING_TXT") + " : " + vAvgRating);

                        JSONArray arr_rides = generalFunc.getJsonArray(Utils.message_str, responseString);

                        if (arr_rides != null && arr_rides.length() > 0) {
                            for (int i = 0; i < arr_rides.length(); i++) {
                                JSONObject obj_temp = generalFunc.getJsonObject(arr_rides, i);

                                HashMap<String, String> map = new HashMap<String, String>();

                                map.put("iRatingId", generalFunc.getJsonValueStr("iRatingId", obj_temp));
                                map.put("iTripId", generalFunc.getJsonValueStr("iTripId", obj_temp));
                                map.put("vRating1", generalFunc.getJsonValueStr("vRating1", obj_temp));
                                String tDateOrig=generalFunc.getJsonValueStr("tDateOrig", obj_temp);
                                map.put("tDateOrig", tDateOrig);
                                map.put("tDateOrigConverted", generalFunc.convertNumberWithRTL(generalFunc.getDateFormatedType(tDateOrig, Utils.OriginalDateFormate, CommonUtilities.OriginalDateFormate))
                                );
                                map.put("vMessage", generalFunc.getJsonValueStr("vMessage", obj_temp));
                                map.put("vName", generalFunc.getJsonValueStr("vName", obj_temp));
                                map.put("vImage", generalFunc.getJsonValueStr("vImage", obj_temp));

                                map.put("LBL_READ_MORE", generalFunc.retrieveLangLBl("", "LBL_READ_MORE"));
                                map.put("JSON", obj_temp.toString());

                                list.add(map);

                            }
                        }

                        if (!nextPage.equals("") && !nextPage.equals("0")) {
                            next_page_str = nextPage;
                            isNextPageAvailable = true;
                        } else {
                            removeNextPageConfig();
                        }

                        feedbackRecyclerAdapter.notifyDataSetChanged();
                        if (list.size() > 0)
                            avgRatingArea.setVisibility(View.VISIBLE);

                    } else {
                        if (list.size() == 0) {
                            removeNextPageConfig();
                            noRidesTxt.setText(generalFunc.retrieveLangLBl("", generalFunc.getJsonValue(Utils.message_str, responseString)));
                            noRidesTxt.setVisibility(View.VISIBLE);
                            avgRatingArea.setVisibility(View.GONE);
                        }

                    }
                } else {
                    if (isLoadMore == false) {
                        removeNextPageConfig();
                        generateErrorView();
                    }

                }

                mIsLoading = false;
            }
        });
        exeWebServer.execute();
    }

    public void removeNextPageConfig() {
        next_page_str = "";
        isNextPageAvailable = false;
        mIsLoading = false;
        feedbackRecyclerAdapter.removeFooterView();
    }

    public void closeLoader() {
        if (loading_ride_history.getVisibility() == View.VISIBLE) {
            loading_ride_history.setVisibility(View.GONE);
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
                getFeedback(false);
            }
        });
    }

    public Context getActContext() {
        return DriverFeedbackActivity.this;
    }

    public class setOnClickList implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            int i = view.getId();
            Utils.hideKeyboard(DriverFeedbackActivity.this);
            if (i == R.id.backImgView) {
                DriverFeedbackActivity.super.onBackPressed();
            }
        }
    }
}
