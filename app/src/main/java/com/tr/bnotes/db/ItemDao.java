package com.tr.bnotes.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.squareup.sqlbrite.BriteDatabase;
import com.squareup.sqlbrite.SqlBrite;
import com.tr.bnotes.model.Item;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import rx.Observable;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

@Singleton
class ItemDao {
    private static final String[] EMPTY_ARGS = new String[0];

    private final BriteDatabase mBriteDatabase;

    private static class ColumnIndices {
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

    @Inject
    ItemDao(ItemDbHelper itemDbHelper) {
        mBriteDatabase = SqlBrite.create()
                .wrapDatabaseHelper(itemDbHelper, Schedulers.immediate());
    }

    public long saveItem(Item item) {
        ContentValues cv = toContentValues(item);
        final int id = item.getId();
        if (id == Item.NO_ID) {
            return mBriteDatabase.insert(ItemContract.Item.TABLE_NAME, cv);
        } else {
            return mBriteDatabase.update(ItemContract.Item.TABLE_NAME, cv,
                    ItemContract.Item._ID + "=?",  String.valueOf(id));
        }
    }

    // reads all the items ordered by date desc
    public Observable<List<Item>> readItems() {
        return readItems("SELECT * FROM " + ItemContract.Item.TABLE_NAME + " ORDER BY "
                + ItemContract.Item.COLUMN_TIME_STAMP + " DESC", EMPTY_ARGS);
    }

    public Observable<List<Item>> readItems(long startTime, long endTime) {
        String[] args = {String.valueOf(startTime), String.valueOf(endTime)};
        return readItems("SELECT * FROM " + ItemContract.Item.TABLE_NAME + " WHERE "
                + ItemContract.Item.COLUMN_TIME_STAMP+ " >=? AND "
                + ItemContract.Item.COLUMN_TIME_STAMP + " <=?", args);
    }

    public Observable<List<Item>> readItems(String sql, String... args) {
        return mBriteDatabase.createQuery(ItemContract.Item.TABLE_NAME, sql, args)
            .mapToList(new Func1<Cursor, Item>() {
                private ColumnIndices mIndices;

                @Override
                public Item call(Cursor cursor) {
                    // Avoid re-querying column indices
                    return toItem(getColumnIndices(cursor), cursor);
                }

                private ColumnIndices getColumnIndices(Cursor cursor) {
                    if (mIndices == null) {
                        mIndices = new ColumnIndices(cursor);
                    }
                    return mIndices;
                }
            });
    }

    public int delete(String[] ids) {
        final String whereClause = ItemContract.Item._ID
                + " IN (" + DbUtil.makePlaceholders(ids.length) + ")";
        return mBriteDatabase.delete(ItemContract.Item.TABLE_NAME, whereClause, ids);
    }

    /**
     * Retrieve subtypes of expense or income.
     */
    public Observable<List<String>> getTypes(int itemType) {
        if (itemType != Item.TYPE_EXPENSE && itemType != Item.TYPE_INCOME) {
            throw new IllegalArgumentException("type != Item.TYPE_EXPENSE && type != Item.TYPE_INCOME");
        }

        final String[] subtypeSelection = new String[]{String.valueOf(itemType)};
        return mBriteDatabase.createQuery(ItemContract.ItemSubtype.TABLE_NAME, "SELECT " + ItemContract.ItemSubtype.COLUMN_SUBTYPE + " FROM " + ItemContract.ItemSubtype.TABLE_NAME + " WHERE  " + ItemContract.ItemSubtype.COLUMN_ITEM_TYPE + "=?", subtypeSelection)
            .mapToList(new Func1<Cursor, String>() {
                @Override
                public String call(Cursor cursor) {
                    int colIdx = cursor.getColumnIndexOrThrow(ItemContract.ItemSubtype.COLUMN_SUBTYPE);
                    return cursor.getString(colIdx);
                }
            });
    }

    static void saveItem(SQLiteDatabase writableDb, Item item) {
        ContentValues cv = toContentValues(item);
        final int id = item.getId();
        if (id == Item.NO_ID) {
            writableDb.insertOrThrow(ItemContract.Item.TABLE_NAME, null, cv);
        } else {
            writableDb.update(ItemContract.Item.TABLE_NAME, cv,
                    ItemContract.Item._ID + "=?", new String[]{String.valueOf(id)});
        }
    }

    private static Item toItem(ColumnIndices columnIndices, Cursor cursor) {
        int type = cursor.getInt(columnIndices.mTypeColumnIdx);
        String subtype = cursor.getString(columnIndices.mSubtypeColumnIdx);
        long amount = cursor.getLong(columnIndices.mAmountColumnIdx);
        long timeStamp = cursor.getLong(columnIndices.mTimeStampColumnIdx);
        String description = cursor.getString(columnIndices.mDescriptionColumnIdx);
        int id = cursor.getInt(columnIndices.mIdIdx);

        return new Item(type, subtype, description, amount, timeStamp, id);
    }

    private static ContentValues toContentValues(Item item) {
        ContentValues cv = new ContentValues();
        cv.put(ItemContract.Item.COLUMN_TYPE, item.getType());
        cv.put(ItemContract.Item.COLUMN_DESCRIPTION,  item.getDescription());
        cv.put(ItemContract.Item.COLUMN_AMOUNT, item.getAmount());
        cv.put(ItemContract.Item.COLUMN_SUBTYPE, item.getSubType());
        cv.put(ItemContract.Item.COLUMN_TIME_STAMP, item.getTimeStamp());
        return cv;
    }
}

