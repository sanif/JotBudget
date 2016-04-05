package com.tr.bnotes.ui.view;

import com.tr.bnotes.model.Item;

import java.util.List;

public interface ItemListView extends BaseView {
    void showItems(List<Item> items);
}
