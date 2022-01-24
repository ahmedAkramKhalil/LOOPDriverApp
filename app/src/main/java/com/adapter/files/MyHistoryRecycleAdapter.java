package com.adapter.files;

import android.content.Context;
import android.graphics.Color;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.autofit.et.lib.AutoFitEditText;
import com.taxifgo.driver.R;
import com.general.files.GeneralFunctions;
import com.squareup.picasso.Picasso;
import com.utils.Utilities;
import com.utils.Utils;
import com.view.DividerView;
import com.view.MTextView;
import com.view.SelectableRoundedImageView;
import com.view.simpleratingbar.SimpleRatingBar;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Admin on 09-07-2016.
 */
public class MyHistoryRecycleAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_ITEM = 1;
    private static final int TYPE_FOOTER = 2;
    private static final int TYPE_HEADER = 3;
    public GeneralFunctions generalFunc;
    ArrayList<HashMap<String, String>> list;
    Context mContext;
    boolean isFooterEnabled = false;
    View footerView;
    FooterViewHolder footerHolder;
    private OnItemClickListener mItemClickListener;
    String type;

    JSONObject userProfileJsonObj;
    int size15_dp;
    int imagewidth ;

    public MyHistoryRecycleAdapter(Context mContext, ArrayList<HashMap<String, String>> list, GeneralFunctions generalFunc, boolean isFooterEnabled) {
        this.mContext = mContext;
        this.list = list;
        this.generalFunc = generalFunc;
        this.isFooterEnabled = isFooterEnabled;
        userProfileJsonObj = generalFunc.getJsonObject(generalFunc.retrieveValue(Utils.USER_PROFILE_JSON));
        size15_dp = (int) mContext.getResources().getDimension(R.dimen._15sdp);
        imagewidth=  (int) mContext.getResources().getDimension(R.dimen._50sdp);
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
        } else if(viewType == TYPE_HEADER){
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.earning_amount_layout, parent, false);
            return new HeaderViewHolder(view);
        }else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_my_history_design, parent, false);
            return new ViewHolder(view);
        }

    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {


        if (holder instanceof ViewHolder) {
            final HashMap<String, String> item = list.get(position);
            final ViewHolder viewHolder = (ViewHolder) holder;
            boolean isAnyButtonShown = false;

            String vPackageName = item.get("vPackageName");
            if (vPackageName != null && !vPackageName.equalsIgnoreCase("")) {
                viewHolder.packageTxt.setVisibility(View.VISIBLE);
                viewHolder.packageTxt.setText(vPackageName);
            } else {
                viewHolder.packageTxt.setVisibility(View.GONE);
            }


            viewHolder.myBookingNoHTxt.setText(item.get("LBL_BOOKING_NO") + "#");
            viewHolder.myBookingNoVTxt.setText(item.get("vRideNo"));

            String ConvertedTripRequestDate = item.get("ConvertedTripRequestDate");
            String ConvertedTripRequestTime = item.get("ConvertedTripRequestTime");
//            String formattedListingDate = item.get("formattedListingDate");

            if (ConvertedTripRequestDate != null) {
                viewHolder.dateTxt.setText(ConvertedTripRequestDate);
                viewHolder.timeTxt.setText(ConvertedTripRequestTime);
            }/* else {
                viewHolder.dateTxt.setText(formattedListingDate);
            }*/


            viewHolder.sourceAddressTxt.setText(item.get("tSaddress"));
            viewHolder.sAddressTxt.setText(item.get("tSaddress"));
            viewHolder.destAddressHTxt.setText(item.get("LBL_DEST_LOCATION"));
            viewHolder.sourceAddressHTxt.setText(item.get("LBL_PICK_UP_LOCATION"));


            String vServiceTitle = item.get("vServiceTitle");
            if (vServiceTitle != null && !vServiceTitle.equalsIgnoreCase("")) {
                viewHolder.typeArea.setVisibility(View.VISIBLE);
                viewHolder.typeArea1.setVisibility(View.GONE);
                viewHolder.SelectedTypeNameTxt.setText(vServiceTitle);
                viewHolder.SelectedTypeNameTxt1.setText(vServiceTitle);
            } else {
                viewHolder.typeArea.setVisibility(View.GONE);
                viewHolder.typeArea1.setVisibility(View.GONE);
            }

            String tDaddress = item.get("tDaddress");
            if (Utils.checkText(tDaddress)) {
                viewHolder.destarea.setVisibility(View.VISIBLE);
                viewHolder.pickupLocArea.setPadding(0, 0, 0, size15_dp);
                viewHolder.aboveLine.setVisibility(View.VISIBLE);
                viewHolder.destAddressTxt.setText(tDaddress);
            } else {
                viewHolder.destarea.setVisibility(View.GONE);
                viewHolder.aboveLine.setVisibility(View.GONE);
                viewHolder.pickupLocArea.setPadding(0, 0, 0, 0);

            }

            String vBookingType = item.get("vBookingType");
            String iActive = item.get("iActive");

            if (Utils.checkText(iActive)) {
                viewHolder.statusArea.setVisibility(View.VISIBLE);
                viewHolder.statusVTxt.setText(item.get("iActive"));
            }

            if (generalFunc.isRTLmode()) {
                viewHolder.statusArea.setRotation(180);
                viewHolder.statusVTxt.setRotation(180);
            }

            viewHolder.SelectedTypeNameTxt1.setEllipsize(TextUtils.TruncateAt.MARQUEE);
            viewHolder.SelectedTypeNameTxt1.setSelected(true);
            viewHolder.SelectedTypeNameTxt1.setSingleLine(true);

            viewHolder.SelectedTypeNameTxt.setSelected(true);
            viewHolder.SelectedTypeNameTxt.setEllipsize(TextUtils.TruncateAt.MARQUEE);
            viewHolder.SelectedTypeNameTxt.setSingleLine(true);

            viewHolder.statusVTxt.setEllipsize(TextUtils.TruncateAt.MARQUEE);
            viewHolder.statusVTxt.setSelected(true);
            viewHolder.statusVTxt.setSingleLine(true);

            viewHolder.typeArea1.setCardBackgroundColor(Color.parseColor(item.get("vService_BG_color")));
            viewHolder.SelectedTypeNameTxt1.setTextColor(Color.parseColor(item.get("vService_TEXT_color")));

            String eType = item.get("eType");
            boolean showUfxMultiArea = eType.equalsIgnoreCase(Utils.CabGeneralType_UberX) || eType.equalsIgnoreCase(Utils.eType_Multi_Delivery);
            if (vServiceTitle != null && !vServiceTitle.equalsIgnoreCase("")) {
                viewHolder.typeArea.setVisibility(View.VISIBLE);
            } else {
                viewHolder.typeArea.setVisibility(View.GONE);
            }
            viewHolder.sAddressTxt.setVisibility(View.VISIBLE);
            viewHolder.ratingBar.setVisibility(View.VISIBLE);
            viewHolder.fareArea.setVisibility(View.GONE);


            viewHolder.cancelBookingBtnArea.setVisibility(View.GONE);
            viewHolder.cancelBookingArea.setVisibility(View.GONE);
            viewHolder.viewCancelReasonBtnArea.setVisibility(View.GONE);
            viewHolder.viewCancelReasonArea.setVisibility(View.GONE);
            viewHolder.viewRequestedServiceBtnArea.setVisibility(View.GONE);
            viewHolder.startTripArea.setVisibility(View.GONE);
            viewHolder.startTripBtnArea.setVisibility(View.GONE);


            if (showUfxMultiArea || vBookingType.equalsIgnoreCase("history")) {
                viewHolder.ufxMultiArea.setVisibility(View.VISIBLE);
                viewHolder.ufxMultiBtnArea.setVisibility(View.VISIBLE);

                viewHolder.noneUfxMultiArea.setVisibility(View.GONE);
                viewHolder.noneUfxMultiBtnArea.setVisibility(View.GONE);

                viewHolder.userNameTxt.setText(item.get("vName"));

                String image_url = item.get("vImage");

                if (Utils.checkText(image_url)) {

                    Picasso.get()
                            .load(Utilities.getResizeImgURL(mContext, image_url, imagewidth, imagewidth))
                            .placeholder(R.mipmap.ic_no_pic_user)
                            .error(R.mipmap.ic_no_pic_user)
                            .into(viewHolder.userImgView);
                } else {
                    viewHolder.userImgView.setImageResource(R.mipmap.ic_no_pic_user);

                }


                viewHolder.ratingBar.setRating(Float.parseFloat(item.get("vAvgRating")));

                if (vBookingType.equalsIgnoreCase("history")) {

                    if (Utils.checkText(image_url)) {
                        Picasso.get()
                                .load(image_url)
                                .placeholder(R.mipmap.ic_no_pic_user)
                                .error(R.mipmap.ic_no_pic_user)
                                .into(viewHolder.userImageView);
                    } else {
                        viewHolder.userImageView.setImageResource(R.mipmap.ic_no_pic_user);
                    }

                    viewHolder.fareTxt.setText(item.get("currencySymbol") + item.get("iFare"));


                    if (vServiceTitle != null && !vServiceTitle.equalsIgnoreCase("")) {
                        viewHolder.typeArea1.setVisibility(View.VISIBLE);
                        viewHolder.typeArea.setVisibility(View.GONE);
                    } else {
                        viewHolder.typeArea1.setVisibility(View.GONE);
                        viewHolder.typeArea.setVisibility(View.GONE);
                    }

                    viewHolder.sAddressTxt.setVisibility(View.GONE);
                    viewHolder.userImgView.setVisibility(View.GONE);
                    viewHolder.userImageArea.setVisibility(View.VISIBLE);
                    viewHolder.ratingBar.setVisibility(View.GONE);
                    viewHolder.fareArea.setVisibility(View.VISIBLE);
                } else {
                    if (vServiceTitle != null && !vServiceTitle.equalsIgnoreCase("")) {
                        viewHolder.typeArea.setVisibility(View.VISIBLE);
                        viewHolder.typeArea1.setVisibility(View.GONE);
                    } else {
                        viewHolder.typeArea.setVisibility(View.GONE);
                        viewHolder.typeArea1.setVisibility(View.GONE);
                    }

                    viewHolder.sAddressTxt.setVisibility(View.VISIBLE);
                    viewHolder.userImgView.setVisibility(View.VISIBLE);
                    viewHolder.userImageArea.setVisibility(View.GONE);
                    viewHolder.ratingBar.setVisibility(View.VISIBLE);
                    viewHolder.fareArea.setVisibility(View.GONE);
                }

            } else {
                viewHolder.ufxMultiArea.setVisibility(View.GONE);
                viewHolder.ufxMultiBtnArea.setVisibility(View.GONE);

                viewHolder.noneUfxMultiArea.setVisibility(View.VISIBLE);
                viewHolder.noneUfxMultiBtnArea.setVisibility(View.VISIBLE);
            }

            String LBL_ACCEPT_JOB = item.get("LBL_ACCEPT_JOB");
            String LBL_START_TRIP = item.get("LBL_START_TRIP");
            String LBL_DECLINE_JOB = item.get("LBL_DECLINE_JOB");
            String LBL_CANCEL_TRIP = item.get("LBL_CANCEL_TRIP");

            if ((vBookingType.equalsIgnoreCase("schedule") || vBookingType.equalsIgnoreCase("pending"))/* && showUfxMultiArea*/) {
                viewHolder.viewRequestedServiceBtn.setText(item.get("LBL_VIEW_REQUESTED_SERVICES"));

                String showViewRequestedServicesBtn = item.get("showViewRequestedServicesBtn");
                if (Utils.checkText(showViewRequestedServicesBtn) && showViewRequestedServicesBtn.equalsIgnoreCase("Yes")) {
                    isAnyButtonShown = true;
                    viewHolder.viewRequestedServiceBtnArea.setVisibility(View.VISIBLE);
                }


                String LBL_VIEW_REASON = item.get("LBL_VIEW_REASON");
                viewHolder.viewCancelReasonBtn.setText(LBL_VIEW_REASON);
                viewHolder.btn_type_view_cancel_reason.setText(LBL_VIEW_REASON);

                String showCancelBtn = item.get("showCancelBtn");
                if (Utils.checkText(showCancelBtn) && showCancelBtn.equalsIgnoreCase("Yes")) {
                    isAnyButtonShown = true;
                    viewHolder.cancelBookingBtn.setText(LBL_CANCEL_TRIP);
                    viewHolder.btn_type_cancel.setText(LBL_CANCEL_TRIP);

                    viewHolder.cancelBookingBtnArea.setVisibility(View.VISIBLE);
                    viewHolder.cancelBookingArea.setVisibility(View.VISIBLE);
                }

                String showViewCancelReasonBtn = item.get("showViewCancelReasonBtn");
                if (Utils.checkText(showViewCancelReasonBtn) && showViewCancelReasonBtn.equalsIgnoreCase("Yes")) {
                    isAnyButtonShown = true;
                    viewHolder.viewCancelReasonBtnArea.setVisibility(View.VISIBLE);
                }

                String showStartBtn = item.get("showStartBtn");
                if (Utils.checkText(showStartBtn) && showStartBtn.equalsIgnoreCase("Yes")) {
                    viewHolder.btn_type_start.setText(LBL_START_TRIP);
                    viewHolder.startTripBtn.setText(LBL_START_TRIP);
                    isAnyButtonShown = true;
                    viewHolder.startTripArea.setVisibility(View.VISIBLE);
                    viewHolder.startTripBtnArea.setVisibility(View.VISIBLE);
                }


                String showAcceptBtn = item.get("showAcceptBtn");
                if (Utils.checkText(showAcceptBtn) && showAcceptBtn.equalsIgnoreCase("Yes")) {
                    viewHolder.btn_type_start.setText(LBL_ACCEPT_JOB);
                    viewHolder.startTripBtn.setText(LBL_ACCEPT_JOB);
                    isAnyButtonShown = true;
                    viewHolder.startTripArea.setVisibility(View.VISIBLE);
                    viewHolder.startTripBtnArea.setVisibility(View.VISIBLE);
                }

                String showDeclineBtn = item.get("showDeclineBtn");
                if (Utils.checkText(showDeclineBtn) && showDeclineBtn.equalsIgnoreCase("Yes")) {
                    viewHolder.cancelBookingBtn.setText(LBL_DECLINE_JOB);
                    viewHolder.btn_type_cancel.setText(LBL_DECLINE_JOB);
                    isAnyButtonShown = true;
                    viewHolder.cancelBookingBtnArea.setVisibility(View.VISIBLE);
                    viewHolder.cancelBookingArea.setVisibility(View.VISIBLE);
                }


                viewHolder.cancelBookingBtnArea.setOnClickListener(view -> {
                    if (mItemClickListener != null) {
                        mItemClickListener.onCancelBookingClickList(view, position);
                    }
                });

                viewHolder.cancelBookingBtn.setOnClickListener(view -> {
                    if (mItemClickListener != null) {
                        mItemClickListener.onCancelBookingClickList(view, position);
                    }
                });


                viewHolder.startTripArea.setOnClickListener(view -> {
                    if (mItemClickListener != null) {
                        mItemClickListener.onTripStartClickList(view, position);
                    }
                });

                viewHolder.startTripBtnArea.setOnClickListener(view -> {
                    if (mItemClickListener != null) {
                        mItemClickListener.onTripStartClickList(view, position);
                    }
                });

                viewHolder.viewRequestedServiceBtnArea.setOnClickListener(view -> {
                    if (mItemClickListener != null) {
                        mItemClickListener.onViewServiceClickList(view, position);
                    }
                });
            }

            if (item.get("iActive").equalsIgnoreCase("Finished") || item.get("iActive").equalsIgnoreCase("Canceled")) {
                //viewHolder.userImgView.setVisibility(View.GONE);
                //viewHolder.userImageArea.setVisibility(View.GONE);
                //viewHolder.timeTxt.setVisibility(View.INVISIBLE);
            } else {
                //viewHolder.timeTxt.setVisibility(View.VISIBLE);
            }

            String eShowHistory = item.get("eShowHistory");

            if (eShowHistory.equalsIgnoreCase("Yes")) {
                viewHolder.contentView.setOnClickListener(v -> {
                    if (mItemClickListener != null) {
                        mItemClickListener.onDetailViewClickList(v, position);
                    }
                });

            }else
            {
                viewHolder.contentView.setOnClickListener(null);
            }


            if (!isAnyButtonShown) {
                if (showUfxMultiArea) {
                    viewHolder.ufxMultiBtnArea.setVisibility(View.GONE);
                } else {
                    viewHolder.noneUfxMultiBtnArea.setVisibility(View.GONE);

                }
            }

            if(item.get("eHailTrip").equalsIgnoreCase("Yes"))
            {
                viewHolder.userImageArea.setVisibility(View.GONE);
            }

        } else if (holder instanceof HeaderViewHolder){
            HeaderViewHolder headerHolder = (HeaderViewHolder) holder;
            Map<String,String> map = list.get(position);

            headerHolder.tripsCountTxt.setText(map.get("TripCount"));
            headerHolder.fareTxt.setText(map.get("TotalEarning"));
            headerHolder.avgRatingCalcTxt.setText(map.get("AvgRating"));

            if (map.get("isPast").equalsIgnoreCase("yes")){
                headerHolder.earnedAmountArea.setVisibility(View.VISIBLE);
            }else {
                headerHolder.earnedAmountArea.setVisibility(View.GONE);
            }

        }
        else {
            FooterViewHolder footerHolder = (FooterViewHolder) holder;
            this.footerHolder = footerHolder;
        }




    }

    @Override
    public int getItemViewType(int position) {

        if ((position)==0){
            return TYPE_HEADER;
        }
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

        void onCancelBookingClickList(View v, int position);

        void onTripStartClickList(View v, int position);

        void onViewServiceClickList(View v, int position);

        void onDetailViewClickList(View v, int position);
    }

    // inner class to hold a reference to each item of RecyclerView
    public class ViewHolder extends RecyclerView.ViewHolder {

        public MTextView myBookingNoHTxt;
        public MTextView myBookingNoVTxt;
        public MTextView dateTxt;
        public MTextView timeTxt;
        public MTextView sourceAddressTxt;
        public MTextView destAddressTxt;
        public ImageView imagedest;
        public MTextView statusVTxt;
        public MTextView etypeTxt;
        public MTextView sourceAddressHTxt;
        public MTextView destAddressHTxt;
        public MTextView SelectedTypeNameTxt;
        public LinearLayout statusArea;

        public LinearLayout noneUfxMultiArea;
        public LinearLayout noneUfxMultiBtnArea;
        public LinearLayout cancelBookingArea;
        public MTextView btn_type_cancel;
        public LinearLayout startTripArea;
        public MTextView btn_type_start;
        public LinearLayout viewCancelReasonArea;
        public MTextView btn_type_view_cancel_reason;

        public MTextView userNameTxt;
        public MTextView sAddressTxt;
        public TextView SelectedTypeNameTxt1;
        public SimpleRatingBar ratingBar;
        public CardView typeArea1;
        public CardView typeArea;
        public SelectableRoundedImageView userImgView;
        public ImageView userImageView;
        public LinearLayout fareArea;
        public CardView userImageArea;
        public AutoFitEditText fareTxt;
        public MTextView packageTxt;

        public LinearLayout ufxMultiArea;
        public LinearLayout ufxMultiBtnArea;
        public LinearLayout cancelBookingBtnArea;
        public MTextView cancelBookingBtn;
        public LinearLayout startTripBtnArea;
        public MTextView viewCancelReasonBtn;
        public LinearLayout viewCancelReasonBtnArea;
        public MTextView startTripBtn;
        public LinearLayout viewRequestedServiceBtnArea;
        public MTextView viewRequestedServiceBtn;

        public LinearLayout pickupLocArea, destarea;
        public LinearLayout contentView;
        public DividerView aboveLine;

        public ViewHolder(View view) {
            super(view);

            myBookingNoHTxt = (MTextView) view.findViewById(R.id.myBookingNoHTxt);
            myBookingNoVTxt = (MTextView) view.findViewById(R.id.myBookingNoVTxt);
            dateTxt = (MTextView) view.findViewById(R.id.dateTxt);
            timeTxt = (MTextView) view.findViewById(R.id.timeTxt);
            sourceAddressTxt = (MTextView) view.findViewById(R.id.sourceAddressTxt);
            destAddressTxt = (MTextView) view.findViewById(R.id.destAddressTxt);
            pickupLocArea = (LinearLayout) view.findViewById(R.id.pickupLocArea);
            contentView = (LinearLayout) view.findViewById(R.id.contentView);
            destarea = (LinearLayout) view.findViewById(R.id.destarea);
            imagedest = (ImageView) view.findViewById(R.id.imagedest);
            statusVTxt = (MTextView) view.findViewById(R.id.statusVTxt);
            sourceAddressHTxt = (MTextView) view.findViewById(R.id.sourceAddressHTxt);
            destAddressHTxt = (MTextView) view.findViewById(R.id.destAddressHTxt);
            statusArea = (LinearLayout) view.findViewById(R.id.statusArea);
            SelectedTypeNameTxt = (MTextView) view.findViewById(R.id.SelectedTypeNameTxt);
            aboveLine = (DividerView) view.findViewById(R.id.aboveLine);

            noneUfxMultiArea = (LinearLayout) view.findViewById(R.id.noneUfxMultiArea);
            noneUfxMultiBtnArea = (LinearLayout) view.findViewById(R.id.noneUfxMultiBtnArea);
            startTripArea = (LinearLayout) view.findViewById(R.id.startTripArea);
            btn_type_start = (MTextView) view.findViewById(R.id.btn_type_start);
            cancelBookingArea = (LinearLayout) view.findViewById(R.id.cancelArea);
            btn_type_cancel = (MTextView) view.findViewById(R.id.btn_type_cancel);
            viewCancelReasonArea = (LinearLayout) view.findViewById(R.id.viewCancelReasonArea);
            btn_type_view_cancel_reason = (MTextView) view.findViewById(R.id.btn_type_view_cancel_reason);


            ufxMultiArea = (LinearLayout) view.findViewById(R.id.ufxMultiArea);
            ufxMultiBtnArea = (LinearLayout) view.findViewById(R.id.ufxMultiBtnArea);
            cancelBookingBtnArea = (LinearLayout) view.findViewById(R.id.cancelBookingBtnArea);
            cancelBookingBtn = (MTextView) view.findViewById(R.id.cancelBookingBtn);
            startTripBtnArea = (LinearLayout) view.findViewById(R.id.startTripBtnArea);
            startTripBtn = (MTextView) view.findViewById(R.id.startTripBtn);
            viewCancelReasonBtnArea = (LinearLayout) view.findViewById(R.id.viewCancelReasonBtnArea);
            viewCancelReasonBtn = (MTextView) view.findViewById(R.id.viewCancelReasonBtn);
            viewRequestedServiceBtnArea = (LinearLayout) view.findViewById(R.id.viewRequestedServiceBtnArea);
            viewRequestedServiceBtn = (MTextView) view.findViewById(R.id.viewRequestedServiceBtn);

            userNameTxt = (MTextView) view.findViewById(R.id.userNameTxt);
            sAddressTxt = (MTextView) view.findViewById(R.id.sAddressTxt);
            SelectedTypeNameTxt1 = view.findViewById(R.id.SelectedTypeNameTxt1);
            fareArea = (LinearLayout) view.findViewById(R.id.fareArea);
            userImgView = (SelectableRoundedImageView) view.findViewById(R.id.userImgView);
            userImageView = (ImageView) view.findViewById(R.id.userImageView);
            userImageArea = (CardView) view.findViewById(R.id.userImageArea);
            ratingBar = (SimpleRatingBar) view.findViewById(R.id.ratingBar);
            fareTxt = (AutoFitEditText) view.findViewById(R.id.fareTxt);
            packageTxt = (MTextView) view.findViewById(R.id.packageTxt);
            typeArea1 = (CardView) view.findViewById(R.id.typeArea1);
            typeArea = (CardView) view.findViewById(R.id.typeArea);
        }
    }

    class FooterViewHolder extends RecyclerView.ViewHolder {
        LinearLayout progressArea;

        public FooterViewHolder(View itemView) {
            super(itemView);

            progressArea = (LinearLayout) itemView;

        }
    }


    class HeaderViewHolder extends RecyclerView.ViewHolder {
        LinearLayout earnedAmountArea;
        MTextView earnTitleTxt,fareTxt,tripsCompletedTxt,tripsCountTxt,avgRatingTxt,avgRatingCalcTxt;
        public HeaderViewHolder(View view) {
            super(view);
            earnTitleTxt = (MTextView) view.findViewById(R.id.earnTitleTxt);
            fareTxt = (MTextView) view.findViewById(R.id.fareTxt);
            tripsCompletedTxt = (MTextView) view.findViewById(R.id.tripsCompletedTxt);
            tripsCountTxt = (MTextView) view.findViewById(R.id.tripsCountTxt);
            avgRatingTxt = (MTextView) view.findViewById(R.id.avgRatingTxt);
            avgRatingCalcTxt = (MTextView) view.findViewById(R.id.avgRatingCalcTxt);
            earnedAmountArea = (LinearLayout) view.findViewById(R.id.earnedAmountArea);


            earnTitleTxt.setText(generalFunc.retrieveLangLBl("", "LBL_TOTAL_EARNINGS"));
           tripsCompletedTxt.setText(generalFunc.retrieveLangLBl("Completed Trips", "LBL_TOTAL_SERVICES"));
           avgRatingTxt.setText(generalFunc.retrieveLangLBl("Avg. Rating", "LBL_AVG_RATING"));
        }
    }
}
