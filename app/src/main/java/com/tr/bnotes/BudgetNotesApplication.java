package com.tr.bnotes;

import android.app.Application;
import android.content.Context;

import com.tr.bnotes.db.DebugContentGenerator;
import com.tr.expenses.BuildConfig;

public class BudgetNotesApplication extends Application {
    private static Context sApplicationContext;

    @Override
    public void onCreate() {
        super.onCreate();
        sApplicationContext = getApplicationContext();
    }

    public static Context getContext() {
        return sApplicationContext;
    }
}
