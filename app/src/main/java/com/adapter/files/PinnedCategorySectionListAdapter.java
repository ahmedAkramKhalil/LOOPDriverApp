package com.adapter.files;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import androidx.core.content.ContextCompat;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.taxifgo.driver.R;
import com.general.files.GeneralFunctions;
import com.general.files.MyApp;
import com.squareup.picasso.Picasso;
import com.view.pinnedListView.CountryListItem;
import com.view.pinnedListView.PinnedSectionListView;

import java.util.ArrayList;

public class PinnedCategorySectionListAdapter extends BaseAdapter implements PinnedSectionListView.PinnedSectionListAdapter, SectionIndexer {

    private CategoryListItem[] sections;

    private LayoutInflater inflater;

    Context mContext;
    ArrayList<CategoryListItem> categoryListItems;

    CountryClick countryClickList;

    boolean isStateList = false;
    GeneralFunctions generalFunctions;
    int color = -1, backColor = -1;
    Typeface font1;
    Typeface font2;

    public PinnedCategorySectionListAdapter(Context mContext, ArrayList<CategoryListItem> categoryListItems, CategoryListItem[] sections) {
        // TODO Auto-generated constructor stub
        this.mContext = mContext;
        this.categoryListItems = categoryListItems;
        this.sections = sections;
        generalFunctions = MyApp.getInstance().getGeneralFun(mContext);
        backColor = Color.parseColor("#eeeeee");
        color = Color.parseColor("#000000");

        font1 = Typeface.createFromAsset(mContext.getResources().getAssets(), "fonts/Poppins_SemiBold.ttf");
        font2 = Typeface.createFromAsset(mContext.getResources().getAssets(), "fonts/Poppins_Regular.ttf");

    }

    public void changeSection(CategoryListItem[] sections) {
        this.sections = sections;
    }

    public void isStateList(boolean value) {
        this.isStateList = value;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (inflater == null)
            inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (convertView == null)
            convertView = inflater.inflate(R.layout.category_section_list_item, null);

        TextView txt_view = (TextView) convertView.findViewById(R.id.txt);
        LinearLayout serviceArea = (LinearLayout) convertView.findViewById(R.id.serviceArea);
        LinearLayout itemLayout = (LinearLayout) convertView.findViewById(R.id.itemLayout);
        RelativeLayout layoutBackground = (RelativeLayout) convertView.findViewById(R.id.layoutBackground);

        ImageView rightImage = (ImageView) convertView.findViewById(R.id.rightImage);
        rightImage.setVisibility(View.VISIBLE);
        ImageView roundImageView = (ImageView) convertView.findViewById(R.id.roundImageView);


        if (generalFunctions != null && generalFunctions.isRTLmode()) {
            rightImage.setRotationY(180);
        }


        txt_view.setTextColor(Color.BLACK);
        txt_view.setTag("" + position);
        final CategoryListItem categoryListItem = categoryListItems.get(position);


        if (categoryListItem.type == CountryListItem.SECTION) {

//			convertView.setBackgroundResource(R.drawable.bg_header_country_list);
            convertView.setBackgroundColor(Color.parseColor("#f1f1f1"));
//			convertView.setAlpha((float) 0.96);
            serviceArea.setClickable(false);
            serviceArea.setEnabled(false);
            txt_view.setText(categoryListItem.getvTitle());
            txt_view.setTextColor(color);
            txt_view.setText(categoryListItem.text);
            txt_view.setTextSize(TypedValue.COMPLEX_UNIT_PX, mContext.getResources().getDimension(R.dimen._16ssp));
            txt_view.setTypeface(font1);
            rightImage.setVisibility(View.GONE);
            layoutBackground.setVisibility(View.GONE);
            serviceArea.setMinimumHeight((int) mContext.getResources().getDimension(R.dimen._40sdp));
            txt_view.setMinimumHeight((int) mContext.getResources().getDimension(R.dimen._40sdp));
            itemLayout.setBackgroundColor(Color.parseColor("#f1f1f1"));
            txt_view.setGravity(Gravity.BOTTOM|Gravity.START);



        } else {
            itemLayout.setBackgroundColor(Color.parseColor("#ffffff"));
            txt_view.setText(categoryListItem.getvTitle());
            serviceArea.setClickable(true);
            txt_view.setTextColor(color);
            txt_view.setTextSize(TypedValue.COMPLEX_UNIT_PX, mContext.getResources().getDimension(R.dimen._12ssp));
            serviceArea.setEnabled(true);
            layoutBackground.setVisibility(View.VISIBLE);
            txt_view.setTypeface(font2);

//            roundImageView.setColorFilter(Color.argb(255, 255, 255, 255));
          //  roundImageView.setColorFilter(Color.parseColor(categoryListItem.vLogo_TINT_color));
           // layoutBackground.setBackground(getRoundBG(categoryListItem.vBGColor));
            Picasso.get().load(categoryListItem.vLogo).placeholder(R.mipmap.ic_no_icon).into(roundImageView);
//			txt_count.setVisibility(View.GONE);
            serviceArea.setMinimumHeight((int) mContext.getResources().getDimension(R.dimen._40sdp));

        }

//        txt_view.setOnClickListener(new OnClickListener() {
        serviceArea.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

//				Toast.makeText(mContext, "hi--" + countryListItem.text, Toast.LENGTH_LONG).show();
                if (countryClickList != null) {
                    countryClickList.countryClickList(categoryListItem);
                }
            }
        });

        return convertView;
    }

    public interface CountryClick {
        void countryClickList(CategoryListItem countryListItem);
    }

    public void setCountryClickListener(CountryClick countryClickList) {
        this.countryClickList = countryClickList;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public CategoryListItem[] getSections() {
        return sections;
    }

    @Override
    public int getPositionForSection(int section) {
        if (section >= sections.length) {
            section = sections.length - 1;
        }
        return sections[section].listPosition;
    }

    @Override
    public int getSectionForPosition(int position) {
        if (position >= getCount()) {
            position = getCount() - 1;
        }
        return categoryListItems.get(position).sectionPosition;
    }

    @Override
    public int getItemViewType(int position) {
        return categoryListItems.get(position).type;
    }

    @Override
    public boolean isItemViewTypePinned(int viewType) {
        return viewType == CountryListItem.SECTION;
    }

    @Override
    public int getCount() {

        return categoryListItems.size();
    }

    @Override
    public Object getItem(int position) {

        return categoryListItems.get(position);
    }

    @Override
    public long getItemId(int position) {

        return position;
    }

    private GradientDrawable getRoundBG(String color) {

        //int strokeWidth = 5;
        //int strokeColor = Color.parseColor("#03dc13");
        int fillColor = Color.parseColor(color);
        GradientDrawable gD = new GradientDrawable();
        gD.setColor(fillColor);
        gD.setShape(GradientDrawable.RECTANGLE);
        gD.setCornerRadius(100);
        //gD.setStroke(strokeWidth, strokeColor);

        return gD;
    }

}
