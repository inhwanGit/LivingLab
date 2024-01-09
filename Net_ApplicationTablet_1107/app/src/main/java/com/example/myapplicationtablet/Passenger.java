package com.example.myapplicationtablet;

public class Passenger {
    private String id;
    private int tableId;
    private int type;
    private BusStop start;
    private BusStop end;

    public Passenger(String id, int tableId, int type, BusStop start, BusStop end){
        this.id = id;
        this.tableId = tableId;
        this.type = type;
        this.start = start;
        this.end = end;
    }

    public String GetId() {
        return id;
    }
    public int GetTableId(){
        return tableId;
    }
    public int GetType(){
        return type;
    }
    public BusStop GetStart(){
        return start;
    }
    public BusStop GetEnd(){
        return end;
    }
}
