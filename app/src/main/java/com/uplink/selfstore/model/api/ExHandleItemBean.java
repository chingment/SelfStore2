package com.uplink.selfstore.model.api;

import java.io.Serializable;
import java.util.List;

public class ExHandleItemBean implements Serializable {
    private   String id;

    private List<ExHandleUniqueBean> uniques;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }


    public List<ExHandleUniqueBean> getUniques() {
        return uniques;
    }

    public void setUniques(List<ExHandleUniqueBean> uniques) {
        this.uniques = uniques;
    }
}
