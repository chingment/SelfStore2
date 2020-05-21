package com.uplink.selfstore.utils;

public class EasemobHelper {

    private static EasemobHelper instance = null;

    public boolean isVideoCalling;

    public synchronized static EasemobHelper getInstance() {
        if (instance == null) {
            instance = new EasemobHelper();
        }
        return instance;
    }



}
