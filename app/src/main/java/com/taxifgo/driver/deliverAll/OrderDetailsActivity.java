package com.taxifgo.driver.deliverAll;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import android.text.Html;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.StrikethroughSpan;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.taxifgo.driver.R;
import com.general.files.AppFunctions;
import com.general.files.ExecuteWebServerUrl;
import com.general.files.GeneralFunctions;
import com.general.files.MyApp;
import com.squareup.picasso.Picasso;
import com.utils.Utilities;
import com.utils.Utils;
import com.view.MTextView;
import com.view.simpleratingbar.SimpleRatingBar;

import org.apache.commons.lang3.text.WordUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class OrderDetailsActivity extends AppCompatActivity {

    ArrayList<String> dataList = new ArrayList<>();
    GeneralFunctions generalFunc;

    ImageView backImgView;
    MTextView titleTxt;
    String iOrderId = "";
    View convertView = null;
    LinearLayout farecontainer;
    MTextView resturantAddressTxt, deliveryaddressTxt, resturantAddressHTxt, destAddressHTxt;
    MTextView paidviaTextH;
    MTextView deliverystatusTxt;

    MTextView orderNoHTxt, orderNoVTxt, orderDateVTxt,billTitleTxt;
    LinearLayout cancelArea;
    LinearLayout billDetails;
    LinearLayout deliveryCancelDetails;
    MTextView deliverycanclestatusTxt;
    MTextView oredrstatusTxt;
    ImageView restaurantImgView;
    int size;
    private String vImage;
    private String vAvgRating;
    private JSONObject userProfileJsonObj;
    private String SYSTEM_PAYMENT_FLOW;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_details);

        generalFunc = MyApp.getInstance().getGeneralFun(getActContext());
        userProfileJsonObj = generalFunc.getJsonObject(generalFunc.retrieveValue(Utils.USER_PROFILE_JSON));
        SYSTEM_PAYMENT_FLOW = generalFunc.getJsonValueStr("SYSTEM_PAYMENT_FLOW", userProfileJsonObj);

        size = (int) this.getResources().getDimension(R.dimen._55sdp);
        titleTxt = (MTextView) findViewById(R.id.titleTxt);
        billDetails = (LinearLayout) findViewById(R.id.billDetails);
        orderNoHTxt = (MTextView) findViewById(R.id.orderNoHTxt);
        orderNoVTxt = (MTextView) findViewById(R.id.orderNoVTxt);
        billTitleTxt = (MTextView) findViewById(R.id.billTitleTxt);
        orderDateVTxt = (MTextView) findViewById(R.id.orderDateVTxt);
        backImgView = (ImageView) findViewById(R.id.backImgView);
        farecontainer = (LinearLayout) findViewById(R.id.fareContainer);
        resturantAddressTxt = (MTextView) findViewById(R.id.resturantAddressTxt);
        resturantAddressHTxt = (MTextView) findViewById(R.id.resturantAddressHTxt);
        deliveryaddressTxt = (MTextView) findViewById(R.id.deliveryaddressTxt);
        destAddressHTxt = (MTextView) findViewById(R.id.destAddressHTxt);
        paidviaTextH = (MTextView) findViewById(R.id.paidviaTextH);
        deliverystatusTxt = (MTextView) findViewById(R.id.deliverystatusTxt);
        deliveryCancelDetails = (LinearLayout) findViewById(R.id.deliveryCancelDetails);
        cancelArea = (LinearLayout) findViewById(R.id.cancelArea);
        deliverycanclestatusTxt = (MTextView) findViewById(R.id.deliverycanclestatusTxt);
        oredrstatusTxt = (MTextView) findViewById(R.id.oredrstatusTxt);
        restaurantImgView = (ImageView) findViewById(R.id.restaurantImgView);

        backImgView.setOnClickListener(new setOnClickList());
        iOrderId = getIntent().getStringExtra("iOrderId");


        setLabel();
        backImgView.setOnClickListener(new setOnClickList());
        getOrderDetails();

    }

    public void setLabel() {
        titleTxt.setVisibility(View.VISIBLE);
        destAddressHTxt.setText(generalFunc.retrieveLangLBl("", "LBL_DELIVERY_ADDRESS"));
        billTitleTxt.setText(generalFunc.retrieveLangLBl("", "LBL_BILL_DETAILS"));
        titleTxt.setText(generalFunc.retrieveLangLBl("RECEIPT", "LBL_RECEIPT_HEADER_TXT"));
    }


    public void getOrderDetails() {

        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "GetOrderDetailsRestaurant");
        parameters.put("iOrderId", iOrderId);
        parameters.put("UserType", Utils.userType);
        parameters.put("eSystem", Utils.eSystem_Type);

        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(), parameters);
        exeWebServer.setLoaderConfig(getActContext(), true, generalFunc);
        exeWebServer.setDataResponseListener(responseString -> {
            JSONObject responseStringObject = generalFunc.getJsonObject(responseString);


            if (responseStringObject != null && !responseStringObject.equals("")) {

                boolean isDataAvail = GeneralFunctions.checkDataAvail(Utils.action_str, responseStringObject);

                if (isDataAvail == true) {

                    String message = generalFunc.getJsonValueStr(Utils.message_str, responseStringObject);
                    resturantAddressTxt.setText(generalFunc.getJsonValue("vRestuarantLocation", message));
                    vImage = generalFunc.getJsonValue("companyImage", message);
                    vAvgRating = generalFunc.getJsonValue("vAvgRating", message);

                    ((SimpleRatingBar) findViewById(R.id.ratingBar)).setRating(generalFunc.parseFloatValue(0,vAvgRating));
                    setImage();

//                    deliveryaddressTxt.setText(WordUtils.capitalize(generalFunc.getJsonValue("UserName", message)) + "\n" + generalFunc.getJsonValue("DeliveryAddress", message));
                    deliveryaddressTxt.setText(generalFunc.getJsonValue("DeliveryAddress", message));

                    orderNoHTxt.setText(generalFunc.retrieveLangLBl("", "LBL_ORDER_NO_TXT"));
                    orderNoVTxt.setText(generalFunc.convertNumberWithRTL(generalFunc.getJsonValue("vOrderNo", message)));
                    orderDateVTxt.setText(generalFunc.convertNumberWithRTL(generalFunc.getDateFormatedType(generalFunc.getJsonValue("tOrderRequestDate_Org", message), Utils.OriginalDateFormate, Utils.getDetailDateFormat(getActContext()))));


                    resturantAddressHTxt.setText(WordUtils.capitalize(generalFunc.getJsonValue("vCompany", message)));
                   /* JSONArray itemListArr = null;
                    itemListArr = generalFunc.getJsonArray("itemlist", message);
                    addItemDetailLayout(itemListArr);*/


                    String LBL_PAID_VIA = generalFunc.retrieveLangLBl("", "LBL_PAID_VIA");
                    String ePaymentOption = generalFunc.getJsonValue("ePaymentOption", message);


                    if (ePaymentOption.equalsIgnoreCase("Cash")) {
                        ((ImageView) findViewById(R.id.paymentTypeImgeView)).setImageResource(R.drawable.ic_cash_new);
                        paidviaTextH.setText(LBL_PAID_VIA + " " + generalFunc.retrieveLangLBl("", "LBL_CASH_TXT"));
                    } else if (ePaymentOption.equalsIgnoreCase("Card")) {
                        if (Utils.checkText(SYSTEM_PAYMENT_FLOW) && !SYSTEM_PAYMENT_FLOW.equalsIgnoreCase("Method-1")) {
                            ((ImageView) findViewById(R.id.paymentTypeImgeView)).setImageResource(R.mipmap.ic_menu_wallet);
                            paidviaTextH.setText(generalFunc.retrieveLangLBl("", "LBL_PAID_VIA_WALLET"));
                        } else {
                            ((ImageView) findViewById(R.id.paymentTypeImgeView)).setImageResource(R.mipmap.ic_card_new);
                            paidviaTextH.setText(LBL_PAID_VIA + " " + generalFunc.retrieveLangLBl("", "LBL_CARD"));
                        }
                    }


                    JSONArray FareDetailsArr = null;
                    FareDetailsArr = generalFunc.getJsonArray("FareDetailsArr", message);

                    addFareDetailLayout(FareDetailsArr);

                    JSONArray itemListArr = null;
                    itemListArr = generalFunc.getJsonArray("itemlist", message);
                    if (billDetails.getChildCount() > 0) {
                        billDetails.removeAllViewsInLayout();
                    }
                    addItemDetailLayout(itemListArr);

                    deliverystatusTxt.setText(AppFunctions.fromHtml(generalFunc.getJsonValueStr("vStatusNew", responseStringObject)));

                    if (generalFunc.getJsonValue("iStatusCode", message).equalsIgnoreCase("6") && generalFunc.getJsonValue("ePaid", message).equals("Yes")) {
                        boolean ePaid = true;
                        ePaymentOption = generalFunc.getJsonValue("ePaymentOption", message);
                        deliverystatusTxt.setVisibility(View.VISIBLE);
                        deliverystatusTxt.setText(AppFunctions.fromHtml(generalFunc.getJsonValue("OrderStatusValue", message)));
                        findViewById(R.id.PayTypeArea).setVisibility(View.VISIBLE);

                    }else if (generalFunc.getJsonValue("iStatusCode", message).equalsIgnoreCase("8")) {
                        deliveryCancelDetails.setVisibility(View.GONE);
                        deliverycanclestatusTxt.setText(generalFunc.getJsonValue("OrderStatustext", message));

                        if (!generalFunc.getJsonValue("CancelOrderMessage", message).equals("") && generalFunc.getJsonValue("CancelOrderMessage", message) != null) {

                            deliveryCancelDetails.setVisibility(View.VISIBLE);
                            deliverycanclestatusTxt.setVisibility(View.GONE);
                            oredrstatusTxt.setVisibility(View.VISIBLE);
                            oredrstatusTxt.setText(generalFunc.getJsonValue("CancelOrderMessage", message));
                        }
                    } else if (generalFunc.getJsonValue("iStatusCode", message).equalsIgnoreCase("7")) {
                        deliveryCancelDetails.setVisibility(View.VISIBLE);
                        cancelArea.setVisibility(View.GONE);
                        if (!generalFunc.getJsonValue("CancelOrderMessage", message).equals("") && generalFunc.getJsonValue("CancelOrderMessage", message) != null) {
                            oredrstatusTxt.setVisibility(View.VISIBLE);
                            oredrstatusTxt.setText(generalFunc.getJsonValue("CancelOrderMessage", message));
                        }

                    }  else {
//                        deliverystatusTxt.setVisibility(View.GONE);
                        findViewById(R.id.paymentMainArea).setVisibility(View.GONE);
                    }

                    deliverystatusTxt.setText(generalFunc.getJsonValue("vStatusNew", message));

                } else {

                }
            } else {


            }
        });
        exeWebServer.execute();
    }

    private void setImage() {
        if (Utils.checkText(vImage)) {

            Picasso.get()
                    .load(vImage)
                    .placeholder(R.mipmap.ic_no_pic_user)
                    .error(R.mipmap.ic_no_pic_user)
                    .into(restaurantImgView);
        }
    }


    private void addItemDetailLayout(JSONArray jobjArray) {


        for (int i = 0; i < jobjArray.length(); i++) {
            JSONObject jobject = generalFunc.getJsonObject(jobjArray, i);
            try {
                additemDetailRow(jobject.getString("vImage"), jobject.getString("MenuItem"), jobject.getString("SubTitle"), jobject.getString("fTotPrice"), /*" x " + */"" + jobject.get("iQty"), jobject.getString("TotalDiscountPrice"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }

    private void additemDetailRow(String itemImage, String menuitemName, String subMenuName, String itemPrice, String qty, String discountprice) {
        final LayoutInflater inflater = (LayoutInflater) getActContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.item_select_bill_design, null);

        MTextView billItems = (MTextView) view.findViewById(R.id.billItems);
        MTextView billItemsQty = (MTextView) view.findViewById(R.id.billItemsQty);
        ImageView imageFoodType = (ImageView) view.findViewById(R.id.imageFoodType);
        CardView foodImageArea = (CardView) view.findViewById(R.id.foodImageArea);
        MTextView serviceTypeNameTxtView = (MTextView) view.findViewById(R.id.serviceTypeNameTxtView);
        MTextView strikeoutbillAmount = (MTextView) view.findViewById(R.id.strikeoutbillAmount);

        final MTextView billAmount = (MTextView) view.findViewById(R.id.billAmount);
        foodImageArea.setVisibility(View.VISIBLE);

        Picasso.get().load(Utilities.getResizeImgURL(getActContext(), itemImage, size, size)).placeholder(R.mipmap.ic_no_icon).error(R.mipmap.ic_no_icon).into(imageFoodType);

        billAmount.setText(generalFunc.convertNumberWithRTL(itemPrice));
        billItemsQty.setText(generalFunc.convertNumberWithRTL(qty));

        billItems.setText(menuitemName);
        /*if (!subMenuName.equalsIgnoreCase("")) {
            serviceTypeNameTxtView.setVisibility(View.VISIBLE);
            serviceTypeNameTxtView.setText(subMenuName);
        } else {
            serviceTypeNameTxtView.setVisibility(View.GONE);
        }
*/


        if (discountprice != null && !discountprice.equals("")) {
            SpannableStringBuilder spanBuilder = new SpannableStringBuilder();

            SpannableString origSpan = new SpannableString(billAmount.getText());

            origSpan.setSpan(new StrikethroughSpan(), 0, billAmount.getText().length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);

            spanBuilder.append(origSpan);

            strikeoutbillAmount.setVisibility(View.VISIBLE);
            strikeoutbillAmount.setText(spanBuilder);
            billAmount.setText(discountprice);
        } else {
            strikeoutbillAmount.setVisibility(View.GONE);
            billAmount.setTextColor(Color.parseColor("#272727"));
            billAmount.setPaintFlags(billAmount.getPaintFlags());
        }


        billDetails.addView(view);
    }


    private void addFareDetailLayout(JSONArray jobjArray) {

        for (int i = 0; i < jobjArray.length(); i++) {
            JSONObject jobject = generalFunc.getJsonObject(jobjArray, i);
            try {
                String data = jobject.names().getString(0);
                addFareDetailRow(data, jobject.get(data).toString(),(jobjArray.length() - 1) == i ? true : false);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }


    private void addFareDetailRow(String row_name, String row_value, boolean isLast) {
        View convertView = null;
        if (row_name.equalsIgnoreCase("eDisplaySeperator")) {
            convertView = new View(getActContext());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Utils.dipToPixels(getActContext(), 1));
            params.setMargins(0, 0, 0, (int) getResources().getDimension(R.dimen._5sdp));
            convertView.setBackgroundColor(Color.parseColor("#dedede"));
            convertView.setLayoutParams(params);
        } else {
            LayoutInflater infalInflater = (LayoutInflater) getActContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.design_fare_deatil_row, null);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.setMargins(0, (int) getResources().getDimension(R.dimen._10sdp), 0, isLast ? (int) getResources().getDimension(R.dimen._10sdp) : 0);
            convertView.setLayoutParams(params);

            MTextView titleHTxt = (MTextView) convertView.findViewById(R.id.titleHTxt);
            MTextView titleVTxt = (MTextView) convertView.findViewById(R.id.titleVTxt);

            titleHTxt.setText(generalFunc.convertNumberWithRTL(row_name));
            titleVTxt.setText(generalFunc.convertNumberWithRTL(row_value));

            if (isLast) {
                // CALCULATE individual fare & show
                titleHTxt.setTextColor(getResources().getColor(R.color.black));
                titleHTxt.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
                Typeface face = Typeface.createFromAsset(getAssets(), "fonts/Poppins_SemiBold.ttf");
                titleHTxt.setTypeface(face);
                titleVTxt.setTypeface(face);
                titleVTxt.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
                titleVTxt.setTextColor(getResources().getColor(R.color.appThemeColor_1));

            }

        }

        if (convertView != null)
            farecontainer.addView(convertView);
    }


    public class setOnClickList implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            Utils.hideKeyboard(getActContext());
            int i = view.getId();
            if (i == R.id.backImgView) {
                onBackPressed();
            }
        }
    }

    public Context getActContext() {
        return OrderDetailsActivity.this;
    }


}
