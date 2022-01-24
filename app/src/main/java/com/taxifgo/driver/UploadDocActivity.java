package com.taxifgo.driver;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.datepicker.files.SlideDateTimeListener;
import com.datepicker.files.SlideDateTimePicker;
import com.general.files.GeneralFunctions;
import com.general.files.ImageFilePath;
import com.general.files.MyApp;
import com.general.files.OpenSourceSelectionDialog;
import com.general.files.StartActProcess;
import com.general.files.UploadProfileImage;
import com.utils.CommonUtilities;
import com.utils.Logger;
import com.utils.Utils;
import com.view.CreateRoundedView;
import com.view.GenerateAlertBox;
import com.view.MButton;
import com.view.MTextView;
import com.view.MaterialRippleLayout;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

public class UploadDocActivity extends AppCompatActivity {

    MTextView titleTxt;
    ImageView backImgView;
    GeneralFunctions generalFunc;
    MButton btn_type2;
    MTextView helpInfoTxtView;
    ImageView dummyInfoCardImgView;
    private Uri fileUri;

    MTextView expBox;
    MTextView expBoxLBL;
    MTextView expBoxTXT;
    FrameLayout expDateSelectArea;

    String selectedDocumentPath = "";

    ImageView imgeselectview;
    public boolean isuploadimageNew = true;

    boolean isbtnclick = false;

    String[] tsite_upload_docs_file_extensions_arr = null;
    private JSONObject obj_userProfile;

    String pathForCameraImage = "";
    boolean isDatePickerOpen = false;

    RelativeLayout selectyear_layout;
    UploadProfileImage uploadProfileImage;
    LinearLayout editArea;
    MTextView editTxtView, viewTxtView;
    String vimage = "";
    View editView;

    @Override
    public void finishActivity(int requestCode) {
        super.finishActivity(requestCode);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_doc);

        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        generalFunc = MyApp.getInstance().getGeneralFun(getActContext());
        if (!getIntent().getStringExtra("vimage").equalsIgnoreCase("")) {
            vimage = getIntent().getStringExtra("vimage");
        }

        editView = findViewById(R.id.editView);
        titleTxt = (MTextView) findViewById(R.id.titleTxt);
        backImgView = (ImageView) findViewById(R.id.backImgView);
        dummyInfoCardImgView = (ImageView) findViewById(R.id.dummyInfoCardImgView);
        helpInfoTxtView = (MTextView) findViewById(R.id.helpInfoTxtView);
        expBox = (MTextView) findViewById(R.id.expBox);
        expBoxLBL = (MTextView) findViewById(R.id.expBoxLBL);
        expBoxTXT = (MTextView) findViewById(R.id.expBoxTxt);
        imgeselectview = (ImageView) findViewById(R.id.imgeselectview);
        expDateSelectArea = (FrameLayout) findViewById(R.id.expDateSelectArea);
        selectyear_layout = (RelativeLayout) findViewById(R.id.selectyear_layout);
        editArea = (LinearLayout) findViewById(R.id.editArea);
        editTxtView = (MTextView) findViewById(R.id.editTxtView);
        viewTxtView = (MTextView) findViewById(R.id.viewTxtView);
        editTxtView.setOnClickListener(new setOnClickList());
        viewTxtView.setOnClickListener(new setOnClickList());

        btn_type2 = ((MaterialRippleLayout) findViewById(R.id.btn_type2)).getChildView();
        backImgView.setOnClickListener(new setOnClickList());

        btn_type2.setId(Utils.generateViewId());
        btn_type2.setOnClickListener(new setOnClickList());
        helpInfoTxtView.setOnClickListener(new setOnClickList());
        dummyInfoCardImgView.setOnClickListener(new setOnClickList());

        String userProfileJson = generalFunc.retrieveValue(Utils.USER_PROFILE_JSON);
        obj_userProfile = generalFunc.getJsonObject(userProfileJson);

        tsite_upload_docs_file_extensions_arr = generalFunc.getJsonValueStr("tsite_upload_docs_file_extensions", obj_userProfile).trim().replaceAll(" ", "").split(",");

        new CreateRoundedView(Color.parseColor("#ffffff"), 8, 2, Color.parseColor("#9c9c9c"), selectyear_layout);

        setLabels();


