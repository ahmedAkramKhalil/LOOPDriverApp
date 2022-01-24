package com.general.files;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.provider.Settings;
import androidx.fragment.app.Fragment;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.taxifgo.driver.ActiveTripActivity;
import com.taxifgo.driver.DriverArrivedActivity;
import com.taxifgo.driver.MainActivity;
import com.taxifgo.driver.R;
import com.fragments.InactiveFragment;
import com.utils.Logger;
import com.utils.Utils;
import com.view.MTextView;

import java.util.List;

public class OpenNoLocationView {
    ViewGroup viewGroup;
    Activity currentAct;

    View noLocView;

    private static OpenNoLocationView currentInst;
    private boolean isViewExecutionLocked = false;

    public static OpenNoLocationView getInstance(Activity currentAct, ViewGroup viewGroup) {
        if (currentInst == null) {
            currentInst = new OpenNoLocationView();
        }

        currentInst.viewGroup = viewGroup;
        currentInst.currentAct = currentAct;

        return currentInst;
    }

    public void configView(boolean isFromNetwork) {
        if (viewGroup != null && currentAct != null) {

            if (isViewExecutionLocked == true) {
                return;
            }

            isViewExecutionLocked = true;

            closeView();

            if (currentAct instanceof MainActivity) {
//                Logger.e("TotalFragments", ":::" + ((MainActivity) currentAct).getSupportFragmentManager().getFragments().size());
                List<Fragment> fragmentsList = ((MainActivity) currentAct).getSupportFragmentManager().getFragments();

                for (int i = 0; i < fragmentsList.size(); i++) {
                    Fragment frag = fragmentsList.get(i);
                    if (frag instanceof InactiveFragment) {
                        isViewExecutionLocked = false;
                        return;
                    }
                }
            }

            GeneralFunctions generalFunc = MyApp.getInstance().getGeneralFun(MyApp.getInstance().getCurrentAct());
            boolean isNetworkConnected = new InternetConnection(currentAct).isNetworkConnected();
            LayoutInflater inflater = (LayoutInflater) currentAct.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View noLocView = inflater.inflate(R.layout.desgin_no_locatin_view, null);

            MTextView noLocTitleTxt = (MTextView) noLocView.findViewById(R.id.noLocTitleTxt);
            MTextView noLocMesageTxt = (MTextView) noLocView.findViewById(R.id.noLocMesageTxt);
            MTextView settingBtn = (MTextView) noLocView.findViewById(R.id.settingBtn);
            MTextView RetryBtn = (MTextView) noLocView.findViewById(R.id.RetryBtn);

            settingBtn.setText(generalFunc.retrieveLangLBl("Settings", "LBL_SETTINGS"));
            RetryBtn.setText(generalFunc.retrieveLangLBl("", "LBL_RETRY_TXT"));

            if (currentAct instanceof MainActivity) {
                noLocView.setPadding(0, getActionBarHeight(), 0, 0);
            }

            settingBtn.setOnClickListener(v -> {
                if (isNetworkConnected) {
                    new StartActProcess(MyApp.getInstance().getCurrentAct()).
                            startActForResult(Settings.ACTION_LOCATION_SOURCE_SETTINGS, Utils.REQUEST_CODE_GPS_ON);
                } else {
                    new StartActProcess(MyApp.getInstance().getCurrentAct()).
                            startActForResult(Settings.ACTION_SETTINGS, Utils.REQUEST_CODE_NETWOEK_ON);
                }
            });

            RetryBtn.setOnClickListener(v -> {
                configView(isFromNetwork);
            });

            if (isNetworkConnected == false) {

                currentInst.noLocView = noLocView;

                noLocTitleTxt.setText(generalFunc.retrieveLangLBl("Internet Connection", "LBL_NO_INTERNET_TITLE"));
                noLocMesageTxt.setText(generalFunc.retrieveLangLBl("Application requires internet connection to be enabled. Please check your network settings.", "LBL_NO_INTERNET_SUB_TITLE"));

                addView(noLocView, "NO_INTERNET");

                isViewExecutionLocked = false;
                return;
            } else if (isFromNetwork == true) {

                if (currentAct instanceof DriverArrivedActivity) {
                    ((DriverArrivedActivity) currentAct).internetIsBack();
                }

                if (currentAct instanceof ActiveTripActivity) {
                    ((ActiveTripActivity) currentAct).internetIsBack();
                }
            }

            if (!generalFunc.isLocationEnabled()) {

                noLocTitleTxt.setText(generalFunc.retrieveLangLBl("Enable Location Service", "LBL_ENABLE_LOC_SERVICE"));
                noLocMesageTxt.setText(generalFunc.retrieveLangLBl("This app requires location services. Please enabled location service from device settings. Go to Settings >> Location >>Turn on", "LBL_NO_LOCATION_ANDROID_TXT"));

                addView(noLocView, "NO_LOCATION");

                isViewExecutionLocked = false;
                return;
            } else {
                if (currentAct instanceof DriverArrivedActivity) {
                    ((DriverArrivedActivity) currentAct).checkUserLocation();
                }
                if (currentAct instanceof ActiveTripActivity) {
                    ((ActiveTripActivity) currentAct).checkUserLocation();
                }
            }

        } else {
            Logger.e("AssertError", "ViewGroup OR Activity cannot be null");
        }
        isViewExecutionLocked = false;
    }

    public int getActionBarHeight() {
        int actionBarHeight = 0;
        TypedValue tv = new TypedValue();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            if (currentAct.getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true))
                actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, currentAct.getResources().getDisplayMetrics());
        } else {
            actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, currentAct.getResources().getDisplayMetrics());
        }
        return actionBarHeight;
    }

    private void addView(View noLocView, String type) {
        closeView();
        currentInst.noLocView = noLocView;

        if (currentAct instanceof MainActivity) {
            if (type.equalsIgnoreCase("NO_LOCATION")) {
                ((MainActivity) currentAct).handleNoLocationDial();
            }
            ((RelativeLayout) (viewGroup.findViewById(R.id.containerView))).addView(noLocView);
        } else {
            viewGroup.addView(noLocView);
        }
    }

    private void closeView() {
        if (noLocView != null || currentAct.findViewById(R.id.noLocView) != null) {
            try {
                if (currentAct instanceof MainActivity) {
                    ((RelativeLayout) (viewGroup.findViewById(R.id.containerView))).removeView(noLocView);
                } else {
                    viewGroup.removeView(noLocView);
                }

                noLocView = null;

            } catch (Exception e) {
                Logger.e("ViewRemove", ":Exception:" + e.getMessage());
            }
        }
    }
}
