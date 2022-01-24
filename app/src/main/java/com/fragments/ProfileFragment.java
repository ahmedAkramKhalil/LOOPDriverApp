package com.fragments;


import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.taxifgo.driver.MyProfileActivity;
import com.taxifgo.driver.R;
import com.general.files.GeneralFunctions;
import com.utils.Utils;
import com.view.MTextView;
import com.view.editBox.MaterialEditText;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends Fragment {


    View view;
    MyProfileActivity myProfileAct;
    GeneralFunctions generalFunc;
    JSONObject userProfileJsonObj;

    MaterialEditText fNameBox;
    MaterialEditText lNameBox;
    MaterialEditText emailBox;
    MaterialEditText mobileBox;
    MaterialEditText langBox;
    MaterialEditText currencyBox;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_profile, container, false);
        myProfileAct = (MyProfileActivity) getActivity();
        generalFunc = myProfileAct.generalFunc;
        userProfileJsonObj = myProfileAct.userProfileJsonObj;

        fNameBox = (MaterialEditText) view.findViewById(R.id.fNameBox);
        lNameBox = (MaterialEditText) view.findViewById(R.id.lNameBox);
        emailBox = (MaterialEditText) view.findViewById(R.id.emailBox);
        mobileBox = (MaterialEditText) view.findViewById(R.id.mobileBox);
        langBox = (MaterialEditText) view.findViewById(R.id.langBox);
        currencyBox = (MaterialEditText) view.findViewById(R.id.currencyBox);


        removeInput();
        setLabels();


        setData();

        myProfileAct.changePageTitle(generalFunc.retrieveLangLBl("", "LBL_PROFILE_TITLE_TXT"));


        return view;
    }

    public void setLabels() {
        fNameBox.setBothText(generalFunc.retrieveLangLBl("", "LBL_FIRST_NAME_HEADER_TXT"));
        lNameBox.setBothText(generalFunc.retrieveLangLBl("", "LBL_LAST_NAME_HEADER_TXT"));
        emailBox.setBothText(generalFunc.retrieveLangLBl("", "LBL_EMAIL_LBL_TXT"));
        mobileBox.setBothText(generalFunc.retrieveLangLBl("", "LBL_MOBILE_NUMBER_HEADER_TXT"));
        langBox.setBothText(generalFunc.retrieveLangLBl("", "LBL_LANGUAGE_TXT"));
        currencyBox.setBothText(generalFunc.retrieveLangLBl("", "LBL_CURRENCY_TXT"));
        ((MTextView) view.findViewById(R.id.serviceDesHTxtView)).setText(generalFunc.retrieveLangLBl("Service Description", "LBL_SERVICE_DESCRIPTION"));
    }

    public class setOnClickList implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            Utils.hideKeyboard(getActivity());
            int i = view.getId();
            Bundle bn = new Bundle();

        }
    }

    public void removeInput() {
        Utils.removeInput(fNameBox);
        Utils.removeInput(lNameBox);
        Utils.removeInput(emailBox);
        Utils.removeInput(mobileBox);
        Utils.removeInput(langBox);
        Utils.removeInput(currencyBox);

        fNameBox.setHideUnderline(true);
        lNameBox.setHideUnderline(true);
        emailBox.setHideUnderline(true);
        mobileBox.setHideUnderline(true);
        langBox.setHideUnderline(true);
        currencyBox.setHideUnderline(true);
    }

    public void setData() {
        JSONArray currencyList_arr = generalFunc.getJsonArray(generalFunc.retrieveValue(Utils.CURRENCY_LIST_KEY));

        if (currencyList_arr.length() < 2) {
            currencyBox.setVisibility(View.GONE);
        }

        fNameBox.setText(generalFunc.getJsonValueStr("vName", userProfileJsonObj));
        lNameBox.setText(generalFunc.getJsonValueStr("vLastName", userProfileJsonObj));
        emailBox.setText(generalFunc.getJsonValueStr("vEmail", userProfileJsonObj));
        currencyBox.setText(generalFunc.getJsonValueStr("vCurrencyDriver", userProfileJsonObj));

        if (generalFunc.getJsonValue("tProfileDescription", userProfileJsonObj).equals("")) {
            ((MTextView) (view.findViewById(R.id.serviceDesVTxtView))).setText("----");
        } else {
            ((MTextView) (view.findViewById(R.id.serviceDesVTxtView))).setText(generalFunc.getJsonValueStr("tProfileDescription", userProfileJsonObj));
        }
        mobileBox.setText("+" + generalFunc.getJsonValue("vCode", userProfileJsonObj) + generalFunc.getJsonValue("vPhone", userProfileJsonObj));


        fNameBox.getLabelFocusAnimator().start();
        lNameBox.getLabelFocusAnimator().start();
        emailBox.getLabelFocusAnimator().start();
        mobileBox.getLabelFocusAnimator().start();
        langBox.getLabelFocusAnimator().start();
        currencyBox.getLabelFocusAnimator().start();


        String APP_TYPE=generalFunc.getJsonValueStr("APP_TYPE", userProfileJsonObj);
        if ((APP_TYPE.equalsIgnoreCase("UberX") || (APP_TYPE.equalsIgnoreCase(Utils.CabGeneralTypeRide_Delivery_UberX) && myProfileAct.isUfxServicesEnabled )) && !generalFunc.isDeliverOnlyEnabled()) {
            (view.findViewById(R.id.serviceDesHTxtView)).setVisibility(View.VISIBLE);
            (view.findViewById(R.id.serviceDesVTxtView)).setVisibility(View.VISIBLE);
        }

        setLanguage();
    }

    public void setLanguage() {
        HashMap<String,String> data=new HashMap<>();
        data.put(Utils.LANGUAGE_LIST_KEY,"");
        data.put(Utils.LANGUAGE_CODE_KEY,"");
        data=generalFunc.retrieveValue(data);
        JSONArray languageList_arr = generalFunc.getJsonArray(data.get(Utils.LANGUAGE_LIST_KEY));

        if (languageList_arr.length() < 2) {
            langBox.setVisibility(View.GONE);
        }

        for (int i = 0; i < languageList_arr.length(); i++) {
            JSONObject obj_temp = generalFunc.getJsonObject(languageList_arr, i);

            if ((data.get(Utils.LANGUAGE_CODE_KEY)).equals(generalFunc.getJsonValueStr("vCode", obj_temp))) {

                langBox.setText(generalFunc.getJsonValueStr("vTitle", obj_temp));
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Utils.hideKeyboard(getActivity());
    }
}
