package com.uplink.selfstore.deviceCtrl;

import android.VendingMachine.symvdio;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.uplink.selfstore.model.PickupActionResult;
import com.uplink.selfstore.model.ScanSlotResult;
import com.uplink.selfstore.app.AppLogcatManager;
import com.uplink.selfstore.utils.LogUtil;

import java.io.File;
import java.util.HashMap;

public class CabinetCtrlByDS {

    private static final String TAG = "CabinetCtrlByDS";
    private static CabinetCtrlByDS mCabinetCtrlByDS= null;
    public static final int S_Motor_Idle = 0;
    public static final int S_Motor_Busy = 1;
    public static final int S_Motor_Done = 2;
    public static final int S_Motor_Timeout = 3;
    public static final int S_RC_SUCCESS = 0;
    public static final int S_RC_INVALID_PARAM = 1;
    public static final int S_RC_ERROR = 2;
    public static final int S_ACTION_GOZERO = 1;
    public static final int S_ACTION_DOWN_Y= 6;

    private boolean cmd_ScanSlotIsStopListener = true;
    private boolean cmd_PickupIsStopListener = true;
    private PickupThread pickupThread;
    private ScanSlotThread scanSlotThread;
    private DoorThread doorThread;
    private Handler scanSlotHandler = null;
    private Handler pickupHandler = null;
    private Handler doorHandler = null;
    private Handler goZeroHandler = null;
    private symvdio sym = null;
    private boolean mIsConnect = false;
    private String mComId="ttyS0";
    private int mComBaud=9600;

    private static final int MESSAGE_WHAT_SCANSLOTS = 1;
    private static final int MESSAGE_WHAT_PICKUP = 2;
    private static final int MESSAGE_WHAT_DOOR = 3;
    private static final int MESSAGE_WHAT_GOZERO = 4;


    private  CabinetCtrlByDS() {
        try {
            sym = new symvdio();
        } catch (Exception ex) {
            sym = null;
        }
    }

    public static CabinetCtrlByDS getInstance() {
        if (mCabinetCtrlByDS == null) {
            synchronized (CabinetCtrlByDS.class) {
                if (mCabinetCtrlByDS == null) {
                    mCabinetCtrlByDS = new CabinetCtrlByDS();
                }
            }
        }
        return mCabinetCtrlByDS;
    }

    public String vesion() {
        String version = "unKnow";
        if (sym == null)
            return version;

        version = sym.SDK_Version();

        return version;
    }

    public void setComId(String comId) {
        mComId = comId;
    }

    public String getComId() {
        return mComId;
    }

    public void setComBaud(int comBaud) {
        mComBaud = comBaud;
    }


    public int getComBaud() {
        return mComBaud;
    }

    public boolean connect() {

//        if (mIsConnect)
//            return true;

        String comId = mComId;
        int comBaud = mComBaud;
        LogUtil.i(TAG, "打开串口：" + comId + "，波特：" + comBaud);
        if (sym == null) {
            LogUtil.d(TAG, "打开串口：" + comId + "，波特：" + comBaud + "，失败，sym为 NULL");
            mIsConnect = false;
        } else {
            File file = new File("/dev/" + comId);
            if (file.exists()) {
                int rc_status = sym.Connect(comId, comBaud);
                LogUtil.d(TAG, "打开串口：" + comId + "，波特：" + comBaud + "，状态为：" + rc_status);
                if (rc_status == 0) {
                    mIsConnect = true;
                }
            } else {
                LogUtil.d(TAG, "打开串口：" + comId + "，波特：" + comBaud + "，失败，串口ID不存在");
            }
        }

        if (!mIsConnect) {
            AppLogcatManager.saveLogcat2Server("logcat -d -s symvdio CabinetCtrlByDS ", "connect");
        }

        return mIsConnect;

    }

    public void disConnect() {
        if (sym != null) {
            sym.disconnect();
        }
        mIsConnect = false;
        cmd_ScanSlotIsStopListener = true;
        cmd_PickupIsStopListener = true;
    }

    public void stopPickup() {
        cmd_PickupIsStopListener = true;
        emgStop();
    }

