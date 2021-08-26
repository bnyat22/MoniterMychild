package com.example.localiser.domains;

import android.app.Application;

import java.util.HashMap;
import java.util.Map;

public class Parent extends Application {
   private String parentId;
   private String childName;
   private Map<String , String> authorities;
   private String parentTelNum;
   private double lat;

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLongt() {
        return longt;
    }

    public void setLongt(double longt) {
        this.longt = longt;
    }

    private double longt;


    public String getParentTelNum() {
        return parentTelNum;
    }

    public void setParentTelNum(String parentTelNum) {
        this.parentTelNum = parentTelNum;
    }

    public Map<String, String> getAuthorities() {
        return authorities;
    }

    public void setAuthorities(Map<String, String> authorities) {
        this.authorities = authorities;
    }

    public String getChildName() {
        return childName;
    }

    public void setChildName(String childName) {
        this.childName = childName;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }
}