        if (getIntent().getStringExtra("allow_date_change").equalsIgnoreCase("No")) {
            btn_type2.setEnabled(false);
            ((MTextView) findViewById(R.id.noteTxt)).setText(getIntent().getStringExtra("doc_update_disable"));
        }
    }

    public void setLabels() {
        titleTxt.setText(generalFunc.retrieveLangLBl("", "LBL_UPLOAD_DOC"));
        editTxtView.setText(generalFunc.retrieveLangLBl("", "LBL_EDIT"));
        viewTxtView.setText(generalFunc.retrieveLangLBl("", "LBL_VIEW"));
        btn_type2.setText(generalFunc.retrieveLangLBl("", "LBL_BTN_SUBMIT_TXT"));
        helpInfoTxtView.setText(generalFunc.retrieveLangLBl("", "LBL_SELECT_DOC"));
        expBox.setText(generalFunc.retrieveLangLBl("", "LBL_SELECT_TXT"));
        expBoxLBL.setText(generalFunc.retrieveLangLBl("", "LBL_EXPIRY_DATE"));
        // expBoxLBL.setBothText("Select");

        if (getIntent().getStringExtra("ex_status").equals("yes")) {
            expBoxTXT.setText(getIntent().getStringExtra("ex_date"));
            // expBox.setBothText("Select");
            expDateSelectArea.setVisibility(View.VISIBLE);
        } else {
            expDateSelectArea.setVisibility(View.GONE);
        }

        String doc_file = getIntent().getStringExtra("doc_file");
        if (!doc_file.equals("")) {
            selectedDocumentPath = doc_file;
            imgeselectview.setVisibility(View.VISIBLE);
            helpInfoTxtView.setVisibility(View.GONE);
            editArea.setVisibility(View.VISIBLE);

            if (!vimage.equalsIgnoreCase("")) {
                viewTxtView.setVisibility(View.VISIBLE);
                editView.setVisibility(View.VISIBLE);

            } else {
                viewTxtView.setVisibility(View.GONE);
                editView.setVisibility(View.GONE);
            }

            // dummyInfoCardImgView.setAlpha(0.2f);
            dummyInfoCardImgView.setAlpha(0.2f);
            dummyInfoCardImgView.setImageDrawable(getResources().getDrawable(R.drawable.ic_card_documents));
            isuploadimageNew = false;
        }


        // Utils.removeInput(expBox);
        selectyear_layout.setOnTouchListener(new setOnTouchList());
        selectyear_layout.setOnClickListener(new setOnClickList());
    }


    public class setOnTouchList implements View.OnTouchListener {

        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (motionEvent.getAction() == MotionEvent.ACTION_UP && !view.hasFocus()) {
                view.performClick();
            }
            return true;
        }
    }

    public Context getActContext() {
        return UploadDocActivity.this;
    }


    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save the user's current state
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putParcelable("file_uri", fileUri);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        // get the file url
        fileUri = savedInstanceState.getParcelable("file_uri");
    }

    public void checkData() {


        if (selectedDocumentPath.equals("")) {
            generalFunc.showMessage(generalFunc.getCurrentView((Activity) getActContext()), generalFunc.retrieveLangLBl("Please attach your document.", "LBL_SELECT_DOC_ERROR"));
            return;
        }
        if (expDateSelectArea.getVisibility() == View.VISIBLE && !Utils.checkText(expBoxTXT.getText().toString())) {
            generalFunc.showMessage(generalFunc.getCurrentView((Activity) getActContext()), generalFunc.retrieveLangLBl("Expiry date is required.", "LBL_EXP_DATE_REQUIRED"));
            return;
        }

        if (isbtnclick) {
            return;
        }
        isbtnclick = true;
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                isbtnclick = false;
            }
        }, 1000);

        ArrayList<String[]> paramsList = new ArrayList<>();
        paramsList.add(Utils.generateImageParams("type", "uploaddrivedocument"));
        paramsList.add(Utils.generateImageParams("iMemberId", generalFunc.getMemberId()));
        paramsList.add(Utils.generateImageParams("MemberType", Utils.app_type));
        paramsList.add(Utils.generateImageParams("doc_usertype", getIntent().getStringExtra("PAGE_TYPE")));
        paramsList.add(Utils.generateImageParams("doc_masterid", getIntent().getStringExtra("doc_masterid")));
        paramsList.add(Utils.generateImageParams("doc_name", getIntent().getStringExtra("doc_name")));
        paramsList.add(Utils.generateImageParams("doc_id", getIntent().getStringExtra("doc_id")));
        paramsList.add(Utils.generateImageParams("tSessionId", generalFunc.getMemberId().equals("") ? "" : generalFunc.retrieveValue(Utils.SESSION_ID_KEY)));
        paramsList.add(Utils.generateImageParams("GeneralUserType", Utils.app_type));
        paramsList.add(Utils.generateImageParams("GeneralMemberId", generalFunc.getMemberId()));
        paramsList.add(Utils.generateImageParams("ex_date", getIntent().getStringExtra("ex_status").equals("yes") ? generalFunc.getDateFormatedType(Utils.getText(expBoxTXT),CommonUtilities.WithoutDayFormat,"yyyy-MM-dd") : ""));
        if (!getIntent().getStringExtra("iDriverVehicleId").equals("")) {
            paramsList.add(Utils.generateImageParams("iDriverVehicleId", getIntent().getStringExtra("iDriverVehicleId")));
        }

        if (!getIntent().getStringExtra("doc_file").equals("")) {

            if (isuploadimageNew) {
                uploadProfileImage = new UploadProfileImage(UploadDocActivity.this, selectedDocumentPath, "TempFile." + Utils.getFileExt(selectedDocumentPath), paramsList, "FILE");
                uploadProfileImage.execute(true);
            } else {
                paramsList.add(Utils.generateImageParams("doc_file", selectedDocumentPath));
                uploadProfileImage = new UploadProfileImage(UploadDocActivity.this, "", "TempFile." + Utils.getFileExt(selectedDocumentPath), paramsList, "FILE");
                uploadProfileImage.execute(false);
            }
        } else {
            uploadProfileImage = new UploadProfileImage(UploadDocActivity.this, selectedDocumentPath, "TempFile." + Utils.getFileExt(selectedDocumentPath), paramsList, "FILE");
            uploadProfileImage.execute(true);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        String selPath = "";
        if (requestCode == OpenSourceSelectionDialog.CAMERA_CAPTURE_IMAGE_REQUEST_CODE && resultCode == RESULT_OK) {
            vimage = "";
            viewTxtView.setVisibility(View.GONE);
            editView.setVisibility(View.GONE);
            if (pathForCameraImage.equalsIgnoreCase("")) {
                selPath = new ImageFilePath().getPath(getActContext(), fileUri);
            } else {
                selPath = pathForCameraImage;
            }

            if (Utils.isValidImageResolution(selPath) == true && generalFunc.isStoragePermissionGranted()) {
                this.selectedDocumentPath = selPath;
            } else {
                generalFunc.showGeneralMessage("", "Please select image which has minimum is 256 * 256 resolution.");
            }
        } else if ((requestCode == OpenSourceSelectionDialog.SELECT_FILE_BROWSABLE || requestCode == OpenSourceSelectionDialog.SELECT_PICTURE) && resultCode == RESULT_OK) {
            if (data != null) {
                vimage = "";
                viewTxtView.setVisibility(View.GONE);
                editView.setVisibility(View.GONE);

                try {
                    Uri selectedImageUri = data.getData();
                    String selectedImagePath = new ImageFilePath().getPath(getActContext(), selectedImageUri);

                    if (selectedImagePath == null) {
                        if (ImageFilePath.isGooglePhotosUri(selectedImageUri) || ImageFilePath.isGoogleDriveUri(selectedImageUri)) {
                            generalFunc.showMessage(generalFunc.getCurrentView((Activity) getActContext()), generalFunc.retrieveLangLBl("", "LBL_FILE_CHOOSE_ERROR"));
                            return;
                        }
                        generalFunc.showMessage(generalFunc.getCurrentView((Activity) getActContext()), generalFunc.retrieveLangLBl("Can't read selected image. Please try again.", "LBL_IMAGE_READ_FAILED"));
                        return;
                    }
                    selPath = selectedImagePath;
                } catch (Exception e) {

                }
            }
        }

        if (resultCode != RESULT_CANCELED && (requestCode == OpenSourceSelectionDialog.CAMERA_CAPTURE_IMAGE_REQUEST_CODE || requestCode == OpenSourceSelectionDialog.SELECT_FILE_BROWSABLE || requestCode == OpenSourceSelectionDialog.SELECT_PICTURE)) {
            if (selPath != null) {

                Logger.e("filePath", "::" + Utils.getFileExt(selPath));

                String fileExtension = Utils.getFileExt(selPath);

                if (Arrays.asList(tsite_upload_docs_file_extensions_arr).contains(fileExtension)) {
                    selectedDocumentPath = selPath;
                    imgeselectview.setVisibility(View.VISIBLE);
                    helpInfoTxtView.setVisibility(View.GONE);
                    editArea.setVisibility(View.VISIBLE);
                    if (!vimage.equalsIgnoreCase("")) {
                        viewTxtView.setVisibility(View.VISIBLE);
                        editView.setVisibility(View.VISIBLE);

                    } else {
                        viewTxtView.setVisibility(View.GONE);
                        editView.setVisibility(View.GONE);
                    }

                    dummyInfoCardImgView.setAlpha(0.2f);
                    dummyInfoCardImgView.setImageDrawable(getResources().getDrawable(R.drawable.ic_card_documents));
                    isuploadimageNew = true;
                } else {
                    imgeselectview.setVisibility(View.GONE);
                    editArea.setVisibility(View.GONE);
                    dummyInfoCardImgView.setOnClickListener(new setOnClickList());
                    helpInfoTxtView.setVisibility(View.VISIBLE);
                    generalFunc.showGeneralMessage("", generalFunc.retrieveLangLBl("You have selected wrong file format for document. " +
                            "Valid formats are pdf, doc, docx, jpg, jpeg, gif, png, bmp, txt.", "LBL_WRONG_FILE_SELECTED_TXT"));
                }

            } else {
                generalFunc.showMessage(generalFunc.getCurrentView((Activity) getActContext()), generalFunc.retrieveLangLBl("", "LBL_TRY_AGAIN_TXT"));
            }
        }
    }

    public void handleImgUploadResponse(String responseString) {

        if (responseString != null && !responseString.equals("")) {

            boolean isDataAvail = GeneralFunctions.checkDataAvail(Utils.action_str, responseString);

            if (isDataAvail == true) {

                String msgTxt = "";
                if (!generalFunc.getJsonValue("doc_under_review", responseString).equalsIgnoreCase("")) {
                    msgTxt = generalFunc.retrieveLangLBl("", generalFunc.getJsonValue("doc_under_review", responseString));
                } else {
                    msgTxt = generalFunc.retrieveLangLBl("Your document is uploaded successfully", "LBL_UPLOAD_DOC_SUCCESS");
                }
                final GenerateAlertBox generateAlert = new GenerateAlertBox(getActContext());
                generateAlert.setCancelable(false);
                generateAlert.setBtnClickList(btn_id -> {
                    generateAlert.closeAlertBox();

                    setResult(RESULT_OK);
                    backImgView.performClick();
                });
                generateAlert.setContentMessage("", msgTxt);
                generateAlert.setPositiveBtn(generalFunc.retrieveLangLBl("", "LBL_BTN_OK_TXT"));

                generateAlert.showAlertBox();
            } else {
                generalFunc.showGeneralMessage("",
                        generalFunc.retrieveLangLBl("", generalFunc.getJsonValue(Utils.message_str, responseString)));
            }
        } else {
            generalFunc.showError();
        }

    }

    public void openDateSelection() {

        if (isDatePickerOpen) {
            return;
        }
        Logger.d("openDateSelection", "::" + Calendar.getInstance().getTime());
        new SlideDateTimePicker.Builder(getSupportFragmentManager())
                .setListener(new SlideDateTimeListener() {
                    @Override
                    public void onDateTimeSet(Date date) {
                        isDatePickerOpen = false;
                        expBoxTXT.setText(Utils.convertDateToFormat(CommonUtilities.WithoutDayFormat, date));
                    }

                    @Override
                    public void onDateTimeCancel() {
                        isDatePickerOpen = false;
                        super.onDateTimeCancel();
                    }
                })
                .setInitialDate(new Date())
                .setMinDate(Calendar.getInstance().getTime())
                //.setMaxDate(maxDate)
                .setIs24HourTime(true)
                .setTimePickerEnabled(false)
                //.setTheme(SlideDateTimePicker.HOLO_DARK)
                .setIndicatorColor(getResources().getColor(R.color.appThemeColor_2))
                .build()
                .show();
        isDatePickerOpen = true;
    }

    private void openDocChoose() {
        OpenSourceSelectionDialog openSourceDialog = new OpenSourceSelectionDialog(getActContext(), generalFunc, true);
        openSourceDialog.setOnFileUriGenerateListener((fileUri, pathForCameraImage) -> {
            this.fileUri = fileUri;
            this.pathForCameraImage = pathForCameraImage;
        });
        openSourceDialog.run();
    }


    public class setOnClickList implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            int i = view.getId();
            Utils.hideKeyboard(UploadDocActivity.this);
            if (i == R.id.backImgView) {
                UploadDocActivity.super.onBackPressed();
            } else if (i == btn_type2.getId()) {
                checkData();
            } else if (i == helpInfoTxtView.getId() || i == dummyInfoCardImgView.getId()) {
                if (generalFunc.isCameraStoragePermissionGranted()) {
                    openDocChoose();
                }
            } else if (i == R.id.selectyear_layout) {
                openDateSelection();
            } else if (i == R.id.editTxtView) {
                if (generalFunc.isCameraStoragePermissionGranted()) {
                    openDocChoose();
                }

            } else if (i == R.id.viewTxtView) {
                new StartActProcess(getActContext()).openURL(vimage);

            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case GeneralFunctions.MY_PERMISSIONS_REQUEST: {
                if (generalFunc.isPermisionGranted()) {
                    openDocChoose();
                }
                break;

            }
        }
    }
}
