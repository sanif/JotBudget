package com.bestapps.jotbudget.util;


import android.app.DatePickerDialog;
import android.content.Context;
import android.os.Build;
import android.os.StrictMode;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.view.Window;
import android.view.WindowManager;

import java.util.Calendar;

public final class Util {
    private Util() {

    }

    public static void showDatePicker(Context context, DatePickerDialog.OnDateSetListener listener, long time) {
        Calendar date = Calendar.getInstance();
        date.setTimeInMillis(time);
        DatePickerDialog datePickerDialog = new DatePickerDialog(context, listener,
                date.get(Calendar.YEAR), date.get(Calendar.MONTH), date.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    public static void enableStrictMode() {
        StrictMode.setThreadPolicy(
                new StrictMode.ThreadPolicy.Builder()
                        .detectDiskReads()
                        .detectDiskWrites()
                        .penaltyLog()
                        .build()
        );
    }

    public static void setStatusBarColor(Window window, @ColorInt int color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // clear FLAG_TRANSLUCENT_STATUS flag:
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

// add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

// finally change the color
            window.setStatusBarColor(color);
        }
    }
}
