package com.uplink.selfstore.model.api;

import java.io.Serializable;

public class FingerVeinnerBean implements Serializable {
    private String fingerVeinnerId;
    private Boolean isUse;


    public String getFingerVeinnerId() {
        return fingerVeinnerId;
    }

    public void setFingerVeinnerId(String fingerVeinnerId) {
        this.fingerVeinnerId = fingerVeinnerId;
    }

    public Boolean getUse() {
        return isUse;
    }

    public void setUse(Boolean use) {
        isUse = use;
    }
}
