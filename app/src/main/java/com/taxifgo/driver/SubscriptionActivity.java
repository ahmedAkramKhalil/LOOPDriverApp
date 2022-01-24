package com.taxifgo.driver;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.adapter.files.SubscriptionAdapter;
import com.general.files.ExecuteWebServerUrl;
import com.general.files.GeneralFunctions;
import com.general.files.MyApp;
import com.general.files.StartActProcess;
import com.utils.CommonUtilities;
import com.utils.Logger;
import com.utils.Utils;
import com.view.ErrorView;
import com.view.GenerateAlertBox;
import com.view.MTextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;


public class SubscriptionActivity extends AppCompatActivity implements SubscriptionAdapter.OnItemClickListener {

    SubscriptionAdapter subscriptionAdapter;
    ArrayList<HashMap<String, String>> list = new ArrayList<>();

    String next_page_str = "";
    boolean isNextPageAvailable = false;
    boolean mIsLoading = false;

    public GeneralFunctions generalFunc;
    public String userProfileJson;

    MTextView noDataTxt;
    MTextView subscriptionTypeTitleTxt;
    MTextView memberShipTitleTxt;
    MTextView subscriptionDesTxt;
    MTextView noPlansTxt;
    MTextView titleTxt;
    ImageView backImgView, rightImgView;
    RecyclerView subscriptionRecyclerView;

