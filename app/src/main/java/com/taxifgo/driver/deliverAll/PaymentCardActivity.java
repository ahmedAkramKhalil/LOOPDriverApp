package com.taxifgo.driver.deliverAll;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ImageView;

import com.taxifgo.driver.R;
import com.general.files.ExecuteWebServerUrl;
import com.general.files.GeneralFunctions;
import com.general.files.MyApp;
import com.stripe.android.Stripe;
import com.stripe.android.TokenCallback;
import com.stripe.android.model.Card;
import com.stripe.android.model.Token;
import com.utils.ModelUtils;
import com.utils.Utils;
import com.view.GenerateAlertBox;
import com.view.MButton;
import com.view.MTextView;
import com.view.MaterialRippleLayout;
import com.view.MyProgressDialog;
import com.view.editBox.MaterialEditText;

import org.json.JSONObject;

import java.util.Calendar;
import java.util.HashMap;

public class PaymentCardActivity extends AppCompatActivity {


    ImageView backImgView;
    MTextView titleTxt;
    GeneralFunctions generalFunc;

    MaterialEditText creditCardBox;
    MaterialEditText cvvBox;
    MaterialEditText mmBox;
    MaterialEditText yyBox;
    String required_str = "";
    MButton btn_type2;
    String userProfileJson;
    private static final int SEL_CARD = 001;
    String iOrderId = "";
    boolean isWallet = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_card);

        generalFunc = MyApp.getInstance().getGeneralFun(getActContext());
        userProfileJson = generalFunc.retrieveValue(Utils.USER_PROFILE_JSON);
        titleTxt = (MTextView) findViewById(R.id.titleTxt);
        backImgView = (ImageView) findViewById(R.id.backImgView);
        backImgView.setOnClickListener(new setOnClickList());
        creditCardBox = (MaterialEditText) findViewById(R.id.creditCardBox);
        cvvBox = (MaterialEditText) findViewById(R.id.cvvBox);
        mmBox = (MaterialEditText) findViewById(R.id.mmBox);
        yyBox = (MaterialEditText) findViewById(R.id.yyBox);
        btn_type2 = ((MaterialRippleLayout) findViewById(R.id.btn_type2)).getChildView();
        btn_type2.setId(Utils.generateViewId());
        btn_type2.setOnClickListener(new setOnClickList());
        isWallet = getIntent().getBooleanExtra("isWallet", false);

        if (getIntent().getStringExtra("iOrderId") != null && !getIntent().getStringExtra("iOrderId").equalsIgnoreCase("")) {
            iOrderId = getIntent().getStringExtra("iOrderId");
        }


        setLabels();

        mmBox.setInputType(InputType.TYPE_CLASS_NUMBER);
        yyBox.setInputType(InputType.TYPE_CLASS_NUMBER);
        cvvBox.setInputType(InputType.TYPE_CLASS_NUMBER);
        creditCardBox.setInputType(InputType.TYPE_CLASS_PHONE);
        int maxLength = 24;
        int monthmaxLegth = 2;
        int cvvMaxLegth = 5;
        int yearMaxLength = 4;
        creditCardBox.setFilters(new InputFilter[]{new InputFilter.LengthFilter(maxLength)});
        mmBox.setFilters(new InputFilter[]{new InputFilter.LengthFilter(monthmaxLegth)});
        cvvBox.setFilters(new InputFilter[]{new InputFilter.LengthFilter(cvvMaxLegth)});
        yyBox.setFilters(new InputFilter[]{new InputFilter.LengthFilter(yearMaxLength)});
        creditCardBox.addTextChangedListener(new TextWatcher() {
            private static final char space = ' ';

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0 && (s.length() % 5) == 0) {
                    final char c = s.charAt(s.length() - 1);
                    if (space == c) {
                        s.delete(s.length() - 1, s.length());
                    }
                }
                // Insert char where needed.
                if (s.length() > 0 && (s.length() % 5) == 0) {
                    char c = s.charAt(s.length() - 1);
                    // Only if its a digit where there should be a space we insert a space
                    if (Character.isDigit(c) && TextUtils.split(s.toString(), String.valueOf(space)).length <= 3) {
                        s.insert(s.length() - 1, String.valueOf(space));
                    }
                }

            }
        });


    }

    public MyProgressDialog showLoader() {
        MyProgressDialog myPDialog = new MyProgressDialog(getActContext(), false, generalFunc.retrieveLangLBl("Loading", "LBL_LOADING_TXT"));
        myPDialog.show();

        return myPDialog;
    }


    public void setLabels() {
        creditCardBox.setBothText(generalFunc.retrieveLangLBl("", "LBL_CARD_NUMBER_HEADER_TXT"), generalFunc.retrieveLangLBl("", "LBL_CARD_NUMBER_HINT_TXT"));
        cvvBox.setBothText(generalFunc.retrieveLangLBl("", "LBL_CVV_HEADER_TXT"), generalFunc.retrieveLangLBl("", "LBL_CVV_HINT_TXT"));
        mmBox.setBothText(generalFunc.retrieveLangLBl("", "LBL_EXP_MONTH_HINT_TXT"), generalFunc.retrieveLangLBl("", "LBL_EXP_MONTH_HINT_TXT"));
        yyBox.setBothText(generalFunc.retrieveLangLBl("", "LBL_EXP_YEAR_HINT_TXT"), generalFunc.retrieveLangLBl("", "LBL_EXP_YEAR_HINT_TXT"));
        required_str = generalFunc.retrieveLangLBl("", "LBL_FEILD_REQUIRD");
        titleTxt.setText(generalFunc.retrieveLangLBl("", "LBL_ADD_CARD"));
        btn_type2.setText(generalFunc.retrieveLangLBl("", "LBL_ADD_CARD"));
    }


    public Context getActContext() {
        return PaymentCardActivity.this;
    }

    public void checkDetails() {

        Card card = new Card(Utils.getText(creditCardBox), generalFunc.parseIntegerValue(0, Utils.getText(mmBox)),
                generalFunc.parseIntegerValue(0, Utils.getText(yyBox)), Utils.getText(cvvBox));

        boolean cardNoEntered = Utils.checkText(creditCardBox) ? (card.validateNumber() ? true :
                Utils.setErrorFields(creditCardBox, generalFunc.retrieveLangLBl("", "LBL_INVALID")))
                : Utils.setErrorFields(creditCardBox, required_str);
        boolean cvvEntered = Utils.checkText(cvvBox) ? (card.validateCVC() ? true :
                Utils.setErrorFields(cvvBox, generalFunc.retrieveLangLBl("", "LBL_INVALID"))) : Utils.setErrorFields(cvvBox, required_str);
        boolean monthEntered = Utils.checkText(mmBox) ? (card.validateExpMonth() ? true :
                Utils.setErrorFields(mmBox, generalFunc.retrieveLangLBl("", "LBL_INVALID"))) : Utils.setErrorFields(mmBox, required_str);
        boolean yearEntered = Utils.checkText(yyBox) ? (validateExpYear(Calendar.getInstance()) ? true :
                Utils.setErrorFields(yyBox, generalFunc.retrieveLangLBl("", "LBL_INVALID"))) : Utils.setErrorFields(yyBox, required_str);
        boolean yearEntedcount = true;
        if (yearEntered == true) {
            yearEntedcount = yyBox.getText().toString().length() == 4 ? true : Utils.setErrorFields(yyBox, generalFunc.retrieveLangLBl("", "LBL_INVALID"));
        }

        if (cardNoEntered == false || cvvEntered == false || monthEntered == false || yearEntered == false || yearEntedcount == false) {
            return;
        }


        if (card.validateCard()) {
            generateToken(card);
        }
    }

    public boolean validateExpYear(Calendar now) {
        return yyBox.getText().toString() != null && !ModelUtils.hasYearPassed(GeneralFunctions.parseIntegerValue(0, yyBox.getText().toString()), now);
    }

    public void generateToken(final Card card) {

        final MyProgressDialog myPDialog = showLoader();

        String STRIPE_PUBLISH_KEY = generalFunc.getJsonValue("STRIPE_PUBLISH_KEY", userProfileJson);
        Stripe stripe = new Stripe(getActContext());

        stripe.createToken(card, STRIPE_PUBLISH_KEY, new TokenCallback() {
            public void onSuccess(Token token) {
                // TODO: Send Token information to your backend to initiate a charge
                myPDialog.close();
                if (isWallet) {
                    addMoneyToWallet(token.getId());
                }
            }

            public void onError(Exception error) {
                myPDialog.close();
                generalFunc.showError();
            }
        });
    }


    private void addMoneyToWallet(String token) {
        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "addMoneyUserWalletByChargeCard");
        parameters.put("iMemberId", generalFunc.getMemberId());
        parameters.put("fAmount", getIntent().getStringExtra("fAmount"));
        parameters.put("UserType", Utils.app_type);
        parameters.put("vStripeToken", token);
        parameters.put("eSystem", Utils.eSystem_Type);

        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(), parameters);
        exeWebServer.setLoaderConfig(getActContext(), true, generalFunc);
        exeWebServer.setDataResponseListener(responseString -> {
            JSONObject responseStringObject=generalFunc.getJsonObject(responseString);

            if (responseStringObject != null && !responseStringObject.equals("")) {

                boolean isDataAvail = GeneralFunctions.checkDataAvail(Utils.action_str, responseStringObject);

                if (isDataAvail) {


                    GenerateAlertBox generateAlertBox = new GenerateAlertBox(getActContext());
                    generateAlertBox.setCancelable(false);
                    generateAlertBox.setContentMessage("", generalFunc.retrieveLangLBl("", generalFunc.getJsonValueStr(Utils.message_str_one, responseStringObject)));

                    generateAlertBox.setPositiveBtn(generalFunc.retrieveLangLBl("", "LBL_OK"));
                    generateAlertBox.setBtnClickList(new GenerateAlertBox.HandleAlertBtnClick() {
                        @Override
                        public void handleBtnClick(int btn_id) {
                            Intent returnIntent = new Intent();
                            setResult(Activity.RESULT_OK, returnIntent);
                            finish();
                            generateAlertBox.closeAlertBox();

                        }
                    });
                    generateAlertBox.showAlertBox();
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


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SEL_CARD) {
            if (resultCode == RESULT_OK) {

            } else {
                onBackPressed();
            }

        }
    }

    public class setOnClickList implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            Utils.hideKeyboard(getActContext());
            int i = view.getId();
            if (i == R.id.backImgView) {
                onBackPressed();
            } else if (i == btn_type2.getId()) {
                checkDetails();
            }
        }
    }

}
