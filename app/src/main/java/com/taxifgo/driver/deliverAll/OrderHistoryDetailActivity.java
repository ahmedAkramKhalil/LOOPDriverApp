package com.taxifgo.driver.deliverAll;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TableRow;

import com.taxifgo.driver.R;
import com.general.files.ExecuteWebServerUrl;
import com.general.files.GeneralFunctions;
import com.general.files.MyApp;
import com.utils.Utils;
import com.view.ErrorView;
import com.view.MTextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class OrderHistoryDetailActivity extends AppCompatActivity {

    MTextView titleTxt;
    ImageView backImgView;

    public GeneralFunctions generalFunc;
    LinearLayout fareDetailDisplayArea;
    private View convertView = null;
    MTextView cartypeTxt;
    private ErrorView errorView;
    private ProgressBar loading_order_detail;
    private HashMap<String, String> OrderData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_history_detail);

        generalFunc = MyApp.getInstance().getGeneralFun(getActContext());

        titleTxt = (MTextView) findViewById(R.id.titleTxt);
        errorView = (ErrorView) findViewById(R.id.errorView);
        loading_order_detail = (ProgressBar) findViewById(R.id.loading_order_detail);
        backImgView = (ImageView) findViewById(R.id.backImgView);
        fareDetailDisplayArea = (LinearLayout) findViewById(R.id.fareDetailDisplayArea);
        cartypeTxt = (MTextView) findViewById(R.id.cartypeTxt);

        OrderData = (HashMap<String, String>) getIntent().getSerializableExtra("OrderData");


        backImgView.setOnClickListener(new setOnClickList());

        setLabels();

        getOrderDetailList();
    }

    public void setLabels() {

        titleTxt.setText(generalFunc.retrieveLangLBl("", "LBL_ORDER_DETAIL_TXT"));

        ((MTextView) findViewById(R.id.oredrNoHTxt)).setText(generalFunc.retrieveLangLBl("", "LBL_ORDER_NO_TXT"));
        ((MTextView) findViewById(R.id.orderdateHTxt)).setText(generalFunc.retrieveLangLBl("", "LBL_ORDER_DATE_TXT"));
        ((MTextView) findViewById(R.id.restaurantNameHTxt)).setText(generalFunc.retrieveLangLBl("Store Name", "LBL_STORE_NAME"));

        ((MTextView) findViewById(R.id.deliveryAddressHTxt)).setText(generalFunc.retrieveLangLBl("Delivery Location", "LBL_DELIVERY_LOCATION_TXT"));

        ((MTextView) findViewById(R.id.chargesHTxt)).setText(generalFunc.retrieveLangLBl("", "LBL_CHARGES_TXT"));

    }

    public void getOrderDetailList() {
        if (errorView.getVisibility() == View.VISIBLE) {
            errorView.setVisibility(View.GONE);
        }

        loading_order_detail.setVisibility(View.VISIBLE);

        final HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "GetOrderDetailsRestaurant");
        parameters.put("iOrderId", OrderData.get("iOrderId"));
        parameters.put("UserType", Utils.app_type);
        parameters.put("eSystem", Utils.eSystem_Type);

        final ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(), parameters);
        exeWebServer.setDataResponseListener(new ExecuteWebServerUrl.SetDataResponse() {
            @Override
            public void setResponse(String responseString) {
                closeLoader();
                JSONObject responseStringObject=generalFunc.getJsonObject(responseString);

                if (responseStringObject != null && !responseStringObject.equals("")) {

                    if (generalFunc.checkDataAvail(Utils.action_str, responseStringObject) == true) {
                        String nextPage = generalFunc.getJsonValueStr("NextPage", responseStringObject);

                        JSONObject msg_obj = generalFunc.getJsonObject("message", responseStringObject);
                        JSONArray itemList = generalFunc.getJsonArray("itemlist", msg_obj);

                        setData(msg_obj);
                    }


                } else {

                    generateErrorView();
                }


            }
        });

        exeWebServer.execute();


    }

    public void closeLoader() {
        if (loading_order_detail.getVisibility() == View.VISIBLE) {
            loading_order_detail.setVisibility(View.GONE);
        }
    }

    public void generateErrorView() {

        closeLoader();

        generalFunc.generateErrorView(errorView, "LBL_ERROR_TXT", "LBL_NO_INTERNET_TXT");

        if (errorView.getVisibility() != View.VISIBLE) {
            errorView.setVisibility(View.VISIBLE);
        }
        errorView.setOnRetryListener(() -> getOrderDetailList());
    }


    public void setData(JSONObject responseString) {

        ((MTextView) findViewById(R.id.oredrNoVTxt)).setText(generalFunc.convertNumberWithRTL(generalFunc.getJsonValueStr("vOrderNo", responseString)));

        ((MTextView) findViewById(R.id.orderdateVTxt)).setText(generalFunc.getJsonValueStr("tOrderRequestDate", responseString));


        ((MTextView) findViewById(R.id.restaurantNameVTxt)).setText(generalFunc.getJsonValueStr("vCompany", responseString) + "\n" + generalFunc.getJsonValueStr("vRestuarantLocation", responseString));

        ((MTextView) findViewById(R.id.deliveryAddressVTxt)).setText(generalFunc.getJsonValueStr("UserAddress", responseString));


        cartypeTxt.setText(generalFunc.getJsonValueStr("vVehicleType", responseString));


        String trip_status_str = generalFunc.getJsonValueStr("vStatus", responseString);
        if (trip_status_str.contains("Canceled")) {
            String cancelLable = "LBL_CANCELED_DELIVERY_TXT";
            ((MTextView) findViewById(R.id.tripStatusTxt)).setText(generalFunc.retrieveLangLBl("", cancelLable));
            (findViewById(R.id.deliveryDetailArea)).setVisibility(View.VISIBLE);

        } else if (trip_status_str.contains("Delivered")) {

            String finishLable = "LBL_FINISHED_DELIVERY_TXT";
            ((MTextView) findViewById(R.id.tripStatusTxt)).setText(generalFunc.retrieveLangLBl("", finishLable));

            (findViewById(R.id.deliveryDetailArea)).setVisibility(View.VISIBLE);


        } else {
            ((MTextView) findViewById(R.id.tripStatusTxt)).setText(trip_status_str);

        }

        if (generalFunc.getJsonValueStr("ePaymentOption", responseString).equals("Cash")) {
            ((MTextView) findViewById(R.id.paymentTypeTxt)).setText(generalFunc.retrieveLangLBl("", "LBL_CASH_PAYMENT_TXT"));
        } else {
            ((MTextView) findViewById(R.id.paymentTypeTxt)).setText(generalFunc.retrieveLangLBl("Card Payment", "LBL_CARD_PAYMENT"));
            ((ImageView) findViewById(R.id.paymentTypeImgeView)).setImageResource(R.mipmap.ic_card_new);
        }

        if (generalFunc.getJsonValueStr("eCancelled", responseString).equals("Yes")) {
            String cancelledLable = "LBL_PREFIX_DELIVERY_CANCEL_DRIVER";

            ((MTextView) findViewById(R.id.tripStatusTxt)).setText(generalFunc.retrieveLangLBl("", cancelledLable) + " " +
                    generalFunc.getJsonValueStr("vCancelReason", responseString));
        }


        boolean FareDetailsArrNew = generalFunc.isJSONkeyAvail("FareDetailsArr", responseString.toString());

        JSONArray FareDetailsArrNewObj = null;
        if (FareDetailsArrNew == true) {
            FareDetailsArrNewObj = generalFunc.getJsonArray("FareDetailsArr", responseString.toString());
        }
        if (FareDetailsArrNewObj != null)
            addFareDetailLayout(FareDetailsArrNewObj);

    }

    private void addFareDetailLayout(JSONArray jobjArray) {

        if (fareDetailDisplayArea.getChildCount() > 0) {
            fareDetailDisplayArea.removeAllViewsInLayout();
        }

        for (int i = 0; i < jobjArray.length(); i++) {
            JSONObject jobject = generalFunc.getJsonObject(jobjArray, i);
            try {
                String data = jobject.names().getString(0);

                addFareDetailRow(data, jobject.get(data).toString(), jobjArray.length() - 1 == i ? true : false);

//                addFareDetailRow(jobject.names().getString(0), jobject.get(jobject.names().getString(0)).toString(), false);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }

    private void addFareDetailRow(String row_name, String row_value, boolean isLast) {
        LayoutInflater infalInflater = (LayoutInflater) getActContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = infalInflater.inflate(R.layout.design_fare_deatil_row, null);
        TableRow FareDetailRow = (TableRow) convertView.findViewById(R.id.FareDetailRow);
        TableLayout fair_area_table_layout = (TableLayout) convertView.findViewById(R.id.fair_area);
        MTextView titleHTxt = (MTextView) convertView.findViewById(R.id.titleHTxt);
        MTextView titleVTxt = (MTextView) convertView.findViewById(R.id.titleVTxt);

        titleHTxt.setText(generalFunc.convertNumberWithRTL(row_name));
        titleVTxt.setText(generalFunc.convertNumberWithRTL(row_value));

        if (isLast == true) {
            TableLayout.LayoutParams tableRowParams =
                    new TableLayout.LayoutParams
                            (TableLayout.LayoutParams.FILL_PARENT, Utils.pxToDp(getActContext(), 40));
            tableRowParams.setMargins(0, 10, 0, 0);

            fair_area_table_layout.setLayoutParams(tableRowParams);
            FareDetailRow.setLayoutParams(tableRowParams);
            titleVTxt.setTextColor(getActContext().getResources().getColor(R.color.appThemeColor_1));
            titleHTxt.setTextColor(getActContext().getResources().getColor(R.color.appThemeColor_1));
            fair_area_table_layout.setBackgroundColor(Color.parseColor("#EBEBEB"));
            fair_area_table_layout.getChildAt(0).setPadding(5, 0, 5, 10);
        } else {
            titleHTxt.setTextColor(Color.parseColor("#303030"));
            titleVTxt.setTextColor(Color.parseColor("#111111"));
        }
        if (convertView != null)
            fareDetailDisplayArea.addView(convertView);
    }

    public Context getActContext() {
        return OrderHistoryDetailActivity.this;
    }

    public class setOnClickList implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            Utils.hideKeyboard(OrderHistoryDetailActivity.this);
            switch (view.getId()) {
                case R.id.backImgView:
                    OrderHistoryDetailActivity.super.onBackPressed();
                    break;

            }
        }
    }
}
