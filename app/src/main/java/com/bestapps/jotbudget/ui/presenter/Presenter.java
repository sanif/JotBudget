package com.bestapps.jotbudget.ui.presenter;

import com.bestapps.jotbudget.ui.view.BaseView;

public interface Presenter<V extends BaseView> {
    void bind(V view);
    void unbind();
}
