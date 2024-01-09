package com.example.myapplicationtablet;

import android.os.Handler;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;

public class GetStationByUid_Thread extends Thread{
    private String serviceUrl = "http://openapitraffic.daejeon.go.kr/api/rest/stationinfo/";
    private String function = "getStationByUid";
    private String serviceKey = "QWUgpO%2Bo3SdnRO6RDHQJXYSTVdFK%2B5ajLGxO5ZiHULK23HApbBbcjgeXL%2BEO6%2BOBa0Ik7JnQW4axGE6%2BxtJxZw%3D%3D";
    private String request = "arsId";
    private String data = "";

    private XmlPullParser parser;
    private Handler handler;

    private boolean bus_headerCd = false;
    private boolean bus_aroBusstopId = false;
    private boolean bus_lat = false;
    private boolean bus_lng = false;

    private String headerCd="";
    private String aroBusstopId="";
    private Location location;
    private Double gpslat;
    private Double gpslng;

    public GetStationByUid_Thread(Handler handler, String data){
        this.handler = handler;
        this.data = data;
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
                        case "headerCd":
                            bus_headerCd = true;
                            break;
                        case "ARO_BUSSTOP_ID":
                            bus_aroBusstopId = true;
                            break;
                        case "GPS_LATI":
                            bus_lat = true;
                            break;
                        case "GPS_LONG":
                            bus_lng = true;
                            break;
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
                        else if(bus_lat){
                            gpslat = Double.parseDouble(parser.getText());
                            bus_lat = false;
                        }
                        else if(bus_lng){
                            gpslng = Double.parseDouble(parser.getText());
                            bus_lng = false;
                        }
                    }
                } else if (eventType == XmlPullParser.END_TAG) {
                    location = new Location(gpslat, gpslng);
                }
                eventType = parser.next();
            }

            //메인 변수에 저장
            MainActivity.busStoplocation = location;


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
