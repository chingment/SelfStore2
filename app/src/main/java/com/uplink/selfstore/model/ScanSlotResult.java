package com.uplink.selfstore.model;

import java.io.Serializable;

public class ScanSlotResult implements Serializable {

    public long useTime;
    public int rows;
    public int[] rowColLayout;

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        this.rows = rows;
    }

    public int[] getRowColLayout() {
        return rowColLayout;
    }

    public void setRowColLayout(int[] rowColLayout) {
        this.rowColLayout = rowColLayout;
    }

    public long getUseTime() {
        return useTime;
    }

    public void setUseTime(long useTime) {
        this.useTime = useTime;
    }
}