package com.uplink.selfstore.deviceCtrl;

import android.VendingMachine.symvdio;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.uplink.selfstore.activity.SmMachineStockActivity;
import com.uplink.selfstore.model.ScanSlotResult;
import com.uplink.selfstore.model.SlotNRC;
import com.uplink.selfstore.utils.LogUtil;

import java.io.Serializable;

public class MachineCtrl {

    private static final String TAG = "MachineCtrl";
    public static int S_Motor_Idle = 0;
    public static int S_Motor_Busy = 1;
    public static int S_Motor_Done = 2;
    public static int S_RC_SUCCESS = 0;
    public static int S_RC_INVALID_PARAM = 1;
    public static int S_RC_ERROR = 2;
    public static int S_ACTION_GOZERO=1;

    private boolean isConnect = false;
    private int current_Cmd = 0;//当前命令 0：代表空闲
    private int cmd_ScanSlot = 1;//扫描货道命令
    private int cmd_Pickup = 2;//取货命令
    private boolean cmd_ScanSlotIsStopListener = true;
    private boolean cmd_PickupIsStopListener = true;
    private PickupListenerThread pickupListenerThread;
    private ScanSlotListenerThread scanListenerThread;
    private symvdio sym = null;

    public static final int MESSAGE_WHAT_SCANSLOTS=1;
    public static final int MESSAGE_WHAT_PICKUP=2;

    public MachineCtrl() {
        try {
            sym = new symvdio();
        }
        catch (Exception ex)
        {
            sym=null;
        }
    }

    public String vesion() {
        String version = "";
        if (sym != null) {
            version = sym.SDK_Version();
        }
        return version;
    }

