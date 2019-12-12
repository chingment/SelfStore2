package com.uplink.selfstore.model.api;

import java.io.Serializable;

public class OpUserInfoBean implements Serializable {
    private String token;
    private String userName;
    private String fullName;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
}
