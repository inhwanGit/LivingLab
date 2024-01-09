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
import java.util.ArrayList;

public class ReverseGeoPointer_Thread extends Thread {
    private Handler handler;

    private final static String NAVER_CLIENT_ID = "gdpvt1ezi4";
    private final static String NAVER_CLIENT_SECRET = "UgcYqxFpo2qr8FmF3i4PxaFFJ49cVuS4gZEiRfD1";

    private Double Latitude;
    private Double Longitude;

    private String json = null;
    private JSONObject jObject;

    private ArrayList<String> areas;
    private ArrayList<String> numbers;

    public ReverseGeoPointer_Thread(Handler handler, Double Latitude, Double Longitude) {
        this.handler = handler;
        this.Latitude = Latitude;
        this.Longitude = Longitude;
    }

    @Override
    public void run() {
        Message message = handler.obtainMessage();
        Bundle bundle = new Bundle();

        try {
            String sLatitude = URLEncoder.encode(Double.toString(Latitude), "UTF-8");
            String sLongitude = URLEncoder.encode(Double.toString(Longitude), "UTF-8");
            //&orders=addr : 지번주소
            //&orders=roadaddr : 도로명주소

            //coords={입력_좌표}&sourcecrs={좌표계}&orders={변환_작업_이름}&output={출력_형식}
            String apiURL = "https://naveropenapi.apigw.ntruss.com/map-reversegeocode/v2/gc?coords=" + sLatitude + "," + sLongitude + "&orders=addr&output=json"; // json

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
            JSONArray addrArray = jObject.getJSONArray("results");
            //Log.d("results =>",addrArray.toString());

            for(int i=0; i<addrArray.length(); i++){
                JSONObject obj = addrArray.getJSONObject(i);
                //String region = obj.getString("region");
                //String land = obj.getString("land");

                //Log.d("TEST/region =>",region);
                //Log.d("TEST/land =>",land);

                JSONObject region = obj.getJSONObject("region");
                JSONObject land = obj.getJSONObject("land");

                JSONObject area1 = region.getJSONObject("area1");
                JSONObject area2 = region.getJSONObject("area2");
                JSONObject area3 = region.getJSONObject("area3");
                JSONObject area4 = region.getJSONObject("area4");

                areas = new ArrayList<>();
                areas.add(area1.getString("name"));
                areas.add(area2.getString("name"));
                areas.add(area3.getString("name"));
                areas.add(area4.getString("name"));

                numbers = new ArrayList<>();
                numbers.add(land.getString("number1"));
                numbers.add(land.getString("number2"));
            }

            bundle.putBoolean("ReverseGeoPointerThread", true);
            bundle.putStringArrayList("areas",areas);
            bundle.putStringArrayList("numbers",numbers);
            message.setData(bundle);
            handler.sendMessage(message);

        } catch (Exception e) {
            e.printStackTrace();
        }

        if (json == null) {
            Log.d("TEST/ReverseGeoPointer", "json == null");
            //return point;
        } else {
            Log.d("TEST/ReverseGeoPointer", "json => " + json);
        }
    }
}


