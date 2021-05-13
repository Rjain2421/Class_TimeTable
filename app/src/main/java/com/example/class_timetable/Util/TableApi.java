package com.example.class_timetable.Util;

public class TableApi {
    private String userId;
    private static TableApi instance;
    public static TableApi getInstance(){
        if(instance==null){
            instance=new TableApi();
        }
        return instance;
    }
    public TableApi(){}

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }


}
