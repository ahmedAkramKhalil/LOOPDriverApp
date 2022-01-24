package com.fragments;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.tabs.TabLayout;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.adapter.files.ViewPagerAdapter;
import com.taxifgo.driver.R;
import com.dialogs.OpenListView;
import com.general.files.GeneralFunctions;
import com.general.files.GetLocationUpdates;
import com.general.files.MyApp;
import com.utils.Utils;
import com.view.MTextView;

import java.util.ArrayList;
import java.util.HashMap;

public class MyBookingFragment extends Fragment  {

    public GeneralFunctions generalFunc;
    MTextView titleTxt;
    ImageView backImgView;
    String userProfileJson;
    CharSequence[] titles;


    public Location userLocation;

    int selTabPos = 0;
    ArrayList<HashMap<String, String>> filterlist = new ArrayList<>();
    ArrayList<HashMap<String, String>> subFilterlist = new ArrayList<>();
    ArrayList<HashMap<String, String>> orderSubFilterlist = new ArrayList<>();

    public String selFilterType = "";
    public String selSubFilterType = "";
    public String selOrderSubFilterType = "";

    public int subFilterPosition = 0;
    public int orderSubFilterPosition = 0;
    public int filterPosition = 0;

    public ImageView filterImageview;
    androidx.appcompat.app.AlertDialog list_type;
    ArrayList<Fragment> fragmentList = new ArrayList<>();
    ViewPager appLogin_view_pager;

    HistoryFragment frag;
    OrderFragment orderFrag;

