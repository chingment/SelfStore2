package com.uplink.selfstore.model.push;

import com.uplink.selfstore.model.api.ConsultBean;

import java.io.Serializable;
import java.util.HashMap;

public class SetSysParamsBean implements Serializable {

    private ConsultBean consult;

    private HashMap<String, String> lights;

    public HashMap<String, String> getLights() {
        return lights;
    }

    public void setLights(HashMap<String, String> lights) {
        this.lights = lights;
    }

    public ConsultBean getConsult() {
        return consult;
    }

    public void setConsult(ConsultBean consult) {
        this.consult = consult;
    }
}
