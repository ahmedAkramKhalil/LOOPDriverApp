package com.taxifgo.driver;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ImageView;

import com.adapter.files.DaySlotAdapter;
import com.adapter.files.TimeSlotAdapter;
import com.general.files.ExecuteWebServerUrl;
import com.general.files.GeneralFunctions;
import com.general.files.MyApp;
import com.utils.Logger;
import com.utils.Utils;
import com.view.GenerateAlertBox;
import com.view.MButton;
import com.view.MTextView;
import com.view.MaterialRippleLayout;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

public class SetAvailabilityActivity extends AppCompatActivity implements DaySlotAdapter.setRecentDateSlotClickList,TimeSlotAdapter.setRecentTimeSlotClickList {

    GeneralFunctions generalFunc;
    ImageView backImgView;
    MTextView titleTxt;
    public    RecyclerView dayslotRecyclerView;
    public      RecyclerView timeslotRecyclerView;
    ArrayList daylist;
    ArrayList passApidaylist;
    ArrayList passApidaylist1;

    MButton btn_type2;
    int submitBtnId;
    MTextView serviceAddrHederTxtView;
    MTextView serviceAddrHederTxtView2;

    String selectday = "";
    String selectday_language = "";
  DaySlotAdapter adapter;
  TimeSlotAdapter timeadapter;
    ArrayList<String> selectedlist;

    View loadingBar;
    View contentArea;

