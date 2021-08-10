package com.uplink.selfstore.model.push;

import java.io.Serializable;

public class SetSysStatusBean implements Serializable {
    private int status;
    private String helpTip="";

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getHelpTip() {
        return helpTip;
    }

    public void setHelpTip(String helpTip) {
        this.helpTip = helpTip;
    }
}
