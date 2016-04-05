package com.tr.bnotes.ui.presenter;


import com.tr.bnotes.model.ConsolidatedStatement;
import com.tr.bnotes.db.ItemManager;
import com.tr.bnotes.util.RxUtil;
import com.tr.bnotes.ui.view.StatsView;

import javax.inject.Inject;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class StatsPresenter extends BasePresenter<StatsView> {
    private ItemManager mItemManager;
    private Subscription mConsolidatedStatementSubscription;

    @Inject
    StatsPresenter(ItemManager itemManager) {
        mItemManager = itemManager;
    }

    @Override
    public void unbind() {
        RxUtil.unsubscribe(mConsolidatedStatementSubscription);
        super.unbind();
    }

    public void loadConsolidatedStatement(long from, long to) {
        mConsolidatedStatementSubscription = mItemManager.getConsolidatedStatement(from, to)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(new Action1<ConsolidatedStatement>() {
                @Override
                public void call(ConsolidatedStatement consolidatedStatement) {
                    getView().showConsolidatedStatement(consolidatedStatement);
                }
            });
    }
}