    View view;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.activity_history, container, false);

        generalFunc = MyApp.getInstance().getGeneralFun(getActContext());

        titleTxt = (MTextView) view.findViewById(R.id.titleTxt);
        backImgView = (ImageView) view.findViewById(R.id.backImgView);

        generalFunc = MyApp.getInstance().getGeneralFun(getActContext());
        userProfileJson = generalFunc.retrieveValue(Utils.USER_PROFILE_JSON);
        backImgView.setVisibility(View.GONE);
        backImgView.setOnClickListener(new setOnClickList());
        filterImageview = (ImageView) view.findViewById(R.id.filterImageview);
        filterImageview.setOnClickListener(new setOnClickList());


        setLabels();

        appLogin_view_pager = (ViewPager) view.findViewById(R.id.appLogin_view_pager);
        TabLayout material_tabs = (TabLayout) view.findViewById(R.id.material_tabs);
        LinearLayout tablayoutArea = (LinearLayout) view.findViewById(R.id.tablayoutArea);
        LinearLayout headerArea = (LinearLayout) view.findViewById(R.id.headerArea);


        if (generalFunc.isDeliverOnlyEnabled()) {
            titles = new CharSequence[]{generalFunc.retrieveLangLBl("Order", "LBL_ORDERS_TXT"),};
            tablayoutArea.setVisibility(View.GONE);
            headerArea.setPadding(0,0,0,0);
            fragmentList.add(generateOrderFrag(Utils.Past));
        }
        else if (generalFunc.isAnyDeliverOptionEnabled()) {
            titles = new CharSequence[]{generalFunc.retrieveLangLBl("", "LBL_BOOKING"), generalFunc.retrieveLangLBl("Order", "LBL_ORDERS_TXT")};
            tablayoutArea.setVisibility(View.VISIBLE);
            fragmentList.add(generateBookingFrag(Utils.Upcoming));
            fragmentList.add(generateOrderFrag(Utils.Past));
        } else {
            titles = new CharSequence[]{generalFunc.retrieveLangLBl("", "LBL_BOOKING"),};
            tablayoutArea.setVisibility(View.GONE);
            headerArea.setPadding(0,0,0,0);
            fragmentList.add(generateBookingFrag(Utils.Past));
        }

        ViewPagerAdapter adapter = new ViewPagerAdapter(getActivity().getSupportFragmentManager(), titles, fragmentList);
        appLogin_view_pager.setAdapter(adapter);
        material_tabs.setupWithViewPager(appLogin_view_pager);
        appLogin_view_pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                selTabPos = position;
                selFilterType = "";
                fragmentList.get(position).onResume();

            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
        return view;
    }

    public void filterManage(ArrayList<HashMap<String, String>> filterlist) {
        this.filterlist = filterlist;
        if (getActivity()!=null && filterlist.size() > 0 && (appLogin_view_pager!=null &&appLogin_view_pager.getCurrentItem()==0))
        {
            filterImageview.setVisibility(View.VISIBLE);
        }else
        {
            filterImageview.setVisibility(View.GONE);
        }
    }

    public void subFilterManage(ArrayList<HashMap<String, String>> filterlist, String type) {
        if (type.equalsIgnoreCase("Order")) {
            this.orderSubFilterlist = filterlist;
        } else {
            this.subFilterlist = filterlist;
        }
    }


    public void setLabels() {
        titleTxt.setText(generalFunc.retrieveLangLBl("", "LBL_MY_BOOKINGS"));
    }

    public HistoryFragment generateBookingFrag(String bookingType) {
        frag = new HistoryFragment();
        Bundle bn = new Bundle();
//        bn.putString("BOOKING_TYPE", bookingType);
        bn.putString("BOOKING_TYPE", "getMemberBookings");
        frag.setArguments(bn);
        return frag;
    }

    public OrderFragment generateOrderFrag(String bookingType) {
        orderFrag = new OrderFragment();
        Bundle bn = new Bundle();
//        bn.putString("BOOKING_TYPE", bookingType);
        bn.putString("BOOKING_TYPE", "getOrderHistory");
        orderFrag.setArguments(bn);
        return orderFrag;
    }


    public HistoryFragment getHistoryFrag() {

        if (frag != null) {
            return frag;
        }
        return null;
    }


    public OrderFragment getOrderFrag() {

        if (orderFrag != null) {
            return orderFrag;
        }
        return null;
    }


    public Context getActContext() {
        return getActivity();
    }


    public class setOnClickList implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            Utils.hideKeyboard(getActContext());
            switch (view.getId()) {
                case R.id.backImgView:
                    getActivity().onBackPressed();
                    break;
                case R.id.filterImageview:
                    BuildType("Normal");
                    break;
            }
        }
    }



    public void BuildType(String type) {
        ArrayList<String> arrayList = populateSubArrayList(type);

        OpenListView.getInstance(getActContext(), generalFunc.retrieveLangLBl("Select Type", "LBL_SELECT_TYPE"), arrayList, OpenListView.OpenDirection.BOTTOM, true, true, position -> {
            if (type.equalsIgnoreCase("Order")) {
                orderSubFilterPosition = position;
                selOrderSubFilterType = orderSubFilterlist.get(position).get("vSubFilterParam");
                getOrderFrag().filterTxt.setText(orderSubFilterlist.get(position).get("vTitle"));
            } else if (type.equalsIgnoreCase("History")) {
                subFilterPosition = position;
                selSubFilterType = subFilterlist.get(position).get("vSubFilterParam");
                getHistoryFrag().filterTxt.setText(subFilterlist.get(position).get("vTitle"));
            } else {
                filterPosition = position;
                selFilterType = filterlist.get(position).get("vFilterParam");
            }
            fragmentList.get(appLogin_view_pager.getCurrentItem()).onResume();

        }).show(populatePos(type), "vTitle");
    }

    private ArrayList<String> populateSubArrayList(String BuildType) {
        ArrayList<String> typeNameList = new ArrayList<>();
        ArrayList<HashMap<String, String>> filterArrayList = BuildType.equalsIgnoreCase("Order") ? orderSubFilterlist : (BuildType.equalsIgnoreCase("History") ? subFilterlist : filterlist);
        if (filterArrayList != null && filterArrayList.size() > 0) {
            for (int i = 0; i < filterArrayList.size(); i++) {
                typeNameList.add((filterArrayList.get(i).get("vTitle")));
            }
        }
        return typeNameList;
    }

    private int populatePos(String BuildType) {
        return BuildType.equalsIgnoreCase("Order") ? orderSubFilterPosition : (BuildType.equalsIgnoreCase("History") ? subFilterPosition : filterPosition);
    }

    public void stopLocUpdates() {
        if (GetLocationUpdates.retrieveInstance() != null) {
            GetLocationUpdates.getInstance().stopLocationUpdates(this);
        }
    }
}
