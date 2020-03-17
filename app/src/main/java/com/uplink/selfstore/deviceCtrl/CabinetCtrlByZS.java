package com.uplink.selfstore.deviceCtrl;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.tamic.statinterface.stats.core.TcStatInterface;
import com.uplink.selfstore.model.ResultBean;
import com.uplink.selfstore.model.ZSCabBoxBean;
import com.uplink.selfstore.utils.CommonUtil;
import com.uplink.selfstore.utils.LogUtil;
import com.uplink.selfstore.utils.serialport.ChangeToolUtils;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

public class CabinetCtrlByZS {
    private static final String TAG = "CabinetCtrlByZS";
    private static CabinetCtrlByZS mCabinetCtrlByZS= null;

    private String strPort="ttyS1";
    private int nBaudrate=115200;
    private Handler mHandler = null;
    public static final int MESSAGE_WHAT_ONEUNLOCK = 1;
    public static final int MESSAGE_WHAT_ALLUNLOCK = 2;
    public static final int MESSAGE_WHAT_QUERYLOCKSTATUS = 3;
    private static int curMessageWhat=0;
    private CabinetMidByZS mCabinetMidByZS;

    private CabinetCtrlByZS(){
        mCabinetMidByZS=new CabinetMidByZS();
        //串口数据监听事件
        mCabinetMidByZS.setOnDataReceiveListener(new CabinetMidByZS.OnDataReceiveListener() {
            @Override
            public void onDataReceive(byte[] buffer) {
                String log = "数据长度:" + buffer.length;
                sendMessage(curMessageWhat, 3, log, null);
            }

            @Override
            public void onSendMessage(String message){
                sendMessage(curMessageWhat, 3, message, null);
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

    public void setConfig(String strPort, int nBaudrate){
        this.strPort=strPort;
        this.nBaudrate=nBaudrate;
    }

    public void unLock(int plate,int num) {

        curMessageWhat=MESSAGE_WHAT_ONEUNLOCK;

        int rc_connect = mCabinetMidByZS.connect(strPort, nBaudrate);

        if(rc_connect!=CabinetMidByZS.RC_SUCCESS) {
            sendMessage(curMessageWhat,4, "连接设备失败[" + rc_connect + "]");
            return;
        }

        int rc_unLock=mCabinetMidByZS.unLock(plate,num);

        if(rc_unLock!=CabinetMidByZS.RC_SUCCESS) {
            sendMessage(curMessageWhat,4, "发送命令失败[" + rc_unLock + "]");
        }

        sendMessage(curMessageWhat,2, "开锁命令发送成功");

//        sz = new byte[11];
//        int len=this.read(sz, this.nTimeout);
//        if(len!= 11) {
//            return new ResultBean<>(2,2,"读取数据失败s:"+len);
//        }
//
//        HashMap<Integer, ZSCabBoxBean> data=new HashMap<Integer, ZSCabBoxBean>();
//
//        String b1= ChangeToolUtils.hexbyte2binaryString(sz[6]);
//        String b2= ChangeToolUtils.hexbyte2binaryString(sz[7]);
//        String b3= ChangeToolUtils.hexbyte2binaryString(sz[8]);
//
//        String b=b3+b2+b1;
//
//        char[] c =b.toCharArray();
//
//        for (int i=0;i<c.length;i=i+2) {
//
//            ZSCabBoxBean box1 = new ZSCabBoxBean();
//            int id=(c.length-i)/2;
//            box1.setId(id);
//            box1.setOpen(CommonUtil.Char2Bool(c[i]));
//            box1.setNonGoods(CommonUtil.Char2Bool(c[i + 1]));
//            data.put(id, box1);
//
//            LogUtil.e(id+":"+c[i]+":"+c[i + 1]);
//        }
//        ResultBean<HashMap<Integer, ZSCabBoxBean>> rc_unlock=mCabinetMidByZS.unLock(plate,num);
//
//        if(rc_unlock.getResult()==1){
//            UnLockResult data=new UnLockResult();
//            data.setCabBoxs(rc_unlock.getData());
//            sendMessageByUnLock(2, rc_unlock.getMessage(), data);
//        }
//        else if(rc_unlock.getResult()==2) {
//            sendMessageByUnLock(3, rc_unlock.getMessage(), null);
//        }
    }

    public void queryLockStatus(int plate,int num) {

        curMessageWhat=MESSAGE_WHAT_QUERYLOCKSTATUS;

        int rc_connect = mCabinetMidByZS.connect(strPort, nBaudrate);

        if(rc_connect!=CabinetMidByZS.RC_SUCCESS) {
            sendMessage(curMessageWhat,4, "连接设备失败[" + rc_connect + "]");
            return;
        }

        int rc_unLock=mCabinetMidByZS.queryLockStatus(plate,num);

        if(rc_unLock!=CabinetMidByZS.RC_SUCCESS) {
            sendMessage(curMessageWhat,4, "发送命令失败[" + rc_unLock + "]");
        }

        sendMessage(curMessageWhat,2, "查询锁状态命令发送成功");


    }

    public void disConnect() {
        if (mCabinetMidByZS != null) {
            mCabinetMidByZS.disconnect();
        }
    }

    public void setHandler(Handler handler) {
        this.mHandler = handler;
    }

    private void sendMessage(int what, int status, String message,UnLockResult result){
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
