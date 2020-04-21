package com.uplink.selfstore.systemCtrl;

public class SystemCtrlInterface {

    private static  ISystemCtrl systemCtrl;

    public static void init(String name) {

        if(name==null){
            systemCtrl = new SystemCtrlByYs();
        }
        else {
            switch (name) {
                case "SX":
                    systemCtrl = new SystemCtrlBySx();
                    break;
                case "YS":
                    systemCtrl = new SystemCtrlByYs();
                    break;
                default:
                    systemCtrl = new SystemCtrlByYs();
                    break;
            }
        }
    }

    public static ISystemCtrl getInstance(){
        return systemCtrl;
    }
}
