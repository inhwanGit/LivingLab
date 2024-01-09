package com.example.myapplication_naver;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.TextView;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;

public class BusStopArrival_Thread7 extends Thread{
    private String serviceUrl = "http://openapitraffic.daejeon.go.kr/api/rest/arrive/";
    private String function = "getArrInfoByStopID";
    private String serviceKey = "QWUgpO%2Bo3SdnRO6RDHQJXYSTVdFK%2B5ajLGxO5ZiHULK23HApbBbcjgeXL%2BEO6%2BOBa0Ik7JnQW4axGE6%2BxtJxZw%3D%3D";
    private String request = "BusStopID";
    private String data = "";

    private XmlPullParser parser;
    private Handler handler;

    private String headerCd="";

    private boolean bus_headerCd = false;
    private boolean bus_routeNo = false;
    private boolean bus_statusPos = false;
    private boolean bus_extimeMin = false;
    private boolean bus_carRegNo = false;
    private boolean bus_routeCd = false;
    private boolean bus_msgTp = false;

    private ArrayList<String> routeNoList = new ArrayList<>();
    private ArrayList<String> statusPosList = new ArrayList<>();
    private ArrayList<String> extimeMinList = new ArrayList<>();
    private ArrayList<String> carRegNoList = new ArrayList<>();
    private ArrayList<String> routeCdList = new ArrayList<>();
    private ArrayList<String> msgTpList = new ArrayList<>();

    public BusStopArrival_Thread7(Handler handler, String data)
    {
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

            Message message = handler.obtainMessage();
            Bundle bundle = new Bundle();

            //xml문서의 끝까지 읽기
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if(eventType == XmlPullParser.START_DOCUMENT){
                    ;
                } else if(eventType == XmlPullParser.START_TAG){
                    String tag_name = parser.getName();
                    switch (tag_name){
                        case "headerCd": bus_headerCd = true;       break;
                        case "ROUTE_NO": bus_routeNo = true;        break;
                        case "STATUS_POS": bus_statusPos = true;    break;
                        case "EXTIME_MIN" : bus_extimeMin = true;   break;
                        case "CAR_REG_NO" : bus_carRegNo = true;    break;
                        case "ROUTE_CD" : bus_routeCd = true;       break;
                        case "MSG_TP" : bus_msgTp = true;           break;
                    }
                } else if(eventType == XmlPullParser.TEXT){
                    if(bus_headerCd) {
                        headerCd = parser.getText();
                        bus_headerCd = false;
                    }
                    if("0".equals(headerCd)){
                        if(bus_routeNo) {
                            //routeNo = parser.getText();
                            Log.d("BusStopArrival_Thread", parser.getText());
                            routeNoList.add(parser.getText());                  //리스트 저장
                            bus_routeNo = false;
                        }
                        else if(bus_statusPos){
                            Log.d("BusStopArrival_Thread", parser.getText());
                            statusPosList.add(parser.getText());
                            bus_statusPos = false;
                        }
                        else if(bus_extimeMin){
                            Log.d("BusStopArrival_Thread", parser.getText());
                            extimeMinList.add(parser.getText());
                            bus_extimeMin = false;
                        }
                        else if(bus_carRegNo){
                            Log.d("BusStopArrival_Thread", parser.getText());
                            carRegNoList.add(parser.getText());
                            bus_carRegNo = false;
                        }
                        else if(bus_routeCd){
                            Log.d("BusStopArrival_Thread", parser.getText());
                            routeCdList.add(parser.getText());
                            bus_routeCd = false;
                        }
                        else if(bus_msgTp){
                            Log.d("BusStopArrival_Thread", parser.getText());
                            msgTpList.add(parser.getText());
                            bus_msgTp = false;
                        }

                    }
                } else if(eventType == XmlPullParser.END_TAG){
                    ;
                }
                eventType = parser.next();
            }

            //메인엑티비티 변수에 대입
            MainActivity.routeNoList.addAll(routeNoList);
            MainActivity.carRegNoList.addAll(carRegNoList);
            MainActivity.routeCdList.addAll(routeCdList);
            MainActivity.statusPosList.addAll(statusPosList);
            MainActivity.extimeMinList.addAll(extimeMinList);
            MainActivity.msgTpList.addAll(msgTpList);

            //메세지 저장 및 전송
            bundle.putBoolean("BusStopArrival_Thread", true);
            bundle.putStringArrayList("ROUTE_NO", routeNoList);
            bundle.putStringArrayList("STATUS_POS", statusPosList);
            bundle.putStringArrayList("EXTIME_MIN", extimeMinList);
            bundle.putStringArrayList("CAR_REG_NO", carRegNoList);
            bundle.putStringArrayList("ROUTE_CD", routeCdList);
            bundle.putStringArrayList("MSG_TP", msgTpList);
            message.setData(bundle);
            handler.sendMessage(message);

        } catch (Exception e) {
            e.printStackTrace();
        }


    }
}
