package com.uplink.selfstore.model.api;

import java.io.Serializable;

/**
 * Created by chingment on 2019/2/21.
 */

public class DeviceLoginResultBean implements Serializable {

    private String deviceId;
    private String userName;

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    private String password;
}