    public boolean connect() {

        if(sym==null) {
            isConnect=false;
            return isConnect;
        }

        try {
            int rc_status = sym.Connect("ttymxc1", 9600);
            if (rc_status == 0) {
                isConnect = true;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            isConnect=false;
            return isConnect;
        }
        finally {
            return isConnect;
        }

    }

    public void disConnect() {
        if (sym != null) {
            sym.disconnect();
        }
        isConnect=false;
        cmd_ScanSlotIsStopListener = true;
        cmd_PickupIsStopListener = true;
    }

    public boolean isNormarl() {
        if (sym == null) {
            return false;
        }
        boolean isflag=sym.SY_MV_DIO_Slave_ConnectSts();
        if(!isflag) {
            connect();
        }
        isflag=sym.SY_MV_DIO_Slave_ConnectSts();

        LogUtil.i("isNormarl:"+isflag);
        return  isflag;
    }

    public boolean isConnect()
    {
        return  isConnect;
    }
    public int[] getScanSlotResult(){
        return  sym.SN_MV_Get_ScanData();
    }
    public void scanSlot() {
        if (!isConnect) {
            sendScanSlotHandlerMessage(1, "启动前，检查设备连接失败", null);
        } else if (!this.isNormarl()) {
            sendScanSlotHandlerMessage(1, "启动前，检查设备不在线", null);
        } else if (!this.isIdle()) {
            sendScanSlotHandlerMessage(1, "启动前，检查设备不在空闲状态", null);
        }
        else {
            int rc_status = sym.SN_MV_SelfAutoScan(0);
            if (rc_status == 0) {
                sendScanSlotHandlerMessage(2, "扫描货道启动成功", null);
                this.current_Cmd = this.cmd_ScanSlot;
                this.cmd_ScanSlotIsStopListener = false;
                scanListenerThread = new ScanSlotListenerThread();
                scanListenerThread.start();
            } else {
                sendScanSlotHandlerMessage(1, "扫描货道启动失败", null);
            }
        }
    }

    public void pickUp(int row,int col) {
        isConnect=connect();
        if (!isConnect) {
            sendPickupHandlerMessage(1, "启动前，检查设备连接失败", null);
        } else if (!this.isNormarl()) {
            sendPickupHandlerMessage(1, "启动前，检查设备不在线", null);
        } else if (!this.isIdle()) {
            sendPickupHandlerMessage(1, "启动前，检查设备不在空闲状态", null);
        } else {
            int rc_status = sym.SN_MV_AutoStart(0, row, col);
            if (rc_status == 0) {
                sendPickupHandlerMessage(2, "取货就绪", null);
                this.current_Cmd = this.cmd_Pickup;
                this.cmd_PickupIsStopListener = false;
                pickupListenerThread = new PickupListenerThread();
                pickupListenerThread.start();
            } else {
                sendPickupHandlerMessage(1, "取货启动失败", null);
            }
        }
    }

    public boolean isIdle() {
        boolean flag = false;

        if (sym != null) {
            boolean flag1 = false;
            int[] rc_status1 = sym.SN_MV_Get_ManuProcStatus();
            if (rc_status1[0] == S_RC_SUCCESS) {
                if (rc_status1[2] == S_Motor_Idle||rc_status1[2] == S_Motor_Done) {
                    flag1 = true;
                }
            }

            boolean flag2 = false;
            int[] rc_status2 = sym.SN_MV_Get_FlowStatus();
            if (rc_status2[0] == S_RC_SUCCESS) {
                if (rc_status2[3] == S_Motor_Idle||rc_status2[3]==S_Motor_Done) {
                    flag2 = true;
                }
            }

            int[] rc_status3 = sym.SN_MV_Get_MotionStatus();
            int[] rc_status4 = sym.SN_MV_Get_ScanStatus();


          //  int s=sym.SN_MV_MotorAction(1,0,0);
            if (flag1 && flag2) {
                return true;
            }

            return false;
        }
        return false;
    }

    public void setScanSlotHandler(Handler scanSlotHandler) {
        this.scanSlotHandler=scanSlotHandler;
    }

    public void  setPickupHandler(Handler pickupHandler) {
        this.pickupHandler=pickupHandler;
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
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                try {
                    if (sym != null) {
                        int[] rc_status = sym.SN_MV_Get_ScanStatus();
                        if (rc_status[0] == S_RC_SUCCESS) {
                            LogUtil.d("扫描结果rc_status0:" + rc_status[0]);
                            int isflag = rc_status[1];//表示扫描是否结束

                            LogUtil.d("扫描结果rc_status1-isflag:" + isflag);
                            if (isflag == 0) {
                                int[] rc_scanresult = sym.SN_MV_Get_ScanData();

                                LogUtil.i("rc_scanresult:" + rc_scanresult[0]);
                                LogUtil.d("扫描结果2-大小：" + rc_scanresult.length);
                                for (int i = 0; i < rc_scanresult.length; i++) {
                                    LogUtil.d("扫描结果2-" + i + "：" + rc_scanresult[i]);
                                }

                                if (rc_scanresult[0] == S_RC_SUCCESS) {

                                    LogUtil.i("扫描结果成功");

                                    ScanSlotResult scanSlotResult = new ScanSlotResult();

                                    int rows = rc_scanresult[1];
                                    scanSlotResult.setRows(rows);

                                    if (rows > 0) {

                                        int[] rowColLayout = new int[rows];

                                        for (int i = 0; i < rows; i++) {
                                            rowColLayout[i] = rc_scanresult[2 + i];
                                        }

                                        scanSlotResult.setRowColLayout(rowColLayout);

                                        LogUtil.i("结果，行：" + rows + ",列：" + rowColLayout);
                                    }
                                    disConnect();
                                    cmd_ScanSlotIsStopListener = true;
                                    sendScanSlotHandlerMessage(4, "扫描结束", scanSlotResult);
                                }
                            } else {
                                sendScanSlotHandlerMessage(3, "正在扫描", null);
                            }
                        }
                    }
                }
                catch (Exception ex) {
                    //todo 处理异常操作
                    ex.printStackTrace();
                    LogUtil.e(TAG,"扫描流程处理失败");
                    LogUtil.e(TAG,ex);
                    disConnect();
                    cmd_ScanSlotIsStopListener = true;
                    sendScanSlotHandlerMessage(5, "扫描失败", null);
                }
            }
        }
    }


    private class PickupListenerThread extends Thread {

        @Override
        public void run() {
            super.run();

            while (!cmd_PickupIsStopListener) {

//                try {
//                    Thread.sleep(50);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }

                try {
                    if (sym != null) {
                        int[] rc_status = sym.SN_MV_Get_FlowStatus();
                        if (rc_status[0] == S_RC_SUCCESS) {
                            PickupResult result = new PickupResult();
                            result.setActionCount(rc_status[1]);//动作总数
                            result.setCurrentActionId(rc_status[2]);//当前动作号
                            result.setCurrentActionStatusCode(rc_status[3]);//当前动作状态

                            if (rc_status[2] == S_ACTION_GOZERO) {
                                if (rc_status[3] == S_Motor_Done) {
                                    result.setPickupComplete(true);//设置取货完成
                                }
                            }

                            if (result.isPickupComplete()) {
                                cmd_PickupIsStopListener = true;
                                sendPickupHandlerMessage(4, "取货成功", result);
                            } else {
                                sendPickupHandlerMessage(3, "正在取货中", result);
                            }
                        } else {
                            LogUtil.i("动作状态查询失败");
                            //sendPickupHandlerMessage(1, "动作状态查询失败", null);
                        }
                    }
                }
                catch (Exception ex) {
                    LogUtil.e(TAG,"取货发生异常");
                    LogUtil.e(TAG,ex);
                    cmd_PickupIsStopListener = true;
                    sendPickupHandlerMessage(5, "取货异常", null);
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
        private String currentActionStatusName2;
        private boolean isPickupComplete;

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
                    this.currentActionStatusName2="空闲状态";
                    break;
                case 1:
                    this.currentActionStatusName="动作执行中";
                    this.currentActionStatusName2="设备运行中";
                    break;
                case 2:
                    this.currentActionStatusName="动作执行完成";
                    this.currentActionStatusName2="执行完成";
                    break;
                 default:
                     this.currentActionStatusName="未知状态";
                     this.currentActionStatusName2="未知状态";
                     break;
            }
        }

        public String getCurrentActionStatusName() {
            return currentActionStatusName;
        }

        public String getCurrentActionStatusName2() {
            return currentActionStatusName2;
        }

        public boolean isPickupComplete() {
            return isPickupComplete;
        }

        public void setPickupComplete(boolean pickupComplete) {
            isPickupComplete = pickupComplete;
        }
    }

}
