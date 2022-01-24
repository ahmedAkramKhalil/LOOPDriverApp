package com.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import com.google.android.material.appbar.AppBarLayout;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.InputType;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.adapter.files.MyHistoryRecycleAdapter;
import com.taxifgo.driver.BookingsActivity;
import com.taxifgo.driver.MainActivity;
import com.taxifgo.driver.MoreServiceInfoActivity;
import com.taxifgo.driver.R;
import com.taxifgo.driver.RideHistoryDetailActivity;
import com.taxifgo.driver.deliverAll.LiveTaskListActivity;
import com.dialogs.OpenListView;
import com.general.files.AppFunctions;
import com.general.files.ExecuteWebServerUrl;
import com.general.files.GeneralFunctions;
import com.general.files.GetLocationUpdates;
import com.general.files.MyApp;
import com.general.files.StartActProcess;
import com.utils.CommonUtilities;
import com.utils.Utils;
import com.view.CreateRoundedView;
import com.view.ErrorView;
import com.view.GenerateAlertBox;
import com.view.MTextView;
import com.view.calendarview.CustomCalendarView;
import com.view.editBox.MaterialEditText;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 */
public class HistoryFragment extends Fragment implements MyHistoryRecycleAdapter.OnItemClickListener, SwipeRefreshLayout.OnRefreshListener, CustomCalendarView.CalendarEventListener {

    View view;

    ProgressBar loading_my_bookings;
    MTextView noRidesTxt;
    public MTextView filterTxt;

    RecyclerView myBookingsRecyclerView;
    ErrorView errorView;

    MyHistoryRecycleAdapter myBookingsRecyclerAdapter;

    ArrayList<HashMap<String, String>> list;

    boolean mIsLoading = false;
    public boolean isNextPageAvailable = false;

    public String next_page_str = "";

    GeneralFunctions generalFunc;

    BookingsActivity bookingAct = null;
    JSONObject userProfileJsonObj;
    String APP_TYPE = "";
    ArrayList<HashMap<String, String>> filterlist;
    ArrayList<HashMap<String, String>> subFilterlist;
    AlertDialog dialog_declineOrder;
    String selectedItemId = "";

    CustomCalendarView calendar_view;
    public LinearLayout calContainerView;

    String SELECTED_DATE = "";
    private LinearLayout filterArea;
    int HISTORYDETAILS = 1;

    private SwipeRefreshLayout swipeRefreshLayout;

