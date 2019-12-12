package com.uplink.selfstore.model.api;

import java.io.Serializable;
import java.util.HashMap;

public class MachineSlotsResultBean implements Serializable {
    private int[] rowColLayout;
    private int[] pendantRows;

    private HashMap<String, SlotBean> slots;

    public int[] getRowColLayout() {
        return rowColLayout;
    }

    public void setRowColLayout(int[] rowColLayout) {
        this.rowColLayout = rowColLayout;
    }

    public HashMap<String, SlotBean> getSlots() {
        return slots;
    }

    public void setSlots(HashMap<String, SlotBean> slots) {
        this.slots = slots;
    }

    public int[] getPendantRows() {
        return pendantRows;
    }

    public void setPendantRows(int[] pendantRows) {
        this.pendantRows = pendantRows;
    }
}
