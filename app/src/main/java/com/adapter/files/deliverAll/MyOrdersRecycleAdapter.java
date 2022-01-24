package com.adapter.files.deliverAll;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.taxifgo.driver.R;
import com.general.files.GeneralFunctions;
import com.view.MTextView;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by admin on 21/05/18.
 */

public class MyOrdersRecycleAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final int TYPE_ITEM = 1;
    private static final int TYPE_FOOTER = 2;
    public static final int TYPE_HEADER = 3;
    public GeneralFunctions generalFunc;
    ArrayList<HashMap<String, String>> list;
    Context mContext;
    boolean isFooterEnabled = false;
    View footerView;
    FooterViewHolder footerHolder;
    private OnItemClickListener mItemClickListener;

    public MyOrdersRecycleAdapter(Context mContext, ArrayList<HashMap<String, String>> list, GeneralFunctions generalFunc, boolean isFooterEnabled) {
        this.mContext = mContext;
        this.list = list;
        this.generalFunc = generalFunc;
        this.isFooterEnabled = isFooterEnabled;
    }

    public void setOnItemClickListener(OnItemClickListener mItemClickListener) {
        this.mItemClickListener = mItemClickListener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        if (viewType == TYPE_HEADER) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order_history_header_design, parent, false);
            return new HeaderViewHolder(v);
        } else if (viewType == TYPE_FOOTER) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.footer_list, parent, false);
            this.footerView = v;
            return new FooterViewHolder(v);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order_history_design, parent, false);
            return new ViewHolder(view);
        }
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {

        if (holder instanceof ViewHolder) {

            final HashMap<String, String> item = list.get(position);
            final ViewHolder viewHolder = (ViewHolder) holder;

            viewHolder.orderNoTxtView.setText("#" + item.get("vOrderNo"));
            viewHolder.totalItemsTxtView.setText(item.get("TotalItems") + " " + item.get("LBL_ITEM"));
            viewHolder.orderTimeTxtView.setText(item.get("tOrderRequestDate"));
            viewHolder.userNameTxtView.setText(item.get("UseName"));

            String EarningFare = item.get("EarningFare");
            String iStatus = item.get("iStatus");
            String iStatusCode = item.get("iStatusCode");

            viewHolder.orderPriceTxtView.setText(EarningFare);
            viewHolder.orderStatusTxtView.setText(iStatus);


            if (iStatus.equalsIgnoreCase("Declined") || iStatusCode.equalsIgnoreCase("9")) {
                viewHolder.orderStatusArea.setBackgroundColor(mContext.getResources().getColor(R.color.red));
            } else if (iStatus.equalsIgnoreCase("Cancelled") || iStatusCode.equalsIgnoreCase("8")) {
                viewHolder.orderStatusArea.setBackgroundColor(mContext.getResources().getColor(R.color.defaultTextColor));
            } else if (iStatus.equalsIgnoreCase("Delivered") || iStatusCode.equalsIgnoreCase("6")) {
                viewHolder.orderStatusArea.setBackgroundColor(mContext.getResources().getColor(R.color.green));
            } else if (iStatus.equalsIgnoreCase("Refunds") || iStatusCode.equalsIgnoreCase("7")) {
                viewHolder.orderStatusArea.setBackgroundColor(mContext.getResources().getColor(R.color.red));
            } else {
                viewHolder.orderStatusArea.setVisibility(View.GONE);
            }

            if (EarningFare != null && EarningFare.trim().equals("")) {
                viewHolder.waitAmtGenerateArea.setVisibility(View.VISIBLE);
            } else {
                viewHolder.waitAmtGenerateArea.setVisibility(View.GONE);
            }

            viewHolder.amtWaitTxtView.setText(item.get("LBL_AMT_GENERATE_PENDING"));

            viewHolder.containView.setOnClickListener(view -> {
                if (mItemClickListener != null) {
                    mItemClickListener.onItemClickList(view, position);
                }
            });

        } else if (holder instanceof HeaderViewHolder) {

            final HashMap<String, String> item = list.get(position);
            final HeaderViewHolder viewHolder = (HeaderViewHolder) holder;

            viewHolder.headerTxtView.setText(item.get("vDate"));

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
        } else if (item.get("TYPE") != null && item.get("TYPE").equalsIgnoreCase("" + TYPE_HEADER)) {
            return TYPE_HEADER;
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
        if (footerHolder != null) {
            footerHolder.progressContainer.setVisibility(View.VISIBLE);
        }
    }

    public void removeFooterView() {
        if (footerHolder != null)
            footerHolder.progressContainer.setVisibility(View.GONE);
    }

    public interface OnItemClickListener {
        void onItemClickList(View view, int position);
    }

    // inner class to hold a reference to each item of RecyclerView
    public class ViewHolder extends RecyclerView.ViewHolder {

        public MTextView orderNoTxtView;
        public MTextView orderTimeTxtView;
        public MTextView totalItemsTxtView;
        public MTextView userNameTxtView;
        public MTextView orderPriceTxtView;
        public MTextView orderStatusTxtView;
        public View containerView;
        public LinearLayout containView;
        public LinearLayout orderStatusArea;
        public MTextView amtWaitTxtView;
        public LinearLayout waitAmtGenerateArea;

        public ViewHolder(View view) {
            super(view);

            containerView = view;

            orderNoTxtView = (MTextView) view.findViewById(R.id.orderNoTxtView);
            orderTimeTxtView = (MTextView) view.findViewById(R.id.orderTimeVTxt);
            totalItemsTxtView = (MTextView) view.findViewById(R.id.totalItemsTxtView);
            userNameTxtView = (MTextView) view.findViewById(R.id.userNameTxtView);
            orderPriceTxtView = (MTextView) view.findViewById(R.id.orderPriceTxtView);
            orderStatusTxtView = (MTextView) view.findViewById(R.id.orderStatusTxtView);
            containView = (LinearLayout) view.findViewById(R.id.containView);
            orderStatusArea = (LinearLayout) view.findViewById(R.id.orderStatusArea);
            amtWaitTxtView = (MTextView) view.findViewById(R.id.amtWaitTxtView);
            waitAmtGenerateArea = (LinearLayout) view.findViewById(R.id.waitAmtGenerateArea);

        }
    }

    class FooterViewHolder extends RecyclerView.ViewHolder {
        LinearLayout progressContainer;

        public FooterViewHolder(View itemView) {
            super(itemView);
            progressContainer = (LinearLayout) itemView.findViewById(R.id.progressContainer);
        }
    }

    class HeaderViewHolder extends RecyclerView.ViewHolder {
        MTextView headerTxtView;

        public HeaderViewHolder(View itemView) {
            super(itemView);
            headerTxtView = (MTextView) itemView.findViewById(R.id.headerTxtView);
        }
    }
}
