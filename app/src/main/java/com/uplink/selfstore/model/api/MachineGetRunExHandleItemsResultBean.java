package com.uplink.selfstore.model.api;

import java.io.Serializable;
import java.util.List;

public class MachineGetRunExHandleItemsResultBean  implements Serializable {

    private List<ExHandleOrderBean> exOrders;

    public List<ExHandleOrderBean> getExOrders() {
        return exOrders;
    }

    public void setExOrders(List<ExHandleOrderBean> exOrders) {
        this.exOrders = exOrders;
    }

}
