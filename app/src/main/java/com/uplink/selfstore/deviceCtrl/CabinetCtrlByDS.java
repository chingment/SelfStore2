package com.uplink.selfstore.deviceCtrl;

import android.VendingMachine.symvdio;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.uplink.selfstore.model.PickupResult;
import com.uplink.selfstore.model.ScanSlotResult;
import com.uplink.selfstore.utils.LogUtil;

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
    private boolean isConnect = false;
    private boolean cmd_ScanSlotIsStopListener = true;
    private boolean cmd_PickupIsStopListener = true;
    private PickupListenerThread pickupListenerThread;
    private ScanSlotListenerThread scanListenerThread;
    private DoorThread doorThread;
    private Handler scanSlotHandler = null;
    private Handler pickupHandler = null;
    private Handler doorHandler = null;
    private Handler goZeroHandler = null;
    private symvdio sym = null;
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
            synchronized (FingerVeinCtrl.class) {
                if (mCabinetCtrlByDS == null) {
                    mCabinetCtrlByDS = new CabinetCtrlByDS();
                }
            }
        }
        return mCabinetCtrlByDS;

    }

    public String vesion() {
        String version = "";
        if (sym != null) {
            version = sym.SDK_Version();
        }
        return version;
    }

    public boolean connect() {

        if (sym == null) {
            isConnect = false;
            return isConnect;
        }

        try {
            int rc_status = sym.Connect("ttymxc1", 9600);
            if (rc_status == 0) {
                isConnect = true;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            isConnect = false;
            return isConnect;
        } finally {
            return isConnect;
        }

    }

    public void disConnect() {
        if (sym != null) {
            sym.disconnect();
        }
        isConnect = false;
        cmd_ScanSlotIsStopListener = true;
        cmd_PickupIsStopListener = true;
    }

    public boolean isNormarl() {
        if (sym == null) {
            return false;
        }


        boolean isflag=false;
        long nStart = System.currentTimeMillis();
        long nEnd = System.currentTimeMillis();

        for (; (nEnd - nStart <= (long) 2 * 1000); nEnd = System.currentTimeMillis()) {

            boolean isConnectSts = sym.SY_MV_DIO_Slave_ConnectSts();
            if(isConnectSts){
                isflag=true;
                break;
            }
            else{
                connect();
            }

            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }


        LogUtil.i(TAG, "isNormarl:" + isflag);
        return isflag;
    }

    public boolean isConnect() {
        return isConnect;
    }

    public int[] getScanSlotResult() {
        return sym.SN_MV_Get_ScanData();
    }

    public void scanSlot() {
        scanListenerThread = new ScanSlotListenerThread();
        scanListenerThread.start();
    }

    public void goZero() {
        isConnect = connect();
        if (isConnect) {
            if (sym != null) {
                sym.SN_MV_EmgStop();
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                sym.SN_MV_MotorAction(1, 0, 0);
            }
        }
    }

    public void firstSet(){
        new Thread(new Runnable() {
            public void run() {
                goZero();//回原点
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                //关闭取货口
                closePickupDoor();
            }

        }).start();

    }

    public void openPickupDoor() {
        isConnect = connect();
        if (isConnect) {
            if (sym != null) {
                sym.SN_MV_ManuProc(1, 0, 0);
            }
        }
    }

    public void closePickupDoor() {
        isConnect = connect();
        if (isConnect) {
            if (sym != null) {
                sym.SN_MV_ManuProc(2, 0, 0);
            }
        }
    }

    public void doorControl() {
        doorThread = new DoorThread();
        doorThread.start();
    }

    public void pickUp(int row,int col,int[] pendantRows) {

        int mode = 0;
        if(pendantRows!=null) {
            for (int z = 0; z < pendantRows.length; z++) {
                if (pendantRows[z] == row) {
                    mode = 1;
                    break;
                }
            }
        }

        pickupListenerThread = new PickupListenerThread(mode, row, col);
        pickupListenerThread.start();
    }

    public void dispose(){
        cmd_PickupIsStopListener=true;
    }

    public boolean isIdle() {
        boolean flag = false;

        long nStart = System.currentTimeMillis();
        long nEnd = System.currentTimeMillis();
        boolean bTryAgain = false;

        for (; (nEnd - nStart <= (long) 2 * 1000|| bTryAgain); nEnd = System.currentTimeMillis()) {
            boolean flag1 = false;
            int[] rc_status1 = sym.SN_MV_Get_ManuProcStatus();
            if (rc_status1[0] == S_RC_SUCCESS) {
                if (rc_status1[2] == S_Motor_Idle || rc_status1[2] == S_Motor_Done) {
                    flag1 = true;
                }
            }

            boolean flag2 = false;
            int[] rc_status2 = sym.SN_MV_Get_FlowStatus();
            if (rc_status2[0] == S_RC_SUCCESS) {
                if (rc_status2[3] == S_Motor_Idle || rc_status2[3] == S_Motor_Done) {
                    flag2 = true;
                }
            }

            flag = flag1 && flag2;

            if (!flag) {
                bTryAgain = true;
            } else {
                break;
            }

        }

        //  int[] rc_status3 = sym.SN_MV_Get_MotionStatus();
        //  int[] rc_status4 = sym.SN_MV_Get_ScanStatus();

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

    private void sendPickupHandlerMessage(int status, String message, PickupResult result) {
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

    private class ScanSlotListenerThread extends Thread {

        @Override
        public void run() {
            super.run();

            if (!isConnect) {
                LogUtil.i(TAG, "扫描流程监听：启动前，检查设备连接失败");
                sendScanSlotHandlerMessage(1, "启动前，检查设备连接失败", null);
                return;
            }

            if (!isNormarl()) {
                LogUtil.i(TAG, "扫描流程监听：启动前，检查设备不在线");
                sendScanSlotHandlerMessage(1, "启动前，检查设备不在线", null);
                return;
            }

            if (!isIdle()) {
                LogUtil.i(TAG, "扫描流程监听：启动前，检查设备不在空闲状态");
                sendScanSlotHandlerMessage(1, "启动前，检查设备不在空闲状态", null);
                return;
            }

            int rt_goZero = sym.SN_MV_MotorAction(1, 0, 0);
            if (rt_goZero != S_RC_SUCCESS) {
                LogUtil.i(TAG, "扫描流程监听：启动回原点失败");
                sendScanSlotHandlerMessage(1, "启动回原点失败", null);
                return;
            }

            LogUtil.i(TAG, "扫描流程监听：扫描货道启动成功");
            sendScanSlotHandlerMessage(2, "扫描货道启动成功", null);

            long nStart = System.currentTimeMillis();
            long nEnd = System.currentTimeMillis();
            boolean bTryAgain = false;
            boolean bCanSelfAutoScan = false;
            for (; (nEnd - nStart <= (long) 60 * 1000 || bTryAgain); nEnd = System.currentTimeMillis()) {
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
                } else {
                    bTryAgain = true;
                }
            }

            if (!bCanSelfAutoScan) {
                LogUtil.i(TAG, "扫描流程监听：回原点失败");
                sendScanSlotHandlerMessage(1, "回原点失败", null);
                return;
            }

            int rc_selfAutoScan = sym.SN_MV_SelfAutoScan(0);


            if (rc_selfAutoScan != S_RC_SUCCESS) {
                LogUtil.i(TAG, "扫描流程监听：扫描货道启动失败");
                sendScanSlotHandlerMessage(1, "扫描货道启动失败", null);
                return;
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
                    if (maxPickTime < 5 * 60 * 1000) {
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
                                    disConnect();
                                    cmd_ScanSlotIsStopListener = true;
                                    sendScanSlotHandlerMessage(4, "扫描结束", scanSlotResult);
                                }
                            } else {
                                sendScanSlotHandlerMessage(3, "正在扫描", null);
                            }
                        }

                    } else {
                        LogUtil.e(TAG, "扫描流程监听：扫描超时");
                        goZero();
                        disConnect();
                        cmd_ScanSlotIsStopListener = true;
                        sendScanSlotHandlerMessage(5, "扫描超时", null);
                    }
                } catch (Exception ex) {
                    //todo 处理异常操作
                    ex.printStackTrace();
                    LogUtil.e(TAG, "扫描流程监听：扫描处理失败");
                    LogUtil.e(TAG, ex);
                    goZero();
                    disConnect();
                    cmd_ScanSlotIsStopListener = true;
                    sendScanSlotHandlerMessage(6, "扫描失败", null);
                }
            }
        }
    }

    private class PickupListenerThread extends Thread {

        private int mode = -1;
        private int row = -1;
        private int col = -1;

        private PickupListenerThread(int mode, int row, int col) {
            this.mode = mode;
            this.row = row;
            this.col = col;
        }

        @Override
        public void run() {
            super.run();
            isConnect = connect();
            if (!isConnect) {
                LogUtil.i(TAG, "取货流程监听：启动前，检查设备连接失败");
                sendPickupHandlerMessage(5, "启动前，检查设备连接失败", null);
                return;
            }

            if (!isNormarl()) {
                LogUtil.i(TAG, "取货流程监听：启动前，检查设备不在线");
                sendPickupHandlerMessage(5, "启动前，检查设备不在线", null);
                return;
            }

            if (!isIdle()) {
                LogUtil.i(TAG, "取货流程监听：启动前，检查设备不在空闲状态");
                sendPickupHandlerMessage(5, "启动前，检查设备不在空闲状态", null);
                return;
            }

            //尝试3次回原点
            boolean isgoZero = false;
            for (int i = 0; i < 3; i++) {
                int rt_goZero = sym.SN_MV_MotorAction(1, 0, 0);
                if (rt_goZero == S_RC_SUCCESS) {
                    isgoZero = true;
                    break;
                }
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            if (!isgoZero) {
                LogUtil.i(TAG, "取货流程监听：启动回原点失败");
                sendPickupHandlerMessage(5, "启动回原点失败", null);
                return;
            }

            LogUtil.i(TAG, "取货流程监听：取货就绪");

            sendPickupHandlerMessage(2, "取货就绪", null);

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
                LogUtil.i(TAG, "取货流程监听：取货回原点失败");
                sendPickupHandlerMessage(5, "取货回原点失败", null);
                return;
            }

            LogUtil.i(TAG, "取货流程监听：mode:" + mode + ",row:" + row + ",col:" + col);

            //尝试3次发送取货命令
            boolean isAutoStart = false;
            for (int i = 0; i < 3; i++) {
                int rc_autoStart = sym.SN_MV_AutoStart(mode, row, col);
                if (rc_autoStart == S_RC_SUCCESS) {
                    isAutoStart = true;
                    break;
                }
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            if (!isAutoStart) {
                LogUtil.i(TAG, "取货流程监听：取货启动失败");
                sendPickupHandlerMessage(5, "尝试3次取货启动失败", null);
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

                            PickupResult result = new PickupResult();
                            result.setActionCount(rc_flowStatus[1]);//动作总数
                            result.setCurrentActionId(rc_flowStatus[2]);//当前动作号
                            result.setCurrentActionStatusCode(rc_flowStatus[3]);//当前动作状态

                            if (rc_flowStatus[0] == S_RC_SUCCESS) {
                                if (rc_flowStatus[2] == S_ACTION_GOZERO) {
                                    if (rc_flowStatus[3] == S_Motor_Done) {
                                        result.setPickupComplete(true);//设置取货完成
                                        long nPickupEndTime = System.currentTimeMillis();
                                        long sTime = nPickupEndTime - nPickupStartTime;
                                        result.setPickupUseTime(sTime);//设置取货消耗时间
                                    }
                                }

                                if (result.isPickupComplete()) {
                                    LogUtil.i(TAG, "取货流程监听：当前动作" + result.getCurrentActionName() + "（" + result.getCurrentActionId() + "）" + "," + result.getCurrentActionStatusName() + "（" + result.getCurrentActionStatusCode() + "）");
                                    LogUtil.i(TAG, "取货流程监听：取货完成，用时" + result.getPickupUseTime());
                                    cmd_PickupIsStopListener = true;
                                    sendPickupHandlerMessage(4, "取货成功", result);
                                } else {
                                    String action_key = result.getCurrentActionId() + "-" + result.getCurrentActionStatusCode();
                                    String action_value = result.getCurrentActionName() + "-" + result.getCurrentActionStatusName();
                                    if (!nPickupActionMap.containsKey(action_key)) {
                                        LogUtil.i(TAG, "取货流程监听：当前动作" + result.getCurrentActionName() + "（" + result.getCurrentActionId() + "）" + "," + result.getCurrentActionStatusName() + "（" + result.getCurrentActionStatusCode() + "）");
                                        nPickupActionMap.put(action_key, action_value);
                                        if (rc_flowStatus[3] == S_Motor_Busy || rc_flowStatus[3] == S_Motor_Done) {
                                            sendPickupHandlerMessage(3, "正在取货中", result);
                                        } else if (rc_flowStatus[3] == S_Motor_Timeout) {
                                            cmd_PickupIsStopListener = true;
                                            goZero();
                                            disConnect();
                                            LogUtil.e(TAG, "取货流程监听：单动作运行取货超时");
                                            sendPickupHandlerMessage(5, "单动作运行取货超时", result);
                                        }
                                    }
                                }
                            }

                        } else {
                            cmd_PickupIsStopListener = true;
                            goZero();
                            disConnect();
                            LogUtil.e(TAG, "取货流程监听：整体动作运行取货超时");
                            sendPickupHandlerMessage(5, "整体动作运行取货超时", null);
                        }
                    } catch (Exception ex) {
                        cmd_PickupIsStopListener = true;
                        goZero();
                        disConnect();
                        LogUtil.e(TAG, "取货流程监听：发生异常");
                        LogUtil.e(TAG, ex);
                        sendPickupHandlerMessage(6, "取货异常", null);
                    }
                }
            }
        }
    }

    private class DoorThread extends Thread {

        @Override
        public void run() {
            super.run();

            isConnect = connect();
            if (isConnect) {
                if (sym != null) {
                    sym.SN_MV_SetLock(true);

                    sendDoorHandlerMessage(1,"请在10秒内打开/关闭柜门");

                    try {
                        Thread.sleep(10*1000);
                    }
                    catch  (Exception ex){

                    }

                    sym.SN_MV_SetLock(false);

                }
            }
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

            isConnect = connect();

            if (!isConnect) {
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

    public void testGoZero(){
        GoZeroThread goZeroThread=new GoZeroThread(true);
        goZeroThread.start();
    }

}