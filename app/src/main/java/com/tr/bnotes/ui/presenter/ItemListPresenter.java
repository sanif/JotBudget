package com.tr.bnotes.ui.presenter;

import com.tr.bnotes.model.Item;
import com.tr.bnotes.db.ItemManager;
import com.tr.bnotes.util.RxUtil;
import com.tr.bnotes.ui.view.ItemListView;

import java.util.List;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class ItemListPresenter extends BasePresenter<ItemListView> {
    private final CompositeSubscription mCompositeSubscription = new CompositeSubscription();

    @Override
    public void unbind() {
        RxUtil.unsubscribe(mCompositeSubscription);
        super.unbind();
    }

    public void loadItems() {
        Subscription sub = ItemManager.readItems()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(new Action1<List<Item>>() {
                @Override
                public void call(List<Item> items) {
                    getView().showItems(items);
                }
            });
        mCompositeSubscription.add(sub);
    }

    public void deleteItems(String[] ids) {
        Subscription sub = ItemManager.deleteItems(ids)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(new Action1<List<Item>>() {
                @Override
                public void call(List<Item> items) {
                    getView().showItems(items);
                }
            });
        mCompositeSubscription.add(sub);
    }
}
