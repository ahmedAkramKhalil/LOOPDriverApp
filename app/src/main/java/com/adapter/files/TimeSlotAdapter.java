package com.adapter.files;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.taxifgo.driver.R;
import com.view.AutoResizeTextView;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Admin on 09-10-2017.
 */

public class TimeSlotAdapter extends RecyclerView.Adapter<TimeSlotAdapter.ViewHolder> {

    Context mContext;
    View view;
    int isSelectedPos = -1;
    setRecentTimeSlotClickList setRecentTimeSlotClickList;
    ArrayList<String> templist = new ArrayList<>();
    ArrayList<String> sellist = new ArrayList<>();

   public ArrayList<HashMap<String, String>> timeSlotList;
    public  ArrayList<HashMap<String, String>> selTimeSlotList;
    public ArrayList<HashMap<String, String>> checkTimeSlotList;
    boolean[] checkedPositions;


    public TimeSlotAdapter(Context context, ArrayList<HashMap<String, String>> timeSlotList, ArrayList<HashMap<String, String>> selTimeSlotList, ArrayList<HashMap<String, String>> checkTimeSlotList) {
        this.mContext = context;
        this.timeSlotList = timeSlotList;
        this.selTimeSlotList = selTimeSlotList;
        this.checkTimeSlotList = checkTimeSlotList;
        checkedPositions = new boolean[timeSlotList.size()];
        makeAllViewfalse();
    }

    @Override
    public TimeSlotAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_timeslot_view, parent, false);
        return new TimeSlotAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final TimeSlotAdapter.ViewHolder holder, final int position) {
        HashMap<String, String> item=timeSlotList.get(position);
        String name = item.get("name");

        holder.stratTimeTxtView.setText(name);
        holder.stratselTimeTxtView.setText(name);

        holder.selmainarea.setVisibility(View.GONE);
        holder.mainarea.setVisibility(View.VISIBLE);


        for (int j = 0; j < selTimeSlotList.size(); j++) {
            HashMap<String, String> data = selTimeSlotList.get(j);
            if (data.get("selname").equals(checkTimeSlotList.get(position).get("selname"))) {
                HashMap<String, String> mapdata = item;
                if (data.get("status").equals("yes")) {
                    isSelectedPos = position;
                    checkedPositions[position] = true;
                    mapdata.put("status", "yes");
                    timeSlotList.set(position, mapdata);

                }else {
                    checkedPositions[position] = false;
                }

                selTimeSlotList.remove(j);
            }
        }


        if (checkedPositions[position]) {
            holder.selmainarea.setVisibility(View.VISIBLE);
            holder.mainarea.setVisibility(View.GONE);
        }else {
            holder.selmainarea.setVisibility(View.GONE);
            holder.mainarea.setVisibility(View.VISIBLE);
        }


        holder.mainarea.setOnClickListener(v -> {
            HashMap<String, String> map = item;
            map.put("status", "yes");
            timeSlotList.set(position, map);
            checkTimeSlotList.set(position, map);
            isSelectedPos = position;
            checkedPositions[position] = true;
            if (setRecentTimeSlotClickList != null) {
                setRecentTimeSlotClickList.itemTimeSlotLocClick(timeSlotList);
            }

           // notifyDataSetChanged();
            notifyItemChanged(position);
        });

        holder.selmainarea.setOnClickListener(v -> {

            isSelectedPos = position;
            HashMap<String, String> map = item;
            map.put("status", "no");
            timeSlotList.set(position, map);
            checkTimeSlotList.set(position, map);
            checkedPositions[position] = false;
            if (setRecentTimeSlotClickList != null) {
                setRecentTimeSlotClickList.itemTimeSlotLocClick(timeSlotList);
            }

                //notifyDataSetChanged();
                notifyItemChanged(position);
        });


    }

    @Override
    public int getItemCount() {
        //  return recentList.size();
        return timeSlotList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {


        AutoResizeTextView stratTimeTxtView,stratselTimeTxtView;
        LinearLayout mainarea, selmainarea;


        public ViewHolder(View itemView) {
            super(itemView);

            stratTimeTxtView = (AutoResizeTextView) itemView.findViewById(R.id.stratTimeTxtView);
            mainarea = (LinearLayout) itemView.findViewById(R.id.mainarea);
            selmainarea = (LinearLayout) itemView.findViewById(R.id.selmainarea);
            stratselTimeTxtView = (AutoResizeTextView) itemView.findViewById(R.id.stratselTimeTxtView);


        }
    }

    public interface setRecentTimeSlotClickList {
        void itemTimeSlotLocClick(ArrayList<HashMap<String, String>> timeSlotList);
    }

    public void setOnClickList(setRecentTimeSlotClickList setRecentTimeSlotClickList) {
        this.setRecentTimeSlotClickList = setRecentTimeSlotClickList;
    }


    public void  makeAllViewfalse(){
        for (int i = 0; i < checkedPositions.length; i++) {
            checkedPositions[i] =false;
            HashMap<String, String> mapdata = timeSlotList.get(i);
            mapdata.put("status", "no");
            timeSlotList.set(i, mapdata);
        }
    }
}
