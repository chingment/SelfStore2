package com.uplink.selfstore.model.api;

import java.io.Serializable;
import java.util.List;

public class RetReplenishGetPlans implements Serializable {

    private int total;
    private int pageSize;

    private List<ReplenishPlanBean> items;

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public List<ReplenishPlanBean> getItems() {
        return items;
    }

    public void setItems(List<ReplenishPlanBean> items) {
        this.items = items;
    }
}
