package com.uplink.selfstore.model.push;

import java.io.Serializable;
import java.util.HashMap;

public class SetSysParamsBean implements Serializable {
    private HashMap<String, String> lights;

    public HashMap<String, String> getLights() {
        return lights;
    }

    public void setLights(HashMap<String, String> lights) {
        this.lights = lights;
    }
}
