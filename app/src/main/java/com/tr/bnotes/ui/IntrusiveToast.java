package com.tr.bnotes.ui;

import android.content.Context;
import android.widget.Toast;

import java.lang.ref.WeakReference;

public class IntrusiveToast {
    private static WeakReference<Toast> sPreviousToast = new WeakReference<>(null);

    /**
     * Shows the toast message immediately, cancelling any previously shown toast.
     */
    public static synchronized void show(Context context, String msg) {
        Toast toast = Toast.makeText(context, msg, Toast.LENGTH_SHORT);
        Toast previousToast = sPreviousToast.get();
        if (previousToast != null) {
            previousToast.cancel();
        }

        sPreviousToast = new WeakReference<>(toast);
        toast.show();
    }
}
