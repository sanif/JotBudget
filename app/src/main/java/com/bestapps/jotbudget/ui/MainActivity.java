package com.bestapps.jotbudget.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.github.clans.fab.FloatingActionMenu;
import com.bestapps.jotbudget.App;
import com.bestapps.jotbudget.event.ItemCreatedOrUpdated;
import com.bestapps.jotbudget.util.Util;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import com.tr.expenses.BuildConfig;
import com.tr.expenses.R;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity
        implements ItemListFragment.OnItemListFragmentTouchedListener,
        ItemListFragment.OnItemListUpdatedListener {
    public static final String TAG = MainActivity.class.getSimpleName();

    @Bind(R.id.floating_actions_menu)
    FloatingActionMenu mFloatingActionsMenu;
    @Bind(R.id.no_items_text)
    TextView mNoItemsText;

    @Inject
    Bus mBus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        App.getComponent().inject(this);
        ButterKnife.bind(this);
        mBus.register(this);

        if (BuildConfig.DEBUG) {
            Util.enableStrictMode();
        }

        Toolbar toolbar = ButterKnife.findById(this, R.id.toolbar);
        setSupportActionBar(toolbar);

        if (savedInstanceState == null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            ItemListFragment fragment = new ItemListFragment();
            transaction.replace(R.id.fragment_container, fragment, ItemListFragment.TAG);
            transaction.commit();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBus.unregister(this);
    }

    @SuppressWarnings("unused")
    @OnClick({R.id.new_expense_fab, R.id.new_income_fab, R.id.view_stats_fab})
    void onFabClicked(View v) {
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

    @Subscribe
    @SuppressWarnings("unused")
    public void onItemCreatedOrUpdated(ItemCreatedOrUpdated event) {
        ItemListFragment itemListFragment
                = (ItemListFragment) getSupportFragmentManager()
                .findFragmentByTag(ItemListFragment.TAG);
        if (itemListFragment != null) {
            itemListFragment.loadItems();
        } else {
            Log.wtf(TAG, "recyclerViewFragment == null");
        }
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


    private void startItemDetailsActivityForResult(int activityType) {
        Intent intent = new Intent(this, ItemDetailsActivity.class);
        intent.putExtra(ItemDetailsActivity.EXTRA_ACTIVITY_TYPE, activityType);
        startActivity(intent);
    }

    private void startStatsActivity() {
        Intent intent = new Intent(this, StatsActivity.class);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_status:
                startStatsActivity();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
