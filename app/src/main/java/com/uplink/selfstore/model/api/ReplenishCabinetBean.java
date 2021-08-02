package com.uplink.selfstore.model.api;

import java.io.Serializable;
import java.util.HashMap;

public class ReplenishCabinetBean implements Serializable {
    private String rowColLayout;
    private HashMap<String, ReplenishSlotBean> slots;

    public String getRowColLayout() {
        return rowColLayout;
    }

    public void setRowColLayout(String rowColLayout) {
        this.rowColLayout = rowColLayout;
    }

    public HashMap<String, ReplenishSlotBean> getSlots() {
        return slots;
    }

    public void setSlots(HashMap<String, ReplenishSlotBean> slots) {
        this.slots = slots;
    }
}
