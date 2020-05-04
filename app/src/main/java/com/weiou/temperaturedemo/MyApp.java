package com.weiou.temperaturedemo;

import android.app.Application;

import com.weiou.lib_temp.CommonModule;

public class MyApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        CommonModule.init(this);
    }
}
