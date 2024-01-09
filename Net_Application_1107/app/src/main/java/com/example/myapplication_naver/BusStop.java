package com.example.myapplication_naver;

import com.naver.maps.geometry.LatLng;

public class BusStop {
    private String name;        //정류소 이름
    private String id;          //정류소 아이디
    private LatLng latLng;      //정류소 좌표
    private int busstopSeq;     //정류장 순번

    public BusStop(String name, String id, LatLng latLng, int busstopSeq) {
        this.name = name;
        this.id = id;
        this.latLng = latLng;
        this.busstopSeq = busstopSeq;
    }

    public String getBusStopName(){
        return name;
    }

    public String getBusStopID(){
        return id;
    }

    public Double getBusStopLatitude(){
        return latLng.latitude;
    }

    public Double getBusStopLongitude(){
        return latLng.longitude;
    }

    public int getBusstopSeq(){ return busstopSeq; }
}
