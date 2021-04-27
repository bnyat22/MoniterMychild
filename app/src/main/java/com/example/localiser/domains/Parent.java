package com.example.localiser.domains;

import android.app.Application;

public class Parent extends Application {
   private String parentId;

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }
}
