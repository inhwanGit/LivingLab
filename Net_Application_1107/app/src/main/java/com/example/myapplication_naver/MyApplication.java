package com.example.myapplication_naver;

import android.app.Application;
import android.content.Context;

public class MyApplication extends Application {
    private static Context context;

    public void onCreate() {
        super.onCreate();
        MyApplication.context = getApplicationContext();
    }

    public static Context ApplicationContext(){
        return MyApplication.context;
    }
}
