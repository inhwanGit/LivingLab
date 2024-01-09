package com.example.myapplication_naver;

public class PubTransPath_Info {
    int num;                             //순번
    int totalTime;                      //총 소요시간
    int payment;                        //총 요금
    int busTransitCount;                //총 환승 카운트
    String firstStartStation;           //최초 출발 정류장
    String lastEndStation;              //최초 도착 정류장
    int busStationCount;                //총 버스정류장 합
    double totalDistance;               //총 거리

    public PubTransPath_Info(int num, int totalTime, int payment, int busTransitCount, String firstStartStation, String lastEndStation, int busStationCount, double totalDistance){
        this.num = num;
        this.totalTime = totalTime;
        this.payment = payment;
        this.busTransitCount = busTransitCount;
        this.firstStartStation = firstStartStation;
        this.lastEndStation = lastEndStation;
        this.busStationCount = busStationCount;
        this.totalDistance = totalDistance;
    }

    public int GetNum(){
        return num;
    }

    public int GetTotalTime(){
        return totalTime;
    }

    public int GetPayment(){
        return payment;
    }

    public int GetBusTransitCount(){
        return busTransitCount;
    }

    public String GetFirstStartStation(){
        return firstStartStation;
    }

    public String GetLastEndStation(){
        return lastEndStation;
    }

    public int GetBusStationCount(){
        return busStationCount;
    }

    public double GetTotalDistance(){
        return totalDistance;
    }
}
