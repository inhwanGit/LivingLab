package com.example.myapplication_naver;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.EditText;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

public class AddrAPI_Thread extends Thread{
    private Handler handler;
    private String addr = "";

    private String json = null;
    private JSONObject jObject;

    private final static String confmKey = "devU01TX0FVVEgyMDIyMDkwMzAyNTUyNjExMjk0NDc=";    //요청 변수 설정 (승인키)
    private String currentPage = "1";   //요청 변수 설정 (현재 페이지. currentPage : n > 0)
    private String countPerPage = "10"; //요청 변수 설정 (페이지당 출력 개수. countPerPage 범위 : 0 < n <= 100)
    private String resultType = "json"; //요청 변수 설정 (검색결과형식 설정, json)
    private String keyword = "";    //요청 변수 설정 (키워드)

    public AddrAPI_Thread(Handler handler, String addr){
        this.handler = handler;
        this.addr = addr;
    }

    @Override
    public void run(){
        //키워드 입력
        keyword = addr;

        Message message = handler.obtainMessage();
        Bundle bundle = new Bundle();

        try{
            String apiUrl = "https://www.juso.go.kr/addrlink/addrLinkApi.do?currentPage="+currentPage
                    +"&countPerPage="+countPerPage+"&keyword="+URLEncoder.encode(keyword,"UTF-8")
                    +"&confmKey="+confmKey+"&resultType="+resultType;

            URL url = new URL(apiUrl);

            BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream(),"UTF-8"));
            StringBuffer sb = new StringBuffer();
            String tempStr = null;
            while(true){
                tempStr = br.readLine();
                if(tempStr == null) break;
                sb.append(tempStr);
            }
            br.close();

            json = sb.toString();

            Log.d("Keyword", addr);

            jObject = new JSONObject(json);
            JSONObject resultObj = jObject.getJSONObject("results");         //<===여기서 오류 왜???
            JSONArray jusoArray = resultObj.getJSONArray("juso");

            ArrayList<String> roadArraylist = new ArrayList<String>();
            ArrayList<String> jibunArraylist = new ArrayList<String>();
            ArrayList<String> bdNmArraylist = new ArrayList<String>();


            for(int i=0; i<jusoArray.length(); i++){
                JSONObject obj = jusoArray.getJSONObject(i);
                String roadAddr = obj.getString("roadAddr");
                String jibunAddr = obj.getString("jibunAddr");
                String bdNm = obj.getString("bdNm");

                bdNmArraylist.add(bdNm);
                roadArraylist.add(roadAddr);
                jibunArraylist.add(jibunAddr);
            }

            //메시지 저장 및 전송
            bundle.putBoolean("AddAPIThread", true);
            bundle.putStringArrayList("ROAD_ADDR_LIST", roadArraylist);
            bundle.putStringArrayList("JIBUN_ADDR_LIST", jibunArraylist);
            bundle.putStringArrayList("BDNM_ADDR_LIST", bdNmArraylist);
            message.setData(bundle);
            handler.sendMessage(message);

        } catch (Exception e) {
            e.printStackTrace();
        }

        if (json == null) {
            Log.d("TEST/AddrAPIThread", "json == null");
        } else {
            Log.d("TEST/AddrAPIThread", "json => " + json);
        }


    }
}
