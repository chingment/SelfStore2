package com.uplink.selfstore.model.api;

import java.io.Serializable;
import java.util.HashMap;

public class ReplenishGetPlanDetailResultBean implements Serializable {
    private HashMap<String, CabinetBean> cabinets;

    public HashMap<String, CabinetBean> getCabinets() {
        return cabinets;
    }

    public void setCabinets(HashMap<String, CabinetBean> cabinets) {
        this.cabinets = cabinets;
    }
}
