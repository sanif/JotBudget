package com.tr.bnotes.util;


import android.app.DatePickerDialog;
import android.content.Context;
import android.os.StrictMode;

import java.io.Closeable;
import java.io.IOException;
import java.util.Calendar;

public class Util {
    private Util() {

    }

    // Show me pre-JDK7 project which does not have this method!
    public static void close(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException ignored) {
            }
        }
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
