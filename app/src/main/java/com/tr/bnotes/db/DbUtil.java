package com.tr.bnotes.db;


import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;

import com.squareup.sqlbrite.BriteDatabase;
import com.squareup.sqlbrite.SqlBrite;
import com.tr.bnotes.BudgetNotesApplication;

import java.util.Collections;

import rx.schedulers.Schedulers;

class DbUtil {
    private static BriteDatabase sBriteDatabase;
    private static SQLiteOpenHelper sSQLiteOpenHelper;
    private DbUtil() {

    }
    static String makePlaceholders(int count) {
        if (count <= 0) {
            throw new IllegalArgumentException("count <= 0");
        }
        return TextUtils.join(",", Collections.nCopies(count, "?"));
    }

    static synchronized SQLiteOpenHelper getsSQLiteOpenHelper() {
        if (sSQLiteOpenHelper == null) {
            sSQLiteOpenHelper = new ItemDbHelper(BudgetNotesApplication.getContext());
        }
        return sSQLiteOpenHelper;
    }

    static synchronized BriteDatabase getSqlBriteDb() {
        if (sBriteDatabase == null) {
            sBriteDatabase = SqlBrite.create()
                    .wrapDatabaseHelper(getsSQLiteOpenHelper(), Schedulers.immediate());
        }
        return sBriteDatabase;
    }
}
