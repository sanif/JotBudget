package com.bestapps.jotbudget.ui.presenter;


import com.bestapps.jotbudget.model.ConsolidatedStatement;
import com.bestapps.jotbudget.ui.view.StatsView;
import com.bestapps.jotbudget.util.RxUtil;
import com.bestapps.jotbudget.db.ItemManager;

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
                    if (getView() != null) {
                        getView().showConsolidatedStatement(consolidatedStatement);
                    }
                }
            });
    }
}
