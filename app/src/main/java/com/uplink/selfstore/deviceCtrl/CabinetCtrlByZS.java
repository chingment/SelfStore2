package com.uplink.selfstore.deviceCtrl;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.uplink.selfstore.model.ResultBean;
import com.uplink.selfstore.model.ZSCabBoxBean;

import java.io.Serializable;
import java.util.HashMap;

public class CabinetCtrlByZS {
    private static final String TAG = "CabinetCtrlByZS";
    private static CabinetCtrlByZS mCabinetCtrlByZS= null;

    private String strPort="ttyS1";
    private int nBaudrate=115200;
    private Handler mHandler = null;
    public static final int MESSAGE_WHAT_UNLOCK = 1;
    public static final int MESSAGE_WHAT_QUERYSTATUS = 2;

    private CabinetMidByZS mCabinetMidByZS;

    public CabinetCtrlByZS(){
        mCabinetMidByZS=new CabinetMidByZS();
    }

    public static CabinetCtrlByZS getInstance() {
        if (mCabinetCtrlByZS == null) {
            synchronized (CabinetCtrlByZS.class) {
                if (mCabinetCtrlByZS == null) {
                    mCabinetCtrlByZS = new CabinetCtrlByZS();
                }
            }
        }
        return mCabinetCtrlByZS;

    }

    public void setConfig(String strPort, int nBaudrate){
        this.strPort=strPort;
        this.nBaudrate=nBaudrate;
    }

    public void unLock(int plate,int num) {

        int rc = mCabinetMidByZS.connect(strPort, nBaudrate);

        if(rc!=CabinetMidByZS.RC_SUCCESS) {
            sendMessageByUnLock(3, "连接设备失败[" + rc + "]", null);
            return;
        }

        ResultBean<HashMap<Integer, ZSCabBoxBean>> rc_unlock=mCabinetMidByZS.unLock(plate,num);

        if(rc_unlock.getResult()==1){
            UnLockResult data=new UnLockResult();
            data.setCabBoxs(rc_unlock.getData());
            sendMessageByUnLock(2, rc_unlock.getMessage(), data);
        }
        else if(rc_unlock.getResult()==2) {
            sendMessageByUnLock(3, rc_unlock.getMessage(), null);
        }
    }

    public void queryStatus(int plate) {


    }

    public void setHandler(Handler handler) {
        this.mHandler = handler;
    }

    private void sendMessageByUnLock(int status, String message, UnLockResult result){
        if (mHandler != null) {
            Message m = new Message();
            m.what = MESSAGE_WHAT_UNLOCK;
            Bundle data = new Bundle();
            data.putInt("status", status);
            data.putString("message", message);
            data.putSerializable("result", result);
            m.setData(data);
            m.setData(data);
            mHandler.sendMessage(m);
        }
    }

    private void sendMessageByQueryStatus(int what){
        if (mHandler != null) {
            Message m = new Message();
            m.what = MESSAGE_WHAT_QUERYSTATUS;
            Bundle data = new Bundle();
            m.setData(data);
            mHandler.sendMessage(m);
        }
    }

    public  class UnLockResult implements Serializable {

       private HashMap<Integer, ZSCabBoxBean> cabBoxs;

        public HashMap<Integer, ZSCabBoxBean> getCabBoxs() {
            return cabBoxs;
        }

        public void setCabBoxs(HashMap<Integer, ZSCabBoxBean> cabBoxs) {
            this.cabBoxs = cabBoxs;
        }
    }
}
