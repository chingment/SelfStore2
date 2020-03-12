package com.uplink.selfstore.model.api;

import java.io.Serializable;

public class CabinetBean implements Serializable {
    private String id;
    private String name;
    private String rowColLayout;

    public String getRowColLayout() {
        return rowColLayout;
    }

    public void setRowColLayout(String rowColLayout) {
        this.rowColLayout = rowColLayout;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
