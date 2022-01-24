package com.adapter.files.deliverAll;

import android.content.Context;
import android.graphics.Color;

import androidx.appcompat.widget.AppCompatRadioButton;
import androidx.recyclerview.widget.RecyclerView;

import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;

import com.taxifgo.driver.R;
import com.general.files.GeneralFunctions;
import com.utils.Utils;
import com.view.MTextView;

import org.json.JSONArray;
import org.json.JSONObject;

import static android.text.Spanned.SPAN_INCLUSIVE_INCLUSIVE;

/**
 * Created by Esite on 14-04-2018.
 */

public class VehicleSingleCheckListAdapter extends RecyclerView.Adapter<VehicleSingleCheckListAdapter.ListItemViewHolder> {

    private JSONArray vehicletypelist;
    private Context mContext;
    public int selectedPosition =-1;
    private OnItemClickListener mItemClickListener;
    private String vCarTypes[];
    private GeneralFunctions generalFunc;


    public VehicleSingleCheckListAdapter(Context mContext, String[] vCarTypes, GeneralFunctions generalFunc, JSONArray vehicletypelist) {

        this.mContext = mContext;
        this.generalFunc = generalFunc;
        this.vCarTypes = vCarTypes;
        if (vehicletypelist == null) {
            throw new IllegalArgumentException(
                    "PrescriptionProductList must not be null");
        }
        this.vehicletypelist = vehicletypelist;

    }

    public void setOnItemClickListener(OnItemClickListener mItemClickListener) {
        this.mItemClickListener = mItemClickListener;
    }


    public void setSelectedPosition(int selectedPosition) {
        this.selectedPosition = selectedPosition;
    }

    private View.OnClickListener onStateChangedListener(final RadioButton checkBox, final int position) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (position == selectedPosition) {
                    selectedPosition = -1;
                } else if (checkBox.isChecked()) {
                    selectedPosition = position;
                } else {
                    selectedPosition = -1;
                }

                if (mItemClickListener != null) {
                    mItemClickListener.onItemClickList(selectedPosition);
                }

                notifyDataSetChanged();
            }
        };
    }

    @Override
    public ListItemViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

        View itemView = LayoutInflater.
                from(viewGroup.getContext()).
                inflate(R.layout.item_select_service_deliver_all_design,
                        viewGroup,
                        false);
        return new ListItemViewHolder(itemView);

    }

    @Override
    public void onBindViewHolder(ListItemViewHolder holder, final int position) {

        final JSONObject obj = generalFunc.getJsonObject(vehicletypelist, position);




        String text1 = generalFunc.getJsonValueStr("vVehicleType", obj);
        String text2="";
        if (generalFunc.getJsonValueStr("showTag", obj).equalsIgnoreCase("Yes")) {
             text2 = " (" + generalFunc.getJsonValueStr("LBL_DELIVERALL", obj) + ")";
        }

        SpannableString span1 = new SpannableString(text1);
        span1.setSpan(new AbsoluteSizeSpan(Utils.dpToPx(20, mContext)), 0, text1.length(), SPAN_INCLUSIVE_INCLUSIVE);
        span1.setSpan(new ForegroundColorSpan(Color.parseColor("#272727")), 0, text1.length(), 0);
        SpannableString span2 = new SpannableString(text2);
        span2.setSpan(new AbsoluteSizeSpan(Utils.dpToPx(14, mContext)), 0, text2.length(), SPAN_INCLUSIVE_INCLUSIVE);
        span2.setSpan(new ForegroundColorSpan(Color.parseColor("#838383")), 0, text2.length(), 0);
        CharSequence finalText = TextUtils.concat(span1, "", span2);

        holder.serviceNameTxtView.setText(finalText);

        String SubTitle=generalFunc.getJsonValueStr("SubTitle", obj);
        if (!Utils.checkText(SubTitle)) {
            holder.serviceTypeNameTxtView.setVisibility(View.GONE);
        }
        holder.serviceTypeNameTxtView.setText(SubTitle);

        holder.chkBox.setTag(position); // This line is important.

        if (position == selectedPosition) {
            holder.chkBox.setChecked(true);
        } else {
            holder.chkBox.setChecked(false);
        }

        holder.chkBox.setOnClickListener(onStateChangedListener(holder.chkBox, position));

    }

    public interface OnItemClickListener {
        void onItemClickList(int position);
    }


    @Override
    public int getItemCount() {
        return vehicletypelist.length();
    }


    public static class ListItemViewHolder extends RecyclerView.ViewHolder {
        private MTextView serviceNameTxtView, serviceTypeNameTxtView, apptypeTxtView;
        private AppCompatRadioButton chkBox;

        public ListItemViewHolder(View v) {
            super(v);
            serviceNameTxtView = (MTextView) v.findViewById(R.id.serviceNameTxtView);
            serviceTypeNameTxtView = (MTextView) v.findViewById(R.id.serviceTypeNameTxtView);
            apptypeTxtView = (MTextView) v.findViewById(R.id.apptypeTxtView);
            chkBox = (AppCompatRadioButton) v.findViewById(R.id.chkBox);

        }
    }
}