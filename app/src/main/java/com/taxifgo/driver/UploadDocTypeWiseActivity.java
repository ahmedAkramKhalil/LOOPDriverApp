package com.taxifgo.driver;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.general.files.GeneralFunctions;
import com.general.files.MyApp;
import com.general.files.StartActProcess;
import com.utils.Utils;
import com.view.MTextView;

public class UploadDocTypeWiseActivity extends AppCompatActivity {


    LinearLayout uberxArea, rideArea;
    GeneralFunctions generalFunctions;
    MTextView ridetitleTxt, deliverytitleTxt, uberxtitleTxt;
    MTextView titleTxt;
    ImageView backImgView;

    public static int ADDVEHICLE = 1;

    int totalVehicles = 0;
    String app_type;
    String userProfileJson;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_doc_type_wise);
        generalFunctions = MyApp.getInstance().getGeneralFun(getActContext());
        initView();
    }

    public void initView() {
        uberxArea = (LinearLayout) findViewById(R.id.uberxArea);
        rideArea = (LinearLayout) findViewById(R.id.rideArea);
        ridetitleTxt = (MTextView) findViewById(R.id.ridetitleTxt);
        deliverytitleTxt = (MTextView) findViewById(R.id.deliverytitleTxt);
        uberxtitleTxt = (MTextView) findViewById(R.id.uberxtitleTxt);
        backImgView = (ImageView) findViewById(R.id.backImgView);
        titleTxt = (MTextView) findViewById(R.id.titleTxt);
        uberxArea.setOnClickListener(new setOnClickList());
        rideArea.setOnClickListener(new setOnClickList());
        backImgView.setOnClickListener(new setOnClickList());

        userProfileJson = generalFunctions.retrieveValue(Utils.USER_PROFILE_JSON);
        app_type = generalFunctions.getJsonValue("APP_TYPE", userProfileJson);

        totalVehicles = getIntent().getIntExtra("totalVehicles", 0);

        titleTxt.setText(generalFunctions.retrieveLangLBl("", "LBL_SELECT_TYPE"));

        if (getIntent().getStringExtra("selView").equalsIgnoreCase("doc")) {
            ridetitleTxt.setText(generalFunctions.retrieveLangLBl("", "LBL_UPLOAD_DOC"));
            deliverytitleTxt.setText(generalFunctions.retrieveLangLBl("", "LBL_UPLOAD_DOC_DELIVERY"));
            uberxtitleTxt.setText(generalFunctions.retrieveLangLBl("", "LBL_UPLOAD_DOC_UFX"));
        } else {
            ridetitleTxt.setText(generalFunctions.retrieveLangLBl("", "LBL_MANAGE_VEHICLES"));
            uberxtitleTxt.setText(generalFunctions.retrieveLangLBl("", "LBL_MANANGE_OTHER_SERVICES"));
        }

        if (generalFunctions.isRTLmode()) {
            ((ImageView) findViewById(R.id.imagearrow)).setRotationY(180);
            ((ImageView) findViewById(R.id.delimagearrow)).setRotationY(180);
            ((ImageView) findViewById(R.id.uberximagearrow)).setRotationY(180);
        }
    }

    public Context getActContext() {
        return UploadDocTypeWiseActivity.this;
    }


    public class setOnClickList implements View.OnClickListener {

        @Override
        public void onClick(View view) {

            Bundle bn = new Bundle();
            bn.putString("PAGE_TYPE", "Driver");
            bn.putString("iDriverVehicleId", "");
            bn.putString("doc_file", "");
            bn.putString("iDriverVehicleId", "");
            Utils.hideKeyboard(UploadDocTypeWiseActivity.this);
            switch (view.getId()) {
                case R.id.backImgView:
                    UploadDocTypeWiseActivity.super.onBackPressed();
                    break;
                case R.id.rideArea:
                    if (getIntent().getStringExtra("selView").equalsIgnoreCase("doc")) {
                        new StartActProcess(getActContext()).startActWithData(ListOfDocumentActivity.class, bn);
                    } else {
                        if (totalVehicles > 0) {
                            new StartActProcess(getActContext()).startActWithData(ManageVehiclesActivity.class, bn);
                        } else {
                            new StartActProcess(getActContext()).startActForResult(AddVehicleActivity.class, bn, ADDVEHICLE);
                        }
                    }
                    break;

                case R.id.uberxArea:
                    if (getIntent().getStringExtra("selView").equalsIgnoreCase("doc")) {
                        bn.putString("seltype", Utils.CabGeneralType_UberX);
                        new StartActProcess(getActContext()).startActWithData(ListOfDocumentActivity.class, bn);
                        break;
                    } else {
                        bn.putString("UBERX_PARENT_CAT_ID", getIntent().getStringExtra("UBERX_PARENT_CAT_ID"));
                        new StartActProcess(getActContext()).startActWithData(UfxCategoryActivity.class, bn);
                    }
            }
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {

            if (data.getStringExtra("iDriverVehicleId") != null && !data.getStringExtra("iDriverVehicleId").equalsIgnoreCase
                    ("")) {
                totalVehicles = 1;
                if (app_type.equalsIgnoreCase(Utils.CabGeneralTypeRide_Delivery)) {
                    if (totalVehicles > 0) {
                        ridetitleTxt.setText(generalFunctions.retrieveLangLBl("", "LBL_MANANGE_VEHICLES_RIDE"));
                        deliverytitleTxt.setText(generalFunctions.retrieveLangLBl("", "LBL_MANANGE_VEHICLES_DELIVERY"));
                    } else {
                        ridetitleTxt.setText(generalFunctions.retrieveLangLBl("", "LBL_ADD_VEHICLES_RIDE"));
                        deliverytitleTxt.setText(generalFunctions.retrieveLangLBl("", "LBL_ADD_VEHICLES_DELIVERY"));
                    }
                } else {
                    ridetitleTxt.setText(generalFunctions.retrieveLangLBl("", "LBL_MANAGE_VEHICLES"));
                    deliverytitleTxt.setText(generalFunctions.retrieveLangLBl("", "LBL_MANANGE_VEHICLES_DELIVERY"));
                    uberxtitleTxt.setText(generalFunctions.retrieveLangLBl("", "LBL_MANANGE_OTHER_SERVICES"));
                }
            }
        }
    }
}
