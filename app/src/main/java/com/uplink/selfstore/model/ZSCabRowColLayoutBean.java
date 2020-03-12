package com.uplink.selfstore.model;

import java.io.Serializable;
import java.util.List;

public class ZSCabRowColLayoutBean implements Serializable {
    private List<ZsCabRowLayoutBean> rows;

    public List<ZsCabRowLayoutBean> getRows() {
        return rows;
    }

    public void setRows(List<ZsCabRowLayoutBean> rows) {
        this.rows = rows;
    }
}
