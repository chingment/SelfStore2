package com.uplink.selfstore.model;

import java.io.Serializable;
import java.util.List;

public class ZsCabRowLayoutBean implements Serializable {
    private  int index;
    private  String id;
    private List<ZSCabColLayoutBean> cols;

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

    public List<ZSCabColLayoutBean> getCols() {
        return cols;
    }

    public void setCols(List<ZSCabColLayoutBean> cols) {
        this.cols = cols;
    }
}
