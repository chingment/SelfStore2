package com.uplink.selfstore.model.api;

import java.io.Serializable;
import java.util.List;

public class MachineGetRunExHandleItemsResultBean  implements Serializable {


    private List<ExHandleReasonBean> exReasons;

    private List<ExHandleOrderBean> exOrders;

    public List<ExHandleReasonBean> getExReasons() {
        return exReasons;
    }

    public void setExReasons(List<ExHandleReasonBean> exReasons) {
        this.exReasons = exReasons;
    }

    public List<ExHandleOrderBean> getExOrders() {
        return exOrders;
    }

    public void setExOrders(List<ExHandleOrderBean> exOrders) {
        this.exOrders = exOrders;
    }

}
