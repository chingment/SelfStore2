package com.uplink.selfstore.model.api;

import java.io.Serializable;

public class TerminalPayOptionBean implements Serializable {
    private  int caller;
    private  int partner;
    private  int[] supportWays;

    public int getCaller() {
        return caller;
    }

    public void setCaller(int caller) {
        this.caller = caller;
    }

    public int getPartner() {
        return partner;
    }

    public void setPartner(int partner) {
        this.partner = partner;
    }

    public int[] getSupportWays() {
        return supportWays;
    }

    public void setSupportWays(int[] supportWays) {
        this.supportWays = supportWays;
    }
}
