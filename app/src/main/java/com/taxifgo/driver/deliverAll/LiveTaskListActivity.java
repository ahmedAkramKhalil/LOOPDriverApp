package com.taxifgo.driver.deliverAll;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.adapter.files.deliverAll.OrderListRecycleAdapter;
import com.taxifgo.driver.BaseActivity;
import com.taxifgo.driver.CallScreenActivity;
import com.taxifgo.driver.R;
import com.fragments.MyBookingFragment;
import com.fragments.MyProfileFragment;
import com.fragments.MyWalletFragment;
import com.general.files.AddBottomBar;
import com.general.files.AppFunctions;
import com.general.files.ExecuteWebServerUrl;
import com.general.files.GeneralFunctions;
import com.general.files.GetLocationUpdates;
import com.general.files.MyApp;
import com.general.files.SinchService;
import com.general.files.StartActProcess;
import com.model.deliverAll.liveTaskListDataModel;
import com.sinch.android.rtc.calling.Call;
import com.utils.CommonUtilities;
import com.utils.Utils;
import com.view.ErrorView;
import com.view.MTextView;

import org.apache.commons.lang3.text.WordUtils;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

/**
 * Created by Admin on 17-04-18.
 */

public class LiveTaskListActivity extends BaseActivity implements OrderListRecycleAdapter.OnItemClickListener {

    public static final String TAG = "MyServiceTag";
    GeneralFunctions generalFunc;
    MTextView titleTxt;
    ImageView menuImgView, backImgView;
    /*Pagination*/
    boolean mIsLoading = false;
    boolean isNextPageAvailable = false;
    String next_page_str = "";
    ProgressBar loading_order_list;
    MTextView noOrderTxt;
    ErrorView errorView;
    String iOrderId = "";
    HashMap<String, String> data_trip;
    String vImage, vName;
    private OrderListRecycleAdapter orderListRecycleAdapter;
    private ArrayList<liveTaskListDataModel> list = new ArrayList<>();
    private RecyclerView orderListRecyclerView;
    private JSONObject userProfileJsonObj;

    AddBottomBar addBottomBar;
    FrameLayout container;
    boolean iswalletFragemnt = false;
    boolean isbookingFragemnt = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_tasks);
        generalFunc = MyApp.getInstance().getGeneralFun(getActContext());
        userProfileJsonObj = generalFunc.getJsonObject(generalFunc.retrieveValue(Utils.USER_PROFILE_JSON));

        addBottomBar = new AddBottomBar(getActContext(), userProfileJsonObj);


        HashMap<String, String> data = (HashMap<String, String>) getIntent().getSerializableExtra("TRIP_DATA");
        this.data_trip = data;

        iOrderId = data_trip.get("iOrderId");

        initView();
        setLabels();


        getAllLiveTasks();

        GetLocationUpdates.getInstance().setTripStartValue(true, true, data_trip.get("iTripId"));

    }

    //    @Override
