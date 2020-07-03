package com.uplink.selfstore.model.chat;

public class CustomMsg<T> {
    public String type;
    public T content;
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }
    public T getContent() {
        return content;
    }
    public void setContent(T content) {
        this.content = content;
    }
}