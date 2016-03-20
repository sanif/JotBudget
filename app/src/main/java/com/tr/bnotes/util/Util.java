package com.tr.bnotes.util;


import android.app.DatePickerDialog;
import android.content.Context;
import android.os.StrictMode;

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
}
