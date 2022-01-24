package com.adapter.files;

import android.content.Context;
import androidx.core.content.ContextCompat;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.taxifgo.driver.R;
import com.general.files.GeneralFunctions;
import com.view.MTextView;
import com.view.TimelineView;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Admin on 19-06-2017.
 */

public class InactiveRecycleAdapter extends RecyclerView.Adapter<InactiveRecycleAdapter.ViewHolder> {


    ArrayList<HashMap<String, String>> list_item;
    Context mContext;
    public GeneralFunctions generalFunc;

    InactiveRecycleAdapter.OnItemClickList onItemClickList;

    public InactiveRecycleAdapter(Context mContext, ArrayList<HashMap<String, String>> list_item, GeneralFunctions generalFunc) {
        this.mContext = mContext;
        this.list_item = list_item;
        this.generalFunc = generalFunc;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_inactive, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {


        holder.mTimelineView.setMarker(ContextCompat.getDrawable(mContext, R.drawable.marker), ContextCompat.getColor(mContext, R.color.appThemeColor_1));

        HashMap<String, String> itemData = list_item.get(position);
        String line = itemData.get("line");
        String state = itemData.get("state");

        if (position == 0) {
            holder.tvView1.setVisibility(View.GONE);
        } else {
            holder.tvView1.setVisibility(View.VISIBLE);
        }
        if (position == list_item.size() - 1) {
            holder.tvView2.setVisibility(View.GONE);
        } else {
            holder.tvView2.setVisibility(View.VISIBLE);
        }


        if (line.equals("start")) {
            holder.mTimelineView.initLine(TimelineView.LineType.BEGIN);

            holder.mTimelineView.setMarker(ContextCompat.getDrawable(mContext, R.drawable.ic_check_mark_button));

        } else if (line.equals("two")) {
            if (state.equals("false")) {
                holder.mTimelineView.setMarker(ContextCompat.getDrawable(mContext, R.drawable.ic_two));
            } else {
                holder.mTimelineView.setMarker(ContextCompat.getDrawable(mContext, R.drawable.ic_check_mark_button));

            }
        } else if (line.equals("three")) {
            if (state.equals("false")) {
                holder.mTimelineView.setMarker(ContextCompat.getDrawable(mContext, R.drawable.ic_three));
            } else {
                holder.mTimelineView.setMarker(ContextCompat.getDrawable(mContext, R.drawable.ic_check_mark_button));

            }
        } else if (line.equals("four")) {
            if (state.equals("false")) {
                holder.mTimelineView.setMarker(ContextCompat.getDrawable(mContext, R.drawable.ic_four));
            } else {
                holder.mTimelineView.setMarker(ContextCompat.getDrawable(mContext, R.drawable.ic_check_mark_button));

            }

        } else if (line.equals("end")) {
            holder.mTimelineView.initLine(TimelineView.LineType.END);
            if (state.equals("false")) {
                if (list_item.size() == 4) {
                    holder.mTimelineView.setMarker(ContextCompat.getDrawable(mContext, R.drawable.ic_four));
                } else {
                    holder.mTimelineView.setMarker(ContextCompat.getDrawable(mContext, R.drawable.ic_five));
                }
            } else {
                holder.mTimelineView.setMarker(ContextCompat.getDrawable(mContext, R.drawable.ic_check_mark_button));

            }
        }
        holder.text_inactive_title.setText(itemData.get("title"));
        if (itemData.get("msg").equals("")) {
            holder.text_inactive_msg.setVisibility(View.GONE);
        } else {
            holder.text_inactive_msg.setText(itemData.get("msg"));
            holder.text_inactive_msg.setVisibility(View.VISIBLE);
        }
        if (itemData.get("btn").equals("")) {
            holder.text_inactive_btn_area.setVisibility(View.GONE);
        } else {
            holder.text_inactive_btn_area.setVisibility(View.VISIBLE);
            holder.text_inactive_btn.setText(itemData.get("btn"));
        }

        holder.text_inactive_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onItemClickList != null) {
                    onItemClickList.onItemClick(position);
                }
            }
        });


    }

    @Override
    public int getItemCount() {
        return list_item.size();
    }

    public interface OnItemClickList {
        void onItemClick(int position);
    }

    public void setOnItemClickList(InactiveRecycleAdapter.OnItemClickList onItemClickList) {
        this.onItemClickList = onItemClickList;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public MTextView text_inactive_title;
        public MTextView text_inactive_msg;
        public MTextView text_inactive_btn;
        public TimelineView mTimelineView;
        CardView text_inactive_btn_area;
        View tvView1, tvView2;

        public ViewHolder(View view) {
            super(view);

            text_inactive_title = (MTextView) view.findViewById(R.id.text_inactive_title);
            text_inactive_msg = (MTextView) view.findViewById(R.id.text_inactive_msg);
            text_inactive_btn = (MTextView) view.findViewById(R.id.text_inactive_btn);
            text_inactive_btn_area = (CardView) view.findViewById(R.id.text_inactive_btn_area);
            mTimelineView = (TimelineView) view.findViewById(R.id.time_marker);
            tvView1 = (View) view.findViewById(R.id.tvView1);
            tvView2 = (View) view.findViewById(R.id.tvView2);
        }
    }

}