    HistoryFragment historyFragment;
    private MyBookingFragment myBookingFragment = null;
    String tripdataPage = "";
    boolean isFirstInstance = true;
    HashMap<String, String> earningamtmap = new HashMap<>();
    public LinearLayout calenderHeaderLayout;
    LinearLayout mainlayout;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_history, container, false);

        if (getActivity() instanceof MainActivity) {
            myBookingFragment = ((MainActivity) getActivity()).myBookingFragment;
            historyFragment = myBookingFragment.getHistoryFrag();
        } else if (getActivity() instanceof LiveTaskListActivity) {
            myBookingFragment = ((LiveTaskListActivity) getActivity()).myBookingFragment;
            historyFragment = myBookingFragment.getHistoryFrag();
        } else if (getActivity() instanceof BookingsActivity) {
            myBookingFragment = null;
            bookingAct = (BookingsActivity) getActivity();
            historyFragment = bookingAct.getHistoryFrag();
        }

        generalFunc = MyApp.getInstance().getGeneralFun(getActivity());

        loading_my_bookings = (ProgressBar) view.findViewById(R.id.loading_my_bookings);
        noRidesTxt = (MTextView) view.findViewById(R.id.noRidesTxt);
        filterTxt = (MTextView) view.findViewById(R.id.filterTxt);
        myBookingsRecyclerView = (RecyclerView) view.findViewById(R.id.myBookingsRecyclerView);
        errorView = (ErrorView) view.findViewById(R.id.errorView);
        calContainerView = (LinearLayout) view.findViewById(R.id.calContainerView);
        calenderHeaderLayout = (LinearLayout) view.findViewById(R.id.calenderHeaderLayout);

        mainlayout = (LinearLayout) view.findViewById(R.id.mainlayout);
        calContainerView.setVisibility(View.GONE);

        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(this);

        filterArea = (LinearLayout) view.findViewById(R.id.filterArea);
        filterArea.setOnClickListener(new setOnClickList());

        list = new ArrayList<>();

        myBookingsRecyclerAdapter = new MyHistoryRecycleAdapter(getActContext(), list, generalFunc, false);
        myBookingsRecyclerView.setAdapter(myBookingsRecyclerAdapter);
        myBookingsRecyclerAdapter.setOnItemClickListener(this);
        userProfileJsonObj = generalFunc.getJsonObject(generalFunc.retrieveValue(Utils.USER_PROFILE_JSON));
        APP_TYPE = generalFunc.getJsonValueStr("APP_TYPE", userProfileJsonObj);

        addCalenderView();

        showHideCalender(false);
        calendar_view.setTitleTextColor(Color.parseColor("#141414"));

        calendar_view.setLeftImage(generalFunc.isRTLmode() ? R.drawable.ic_right_arrow_circle : R.drawable.ic_left_arrow_circle);
        calendar_view.setRightImage(generalFunc.isRTLmode() ? R.drawable.ic_left_arrow_circle : R.drawable.ic_right_arrow_circle);

        calendar_view.setRightImageTint(getResources().getColor(R.color.appThemeColor_1));
        calendar_view.setLeftImageTint(getResources().getColor(R.color.appThemeColor_1));

        myBookingsRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                int topRowVerticalPosition =
                        (recyclerView == null || recyclerView.getChildCount() == 0) ? 0 : recyclerView.getChildAt(0).getTop();
                swipeRefreshLayout.setEnabled(topRowVerticalPosition >= 0);

                int visibleItemCount = myBookingsRecyclerView.getLayoutManager().getChildCount();
                int totalItemCount = myBookingsRecyclerView.getLayoutManager().getItemCount();
                int firstVisibleItemPosition = ((LinearLayoutManager) myBookingsRecyclerView.getLayoutManager()).findFirstVisibleItemPosition();

                int lastInScreen = firstVisibleItemPosition + visibleItemCount;
                if ((lastInScreen == totalItemCount) && !(mIsLoading) && isNextPageAvailable) {
                    mIsLoading = true;
                    myBookingsRecyclerAdapter.addFooterView();
                    getBookingsHistory(true);
                } else if (!isNextPageAvailable) {
                    myBookingsRecyclerAdapter.removeFooterView();
                }
            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }
        });

        return view;
    }

    private void setDate(Date date) {

        Calendar cal = Calendar.getInstance();
        cal.setTime(date);

        SimpleDateFormat date_format = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

        SELECTED_DATE = date_format.format(cal.getTime());
        showHideCalender(false);
        removeNextPageConfig();

        getBookingsHistory(false);
    }

    private void addCalenderView() {
        calendar_view = new CustomCalendarView(getActContext(), AppFunctions.getXmlResource(getActContext(), R.layout.ride_history_cal), calenderHeaderLayout);

        calendar_view.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        ((AppBarLayout) view.findViewById(R.id.appBarLay)).addOnOffsetChangedListener((appBarLayout, verticalOffset) -> {
            if (Math.abs(verticalOffset) == appBarLayout.getTotalScrollRange()) {
                // Collapsed
                showHideCalender(false);
            } else if (verticalOffset == 0) {
                // Expanded
            } else {
                // Somewhere in between
            }
        });

        calendar_view.setCalendarEventListener(this);


        calContainerView.addView(calendar_view);
    }

    public void showHideCalender(boolean show) {
        if (show) {
            calendar_view.setArrowImage(R.drawable.ic_caret_up);
            calendar_view.showDateSelectionView();
            calendar_view.setTitleLayoutBGColor(getResources().getColor(R.color.white));
            calendar_view.setWeekLayoutBGColor(getResources().getColor(R.color.white));
        } else {
            calendar_view.hideDateSelectionView();
            calendar_view.setArrowImage(R.drawable.ic_caret_down);
            calendar_view.setTitleLayoutBGColor(getResources().getColor(R.color.appThemeColor_bg_parent_1));
            calendar_view.setWeekLayoutBGColor(getResources().getColor(R.color.appThemeColor_bg_parent_1));
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (myBookingFragment != null) {

            if (myBookingFragment.getId() != 0) {
                myBookingFragment.filterImageview.setVisibility(View.VISIBLE);
            } else {
                return;
            }

        } else {
            bookingAct = (BookingsActivity) getActivity();
            bookingAct.filterImageview.setVisibility(View.VISIBLE);
        }

        if (Utils.checkText(SELECTED_DATE) && !isFirstInstance) {
            getBookingsHistory(false);
        } else {
            isFirstInstance = !isFirstInstance;
        }

        if (calendar_view != null) {
            calendar_view.invalidate();
        }
    }

    public boolean isDeliver(String eType) {
        if (eType.equals(Utils.CabGeneralType_Deliver) || eType.equalsIgnoreCase("Deliver")) {
            return true;
        }
        return false;
    }

    @Override
    public void onCancelBookingClickList(View v, int position) {
        confirmCancelBooking(list.get(position).get("iCabBookingId"), list.get(position));
    }

    @Override
    public void onTripStartClickList(View v, int position) {
        String contentMsg = "";

        String eTypeVal = list.get(position).get("eTypeVal");

        if (eTypeVal.equalsIgnoreCase(Utils.CabGeneralType_UberX)) {
            contentMsg = generalFunc.retrieveLangLBl("", "LBL_CONFIRM_START_JOB");
        } else if (eTypeVal.equalsIgnoreCase(Utils.CabGeneralType_Ride)) {
            contentMsg = generalFunc.retrieveLangLBl("", "LBL_CONFIRM_START_TRIP_TXT");
        } else {
            contentMsg = generalFunc.retrieveLangLBl("", "LBL_CONFIRM_START_DELIVERY");
        }

        buildMsgOnStartTripBtn(list.get(position).get("iCabBookingId"), list.get(position).get("iActive"), contentMsg);
    }

    @Override
    public void onViewServiceClickList(View v, int position) {
        Bundle bundle = new Bundle();
        bundle.putString("iCabBookingId", list.get(position).get("iCabBookingId"));
        new StartActProcess(getActContext()).startActWithData(MoreServiceInfoActivity.class, bundle);
    }

    @Override
    public void onDetailViewClickList(View v, int position) {
        Bundle bn = new Bundle();
        bn.putString("iTripId", list.get(position).get("iTripId"));
        new StartActProcess(getActivity()).startActForResult(RideHistoryDetailActivity.class, bn, HISTORYDETAILS);

    }

    public void confirmCancelBooking(final String iCabBookingId, HashMap<String, String> list) {
        getDeclineReasonsList(iCabBookingId, list);
    }

    public void getDeclineReasonsList(String iCabBookingId, HashMap<String, String> list) {
        HashMap<String, String> parameters = new HashMap<>();

        parameters.put("type", "GetCancelReasons");
        // parameters.put("iTripId", iCabBookingId);
        parameters.put("iCabBookingId", iCabBookingId);
        parameters.put("iMemberId", generalFunc.getMemberId());
        parameters.put("eUserType", list.get("eTypeVal"));

        ExecuteWebServerUrl exeServerTask = new ExecuteWebServerUrl(getActContext(), parameters);
        exeServerTask.setLoaderConfig(getActContext(), true, generalFunc);
        exeServerTask.setDataResponseListener(responseString -> {
            JSONObject responseStringObj = generalFunc.getJsonObject(responseString);

            if (responseStringObj != null) {

                boolean isDataAvail = GeneralFunctions.checkDataAvail(Utils.action_str, responseStringObj);

                if (isDataAvail) {
                    showDeclineReasonsAlert(responseStringObj, list);
                } else {
                    generalFunc.showGeneralMessage("", generalFunc.retrieveLangLBl("", generalFunc.getJsonValueStr(Utils.message_str, responseStringObj)));
                }

            } else {
                generalFunc.showError();
            }

        });
        exeServerTask.execute();
    }

    String titleDailog = "";

    int selCurrentPosition = -1;

    public void showDeclineReasonsAlert(JSONObject responseString, HashMap<String, String> listdata) {
        selCurrentPosition = -1;
        AlertDialog.Builder builder = new AlertDialog.Builder(getActContext());
        if (listdata.get("iActive").equalsIgnoreCase("Pending")) {
            titleDailog = (generalFunc.retrieveLangLBl("Decline Job", "LBL_DECLINE_BOOKING"));
        } else {
            if (listdata.get("eTypeVal").equalsIgnoreCase(Utils.CabGeneralType_Ride)) {
                titleDailog = (generalFunc.retrieveLangLBl("Cancel Booking", "LBL_CANCEL_TRIP"));
            } else if (listdata.get("eTypeVal").equalsIgnoreCase(Utils.CabGeneralType_UberX)) {
                titleDailog = (generalFunc.retrieveLangLBl("", "LBL_CANCEL_JOB"));
            } else {
                titleDailog = (generalFunc.retrieveLangLBl("", "LBL_CANCEL_DELIVERY"));
            }
        }

        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.decline_order_dialog_design, null);
        builder.setView(dialogView);

        MaterialEditText reasonBox = (MaterialEditText) dialogView.findViewById(R.id.inputBox);
        RelativeLayout commentArea = (RelativeLayout) dialogView.findViewById(R.id.commentArea);
        reasonBox.setHideUnderline(true);
        if (generalFunc.isRTLmode()) {
            reasonBox.setPaddings(0, 0, (int) getResources().getDimension(R.dimen._10sdp), 0);
        } else {
            reasonBox.setPaddings((int) getResources().getDimension(R.dimen._10sdp), 0, 0, 0);
        }

        reasonBox.setSingleLine(false);
        reasonBox.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        reasonBox.setGravity(Gravity.TOP);
        if (generalFunc.isRTLmode()) {
            reasonBox.setPaddings(0, 0, (int) getResources().getDimension(R.dimen._10sdp), 0);
        } else {
            reasonBox.setPaddings((int) getResources().getDimension(R.dimen._10sdp), 0, 0, 0);
        }

        reasonBox.setVisibility(View.GONE);
        commentArea.setVisibility(View.GONE);
        new CreateRoundedView(Color.parseColor("#ffffff"), 5, 1, Color.parseColor("#C5C3C3"), commentArea);
        reasonBox.setBothText("", generalFunc.retrieveLangLBl("", "LBL_ENTER_REASON"));

        ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();

        JSONArray arr_msg = generalFunc.getJsonArray(Utils.message_str, responseString);
        if (arr_msg != null) {

            for (int i = 0; i < arr_msg.length(); i++) {

                JSONObject obj_tmp = generalFunc.getJsonObject(arr_msg, i);


                HashMap<String, String> datamap = new HashMap<>();
                datamap.put("title", generalFunc.getJsonValueStr("vTitle", obj_tmp));
                datamap.put("id", generalFunc.getJsonValueStr("iCancelReasonId", obj_tmp));
                list.add(datamap);
            }

            HashMap<String, String> othermap = new HashMap<>();
            othermap.put("title", generalFunc.retrieveLangLBl("", "LBL_OTHER_TXT"));
            othermap.put("id", "");
            list.add(othermap);

            MTextView cancelTxt = (MTextView) dialogView.findViewById(R.id.cancelTxt);
            MTextView submitTxt = (MTextView) dialogView.findViewById(R.id.submitTxt);
            MTextView subTitleTxt = (MTextView) dialogView.findViewById(R.id.subTitleTxt);
            ImageView cancelImg = (ImageView) dialogView.findViewById(R.id.cancelImg);
            subTitleTxt.setText(titleDailog);
            MTextView declinereasonBox = (MTextView) dialogView.findViewById(R.id.declinereasonBox);
            declinereasonBox.setText(generalFunc.retrieveLangLBl("Select Reason", "LBL_SELECT_CANCEL_REASON"));
            submitTxt.setClickable(false);
            submitTxt.setTextColor(getResources().getColor(R.color.gray_holo_light));
            submitTxt.setText(generalFunc.retrieveLangLBl("", "LBL_YES"));
            cancelTxt.setText(generalFunc.retrieveLangLBl("", "LBL_NO"));

            submitTxt.setOnClickListener(v -> {
                selectedItemId = list.get(selCurrentPosition).get("id");

                if (selCurrentPosition == -1) {
                    return;
                }

                if (!Utils.checkText(reasonBox) && selCurrentPosition == (list.size() - 1)) {
                    reasonBox.setError(generalFunc.retrieveLangLBl("", "LBL_FEILD_REQUIRD_ERROR_TXT"));
                    return;
                }

                if (listdata.get("iActive").equalsIgnoreCase("Pending")) {
                    declineBooking(list.get(selCurrentPosition).get("id"), Utils.getText(reasonBox), reasonBox.getText().toString().trim(), listdata);
                } else {
                    cancelTrip(list.get(selCurrentPosition).get("id"), Utils.getText(reasonBox), reasonBox.getText().toString().trim(), listdata);
                }

                dialog_declineOrder.dismiss();
            });
            cancelTxt.setOnClickListener(v -> {
                Utils.hideKeyboard(getContext());
                dialog_declineOrder.dismiss();
            });

            cancelImg.setOnClickListener(v -> {
                Utils.hideKeyboard(getContext());
                dialog_declineOrder.dismiss();
            });

            declinereasonBox.setOnClickListener(v -> OpenListView.getInstance(getActContext(), generalFunc.retrieveLangLBl("", "LBL_SELECT_REASON"), list, OpenListView.OpenDirection.CENTER, true, position -> {

                selCurrentPosition = position;
                HashMap<String, String> mapData = list.get(position);
                declinereasonBox.setText(mapData.get("title"));
                if (selCurrentPosition == (list.size() - 1)) {
                    reasonBox.setVisibility(View.VISIBLE);
                    commentArea.setVisibility(View.VISIBLE);
                } else {
                    reasonBox.setVisibility(View.GONE);
                    commentArea.setVisibility(View.GONE);
                }
                submitTxt.setClickable(true);
                submitTxt.setTextColor(getResources().getColor(R.color.white));

            }).show(selCurrentPosition, "title"));

            dialog_declineOrder = builder.create();
            dialog_declineOrder.setCancelable(false);
            dialog_declineOrder.getWindow().setBackgroundDrawable(getContext().getResources().getDrawable(R.drawable.all_roundcurve_card));
            dialog_declineOrder.show();

        } else {
            generalFunc.showGeneralMessage("", generalFunc.retrieveLangLBl("", "LBL_NO_DATA_AVAIL"));
        }
    }

    public void acceptBooking(String iCabBookingId, String eConfirmByProvider) {

        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "UpdateBookingStatus");
        parameters.put("UserType", Utils.app_type);
        parameters.put("iDriverId", generalFunc.getMemberId());
        parameters.put("iCabBookingId", iCabBookingId);
        parameters.put("eStatus", "Accepted");
        parameters.put("eConfirmByProvider", eConfirmByProvider);

        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(), parameters);
        exeWebServer.setLoaderConfig(getActContext(), true, generalFunc);
        exeWebServer.setDataResponseListener(responseString -> {

            JSONObject responseStringObj = generalFunc.getJsonObject(responseString);

            if (responseStringObj != null) {

                boolean isDataAvail = GeneralFunctions.checkDataAvail(Utils.action_str, responseStringObj);

                if (isDataAvail) {
                    list.clear();
                    myBookingsRecyclerAdapter.notifyDataSetChanged();

                    final GenerateAlertBox generateAlert = new GenerateAlertBox(getActContext());
                    generateAlert.setCancelable(false);
                    generateAlert.setBtnClickList(btn_id -> {
                        generateAlert.closeAlertBox();

                        getBookingsHistory(false);
                    });
                    generateAlert.setContentMessage("", generalFunc.retrieveLangLBl("", generalFunc.getJsonValueStr(Utils.message_str, responseStringObj)));
                    generateAlert.setPositiveBtn(generalFunc.retrieveLangLBl("", "LBL_BTN_OK_TXT"));

                    generateAlert.showAlertBox();
                } else {

                    String BookingFound = generalFunc.getJsonValueStr("BookingFound", responseStringObj);

                    if (BookingFound.equalsIgnoreCase("Yes")) {

                        GenerateAlertBox alertBox = new GenerateAlertBox(getActContext());
                        alertBox.setCancelable(false);
                        alertBox.setContentMessage("", generalFunc.retrieveLangLBl("", generalFunc.getJsonValueStr(Utils.message_str, responseStringObj)));
                        alertBox.setPositiveBtn(generalFunc.retrieveLangLBl("", "LBL_BTN_OK_TXT"));
                        alertBox.setNegativeBtn(generalFunc.retrieveLangLBl("", "LBL_CANCEL_TXT"));
                        alertBox.setBtnClickList(btn_id -> {
                            if (btn_id == 0) {
                                alertBox.closeAlertBox();
                            } else if (btn_id == 1) {
                                acceptBooking(iCabBookingId, "Yes");
                                alertBox.closeAlertBox();
                            }
                        });
                        alertBox.showAlertBox();
                    } else {
                        generalFunc.showGeneralMessage("",
                                generalFunc.retrieveLangLBl("", generalFunc.getJsonValueStr(Utils.message_str, responseStringObj)));
                    }
                }

            } else {
                generalFunc.showError();
            }
        });
        exeWebServer.execute();
    }

    public void declineBooking(String iCancelReasonId, String comment, String reason, HashMap<String, String> data_trip) {

        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "UpdateBookingStatus");
        parameters.put("UserType", Utils.app_type);
        parameters.put("iUserId", generalFunc.getMemberId());
        parameters.put("iCabBookingId", data_trip.get("iCabBookingId"));
        parameters.put("vCancelReason", reason);
        parameters.put("eStatus", "Declined");
        parameters.put("iCancelReasonId", iCancelReasonId);
        parameters.put("Reason", reason);
        parameters.put("Comment", comment);


        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(), parameters);
        exeWebServer.setLoaderConfig(getActContext(), true, generalFunc);
        exeWebServer.setDataResponseListener(responseString -> {
            JSONObject responseStringObj = generalFunc.getJsonObject(responseString);
            if (responseStringObj != null && !responseStringObj.equals("")) {

                boolean isDataAvail = GeneralFunctions.checkDataAvail(Utils.action_str, responseStringObj);

                if (isDataAvail) {
                    list.clear();
                    myBookingsRecyclerAdapter.notifyDataSetChanged();

                    final GenerateAlertBox generateAlert = new GenerateAlertBox(getActContext());
                    generateAlert.setCancelable(false);
                    generateAlert.setBtnClickList(btn_id -> {
                        generateAlert.closeAlertBox();
                        getBookingsHistory(false);
                    });
                    generateAlert.setContentMessage("", generalFunc.retrieveLangLBl("", generalFunc.getJsonValueStr(Utils.message_str, responseStringObj)));
                    generateAlert.setPositiveBtn(generalFunc.retrieveLangLBl("", "LBL_BTN_OK_TXT"));

                    generateAlert.showAlertBox();
                } else {
                    generalFunc.showGeneralMessage("",
                            generalFunc.retrieveLangLBl("", generalFunc.getJsonValueStr(Utils.message_str, responseStringObj)));
                }

            } else {
                generalFunc.showError();
            }
        });
        exeWebServer.execute();
    }

    public void cancelTrip(String iCancelReasonId, String comment, String reason, HashMap<String, String> data_trip) {


        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "cancelBooking");
        parameters.put("UserType", Utils.app_type);
        parameters.put("iUserId", generalFunc.getMemberId());
        parameters.put("iCabBookingId", data_trip.get("iCabBookingId"));
        parameters.put("Comment", comment);
        parameters.put("iCancelReasonId", iCancelReasonId);
        parameters.put("Reason", reason);


        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(), parameters);
        exeWebServer.setLoaderConfig(getActContext(), true, generalFunc);
        exeWebServer.setDataResponseListener(responseString -> {
            JSONObject responseStringObj = generalFunc.getJsonObject(responseString);

            if (responseStringObj != null) {

                boolean isDataAvail = GeneralFunctions.checkDataAvail(Utils.action_str, responseStringObj);

                if (isDataAvail) {
                    list.clear();
                    myBookingsRecyclerAdapter.notifyDataSetChanged();
                    getBookingsHistory(false);
                    generalFunc.showGeneralMessage("",
                            generalFunc.retrieveLangLBl("", generalFunc.getJsonValueStr(Utils.message_str, responseStringObj)));
                } else {
                    generalFunc.showGeneralMessage("",
                            generalFunc.retrieveLangLBl("", generalFunc.getJsonValueStr(Utils.message_str, responseStringObj)));
                }
            } else {
                generalFunc.showError();
            }
        });
        exeWebServer.execute();
    }

    public void cancelBooking(String iCabBookingId, String reason, boolean isUfx) {

        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "cancelBooking");
        parameters.put("UserType", Utils.app_type);
        parameters.put("iUserId", generalFunc.getMemberId());
        parameters.put("iCabBookingId", iCabBookingId);
        parameters.put("Reason", reason);
        if (!isUfx) {
            parameters.put("DataType", "PENDING");
        }

        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(), parameters);
        exeWebServer.setLoaderConfig(getActContext(), true, generalFunc);
        exeWebServer.setDataResponseListener(responseString -> {
            JSONObject responseStringObj = generalFunc.getJsonObject(responseString);
            if (responseStringObj != null && !responseStringObj.equals("")) {
                boolean isDataAvail = GeneralFunctions.checkDataAvail(Utils.action_str, responseStringObj);

                if (isDataAvail) {
                    list.clear();
                    myBookingsRecyclerAdapter.notifyDataSetChanged();
                    getBookingsHistory(false);
                    generalFunc.showGeneralMessage("",
                            generalFunc.retrieveLangLBl("", generalFunc.getJsonValueStr(Utils.message_str, responseStringObj)));
                } else {
                    generalFunc.showGeneralMessage("",
                            generalFunc.retrieveLangLBl("", generalFunc.getJsonValueStr(Utils.message_str, responseStringObj)));
                }

            } else {
                generalFunc.showError();
            }
        });
        exeWebServer.execute();
    }

    public void buildMsgOnStartTripBtn(final String iCabBookingId, String type, String contentMsg) {
        final GenerateAlertBox generateAlert = new GenerateAlertBox(getActContext());
        generateAlert.setCancelable(false);
        generateAlert.setBtnClickList(btn_id -> {
            if (btn_id == 0) {
                generateAlert.closeAlertBox();
            } else {
                if (type.equalsIgnoreCase("Pending")) {
                    acceptBooking(iCabBookingId, "No");
                } else {
                    startTrip(iCabBookingId);
                }
            }
        });

        if (type.equalsIgnoreCase("Pending")) {
            generateAlert.setContentMessage("", generalFunc.retrieveLangLBl("Are you sure? You want to accept this job.", "LBL_CONFIRM_ACCEPT_JOB"));
        } else {
            generateAlert.setContentMessage("", contentMsg);
        }

        generateAlert.setPositiveBtn(generalFunc.retrieveLangLBl("", "LBL_BTN_YES_TXT"));
        generateAlert.setNegativeBtn(generalFunc.retrieveLangLBl("", "LBL_BTN_NO_TXT"));
        generateAlert.showAlertBox();
    }

    public void startTrip(String iCabBookingId) {

        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "GenerateTrip");
        parameters.put("UserType", Utils.app_type);
        parameters.put("DriverID", generalFunc.getMemberId());
        parameters.put("iCabBookingId", iCabBookingId);
        parameters.put("GoogleServerKey", generalFunc.retrieveValue(Utils.GOOGLE_SERVER_ANDROID_DRIVER_APP_KEY));


        if (myBookingFragment != null && myBookingFragment.userLocation != null) {
            parameters.put("vLatitude", "" + myBookingFragment.userLocation.getLatitude());
            parameters.put("vLongitude", "" + myBookingFragment.userLocation.getLongitude());
        } else if (bookingAct != null && bookingAct.userLocation != null) {
            parameters.put("vLatitude", "" + bookingAct.userLocation.getLatitude());
            parameters.put("vLongitude", "" + bookingAct.userLocation.getLongitude());
        } else if (GetLocationUpdates.getInstance() != null && GetLocationUpdates.getInstance().getLastLocation() != null) {
            Location lastLocation = GetLocationUpdates.getInstance().getLastLocation();
            parameters.put("vLatitude", "" + lastLocation.getLatitude());
            parameters.put("vLongitude", "" + lastLocation.getLongitude());
        }

        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(), parameters);
        exeWebServer.setLoaderConfig(getActContext(), true, generalFunc);
        exeWebServer.setDataResponseListener(responseString -> {
            JSONObject responseStringObj = generalFunc.getJsonObject(responseString);

            if (responseStringObj != null) {
                boolean isDataAvail = GeneralFunctions.checkDataAvail(Utils.action_str, responseStringObj);

                if (isDataAvail) {

                    if (myBookingFragment != null) {
                        myBookingFragment.stopLocUpdates();
                    } else if (bookingAct != null && bookingAct.userLocation != null) {
                        bookingAct.stopLocUpdates();
                    }

                    MyApp.getInstance().restartWithGetDataApp();
                } else {
                    String message = generalFunc.getJsonValueStr(Utils.message_str, responseStringObj);

                    if (message.equalsIgnoreCase("DO_RESTART")) {
                        MyApp.getInstance().restartWithGetDataApp();
                        return;
                    }

                    if (generalFunc.getJsonValueStr("DO_RELOAD", responseStringObj).equalsIgnoreCase("YES")) {
                        generalFunc.showGeneralMessage("", generalFunc.retrieveLangLBl("", message), generalFunc.retrieveLangLBl("Ok", "LBL_BTN_OK_TXT"), "", buttonId -> {
                            list.clear();
                            myBookingsRecyclerAdapter.notifyDataSetChanged();
                            getBookingsHistory(false);
                        });
                        return;
                    }

                    generalFunc.showGeneralMessage("", generalFunc.retrieveLangLBl("", message));
                }

            } else {
                generalFunc.showError();
            }
        });
        exeWebServer.execute();
    }


    public void getBookingsHistory(final boolean isLoadMore) {
        if (errorView != null) {
            if (errorView.getVisibility() == View.VISIBLE) {
                errorView.setVisibility(View.GONE);
            }
        }

        if (loading_my_bookings != null) {
            if (loading_my_bookings.getVisibility() != View.VISIBLE && !isLoadMore) {
                loading_my_bookings.setVisibility(View.VISIBLE);
            }
            // mainlayout.setVisibility(View.GONE);
        }

        if (!isLoadMore) {
            removeNextPageConfig();
        }

        final HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "getMemberBookings");
        parameters.put("memberId", generalFunc.getMemberId());
        parameters.put("UserType", Utils.app_type);
        if (myBookingFragment != null) {
            parameters.put("vFilterParam", myBookingFragment.selFilterType);
            String eFilterSel = myBookingFragment.selSubFilterType;
            parameters.put("vSubFilterParam", eFilterSel);
            parameters.put("dDateOrig", eFilterSel.equalsIgnoreCase("past") ? SELECTED_DATE : "");
        } else {
            parameters.put("vFilterParam", bookingAct.selFilterType);
            String eFilterSel = bookingAct.selSubFilterType;
            parameters.put("vSubFilterParam", bookingAct.selSubFilterType);
            parameters.put("dDateOrig", eFilterSel.equalsIgnoreCase("past") ? SELECTED_DATE : "");
        }

        if (isLoadMore) {
            parameters.put("page", next_page_str);
        } else {
            earningamtmap.put("header", "true");
            earningamtmap.put("TripCount", "--");
            earningamtmap.put("TotalEarning", "--");
            earningamtmap.put("AvgRating", "--");
            list.add(earningamtmap);
            list.clear();
            myBookingsRecyclerView.setVisibility(View.GONE);
        }
        parameters.put("tripdataPage", tripdataPage);

        noRidesTxt.setVisibility(View.GONE);


        final ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(), parameters);
        exeWebServer.setDataResponseListener(responseString -> {

            swipeRefreshLayout.setRefreshing(false);

            noRidesTxt.setVisibility(View.GONE);
            myBookingsRecyclerView.setVisibility(View.VISIBLE);

            JSONObject responseObj = generalFunc.getJsonObject(responseString);
            tripdataPage = generalFunc.getJsonValueStr("tripdataPage", responseObj);

            if (responseObj != null && !responseObj.equals("")) {
                if (list.size() == 0) {
                    earningamtmap.put("header", "true");
                    earningamtmap.put("TripCount", "--");
                    earningamtmap.put("TotalEarning", "--");
                    earningamtmap.put("AvgRating", "--");
                    list.add(earningamtmap);
                }

                closeLoader();

                if (GeneralFunctions.checkDataAvail(Utils.action_str, responseObj)) {

                    String nextPage = generalFunc.getJsonValueStr("NextPage", responseObj);
                    String currencySymbol = generalFunc.getJsonValueStr("CurrencySymbol", responseObj);

                    JSONArray arr_rides = generalFunc.getJsonArray(Utils.message_str, responseObj);

                    if (arr_rides != null && arr_rides.length() > 0) {
                        String LBL_JOB_LOCATION_TXT = "", LBL_RENTAL_CATEGORY_TXT = "";
                        String LBL_DELIVERY_NO = "", LBL_CANCEL_BOOKING = "", LBL_BOOKING = "", LBL_PICK_UP_LOCATION = "", LBL_DEST_LOCATION = "", LBL_SENDER_LOCATION = "", LBL_RECEIVER_LOCATION = "";
                        String LBL_Status = "", LBL_VIEW_REASON = "", LBL_REBOOKING = "", LBL_RESCHEDULE = "", LBL_VIEW_REQUESTED_SERVICES = "";
                        String LBL_MULTI_LIVE_TRACK_TEXT = "", LBL_VIEW_DETAILS = "";
                        String LBL_ACCEPT_JOB = "", LBL_START_TRIP = "", LBL_DECLINE_JOB = "", LBL_CANCEL_TRIP = "";
                        if (arr_rides.length() > 0) {
                            LBL_Status = generalFunc.retrieveLangLBl("", "LBL_Status");
                            LBL_RENTAL_CATEGORY_TXT = generalFunc.retrieveLangLBl("", "LBL_RENTAL_CATEGORY_TXT");
                            LBL_DELIVERY_NO = generalFunc.retrieveLangLBl("Delivery No", "LBL_DELIVERY_NO");
                            LBL_CANCEL_BOOKING = generalFunc.retrieveLangLBl("", "LBL_CANCEL_BOOKING");
                            LBL_BOOKING = generalFunc.retrieveLangLBl("", "LBL_BOOKING");

                            LBL_PICK_UP_LOCATION = generalFunc.retrieveLangLBl("", "LBL_PICK_UP_LOCATION");
                            LBL_DEST_LOCATION = generalFunc.retrieveLangLBl("", "LBL_DEST_LOCATION");
                            LBL_JOB_LOCATION_TXT = generalFunc.retrieveLangLBl("", "LBL_JOB_LOCATION_TXT");
                            LBL_SENDER_LOCATION = generalFunc.retrieveLangLBl("", "LBL_SENDER_LOCATION");
                            LBL_RECEIVER_LOCATION = generalFunc.retrieveLangLBl("", "LBL_RECEIVER_LOCATION");

                            LBL_MULTI_LIVE_TRACK_TEXT = generalFunc.retrieveLangLBl("", "LBL_MULTI_LIVE_TRACK_TEXT");
                            LBL_VIEW_DETAILS = generalFunc.retrieveLangLBl("", "LBL_VIEW_DETAILS");
                            LBL_VIEW_REASON = generalFunc.retrieveLangLBl("", "LBL_VIEW_REASON");
                            LBL_RESCHEDULE = generalFunc.retrieveLangLBl("", "LBL_RESCHEDULE");
                            LBL_REBOOKING = generalFunc.retrieveLangLBl("", "LBL_REBOOKING");
                            LBL_VIEW_REQUESTED_SERVICES = generalFunc.retrieveLangLBl("", "LBL_VIEW_REQUESTED_SERVICES");

                            LBL_ACCEPT_JOB = generalFunc.retrieveLangLBl("", "LBL_ACCEPT_JOB");
                            LBL_START_TRIP = generalFunc.retrieveLangLBl("", "LBL_START_TRIP");
                            LBL_DECLINE_JOB = generalFunc.retrieveLangLBl("", "LBL_DECLINE_JOB");
                            LBL_CANCEL_TRIP = generalFunc.retrieveLangLBl("", "LBL_CANCEL_TRIP");


                        }

                        for (int i = 0; i < arr_rides.length(); i++) {
                            JSONObject obj_temp = generalFunc.getJsonObject(arr_rides, i);
                            HashMap<String, String> map = new HashMap<String, String>();

                            map.put("vBookingType", generalFunc.getJsonValueStr("vBookingType", obj_temp));
                            map.put("vPhone", generalFunc.getJsonValueStr("vPhone", obj_temp));
                            map.put("vImage", generalFunc.getJsonValueStr("vImage", obj_temp));
                            map.put("vAvgRating", "" + GeneralFunctions.parseFloatValue(0, generalFunc.getJsonValueStr("vAvgRating", obj_temp)));
                            map.put("vTimeZone", generalFunc.getJsonValueStr("vTimeZone", obj_temp));
                            map.put("tSaddress", generalFunc.getJsonValueStr("tSaddress", obj_temp));
                            map.put("tDaddress", generalFunc.getJsonValueStr("tDaddress", obj_temp));
                            map.put("vRideNo", generalFunc.convertNumberWithRTL(generalFunc.getJsonValueStr("vRideNo", obj_temp)));
                            map.put("vName", generalFunc.getJsonValueStr("vName", obj_temp));
                            map.put("iTripId", generalFunc.getJsonValueStr("iTripId", obj_temp));
                            map.put("iCabBookingId", generalFunc.getJsonValueStr("iCabBookingId", obj_temp));
                            map.put("vServiceTitle", generalFunc.getJsonValueStr("vServiceTitle", obj_temp));
                            map.put("vVehicleType", generalFunc.getJsonValueStr("vVehicleType", obj_temp));
                            map.put("driverStatus", generalFunc.getJsonValueStr("driverStatus", obj_temp));
                            map.put("eShowHistory", generalFunc.getJsonValueStr("eShowHistory", obj_temp));
                            map.put("eHailTrip", generalFunc.getJsonValueStr("eHailTrip", obj_temp));

                            String eType = generalFunc.getJsonValueStr("eType", obj_temp);
                            String iActive = generalFunc.getJsonValueStr("iActive", obj_temp);
                            String dBooking_dateOrig = generalFunc.getJsonValueStr("dBooking_dateOrig", obj_temp);
                            String tTripRequestDate = generalFunc.getJsonValueStr("tTripRequestDate", obj_temp);

                            map.put("appType", APP_TYPE);
                            map.put("currenteType", eType);
                            map.put("eType", eType);
                            map.put("eTypeVal", eType);
                            map.put("iActive", iActive);

                            map.put("vService_BG_color", generalFunc.getJsonValueStr("vService_BG_color", obj_temp));
                            map.put("vService_TEXT_color", generalFunc.getJsonValueStr("vService_TEXT_color", obj_temp));

                            map.put("iRentalPackageId", generalFunc.getJsonValueStr("iRentalPackageId", obj_temp));
                            map.put("iVehicleTypeId", generalFunc.getJsonValueStr("iVehicleTypeId", obj_temp));
                            map.put("vLatitude", generalFunc.getJsonValueStr("vLatitude", obj_temp));
                            map.put("vLongitude", generalFunc.getJsonValueStr("vLongitude", obj_temp));
                            map.put("vPhone", generalFunc.getJsonValueStr("vPhone", obj_temp));
                            map.put("vCode", generalFunc.getJsonValueStr("vCode", obj_temp));
                            map.put("vPackageName", generalFunc.getJsonValueStr("vPackageName", obj_temp));
                            map.put("moreServices", generalFunc.getJsonValueStr("moreServices", obj_temp));
                            map.put("is_rating", generalFunc.getJsonValueStr("is_rating", obj_temp));
                            map.put("eFavDriver", generalFunc.getJsonValueStr("eFavDriver", obj_temp));
                            map.put("currencySymbol", currencySymbol);
                            map.put("iFare", generalFunc.convertNumberWithRTL(generalFunc.getJsonValueStr("iFare", obj_temp)));

                            if (eType.equalsIgnoreCase(Utils.eType_Multi_Delivery)) {
                                map.put("tDaddress", "");
                            }

                            map.put("showViewRequestedServicesBtn", generalFunc.getJsonValueStr("showViewRequestedServicesBtn", obj_temp));
                            map.put("showCancelBookingBtn", generalFunc.getJsonValueStr("showCancelBookingBtn", obj_temp));
                            map.put("showReScheduleBtn", generalFunc.getJsonValueStr("showReScheduleBtn", obj_temp));
                            map.put("showReBookingBtn", generalFunc.getJsonValueStr("showReBookingBtn", obj_temp));
                            map.put("showViewCancelReasonBtn", generalFunc.getJsonValueStr("showViewCancelReasonBtn", obj_temp));
                            map.put("showViewDetailBtn", generalFunc.getJsonValueStr("showViewDetailBtn", obj_temp));
                            map.put("showLiveTrackBtn", generalFunc.getJsonValueStr("showLiveTrackBtn", obj_temp));
                            map.put("showAcceptBtn", generalFunc.getJsonValueStr("showAcceptBtn", obj_temp));
                            map.put("showDeclineBtn", generalFunc.getJsonValueStr("showDeclineBtn", obj_temp));
                            map.put("showStartBtn", generalFunc.getJsonValueStr("showStartBtn", obj_temp));
                            map.put("showCancelBtn", generalFunc.getJsonValueStr("showCancelBtn", obj_temp));

                            try {
                                map.put("ConvertedTripRequestDate", generalFunc.convertNumberWithRTL(generalFunc.getDateFormatedType(dBooking_dateOrig, Utils.OriginalDateFormate, CommonUtilities.OriginalDateFormate)));
                                map.put("ConvertedTripRequestTime", generalFunc.convertNumberWithRTL(generalFunc.getDateFormatedType(dBooking_dateOrig, Utils.OriginalDateFormate, CommonUtilities.OriginalTimeFormate)));
                            } catch (Exception e) {
                                e.printStackTrace();
                                map.put("ConvertedTripRequestDate", "");
                                map.put("ConvertedTripRequestTime", "");
                            }

                            if (eType.equalsIgnoreCase("deliver") || eType.equalsIgnoreCase(Utils.eType_Multi_Delivery)) {
                                map.put("LBL_PICK_UP_LOCATION", LBL_SENDER_LOCATION);
                                map.put("LBL_DEST_LOCATION", LBL_RECEIVER_LOCATION);
                            } else {
                                map.put("LBL_PICK_UP_LOCATION", LBL_PICK_UP_LOCATION);
                                map.put("LBL_DEST_LOCATION", LBL_DEST_LOCATION);
                            }

                            map.put("LBL_BOOKING_NO", LBL_BOOKING);
                            map.put("LBL_Status", LBL_Status);

                            if (isDeliver(eType) || eType.equalsIgnoreCase(Utils.eType_Multi_Delivery)) {
                                map.put("LBL_BOOKING_NO", LBL_DELIVERY_NO);
                            } else {
                                map.put("LBL_BOOKING_NO", LBL_BOOKING);
                            }
                            map.put("LBL_JOB_LOCATION_TXT", LBL_JOB_LOCATION_TXT);
                            map.put("LBL_CANCEL_BOOKING", LBL_CANCEL_BOOKING);
                            map.put("LBL_RENTAL_CATEGORY_TXT", LBL_RENTAL_CATEGORY_TXT);
                            map.put("liveTrackLBL", LBL_MULTI_LIVE_TRACK_TEXT);
                            map.put("viewDetailLBL", LBL_VIEW_DETAILS);
                            map.put("LBL_VIEW_REASON", LBL_VIEW_REASON);
                            map.put("LBL_RESCHEDULE", LBL_RESCHEDULE);
                            map.put("LBL_REBOOKING", LBL_REBOOKING);
                            map.put("LBL_VIEW_REQUESTED_SERVICES", LBL_VIEW_REQUESTED_SERVICES);
                            map.put("LBL_ACCEPT_JOB", LBL_ACCEPT_JOB);
                            map.put("LBL_START_TRIP", LBL_START_TRIP);
                            map.put("LBL_DECLINE_JOB", LBL_DECLINE_JOB);
                            map.put("LBL_CANCEL_TRIP", LBL_CANCEL_TRIP);

                            map.put("JSON", obj_temp.toString());
                            map.put("APP_TYPE", APP_TYPE);
                            list.add(map);
                        }
                    }

                    list.get(0).put("TripCount", generalFunc.convertNumberWithRTL(generalFunc.getJsonValueStr("TripCount", responseObj)));
                    list.get(0).put("TotalEarning", currencySymbol + generalFunc.convertNumberWithRTL(generalFunc.getJsonValueStr("TotalEarning", responseObj)));
                    list.get(0).put("AvgRating", "" + GeneralFunctions.parseFloatValue(0, generalFunc.getJsonValueStr("AvgRating", responseObj)));

                    buildFilterTypes(responseObj);

                    if (!nextPage.equals("") && !nextPage.equals("0")) {
                        next_page_str = nextPage;
                        isNextPageAvailable = true;
                    } else {
                        removeNextPageConfig();
                    }

                } else {
                    buildFilterTypes(responseObj);

                    if (list.size() == 1) {
                        removeNextPageConfig();
                        noRidesTxt.setText(generalFunc.retrieveLangLBl("", generalFunc.getJsonValueStr(Utils.message_str, responseObj)));
                        noRidesTxt.setVisibility(View.VISIBLE);

                        list.get(0).put("TripCount", "--");
                        list.get(0).put("TotalEarning", "--");
                        list.get(0).put("AvgRating", "--");
                    }
                }
            } else {
                if (!isLoadMore) {
                    buildFilterTypes(responseObj);
                    removeNextPageConfig();
                    generateErrorView();

                    list.get(0).put("TripCount", "--");
                    list.get(0).put("TotalEarning", "--");
                    list.get(0).put("AvgRating", "--");
                    noRidesTxt.setText(generalFunc.retrieveLangLBl("", generalFunc.getJsonValueStr(Utils.message_str, responseObj)));
                    noRidesTxt.setVisibility(View.VISIBLE);
                }

            }
            myBookingsRecyclerAdapter.notifyDataSetChanged();
            mIsLoading = false;
            // mainlayout.setVisibility(View.VISIBLE);
        });
        exeWebServer.execute();
    }

    private void buildFilterTypes(JSONObject responseObj) {
        if (responseObj == null) return;
        String eFilterSel = generalFunc.getJsonValueStr("eFilterSel", responseObj);

        JSONArray arr_type_filter = generalFunc.getJsonArray("AppTypeFilterArr", responseObj);

        filterlist = new ArrayList<>();
        if (arr_type_filter != null && arr_type_filter.length() > 0) {
            for (int i = 0; i < arr_type_filter.length(); i++) {
                JSONObject obj_temp = generalFunc.getJsonObject(arr_type_filter, i);
                HashMap<String, String> map = new HashMap<String, String>();
                String vTitle = generalFunc.getJsonValueStr("vTitle", obj_temp);
                map.put("vTitle", vTitle);
                String vFilterParam = generalFunc.getJsonValueStr("vFilterParam", obj_temp);
                map.put("vFilterParam", vFilterParam);

                filterlist.add(map);
            }
        }

        if (myBookingFragment != null) {
            myBookingFragment.filterManage(filterlist);
        } else {
            bookingAct.filterManage(filterlist);
        }

        JSONArray subFilterOptionArr = generalFunc.getJsonArray("subFilterOption", responseObj);

        subFilterlist = new ArrayList<>();
        if (subFilterOptionArr != null && subFilterOptionArr.length() > 0) {
            for (int i = 0; i < subFilterOptionArr.length(); i++) {
                JSONObject obj_temp = generalFunc.getJsonObject(subFilterOptionArr, i);
                HashMap<String, String> map = new HashMap<String, String>();
                String vTitle = generalFunc.getJsonValueStr("vTitle", obj_temp);
                map.put("vTitle", vTitle);
                String vSubFilterParam = generalFunc.getJsonValueStr("vSubFilterParam", obj_temp);
                map.put("vSubFilterParam", vSubFilterParam);

                if (vSubFilterParam.equalsIgnoreCase(eFilterSel)) {
                    filterTxt.setText(vTitle);
                    if (myBookingFragment != null) {
                        myBookingFragment.selSubFilterType = eFilterSel;
                        myBookingFragment.subFilterPosition = i;
                    } else {
                        bookingAct.selSubFilterType = eFilterSel;
                        bookingAct.subFilterPosition = i;
                    }
                }

                if (eFilterSel.equalsIgnoreCase("past")) {
                    calContainerView.setVisibility(View.VISIBLE);
                    calenderHeaderLayout.setVisibility(View.VISIBLE);
                    list.get(0).put("isPast", "yes");
                } else {
                    calContainerView.setVisibility(View.GONE);
                    calenderHeaderLayout.setVisibility(View.GONE);
                    list.get(0).put("isPast", "no");
                }

                subFilterlist.add(map);
            }

        }
        if (myBookingFragment != null) {
            myBookingFragment.subFilterManage(subFilterlist, "History");
        } else {
            bookingAct.subFilterManage(subFilterlist, "History");
        }
    }

    @Override
    public void onCalendarTitleViewClick() {
        if (calendar_view.findViewById(R.id.weekLayout).getVisibility() == View.VISIBLE) {
            showHideCalender(false);
        } else {
            showHideCalender(true);
        }
    }

    @Override
    public void onCalendarPreviousButtonClick() {

    }

    @Override
    public void onCalendarNextButtonClick() {

    }

    @Override
    public void onCalendarDateSelected(Date date) {
        list.clear();
        setDate(date);
    }

    @Override
    public void onCalendarMonthChanged(Date date) {
        list.clear();
        setDate(date);
    }

    @Override
    public void onCalendarCurrentDayFound(Date date) {
        list.clear();
        setDate(date);
    }


    public class setOnClickList implements View.OnClickListener {
        boolean isTripItemClick = false;
        int tripItemPosition = 0;
        boolean isTripItemCancle = false;

        public setOnClickList() {
        }

        public setOnClickList(boolean isTripItemClick, int tripItemPosition, boolean isTripItemCancle) {
            this.isTripItemClick = isTripItemClick;
            this.tripItemPosition = tripItemPosition;
            this.isTripItemCancle = isTripItemCancle;
        }

        @Override
        public void onClick(View view) {
            Utils.hideKeyboard(getActContext());

            if (isTripItemClick) {
                Bundle bn = new Bundle();
                bn.putString("iTripId", list.get(tripItemPosition).get("iTripId"));
                new StartActProcess(getActivity()).startActForResult(RideHistoryDetailActivity.class, bn, HISTORYDETAILS);
            } else {
                int i = view.getId();
                if (i == R.id.backImgView) {
                    getActivity().onBackPressed();
                } else if (i == R.id.filterArea) {
                    if (myBookingFragment != null) {
                        myBookingFragment.BuildType("History");
                    } else {
                        bookingAct.BuildType("History");
                    }
                }
            }

        }
    }

    public void removeNextPageConfig() {
        next_page_str = "";
        isNextPageAvailable = false;
        mIsLoading = false;
        myBookingsRecyclerAdapter.removeFooterView();
    }

    public void closeLoader() {
        if (loading_my_bookings.getVisibility() == View.VISIBLE) {
            loading_my_bookings.setVisibility(View.GONE);
        }
    }

    public void generateErrorView() {

        closeLoader();

        generalFunc.generateErrorView(errorView, "LBL_ERROR_TXT", "LBL_NO_INTERNET_TXT");

        if (errorView.getVisibility() != View.VISIBLE) {
            errorView.setVisibility(View.VISIBLE);
        }
        errorView.setOnRetryListener(() -> getBookingsHistory(false));
    }

    public Context getActContext() {
        if (myBookingFragment != null) {
            return myBookingFragment.getActContext();
        } else {
            return bookingAct.getActContext();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Utils.hideKeyboard(getActivity());
    }

    @Override
    public void onRefresh() {
        swipeRefreshLayout.setRefreshing(true);
        getBookingsHistory(false);
    }
}
