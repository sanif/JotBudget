package com.tr.bnotes.di;

import com.tr.bnotes.ui.ItemDetailsActivity;
import com.tr.bnotes.ui.ItemListFragment;
import com.tr.bnotes.ui.MainActivity;
import com.tr.bnotes.ui.StatsActivity;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = AppModule.class)
public interface AppComponent {
    void inject(MainActivity mainActivity);

    void inject(ItemDetailsActivity itemDetailsActivity);

    void inject(ItemListFragment itemListFragment);

    void inject(StatsActivity statsActivity);
}
