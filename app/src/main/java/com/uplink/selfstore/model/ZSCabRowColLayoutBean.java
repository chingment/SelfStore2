package com.uplink.selfstore.model;

import java.io.Serializable;
import java.util.List;

public class ZSCabRowColLayoutBean implements Serializable {
    private List<List<String>>  rows;


    public List<List<String>> getRows() {
        return rows;
    }

    public void setRows(List<List<String>> rows) {
        this.rows = rows;
    }
}
