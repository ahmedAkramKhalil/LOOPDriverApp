package com.adapter.files;

import android.content.Context;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.taxifgo.driver.R;
import com.general.files.GeneralFunctions;
import com.squareup.picasso.Picasso;
import com.utils.Utilities;
import com.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;

public class GalleryImagesRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_ITEM = 1;
    private static final int TYPE_FOOTER = 2;
    public GeneralFunctions generalFunc;
    ArrayList<HashMap<String, String>> list;
    Context mContext;
    boolean isFooterEnabled = false;
    View footerView;
    FooterViewHolder footerHolder;
    private OnItemClickListener mItemClickListener;

    int itemWidth;
    int width;

    public GalleryImagesRecyclerAdapter(Context mContext, ArrayList<HashMap<String, String>> list, GeneralFunctions generalFunc, boolean isFooterEnabled) {
        this.mContext = mContext;
        this.list = list;
        this.generalFunc = generalFunc;
        this.isFooterEnabled = isFooterEnabled;
        itemWidth = (int) (getScreenDPWidth() / getNumOfColumns());
        width = Utils.dipToPixels(mContext, itemWidth) - Utils.dipToPixels(mContext, getNumOfColumns() * 10);
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
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_gallery_list, parent, false);
            return new ViewHolder(view);
        }

    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {


        if (holder instanceof ViewHolder) {
            final HashMap<String, String> item = list.get(position);

            final ViewHolder viewHolder = (ViewHolder) holder;

//            viewHolder.contentView.setMinimumHeight(width);

            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) viewHolder.contentAreaView.getLayoutParams();

            params.width = width;
            params.height = width;

            viewHolder.contentAreaView.setLayoutParams(params);

            viewHolder.deleteImgView.setVisibility(View.GONE);

            viewHolder.deleteImgView.setOnClickListener(view -> {
                if (mItemClickListener != null) {
                    mItemClickListener.onDeleteClick(view, position);
                }
            });

            viewHolder.seperatorView.setVisibility(View.GONE);
            if (position == list.size() - 1) {
                viewHolder.seperatorView.setVisibility(View.VISIBLE);
            }

            Picasso.get()
                    .load(Utilities.getResizeImgURL(mContext, item.get("vImage"), params.width, params.height))
                    .placeholder(R.mipmap.ic_no_icon).error(R.mipmap.ic_no_icon)
                    .into(viewHolder.galleryImgView, new com.squareup.picasso.Callback() {
                        @Override
                        public void onSuccess() {
                            viewHolder.deleteImgView.setVisibility(View.VISIBLE);
                        }

                        @Override
                        public void onError(Exception e) {
                            viewHolder.deleteImgView.setVisibility(View.VISIBLE);
                        }
                    });

            viewHolder.galleryImgView.setOnClickListener(view -> {
                if (mItemClickListener != null) {
                    mItemClickListener.onItemClickList(view, position);
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


    public interface OnItemClickListener {
        void onItemClickList(View v, int position);

        void onDeleteClick(View v, int position);
    }

    // inner class to hold a reference to each item of RecyclerView
    public class ViewHolder extends RecyclerView.ViewHolder {

        public AppCompatImageView galleryImgView;
        public AppCompatImageView deleteImgView;

        public View contentView;
        public View contentAreaView;
        public View seperatorView;

        public ViewHolder(View view) {
            super(view);
            contentView = view;
            seperatorView = view.findViewById(R.id.seperatorView);
            contentAreaView = view.findViewById(R.id.contentAreaView);
            galleryImgView = (AppCompatImageView) view.findViewById(R.id.galleryImgView);
            deleteImgView = (AppCompatImageView) view.findViewById(R.id.deleteImgView);

        }
    }

    class FooterViewHolder extends RecyclerView.ViewHolder {
        LinearLayout progressContainer;

        public FooterViewHolder(View itemView) {
            super(itemView);

            progressContainer = (LinearLayout) itemView.findViewById(R.id.progressContainer);

        }
    }

    public float getScreenDPWidth() {
        DisplayMetrics displayMetrics = mContext.getResources().getDisplayMetrics();
        float dpWidth = (displayMetrics.widthPixels - Utils.dipToPixels(mContext, 10)) / displayMetrics.density; // 10 is for recycler view left-right margin
        return dpWidth;
    }

    public Integer getNumOfColumns() {
        int noOfColumns = (int) (getScreenDPWidth() / 130);
        return noOfColumns;
    }
}
