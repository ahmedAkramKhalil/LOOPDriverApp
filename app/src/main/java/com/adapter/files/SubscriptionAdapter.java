package com.adapter.files;

import android.content.Context;
import android.graphics.Color;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.taxifgo.driver.R;
import com.general.files.GeneralFunctions;
import com.kyleduo.switchbutton.SwitchButton;
import com.utils.Utils;
import com.view.CreateRoundedView;
import com.view.MTextView;

import java.util.ArrayList;
import java.util.HashMap;


public class SubscriptionAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public GeneralFunctions generalFunc;
    ArrayList<HashMap<String, String>> list;
    Context mContext;
    private OnItemClickListener mItemClickListener;
    boolean isFooterEnabled = false;
    View footerView;
    FooterViewHolder footerHolder;
    String type = "";// "","Details", "history"

    private static final int TYPE_ITEM = 1;
    private static final int TYPE_HISTORY = 3;
    private static final int TYPE_FOOTER = 2;

    int renewBtnBackStrokeColor=-1;
    int subscribedBtnBackColor=-1;
    int subscribeBtnBackColor=-1;
    int subscribedBtnStrokeColor=-1;
    int subscribeBtnStrokeColor=-1;
    int btnRadius=-1;

    public SubscriptionAdapter(Context mContext, ArrayList<HashMap<String, String>> list, String type, GeneralFunctions generalFunc, boolean isFooterEnabled) {
        this.mContext = mContext;
        this.list = list;
        this.generalFunc = generalFunc;
        this.isFooterEnabled = isFooterEnabled;
        this.type = type;

        renewBtnBackStrokeColor=Color.parseColor("#4864a8");
        subscribedBtnBackColor=mContext.getResources().getColor(R.color.black);
        subscribeBtnBackColor=mContext.getResources().getColor(R.color.appThemeColor_1);
        subscribedBtnStrokeColor=mContext.getResources().getColor(R.color.light_back_logout_txt_color);
        subscribeBtnStrokeColor=mContext.getResources().getColor(R.color.light_back_color);
        btnRadius=Utils.dipToPixels(mContext, 20);

    }

    public void setOnItemClickListener(SubscriptionAdapter.OnItemClickListener mItemClickListener) {
        this.mItemClickListener = mItemClickListener;
    }

    // Return the size of your itemsData (invoked by the layout manager)
    @Override
    public int getItemCount() {
        if (isFooterEnabled) {
            return list.size() + 1;
        } else {
            return list.size();
        }
    }

    public void addFooterView() {
        this.isFooterEnabled = true;
        notifyDataSetChanged();
        if (footerHolder != null) {
            footerHolder.progressContainer.setVisibility(View.VISIBLE);
        }
    }

    public void removeFooterView() {
        if (footerHolder != null)
            footerHolder.progressContainer.setVisibility(View.GONE);
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if (viewType == TYPE_HISTORY) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_subscription_history, parent, false);
            return new HistoryViewHolder(v);
        } else if (viewType == TYPE_FOOTER) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.footer_list, parent, false);
            this.footerView = v;
            return new FooterViewHolder(v);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_subscription, parent, false);
            return new ViewHolder(view);
        }

    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {

        if (holder instanceof ViewHolder) {
            final SubscriptionAdapter.ViewHolder viewHolder = (SubscriptionAdapter.ViewHolder) holder;

            HashMap<String, String> item = list.get(position);

            viewHolder.subscriptionTxt.setText(item.get("vPlanName"));
            viewHolder.subscriptionNameTxt.setText("(" + item.get("PlanDuration") + ")");
            viewHolder.subscriptionDescTxt.setText(item.get("vPlanDescription"));
            viewHolder.subscriptionPriceTxt.setText(item.get("fPlanPrice"));
            viewHolder.subScribeBtnTxt.setText(item.get("eSubscriptionStatusLbl"));
            viewHolder.renewBtnTxt.setText(item.get("renewLBL"));


            if (item.get("showPlanDetails").equalsIgnoreCase("Yes")) {
                viewHolder.statusArea.setVisibility(View.VISIBLE);
                viewHolder.renewBtnTxt.setVisibility(item.get("isRenew").equalsIgnoreCase("Yes")?View.VISIBLE:View.GONE);

                viewHolder.statusTxt.setText(item.get("statusLbl"));
                viewHolder.subscribedStatus.setText(": " + item.get("eSubscriptionDetailStatusLbl"));

                viewHolder.planDetailsArea.setVisibility(View.VISIBLE);
                viewHolder.tvSubscribOnDateTxt.setText(item.get("subscribedOnLBL"));
                viewHolder.tvSubscribOnDateValTxt.setText(": " + item.get("tSubscribeDate"));
                viewHolder.tvExpireOnDateTxt.setText(item.get("expiredOnLBL"));
                viewHolder.tvExpireOnDateValTxt.setText(": " + item.get("tExpiryDate"));

                new CreateRoundedView(subscribedBtnBackColor, btnRadius, 2, subscribedBtnStrokeColor, viewHolder.subScribeBtnTxt);
                new CreateRoundedView(renewBtnBackStrokeColor, btnRadius, 2, renewBtnBackStrokeColor, viewHolder.renewBtnTxt);


            } else {

                viewHolder.renewBtnTxt.setVisibility(View.GONE);
                viewHolder.statusArea.setVisibility(View.GONE);

                new CreateRoundedView(subscribeBtnBackColor, btnRadius, 2, subscribeBtnStrokeColor, viewHolder.subScribeBtnTxt);
                viewHolder.planDetailsArea.setVisibility(View.GONE);
            }


            viewHolder.subScribeBtnTxt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mItemClickListener != null) {
                        mItemClickListener.onSubscribeItemClick(view, position,"Cancel");
                    }
                }
            });

            viewHolder.renewBtnTxt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mItemClickListener != null) {
                        mItemClickListener.onSubscribeItemClick(view, position,"Renew");
                    }
                }
            });
        } else if (holder instanceof HistoryViewHolder) {

            final HashMap<String, String> item = list.get(position);
            final HistoryViewHolder viewHolder = (HistoryViewHolder) holder;

            viewHolder.tvPlanTypeTitleTxt.setText(item.get("planTypeLBL"));
            viewHolder.tvPlanTypeValTxt.setText(item.get("vPlanName"));
            viewHolder.tvPlanDurationTitleTxt.setText(item.get("planDurationLBL"));
            viewHolder.tvPlanDurationValTxt.setText(item.get("PlanDuration"));
            viewHolder.tvPlanPriceTitleTxt.setText(item.get("planPriceLBL"));
            viewHolder.tvPlanPriceValTxt.setText(item.get("fPlanPrice"));
            viewHolder.tvSubscribOnDateTxt.setText(item.get("subscribedOnLBL"));
            viewHolder.tvSubscribOnDateValTxt.setText(item.get("tSubscribeDate"));
            viewHolder.tvExpireOnDateTxt.setText(item.get("expiredOnLBL"));
            viewHolder.tvExpireOnDateValTxt.setText(item.get("tExpiryDate"));
            viewHolder.subscribedStatusTitle.setText(item.get("subscribedStatusLbl"));
            viewHolder.subscribedStatus.setText(item.get("eSubscriptionStatusLbl"));

            String planLeftDays = item.get("planLeftDays");

            if (item.get("eSubscriptionStatus").equalsIgnoreCase("Subscribed") && !TextUtils.isEmpty(planLeftDays)) {
                viewHolder.packageArea.setVisibility(View.VISIBLE);
                viewHolder.tvLeftPackageDays.setText(item.get("FormattedPlanLeftDays"));
                viewHolder.tv_daysTitle.setText(item.get("planLeftDaysTitle1"));
                viewHolder.tv_daysTitle2.setText(item.get("planLeftDaysTitle2"));
            } else {
                viewHolder.packageArea.setVisibility(View.GONE);
            }

            viewHolder.tvPackageDetail.setText(item.get("PlanDuration"));

        } else {
            FooterViewHolder footerHolder = (FooterViewHolder) holder;
            this.footerHolder = footerHolder;
        }
    }


    @Override
    public int getItemViewType(int position) {
        HashMap<String, String> item = position < list.size() ? list.get(position) : new HashMap<>();
        if (isPositionFooter(position) && isFooterEnabled == true) {
            return TYPE_FOOTER;
        } else if (type.equalsIgnoreCase("HIstory") || type.equalsIgnoreCase("Details")) {
            return TYPE_HISTORY;
        }
        return TYPE_ITEM;
    }


    private boolean isPositionFooter(int position) {
        return position == list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public MTextView subscriptionTxt;
        public MTextView subscriptionNameTxt;
        public MTextView subscriptionDetailTxt;
        public MTextView subscriptionDescTxt;
        public MTextView statusTxt;
        public SwitchButton statusSwitch;
        public MTextView subScribeBtnTxt;
        public MTextView renewBtnTxt;
        public MTextView subscriptionPriceTxt;
        public MTextView subscribedStatus;
        public View view_line;
        public MTextView tvSubscribOnDateTxt;
        public MTextView tvSubscribOnDateValTxt;
        public MTextView tvExpireOnDateTxt;
        public MTextView tvExpireOnDateValTxt;
        public LinearLayout planDetailsArea;
        public LinearLayout statusArea;
        public LinearLayout btnArea;

        public ViewHolder(View view) {
            super(view);

            subscriptionTxt = (MTextView) view.findViewById(R.id.subscriptionTxt);
            subscriptionNameTxt = (MTextView) view.findViewById(R.id.subscriptionNameTxt);
            subscriptionDetailTxt = (MTextView) view.findViewById(R.id.subscriptionDetailTxt);
            subscriptionDescTxt = (MTextView) view.findViewById(R.id.subscriptionDescTxt);
            statusTxt = (MTextView) view.findViewById(R.id.statusTxt);
            statusSwitch = (SwitchButton) view.findViewById(R.id.statusSwitch);
            subScribeBtnTxt = (MTextView) view.findViewById(R.id.subScribeBtnTxt);
            renewBtnTxt = (MTextView) view.findViewById(R.id.renewBtnTxt);
            subscriptionPriceTxt = (MTextView) view.findViewById(R.id.subscriptionPriceTxt);
            subscribedStatus = (MTextView) view.findViewById(R.id.subscribedStatus);
            tvSubscribOnDateTxt = (MTextView) view.findViewById(R.id.tvSubscribOnDateTxt);
            tvSubscribOnDateValTxt = (MTextView) view.findViewById(R.id.tvSubscribOnDateValTxt);
            tvExpireOnDateTxt = (MTextView) view.findViewById(R.id.tvExpireOnDateTxt);
            tvExpireOnDateValTxt = (MTextView) view.findViewById(R.id.tvExpireOnDateValTxt);
            planDetailsArea = (LinearLayout) view.findViewById(R.id.planDetailsArea);
            statusArea = (LinearLayout) view.findViewById(R.id.statusArea);
            btnArea = (LinearLayout) view.findViewById(R.id.btnArea);
            view_line = (View) view.findViewById(R.id.view_line);


        }
    }

    public class HistoryViewHolder extends RecyclerView.ViewHolder {

        public MTextView tvPlanTypeTitleTxt;
        public MTextView tvPlanTypeValTxt;
        public MTextView tvPlanDurationTitleTxt;
        public MTextView tvPlanDurationValTxt;
        public MTextView tvPlanPriceTitleTxt;
        public MTextView tvPlanPriceValTxt;
        public MTextView tvSubscribOnDateTxt;
        public MTextView tvSubscribOnDateValTxt;
        public MTextView tvExpireOnDateTxt;
        public MTextView tvExpireOnDateValTxt;
        public MTextView subscribedStatus;
        public MTextView subscribedStatusTitle;
        public MTextView tvLeftPackageDays;
        public MTextView tvPackageDetail;
        public MTextView tv_daysTitle2;
        public MTextView tv_daysTitle;
        public LinearLayout packageArea;


        public HistoryViewHolder(View view) {
            super(view);

            tvPlanTypeTitleTxt = (MTextView) view.findViewById(R.id.tvPlanTypeTitleTxt);
            tvPlanTypeValTxt = (MTextView) view.findViewById(R.id.tvPlanTypeValTxt);
            tvPlanDurationTitleTxt = (MTextView) view.findViewById(R.id.tvPlanDurationTitleTxt);
            tvPlanDurationValTxt = (MTextView) view.findViewById(R.id.tvPlanDurationValTxt);
            tvPlanPriceTitleTxt = (MTextView) view.findViewById(R.id.tvPlanPriceTitleTxt);
            tvPlanPriceValTxt = (MTextView) view.findViewById(R.id.tvPlanPriceValTxt);
            tvSubscribOnDateTxt = (MTextView) view.findViewById(R.id.tvSubscribOnDateTxt);
            tvSubscribOnDateValTxt = (MTextView) view.findViewById(R.id.tvSubscribOnDateValTxt);
            tvExpireOnDateTxt = (MTextView) view.findViewById(R.id.tvExpireOnDateTxt);
            tvExpireOnDateValTxt = (MTextView) view.findViewById(R.id.tvExpireOnDateValTxt);
            subscribedStatus = (MTextView) view.findViewById(R.id.subscribedStatus);
            subscribedStatusTitle = (MTextView) view.findViewById(R.id.subscribedStatusTitle);
            tvLeftPackageDays = (MTextView) view.findViewById(R.id.tvLeftPackageDays);
            tv_daysTitle = (MTextView) view.findViewById(R.id.tv_daysTitle);
            tv_daysTitle2 = (MTextView) view.findViewById(R.id.tv_daysTitle2);
            tvPackageDetail = (MTextView) view.findViewById(R.id.tvPackageDetail);
            packageArea = (LinearLayout) view.findViewById(R.id.packageArea);


        }
    }

    public interface OnItemClickListener {

        void onSubscribeItemClick(View v, int position,String planStatus);
    }


    class FooterViewHolder extends RecyclerView.ViewHolder {
        LinearLayout progressContainer;

        public FooterViewHolder(View itemView) {
            super(itemView);
            progressContainer = (LinearLayout) itemView.findViewById(R.id.progressContainer);
        }
    }


}
