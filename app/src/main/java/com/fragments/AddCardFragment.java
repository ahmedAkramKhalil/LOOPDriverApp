package com.fragments;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.taxifgo.driver.CardPaymentActivity;
import com.taxifgo.driver.R;
import com.taxifgo.driver.VerifyCardTokenActivity;
import com.general.files.ExecuteWebServerUrl;
import com.general.files.GeneralFunctions;
import com.general.files.StartActProcess;
import com.paymaya.sdk.android.payment.PayMayaPayment;
import com.paymaya.sdk.android.payment.models.PaymentToken;
import com.stripe.android.CardUtils;
import com.stripe.android.Stripe;
import com.stripe.android.TokenCallback;
import com.stripe.android.model.Card;
import com.stripe.android.model.Token;
import com.stripe.android.view.CardMultilineWidget;
import com.stripe.android.view.CardNumberEditText;
import com.stripe.android.view.ExpiryDateEditText;
import com.stripe.android.view.StripeEditText;
import com.utils.ModelUtils;
import com.utils.Utils;
import com.view.MButton;
import com.view.MaterialRippleLayout;
import com.view.MyProgressDialog;
import com.view.editBox.MaterialEditText;
import com.xendit.Models.XenditError;
import com.xendit.Xendit;

import org.json.JSONObject;

import java.security.GeneralSecurityException;
import java.util.Calendar;
import java.util.HashMap;

import co.omise.android.Client;
import co.omise.android.TokenRequest;
import co.omise.android.TokenRequestListener;
import io.card.payment.CardIOActivity;
import io.card.payment.CreditCard;

/**
 * A simple {@link Fragment} subclass.
 */
public class AddCardFragment extends Fragment implements TextWatcher {

    private static final char SPACE_CHAR = ' ';
    GeneralFunctions generalFunc;
    View view;

    CardPaymentActivity cardPayAct;

    JSONObject userProfileJsonObj;
    MButton btn_type2;
    MaterialEditText nameOfCardBox;
    MaterialEditText creditCardBox;
    MaterialEditText cvvBox;
    MaterialEditText mmBox;
    MaterialEditText yyBox;

    View nameArea;
    String required_str = "";
    public boolean isInProcessMode = false;

    LinearLayout defaultArea, stripearea;
    CardMultilineWidget card_input_widget;
    ImageView stCardImgView;
    ImageView cardImgView;

