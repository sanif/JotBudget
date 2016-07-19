package com.bestapps.jotbudget;

import android.app.Application;

import com.bestapps.jotbudget.di.AppComponent;
import com.bestapps.jotbudget.di.AppModule;
import com.bestapps.jotbudget.di.DaggerAppComponent;


public class App extends Application {
    private static App sApp;
    private AppComponent mAppComponent;

    @Override
    public void onCreate() {
        super.onCreate();
        sApp = this;

        mAppComponent = DaggerAppComponent.builder()
                .appModule(new AppModule(this))
                .build();
    }

    public static AppComponent getComponent() {
        return sApp.mAppComponent;
    }
}