//    protected void onRestart() {
//        super.onRestart();
//        Logger.d("okhttp","On restart of LiveTaskListActivity");
//        getAllLiveTasks();
//    }

    @Override
    protected void onResume() {
        super.onResume();

        if (myWalletFragment != null && iswalletFragemnt) {
            myWalletFragment.onResume();
        }

        if (myBookingFragment != null && isbookingFragemnt) {
            myBookingFragment.onResume();
        }
    }

    private void initView() {

        container = (FrameLayout) findViewById(R.id.container);
        titleTxt = (MTextView) findViewById(R.id.titleTxt);
        backImgView = (ImageView) findViewById(R.id.backImgView);
        menuImgView = (ImageView) findViewById(R.id.menuImgView);
        backImgView.setVisibility(View.GONE);
        menuImgView.setVisibility(View.GONE);
        orderListRecyclerView = (RecyclerView) findViewById(R.id.orderListRecyclerView);
        noOrderTxt = (MTextView) findViewById(R.id.noOrderTxt);
        loading_order_list = (ProgressBar) findViewById(R.id.loading_order_list);
        errorView = (ErrorView) findViewById(R.id.errorView);
    }

    MyProfileFragment myProfileFragment;
    MyWalletFragment myWalletFragment;
    public MyBookingFragment myBookingFragment;

    public void openProfileFragment() {
        iswalletFragemnt = false;
        isbookingFragemnt = false;

//        if (myProfileFragment != null) {
//            myProfileFragment = null;
//            Utils.runGC();
//        }


        container.setVisibility(View.VISIBLE);
        if (myProfileFragment == null) {
            myProfileFragment = new MyProfileFragment();
        }

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, myProfileFragment).commit();


    }

    public void openWalletFragment() {

//        if (myProfileFragment != null) {
//            myProfileFragment = null;
//            Utils.runGC();
//        }

        iswalletFragemnt = true;
        isbookingFragemnt =false;

        container.setVisibility(View.VISIBLE);
        if (myWalletFragment == null) {
            myWalletFragment = new MyWalletFragment();
        }

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, myWalletFragment).commit();
    }

    public void openBookingFrgament() {

//        if (myProfileFragment != null) {
//            myProfileFragment = null;
//            Utils.runGC();
//        }

        iswalletFragemnt = false;
        isbookingFragemnt = true;

        container.setVisibility(View.VISIBLE);
        if (myBookingFragment == null) {
            myBookingFragment = new MyBookingFragment();
        }else {
            myBookingFragment.onDestroy();
            myBookingFragment = new MyBookingFragment();
        }

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, myBookingFragment).commit();
    }


    public void manageHome() {
        iswalletFragemnt = false;
        isbookingFragemnt=false;
        container.setVisibility(View.GONE);
    }


    private void getAllLiveTasks() {
        list = new ArrayList<>();
        orderListRecycleAdapter = new OrderListRecycleAdapter(getActContext(), list, generalFunc, false);
        orderListRecyclerView.setAdapter(orderListRecycleAdapter);
        orderListRecycleAdapter.notifyDataSetChanged();
        orderListRecycleAdapter.setOnItemClickListener(this);

        orderListRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                                                      @Override
                                                      public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                                                          super.onScrolled(recyclerView, dx, dy);

                                                          int visibleItemCount = recyclerView.getLayoutManager().getChildCount();
                                                          int totalItemCount = recyclerView.getLayoutManager().getItemCount();
                                                          int firstVisibleItemPosition = ((LinearLayoutManager) recyclerView.getLayoutManager()).findFirstVisibleItemPosition();

                                                          int lastInScreen = firstVisibleItemPosition + visibleItemCount;
                                                          if ((lastInScreen == totalItemCount) && !(mIsLoading) && isNextPageAvailable == true) {

                                                              mIsLoading = true;
                                                              orderListRecycleAdapter.addFooterView();

                                                              getLiveTaskOrderList(true);

                                                          } else if (isNextPageAvailable == false) {
                                                              orderListRecycleAdapter.removeFooterView();
                                                          }
                                                      }
                                                  }

        );

        getLiveTaskOrderList(false);
    }

    public void getLiveTaskOrderList(final boolean isLoadMore) {
        if (errorView.getVisibility() == View.VISIBLE) {
            errorView.setVisibility(View.GONE);
        }

        if (loading_order_list.getVisibility() == View.GONE) {
            loading_order_list.setVisibility(View.VISIBLE);
        }


        final HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "GetLiveTaskDetailDriver");
        parameters.put("iOrderId", iOrderId);
        if (isLoadMore == true) {
            parameters.put("page", next_page_str);
        }
        parameters.put("eSystem", Utils.eSystem_Type);

        noOrderTxt.setVisibility(View.GONE);
        list.clear();
        final ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(), parameters);
        exeWebServer.setDataResponseListener(responseString -> {

            noOrderTxt.setVisibility(View.GONE);
            closeLoader();
            JSONObject responseStringObject = generalFunc.getJsonObject(responseString);

            if (responseStringObject != null && !responseStringObject.equals("")) {


                if (generalFunc.checkDataAvail(Utils.action_str, responseStringObject)) {

                    String nextPage = generalFunc.getJsonValueStr("NextPage", responseStringObject);

                    JSONObject msg_obj = generalFunc.getJsonObject("message", responseStringObject);

                    // User's Details Add
                    liveTaskListDataModel model = new liveTaskListDataModel();

                    // Restaurant's Details Add
                    liveTaskListDataModel model2 = new liveTaskListDataModel();

                    /*Set Order detail*/
                    model.setPickedFromRes(generalFunc.getJsonValueStr("PickedFromRes", msg_obj));
                    model.setOrderNumber(generalFunc.convertNumberWithRTL(generalFunc.getJsonValueStr("vOrderNo", msg_obj)));
                    model.setIsPhotoUploaded(generalFunc.getJsonValueStr("isPhotoUploaded", msg_obj));
                    model.setvVehicleType(generalFunc.getJsonValueStr("vVehicleType", msg_obj));

                    model2.setOrderNumber(generalFunc.convertNumberWithRTL(generalFunc.getJsonValueStr("vOrderNo", msg_obj)));
                    model2.setPickedFromRes(generalFunc.getJsonValueStr("PickedFromRes", msg_obj));
                    model2.setIsPhotoUploaded(generalFunc.getJsonValueStr("isPhotoUploaded", msg_obj));
                    model2.setvVehicleType(generalFunc.getJsonValueStr("vVehicleType", msg_obj));

                    /*Set Restaurant Detail*/
                    model2.setRestaurantName(generalFunc.getJsonValueStr("vCompany", msg_obj));
                    model2.setRestaurantLattitude(generalFunc.getJsonValueStr("vRestuarantLocationLat", msg_obj));
                    model2.setRestaurantLongitude(generalFunc.getJsonValueStr("vRestuarantLocationLong", msg_obj));
                    model2.setRestaurantNumber(generalFunc.getJsonValueStr("vPhoneRestaurant", msg_obj));
                    model2.setRestaurantId(generalFunc.getJsonValueStr("iCompanyId", msg_obj));
                    model2.setRestaurantImage(generalFunc.getJsonValueStr("vRestuarantImage", msg_obj));


                    /*Set Restaurant Detail Model*/
                    model.setRestaurantName(generalFunc.getJsonValueStr("vCompany", msg_obj));
                    model.setRestaurantLattitude(generalFunc.getJsonValueStr("vRestuarantLocationLat", msg_obj));
                    model.setRestaurantLongitude(generalFunc.getJsonValueStr("vRestuarantLocationLong", msg_obj));
                    model.setRestaurantNumber(generalFunc.getJsonValueStr("vPhoneRestaurant", msg_obj));
                    model.setRestaurantId(generalFunc.getJsonValueStr("iCompanyId", msg_obj));
                    model.setRestaurantImage(generalFunc.getJsonValueStr("vRestuarantImage", msg_obj));


                    String restAddress = generalFunc.getJsonValueStr("vRestuarantLocation", msg_obj);
                    model2.setRestaurantAddress(Utils.checkText(restAddress) ? WordUtils.capitalize(restAddress) : restAddress);

                    /*User Detail Model 2*/
                    model2.setUserName(generalFunc.getJsonValueStr("UserName", msg_obj));
                    String userAddress = generalFunc.getJsonValueStr("UserAdress", msg_obj);
                    model2.setUserAddress(Utils.checkText(userAddress) ? WordUtils.capitalize(userAddress) : userAddress);
                    model2.setUserLattitude(generalFunc.getJsonValueStr("vLatitude", msg_obj));
                    model2.setUserLongitude(generalFunc.getJsonValueStr("vLongitude", msg_obj));
                    model2.setUserNumber(generalFunc.getJsonValueStr("vPhoneUser", msg_obj));

                    /*User Detail*/
                    model.setUserName(generalFunc.getJsonValueStr("UserName", msg_obj));
                    model.setUserAddress(Utils.checkText(userAddress) ? WordUtils.capitalize(userAddress) : userAddress);
                    model.setUserLattitude(generalFunc.getJsonValueStr("vLatitude", msg_obj));
                    model.setUserLongitude(generalFunc.getJsonValueStr("vLongitude", msg_obj));
                    model.setUserNumber(generalFunc.getJsonValueStr("vPhoneUser", msg_obj));

                    /*Set Lables*/
                    model.setLBL_CALL_TXT(generalFunc.retrieveLangLBl("Call", "LBL_CALL_TXT"));
                    model.setLBL_NAVIGATE(generalFunc.retrieveLangLBl("Navigate", "LBL_NAVIGATE"));
                    model.setLBL_PICKUP(generalFunc.retrieveLangLBl("Pickup", "LBL_PICKUP"));
                    model.setLBL_DELIVER(generalFunc.retrieveLangLBl("Deliver", "LBL_DELIVER"));
                    model.setLBL_CURRENT_TASK_TXT(generalFunc.retrieveLangLBl("Current Task", "LBL_CURRENT_TASK_TXT"));
                    model.setLBL_NEXT_TASK_TXT(generalFunc.retrieveLangLBl("Next Task", "LBL_NEXT_TASK_TXT"));


                    model2.setLBL_CALL_TXT(generalFunc.retrieveLangLBl("Call", "LBL_CALL_TXT"));
                    model2.setLBL_NAVIGATE(generalFunc.retrieveLangLBl("Navigate", "LBL_NAVIGATE"));
                    model2.setLBL_PICKUP(generalFunc.retrieveLangLBl("Pickup", "LBL_PICKUP"));
                    model2.setLBL_DELIVER(generalFunc.retrieveLangLBl("Deliver", "LBL_DELIVER"));
                    model2.setLBL_CURRENT_TASK_TXT(generalFunc.retrieveLangLBl("Current Task", "LBL_CURRENT_TASK_TXT"));
                    model2.setLBL_NEXT_TASK_TXT(generalFunc.retrieveLangLBl("Next Task", "LBL_NEXT_TASK_TXT"));
                    model2.setLBL_NEXT_TASK_TXT(generalFunc.retrieveLangLBl("Next Task", "LBL_NEXT_TASK_TXT"));


                    model.setIsRestaurant("NO");
                    list.add(model);

                    if (generalFunc.getJsonValueStr("PickedFromRes", msg_obj).equalsIgnoreCase("No") || (generalFunc.getJsonValueStr("PickedFromRes", msg_obj).equalsIgnoreCase("Yes") && generalFunc.getJsonValueStr("isPhotoUploaded", msg_obj).equalsIgnoreCase("No"))) {
                        model2.setIsRestaurant("Yes");
                        list.add(model2);
                    }

                    Collections.reverse(list);

                    if (!nextPage.equals("") && !nextPage.equals("0")) {
                        next_page_str = nextPage;
                        isNextPageAvailable = true;
                    } else {
                        removeNextPageConfig();
                    }

                    orderListRecycleAdapter.notifyDataSetChanged();

                } else {
                    if (list.size() == 0) {
                        removeNextPageConfig();
                        noOrderTxt.setText(generalFunc.retrieveLangLBl("", generalFunc.getJsonValueStr(Utils.message_str, responseStringObject)));
                        noOrderTxt.setVisibility(View.VISIBLE);
                    }
                }

                orderListRecycleAdapter.notifyDataSetChanged();


            } else {
                if (!isLoadMore) {
                    removeNextPageConfig();
                    generateErrorView();
                }

            }

            mIsLoading = false;
        });

        if (!isLoadMore) {
            if (list.size() == 0) {
                exeWebServer.execute();
            }

        } else {
            exeWebServer.execute();
        }


    }

    public void generateErrorView() {

        closeLoader();

        generalFunc.generateErrorView(errorView, "LBL_ERROR_TXT", "LBL_NO_INTERNET_TXT");

        if (errorView.getVisibility() != View.VISIBLE) {
            errorView.setVisibility(View.VISIBLE);
        }


        errorView.setOnRetryListener(() -> getLiveTaskOrderList(false));
    }

    public void removeNextPageConfig() {
        next_page_str = "";
        isNextPageAvailable = false;
        mIsLoading = false;
        orderListRecycleAdapter.removeFooterView();
    }

    public void closeLoader() {
        if (loading_order_list.getVisibility() == View.VISIBLE) {
            loading_order_list.setVisibility(View.GONE);
        }
    }

    public void setLabels() {
        titleTxt.setText(generalFunc.retrieveLangLBl("", "LBL_LIVE_TASKS"));
        noOrderTxt.setText("Live Tasks");
    }

    public void sinchCall(boolean isStore) {

        if (isStore) {
            if (!list.get(0).getRestaurantImage().equals("")) {
                vImage = CommonUtilities.COMPANY_PHOTO_PATH + list.get(0).getRestaurantId() + "/"
                        + list.get(0).getRestaurantImage();
            }
            vName = list.get(0).getRestaurantName();

        } else {

            if (!data_trip.get("PPicName").equals("")) {
                vImage = CommonUtilities.USER_PHOTO_PATH + data_trip.get("passengerId") + "/"
                        + data_trip.get("PPicName");
            }
            vName = data_trip.get("PName");
        }

        if (generalFunc.isCallPermissionGranted(false) == false) {
            generalFunc.isCallPermissionGranted(true);
        } else {

            if (new AppFunctions(getActContext()).checkSinchInstance(getSinchServiceInterface())) {

                HashMap<String, String> hashMap = new HashMap<>();


                hashMap.put("Id", generalFunc.getMemberId());
                hashMap.put("Name", generalFunc.getJsonValueStr("vName", userProfileJsonObj));
                hashMap.put("PImage", generalFunc.getJsonValueStr("vImage", userProfileJsonObj));
                hashMap.put("type", Utils.userType);
                hashMap.put("isDriver", "Yes");


                getSinchServiceInterface().getSinchClient().setPushNotificationDisplayName(generalFunc.retrieveLangLBl("", "LBL_INCOMING_CALL"));
                if (isStore) {
                    Call call = getSinchServiceInterface().callUser(Utils.CALLTOSTORE + "_" + list.get(0).getRestaurantId(), hashMap);
                    String callId = call.getCallId();
                    Intent callScreen = new Intent(getActContext(), CallScreenActivity.class);
                    callScreen.putExtra(SinchService.CALL_ID, callId);
                    callScreen.putExtra("vImage", vImage);
                    callScreen.putExtra("vName", vName);
                    startActivity(callScreen);

                } else {
                    Call call = getSinchServiceInterface().callUser(Utils.CALLTOPASSENGER + "_" + data_trip.get("PassengerId"), hashMap);
                    String callId = call.getCallId();
                    Intent callScreen = new Intent(getActContext(), CallScreenActivity.class);
                    callScreen.putExtra(SinchService.CALL_ID, callId);
                    callScreen.putExtra("vImage", vImage);
                    callScreen.putExtra("vName", vName);
                    startActivity(callScreen);

                }
            }
        }
    }


    @Override
    public void onItemClickList(int position, String pickedFromRes) {
        if (list == null || list.size() == 0) {
            return;
        }


        if (pickedFromRes.equalsIgnoreCase("Call")) {


            if (list.get(position).getPickedFromRes().equalsIgnoreCase("No")) {
                // call(list.get(position).getRestaurantNumber());

                if (generalFunc.getJsonValueStr("RIDE_DRIVER_CALLING_METHOD", userProfileJsonObj).equals("Voip")) {
                    sinchCall(true);
                } else {
                    getMaskNumber(list.get(position).getRestaurantNumber());
                }
            } else {
                // call(list.get(position).getUserNumber());

                if (generalFunc.getJsonValueStr("RIDE_DRIVER_CALLING_METHOD", userProfileJsonObj).equals("Voip")) {
                    sinchCall(false);
                } else {
                    getMaskNumber(list.get(position).getUserNumber());
                }
            }
        } else if (pickedFromRes.equalsIgnoreCase("Navigate")) {


            if (list.get(position).isRestaurant().equalsIgnoreCase("Yes")) {
                Bundle bn = new Bundle();
                bn.putString("type", "trackRest");
                bn.putSerializable("TRIP_DATA", data_trip);
                bn.putSerializable("currentTaskData", list.get(position));
                if (!list.get(0).getRestaurantImage().equals("")) {
                    vImage = CommonUtilities.COMPANY_PHOTO_PATH + list.get(0).getRestaurantId() + "/"
                            + list.get(0).getRestaurantImage();
                }
                bn.putString("vImage", vImage);
                bn.putString("callid", list.get(0).getRestaurantId());
                bn.putBoolean("isStore", true);
                new StartActProcess(getActContext()).startActWithData(TrackOrderActivity.class, bn);
            } else {
                Bundle bn = new Bundle();
                bn.putString("type", "trackUser");
                bn.putSerializable("TRIP_DATA", data_trip);
                bn.putSerializable("currentTaskData", list.get(position));
                if (!data_trip.get("PPicName").equals("")) {
                    vImage = CommonUtilities.USER_PHOTO_PATH + data_trip.get("passengerId") + "/"
                            + data_trip.get("PPicName");
                }
                bn.putString("vImage", vImage);
                bn.putBoolean("isStore", false);
                bn.putString("callid", data_trip.get("passengerId"));
                new StartActProcess(getActContext()).startActWithData(TrackOrderActivity.class, bn);
            }

        } else {

            Bundle bn = new Bundle();
            bn.putSerializable("TRIP_DATA", data_trip);
            bn.putString("isPhotoUploaded", list.get(position).getIsPhotoUploaded());
            if (list.get(position).getPickedFromRes().equalsIgnoreCase("No") || (list.get(position).getPickedFromRes().equalsIgnoreCase("Yes") && list.get(position).getIsPhotoUploaded().equalsIgnoreCase("No"))) {
                bn.putString("isDeliver", "No");
                bn.putString("PickedFromRes", list.get(position).getPickedFromRes());
                new StartActProcess(getActContext()).startActForResult(LiveTrackOrderDetailActivity.class, bn, Utils.ORDER_DETAIL_REQUEST_CODE);
            } else {
                bn.putString("isDeliver", "Yes");
                new StartActProcess(getActContext()).startActForResult(LiveTrackOrderDetail2Activity.class, bn, Utils.ORDER_DETAIL_REQUEST_CODE);
            }


        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Utils.ORDER_DETAIL_REQUEST_CODE) {
            getAllLiveTasks();
        }

    }

    public void getMaskNumber(String number) {
        if (generalFunc.getJsonValueStr("CALLMASKING_ENABLED", userProfileJsonObj).equalsIgnoreCase("Yes")) {
            HashMap<String, String> parameters = new HashMap<String, String>();
            parameters.put("type", "getCallMaskNumber");
            parameters.put("iOrderId", iOrderId);
            parameters.put("iTripid", data_trip.get("iTripId"));
            parameters.put("UserType", Utils.userType);
            parameters.put("iMemberId", generalFunc.getMemberId());
            parameters.put("eSystem", Utils.eSystem_Type);

            ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(), parameters);
            exeWebServer.setLoaderConfig(getActContext(), true, generalFunc);

            exeWebServer.setDataResponseListener(responseString -> {
                JSONObject responseStringObject = generalFunc.getJsonObject(responseString);

                if (responseStringObject != null && !responseStringObject.equals("")) {

                    boolean isDataAvail = GeneralFunctions.checkDataAvail(Utils.action_str, responseStringObject);

                    if (isDataAvail == true) {
                        String message = generalFunc.getJsonValueStr(Utils.message_str, responseStringObject);
                        call(generalFunc.getJsonValue("PhoneNo", message));
                    } else {
                        call(number);
                    }
                } else {
                    generalFunc.showError();
                }
            });
            exeWebServer.execute();
        } else {
            call(number);
        }
    }


    public void call(String phoneNumber) {
        try {

            Intent callIntent = new Intent(Intent.ACTION_DIAL);
            callIntent.setData(Uri.parse("tel:" + phoneNumber));
            startActivity(callIntent);

        } catch (Exception e) {
            // TODO: handle exception
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public Context getActContext() {
        return LiveTaskListActivity.this;
    }

    @Override
    public void onBackPressed() {
        return;
    }

    public class setOnClickList implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            int i = view.getId();
            Utils.hideKeyboard(LiveTaskListActivity.this);
            if (i == R.id.backImgView) {
                LiveTaskListActivity.super.onBackPressed();
            }
        }
    }

}
