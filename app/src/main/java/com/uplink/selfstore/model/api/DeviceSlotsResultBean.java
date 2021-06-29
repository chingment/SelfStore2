package com.uplink.selfstore.model.api;

import java.io.Serializable;
import java.util.HashMap;

public class DeviceSlotsResultBean implements Serializable {
    private  String rowColLayout;

    public String getRowColLayout() {
        return rowColLayout;
    }

    public void setRowColLayout(String rowColLayout) {
        this.rowColLayout = rowColLayout;
    }

    private HashMap<String, SlotBean> slots;

    public HashMap<String, SlotBean> getSlots() {
        return slots;
    }

    public void setSlots(HashMap<String, SlotBean> slots) {
        this.slots = slots;
    }

}
