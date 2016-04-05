package com.tr.bnotes.ui.presenter;

import com.tr.bnotes.ui.view.BaseView;

public interface Presenter<V extends BaseView> {
    void bind(V view);
    void unbind();
}
