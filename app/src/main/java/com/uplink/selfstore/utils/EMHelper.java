package com.uplink.selfstore.utils;

public class EMHelper {

    private static EMHelper instance = null;

    public boolean isVideoCalling;

    public synchronized static EMHelper getInstance() {
        if (instance == null) {
            instance = new EMHelper();
        }
        return instance;
    }



}
