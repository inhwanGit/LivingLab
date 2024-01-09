package com.example.myapplicationtablet;

import com.google.gson.JsonObject;

import java.io.Serializable;

public class User implements Serializable {
    private int num;            //순번
    private String id;          //식별번호
    private int type;           //장애유형
    private int event;          //이벤트 타입
    private String bus;         //승차할 버스의 차번호
    private Location location;  //현재 나의 위치
    private BusStop current;     //현재 정류장(대기시 null, 탑승시 현재정류장)
    private BusStop start;      //승차 정류장
    private BusStop end;        //하차 정류장
    private String destination; //목적지 주소

    public User(String id, int type, int event, String bus, Location location, BusStop current, BusStop start, BusStop end, String destination){
        this.id = id;
        this.type = type;
        this.event = event;
        this.bus = bus;
        this.location = location;
        this.current = current;
        this.start = start;
        this.end = end;
        this.destination = destination;
    }

    public void SetNum(int num){
        this.num = num;
    }

    public String GetId(){
        return id;
    }
    public int GetType(){
        return type;
    }
    public int GetEvent(){
        return event;
    }
    public String GetBus(){
        return bus;
    }
    public Location GetLocation(){
        return location;
    }
    public BusStop GetCurrent(){
        return current;
    }
    public BusStop GetStart(){
        return start;
    }
    public BusStop GetEnd(){
        return end;
    }
    public String GetDestination(){
        return destination;
    }
    public int GetNum(){
        return num;
    }


    @Override
    public String toString(){
        String text = "num : " + num + ", " + "id : " + id + ", " + "type : " + type + ", " + "event : " + event + ", " + "bus : " + bus + ", "
                 + "location : " + location.GetLat() + "/" + location.GetLong() + ", " + "current : " + current.GetName() + ", "
                 + "start : " + start.GetName() + ", " + "end : " + end.GetName() + ", " + "destination : " + destination;

        return text;
    }
}