    public boolean isNormarl() {

        if(!mIsConnect){
            LogUtil.d(TAG, "isNormarl：尝试再连接多一次");
            connect();
        }

        if (!mIsConnect) {
            return false;
        }

        if(sym==null)
            return false;

        boolean isflag=false;
        long nStart = System.currentTimeMillis();
        long nEnd = System.currentTimeMillis();

        for (; (nEnd - nStart <= (long) 2 * 1000); nEnd = System.currentTimeMillis()) {

            boolean isConnectSts = sym.SY_MV_DIO_Slave_ConnectSts();
            if(isConnectSts){
                isflag=true;
                break;
            }
//            else{
//                connect();
//            }

            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }


        LogUtil.i(TAG, "isNormarl:" + isflag);
        return isflag;
    }

    public boolean isConnect() {
        return mIsConnect;
    }

    public int[] getScanSlotResult() {
        if(sym==null)
            return null;

        return sym.SN_MV_Get_ScanData();
    }

    public void scanSlot() {
        scanSlotThread = new ScanSlotThread();
        scanSlotThread.start();
    }

    public void goZero() {

        if(!mIsConnect){
            LogUtil.d(TAG, "goZero：尝试再连接多一次");
            connect();
        }

        if (!mIsConnect)
            return;

        if(sym==null)
            return;

        sym.SN_MV_EmgStop();
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        sym.SN_MV_MotorAction(1, 0, 0);

    }

