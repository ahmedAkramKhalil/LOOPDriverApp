package com.taxifgo.driver;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import androidx.core.content.FileProvider;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.adapter.files.GalleryImagesRecyclerAdapter;
import com.general.files.ExecuteWebServerUrl;
import com.general.files.GeneralFunctions;
import com.general.files.ImageFilePath;
import com.general.files.MyApp;
import com.general.files.UploadProfileImage;
import com.squareup.picasso.Picasso;
import com.utils.Utilities;
import com.utils.Utils;
import com.view.FloatingAction.FloatingActionButton;
import com.view.FloatingAction.FloatingActionMenu;
import com.view.MTextView;
import com.view.carouselview.CarouselView;
import com.view.carouselview.ViewListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;

public class MyGalleryActivity extends AppCompatActivity implements View.OnClickListener, GalleryImagesRecyclerAdapter.OnItemClickListener {

    public static final int MEDIA_TYPE_IMAGE = 1;
    private static final String IMAGE_DIRECTORY_NAME = "Temp";
    private static final int SELECT_PICTURE = 2;
    private static final int CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 100;

    RecyclerView galleryRecyclerView;
    MTextView titleTxt;
    ImageView backImgView;
    AppCompatImageView noImgView;
    ProgressBar loading_images;

    ImageView filterImageview;

    CarouselView carouselView;

    GeneralFunctions generalFunc;

    MTextView closeCarouselTxtView;
    FloatingActionMenu imgAddOptionMenu;
    FloatingActionButton cameraItem;
    FloatingActionButton galleryItem;

    View carouselContainerView;

    private String selectedImagePath = "";
    private String pathForCameraImage = "";
    private Uri fileUri;

    GalleryImagesRecyclerAdapter adapter;

    ArrayList<HashMap<String, String>> listData = new ArrayList<>();

    String userProfileJson;
    String APP_TYPE = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_gallery);

        generalFunc = MyApp.getInstance().getGeneralFun(getActContext());

        titleTxt = (MTextView) findViewById(R.id.titleTxt);
        backImgView = (ImageView) findViewById(R.id.backImgView);
        noImgView = (AppCompatImageView) findViewById(R.id.noImgView);
        galleryRecyclerView = (RecyclerView) findViewById(R.id.galleryRecyclerView);
        imgAddOptionMenu = (FloatingActionMenu) findViewById(R.id.imgAddOptionMenu);
        loading_images = (ProgressBar) findViewById(R.id.loading_images);
        carouselContainerView = findViewById(R.id.carouselContainerView);
        carouselView = (CarouselView) findViewById(R.id.carouselView);
        closeCarouselTxtView = (MTextView) findViewById(R.id.closeCarouselTxtView);
        filterImageview = (ImageView) findViewById(R.id.filterImageview);

        cameraItem = (FloatingActionButton) findViewById(R.id.cameraItem);
        galleryItem = (FloatingActionButton) findViewById(R.id.galleryItem);

        userProfileJson = generalFunc.retrieveValue(Utils.USER_PROFILE_JSON);
        APP_TYPE = generalFunc.getJsonValue("APP_TYPE", userProfileJson);


        adapter = new GalleryImagesRecyclerAdapter(getActContext(), listData, generalFunc, false);

        galleryRecyclerView.setAdapter(adapter);

        backImgView.setOnClickListener(this);
        cameraItem.setOnClickListener(this);
        galleryItem.setOnClickListener(this);
