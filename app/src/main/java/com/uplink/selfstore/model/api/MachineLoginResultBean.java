package com.uplink.selfstore.model.api;

import java.io.Serializable;

/**
 * Created by chingment on 2019/2/21.
 */

public class MachineLoginResultBean implements Serializable {

    private String machineId;
    private String userName;

    public String getMachineId() {
        return machineId;
    }

    public void setMachineId(String machineId) {
        this.machineId = machineId;
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


