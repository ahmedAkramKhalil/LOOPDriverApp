package com.taxifgo.driver.deliverAll;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.text.InputType;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.taxifgo.driver.R;
import com.general.files.ExecuteWebServerUrl;
import com.general.files.GeneralFunctions;
import com.general.files.GetLocationUpdates;
import com.general.files.MyApp;
import com.utils.Utils;
import com.view.MButton;
import com.view.MTextView;
import com.view.MaterialRippleLayout;
import com.view.editBox.MaterialEditText;
import com.view.simpleratingbar.SimpleRatingBar;

import org.apache.commons.lang3.text.WordUtils;
import org.json.JSONObject;

import java.util.HashMap;

public class DeliverAllRatingActivity extends AppCompatActivity {

    MTextView titleTxt;
    ImageView backImgView;

    GeneralFunctions generalFunc;

    MButton btn_type2;
    MaterialEditText commentBox;

    SimpleRatingBar ratingBar;
    String iTripId_str;

    HashMap<String, String> data_trip;
    boolean isSubmitClicked = false;
    private String iOrderId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deliver_all_rating);

        generalFunc = MyApp.getInstance().getGeneralFun(getActContext());

        data_trip = (HashMap<String, String>) getIntent().getSerializableExtra("TRIP_DATA");
        iTripId_str = data_trip.get("TripId");
        iOrderId = data_trip.get("LastOrderId");

        titleTxt = (MTextView) findViewById(R.id.titleTxt);
        backImgView = (ImageView) findViewById(R.id.backImgView);
        commentBox = (MaterialEditText) findViewById(R.id.commentBox);
        ratingBar = (SimpleRatingBar) findViewById(R.id.ratingBar);
        btn_type2 = ((MaterialRippleLayout) findViewById(R.id.btn_type2)).getChildView();


        (findViewById(R.id.backImgView)).setVisibility(View.GONE);

        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) titleTxt.getLayoutParams();
        params.setMargins(Utils.dipToPixels(getActContext(), 20), 0, 0, 0);
        titleTxt.setLayoutParams(params);

        btn_type2.setId(Utils.generateViewId());
        btn_type2.setOnClickListener(new setOnClickList());

        commentBox.setSingleLine(false);
        commentBox.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        commentBox.setImeOptions(EditorInfo.IME_ACTION_DONE);
        commentBox.setGravity(Gravity.TOP);
        commentBox.setFloatingLabel(MaterialEditText.FLOATING_LABEL_NONE);

        setLabels();

        ((MTextView) findViewById(R.id.nameTxt)).setText(WordUtils.capitalize(data_trip.get("LastOrderUserName")) + "\n" + generalFunc.convertNumberWithRTL(data_trip.get("LastOrderNo")));
        ((MTextView) findViewById(R.id.fareTxt)).setText(generalFunc.convertNumberWithRTL(data_trip.get("LastOrderAmount")));

        if (savedInstanceState != null) {
            // Restore value of members from saved state
            String restratValue_str = savedInstanceState.getString("RESTART_STATE");

            if (restratValue_str != null && !restratValue_str.equals("") && restratValue_str.trim().equals("true")) {
                generalFunc.restartApp();
            }
        }

        GetLocationUpdates.getInstance().setTripStartValue(false, false, "");
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {

        outState.putString("RESTART_STATE", "true");
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();

    }

    @Override
    protected void onPause() {
        super.onPause();


    }

    public void setLabels() {
        titleTxt.setText(generalFunc.retrieveLangLBl("", "LBL_RATING"));
        ((MTextView) findViewById(R.id.rateTxt)).setText(generalFunc.retrieveLangLBl("Rate", "LBL_RATE"));
        ((MTextView) findViewById(R.id.orderDeliveredTxt)).setText(generalFunc.retrieveLangLBl("You just delivered an order.", "LBL_ORDER_DELIVERED_BY_CARRIER"));
        ((MTextView) findViewById(R.id.feedbackTxt)).setText(generalFunc.retrieveLangLBl("Feedback", "LBL_FEEDBACK_TXT"));
        commentBox.setHint(generalFunc.retrieveLangLBl("Give your feedback", "LBL_WRITE_COMMENT_HINT_TXT"));
        btn_type2.setText(generalFunc.retrieveLangLBl("", "LBL_BTN_SUBMIT_TXT"));
    }

    public void submitRating() {
        isSubmitClicked = true;
        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "submitRating");
        parameters.put("iGeneralUserId", generalFunc.getMemberId());
        parameters.put("iMemberId", generalFunc.getMemberId());
        parameters.put("tripID", iTripId_str);
        parameters.put("iOrderId", iOrderId);
        parameters.put("rating", "" + ratingBar.getRating() + "");
        parameters.put("message", Utils.getText(commentBox));
        parameters.put("eFromUserType", Utils.app_type);
        parameters.put("eToUserType", Utils.passenger_app_type);
        parameters.put("eSystem", Utils.eSystem_Type);

        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(), parameters);
        exeWebServer.setLoaderConfig(getActContext(), true, generalFunc);
        exeWebServer.setDataResponseListener(new ExecuteWebServerUrl.SetDataResponse() {
            @Override
            public void setResponse(String responseString) {
                JSONObject responseStringObject = generalFunc.getJsonObject(responseString);

                if (responseStringObject != null && !responseStringObject.equals("")) {
                    isSubmitClicked = true;
                    boolean isDataAvail = GeneralFunctions.checkDataAvail(Utils.action_str, responseStringObject);

                    if (isDataAvail == true) {
                        isSubmitClicked = false;

                        showBookingAlert(generalFunc.retrieveLangLBl("", "LBL_FINISHED_DELIVERY_TXT"));

                    } else {
                        isSubmitClicked = false;
                        generalFunc.showGeneralMessage("",
                                generalFunc.retrieveLangLBl("", generalFunc.getJsonValueStr(Utils.message_str, responseStringObject)));
                    }
                } else {
                    isSubmitClicked = false;
                    generalFunc.showError();
                }
            }
        });
        exeWebServer.execute();
    }

    public void showBookingAlert(String message) {
        androidx.appcompat.app.AlertDialog alertDialog;
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getActContext());
        builder.setTitle("");
        builder.setCancelable(false);
        LayoutInflater inflater = (LayoutInflater) getActContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View dialogView = inflater.inflate(R.layout.dialog_booking_view, null);
        builder.setView(dialogView);

        final MTextView titleTxt = (MTextView) dialogView.findViewById(R.id.titleTxt);
        final MTextView mesasgeTxt = (MTextView) dialogView.findViewById(R.id.mesasgeTxt);


        titleTxt.setText(generalFunc.retrieveLangLBl("Booking Successful", "LBL_SUCCESS_FINISHED_DRDL"));
        mesasgeTxt.setText(message);


        builder.setPositiveButton(generalFunc.retrieveLangLBl("", "LBL_OK_THANKS"), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                generalFunc.saveGoOnlineInfo();
                MyApp.getInstance().restartWithGetDataApp();

            }
        });


        alertDialog = builder.create();
        alertDialog.setCancelable(false);
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();


    }


    public Context getActContext() {
        return DeliverAllRatingActivity.this;
    }

    @Override
    public void onBackPressed() {
        return;
    }


    public class setOnClickList implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            int i = view.getId();
            Utils.hideKeyboard(DeliverAllRatingActivity.this);

            if (i == btn_type2.getId()) {
                if (!isSubmitClicked) {

                    if (ratingBar.getRating() < 0.5) {
                        generalFunc.showMessage(generalFunc.getCurrentView(DeliverAllRatingActivity.this),
                                generalFunc.retrieveLangLBl("", "LBL_ERROR_RATING_DIALOG_TXT"));
                        return;
                    }
                    submitRating();
                }
            }
        }
    }
}
