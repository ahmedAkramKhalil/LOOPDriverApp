package com.general.files;

import android.content.Context;

import com.taxifgo.driver.AppLoignRegisterActivity;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.models.User;
import com.utils.Logger;
import com.utils.Utils;
import com.view.MyProgressDialog;

import java.util.HashMap;

import retrofit2.Call;

/**
 * Created by Admin on 29-06-2016.
 */
public class RegisterLinkedinLoginResCallBack {
    Context mContext;
    GeneralFunctions generalFunc;

    MyProgressDialog myPDialog;
    AppLoignRegisterActivity appLoginAct;

    public RegisterLinkedinLoginResCallBack(Context mContext) {
        this.mContext = mContext;

        generalFunc = MyApp.getInstance().getGeneralFun(mContext);
        appLoginAct = (AppLoignRegisterActivity) mContext;

    }


    public void continueLogin() {
        OpenLinkedinDialog openLinkedinDialog = new OpenLinkedinDialog(mContext, generalFunc);

    }


}
