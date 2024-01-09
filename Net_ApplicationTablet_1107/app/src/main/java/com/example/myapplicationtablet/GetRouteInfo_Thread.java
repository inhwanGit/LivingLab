package com.example.myapplicationtablet;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;

public class GetRouteInfo_Thread extends Thread{
    private String serviceUrl = "http://openapitraffic.daejeon.go.kr/api/rest/busRouteInfo/";
    private String function = "getRouteInfo";
    private String serviceKey = "QWUgpO%2Bo3SdnRO6RDHQJXYSTVdFK%2B5ajLGxO5ZiHULK23HApbBbcjgeXL%2BEO6%2BOBa0Ik7JnQW4axGE6%2BxtJxZw%3D%3D";
    private String request = "busRouteId";
    private String data = "";

    private XmlPullParser parser;
    private Handler handler;

    private boolean bus_headerMsg = false;
    private boolean bus_headerCd = false;
    private boolean bus_routeNo = false;
    private boolean bus_routeCd = false;
    private boolean bus_routeTp = false;

    private String headerCd="";
    private String headerMsg="";
    private String routeNo="";      //노선 번호
    private String routeCd="";      //노선 번호 유니크값(7자리)
    private int routeTp;            //노선 타입 (1:급행 2:간선,3:지선,4:외곽, 5:마을, 6:첨단)

    public GetRouteInfo_Thread(Handler handler, String data) {
        this.handler = handler;
        this.data = data;
    }

    @Override
    public void run() {
        try {
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
                        case "headerCd": bus_headerCd = true;           break;
                        case "headerMsg": bus_headerMsg = true;           break;
                        case "ROUTE_NO": bus_routeNo = true;            break;
                        case "ROUTE_CD": bus_routeCd = true;            break;
                        case "ROUTE_TP": bus_routeTp = true;            break;
                    }
                } else if(eventType == XmlPullParser.TEXT){

                    if(bus_headerCd) {
                        headerCd = parser.getText();
                        bus_headerCd = false;
                    }
                    if(bus_headerMsg) {
                        headerMsg = parser.getText();
                        bus_headerMsg = false;
                    }
                    if("0".equals(headerCd)){
                        if(bus_routeNo){
                            routeNo = parser.getText();
                            bus_routeNo = false;
                        }
                        else if(bus_routeCd){
                            routeCd = parser.getText();
                            bus_routeCd = false;
                        }
                        else if(bus_routeTp){
                            String text[] = parser.getText().split(" ");
                            String textsplit = text[0];
                            routeTp = Integer.parseInt(textsplit);
                            bus_routeTp = false;
                        }
                    } else if("4".equals(headerCd)){
                        //요청한 결과가 나오지 않을 경우

                        //메세지 저장 및 전송
                        Message message = handler.obtainMessage();
                        Bundle bundle = new Bundle();
                        bundle.putBoolean("GetRouteInfo_Thread", true);
                        bundle.putString("ERROR", headerMsg);
                        message.setData(bundle);
                        handler.sendMessage(message);

                        return;
                    }
                } else if(eventType == XmlPullParser.END_TAG){
                    ;
                }
                eventType = parser.next();
            }

            //메세지 저장 및 전송
            Message message = handler.obtainMessage();
            Bundle bundle = new Bundle();
            bundle.putBoolean("GetRouteInfo_Thread", true);
            bundle.putString("ROUTE_NO", routeNo);
            bundle.putString("ROUTE_CD", routeCd);
            bundle.putInt("ROUTE_TP", routeTp);
            message.setData(bundle);
            handler.sendMessage(message);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
