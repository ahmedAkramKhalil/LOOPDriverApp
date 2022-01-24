package com.general.files;

import android.app.Activity;
import android.content.Context;

import androidx.core.content.ContextCompat;

import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.taxifgo.driver.MainActivity;
import com.taxifgo.driver.R;
import com.taxifgo.driver.deliverAll.LiveTaskListActivity;
import com.view.MTextView;


import org.json.JSONObject;


public class AddBottomBar {


    public JSONObject userProfileJson;
    Context mContext;
    View view;

    public LinearLayout profileArea, homeArea, historyArea, walletArea;
    MTextView historyTxt, walletTxt, profileTxt, homeTxt;
    ImageView home_img, bookingImg, walletImg, profileImg;
    GeneralFunctions generalFunc;


    public AddBottomBar(Context mContext, JSONObject userProfileJson) {

        this.mContext = mContext;
        this.userProfileJson = userProfileJson;
        generalFunc = new GeneralFunctions(mContext);
        view = ((Activity) mContext).findViewById(android.R.id.content);


        historyTxt = view.findViewById(R.id.historyTxt);
        walletTxt = view.findViewById(R.id.walletTxt);
        profileTxt = view.findViewById(R.id.profileTxt);
        homeTxt = view.findViewById(R.id.homeTxt);
        home_img = view.findViewById(R.id.home_img);
        bookingImg = view.findViewById(R.id.bookingImg);
        walletImg = view.findViewById(R.id.walletImg);
        profileImg = view.findViewById(R.id.profileImg);


        historyArea = view.findViewById(R.id.historyArea);
        walletArea = view.findViewById(R.id.walletArea);
        profileArea = view.findViewById(R.id.profileArea);
        homeArea = view.findViewById(R.id.homeArea);
        profileArea.setOnClickListener(new setOnClickList());
        homeArea.setOnClickListener(new setOnClickList());
        walletArea.setOnClickListener(new setOnClickList());
        historyArea.setOnClickListener(new setOnClickList());
        manageBottomMenu(homeTxt);
        setLabel();
    }

    public void setLabel() {
        profileTxt.setText(generalFunc.retrieveLangLBl("", "LBL_HEADER_RDU_PROFILE"));

        homeTxt.setText(generalFunc.retrieveLangLBl("", "LBL_HOME"));
        walletTxt.setText(generalFunc.retrieveLangLBl("", "LBL_HEADER_RDU_WALLET"));
        historyTxt.setText(generalFunc.retrieveLangLBl("", "LBL_HEADER_RDU_BOOKINGS"));

    }

    public void manageBottomMenu(MTextView selTextView) {
        //manage Select deselect Bottom Menu
        if (selTextView.getId() == homeTxt.getId()) {
            homeTxt.setTextColor(mContext.getResources().getColor(R.color.appThemeColor_1));
            home_img.setColorFilter(ContextCompat.getColor(mContext, R.color.appThemeColor_1), android.graphics.PorterDuff.Mode.SRC_IN);
        } else {
            homeTxt.setTextColor(mContext.getResources().getColor(R.color.homedeSelectColor));
            home_img.setColorFilter(ContextCompat.getColor(mContext, R.color.homedeSelectColor), android.graphics.PorterDuff.Mode.SRC_IN);
        }

        if (selTextView.getId() == historyTxt.getId()) {
            historyTxt.setTextColor(mContext.getResources().getColor(R.color.appThemeColor_1));
            bookingImg.setColorFilter(ContextCompat.getColor(mContext, R.color.appThemeColor_1), android.graphics.PorterDuff.Mode.SRC_IN);
        } else {
            historyTxt.setTextColor(mContext.getResources().getColor(R.color.homedeSelectColor));
            bookingImg.setColorFilter(ContextCompat.getColor(mContext, R.color.homedeSelectColor), android.graphics.PorterDuff.Mode.SRC_IN);
        }
        if (selTextView.getId() == walletTxt.getId()) {
            walletTxt.setTextColor(mContext.getResources().getColor(R.color.appThemeColor_1));
            walletImg.setColorFilter(ContextCompat.getColor(mContext, R.color.appThemeColor_1), android.graphics.PorterDuff.Mode.SRC_IN);
        } else {
            walletTxt.setTextColor(mContext.getResources().getColor(R.color.homedeSelectColor));
            walletImg.setColorFilter(ContextCompat.getColor(mContext, R.color.homedeSelectColor), android.graphics.PorterDuff.Mode.SRC_IN);
        }
        if (selTextView.getId() == profileTxt.getId()) {
            profileTxt.setTextColor(mContext.getResources().getColor(R.color.appThemeColor_1));
            profileImg.setColorFilter(ContextCompat.getColor(mContext, R.color.appThemeColor_1), android.graphics.PorterDuff.Mode.SRC_IN);
        } else {
            profileTxt.setTextColor(mContext.getResources().getColor(R.color.homedeSelectColor));
            profileImg.setColorFilter(ContextCompat.getColor(mContext, R.color.homedeSelectColor), android.graphics.PorterDuff.Mode.SRC_IN);
        }

    }


    public class setOnClickList implements View.OnClickListener {

        @Override
        public void onClick(View view) {

            if (view.getId() == profileArea.getId()) {
                manageBottomMenu(profileTxt);
                if (mContext instanceof MainActivity) {
                    MainActivity mainActivity = (MainActivity) mContext;
                    mainActivity.openProfileFragment();
                } else if (mContext instanceof LiveTaskListActivity) {
                    LiveTaskListActivity liveTaskListActivity = (LiveTaskListActivity) mContext;
                    liveTaskListActivity.openProfileFragment();
                }
            } else if (view.getId() == homeArea.getId()) {
                manageBottomMenu(homeTxt);
                if (mContext instanceof MainActivity) {
                    MainActivity mainActivity = (MainActivity) mContext;
                    mainActivity.manageHome();
                } else if (mContext instanceof LiveTaskListActivity) {
                    LiveTaskListActivity liveTaskListActivity = (LiveTaskListActivity) mContext;
                    liveTaskListActivity.manageHome();
                }

            } else if (view.getId() == historyArea.getId()) {
                manageBottomMenu(historyTxt);

                if (mContext instanceof MainActivity) {
                    MainActivity mainActivity = (MainActivity) mContext;
                    mainActivity.openBookingFrgament();
                } else if (mContext instanceof LiveTaskListActivity) {
                    LiveTaskListActivity liveTaskListActivity = (LiveTaskListActivity) mContext;
                    liveTaskListActivity.openBookingFrgament();
                }


            } else if (view.getId() == walletArea.getId()) {
                manageBottomMenu(walletTxt);
                if (mContext instanceof MainActivity) {
                    MainActivity mainActivity = (MainActivity) mContext;
                    mainActivity.openWalletFrgament();
                } else if (mContext instanceof LiveTaskListActivity) {
                    LiveTaskListActivity liveTaskListActivity = (LiveTaskListActivity) mContext;
                    liveTaskListActivity.openWalletFragment();
                }

            }


        }
    }


}
