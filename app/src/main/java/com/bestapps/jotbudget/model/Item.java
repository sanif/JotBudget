package com.bestapps.jotbudget.model;

import android.os.Parcel;
import android.os.Parcelable;

// Expense / Income item
public final class Item implements Parcelable {
    public static final int NO_ID = -1;
    public static final int TYPE_EXPENSE = 0;
    public static final int TYPE_INCOME = 1;
    private final int itemType;
    private final String subtype;
    private final long timeStamp;
    private final String description;
    private final long amount;
    private final int id;

    public Item(int itemType, String subType, String description, long amount, long timeStamp, int id) {
        if (amount < 0) { // we keep the amount unsigned
            throw new IllegalArgumentException("amount < 0");
        }
        this.itemType = itemType;
        this.timeStamp = timeStamp;
        this.subtype = subType;
        this.description = description;
        this.amount = amount;
        this.id = id;
    }

    public int getType() {
        return itemType;
    }

    public String getSubType() {
        return subtype;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public String getDescription() {
        return description;
    }

    public long getAmount() {
        return amount;
    }

    public int getId() {
        return id;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.itemType);
        dest.writeString(this.subtype);
        dest.writeLong(this.timeStamp);
        dest.writeString(this.description);
        dest.writeLong(this.amount);
        dest.writeInt(this.id);
    }

    protected Item(Parcel in) {
        this.itemType = in.readInt();
        this.subtype = in.readString();
        this.timeStamp = in.readLong();
        this.description = in.readString();
        this.amount = in.readLong();
        this.id = in.readInt();
    }

    public static final Creator<Item> CREATOR = new Creator<Item>() {
        public Item createFromParcel(Parcel source) {
            return new Item(source);
        }

        public Item[] newArray(int size) {
            return new Item[size];
        }
    };
}