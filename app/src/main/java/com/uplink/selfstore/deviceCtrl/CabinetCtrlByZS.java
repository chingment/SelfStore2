package com.uplink.selfstore.deviceCtrl;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.uplink.selfstore.model.ZSCabBoxBean;
import com.uplink.selfstore.utils.CommonUtil;
import com.uplink.selfstore.utils.serialport.ChangeToolUtils;

import java.io.Serializable;
import java.util.HashMap;

public class CabinetCtrlByZS {
    private static final String TAG = "CabinetCtrlByZS";
    private static CabinetCtrlByZS mCabinetCtrlByZS= null;

    private static String ComId="ttyS1";
    private int nBaudrate=115200;
    private Handler mHandler = null;
    public static final int MESSAGE_WHAT_ONEUNLOCK = 1;
    public static final int MESSAGE_WHAT_ALLUNLOCK = 2;
    public static final int MESSAGE_WHAT_QUERYLOCKSTATUS = 3;
    public static final int MESSAGE_WHAT_DOORCONTROL = 4;
    private static int curMessageWhat=0;
    private CabinetMidByZS mCabinetMidByZS;

    private CabinetCtrlByZS() {
        mCabinetMidByZS = new CabinetMidByZS();
        //串口数据监听事件
        mCabinetMidByZS.setOnDataReceiveListener(new CabinetMidByZS.OnDataReceiveListener() {
            @Override
            public void onDataReceive(byte[] buffer) {
                if (buffer.length == 11) {
                    if (buffer[0] == 0x06) {

                        HashMap<Integer, ZSCabBoxBean> cabBoxs = new HashMap<Integer, ZSCabBoxBean>();

                        String b1 = ChangeToolUtils.hexbyte2binaryString(buffer[6]);
                        String b2 = ChangeToolUtils.hexbyte2binaryString(buffer[7]);
                        String b3 = ChangeToolUtils.hexbyte2binaryString(buffer[8]);

                        String b = b3 + b2 + b1;

                        char[] c = b.toCharArray();

                        for (int i = 0; i < c.length; i = i + 2) {

                            ZSCabBoxBean cabBox = new ZSCabBoxBean();
                            int id = (c.length - i) / 2;
                            cabBox.setId(id);
                            cabBox.setOpen(!CommonUtil.Char2Bool(c[i + 1]));
                            cabBox.setNonGoods(!CommonUtil.Char2Bool(c[i]));
                            cabBoxs.put(id, cabBox);

                        }

                        ZSCabBoxStatusResult result = new ZSCabBoxStatusResult();
                        result.setCabBoxs(cabBoxs);
                        sendMessage(curMessageWhat, 4, "反馈成功", result);
                    }
                }
            }

            @Override
            public void onSendLog(String message) {
                sendMessage(curMessageWhat, 1, message);
            }
        });
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

    public void setComId(String comId) {
        CabinetCtrlByZS.ComId = comId;
    }

    public String getComId() {
       return CabinetCtrlByZS.ComId;
    }

    public void unLock(int plate,int num) {

        curMessageWhat=MESSAGE_WHAT_ONEUNLOCK;

        int rc_connect = mCabinetMidByZS.connect(CabinetCtrlByZS.ComId, nBaudrate);

        if(rc_connect!=CabinetMidByZS.RC_SUCCESS) {
            sendMessage(curMessageWhat,6, "连接设备失败[" + rc_connect + "]");
            return;
        }

        sendMessage(curMessageWhat,2, "准备就绪");

        int rc_unLock=mCabinetMidByZS.unLock(plate,num);

        if(rc_unLock!=CabinetMidByZS.RC_SUCCESS) {
            sendMessage(curMessageWhat,6, "发送命令失败[" + rc_unLock + "]");
        }

        sendMessage(curMessageWhat,3, "开锁命令发送成功");

    }

    public void queryLockStatus(int plate,int num) {

        curMessageWhat=MESSAGE_WHAT_QUERYLOCKSTATUS;

        int rc_connect = mCabinetMidByZS.connect(CabinetCtrlByZS.ComId, nBaudrate);

        if(rc_connect!=CabinetMidByZS.RC_SUCCESS) {
            sendMessage(curMessageWhat,6, "连接设备失败[" + rc_connect + "]");
            return;
        }

        sendMessage(curMessageWhat,2, "准备就绪");

        int rc_unLock=mCabinetMidByZS.queryLockStatus(plate,num);

        if(rc_unLock!=CabinetMidByZS.RC_SUCCESS) {
            sendMessage(curMessageWhat,6, "发送命令失败[" + rc_unLock + "]");
        }

        sendMessage(curMessageWhat,3, "查询锁状态命令发送成功");


    }

    public void doorControl() {


        curMessageWhat=MESSAGE_WHAT_DOORCONTROL;

        int rc_connect = mCabinetMidByZS.connect(CabinetCtrlByZS.ComId, nBaudrate);

        if(rc_connect!=CabinetMidByZS.RC_SUCCESS) {
            sendMessage(curMessageWhat,6, "连接设备失败[" + rc_connect + "]");
            return;
        }

        sendMessage(curMessageWhat,2, "准备就绪");

        int rc_unLock=mCabinetMidByZS.queryLockStatus(1,12);

        if(rc_unLock!=CabinetMidByZS.RC_SUCCESS) {
            sendMessage(curMessageWhat,6, "发送命令失败[" + rc_unLock + "]");
        }

        sendMessage(curMessageWhat,3, "命令发送成功");

        //doorThread = new DoorThread();
        //doorThread.start();
    }

    public void disConnect() {
        if (mCabinetMidByZS != null) {
            mCabinetMidByZS.disconnect();
        }
    }

    public void setHandler(Handler handler) {
        this.mHandler = handler;
    }

    private void sendMessage(int what, int status, String message,ZSCabBoxStatusResult result){
        if (mHandler != null) {
            Message m = new Message();
            m.what = what;
            Bundle data = new Bundle();
            data.putInt("status", status);
            data.putString("message", message);
            data.putSerializable("result", result);
            m.setData(data);
            mHandler.sendMessage(m);
        }
    }

    private void sendMessage(int what, int status, String message){
        sendMessage(what,status,message,null);
    }

    public  class ZSCabBoxStatusResult implements Serializable {

        private HashMap<Integer, ZSCabBoxBean> cabBoxs;

        public HashMap<Integer, ZSCabBoxBean> getCabBoxs() {
            return cabBoxs;
        }

        public void setCabBoxs(HashMap<Integer, ZSCabBoxBean> cabBoxs) {
            this.cabBoxs = cabBoxs;
        }
    }
}
