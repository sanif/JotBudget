package com.bestapps.jotbudget.ui;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TextView;

import com.bestapps.jotbudget.util.DateUtil;
import com.bestapps.jotbudget.App;
import com.bestapps.jotbudget.model.ConsolidatedStatement;
import com.bestapps.jotbudget.ui.presenter.StatsPresenter;
import com.bestapps.jotbudget.util.CurrencyUtil;
import com.bestapps.jotbudget.util.PieChartUtil;
import com.bestapps.jotbudget.util.Util;
import com.bestapps.jotbudget.ui.view.StatsView;
import com.tr.expenses.R;

import java.util.Calendar;
import java.util.Map;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import lecho.lib.hellocharts.view.PieChartView;

public class StatsActivity extends AppCompatActivity implements StatsView {

    private static final int TAB_POSITION_INCOME = 0;
    private static final int TAB_POSITION_EXPENSE = 1;
    private static final String FROM_DATE_KEY = "from_date";
    private static final String TO_DATE_KEY = "to_date";

    @Bind(R.id.chart) PieChartView mChart;
    @Bind(R.id.from_date_text) TextView mFromDateView;
    @Bind(R.id.to_date_text) TextView mToDateView;
    @Bind(R.id.income_amount_text) TextView mIncomeView;
    @Bind(R.id.expense_amount_text) TextView mExpenseView;
    @Bind(R.id.no_chart_data_text) TextView mNoChartDataTextView;
    @Bind(R.id.margin_text) TextView mMargin;

    private int[] mExpenseColors;
    private int[] mIncomeColors;

    private int mPrimaryColor;
    private int mAccentColor;
    private int mSecondaryTextColor;

    private ConsolidatedStatement mConsolidatedStatement;
    private int mSelectedTabPosition;

