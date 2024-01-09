package com.example.myapplication_naver;

public class Bus {
    private String carRegNo;    //차량 번호
    private String routeId;     //노선 id
    private int type;           //차량 타입 0:정보없음 1:일반 2:저상

    //public Bus(){ }
    public Bus(String routeId, String carRegNo, int type){
        this.routeId = routeId;
        this.carRegNo = carRegNo;
        this.type = type;
    }

    public String GetBusRouteId(){
        return routeId;
    }

    public String GetBusCarRegNo(){
        return carRegNo;
    }

    public int GetBusType(){
        return type;
    }
}
