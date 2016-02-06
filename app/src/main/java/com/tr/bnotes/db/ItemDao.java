package com.tr.bnotes.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.tr.bnotes.ConsolidatedStatement;
import com.tr.bnotes.Item;
import com.tr.bnotes.util.Util;

public class ItemDao {
    /**
     * Opaque class, cached by the client and provided along with the cursor in order to avoid
     * requerying column indices for the same cursor
     */
    public static class ColumnIndices {
        private final int mIdIdx;
        private final int mTypeColumnIdx;
        private final int mSubtypeColumnIdx;
        private final int mAmountColumnIdx;
        private final int mTimeStampColumnIdx;
        private final int mDescriptionColumnIdx;

        public ColumnIndices(Cursor itemCursor) {
            mIdIdx = itemCursor.getColumnIndexOrThrow(ItemContract.Item._ID);
            mTypeColumnIdx = itemCursor.getColumnIndexOrThrow(ItemContract.Item.COLUMN_TYPE);
            mSubtypeColumnIdx = itemCursor.getColumnIndexOrThrow(ItemContract.Item.COLUMN_SUBTYPE);
            mAmountColumnIdx = itemCursor.getColumnIndexOrThrow(ItemContract.Item.COLUMN_AMOUNT);
            mTimeStampColumnIdx = itemCursor.getColumnIndexOrThrow(ItemContract.Item.COLUMN_TIME_STAMP);
            mDescriptionColumnIdx = itemCursor.getColumnIndexOrThrow(ItemContract.Item.COLUMN_DESCRIPTION);
        }
    }

    private static final String[] SUBTYPE_COLUMN_SELECTION
            = new String[]{ItemContract.ItemSubtype.COLUMN_SUBTYPE};

    private static final String[] STATEMENT_PROJECTION = new String[]{
            ItemContract.Item.COLUMN_AMOUNT,
            ItemContract.Item.COLUMN_TYPE,
            ItemContract.Item.COLUMN_SUBTYPE
    };

    private ItemDao() {
    }

    public static String[] getExpenseTypes(Context context, String... customTypes) {
        return getSubtypesOfType(context, ItemContract.ITEM_TYPE_EXPENSE, customTypes);
    }

    public static String[] getIncomeTypes(Context context, String... customTypes) {
        return getSubtypesOfType(context, ItemContract.ITEM_TYPE_INCOME, customTypes);
    }

    public static void putItemIntoDb(Context context, Item item) {
        ItemReaderDbHelper helper = new ItemReaderDbHelper(context);
        SQLiteDatabase writableDatabase = helper.getWritableDatabase();
        putItemIntoDb(writableDatabase, item);
        writableDatabase.close();
    }

    // reads all the items ordered by date desc
    public static Cursor readAllOrderedByDate(Context context) {
        ItemReaderDbHelper dbHelper = new ItemReaderDbHelper(context);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        return db.query(ItemContract.Item.TABLE_NAME, null, null, null, null, null,
                ItemContract.Item.COLUMN_TIME_STAMP + " DESC");
    }

    public static ConsolidatedStatement readConsolidatedStatementForPeriod(
            Context context, long startTime, long endTime) {
        ConsolidatedStatement.Builder builder = new ConsolidatedStatement.Builder();
        final String[] selectionArgs = new String[]{String.valueOf(startTime), String.valueOf(endTime)};
        ItemReaderDbHelper dbHelper = new ItemReaderDbHelper(context);
        SQLiteDatabase db = null;
        Cursor result = null;
        try {
            db = dbHelper.getReadableDatabase();
            final String timeStampCol = ItemContract.Item.COLUMN_TIME_STAMP;
            result = db.query(ItemContract.Item.TABLE_NAME, STATEMENT_PROJECTION,
                    timeStampCol + ">=? and " + timeStampCol + "<=?", selectionArgs, null, null, null);
            int amountIdx = result.getColumnIndexOrThrow(ItemContract.Item.COLUMN_AMOUNT);
            int typeIdx = result.getColumnIndexOrThrow(ItemContract.Item.COLUMN_TYPE);
            int subTypeIdx = result.getColumnIndexOrThrow(ItemContract.Item.COLUMN_SUBTYPE);
            while (result.moveToNext()) {
                int type = result.getInt(typeIdx);
                String subType = result.getString(subTypeIdx);
                long amount = result.getLong(amountIdx);
                if (type == ItemContract.ITEM_TYPE_EXPENSE) {
                    builder.addExpense(subType, amount);
                } else if (type == ItemContract.ITEM_TYPE_INCOME) {
                    builder.addIncome(subType, amount);
                } else {
                    throw new AssertionError("Unknown type");
                }
            }
        } finally {
            Util.close(result);
            Util.close(db);
        }
        return builder.build();
    }

