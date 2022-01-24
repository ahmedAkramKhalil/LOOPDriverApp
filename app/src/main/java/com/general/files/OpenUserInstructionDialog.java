package com.general.files;

import android.content.Context;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;

import com.taxifgo.driver.R;
import com.view.MTextView;

import java.util.HashMap;

public class OpenUserInstructionDialog {

    Context mContext;
    HashMap<String, String> data_trip;
    GeneralFunctions generalFunc;

    androidx.appcompat.app.AlertDialog alertDialog;

    ProgressBar LoadingProgressBar;
    boolean isnotification;

    public OpenUserInstructionDialog(Context mContext, HashMap<String, String> data_trip, GeneralFunctions generalFunc, boolean isnotification) {
        this.mContext = mContext;
        this.data_trip = data_trip;
        this.generalFunc = generalFunc;
        this.isnotification = isnotification;

        show();
    }

    public void show() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(mContext);
        builder.setTitle("");

        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View dialogView = inflater.inflate(R.layout.design_user_instruction_dialog, null);
        builder.setView(dialogView);

        LoadingProgressBar = ((ProgressBar) dialogView.findViewById(R.id.LoadingProgressBar));


        String insTitle = "";
        String insClose = "";

        insTitle = generalFunc.retrieveLangLBl("", "LBL_USER_INSTRUCTION");
        insClose = generalFunc.retrieveLangLBl("", "LBL_CLOSE_TXT");
        ((MTextView) dialogView.findViewById(R.id.userITxt)).setText(insTitle);
        ((MTextView) dialogView.findViewById(R.id.closeTxtArea)).setText(insClose);
        ((MTextView) dialogView.findViewById(R.id.userInsTxt)).setText(data_trip.get("tRidersIns"));
        ((MTextView) dialogView.findViewById(R.id.userInsTxt)).setMovementMethod(new ScrollingMovementMethod());
        (dialogView.findViewById(R.id.closeTxtArea)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (alertDialog != null) {
                    alertDialog.dismiss();
                }

            }
        });


        alertDialog = builder.create();
        if (generalFunc.isRTLmode() == true) {
            generalFunc.forceRTLIfSupported(alertDialog);
        }
        alertDialog.show();
        if (isnotification) {
            isnotification = false;
            dialogView.findViewById(R.id.msgArea).performClick();
        }
    }


}
