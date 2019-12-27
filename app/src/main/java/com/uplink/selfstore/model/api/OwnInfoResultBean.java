package com.uplink.selfstore.model.api;

import java.io.Serializable;

public class OwnInfoResultBean implements Serializable {
    private String introduction;
    private String avatar;
    private String fullName;
    private String userName;
    private String email;
    private String phoneNumber;
    private int fingerVinCount;

    public String getIntroduction() {
        return introduction;
    }

    public void setIntroduction(String introduction) {
        this.introduction = introduction;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public int getFingerVinCount() {
        return fingerVinCount;
    }

    public void setFingerVinCount(int fingerVinCount) {
        this.fingerVinCount = fingerVinCount;
    }
}
