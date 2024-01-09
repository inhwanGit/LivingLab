package com.example.myapplicationtablet;

public class Bus {
    private String carRegNo;    //차량 번호
    private String routeCd;     //노선 번호 유니크 값(7자리)
    private String routeNo;     //노선 번호
    private int routeTp;        //노선 타입
    private int type;           //차량 타입 0:정보없음 1:일반 2:저상

    public Bus(){ }
    public Bus(String routeId, String carRegNo, int type){
        this.routeCd = routeId;
        this.carRegNo = carRegNo;
        this.type = type;
    }

    public void SetBusRouteCd(String routeCd){
        this.routeCd = routeCd;
    }

    public void SetBusRouteNo(String routeNo){
        this.routeNo = routeNo;
    }

    public void SetBusRouteTp(int routeTp){
        this.routeTp = routeTp;
    }

    public void SetBusCarRegNo(String carRegNo){
        this.carRegNo = carRegNo;
    }

    public void SetBusType(int type){
        this.type = type;
    }

    public String GetBusRouteCd(){
        return routeCd;
    }

    public String GetBusRouteNo(){
        return routeNo;
    }

    public int GetBusRouteTp(){
        return routeTp;
    }

    public String GetBusCarRegNo(){
        return carRegNo;
    }

    public int GetBusType(){
        return type;
    }
}
