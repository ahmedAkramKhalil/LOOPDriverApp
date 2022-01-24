package com.taxifgo.driver;

import android.content.Context;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.adapter.files.TimeSlotAdapter;
import com.general.files.ExecuteWebServerUrl;
import com.general.files.GeneralFunctions;
import com.general.files.MyApp;
import com.general.files.StartActProcess;
import com.utils.Utils;
import com.view.GenerateAlertBox;
import com.view.MButton;
import com.view.MTextView;
import com.view.MaterialRippleLayout;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class setTimeScheduleActivity extends AppCompatActivity implements TimeSlotAdapter.setRecentTimeSlotClickList {

    GeneralFunctions generalFunc;
    ImageView backImgView;
    MTextView titleTxt;
    RecyclerView timeslotRecyclerView;
    ArrayList daylist;
    MTextView serviceAddrHederTxtView;
    MButton btn_type2;
    int submitBtnId;

    String selectday;
    TimeSlotAdapter adapter;

    ArrayList<HashMap<String, String>> timeSlotList;
    ArrayList<HashMap<String, String>> selTimeSlotList;
    ArrayList<HashMap<String, String>> checkTimeSlotList;

    View loadingBar;
    View contentArea;

    private static final String TAG = "setTimeScheduleActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_time_schedule);

        generalFunc = MyApp.getInstance().getGeneralFun(getActContext());
        selectday = getIntent().getStringExtra("selectday");
        Log.d(TAG, "onCreate: "+selectday);
        backImgView = (ImageView) findViewById(R.id.backImgView);
        titleTxt = (MTextView) findViewById(R.id.titleTxt);
        loadingBar = findViewById(R.id.loadingBar);
        contentArea = findViewById(R.id.contentArea);

        timeSlotList = new ArrayList<HashMap<String, String>>();
        selTimeSlotList = new ArrayList<HashMap<String, String>>();
        checkTimeSlotList = new ArrayList<HashMap<String, String>>();

        backImgView.setOnClickListener(new setOnClick());

        settimeSlotData();
        timeslotRecyclerView = (RecyclerView) findViewById(R.id.timeslotRecyclerView);
        serviceAddrHederTxtView = (MTextView) findViewById(R.id.serviceAddrHederTxtView);

        timeslotRecyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        // dayslotRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
       // adapter = new TimeSlotAdapter(getActContext(), timeSlotList, selTimeSlotList, checkTimeSlotList);
        timeslotRecyclerView.setAdapter(adapter);
        adapter.setOnClickList(this);
        btn_type2 = ((MaterialRippleLayout) findViewById(R.id.btn_type2)).getChildView();
        submitBtnId = Utils.generateViewId();
        btn_type2.setId(submitBtnId);
        btn_type2.setOnClickListener(new setOnClick());
        setLabel();

        getTimeSlotDetails();
    }


    public void setLabel() {
        titleTxt.setText(getIntent().getStringExtra("selectday_language"));
//        titleTxt.setText(selectday);
        serviceAddrHederTxtView.setText(generalFunc.retrieveLangLBl("Select the timeslot you are available to work.", "LBL_SELECT_TIME_SLOT"));
        btn_type2.setText(generalFunc.retrieveLangLBl("", "LBL_UPDATE_GENERAL"));
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
                setTimeScheduleActivity
                        .super.onBackPressed();
            } else if (i == submitBtnId) {
                addTimeSlotApi();
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

                if (isDataAvail) {


                    selTimeSlotList.clear();

                    String messageJson = generalFunc.getJsonValueStr(Utils.message_str, responseStringObject);

                    String[] vAvailableTimes = generalFunc.getJsonValue("vAvailableTimes", messageJson).split(",");
                    for (int i = 0; i < vAvailableTimes.length; i++) {

                        HashMap<String, String> map = new HashMap<String, String>();
                        map.put("selname", vAvailableTimes[i]);
                        map.put("status", "yes");
                        selTimeSlotList.add(map);
                    }
                    adapter.notifyDataSetChanged();
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

    public void addTimeSlotApi() {

        String selectedtime = "";
        for (int i = 0; i < timeSlotList.size(); i++) {
            if (timeSlotList.get(i).get("status").equals("yes")) {
                if (selectedtime.length() == 0) {
                    selectedtime = checkTimeSlotList.get(i).get("selname");
                } else {
                    selectedtime = selectedtime + "," + checkTimeSlotList.get(i).get("selname");
                }
            }
        }


        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "UpdateAvailability");
        parameters.put("iDriverId", generalFunc.getMemberId());
        parameters.put("vDay", selectday);
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


                        new StartActProcess(getActContext()).setOkResult();
                        backImgView.performClick();

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

    public Context getActContext() {
        return setTimeScheduleActivity.this;
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
}