    public void firstSet(){
        new Thread(new Runnable() {
            public void run() {
                LogUtil.d(TAG,"初始设备设置："+mIsConnect);

                if(!mIsConnect){
                    LogUtil.d(TAG, "firstSet：尝试再连接多一次");
                    connect();
                }

                if(mIsConnect) {

                    sym.SN_MV_EmgStop();//急停

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    sym.SN_MV_MotorAction(1, 0, 0);//回原点

                    try {
                        Thread.sleep(4000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    sym.SN_MV_ManuProc(1, 0, 0);//打开取货们

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    sym.SN_MV_ManuProc(2, 0, 0);//关闭取货们
                }

               //AppLogcatManager.saveLogcat2Server("logcat -d -s symvdio CabinetCtrlByDS ", "firstset");
            }

        }).start();

    }

    public void openPickupDoor() {


        new Thread(new Runnable() {
            public void run() {

                if(mIsConnect) {

                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    sym.SN_MV_MotorAction(1, 0, 0);//回原点

                    try {
                        Thread.sleep(4000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    sym.SN_MV_ManuProc(1, 0, 0);//打开取货们

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    sym.SN_MV_ManuProc(2, 0, 0);//关闭取货们
                }
            }

        }).start();


    }

    public void setCbLight(int value){

        new Thread(new Runnable() {
            public void run() {
                if(mIsConnect) {
                    sym.SN_MV_OutputCheck(value);
                }
            }
        }).start();

    }

    public void doorControl() {
        doorThread = new DoorThread();
        doorThread.start();
    }

    public void emgStop() {
        if (!mIsConnect)
            return;

        if (sym == null)
            return;

        sym.SN_MV_EmgStop();
    }

    public void startPickUp(boolean isGoZero, int row,int col,int[] pendantRows) {

        int mode = 0;
        if(pendantRows!=null) {
            for (int z = 0; z < pendantRows.length; z++) {
                if (pendantRows[z] == row) {
                    mode = 1;
                    break;
                }
            }
        }

        pickupThread = new PickupThread(isGoZero,mode, row, col);
        pickupThread.start();
    }

    public boolean isIdle() {

        if(!mIsConnect){
            LogUtil.d(TAG, "判断空闲状态：尝试再连接多一次");
            connect();
        }

        if (!mIsConnect)
            return false;

        if (sym == null)
            return false;

        boolean flag = false;

        long nStart = System.currentTimeMillis();
        long nEnd = System.currentTimeMillis();

        for (; (nEnd - nStart <= (long) 3 * 1000); nEnd = System.currentTimeMillis()) {

            boolean flag1 = true;

//            int[] rc_status1 = sym.SN_MV_Get_ManuProcStatus();
//            if (rc_status1[0] == S_RC_SUCCESS) {
//                if (rc_status1[2] == S_Motor_Idle || rc_status1[2] == S_Motor_Done) {
//                    flag1 = true;
//                }
//            }

            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            boolean flag2 = false;
            int[] rc_status2 = sym.SN_MV_Get_FlowStatus();
            if (rc_status2[0] == S_RC_SUCCESS) {
                if (rc_status2[3] !=1) {
                    flag2 = true;
                }
            }

            flag = flag1 && flag2;

            if (flag) {
                break;
            }

            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }

        return flag;

    }

    public void setScanSlotHandler(Handler scanSlotHandler) {
        this.scanSlotHandler = scanSlotHandler;
    }

    public void setPickupHandler(Handler pickupHandler) {
        this.pickupHandler = pickupHandler;
    }

    public void setDoorHandler(Handler doorHandler) {
        this.doorHandler = doorHandler;
    }

    public void setGoZeroHandler(Handler goZeroHandler) {
        this.goZeroHandler = goZeroHandler;
    }

    private void sendScanSlotHandlerMessage(int status, String message, ScanSlotResult result) {
        if (scanSlotHandler != null) {
            Message m = new Message();
            m.what = MESSAGE_WHAT_SCANSLOTS;
            Bundle data = new Bundle();
            data.putInt("status", status);
            data.putString("message", message);
            data.putSerializable("result", result);
            m.setData(data);
            scanSlotHandler.sendMessage(m);
        }
    }

    private void sendPickupHandlerMessage(int status, String message, PickupActionResult result) {
        if (pickupHandler != null) {
            Message m = new Message();
            m.what = MESSAGE_WHAT_PICKUP;
            Bundle data = new Bundle();
            data.putInt("status", status);
            data.putString("message", message);
            data.putSerializable("result", result);
            m.setData(data);
            pickupHandler.sendMessage(m);
        }
    }

    private void sendDoorHandlerMessage(int status, String message) {
        if (doorHandler != null) {
            Message m = new Message();
            m.what = MESSAGE_WHAT_DOOR;
            Bundle data = new Bundle();
            data.putInt("status", status);
            data.putString("message", message);
            m.setData(data);
            doorHandler.sendMessage(m);
        }
    }

    private void sendGoZeroHandlerMessage(int status, String message) {
        if (goZeroHandler != null) {
            Message m = new Message();
            m.what = MESSAGE_WHAT_GOZERO;
            Bundle data = new Bundle();
            data.putInt("status", status);
            data.putString("message", message);
            m.setData(data);
            goZeroHandler.sendMessage(m);
        }
    }

    public class ScanSlotThread extends Thread {

        @Override
        public void run() {
            super.run();

            if(!mIsConnect){
                LogUtil.d(TAG, "扫描流程监听：尝试再连接多一次");
                connect();
            }

            if (!mIsConnect) {
                LogUtil.i(TAG, "扫描流程监听：启动前，检查设备连接失败");
                sendScanSlotHandlerMessage(6, "启动前，检查设备连接失败", null);
                return;
            }

            if (!isNormarl()) {
                LogUtil.i(TAG, "扫描流程监听：启动前，检查设备不在线");
                sendScanSlotHandlerMessage(6, "启动前，检查设备不在线", null);
                return;
            }

//            if (!isIdle()) {
//                LogUtil.i(TAG, "扫描流程监听：启动前，检查设备不在空闲状态");
//                sendScanSlotHandlerMessage(1, "启动前，检查设备不在空闲状态", null);
//                return;
//            }

            int rt_goZero = sym.SN_MV_MotorAction(1, 0, 0);
            if (rt_goZero != S_RC_SUCCESS) {
                LogUtil.i(TAG, "扫描流程监听：启动回原点失败");
                sendScanSlotHandlerMessage(6, "启动回原点失败", null);
                return;
            }

            sendScanSlotHandlerMessage(2, "扫描货道就绪成功", null);

            boolean bCanSelfAutoScan = false;
            for (int i=0; i<60; i++) {
                int[] result = sym.SN_MV_Get_MotionStatus();
                boolean isInZero = false;
                if (result[0] == S_RC_SUCCESS) {
                    if (result[2] == S_Motor_Done || result[2] == S_Motor_Idle) {
                        isInZero = true;
                    }
                }
                if (isInZero) {
                    bCanSelfAutoScan = true;
                    break;
                }

                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }


            if (!bCanSelfAutoScan) {
                LogUtil.i(TAG, "扫描流程监听：回原点失败");
                sendScanSlotHandlerMessage(6, "回原点失败", null);
                return;
            }

            int rc_selfAutoScan = sym.SN_MV_SelfAutoScan(0);

            if (rc_selfAutoScan != S_RC_SUCCESS) {
                LogUtil.i(TAG, "扫描流程监听：扫描货道启动失败");
                sendScanSlotHandlerMessage(6, "扫描货道启动失败", null);
                return;
            }
            else
            {
                sendScanSlotHandlerMessage(2, "扫描货道启动成功", null);
            }

            cmd_ScanSlotIsStopListener = false;
            long nScanSlotStartTime = System.currentTimeMillis();
            while (!cmd_ScanSlotIsStopListener) {

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                try {
                    long maxPickTime = System.currentTimeMillis() - nScanSlotStartTime;
                    if (maxPickTime < 10 * 60 * 1000) {
                        int[] rc_scanStatus = sym.SN_MV_Get_ScanStatus();
                        if (rc_scanStatus[0] == S_RC_SUCCESS) {
                            //LogUtil.i(TAG, "扫描流程监听：扫描状态" + rc_scanStatus[0]);
                            int isflag = rc_scanStatus[1];//表示扫描是否结束

                            //LogUtil.i(TAG, "扫描流程监听：扫描流程是否结束，" + isflag);
                            if (isflag == 0) {
                                int[] rc_scanData = sym.SN_MV_Get_ScanData();

                                LogUtil.i(TAG, "扫描流程监听：结果长度" + rc_scanData.length);
                                for (int i = 0; i < rc_scanData.length; i++) {
                                    LogUtil.d("扫描流程监听：结果【" + i + "】：" + rc_scanData[i]);
                                }

                                if (rc_scanData[0] == S_RC_SUCCESS) {


                                    ScanSlotResult scanSlotResult = new ScanSlotResult();

                                    long nScanSlotEndTime = System.currentTimeMillis();
                                    long sTime = nScanSlotEndTime - nScanSlotStartTime;
                                    scanSlotResult.setUseTime(sTime);//设置取货消耗时间


                                    LogUtil.i(TAG, "扫描流程监听：扫描成功，使用时长：" + sTime);

                                    int rows = rc_scanData[1];
                                    scanSlotResult.setRows(rows);

                                    if (rows > 0) {

                                        int[] rowColLayout = new int[rows];

                                        for (int i = 0; i < rows; i++) {
                                            rowColLayout[i] = rc_scanData[2 + i];
                                        }
                                        scanSlotResult.setRowColLayout(rowColLayout);
                                    }
                                    cmd_ScanSlotIsStopListener = true;
                                    sendScanSlotHandlerMessage(4, "扫描结束", scanSlotResult);
                                }
                            } else {
                                sendScanSlotHandlerMessage(3, "正在扫描", null);
                            }
                        }

                        try {
                            Thread.sleep(300);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                    } else {
                        LogUtil.d(TAG, "扫描流程监听：扫描超时");
                        goZero();
                        cmd_ScanSlotIsStopListener = true;
                        sendScanSlotHandlerMessage(5, "扫描超时", null);
                    }
                } catch (Exception ex) {
                    //todo 处理异常操作
                    ex.printStackTrace();
                    LogUtil.e(TAG, "扫描流程监听：扫描处理失败");
                    LogUtil.e(TAG, ex);
                    goZero();
                    cmd_ScanSlotIsStopListener = true;
                    sendScanSlotHandlerMessage(6, "扫描失败", null);
                }
            }
        }
    }

    public class PickupThread extends Thread {

        private int mode = -1;
        private int row = -1;
        private int col = -1;
        private boolean isGoZero=false;
        private PickupThread(Boolean isGoZero, int mode, int row, int col) {
            this.mode = mode;
            this.row = row;
            this.col = col;
            this.isGoZero=isGoZero;
        }

        @Override
        public void run() {
            super.run();

            sendPickupHandlerMessage(2, "检查设备连接状态中", null);

            if(!mIsConnect){
                LogUtil.d(TAG, "取货流程监听：设备未连接，尝试再连接多一次");
                connect();
            }

            if (!mIsConnect) {
                sendPickupHandlerMessage(5, "设备连接失败", null);
                interrupt();
                return;
            }

            sendPickupHandlerMessage(2, "设备已连接,检查在线状态中", null);

            if (!isNormarl()) {
                LogUtil.i(TAG, "取货流程监听：设备不在线");
                sendPickupHandlerMessage(5, "设备不在线", null);
                interrupt();
                return;
            }

            sendPickupHandlerMessage(2, "设备已在线，检查运行状态中", null);

            boolean isIdle=false;

            for (int i = 0; i < 60; i++) {

                int[] rc_status = sym.SN_MV_Get_FlowStatus();
                if (rc_status[0] == S_RC_SUCCESS) {
                    if (rc_status[3] != 1) { //直接判断不等于1 为非空闲状态
                        isIdle = true;
                        break;
                    }
                }

                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }

            if(!isIdle) {
                LogUtil.i(TAG, "取货流程监听：设备不在空闲状态");
                sendPickupHandlerMessage(5, "设备不在空闲状态", null);
                interrupt();
                return;
            }

            sendPickupHandlerMessage(2, "取货准备就绪", null);

            if(isGoZero) {
                //尝试3次发送回原点
                boolean isGoZero_Cmd = false;
                for (int i = 0; i < 3; i++) {
                    int rt_goZero = sym.SN_MV_MotorAction(1, 0, 0);
                    if (rt_goZero == S_RC_SUCCESS) {
                        isGoZero_Cmd = true;
                        break;
                    }
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                if (!isGoZero_Cmd) {
                    LogUtil.i(TAG, "取货流程监听：启动回原点命令失败");
                    sendPickupHandlerMessage(5, "启动回原点命令失败", null);
                    interrupt();
                    return;
                }

                sendPickupHandlerMessage(2, "启动回原点命令成功", null);
            }

            //尝试查询回原点状态
            boolean bCanAutoStart = false;

            for (int i = 0; i < 60; i++) {

                int[] rc_status1 = sym.SN_MV_Get_MotionStatus();
                if (rc_status1[0] == S_RC_SUCCESS) {
                    if (rc_status1[2] == S_Motor_Idle || rc_status1[2] == S_Motor_Done) {
                        bCanAutoStart = true;
                        break;
                    }
                }

                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                sendPickupHandlerMessage(2, "正在检查原点状态", null);

            }

            if (!bCanAutoStart) {
                LogUtil.i(TAG, "取货流程监听：取货回原点失败");
                sendPickupHandlerMessage(5, "取货就绪失败", null);
                interrupt();
            }
            else {
                LogUtil.i(TAG, "取货流程监听：mode:" + mode + ",row:" + row + ",col:" + col);

                sendPickupHandlerMessage(2, "取货就绪成功..请稍等", null);


                boolean isAutoStart = false;
                boolean isCanSendCmdByAutoStart=true;
                for (int i = 0; i < 10; i++) {
                    if (isCanSendCmdByAutoStart) {
                        isCanSendCmdByAutoStart = false;
                        int rc_autoStart = sym.SN_MV_AutoStart(mode, row, col);
                        if (rc_autoStart == S_RC_SUCCESS) {
                            sendPickupHandlerMessage(2, "正在出货中", null);
                            isAutoStart = true;
                            break;
                        } else {
                            LogUtil.i(TAG, "取货流程监听：取货启动失败,状态" + rc_autoStart + ",正在尝试" + i + "次,休息1秒");

                            //先急停
                            sym.SN_MV_EmgStop();

                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                            //尝试3次发送回原点
                            boolean isGoZero_Cmd = false;
                            for (int j = 0; j < 3; j++) {
                                int rt_goZero = sym.SN_MV_MotorAction(1, 0, 0);
                                if (rt_goZero == S_RC_SUCCESS) {
                                    isGoZero_Cmd = true;
                                    break;
                                }
                                try {
                                    Thread.sleep(1000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }

                            if (!isGoZero_Cmd) {
                                LogUtil.i(TAG, "取货流程监听：启动回原点命令失败");
                                sendPickupHandlerMessage(5, "启动回原点命令失败", null);
                                interrupt();
                                return;
                            }


                            for (int j = 0; j < 60; j++) {
                                int[] rc_status1 = sym.SN_MV_Get_MotionStatus();
                                if (rc_status1[0] == S_RC_SUCCESS) {
                                    if (rc_status1[2] == S_Motor_Idle || rc_status1[2] == S_Motor_Done) {
                                        isCanSendCmdByAutoStart = true;
                                        break;
                                    }
                                }
                                try {
                                    Thread.sleep(2000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }


//                //尝试3次发送取货命令
//                boolean isAutoStart = false;
//                boolean isCanSendCmdByAutoStart=true;
//                for (int i = 0; i < 10; i++) {
//                    if(isCanSendCmdByAutoStart) {
//                        isCanSendCmdByAutoStart = false;
//                        int rc_autoStart = sym.SN_MV_AutoStart(mode, row, col);
//                        if (rc_autoStart == S_RC_SUCCESS) {
//                            sendPickupHandlerMessage(2, "正在出货中", null);
//                            isAutoStart = true;
//                            break;
//                        } else {
//                            LogUtil.i(TAG, "取货流程监听：取货启动失败,状态" + rc_autoStart + ",正在尝试" + i + "次,休息1秒");
//                        }
//                    }
//                    else {
//                        LogUtil.i(TAG, "取货流程监听：取货启动失败,正在尝试" + i + "次,休息1秒,不能再发送取后命令");
//                    }
//
//                    try {
//                        Thread.sleep(1000);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//
//                    int[] rc_flowStatus = sym.SN_MV_Get_FlowStatus();
//                    if (rc_flowStatus[0] == S_RC_SUCCESS) {
//                        LogUtil.i(TAG, "取货流程监听：取货启动失败,正在尝试" + i + "次,休息1秒,当前状态"+rc_flowStatus[3]);
//                        if (rc_flowStatus[3] != 1) {
//                            isCanSendCmdByAutoStart = true;
//                        }
//                    }
//                }

                if (!isAutoStart) {
                    LogUtil.i(TAG, "取货流程监听：取货命令启动失败");
                    sendPickupHandlerMessage(5, "取货启动失败", null);
                    interrupt();
                } else {
                    cmd_PickupIsStopListener = false;

                    HashMap<String, String> nPickupActionMap = new HashMap<>();

                    long nPickupStartTime = System.currentTimeMillis();

                    while (!cmd_PickupIsStopListener) {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        try {
                            long maxPickTime = System.currentTimeMillis() - nPickupStartTime;
                            if (maxPickTime < 2 * 60 * 1000) {

                                int[] rc_flowStatus = sym.SN_MV_Get_FlowStatus();

                                PickupActionResult result = new PickupActionResult();
                                result.setActionCount(rc_flowStatus[1]);//动作总数
                                result.setActionId(rc_flowStatus[2]);//当前动作号
                                result.setActionStatusCode(rc_flowStatus[3]);//当前动作状态

                                boolean isPickupComplete = false;
                                if (rc_flowStatus[0] == S_RC_SUCCESS) {
                                    if (rc_flowStatus[2] == S_ACTION_GOZERO) {
                                        if (rc_flowStatus[3] == S_Motor_Done) {
                                            isPickupComplete = true;//设置取货完成
                                            long nPickupEndTime = System.currentTimeMillis();
                                            long sTime = nPickupEndTime - nPickupStartTime;
                                            result.setPickupUseTime(sTime);//设置取货消耗时间
                                        }
                                    }

                                    if (isPickupComplete) {
                                        LogUtil.i(TAG, "取货流程监听：当前动作" + result.getActionName() + "（" + result.getActionId() + "）" + "," + result.getActionStatusName() + "（" + result.getActionStatusCode() + "）");
                                        LogUtil.i(TAG, "取货流程监听：取货完成，用时" + result.getPickupUseTime());
                                        cmd_PickupIsStopListener = true;
                                        sendPickupHandlerMessage(4, "取货成功", result);
                                    } else {
                                        String action_key = result.getActionId() + "-" + result.getActionStatusCode();
                                        String action_value = result.getActionName() + "-" + result.getActionStatusName();
                                        if (!nPickupActionMap.containsKey(action_key)) {
                                            LogUtil.i(TAG, "取货流程监听：当前动作" + result.getActionName() + "（" + result.getActionId() + "）" + "," + result.getActionStatusName() + "（" + result.getActionStatusCode() + "）");
                                            nPickupActionMap.put(action_key, action_value);
                                            if (rc_flowStatus[3] == S_Motor_Busy || rc_flowStatus[3] == S_Motor_Done) {
                                                sendPickupHandlerMessage(3, "正在取货中", result);
                                            } else if (rc_flowStatus[3] == S_Motor_Timeout) {
                                                cmd_PickupIsStopListener = true;
                                                sym.SN_MV_EmgStop();
                                                LogUtil.d(TAG, "取货流程监听：单动作运行取货超时");
                                                sendPickupHandlerMessage(5, "单动作运行取货超时", result);
                                            }
                                        }
                                    }
                                }

                                try {
                                    Thread.sleep(300);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }

                            } else {
                                cmd_PickupIsStopListener = true;
                                sym.SN_MV_EmgStop();
                                LogUtil.d(TAG, "取货流程监听：整体动作运行取货超时");
                                sendPickupHandlerMessage(5, "整体动作运行取货超时", null);
                            }
                        } catch (Exception ex) {
                            cmd_PickupIsStopListener = true;
                            sym.SN_MV_EmgStop();
                            LogUtil.e(TAG, "取货流程监听：发生异常");
                            LogUtil.e(TAG, ex);
                            sendPickupHandlerMessage(6, "取货异常:" + ex.getMessage(), null);
                        }
                    }
                }
            }
        }
    }

    private class DoorThread extends Thread {

        @Override
        public void run() {
            super.run();

            if(!mIsConnect){
                LogUtil.d(TAG, "开门：尝试再连接多一次");
                connect();
            }


            if (!mIsConnect) {
                sendDoorHandlerMessage(1, "检测设备未连接");
                return;
            }



            sym.SN_MV_SetLock(true);

            sendDoorHandlerMessage(1, "请在10秒内打开/关闭柜门");

            try {
                Thread.sleep(10 * 1000);
            } catch (Exception ex) {

            }

            sym.SN_MV_SetLock(false);

        }
    }

    private class GoZeroThread extends Thread {

        private boolean mIsCmdClosePickupDoor=false;
        public GoZeroThread(boolean isCmdClosePickupDoor){
            mIsCmdClosePickupDoor=isCmdClosePickupDoor;
        }

        @Override
        public void run() {
            super.run();

            if(!mIsConnect){
                LogUtil.d(TAG, "回原点流程监听：尝试再连接多一次");
                connect();
            }

            if (!mIsConnect) {
                LogUtil.i(TAG, "回原点流程监听：启动前，检查设备连接失败");
                sendGoZeroHandlerMessage(5, "启动前，检查设备连接失败");
                return;
            }

            if (!isNormarl()) {
                LogUtil.i(TAG, "回原点流程监听：启动前，检查设备不在线");
                sendGoZeroHandlerMessage(5, "启动前，检查设备不在线");
                return;
            }

            if (!isIdle()) {
                LogUtil.i(TAG, "回原点流程监听：启动前，检查设备不在空闲状态");
                sendGoZeroHandlerMessage(5, "启动前，检查设备不在空闲状态");
                return;
            }

            int st_emgStop=sym.SN_MV_EmgStop();
            if(st_emgStop!=0){
                LogUtil.i(TAG, "回原点流程监听：启动急停不成功");
                sendGoZeroHandlerMessage(5, "回原点流程监听：启动急停不成功，状态："+st_emgStop);
                return;
            }

            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            int rt_goZero = sym.SN_MV_MotorAction(1, 0, 0);
            if (rt_goZero != S_RC_SUCCESS) {
                LogUtil.i(TAG, "回原点流程监听：启动回原点失败");
                sendGoZeroHandlerMessage(5, "启动回原点失败,状态："+rt_goZero);
                return;
            }

            sendGoZeroHandlerMessage(2, "启动回原点进行中");

            long nStart = System.currentTimeMillis();
            long nEnd = System.currentTimeMillis();
            boolean bTryAgain = false;
            boolean bCanAutoStart = false;
            for (; (nEnd - nStart <= (long) 120 * 1000 || bTryAgain); nEnd = System.currentTimeMillis()) {
                int[] result = sym.SN_MV_Get_MotionStatus();
                boolean isInZero = false;
                if (result[0] == S_RC_SUCCESS) {
                    if (result[2] == S_Motor_Done || result[2] == S_Motor_Idle) {
                        isInZero = true;
                    }
                }

                if (isInZero) {
                    bCanAutoStart = true;
                    break;
                } else {
                    bTryAgain = true;
                }
            }

            if (!bCanAutoStart) {
                LogUtil.i(TAG, "回原点流程监听：回原点失败");
                sendGoZeroHandlerMessage(5, "回原点失败");
                return;
            }

            sendGoZeroHandlerMessage(3, "回原点成功");

            if(mIsCmdClosePickupDoor){
                sym.SN_MV_ManuProc(2, 0, 0);
            }
        }
    }

}
