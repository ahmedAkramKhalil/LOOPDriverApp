package com.taxifgo.driver;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import androidx.core.widget.NestedScrollView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.appcompat.widget.AppCompatImageView;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.autofit.et.lib.AutoFitEditText;
import com.general.files.ExecuteWebServerUrl;
import com.general.files.GeneralFunctions;
import com.general.files.MyApp;
import com.utils.Utils;
import com.view.GenerateAlertBox;
import com.view.MButton;
import com.view.MTextView;
import com.view.MaterialRippleLayout;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.text.Spanned.SPAN_INCLUSIVE_INCLUSIVE;


public class AddServiceActivity extends AppCompatActivity {

    MTextView titleTxt;
    ImageView backImgView;

    GeneralFunctions generalFunc;
    String iVehicleCategoryId = "";
    String vTitle = "";
    ArrayList<String> dataList = new ArrayList<>();
    LinearLayout serviceSelectArea;

    MButton btn_type2;
    int submitBtnId;
    Dialog PriceEditConifrmAlertDialog;

    String fAmount = "";

    ArrayList<Boolean> carTypesStatusArr;

    ProgressBar loadingBar;
    View contentView;
    String userProfileJson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_service);

        generalFunc = MyApp.getInstance().getGeneralFun(getActContext());
        userProfileJson = generalFunc.retrieveValue(Utils.USER_PROFILE_JSON);
        titleTxt = (MTextView) findViewById(R.id.titleTxt);
        backImgView = (ImageView) findViewById(R.id.backImgView);
        backImgView.setOnClickListener(new setOnClickList());
        serviceSelectArea = (LinearLayout) findViewById(R.id.serviceSelectArea);
        btn_type2 = ((MaterialRippleLayout) findViewById(R.id.btn_type2)).getChildView();
        loadingBar = findViewById(R.id.loadingBar);
        contentView = findViewById(R.id.contentView);

        submitBtnId = Utils.generateViewId();
        btn_type2.setId(submitBtnId);

        btn_type2.setOnClickListener(new setOnClickList());

        Intent in = getIntent();
        iVehicleCategoryId = in.getStringExtra("iVehicleCategoryId");
        vTitle = in.getStringExtra("vTitle");
        setLabels();
        carTypesStatusArr = new ArrayList<>();
        getsubCategoryList();
    }

    public void getsubCategoryList() {

        loadingBar.setVisibility(View.VISIBLE);
        contentView.setVisibility(View.GONE);

        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "getServiceTypes");
        parameters.put("iVehicleCategoryId", iVehicleCategoryId);
        parameters.put("iDriverId", generalFunc.getMemberId());
        parameters.put("UserType", Utils.userType);

        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(), parameters);
        exeWebServer.setLoaderConfig(getActContext(), false, generalFunc);
        exeWebServer.setDataResponseListener(responseString -> {
            JSONObject responseStringObject = generalFunc.getJsonObject(responseString);

            if (responseStringObject != null && !responseStringObject.equals("")) {
                boolean isDataAvail = GeneralFunctions.checkDataAvail(Utils.action_str, responseStringObject);

                if (isDataAvail) {

                    String ENABLE_DRIVER_SERVICE_REQUEST_MODULE = generalFunc.getJsonValueStr("ENABLE_DRIVER_SERVICE_REQUEST_MODULE", responseStringObject);
                    JSONArray carList_arr = generalFunc.getJsonArray("message", responseStringObject);

                    if (carList_arr != null) {
                        for (int i = 0; i < carList_arr.length(); i++) {
                            JSONObject obj = generalFunc.getJsonObject(carList_arr, i);
                            dataList.add(obj.toString());
                        }
                    }
                    //buildServices();
                    buildServices(ENABLE_DRIVER_SERVICE_REQUEST_MODULE);
                } else {

                    final GenerateAlertBox generateAlert = new GenerateAlertBox(getActContext());
                    generateAlert.setCancelable(false);
                    generateAlert.setBtnClickList(btn_id -> {
                        generateAlert.closeAlertBox();

                        backImgView.performClick();
                    });
                    generateAlert.setContentMessage("", generalFunc.retrieveLangLBl("", generalFunc.getJsonValue(Utils.message_str, responseString)));
                    generateAlert.setPositiveBtn(generalFunc.retrieveLangLBl("", "LBL_BTN_OK_TXT"));

                    generateAlert.showAlertBox();
                }
            } else {
                generalFunc.showError();
            }

            loadingBar.setVisibility(View.GONE);
            contentView.setVisibility(View.VISIBLE);

        });
        exeWebServer.execute();
    }


    public void buildServices(String ENABLE_DRIVER_SERVICE_REQUEST_MODULE) {

        if (serviceSelectArea.getChildCount() > 0) {
            serviceSelectArea.removeAllViewsInLayout();
        }
        for (int i = 0; i < dataList.size(); i++) {
            String obj = dataList.get(i);

            final LayoutInflater inflater = (LayoutInflater) getActContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.item_select_service_design, null);

            MTextView serviceNameTxtView = (MTextView) view.findViewById(R.id.serviceNameTxtView);
            MTextView serviceTypeNameTxtView = (MTextView) view.findViewById(R.id.serviceTypeNameTxtView);

            final MTextView minHourTxtView = (MTextView) view.findViewById(R.id.minHourTxtView);

            final MTextView serviceamtHtxt = (MTextView) view.findViewById(R.id.serviceamtHtxt);
            final MTextView serviceamtVtxt = (MTextView) view.findViewById(R.id.serviceamtVtxt);
            final ImageView editBtn = (ImageView) view.findViewById(R.id.editBtn);
            final LinearLayout editarea = (LinearLayout) view.findViewById(R.id.editarea);

            String[] vCarTypes = {};

            AppCompatCheckBox chkBox = (AppCompatCheckBox) view.findViewById(R.id.chkBox);

            serviceamtHtxt.setText(generalFunc.retrieveLangLBl("Rate", "LBL_RATE") + ":");

            serviceNameTxtView.setText(generalFunc.convertNumberWithRTL(generalFunc.getJsonValue("vTitle", obj)));
            serviceTypeNameTxtView.setText(generalFunc.getJsonValue("SubTitle", obj));

            String ischeck = generalFunc.getJsonValue("VehicleServiceStatus", obj);

            /*new service pending addon*/
            String eServiceStatus = generalFunc.getJsonValue("eServiceRequest", obj);

            if (ENABLE_DRIVER_SERVICE_REQUEST_MODULE.equalsIgnoreCase("Yes")) {

                if ((ischeck.equalsIgnoreCase("true") && eServiceStatus.equalsIgnoreCase("Active"))) {
                    chkBox.setChecked(true);
                    chkBox.setClickable(true);
                    carTypesStatusArr.add(true);

                } else if (ischeck.equalsIgnoreCase("false") && eServiceStatus.equalsIgnoreCase("Pending")) {

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        chkBox.setButtonDrawable(R.drawable.ic_mark_gray);


                    }

                    chkBox.setChecked(true);
                    chkBox.setClickable(true);
                    carTypesStatusArr.add(false);

                } else if (ischeck.equalsIgnoreCase("false") && eServiceStatus.equalsIgnoreCase("Inactive")) {

                    chkBox.setChecked(false);
                    chkBox.setClickable(true);
                    carTypesStatusArr.add(false);
                }

            } else {

                if (ischeck.equalsIgnoreCase("true") || Arrays.asList(vCarTypes).contains(generalFunc.getJsonValue("iVehicleTypeId", obj))) {
                    chkBox.setChecked(true);
                    carTypesStatusArr.add(true);
                } else {
                    carTypesStatusArr.add(false);
                }
            }
            /*end*/


            final int finalI = i;
            String eFareType = generalFunc.getJsonValue("eFareType", obj);
            if (generalFunc.getJsonValue("ePriceType", obj).equalsIgnoreCase("Provider") && (eFareType.equalsIgnoreCase("Fixed") || eFareType.equalsIgnoreCase("Hourly"))) {
                editarea.setVisibility(View.VISIBLE);
                editBtn.setVisibility(View.VISIBLE);
            } else {
                editarea.setVisibility(View.GONE);
                editBtn.setVisibility(View.GONE);
            }

            if (eFareType != null && eFareType.trim().equals("Hourly") && generalFunc.getJsonValue("fMinHour", obj) != null && GeneralFunctions.parseIntegerValue(0, generalFunc.getJsonValue("fMinHour", obj)) > 1) {

                minHourTxtView.setVisibility(View.VISIBLE);
                minHourTxtView.setText("" + "(" + generalFunc.retrieveLangLBl("", "LBL_MINIMUM_TXT") + " " + generalFunc.getJsonValue("fMinHour", obj) + " " + generalFunc.retrieveLangLBl("", "LBL_HOURS_TXT") + ")");
            } else {
                minHourTxtView.setVisibility(View.GONE);
            }

