package com.adapter.files;

import android.content.Context;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.taxifgo.driver.R;
import com.utils.Utils;
import com.view.MTextView;

import java.util.ArrayList;

/**
 * Created by Admin on 09-10-2017.
 */

public class DaySlotAdapter extends RecyclerView.Adapter<DaySlotAdapter.ViewHolder> {

    Context mContext;
    View view;
    public int isSelectedPos = -1;
    setRecentDateSlotClickList setRecentDateSlotClickList;
    ArrayList<String> daylist;
    ArrayList<String> selectedlist;
    ArrayList<String> displaylist;
    public String selectday = "";
    RecyclerView dayslotRecyclerView;
    int screenWidth;

    public DaySlotAdapter(Context context, ArrayList<String> daylist, ArrayList<String> selectedlist, ArrayList<String> displaylist, String selectday, RecyclerView dayslotRecyclerView,int screenWidth) {
        this.mContext = context;
        this.daylist = daylist;
        this.selectedlist = selectedlist;
        this.displaylist = displaylist;
        this.selectday = selectday;
        this.dayslotRecyclerView = dayslotRecyclerView;
        this.screenWidth = screenWidth;
    }

    @Override
    public DaySlotAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_dayslot_view, parent, false);

        return new DaySlotAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final DaySlotAdapter.ViewHolder holder, final int position) {

        String item = displaylist.get(position);
        String dayItem = daylist.get(position);
        holder.stratTimeTxtView.setText(item);
        holder.stratselTimeTxtView.setText(item);
        holder.sTimeTxtView.setText(dayItem);



        if (selectday.equalsIgnoreCase(dayItem)) {
            //  holder.selmainarea.setVisibility(View.VISIBLE);
            // holder.mainarea.setVisibility(View.VISIBLE);
            isSelectedPos = position;
            holder.mainarea.setBackgroundColor(mContext.getResources().getColor(R.color.appThemeColor_1));
            holder.cardview.setCardBackgroundColor(mContext.getResources().getColor(R.color.appThemeColor_1));
            holder.stratTimeTxtView.setTextColor(mContext.getResources().getColor(R.color.white));


               /* if (mContext instanceof SetAvailabilityActivity){

                   // int visiblePostion =  ((LinearLayoutManager)((SetAvailabilityActivity)mContext).dayslotRecyclerView.getLayoutManager()).findFirstVisibleItemPosition();
                   // ((SetAvailabilityActivity)mContext).dayslotRecyclerView.scrollToPosition(visiblePostion+1);

                    if (position==daylist.size()-1)
                        ((SetAvailabilityActivity)mContext).dayslotRecyclerView.scrollToPosition(position);
                    else if (position<=2){
                        ((SetAvailabilityActivity)mContext).dayslotRecyclerView.scrollToPosition(0);
                    }else {
                        ((SetAvailabilityActivity)mContext).dayslotRecyclerView.scrollToPosition(position+1);
                    }

                }*/
        } else {
            // holder.selmainarea.setVisibility(View.GONE);
            // holder.mainarea.setVisibility(View.VISIBLE);
            holder.mainarea.setBackgroundColor(mContext.getResources().getColor(R.color.white));
            holder.cardview.setCardBackgroundColor(mContext.getResources().getColor(R.color.white));
            holder.stratTimeTxtView.setTextColor(mContext.getResources().getColor(R.color.black));
        }
        //  }

        holder.mainarea.setOnClickListener(v -> {
            isSelectedPos = position;
            if (setRecentDateSlotClickList != null) {
                setRecentDateSlotClickList.itemDateSlotLocClick(position);
            }
        });

        holder.selmainarea.setOnClickListener(v -> {

            isSelectedPos = position;
            if (setRecentDateSlotClickList != null) {
                setRecentDateSlotClickList.itemDateSlotLocClick(position);
            }
        });


    }

    @Override
    public int getItemCount() {
        //  return recentList.size();
        return daylist.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        MTextView stratTimeTxtView, stratselTimeTxtView, sTimeTxtView;
        LinearLayout mainarea, selmainarea, selarea;
        CardView cardview;

        public ViewHolder(View itemView) {
            super(itemView);

            stratTimeTxtView = (MTextView) itemView.findViewById(R.id.stratTimeTxtView);
            mainarea = (LinearLayout) itemView.findViewById(R.id.mainarea);
            selmainarea = (LinearLayout) itemView.findViewById(R.id.selmainarea);
            selarea = (LinearLayout) itemView.findViewById(R.id.selarea);
            stratselTimeTxtView = (MTextView) itemView.findViewById(R.id.stratselTimeTxtView);
            sTimeTxtView = (MTextView) itemView.findViewById(R.id.sTimeTxtView);
            cardview = itemView.findViewById(R.id.cardview);
        }
    }

    public interface setRecentDateSlotClickList {
        void itemDateSlotLocClick(int position);
    }

    public void setOnClickList(setRecentDateSlotClickList setRecentDateSlotClickList) {
        this.setRecentDateSlotClickList = setRecentDateSlotClickList;
    }


}
