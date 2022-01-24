package com.taxifgo.driver;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.adapter.files.CategoryListItem;
import com.adapter.files.PinnedCategorySectionListAdapter;
import com.general.files.ExecuteWebServerUrl;
import com.general.files.GeneralFunctions;
import com.general.files.MyApp;
import com.general.files.StartActProcess;
import com.utils.Utilities;
import com.utils.Utils;
import com.view.ErrorView;
import com.view.MTextView;
import com.view.pinnedListView.PinnedSectionListView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class UfxCategoryActivity extends AppCompatActivity implements PinnedCategorySectionListAdapter.CountryClick {


    MTextView titleTxt;
    ImageView backImgView;

    GeneralFunctions generalFunc;

    ProgressBar loading;

    ErrorView errorView;

    String next_page_str = "";

    ArrayList<CategoryListItem> categoryitems_list;
    PinnedCategorySectionListAdapter pinnedSectionListAdapter;
    PinnedSectionListView category_list;

    boolean mIsLoading = false;
    boolean isNextPageAvailable = false;
    String UBERX_PARENT_CAT_ID = "";
    MTextView introTxt;

    String app_type = "Ride";


    private JSONObject obj_userProfile;
    MTextView noResTxt;

    View footerListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ufx_category);

        generalFunc = MyApp.getInstance().getGeneralFun(getActContext());


        obj_userProfile = generalFunc.getJsonObject(generalFunc.retrieveValue(Utils.USER_PROFILE_JSON));
        app_type = generalFunc.getJsonValueStr("APP_TYPE", obj_userProfile);

        titleTxt = (MTextView) findViewById(R.id.titleTxt);
        backImgView = (ImageView) findViewById(R.id.backImgView);
        introTxt = (MTextView) findViewById(R.id.introTxt);
        noResTxt = (MTextView) findViewById(R.id.noResTxt);

        loading = (ProgressBar) findViewById(R.id.loading);
        errorView = (ErrorView) findViewById(R.id.errorView);
        category_list = (PinnedSectionListView) findViewById(R.id.category_list);
        category_list.setShadowVisible(true);
        UBERX_PARENT_CAT_ID = getIntent().getStringExtra("UBERX_PARENT_CAT_ID");

        backImgView.setOnClickListener(new setOnClickList());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            category_list.setFastScrollEnabled(false);
            category_list.setFastScrollAlwaysVisible(false);
        }

        if (app_type.equalsIgnoreCase(Utils.CabGeneralTypeRide_Delivery_UberX)) {
            titleTxt.setText(generalFunc.retrieveLangLBl("", "LBL_MANANGE_OTHER_SERVICES"));
        } else {
            titleTxt.setText(generalFunc.retrieveLangLBl("", "LBL_MANANGE_SERVICES"));
        }

        introTxt.setText(generalFunc.retrieveLangLBl("Select category below to add services you are going to provide", "LBL_MANAGE_SERVICE_INTRO_TXT"));

        categoryitems_list = new ArrayList<>();
        getCategoryList(false);


        category_list.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

                int lastInScreen = firstVisibleItem + visibleItemCount;
                if ((lastInScreen == totalItemCount) && !(mIsLoading) && isNextPageAvailable) {
                    mIsLoading = true;
                    addFooterView();
                    getCategoryList(true);
                } else if (!isNextPageAvailable) {
                    removeFooterView();
                }

            }
        });


    }

    private void addFooterView() {
        removeFooterView();
        if (footerListView == null) {
            footerListView = (LayoutInflater.from(getActContext())).inflate(R.layout.footer_list, category_list, false);
        }
        category_list.addFooterView(footerListView);
    }

    private void removeFooterView() {
        if (footerListView == null) {
            return;
        }
        category_list.removeFooterView(footerListView);
        footerListView = null;
    }

    public class setOnClickList implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            if (view == backImgView) {
                onBackPressed();
            }
        }
    }

    public void removeNextPageConfig() {
        next_page_str = "";
        isNextPageAvailable = false;
        mIsLoading = false;
        removeFooterView();
//        loading.setVisibility(View.GONE);
    }


    public Context getActContext() {
        return UfxCategoryActivity.this;
    }

    public void setLabels() {
        titleTxt.setText(generalFunc.retrieveLangLBl("Select Category", "LBL_SELECT_CATEGORY"));
    }

    public void closeLoader() {
        if (loading.getVisibility() == View.VISIBLE) {
            loading.setVisibility(View.GONE);
        }
    }


    int sectionPosition = 0, listPosition = 0;

    public void getCategoryList(final boolean isLoadMore) {
        if (errorView.getVisibility() == View.VISIBLE) {
            errorView.setVisibility(View.GONE);
        }
        if (loading.getVisibility() != View.VISIBLE) {
            loading.setVisibility(!isLoadMore ? View.VISIBLE : View.GONE);
        }

        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "getvehicleCategory");
        parameters.put("iDriverId", generalFunc.getMemberId());
        parameters.put("iVehicleCategoryId", UBERX_PARENT_CAT_ID);
        if (isLoadMore) {
            parameters.put("page", next_page_str);
        }

        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(), parameters);
        exeWebServer.setDataResponseListener(responseString -> {
            JSONObject responseStringObject=generalFunc.getJsonObject(responseString);

            if (responseStringObject != null && !responseStringObject.equals("")) {

                if (GeneralFunctions.checkDataAvail(Utils.action_str, responseStringObject)) {

                    String nextPage = generalFunc.getJsonValueStr("NextPage", responseStringObject);
                    if (!UBERX_PARENT_CAT_ID.equalsIgnoreCase("0")) {
                        CategoryListItem[] sections = new CategoryListItem[generalFunc.getJsonArray(Utils.message_str, responseStringObject).length()];

                        JSONArray mainListArr = generalFunc.getJsonArray(Utils.message_str, responseStringObject);
                        if (pinnedSectionListAdapter == null) {
                            pinnedSectionListAdapter = new PinnedCategorySectionListAdapter(getActContext(), categoryitems_list, sections);
                            category_list.setAdapter(pinnedSectionListAdapter);
                        }

                        pinnedSectionListAdapter.setCountryClickListener(UfxCategoryActivity.this);


                        for (int j = 0; j < mainListArr.length(); j++) {

                            CategoryListItem section = new CategoryListItem(CategoryListItem.SECTION, "0");
                            section.sectionPosition = sectionPosition;
                            section.listPosition = listPosition++;
                            section.CountSubItems = GeneralFunctions.parseIntegerValue(0, j + "");
                            sections[sectionPosition] = section;

                            JSONObject subTempJson = generalFunc.getJsonObject(mainListArr, j);

                            CategoryListItem categoryListItem = new CategoryListItem(CategoryListItem.ITEM, generalFunc.getJsonValueStr("vTitle", subTempJson));
                            categoryListItem.sectionPosition = sectionPosition;
                            categoryListItem.listPosition = listPosition++;
                            categoryListItem.setvTitle(generalFunc.getJsonValueStr("vTitle", subTempJson));
                            categoryListItem.setiVehicleCategoryId(generalFunc.getJsonValueStr("iVehicleCategoryId", subTempJson));

                            categoryitems_list.add(categoryListItem);
                            pinnedSectionListAdapter.notifyDataSetChanged();
                            sectionPosition++;
                        }

                    } else {

                        JSONArray mainListArr = generalFunc.getJsonArray(Utils.message_str, responseStringObject);
                        CategoryListItem[] sections = null;

                        int sectionPosition = 0, listPosition = 0;
                        int lastItemPosition = 0;

                        if (pinnedSectionListAdapter != null) {
                            sectionPosition = pinnedSectionListAdapter.getSections().length - 1;
                            listPosition = pinnedSectionListAdapter.getSections().length - 1;

                            sections = new CategoryListItem[pinnedSectionListAdapter.getSections().length + mainListArr.length()];

                            lastItemPosition = pinnedSectionListAdapter.getSections().length - 1;

                            for (int i = 0; i < pinnedSectionListAdapter.getSections().length; i++) {
                                sections[i] = pinnedSectionListAdapter.getSections()[i];
                            }

                        } else {
                            sections = new CategoryListItem[mainListArr.length()];
                        }


                        for (int i = 0; i < mainListArr.length(); i++) {
                            JSONObject tempJson = generalFunc.getJsonObject(mainListArr, i);

                            String iVehicleCategoryId = generalFunc.getJsonValueStr("iVehicleCategoryId", tempJson);
                            String vCategory = generalFunc.getJsonValueStr("vCategory", tempJson);

                            CategoryListItem section = new CategoryListItem(CategoryListItem.SECTION, vCategory);
                            section.sectionPosition = sectionPosition;
                            section.listPosition = listPosition++;
                            section.CountSubItems = GeneralFunctions.parseIntegerValue(0, vCategory);

                            sections[sectionPosition] = section;

                            categoryitems_list.add(section);

                            JSONArray subListArr = generalFunc.getJsonArray("SubCategory", tempJson);

                            for (int j = 0; j < subListArr.length(); j++) {
                                JSONObject subTempJson = generalFunc.getJsonObject(subListArr, j);

                                CategoryListItem categoryListItem = new CategoryListItem(CategoryListItem.ITEM, generalFunc.getJsonValueStr("vCategory", tempJson));
                                categoryListItem.sectionPosition = sectionPosition;
                                categoryListItem.listPosition = listPosition++;
                                categoryListItem.setvTitle(generalFunc.getJsonValueStr("vTitle", subTempJson));
                                String resizeImageUrl = Utilities.getResizeImgURL(UfxCategoryActivity.this, generalFunc.getJsonValueStr("vLogo_image", subTempJson), 50, 50);
                                categoryListItem.setvLogo(resizeImageUrl);
                                categoryListItem.setvBGColor(generalFunc.getJsonValueStr("vLogo_BG_color", subTempJson));
                                categoryListItem.setvLogo_TINT_color(generalFunc.getJsonValueStr("vLogo_TINT_color", subTempJson));
                                categoryListItem.setiVehicleCategoryId(generalFunc.getJsonValueStr("iVehicleCategoryId", subTempJson));

                                categoryitems_list.add(categoryListItem);
                            }

                            sectionPosition++;
                        }

                        if (pinnedSectionListAdapter == null) {

                            pinnedSectionListAdapter = new PinnedCategorySectionListAdapter(getActContext(), categoryitems_list, sections);
                            category_list.setAdapter(pinnedSectionListAdapter);

                            pinnedSectionListAdapter.setCountryClickListener(UfxCategoryActivity.this);

                        } else {
                            pinnedSectionListAdapter.changeSection(sections);
                        }

                        pinnedSectionListAdapter.notifyDataSetChanged();
                    }

                    if (!nextPage.equals("") && !nextPage.equals("0")) {
                        next_page_str = nextPage;
                        isNextPageAvailable = true;
                    } else {
                        removeNextPageConfig();
                    }
                    pinnedSectionListAdapter.notifyDataSetChanged();
                } else {
                    introTxt.setVisibility(View.GONE);
                    noResTxt.setText(generalFunc.retrieveLangLBl("", generalFunc.getJsonValueStr(Utils.message_str, responseStringObject)));
                    noResTxt.setVisibility(View.VISIBLE);
                }
            } else {
                generateErrorView();
            }
            closeLoader();

            mIsLoading = false;
        });
        exeWebServer.execute();
    }

    public void generateErrorView() {

        closeLoader();

        generalFunc.generateErrorView(errorView, "LBL_ERROR_TXT", "LBL_NO_INTERNET_TXT");

        if (errorView.getVisibility() != View.VISIBLE) {
            errorView.setVisibility(View.VISIBLE);
        }
        errorView.setOnRetryListener(() -> getCategoryList(false));
    }

    @Override
    public void countryClickList(CategoryListItem countryListItem) {
        Bundle bn = new Bundle();
        bn.putString("iVehicleCategoryId", countryListItem.getiVehicleCategoryId());
        bn.putString("vTitle", countryListItem.getvTitle());
        (new StartActProcess(getActContext())).startActWithData(AddServiceActivity.class, bn);
    }
}
