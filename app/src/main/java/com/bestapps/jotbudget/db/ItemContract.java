package com.bestapps.jotbudget.db;

import android.provider.BaseColumns;

final class ItemContract {
    private ItemContract() {
    }

    static abstract class ItemSubtype implements BaseColumns {
        public static final String TABLE_NAME = "item_type";
        public static final String COLUMN_ITEM_TYPE = "type";
        public static final String COLUMN_SUBTYPE = "subtype";
    }

    static abstract class Item implements BaseColumns {
        public static final String TABLE_NAME = "item";
        public static final String COLUMN_TYPE = "type";
        public static final String COLUMN_SUBTYPE = "subtype";
        public static final String COLUMN_AMOUNT = "amount";
        public static final String COLUMN_TIME_STAMP = "time_stamp";
        public static final String COLUMN_DESCRIPTION = "description";
    }
}
