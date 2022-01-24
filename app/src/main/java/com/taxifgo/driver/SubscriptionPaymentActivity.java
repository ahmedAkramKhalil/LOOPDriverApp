package com.taxifgo.driver;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatCheckBox;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RadioButton;

import com.general.files.AppFunctions;
import com.general.files.ExecuteWebServerUrl;
import com.general.files.GeneralFunctions;
import com.general.files.MyApp;
import com.general.files.StartActProcess;
import com.utils.Logger;
import com.utils.Utils;
import com.view.CreateRoundedView;
import com.view.GenerateAlertBox;
import com.view.MTextView;
import com.view.anim.loader.AVLoadingIndicatorView;
import com.view.editBox.MaterialEditText;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;

public class SubscriptionPaymentActivity extends AppCompatActivity {
    public GeneralFunctions generalFunc;
    public JSONObject userProfileJsonObj;
    String SYSTEM_PAYMENT_FLOW;
    String APP_PAYMENT_MODE;
    String APP_PAYMENT_METHOD;

    MTextView titleTxt;
    MTextView subscriptionDesTxt;
    MTextView walletBalanceTxt;
    MTextView walletBalanceValTxt;
    MTextView planNameTxt;
    MTextView planPriceTxt;
    MTextView planPriceHTxt;
    MTextView planNameHTxt;
    MTextView cardPaymentTxt;
    MTextView subScribeBtnTxt;
    ImageView backImgView;

    AppCompatCheckBox cb_wallet;
    RadioButton cardPaymentRadioBtn;
    private HashMap<String, String> planDetails;

    int TRANSACTION_COMPLETED = 12345;
    int WALLET_MONEY_ADDED = 12789;

