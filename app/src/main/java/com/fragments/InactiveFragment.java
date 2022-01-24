package com.fragments;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.adapter.files.InactiveRecycleAdapter;
import com.taxifgo.driver.AddVehicleActivity;
import com.taxifgo.driver.ListOfDocumentActivity;
import com.taxifgo.driver.MainActivity;
import com.taxifgo.driver.ManageVehiclesActivity;
import com.taxifgo.driver.R;
import com.taxifgo.driver.SetAvailabilityActivity;
import com.taxifgo.driver.UfxCategoryActivity;
import com.taxifgo.driver.UploadDocTypeWiseActivity;
import com.general.files.ExecuteWebServerUrl;
import com.general.files.GeneralFunctions;
import com.general.files.MyApp;
import com.general.files.StartActProcess;
import com.general.files.SuccessDialog;
import com.utils.Utils;
import com.view.MButton;
import com.view.MaterialRippleLayout;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Admin on 19-06-2017.
 */

public class InactiveFragment extends Fragment implements InactiveRecycleAdapter.OnItemClickList {

    View view;
    private RecyclerView mRecyclerView;
    ArrayList<HashMap<String, String>> list;
    InactiveRecycleAdapter inactiveRecycleAdapter;
    GeneralFunctions generalFunc;
    MainActivity mainActivity;

    ProgressBar loadingBar;
    View contentArea;

    boolean isdocprogress = false;
    boolean isvehicleprogress = false;
    boolean isdriveractive = false;
    boolean isavailable = false;
    JSONObject userProfileJsonObj;
    String app_type = "";

    MButton btn_type2;
    int submitBtnId;
    boolean isbtnClick = false;
    public int totalVehicles = 0;
    public boolean isUfxServicesEnabled=true;

