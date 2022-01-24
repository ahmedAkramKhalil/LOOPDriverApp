package com.taxifgo.driver.deliverAll;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.format.Time;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.adapter.files.deliverAll.OrderHistoryRecycleAdapter;
import com.taxifgo.driver.R;
import com.datepicker.files.SlideDateTimeListener;
import com.datepicker.files.SlideDateTimePicker;
import com.general.files.ExecuteWebServerUrl;
import com.general.files.GeneralFunctions;
import com.general.files.MyApp;
import com.general.files.StartActProcess;
import com.utils.CommonUtilities;
import com.utils.Logger;
import com.utils.Utils;
import com.view.ErrorView;
import com.view.MTextView;
import com.view.editBox.MaterialEditText;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class OrderHistoryActivity extends AppCompatActivity implements OrderHistoryRecycleAdapter.OnItemClickListener {

    public GeneralFunctions generalFunc;
    public String userProfileJson;
    public MTextView earningFareHTxt;
    public MTextView earningFareVTxt;
    public MTextView totalOrderHTxt;
    public MTextView totalOrderVTxt;
    MTextView titleTxt;
    ImageView backImgView;

    MaterialEditText fromDateEditBox;
    MaterialEditText toDateEditBox;

    ProgressBar loading_history;

    boolean mIsLoading = false;
    boolean isNextPageAvailable = false;

    MTextView noOrdersTxt;

    RecyclerView historyRecyclerView;
    ErrorView errorView;

    String next_page_str = "";

    OrderHistoryRecycleAdapter orderHistoryRecycleAdapter;

    ArrayList<HashMap<String, String>> listData = new ArrayList<>();

    String fromSelectedTime = "";
    String toSelectedTime = "";
    String previousHeaderDate = "";
    Date fromDateDay = null;
    Date toDateDay = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_history);

        generalFunc = MyApp.getInstance().getGeneralFun(getActContext());

        userProfileJson = generalFunc.retrieveValue(Utils.USER_PROFILE_JSON);

        titleTxt = (MTextView) findViewById(R.id.titleTxt);
        backImgView = (ImageView) findViewById(R.id.backImgView);
        fromDateEditBox = (MaterialEditText) findViewById(R.id.fromDateEditBox);
        toDateEditBox = (MaterialEditText) findViewById(R.id.toDateEditBox);
        loading_history = (ProgressBar) findViewById(R.id.loading_history);
        historyRecyclerView = (RecyclerView) findViewById(R.id.historyRecyclerView);
        noOrdersTxt = (MTextView) findViewById(R.id.noOrdersTxt);
        errorView = (ErrorView) findViewById(R.id.errorView);

        earningFareHTxt = (MTextView) findViewById(R.id.earningFareHTxt);
        earningFareVTxt = (MTextView) findViewById(R.id.earningFareVTxt);
        totalOrderHTxt = (MTextView) findViewById(R.id.totalOrderHTxt);
        totalOrderVTxt = (MTextView) findViewById(R.id.totalOrderVTxt);

        orderHistoryRecycleAdapter = new OrderHistoryRecycleAdapter(getActContext(), listData, generalFunc, true);
        historyRecyclerView.setAdapter(orderHistoryRecycleAdapter);
        orderHistoryRecycleAdapter.setOnItemClickListener(this);

        backImgView.setOnClickListener(new setOnClickList());

        fromDateEditBox.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.mipmap.ic_calendar_history, 0);
        toDateEditBox.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.mipmap.ic_calendar_history, 0);
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
                    orderHistoryRecycleAdapter.addFooterView();

                    getPastOrders(true, fromSelectedTime, toSelectedTime);

                } else if (isNextPageAvailable == false) {
                    orderHistoryRecycleAdapter.removeFooterView();
                }
            }
        });

        long fromDateMillis = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000);
        Time fromDate = new Time();
        fromDate.set(fromDateMillis);

        fromDateDay = Utils.convertStringToDate("ddMMyyyy", getDateFromMilliSec(fromDateMillis, "ddMMyyyy"));

        fromSelectedTime = getDateFromMilliSec(fromDateMillis, "yyyy-MM-dd", Locale.US);
        fromDateEditBox.setText(getDateFromMilliSec(fromDateMillis, "dd MMM yyyy"));

        long toDateMillis = System.currentTimeMillis();
        Time toDate = new Time();
        toDate.set(toDateMillis);

        toDateDay = Utils.convertStringToDate("ddMMyyyy", getDateFromMilliSec(toDateMillis, "ddMMyyyy"));
        toSelectedTime = getDateFromMilliSec(toDateMillis, "yyyy-MM-dd", Locale.US);
        toDateEditBox.setText(getDateFromMilliSec(toDateMillis, "dd MMM yyyy"));

        fromDateEditBox.getLabelFocusAnimator().start();
        toDateEditBox.getLabelFocusAnimator().start();

        removeInput();
        setLabels();


        earningFareVTxt.setText("--");
        totalOrderVTxt.setText("--");

        getPastOrders(false, fromSelectedTime, toSelectedTime);
    }

    public void setLabels() {
//        titleTxt.setText(generalFunc.retrieveLangLBl("", "LBL_EARNING_HISTORY"));
        titleTxt.setText(generalFunc.retrieveLangLBl("", "LBL_ORDERS"));
        fromDateEditBox.setBothText(generalFunc.retrieveLangLBl("", "LBL_From"));
        toDateEditBox.setBothText(generalFunc.retrieveLangLBl("", "LBL_TO"));

        earningFareHTxt.setText(generalFunc.retrieveLangLBl("", "LBL_TOTAL_EARNINGS"));
        totalOrderHTxt.setText(generalFunc.retrieveLangLBl("", "LBL_TOTAL_ORDERS"));
    }

    public String getDateFromMilliSec(long dateInMillis, String dateFormat) {
        String convertdate = "";
        Locale locale = new Locale(generalFunc.retrieveValue(Utils.LANGUAGE_CODE_KEY));
        SimpleDateFormat original_formate = new SimpleDateFormat(dateFormat);
        String dateString = original_formate.format(new Date(dateInMillis));
        SimpleDateFormat date_format = new SimpleDateFormat(dateFormat, locale);

        try {
            Date datedata = original_formate.parse(dateString);
            convertdate = date_format.format(datedata);
        } catch (ParseException e) {
            e.printStackTrace();
            Logger.d("getDateFormatedType", "::" + e.toString());
        }


        return convertdate;
    }

    public String getDateFromMilliSec(long dateInMillis, String dateFormat, Locale locale) {
        SimpleDateFormat formatter = new SimpleDateFormat(dateFormat, locale);

        String dateString = formatter.format(new Date(dateInMillis));

        return dateString;
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    public void removeInput() {
        Utils.removeInput(fromDateEditBox);
        Utils.removeInput(toDateEditBox);

        fromDateEditBox.setOnTouchListener(new setOnTouchList());
        toDateEditBox.setOnTouchListener(new setOnTouchList());

        fromDateEditBox.setOnClickListener(new setOnClickList());
        toDateEditBox.setOnClickListener(new setOnClickList());
    }

    public Context getActContext() {
        return OrderHistoryActivity.this;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {

        }
    }

    public void openFromDateSelection() {
        new SlideDateTimePicker.Builder(getSupportFragmentManager())
                .setListener(new SlideDateTimeListener() {
                    @Override
                    public void onDateTimeSet(Date date) {

                        if (Utils.convertStringToDate("ddMMyyyy", getDateFromMilliSec(date.getTime(), "ddMMyyyy")).equals(toDateDay) || Utils.convertStringToDate("ddMMyyyy", getDateFromMilliSec(date.getTime(), "ddMMyyyy")).before(toDateDay)) {

                            fromDateDay = Utils.convertStringToDate("ddMMyyyy", getDateFromMilliSec(date.getTime(), "ddMMyyyy"));

                            String selectedDateTime = Utils.convertDateToFormat("yyyy-MM-dd HH:mm:ss", date);
                            String selectedDateTimeZone = Calendar.getInstance().getTimeZone().getID();

                            fromSelectedTime = Utils.convertDateToFormat("yyyy-MM-dd", date);

                            fromDateEditBox.setText(getDateFromMilliSec(date.getTime(), "dd MMM yyyy"));

                            getPastOrders(false, fromSelectedTime, toSelectedTime);
                        } else {
                            generalFunc.showGeneralMessage("", generalFunc.retrieveLangLBl("", "LBL_FROM_DATE_RESTRICT"));
                        }
                    }

                    @Override
                    public void onDateTimeCancel() {

                    }
                })
                .setTimePickerEnabled(false)
                .setInitialDate(new Date())
                .setMaxDate(new Date())
                .setIs24HourTime(false)
                .setIndicatorColor(getResources().getColor(R.color.appThemeColor_2))
                .build()
                .show();
    }

    public void openToDateSelection() {
        new SlideDateTimePicker.Builder(getSupportFragmentManager())
                .setListener(new SlideDateTimeListener() {
                    @Override
                    public void onDateTimeSet(Date date) {

                        if (Utils.convertStringToDate("ddMMyyyy", getDateFromMilliSec(date.getTime(), "ddMMyyyy")).equals(fromDateDay) || Utils.convertStringToDate("ddMMyyyy", getDateFromMilliSec(date.getTime(), "ddMMyyyy")).after(fromDateDay)) {

                            toDateDay = Utils.convertStringToDate("ddMMyyyy", getDateFromMilliSec(date.getTime(), "ddMMyyyy"));

                            String selectedDateTime = Utils.convertDateToFormat("yyyy-MM-dd HH:mm:ss", date);
                            String selectedDateTimeZone = Calendar.getInstance().getTimeZone().getID();

                            toSelectedTime = Utils.convertDateToFormat("yyyy-MM-dd", date);

                            toDateEditBox.setText(getDateFromMilliSec(date.getTime(), "dd MMM yyyy"));

                            getPastOrders(false, fromSelectedTime, toSelectedTime);
                        } else {
                            generalFunc.showGeneralMessage("", generalFunc.retrieveLangLBl("", "LBL_TO_DATE_RESTRICT"));
                        }

                    }

                    @Override
                    public void onDateTimeCancel() {

                    }

                })
                .setTimePickerEnabled(false)
                .setInitialDate(new Date())
                .setMaxDate(new Date())
                .setIs24HourTime(false)
                .setIndicatorColor(getResources().getColor(R.color.appThemeColor_2))
                .build()
                .show();
    }


    @Override
    public void onItemClickList(View view, int position) {

//        Bundle bn = new Bundle();
//        bn.putSerializable("OrderData", listData.get(position));
//        new StartActProcess(getActContext()).startActWithData(OrderHistoryActivity.class, bn);

        Bundle bn = new Bundle();
        bn.putSerializable("iOrderId", listData.get(position).get("iOrderId"));
        new StartActProcess(getActContext()).startActWithData(OrderDetailsActivity.class, bn);
    }


    public void getPastOrders(boolean isLoadMore, String fromSelectedTime, String toSelectedTime) {

        if (isLoadMore == false) {
            listData.clear();
            orderHistoryRecycleAdapter.notifyDataSetChanged();
            isNextPageAvailable = false;
            mIsLoading = true;
        }

        if (errorView.getVisibility() == View.VISIBLE) {
            errorView.setVisibility(View.GONE);
        }
        if (loading_history.getVisibility() != View.VISIBLE && isLoadMore == false) {
            loading_history.setVisibility(View.VISIBLE);
        }

        noOrdersTxt.setVisibility(View.GONE);

        final HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "getOrderHistory");
        parameters.put("iGeneralUserId", generalFunc.getMemberId());
        parameters.put("UserType", Utils.app_type);
        parameters.put("vFromDate", fromSelectedTime);
        parameters.put("vToDate", toSelectedTime);
        if (isLoadMore == true) {
            parameters.put("page", next_page_str);
        }
        parameters.put("eSystem", Utils.eSystem_Type);

        final ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(), parameters);
        exeWebServer.setDataResponseListener(responseString -> {
            noOrdersTxt.setVisibility(View.GONE);
            JSONObject responseStringObject=generalFunc.getJsonObject(responseString);

            if (responseStringObject != null && !responseStringObject.equals("")) {

                closeLoader();

                if (generalFunc.checkDataAvail(Utils.action_str, responseStringObject) == true) {
                    String nextPage = generalFunc.getJsonValueStr("NextPage", responseStringObject);
                    String TotalOrder = generalFunc.convertNumberWithRTL(generalFunc.getJsonValueStr("TotalOrder", responseStringObject));
                    String TotalEarning = generalFunc.convertNumberWithRTL(generalFunc.getJsonValueStr("TotalEarning", responseStringObject));

                    earningFareVTxt.setText(TotalEarning.equals("") ? "--" : TotalEarning);
                    totalOrderVTxt.setText(TotalOrder.equals("") ? "--" : TotalOrder);

                    JSONArray arr_orders = generalFunc.getJsonArray(Utils.message_str, responseStringObject);

                    Logger.d("getOrderHistory", "response::" + responseStringObject);


                    if (arr_orders != null) {
                        for (int i = 0; i < arr_orders.length(); i++) {
                            JSONObject obj_temp = generalFunc.getJsonObject(arr_orders, i);

                            /*String vDate = generalFunc.getJsonValueStr("vDate", obj_temp);

                            if (!previousHeaderDate.equalsIgnoreCase(vDate)) {

                                HashMap<String, String> mapHeader = new HashMap<String, String>();
                                mapHeader.put("vDate", generalFunc.convertNumberWithRTL(vDate));
                                mapHeader.put("TYPE", "" + OrderHistoryRecycleAdapter.TYPE_HEADER);

                                listData.add(mapHeader);
                                previousHeaderDate = vDate;
                            }*/

                            JSONArray arr_date = generalFunc.getJsonArray(Utils.data_str, obj_temp);
                            if (arr_date!=null && arr_date.length()>0) {
                                for (int j = 0; j < arr_date.length(); j++) {
                                    HashMap<String, String> map = new HashMap<String, String>();

                                    JSONObject obj_date_temp = generalFunc.getJsonObject(arr_date, j);

                                    map.put("iOrderId", generalFunc.getJsonValueStr("iOrderId", obj_date_temp));
                                    map.put("vOrderNo", generalFunc.convertNumberWithRTL(generalFunc.getJsonValueStr("vOrderNo", obj_date_temp)));
                                    map.put("vServiceCategoryName", generalFunc.convertNumberWithRTL(generalFunc.getJsonValueStr("vServiceCategoryName", obj_date_temp)));
                                    String tOrderRequestDate=generalFunc.getJsonValueStr("tOrderRequestDate", obj_date_temp);


                                    map.put("ConvertedOrderRequestDate", generalFunc.convertNumberWithRTL(generalFunc.getDateFormatedType(tOrderRequestDate, Utils.OriginalDateFormate,CommonUtilities.OriginalDateFormate)));
                                    map.put("ConvertedOrderRequestTime", generalFunc.convertNumberWithRTL(generalFunc.getDateFormatedType(tOrderRequestDate,   Utils.OriginalDateFormate,CommonUtilities.OriginalTimeFormate)));


                                    map.put("UseName", generalFunc.getJsonValueStr("UseName", obj_date_temp));
                                    map.put("TotalItems", generalFunc.convertNumberWithRTL(generalFunc.getJsonValueStr("TotalItems", obj_date_temp)));
                                    map.put("iStatus", generalFunc.getJsonValueStr("iStatus", obj_date_temp));
                                    map.put("fTotalGenerateFare", generalFunc.convertNumberWithRTL((generalFunc.getJsonValueStr("fTotalGenerateFare", obj_date_temp))));
                                    map.put("EarningFare", generalFunc.convertNumberWithRTL(generalFunc.getJsonValueStr("EarningFare", obj_date_temp)));
                                    map.put("LBL_AMT_GENERATE_PENDING", generalFunc.retrieveLangLBl("", "LBL_AMT_GENERATE_PENDING"));
                                    map.put("iStatusCode", generalFunc.getJsonValueStr("iStatusCode", obj_date_temp));
                                    map.put("TYPE", "" + OrderHistoryRecycleAdapter.TYPE_ITEM);
                                    int item = generalFunc.parseIntegerValue(0, generalFunc.getJsonValueStr("TotalItems", obj_date_temp));
                                    map.put("LBL_ITEM", item <= 1 ? generalFunc.retrieveLangLBl("", "LBL_ITEM") : generalFunc.retrieveLangLBl("", "LBL_ITEMS"));

                                    listData.add(map);
                                }
                            }

                        }
                    }
                    if (!nextPage.equals("") && !nextPage.equals("0")) {
                        next_page_str = nextPage;
                        isNextPageAvailable = true;
                    } else {
                        removeNextPageConfig();
                    }

                    orderHistoryRecycleAdapter.notifyDataSetChanged();

                } else {
                    if (listData.size() == 0) {
                        removeNextPageConfig();
                        noOrdersTxt.setText(generalFunc.retrieveLangLBl("", generalFunc.getJsonValueStr(Utils.message_str, responseStringObject)));
                        noOrdersTxt.setVisibility(View.VISIBLE);
                        earningFareVTxt.setText("--");
                        totalOrderVTxt.setText("--");
                    }
                }
            } else {
                if (isLoadMore == false) {
                    removeNextPageConfig();
                    generateErrorView();
                    earningFareVTxt.setText("--");
                    totalOrderVTxt.setText("--");
                }

            }
            mIsLoading = false;

        });
        exeWebServer.execute();
    }

    public void removeNextPageConfig() {
        next_page_str = "";
        isNextPageAvailable = false;
        mIsLoading = false;
        orderHistoryRecycleAdapter.removeFooterView();
    }

    public void closeLoader() {
        if (loading_history.getVisibility() == View.VISIBLE) {
            loading_history.setVisibility(View.GONE);
        }
    }

    public void generateErrorView() {

        closeLoader();

        generalFunc.generateErrorView(errorView, "LBL_ERROR_TXT", "LBL_NO_INTERNET_TXT");

        if (errorView.getVisibility() != View.VISIBLE) {
            errorView.setVisibility(View.VISIBLE);
        }
        errorView.setOnRetryListener(() -> getPastOrders(false, fromSelectedTime, toSelectedTime));
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

    public class setOnClickList implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            Utils.hideKeyboard(OrderHistoryActivity.this);
            switch (view.getId()) {
                case R.id.backImgView:
                    OrderHistoryActivity.super.onBackPressed();
                    break;
                case R.id.fromDateEditBox:
                    openFromDateSelection();
                    break;
                case R.id.toDateEditBox:
                    openToDateSelection();
                    break;

            }
        }
    }

}