    LinearLayout contentArea;
    ProgressBar loading;
    ErrorView errorView;
    int TRANSACTION_COMPLETED=12345;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subscription);
        generalFunc = MyApp.getInstance().getGeneralFun(getActContext());
        userProfileJson = generalFunc.retrieveValue(Utils.USER_PROFILE_JSON);

        initView();
        setLables();

        subscriptionAdapter = new SubscriptionAdapter(getActContext(), list, "", generalFunc, false);
        subscriptionRecyclerView.setAdapter(subscriptionAdapter);
        subscriptionAdapter.setOnItemClickListener(this);

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
                    getSubscriptionPlans(true);
                    subscriptionAdapter.addFooterView();

                } else if (isNextPageAvailable == false) {
                    subscriptionAdapter.removeFooterView();
                }
            }
        });



    }

    @Override
    protected void onResume() {
        super.onResume();
        getSubscriptionPlans(false);
    }

    private void getSubscriptionPlans(boolean isLoadMore) {
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
        parameters.put("type", "getSubscriptionPlans");
        parameters.put("iDriverId", generalFunc.getMemberId());
        parameters.put("UserType", Utils.app_type);
        if (isLoadMore) {
            parameters.put("page", next_page_str);
        }


        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(), parameters);
        exeWebServer.setDataResponseListener(responseString -> {

            JSONObject responseStringObj = generalFunc.getJsonObject(responseString);

            if (responseString != null && !responseString.equals("")) {

                if (generalFunc.checkDataAvail(Utils.action_str, responseStringObj)) {
                    String nextPage = generalFunc.getJsonValueStr("NextPage", responseStringObj);
                    contentArea.setVisibility(View.VISIBLE);

                    JSONArray arr_subsucription_plans = generalFunc.getJsonArray(Utils.message_str, responseStringObj);

                    if (arr_subsucription_plans != null && arr_subsucription_plans.length() > 0) {

                        list.clear();

                        for (int i = 0; i < arr_subsucription_plans.length(); i++) {
                            JSONObject obj_temp = generalFunc.getJsonObject(arr_subsucription_plans, i);

                            HashMap<String, String> map = new HashMap<String, String>();
                            map.put("iDriverSubscriptionPlanId", generalFunc.getJsonValueStr("iDriverSubscriptionPlanId",obj_temp));
                            map.put("iDriverSubscriptionDetailsId", generalFunc.getJsonValueStr("iDriverSubscriptionDetailsId",obj_temp));
                            map.put("PlanTypeTitle", generalFunc.getJsonValueStr("PlanTypeTitle",obj_temp));

                            String planType=generalFunc.getJsonValueStr("PlanType",obj_temp);
                            map.put("PlanType", planType);
                            map.put("vPlanDuration", generalFunc.convertNumberWithRTL(generalFunc.getJsonValueStr("vPlanDuration",obj_temp)));

                            map.put("vPlanName", generalFunc.getJsonValueStr("vPlanName",obj_temp));
                            map.put("vPlanPeriod", generalFunc.convertNumberWithRTL(generalFunc.getJsonValueStr("vPlanPeriod",obj_temp)));
                            map.put("vPlanDescription", generalFunc.getJsonValueStr("vPlanDescription",obj_temp));
                            map.put("isRenew", generalFunc.getJsonValueStr("isRenew",obj_temp));
                            String planLeftDays=generalFunc.getJsonValueStr("planLeftDays",obj_temp);

                            map.put("planLeftDays", planLeftDays);
                            map.put("FormattedPlanLeftDays", generalFunc.convertNumberWithRTL(planLeftDays));

                            map.put("PlanDuration", generalFunc.retrieveLangLBl("","LBL_SUB_DURATION_TXT")+" "+generalFunc.convertNumberWithRTL(generalFunc.getJsonValueStr("PlanDuration", obj_temp)));

                            map.put("fPlanPrice", generalFunc.convertNumberWithRTL(generalFunc.getJsonValueStr("fPlanPrice",obj_temp)));
                            String eSubscriptionStatus=generalFunc.getJsonValueStr("eSubscriptionStatus",obj_temp);
                            map.put("eSubscriptionStatus", eSubscriptionStatus);

                            String listLbl="LBL_SUBSCRIBE";
                            String detailLbl="";
                            String showPlanDetails="No";

                            if (eSubscriptionStatus.equalsIgnoreCase("Subscribed"))
                            {
                                listLbl="LBL_CANCEL_SUBSCRIPTION_TXT";
                                detailLbl="LBL_SUBSCRIBED_TXT";
                                showPlanDetails="Yes";

                            }else if (eSubscriptionStatus.equalsIgnoreCase("UnSubscribed"))
                            {
                                detailLbl="LBL_NOT_SUBSCRIBED_TXT";

                            }else if (eSubscriptionStatus.equalsIgnoreCase("Expired"))
                            {
                                detailLbl="LBL_SUB_EXPIRED_TXT";
                            }
                            else if (eSubscriptionStatus.equalsIgnoreCase("Cancelled"))
                            {
                                detailLbl="LBL_SUB_CANCELLED_TXT";
                            }
                            map.put("eSubscriptionStatusLbl", generalFunc.retrieveLangLBl("",listLbl));
                            Log.d("eSubscriptionStatus","listLbl"+listLbl);
                            Log.d("eSubscriptionStatus","detailLbl"+detailLbl);

                            map.put("eSubscriptionDetailStatusLbl", generalFunc.retrieveLangLBl("",detailLbl));
                            map.put("showPlanDetails", showPlanDetails);

                            String tSubscribeDate=generalFunc.getJsonValueStr("tSubscribeDate",obj_temp);
                            String tExpiryDate=generalFunc.getJsonValueStr("tExpiryDate",obj_temp);
                            map.put("tSubscribeDate", tSubscribeDate.equalsIgnoreCase("N/A")?tSubscribeDate:generalFunc.convertNumberWithRTL(generalFunc.getDateFormatedType(tSubscribeDate, Utils.OriginalDateFormate, CommonUtilities.OriginalDateFormate)));
                            map.put("tExpiryDate", tExpiryDate.equalsIgnoreCase("N/A")?tExpiryDate:generalFunc.convertNumberWithRTL(generalFunc.convertNumberWithRTL(generalFunc.getDateFormatedType(tExpiryDate, Utils.OriginalDateFormate, CommonUtilities.OriginalDateFormate))));

                            map.put("subscribedOnLBL",generalFunc.retrieveLangLBl("","LBL_SUB_ON_TXT"));
                            map.put("expiredOnLBL", generalFunc.retrieveLangLBl("","LBL_SUB_EXPIRED_ON_TXT"));

                            map.put("statusLbl", generalFunc.retrieveLangLBl("", "LBL_Status"));
                            map.put("vPlanDescriptionLbl", generalFunc.retrieveLangLBl("", "LBL_DETAILS"));
                            map.put("subscriptionLbl", generalFunc.retrieveLangLBl("", "LBL_SUBSCRIPTION_TXT"));
                            map.put("renewLBL", generalFunc.retrieveLangLBl("", "LBL_SUB_RENEW_PLAN_TXT"));

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

                }else {
                    list.clear();
                    if (list.size() == 0) {
                        removeNextPageConfig();
                        noPlansTxt.setText(generalFunc.retrieveLangLBl("", generalFunc.getJsonValueStr(Utils.message_str, responseStringObj)));
                        noPlansTxt.setVisibility(View.VISIBLE);
                        subscriptionAdapter.notifyDataSetChanged();
                    }
                }

        } else{
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
        errorView.setOnRetryListener(() -> getSubscriptionPlans(false));
    }

    private void setLables() {
        subscriptionTypeTitleTxt.setText(generalFunc.retrieveLangLBl("", "LBL_SUBSCRIBE_WITH_US"));
        titleTxt.setText(generalFunc.retrieveLangLBl("", "LBL_SUBSCRIPTION_PLANS"));
        memberShipTitleTxt.setText(generalFunc.retrieveLangLBl("", "LBL_CHOOSE_MEMBERSHIP"));
        subscriptionDesTxt.setText(generalFunc.retrieveLangLBl("", "LBL_SUBSCRIPTION_GUIDE_TXT"));
        noPlansTxt.setText(generalFunc.retrieveLangLBl("", "LBL_SUBSCRIPTION_GUIDE_TXT"));
    }


    private void initView() {
        subscriptionRecyclerView = findViewById(R.id.subscriptionRecyclerView);
        noDataTxt = findViewById(R.id.noDataTxt);

        titleTxt = findViewById(R.id.titleTxt);
        subscriptionTypeTitleTxt = findViewById(R.id.subscriptionTypeTitleTxt);
        memberShipTitleTxt = findViewById(R.id.memberShipTitleTxt);
        subscriptionDesTxt = findViewById(R.id.subscriptionDesTxt);
        memberShipTitleTxt.setVisibility(View.VISIBLE);

        backImgView = findViewById(R.id.backImgView);
        rightImgView = findViewById(R.id.rightImgView);

        loading = (ProgressBar) findViewById(R.id.loading);
        errorView = (ErrorView) findViewById(R.id.errorView);
        contentArea = (LinearLayout) findViewById(R.id.contentArea);
        noPlansTxt = findViewById(R.id.noPlansTxt);

        rightImgView.setImageResource(R.mipmap.ic_waybill);
        rightImgView.setColorFilter(getActContext().getResources().getColor(R.color.white));
        rightImgView.setVisibility(View.VISIBLE);


        backImgView.setOnClickListener(new setOnClickList());
        rightImgView.setOnClickListener(new setOnClickList());

    }

public class setOnClickList implements View.OnClickListener {

    @Override
    public void onClick(View view) {
        Utils.hideKeyboard(SubscriptionActivity.this);
        int i = view.getId();
        if (i == R.id.backImgView) {
            SubscriptionActivity.super.onBackPressed();
        }
        if (i == R.id.rightImgView) {
            setBounceAnimation(rightImgView, () -> {
                new StartActProcess(getActContext()).startAct(SubscriptionHistoryActivity.class);
            });

        }
    }
}

    public Context getActContext() {
        return SubscriptionActivity.this; // Must be context of activity not application
    }

    @Override
    public void onSubscribeItemClick(View v, int position,String planType) {

        HashMap<String, String> item = list.get(position);
        if (item.get("eSubscriptionStatus").equalsIgnoreCase("Subscribed"))
        {
            if (planType.equalsIgnoreCase("Cancel")) {
                buildMsgOnCancelSubscription(position);
            }
            else if (planType.equalsIgnoreCase("Renew"))
            {
                Bundle bn=new Bundle();
                bn.putSerializable("PlanDetails",item);
                bn.putSerializable("isRenew","Yes");
                new StartActProcess(getActContext()).startActForResult(SubscriptionPaymentActivity.class,bn,TRANSACTION_COMPLETED);
            }
        }
        else if (!item.get("eSubscriptionStatus").equalsIgnoreCase("Subscribed"))
        {
            Bundle bn=new Bundle();
            bn.putSerializable("PlanDetails",item);
            new StartActProcess(getActContext()).startActForResult(SubscriptionPaymentActivity.class,bn,TRANSACTION_COMPLETED);
        }

    }

    public void buildMsgOnCancelSubscription(int pos) {
        final GenerateAlertBox generateAlert = new GenerateAlertBox(getActContext());
        generateAlert.setCancelable(false);
        generateAlert.setBtnClickList(btn_id -> {
            if (btn_id == 0) {
                generateAlert.closeAlertBox();
            } else {
                cancelSubscription(pos);
                generateAlert.closeAlertBox();
            }

        });
        generateAlert.setContentMessage("", generalFunc.retrieveLangLBl("", "LBL_CANCEL_SUBSCRIPTION_NOTE_TXT"));
        generateAlert.setPositiveBtn(generalFunc.retrieveLangLBl("", "LBL_YES"));
        generateAlert.setNegativeBtn(generalFunc.retrieveLangLBl("", "LBL_NO"));

        generateAlert.showAlertBox();
    }

    private void cancelSubscription(int pos) {
        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "CancelSubscription");
        parameters.put("iDriverId", generalFunc.getMemberId());
//        parameters.put("iDriverSubscriptionDetailsId",list.get(pos).get("iDriverSubscriptionDetailsId"));
        parameters.put("iDriverSubscriptionPlanId",list.get(pos).get("iDriverSubscriptionPlanId"));
        parameters.put("UserType", Utils.app_type);

        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(), parameters);
        exeWebServer.setLoaderConfig(getActContext(),true,generalFunc);
        exeWebServer.setDataResponseListener(responseString -> {
            JSONObject responseStringObject=generalFunc.getJsonObject(responseString);

            if (responseStringObject != null && !responseStringObject.equals("")) {

                if (generalFunc.checkDataAvail(Utils.action_str, responseStringObject)) {
                    generalFunc.showGeneralMessage("",generalFunc.retrieveLangLBl("", generalFunc.getJsonValueStr(Utils.message_str, responseStringObject)));
                    getSubscriptionPlans(false);
                }else {
                    generalFunc.showGeneralMessage("",generalFunc.retrieveLangLBl("", generalFunc.getJsonValueStr(Utils.message_str, responseStringObject)));
                }

            } else{
                generateErrorView();
            }
            closeLoader();
            mIsLoading = false;
        });
        exeWebServer.execute();
    }



    private void setBounceAnimation(View view, BounceAnimListener bounceAnimListener) {
        Animation anim = AnimationUtils.loadAnimation(getActContext(), R.anim.bounce_interpolator);
        anim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {

                if (bounceAnimListener != null) {
                    bounceAnimListener.onAnimationFinished();
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        view.startAnimation(anim);
    }

private interface BounceAnimListener {
    void onAnimationFinished();
}
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode==TRANSACTION_COMPLETED && resultCode==RESULT_OK)
        {
            Logger.d("DEBUG", "TRANSACTION_COMPLETED::LIST OF PLANS" );
            getSubscriptionPlans(false);
        }
    }
}