//            chkBox.setOnCheckedChangeListener((buttonView, isChecked) -> carTypesStatusArr.set(finalI, isChecked));

            chkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                    /*new service pending addon*/
                    if (ENABLE_DRIVER_SERVICE_REQUEST_MODULE.equalsIgnoreCase("Yes")) {

                        if (isChecked == false) {
//                        Toast.makeText(getActContext(), "un-check " + finalI + isChecked, Toast.LENGTH_SHORT).show();
//                        carTypesStatusArr.set(finalI, false);

                            if (eServiceStatus.equalsIgnoreCase("Pending")) {

                                final GenerateAlertBox generateAlert = new GenerateAlertBox(getActContext());
                                generateAlert.setCancelable(false);
                                generateAlert.setBtnClickList(new GenerateAlertBox.HandleAlertBtnClick() {
                                    @Override
                                    public void handleBtnClick(int btn_id) {
                                        if (btn_id == 1) {
                                            carTypesStatusArr.set(finalI, false);
                                            generateAlert.closeAlertBox();
                                            chkBox.setChecked(true);
                                        }
                                    }
                                });
                                generateAlert.setContentMessage("", generalFunc.retrieveLangLBl("", "LBL_SERVICE_REQUEST_PENDING"));
                                generateAlert.setPositiveBtn(generalFunc.retrieveLangLBl("", "LBL_OK"));
                                generateAlert.showAlertBox();

                            } else if (eServiceStatus.equalsIgnoreCase("Active")) {

                                GenerateAlertBox generateAlert = new GenerateAlertBox(getActContext());
                                generateAlert.setCancelable(false);
                                generateAlert.setBtnClickList(new GenerateAlertBox.HandleAlertBtnClick() {
                                    @Override
                                    public void handleBtnClick(int btn_id) {
                                        if (btn_id == 0) {
                                            carTypesStatusArr.set(finalI, true);
                                            chkBox.setChecked(true);
                                        } else {
                                            carTypesStatusArr.set(finalI, false);
                                        }
                                    }
                                });
                                generateAlert.setContentMessage("", generalFunc.retrieveLangLBl("", "LBL_UNSELECT_CHECKBOX_FOR_SERVICE"));
                                generateAlert.setPositiveBtn(generalFunc.retrieveLangLBl("", "LBL_BTN_OK_TXT"));
                                generateAlert.setNegativeBtn(generalFunc.retrieveLangLBl("", "LBL_CANCEL_TXT"));
                                generateAlert.showAlertBox();

                            } else {
                                carTypesStatusArr.set(finalI, false);
                            }

                        } else {
//                        Toast.makeText(getActContext(), "check " + finalI + isChecked, Toast.LENGTH_SHORT).show();
                            carTypesStatusArr.set(finalI, true);
                        }

                    } else {
                        carTypesStatusArr.set(finalI, isChecked);
                    }
                    /*end*/
                }
            });

            if (eFareType.equalsIgnoreCase("Hourly")) {

                String text1 = generalFunc.getJsonValue("vCurrencySymbol", obj) + " " + generalFunc.getJsonValue("fAmount", obj);
                String text2 = "/" + generalFunc.retrieveLangLBl("hour", "LBL_HOUR");
                SpannableString span1 = new SpannableString(text1);
                span1.setSpan(new AbsoluteSizeSpan(Utils.dpToPx(18, getActContext())), 0, text1.length(), SPAN_INCLUSIVE_INCLUSIVE);

                SpannableString span2 = new SpannableString(text2);
                span2.setSpan(new AbsoluteSizeSpan(Utils.dpToPx(12, getActContext())), 0, text2.length(), SPAN_INCLUSIVE_INCLUSIVE);
                span2.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.gray)), 0, text2.length(), 0);
                CharSequence finalText = TextUtils.concat(span1, "", span2);

                serviceamtVtxt.setText(finalText);
            } else {
                serviceamtVtxt.setText(generalFunc.getJsonValue("vCurrencySymbol", obj) + " " + generalFunc.getJsonValue("fAmount", obj));
            }

            fAmount = generalFunc.getJsonValue("fAmount", obj);

            editBtn.setOnClickListener(v -> driverChangePriceDilalg(finalI));
            serviceSelectArea.addView(view);
        }

        View tmpView = new View(getActContext());
        NestedScrollView.LayoutParams tmpLayoutParams = new NestedScrollView.LayoutParams(NestedScrollView.LayoutParams.MATCH_PARENT, Utils.dipToPixels(getActContext(), 15));

        tmpView.setLayoutParams(tmpLayoutParams);
        serviceSelectArea.addView(tmpView);
    }


    public void manageButton(MButton btn, AutoFitEditText editText) {
        if (Utils.checkText(editText)) {
            if (generalFunc.parseDoubleValue(0, Utils.getText(editText)) > 0) {
                btn.setEnabled(true);
            } else {
                btn.setEnabled(false);
            }
        } else {
            btn.setEnabled(false);
        }

    }


    public void driverChangePriceDilalg(final int pos) {
        PriceEditConifrmAlertDialog = new Dialog(getActContext(), R.style.ImageSourceDialogStyle);
        PriceEditConifrmAlertDialog.setContentView(R.layout.desgin_extracharge_confirm);

       /* android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(getActContext());
        LayoutInflater inflater = (LayoutInflater) getActContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View dialogView = inflater.inflate(R.layout.desgin_extracharge_confirm, null);
        builder.setView(dialogView);*/

        final AutoFitEditText tipAmountEditBox = (AutoFitEditText) PriceEditConifrmAlertDialog.findViewById(R.id.editBox);
        tipAmountEditBox.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_CLASS_NUMBER);
        tipAmountEditBox.setVisibility(View.VISIBLE);


        final MButton giveTipTxtArea = ((MaterialRippleLayout) PriceEditConifrmAlertDialog.findViewById(R.id.giveTipTxtArea)).getChildView();
        final MTextView skipTxtArea = (MTextView) PriceEditConifrmAlertDialog.findViewById(R.id.skipTxtArea);
        final MTextView titileTxt = (MTextView) PriceEditConifrmAlertDialog.findViewById(R.id.titileTxt);
        final MTextView msgTxt = (MTextView) PriceEditConifrmAlertDialog.findViewById(R.id.msgTxt);
        final MTextView addmoneynote = (MTextView) PriceEditConifrmAlertDialog.findViewById(R.id.addmoneynote);
        final MTextView CurrencySymbolTXT = (MTextView) PriceEditConifrmAlertDialog.findViewById(R.id.CurrencySymbolTXT);
        ImageView minusImageView = (ImageView) PriceEditConifrmAlertDialog.findViewById(R.id.minusImageView);
        ImageView addImageView = (ImageView) PriceEditConifrmAlertDialog.findViewById(R.id.addImageView);
        final MTextView lblserviceprice = (MTextView) PriceEditConifrmAlertDialog.findViewById(R.id.lblserviceprice);


        msgTxt.setVisibility(View.VISIBLE);
        titileTxt.setText(generalFunc.retrieveLangLBl("Enter Service Amount Below:", "LBL_ENTER_SERVICE_AMOUNT"));
        skipTxtArea.setText(generalFunc.retrieveLangLBl("", "LBL_CANCEL_TXT"));
        lblserviceprice.setText(generalFunc.retrieveLangLBl("", "LBL_SERVICE_PRICE"));
        msgTxt.setText("");

        msgTxt.setVisibility(View.GONE);
        giveTipTxtArea.setText("" + generalFunc.retrieveLangLBl("", "LBL_CONFIRM_TXT"));
        skipTxtArea.setText("" + generalFunc.retrieveLangLBl("", "LBL_CANCEL_TXT"));
        addmoneynote.setText(generalFunc.retrieveLangLBl("", "LBL_ADD_MONEY_MSG"));
        skipTxtArea.setOnClickListener(view -> {
            Utils.hideKeyboard(AddServiceActivity.this);
            PriceEditConifrmAlertDialog.dismiss();

        });

        tipAmountEditBox.setText(generalFunc.retrieveLangLBl("", "LBL_ENTER_AMOUNT"));
        // tipAmountEditBox.setPaddings(22, 0, 0, 0);

        String obj = dataList.get(pos);

        if (!generalFunc.getJsonValue("fAmount", obj).equals("") && generalFunc.getJsonValue("fAmount", obj) != null) {

            tipAmountEditBox.setText(String.format("%.2f", (double) GeneralFunctions.parseDoubleValue(0, generalFunc.getJsonValue("fAmount", obj))));
        } else {
            tipAmountEditBox.setText("0.00");
        }


        addImageView.setOnClickListener(view -> mangePluseView(tipAmountEditBox));
        minusImageView.setOnClickListener(view -> mangeMinusView(tipAmountEditBox));


        CurrencySymbolTXT.setText(generalFunc.getJsonValue("vCurrencyDriver", userProfileJson));

        tipAmountEditBox.setFilters(new InputFilter[]{new com.general.files.DecimalDigitsInputFilter(2)});
        tipAmountEditBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                manageButton(giveTipTxtArea, tipAmountEditBox);
            }

            @Override
            public void afterTextChanged(Editable s) {


                if (tipAmountEditBox.getText().length() == 1) {
                    if (tipAmountEditBox.getText().toString().contains(".")) {
                        tipAmountEditBox.setText("0.");
                        tipAmountEditBox.setSelection(tipAmountEditBox.length());
                    }
                }
            }
        });


        giveTipTxtArea.setOnClickListener(view -> {
            Utils.hideKeyboard(AddServiceActivity.this);

            final boolean tipAmountEntered = Utils.checkText(tipAmountEditBox) ? true : Utils.setErrorFields(tipAmountEditBox, generalFunc.retrieveLangLBl("", "LBL_FEILD_REQUIRD"));

            if (!tipAmountEntered) {
                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) CurrencySymbolTXT.getLayoutParams();
                params.setMargins(0, 0, 0, 25);
                CurrencySymbolTXT.setLayoutParams(params);
                return;

            }
            if (GeneralFunctions.parseDoubleValue(0, tipAmountEditBox.getText().toString()) > 0) {
                PriceEditConifrmAlertDialog.dismiss();

                try {
                    fAmount = tipAmountEditBox.getText().toString();
                    addServiceAmount(pos);
                } catch (Exception e) {
                }
            } else {
                tipAmountEditBox.setText("");
                Utils.setErrorFields(tipAmountEditBox, generalFunc.retrieveLangLBl("", "LBL_ADD_CORRECT_DETAIL_TXT"));
            }
        });
        // PriceEditConifrmAlertDialog = builder.create();
        PriceEditConifrmAlertDialog.setCancelable(false);
        if (generalFunc.isRTLmode()) {
            generalFunc.forceRTLIfSupported(PriceEditConifrmAlertDialog);
        }


        Window window = PriceEditConifrmAlertDialog.getWindow();
        window.setGravity(Gravity.BOTTOM);
        window.setLayout(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        PriceEditConifrmAlertDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        PriceEditConifrmAlertDialog.show();

    }



    public void setLabels() {
        titleTxt.setText(vTitle);
        btn_type2.setText(generalFunc.retrieveLangLBl("Update Services", "LBL_UPDATE_SERVICES"));
    }

    public Context getActContext() {
        return AddServiceActivity.this;
    }

    public class setOnClickList implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            int i = view.getId();
            if (i == submitBtnId) {
                boolean isCarTypeSelected = false;
                String carTypes = "";
                for (int j = 0; j < carTypesStatusArr.size(); j++) {
                    if (carTypesStatusArr.get(j)) {
                        isCarTypeSelected = true;
                        String iVehicleTypeId = generalFunc.getJsonValue("iVehicleTypeId", dataList.get(j).toString());
                        carTypes = carTypes.equals("") ? iVehicleTypeId : (carTypes + "," + iVehicleTypeId);
                    }
                }
                addService(carTypes);
            } else if (view == backImgView) {
                onBackPressed();
            }

        }
    }

    public void addServiceAmount(int pos) {
        String obj = dataList.get(pos);

        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "UpdateDriverServiceAmount");
        parameters.put("iVehicleTypeId", generalFunc.getJsonValue("iVehicleTypeId", obj.toString()));
        parameters.put("iDriverId", generalFunc.getMemberId());
        parameters.put("fAmount", fAmount);
        parameters.put("UserType", Utils.app_type);

        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(), parameters);
        exeWebServer.setLoaderConfig(getActContext(), true, generalFunc);
        exeWebServer.setDataResponseListener(responseString -> {
            JSONObject responseStringObject = generalFunc.getJsonObject(responseString);

            if (responseStringObject != null && !responseStringObject.equals("")) {
                dataList.clear();
                boolean isDataAvail = GeneralFunctions.checkDataAvail(Utils.action_str, responseStringObject);

                if (isDataAvail) {
                    final GenerateAlertBox generateAlert = new GenerateAlertBox(getActContext());
                    generateAlert.setCancelable(false);
                    generateAlert.setBtnClickList(btn_id -> {
                        generateAlert.closeAlertBox();
                        carTypesStatusArr.clear();
                        getsubCategoryList();
                    });

                    generateAlert.setContentMessage("", generalFunc.retrieveLangLBl("", generalFunc.getJsonValueStr(Utils.message_str, responseStringObject)));
                    generateAlert.setPositiveBtn(generalFunc.retrieveLangLBl("", "LBL_BTN_OK_TXT"));

                    generateAlert.showAlertBox();
                } else {
//                    generalFunc.showGeneralMessage("",
//                            generalFunc.retrieveLangLBl("", generalFunc.getJsonValueStr(Utils.message_str, responseStringObject)));
                    generalFunc.showGeneralMessage("", generalFunc.retrieveLangLBl("", generalFunc.getJsonValueStr(Utils.message_str, responseStringObject)));
                }
            } else {
                generalFunc.showError();
            }
        });
        exeWebServer.execute();
    }


    public void addService(String vCarType) {
        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "UpdateDriverVehicle");
        parameters.put("iDriverId", generalFunc.getMemberId());
        parameters.put("UserType", Utils.app_type);
        parameters.put("vCarType", vCarType);
        parameters.put("iVehicleCategoryId", iVehicleCategoryId);
        parameters.put("eType", Utils.CabGeneralType_UberX);

        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(), parameters);
        exeWebServer.setLoaderConfig(getActContext(), true, generalFunc);
        exeWebServer.setDataResponseListener(responseString -> {
            JSONObject responseStringObject = generalFunc.getJsonObject(responseString);

            if (responseStringObject != null && !responseStringObject.equals("")) {
                boolean isDataAvail = GeneralFunctions.checkDataAvail(Utils.action_str, responseStringObject);

                if (isDataAvail) {
                    final GenerateAlertBox generateAlert = new GenerateAlertBox(getActContext());
                    generateAlert.setCancelable(false);
                    generateAlert.setBtnClickList(btn_id -> {
                        generateAlert.closeAlertBox();
                        setResult(RESULT_OK);
                        backImgView.performClick();
                    });

                    generateAlert.setContentMessage("", generalFunc.retrieveLangLBl("", generalFunc.getJsonValueStr(Utils.message_str, responseStringObject)));
                    generateAlert.setPositiveBtn(generalFunc.retrieveLangLBl("", "LBL_BTN_OK_TXT"));

                    generateAlert.showAlertBox();

                } else {
                    generalFunc.showGeneralMessage("",
                            generalFunc.retrieveLangLBl("", generalFunc.getJsonValueStr(Utils.message_str, responseStringObject)));
                }
            } else {
                generalFunc.showError();
            }
        });
        exeWebServer.execute();
    }

    public class DecimalDigitsInputFilter implements InputFilter {

        Pattern mPattern;

        public DecimalDigitsInputFilter(int digitsBeforeZero, int digitsAfterZero) {
            mPattern = Pattern.compile("[0-9]{0," + (digitsBeforeZero - 1) + "}+((\\.[0-9]{0," + (digitsAfterZero - 1) + "})?)||(\\.)?");
        }

        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            Matcher matcher = mPattern.matcher(dest);
            if (!matcher.matches())
                return "";
            return null;
        }

    }

    public void mangeMinusView(AutoFitEditText rechargeBox) {
        if (Utils.checkText(rechargeBox) == true && GeneralFunctions.parseDoubleValue(0, rechargeBox.getText().toString()) > 0) {

            rechargeBox.setText(String.format("%.2f", (double) (GeneralFunctions.parseDoubleValue(0.0, rechargeBox.getText().toString()) - 1)));


        } else {
            // rechargeBox.setText(defaultAmountVal);
        }
    }


    public void mangePluseView(AutoFitEditText rechargeBox) {
        if (Utils.checkText(rechargeBox) == true) {

            rechargeBox.setText(String.format("%.2f", (double) (GeneralFunctions.parseDoubleValue(0.0, rechargeBox.getText().toString()) + 1)));


        } else {
            rechargeBox.setText("1.00");


        }
    }
}
