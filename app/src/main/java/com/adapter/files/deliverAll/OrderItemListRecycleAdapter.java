package com.adapter.files.deliverAll;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;

import com.taxifgo.driver.R;
import com.general.files.GeneralFunctions;
import com.general.files.RoundCornerDrawable;
import com.model.deliverAll.orderItemDetailDataModel;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.utils.Utils;
import com.view.MTextView;
import com.view.SelectableRoundedImageView;

import org.apache.commons.lang3.text.WordUtils;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by Admin on 09-07-2016.
 */
public class OrderItemListRecycleAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_ITEM = 1;
    private static final int TYPE_FOOTER = 2;
    public GeneralFunctions generalFunc;
    ArrayList<orderItemDetailDataModel> subItemList = new ArrayList<>();
    Boolean[] checked_Item_List;
    Context mContext;
    boolean isFooterEnabled = false;
    String isPhotoUploaded = "";
    View footerView;
    FooterViewHolder footerHolder;
    private OnItemClickListener mItemClickListener;
    Drawable selected;
    Drawable unSelected;
    int color = -1;
    Boolean isImageShow = false;

    public OrderItemListRecycleAdapter(Context mContext, ArrayList<orderItemDetailDataModel> subItemList, GeneralFunctions generalFunc, boolean isFooterEnabled, String isPhotoUploaded, Boolean isImageShow) {
        this.mContext = mContext;
        this.subItemList = subItemList;
        this.checked_Item_List = new Boolean[subItemList.size()];
        Arrays.fill(checked_Item_List, false);
        this.generalFunc = generalFunc;
        this.isFooterEnabled = isFooterEnabled;
        this.isPhotoUploaded = isPhotoUploaded;
        setAllItemscheckedAccordingState();

        selected = mContext.getResources().getDrawable(R.drawable.selected_card_view_background);
        unSelected = mContext.getResources().getDrawable(R.drawable.unselected_card_view_background);
        color = mContext.getResources().getColor(R.color.appThemeColor_1);
        this.isImageShow = isImageShow;

    }

    public void setSubItemList(ArrayList<orderItemDetailDataModel> subItemList, String isPhotoUploadPending) {
        this.isPhotoUploaded = isPhotoUploadPending;
        this.checked_Item_List = new Boolean[subItemList.size()];
        setAllItemscheckedAccordingState();
    }

    public void setAllItemscheckedAccordingState() {
        if (Utils.checkText(isPhotoUploaded) && isPhotoUploaded.equalsIgnoreCase("No")) {
            Arrays.fill(checked_Item_List, true);
        } else {
            Arrays.fill(checked_Item_List, false);
        }
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
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.order_item_list_cell, parent, false);
            if (isImageShow) {
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.order_item_list_cell_new, parent, false);

            }

            return new ViewHolder(view);
        }
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {


        if (holder instanceof ViewHolder) {
            final orderItemDetailDataModel item = subItemList.get(position);

            final ViewHolder viewHolder = (ViewHolder) holder;

            Boolean posData = checked_Item_List[position];

//            if (posData == true) {
//                viewHolder.item_list_detail_cv.setBackground(selected);
//            } else {
//                viewHolder.item_list_detail_cv.setBackground(unSelected);
//            }
            viewHolder.item_chkBox.setChecked(posData);
            if (Utils.checkText(item.getItemName())) {
                viewHolder.itemNameTxt.setText(WordUtils.capitalizeFully(item.getItemName()));
            }
            if (Utils.checkText(item.getSubItemName())) {
                viewHolder.subItemNameTxt.setVisibility(View.VISIBLE);
                if (Utils.checkText(item.getSubItemName())) {
                    viewHolder.subItemNameTxt.setText(WordUtils.capitalizeFully(item.getSubItemName()));
                }
            }
            viewHolder.itemQuantityTxt.setText("x  " + generalFunc.convertNumberWithRTL(item.getItemQuantity()));
            viewHolder.itemPriceTxt.setText(generalFunc.convertNumberWithRTL(item.getItemTotalPrice()));


            //discuss with Druvin and comment this code
//            if (item.getTotalDiscountPrice() != null && !item.getTotalDiscountPrice().equals("")) {
//               // viewHolder.itemPriceTxt.setTextColor(mContext.getResources().getColor(R.color.gray));
//
//                SpannableStringBuilder spanBuilder = new SpannableStringBuilder();
//
//               // CharSequence itemPriceTxt = viewHolder.itemPriceTxt.getText();
//
//                SpannableString origSpan = new SpannableString( viewHolder.itemPriceTxt.getText());
//
//                origSpan.setSpan(new StrikethroughSpan(), 0,  viewHolder.itemPriceTxt.getText().length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
//
//                spanBuilder.append(origSpan);
//
////                String priceStr = "\n" + generalFunc.convertNumberWithRTL(item.getTotalDiscountPrice());
////
////                SpannableString discountSpan = new SpannableString(priceStr);
////                discountSpan.setSpan(new ForegroundColorSpan(color), 0, priceStr.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
//               // spanBuilder.append(discountSpan);
//
//                viewHolder.itemPriceTxt.setText(spanBuilder.toString());
//            } else {

            viewHolder.itemPriceTxt.setTextColor(color);
            viewHolder.itemPriceTxt.setPaintFlags(viewHolder.itemPriceTxt.getPaintFlags());
            // }


            viewHolder.item_list_detail_cv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if (!isImageShow) {
                        viewHolder.item_chkBox.performClick();
                    }
                }
            });
            viewHolder.item_chkBox.setOnClickListener(onStateChangedListener(viewHolder.item_chkBox, position));


            if (isImageShow) {
                String vImage = "";
                if (!item.getvImage().equalsIgnoreCase("")) {
                    vImage = item.getvImage();
                } else {
                    vImage = "default";
                }
                Picasso.get()
                        .load(vImage)
                        .placeholder(R.mipmap.ic_no_icon)
                        .error(R.mipmap.ic_no_icon)
                        .into(viewHolder.itemImg, new Callback() {
                            @Override
                            public void onSuccess() {
                                try {
                                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                                        viewHolder.itemImg.invalidate();

                                        BitmapDrawable drawable = (BitmapDrawable) viewHolder.itemImg.getDrawable();
                                        Bitmap bitmap = drawable.getBitmap();
                                        viewHolder.itemArea.setPreventCornerOverlap(false);

                                        RoundCornerDrawable round = new RoundCornerDrawable(bitmap, mContext.getResources().getDimension(R.dimen._10sdp), 0);
                                        viewHolder.itemImg.setVisibility(View.GONE);
                                        viewHolder.itemArea.setBackground(round);
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                            }

                            @Override
                            public void onError(Exception e) {

                            }
                        });

                viewHolder.item_chkBox.setVisibility(View.GONE);
                viewHolder.itemImg.setVisibility(View.VISIBLE);
                viewHolder.itemArea.setVisibility(View.VISIBLE);
            }


        } else {
            FooterViewHolder footerHolder = (FooterViewHolder) holder;
            this.footerHolder = footerHolder;
        }


    }

    private View.OnClickListener onStateChangedListener(final CheckBox checkBox, final int position) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkBox.isChecked()) {
                    checked_Item_List[position] = true;
                } else {
                    checked_Item_List[position] = false;
                }
                notifyDataSetChanged();
            }
        };
    }

    public boolean areAllTrue() {
        for (boolean b : checked_Item_List) if (!b) return false;
        return true;
    }


    @Override
    public int getItemViewType(int position) {
        if (isPositionFooter(position) && isFooterEnabled == true) {
            return TYPE_FOOTER;
        }
        return TYPE_ITEM;
    }

    private boolean isPositionFooter(int position) {
        return position == subItemList.size();
    }

    // Return the size of your itemsData (invoked by the layout manager)
    @Override
    public int getItemCount() {
        if (isFooterEnabled == true) {
            return subItemList.size() + 1;
        } else {
            return subItemList.size();
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


        private CheckBox item_chkBox;
        private MTextView itemNameTxt, subItemNameTxt, itemQuantityTxt, itemPriceTxt;
        private LinearLayout itemList_ll;
        private CardView item_list_detail_cv;
        private CardView itemArea;
        private SelectableRoundedImageView itemImg;

        public ViewHolder(View view) {
            super(view);

            item_chkBox = (CheckBox) view.findViewById(R.id.item_chkBox);
            itemNameTxt = (MTextView) view.findViewById(R.id.itemNameTxt);
            subItemNameTxt = (MTextView) view.findViewById(R.id.subItemNameTxt);
            itemQuantityTxt = (MTextView) view.findViewById(R.id.itemQuantityTxt);
            itemPriceTxt = (MTextView) view.findViewById(R.id.itemPriceTxt);
            itemList_ll = (LinearLayout) view.findViewById(R.id.itemList_ll);
            item_list_detail_cv = (CardView) view.findViewById(R.id.item_list_detail_cv);
            itemArea = (CardView) view.findViewById(R.id.itemArea);
            itemImg = (SelectableRoundedImageView) view.findViewById(R.id.itemImg);


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