    public static Item fromCursorRow(ColumnIndices columnIndices, Cursor cursor) {
        int type = cursor.getInt(columnIndices.mTypeColumnIdx);
        String subtype = cursor.getString(columnIndices.mSubtypeColumnIdx);
        long amount = cursor.getLong(columnIndices.mAmountColumnIdx);
        long timeStamp = cursor.getLong(columnIndices.mTimeStampColumnIdx);
        String description = cursor.getString(columnIndices.mDescriptionColumnIdx);
        int id = getItemId(columnIndices, cursor);

        return new Item(type, subtype, description, amount, timeStamp, id);
    }

    public static int getItemId(ColumnIndices columnIndices, Cursor cursor) {
        return cursor.getInt(columnIndices.mIdIdx);
    }

    public static void delete(Context context, String[] ids) {
        final String whereClause = ItemContract.Item._ID + " IN (" + DbUtil.makePlaceholders(ids.length) + ")";
        ItemReaderDbHelper dbHelper = new ItemReaderDbHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(ItemContract.Item.TABLE_NAME, whereClause, ids);
        db.close();
    }

    static void putNewItemIntoDb(SQLiteDatabase writableDb, int itemType, String subType, String description, long amount, long timeStamp) {
        Item item = new Item(itemType, subType, description, amount, timeStamp, Item.NO_ID);
        putItemIntoDb(writableDb, item);
    }

    static void putItemIntoDb(SQLiteDatabase writableDb, Item item) {
        ContentValues cv = toContentValues(item);
        final int id = item.getId();
        if (id == Item.NO_ID) {
            writableDb.insertOrThrow(ItemContract.Item.TABLE_NAME, null, cv);
        } else {
            writableDb.update(ItemContract.Item.TABLE_NAME, cv,
                    ItemContract.Item._ID + "=?", new String[]{String.valueOf(id)});
        }
    }

    static ContentValues toContentValues(Item item) {
        ContentValues cv = new ContentValues();
        cv.put(ItemContract.Item.COLUMN_TYPE, item.getType());
        cv.put(ItemContract.Item.COLUMN_DESCRIPTION,  item.getDescription());
        cv.put(ItemContract.Item.COLUMN_AMOUNT, item.getAmount());
        cv.put(ItemContract.Item.COLUMN_SUBTYPE, item.getSubType());
        cv.put(ItemContract.Item.COLUMN_TIME_STAMP, item.getTimeStamp());
        return cv;
    }

    /**
     * Retrieve subtypes of expense or income.
     */
    private static String[] getSubtypesOfType(Context context, int type, String... customTypes) {
        if (type != ItemContract.ITEM_TYPE_EXPENSE && type != ItemContract.ITEM_TYPE_INCOME) {
            throw new IllegalArgumentException("type != ItemContract.ITEM_TYPE_EXPENSE && type != ItemContract.ITEM_TYPE_INCOME");
        }
        final String[] subtypeSelection = new String[]{String.valueOf(type)};
        ItemReaderDbHelper dbHelper = new ItemReaderDbHelper(context);
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor c = db.query(ItemContract.ItemSubtype.TABLE_NAME, SUBTYPE_COLUMN_SELECTION,
                ItemContract.ItemSubtype.COLUMN_ITEM_TYPE + "=?",
                subtypeSelection,
                null, null, null);

        String[] result = new String[c.getCount() + customTypes.length];

        int idx = 0;
        int colIdx = c.getColumnIndexOrThrow(ItemContract.ItemSubtype.COLUMN_SUBTYPE);
        while (c.moveToNext()) {
            result[idx] = c.getString(colIdx);
            idx++;
        }
        c.close();

        for (String customType: customTypes) {
            result[idx] = customType;
            idx++;
        }

        return result;
    }
}

