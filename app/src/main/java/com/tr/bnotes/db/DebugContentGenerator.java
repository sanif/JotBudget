package com.tr.bnotes.db;


import android.database.sqlite.SQLiteDatabase;

import com.tr.bnotes.Item;
import com.tr.bnotes.util.DateUtil;
import com.tr.expenses.BuildConfig;

import java.util.Calendar;

public class DebugContentGenerator {
    public static void generateItemList(SQLiteDatabase writableDb) {
        if (BuildConfig.FANCY_DEFAULT_CONTENT_IN_DEBUG) {
            populateFancy(writableDb);
        } else {
            populateDummy(writableDb);
        }
    }

    /**
     * Generates lots of placeholder items.
     */
    private static void populateDummy(SQLiteDatabase writableDb) {
        long now = System.currentTimeMillis();
        Calendar time = Calendar.getInstance();
        time.setTimeInMillis(now);
        for (int i = 0; i < 500; i++) {
            int type = i % 2 == 0 ? Item.TYPE_EXPENSE : Item.TYPE_INCOME;
            long millis = time.getTimeInMillis();
            Item item = new Item(type, "Test #" + i % 100, "#" + i, i * 1000, millis, Item.NO_ID);
            ItemDao.saveItem(writableDb, item);
            if (i != 0 && i % 3 == 0) {
                time.add(Calendar.DAY_OF_YEAR, -10);
            }
        }
    }

    /**
     * Generates reasonable amount of good-looking income / expense items.
     */
    private static void populateFancy(SQLiteDatabase writableDb) {
        long startOfToday = DateUtil.parse(DateUtil.format(System.currentTimeMillis()));
        ItemDao.saveItem(writableDb, new Item(Item.TYPE_EXPENSE,
                "Clothes", "New jeans", 10000, startOfToday, Item.NO_ID));
        ItemDao.saveItem(writableDb, new Item(Item.TYPE_EXPENSE,
                "Flat / House", "Monthly rent", 35000, startOfToday, Item.NO_ID));
        ItemDao.saveItem(writableDb, new Item(Item.TYPE_EXPENSE,
                "Grocery", "Fruits & vegetables", 3567, DateUtil.parse("19/08/2015"), Item.NO_ID));
        ItemDao.saveItem(writableDb, new Item(Item.TYPE_EXPENSE,
                "Fuel", "Full gas tank", 7590, DateUtil.parse("14/08/2015"), Item.NO_ID));
        ItemDao.saveItem(writableDb, new Item(Item.TYPE_EXPENSE,
                "Clothes", "New shirt", 3599, DateUtil.parse("11/08/2015"), Item.NO_ID));
        ItemDao.saveItem(writableDb, new Item(Item.TYPE_EXPENSE,
                "Restaurant", "Family get together", 3691, DateUtil.parse("05/08/2015"), Item.NO_ID));
        ItemDao.saveItem(writableDb, new Item(Item.TYPE_INCOME,
                "Salary", "", 90000, DateUtil.parse("31/07/2015"), Item.NO_ID));
        ItemDao.saveItem(writableDb, new Item(Item.TYPE_EXPENSE,
                "Personal care", "New perfume", 3865, DateUtil.parse("29/07/2015"), Item.NO_ID));
        ItemDao.saveItem(writableDb, new Item(Item.TYPE_EXPENSE,
                "Grocery", "Bottle of milk", 345, DateUtil.parse("26/07/2015"), Item.NO_ID));
    }
}
