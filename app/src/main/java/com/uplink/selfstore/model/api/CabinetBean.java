package com.uplink.selfstore.model.api;

import java.io.Serializable;

public class CabinetBean implements Serializable {
    private String id;
    private String name;
    private int[] rowColLayout;
    private int[] pendantRows;

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

    public int[] getRowColLayout() {
        return rowColLayout;
    }

    public void setRowColLayout(int[] rowColLayout) {
        this.rowColLayout = rowColLayout;
    }

    public int[] getPendantRows() {
        return pendantRows;
    }

    public void setPendantRows(int[] pendantRows) {
        this.pendantRows = pendantRows;
    }
}
