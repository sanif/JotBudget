package com.bestapps.jotbudget.ui;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.bestapps.jotbudget.App;
import com.bestapps.jotbudget.ui.view.ItemListView;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import com.bestapps.jotbudget.event.ItemCreatedOrUpdated;
import com.bestapps.jotbudget.model.Item;
import com.bestapps.jotbudget.ui.presenter.ItemListPresenter;
import com.tr.expenses.R;

import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;


public class ItemListFragment extends Fragment
        implements ItemListAdapter.OnClickListener, ItemListAdapter.OnLongClickListener,
        View.OnTouchListener,
        ActionMode.Callback, ItemListView {
    public static final String TAG = ItemListFragment.class.getSimpleName();

    public interface OnItemListFragmentTouchedListener {
        void onItemListFragmentTouched();
    }

    public interface OnItemListUpdatedListener {
        void onItemListUpdated(int itemCountAfterUpdate);
    }

    private ItemListAdapter mAdapter;
    private ActionMode mActiveActionMode;

    @Inject ItemListPresenter mItemListPresenter;
    @Inject Bus mBus;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.item_list_fragment, container, false);
        rootView.setTag(TAG);
        rootView.setOnTouchListener(this);

        App.getComponent().inject(this);
        mItemListPresenter.bind(this);
        mBus.register(this);

        final RecyclerView itemListRecyclerView
                = ButterKnife.findById(rootView, R.id.item_list_recycler_view);
        mAdapter = new ItemListAdapter(this, this);
        loadItems();
        setupRecyclerView(itemListRecyclerView, mAdapter);
        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mBus.unregister(this);
        mItemListPresenter.unbind();
    }

    @Override
    public void onClick(View v, int adapterPosition) {
        if (!isActionModeActive()) {
            Intent intent = new Intent(v.getContext(), ItemDetailsActivity.class);
            intent.putExtra(ItemDetailsActivity.EXTRA_ITEM_DATA, mAdapter.getItem(adapterPosition));
            startActivity(intent);
        } else {
            boolean shouldSelect = !mAdapter.isSelected(adapterPosition);
            mAdapter.setSelected(adapterPosition, shouldSelect);
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
                    mItemListPresenter.deleteItems(mAdapter.getSelectedIds());
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

    @Override
    public void showItems(List<Item> items) {
        setData(items);
    }

    public void loadItems() {
        mItemListPresenter.loadItems();
    }

    private boolean isActionModeActive() {
        return mActiveActionMode != null;
    }

    private void notifyActivityItemListUpdate(int itemCountAfterUpdate) {
        Activity a = getActivity();
        if (a instanceof OnItemListUpdatedListener) {
            ((OnItemListUpdatedListener) a).onItemListUpdated(itemCountAfterUpdate);
        }
    }

    @Subscribe
    @SuppressWarnings("unused")
    public void onItemCreatedOrUpdated(ItemCreatedOrUpdated event) {
        loadItems();
    }

    private static void setupRecyclerView(RecyclerView recyclerView, RecyclerView.Adapter adapter) {
        final Context recyclerViewContext = recyclerView.getContext();
        recyclerView.setLayoutManager(new LinearLayoutManager(recyclerViewContext));
        recyclerView.setAdapter(adapter);
    }

    private void setData(List<Item> items) {
        mAdapter.setData(items);
        mAdapter.notifyDataSetChanged();
        notifyActivityItemListUpdate(mAdapter.getItemCount());
    }
}
