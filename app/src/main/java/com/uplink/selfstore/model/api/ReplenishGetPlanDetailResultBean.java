package com.uplink.selfstore.model.api;

import java.io.Serializable;
import java.util.HashMap;

public class ReplenishGetPlanDetailResultBean implements Serializable {
    private HashMap<String, ReplenishCabinetBean> cabinets;

    public HashMap<String, ReplenishCabinetBean> getCabinets() {
        return cabinets;
    }

    public void setCabinets(HashMap<String, ReplenishCabinetBean> cabinets) {
        this.cabinets = cabinets;
    }
}
