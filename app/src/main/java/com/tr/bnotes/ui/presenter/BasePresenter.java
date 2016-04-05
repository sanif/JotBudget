package com.tr.bnotes.ui.presenter;

import com.tr.bnotes.ui.view.BaseView;

public class BasePresenter<V extends BaseView> implements Presenter<V> {
    private V mView;

    @Override
    public void bind(V view) {
        mView = view;
    }

    @Override
    public void unbind() {
        mView = null;
    }

    protected final V getView() {
        return mView;
    }
}

