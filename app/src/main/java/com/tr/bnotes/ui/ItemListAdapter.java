package com.tr.bnotes.ui;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.tr.bnotes.model.Item;
import com.tr.bnotes.util.CurrencyUtil;
import com.tr.bnotes.util.DateUtil;
import com.tr.expenses.R;

import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ItemListAdapter extends RecyclerView.Adapter<ItemListAdapter.ItemViewHolder> {
    public interface OnLongClickListener {
        void onLongClick(View v, int adapterPosition);
    }

    public interface OnClickListener {
        void onClick(View v, int adapterPosition);
    }

    private List<Item> mItemList = Collections.emptyList();
    private final OnClickListener mOnClickListener;
    private final OnLongClickListener mOnLongClickListener;

    // calendars are expensive to re-create in terms of allocation so we keep 'em cached
    private final Calendar mToday = Calendar.getInstance();
    private final Calendar mDateCal = Calendar.getInstance();

    private SparseBooleanArray mSelectedItems = new SparseBooleanArray();

    public ItemListAdapter(OnClickListener onClickListener,
                           OnLongClickListener onLongClickListener) {
        mOnClickListener = onClickListener;
        mOnLongClickListener = onLongClickListener;
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item, viewGroup, false);
        return new ItemViewHolder(v, mOnClickListener, mOnLongClickListener);
    }

    @Override
    public void onBindViewHolder(ItemViewHolder itemViewHolder, final int position) {

        final Item item = mItemList.get(position);
        itemViewHolder.detailsTextView.setText(item.getDescription());
        itemViewHolder.headerTextView.setText(item.getSubType());

        mDateCal.setTimeInMillis(item.getTimeStamp());
        itemViewHolder.dateTextView.setText(DateUtil.formatForUI(mToday, mDateCal));

        if (isSelected(position)) {
            itemViewHolder.itemLayout.setActivated(true);
        } else {
            itemViewHolder.itemLayout.setActivated(false);
        }

        final String amount = CurrencyUtil.toUnsignedCurrencyString(item.getAmount());
        final int imageDrawableResId;
        final int imageColorFilter;
        final String moneyAmountText;
        final int moneyAmountTextColor;

        final int itemType = item.getType();

        final Context itemImageContext = itemViewHolder.itemImage.getContext();
        final Context moneyAmountViewContext = itemViewHolder.moneyAmountTextView.getContext();
        if (itemType == Item.TYPE_EXPENSE) {
            imageDrawableResId = R.drawable.ic_trending_down_black_48dp;
            imageColorFilter = ContextCompat.getColor(itemImageContext, R.color.accent_color);
            moneyAmountText = "-" + amount;
            moneyAmountTextColor = ContextCompat.getColor(moneyAmountViewContext, R.color.accent_color);
        } else if (itemType == Item.TYPE_INCOME) {
            imageDrawableResId = R.drawable.ic_trending_up_black_48dp;
            imageColorFilter = ContextCompat.getColor(itemImageContext, R.color.primary_color);
            moneyAmountText = "+" + amount;
            moneyAmountTextColor = ContextCompat.getColor(moneyAmountViewContext, R.color.primary_color);
        } else {
            throw new AssertionError("Income type not supported: " + itemType);
        }

        Glide.with(itemViewHolder.itemImage.getContext())
                .load(imageDrawableResId)
                .into(itemViewHolder.itemImage);
        itemViewHolder.itemImage.setColorFilter(imageColorFilter);
        itemViewHolder.moneyAmountTextView.setText(moneyAmountText);
        itemViewHolder.moneyAmountTextView.setTextColor(moneyAmountTextColor);
    }

    @Override
    public int getItemCount() {
        return mItemList.size();
    }

    public Item getItem(int position) {
        return mItemList.get(position);
    }

    public void setData(List<Item> itemList) {
        if (itemList == mItemList) {
            return;
        }
        mItemList = itemList;
        clearSelected();
        // Update today's date if the last item list was was set on a previous day (in case somebody
        // is using the app @ midnight for example)
        mToday.setTimeInMillis(System.currentTimeMillis());
    }

    public void setSelected(int position, boolean selected) {
        if (selected) {
            mSelectedItems.put(position, true);
        } else {
            mSelectedItems.delete(position);
        }
        notifyItemChanged(position);
    }

    public boolean hasItemsSelected() {
        return mSelectedItems.size() > 0;
    }

    public void clearSelected() {
        if (hasItemsSelected()) {
            mSelectedItems.clear();
            notifyDataSetChanged();
        }
    }

    public String[] getSelectedIds() {
        final String[] ids = new String[mSelectedItems.size()];
        for (int i = 0; i < mSelectedItems.size(); i++) {
            boolean selected = mSelectedItems.valueAt(i);
            if (selected) {
                ids[i] = String.valueOf(getItem(mSelectedItems.keyAt(i)).getId());
            }
        }
        return ids;
    }

    public boolean isSelected(int position) {
        return mSelectedItems.get(position);
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener, View.OnLongClickListener {
        @Bind(R.id.item_details_text) TextView detailsTextView;
        @Bind(R.id.item_type_text) TextView headerTextView;
        @Bind(R.id.item_image) ImageView itemImage;
        @Bind(R.id.item_date_text) TextView dateTextView;
        @Bind(R.id.amount_text) TextView moneyAmountTextView;
        @Bind(R.id.item_layout) LinearLayout itemLayout;

        private final OnClickListener onClickListener;
        private final OnLongClickListener onLongClickListener;
        private final View rootView;

        public ItemViewHolder(View v, OnClickListener onClickListener,
                              OnLongClickListener onLongClickListener) {
            super(v);
            ButterKnife.bind(this, v);

            rootView = v;
            rootView.setOnClickListener(this);
            rootView.setOnLongClickListener(this);
            this.onClickListener = onClickListener;
            this.onLongClickListener = onLongClickListener;
        }

        @Override
        public void onClick(View v) {
            onClickListener.onClick(v, getAdapterPosition());
        }

        @Override
        public boolean onLongClick(View v) {
            onLongClickListener.onLongClick(v, getAdapterPosition());
            return true;
        }
    }
}
