package com.tr.bnotes;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
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

import com.tr.bnotes.db.ItemDao;
import com.tr.expenses.R;

import butterknife.ButterKnife;


public class ItemListFragment extends Fragment
        implements ItemListAdapter.ItemViewHolder.OnClickListener,
        ItemListAdapter.ItemViewHolder.OnLongClickListener,
        View.OnTouchListener,
        ActionMode.Callback {
    public static final String TAG = ItemListFragment.class.getSimpleName();
    private ItemListAdapter mAdapter;
    private ActionMode mActiveActionMode;

    public interface OnItemListFragmentTouchedListener {
        void onItemListFragmentTouched();
    }

    public interface OnItemListUpdatedListener {
        void onItemListUpdated(int itemCountAfterUpdate);
    }

    private boolean isActionModeActive() {
        return mActiveActionMode != null;
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.item_list_fragment, container, false);
        rootView.setTag(TAG);
        rootView.setOnTouchListener(this);


        final RecyclerView itemListRecyclerView
                = ButterKnife.findById(rootView, R.id.item_list_recycler_view);
        mAdapter = new ItemListAdapter(this, this);
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                readItemDataAndSwapCursor(getActivity());
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                notifyAdapterDataSetChanged();
            }
        }.execute();

        setupRecyclerView(itemListRecyclerView, mAdapter);
        return rootView;
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
                    deleteListItemsAndUpdate(getContext(), selectedPositions);
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
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                readItemDataAndSwapCursor(getActivity());
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                notifyAdapterDataSetChanged();
            }
        }.execute();
    }

    private void readItemDataAndSwapCursor(Context context) {
        Cursor listCursor = ItemDao.readAllOrderedByDate(context);
        mAdapter.swapCursor(listCursor);
    }

    private void deleteListItemsAndUpdate(final Context context,
                                          final SparseBooleanArray selectedPositions) {
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                deleteSelectedPositions(context, selectedPositions);
                readItemDataAndSwapCursor(context);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                notifyAdapterDataSetChanged();
            }
        }.execute();

    }

    private void notifyAdapterDataSetChanged() {
        mAdapter.notifyDataSetChanged();
        notifyActivityAboutItemListUpdate(mAdapter.getItemCount());
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
                ids[i] = String.valueOf(mAdapter.itemId(selectedPositions.keyAt(i)));
            }
        }

        ItemDao.delete(context, ids);
    }

    private static void setupRecyclerView(RecyclerView recyclerView, RecyclerView.Adapter adapter) {
        final Context recyclerViewContext = recyclerView.getContext();
        recyclerView.setLayoutManager(new LinearLayoutManager(recyclerViewContext));
        recyclerView.setAdapter(adapter);
    }
}
