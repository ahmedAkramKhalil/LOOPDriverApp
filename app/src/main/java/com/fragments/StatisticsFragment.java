package com.fragments;

import android.content.Context;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;
import androidx.cardview.widget.CardView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.autofit.et.lib.AutoFitEditText;
import com.taxifgo.driver.R;
import com.dialogs.OpenListView;
import com.general.files.ExecuteWebServerUrl;
import com.general.files.GeneralFunctions;
import com.general.files.MyApp;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.utils.Logger;
import com.utils.Utils;
import com.view.ErrorView;
import com.view.MTextView;
import com.view.anim.loader.AVLoadingIndicatorView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class StatisticsFragment extends Fragment {
    View view;
    JSONObject userProfileJsonObj;
    GeneralFunctions generalFunc;
    String selectedyear = "";
    MTextView noTripHTxt;
    MTextView totalearnHTxt;
    MTextView yearBox, yearTxt;
    AutoFitEditText totalearnVTxt,noTripVTxt;
    ArrayList<HashMap<String, String>> items_txt_year = new ArrayList<>();
    String TotalEarning = "";
    String TripCount = "";
    ArrayList<String> listData = new ArrayList<>();

    LineChart chart;
    ArrayList<String> monthList = new ArrayList<>();


    AVLoadingIndicatorView loaderView;
    ErrorView errorView;
    LinearLayout yearSelectArea;
    CardView bottomarea;

    int type;
    LinearLayout chartContainer;
    ImageView calenderImg;
    ImageView image_1;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_statitistics, container, false);

        generalFunc = MyApp.getInstance().getGeneralFun(getActContext());
        userProfileJsonObj = generalFunc.getJsonObject(generalFunc.retrieveValue(Utils.USER_PROFILE_JSON));
        type = getArguments().getInt("type");

        image_1 =  (ImageView)view.findViewById(R.id.image_1);
        noTripHTxt = (MTextView) view.findViewById(R.id.noTripHTxt);
        calenderImg = (ImageView) view.findViewById(R.id.calenderImg);
        yearTxt = (MTextView) view.findViewById(R.id.yearTxt);
        noTripVTxt = (AutoFitEditText) view.findViewById(R.id.noTripVTxt);
        totalearnHTxt = (MTextView) view.findViewById(R.id.totalearnHTxt);
        totalearnVTxt = (AutoFitEditText) view.findViewById(R.id.totalearnVTxt);
        totalearnVTxt.setClickable(false);
        yearBox = (MTextView) view.findViewById(R.id.yearBox);

        errorView = (ErrorView) view.findViewById(R.id.errorView);
        loaderView = (AVLoadingIndicatorView) view.findViewById(R.id.loaderView);

        // yearBox.getLabelFocusAnimator().start();
        yearSelectArea = (LinearLayout) view.findViewById(R.id.yearSelectArea);
        bottomarea = (CardView) view.findViewById(R.id.bottomarea);
        chartContainer = (LinearLayout) view.findViewById(R.id.chartContainer);
        //  Utils.removeInput(yearBox);
        yearBox.setOnTouchListener(new setOnTouchList());
        yearBox.setOnClickListener(new setOnClickList());
        calenderImg.setOnClickListener(new setOnClickList());

        setLabels();
        getChartDetails();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if(type == Utils.MENU_ORDER_STATISTICS)
        {
            image_1.setImageResource(R.drawable.ic_order_2);
        }else {
            image_1.setImageResource(R.drawable.ic_sports_car);
        }
    }

    public void setLabels() {

        totalearnHTxt.setText((generalFunc.retrieveLangLBl("", "LBL_TOTAL_EARNINGS")));
        noTripHTxt.setText((generalFunc.retrieveLangLBl("", "LBL_NUMBER_OF_TRIPS")));
        yearBox.setText((generalFunc.retrieveLangLBl("", "LBL_YEAR")));
        yearTxt.setText((generalFunc.retrieveLangLBl("", "LBL_YEAR")));
        //, generalFunc.retrieveLangLBl("", "LBL_CHOOSE_YEAR"));

        if(type == Utils.MENU_ORDER_STATISTICS)
        {
            noTripHTxt.setText((generalFunc.retrieveLangLBl("", "LBL_NUMBER_OF_ORDERS")));

        }
    }


    public void getChartDetails() {

        loaderView.setVisibility(View.VISIBLE);
        if (errorView.getVisibility() == View.VISIBLE) {
            errorView.setVisibility(View.GONE);
        }


        final HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "getYearTotalEarnings");

        parameters.put("UserType", Utils.app_type);
        parameters.put("year", selectedyear);
        if (type == Utils.MENU_ORDER_STATISTICS) {
            parameters.put("iMemberId", generalFunc.getMemberId());
            parameters.put("eSystem", "DeliverAll");
        } else {
            parameters.put("iDriverId", generalFunc.getMemberId());

        }


        final ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(), parameters);
        exeWebServer.setDataResponseListener(new ExecuteWebServerUrl.SetDataResponse() {
            @Override
            public void setResponse(String responseString) {

                if (responseString != null && !responseString.equals("")) {
                    yearSelectArea.setVisibility(View.VISIBLE);
                    bottomarea.setVisibility(View.VISIBLE);

                    if (generalFunc.checkDataAvail(Utils.action_str, responseString) == true) {

                        if (type == Utils.MENU_ORDER_STATISTICS) {
                            JSONObject responseStringObject = generalFunc.getJsonObject(responseString);
                            TripCount = generalFunc.convertNumberWithRTL(generalFunc.getJsonValueStr("OrderCount", responseStringObject));
                        } else {
                            TripCount = generalFunc.getJsonValue("TripCount", responseString);
                        }

                        String MaxEarning = generalFunc.getJsonValue("MaxEarning", responseString);
                        TotalEarning = generalFunc.getJsonValue("TotalEarning", responseString);
                        selectedyear = generalFunc.getJsonValue("CurrentYear", responseString);
                        JSONArray YearMonthArr = generalFunc.getJsonArray("YearMonthArr", responseString);
                        listData.clear();
                        monthList.clear();
                        items_txt_year.clear();
                        if (generalFunc.isRTLmode()) {
                            for (int j = YearMonthArr.length() - 1; j >= 0; j--) {
                                JSONObject jsonObject = generalFunc.getJsonObject(YearMonthArr, j);


                                monthList.add(generalFunc.getJsonValue("CurrentMonth", jsonObject.toString()));


                                listData.add(jsonObject.optString("TotalEarnings"));
                            }

                        } else {
                            for (int j = 0; j < YearMonthArr.length(); j++) {
                                JSONObject jsonObject = generalFunc.getJsonObject(YearMonthArr, j);

                                monthList.add(generalFunc.getJsonValue("CurrentMonth", jsonObject.toString()));

                                listData.add(jsonObject.optString("TotalEarnings"));
                            }

                        }
                        JSONArray yeararray = generalFunc.getJsonArray("YearArr", responseString);
                        for (int i = 0; i < yeararray.length(); i++) {
                            if (selectedyear.equalsIgnoreCase((String) generalFunc.getValueFromJsonArr(yeararray, i))) {
                                selYearPosition = i;
                            }
                            HashMap<String, String> hashMap = new HashMap<>();
                            hashMap.put("vTitle", generalFunc.convertNumberWithRTL((String) generalFunc.getValueFromJsonArr(yeararray, i)));
                            items_txt_year.add(hashMap);
                        }

                        setData();

                        generateData();

                        //  chart.setViewportCalculationEnabled(false);

                        if (MaxEarning.equals("0")) {
                            MaxEarning = "1";
                        }
                        //chart.setVisibility(View.VISIBLE);
                        loaderView.setVisibility(View.GONE);
                        //resetViewport(generalFunc.parseFloatValue(0, MaxEarning));

                        if (chart != null) {
                            XAxis xAxis = chart.getXAxis();
                            //  xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                            xAxis.setDrawGridLines(false);
                            xAxis.setLabelCount(12, false);
                            xAxis.setAxisMinimum(0);
                            xAxis.setDrawLabels(true);

                            xAxis.setGranularity(1);
                            xAxis.setTextColor(getResources().getColor(R.color.appThemeColor_1));
                            xAxis.setValueFormatter(new IAxisValueFormatter() {
                                @Override
                                public String getFormattedValue(float value, AxisBase axis) {
                                    return monthList.get((int) value);
                                }
                            });
                        }

                    } else {

                    }
                } else {
                    generateErrorView();
                    loaderView.setVisibility(View.GONE);
                }
            }


        });
        exeWebServer.execute();
    }

    public void generateErrorView() {

        yearSelectArea.setVisibility(View.GONE);
        bottomarea.setVisibility(View.GONE);
        generalFunc.generateErrorView(errorView, "LBL_ERROR_TXT", "LBL_NO_INTERNET_TXT");

        if (errorView.getVisibility() != View.VISIBLE) {
            errorView.setVisibility(View.VISIBLE);
        }
        errorView.setOnRetryListener(new ErrorView.RetryListener() {
            @Override
            public void onRetry() {
                getChartDetails();
            }
        });
    }

    private void generateData() {

        LayoutInflater inflater = (LayoutInflater) getActContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.linechart_view, null);
        LineChart chart = (LineChart) view.findViewById(R.id.chart1);
        view.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
        this.chart = chart;

        chart.getAxisLeft().setDrawLabels(false);
        chart.getViewPortHandler().setMaximumScaleX(10);
        chart.getViewPortHandler().setMaximumScaleY(5);
        chart.setPinchZoom(true);


        chart.getAxisRight().setDrawLabels(false);
        // chart.getXAxis().setDrawLabels(false);
        chart.getDescription().setEnabled(false);
        chart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        chart.getAxisRight().setEnabled(false);
        chart.getAxisLeft().setEnabled(false);
        chart.getAxisLeft().setAxisMinimum(0f);
        chart.getLegend().setEnabled(false);

        chart.getAxisRight().setDrawGridLines(false);
        chart.getAxisLeft().setDrawGridLines(false);
        chart.getXAxis().setDrawGridLines(false);


        if (chartContainer.getChildCount() > 0) {
            chartContainer.removeAllViewsInLayout();
        }


        chartContainer.addView(view);

        ArrayList<Entry> values = new ArrayList<Entry>();

        for (int i = 0; i < listData.size(); i++) {
            Logger.d("ListData", "::" + listData.get(i));
            values.add(new Entry(i, generalFunc.parseFloatValue(0, listData.get(i)), getResources().getDrawable(R.drawable.chart_circle_24dp)));
        }


        LineDataSet set1;

        if (chart.getData() != null &&
                chart.getData().getDataSetCount() > 0) {
            set1 = (LineDataSet) chart.getData().getDataSetByIndex(0);
            set1.setMode(LineDataSet.Mode.CUBIC_BEZIER);
            set1.setValues(values);
            //  set1.setDrawFilled(true);
            //  set1.setFillColor(getResources().getColor(R.color.appThemeColor_1));
            chart.getData().notifyDataChanged();
            chart.notifyDataSetChanged();


        } else {
            // create a dataset and give it a type
            set1 = new LineDataSet(values, "DataSet 1");
            set1.setMode(LineDataSet.Mode.CUBIC_BEZIER);
            set1.setDrawIcons(false);
            //   set1.setDrawFilled(true);
            //  set1.setFillColor(getResources().getColor(R.color.appThemeColor_1));
            // set the line to be drawn like this "- - - - - -"

            //   set1.enableDashedLine(10f, 5f, 0f);
            //set1.enableDashedHighlightLine(10f, 5f, 0f);
            set1.setColor(getResources().getColor(R.color.appThemeColor_1));
            set1.setCircleColor(getResources().getColor(R.color.appThemeColor_1));
            set1.setLineWidth(2f);
            set1.setCircleRadius(5f);
            set1.setDrawCircleHole(false);
            set1.setValueTextSize(12f);
            set1.setDrawFilled(false);
            //  set1.setMode(LineDataSet.Mode.CUBIC_BEZIER);
            set1.setFormLineWidth(1f);
            set1.setFormLineDashEffect(new DashPathEffect(new float[]{10f, 5f}, 0f));
            set1.setDrawHorizontalHighlightIndicator(false);
            set1.setDrawVerticalHighlightIndicator(false);
            set1.setFormSize(15.f);

            if (com.github.mikephil.charting.utils.Utils.getSDKInt() >= 18) {
                // fill drawable only supported on api level 18 and above
                Drawable drawable = ContextCompat.getDrawable(getActContext(), R.drawable.fade_red);
                set1.setFillDrawable(drawable);
            } else {
                set1.setFillColor(Color.BLACK);
            }

            ArrayList<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
            dataSets.add(set1); // add the datasets

            // create a data object with the datasets
            LineData data = new LineData(dataSets);

            // set data
            chart.setData(data);
        }

    }

    public String getSelectYearText() {
        return ("" + generalFunc.retrieveLangLBl("", "LBL_CHOOSE_YEAR"));
    }

    private void setData() {
        String totalEarning = generalFunc.convertNumberWithRTL(TotalEarning);
        totalearnVTxt.setText(totalEarning);
        // totalearnVTxt.setTextDirection(View.TEXT_DIRECTION_RTL);

        noTripVTxt.setText(generalFunc.convertNumberWithRTL(TripCount));
        yearBox.setText(generalFunc.convertNumberWithRTL(selectedyear));


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
            int i = view.getId();
            Utils.hideKeyboard(getActContext());

            if (i == R.id.backImgView) {
                //getActContext().onBackPressed();
            } else if (i == R.id.yearBox || i == R.id.calenderImg) {
                showYearDialog();

            }
        }
    }

    int selYearPosition = -1;

    public void showYearDialog() {
        OpenListView.getInstance(getActContext(), yearTxt.getText().toString(), items_txt_year, OpenListView.OpenDirection.CENTER, true, position -> {


            selYearPosition = position;

            yearBox.setText(items_txt_year.get(position).get("vTitle"));
            selectedyear = items_txt_year.get(position).get("vTitle");
            getChartDetails();

        }).show(selYearPosition, "vTitle");
    }

    public Context getActContext() {
        return getActivity();
    }
}
