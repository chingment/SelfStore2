package com.uplink.selfstore.model.api;

import java.io.Serializable;
import java.util.List;

public class ExHandleItemBean implements Serializable {
    private   String itemId;

    private List<ExHandleUniqueBean> uniques;


    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public List<ExHandleUniqueBean> getUniques() {
        return uniques;
    }

    public void setUniques(List<ExHandleUniqueBean> uniques) {
        this.uniques = uniques;
    }
}
