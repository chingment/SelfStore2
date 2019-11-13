package com.uplink.selfstore.deviceCtrl;

import android.VendingMachine.symvdio;

import com.uplink.selfstore.utils.LogUtil;

import java.io.Serializable;

public class MachineCtrl {

    private int current_Cmd = 0;//当前命令 0：代表空闲

    private int cmd_ScanSlot = 1;//扫描货道命令
    private int cmd_Pickup = 2;//取货命令
    private boolean cmd_ScanSlotIsStopListener = true;
    private boolean cmd_PickupIsStopListener = true;

    private symvdio sym = null;

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
    }

    public boolean isNormarl() {
        if (sym == null) {
            return false;
        }

        return  sym.SY_MV_DIO_Slave_ConnectSts();
    }

    public void scanSlot(ScanSlotListener scanSlotListener) {
        this.scanSlotListener = scanSlotListener;

        //this.current_Cmd = this.cmd_ScanSlot;
        //this.cmd_ScanSlotIsStopListener = false;

//        ScanListenerThread scanListenerThread = new ScanListenerThread();
//        scanListenerThread.start();

        if (sym == null) {
            this.scanSlotListener.receive(1, "启动前，检查设备对象为空", null);
        }
        else if (!this.connect()) {
            this.scanSlotListener.receive(1, "启动前，检查设备连接失败", null);
        } else if (!this.isNormarl()) {
            this.scanSlotListener.receive(1, "启动前，检查设备不在线", null);
        }
        else {
            int rc_status = sym.SN_MV_SelfAutoScan(0);
            if (rc_status == 0) {
                this.current_Cmd = this.cmd_ScanSlot;
                this.cmd_ScanSlotIsStopListener = false;
                ScanListenerThread scanListenerThread = new ScanListenerThread();
                scanListenerThread.start();
            } else {
                this.scanSlotListener.receive(1, "扫描货道启动失败", null);
            }
        }
    }


    public void pickUp(int row,int col,PickupListener pickupListener) {

        this.pickupListener=pickupListener;

        if (sym == null) {
            this.pickupListener.receive(1, "启动前，检查设备对象为空", null);
        }
        else if (!this.connect()) {
            this.pickupListener.receive(1, "启动前，检查设备连接失败", null);
        } else if (!this.isNormarl()) {
            this.pickupListener.receive(1, "启动前，检查设备不在线", null);
        }
        else {
            int rc_status = sym.SN_MV_AutoStart(0,row,col);
            if (rc_status == 0) {
                this.current_Cmd = this.cmd_Pickup;
                this.cmd_PickupIsStopListener = false;
                PickupListenerThread pickupListenerThread = new PickupListenerThread();
                pickupListenerThread.start();
            } else {
                this.pickupListener.receive(1, "取货启动失败", null);
            }
        }
    }

    private ScanSlotListener scanSlotListener = null;
    private PickupListener pickupListener=null;

    public interface ScanSlotListener {
        //1 异常错误
        void receive(int status, String message, ScanSlotResult result);
    }

    public interface PickupListener {
        //1 异常错误
        void receive(int status, String message, PickupResult result);
    }

    private class ScanListenerThread extends Thread {

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

                                scanSlotListener.receive(3, "扫描结束", scanSlotResult);
                                cmd_ScanSlotIsStopListener = true;
                            }
                        } else {
                            scanSlotListener.receive(2, "正在扫描", null);
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

    public class PickupResult implements Serializable {

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
                        int action = rc_status[3];//表示扫描是否结束

                        if(action==0)
                        {
                            pickupListener.receive(0, "空闲状态，没有执行动作", null);
                        }
                        else if(action==1)
                        {
                            pickupListener.receive(1, "正在取货中", null);
                        }
                        else if(action==2)
                        {
                            pickupListener.receive(2, "取货完成", null);
                        }
                        else
                        {

                        }

                    }
                    else {

                    }
                }
            }
        }
    }
}
