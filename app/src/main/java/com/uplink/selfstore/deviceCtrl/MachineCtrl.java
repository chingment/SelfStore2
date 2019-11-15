package com.uplink.selfstore.deviceCtrl;

import android.VendingMachine.symvdio;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.uplink.selfstore.model.SlotNRC;
import com.uplink.selfstore.utils.LogUtil;

import java.io.Serializable;

public class MachineCtrl {

    private int current_Cmd = 0;//当前命令 0：代表空闲

    private int cmd_ScanSlot = 1;//扫描货道命令
    private int cmd_Pickup = 2;//取货命令
    private boolean cmd_ScanSlotIsStopListener = true;
    private boolean cmd_PickupIsStopListener = true;

    private symvdio sym = null;

    public static final int MESSAGE_WHAT_SCANSLOTS=1;
    public static final int MESSAGE_WHAT_PICKUP=2;

    public MachineCtrl() {
        sym = new symvdio();
    }

    public String vesion() {
        String version = "";
        if (sym != null) {
            version = sym.SDK_Version();
        }
        return version;
    }

    public boolean connect() {

        try {


            if (sym == null) {
                return false;
            }

            int rc_status = sym.Connect("ttymxc1", 9600);

            if (rc_status == 0) {
                return true;
            }


            return false;
        }
        catch (Exception ex)
        {
            return  false;
        }
    }

    public void disConnect() {
        if (sym != null) {
            sym.disconnect();
        }

        cmd_ScanSlotIsStopListener = true;
        cmd_PickupIsStopListener = true;
    }

    public boolean isNormarl() {
        if (sym == null) {
            return false;
        }

        return  sym.SY_MV_DIO_Slave_ConnectSts();
    }

    public void scanSlot() {

        //this.current_Cmd = this.cmd_ScanSlot;
        //this.cmd_ScanSlotIsStopListener = false;

//        ScanListenerThread scanListenerThread = new ScanListenerThread();
//        scanListenerThread.start();

        if (sym == null) {
            sendScanSlotHandlerMessage(1, "启动前，检查设备对象为空", null);
        }
        else if (!this.connect()) {
            sendScanSlotHandlerMessage(1, "启动前，检查设备连接失败", null);
        } else if (!this.isNormarl()) {
            sendScanSlotHandlerMessage(1, "启动前，检查设备不在线", null);
        }
        else {
            int rc_status = sym.SN_MV_SelfAutoScan(0);
            if (rc_status == 0) {
                this.current_Cmd = this.cmd_ScanSlot;
                this.cmd_ScanSlotIsStopListener = false;
                ScanSlotListenerThread scanListenerThread = new ScanSlotListenerThread();
                scanListenerThread.start();
            } else {
                sendScanSlotHandlerMessage(1, "扫描货道启动失败", null);
            }
        }
    }

    public void pickUp(int row,int col) {

        if (sym == null) {
           sendPickupHandlerMessage(1, "启动前，检查设备对象为空", null);
        }
        else if (!this.connect()) {
            sendPickupHandlerMessage(1, "启动前，检查设备连接失败", null);
        } else if (!this.isNormarl()) {
            sendPickupHandlerMessage(1, "启动前，检查设备不在线", null);
        }
        else {
            int rc_status = sym.SN_MV_AutoStart(0,row,col);
            if (rc_status == 0) {
                this.current_Cmd = this.cmd_Pickup;
            } else {
                sendPickupHandlerMessage(1, "取货启动失败", null);
            }
        }
    }


    public void setScanSlotHandler(Handler scanSlotHandler) {
        this.scanSlotHandler=scanSlotHandler;
    }

    public void  setPickupHandler(Handler pickupHandler) {
        this.pickupHandler=pickupHandler;
        this.cmd_PickupIsStopListener = false;
        PickupListenerThread pickupListenerThread = new PickupListenerThread();
        pickupListenerThread.start();
    }


    private Handler scanSlotHandler = null;
    private Handler pickupHandler=null;

    private void sendScanSlotHandlerMessage(int status, String message, ScanSlotResult result) {
        if(scanSlotHandler!=null) {
            Message m = new Message();
            m.what = MESSAGE_WHAT_SCANSLOTS;
            Bundle data=new Bundle();
            data.putInt("status",status);
            data.putString("message",message);
            data.putSerializable("result",result);
            m.setData(data);
            scanSlotHandler.sendMessage(m);
        }
    }

    private void sendPickupHandlerMessage(int status, String message, PickupResult result) {
        if(pickupHandler!=null) {
            Message m = new Message();
            m.what = MESSAGE_WHAT_PICKUP;
            Bundle data=new Bundle();
            data.putInt("status",status);
            data.putString("message",message);
            data.putSerializable("result",result);
            m.setData(data);
            pickupHandler.sendMessage(m);
        }
    }


