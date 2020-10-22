package com.uplink.selfstore.model.api;

import java.io.Serializable;

public class ScannerBean implements Serializable {

    private String scannerId;
    private Boolean isUse;
    private String comId;


    public String getScannerId() {
        return scannerId;
    }

    public void setScannerId(String scannerId) {
        this.scannerId = scannerId;
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