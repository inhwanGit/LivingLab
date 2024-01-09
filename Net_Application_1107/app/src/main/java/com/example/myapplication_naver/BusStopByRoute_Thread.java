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

public class BusStopByRoute_Thread extends Thread{
    private String serviceUrl = "http://openapitraffic.daejeon.go.kr/api/rest/busRouteInfo/";
    private String function = "getStaionByRoute";
    private String serviceKey = "%2BmuvUGu4XWbAMAXl1yBcuCAXW0l3GGQWNznkWoPExJgMZGPpKVTJ2WUh59x5u0IuMhwbAV9IiEsvm7L1T%2FpboA%3D%3D";
    private String request = "busRouteId";
    private String data = "";

    private XmlPullParser parser;
    private Handler handler;

    private String headerCd="";
    private String busstopNm ="";
    private String busnodeId = "";
    private String gpsLat ="";
    private String gpsLong ="";
    private int busstopSeq=0;

    private boolean bus_headerCd = false;
    private boolean bus_busstopNm = false;
    private boolean bus_busnodeId = false;
    private boolean bus_gpsLati = false;
    private boolean bus_gpsLong = false;
    private boolean bus_busstopSeq = false;

    private ArrayList<BusStop> busStops = new ArrayList<>();


    public BusStopByRoute_Thread(Handler handler, String data) {
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
                Message message = handler.obtainMessage();
                Bundle bundle = new Bundle();

                if(eventType == XmlPullParser.START_DOCUMENT){
                    ;
                } else if(eventType == XmlPullParser.START_TAG){
                    String tag_name = parser.getName();
                    switch (tag_name){
                        case "headerCd": bus_headerCd = true;           break;
                        case "BUSSTOP_NM": bus_busstopNm = true;        break;
                        case "BUS_NODE_ID": bus_busnodeId = true;       break;
                        case "GPS_LATI": bus_gpsLati = true;            break;
                        case "GPS_LONG": bus_gpsLong = true;            break;
                        case "BUSSTOP_SEQ" : bus_busstopSeq = true;     break;
                    }
                } else if(eventType == XmlPullParser.TEXT){

                    if(bus_headerCd) {
                        headerCd = parser.getText();
                        bus_headerCd = false;
                    }
                    if("0".equals(headerCd)){
                        if(bus_busstopNm) {
                            //Log.d("BusStopByRoute_Thread", parser.getText());
                            busstopNm = parser.getText();
                            bus_busstopNm = false;

                        } else if(bus_busnodeId) {
                            //Log.d("BusStopByRoute_Thread", parser.getText());
                            busnodeId = parser.getText();
                            bus_busnodeId = false;

                        } else if(bus_gpsLati) {
                            //Log.d("BusStopByRoute_Thread", parser.getText());
                            gpsLat = parser.getText();
                            bus_gpsLati = false;

                        } else if(bus_gpsLong) {
                            //Log.d("BusStopByRoute_Thread", parser.getText());
                            gpsLong = parser.getText();
                            bus_gpsLong = false;

                        }
                        else if(bus_busstopSeq){
                            //Log.d("BusStopByRoute_Thread", parser.getText());
                            busstopSeq = Integer.parseInt(parser.getText());
                            bus_busstopSeq = false;
                        }
                    }
                } else if(eventType == XmlPullParser.END_TAG){
                    String tag_name = parser.getName();
                    if(tag_name.equals("itemList")){

                        //버스정류장 객체
                        BusStop busStop = new BusStop(busstopNm, busnodeId, new LatLng(Double.parseDouble(gpsLat), Double.parseDouble(gpsLong)), busstopSeq);
                        busStops.add(busStop);

                        bundle.putBoolean("BusStopByRoute_Thread", true);
                        bundle.putString("BUSSTOP_NM", busstopNm);
                        bundle.putString("BUS_NODE_ID", busnodeId);
                        bundle.putString("GPS_LATI", gpsLat);
                        bundle.putString("GPS_LONG", gpsLong);
                        message.setData(bundle);
                        handler.sendMessage(message);
                    }
                }
                eventType = parser.next();
            }

            MainActivity.busStops.addAll(busStops);

        } catch (Exception e) {
            e.printStackTrace();
        }


    }
}
