package com.adapter.files.deliverAll;

import android.content.Context;
import android.graphics.drawable.Drawable;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.taxifgo.driver.R;
import com.general.files.GeneralFunctions;
import com.model.deliverAll.liveTaskListDataModel;
import com.squareup.picasso.Picasso;
import com.utils.CommonUtilities;
import com.utils.Logger;
import com.utils.Utilities;
import com.utils.Utils;
import com.view.MTextView;
import com.view.SelectableRoundedImageView;

import org.apache.commons.lang3.text.WordUtils;

import java.util.ArrayList;

/**
 * Created by Admin on 09-07-2016.
 */
public class OrderListRecycleAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_ITEM = 1;
    private static final int TYPE_FOOTER = 2;
    public GeneralFunctions generalFunc;
    ArrayList<liveTaskListDataModel> list;
    Context mContext;
    boolean isFooterEnabled = false;
    View footerView;
    FooterViewHolder footerHolder;
    private OnItemClickListener mItemClickListener;
    int size = -1;
    int statusBackColor = -1;
    int color = -1;

    public OrderListRecycleAdapter(Context mContext, ArrayList<liveTaskListDataModel> list, GeneralFunctions generalFunc, boolean isFooterEnabled) {
        this.mContext = mContext;
        this.list = list;
        this.generalFunc = generalFunc;
        this.isFooterEnabled = isFooterEnabled;

        size = (int) mContext.getResources().getDimension(R.dimen._50sdp);
        statusBackColor = mContext.getResources().getColor(R.color.appThemeColor_1);
        color = mContext.getResources().getColor(R.color.orange);
    }

    public void setOnItemClickListener(OnItemClickListener mItemClickListener) {
        this.mItemClickListener = mItemClickListener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        if (viewType == TYPE_FOOTER) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.footer_list, parent, false);
            this.footerView = v;
            return new FooterViewHolder(v);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.live_task_order_list_cell, parent, false);

            return new ViewHolder(view);
        }

    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {


        if (holder instanceof ViewHolder) {
            final liveTaskListDataModel item = list.get(position);

            final ViewHolder viewHolder = (ViewHolder) holder;


            viewHolder.orderCellArea.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mItemClickListener != null) {
                        mItemClickListener.onItemClickList(position, "");
                    }
                }
            });

            viewHolder.callTxt.setText(item.getLBL_CALL_TXT());
            viewHolder.navigateTxt.setText(item.getLBL_NAVIGATE());

         /*   int width = viewHolder.call_navigate_Area.getMeasuredWidth();
            int size1=width/2;
            viewHolder.callTxt.setMaxWidth(size1);
            viewHolder.navigateTxt.setMaxWidth(size1);
*/

            boolean isRtl = generalFunc.isRTLmode();
            if (item.isRestaurant().equalsIgnoreCase("Yes")) {
                viewHolder.orderPhaseTitleTxt.setText(item.getLBL_CURRENT_TASK_TXT());
                Logger.e("OrderNumber", "::" + item.getOrderNumber());
                viewHolder.orderState_numberTxt.setText(item.getLBL_PICKUP() + " #" + generalFunc.convertNumberWithRTL(item.getOrderNumber()));
                viewHolder.placeNameTxt.setText(item.getRestaurantName());
                viewHolder.placeAddressTxt.setText(item.getRestaurantAddress());
                viewHolder.call_navigate_Area.setVisibility(View.VISIBLE);

                String image_url = CommonUtilities.COMPANY_PHOTO_PATH + item.getRestaurantId() + "/" + item.getRestaurantImage();

                Picasso.get()
                        .load(Utilities.getResizeImgURL(mContext, image_url, size, size))
                        .placeholder(R.mipmap.ic_no_icon)
                        .error(R.mipmap.ic_no_icon)
                        .into(viewHolder.storeImgView);

                viewHolder.iv_display_icon.setVisibility(View.GONE);
                viewHolder.storeImgView.setVisibility(View.VISIBLE);
                //  viewHolder.orderStatusNumberArea.setBackgroundColor(statusBackColor);

                if (generalFunc.isRTLmode()) {
                    viewHolder.orderStatusNumberArea.setBackground(mContext.getResources().getDrawable(R.drawable.start_curve_cardview_rtl));
                }


                Drawable buttonDrawable = viewHolder.orderStatusNumberArea.getBackground();

                buttonDrawable = DrawableCompat.wrap(buttonDrawable);
                //the color is a direct color int and not a color resource
                DrawableCompat.setTint(buttonDrawable, statusBackColor);
                viewHolder.orderStatusNumberArea.setBackground(buttonDrawable);
            } else {
                String pickedFromRes = item.getPickedFromRes();

                if (pickedFromRes.equalsIgnoreCase("Yes")) {
                    viewHolder.orderPhaseTitleTxt.setText(item.getLBL_CURRENT_TASK_TXT());
                } else {
                    viewHolder.orderPhaseTitleTxt.setText(item.getLBL_NEXT_TASK_TXT());
                }
                String userName = item.getUserName();

                if (Utils.checkText(userName)) {
                    viewHolder.placeNameTxt.setText(WordUtils.capitalizeFully(userName));
                }
                viewHolder.placeAddressTxt.setText(item.getUserAddress());
                Logger.e("Data", "::" + item.getOrderNumber());
                viewHolder.orderState_numberTxt.setText(item.getLBL_DELIVER() + " #" + generalFunc.convertNumberWithRTL(item.getOrderNumber()));

                viewHolder.iv_display_icon.setVisibility(View.VISIBLE);
                viewHolder.storeImgView.setVisibility(View.GONE);
                viewHolder.iv_display_icon.setImageResource(R.drawable.ic_location);
                //  viewHolder.orderStatusNumberArea.setBackgroundColor(color);
                if (generalFunc.isRTLmode()) {
                    viewHolder.orderStatusNumberArea.setBackground(mContext.getResources().getDrawable(R.drawable.start_curve_cardview_rtl));
                }

                Drawable buttonDrawable = viewHolder.orderStatusNumberArea.getBackground();
                buttonDrawable = DrawableCompat.wrap(buttonDrawable);
                //the color is a direct color int and not a color resource
                DrawableCompat.setTint(buttonDrawable, color);
                viewHolder.orderStatusNumberArea.setBackground(buttonDrawable);
                viewHolder.call_navigate_Area.setVisibility(View.GONE);


                if (pickedFromRes.equalsIgnoreCase("No") || (pickedFromRes.equalsIgnoreCase("Yes") && item.getIsPhotoUploaded().equalsIgnoreCase("No"))) {
                    viewHolder.orderCellArea.setOnClickListener(null);
                }
            }


            viewHolder.callTxt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mItemClickListener != null) {
                        mItemClickListener.onItemClickList(position, "Call");
                    }
                }
            });

            viewHolder.navigateTxt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mItemClickListener != null) {
                        mItemClickListener.onItemClickList(position, "Navigate");
                    }
                }
            });


        } else {
            FooterViewHolder footerHolder = (FooterViewHolder) holder;
            this.footerHolder = footerHolder;
        }


    }

    @Override
    public int getItemViewType(int position) {
        if (isPositionFooter(position) && isFooterEnabled == true) {
            return TYPE_FOOTER;
        }
        return TYPE_ITEM;
    }

    private boolean isPositionFooter(int position) {
        return position == list.size();
    }

    // Return the size of your itemsData (invoked by the layout manager)
    @Override
    public int getItemCount() {
        if (isFooterEnabled == true) {
            return list.size() + 1;
        } else {
            return list.size();
        }

    }

    public void addFooterView() {
        this.isFooterEnabled = true;
        notifyDataSetChanged();
        if (footerHolder != null)
            footerHolder.progressArea.setVisibility(View.VISIBLE);
    }

    public void removeFooterView() {
        if (footerHolder != null)
            footerHolder.progressArea.setVisibility(View.GONE);
    }


    public interface OnItemClickListener {
        void onItemClickList(int position, String pickedFromRes);
    }

    // inner class to hold a reference to each item of RecyclerView
    public class ViewHolder extends RecyclerView.ViewHolder {


        private ImageView iv_display_icon;
        private SelectableRoundedImageView storeImgView;
        private MTextView orderPhaseTitleTxt, placeNameTxt, placeAddressTxt;
        private MTextView callTxt, navigateTxt;
        private MTextView orderState_numberTxt;
        private LinearLayout placeDetailArea, call_navigate_Area;
        private LinearLayout bookingNumArea;
        private LinearLayout orderStatusNumberArea;
        private FrameLayout orderCellArea;

        public ViewHolder(View view) {
            super(view);

            orderPhaseTitleTxt = (MTextView) view.findViewById(R.id.orderPhaseTitleTxt);
            callTxt = (MTextView) view.findViewById(R.id.callTxt);
            navigateTxt = (MTextView) view.findViewById(R.id.navigateTxt);
            orderState_numberTxt = (MTextView) view.findViewById(R.id.orderState_numberTxt);
            placeNameTxt = (MTextView) view.findViewById(R.id.placeNameTxt);
            placeAddressTxt = (MTextView) view.findViewById(R.id.placeAddressTxt);
            iv_display_icon = (ImageView) view.findViewById(R.id.iv_display_icon);
            storeImgView = (SelectableRoundedImageView) view.findViewById(R.id.storeImgView);
            call_navigate_Area = (LinearLayout) view.findViewById(R.id.call_navigate_Area);
            placeDetailArea = (LinearLayout) view.findViewById(R.id.placeDetailArea);
            bookingNumArea = (LinearLayout) view.findViewById(R.id.bookingNumArea);
            orderCellArea = (FrameLayout) view.findViewById(R.id.orderCellArea);
            orderStatusNumberArea = (LinearLayout) view.findViewById(R.id.orderStatusNumberArea);

        }
    }

    class FooterViewHolder extends RecyclerView.ViewHolder {
        LinearLayout progressArea;

        public FooterViewHolder(View itemView) {
            super(itemView);

            progressArea = (LinearLayout) itemView;

        }
    }
}
