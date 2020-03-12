package com.uplink.selfstore.model;

import java.io.Serializable;

public class DSCabRowColLayoutBean implements Serializable {

    private int[] rows;
    private int[] pendantRows;

    public int[] getRows() {
        return rows;
    }

    public void setRows(int[] rows) {
        this.rows = rows;
    }

    public int[] getPendantRows() {
        return pendantRows;
    }

    public void setPendantRows(int[] pendantRows) {
        this.pendantRows = pendantRows;
    }
}