//        carouselContainerView.setOnClickListener(this);
        closeCarouselTxtView.setOnClickListener(this);

        setLabels();

        if (APP_TYPE.equalsIgnoreCase(Utils.CabGeneralTypeRide_Delivery_UberX)) {
            filterImageview.setImageDrawable(getResources().getDrawable(R.mipmap.ic_menu_help));
            filterImageview.setPadding(Utils.dipToPixels(getActContext(), 13), Utils.dipToPixels(getActContext(), 13), Utils.dipToPixels(getActContext(), 13), Utils.dipToPixels(getActContext(), 13));
            filterImageview.setVisibility(View.VISIBLE);

            filterImageview.setOnClickListener(this);
        }

        Drawable mGalleryDrawable = getActContext().getResources().getDrawable(R.mipmap.ic_gallery_fab);

        mGalleryDrawable.setColorFilter(new PorterDuffColorFilter(getResources().getColor(R.color.appThemeColor_TXT_1), PorterDuff.Mode.SRC_IN));

        galleryItem.setImageDrawable(mGalleryDrawable);

        Drawable mCameraDrawable = getActContext().getResources().getDrawable(R.mipmap.ic_camera_fab);
        mCameraDrawable.setColorFilter(new PorterDuffColorFilter(getResources().getColor(R.color.appThemeColor_TXT_1), PorterDuff.Mode.SRC_IN));

        cameraItem.setImageDrawable(mCameraDrawable);

        GridLayoutManager gridLay = new GridLayoutManager(getActContext(), adapter.getNumOfColumns());

        galleryRecyclerView.setLayoutManager(gridLay);

        adapter.setOnItemClickListener(this);
        getImages();
    }

    public void setLabels() {
        titleTxt.setText(generalFunc.retrieveLangLBl("Manage Gallery", "LBL_MANAGE_GALLARY"));

        cameraItem.setLabelText(generalFunc.retrieveLangLBl("", "LBL_CAMERA"));
        galleryItem.setLabelText(generalFunc.retrieveLangLBl("", "LBL_GALLERY"));
        closeCarouselTxtView.setText(generalFunc.retrieveLangLBl("", "LBL_CLOSE_TXT"));
    }

    private void getImages() {
        loading_images.setVisibility(View.VISIBLE);
        noImgView.setVisibility(View.GONE);
        listData.clear();

        adapter.notifyDataSetChanged();

        HashMap<String, String> parameters = new HashMap<>();
        parameters.put("type", "getProviderImages");
        parameters.put("UserType", Utils.app_type);
        parameters.put("iDriverId", generalFunc.getMemberId());
        parameters.put("SelectedCabType", Utils.CabGeneralType_UberX);

        ExecuteWebServerUrl exeServerUrl = new ExecuteWebServerUrl(getActContext(), parameters);
        exeServerUrl.setLoaderConfig(getActContext(), false, generalFunc);
        exeServerUrl.setDataResponseListener(responseString -> {
            JSONObject responseStringObject=generalFunc.getJsonObject(responseString);

            if (responseStringObject.toString() != null && !responseStringObject.toString().equalsIgnoreCase("")) {
                boolean isDataAvail = GeneralFunctions.checkDataAvail(Utils.action_str, responseStringObject.toString());

                if (isDataAvail) {
                    listData.clear();

                    JSONArray arr_data = generalFunc.getJsonArray(Utils.message_str, responseStringObject.toString());

                    if (arr_data != null) {
                        for (int i = 0; i < arr_data.length(); i++) {
                            JSONObject obj_tmp = generalFunc.getJsonObject(arr_data, i);

                            HashMap<String, String> mapData = new HashMap<>();
                            Iterator<String> keysItr = obj_tmp.keys();
                            while (keysItr.hasNext()) {
                                String key = keysItr.next();
                                String value = generalFunc.getJsonValueStr(key, obj_tmp);

                                mapData.put(key, value);
                            }
                            listData.add(mapData);
                        }
                    }

                    adapter.notifyDataSetChanged();


                    if (listData.size() == 0) {
                        noImgView.setVisibility(View.VISIBLE);
                    }

                } else {
                    noImgView.setVisibility(View.VISIBLE);
                }

            } else {
                generalFunc.showError(true);
            }

            loading_images.setVisibility(View.GONE);
        });
        exeServerUrl.execute();
    }

    ViewListener viewListener = position -> {
        ImageView customView = new ImageView(getActContext());

        CarouselView.LayoutParams layParams = new CarouselView.LayoutParams(CarouselView.LayoutParams.MATCH_PARENT, CarouselView.LayoutParams.MATCH_PARENT);
//        layParams.leftMargin = Utils.dipToPixels(getActContext(), 15);
//        layParams.rightMargin = Utils.dipToPixels(getActContext(), 15);
        customView.setLayoutParams(layParams);

        customView.setPadding(Utils.dipToPixels(getActContext(), 15), 0, Utils.dipToPixels(getActContext(), 15), 0);
        customView.setImageResource(R.mipmap.ic_no_icon);

        final HashMap<String, String> item = listData.get(position);

        Picasso.get()
                .load(Utilities.getResizeImgURL(getActContext(), item.get("vImage"), ((int) Utils.getScreenPixelWidth(getActContext())) - Utils.dipToPixels(getActContext(), 30), 0, Utils.getScreenPixelHeight(getActContext()) - Utils.dipToPixels(getActContext(), 30)))
                .placeholder(R.mipmap.ic_no_icon).error(R.mipmap.ic_no_icon)
                .into(customView, null);

        return customView;
    };

    public Context getActContext() {
        return MyGalleryActivity.this;
    }

    public void chooseFromGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_PICTURE);
    }


    public void chooseFromCamera() {

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);

        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);

        startActivityForResult(intent, CAMERA_CAPTURE_IMAGE_REQUEST_CODE);
    }

    private boolean isDeviceSupportCamera() {
        if (getApplicationContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            return true;
        } else {
            return false;
        }
    }

    public Uri getOutputMediaFileUri(int type) {
        return FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".provider", getOutputMediaFile(type));
    }

    private File getOutputMediaFile(int type) {

        // External sdcard location
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                IMAGE_DIRECTORY_NAME);

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator + "IMG_" + timeStamp + ".jpg");
            pathForCameraImage = mediaFile.getAbsolutePath();
        } else {
            return null;
        }

        return mediaFile;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.backImgView:
                MyGalleryActivity.super.onBackPressed();
                break;
            case R.id.filterImageview:
                generalFunc.showGeneralMessage("", generalFunc.retrieveLangLBl("", "LBL_GALLERY_IMG_NOTE"));
                break;
            case R.id.closeCarouselTxtView:
                if (carouselContainerView.getVisibility() == View.VISIBLE) {
                    carouselContainerView.setVisibility(View.GONE);
                }
                break;
            case R.id.cameraItem:
                imgAddOptionMenu.close(true);
                if (generalFunc.isCameraStoragePermissionGranted()) {
                    if (!isDeviceSupportCamera()) {
                        generalFunc.showMessage(generalFunc.getCurrentView(MyGalleryActivity.this), generalFunc.retrieveLangLBl("", "LBL_NOT_SUPPORT_CAMERA_TXT"));
                    } else {
                        chooseFromCamera();
                    }
                }
                break;
            case R.id.galleryItem:
                imgAddOptionMenu.close(true);
                if (generalFunc.isCameraStoragePermissionGranted()) {
                    chooseFromGallery();
                }
                break;
        }
    }

    private void configProviderImage(String iImageId, String action_type) {

        ArrayList<String[]> paramsList = new ArrayList<>();
        paramsList.add(generalFunc.generateImageParams("type", "configProviderImages"));
        paramsList.add(generalFunc.generateImageParams("iDriverId", generalFunc.getMemberId()));
        paramsList.add(generalFunc.generateImageParams("UserType", Utils.app_type));
        paramsList.add(generalFunc.generateImageParams("action_type", action_type));
        paramsList.add(generalFunc.generateImageParams("iImageId", iImageId));

        new UploadProfileImage(MyGalleryActivity.this, selectedImagePath, Utils.TempProfileImageName, paramsList, "GALLERY").execute();

    }

    public void handleImgUploadResponse(String responseString, String imageUploadedType) {
        selectedImagePath = "";
        if (responseString != null && !responseString.equals("")) {
            boolean isDataAvail = GeneralFunctions.checkDataAvail(Utils.action_str, responseString);

            if (isDataAvail) {
                getImages();
            }

            generalFunc.showMessage(generalFunc.getCurrentView((Activity) getActContext()), generalFunc.retrieveLangLBl("", generalFunc.getJsonValue(Utils.message_str, responseString)));
        } else {
            generalFunc.showError();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CAMERA_CAPTURE_IMAGE_REQUEST_CODE && resultCode == RESULT_OK) {
            if (pathForCameraImage.equalsIgnoreCase("")) {
                selectedImagePath = new ImageFilePath().getPath(getActContext(), fileUri);
            } else {
                selectedImagePath = pathForCameraImage;
            }

            if (selectedImagePath == null || selectedImagePath.equalsIgnoreCase("")) {
                selectedImagePath = "";

                generalFunc.showMessage(generalFunc.getCurrentView((Activity) getActContext()), generalFunc.retrieveLangLBl("Can't read selected image. Please try again.", "LBL_IMAGE_READ_FAILED"));
                return;
            }

            configProviderImage("", "ADD");
        } else if (requestCode == SELECT_PICTURE && resultCode == RESULT_OK) {

            Uri selectedImageUri = data.getData();

            selectedImagePath = ImageFilePath.getPath(getApplicationContext(), selectedImageUri);

            if (selectedImagePath == null || selectedImagePath.equalsIgnoreCase("")) {
                selectedImagePath = "";

                generalFunc.showMessage(generalFunc.getCurrentView((Activity) getActContext()), generalFunc.retrieveLangLBl("Can't read selected image. Please try again.", "LBL_IMAGE_READ_FAILED"));
                return;
            }

            configProviderImage("", "ADD");
        }
    }

    @Override
    public void onItemClickList(View v, int position) {
        carouselContainerView.setVisibility(View.VISIBLE);
        carouselView.setViewListener(viewListener);
        carouselView.setPageCount(listData.size());
        carouselView.setCurrentItem(position);
    }

    @Override
    public void onDeleteClick(View v, int position) {
        generalFunc.showGeneralMessage("", generalFunc.retrieveLangLBl("", "LBL_DELETE_IMG_CONFIRM_NOTE"), generalFunc.retrieveLangLBl("", "LBL_NO"), generalFunc.retrieveLangLBl("", "LBL_YES"), buttonId -> {

            if (buttonId == 1) {
                selectedImagePath = "";
                configProviderImage(listData.get(position).get("iImageId"), "DELETE");
            }

        });
    }

    @Override
    public void onBackPressed() {
        if (carouselContainerView.getVisibility() == View.VISIBLE) {
            carouselContainerView.setVisibility(View.GONE);
            return;
        }
        super.onBackPressed();
    }
}
