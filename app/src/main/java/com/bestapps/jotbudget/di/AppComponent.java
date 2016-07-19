package com.bestapps.jotbudget.di;

import com.bestapps.jotbudget.ui.ItemDetailsActivity;
import com.bestapps.jotbudget.ui.ItemListFragment;
import com.bestapps.jotbudget.ui.StatsActivity;
import com.bestapps.jotbudget.ui.MainActivity;

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