    String LBL_ADD_CARD, LBL_CHANGE_CARD;
    String APP_PAYMENT_METHOD;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_add_card, container, false);

        cardPayAct = (CardPaymentActivity) getActivity();
        generalFunc = cardPayAct.generalFunc;
        userProfileJsonObj = cardPayAct.userProfileJsonObj;
        btn_type2 = ((MaterialRippleLayout) view.findViewById(R.id.btn_type2)).getChildView();
        nameOfCardBox = (MaterialEditText) view.findViewById(R.id.nameOfCardBox);
        creditCardBox = (MaterialEditText) view.findViewById(R.id.creditCardBox);
        cvvBox = (MaterialEditText) view.findViewById(R.id.cvvBox);
        mmBox = (MaterialEditText) view.findViewById(R.id.mmBox);
        yyBox = (MaterialEditText) view.findViewById(R.id.yyBox);
        defaultArea = (LinearLayout) view.findViewById(R.id.defaultArea);
        stripearea = (LinearLayout) view.findViewById(R.id.stripearea);
        card_input_widget = (CardMultilineWidget) view.findViewById(R.id.card_input_widget);
        stCardImgView = (ImageView) view.findViewById(R.id.stCardImgView);
        cardImgView = (ImageView) view.findViewById(R.id.cardImgView);
        nameArea = view.findViewById(R.id.nameArea);

        cardImgView.setImageDrawable(getResources().getDrawable(R.drawable.ic_card_default));

        LBL_ADD_CARD = generalFunc.retrieveLangLBl("", "LBL_ADD_CARD");
        LBL_CHANGE_CARD = generalFunc.retrieveLangLBl("", "LBL_CHANGE_CARD");
        APP_PAYMENT_METHOD = generalFunc.getJsonValueStr("APP_PAYMENT_METHOD", userProfileJsonObj);

        if (getArguments().getString("PAGE_MODE").equals("ADD_CARD")) {
            cardPayAct.changePageTitle(LBL_ADD_CARD);
            btn_type2.setText(LBL_ADD_CARD);

        } else {
            cardPayAct.changePageTitle(LBL_CHANGE_CARD);
            btn_type2.setText(LBL_CHANGE_CARD);
        }


        btn_type2.setId(Utils.generateViewId());
        btn_type2.setOnClickListener(new setOnClickList());

        setLabels();


        mmBox.setInputType(InputType.TYPE_CLASS_NUMBER);
        yyBox.setInputType(InputType.TYPE_CLASS_NUMBER);
        cvvBox.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        cvvBox.setImeOptions(EditorInfo.IME_ACTION_DONE);
        creditCardBox.setInputType(InputType.TYPE_CLASS_PHONE);

        creditCardBox.setFilters(new InputFilter[]{new InputFilter.LengthFilter(24)});
        mmBox.setFilters(new InputFilter[]{new InputFilter.LengthFilter(2)});
        cvvBox.setFilters(new InputFilter[]{new InputFilter.LengthFilter(5)});
        yyBox.setFilters(new InputFilter[]{new InputFilter.LengthFilter(4)});

        creditCardBox.addTextChangedListener(this);


        if (APP_PAYMENT_METHOD.equalsIgnoreCase("Stripe")) {
            stripearea.setVisibility(View.VISIBLE);
            defaultArea.setVisibility(View.GONE);
            configureStripeView();
        } else {
            stripearea.setVisibility(View.GONE);
            defaultArea.setVisibility(View.VISIBLE);

        }
        setCardIOView(true);

        setBrands();

        return view;
    }

    public void setBrands() {
        Card.BRAND_RESOURCE_MAP.clear();
        HashMap<String, Integer> brandMap = new HashMap<String, Integer>();
        brandMap.put(Card.AMERICAN_EXPRESS, R.drawable.ic_amex_system);
        brandMap.put(Card.DINERS_CLUB, R.drawable.ic_diners_system);
        brandMap.put(Card.DISCOVER, R.drawable.ic_discover_system);
        brandMap.put(Card.JCB, R.drawable.ic_jcb_system);
        brandMap.put(Card.MASTERCARD, R.drawable.ic_mastercard_system);
        brandMap.put(Card.VISA, R.drawable.ic_visa_system);
        brandMap.put(Card.UNIONPAY, R.drawable.ic_unionpay_system);
        brandMap.put(Card.UNKNOWN, R.drawable.ic_unknown);
        Card.BRAND_RESOURCE_MAP.putAll(brandMap);
    }

    public void setLabels() {

        nameOfCardBox.setBothText(generalFunc.retrieveLangLBl("", "LBL_CARD_HOLDER_NAME_TXT"), generalFunc.retrieveLangLBl("", "LBL_CARD_HOLDER_NAME_TXT"));
        creditCardBox.setBothText(generalFunc.retrieveLangLBl("", "LBL_CARD_NUMBER_HEADER_TXT"), generalFunc.retrieveLangLBl("", "LBL_CARD_NUMBER_HINT_TXT"));
        cvvBox.setBothText(generalFunc.retrieveLangLBl("", "LBL_CVV_HEADER_TXT"), generalFunc.retrieveLangLBl("", "LBL_CVV_HINT_TXT"));
        mmBox.setBothText(generalFunc.retrieveLangLBl("", "LBL_EXP_MONTH_HINT_TXT"), generalFunc.retrieveLangLBl("", "LBL_EXP_MONTH_HINT_TXT"));
        yyBox.setBothText(generalFunc.retrieveLangLBl("", "LBL_EXP_YEAR_HINT_TXT"), generalFunc.retrieveLangLBl("", "LBL_EXP_YEAR_HINT_TXT"));

        required_str = generalFunc.retrieveLangLBl("", "LBL_FEILD_REQUIRD");
//        LBL_ERROR_CVV_NUMBER_TXT
        creditCardBox.addTextChangedListener(new CardTypeTextWatcher());
    }

    public boolean validateExpYear(Calendar now) {
        return yyBox.getText().toString() != null && !ModelUtils.hasYearPassed(GeneralFunctions.parseIntegerValue(0, yyBox.getText().toString()), now);
    }

    private void setCardIOView(boolean isShow) {
        if (Utils.isClassExist("io.card.payment.CardIOActivity")) {
            try {

                View actView = generalFunc.getCurrentView(getActivity());

                if (actView.findViewById(R.id.cardioview) != null) {
                    View cardioview = actView.findViewById(R.id.cardioview);
                    cardioview.setVisibility(isShow ? View.VISIBLE : View.GONE);
                    cardioview.setOnClickListener(new setOnClickList());
                }

            } catch (Exception e) {

            }
        }
    }

    @Override
    public void onDetach() {
        setCardIOView(false);
        super.onDetach();
    }

    public class CardTypeTextWatcher implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void afterTextChanged(Editable editable) {


            getBrands(CardUtils.getPossibleCardType(editable.toString()));

//            switch (CardUtils.getPossibleCardType(editable.toString())) {
//                case Card.AMERICAN_EXPRESS:
//                    stCardImgView.setImageDrawable(getResources().getDrawable(R.drawable.ic_amex));
//                    cardImgView.setImageDrawable(getResources().getDrawable(R.drawable.ic_amex));
//                    break;
//                case Card.DINERS_CLUB:
//                    stCardImgView.setImageDrawable(getResources().getDrawable(R.drawable.ic_diners));
//                    cardImgView.setImageDrawable(getResources().getDrawable(R.drawable.ic_diners));
//                    break;
//                case Card.DISCOVER:
//                    stCardImgView.setImageDrawable(getResources().getDrawable(R.drawable.ic_discover));
//                    cardImgView.setImageDrawable(getResources().getDrawable(R.drawable.ic_discover));
//                    break;
//                case Card.JCB:
//                    stCardImgView.setImageDrawable(getResources().getDrawable(R.drawable.ic_jcb));
//                    cardImgView.setImageDrawable(getResources().getDrawable(R.drawable.ic_jcb));
//                    break;
//                case Card.MASTERCARD:
//                    stCardImgView.setImageDrawable(getResources().getDrawable(R.drawable.ic_mastercard));
//                    cardImgView.setImageDrawable(getResources().getDrawable(R.drawable.ic_mastercard));
//                    break;
//                case Card.VISA:
//                    stCardImgView.setImageDrawable(getResources().getDrawable(R.drawable.ic_visa));
//                    cardImgView.setImageDrawable(getResources().getDrawable(R.drawable.ic_visa));
//                    break;
//                case Card.UNIONPAY:
//                    Logger.e("UnionPay",":: called");
//                    stCardImgView.setImageDrawable(getResources().getDrawable(R.drawable.ic_unionpay_system));
//                    cardImgView.setImageDrawable(getResources().getDrawable(R.drawable.ic_unionpay_system));
//                    break;
//                case Card.UNKNOWN:
//                    stCardImgView.setImageDrawable(getResources().getDrawable(R.drawable.ic_card_default));
//                    cardImgView.setImageDrawable(getResources().getDrawable(R.drawable.ic_card_default));
//                    break;
//                default:
//                    stCardImgView.setImageDrawable(getResources().getDrawable(R.drawable.ic_card_default));
//                    cardImgView.setImageDrawable(getResources().getDrawable(R.drawable.ic_card_default));
            // }
        }
    }


    public Drawable getBrands(String carVal) {

        if (carVal.equalsIgnoreCase(Card.AMERICAN_EXPRESS))
            return getResources().getDrawable(R.drawable.ic_amex_system);
        else if (carVal.equalsIgnoreCase(Card.DINERS_CLUB))
            return getResources().getDrawable(R.drawable.ic_diners_system);
        else if (carVal.equalsIgnoreCase(Card.DISCOVER))
            return getResources().getDrawable(R.drawable.ic_discover_system);
        else if (carVal.equalsIgnoreCase(Card.JCB))
            return getResources().getDrawable(R.drawable.ic_jcb_system);
        else if (carVal.equalsIgnoreCase(Card.MASTERCARD))
            return getResources().getDrawable(R.drawable.ic_mastercard_system);
        else if (carVal.equalsIgnoreCase(Card.VISA))
            return getResources().getDrawable(R.drawable.ic_visa_system);
        else if (carVal.equalsIgnoreCase(Card.UNIONPAY))
            return getResources().getDrawable(R.drawable.ic_unionpay_system);
        else if (carVal.equalsIgnoreCase(Card.UNKNOWN))
            return getResources().getDrawable(R.drawable.ic_card_default);
        else
            return getResources().getDrawable(R.drawable.ic_card_default);

    }

    public void configureStripeView() {
//        CardUtils.getPossibleCardType("41111111");
        stCardImgView.setImageDrawable(getResources().getDrawable(R.drawable.ic_card_default));

        View cardNumView = getStripeCardBox(com.stripe.android.R.id.et_add_source_card_number_ml);

        if (cardNumView != null && cardNumView instanceof CardNumberEditText) {

//            card_input_widget.setCardNumberTextWatcher(new CardTypeTextWatcher());

            card_input_widget.setCardNumberTextWatcher(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void afterTextChanged(Editable editable) {
                    int charCount = editable.length();

                    if (charCount > 0 && ((CardNumberEditText) cardNumView).getCardBrand() != null) {
                        stCardImgView.setImageDrawable(getBrands(CardUtils.getPossibleCardType(editable.toString())));
                    } else {
                        stCardImgView.setImageDrawable(getResources().getDrawable(R.drawable.ic_card_default));
                    }
                }
            });
        }
    }

    public void checkDetails() {

        if (APP_PAYMENT_METHOD.equalsIgnoreCase("Stripe")) {
//            Card cardToSave = card_input_widget.getCard();

            if (!card_input_widget.validateAllFields()) {
                generalFunc.showGeneralMessage("", generalFunc.retrieveLangLBl("", "LBL_INVALID_CARD_DETAILS"));
                return;
            }

            generateStripeToken(card_input_widget.getCard());


            return;
        }

        Card card = new Card(Utils.getText(creditCardBox), generalFunc.parseIntegerValue(0, Utils.getText(mmBox)),
                generalFunc.parseIntegerValue(0, Utils.getText(yyBox)), Utils.getText(cvvBox));

        boolean isNameEntered = true;

        if (nameArea.getVisibility() == View.VISIBLE) {
            isNameEntered = Utils.checkText(nameOfCardBox) ? true : Utils.setErrorFields(nameOfCardBox, generalFunc.retrieveLangLBl("", "LBL_FEILD_REQUIRD"));
        }

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


        if (isNameEntered == false || cardNoEntered == false || cvvEntered == false || monthEntered == false || yearEntered == false || yearEntedcount == false) {
            return;
        }


        if (APP_PAYMENT_METHOD.equalsIgnoreCase("Stripe")) {
            if (card.validateCard()) {
                generateStripeToken(card);
            }
        }
    }

    public void generateXenditToken(final com.xendit.Models.Card card) {

        final MyProgressDialog myPDialog = showLoader();

        String XENDIT_PUBLIC_KEY = generalFunc.getJsonValueStr("XENDIT_PUBLIC_KEY", userProfileJsonObj);

        if (card == null) {
            if (myPDialog != null) {
                myPDialog.close();
            }
            return;
        }


        final Xendit xendit = new Xendit(getActContext(), XENDIT_PUBLIC_KEY);

        xendit.createMultipleUseToken(card, new com.xendit.TokenCallback() {
            @Override
            public void onSuccess(com.xendit.Models.Token token) {
                myPDialog.close();
                CreateCustomer(Utils.maskCardNumber(card.getCreditCardNumber()), token.getId());

            }

            @Override
            public void onError(XenditError error) {
                myPDialog.close();

                generalFunc.showMessage(btn_type2, error.getErrorMessage());
            }
        });


    }


    public void generatePayMayaToken(final com.paymaya.sdk.android.payment.models.Card card) {

        new AsyncTask<String, String, PaymentToken>() {
            MyProgressDialog myPDialog = null;

            @Override
            protected PaymentToken doInBackground(String... strings) {
                String STRIPE_PUBLISH_KEY = generalFunc.getJsonValueStr("PAYMAYA_PUBLISH_KEY", userProfileJsonObj);

                PayMayaPayment payMayaPayment = new PayMayaPayment(STRIPE_PUBLISH_KEY, card);
                PaymentToken paymentToken = payMayaPayment.getPaymentToken();

                return paymentToken;
            }

            @Override
            protected void onPreExecute() {
                super.onPreExecute();

                myPDialog = showLoader();
            }

            @Override
            protected void onPostExecute(PaymentToken paymentToken) {
                if (paymentToken != null) {
                    myPDialog.close();
                    CreateCustomer(Utils.maskCardNumber(card.getNumber()), paymentToken.getPaymentTokenId());
                } else {
                    closeProcessingMode();
                    myPDialog.close();
                    generalFunc.showError();


                }
            }
        }.execute();

    }

    public void setProcessingMode() {
        isInProcessMode = true;
        btn_type2.setText(generalFunc.retrieveLangLBl("Processing Payment", "LBL_PROCESS_PAYMENT_TXT"));
        creditCardBox.setEnabled(false);
        mmBox.setEnabled(false);
        yyBox.setEnabled(false);
        cvvBox.setEnabled(false);
        btn_type2.setEnabled(false);
    }


    public void closeProcessingMode() {
        try {
            isInProcessMode = false;
            if (getArguments().getString("PAGE_MODE").equals("ADD_CARD")) {
                btn_type2.setText(LBL_ADD_CARD);
            } else {
                btn_type2.setText(LBL_CHANGE_CARD);
            }
            creditCardBox.setEnabled(true);
            mmBox.setEnabled(true);
            yyBox.setEnabled(true);
            cvvBox.setEnabled(true);
            btn_type2.setEnabled(true);
        } catch (Exception e) {

        }
    }

    public MyProgressDialog showLoader() {
        MyProgressDialog myPDialog = new MyProgressDialog(getActContext(), false, generalFunc.retrieveLangLBl("Loading", "LBL_LOADING_TXT"));
        myPDialog.show();

        return myPDialog;
    }

    public void generateOmiseToken(String cardHolderName, String cardNumber, String expMonth, String expYear, String cvv) throws GeneralSecurityException {
        final MyProgressDialog myPDialog = showLoader();
        String OMISE_PUBLIC_KEY = generalFunc.getJsonValueStr("OMISE_PUBLIC_KEY", userProfileJsonObj);
        Client client = new Client(OMISE_PUBLIC_KEY);

        TokenRequest request = new TokenRequest();
        request.number = cardNumber.replaceAll("\\s+", "");
        request.name = cardHolderName;
        request.expirationMonth = GeneralFunctions.parseIntegerValue(1, expMonth);
        request.expirationYear = GeneralFunctions.parseIntegerValue(Calendar.getInstance().get(Calendar.YEAR), expYear);
        request.securityCode = cvv;

        client.send(request, new TokenRequestListener() {

            @Override
            public void onTokenRequestSucceed(TokenRequest tokenRequest, co.omise.android.models.Token token) {
                myPDialog.close();
                CreateCustomer(Utils.maskCardNumber(cardNumber), token.id);
            }

            @Override
            public void onTokenRequestFailed(TokenRequest tokenRequest, Throwable throwable) {
                myPDialog.close();
            }
        });

    }

    public void generateStripeToken(final Card card) {

        final MyProgressDialog myPDialog = showLoader();

        String STRIPE_PUBLISH_KEY = generalFunc.getJsonValueStr("STRIPE_PUBLISH_KEY", userProfileJsonObj);
        Stripe stripe = new Stripe(getActContext());

        stripe.createToken(card, STRIPE_PUBLISH_KEY, new TokenCallback() {
            public void onSuccess(Token token) {
                // TODO: Send Token information to your backend to initiate a charge
                myPDialog.close();
//                CreateCustomer(card, null, null, token.getId());
                CreateCustomer(Utils.maskCardNumber(card.getNumber()), token.getId());
            }

            public void onError(Exception error) {
                myPDialog.close();
                generalFunc.showError();
            }
        });
    }


    public void setScanData(CreditCard scanResult) {
        if (scanResult == null) {
            return;
        }

        String cardnumber = scanResult.getFormattedCardNumber();
        String expMonth = "" + scanResult.expiryMonth;
        String expYear = "" + scanResult.expiryYear;
        String cvc = "" + scanResult.cvv;
        String cardHolderName = scanResult.cardholderName == null ? "" : scanResult.cardholderName;

        if (APP_PAYMENT_METHOD.equalsIgnoreCase("Stripe")) {

            String expYear_last = expYear.length() == 4 ? expYear.substring(2, 4) : expYear;

            View cardNumView = getStripeCardBox(com.stripe.android.R.id.et_add_source_card_number_ml);
            if (cardNumView != null && cardNumView instanceof CardNumberEditText) {
                ((CardNumberEditText) cardNumView).setText(cardnumber);
            }

            View expView = getStripeCardBox(com.stripe.android.R.id.et_add_source_expiry_ml);
            if (expView != null && expView instanceof ExpiryDateEditText) {
                ((ExpiryDateEditText) expView).setText((GeneralFunctions.parseIntegerValue(0, expMonth) < 10 ? ("0" + expMonth) : expMonth) + "/" + expYear_last);
            }

            View cvvNumView = getStripeCardBox(com.stripe.android.R.id.et_add_source_cvc_ml);
            if (cvvNumView != null && cvvNumView instanceof StripeEditText) {
                ((StripeEditText) cvvNumView).setText(cvc);
            }

        } else {
            if (nameArea.getVisibility() == View.VISIBLE) {
                nameOfCardBox.setText(cardHolderName);
            }
            creditCardBox.setText(cardnumber);
            mmBox.setText(expMonth);
            yyBox.setText(expYear);
            cvvBox.setText(cvc);
        }
    }

    public View getStripeCardBox(int id) {
        if (stripearea.getVisibility() == View.VISIBLE && card_input_widget.findViewById(id) != null) {

            return card_input_widget.findViewById(id);
        }
        return null;
    }

    public void CreateCustomer(String cardNum, String token) {
        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "GenerateCustomer");
        parameters.put("iUserId", generalFunc.getMemberId());

        parameters.put("CardNo", cardNum);

        if (APP_PAYMENT_METHOD.equalsIgnoreCase("Stripe")) {
            parameters.put("vStripeToken", token);
        }

        parameters.put("UserType", Utils.app_type);

        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(), parameters);
        exeWebServer.setLoaderConfig(getActContext(), true, generalFunc);
        exeWebServer.setDataResponseListener(responseString -> {
            JSONObject responseStringObj = generalFunc.getJsonObject(responseString);

            if (responseStringObj != null && !responseStringObj.equals("")) {

                boolean isDataAvail = GeneralFunctions.checkDataAvail(Utils.action_str, responseStringObj);


                if (isDataAvail) {

                    if (APP_PAYMENT_METHOD.equalsIgnoreCase("Stripe")) {
                        generalFunc.storeData(Utils.USER_PROFILE_JSON, generalFunc.getJsonValueStr(Utils.message_str, responseStringObj));
                        cardPayAct.changeUserProfileJson(generalFunc.getJsonValueStr(Utils.message_str, responseStringObj));
                    }
                } else {

                    generalFunc.showGeneralMessage("",
                            generalFunc.retrieveLangLBl("", generalFunc.getJsonValueStr(Utils.message_str, responseStringObj)));
                }
            } else {
                generalFunc.showError();
            }
        });
        exeWebServer.execute();
    }


    public void setdata(int requestCode, int resultCode, Intent data) {

        if (requestCode == Utils.REQ_VERIFY_CARD_PIN_CODE && resultCode == Activity.RESULT_OK && data != null) {

            UpdateCustomerToken((HashMap<String, Object>) data.getSerializableExtra("data"));
        }
    }

    private void UpdateCustomerToken(HashMap<String, Object> data) {
        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "UpdateCustomerToken");
        parameters.put("iUserId", generalFunc.getMemberId());
        parameters.put("vPaymayaToken", data.get("vPaymayaToken").toString());
        parameters.put("UserType", Utils.app_type);

        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(), parameters);
        exeWebServer.setLoaderConfig(getActContext(), true, generalFunc);
        exeWebServer.setDataResponseListener(responseString -> {
            JSONObject responseStringObj = generalFunc.getJsonObject(responseString);
            if (responseStringObj != null && !responseStringObj.equals("")) {

                boolean isDataAvail = GeneralFunctions.checkDataAvail(Utils.action_str, responseStringObj);

                if (isDataAvail) {
                    generalFunc.storeData(Utils.USER_PROFILE_JSON, generalFunc.getJsonValueStr(Utils.message_str, responseStringObj));
                    cardPayAct.changeUserProfileJson(generalFunc.getJsonValueStr(Utils.message_str, responseStringObj));
                } else {
                    closeProcessingMode();
                    generalFunc.showGeneralMessage("",
                            generalFunc.retrieveLangLBl("", generalFunc.getJsonValueStr(Utils.message_str, responseStringObj)));
                }
            } else {
                closeProcessingMode();
                generalFunc.showError();
            }
        });
        exeWebServer.execute();
    }


    public Context getActContext() {
        return cardPayAct.getActContext();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Utils.hideKeyboard(getActContext());
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        if (s.length() > 0 && (s.length() % 5) == 0) {
            final char c = s.charAt(s.length() - 1);
            if (SPACE_CHAR == c) {
                s.delete(s.length() - 1, s.length());
            }
        }
        // Insert char where needed.
        if (s.length() > 0 && (s.length() % 5) == 0) {
            char c = s.charAt(s.length() - 1);
            // Only if its a digit where there should be a space we insert a space
            if (Character.isDigit(c) && TextUtils.split(s.toString(), String.valueOf(SPACE_CHAR)).length <= 3) {
                s.insert(s.length() - 1, String.valueOf(SPACE_CHAR));
            }
        }
    }

    public class setOnClickList implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            Utils.hideKeyboard(getActContext());
            int i = view.getId();
            if (i == btn_type2.getId()) {
                checkDetails();
            } else if (i == R.id.cardioview) {

                if (generalFunc.isCameraPermissionGranted() && Utils.isClassExist("io.card.payment.CardIOActivity")) {
                    Bundle bn = new Bundle();
                    bn.putBoolean(CardIOActivity.EXTRA_REQUIRE_EXPIRY, true);
                    bn.putBoolean(CardIOActivity.EXTRA_SCAN_EXPIRY, true);
                    bn.putBoolean(CardIOActivity.EXTRA_REQUIRE_CVV, true);


                    bn.putBoolean(CardIOActivity.EXTRA_REQUIRE_POSTAL_CODE, false);
                    bn.putBoolean(CardIOActivity.EXTRA_HIDE_CARDIO_LOGO, true);
                    bn.putBoolean(CardIOActivity.EXTRA_USE_PAYPAL_ACTIONBAR_ICON, false);
                    bn.putBoolean(CardIOActivity.EXTRA_SUPPRESS_MANUAL_ENTRY, false);
                    new StartActProcess(getActContext()).startActForResult(AddCardFragment.this, CardIOActivity.class, Utils.MY_SCAN_REQUEST_CODE, bn);
                }

            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Utils.MY_SCAN_REQUEST_CODE && data != null && data.hasExtra(CardIOActivity.EXTRA_SCAN_RESULT)) {

            CreditCard scanResult = data.getParcelableExtra(CardIOActivity.EXTRA_SCAN_RESULT);

            setScanData(scanResult);

        }
    }
}
