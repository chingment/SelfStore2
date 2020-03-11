package com.uplink.selfstore.deviceCtrl;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import java.io.Serializable;

public class CabinetCtrlByZS {
    private static final String TAG = "CabinetCtrlByZS";
    private static CabinetCtrlByZS mCabinetCtrlByZS= null;

    private String strPort="ttyS1";
    private int nBaudrate=115200;
    private Handler mHandler = null;
    private static final int MESSAGE_WHAT_UNLOCK = 1;
    private static final int MESSAGE_WHAT_QUERYSTATUS = 2;

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

        if(rc!=CabinetMidByZS.RC_SUCCESS){
            sendMessageByUnLock(3,"连接设备失败["+rc+"]",null);
            return;
        }

        mCabinetMidByZS.unLock(plate,num);
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

    }
}
