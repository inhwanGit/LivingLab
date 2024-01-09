package com.example.myapplication_naver;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.naver.maps.geometry.LatLng;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;

public class GetBusPosByRtid_Thread extends Thread{
    private String serviceUrl = "http://openapitraffic.daejeon.go.kr/api/rest/busposinfo/";
    private String function = "getBusPosByRtid";
    private String serviceKey = "QWUgpO%2Bo3SdnRO6RDHQJXYSTVdFK%2B5ajLGxO5ZiHULK23HApbBbcjgeXL%2BEO6%2BOBa0Ik7JnQW4axGE6%2BxtJxZw%3D%3D";
    //private String serviceKey = "%2BmuvUGu4XWbAMAXl1yBcuCAXW0l3GGQWNznkWoPExJgMZGPpKVTJ2WUh59x5u0IuMhwbAV9IiEsvm7L1T%2FpboA%3D%3D";
    private String busRouteId = "busRouteId";
    private String data = "";

    private XmlPullParser parser;
    private Handler handler;

    private String headerCd="";

    private boolean bus_headerCd = false;
    private boolean bus_plateNo = false;
    private boolean bus_gpsLati = false;
    private boolean bus_gpsLong = false;
    private boolean bus_busStopId7 = false;
    private boolean bus_busStopId5 = false;

    private ArrayList<String> plateNoList = new ArrayList<>();
    private ArrayList<String> gpsLatiList = new ArrayList<>();
    private ArrayList<String> gpsLongList = new ArrayList<>();
    private ArrayList<String> busStopId7List = new ArrayList<>();
    private ArrayList<String> busStopId5List = new ArrayList<>();

    public GetBusPosByRtid_Thread (Handler handler, String data){
        this.handler = handler;
        this.data = data;
    }

    @Override
    public void run() {
        try{
            String strUrl = serviceUrl + function +"?serviceKey=" + serviceKey + "&" + busRouteId + "=" + data;

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
                        case "PLATE_NO": bus_plateNo = true;        break;
                        case "GPS_LATI": bus_gpsLati = true;        break;
                        case "GPS_LONG": bus_gpsLong = true;        break;
                        case "BUS_NODE_ID" : bus_busStopId7 = true;    break;
                        case "BUS_STOP_ID" : bus_busStopId5 = true;     break;
                    }
                } else if(eventType == XmlPullParser.TEXT){
                    if(bus_headerCd) {
                        headerCd = parser.getText();
                        bus_headerCd = false;
                    }
                    if("0".equals(headerCd)){
                        if(bus_plateNo){
                            //Log.d("BusStopArrival_Thread", parser.getText());
                            plateNoList.add(parser.getText());
                            bus_plateNo = false;
                        }
                        else if(bus_gpsLati){
                            //Log.d("BusStopArrival_Thread", parser.getText());
                            gpsLatiList.add(parser.getText());
                            bus_gpsLati = false;
                        }
                        else if(bus_gpsLong){
                            //Log.d("BusStopArrival_Thread", parser.getText());
                            gpsLongList.add(parser.getText());
                            bus_gpsLong = false;
                        }
                        else if(bus_busStopId7){
                            busStopId7List.add(parser.getText());
                            bus_busStopId7 = false;
                        }
                        else if(bus_busStopId5){
                            busStopId5List.add(parser.getText());
                            bus_busStopId5 = false;
                        }
                    }
                } else if(eventType == XmlPullParser.END_TAG){
                    ;
                }
                eventType = parser.next();
            }

            MainActivity.plateNoList.addAll(plateNoList);
            MainActivity.bus_gpsLatiList.addAll(gpsLatiList);
            MainActivity.bus_gpsLongList.addAll(gpsLongList);
            MainActivity.busStopId7List.addAll(busStopId7List);
            MainActivity.busStopId5List.addAll(busStopId5List);

            //메세지 저장 및 전송
            bundle.putBoolean("GetBusPosByRtid_Thread", true);
            bundle.putStringArrayList("PLATE_NO", plateNoList);
            bundle.putStringArrayList("GPS_LATI", gpsLatiList);
            bundle.putStringArrayList("GPS_LONG", gpsLongList);
            message.setData(bundle);
            handler.sendMessage(message);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
