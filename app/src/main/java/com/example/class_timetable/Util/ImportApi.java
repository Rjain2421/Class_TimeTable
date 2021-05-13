package com.example.class_timetable.Util;

import com.example.class_timetable.model.ClassDetail;

import java.util.HashMap;
import java.util.List;

public class ImportApi {
    private HashMap<String,ClassDetail> importList;
    private static ImportApi instance;
    public static ImportApi getInstance(){
        if(instance==null){
            instance=new ImportApi();
        }
        return instance;
    }

    public ImportApi(){}

    public HashMap<String, ClassDetail> getImportList() {
        return importList;
    }

    public void setImportList(HashMap<String, ClassDetail> importList) {
        this.importList = importList;
    }
}
