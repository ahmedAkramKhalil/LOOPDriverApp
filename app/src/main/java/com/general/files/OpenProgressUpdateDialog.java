package com.general.files;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;

import androidx.core.content.FileProvider;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.taxifgo.driver.BuildConfig;
import com.taxifgo.driver.R;
import com.utils.Logger;
import com.view.GenerateAlertBox;
import com.view.MTextView;

public class OpenProgressUpdateDialog implements Runnable {

    Context mContext;
    GeneralFunctions generalFunc;
    ProgressBar mProgress;
    ProgressBar simpleProgressbar;
    MTextView progressTxt;
    public   Dialog dialog_img_update;
    UploadProfileImage uploadProfileImage;
    ImageView cancelUpload;

    public OpenProgressUpdateDialog(Context mContext, GeneralFunctions generalFunc,UploadProfileImage uploadProfileImage) {
        this.mContext = mContext;
        this.generalFunc = generalFunc;
        this.uploadProfileImage = uploadProfileImage;
    }
    @Override
    public void run() {
        if (mContext instanceof Activity == false) {
            Logger.e(BuildConfig.APPLICATION_ID, "Context must be instance of Activity OR Fragment");
            return;
        }

        dialog_img_update = new Dialog(mContext, R.style.ImageSourceDialogStyle);

        dialog_img_update.setContentView(R.layout.dialog_progress_update);

        MTextView pleasewaitTxt = (MTextView) dialog_img_update.findViewById(R.id.pleasewaitTxt);
        MTextView uploadingTxt = (MTextView) dialog_img_update.findViewById(R.id.uploadingTxt);
        progressTxt = (MTextView) dialog_img_update.findViewById(R.id.progressTxt);
        pleasewaitTxt.setText(generalFunc.retrieveLangLBl("Please Wait", "LBL_PLEASE_WAIT"));
        uploadingTxt.setText(generalFunc.retrieveLangLBl("your documet is uploading", "LBL_DOCUMET_UPLOADING"));

         cancelUpload = dialog_img_update.findViewById(R.id.cancelUpload);
        cancelUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final GenerateAlertBox generateAlert = new GenerateAlertBox(mContext);
                generateAlert.setCancelable(false);
                generateAlert.setBtnClickList(btn_id -> {
                    if (btn_id == 1) {
                        uploadProfileImage.cancel(true);
                        // dialog_img_update.cancel();

                    } else if (btn_id == 0) {
                        generateAlert.closeAlertBox();
                    }
                });
                generateAlert.setContentMessage("", generalFunc.retrieveLangLBl("Are you sure you want to cancel upload?","LBL_SURE_CANCEL_UPLOAD"));
                generateAlert.setPositiveBtn(generalFunc.retrieveLangLBl("", "LBL_BTN_OK_TXT"));
                generateAlert.setNegativeBtn(generalFunc.retrieveLangLBl("", "LBL_BTN_CANCEL_TRIP_TXT"));
                generateAlert.showAlertBox();

            }
        });


        mProgress = (ProgressBar) dialog_img_update.findViewById(R.id.circularProgressbar);

        simpleProgressbar = (ProgressBar) dialog_img_update.findViewById(R.id.simpleProgressbar);


        dialog_img_update.setCanceledOnTouchOutside(false);
        dialog_img_update.setCancelable(false);

        Window window = dialog_img_update.getWindow();
        window.setGravity(Gravity.BOTTOM);

        window.setLayout(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        dialog_img_update.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        if (generalFunc.isRTLmode()) {
            dialog_img_update.getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        }
        dialog_img_update.show();
    }


    public void updateProgress(int progress) {
        progressTxt.setText(""+progress+"%");
        mProgress.setProgress(progress);
        if (progress==100){
            mProgress.setVisibility(View.GONE);
            simpleProgressbar.setVisibility(View.VISIBLE);
            cancelUpload.setVisibility(View.GONE);

        }
    }
}
