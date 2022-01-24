package com.general.files;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.adapter.files.DrawerAdapter;
import com.taxifgo.driver.BankDetailActivity;
import com.taxifgo.driver.BookingsActivity;
import com.taxifgo.driver.CardPaymentActivity;
import com.taxifgo.driver.ContactUsActivity;
import com.taxifgo.driver.DriverFeedbackActivity;
import com.taxifgo.driver.EmergencyContactActivity;
import com.taxifgo.driver.HelpActivity;
import com.taxifgo.driver.InviteFriendsActivity;
import com.taxifgo.driver.ListOfDocumentActivity;
import com.taxifgo.driver.MainActivity;
import com.taxifgo.driver.ManageVehiclesActivity;
import com.taxifgo.driver.MyGalleryActivity;
import com.taxifgo.driver.MyHeatViewActivity;
import com.taxifgo.driver.MyProfileActivity;
import com.taxifgo.driver.MyWalletActivity;
import com.taxifgo.driver.NotificationActivity;
import com.taxifgo.driver.PrefranceActivity;
import com.taxifgo.driver.R;
import com.taxifgo.driver.SetAvailabilityActivity;
import com.taxifgo.driver.StaticPageActivity;
import com.taxifgo.driver.StatisticsActivity;
import com.taxifgo.driver.SubscriptionActivity;
import com.taxifgo.driver.SupportActivity;
import com.taxifgo.driver.UfxCategoryActivity;
import com.taxifgo.driver.UploadDocTypeWiseActivity;
import com.taxifgo.driver.VerifyInfoActivity;
import com.taxifgo.driver.WayBillActivity;
import com.taxifgo.driver.deliverAll.LiveTaskListActivity;
import com.taxifgo.driver.deliverAll.OrderHistoryActivity;
import com.taxifgo.driver.deliverAll.OrderStatisticsActivity;
import com.utils.CommonUtilities;
import com.utils.Utils;
import com.view.GenerateAlertBox;
import com.view.MTextView;
import com.view.SelectableRoundedImageView;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class AddDrawer implements AdapterView.OnItemClickListener {

    private String app_type;
    public MTextView walletbalncetxt;
    Context mContext;
    View view;
    DrawerLayout mDrawerLayout;
    ListView menuListView;
    DrawerAdapter drawerAdapter;
    ArrayList<String[]> list_menu_items;
    GeneralFunctions generalFunc;
    DrawerClickListener drawerClickListener;
    boolean isMenuState = true;
    boolean isDriverOnline;
    LinearLayout logoutarea;
    ImageView logoutimage;
    MTextView logoutTxt;
    ImageView imgSetting;
    LinearLayout left_linear;
    MainActivity mainActivity;
    public JSONObject obj_userProfile;
    private boolean iswallet;

    public AddDrawer(Context mContext, JSONObject obj_userProfile) {
        this.mContext = mContext;
        this.obj_userProfile = obj_userProfile;
        view = ((Activity) mContext).findViewById(android.R.id.content);
        mDrawerLayout = (DrawerLayout) view.findViewById(R.id.drawer_layout);
        menuListView = (ListView) view.findViewById(R.id.menuListView);
        logoutarea = (LinearLayout) view.findViewById(R.id.logoutarea);
        logoutimage = (ImageView) view.findViewById(R.id.logoutimage);
        logoutTxt = (MTextView) view.findViewById(R.id.logoutTxt);
        imgSetting = (ImageView) view.findViewById(R.id.imgSetting);
        left_linear = (LinearLayout) view.findViewById(R.id.left_linear);
        imgSetting.setOnClickListener(new setOnClickLst());
        logoutarea.setOnClickListener(new setOnClickLst());
        generalFunc = MyApp.getInstance().getGeneralFun(mContext);
        logoutimage.setImageDrawable(mContext.getResources().getDrawable(R.mipmap.ic_menu_logout));
        logoutTxt.setText(generalFunc.retrieveLangLBl("", "LBL_SIGNOUT_TXT"));
        walletbalncetxt = (MTextView) view.findViewById(R.id.walletbalncetxt);


        android.view.Display display = ((android.view.WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        left_linear.getLayoutParams().width = display.getWidth() * 75 / 100;
        left_linear.requestLayout();

        if (mContext instanceof MainActivity) {
            mainActivity = (MainActivity) mContext;
        }

        app_type = generalFunc.getJsonValueStr("APP_TYPE", obj_userProfile);

        buildDrawer();
        setUserInfo();

        if (mContext instanceof LiveTaskListActivity/* || mContext instanceof MainActivity*/) {
            (view.findViewById(R.id.menuImgView)).setOnClickListener(new setOnClickLst());
        }
    }

    public void setIsDriverOnline(boolean isDriverOnline) {
        this.isDriverOnline = isDriverOnline;

    }

    public void setIswallet(boolean iswallet) {
        this.iswallet = iswallet;
    }

    public void setMenuImgClick(View view, boolean isDriverAssigned) {
        if (isDriverAssigned) {
            (view.findViewById(R.id.backImgView)).setOnClickListener(new setOnClickLst());
        } else {
            (view.findViewById(R.id.menuImgView)).setOnClickListener(new setOnClickLst());
        }
    }

    public void changeUserProfileJson(JSONObject userProfileJson) {
        obj_userProfile = userProfileJson;
        app_type = generalFunc.getJsonValueStr("APP_TYPE", obj_userProfile);
        setUserInfo();
    }

    public void configDrawer(boolean isHide) {
        (view.findViewById(R.id.left_linear)).setVisibility(View.GONE);
        DrawerLayout.LayoutParams params = (DrawerLayout.LayoutParams) (view.findViewById(R.id.left_linear)).getLayoutParams();
        params.gravity = isHide == true ? Gravity.NO_GRAVITY : GravityCompat.START;
        (view.findViewById(R.id.left_linear)).setLayoutParams(params);
    }

    public void setMenuState(boolean isMenuState) {
        this.isMenuState = isMenuState;

        if (!this.isMenuState) {
            ((ImageView) view.findViewById(R.id.menuImgView)).setImageResource(R.mipmap.ic_back_arrow);

            configDrawer(true);

        } else {
            ((ImageView) view.findViewById(R.id.menuImgView)).setImageResource(R.mipmap.ic_drawer_menu);

            configDrawer(false);
        }
    }

    public void buildDrawer() {
        if (list_menu_items == null) {
            list_menu_items = new ArrayList();
            drawerAdapter = new DrawerAdapter(list_menu_items, mContext, generalFunc);

            menuListView.setAdapter(drawerAdapter);
            menuListView.setOnItemClickListener(this);
        } else {
            list_menu_items.clear();
        }

        list_menu_items.add(new String[]{"" + R.mipmap.ic_menu_profile, generalFunc.retrieveLangLBl("", "LBL_MY_PROFILE_HEADER_TXT"), "" + Utils.MENU_PROFILE});


        if (generalFunc.isDeliverOnlyEnabled()) {
            if (app_type.equalsIgnoreCase(Utils.CabGeneralType_UberX) || app_type.equalsIgnoreCase(Utils.CabGeneralTypeRide_Delivery_UberX) && !generalFunc.isDeliverOnlyEnabled()) {
                list_menu_items.add(new String[]{"" + R.mipmap.ic_menu_service, generalFunc.retrieveLangLBl("", "LBL_MANANGE_SERVICES"), "" + Utils.MENU_MANAGE_VEHICLES});
            } else {
                list_menu_items.add(new String[]{"" + R.mipmap.ic_menu_car, generalFunc.retrieveLangLBl("", "LBL_MANAGE_VEHICLES"), "" + Utils.MENU_MANAGE_VEHICLES});
            }

            list_menu_items.add(new String[]{"" + R.mipmap.ic_menu_doc, generalFunc.retrieveLangLBl("Your Documents", "LBL_MANAGE_DOCUMENT"), "" + Utils.MENU_YOUR_DOCUMENTS});

        } else {

            if (generalFunc.retrieveValue(Utils.DRIVER_SUBSCRIPTION_ENABLE_KEY).equalsIgnoreCase("Yes")) {
                list_menu_items.add(new String[]{"" + R.mipmap.ic_menu_subscription, generalFunc.retrieveLangLBl("", "LBL_MY_SUBSCRIPTION"), "" + Utils.MENU_SUBSCRIPTION});
            }

            list_menu_items.add(new String[]{"" + R.mipmap.ic_menu_doc, generalFunc.retrieveLangLBl("Your Documents", "LBL_MANAGE_DOCUMENT"), "" + Utils.MENU_YOUR_DOCUMENTS});

            if (app_type.equalsIgnoreCase(Utils.CabGeneralType_UberX) || app_type.equalsIgnoreCase(Utils.CabGeneralTypeRide_Delivery_UberX) && !generalFunc.isDeliverOnlyEnabled()) {
                list_menu_items.add(new String[]{"" + R.mipmap.ic_menu_service, generalFunc.retrieveLangLBl("", "LBL_MANANGE_SERVICES"), "" + Utils.MENU_MANAGE_VEHICLES});
            } else {
                list_menu_items.add(new String[]{"" + R.mipmap.ic_menu_car, generalFunc.retrieveLangLBl("", "LBL_MANAGE_VEHICLES"), "" + Utils.MENU_MANAGE_VEHICLES});
            }
        }

        if (app_type.equalsIgnoreCase(Utils.CabGeneralType_UberX) || app_type.equalsIgnoreCase(Utils.CabGeneralTypeRide_Delivery_UberX) && !generalFunc.isDeliverOnlyEnabled()) {
            list_menu_items.add(new String[]{"" + R.mipmap.setavail, generalFunc.retrieveLangLBl("Set Availability", "LBL_MY_AVAILABILITY"), "" + Utils.MENU_SET_AVAILABILITY});
        }

        if ((app_type.equalsIgnoreCase(Utils.CabGeneralType_UberX) || app_type.equalsIgnoreCase(Utils.CabGeneralTypeRide_Delivery_UberX)) && generalFunc.getJsonValueStr("SERVICE_PROVIDER_FLOW", obj_userProfile).equalsIgnoreCase("PROVIDER") && !generalFunc.isDeliverOnlyEnabled()) {
            list_menu_items.add(new String[]{"" + R.mipmap.ic_menu_my_gallery, generalFunc.retrieveLangLBl("Manage Gallery", "LBL_MANAGE_GALLARY"), "" + Utils.MENU_MY_GALLERY});
        }

        if (!generalFunc.isDeliverOnlyEnabled()) {
            String menuMsgYourTrips = "";
            if (app_type.equalsIgnoreCase(Utils.CabGeneralType_Ride)) {
                menuMsgYourTrips = generalFunc.retrieveLangLBl("", "LBL_YOUR_TRIPS");
            } else if (app_type.equalsIgnoreCase("Delivery")) {
                menuMsgYourTrips = generalFunc.retrieveLangLBl("", "LBL_YOUR_DELIVERY");
            } else if (app_type.equalsIgnoreCase(Utils.CabGeneralType_UberX)) {
                menuMsgYourTrips = generalFunc.retrieveLangLBl("", "LBL_YOUR_JOB");
            } else {
                menuMsgYourTrips = generalFunc.retrieveLangLBl("", "LBL_YOUR_BOOKING");
            }
            list_menu_items.add(new String[]{"" + R.mipmap.ic_menu_yourtrip, menuMsgYourTrips, "" + Utils.MENU_YOUR_TRIPS});

        }

        if (generalFunc.isAnyDeliverOptionEnabled()) {
            list_menu_items.add(new String[]{"" + R.mipmap.ic_menu_order, generalFunc.retrieveLangLBl("", "LBL_ORDERS"), "" + Utils.MENU_ORDER});
        }


        list_menu_items.add(new String[]{"" + R.mipmap.ic_menu_bank_detail_icon, generalFunc.retrieveLangLBl("", "LBL_BANK_DETAILS_TXT"), "" + Utils.MENU_BANK_DETAIL});


        if (generalFunc.getJsonValueStr("SYSTEM_PAYMENT_FLOW", obj_userProfile).equalsIgnoreCase("Method-1")) {
            if (!generalFunc.getJsonValueStr("APP_PAYMENT_MODE", obj_userProfile).equalsIgnoreCase("Cash") /*&& !generalFunc.isDeliverOnlyEnabled()*/) {
                list_menu_items.add(new String[]{"" + R.mipmap.ic_menu_card, generalFunc.retrieveLangLBl("Payment", "LBL_PAYMENT"), "" + Utils.MENU_PAYMENT});
            }
        }


        if (!generalFunc.getJsonValueStr(Utils.WALLET_ENABLE, obj_userProfile).equals("") && generalFunc.getJsonValueStr(Utils.WALLET_ENABLE, obj_userProfile).equalsIgnoreCase("Yes")) {
            list_menu_items.add(new String[]{"" + R.mipmap.ic_menu_wallet, generalFunc.retrieveLangLBl("", "LBL_LEFT_MENU_WALLET"), "" + Utils.MENU_WALLET});
        }

        boolean isWayBillEnabled = generalFunc.getJsonValueStr("WAYBILL_ENABLE", obj_userProfile) != null && generalFunc.getJsonValueStr("WAYBILL_ENABLE", obj_userProfile).equalsIgnoreCase("yes");
        if (isWayBillEnabled) {
            list_menu_items.add(new String[]{"" + R.mipmap.ic_waybill, generalFunc.retrieveLangLBl("Way Bill", "LBL_MENU_WAY_BILL"), "" + Utils.MENU_WAY_BILL});
        }
        if (!generalFunc.getJsonValueStr("eEmailVerified", obj_userProfile).equalsIgnoreCase("YES") ||
                !generalFunc.getJsonValueStr("ePhoneVerified", obj_userProfile).equalsIgnoreCase("YES")) {
            list_menu_items.add(new String[]{"" + R.mipmap.ic_menu_privacy, generalFunc.retrieveLangLBl("", "LBL_ACCOUNT_VERIFY_TXT"), "" + Utils.MENU_ACCOUNT_VERIFY});
        }


        if (!app_type.equalsIgnoreCase(Utils.CabGeneralType_UberX) && !generalFunc.isDeliverOnlyEnabled()) {
            list_menu_items.add(new String[]{"" + R.mipmap.ic_menu_my_heat_view, generalFunc.retrieveLangLBl("", "LBL_MENU_MY_HEATVIEW"), "" + Utils.MENU_MY_HEATVIEW});
        }

        if (!generalFunc.isDeliverOnlyEnabled()) {
            list_menu_items.add(new String[]{"" + R.mipmap.ic_menu_emergency, generalFunc.retrieveLangLBl("Emergency Contact", "LBL_EMERGENCY_CONTACT"), "" + Utils.MENU_EMERGENCY_CONTACT});
        }


        if (app_type.equalsIgnoreCase(Utils.CabGeneralType_Ride) || !generalFunc.isDeliverOnlyEnabled()) {
            list_menu_items.add(new String[]{"" + R.mipmap.ic_menu_feedback, generalFunc.retrieveLangLBl("Rider Feedback", "LBL_RIDER_FEEDBACK"), "" + Utils.MENU_FEEDBACK});
        } else if (app_type.equalsIgnoreCase("Delivery")) {
            list_menu_items.add(new String[]{"" + R.mipmap.ic_menu_feedback, generalFunc.retrieveLangLBl("", "LBL_SENDER_fEEDBACK"), "" + Utils.MENU_FEEDBACK});
        } else {
            list_menu_items.add(new String[]{"" + R.mipmap.ic_menu_feedback, generalFunc.retrieveLangLBl("", "LBL_USER_FEEDBACK"), "" + Utils.MENU_FEEDBACK});
        }


        if (!generalFunc.isDeliverOnlyEnabled()) {
            if (app_type.equalsIgnoreCase(Utils.CabGeneralType_Ride)) {
                list_menu_items.add(new String[]{"" + R.mipmap.ic_menu_chart, generalFunc.retrieveLangLBl("Trip Statistics", "LBL_TRIP_STATISTICS_TXT"), "" + Utils.MENU_TRIP_STATISTICS});
            } else {
                list_menu_items.add(new String[]{"" + R.mipmap.ic_menu_chart, generalFunc.retrieveLangLBl("Trip Statistics", "LBL_STATISTICS"), "" + Utils.MENU_TRIP_STATISTICS});
            }
        }

       // if (generalFunc.isAnyDeliverOptionEnabled()) {
       //     list_menu_items.add(new String[]{"" + R.mipmap.ic_menu_chart, generalFunc.retrieveLangLBl("Trip Statistics", "LBL_TRIP_STATISTICS_TXT_DL"), "" + Utils.MENU_ORDER_STATISTICS});
       // }


        if (!generalFunc.getJsonValueStr(Utils.REFERRAL_SCHEME_ENABLE, obj_userProfile).equals("") && generalFunc.getJsonValueStr(Utils.REFERRAL_SCHEME_ENABLE, obj_userProfile).equalsIgnoreCase("Yes")) {
            list_menu_items.add(new String[]{"" + R.mipmap.ic_menu_invite, generalFunc.retrieveLangLBl("Invite Friends", "LBL_INVITE_FRIEND_TXT"), "" + Utils.MENU_INVITE_FRIEND});
        }

        if (generalFunc.getJsonValueStr("ENABLE_NEWS_SECTION", obj_userProfile) != null && generalFunc.getJsonValueStr("ENABLE_NEWS_SECTION", obj_userProfile).equalsIgnoreCase("yes")) {
            list_menu_items.add(new String[]{"" + R.drawable.ic_menu_notification, generalFunc.retrieveLangLBl("Notifications", "LBL_NOTIFICATIONS"), "" + Utils.MENU_NOTIFICATION});
        }
        list_menu_items.add(new String[]{"" + R.mipmap.ic_menu_support, generalFunc.retrieveLangLBl("Support", "LBL_SUPPORT_HEADER_TXT"), "" + Utils.MENU_SUPPORT});

        drawerAdapter.notifyDataSetChanged();
    }


    public void setUserInfo() {
        ((MTextView) view.findViewById(R.id.userNameTxt)).setText(generalFunc.getJsonValueStr("vName", obj_userProfile) + " "
                + generalFunc.getJsonValueStr("vLastName", obj_userProfile));
        ((MTextView) view.findViewById(R.id.walletbalncetxt)).setText(generalFunc.retrieveLangLBl("", "LBL_WALLET_BALANCE") + ": " + generalFunc.convertNumberWithRTL(generalFunc.getJsonValueStr("user_available_balance", obj_userProfile)));

        (new AppFunctions(mContext)).checkProfileImage((SelectableRoundedImageView) view.findViewById(R.id.userImgView), obj_userProfile.toString(), "vImage");
    }

    public void openMenuProfile() {
        Bundle bn = new Bundle();
        /*if (mContext instanceof LiveTaskListActivity) {
            bn.putBoolean("isDriverOnline", true);
        } else {*/
            bn.putBoolean("isDriverOnline", isDriverOnline);
      //  }

        new StartActProcess(mContext).startActForResult(MyProfileActivity.class, bn, Utils.MY_PROFILE_REQ_CODE);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {
        int itemId = GeneralFunctions.parseIntegerValue(0, list_menu_items.get(position)[2]);
        Bundle bn = new Bundle();
        Utils.hideKeyboard(mContext);

        drawerAdapter.notifyDataSetChanged();
        switch (itemId) {

            case Utils.MENU_PROFILE:
                openMenuProfile();
                break;
            case Utils.MENU_SUBSCRIPTION:
                new StartActProcess(mContext).startAct(SubscriptionActivity.class);
                break;
            case Utils.MENU_SET_AVAILABILITY:
                new StartActProcess(mContext).startAct(SetAvailabilityActivity.class);
                break;
            case Utils.MENU_MY_GALLERY:
                new StartActProcess(mContext).startAct(MyGalleryActivity.class);
                break;
            case Utils.MENU_PAYMENT:
                new StartActProcess(mContext).startActForResult(CardPaymentActivity.class, bn, Utils.CARD_PAYMENT_REQ_CODE);
                break;
            case Utils.MENU_RIDE_HISTORY:
                //new StartActProcess(mContext).startActWithData(RideHistoryActivity.class, bn);
                break;
            case Utils.MENU_BOOKINGS:
                new StartActProcess(mContext).startActWithData(BookingsActivity.class, bn);
                break;
            case Utils.MENU_FEEDBACK:
                new StartActProcess(mContext).startActWithData(DriverFeedbackActivity.class, bn);
                break;
            case Utils.MENU_BANK_DETAIL:
                new StartActProcess(mContext).startActWithData(BankDetailActivity.class, bn);
                break;
            case Utils.MENU_ABOUT_US:
                new StartActProcess(mContext).startAct(StaticPageActivity.class);
                break;
            case Utils.MENU_POLICY:
                (new StartActProcess(mContext)).openURL(CommonUtilities.SERVER_URL + "privacy-policy");
                break;
            case Utils.MENU_CONTACT_US:
                new StartActProcess(mContext).startAct(ContactUsActivity.class);
                break;
            case Utils.MENU_YOUR_DOCUMENTS:
                bn.putString("PAGE_TYPE", "Driver");
                bn.putString("iDriverVehicleId", "");
                bn.putString("doc_file", "");
                bn.putString("iDriverVehicleId", "");
                bn.putString("seltype", app_type);
                new StartActProcess(mContext).startActWithData(ListOfDocumentActivity.class, bn);
                break;
            case Utils.MENU_TRIP_STATISTICS:
                new StartActProcess(mContext).startActWithData(StatisticsActivity.class, bn);
                break;
            case Utils.MENU_ORDER_STATISTICS:
                new StartActProcess(mContext).startActWithData(OrderStatisticsActivity.class, bn);
                break;
            case Utils.MENU_MANAGE_VEHICLES:
                bn.putString("iDriverVehicleId", generalFunc.getJsonValueStr("iDriverVehicleId", obj_userProfile));
                bn.putString("app_type", app_type);
                if (mContext instanceof LiveTaskListActivity) {
                    bn.putString("isDriverOnline", "true");
                }

                if (generalFunc.isDeliverOnlyEnabled()) {
                    new StartActProcess(mContext).startActWithData(ManageVehiclesActivity.class, bn);
                } else {
                    if (app_type.equalsIgnoreCase(Utils.CabGeneralType_UberX) || (app_type.equalsIgnoreCase(Utils.CabGeneralTypeRide_Delivery_UberX) && generalFunc.getJsonValueStr("eShowVehicles", obj_userProfile).equalsIgnoreCase("No"))) {
                        bn.putString("UBERX_PARENT_CAT_ID", generalFunc.getJsonValueStr("UBERX_PARENT_CAT_ID", obj_userProfile));
                        (new StartActProcess(mContext)).startActWithData(UfxCategoryActivity.class, bn);
                    } else if (app_type.equalsIgnoreCase(Utils.CabGeneralTypeRide_Delivery_UberX) && generalFunc.getJsonValueStr("eShowVehicles", obj_userProfile).equalsIgnoreCase("Yes")) {
                        bn.putString("apptype", app_type);
                        bn.putString("selView", "vehicle");
                        bn.putInt("totalVehicles", 1);
                        bn.putString("UBERX_PARENT_CAT_ID", app_type.equalsIgnoreCase(Utils.CabGeneralTypeRide_Delivery_UberX) ? generalFunc.getJsonValueStr("UBERX_PARENT_CAT_ID", obj_userProfile) : "");
                        new StartActProcess(mContext).startActWithData(UploadDocTypeWiseActivity.class, bn);

                    } else {
                        new StartActProcess(mContext).startActWithData(ManageVehiclesActivity.class, bn);
                    }
                }

                break;

            case Utils.MENU_HELP:
                new StartActProcess(mContext).startAct(HelpActivity.class);
                break;
            case Utils.MENU_WALLET:
                iswallet = true;
                new StartActProcess(mContext).startActWithData(MyWalletActivity.class, bn);
                break;
            case Utils.MENU_WAY_BILL:
                JSONObject last_trip_data = generalFunc.getJsonObject("TripDetails", obj_userProfile);
                if (generalFunc.getJsonValueStr("eSystem", last_trip_data).equalsIgnoreCase(Utils.eSystem_Type) || generalFunc.isDeliverOnlyEnabled()) {
                    bn.putString("eSystem", "yes");
                }
                new StartActProcess(mContext).startActWithData(WayBillActivity.class, bn);
                break;
            case Utils.MENU_ACCOUNT_VERIFY:
                boolean isEmailVerified=generalFunc.getJsonValueStr("eEmailVerified", obj_userProfile).equalsIgnoreCase("YES");
                boolean isPhoneVerified=generalFunc.getJsonValueStr("ePhoneVerified", obj_userProfile).equalsIgnoreCase("YES");

                if (!isEmailVerified ||
                        !isPhoneVerified) {
                    Bundle bn1 = new Bundle();
                    if (!isEmailVerified &&
                            !isPhoneVerified) {
                        bn1.putString("msg", "DO_EMAIL_PHONE_VERIFY");
                    } else if (!isEmailVerified) {
                        bn1.putString("msg", "DO_EMAIL_VERIFY");
                    } else if (!isPhoneVerified) {
                        bn1.putString("msg", "DO_PHONE_VERIFY");
                    }

                    new StartActProcess(mContext).startActForResult(VerifyInfoActivity.class, bn1, Utils.VERIFY_INFO_REQ_CODE);
                }
                break;
            case Utils.MENU_INVITE_FRIEND:
                new StartActProcess(mContext).startActWithData(InviteFriendsActivity.class, bn);
                break;
            case Utils.MENU_EMERGENCY_CONTACT:
                new StartActProcess(mContext).startAct(EmergencyContactActivity.class);
                break;
            case Utils.MENU_SUPPORT:
                new StartActProcess(mContext).startAct(SupportActivity.class);
                break;
            case Utils.MENU_NOTIFICATION:
                new StartActProcess(mContext).startAct(NotificationActivity.class);
                break;
            case Utils.MENU_YOUR_TRIPS:
                new StartActProcess(mContext).startActWithData(BookingsActivity.class, bn);
                break;
            case Utils.MENU_ORDER:
                new StartActProcess(mContext).startActWithData(OrderHistoryActivity.class, bn);
                break;
            case Utils.MENU_SIGN_OUT:
                MyApp.getInstance().logOutFromDevice(false);
                break;
            case Utils.MENU_MY_HEATVIEW:
                new StartActProcess(mContext).startActWithData(MyHeatViewActivity.class, bn);
        }

        closeDrawer();
    }

    public void closeDrawer() {
        (view.findViewById(R.id.left_linear)).setVisibility(View.GONE);
        mDrawerLayout.closeDrawer(GravityCompat.START);
    }

    public boolean checkDrawerState(boolean isOpenDrawer) {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START) == true) {
            closeDrawer();
            return true;
        } else if (isOpenDrawer == true) {
            openDrawer();
        }
        return false;
    }

    public void openDrawer() {
        (view.findViewById(R.id.left_linear)).setVisibility(View.VISIBLE);
        mDrawerLayout.openDrawer(GravityCompat.START);
    }

    private void setMenuAction() {
        if (isMenuState) {
            openDrawer();
        } else {
            setMenuState(true);
            if (drawerClickListener != null) {
                drawerClickListener.onClick();
            }
        }
    }

    public void setItemClickList(DrawerClickListener itemClickList) {
        this.drawerClickListener = itemClickList;
    }

    public interface DrawerClickListener {
        void onClick();
    }

    public class setOnClickLst implements View.OnClickListener {

        @Override
        public void onClick(View v) {

            switch (v.getId()) {
                case R.id.menuImgView:
                    setMenuAction();
                    break;
                case R.id.backImgView:
                    setMenuAction();
                    break;

                case R.id.imgSetting:
                        closeDrawer();
                        obj_userProfile = generalFunc.getJsonObject(generalFunc.retrieveValue(Utils.USER_PROFILE_JSON));

                        if (generalFunc.retrieveValue(Utils.FEMALE_RIDE_REQ_ENABLE).equalsIgnoreCase("yes")&& !generalFunc.isDeliverOnlyEnabled()) {
                            if (generalFunc.getJsonValueStr("eGender", obj_userProfile).equalsIgnoreCase("feMale")) {
                                new StartActProcess(mContext).startAct(PrefranceActivity.class);
                            } else {
                                if (generalFunc.getJsonValueStr("eGender", obj_userProfile).equals("")) {
                                    genderDailog();

                                } else {
                                    menuListView.performItemClick(view, 0, Utils.MENU_PROFILE);
                                }
                            }
                        } else {
                            menuListView.performItemClick(view, 0, Utils.MENU_PROFILE);
                        }

                    break;

                case R.id.logoutarea:
                    final GenerateAlertBox generateAlert = new GenerateAlertBox(mContext);
                    generateAlert.setCancelable(false);
                    generateAlert.setBtnClickList(new GenerateAlertBox.HandleAlertBtnClick() {
                        @Override
                        public void handleBtnClick(int btn_id) {
                            if (btn_id == 0) {
                                generateAlert.closeAlertBox();
                            } else {
                                MyApp.getInstance().logOutFromDevice(false);
                            }

                        }
                    });
                    generateAlert.setContentMessage(generalFunc.retrieveLangLBl("Logout", "LBL_LOGOUT"), generalFunc.retrieveLangLBl("Are you sure you want to logout?", "LBL_WANT_LOGOUT_APP_TXT"));
                    generateAlert.setPositiveBtn(generalFunc.retrieveLangLBl("", "LBL_YES"));
                    generateAlert.setNegativeBtn(generalFunc.retrieveLangLBl("", "LBL_NO"));
                    generateAlert.showAlertBox();

                    break;
            }
        }
    }

    public void genderDailog() {
        closeDrawer();

        final Dialog builder = new Dialog(mContext, R.style.Theme_Dialog);
        builder.requestWindowFeature(Window.FEATURE_NO_TITLE);
        builder.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        builder.setContentView(R.layout.gender_view);
        builder.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);

//        LayoutInflater inflater = (LayoutInflater) getActContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//        View dialogView = inflater.inflate(R.layout.gender_view, null);

        final MTextView genderTitleTxt = (MTextView) builder.findViewById(R.id.genderTitleTxt);
        final MTextView maleTxt = (MTextView) builder.findViewById(R.id.maleTxt);
        final MTextView femaleTxt = (MTextView) builder.findViewById(R.id.femaleTxt);
        final ImageView gendercancel = (ImageView) builder.findViewById(R.id.gendercancel);
        final ImageView gendermale = (ImageView) builder.findViewById(R.id.gendermale);
        final ImageView genderfemale = (ImageView) builder.findViewById(R.id.genderfemale);
        final LinearLayout male_area = (LinearLayout) builder.findViewById(R.id.male_area);
        final LinearLayout female_area = (LinearLayout) builder.findViewById(R.id.female_area);

        genderTitleTxt.setText(generalFunc.retrieveLangLBl("Select your gender to continue", "LBL_SELECT_GENDER"));
        maleTxt.setText(generalFunc.retrieveLangLBl("Male", "LBL_MALE_TXT"));
        femaleTxt.setText(generalFunc.retrieveLangLBl("FeMale", "LBL_FEMALE_TXT"));

        gendercancel.setOnClickListener(v -> builder.dismiss());

        male_area.setOnClickListener(v -> {
            callgederApi("Male");
            builder.dismiss();

        });
        female_area.setOnClickListener(v -> {
            callgederApi("Female");
            builder.dismiss();

        });

        builder.show();

    }

    public void callgederApi(String egender) {
        HashMap<String, String> parameters = new HashMap<>();
        parameters.put("type", "updateUserGender");
        parameters.put("UserType", Utils.userType);
        parameters.put("iMemberId", generalFunc.getMemberId());
        parameters.put("eGender", egender);


        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(mContext, parameters);
        exeWebServer.setLoaderConfig(mContext, true, generalFunc);
        exeWebServer.setDataResponseListener(responseString -> {
            JSONObject responseStringObject=generalFunc.getJsonObject(responseString);

            boolean isDataAvail = GeneralFunctions.checkDataAvail(Utils.action_str, responseStringObject);

            String message = generalFunc.getJsonValueStr(Utils.message_str, responseStringObject);
            if (isDataAvail) {
                generalFunc.storeData(Utils.USER_PROFILE_JSON, message);
                obj_userProfile = generalFunc.getJsonObject(generalFunc.retrieveValue(Utils.USER_PROFILE_JSON));
                imgSetting.performClick();
            }
        });
        exeWebServer.execute();
    }
}
