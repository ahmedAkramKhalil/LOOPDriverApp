package com.taxifgo.driver;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import com.general.files.GeneralFunctions;
import com.general.files.MyApp;
import com.utils.Logger;
import com.utils.Utils;
import com.view.MTextView;

public class SubscribedPlanConfirmationActivity extends AppCompatActivity {

    MTextView tv_tap_anywhere;
    MTextView thanksTxt;
    MTextView subscribedTxt;
    public GeneralFunctions generalFunc;

    MTextView titleTxt;
    ImageView backImgView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subscription_purchased);

        generalFunc = MyApp.getInstance().getGeneralFun(getActContext());

        initView();
        setLables();

        findViewById(R.id.contentArea).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                backImgView.performClick();
                return true;
            }
        });

        new Handler().postDelayed(() -> {
            backImgView.performClick();
        }, 10000);

    }

    private Context getActContext() {
        return SubscribedPlanConfirmationActivity.this;
    }

    private void setLables() {
        tv_tap_anywhere.setText(generalFunc.retrieveLangLBl("", "LBL_TAP_TO_GOBACK_TXT"));
        thanksTxt.setText(generalFunc.retrieveLangLBl("", "LBL_SUBSCRIBED_THANK_YOU_TXT"));
        subscribedTxt.setText(generalFunc.retrieveLangLBl("", "LBL_SUBSCRIBED_DESCRIPTION_TXT"));
        titleTxt.setText(generalFunc.retrieveLangLBl("", "LBL_SUBSCRIPTION_COMPLETED_TITLE_TXT"));
    }

    private void initView() {
        tv_tap_anywhere = findViewById(R.id.tv_tap_anywhere);
        thanksTxt = findViewById(R.id.thanksTxt);
        subscribedTxt = findViewById(R.id.subscribedTxt);
        titleTxt = findViewById(R.id.titleTxt);
        backImgView = findViewById(R.id.backImgView);
        backImgView.setVisibility(View.GONE);
        backImgView.setOnClickListener(new setOnClickList());
    }

    public class setOnClickList implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            Utils.hideKeyboard(SubscribedPlanConfirmationActivity.this);
            int i = view.getId();
            if (i == R.id.backImgView) {
                Logger.d("DEBUG", "TRANSACTION_COMPLETED::ON BACK PRESS" );
                Intent returnIntent = new Intent();
                setResult(Activity.RESULT_OK, returnIntent);
                finish();
            }

        }
    }

    @Override
    public void onBackPressed() {
        return;
    }
}
