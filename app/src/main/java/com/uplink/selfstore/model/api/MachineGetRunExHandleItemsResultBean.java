package com.uplink.selfstore.model.api;

import java.io.Serializable;
import java.util.List;

public class MachineGetRunExHandleItemsResultBean  implements Serializable {


    private List<ExHandleReasonBean> exReasons;

    private List<ExHandleItemBean> exItems;

    public List<ExHandleReasonBean> getExReasons() {
        return exReasons;
    }

    public void setExReasons(List<ExHandleReasonBean> exReasons) {
        this.exReasons = exReasons;
    }


    public List<ExHandleItemBean> getExItems() {
        return exItems;
    }

    public void setExItems(List<ExHandleItemBean> exItems) {
        this.exItems = exItems;
    }
}
