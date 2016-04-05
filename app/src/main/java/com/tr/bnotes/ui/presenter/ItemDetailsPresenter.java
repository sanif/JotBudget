package com.tr.bnotes.ui.presenter;

import android.content.Context;

import com.tr.bnotes.model.Item;
import com.tr.bnotes.db.ItemManager;
import com.tr.bnotes.util.RxUtil;
import com.tr.bnotes.ui.view.ItemDetailsView;
import com.tr.expenses.R;

import java.util.List;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class ItemDetailsPresenter extends BasePresenter<ItemDetailsView> {
    private Subscription mItemTypesSubscription;

    @Override
    public void unbind() {
        RxUtil.unsubscribe(mItemTypesSubscription);
        super.unbind();
    }

    public void loadSubTypes(final Context context, final int itemType) {
        mItemTypesSubscription = ItemManager.getItemTypes(itemType)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .map(new Func1<List<String>, SubTypes>() {
                @Override
                public SubTypes call(List<String> subTypes) {
                    final String title;
                    if (itemType == Item.TYPE_EXPENSE) {
                        title = context.getString(R.string.pick_expense_type);
                    } else {
                        title = context.getString(R.string.pick_income_type);
                    }
                    return new SubTypes(title, subTypes);
                }
            })
            .subscribe(new Action1<SubTypes>() {
                @Override
                public void call(SubTypes subTypes) {
                    getView().showSubTypes(subTypes.mTitle, subTypes.mSubTypes);
                }
            });
    }

    public void saveItem(Item item) {
        ItemManager.saveItem(item)
            .subscribeOn(Schedulers.io())
            .subscribe();
    }

    private static class SubTypes {
        private final String mTitle;
        private final List<String> mSubTypes;

        public SubTypes(String title, List<String> subTypes) {
            mTitle = title;
            mSubTypes = subTypes;
        }
    }
}
