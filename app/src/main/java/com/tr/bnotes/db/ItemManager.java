package com.tr.bnotes.db;

import com.tr.bnotes.model.ConsolidatedStatement;
import com.tr.bnotes.model.Item;

import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Func1;


public class ItemManager {
    public static Observable<List<Item>> readItems() {
        return ItemDao.readItems();
    }

    public static Observable<List<Item>> deleteItems(final String[] ids) {
        return Observable.create(new Observable.OnSubscribe<Void>() {
            @Override
            public void call(Subscriber<? super Void> subscriber) {
                ItemDao.delete(ids);

            }
        }).flatMap(new Func1<Void, Observable<List<Item>>>() {
            @Override
            public Observable<List<Item>> call(Void aVoid) {
                return readItems();
            }
        });
    }

    public static Observable<Void> saveItem(final Item item) {
        return Observable.create(new Observable.OnSubscribe<Void>() {
            @Override
            public void call(Subscriber<? super Void> subscriber) {
                ItemDao.saveItem(item);
            }
        });
    }

    public static Observable<ConsolidatedStatement> getConsolidatedStatement(long startTime, long endTime) {
        return ItemDao.readItems(startTime, endTime)
            .map(new Func1<List<Item>, ConsolidatedStatement>() {
                @Override
                public ConsolidatedStatement call(List<Item> items) {
                    return ConsolidatedStatement.from(items);
                }
            });
    }

    public static Observable<List<String>> getItemTypes(final int type) {
        return ItemDao.getTypes(type);
    }
}
