package com.taxifgo.driver;

import android.content.Context;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ImageView;

import com.general.files.ExecuteWebServerUrl;
import com.general.files.GeneralFunctions;
import com.general.files.MyApp;
import com.utils.Logger;
import com.utils.Utils;
import com.view.MButton;
import com.view.MTextView;
import com.view.MaterialRippleLayout;
import com.view.editBox.MaterialEditText;

import java.util.ArrayList;
import java.util.HashMap;

public class AdditionalChargeActivity extends AppCompatActivity {

    MTextView titleTxt;
    ImageView backImgView;
    MTextView additionalchargeHTxt, matrialfeeHTxt, miscfeeHTxt, discountHTxt, matrialfeeSTxt, miscfeeSTxt, discountSTxt;
    MTextView finalvalTxt, finalHTxt, currentchargeHTxt, currentchargeVTxt, noteLbl, noteTxt;
    MaterialEditText timatrialfeeVTxt, miscfeeVTxt, discountVTxt;
    MTextView matrialfeeCurrancyTxt, miscfeeCurrancyTxt, discountCurrancyTxt;

    double matrialfee = 0.00;
    double miscfee = 0.00;
    double discount = 0.00;
    double finaltotal = 0.00;
    ArrayList<Double> additonallist = new ArrayList<>();
    boolean isDiscountCalc = true;
    GeneralFunctions generalFunc;
    String currencetprice = "0.00";
    MButton submitBtn;
    MButton skipBtn;
    HashMap<String, String> data;
    String CurrencySymbol;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_additional_charge);
        data = (HashMap<String, String>) getIntent().getSerializableExtra("TRIP_DATA");
        initViews();
    }

    public Context getActContext() {
        return AdditionalChargeActivity.this;
    }


    public void initViews() {
        generalFunc = MyApp.getInstance().getGeneralFun(getActContext());
        backImgView = (ImageView) findViewById(R.id.backImgView);
        titleTxt = (MTextView) findViewById(R.id.titleTxt);
        backImgView.setVisibility(View.GONE);

        additionalchargeHTxt = (MTextView) findViewById(R.id.additionalchargeHTxt);
        matrialfeeHTxt = (MTextView) findViewById(R.id.matrialfeeHTxt);
        miscfeeHTxt = (MTextView) findViewById(R.id.miscfeeHTxt);
        discountHTxt = (MTextView) findViewById(R.id.discountHTxt);

        matrialfeeSTxt = (MTextView) findViewById(R.id.matrialfeeSTxt);
        miscfeeSTxt = (MTextView) findViewById(R.id.miscfeeSTxt);
        discountSTxt = (MTextView) findViewById(R.id.discountSTxt);

        finalvalTxt = (MTextView) findViewById(R.id.finalvalTxt);
        finalHTxt = (MTextView) findViewById(R.id.finalHTxt);

        currentchargeHTxt = (MTextView) findViewById(R.id.currentchargeHTxt);
        currentchargeVTxt = (MTextView) findViewById(R.id.currentchargeVTxt);

        noteLbl = (MTextView) findViewById(R.id.noteLbl);
        noteTxt = (MTextView) findViewById(R.id.noteTxt);

        matrialfeeCurrancyTxt = (MTextView) findViewById(R.id.matrialfeeCurrancyTxt);
        miscfeeCurrancyTxt = (MTextView) findViewById(R.id.miscfeeCurrancyTxt);
        discountCurrancyTxt = (MTextView) findViewById(R.id.discountCurrancyTxt);

        submitBtn = ((MaterialRippleLayout) findViewById(R.id.submitBtn)).getChildView();
        skipBtn = ((MaterialRippleLayout) findViewById(R.id.skipBtn)).getChildView();

        submitBtn.setId(Utils.generateViewId());
        skipBtn.setId(Utils.generateViewId());
        submitBtn.setOnClickListener(new setOnClickList());
        skipBtn.setOnClickListener(new setOnClickList());


        timatrialfeeVTxt = (MaterialEditText) findViewById(R.id.timatrialfeeVTxt);
        miscfeeVTxt = (MaterialEditText) findViewById(R.id.miscfeeVTxt);
        discountVTxt = (MaterialEditText) findViewById(R.id.discountVTxt);

        int maxLength = 10;
        InputFilter[] FilterArray = new InputFilter[1];
        FilterArray[0] = new InputFilter.LengthFilter(maxLength);
        timatrialfeeVTxt.setFilters(FilterArray);
        miscfeeVTxt.setFilters(FilterArray);
        discountVTxt.setFilters(FilterArray);


        timatrialfeeVTxt.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_CLASS_NUMBER);
        miscfeeVTxt.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_CLASS_NUMBER);
        discountVTxt.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_CLASS_NUMBER);
        discountVTxt.setShowClearButton(false);
        miscfeeVTxt.setShowClearButton(false);
        timatrialfeeVTxt.setShowClearButton(false);

        String data = generalFunc.convertNumberWithRTL("0.00");
        miscfeeVTxt.setHint(data);
        timatrialfeeVTxt.setHint(data);
        discountVTxt.setHint(data);

        setLabel();
        defaultAddtionalprice();
        calculateData("", finalvalTxt);

        timatrialfeeVTxt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                String val = timatrialfeeVTxt.getText().toString();
                if (val.startsWith(".") || val.equals(".")) {
                    timatrialfeeVTxt.setText("0.");
                    timatrialfeeVTxt.setSelection(2);
                }

            }

            @Override
            public void afterTextChanged(Editable s) {

                if (s.length() > 0) {
                    matrialfee = 0.00;
                    try {
                        matrialfee = Double.parseDouble(s.toString());
                        additonallist.remove(0);
                        additonallist.add(0, matrialfee);
                        calculateData(s.toString(), finalvalTxt);
                    } catch (Exception e) {

                    }
                } else {
                    additonallist.remove(0);
                    additonallist.add(0, 0.00);
                    calculateData(s.toString(), finalvalTxt);
                }

            }
        });

        miscfeeVTxt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                String val = miscfeeVTxt.getText().toString();
                if (val.startsWith(".") || val.equals(".")) {
                    miscfeeVTxt.setText("0.");
                    miscfeeVTxt.setSelection(2);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0) {
                    miscfee = 0.00;
                    try {
                        miscfee = Double.parseDouble(s.toString());
                        additonallist.remove(1);
                        additonallist.add(1, miscfee);
                        calculateData(s.toString(), finalvalTxt);
                    } catch (Exception e) {

                    }
                } else {
                    additonallist.remove(1);
                    additonallist.add(1, 0.00);
                    calculateData(s.toString(), finalvalTxt);
                }

            }
        });

        discountVTxt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                String val = discountVTxt.getText().toString();
                if (val.startsWith(".") || val.equals(".")) {
                    discountVTxt.setText("0.");
                    discountVTxt.setSelection(2);
                }

                if (isDiscountCalc == false) {
                    isDiscountCalc = true;
                    return;
                }
                double discountValue = GeneralFunctions.parseDoubleValue(0.0, "" + s);
                if (discount > 0) {
                    finaltotal = finaltotal + discount;
                }
                Logger.d("CheckVal", "::" + (finaltotal - discountValue));
                if ((finaltotal - discountValue) < 0) {
                    try {

                        isDiscountCalc = false;
                        discountVTxt.setText(finaltotal + "");
                        discountVTxt.setSelection(("" + finaltotal).length());
                        discountValue = finaltotal;
                    } catch (Exception e) {
//                        finaltotal = finaltotal - discountValue;
//                        discount = discountValue;
//
//                        additonallist.remove(2);
//                        additonallist.add(2, discount);
                        discountVTxt.setText(GeneralFunctions.convertDecimalPlaceDisplay(finaltotal) + "");
                        calculateData(Utils.getText(discountVTxt), finalvalTxt);
                        return;
//                        isDiscountCalc = false;
//                        Logger.e("Exception", "::" + e.toString());
//                        discountVTxt.setText(GeneralFunctions.convertDecimalPlaceDisplay(finaltotal) + "");

                    }
                }


                finaltotal = finaltotal - discountValue;
                discount = discountValue;

                additonallist.remove(2);
                additonallist.add(2, discount);
                calculateData(s.toString(), finalvalTxt);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

    }

    private void defaultAddtionalprice() {
        additonallist.add(0, 0.00);
        additonallist.add(1, 0.00);
        additonallist.add(2, 0.00);
    }


    public void setLabel() {
        matrialfeeHTxt.setText(generalFunc.retrieveLangLBl("Material fee", "LBL_MATERIAL_FEE"));
        miscfeeHTxt.setText(generalFunc.retrieveLangLBl("Misc fee", "LBL_MISC_FEE"));
        discountHTxt.setText(generalFunc.retrieveLangLBl("Provider Discount", "LBL_PROVIDER_DISCOUNT"));
        finalHTxt.setText(generalFunc.retrieveLangLBl("FINAL TOTAL", "LBL_FINAL_TOTAL_HINT"));
        currentchargeHTxt.setText(generalFunc.retrieveLangLBl("Service Cost", "LBL_SERVICE_COST"));
        noteLbl.setText(generalFunc.retrieveLangLBl("", "LBL_NOTE") + ":-");
        noteTxt.setText(generalFunc.retrieveLangLBl("", "LBL_ADDITIONAL_CHARGE_NOTE"));
        titleTxt.setText(generalFunc.retrieveLangLBl("", "LBL_ADDITIONAL_CHARGES"));
        submitBtn.setText(generalFunc.retrieveLangLBl("", "LBL_BTN_SUBMIT_TXT"));
        skipBtn.setText(generalFunc.retrieveLangLBl("", "LBL_SKIP_TXT"));

        CurrencySymbol = generalFunc.getJsonValue("CurrencySymbol", generalFunc.retrieveValue(Utils.USER_PROFILE_JSON));
        matrialfeeCurrancyTxt.setText(CurrencySymbol);
        miscfeeCurrancyTxt.setText(CurrencySymbol);
        discountCurrancyTxt.setText(CurrencySymbol);

        currencetprice = data.get("TotalFareUberXValue");
        currentchargeVTxt.setText(generalFunc.convertNumberWithRTL(data.get("TotalFareUberX")));
    }


    private void calculateData(String s, MTextView finalvalTxt) {
        try {
            finaltotal = 0.00;
            finaltotal = GeneralFunctions.parseDoubleValue(0, currencetprice) + GeneralFunctions.parseDoubleValue(0, additonallist.get(0).toString()) + GeneralFunctions.parseDoubleValue(0, additonallist.get(1).toString()) - GeneralFunctions.parseDoubleValue(0, additonallist.get(2).toString());
//            if (finaltotal < 0) {
//                finalvalTxt.setText(CurrencySymbol + " " +Utils.getText(discountVTxt));
//                return;
//            }
            finalvalTxt.setText(CurrencySymbol + " " + generalFunc.convertNumberWithRTL(GeneralFunctions.convertDecimalPlaceDisplay(finaltotal)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void setTripEnd(boolean isSkip) {


        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "ProcessEndTrip");
        parameters.put("TripId", data.get("iTripId"));
        parameters.put("DriverId", generalFunc.getMemberId());
        parameters.put("fMaterialFee", isSkip ? "" : additonallist.get(0).toString());
        parameters.put("fMiscFee", isSkip ? "" : additonallist.get(1).toString());
        parameters.put("fDriverDiscount", isSkip ? "" : additonallist.get(2).toString());
        parameters.put("PassengerId", data.get("PassengerId"));
        parameters.put("iTripTimeId", data.get("iTripTimeId"));
        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(), parameters);
        exeWebServer.setLoaderConfig(getActContext(), true, generalFunc);
        exeWebServer.setDataResponseListener(responseString -> endTripResponse(responseString));
        exeWebServer.execute();
    }


    private void endTripResponse(String responseString) {

        if (responseString != null && !responseString.equals("")) {

            boolean isDataAvail = GeneralFunctions.checkDataAvail(Utils.action_str, responseString);

            if (isDataAvail == true) {
                MyApp.getInstance().restartWithGetDataApp();

            } else {
                generalFunc.showGeneralMessage("",
                        generalFunc.retrieveLangLBl("", generalFunc.getJsonValue(Utils.message_str, responseString)));
            }
        }
    }


    public class setOnClickList implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            int i = view.getId();
            if (i == submitBtn.getId() || i == skipBtn.getId()) {
                setTripEnd(i == skipBtn.getId());
            }
        }
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
    }
}
