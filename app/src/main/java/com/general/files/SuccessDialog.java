package com.general.files;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDialog;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;


import com.taxifgo.driver.R;
import com.view.MButton;
import com.view.MTextView;
import com.view.MaterialRippleLayout;

import java.util.Objects;

public class SuccessDialog extends AppCompatDialog implements View.OnClickListener {

    private String message = "";
    private String messageNote = "";
    private String setButtonText = "";
    private View.OnClickListener mClickListener = null;
    private OnClickList clickListener = null;

    public SuccessDialog(@NonNull Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        Objects.requireNonNull(getWindow()).setBackgroundDrawableResource(android.R.color.transparent);
        setContentView(R.layout.layout_success_dialog);


        MTextView messageTextView = (MTextView) findViewById(R.id.messageTextView);
        MTextView messageNoteTextView = (MTextView) findViewById(R.id.messageNoteTextView);
        messageTextView.setText(message);
        MButton btn_type2 = ((MaterialRippleLayout) findViewById(R.id.btn_type2)).getChildView();
        btn_type2.setOnClickListener(this);
        btn_type2.setText(setButtonText);

        if (messageNote.equalsIgnoreCase("")) {
            messageNoteTextView.setVisibility(View.GONE);
        } else {
            messageNoteTextView.setVisibility(View.VISIBLE);
            messageNoteTextView.setText(messageNote);
        }

    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setMessageNote(String messageNote) {
        this.messageNote = messageNote;
    }

    public void setButtonText(String ButtonText) {
        this.setButtonText = ButtonText;
    }

    public static SuccessDialog showSuccessDialog(Context mContext, String message, String messageNote, String buttonText, boolean isCancelable, OnClickList clickListener) {

        SuccessDialog successDialog = new SuccessDialog(mContext);

        successDialog.setCancelable(isCancelable);
        successDialog.setMessage(message);
        successDialog.setMessageNote(messageNote);
        successDialog.setButtonText(buttonText);
        successDialog.clickListener = clickListener;
        successDialog.show();

        Window window = successDialog.getWindow();
        window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);



        return successDialog;
    }

    @Override
    public void onClick(View v) {
        dismiss();

        if (clickListener != null) {
            clickListener.onClick();
        }
    }

    public interface OnClickList {
        void onClick();
    }
}
