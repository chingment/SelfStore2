package com.uplink.selfstore.model.api;

import java.io.Serializable;
import java.util.HashMap;

public class MachineSlotsResultBean implements Serializable {
    private  String rowColLayout;
    private int fixSlotQuantity;

    public int getFixSlotQuantity() {
        return fixSlotQuantity;
    }

    public void setFixSlotQuantity(int fixSlotQuantity) {
        this.fixSlotQuantity = fixSlotQuantity;
    }

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
