package com.example.myapplication_naver;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.ArrayList;

public class SharedPrefManager_StarSave {
    private SharedPreferences mSharedPrefs;
    private SharedPreferences.Editor mEdit;
    private ArrayList<String> arrPackage;

    private static SharedPrefManager_StarSave mInstance;
    public static SharedPrefManager_StarSave getInstance(Context context) {
        if(mInstance == null) {
            mInstance = new SharedPrefManager_StarSave(context);
        }
        return mInstance;
    }

    private SharedPrefManager_StarSave(Context context) {
        mSharedPrefs = context.getSharedPreferences("Search_History", MODE_PRIVATE);
        mEdit = mSharedPrefs.edit();
        arrPackage = new ArrayList<>();
    }

    //------------- 경로 저장하기 START -------------
    public int addRoute(int count, String route) {
        for(int i=0; i<20; i++) {
            if(count == i) {
                mEdit.putString("routes"+i, route);
                mEdit.commit();
                return i;
            }
        }
        if(count == 20) {
            mEdit.putString("routes0", route);
            mEdit.commit();
        }
        return 0;
    }

    public void addCount_rou(int count) {
        mEdit.putInt("count_route", count);
        mEdit.commit();
    }
    //------------- 경로 저장하기 END ---------------

    //----------- 즐겨찾기 저장하기 START ------------
    public int addStar(int count, String route) {
        for(int i=0; i<20; i++) {
            if(count == i) {
                mEdit.putString("star"+i, route);
                mEdit.commit();
                return i;
            }
        }
        if(count == 20) {
            mEdit.putString("star0", route);
            mEdit.commit();
        }
        return 0;
    }

    public void addCount_star(int count) {
        mEdit.putInt("count_star", count);
        mEdit.commit();
    }
    //----------- 즐겨찾기 저장하기 END --------------

    //즐겨찾기 검색하기
    public boolean findStar(String route) {
        for(int i=0; i<20; i++) {
            String star = mSharedPrefs.getString("star"+i, "");
            if(star.equals(route)) return true;
        }
        return false;
    }

    //경로 검색하기
    public boolean findRoute(String route) {
        for(int i=0; i<20; i++) {
            String star = mSharedPrefs.getString("routes"+i, "");
            if(star.equals(route)) return true;
        }
        return false;
    }

    //----------- 즐겨찾기 삭제하기 START ------------
    public void DelStar(int count, String route) {
        for(int i=0; i<20; i++) {
            String star = mSharedPrefs.getString("star"+i, "");
            if(star.equals(route)) {
                mEdit.remove("star"+i);
                Log.d("delStar"+i, route+i);
                Log.d("count", count+"");
                mEdit.commit();

                for(;i!=count+1;i++) {
                    String front = mSharedPrefs.getString("star"+(i+1), "");
                    Log.d("star(i+1)"+(i+1), front);
                    mEdit.putString("star"+i, front);
                    Log.d("star(i)"+i, front);
                    mEdit.remove("star"+count);
                    mEdit.commit();
                    if(i==count) {
                        mEdit.putInt("count_star", count-1);
                        mEdit.commit();
                    }
                }
            }
        }
    }
    //----------- 즐겨찾기 삭제하기 END --------------

    public void DelRoute(int count, String route) {
        for(int i=0; i<20; i++) {
            String star = mSharedPrefs.getString("routes"+i, "");
            if(star.equals(route)) {
                mEdit.remove("routes"+i);
                Log.d("delRoute"+i, route+i);
                Log.d("count", count+"");
                mEdit.commit();

                for(;i!=count+1;i++) {
                    String front = mSharedPrefs.getString("routes"+(i+1), "");
                    Log.d("routes(i+1)"+(i+1), front);
                    mEdit.putString("routes"+i, front);
                    Log.d("routes(i)"+i, front);
                    mEdit.remove("routes"+count);
                    mEdit.commit();
                    if(i==count) {
                        mEdit.putInt("count_route", count-1);
                        mEdit.commit();
                    }
                }
            }
        }
    }
}