    private class ScanSlotListenerThread extends Thread {

        @Override
        public void run() {
            super.run();

            while (!cmd_ScanSlotIsStopListener) {

                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

//                ScanResult scanResult = new ScanResult();
//                scanResult.setRows(5);
//                int[] s=new int[]{8,7,6,5,16};
//                scanResult.setRowColLayout(s);
//                scanListener.receive(3, "扫描结束", scanResult);
//                cmd_ScanSlotIsStopListener = true;

                if (sym != null) {
                    int[] rc_status = sym.SN_MV_Get_ScanStatus();
                    if (rc_status[0] == 0) {
                        int isflag = rc_status[1];//表示扫描是否结束
                        if (isflag == 0) {
                            int[] rc_scanresult = sym.SN_MV_Get_ScanData();
                            if (rc_scanresult[0] == 0) {
                                ScanSlotResult scanSlotResult = new ScanSlotResult();

                                int rows = rc_scanresult[1];
                                scanSlotResult.setRows(rows);

                                if (rows > 0) {

                                    int[] rowColLayout = new int[rows];

                                    for (int i = 2; i < rows; i++) {
                                        rowColLayout[i - 2] = rc_scanresult[i];
                                    }

                                    scanSlotResult.setRowColLayout(rowColLayout);
                                }

                                sendScanSlotHandlerMessage(3, "扫描结束", scanSlotResult);
                                cmd_ScanSlotIsStopListener = true;
                            }
                        } else {
                            sendScanSlotHandlerMessage(2, "正在扫描", null);
                        }
                    }
                }
            }
        }
    }

    public class ScanSlotResult implements Serializable {

        public int rows;
        public int[] rowColLayout;

        public int getRows() {
            return rows;
        }

        public void setRows(int rows) {
            this.rows = rows;
        }

        public int[] getRowColLayout() {
            return rowColLayout;
        }

        public void setRowColLayout(int[] rowColLayout) {
            this.rowColLayout = rowColLayout;
        }
    }

    private class PickupListenerThread extends Thread {

        @Override
        public void run() {
            super.run();

            while (!cmd_PickupIsStopListener) {

                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }


                if (sym != null) {
                    int[] rc_status = sym.SN_MV_Get_FlowStatus();
                    if (rc_status[0] == 0) {
                        PickupResult result=new PickupResult();
                        result.setActionCount(rc_status[1]);//动作总数
                        result.setCurrentActionId(rc_status[2]);//当前动作号
                        result.setCurrentActionStatusCode(rc_status[3]);//当前动作状态
                        sendPickupHandlerMessage(2, "", result);
                    }
                }
            }
        }
    }

    public class PickupResult implements Serializable {
        private int actionCount;
        private int currentActionId;
        private String currentActionName;
        private int currentActionStatusCode;
        private String currentActionStatusName;

        public int getActionCount() {
            return actionCount;
        }

        public void setActionCount(int actionCount) {
            this.actionCount = actionCount;
        }

        public int getCurrentActionId() {
            return currentActionId;
        }

        public void setCurrentActionId(int currentActionId) {
            this.currentActionId = currentActionId;

            switch (currentActionId){

                case  0:
                    this.currentActionName="机器停止复位";
                    break;
                case  1:
                    this.currentActionName="回到原点";
                    break;
                case  2:
                    this.currentActionName="XY移动";
                    break;
                case  3:
                    this.currentActionName="接货动作1";
                    break;
                case  4:
                    this.currentActionName="接货动作2";
                    break;
                case  5:
                    this.currentActionName="Y 轴上移至出货口";
                    break;
                case  6:
                    this.currentActionName="Y 轴下移至出货口";
                    break;
                case  7:
                    this.currentActionName="货架移动至出货口";
                    break;
                case  8:
                    this.currentActionName="提货动作";
                    break;
                 default:
                     this.currentActionName="未知动作";
                     break;
            }
        }

        public String getCurrentActionName() {
            return currentActionName;
        }

        public int getCurrentActionStatusCode() {
            return currentActionStatusCode;
        }

        public void setCurrentActionStatusCode(int currentActionStatusCode) {
            this.currentActionStatusCode = currentActionStatusCode;

            switch (currentActionStatusCode)
            {
                case 0:
                    this.currentActionStatusName="空闲状态，没有执行动作";
                    break;
                case 1:
                    this.currentActionStatusName="动作执行中";
                    break;
                case 2:
                    this.currentActionStatusName="动作执行完成";
                    break;
                 default:
                     this.currentActionStatusName="未知状态";
                     break;
            }
        }

        public String getCurrentActionStatusName() {
            return currentActionStatusName;
        }

    }

}
