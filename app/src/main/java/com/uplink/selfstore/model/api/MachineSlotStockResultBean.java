package com.uplink.selfstore.model.api;

import java.io.Serializable;
import java.util.HashMap;

public class MachineSlotStockResultBean implements Serializable {

    private HashMap<String, SlotProductSkuBean> slotStocks;

    public HashMap<String, SlotProductSkuBean> getSlotStocks() {
        return slotStocks;
    }

    public void setSlotStocks(HashMap<String, SlotProductSkuBean> slotStocks) {
        this.slotStocks = slotStocks;
    }
}