    @Inject StatsPresenter mStatsPresenter;

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        long toDate = DateUtil.parse(mToDateView.getText().toString());
        long fromDate = DateUtil.parse(mFromDateView.getText().toString());
        outState.putLong(TO_DATE_KEY, toDate);
        outState.putLong(FROM_DATE_KEY, fromDate);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);
        ButterKnife.bind(this);
        App.getComponent().inject(this);
        mStatsPresenter.bind(this);
        initColors();

        Toolbar toolbar = ButterKnife.findById(this, R.id.stats_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (savedInstanceState != null) {
            long toDate = savedInstanceState.getLong(TO_DATE_KEY);
            long fromDate = savedInstanceState.getLong(FROM_DATE_KEY);
            mToDateView.setText(DateUtil.format(toDate));
            mFromDateView.setText(DateUtil.format(fromDate));
        } else {
            long now = System.currentTimeMillis();
            mFromDateView.setText(DateUtil.format(now));
            mToDateView.setText(DateUtil.format(now));
        }

        PieChartUtil.initChart(mChart);

        setupTabLayout();

        refreshDisplayedData();

        Util.setStatusBarColor(getWindow(),ContextCompat.getColor(this,R.color.dark_blue));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mStatsPresenter.unbind();
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.from_date_text)
    void onFromDateTextClicked(View v) {
        long previousTimeValue = DateUtil.parse(mFromDateView.getText().toString());
        Util.showDatePicker(v.getContext(), new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                long newDateMillis = millisFromCalendarDate(year, monthOfYear, dayOfMonth);
                updateDateView(mFromDateView, newDateMillis);
                long toDate = DateUtil.parse(mToDateView.getText().toString());
                long fromDate = DateUtil.parse(mFromDateView.getText().toString());
                if (fromDate > toDate) {
                    updateDateView(mToDateView, newDateMillis);
                }
                refreshDisplayedData();
            }
        }, previousTimeValue);
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.to_date_text)
    void onToDateTextClicked(View v) {
        long previousTimeValue = DateUtil.parse(mToDateView.getText().toString());
        Util.showDatePicker(v.getContext(), new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                long newDateMillis = millisFromCalendarDate(year, monthOfYear, dayOfMonth);
                updateDateView(mToDateView, newDateMillis);
                // check if we are before the fromdate
                long toDate = DateUtil.parse(mToDateView.getText().toString());
                long fromDate = DateUtil.parse(mFromDateView.getText().toString());
                if (toDate < fromDate) {
                    updateDateView(mFromDateView, newDateMillis);
                }
                refreshDisplayedData();
            }
        }, previousTimeValue);
    }

    @Override
    public void showConsolidatedStatement(ConsolidatedStatement consolidatedStatement) {
        mConsolidatedStatement = consolidatedStatement;
        displayStatementData();
        displayChart();
    }

    private void setupTabLayout() {
        final TabLayout tabLayout = ButterKnife.findById(this, R.id.tabs);
        tabLayout.addTab(tabLayout.newTab().setText(getResources().getString(R.string.income_chart)),
                TAB_POSITION_INCOME);
        tabLayout.addTab(tabLayout.newTab().setText(getResources().getString(R.string.expense_chart)),
                TAB_POSITION_EXPENSE);
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == TAB_POSITION_EXPENSE) {
                    mSelectedTabPosition = TAB_POSITION_EXPENSE;
                    tabLayout.setSelectedTabIndicatorColor(mAccentColor);
                } else {
                    mSelectedTabPosition = TAB_POSITION_INCOME;
                    tabLayout.setSelectedTabIndicatorColor(mPrimaryColor);
                }
                displayChart();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
    }

    private void refreshDisplayedData() {
        final long from = DateUtil.parse(mFromDateView.getText().toString());
        final long to = DateUtil.parse(mToDateView.getText().toString());
        mStatsPresenter.loadConsolidatedStatement(from, to);
    }

    private void displayChart() {
        if (mSelectedTabPosition == TAB_POSITION_EXPENSE) {
            String noExpenseString = getResources().getString(R.string.no_expenses_in_this_period);
            displayChart(mConsolidatedStatement.getExpenses(), mExpenseColors, noExpenseString);
        } else {
            String noIncomeString = getResources().getString(R.string.no_income_in_this_period);
            displayChart(mConsolidatedStatement.getIncomes(), mIncomeColors, noIncomeString);
        }
    }

    private void displayChart(Map<String, Long> values, int[] colors, String noValueString) {
        if (values.size() == 0) {
            mNoChartDataTextView.setText(noValueString);
            mChart.setVisibility(View.GONE);
            mNoChartDataTextView.setVisibility(View.VISIBLE);
        } else {
            PieChartUtil.setData(mChart, values, colors);
            mNoChartDataTextView.setVisibility(View.GONE);
            mChart.setVisibility(View.VISIBLE);
        }
    }

    private void displayStatementData() {
        final int marginViewColor;
        final long margin = mConsolidatedStatement.getMargin();
        if (margin > 0) {
            marginViewColor = mPrimaryColor;
        } else if (margin < 0) {
            marginViewColor = mAccentColor;
        } else {
            marginViewColor = mSecondaryTextColor;
        }

        mMargin.setTextColor(marginViewColor);
        mIncomeView.setText(CurrencyUtil.toUnsignedCurrencyString(mConsolidatedStatement.getTotalIncome()));
        mExpenseView.setText(CurrencyUtil.toUnsignedCurrencyString(mConsolidatedStatement.getTotalExpense()));
        mMargin.setText(CurrencyUtil.toSignedCurrencyString(margin));
    }

    private void initColors() {
        mPrimaryColor = ContextCompat.getColor(StatsActivity.this, R.color.primary_color);
        mAccentColor = ContextCompat.getColor(StatsActivity.this, R.color.accent_color);
        mSecondaryTextColor = ContextCompat.getColor(StatsActivity.this, R.color.text_secondary);

        mExpenseColors = new int[]{
                ContextCompat.getColor(this, R.color.expense_graph_color_1),
                ContextCompat.getColor(this, R.color.expense_graph_color_2),
                ContextCompat.getColor(this, R.color.expense_graph_color_3),
                ContextCompat.getColor(this, R.color.expense_graph_color_4),
                ContextCompat.getColor(this, R.color.expense_graph_color_5)
        };

        mIncomeColors = new int[]{
                ContextCompat.getColor(this, R.color.income_graph_color_1),
                ContextCompat.getColor(this, R.color.income_graph_color_2),
                ContextCompat.getColor(this, R.color.income_graph_color_3),
                ContextCompat.getColor(this, R.color.income_graph_color_4),
                ContextCompat.getColor(this, R.color.income_graph_color_5)
        };
    }

    private static void updateDateView(TextView dateView, long date) {
        dateView.setText(DateUtil.format(date));
    }

    private static long millisFromCalendarDate(int year, int monthOfYear, int dayOfMonth) {
        Calendar newDate = Calendar.getInstance();
        newDate.set(year, monthOfYear, dayOfMonth);
        return newDate.getTimeInMillis();
    }
}
