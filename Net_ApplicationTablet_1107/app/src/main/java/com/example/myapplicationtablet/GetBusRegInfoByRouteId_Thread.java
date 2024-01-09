package com.example.myapplicationtablet;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;

public class GetBusRegInfoByRouteId_Thread extends Thread{
    private String serviceUrl = "http://openapitraffic.daejeon.go.kr/api/rest/busreginfo/";
    private String function = "getBusRegInfoByRouteId";
    private String serviceKey = "QWUgpO%2Bo3SdnRO6RDHQJXYSTVdFK%2B5ajLGxO5ZiHULK23HApbBbcjgeXL%2BEO6%2BOBa0Ik7JnQW4axGE6%2BxtJxZw%3D%3D";
    private String request = "busRouteId";
    private String data = "";

    private XmlPullParser parser;
    private Handler handler;

    private boolean bus_headerCd = false;
    private boolean bus_routeCd = false;
    private boolean bus_carRegNo = false;
    private boolean bus_type = false;

    private String headerCd="";
    private String routeCd;
    private String carRegNo;
    private int type;
    private ArrayList<Bus> buslist = new ArrayList<>();

    public GetBusRegInfoByRouteId_Thread(Handler handler, String data)
    {
        this.handler = handler;
        this.data = data;
    }

    @Override
    public void run() {
        try{
            String strUrl = serviceUrl + function +"?serviceKey=" + serviceKey + "&" + request + "=" + data;

            URL url = new URL(strUrl);

            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            parser = factory.newPullParser();

            InputStream is = url.openStream();
            parser.setInput(new InputStreamReader(is, "UTF-8"));

            //event type얻어오기
            int eventType = parser.getEventType();

            //xml문서의 끝까지 읽기
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if(eventType == XmlPullParser.START_DOCUMENT){
                    ;
                } else if(eventType == XmlPullParser.START_TAG){
                    String tag_name = parser.getName();
                    switch (tag_name){
                        case "headerCd": bus_headerCd = true;       break;
                        case "ROUTE_CD" : bus_routeCd = true;       break;
                        case "CAR_REG_NO" : bus_carRegNo = true;    break;
                        case "BUS_TYPE" : bus_type = true;          break;
                    }
                } else if(eventType == XmlPullParser.TEXT){

                    if(bus_headerCd) {
                        headerCd = parser.getText();
                        bus_headerCd = false;
                    }
                    if("0".equals(headerCd)){
                        if(bus_routeCd){
                            routeCd = parser.getText();
                            bus_routeCd = false;
                        }
                        else if(bus_carRegNo){
                            carRegNo = parser.getText();
                            bus_carRegNo = false;
                        }
                        else if(bus_type){
                            type = Integer.parseInt(parser.getText());
                            bus_type = false;
                        }
                    }
                } else if(eventType == XmlPullParser.END_TAG){
                    String tag_name = parser.getName();
                    if(tag_name.equals("itemList")){
                        //버스 객체 생성하고 리스트에 저장
                        //Log.d("GetBusRegInfoByRouteId_Thread", routeCd + "/" + carRegNo + "/" + type);
                        Bus bus = new Bus(routeCd, carRegNo, type);
                        buslist.add(bus);
                    }
                }
                eventType = parser.next();
            }

            //메세지 저장 및 전송
            Message message = handler.obtainMessage();
            Bundle bundle = new Bundle();
            bundle.putBoolean("GetBusRegInfoByRouteId_Thread", true);
            bundle.putSerializable("BUS_LIST", buslist);
            message.setData(bundle);
            handler.sendMessage(message);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
