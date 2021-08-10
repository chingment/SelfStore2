package com.uplink.selfstore.model.api;

import java.io.Serializable;
import java.util.List;

public class LightingBean implements Serializable {

    public List<LightingCbBean>  cbs;


    public List<LightingCbBean> getCbs() {
        return cbs;
    }

    public void setCbs(List<LightingCbBean> cbs) {
        this.cbs = cbs;
    }
}
