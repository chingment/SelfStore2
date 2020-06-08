package com.uplink.selfstore.model.api;

import java.io.Serializable;
import java.util.List;

public class ImSeatBean  implements Serializable {
    private String userId;
    private String nickName;
    private String avatar;
    private String briefDes;
    private List<String> charTags;
    private String imUserName;
    private String imStatus;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getBriefDes() {
        return briefDes;
    }

    public void setBriefDes(String briefDes) {
        this.briefDes = briefDes;
    }

    public List<String> getCharTags() {
        return charTags;
    }

    public void setCharTags(List<String> charTags) {
        this.charTags = charTags;
    }

    public String getImUserName() {
        return imUserName;
    }

    public void setImUserName(String imUserName) {
        this.imUserName = imUserName;
    }

    public String getImStatus() {
        return imStatus;
    }

    public void setImStatus(String imStatus) {
        this.imStatus = imStatus;
    }
}
