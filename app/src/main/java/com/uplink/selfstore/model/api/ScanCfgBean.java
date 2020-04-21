package com.uplink.selfstore.model.api;

import java.io.Serializable;

public class ScanCfgBean implements Serializable {

    private String id;
    private Boolean isUse;
    private String comId;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Boolean getUse() {
        return isUse;
    }

    public void setUse(Boolean use) {
        isUse = use;
    }

    public String getComId() {
        return comId;
    }

    public void setComId(String comId) {
        this.comId = comId;
    }

}