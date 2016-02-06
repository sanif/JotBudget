package com.tr.bnotes;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.tr.bnotes.util.Util;
import com.tr.expenses.BuildConfig;
import com.tr.expenses.R;

public class MainActivity extends AppCompatActivity
        implements View.OnClickListener,
        ItemListFragment.OnItemListFragmentTouchedListener,
        ItemListFragment.OnItemListUpdatedListener {
    public static final String TAG = MainActivity.class.getSimpleName();

    private FloatingActionMenu mFloatingActionsMenu;
    private TextView mNoItemsText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (BuildConfig.DEBUG) {
            Util.enableStrictMode();
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mNoItemsText = (TextView) findViewById(R.id.no_items_text);

        setUpFloatingActionsMenu();

        if (savedInstanceState == null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            ItemListFragment fragment = new ItemListFragment();
            transaction.replace(R.id.fragment_container, fragment, ItemListFragment.TAG);
            transaction.commit();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ItemDetailsActivity.REQUEST_CODE) {
            if (resultCode == ItemDetailsActivity.RESULT_CREATED_OR_UPDATED) {
                ItemListFragment itemListFragment
                        = (ItemListFragment) getSupportFragmentManager()
                        .findFragmentByTag(ItemListFragment.TAG);
                if (itemListFragment != null) {
                    itemListFragment.updateView();
                } else {
                    Log.wtf(TAG, "recyclerViewFragment == null");
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onClick(View v) {
        final int id = v.getId();
        switch (id) {
            case R.id.new_expense_fab:
                startItemDetailsActivityForResult(ItemDetailsActivity.ACTIVITY_TYPE_EXPENSE);
                break;
            case R.id.new_income_fab:
                startItemDetailsActivityForResult(ItemDetailsActivity.ACTIVITY_TYPE_INCOME);
                break;
            case R.id.view_stats_fab:
                startStatsActivity();
                break;
            default:
                throw new AssertionError("Unknown id " + id);
        }
        mFloatingActionsMenu.close(false);
    }

    private void setUpFloatingActionsMenu() {
        mFloatingActionsMenu = (FloatingActionMenu) findViewById(R.id.floating_actions_menu);
        FloatingActionButton newExpenseFab = (FloatingActionButton) findViewById(R.id.new_expense_fab);
        FloatingActionButton newIncomeFab = (FloatingActionButton) findViewById(R.id.new_income_fab);
        FloatingActionButton viewStatsFab = (FloatingActionButton) findViewById(R.id.view_stats_fab);
        newExpenseFab.setOnClickListener(this);
        newIncomeFab.setOnClickListener(this);
        viewStatsFab.setOnClickListener(this);
    }

    private void startItemDetailsActivityForResult(int activityType) {
        Intent intent = new Intent(this, ItemDetailsActivity.class);
        intent.putExtra(ItemDetailsActivity.EXTRA_ACTIVITY_TYPE, activityType);
        startActivityForResult(intent, ItemDetailsActivity.REQUEST_CODE);
    }

    private void startStatsActivity() {
        Intent intent = new Intent(this, StatsActivity.class);
        startActivity(intent);
    }

    @Override
    public void onItemListFragmentTouched() {
        // the user is scrolling the fragment content, close the FAM if open.
        if (mFloatingActionsMenu.isOpened()) {
            mFloatingActionsMenu.close(true);
        }
    }

    @Override
    public void onItemListUpdated(int itemCountAfterUpdate) {
        // show placeholder text when there are no items recorded
        if (itemCountAfterUpdate == 0) {
            mNoItemsText.setVisibility(View.VISIBLE);
        } else {
            mNoItemsText.setVisibility(View.GONE);
        }
    }
}
