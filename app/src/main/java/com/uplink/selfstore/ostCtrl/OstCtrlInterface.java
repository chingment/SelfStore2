package com.uplink.selfstore.ostCtrl;

public class OstCtrlInterface {

    private static IOstCtrl ostCtrl;

    public static void init(String model) {
        if(model==null){
            ostCtrl = new OstCtrlByYs();
        }
        else {
            switch (model) {
                case "SABRESD-MX6DQ":
                    ostCtrl = new OstCtrlBySx();
                    break;
                case "rk3399-all":
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
