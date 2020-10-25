package com.uplink.selfstore.model.api;

import java.io.Serializable;

public class AdContentBean implements Serializable {

    private String dataType;
    private String dataUrl;

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public String getDataUrl() {
        return dataUrl;
    }

    public void setDataUrl(String dataUrl) {
        this.dataUrl = dataUrl;
    }
}
