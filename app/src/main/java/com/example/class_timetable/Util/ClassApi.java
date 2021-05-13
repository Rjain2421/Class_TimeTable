package com.example.class_timetable.Util;

import com.example.class_timetable.model.ClassDetail;

public class ClassApi {
    private String username,userId;
    private static ClassApi instance;
    public static ClassApi getInstance(){
        if(instance==null){
            instance=new ClassApi();
        }
        return instance;
    }
    public ClassApi(){}

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }


}
