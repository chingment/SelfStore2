package com.uplink.selfstore.model.push;

import java.io.Serializable;

public class UpdateHomeLogoBean implements Serializable {
    private String url;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
