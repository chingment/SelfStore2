package com.uplink.selfstore.model;

import java.io.Serializable;

public class CabinetBoxBean implements Serializable {

    private int id;
    private boolean isOpen;
    private boolean isNonGoods;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isOpen() {
        return isOpen;
    }

    public void setOpen(boolean open) {
        isOpen = open;
    }

    public boolean isNonGoods() {
        return isNonGoods;
    }

    public void setNonGoods(boolean nonGoods) {
        isNonGoods = nonGoods;
    }
}