    ArrayList<HashMap<String, String>> timeSlotList;
    ArrayList<HashMap<String, String>> selTimeSlotList;
    ArrayList<HashMap<String, String>> checkTimeSlotList;

int screenWidth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_availability);
        selectedlist = new ArrayList<>();


        generalFunc = MyApp.getInstance().getGeneralFun(getActContext());
        setDayData();

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        backImgView = (ImageView) findViewById(R.id.backImgView);
        titleTxt = (MTextView) findViewById(R.id.titleTxt);

        contentArea = findViewById(R.id.contentArea);
        loadingBar = findViewById(R.id.loadingBar);

        backImgView.setOnClickListener(new setOnClick());

        serviceAddrHederTxtView = (MTextView) findViewById(R.id.serviceAddrHederTxtView);
        serviceAddrHederTxtView2 = (MTextView) findViewById(R.id.serviceAddrHederTxtView2);

        screenWidth = displayMetrics.widthPixels;
        dayslotRecyclerView = (RecyclerView) findViewById(R.id.dayslotRecyclerView);
        adapter = new DaySlotAdapter(getActContext(), passApidaylist, selectedlist, daylist,selectday,dayslotRecyclerView,screenWidth);
        dayslotRecyclerView.setAdapter(adapter);
        dayslotRecyclerView.setClipToPadding(false);
        adapter.setOnClickList(this);


        timeSlotList = new ArrayList<HashMap<String, String>>();
        selTimeSlotList = new ArrayList<HashMap<String, String>>();
        checkTimeSlotList = new ArrayList<HashMap<String, String>>();
        settimeSlotData();
        timeslotRecyclerView = (RecyclerView) findViewById(R.id.timeslotRecyclerView);
        timeslotRecyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        timeadapter = new TimeSlotAdapter(getActContext(), timeSlotList, selTimeSlotList, checkTimeSlotList);
        timeslotRecyclerView.setAdapter(timeadapter);
      //  timeadapter.setOnClickList(this);



        btn_type2 = ((MaterialRippleLayout) findViewById(R.id.btn_type2)).getChildView();


        submitBtnId = Utils.generateViewId();
        btn_type2.setId(submitBtnId);

        btn_type2.setOnClickListener(new setOnClick());
        setLabel();
        getselDayApi();
        getTimeSlotDetails();
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    public void setLabel() {
        titleTxt.setText(generalFunc.retrieveLangLBl("Set Availability", "LBL_MY_AVAILABILITY"));
        serviceAddrHederTxtView.setText(generalFunc.retrieveLangLBl("What day?", "LBL_WHAT_DAY"));
        serviceAddrHederTxtView2.setText(generalFunc.retrieveLangLBl("Available times slots?.", "LBL_WHAT_TIME"));

        btn_type2.setText(generalFunc.retrieveLangLBl("", "LBL_CONTINUE_BTN"));

    }


    public void setDayData() {
        daylist = new ArrayList<>();
        passApidaylist = new ArrayList();
        passApidaylist1 = new ArrayList();
        Locale locale = new Locale(generalFunc.retrieveValue(Utils.LANGUAGE_CODE_KEY));


        String[] namesOfDays;
        String[] namesOfDays1;

        if (generalFunc.retrieveValue(Utils.LANGUAGE_CODE_KEY).equalsIgnoreCase("th")) {
            namesOfDays = DateFormatSymbols.getInstance(Locale.ENGLISH).getShortWeekdays();
            namesOfDays1 = DateFormatSymbols.getInstance(locale).getShortWeekdays();
        } else {
            namesOfDays = DateFormatSymbols.getInstance(Locale.ENGLISH).getWeekdays();
            namesOfDays1 = DateFormatSymbols.getInstance(locale).getWeekdays();
        }
        for (int i = 0; i < namesOfDays.length; i++) {
            if (i != 0) {
                passApidaylist.add(namesOfDays[i]);
                passApidaylist1.add(namesOfDays1[i]);
            }
        }

        String[] passnamesOfDays;
        if (generalFunc.retrieveValue(Utils.LANGUAGE_CODE_KEY).equalsIgnoreCase("th")) {
            passnamesOfDays = DateFormatSymbols.getInstance(locale).getShortWeekdays();
        } else {
            passnamesOfDays = DateFormatSymbols.getInstance(locale).getWeekdays();
        }
        for (int i = 0; i < passnamesOfDays.length; i++) {
            if (i != 0) {
                daylist.add(passnamesOfDays[i]);
            }
        }


    }



    public void getTimeSlotDetails() {

        loadingBar.setVisibility(View.VISIBLE);
        contentArea.setVisibility(View.GONE);

        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "DisplayAvailability");
        parameters.put("iDriverId", generalFunc.getMemberId());
        parameters.put("vDay", selectday);

        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(), parameters);
        exeWebServer.setLoaderConfig(getActContext(), false, generalFunc);
        exeWebServer.setDataResponseListener(responseString -> {
            JSONObject responseStringObject=generalFunc.getJsonObject(responseString);

            if (responseStringObject != null && !responseStringObject.equals("")) {

                boolean isDataAvail = GeneralFunctions.checkDataAvail(Utils.action_str, responseStringObject);

                selectday = generalFunc.getJsonValueStr("vDay",responseStringObject);
               // selectday = "Friday";


                if (isDataAvail) {

                    String messageJson = generalFunc.getJsonValueStr(Utils.message_str, responseStringObject);

                    String[] vAvailableTimes = generalFunc.getJsonValue("vAvailableTimes", messageJson).split(",");
                    for (int i = 0; i < vAvailableTimes.length; i++) {

                        HashMap<String, String> map = new HashMap<String, String>();
                        map.put("selname", vAvailableTimes[i]);
                        map.put("status", "yes");
                        timeadapter.selTimeSlotList.add(map);
                    }
                    timeadapter.notifyDataSetChanged();
                }
                if (!selectday.isEmpty()){
                    adapter.selectday=selectday;
                    adapter.notifyDataSetChanged();
                    ((LinearLayoutManager) dayslotRecyclerView.getLayoutManager()).scrollToPositionWithOffset(daylist.indexOf(selectday), (screenWidth/2- Utils.dipToPixels(getActContext(),65)));
                }

                loadingBar.setVisibility(View.GONE);
                contentArea.setVisibility(View.VISIBLE);

            } else {
                generalFunc.showError(true);
            }
            loadingBar.setVisibility(View.GONE);
        });
        exeWebServer.execute();
    }



    public void getselDayApi() {
        // http://192.168.1.131/cubetaxidev/webservice_test_ufx.php?type=DisplayDriverDaysAvailability&iDriverId=31

        loadingBar.setVisibility(View.VISIBLE);
        contentArea.setVisibility(View.GONE);

        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "DisplayDriverDaysAvailability");
        parameters.put("iDriverId", generalFunc.getMemberId());

        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(), parameters);
        exeWebServer.setLoaderConfig(getActContext(), false, generalFunc);
        exeWebServer.setDataResponseListener(responseString -> {

            JSONObject responseStringObject = generalFunc.getJsonObject(responseString);

            if (responseStringObject != null && !responseStringObject.equals("")) {

                boolean isDataAvail = GeneralFunctions.checkDataAvail(Utils.action_str, responseStringObject);

                if (isDataAvail) {

                    selectedlist.clear();
                    JSONArray obj_arr = generalFunc.getJsonArray(Utils.message_str, responseStringObject);
                    if (obj_arr == null || obj_arr.length() == 0) {
                        return;
                    }

                    for (int i = 0; i < obj_arr.length(); i++) {
                        JSONObject obj_temp = generalFunc.getJsonObject(obj_arr, i);
                        selectedlist.add(generalFunc.getJsonValueStr("vDay", obj_temp));

                    }

                }

                loadingBar.setVisibility(View.GONE);
                contentArea.setVisibility(View.VISIBLE);

            } else {
                generalFunc.showError(true);
            }
        });
        exeWebServer.execute();
    }

    public Context getActContext() {
        return SetAvailabilityActivity.this;
    }

    @Override
    public void itemDateSlotLocClick(int position) {

        selectday = passApidaylist.get(position).toString();
        selectday_language = passApidaylist1.get(position).toString();
       // String dispselectday = daylist.get(position).toString();
       // Bundle bundle = new Bundle();
      //  bundle.putString("selectday", selectday);
        //bundle.putString("selectday_language", selectday_language);
      //  bundle.putString("dispselectday", dispselectday);
      //  bundle.putString("dispselectday", dispselectday);s

        adapter.selectday=selectday;
        adapter.notifyDataSetChanged();
        timeadapter.selTimeSlotList.clear();
        timeadapter. makeAllViewfalse();
        timeadapter.notifyDataSetChanged();
        getTimeSlotDetails();
        ((LinearLayoutManager) dayslotRecyclerView.getLayoutManager()).scrollToPositionWithOffset(position, (screenWidth/2- Utils.dipToPixels(getActContext(),65)));
       // new StartActProcess(getActContext()).startActWithData(setTimeScheduleActivity.class, bundle);
       // adapter.isSelectedPos = -1;
        //adapter.notifyDataSetChanged();

    }

    @Override
    public void itemTimeSlotLocClick(ArrayList<HashMap<String, String>> timeSlotList) {
        this.timeSlotList = timeSlotList;
    }


    public class setOnClick implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            int i = view.getId();
            if (i == R.id.backImgView) {
                SetAvailabilityActivity.super.onBackPressed();
            } else if (i == submitBtnId) {
                if (timeSlotList.size()==0) {
                    generalFunc.showGeneralMessage("", generalFunc.retrieveLangLBl("Please select Slot", "LBL_SELECT_TIME_SLOT"));
                    return;
                }else {
                    addTimeSlotApi();
                }
            }
        }
    }

    public void settimeSlotData() {

        for (int i = 0; i <= 23; i++) {
            HashMap<String, String> map = new HashMap<>();
            HashMap<String, String> checkmap = new HashMap<>();

            map.put("status", "no");
            checkmap.put("status", "no");

            int fromtime = i;
            int toTime = i + 1;


            String fromtimedisp = "";
            String Totimedisp = "";
            String selfromtime = "";
            String seltoTime = "";

            if (fromtime == 0) {
                fromtime = 12;
            }

            if (fromtime < 10) {
                selfromtime = "0" + fromtime;
            } else {
                selfromtime = fromtime + "";
            }

            if (toTime < 10) {
                seltoTime = "0" + toTime;
            } else {
                seltoTime = toTime + "";
            }

            if (i < 12) {


                if (fromtime < 10) {
                    fromtimedisp = "0" + fromtime;

                } else {
                    fromtimedisp = fromtime + "";

                }

                if (toTime < 10) {
                    Totimedisp = "0" + toTime;

                } else {
                    Totimedisp = toTime + "";
                }

                map.put("name", generalFunc.convertNumberWithRTL(fromtimedisp + " " + generalFunc.retrieveLangLBl("am", "LBL_AM_TXT") + " - " + Totimedisp + " " + generalFunc.retrieveLangLBl(i == 11 ? "pm" : "am", i == 11 ? "LBL_PM_TXT" : "LBL_AM_TXT")));
                map.put("selname", generalFunc.convertNumberWithRTL(selfromtime + "-" + seltoTime));

                checkmap.put("name", fromtimedisp + " - " + Totimedisp + " " + generalFunc.retrieveLangLBl("am", "LBL_AM_TXT"));
                checkmap.put("selname", selfromtime + "-" + seltoTime);


            } else {

                fromtime = fromtime % 12;
                toTime = toTime % 12;
                if (fromtime == 0) {
                    fromtime = 12;
                }

                if (toTime == 0) {
                    toTime = 12;
                }
                if (fromtime < 10) {
                    fromtimedisp = "0" + fromtime;
                } else {
                    fromtimedisp = fromtime + "";
                }

                if (toTime < 10) {
                    Totimedisp = "0" + toTime;
                } else {
                    Totimedisp = toTime + "";
                }

                map.put("name", generalFunc.convertNumberWithRTL(fromtimedisp + " " + generalFunc.retrieveLangLBl("pm", "LBL_PM_TXT") + " - " + Totimedisp + " " + generalFunc.retrieveLangLBl(i == 23 ? "am" : "pm",  i == 23 ? "LBL_AM_TXT" : "LBL_PM_TXT")));
                map.put("selname", generalFunc.convertNumberWithRTL(selfromtime + "-" + seltoTime));

                checkmap.put("name", fromtimedisp + " - " + Totimedisp + " " + generalFunc.retrieveLangLBl("pm", "LBL_PM_TXT"));
                checkmap.put("selname", selfromtime + "-" + seltoTime);
            }

            timeSlotList.add(map);
            checkTimeSlotList.add(checkmap);
        }

    }


    public void addTimeSlotApi() {

        String selectedtime = "";
        for (int i = 0; i < timeSlotList.size(); i++) {
            if (timeSlotList.get(i).get("status").equals("yes")) {
                if (selectedtime.length() == 0) {
                    selectedtime = timeSlotList.get(i).get("selname");
                } else {
                    selectedtime = selectedtime + "," + timeSlotList.get(i).get("selname");
                }
            }
        }


        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "UpdateAvailability");
        parameters.put("iDriverId", generalFunc.getMemberId());
        parameters.put("vDay", adapter.selectday);
        parameters.put("vAvailableTimes", selectedtime);
        parameters.put("UserType", Utils.app_type);

        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(), parameters);
        exeWebServer.setLoaderConfig(getActContext(), true, generalFunc);
        exeWebServer.setDataResponseListener(responseString -> {
            JSONObject responseStringObject=generalFunc.getJsonObject(responseString);

            if (responseStringObject != null && !responseStringObject.equals("")) {

                boolean isDataAvail = GeneralFunctions.checkDataAvail(Utils.action_str, responseStringObject);

                if (isDataAvail) {

                    final GenerateAlertBox generateAlert = new GenerateAlertBox(getActContext());
                    generateAlert.setCancelable(false);
                    generateAlert.setBtnClickList(btn_id -> {
                        generateAlert.closeAlertBox();


                       // new StartActProcess(getActContext()).setOkResult();
                       // backImgView.performClick();

                    });
                    generateAlert.setContentMessage("", generalFunc.retrieveLangLBl("Time slots added successfully", "LBL_TIMESLOT_ADD_SUCESS_MSG"));
                    generateAlert.setPositiveBtn(generalFunc.retrieveLangLBl("Ok", "LBL_BTN_OK_TXT"));

                    generateAlert.showAlertBox();

                } else {
                    generalFunc.showGeneralMessage("",
                            generalFunc.retrieveLangLBl("", generalFunc.getJsonValueStr(Utils.message_str, responseStringObject)));
                }
            } else {
                generalFunc.showError();
            }
        });
        exeWebServer.execute();
    }
}
