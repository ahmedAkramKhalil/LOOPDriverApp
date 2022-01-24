package com.fragments;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import com.google.android.material.appbar.AppBarLayout;
import androidx.fragment.app.Fragment;
import androidx.core.widget.NestedScrollView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.format.Time;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.adapter.files.deliverAll.OrderHistoryRecycleAdapter;
import com.taxifgo.driver.BookingsActivity;
import com.taxifgo.driver.MainActivity;
import com.taxifgo.driver.R;
import com.taxifgo.driver.deliverAll.LiveTaskListActivity;
import com.taxifgo.driver.deliverAll.OrderDetailsActivity;
import com.datepicker.files.SlideDateTimeListener;
import com.datepicker.files.SlideDateTimePicker;
import com.general.files.AppFunctions;
import com.general.files.ExecuteWebServerUrl;
import com.general.files.GeneralFunctions;
import com.general.files.MyApp;
import com.general.files.StartActProcess;
import com.utils.CommonUtilities;
import com.utils.Utils;
import com.view.ErrorView;
import com.view.MTextView;
import com.view.calendarview.CustomCalendarView;
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


/**
 * A simple {@link Fragment} subclass.
 */
public class OrderFragment extends Fragment implements OrderHistoryRecycleAdapter.OnItemClickListener, SwipeRefreshLayout.OnRefreshListener ,CustomCalendarView.CalendarEventListener{


    public GeneralFunctions generalFunc;
    public String userProfileJson;
    public MTextView earningFareHTxt;
    public MTextView earningFareVTxt;
    public MTextView totalOrderHTxt;
    public MTextView totalOrderVTxt;
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
    String SELECTED_DATE = "";
    String previousHeaderDate = "";
    Date fromDateDay = null;
    Date toDateDay = null;
    private View view;
    private BookingsActivity myOrderAct;

    CustomCalendarView calendar_view;
    private LinearLayout calContainerView;

    public MTextView filterTxt;
    ArrayList<HashMap<String, String>> filterlist = new ArrayList<>();
    ArrayList<HashMap<String, String>> subFilterlist = new ArrayList<>();
    private LinearLayout filterArea;

    private SwipeRefreshLayout swipeRefreshLayout;

    NestedScrollView nestedScrollView;

    private OrderFragment orderFragment;
    private MyBookingFragment myBookingFragment = null;
    boolean isFirstInstance=true;

    LinearLayout calenderHeaderLayout;
    LinearLayout detailsArea;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        view = inflater.inflate(R.layout.fragment_order, container, false);

        if (getActivity() instanceof MainActivity) {
            myBookingFragment = ((MainActivity) getActivity()).myBookingFragment;
            orderFragment = myBookingFragment.getOrderFrag();
        } else if (getActivity() instanceof LiveTaskListActivity) {
            myBookingFragment = ((LiveTaskListActivity) getActivity()).myBookingFragment;
            orderFragment = myBookingFragment.getOrderFrag();
        } else {
            myBookingFragment = null;
            myOrderAct = (BookingsActivity) getActivity();
            orderFragment = myOrderAct.getOrderFrag();
        }

        generalFunc = MyApp.getInstance().getGeneralFun(getActivity());

        userProfileJson = generalFunc.retrieveValue(Utils.USER_PROFILE_JSON);

        filterArea = (LinearLayout) view.findViewById(R.id.filterArea);
        filterArea.setOnClickListener(new setOnClickList());

        detailsArea=(LinearLayout)view.findViewById(R.id.detailsArea);
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(this);

        fromDateEditBox = (MaterialEditText) view.findViewById(R.id.fromDateEditBox);
        toDateEditBox = (MaterialEditText) view.findViewById(R.id.toDateEditBox);
        loading_history = (ProgressBar) view.findViewById(R.id.loading_history);
        historyRecyclerView = (RecyclerView) view.findViewById(R.id.historyRecyclerView);
        noOrdersTxt = (MTextView) view.findViewById(R.id.noOrdersTxt);
        filterTxt = (MTextView) view.findViewById(R.id.filterTxt);
        errorView = (ErrorView) view.findViewById(R.id.errorView);
        calContainerView = (LinearLayout) view.findViewById(R.id.calContainerView);
        calenderHeaderLayout = (LinearLayout) view.findViewById(R.id.calenderHeaderLayout);

