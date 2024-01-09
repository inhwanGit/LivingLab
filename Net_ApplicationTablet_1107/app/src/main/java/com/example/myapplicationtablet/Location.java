package com.example.myapplicationtablet;

public class Location {
    private Double latitude;
    private Double longitude;

    public Location(){}
    public Location(Double latitude,Double longitude){
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Double GetLat(){
        return latitude;
    }

    public Double GetLong(){
        return longitude;
    }

}
