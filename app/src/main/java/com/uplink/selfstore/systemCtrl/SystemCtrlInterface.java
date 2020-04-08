package com.uplink.selfstore.systemCtrl;

public class SystemCtrlInterface {

    private static  ISystemCtrl systemCtrl;

    public static void init(String name) {

        systemCtrl = new SystemCtrlBySx();
    }

    public static ISystemCtrl getInstance(){
        return systemCtrl;
    }
}
