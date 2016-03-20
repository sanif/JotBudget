package com.tr.bnotes.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.tr.bnotes.Item;
import com.tr.expenses.BuildConfig;

import java.util.Arrays;

class ItemDbHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "items.db";

    // Schema is kept simple and somewhat denormalised.
    private static final String CREATE_ITEM_SUBTYPE = "CREATE TABLE "
            + ItemContract.ItemSubtype.TABLE_NAME + "("
            + ItemContract.ItemSubtype._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + ItemContract.ItemSubtype.COLUMN_ITEM_TYPE + " INTEGER NOT NULL, "
            + ItemContract.ItemSubtype.COLUMN_SUBTYPE + " TEXT NOT NULL UNIQUE);";

    private static final String CREATE_ITEM = "CREATE TABLE "
            + ItemContract.Item.TABLE_NAME + "("
            + ItemContract.Item._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + ItemContract.Item.COLUMN_TYPE + " INTEGER NOT NULL, "
            + ItemContract.Item.COLUMN_SUBTYPE + " TEXT NOT NULL, "
            + ItemContract.Item.COLUMN_AMOUNT + " INTEGER NOT NULL, "
            + ItemContract.Item.COLUMN_TIME_STAMP + " INTEGER NOT NULL, "
            + ItemContract.Item.COLUMN_DESCRIPTION + " TEXT);";

    public ItemDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_ITEM_SUBTYPE);
        db.execSQL(CREATE_ITEM);

        populateItemSubtype(db);

        if (BuildConfig.DEBUG) {
            DebugContentGenerator.generateItemList(db);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    private static void populateItemSubtype(SQLiteDatabase writableDb) {
        final String[] expenseTypes = {"Grocery", "Flat / House", "Personal care", "Clothes",
                "Fuel", "Gym", "Medicine", "Restaurant"};
        final String[] incomeType = {"Salary", "Advance payment", "Performance / End of year bonus",
                "Scholarship"};
        Arrays.sort(expenseTypes);
        Arrays.sort(incomeType);

        insertItemSubtypes(writableDb, Item.TYPE_EXPENSE, expenseTypes);
        insertItemSubtypes(writableDb, Item.TYPE_INCOME, incomeType);
    }

    private static void insertItemSubtypes(SQLiteDatabase writableDb, int type, String[] subtypes) {
        ContentValues values = new ContentValues();
        for (String subtype : subtypes) {
            values.put(ItemContract.ItemSubtype.COLUMN_ITEM_TYPE, type);
            values.put(ItemContract.ItemSubtype.COLUMN_SUBTYPE, subtype);
            writableDb.insertOrThrow(ItemContract.ItemSubtype.TABLE_NAME, null, values);
        }
    }
}