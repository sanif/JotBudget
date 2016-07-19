package com.bestapps.jotbudget.ui.view;

import com.bestapps.jotbudget.model.Item;

import java.util.List;

public interface ItemListView extends BaseView {
    void showItems(List<Item> items);
}
