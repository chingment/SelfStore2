package com.uplink.selfstore.model.api;

import java.io.Serializable;
import java.util.HashMap;

public class MachineSlotsResultBean implements Serializable {
    private HashMap<String, SlotBean> slots;
    public HashMap<String, SlotBean> getSlots() {
        return slots;
    }

    public void setSlots(HashMap<String, SlotBean> slots) {
        this.slots = slots;
    }
}