    AVLoadingIndicatorView loaderView;
    WebView paymentWebview;
    private boolean isCardValidated;
    AlertDialog cashBalAlertDialog;
    String isRenew="";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.subcription_payment_activity);
        generalFunc = MyApp.getInstance().getGeneralFun(getActContext());

        getUserPeofileJson(generalFunc.getJsonObject(generalFunc.retrieveValue(Utils.USER_PROFILE_JSON)));

        planDetails = (HashMap<String, String>) getIntent().getSerializableExtra("PlanDetails");
        isRenew = getIntent().hasExtra("isRenew")?getIntent().getStringExtra("isRenew"):"";

        initView();
        setLables();

        ((ImageView)findViewById(R.id.iv_icon)).setImageResource(R.mipmap.ic_tip);


        new CreateRoundedView(getActContext().getResources().getColor(R.color.appThemeColor_1), Utils.dipToPixels(getActContext(), 20), 2,
                getActContext().getResources().getColor(R.color.light_back_color), findViewById(R.id.subScribeBtnTxt));

        managePaymentMethod();




        cardPaymentRadioBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {


            }
        });

        cb_wallet.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                findViewById(R.id.iv_wallet).setVisibility(isChecked ? View.VISIBLE : View.GONE);
            }
        });

        setValues();
    }

    private void getUserPeofileJson(JSONObject object) {
        userProfileJsonObj = object;
        APP_PAYMENT_MODE = generalFunc.getJsonValueStr("APP_PAYMENT_MODE", userProfileJsonObj);
        APP_PAYMENT_METHOD = generalFunc.getJsonValueStr("APP_PAYMENT_METHOD", userProfileJsonObj);
        SYSTEM_PAYMENT_FLOW = generalFunc.getJsonValueStr("SYSTEM_PAYMENT_FLOW", userProfileJsonObj);


    }

    private void setValues() {
        String htmlString = "<b><font color=" + getActContext().getResources().getColor(R.color.black) + ">" + generalFunc.retrieveLangLBl("", "LBL_SUB_NOTE_TXT") + ": " + "</font></b>";
        ((MTextView)findViewById(R.id.noteText)).setText(AppFunctions.fromHtml(htmlString+generalFunc.retrieveLangLBl("", "LBL_UPGRADE_NOTE_TXT")));



        planNameHTxt.setText(generalFunc.retrieveLangLBl("", "LBL_SUBSCRIPTION_PLAN_NAME"));
        planNameTxt.setText(": "+planDetails.get("vPlanName"));

        planPriceHTxt.setText(generalFunc.retrieveLangLBl("", "LBL_SUB_PLAN_PRICE_TXT"));
        planPriceTxt.setText(": "+planDetails.get("fPlanPrice"));

        walletBalanceValTxt.setText(generalFunc.convertNumberWithRTL(generalFunc.convertNumberWithRTL(generalFunc.getJsonValueStr("user_available_balance", userProfileJsonObj))));
    }

    @Override
    protected void onResume() {
        super.onResume();

        //if (generalFunc.retrieveValue(Utils.ISWALLETBALNCECHANGE).equalsIgnoreCase("Yes")) {
            getWalletBalDetails();
       // }
    }

    private void managePaymentMethod() {

        if (SYSTEM_PAYMENT_FLOW.equalsIgnoreCase("Method-1")) {
            if (APP_PAYMENT_MODE.equalsIgnoreCase("Cash-Card") ||
                    APP_PAYMENT_MODE.equalsIgnoreCase("Card")) {
                findViewById(R.id.cardArea).setVisibility(View.VISIBLE);
            }
        } else if (!SYSTEM_PAYMENT_FLOW.equalsIgnoreCase("Method-1")) {
            findViewById(R.id.cardArea).setVisibility(View.GONE);
        }
    }

    private void setLables() {
        titleTxt.setText(generalFunc.retrieveLangLBl("", "LBL_SELECT_PAYMENT_METHOD_TXT"));
        subscriptionDesTxt.setText(generalFunc.retrieveLangLBl("", "LBL_SELECT_PAYMENT_METHOD_DESC_TXT"));
        subScribeBtnTxt.setText(generalFunc.retrieveLangLBl("", "LBL_SUBSCRIPTION_TXT"));
        walletBalanceTxt.setText(generalFunc.retrieveLangLBl("", "LBL_USE_WALLET_BALANCE"));
        cardPaymentTxt.setText(generalFunc.retrieveLangLBl("", "LBL_CARD"));
        cardPaymentRadioBtn.setText(generalFunc.retrieveLangLBl("", "LBL_CARD"));

        findViewById(R.id.walletArea).setOnClickListener(new setOnClickList());
        findViewById(R.id.cardArea).setOnClickListener(new setOnClickList());
    }

    private void initView() {
        titleTxt = findViewById(R.id.titleTxt);
        backImgView = findViewById(R.id.backImgView);

        subscriptionDesTxt = findViewById(R.id.subscriptionDesTxt);
        subScribeBtnTxt = findViewById(R.id.subScribeBtnTxt);
        cardPaymentRadioBtn = findViewById(R.id.cardPaymentRadioBtn);
        cb_wallet = findViewById(R.id.cb_wallet);
        walletBalanceTxt = findViewById(R.id.walletBalanceTxt);
        walletBalanceValTxt = findViewById(R.id.walletBalanceValTxt);
        planNameTxt = findViewById(R.id.planNameTxt);
        planNameHTxt = findViewById(R.id.planNameHTxt);
        planPriceTxt = findViewById(R.id.planPriceTxt);
        planPriceHTxt = findViewById(R.id.planPriceHTxt);
        cardPaymentTxt = findViewById(R.id.cardPaymentTxt);

        paymentWebview = (WebView) findViewById(R.id.paymentWebview);
        loaderView = (AVLoadingIndicatorView) findViewById(R.id.loaderView);


        backImgView.setOnClickListener(new setOnClickList());
        subScribeBtnTxt.setOnClickListener(new setOnClickList());
    }

    public class setOnClickList implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            Utils.hideKeyboard(SubscriptionPaymentActivity.this);
            int i = view.getId();
            if (i == R.id.backImgView) {
                SubscriptionPaymentActivity.super.onBackPressed();
            } else if (i == R.id.subScribeBtnTxt) {
                checkValues();
            } else if (i == R.id.walletArea) {
                cb_wallet.setChecked(!cb_wallet.isChecked());
            } else if (i == R.id.cardArea) {
                cardPaymentRadioBtn.setChecked(!cardPaymentRadioBtn.isChecked());
            }
        }
    }

    private void checkValues() {

        if (!cb_wallet.isChecked() && !cardPaymentRadioBtn.isChecked()) {
            generalFunc.showMessage(subScribeBtnTxt, generalFunc.retrieveLangLBl("", "LBL_SELECT_PAYMENT_METHOD_DESC_TXT"));
            return;
        }

        if (SYSTEM_PAYMENT_FLOW.equalsIgnoreCase("Method-1") && cardPaymentRadioBtn.isChecked() && !isCardValidated) {
            checkCardData();
        }else
        {
            confirmSubscription();
        }

    }

    public void checkCardData() {
        if (APP_PAYMENT_METHOD.equalsIgnoreCase("Stripe")) {
            String vStripeCusId = generalFunc.getJsonValueStr("vStripeCusId", userProfileJsonObj);
            if (vStripeCusId.equals("")) {
                OpenCardPaymentAct(true);
            } else {
                showPaymentBox();
            }
        }

    }


    public void showPaymentBox() {
        androidx.appcompat.app.AlertDialog alertDialog;
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getActContext());
        builder.setTitle("");
        builder.setCancelable(false);
        LayoutInflater inflater = (LayoutInflater) getActContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View dialogView = inflater.inflate(R.layout.input_box_view, null);
        builder.setView(dialogView);

        final MaterialEditText input = (MaterialEditText) dialogView.findViewById(R.id.editBox);
        final MTextView subTitleTxt = (MTextView) dialogView.findViewById(R.id.subTitleTxt);

        Utils.removeInput(input);

        subTitleTxt.setVisibility(View.VISIBLE);
        subTitleTxt.setText(generalFunc.retrieveLangLBl("", "LBL_TITLE_PAYMENT_ALERT"));
        input.setText(generalFunc.getJsonValueStr("vCreditCard", userProfileJsonObj));

        builder.setPositiveButton(generalFunc.retrieveLangLBl("Confirm", "LBL_BTN_TRIP_CANCEL_CONFIRM_TXT"), (dialog, which) -> {
            dialog.cancel();

                checkPaymentCard();
        });
        builder.setNeutralButton(generalFunc.retrieveLangLBl("Change", "LBL_CHANGE"), (dialog, which) -> {
            dialog.cancel();
            OpenCardPaymentAct(true);

        });
        builder.setNegativeButton(generalFunc.retrieveLangLBl("Cancel", "LBL_CANCEL_TXT"), (dialog, which) -> {
            dialog.cancel();

        });


        alertDialog = builder.create();
        alertDialog.setCancelable(false);
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();
    }

    public void checkPaymentCard() {
        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "CheckCard");
        parameters.put("iUserId", generalFunc.getMemberId());

        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(), parameters);
        exeWebServer.setLoaderConfig(getActContext(), true, generalFunc);
        exeWebServer.setDataResponseListener(responseString -> {
            JSONObject responseStringObject=generalFunc.getJsonObject(responseString);

            if (responseStringObject != null && !responseStringObject.equals("")) {

                String action = generalFunc.getJsonValueStr(Utils.action_str, responseStringObject);
                if (action.equals("1")) {

                    isCardValidated = true;
                    confirmSubscription();
                } else {
                    generalFunc.showGeneralMessage("", generalFunc.retrieveLangLBl("", generalFunc.getJsonValueStr(Utils.message_str, responseStringObject)));
                }
            } else {
                generalFunc.showError();
            }
        });
        exeWebServer.execute();
    }

    private void confirmSubscription() {
        final GenerateAlertBox generateAlert = new GenerateAlertBox(getActContext());
        generateAlert.setCancelable(false);
        generateAlert.setBtnClickList(btn_id -> {
            if (btn_id == 0) {
                generateAlert.closeAlertBox();

            } else {
                generateAlert.closeAlertBox();
                subscribePlan("");
            }

        });
        generateAlert.setContentMessage("", generalFunc.retrieveLangLBl("", "LBL_ENABLE_SUBSCRIPTION_NOTE"));
        generateAlert.setPositiveBtn(generalFunc.retrieveLangLBl("", "LBL_YES"));
        generateAlert.setNegativeBtn(generalFunc.retrieveLangLBl("", "LBL_NO"));
        generateAlert.showAlertBox();

    }

    public void OpenCardPaymentAct(boolean fromcabselection) {
        Bundle bn = new Bundle();
        // bn.putString("UserProfileJson", userProfileJson);
        bn.putBoolean("fromcabselection", fromcabselection);
        new StartActProcess(getActContext()).startActForResult(CardPaymentActivity.class, bn, Utils.CARD_PAYMENT_REQ_CODE);
    }


    private void subscribePlan(String isUpgrade) {
        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "SubscribePlan");
        parameters.put("iDriverId", generalFunc.getMemberId());
        parameters.put("UserType", Utils.app_type);
        parameters.put("isCard", cardPaymentRadioBtn.isChecked() ? "Yes" : "No");
        parameters.put("isWallet", cb_wallet.isChecked() ? "Yes" : "No");
        parameters.put("iDriverSubscriptionPlanId", planDetails.get("iDriverSubscriptionPlanId"));

        if (isUpgrade.equalsIgnoreCase("Yes")) {
            parameters.put("isUpgrade",isUpgrade);
        }

        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(), parameters);
        exeWebServer.setLoaderConfig(getActContext(), true, generalFunc);
        exeWebServer.setCancelAble(false);
        exeWebServer.setDataResponseListener(responseString -> {
            JSONObject responseStringObject=generalFunc.getJsonObject(responseString);

            if (responseString != null && !responseString.equals("")) {

                if (generalFunc.checkDataAvail(Utils.action_str, responseStringObject)) {

                    String message = generalFunc.getJsonValueStr(Utils.message_str, responseStringObject);
                    String isUpgradeStr = generalFunc.getJsonValueStr("isUpgrade", responseStringObject);
                    String loadWebView = generalFunc.getJsonValueStr("loadWebView", responseStringObject);

                    if (isUpgradeStr.equalsIgnoreCase("Yes"))
                    {
                        final GenerateAlertBox generateAlert = new GenerateAlertBox(getActContext());
                        generateAlert.setCancelable(false);
                        generateAlert.setBtnClickList(btn_id -> {
                            if (btn_id == 0) {
                                generateAlert.closeAlertBox();

                            } else {
                                generateAlert.closeAlertBox();
                                subscribePlan(isUpgradeStr);

                            }

                        });
                        generateAlert.setContentMessage("", generalFunc.retrieveLangLBl("", generalFunc.getJsonValue(Utils.message_str, responseString)));
                        generateAlert.setPositiveBtn(generalFunc.retrieveLangLBl("", "LBL_YES"));
                        generateAlert.setNegativeBtn(generalFunc.retrieveLangLBl("", "LBL_NO"));
                        generateAlert.showAlertBox();

                    }else {
                        isCardValidated = false;
                        //LBL_LOW_WALLET_BAL_NOTE
                        if (loadWebView.equalsIgnoreCase("Yes")) {
                            paymentWebview.setWebViewClient(new myWebClient());
                            paymentWebview.getSettings().setJavaScriptEnabled(true);
                            paymentWebview.loadUrl(message);
                            paymentWebview.setFocusable(true);
                            paymentWebview.setVisibility(View.VISIBLE);
                            loaderView.setVisibility(View.VISIBLE);
                        } else {
                            redirectToThankYouScreen();
                        }
                    }

                } else {

                    if (generalFunc.getJsonValue(Utils.message_str, responseString).equalsIgnoreCase("LBL_LOW_WALLET_BAL_NOTE")) {
                        String  walletMsg = generalFunc.retrieveLangLBl("", "LBL_LOW_WALLET_BAL_NOTE");
                        Bundle bn=new Bundle();
                        bn.putString("isFrom","SubscriptionPayment");
                        buildLowBalanceMessage(getActContext(),walletMsg,bn);
                    }
                    else {
                        generalFunc.showMessage(subScribeBtnTxt, generalFunc.retrieveLangLBl("", generalFunc.getJsonValueStr(Utils.message_str, responseStringObject)));
                    }
                }
            } else {
                generalFunc.showError();
            }
        });
        exeWebServer.execute();
    }

    private void redirectToThankYouScreen() {
     //   generalFunc.storeData(Utils.ISWALLETBALNCECHANGE, "Yes");
        new StartActProcess(getActContext()).startActForResult(SubscribedPlanConfirmationActivity.class, TRANSACTION_COMPLETED);
    }



    public void buildLowBalanceMessage(final Context context, String message, final Bundle bn) {

        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(context);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View dialogView = inflater.inflate(R.layout.design_cash_balance_dialoge, null);
        builder.setView(dialogView);

        final MTextView addNowTxtArea = (MTextView) dialogView.findViewById(R.id.addNowTxtArea);
        final MTextView msgTxt = (MTextView) dialogView.findViewById(R.id.msgTxt);
        final MTextView skipTxtArea = (MTextView) dialogView.findViewById(R.id.skipTxtArea);
        final MTextView titileTxt = (MTextView) dialogView.findViewById(R.id.titileTxt);
        titileTxt.setText(generalFunc.retrieveLangLBl("", "LBL_LOW_BALANCE"));


        if (generalFunc.getJsonValueStr("APP_PAYMENT_MODE", userProfileJsonObj).equalsIgnoreCase("Cash")) {
            addNowTxtArea.setText(generalFunc.retrieveLangLBl("", "LBL_CONTACT_US_TXT"));
        } else {
            addNowTxtArea.setText(generalFunc.retrieveLangLBl("", "LBL_ADD_NOW"));
        }


        skipTxtArea.setText(generalFunc.retrieveLangLBl("", "LBL_CANCEL_TXT"));
        msgTxt.setText(message);


        skipTxtArea.setOnClickListener(view -> cashBalAlertDialog.dismiss());

        addNowTxtArea.setOnClickListener(view -> {
            cashBalAlertDialog.dismiss();
            if (generalFunc.getJsonValueStr("APP_PAYMENT_MODE", userProfileJsonObj).equalsIgnoreCase("Cash")) {
                new StartActProcess(context).startAct(ContactUsActivity.class);

            } else {
                new StartActProcess(context).startActForResult(MyWalletActivity.class, bn,WALLET_MONEY_ADDED);
            }

        });
        cashBalAlertDialog = builder.create();
        cashBalAlertDialog.setCancelable(false);
        if (generalFunc.isRTLmode() == true) {
            generalFunc.forceRTLIfSupported(cashBalAlertDialog);
        }
        cashBalAlertDialog.show();
    }

    public Context getActContext() {
        return SubscriptionPaymentActivity.this; // Must be context of activity not application
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == TRANSACTION_COMPLETED && resultCode == RESULT_OK) {
            Logger.d("DEBUG", "TRANSACTION_COMPLETED::PAYMENT" );
            Intent returnIntent = new Intent();
            setResult(Activity.RESULT_OK, returnIntent);
            finish();
        }else if (requestCode == WALLET_MONEY_ADDED && resultCode == RESULT_OK) {
            Logger.d("DEBUG", "WALLET_MONEY_ADDED::" );
            String userProfileJson = generalFunc.retrieveValue(Utils.USER_PROFILE_JSON);
            getUserPeofileJson(generalFunc.getJsonObject(userProfileJson));

            walletBalanceValTxt.setText(generalFunc.convertNumberWithRTL(generalFunc.convertNumberWithRTL(generalFunc.getJsonValue("user_available_balance", userProfileJson))));

        }else if (requestCode == Utils.CARD_PAYMENT_REQ_CODE && resultCode == RESULT_OK && data != null) {
            String userProfileJson = generalFunc.retrieveValue(Utils.USER_PROFILE_JSON);
            getUserPeofileJson(generalFunc.getJsonObject(userProfileJson));
            walletBalanceValTxt.setText(generalFunc.convertNumberWithRTL(generalFunc.convertNumberWithRTL(generalFunc.getJsonValue("user_available_balance", userProfileJson))));
            isCardValidated = true;
        }
    }

    public class myWebClient extends WebViewClient {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }


        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {

            String data = url;
            data = data.substring(data.indexOf("data") + 5, data.length());
            try {

                String datajson = URLDecoder.decode(data, "UTF-8");
                loaderView.setVisibility(View.VISIBLE);

                view.setOnTouchListener(null);

                if (url.contains("success=1")) {
                    paymentWebview.setVisibility(View.GONE);
                    generalFunc.showGeneralMessage("", generalFunc.retrieveLangLBl("", generalFunc.retrieveLangLBl("", "LBL_SUBSCRIBED_SUCCESFULLY_TXT")), "", generalFunc.retrieveLangLBl("", "LBL_OK"), i -> {
                        redirectToThankYouScreen();
                    });
                }

                if (url.contains("success=0")) {

                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

        }


        @Override
        public void onReceivedError(WebView view, int errorCode,
                                    String description, String failingUrl) {

            generalFunc.showError();
            loaderView.setVisibility(View.GONE);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            loaderView.setVisibility(View.GONE);

            view.setOnTouchListener((v, event) -> {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                    case MotionEvent.ACTION_UP:
                        if (!v.hasFocus()) {
                            v.requestFocus();
                        }
                        break;
                }
                return false;
            });

        }
    }


    @Override
    public void onBackPressed() {

        if (paymentWebview.getVisibility() == View.VISIBLE) {
            generalFunc.showGeneralMessage("", generalFunc.retrieveLangLBl("", "LBL_CANCEL_PAYMENT_PROCESS"), generalFunc.retrieveLangLBl("", "LBL_NO"), generalFunc.retrieveLangLBl("", "LBL_YES"), buttonId -> {
                if (buttonId == 1) {
                    paymentWebview.setVisibility(View.GONE);
                    paymentWebview.stopLoading();
                    loaderView.setVisibility(View.GONE);

                    cardPaymentRadioBtn.setChecked(false);
                    cb_wallet.setChecked(false);
                }
            });

            return;
        }

        super.onBackPressed();
    }

    public void getWalletBalDetails() {

        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "GetMemberWalletBalance");
        parameters.put("iUserId", generalFunc.getMemberId());
        parameters.put("UserType", Utils.app_type);

        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(), parameters);
        exeWebServer.setLoaderConfig(getActContext(), false, generalFunc);
        exeWebServer.setDataResponseListener(responseString -> {
            JSONObject responseStringObject=generalFunc.getJsonObject(responseString);

            if (responseStringObject != null && !responseStringObject.equals("")) {

                boolean isDataAvail = GeneralFunctions.checkDataAvail(Utils.action_str, responseStringObject);

                if (isDataAvail) {
                    try {
                        String userProfileJsonStr = generalFunc.retrieveValue(Utils.USER_PROFILE_JSON);
                        JSONObject object = generalFunc.getJsonObject(userProfileJsonStr);
                        String MemberBalance=generalFunc.getJsonValueStr("MemberBalance", responseStringObject);
                        object.put("user_available_balance", MemberBalance);
                        generalFunc.storeData(Utils.USER_PROFILE_JSON, object.toString());

                        getUserPeofileJson(object);


                        walletBalanceValTxt.setText(generalFunc.convertNumberWithRTL(generalFunc.convertNumberWithRTL(generalFunc.getJsonValueStr("user_available_balance", userProfileJsonObj))));

                    } catch (Exception e) {

                    }
                }
            }
        });
        exeWebServer.execute();
    }

}
