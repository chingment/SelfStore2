package com.uplink.selfstore.ostCtrl;

public class OstCtrlInterface {

    private static IOstCtrl ostCtrl;

    public static void init(String name) {
        if(name==null){
            ostCtrl = new OstCtrlByYs();
        }
        else {
            switch (name) {
                case "SX":
                    ostCtrl = new OstCtrlBySx();
                    break;
                case "YS":
                    ostCtrl = new OstCtrlByYs();
                    break;
                default:
                    ostCtrl = new OstCtrlByYs();
                    break;
            }
        }
    }

    public static IOstCtrl getInstance(){
        return ostCtrl;
    }
}
