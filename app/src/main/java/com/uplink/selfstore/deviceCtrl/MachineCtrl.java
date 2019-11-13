package com.uplink.selfstore.deviceCtrl;

import android.VendingMachine.symvdio;

import com.uplink.selfstore.utils.LogUtil;

import java.io.Serializable;

public class MachineCtrl {

    private int current_Cmd = 0;//当前命令 0：代表空闲

    private int cmd_ScanSlot = 1;//扫描货道命令
    private boolean cmd_ScanSlotIsStopListener = true;

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

    public boolean isOnline() {
        if (sym == null) {
            return false;
        }

        return  sym.SY_MV_DIO_Slave_ConnectSts();
    }

    public void scanSlot(ScanListener scanListener) {

        this.current_Cmd = this.cmd_ScanSlot;
        this.cmd_ScanSlotIsStopListener = false;
        this.scanListener = scanListener;

        ScanListenerThread scanListenerThread = new ScanListenerThread();
        scanListenerThread.start();

//        if (sym == null) {
//            this.scanListener.receive(1, "启动前，检查设备对象为空", null);
//        }
//        else if (!this.connect()) {
//            this.scanListener.receive(1, "启动前，检查设备连接失败", null);
//        } else if (!this.isOnline()) {
//            this.scanListener.receive(1, "启动前，检查设备不在线", null);
//        }
//        else {
//            int rc_status = sym.SN_MV_SelfAutoScan(0);
//            if (rc_status == 0) {
//                this.current_Cmd = this.cmd_ScanSlot;
//                this.cmd_ScanSlotIsStopListener = false;
//                ScanListenerThread scanListenerThread = new ScanListenerThread();
//                scanListenerThread.start();
//            } else {
//                this.scanListener.receive(1, "扫描货道启动失败", null);
//            }
//        }
    }

    private ScanListener scanListener = null;

    public interface ScanListener {
        //1 异常错误
        void receive(int status, String message, ScanResult scanResult);
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

                ScanResult scanResult = new ScanResult();
                scanResult.setRows(5);
                int[] s=new int[]{8,7,6,5,16};
                scanResult.setRowColLayout(s);
                scanListener.receive(3, "扫描结束", scanResult);
                cmd_ScanSlotIsStopListener = true;

//                if (sym != null) {
//                    int[] rc_status = sym.SN_MV_Get_ScanStatus();
//                    if (rc_status[0] == 0) {
//                        int isflag = rc_status[1];//表示扫描是否结束
//                        if (isflag == 0) {
//                            int[] rc_scanresult = sym.SN_MV_Get_ScanData();
//                            if (rc_scanresult[0] == 0) {
//                                ScanResult scanResult = new ScanResult();
//
//                                int rows = rc_scanresult[1];
//                                scanResult.setRows(rows);
//
//                                if (rows > 0) {
//
//                                    int[] rowColLayout = new int[rows];
//
//                                    for (int i = 2; i < rows; i++) {
//                                        rowColLayout[i - 2] = rc_scanresult[i];
//                                    }
//
//                                    scanResult.setRowColLayout(rowColLayout);
//                                }
//
//                                scanListener.receive(3, "扫描结束", scanResult);
//                                cmd_ScanSlotIsStopListener = true;
//                            }
//                        } else {
//                            scanListener.receive(2, "正在扫描", null);
//                        }
//                    }
//                }
            }
        }
    }

    public class ScanResult implements Serializable {

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
}
