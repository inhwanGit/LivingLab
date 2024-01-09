package com.example.myapplication_naver;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPrefManager_Setting {
    private SharedPreferences mSharedPrefs;
    private SharedPreferences.Editor mEdit;

    private static SharedPrefManager_Setting mInstance;

    public static SharedPrefManager_Setting getInstance(Context context) {
        if(mInstance == null) {
            mInstance = new SharedPrefManager_Setting(context);
        }
        return mInstance;
    }

    private SharedPrefManager_Setting(Context context) {
        mSharedPrefs = context.getSharedPreferences("Setting_History", MODE_PRIVATE);
        mEdit = mSharedPrefs.edit();
    }

    public void add_textRaise(boolean bool) {
        mEdit.putBoolean("textRaise", bool);
        mEdit.commit();
    }
    public void add_colorEmphasis(boolean bool) {
        mEdit.putBoolean("colorEmphasis", bool);
        mEdit.commit();
    }
    public void add_SearchListOn(boolean bool) {
        mEdit.putBoolean("SearchListOn", bool);
        mEdit.commit();
    }

    public void add_VibratorOn(boolean bool){
        mEdit.putBoolean("VibratorOn", bool);
        mEdit.commit();
    }
}
