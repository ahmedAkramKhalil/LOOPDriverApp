package com.taxifgo.driver;

import android.content.Context;
import android.os.Bundle;

import com.google.android.material.tabs.TabLayout;

import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.adapter.files.ViewPagerAdapter;
import com.fragments.StatisticsFragment;
import com.general.files.GeneralFunctions;
import com.general.files.MyApp;
import com.utils.Utils;
import com.view.MTextView;

import org.json.JSONObject;

import java.util.ArrayList;


public class StatisticsActivity extends AppCompatActivity {

    MTextView titleTxt;
    ImageView backImgView;
    GeneralFunctions generalFunc;
    JSONObject userProfileJsonObj;
    CharSequence[] titles;
    ArrayList<Fragment> fragmentList = new ArrayList<>();

    LinearLayout tablayoutArea, toolbar_layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);
        generalFunc = MyApp.getInstance().getGeneralFun(getActContext());
        titleTxt = (MTextView) findViewById(R.id.titleTxt);
        backImgView = (ImageView) findViewById(R.id.backImgView);
        tablayoutArea = (LinearLayout) findViewById(R.id.tablayoutArea);
        toolbar_layout = (LinearLayout) findViewById(R.id.toolbar_layout);

        backImgView.setOnClickListener(new setOnClickList());
        userProfileJsonObj = generalFunc.getJsonObject(generalFunc.retrieveValue(Utils.USER_PROFILE_JSON));

        ViewPager appLogin_view_pager = (ViewPager) findViewById(R.id.appLogin_view_pager);
        TabLayout material_tabs = (TabLayout) findViewById(R.id.material_tabs);

        titles = new CharSequence[]{generalFunc.retrieveLangLBl("Trip", "LBL_TRIP_TXT")};

        material_tabs.setVisibility(View.VISIBLE);
        if (generalFunc.retrieveValue(Utils.ONLYDELIVERALL_KEY).equalsIgnoreCase("No")) {
            fragmentList.add(generateStatisticsFrag(Utils.MENU_TRIP_STATISTICS));
        }

        if (generalFunc.isAnyDeliverOptionEnabled()) {
            fragmentList.add(generateStatisticsFrag(Utils.MENU_ORDER_STATISTICS));
            tablayoutArea.setVisibility(View.VISIBLE);
            titles = new CharSequence[]{generalFunc.retrieveLangLBl("Trip", "LBL_TRIP_TXT"), generalFunc.retrieveLangLBl("Order", "LBL_ORDER")};
            toolbar_layout.setBackgroundColor(getResources().getColor(R.color.appThemeColor_1));
            toolbar_layout.setPadding(0, 0, 0, (int) getResources().getDimension(R.dimen._18sdp));
        }

        if (generalFunc.retrieveValue(Utils.ONLYDELIVERALL_KEY).equalsIgnoreCase("Yes")) {
            tablayoutArea.setVisibility(View.GONE);
            fragmentList.add(generateStatisticsFrag(Utils.MENU_ORDER_STATISTICS));
        }

        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager(), titles, fragmentList);
        appLogin_view_pager.setAdapter(adapter);
        material_tabs.setupWithViewPager(appLogin_view_pager);


        appLogin_view_pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

                fragmentList.get(position).onResume();
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        setLabels();

    }


    public StatisticsFragment generateStatisticsFrag(int type) {
        StatisticsFragment frag = new StatisticsFragment();
        Bundle bn = new Bundle();
        bn.putInt("type", type);
        frag.setArguments(bn);
        return frag;
    }


    public Context getActContext() {
        return StatisticsActivity.this;
    }


    public class setOnClickList implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            int i = view.getId();
            Utils.hideKeyboard(StatisticsActivity.this);

            if (i == R.id.backImgView) {
                StatisticsActivity.super.onBackPressed();
            }
        }
    }


    public void setLabels() {
        titleTxt.setText(generalFunc.retrieveLangLBl("Trip Statistics", "LBL_STATISTICS"));
    }


}
