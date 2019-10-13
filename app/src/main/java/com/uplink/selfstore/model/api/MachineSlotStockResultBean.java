package com.uplink.selfstore.model.api;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

public class MachineSlotStockResultBean implements Serializable {
    public HashMap<String, SlotProductSkuBean> getSlotProductSkus() {
        return slotProductSkus;
    }

    public void setSlotProductSkus(HashMap<String, SlotProductSkuBean> slotProductSkus) {
        this.slotProductSkus = slotProductSkus;
    }

    private HashMap<String, SlotProductSkuBean> slotProductSkus;

}
