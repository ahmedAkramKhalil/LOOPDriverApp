package com.taxifgo.driver;

import android.content.Context;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.text.InputType;
import android.view.View;
import android.widget.ImageView;

import com.general.files.ExecuteWebServerUrl;
import com.general.files.GeneralFunctions;
import com.general.files.MyApp;
import com.utils.Utils;
import com.view.GenerateAlertBox;
import com.view.MButton;
import com.view.MTextView;
import com.view.MaterialRippleLayout;
import com.view.editBox.MaterialEditText;

import org.json.JSONObject;

import java.util.HashMap;

public class BankDetailActivity extends AppCompatActivity {

    GeneralFunctions generalFunc;
    MButton submitBtn;
    ImageView backImgView;
    MTextView titleTxt;

    MaterialEditText vPaymentEmail, vBankAccountHolderName, vAccountNumber, vBankLocation, vBankName, vBIC_SWIFT_Code;
    String required_str = "";
    String error_email_str = "";

    View loadingBar;
    View contentArea;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bank_detail);

        generalFunc = MyApp.getInstance().getGeneralFun(getActContext());
        submitBtn = ((MaterialRippleLayout) findViewById(R.id.submitBtn)).getChildView();

        titleTxt = (MTextView) findViewById(R.id.titleTxt);
        backImgView = (ImageView) findViewById(R.id.backImgView);

        loadingBar = findViewById(R.id.loadingBar);
        contentArea = findViewById(R.id.contentArea);

        vPaymentEmail = (MaterialEditText) findViewById(R.id.vPaymentEmailBox);
        vBankAccountHolderName = (MaterialEditText) findViewById(R.id.vBankAccountHolderNameBox);
        vAccountNumber = (MaterialEditText) findViewById(R.id.vAccountNumberBox);
        vBankLocation = (MaterialEditText) findViewById(R.id.vBankLocation);
        vBankName = (MaterialEditText) findViewById(R.id.vBankName);
        vBIC_SWIFT_Code = (MaterialEditText) findViewById(R.id.vBIC_SWIFT_Code);

        vAccountNumber.setInputType(InputType.TYPE_CLASS_NUMBER);
        setData();
        submitBtn.setId(Utils.generateViewId());

        submitBtn.setOnClickListener(new setOnClickList());
        backImgView.setOnClickListener(new setOnClickList());
        isBankDetailDisplay("", "", "", "", "", "", "Yes", false);
    }

    private void setData() {

        titleTxt.setText(generalFunc.retrieveLangLBl("", "LBL_BANK_DETAILS_TXT"));
        submitBtn.setText(generalFunc.retrieveLangLBl("", "LBL_BTN_SUBMIT_TXT"));

        vPaymentEmail.setBothText(generalFunc.retrieveLangLBl("", "LBL_PAYMENT_EMAIL_TXT"));
        vBankAccountHolderName.setBothText(generalFunc.retrieveLangLBl("", "LBL_PROFILE_BANK_HOLDER_TXT"));
        vAccountNumber.setBothText(generalFunc.retrieveLangLBl("", "LBL_ACCOUNT_NUMBER"));
        vBankLocation.setBothText(generalFunc.retrieveLangLBl("", "LBL_BANK_LOCATION"));
        vBankName.setBothText(generalFunc.retrieveLangLBl("", "LBL_BANK_NAME"));
        vBIC_SWIFT_Code.setBothText(generalFunc.retrieveLangLBl("", "LBL_BIC_SWIFT_CODE"));

        required_str = generalFunc.retrieveLangLBl("", "LBL_FEILD_REQUIRD");
        error_email_str = generalFunc.retrieveLangLBl("", "LBL_FEILD_EMAIL_ERROR");

//        vAccountNumber.setInputType(InputType.TYPE_CLASS_NUMBER);
        vPaymentEmail.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS | InputType.TYPE_CLASS_TEXT);

    }

    private void isBankDetailDisplay(String vPaymentEmail, String vBankAccountHolderName, String vAccountNumber, String vBankLocation, String vBankName,
                                     String vBIC_SWIFT_Code, String eDisplay, final boolean isAlert) {

        if (eDisplay.equalsIgnoreCase("Yes")) {
            contentArea.setVisibility(View.GONE);
            loadingBar.setVisibility(View.VISIBLE);
        }

        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "DriverBankDetails");
        parameters.put("iDriverId", generalFunc.getMemberId());
        parameters.put("userType", Utils.APP_TYPE);
        parameters.put("vPaymentEmail", vPaymentEmail);
        parameters.put("vBankAccountHolderName", vBankAccountHolderName);
        parameters.put("vAccountNumber", vAccountNumber);
        parameters.put("vBankLocation", vBankLocation);
        parameters.put("vBankName", vBankName);
        parameters.put("vBIC_SWIFT_Code", vBIC_SWIFT_Code);
        parameters.put("eDisplay", eDisplay);

        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(), parameters);
        exeWebServer.setLoaderConfig(getActContext(), loadingBar.getVisibility() != View.VISIBLE, generalFunc);
        exeWebServer.setIsDeviceTokenGenerate(true, "vDeviceToken", generalFunc);
        exeWebServer.setDataResponseListener(responseString -> {
            JSONObject responseStringObject=generalFunc.getJsonObject(responseString);

            if (responseStringObject != null && !responseStringObject.equals("")) {

                boolean isDataAvail = GeneralFunctions.checkDataAvail(Utils.action_str, responseStringObject);
                if (isDataAvail) {
                    JSONObject msg_obj = generalFunc.getJsonObject("message", responseStringObject);

                    String vPaymentEmail1 = generalFunc.getJsonValueStr("vPaymentEmail", msg_obj);
                    String vBankAccountHolderName1 = generalFunc.getJsonValueStr("vBankAccountHolderName", msg_obj);
                    String vAccountNumber1 = generalFunc.getJsonValueStr("vAccountNumber", msg_obj);
                    String vBankLocation1 = generalFunc.getJsonValueStr("vBankLocation", msg_obj);
                    String vBankName1 = generalFunc.getJsonValueStr("vBankName", msg_obj);
                    String vBIC_SWIFT_Code1 = generalFunc.getJsonValueStr("vBIC_SWIFT_Code", msg_obj);

                    if (!vPaymentEmail1.equals("")) {
                        ((MaterialEditText) findViewById(R.id.vPaymentEmailBox)).setText(vPaymentEmail1);
                    }
                    if (!vBankAccountHolderName1.equals("")) {
                        ((MaterialEditText) findViewById(R.id.vBankAccountHolderNameBox)).setText(vBankAccountHolderName1);
                    }
                    if (!vAccountNumber1.equals("")) {
                        ((MaterialEditText) findViewById(R.id.vAccountNumberBox)).setText(vAccountNumber1);
                    }
                    if (!vBankLocation1.equals("")) {
                        ((MaterialEditText) findViewById(R.id.vBankLocation)).setText(vBankLocation1);
                    }
                    if (!vBankName1.equals("")) {
                        ((MaterialEditText) findViewById(R.id.vBankName)).setText(vBankName1);
                    }
                    if (!vBIC_SWIFT_Code1.equals("")) {
                        ((MaterialEditText) findViewById(R.id.vBIC_SWIFT_Code)).setText(vBIC_SWIFT_Code1);
                    }


                    if (isAlert) {
                        GenerateAlertBox alertBox = new GenerateAlertBox(getActContext());
                        alertBox.setContentMessage("", generalFunc.retrieveLangLBl("", "LBL_BANK_DETAILS_UPDATED"));
                        alertBox.setPositiveBtn(generalFunc.retrieveLangLBl("", "LBL_BTN_OK_GENERAL"));
                        alertBox.setBtnClickList(btn_id -> {
                            if (btn_id == 1) {
                                BankDetailActivity.super.onBackPressed();
                            }
                        });
                        alertBox.showAlertBox();
                    }
                } else {

                }
            } else {
                generalFunc.showError();
            }

            contentArea.setVisibility(View.VISIBLE);
            loadingBar.setVisibility(View.GONE);
        });
        exeWebServer.execute();
    }

    public Context getActContext() {
        return BankDetailActivity.this;
    }

    public class setOnClickList implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            int i = view.getId();

            if (i == submitBtn.getId()) {
                checkData();
            } else if (i == R.id.backImgView) {
                BankDetailActivity.this.onBackPressed();
            }
        }
    }

    private void checkData() {

        boolean isPaymentEmail = Utils.checkText(vPaymentEmail) ? generalFunc.isEmailValid(Utils.getText(vPaymentEmail)) ? true : Utils.setErrorFields(vPaymentEmail, error_email_str) : Utils.setErrorFields(vPaymentEmail, required_str);

        boolean isSwiftCode = Utils.checkText(vBIC_SWIFT_Code) ? true : Utils.setErrorFields(vBIC_SWIFT_Code, required_str);
        boolean isAccountNumber = Utils.checkText(vAccountNumber) ? true : Utils.setErrorFields(vAccountNumber, required_str);
        boolean isBankAccountHolderName = Utils.checkText(vBankAccountHolderName) ? true : Utils.setErrorFields(vBankAccountHolderName, required_str);
        boolean isBankName = Utils.checkText(vBankName) ? true : Utils.setErrorFields(vBankName, required_str);
        boolean isBankLocation = Utils.checkText(vBankLocation) ? true : Utils.setErrorFields(vBankLocation, required_str);

        if (isPaymentEmail == false || isBankAccountHolderName == false || isAccountNumber == false || isBankLocation == false || isBankName == false || isSwiftCode == false) {
            return;
        }

        isBankDetailDisplay(Utils.getText(vPaymentEmail), Utils.getText(vBankAccountHolderName), Utils.getText(vAccountNumber),
                Utils.getText(vBankLocation), Utils.getText(vBankName), Utils.getText(vBIC_SWIFT_Code), "No", true);


    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
