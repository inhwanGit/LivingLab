package com.example.myapplicationtablet;

public class BusStop {
    private String id;
    private String name;

    public BusStop(String id, String name){
        this.id = id;
        this.name = name;
    }

    public String GetId(){
        return id;
    }
    public String GetName(){
        return name;
    }
}
