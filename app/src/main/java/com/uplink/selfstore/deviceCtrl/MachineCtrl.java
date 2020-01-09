package com.uplink.selfstore.deviceCtrl;

import android.VendingMachine.symvdio;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.uplink.selfstore.activity.SmMachineStockActivity;
import com.uplink.selfstore.model.PickupResult;
import com.uplink.selfstore.model.ScanSlotResult;
import com.uplink.selfstore.model.SlotNRC;
import com.uplink.selfstore.utils.LogUtil;

import java.io.Serializable;
import java.util.HashMap;

public class MachineCtrl {

    private static final String TAG = "MachineCtrl";
    private static MachineCtrl mMachineCtrl= null;
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
    private Handler scanSlotHandler = null;
    private Handler pickupHandler = null;
    private symvdio sym = null;
    private static final int MESSAGE_WHAT_SCANSLOTS = 1;
    private static final int MESSAGE_WHAT_PICKUP = 2;


    private  MachineCtrl() {
        try {
            sym = new symvdio();
        } catch (Exception ex) {
            sym = null;
        }
    }

    public static MachineCtrl getInstance() {
        if (mMachineCtrl == null) {
            synchronized (FingerVeinCtrl.class) {
                if (mMachineCtrl == null) {
                    mMachineCtrl = new MachineCtrl();
                }
            }
        }
        return mMachineCtrl;

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
        boolean isflag = sym.SY_MV_DIO_Slave_ConnectSts();
        if (!isflag) {
            connect();
        }
        isflag = sym.SY_MV_DIO_Slave_ConnectSts();

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

    public void goGoZero() {
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

    public void openPickupDoor() {
        isConnect = connect();
        if (isConnect) {
            if (sym != null) {
                sym.SN_MV_MotorAction(1, 0, 0);
            }
        }
    }

    public void pickUp(int mode, int row, int col) {
        pickupListenerThread = new PickupListenerThread(mode, row, col);
        pickupListenerThread.start();
    }

    public boolean isIdle() {
        boolean flag = false;

        long nStart = System.currentTimeMillis();
        long nEnd = System.currentTimeMillis();
        boolean bTryAgain = false;

        for (; (nEnd - nStart <= (long) 1000 || bTryAgain); nEnd = System.currentTimeMillis()) {
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
                        goGoZero();
                        disConnect();
                        cmd_ScanSlotIsStopListener = true;
                        sendScanSlotHandlerMessage(5, "扫描超时", null);
                    }
                } catch (Exception ex) {
                    //todo 处理异常操作
                    ex.printStackTrace();
                    LogUtil.e(TAG, "扫描流程监听：扫描处理失败");
                    LogUtil.e(TAG, ex);
                    goGoZero();
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
                sendPickupHandlerMessage(1, "启动前，检查设备连接失败", null);
                return;
            }

            if (!isNormarl()) {
                LogUtil.i(TAG, "取货流程监听：启动前，检查设备不在线");
                sendPickupHandlerMessage(1, "启动前，检查设备不在线", null);
                return;
            }

            if (!isIdle()) {
                LogUtil.i(TAG, "取货流程监听：启动前，检查设备不在空闲状态");
                sendPickupHandlerMessage(1, "启动前，检查设备不在空闲状态", null);
                return;
            }

            int rt_goZero = sym.SN_MV_MotorAction(1, 0, 0);
            if (rt_goZero != S_RC_SUCCESS) {
                LogUtil.i(TAG, "取货流程监听：启动回原点失败");
                sendPickupHandlerMessage(1, "启动回原点失败", null);
                return;
            }

            LogUtil.i(TAG, "取货流程监听：取货就绪");

            sendPickupHandlerMessage(2, "取货就绪", null);

            long nStart = System.currentTimeMillis();
            long nEnd = System.currentTimeMillis();
            boolean bTryAgain = false;
            boolean bCanAutoStart = false;
            for (; (nEnd - nStart <= (long) 60 * 1000 || bTryAgain); nEnd = System.currentTimeMillis()) {
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
                sendPickupHandlerMessage(1, "取货回原点失败", null);
                return;
            }

            LogUtil.i(TAG, "取货流程监听：mode:" + mode + ",row:" + row + ",col:" + col);

            int rc_autoStart = sym.SN_MV_AutoStart(mode, row, col);
            if (rc_autoStart != S_RC_SUCCESS) {
                LogUtil.i(TAG, "取货流程监听：取货启动失败");
                sendPickupHandlerMessage(1, "取货启动失败", null);
                return;
            }

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
                                    if(rc_flowStatus[3] == S_Motor_Busy||rc_flowStatus[3] == S_Motor_Done){
                                        sendPickupHandlerMessage(3, "正在取货中", result);
                                    }
                                    else if(rc_flowStatus[3] == S_Motor_Timeout) {
                                        goGoZero();
                                        disConnect();
                                        LogUtil.e(TAG, "取货流程监听：单动作运行取货超时");
                                        cmd_PickupIsStopListener = true;
                                        sendPickupHandlerMessage(5, "单动作运行取货超时", result);
                                    }
                                }
                            }
                        }

                    } else {
                        goGoZero();
                        disConnect();
                        LogUtil.e(TAG, "取货流程监听：整体动作运行取货超时");
                        cmd_PickupIsStopListener = true;
                        sendPickupHandlerMessage(5, "整体动作运行取货超时", null);
                    }
                } catch (Exception ex) {
                    goGoZero();
                    disConnect();
                    LogUtil.e(TAG, "取货流程监听：发生异常");
                    LogUtil.e(TAG, ex);
                    cmd_PickupIsStopListener = true;
                    sendPickupHandlerMessage(6, "取货异常", null);
                }
            }
        }
    }
}
