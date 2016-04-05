package com.tr.bnotes.db;

import com.tr.bnotes.model.ConsolidatedStatement;
import com.tr.bnotes.model.Item;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Func1;


@Singleton
public class ItemManager {

    private final ItemDao mItemDao;

    @Inject
    ItemManager(ItemDao itemDao) {
        mItemDao = itemDao;
    }

    public Observable<List<Item>> readItems() {
        return mItemDao.readItems();
    }

    public  Observable<List<Item>> deleteItems(final String[] ids) {
        return Observable.create(new Observable.OnSubscribe<Void>() {
            @Override
            public void call(Subscriber<? super Void> subscriber) {
                mItemDao.delete(ids);

            }
        }).flatMap(new Func1<Void, Observable<List<Item>>>() {
            @Override
            public Observable<List<Item>> call(Void aVoid) {
                return readItems();
            }
        });
    }

    public Observable<Void> saveItem(final Item item) {
        return Observable.create(new Observable.OnSubscribe<Void>() {
            @Override
            public void call(Subscriber<? super Void> subscriber) {
                mItemDao.saveItem(item);
            }
        });
    }

    public Observable<ConsolidatedStatement> getConsolidatedStatement(long startTime, long endTime) {
        return mItemDao.readItems(startTime, endTime)
            .map(new Func1<List<Item>, ConsolidatedStatement>() {
                @Override
                public ConsolidatedStatement call(List<Item> items) {
                    return ConsolidatedStatement.from(items);
                }
            });
    }

    public Observable<List<String>> getItemTypes(final int type) {
        return mItemDao.getTypes(type);
    }
}
