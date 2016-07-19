package com.bestapps.jotbudget.ui;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.DialogInterface;

import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import com.squareup.otto.Bus;
import com.bestapps.jotbudget.App;
import com.bestapps.jotbudget.event.ItemCreatedOrUpdated;
import com.bestapps.jotbudget.model.Item;
import com.bestapps.jotbudget.ui.presenter.ItemDetailsPresenter;
import com.bestapps.jotbudget.util.CurrencyUtil;
import com.bestapps.jotbudget.util.DateUtil;
import com.bestapps.jotbudget.util.Util;
import com.bestapps.jotbudget.ui.view.ItemDetailsView;
import com.tr.expenses.R;

import java.util.Calendar;
import java.util.List;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ItemDetailsActivity extends AppCompatActivity implements ItemDetailsView {
    public static final String EXTRA_ITEM_DATA = "EXTRA_ITEM_DATA";
    public static final String EXTRA_ACTIVITY_TYPE = "EXTRA_ACTIVITY_TYPE";

    public static final int ACTIVITY_TYPE_EXPENSE = 0;
    public static final int ACTIVITY_TYPE_INCOME = 1;

    private static final int MAX_AMOUNT_FIELD_LENGTH = 13;

    @Bind(R.id.sub_type_text) TextView mSubTypeTextView;
    @Bind(R.id.date_text) TextView mDateTextView;
    @Bind(R.id.amount_edit_text) EditText mAmountEditText;
    @Bind(R.id.details_edit_text) TextView mDetailsTextView;

    private Item mOriginalItem;
    private int mActivityItemType;

    @Inject ItemDetailsPresenter mItemDetailsPresenter;
    @Inject Bus mBus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_details);

        ButterKnife.bind(this);
        App.getComponent().inject(this);
        mItemDetailsPresenter.bind(this);

        Toolbar toolbar = ButterKnife.findById(this, R.id.details_toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        setupContent();
        setupAmountEditText();


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mItemDetailsPresenter.unbind();
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.date_text)
    void onDateClicked() {
        long previousTimeValue = DateUtil.parse(mDateTextView.getText().toString());
        Util.showDatePicker(mDateTextView.getContext(), new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                Calendar cal = Calendar.getInstance();
                cal.set(year, monthOfYear, dayOfMonth);
                mDateTextView.setText(DateUtil.format(cal.getTimeInMillis()));
            }
        }, previousTimeValue);
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.sub_type_text)
    void onSubtypeClicked(View v) {
        hideKeyboard(v);
        displaySubTypePickerDialog();
    }

    @Override
    public void showSubTypes(String title, List<String> subTypes) {
        displaySubTypePickerDialog(title, subTypes);
    }

    private void setupContent() {
        mOriginalItem = getIntent().getParcelableExtra(EXTRA_ITEM_DATA);
        if (mOriginalItem != null) { // we are passed an item to display
            mDateTextView.setText(DateUtil.format(mOriginalItem.getTimeStamp()));
            mDetailsTextView.setText(mOriginalItem.getDescription());
            mDetailsTextView.setText(mOriginalItem.getDescription());
            mAmountEditText.setText(CurrencyUtil.toUnsignedCurrencyString(mOriginalItem.getAmount()));
            mActivityItemType = mOriginalItem.getType();
            mSubTypeTextView.setText(mOriginalItem.getSubType());
        } else { // we opened in order to create a new item
            mActivityItemType = getIntent().getIntExtra(EXTRA_ACTIVITY_TYPE, -1);
            if (mActivityItemType == ACTIVITY_TYPE_EXPENSE) {
                setTitle(getString(R.string.new_expense));
            } else if (mActivityItemType == ACTIVITY_TYPE_INCOME) {
                setTitle(getString(R.string.new_income));
            } else {
                throw new AssertionError();
            }

            mDateTextView.setText(DateUtil.format(System.currentTimeMillis()));
            mAmountEditText.setText(CurrencyUtil.toUnsignedCurrencyString(0));
        }

        final String sign;
        final int color;
        if (mActivityItemType == Item.TYPE_EXPENSE) {
            color = R.color.accent_color;
            sign = "-";
        } else {
            color = R.color.primary_color;
            sign = "+";
        }

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            getSupportActionBar().setBackgroundDrawable(ContextCompat.getDrawable(this, color));
        }

        int resolvedColor = ContextCompat.getColor(this,color);

        Util.setStatusBarColor(getWindow(),resolvedColor);

        mSubTypeTextView.setTextColor(resolvedColor);
        mAmountEditText.setTextColor(resolvedColor);
        final TextView signTextView = ButterKnife.findById(this, R.id.sign_view);
        signTextView.setText(sign);
        signTextView.setTextColor(resolvedColor);
    }

    private void setupAmountEditText() {
        mAmountEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAmountEditText.setSelection(mAmountEditText.getText().length());
            }
        });

        // http://stackoverflow.com/a/5191860
        mAmountEditText.addTextChangedListener(new TextWatcher() {
            private String current = "";
            private String lastValidAmount;

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().equals(current)) {
                    // disable this listener while performing shenanigans to mAmountEditText so as
                    // not to fall into recursion on setText() call.
                    mAmountEditText.removeTextChangedListener(this);
                    current = CurrencyUtil.sanitize(s.toString());
                    if (current.length() <= MAX_AMOUNT_FIELD_LENGTH) { // ok to go, update the field
                        mAmountEditText.setText(current);
                        lastValidAmount = current;
                    } else { // field is too long
                        mAmountEditText.setText(lastValidAmount);
                        IntrusiveToast.show(ItemDetailsActivity.this, getString(R.string.value_is_too_large));
                    }
                    mAmountEditText.addTextChangedListener(this);
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                mAmountEditText.setSelection(mAmountEditText.length());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_item_details, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_save:
                if (saveItem()) {
                    mBus.post(new ItemCreatedOrUpdated());
                    finish();
                    return true;
                }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void hideKeyboard(View view) {
        InputMethodManager inputMethodManager
                = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private void displaySubTypePickerDialog() {
        mItemDetailsPresenter.loadSubTypes(this, mActivityItemType);
    }

    private void displaySubTypePickerDialog(final String title, List<String> subTypeList) {
        final String other = getString(R.string.other_ellipsized);

        // Add "Other..." item to the end of this list
        int itemArrayLen = subTypeList.size() + 1;
        final String[] subTypes = new String[itemArrayLen];
        subTypeList.toArray(subTypes);
        subTypes[itemArrayLen - 1] = other;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title).setItems(subTypes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                String text = subTypes[which];
                if (text.equals(other)) {
                    displayCustomSubTypePickerDialog();
                } else {
                    mSubTypeTextView.setText(subTypes[which]);
                }
            }
        });
        builder.show();
    }

    private void displayCustomSubTypePickerDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final String title;
        if (mActivityItemType == Item.TYPE_EXPENSE) {
            title = getString(R.string.custom_expense);
        } else {
            title = getString(R.string.custom_income);
        }
        builder.setTitle(title);

        final EditText input = new EditText(this);

        builder.setView(input);
        input.performClick();
        builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mSubTypeTextView.setText(input.getText());
            }
        });
        builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    /**
     * Saves currently displayed item into the database
     */
    private boolean saveItem() {
        String subType = mSubTypeTextView.getText().toString();
        if (subType.isEmpty()) {
            IntrusiveToast.show(this, getString(R.string.please_pick_the_type));
            return false;
        }
        long date = DateUtil.parse(mDateTextView.getText().toString());
        long amount = CurrencyUtil.fromString(mAmountEditText.getText().toString());
        String details = mDetailsTextView.getText().toString();

        final int id;
        if (mOriginalItem != null) {
            id = mOriginalItem.getId();
        } else {
            id = Item.NO_ID;
        }
        final Item item = new Item(mActivityItemType, subType, details, amount, date, id);
        mItemDetailsPresenter.saveItem(item);
        return true;
    }
}
