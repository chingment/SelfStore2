package com.uplink.selfstore.model.api;

import java.io.Serializable;
import java.util.List;

public class ReplenishGetPlansResultBean implements Serializable {

    private List<ReplenishPlanBean> items;

    public List<ReplenishPlanBean> getItems() {
        return items;
    }

    public void setItems(List<ReplenishPlanBean> items) {
        this.items = items;
    }
}