        ((MTextView) view.findViewById(R.id.avgRatingTxt)).setText(generalFunc.retrieveLangLBl("Avg. Rating", "LBL_AVG_RATING"));

        earningFareHTxt = (MTextView) view.findViewById(R.id.earningFareHTxt);
        earningFareVTxt = (MTextView) view.findViewById(R.id.earningFareVTxt);
        totalOrderHTxt = (MTextView) view.findViewById(R.id.totalOrderHTxt);
        totalOrderVTxt = (MTextView) view.findViewById(R.id.totalOrderVTxt);

        orderHistoryRecycleAdapter = new OrderHistoryRecycleAdapter(getActContext(), listData, generalFunc, false);
        historyRecyclerView.setAdapter(orderHistoryRecycleAdapter);
        orderHistoryRecycleAdapter.setOnItemClickListener(this);
        historyRecyclerView.setNestedScrollingEnabled(false);

        fromDateEditBox.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.mipmap.ic_calendar_history, 0);
        toDateEditBox.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.mipmap.ic_calendar_history, 0);

        nestedScrollView = view.findViewById(R.id.nestedScrollView);
        nestedScrollView.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener) (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
            if (calendar_view != null) {
                showHideCalender(false);
            }

            if (v.getChildAt(v.getChildCount() - 1) != null) {

                if ((scrollY >= (v.getChildAt(v.getChildCount() - 1).getMeasuredHeight() - v.getMeasuredHeight())) &&
                        scrollY > oldScrollY) {

                    int visibleItemCount = historyRecyclerView.getLayoutManager().getChildCount();
                    int totalItemCount = historyRecyclerView.getLayoutManager().getItemCount();
                    int firstVisibleItemPosition = ((LinearLayoutManager) historyRecyclerView.getLayoutManager()).findFirstVisibleItemPosition();


                    int lastInScreen = firstVisibleItemPosition + visibleItemCount;
                    if ((lastInScreen == totalItemCount) && !(mIsLoading) && isNextPageAvailable) {
                        mIsLoading = true;
                        orderHistoryRecycleAdapter.addFooterView();
                        getPastOrders(true, fromSelectedTime, toSelectedTime);
                    } else if (!isNextPageAvailable) {
                        orderHistoryRecycleAdapter.removeFooterView();
                    }
                }
            }
        });

        addCalenderView();

        showHideCalender(false);
        calendar_view.setTitleTextColor(Color.parseColor("#141414"));

        calendar_view.setLeftImage(generalFunc.isRTLmode() ? R.drawable.ic_right_arrow_circle: R.drawable.ic_left_arrow_circle );
        calendar_view.setRightImage(generalFunc.isRTLmode() ? R.drawable.ic_left_arrow_circle: R.drawable.ic_right_arrow_circle );

        calendar_view.setRightImageTint(getResources().getColor(R.color.appThemeColor_1));
        calendar_view.setLeftImageTint(getResources().getColor(R.color.appThemeColor_1));

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

        return view;
    }

    public Context getActContext() {
        if (myBookingFragment != null) {
            return myBookingFragment.getActContext();
        } else {
            return myOrderAct.getActContext();
        }
    }

    private void addCalenderView() {
        calendar_view = new CustomCalendarView(getActContext(), AppFunctions.getXmlResource(getActContext(), R.layout.ride_history_cal), calenderHeaderLayout);

        calendar_view.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        ((AppBarLayout) view.findViewById(R.id.appBarLay)).addOnOffsetChangedListener((appBarLayout, verticalOffset) -> {
            if (Math.abs(verticalOffset) == appBarLayout.getTotalScrollRange()) {
                // Collapsed
                showHideCalender(false);
            } else if (verticalOffset == 0) {
                // Expanded
            } else {
                // Somewhere in between
            }
        });

        calendar_view.setCalendarEventListener(this);

        calContainerView.addView(calendar_view);
    }

    public void showHideCalender(boolean show) {
        if (show) {
            calendar_view.setArrowImage(R.drawable.ic_caret_up);
            calendar_view.showDateSelectionView();
            calendar_view.setTitleLayoutBGColor(getResources().getColor(R.color.white));
            calendar_view.setWeekLayoutBGColor(getResources().getColor(R.color.white));
        } else {
            calendar_view.hideDateSelectionView();
            calendar_view.setArrowImage(R.drawable.ic_caret_down);
            calendar_view.setTitleLayoutBGColor(getResources().getColor(R.color.appThemeColor_bg_parent_1));
            calendar_view.setWeekLayoutBGColor(getResources().getColor(R.color.appThemeColor_bg_parent_1));
        }
    }

    private void setDate(Date date) {

        Calendar cal = Calendar.getInstance();
        cal.setTime(date);

        SimpleDateFormat date_format = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

        String date_formatted = date_format.format(cal.getTime());
        SELECTED_DATE = date_formatted;
//        Logger.d("DATE_SELECTED", "setDate" + date_formatted);

        showHideCalender(false);
        removeNextPageConfig();

        getPastOrders(false, fromSelectedTime, toSelectedTime);

    }

    public void setLabels() {
        //titleTxt.setText(generalFunc.retrieveLangLBl("", "LBL_ORDERS"));
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
//            Logger.d("getDateFormatedType", "::" + e.toString());
        }


        return convertdate;
    }

    public String getDateFromMilliSec(long dateInMillis, String dateFormat, Locale locale) {
        SimpleDateFormat formatter = new SimpleDateFormat(dateFormat, locale);

        String dateString = formatter.format(new Date(dateInMillis));

        return dateString;
    }


    @Override
    public void onResume() {
        super.onResume();
        if (myBookingFragment != null) {
            if (myBookingFragment.getId() != 0) {
                myBookingFragment.filterImageview.setVisibility(View.GONE);
            } else {
                return;
            }
        } else {
            myOrderAct.filterImageview.setVisibility(View.GONE);
        }


        if (!isFirstInstance) {
            getPastOrders(false, fromSelectedTime, toSelectedTime);
        }else
        {
            isFirstInstance=!isFirstInstance;
        }


    }


    public void removeInput() {
        Utils.removeInput(fromDateEditBox);
        Utils.removeInput(toDateEditBox);

        fromDateEditBox.setOnTouchListener(new setOnTouchList());
        toDateEditBox.setOnTouchListener(new setOnTouchList());

        fromDateEditBox.setOnClickListener(new setOnClickList());
        toDateEditBox.setOnClickListener(new setOnClickList());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {

        }
    }

    public void openFromDateSelection() {
        new SlideDateTimePicker.Builder(getActivity().getSupportFragmentManager())
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
        new SlideDateTimePicker.Builder(getActivity().getSupportFragmentManager())
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
        new StartActProcess(getActivity()).startActWithData(OrderDetailsActivity.class, bn);
    }

    public void getPastOrders(boolean isLoadMore, String fromSelectedTime, String toSelectedTime) {

        if (isLoadMore == false) {
            listData.clear();
            orderHistoryRecycleAdapter.notifyDataSetChanged();
            isNextPageAvailable = false;
            mIsLoading = true;
            detailsArea.setVisibility(View.INVISIBLE);
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
        parameters.put("vFromDate", SELECTED_DATE);
        if (myBookingFragment != null) {
            parameters.put("vSubFilterParam", myBookingFragment.selOrderSubFilterType);
        } else {
            parameters.put("vSubFilterParam", myOrderAct.selOrderSubFilterType);
        }

        if (isLoadMore == true) {
            parameters.put("page", next_page_str);
        }
        parameters.put("eSystem", Utils.eSystem_Type);

  /* if (Utils.checkText(SELECTED_DATE)) {
            parameters.put("dDateOrig", SELECTED_DATE);
        }*/

//        parameters.put("vFromDate", fromSelectedTime);
//        parameters.put("vToDate", toSelectedTime);
        final ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(), parameters);
        exeWebServer.setDataResponseListener(responseString -> {
            noOrdersTxt.setVisibility(View.GONE);

            JSONObject responseStringObject = generalFunc.getJsonObject(responseString);

            swipeRefreshLayout.setRefreshing(false);

            if (responseStringObject != null && !responseStringObject.equals("")) {
                detailsArea.setVisibility(View.VISIBLE);
                closeLoader();

                if (generalFunc.checkDataAvail(Utils.action_str, responseStringObject) == true) {
                    String nextPage = generalFunc.getJsonValueStr("NextPage", responseStringObject);
                    String TotalOrder = generalFunc.convertNumberWithRTL(generalFunc.getJsonValueStr("TotalOrder", responseStringObject));
                    String TotalEarning = generalFunc.convertNumberWithRTL(generalFunc.getJsonValueStr("TotalEarning", responseStringObject));
                    String AvgRating = generalFunc.convertNumberWithRTL(generalFunc.getJsonValueStr("AvgRating", responseStringObject));

                    earningFareVTxt.setText(TotalEarning.equals("") ? "--" : TotalEarning);
                    totalOrderVTxt.setText(TotalOrder.equals("") ? "--" : TotalOrder);


                    ((MTextView) view.findViewById(R.id.avgRatingCalcTxt)).setText("" + GeneralFunctions.parseFloatValue(0, Utils.checkText(AvgRating) ? AvgRating : "0"));

                    JSONArray arr_orders = generalFunc.getJsonArray(Utils.message_str, responseStringObject);

//                    Logger.d("getOrderHistory", "response::" + responseStringObject);


                    if (arr_orders != null) {
                        for (int i = 0; i < arr_orders.length(); i++) {
                            JSONObject obj_temp = generalFunc.getJsonObject(arr_orders, i);

                            String vDate = generalFunc.getJsonValueStr("vDate", obj_temp);
/*
                            if (!previousHeaderDate.equalsIgnoreCase(vDate)) {

                                HashMap<String, String> mapHeader = new HashMap<String, String>();
                                mapHeader.put("vDate", generalFunc.convertNumberWithRTL(vDate));
                                mapHeader.put("TYPE", "" + OrderHistoryRecycleAdapter.TYPE_HEADER);

                                listData.add(mapHeader);
                                previousHeaderDate = vDate;
                            }*/

                            JSONArray arr_date = generalFunc.getJsonArray(Utils.data_str, obj_temp);
                            for (int j = 0; j < arr_date.length(); j++) {
                                HashMap<String, String> map = new HashMap<String, String>();

                                JSONObject obj_date_temp = generalFunc.getJsonObject(arr_date, j);

                                map.put("iOrderId", generalFunc.getJsonValueStr("iOrderId", obj_date_temp));
                                map.put("vCompany", generalFunc.getJsonValueStr("vCompany", obj_date_temp));
                                map.put("vServiceCategoryName", generalFunc.getJsonValueStr("vServiceCategoryName", obj_date_temp));
                                map.put("vOrderNo", generalFunc.convertNumberWithRTL(generalFunc.getJsonValueStr("vOrderNo", obj_date_temp)));
                                String tOrderRequestDate_Org = generalFunc.getJsonValueStr("tOrderRequestDate_Org", obj_date_temp);
                                map.put("tOrderRequestDate_Org", generalFunc.convertNumberWithRTL(tOrderRequestDate_Org));


                                try {
                                    map.put("ConvertedOrderRequestDate", generalFunc.convertNumberWithRTL(generalFunc.getDateFormatedType(tOrderRequestDate_Org, Utils.OriginalDateFormate, CommonUtilities.OriginalDateFormate)));
                                    map.put("ConvertedOrderRequestTime",  generalFunc.convertNumberWithRTL(generalFunc.getDateFormatedType(tOrderRequestDate_Org, Utils.OriginalDateFormate, CommonUtilities.OriginalTimeFormate)));

                                } catch (Exception e) {
                                    e.printStackTrace();
                                    map.put("ConvertedOrderRequestDate", "");
                                    map.put("ConvertedOrderRequestTime", "");
                                }

                                map.put("vAvgRating", "" + generalFunc.parseFloatValue(0, generalFunc.getJsonValueStr("vAvgRating", obj_temp)));
                                map.put("tOrderRequestDate", generalFunc.convertNumberWithRTL(generalFunc.getJsonValueStr("tOrderRequestDate", obj_date_temp)));
                                map.put("UseName", generalFunc.getJsonValueStr("UseName", obj_date_temp));
                                map.put("vUserAddress", generalFunc.getJsonValueStr("vUserAddress", obj_date_temp));

                                map.put("vService_BG_color", generalFunc.getJsonValueStr("vService_BG_color", obj_date_temp));
                                map.put("vService_TEXT_color", generalFunc.getJsonValueStr("vService_TEXT_color", obj_date_temp));

                                map.put("TotalItems", generalFunc.convertNumberWithRTL(generalFunc.getJsonValueStr("TotalItems", obj_date_temp)));
                                map.put("vImage", generalFunc.getJsonValueStr("vImage", obj_date_temp));
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



                        /*JSONArray arr_type_filter = generalFunc.getJsonArray("AppTypeFilterArr", responseStringObject);

                        if (arr_type_filter != null && arr_type_filter.length() > 0) {
                            filterlist = new ArrayList<>();
                            for (int i = 0; i < arr_type_filter.length(); i++) {
                                JSONObject obj_temp = generalFunc.getJsonObject(arr_type_filter, i);
                                HashMap<String, String> map = new HashMap<String, String>();
                                String vTitle = generalFunc.getJsonValueStr("vTitle", obj_temp);
                                map.put("vTitle", vTitle);
                                String vFilterParam=generalFunc.getJsonValueStr("vFilterParam", obj_temp);
                                map.put("vFilterParam", vFilterParam);



                            myOrderAct.filterManage(filterlist);
                        }*/


                    }
                    buildFilterTypes(responseStringObject);
                    if (!nextPage.equals("") && !nextPage.equals("0")) {
                        next_page_str = nextPage;
                        isNextPageAvailable = true;
                    } else {
                        removeNextPageConfig();
                    }

                    orderHistoryRecycleAdapter.notifyDataSetChanged();

                } else {
                    buildFilterTypes(responseStringObject);

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
                    buildFilterTypes(responseStringObject);
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

    public void buildFilterTypes(JSONObject responseStringObject) {
        if (responseStringObject == null) return;
            String eFilterSel = generalFunc.getJsonValueStr("eFilterSel", responseStringObject);

            JSONArray subFilterOptionArr = generalFunc.getJsonArray("subFilterOption", responseStringObject);

            subFilterlist = new ArrayList<>();
            if (subFilterOptionArr != null && subFilterOptionArr.length() > 0) {
                for (int i = 0; i < subFilterOptionArr.length(); i++) {
                    JSONObject obj_temp = generalFunc.getJsonObject(subFilterOptionArr, i);
                    HashMap<String, String> map = new HashMap<String, String>();
                    String vTitle = generalFunc.getJsonValueStr("vTitle", obj_temp);
                    map.put("vTitle", vTitle);
                    String vSubFilterParam = generalFunc.getJsonValueStr("vSubFilterParam", obj_temp);
                    map.put("vSubFilterParam", vSubFilterParam);

                    if (vSubFilterParam.equalsIgnoreCase(eFilterSel)) {

                        if (myBookingFragment != null) {
                            myBookingFragment.selOrderSubFilterType = eFilterSel;
                            myBookingFragment.orderSubFilterPosition = i;

                        } else {
                            myOrderAct.selOrderSubFilterType = eFilterSel;
                            myOrderAct.orderSubFilterPosition = i;
                        }

                        filterTxt.setText(vTitle);
                    }
                    subFilterlist.add(map);
                }
            }

            if (myBookingFragment != null) {
                myBookingFragment.subFilterManage(subFilterlist, "Order");

            } else {
                myOrderAct.subFilterManage(subFilterlist, "Order");

            }

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

    @Override
    public void onCalendarTitleViewClick() {
        if (calendar_view.findViewById(R.id.weekLayout).getVisibility() == View.VISIBLE) {
            showHideCalender(false);
        } else {
            showHideCalender(true);
        }

    }

    @Override
    public void onCalendarPreviousButtonClick() {

    }

    @Override
    public void onCalendarNextButtonClick() {

    }



    @Override
    public void onCalendarDateSelected(Date date) {
        listData.clear();
        setDate(date);
    }

    @Override
    public void onCalendarMonthChanged(Date date) {
        listData.clear();
        setDate(date);
    }

    @Override
    public void onCalendarCurrentDayFound(Date date) {
        listData.clear();
        setDate(date);
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
            Utils.hideKeyboard(getActContext());
            switch (view.getId()) {
                case R.id.fromDateEditBox:
                    openFromDateSelection();
                    break;
                case R.id.toDateEditBox:
                    openToDateSelection();
                    break;
                case R.id.filterArea:
                    if (myBookingFragment != null) {
                        myBookingFragment.BuildType("Order");
                    } else {
                        myOrderAct.BuildType("Order");
                    }
                    break;
            }
        }
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Utils.hideKeyboard(getActivity());
    }

    @Override
    public void onRefresh() {
        swipeRefreshLayout.setRefreshing(true);
        getPastOrders(false, fromSelectedTime, toSelectedTime);
    }

}
