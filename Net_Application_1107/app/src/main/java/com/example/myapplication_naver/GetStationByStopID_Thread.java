package com.example.myapplication_naver;

import android.os.Handler;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;

public class GetStationByStopID_Thread extends Thread{
    private String serviceUrl = "http://openapitraffic.daejeon.go.kr/api/rest/stationinfo/";
    private String function = "getStationByStopID";
    private String serviceKey = "QWUgpO%2Bo3SdnRO6RDHQJXYSTVdFK%2B5ajLGxO5ZiHULK23HApbBbcjgeXL%2BEO6%2BOBa0Ik7JnQW4axGE6%2BxtJxZw%3D%3D";
    private String request = "BusStopID";
    private String data = "";

    private String msg="";

    private XmlPullParser parser;
    private Handler handler;

    private boolean bus_headerCd = false;
    private boolean bus_aroBusstopId = false;
    private boolean bus_gpsLati = false;
    private boolean bus_gpsLong = false;
    private boolean bus_busstop_nm = false;

    private String headerCd="";
    private String aroBusstopId="";
    private Double gpsLati;
    private Double gpsLong;
    private String busstop_nm;

    public GetStationByStopID_Thread(Handler handler, String data, String msg){
        this.handler = handler;
        this.data = data;
        this.msg = msg;
    }

    @Override
    public void run() {
        try {
            String strUrl = serviceUrl + function + "?serviceKey=" + serviceKey + "&" + request + "=" + data;

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
                if (eventType == XmlPullParser.START_DOCUMENT) {
                    ;
                } else if (eventType == XmlPullParser.START_TAG) {
                    String tag_name = parser.getName();
                    switch (tag_name) {
                        case "headerCd": bus_headerCd = true;               break;
                        case "ARO_BUSSTOP_ID": bus_aroBusstopId = true;     break;
                        case "GPS_LATI": bus_gpsLati = true;                break;
                        case "GPS_LONG": bus_gpsLong = true;                break;
                        case "BUSSTOP_NM" : bus_busstop_nm = true;          break;

                    }
                } else if (eventType == XmlPullParser.TEXT) {

                    if (bus_headerCd) {
                        headerCd = parser.getText();
                        bus_headerCd = false;
                    }
                    if ("0".equals(headerCd)) {
                        if (bus_aroBusstopId) {
                            aroBusstopId = parser.getText();
                            bus_aroBusstopId = false;
                        }
                        else if(bus_gpsLati){
                            gpsLati = Double.parseDouble(parser.getText());
                            bus_gpsLati = false;
                        }
                        else if(bus_gpsLong){
                            gpsLong = Double.parseDouble(parser.getText());
                            bus_gpsLong = false;
                        }
                        else if(bus_busstop_nm){
                            busstop_nm = parser.getText();
                            bus_busstop_nm = false;
                        }
                    }
                } else if (eventType == XmlPullParser.END_TAG) {
                }
                eventType = parser.next();
            }

            if(msg.equals("MainActivity")){
                //메인 변수에 저장
                MainActivity.aroBusstopId = aroBusstopId;
                MainActivity.busStopNM = busstop_nm;
            }
            else if(msg.equals("BusStopActivity")){
                BusStop_Activity.busStop_gpsLati = gpsLati;
                BusStop_Activity.busStop_gpsLong = gpsLong;
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
