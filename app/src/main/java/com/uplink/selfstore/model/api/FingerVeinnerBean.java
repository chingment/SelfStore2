package com.uplink.selfstore.model.api;

import java.io.Serializable;

public class FingerVeinnerBean implements Serializable {
    private String id;
    private Boolean isUse;

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
}
