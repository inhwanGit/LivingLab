package com.example.myapplication_naver;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.ArrayList;

public class SharedPrefManager_RecentRoute {
    private SharedPreferences mSharedPrefs;
    private SharedPreferences.Editor mEdit;
    private ArrayList<String> arrPackage;


    private static SharedPrefManager_RecentRoute mInstance;
    public static SharedPrefManager_RecentRoute getInstance(Context context) {
        if(mInstance == null) {
            mInstance = new SharedPrefManager_RecentRoute(context);
        }
        return mInstance;
    }

    private SharedPrefManager_RecentRoute(Context context) {
        mSharedPrefs = context.getSharedPreferences("Search_History", MODE_PRIVATE);
        mEdit = mSharedPrefs.edit();
        arrPackage = new ArrayList<>();
    }
    //------------- 저장하기 START -----------------
    public int addText(int count, String text) {
        for(int i=0; i<20; i++) {
            if(count == i) {
                mEdit.putString("history"+i, text);
                mEdit.commit();
                return i;
            }
        }
        if(count == 20) {
            mEdit.putString("history0", text);
            mEdit.commit();
        }
        return 0;
    }

    public void addCount(int count) {
        mEdit.putInt("count_his", count);
        mEdit.commit();
    }
    //------------- 저장하기 END --------------------

    //저장된 글자 검색하기
    public boolean findText(String text) {
        for(int i=0; i<20; i++) {
            String star = mSharedPrefs.getString("history"+i, "");
            if(star.equals(text)) return true;
        }
        return false;
    }
}

