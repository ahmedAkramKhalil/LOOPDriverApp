package com.taxifgo.driver;

import android.content.Context;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.general.files.ExecuteWebServerUrl;
import com.general.files.GeneralFunctions;
import com.general.files.MyApp;
import com.utils.Utils;
import com.view.MTextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;

public class MoreServiceInfoActivity extends AppCompatActivity {

    public GeneralFunctions generalFunc;
    MTextView titleTxt;
    ImageView backImgView;
    LinearLayout itemContainer;

    ProgressBar loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_more_service_info);


        titleTxt = (MTextView) findViewById(R.id.titleTxt);
        backImgView = (ImageView) findViewById(R.id.backImgView);
        generalFunc = MyApp.getInstance().getGeneralFun(getActContext());
        backImgView.setOnClickListener(new setOnClickList());
        itemContainer = (LinearLayout) findViewById(R.id.itemContainer);
        loadingBar = findViewById(R.id.loadingBar);

        titleTxt.setText(generalFunc.retrieveLangLBl("", "LBL_TITLE_REQUESTED_SERVICES"));
        getDetails();
    }


    public void getDetails() {

        loadingBar.setVisibility(View.VISIBLE);

        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "getSpecialInstructionData");
        parameters.put("iMemberId", generalFunc.getMemberId());
        parameters.put("UserType", Utils.app_type);

        if (getIntent().getStringExtra("iTripId") != null && !getIntent().getStringExtra("iTripId").equalsIgnoreCase("")) {
            parameters.put("iTripId", getIntent().getStringExtra("iTripId"));
        } else if (getIntent().getStringExtra("iCabRequestId") != null && !getIntent().getStringExtra("iCabRequestId").equalsIgnoreCase("")) {
            parameters.put("iCabRequestId", getIntent().getStringExtra("iCabRequestId"));
        } else {
            parameters.put("iCabBookingId", getIntent().getStringExtra("iCabBookingId"));

        }

        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(), parameters);
        exeWebServer.setLoaderConfig(getActContext(), false, generalFunc);
        exeWebServer.setDataResponseListener(responseString -> {
            JSONObject responseStringObject=generalFunc.getJsonObject(responseString);

            if (responseStringObject != null && !responseStringObject.equals("")) {

                boolean isDataAvail = GeneralFunctions.checkDataAvail(Utils.action_str, responseStringObject);

                if (isDataAvail) {
                    if (itemContainer.getChildCount() > 0) {
                        itemContainer.removeAllViewsInLayout();
                    }

                    JSONArray itemArray = generalFunc.getJsonArray(Utils.message_str, responseStringObject);
                    for (int i = 0; i < itemArray.length(); i++) {
                        JSONObject data = generalFunc.getJsonObject(itemArray, i);

                        LayoutInflater topinginflater = (LayoutInflater) getActContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        View editCartView = topinginflater.inflate(R.layout.moreserviceitem, null);
                        MTextView serviceNameTxt = (MTextView) editCartView.findViewById(R.id.serviceNameTxt);
                        MTextView qtyTxt = (MTextView) editCartView.findViewById(R.id.qtyTxt);
                        MTextView commentHTxt = (MTextView) editCartView.findViewById(R.id.commentHTxt);
                        MTextView commentVTxt = (MTextView) editCartView.findViewById(R.id.commentVTxt);

                        serviceNameTxt.setText(generalFunc.getJsonValue("title", data.toString()));
                        qtyTxt.setText(generalFunc.convertNumberWithRTL(generalFunc.getJsonValue("Qty", data.toString())));
                        commentHTxt.setText(generalFunc.retrieveLangLBl("", "LBL_SPECIAL_INSTRUCTION_TXT") + " : ");
                        commentVTxt.setText(generalFunc.getJsonValue("comment", data.toString()).equals("") ? generalFunc.retrieveLangLBl("", "LBL_NO_SPECIAL_INSTRUCTION") : generalFunc.getJsonValue("comment", data.toString()));

                        itemContainer.addView(editCartView);
                    }

                } else {
                    generalFunc.showGeneralMessage("",
                            generalFunc.retrieveLangLBl("", generalFunc.getJsonValueStr(Utils.message_str, responseStringObject)), true);
                }
            } else {
                generalFunc.showError(true);
            }

            loadingBar.setVisibility(View.GONE);
        });
        exeWebServer.execute();
    }

    public Context getActContext() {
        return MoreServiceInfoActivity.this;
    }


    public class setOnClickList implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            Utils.hideKeyboard(MoreServiceInfoActivity.this);
            switch (view.getId()) {
                case R.id.backImgView:
                    MoreServiceInfoActivity.super.onBackPressed();
                    break;
            }
        }
    }
}
