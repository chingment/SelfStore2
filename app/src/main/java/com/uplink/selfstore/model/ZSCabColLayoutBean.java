package com.uplink.selfstore.model;

import java.io.Serializable;
import java.util.List;

public class ZSCabColLayoutBean implements Serializable {
    private  int index;
    private  String id;
    private  boolean canUse;

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isCanUse() {
        return canUse;
    }

    public void setCanUse(boolean canUse) {
        this.canUse = canUse;
    }
}
