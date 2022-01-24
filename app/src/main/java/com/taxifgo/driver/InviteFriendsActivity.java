package com.taxifgo.driver;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

import com.general.files.GeneralFunctions;
import com.general.files.MyApp;
import com.utils.Utils;
import com.view.MButton;
import com.view.MTextView;
import com.view.MaterialRippleLayout;

import org.json.JSONObject;

/**
 * Created by Admin on 03-11-2016.
 */
public class InviteFriendsActivity extends AppCompatActivity {

    private MButton btn_type3;
    MTextView titleTxt, shareTxtLbl, invitecodeTxt, shareTxt;
    ImageView backImgView;

    JSONObject userProfileJsonObj;
    String vRefCode = "";
    String LBL_INVITE_FRIEND_TXT="";
    GeneralFunctions generalFunc;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invite_friends);


        init();


    }

    private void init() {

        generalFunc = MyApp.getInstance().getGeneralFun(getActContext());

        //userProfileJson = getIntent().getStringExtra("UserProfileJson");
        userProfileJsonObj = generalFunc.getJsonObject(generalFunc.retrieveValue(Utils.USER_PROFILE_JSON));

        titleTxt = (MTextView) findViewById(R.id.titleTxt);
        shareTxtLbl = (MTextView) findViewById(R.id.shareTxtLbl);
        invitecodeTxt = (MTextView) findViewById(R.id.invitecodeTxt);
        shareTxt = (MTextView) findViewById(R.id.shareTxt);
        backImgView = (ImageView) findViewById(R.id.backImgView);

        btn_type3 = ((MaterialRippleLayout) findViewById(R.id.btn_type3)).getChildView();
        btn_type3.setId(Utils.generateViewId());

        setLabels();

        btn_type3.setOnClickListener(new setOnClickList());
        backImgView.setOnClickListener(new setOnClickList());
    }

    public void setLabels() {
        LBL_INVITE_FRIEND_TXT=generalFunc.retrieveLangLBl("", "LBL_INVITE_FRIEND_TXT");

        titleTxt.setText(LBL_INVITE_FRIEND_TXT);
        btn_type3.setText(LBL_INVITE_FRIEND_TXT);
        shareTxtLbl.setText(generalFunc.retrieveLangLBl("", "LBL_INVITE_FRIEND_SHARE"));
//        shareTxt.setText(generalFunc.retrieveLangLBl("", "LBL_INVITE_FRIEND_SHARE_TXT"));
        shareTxt.setText(generalFunc.convertNumberWithRTL(generalFunc.getJsonValueStr("INVITE_DESCRIPTION_CONTENT", userProfileJsonObj)));
        vRefCode = generalFunc.getJsonValueStr("vRefCode", userProfileJsonObj);

        invitecodeTxt.setText(vRefCode.trim());

    }

    public Context getActContext() {
        return InviteFriendsActivity.this;
    }


    public class setOnClickList implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            int i = view.getId();
            Utils.hideKeyboard(InviteFriendsActivity.this);
            if (i == R.id.backImgView) {
                InviteFriendsActivity.super.onBackPressed();

            } else if (i == btn_type3.getId()) {
                Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                sharingIntent.putExtra(Intent.EXTRA_SUBJECT, LBL_INVITE_FRIEND_TXT);
//                sharingIntent.putExtra(Intent.EXTRA_TEXT, generalFunc.retrieveLangLBl("", "SHARE_CONTENT") + " " + generalFunc.retrieveLangLBl("", "MY_REFERAL_CODE") + " : " + vRefCode.trim());
                sharingIntent.putExtra(Intent.EXTRA_TEXT, generalFunc.getJsonValueStr("INVITE_SHARE_CONTENT", userProfileJsonObj));
                startActivity(Intent.createChooser(sharingIntent, generalFunc.retrieveLangLBl("", "LBL_SHARE_USING")));
            }
        }
    }


}

