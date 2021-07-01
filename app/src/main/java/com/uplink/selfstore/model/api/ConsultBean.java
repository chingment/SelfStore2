package com.uplink.selfstore.model.api;

import java.io.Serializable;

public class ConsultBean implements Serializable {

    private String csrQrCode;
    private String csrPhoneNumber;
    private String csrHelpTip;

    public String getCsrHelpTip() {
        return csrHelpTip;
    }

    public void setCsrHelpTip(String csrHelpTip) {
        this.csrHelpTip = csrHelpTip;
    }

    public String getCsrQrCode() {
        return csrQrCode;
    }

    public void setCsrQrCode(String csrQrCode) {
        this.csrQrCode = csrQrCode;
    }

    public String getCsrPhoneNumber() {
        return csrPhoneNumber;
    }

    public void setCsrPhoneNumber(String csrPhoneNumber) {
        this.csrPhoneNumber = csrPhoneNumber;
    }
}
