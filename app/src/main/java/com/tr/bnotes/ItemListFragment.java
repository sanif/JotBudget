package com.tr.bnotes;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.tr.bnotes.db.ItemManager;
import com.tr.bnotes.util.RxUtil;
import com.tr.expenses.R;

import java.util.List;

import butterknife.ButterKnife;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;


public class ItemListFragment extends Fragment
        implements ItemListAdapter.OnClickListener, ItemListAdapter.OnLongClickListener,
        View.OnTouchListener,
        ActionMode.Callback {
    public static final String TAG = ItemListFragment.class.getSimpleName();

    public interface OnItemListFragmentTouchedListener {
        void onItemListFragmentTouched();
    }

    public interface OnItemListUpdatedListener {
        void onItemListUpdated(int itemCountAfterUpdate);
    }

    private ItemListAdapter mAdapter;
    private ActionMode mActiveActionMode;

    private final CompositeSubscription mCompositeSubscription = new CompositeSubscription();

    private boolean isActionModeActive() {
        return mActiveActionMode != null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.item_list_fragment, container, false);
        rootView.setTag(TAG);
        rootView.setOnTouchListener(this);


        final RecyclerView itemListRecyclerView
                = ButterKnife.findById(rootView, R.id.item_list_recycler_view);
        mAdapter = new ItemListAdapter(this, this);
        updateView();
        setupRecyclerView(itemListRecyclerView, mAdapter);
        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        RxUtil.unsubscribe(mCompositeSubscription);
    }

    @Override
    public void onClick(View v, int adapterPosition) {
        if (!isActionModeActive()) {
            Intent intent = new Intent(v.getContext(), ItemDetailsActivity.class);
            intent.putExtra(ItemDetailsActivity.EXTRA_ITEM_DATA, mAdapter.getItem(adapterPosition));
            startActivityForResult(intent, ItemDetailsActivity.REQUEST_CODE);
        } else {
            boolean shouldSelect = !mAdapter.isSelected(adapterPosition);
            mAdapter.setSelected(adapterPosition, shouldSelect);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ItemDetailsActivity.REQUEST_CODE) {
            if (resultCode == ItemDetailsActivity.RESULT_CREATED_OR_UPDATED) {
                updateView();
            }
        }
    }

    @Override
    public void onLongClick(View v, int adapterPosition) {
        mAdapter.setSelected(adapterPosition, true);
        mActiveActionMode = getActivity().startActionMode(this);
    }


    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        mActiveActionMode = mode;
        getActivity().getMenuInflater().inflate(R.menu.menu_context, menu);
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        switch (item.getItemId()) {
            case R.id.done:
                mActiveActionMode.finish();
                return true;
            case R.id.delete:
                if (mAdapter.hasItemsSelected()) {
                    SparseBooleanArray selectedPositions = mAdapter.drainSelectedPositions();
                    deleteSelectedPositions(getContext(), selectedPositions);
                }
                mActiveActionMode.finish();
                return true;
            default:
                mActiveActionMode.finish();
                return true;
        }
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        mActiveActionMode = null;
        mAdapter.clearSelected();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        Activity a = getActivity();
        if (a instanceof OnItemListFragmentTouchedListener) {
            ((OnItemListFragmentTouchedListener) a).onItemListFragmentTouched();
        }
        return false;
    }

    public void updateView() {
        Subscription sub = ItemManager.readItems()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(new Action1<List<Item>>() {
                @Override
                public void call(List<Item> items) {
                    setData(items);
                }
            });
        mCompositeSubscription.add(sub);
    }

    private void notifyActivityAboutItemListUpdate(int itemCountAfterUpdate) {
        Activity a = getActivity();
        if (a instanceof OnItemListUpdatedListener) {
            ((OnItemListUpdatedListener) a).onItemListUpdated(itemCountAfterUpdate);
        }
    }

    private synchronized void deleteSelectedPositions(Context context,
                                                      SparseBooleanArray selectedPositions) {
        if (selectedPositions.size() <= 0) {
            throw new IllegalArgumentException("selectedPositions.size() <= 0");
        }

        final String[] ids = new String[selectedPositions.size()];
        for (int i = 0; i < selectedPositions.size(); i++) {
            boolean selected = selectedPositions.valueAt(i);
            if (selected) {
                ids[i] = String.valueOf(mAdapter.getItem(selectedPositions.keyAt(i)).getId());
            }
        }

        Subscription sub = ItemManager.deleteItems(ids)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(new Action1<List<Item>>() {
                @Override
                public void call(List<Item> items) {
                    setData(items);
                }
            });
        mCompositeSubscription.add(sub);

    }

    private void setData(List<Item> items) {
        mAdapter.setData(items);
        mAdapter.notifyDataSetChanged();
        notifyActivityAboutItemListUpdate(mAdapter.getItemCount());
    }

    private static void setupRecyclerView(RecyclerView recyclerView, RecyclerView.Adapter adapter) {
        final Context recyclerViewContext = recyclerView.getContext();
        recyclerView.setLayoutManager(new LinearLayoutManager(recyclerViewContext));
        recyclerView.setAdapter(adapter);
    }
}
