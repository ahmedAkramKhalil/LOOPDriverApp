package com.adapter.files;

import android.content.Context;
import android.graphics.Color;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.taxifgo.driver.R;
import com.taxifgo.driver.ViewStopOverDetailsActivity;
import com.general.files.GeneralFunctions;
import com.view.MTextView;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Admin on 03-11-2017.
 */

public class ViewStopOverDetailRecyclerAdapter extends RecyclerView.Adapter<ViewStopOverDetailRecyclerAdapter.ViewHolder> {

    public GeneralFunctions generalFunc;
    ArrayList<HashMap<String,String>> list_item;
    Context mContext;
    ViewStopOverDetailsActivity mActivity;
    OnItemClickList onItemClickList;

    int lineColor=-1;

    public ViewStopOverDetailRecyclerAdapter(Context mContext, ViewStopOverDetailsActivity mActivity, ArrayList<HashMap<String,String>> list_item, GeneralFunctions generalFunc) {
        this.mContext = mContext;
        this.list_item = list_item;
        this.generalFunc = generalFunc;
        this.mActivity = mActivity;
        lineColor=Color.parseColor("#6eb746");

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.design_view_stop_over_detail, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, final int position) {
        final HashMap<String,String> item = list_item.get(position);

        viewHolder.stopOverNoTxt.setText("" + item.get("LBL_NEXT_STOP_OVER_POINT"));
        viewHolder.upcomingStopOverLocArea.setVisibility(View.GONE);
        viewHolder.nextStopOverLocTxt.setText(item.get("LBL_STOPOVER_POINT"));
        viewHolder.stopOver_no_txt.setText("" + (position + 1));
        viewHolder.stopOver_no_txt.setTextColor(Color.parseColor("#141414"));
        viewHolder.stopOverAddressTxt.setText(item.get("tDAddress"));

        if (item.get("eReached").equalsIgnoreCase("Yes") || item.get("eCanceled").equalsIgnoreCase("Yes")) {
            viewHolder.iv_completed.setVisibility(View.VISIBLE);
            viewHolder.stopOver_no_txt.setTextColor(mContext.getResources().getColor(R.color.white));
            viewHolder.iv_red_dot.setVisibility(View.GONE);
            viewHolder.mainLine.setBackgroundColor(lineColor);
            viewHolder.mainLineTop.setBackgroundColor(lineColor);
        }

    }

    @Override
    public int getItemCount() {
        return list_item.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        CardView cv2;
        MTextView nextStopOverLocTxt, upcomingStopOverLocTxt;
        MTextView stopOverNoTxt, stopOverAddressTxt;
        LinearLayout nextStopOverLocArea, upcomingStopOverLocArea;
        MTextView  stopOver_no_txt;

        ImageView iv_red_dot, iv_green_dot, iv_completed;
        View mainLine,mainLineTop;

        public ViewHolder(View view) {
            super(view);
            cv2 = (CardView) view.findViewById(R.id.cv2);
            nextStopOverLocTxt = (MTextView) view.findViewById(R.id.nextStopOverLocTxt);
            stopOverNoTxt = (MTextView) view.findViewById(R.id.stopOverNoTxt);
            stopOverAddressTxt = (MTextView) view.findViewById(R.id.stopOverAddressTxt);
            upcomingStopOverLocTxt = (MTextView) view.findViewById(R.id.upcomingStopOverLocTxt);

            mainLine = (View) view.findViewById(R.id.mainLine);
            mainLineTop = (View) view.findViewById(R.id.mainLineTop);

            nextStopOverLocArea = (LinearLayout) view.findViewById(R.id.nextStopOverLocArea);
            upcomingStopOverLocArea = (LinearLayout) view.findViewById(R.id.upcomingStopOverLocArea);

            iv_red_dot = (ImageView) view.findViewById(R.id.iv_red_dot);
            iv_green_dot = (ImageView) view.findViewById(R.id.iv_green_dot);
            iv_completed = (ImageView) view.findViewById(R.id.iv_completed);
            stopOver_no_txt = (MTextView) view.findViewById(R.id.stopOver_no_txt);
        }
    }


    public void setOnItemClickList(OnItemClickList onItemClickList) {
        this.onItemClickList = onItemClickList;
    }

    public interface OnItemClickList {
        void onItemClick(String data, String type, int position);
    }


}
