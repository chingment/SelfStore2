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
import java.util.HashMap;

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
    private HashMap<String, String> action_map = new HashMap<>();

    public long nPickupStartTime=0;

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

        LogUtil.i(TAG,"isNormarl:"+isflag);
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
        } else {

            int rt_goZero = sym.SN_MV_MotorAction(1, 0, 0);
            if (rt_goZero != S_RC_SUCCESS) {
                sendScanSlotHandlerMessage(1, "启动回原点失败", null);
                return;
            }

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
                sendScanSlotHandlerMessage(1, "回原点失败", null);
                return;
            }

            int rc_selfAutoScan = sym.SN_MV_SelfAutoScan(0);


            if (rc_selfAutoScan != S_RC_SUCCESS) {
                sendScanSlotHandlerMessage(1, "扫描货道启动失败", null);
                return;
            }

            this.current_Cmd = this.cmd_ScanSlot;
            this.cmd_ScanSlotIsStopListener = false;
            scanListenerThread = new ScanSlotListenerThread();
            scanListenerThread.start();

        }
    }


    public  void  goGoZero() {
        isConnect=connect();
        if (isConnect) {
            if(sym!=null) {
                sym.SN_MV_MotorAction(1, 0, 0);
            }
        }
    }

    public void pickUp(int row,int col) {
        isConnect = connect();
        if (!isConnect) {
            sendPickupHandlerMessage(1, "启动前，检查设备连接失败", null);
        } else if (!this.isNormarl()) {
            sendPickupHandlerMessage(1, "启动前，检查设备不在线", null);
        } else if (!this.isIdle()) {
            sendPickupHandlerMessage(1, "启动前，检查设备不在空闲状态", null);
        } else {

            int rt_goZero = sym.SN_MV_MotorAction(1, 0, 0);
            if (rt_goZero != S_RC_SUCCESS) {
                sendPickupHandlerMessage(1, "启动回原点失败", null);
                return;
            }

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
                sendPickupHandlerMessage(1, "取货回原点失败", null);
                return;
            }


            int rc_autoStart = sym.SN_MV_AutoStart(0, row, col);
            if (rc_autoStart != S_RC_SUCCESS) {
                sendPickupHandlerMessage(1, "取货启动失败", null);
                return;
            }

            action_map = new HashMap<>();
            this.current_Cmd = this.cmd_Pickup;
            this.cmd_PickupIsStopListener = false;
            pickupListenerThread = new PickupListenerThread();
            pickupListenerThread.start();

        }
    }

    public boolean isIdle() {
        boolean flag = false;

        long nStart = System.currentTimeMillis();
        long nEnd = System.currentTimeMillis();
        boolean bTryAgain = false;

        if (sym != null) {

            for(;(nEnd - nStart <= (long)1000 || bTryAgain); nEnd = System.currentTimeMillis()) {
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

                flag=flag1 && flag2;

                if(!flag) {
                    bTryAgain=true;
                }
                else {
                    break;
                }

            }

          //  int[] rc_status3 = sym.SN_MV_Get_MotionStatus();
          //  int[] rc_status4 = sym.SN_MV_Get_ScanStatus();

            return flag;
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
                            LogUtil.i(TAG,"扫描结果rc_status0:" + rc_status[0]);
                            int isflag = rc_status[1];//表示扫描是否结束

                            LogUtil.i(TAG,"扫描结果rc_status1-isflag:" + isflag);
                            if (isflag == 0) {
                                int[] rc_scanresult = sym.SN_MV_Get_ScanData();

                                LogUtil.i(TAG,"rc_scanresult:" + rc_scanresult[0]);
                                LogUtil.i(TAG,"扫描结果2-大小：" + rc_scanresult.length);
                                for (int i = 0; i < rc_scanresult.length; i++) {
                                    LogUtil.d("扫描结果2-" + i + "：" + rc_scanresult[i]);
                                }

                                if (rc_scanresult[0] == S_RC_SUCCESS) {

                                    LogUtil.i(TAG,"扫描结果成功");

                                    ScanSlotResult scanSlotResult = new ScanSlotResult();

                                    int rows = rc_scanresult[1];
                                    scanSlotResult.setRows(rows);

                                    if (rows > 0) {

                                        int[] rowColLayout = new int[rows];

                                        for (int i = 0; i < rows; i++) {
                                            rowColLayout[i] = rc_scanresult[2 + i];
                                        }

                                        scanSlotResult.setRowColLayout(rowColLayout);

                                        LogUtil.i(TAG,"结果，行：" + rows + ",列：" + rowColLayout);
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
            long  nPickupStartTime=System.currentTimeMillis();
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
                                    long nPickupEndTime = System.currentTimeMillis();
                                    long sTime=nPickupEndTime-nPickupStartTime;
                                    result.setPickupUseTime(sTime);//设置取货消耗时间
                                }
                            }

                            if (result.isPickupComplete()) {
                                LogUtil.i(TAG,"取货流程监听：当前动作"+result.getCurrentActionName()+"（"+result.getCurrentActionId()+"）" + "," + result.getCurrentActionStatusName()+"（"+result.getCurrentActionStatusCode()+"）");
                                LogUtil.i(TAG,"取货流程监听：取货完成，用时"+result.getPickupUseTime());
                                cmd_PickupIsStopListener = true;
                                sendPickupHandlerMessage(4, "取货成功", result);
                            } else {
                                String action_key=result.getCurrentActionId()+"-"+result.getCurrentActionStatusCode();
                                String action_value=result.getCurrentActionName()+"-"+result.getCurrentActionStatusName();
                                if(!action_map.containsKey(action_key)) {
                                    LogUtil.i(TAG,"取货流程监听：当前动作"+result.getCurrentActionName()+"（"+result.getCurrentActionId()+"）" + "," + result.getCurrentActionStatusName()+"（"+result.getCurrentActionStatusCode()+"）");
                                    action_map.put(action_key,action_value);
                                    sendPickupHandlerMessage(3, "正在取货中", result);
                                }
                            }
                        } else {
                            LogUtil.e(TAG,"取货流程监听：流程状态查询失败");
                        }
                    }
                }
                catch (Exception ex) {
                    LogUtil.e(TAG,"取货流程监听：发生异常");
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
        private boolean isPickupComplete;
        private long pickupUseTime;

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

        public boolean isPickupComplete() {
            return isPickupComplete;
        }

        public void setPickupComplete(boolean pickupComplete) {
            isPickupComplete = pickupComplete;
        }

        public long getPickupUseTime() {
            return pickupUseTime;
        }

        public void setPickupUseTime(long pickupUseTime) {
            this.pickupUseTime = pickupUseTime;
        }
    }

}
