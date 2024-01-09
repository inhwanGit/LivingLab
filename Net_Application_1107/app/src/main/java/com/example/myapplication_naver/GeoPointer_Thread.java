package com.example.myapplication_naver;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class GeoPointer_Thread extends Thread {
    private Handler handler;

    private final static String NAVER_CLIENT_ID = "gdpvt1ezi4";
    private final static String NAVER_CLIENT_SECRET = "UgcYqxFpo2qr8FmF3i4PxaFFJ49cVuS4gZEiRfD1";
    private String addr = "";

    private String json = null;
    private JSONObject jObject;

    private String latitude ="";
    private String longtude ="";

    public GeoPointer_Thread(Handler handler, String addr) {
        this.handler = handler;
        this.addr = addr;
        //this.nowlat = Double.toString(nowlat);
        //this.nowlong = Double.toString(nowlong);
        //this.nowlat = nowlat;   //검색 중심 좌표
        //this.nowlong = nowlong; //검색 중심 좌표
    }

    @Override
    public void run() {
        Message message = handler.obtainMessage();
        Bundle bundle = new Bundle();

        try {
            String K_addr = addr;
            addr = URLEncoder.encode(addr, "UTF-8");
            String apiURL = "https://naveropenapi.apigw.ntruss.com/map-geocode/v2/geocode?query=" + addr; // json

            URL url = new URL(apiURL);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("X-NCP-APIGW-API-KEY-ID", NAVER_CLIENT_ID);
            con.setRequestProperty("X-NCP-APIGW-API-KEY", NAVER_CLIENT_SECRET);

            int responseCode = con.getResponseCode();
            BufferedReader br;
            if (responseCode == 200) { // 정상 호출
                br = new BufferedReader(new InputStreamReader(con.getInputStream()));
            } else { // 에러 발생
                br = new BufferedReader(new InputStreamReader(con.getErrorStream()));
            }
            String inputLine;
            StringBuffer response = new StringBuffer();
            while ((inputLine = br.readLine()) != null) {
                response.append(inputLine);
            }
            br.close();
            json = response.toString();


            jObject = new JSONObject(json);
            JSONArray addrArray = jObject.getJSONArray("addresses");
            double x; double y;
            //double distance;

            for(int i=0; i<addrArray.length(); i++){
                JSONObject obj = addrArray.getJSONObject(i);
                x = obj.getDouble("x");
                y = obj.getDouble("y");
                //distance = obj.getDouble("distance");

                MainActivity.GeoLatitude = obj.getString("y");
                MainActivity.GeoLongitude = obj.getString("x");

                Log.d(K_addr,"x : " + x + " y : " + y);
                bundle.putBoolean("GeoPointerThread", true);
                bundle.putDouble("Latitude",y);
                bundle.putDouble("Longitude",x);
                //bundle.putDouble("Distance",distance);
            }

            message.setData(bundle);
            handler.sendMessage(message);

        } catch (Exception e) {
            e.printStackTrace();
        }

        if (json == null) {
            Log.d("TEST/GeoPointer", "json == null");
            //return point;
        } else {
            Log.d("TEST/GeoPointer", "json => " + json);
        }
    }
}