    String LBL_UPLOAD_DOCS_NOTE, LBL_MANAGE_VEHICLES, LBL_ADD_VEHICLE, LBL_ADD_VEHICLE_AND_DOC_NOTE;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_inactive, container, false);

        mainActivity = (MainActivity) getActivity();
        generalFunc = mainActivity.generalFunc;

        mRecyclerView = (RecyclerView) view.findViewById(R.id.inActiveRecyclerView);
        loadingBar = (ProgressBar) view.findViewById(R.id.loadingBar);
        contentArea = view.findViewById(R.id.contentArea);

        btn_type2 = ((MaterialRippleLayout) view.findViewById(R.id.btn_type2)).getChildView();
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        mRecyclerView.setHasFixedSize(true);

        userProfileJsonObj = generalFunc.getJsonObject(generalFunc.retrieveValue(Utils.USER_PROFILE_JSON));
        app_type = generalFunc.getJsonValueStr("APP_TYPE", userProfileJsonObj);
        String UFX_SERVICE_AVAILABLE = generalFunc.getJsonValueStr("UFX_SERVICE_AVAILABLE", userProfileJsonObj);
        isUfxServicesEnabled = !Utils.checkText(UFX_SERVICE_AVAILABLE) || (UFX_SERVICE_AVAILABLE!=null &&UFX_SERVICE_AVAILABLE.equalsIgnoreCase("Yes"));

        submitBtnId = Utils.generateViewId();
        btn_type2.setId(submitBtnId);

        btn_type2.setText(generalFunc.retrieveLangLBl("", "LBL_CHECK_ACC_STATUS"));
        btn_type2.setOnClickListener(new setOnClickList());

        LBL_UPLOAD_DOCS_NOTE = generalFunc.retrieveLangLBl("We need to verify your driving documents to activate your account.", "LBL_UPLOAD_DOCS_NOTE");
        LBL_MANAGE_VEHICLES = generalFunc.retrieveLangLBl("Manage Vehicles", "LBL_MANAGE_VEHICLES");
        LBL_ADD_VEHICLE = generalFunc.retrieveLangLBl("Add Vehicle", "LBL_ADD_VEHICLE");
        LBL_ADD_VEHICLE_AND_DOC_NOTE = generalFunc.retrieveLangLBl("Please add your vehicles and its document. After that we will verify its registration.", "LBL_ADD_VEHICLE_AND_DOC_NOTE");

        return view;
    }

    private void setData() {
        list = new ArrayList<>();
        HashMap<String, String> map1 = new HashMap<String, String>();
        map1.put("title", generalFunc.retrieveLangLBl("Registration Successful", "LBL_REGISTRATION_SUCCESS"));
        map1.put("msg", "");
        map1.put("btn", "");
        map1.put("line", "start");
        map1.put("state", "true");
        list.add(map1);

        HashMap<String, String> map2 = new HashMap<String, String>();
        if (!isdocprogress) {
            map2.put("title", generalFunc.retrieveLangLBl("Upload your documents", "LBL_UPLOAD_YOUR_DOCS"));

            if (generalFunc.isDeliverOnlyEnabled()) {
                map2.put("msg", LBL_UPLOAD_DOCS_NOTE);
            } else {
                if (app_type.equalsIgnoreCase(Utils.CabGeneralTypeRide_Delivery_UberX) && isUfxServicesEnabled) {
                    map2.put("msg", LBL_UPLOAD_DOCS_NOTE);
                } else {
                    map2.put("msg", LBL_UPLOAD_DOCS_NOTE);
                }
            }

            map2.put("btn", generalFunc.retrieveLangLBl("Upload Document", "LBL_UPLOAD_DOC"));
            map2.put("line", "two");
            map2.put("state", isdocprogress + "");
        } else {
            map2.put("title", generalFunc.retrieveLangLBl("Upload your Documents Successful", "LBL_UPLOADDOC_SUCCESS"));
            map2.put("msg", "");
            map2.put("btn", "");
            map2.put("line", "two");
            map2.put("state", isdocprogress + "");

        }

        list.add(map2);

        HashMap<String, String> map3 = new HashMap<String, String>();
        if (!isvehicleprogress) {

            if (generalFunc.isDeliverOnlyEnabled()) {
                map3.put("title", generalFunc.retrieveLangLBl("Add vehicles with document", "LBL_ADD_VEHICLE_AND_DOC"));
                map3.put("msg", LBL_ADD_VEHICLE_AND_DOC_NOTE);

                if (totalVehicles > 0) {
                    map3.put("btn", LBL_MANAGE_VEHICLES);
                } else {
                    map3.put("btn", LBL_ADD_VEHICLE);
                }
            } else {

                if (app_type.equalsIgnoreCase(Utils.CabGeneralType_UberX)) {
                    map3.put("title", generalFunc.retrieveLangLBl("Add your services", "LBL_ADD_SERVICE_TITLE"));
                    map3.put("msg", generalFunc.retrieveLangLBl("Please select your services as per your expertise and industry.", "LBL_ADD_SERVICE_NOTE"));
                    map3.put("btn", generalFunc.retrieveLangLBl("Select Services", "LBL_SELECT_SERVICE"));
                } else {

                    if (app_type.equalsIgnoreCase(Utils.CabGeneralTypeRide_Delivery_UberX) && isUfxServicesEnabled) {
                        map3.put("title", generalFunc.retrieveLangLBl("", "LBL_ADD_SERVICES_AND_DOC"));
                        map3.put("msg", generalFunc.retrieveLangLBl("Please add your vehicles and its document. After that we will verify its registration.", "LBL_ADD_SERVICES_AND_DOC_NOTE"));
                    } else {
                        map3.put("title", generalFunc.retrieveLangLBl("Add vehicles with document", "LBL_ADD_VEHICLE_AND_DOC"));
                        map3.put("msg", LBL_ADD_VEHICLE_AND_DOC_NOTE);
                    }
                    if (app_type.equalsIgnoreCase(Utils.CabGeneralTypeRide_Delivery_UberX) && isUfxServicesEnabled) {
                        map3.put("btn", generalFunc.retrieveLangLBl("Manage Vehicles", "LBL_MANANGE_SERVICES"));
                    } else if (app_type.equalsIgnoreCase(Utils.CabGeneralTypeRide_Delivery)) {
                        if (totalVehicles > 0) {
                            map3.put("btn", LBL_MANAGE_VEHICLES);
                        } else {
                            map3.put("btn", LBL_ADD_VEHICLE);
                        }
                    } else {
                        if (totalVehicles > 0) {
                            map3.put("btn", LBL_MANAGE_VEHICLES);
                        } else {
                            map3.put("btn", LBL_ADD_VEHICLE);
                        }
                    }
                }
            }

            map3.put("line", "three");
            map3.put("state", isvehicleprogress + "");
        } else {
            if (app_type.equalsIgnoreCase(Utils.CabGeneralType_UberX) && !generalFunc.isDeliverOnlyEnabled()) {
                map3.put("title", generalFunc.retrieveLangLBl("Your Service added successfully.", "LBL_SERVICE_ADD_SUCCESS"));
            } else {
                map3.put("title", generalFunc.retrieveLangLBl("Your vehicle added successfully.", "LBL_VEHICLE_ADD_SUCCESS"));
            }
            map3.put("msg", "");
            map3.put("btn", "");
            map3.put("line", "three");
            map3.put("state", isvehicleprogress + "");
        }
        list.add(map3);

        if (app_type.equalsIgnoreCase(Utils.CabGeneralType_UberX) || (app_type.equalsIgnoreCase(Utils.CabGeneralTypeRide_Delivery_UberX)&& isUfxServicesEnabled) && !generalFunc.isDeliverOnlyEnabled()) {

            if (!isavailable) {
                HashMap<String, String> map4 = new HashMap<String, String>();
                map4.put("title", generalFunc.retrieveLangLBl(" Add your availability", "LBL_ADD_YOUR_AVAILABILITY"));
                map4.put("msg", generalFunc.retrieveLangLBl("Add your availability for scheduled booking requests ", "LBL_ADD_AVAILABILITY_DOC_NOTE"));
                map4.put("btn", generalFunc.retrieveLangLBl("", "LBL_SET_AVAILABILITY_TXT"));
                map4.put("line", "four");
                map4.put("state", isavailable + "");
                list.add(map4);
            } else {
                HashMap<String, String> map4 = new HashMap<String, String>();
                map4.put("title", generalFunc.retrieveLangLBl("Availability set successfully", "LBL_AVAILABILITY_ADD_SUCESS_MSG"));
                map4.put("msg", "");
                map4.put("btn", "");
                map4.put("line", "four");
                map4.put("state", isavailable + "");
                list.add(map4);
            }

            if (isdriveractive) {
                HashMap<String, String> map5 = new HashMap<String, String>();
                map5.put("title", generalFunc.retrieveLangLBl("", "LBL_ADMIN_APPROVE"));
                map5.put("msg", "");
                map5.put("btn", "");
                map5.put("line", "end");
                map5.put("state", isdriveractive + "");
                list.add(map5);
            } else {
                HashMap<String, String> map5 = new HashMap<String, String>();
                map5.put("title", generalFunc.retrieveLangLBl("Waiting for admin's approval", "LBL_WAIT_ADMIN_APPROVE"));
                map5.put("msg", generalFunc.retrieveLangLBl("We will check your provided information and get back to you soon.", "LBL_WAIT_ADMIN_APPROVE_NOTE"));
                map5.put("btn", "");
                map5.put("line", "end");
                map5.put("state", isdriveractive + "");
                list.add(map5);
            }

        } else {

            if (isdriveractive) {
                HashMap<String, String> map4 = new HashMap<String, String>();
                map4.put("title", generalFunc.retrieveLangLBl("", "LBL_ADMIN_APPROVE"));
                map4.put("msg", "");
                map4.put("btn", "");
                map4.put("line", "end");
                map4.put("state", isdriveractive + "");
                list.add(map4);
            } else {
                HashMap<String, String> map4 = new HashMap<String, String>();
                map4.put("title", generalFunc.retrieveLangLBl("Waiting for admin's approval", "LBL_WAIT_ADMIN_APPROVE"));
                map4.put("msg", generalFunc.retrieveLangLBl("We will check your provided information and get back to you soon.", "LBL_WAIT_ADMIN_APPROVE_NOTE_DRIVER"));
                map4.put("btn", "");
                map4.put("line", "end");
                map4.put("state", isdriveractive + "");
                list.add(map4);

            }
        }

        inactiveRecycleAdapter = new InactiveRecycleAdapter(mainActivity.getActContext(), list, mainActivity.generalFunc);
        mRecyclerView.setAdapter(inactiveRecycleAdapter);
        inactiveRecycleAdapter.setOnItemClickList(this);
    }

    @Override
    public void onItemClick(int position) {
        Utils.hideKeyboard(getActivity());
        if (position == 1) {
            //open upload doc activity
           /* if (app_type.equalsIgnoreCase(Utils.CabGeneralTypeRide_Delivery) || app_type.equalsIgnoreCase(Utils.CabGeneralTypeRide_Delivery_UberX)) {
                Bundle bundle = new Bundle();
                bundle.putString("apptype", app_type);
                bundle.putString("selView", "doc");
                new StartActProcess(mainActivity.getActContext()).startActWithData(UploadDocTypeWiseActivity.class, bundle);
            } else {*/
            Bundle bn = new Bundle();
            bn.putString("PAGE_TYPE", "Driver");
            bn.putString("doc_file", "");
            bn.putString("iDriverVehicleId", "");
            new StartActProcess(mainActivity.getActContext()).startActWithData(ListOfDocumentActivity.class, bn);
            // }


        } else if (position == 2) {

            Bundle bn = new Bundle();

            if (generalFunc.isDeliverOnlyEnabled()) {
                if (totalVehicles > 0) {
                    new StartActProcess(getActivity()).startActWithData(ManageVehiclesActivity.class, bn);
                } else {
                    new StartActProcess(getActivity()).startActWithData(AddVehicleActivity.class, bn);
                }
                return;
            }

            String eShowVehicles = generalFunc.getJsonValueStr("eShowVehicles", userProfileJsonObj);
            if (app_type.equals(Utils.CabGeneralType_UberX) || ((app_type.equalsIgnoreCase(Utils.CabGeneralTypeRide_Delivery_UberX)&& isUfxServicesEnabled) && eShowVehicles.equalsIgnoreCase("No"))) {
                bn.putString("UBERX_PARENT_CAT_ID", generalFunc.getJsonValueStr("UBERX_PARENT_CAT_ID", userProfileJsonObj));
                (new StartActProcess(getActivity())).startActWithData(UfxCategoryActivity.class, bn);
            } else if ((app_type.equalsIgnoreCase(Utils.CabGeneralTypeRide_Delivery_UberX) && isUfxServicesEnabled)&& eShowVehicles.equalsIgnoreCase("Yes")) {
                bn.putString("apptype", app_type);
                bn.putString("selView", "vehicle");
                bn.putInt("totalVehicles", totalVehicles);

                bn.putString("UBERX_PARENT_CAT_ID", app_type.equalsIgnoreCase(Utils.CabGeneralTypeRide_Delivery) ? "" : generalFunc.getJsonValueStr("UBERX_PARENT_CAT_ID", userProfileJsonObj));

                new StartActProcess(mainActivity.getActContext()).startActWithData(UploadDocTypeWiseActivity.class, bn);
            } else {
                if (totalVehicles > 0) {
                    new StartActProcess(getActivity()).startActWithData(ManageVehiclesActivity.class, bn);
                } else {
                    new StartActProcess(getActivity()).startActWithData(AddVehicleActivity.class, bn);
                }
            }
        } else if (position == 3) {
            new StartActProcess(getActivity()).startAct(SetAvailabilityActivity.class);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!isbtnClick) {
            getDriverStateDetails();
        }
    }

    public void getDriverStateDetails() {

        loadingBar.setVisibility(View.VISIBLE);
        contentArea.setVisibility(View.GONE);

        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "getDriverStates");
        parameters.put("iDriverId", generalFunc.getMemberId());
        parameters.put("UserType", Utils.app_type);

        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(mainActivity.getActContext(), parameters);
        exeWebServer.setLoaderConfig(mainActivity.getActContext(), false, generalFunc);
        exeWebServer.setDataResponseListener(responseString -> {

            JSONObject responseStringObj = generalFunc.getJsonObject(responseString);
            loadingBar.setVisibility(View.GONE);
            contentArea.setVisibility(View.VISIBLE);

            if (responseStringObj != null && !responseStringObj.equals("")) {
                boolean isDataAvail = GeneralFunctions.checkDataAvail(Utils.action_str, responseStringObj);

                if (isDataAvail) {

                    if (generalFunc.getJsonValueStr("IS_DOCUMENT_PROCESS_COMPLETED", responseStringObj).equalsIgnoreCase("yes")) {
                        isdocprogress = true;
                    } else {
                        isdocprogress = false;
                    }

                    if (generalFunc.getJsonValueStr("IS_VEHICLE_PROCESS_COMPLETED", responseStringObj).equalsIgnoreCase("yes")) {
                        isvehicleprogress = true;
                    } else {
                        isvehicleprogress = false;
                    }

                    if (generalFunc.getJsonValueStr("IS_DRIVER_STATE_ACTIVATED", responseStringObj).equalsIgnoreCase("yes")) {
                        isdriveractive = true;
                    } else {
                        isdriveractive = false;
                    }

                    if (generalFunc.getJsonValueStr("IS_DRIVER_MANAGE_TIME_AVAILABLE", responseStringObj).equalsIgnoreCase("yes")) {
                        isavailable = true;
                    } else {
                        isavailable = false;
                    }

                    totalVehicles = GeneralFunctions.parseIntegerValue(0, generalFunc.getJsonValueStr("TotalVehicles", responseStringObj));

                    if (app_type.equalsIgnoreCase(Utils.CabGeneralType_UberX)) {
                        if (isdocprogress && isvehicleprogress && isdriveractive && isavailable) {
                            setData();
                            if (!isbtnClick) {
                                MyApp.getInstance().restartWithGetDataApp();
                                return;
                            } else {
                                handleDialog();
                                return;
                            }
                        }
                    } else {
                        if (isdocprogress && isvehicleprogress && isdriveractive) {
                            setData();
                            if (!isbtnClick) {
                                MyApp.getInstance().restartWithGetDataApp();
                                return;
                            } else {
                                handleDialog();
                                return;
                            }
                        }

                    }
                    if (isbtnClick) {
                        generalFunc.showGeneralMessage("", generalFunc.retrieveLangLBl("", "LBL_DRIVER_STATUS_INCOMPLETE"));
                        isbtnClick = false;
                    }

                    setData();


                } else {
                    generalFunc.showGeneralMessage("",
                            generalFunc.retrieveLangLBl("", generalFunc.getJsonValueStr(Utils.message_str, responseStringObj)));
                }
            }
        });
        exeWebServer.execute();
    }

    public void handleDialog() {
        SuccessDialog.showSuccessDialog(getActivity(), generalFunc.retrieveLangLBl("Your Account Has Been Activated Successfully!", "LBL_DRIVER_STATUS_COMPLETE"), "",generalFunc.retrieveLangLBl("Ok", "LBL_OK"), false, () -> MyApp.getInstance().restartWithGetDataApp());

        /*final GenerateAlertBox generateAlertBox = new GenerateAlertBox(getActivity());
        generateAlertBox.setCancelable(false);
        generateAlertBox.setContentMessage("", generalFunc.retrieveLangLBl("", "LBL_DRIVER_STATUS_COMPLETE"));
        generateAlertBox.setBtnClickList(btn_id -> {
            generateAlertBox.closeAlertBox();
            MyApp.getInstance().restartWithGetDataApp();
        });
        generateAlertBox.setNegativeBtn(generalFunc.retrieveLangLBl("", "LBL_BTN_OK_TXT"));
        generateAlertBox.showAlertBox();*/
    }

    public class setOnClickList implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            int i = view.getId();
            if (i == submitBtnId) {
                isbtnClick = true;
                getDriverStateDetails();
            }
        }
    }
}
