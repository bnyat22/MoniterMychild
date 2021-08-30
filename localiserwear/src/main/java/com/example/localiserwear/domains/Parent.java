package com.example.localiserwear.domains;

import android.app.Application;

import java.util.Map;

public class Parent extends Application {
   private static String parentId;
   private static String childName;
   private static Map<String , String> authorities;
   private static String parentTelNum;
   private static double lat;

    public static double getLat() {
        return lat;
    }

    public static void setLat(double lat) {
        Parent.lat = lat;
    }

    public static double getLongt() {
        return longt;
    }

    public static void setLongt(double longt) {
        Parent.longt = longt;
    }

    private static double longt;


    public static String getParentTelNum() {
        return parentTelNum;
    }

    public static void setParentTelNum(String parentTelNum) {
        Parent.parentTelNum = parentTelNum;
    }

    public static Map<String, String> getAuthorities() {
        return authorities;
    }

    public static void setAuthorities(Map<String, String> authorities) {
        Parent.authorities = authorities;
    }

    public static String getChildName() {
        return childName;
    }

    public static void setChildName(String childName) {
        Parent.childName = childName;
    }

    public static String getParentId() {
        return parentId;
    }

    public static void setParentId(String parentId) {
        Parent.parentId = parentId;
    }
}
