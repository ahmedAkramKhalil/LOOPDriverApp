package com.taxifgo.driver;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.adapter.files.SubscriptionAdapter;
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

public class SubscriptionHistoryActivity extends AppCompatActivity {

    public GeneralFunctions generalFunc;
    public String userProfileJson;

    MTextView noDataTxt;
    MTextView subscriptionTypeTitleTxt;
    MTextView subscriptionDesTxt;
    MTextView titleTxt;
    ImageView backImgView;
    RecyclerView subscriptionRecyclerView;

    ArrayList<HashMap<String, String>> list = new ArrayList<>();
    SubscriptionAdapter subscriptionAdapter;

    String next_page_str = "";
    boolean isNextPageAvailable = false;
    boolean mIsLoading = false;

    LinearLayout contentArea;
    ProgressBar loading;
    ErrorView errorView;
    MTextView noPlansTxt;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subscription);
        generalFunc = MyApp.getInstance().getGeneralFun(getActContext());

        userProfileJson = generalFunc.retrieveValue(Utils.USER_PROFILE_JSON);

        initView();
        setLables();

        ((ImageView)findViewById(R.id.iv_icon)).setImageResource(R.mipmap.ic_waybill);

        subscriptionAdapter = new SubscriptionAdapter(getActContext(), list, "History", generalFunc, false);
        subscriptionRecyclerView.setAdapter(subscriptionAdapter);


        subscriptionRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                int visibleItemCount = recyclerView.getLayoutManager().getChildCount();
                int totalItemCount = recyclerView.getLayoutManager().getItemCount();
                int firstVisibleItemPosition = ((LinearLayoutManager) recyclerView.getLayoutManager()).findFirstVisibleItemPosition();

                int lastInScreen = firstVisibleItemPosition + visibleItemCount;
                if ((lastInScreen == totalItemCount) && !(mIsLoading) && isNextPageAvailable == true) {

                    mIsLoading = true;
                    getDriverSubscriptionHistory(true);
                    subscriptionAdapter.addFooterView();

                } else if (isNextPageAvailable == false) {
                    subscriptionAdapter.removeFooterView();
                }
            }
        });

        getDriverSubscriptionHistory(false);
    }

    private void getDriverSubscriptionHistory(boolean isLoadMore) {
        if (errorView.getVisibility() == View.VISIBLE) {
            errorView.setVisibility(View.GONE);
        }
        if (contentArea.getVisibility() == View.VISIBLE) {
            contentArea.setVisibility(View.GONE);
        }
        if (loading.getVisibility() != View.VISIBLE) {
            loading.setVisibility(View.VISIBLE);
        }


        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "getDriverSubscriptionHistory");
        parameters.put("iDriverId", generalFunc.getMemberId());
        parameters.put("UserType", Utils.app_type);
        if (isLoadMore) {
            parameters.put("page", next_page_str);
        }


        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(), parameters);
        exeWebServer.setDataResponseListener(responseString -> {

            JSONObject responseStringObj = generalFunc.getJsonObject(responseString);

            if (responseStringObj != null && !responseStringObj.equals("")) {
                Logger.d("DATA_RESPO","History responseStringObj"+responseStringObj.toString());

                if (generalFunc.checkDataAvail(Utils.action_str, responseStringObj)) {
                    String nextPage = generalFunc.getJsonValueStr("NextPage", responseStringObj);
                    contentArea.setVisibility(View.VISIBLE);

                    JSONArray arr_subsucription_plans = generalFunc.getJsonArray(Utils.message_str, responseStringObj);

                    if (arr_subsucription_plans != null && arr_subsucription_plans.length() > 0) {

                        for (int i = 0; i < arr_subsucription_plans.length(); i++) {
                            JSONObject obj_temp = generalFunc.getJsonObject(arr_subsucription_plans, i);


                            HashMap<String, String> map = new HashMap<String, String>();
                            map.put("iDriverSubscriptionPlanId", generalFunc.getJsonValueStr("iDriverSubscriptionPlanId", obj_temp));
                            map.put("iDriverSubscriptionDetailsId", generalFunc.getJsonValueStr("iDriverSubscriptionDetailsId", obj_temp));
                            map.put("PlanTypeTitle", generalFunc.getJsonValueStr("PlanTypeTitle", obj_temp));
                            map.put("PlanType", generalFunc.getJsonValueStr("PlanType", obj_temp));

                            map.put("vPlanName", generalFunc.getJsonValueStr("vPlanName", obj_temp));
                            map.put("PlanDuration", generalFunc.convertNumberWithRTL(generalFunc.getJsonValueStr("PlanDuration", obj_temp)));
                            map.put("vPlanPeriod", generalFunc.convertNumberWithRTL(generalFunc.getJsonValueStr("vPlanPeriod", obj_temp)));
                            map.put("vPlanDescription", generalFunc.getJsonValueStr("vPlanDescription", obj_temp));
                            String planLeftDays=generalFunc.getJsonValueStr("planLeftDays", obj_temp);
                            map.put("planLeftDays",planLeftDays);
                            map.put("FormattedPlanLeftDays", generalFunc.convertNumberWithRTL(planLeftDays));

                            map.put("fPlanPrice", generalFunc.convertNumberWithRTL(generalFunc.getJsonValueStr("fPlanPrice", obj_temp)));
                            String eSubscriptionStatus=generalFunc.getJsonValueStr("eSubscriptionStatus",obj_temp);
                            map.put("eSubscriptionStatus", eSubscriptionStatus);


                            String tSubscribeDate = generalFunc.getJsonValueStr("tSubscribeDate", obj_temp);
                            String tExpiryDate = generalFunc.getJsonValueStr("tExpiryDate", obj_temp);

                            map.put("tSubscribeDate", tSubscribeDate.equalsIgnoreCase("N/A")?tSubscribeDate:generalFunc.convertNumberWithRTL(generalFunc.getDateFormatedType(tSubscribeDate, Utils.OriginalDateFormate, CommonUtilities.OriginalDateFormate)));
                            map.put("tExpiryDate", tExpiryDate.equalsIgnoreCase("N/A")?tExpiryDate:generalFunc.convertNumberWithRTL(generalFunc.getDateFormatedType(tExpiryDate, Utils.OriginalDateFormate, CommonUtilities.OriginalDateFormate)));
                            map.put("isRenew", generalFunc.getJsonValueStr("isRenew",obj_temp));



                            String lbl="";
                            if (eSubscriptionStatus.equalsIgnoreCase("Subscribed"))
                            {
                                lbl="LBL_SUB_ACTIVE_TXT";
                            }else if (eSubscriptionStatus.equalsIgnoreCase("Expired"))
                            {
                                lbl="LBL_SUB_EXPIRED_TXT";
                            }
                            else if (eSubscriptionStatus.equalsIgnoreCase("Inactive"))
                            {
                                lbl="LBL_SUB_INACTIVE_TXT";
                            } else if (eSubscriptionStatus.equalsIgnoreCase("Cancelled"))
                            {
                                lbl="LBL_SUB_CANCELLED_TXT";
                            }


                            map.put("eSubscriptionStatusLbl", generalFunc.retrieveLangLBl("",lbl));
                            map.put("statusLbl", generalFunc.retrieveLangLBl("", "LBL_Status"));
                            map.put("vPlanDescriptionLbl", generalFunc.retrieveLangLBl("", "LBL_DETAILS"));
                            map.put("subscriptionLbl", generalFunc.retrieveLangLBl("", "LBL_SUBSCRIPTION_TXT"));
                            map.put("planLeftDaysTitle1", generalFunc.retrieveLangLBl("","LBL_DAYS"));
                            map.put("planLeftDaysTitle2", generalFunc.retrieveLangLBl("","LBL_SUB_DAYS_LEFT_TXT"));
                            map.put("subscribedStatusLbl", generalFunc.retrieveLangLBl("","LBL_SUBSCRIPTION_STATUS_TXT"));
                            map.put("planTypeLBL", generalFunc.retrieveLangLBl("","LBL_SUB_PLAN_TYPE_TXT"));
                            map.put("planTypeLBL", generalFunc.retrieveLangLBl("","LBL_SUBSCRIPTION_PLAN_NAME"));
                            map.put("planDurationLBL", generalFunc.retrieveLangLBl("","LBL_SUB_PLAN_DURATION_TXT"));
                            map.put("planPriceLBL", generalFunc.retrieveLangLBl("","LBL_SUB_PLAN_PRICE_TXT"));
                            map.put("subscribedOnLBL",generalFunc.retrieveLangLBl("","LBL_SUB_ON_TXT"));
                            map.put("expiredOnLBL", generalFunc.retrieveLangLBl("","LBL_SUB_EXPIRED_ON_TXT"));
                            map.put("renewLBL", generalFunc.retrieveLangLBl("", "LBL_SUB_RENEW_PLAN_TXT"));

//                            map.put("JSON", obj_temp.toString());

                            list.add(map);
                        }

                        if (!nextPage.equals("") && !nextPage.equals("0")) {
                            next_page_str = nextPage;
                            isNextPageAvailable = true;
                        } else {
                            removeNextPageConfig();
                        }


                        subscriptionAdapter.notifyDataSetChanged();

                        findViewById(R.id.scrollView).setScrollY(0);

                    } else {
                        list.clear();
                        if (list.size() == 0) {
                            removeNextPageConfig();
                            noPlansTxt.setText(generalFunc.retrieveLangLBl("", generalFunc.getJsonValueStr(Utils.message_str, responseStringObj)));
                            noPlansTxt.setVisibility(View.VISIBLE);
                            subscriptionAdapter.notifyDataSetChanged();
                        }
                    }

                } else if (isLoadMore == false) {
                    list.clear();
                    if (list.size() == 0) {
                        removeNextPageConfig();
                        noPlansTxt.setText(generalFunc.retrieveLangLBl("", generalFunc.getJsonValueStr(Utils.message_str, responseStringObj)));
                        noPlansTxt.setVisibility(View.VISIBLE);
                        subscriptionAdapter.notifyDataSetChanged();
                    }
                }
            } else {
                generateErrorView();
            }
            closeLoader();
            mIsLoading = false;
        });
        exeWebServer.execute();

    }

    public void removeNextPageConfig() {
        next_page_str = "";
        isNextPageAvailable = false;
        mIsLoading = false;
        subscriptionAdapter.removeFooterView();
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
        errorView.setOnRetryListener(() -> getDriverSubscriptionHistory(false));
    }

    private void setLables() {
        subscriptionTypeTitleTxt.setText(generalFunc.retrieveLangLBl("", "LBL_SUBSCRIPTION_HISTORY_TXT"));
        subscriptionDesTxt.setText(generalFunc.retrieveLangLBl("", "LBL_SUBSCRIPTION_HISTORY_DESC_TXT"));
        titleTxt.setText(generalFunc.retrieveLangLBl("", "LBL_SUBSCRIPTION_HISTORY_TITLE_TXT"));
    }

    private void initView() {
        titleTxt = findViewById(R.id.titleTxt);
        backImgView = findViewById(R.id.backImgView);

        subscriptionRecyclerView = findViewById(R.id.subscriptionRecyclerView);
        noDataTxt = findViewById(R.id.noDataTxt);
        subscriptionTypeTitleTxt = findViewById(R.id.subscriptionTypeTitleTxt);
        subscriptionDesTxt = findViewById(R.id.subscriptionDesTxt);

        loading = (ProgressBar) findViewById(R.id.loading);
        errorView = (ErrorView) findViewById(R.id.errorView);
        contentArea = (LinearLayout) findViewById(R.id.contentArea);
        noPlansTxt = findViewById(R.id.noPlansTxt);


        backImgView.setOnClickListener(new setOnClickList());

    }

    public class setOnClickList implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            Utils.hideKeyboard(SubscriptionHistoryActivity.this);
            int i = view.getId();
            if (i == R.id.backImgView) {
                SubscriptionHistoryActivity.super.onBackPressed();
            }
        }
    }


    public Context getActContext() {
        return SubscriptionHistoryActivity.this; // Must be context of activity not application
    }

}
