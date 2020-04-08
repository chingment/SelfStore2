package com.uplink.selfstore.model.api;

import java.io.Serializable;
import java.util.List;

public class ExHandleOrderBean implements Serializable {
    private   String id;

    private String sn;

    private List<ExHandleOrderDetailItemBean> detailItems;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSn() {
        return sn;
    }

    public void setSn(String sn) {
        this.sn = sn;
    }


    public List<ExHandleOrderDetailItemBean> getDetailItems() {
        return detailItems;
    }

    public void setDetailItems(List<ExHandleOrderDetailItemBean> detailItems) {
        this.detailItems = detailItems;
    }
}
