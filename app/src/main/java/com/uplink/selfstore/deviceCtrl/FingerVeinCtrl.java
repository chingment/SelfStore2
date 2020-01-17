package com.uplink.selfstore.deviceCtrl;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.tamic.statinterface.stats.core.TcStatInterface;
import com.uplink.selfstore.utils.LogUtil;
import com.wedone.BioVein;

import java.util.HashMap;

import static java.lang.Thread.sleep;

public class FingerVeinCtrl {
    private static String TAG = "FingerVeinCtrl";
    private static FingerVeinCtrl mFingerVeinCtrl= null;

   // private Context mContext;
    private byte[]  mByteDevName;
    private Handler checkLoginHandler = null;
    private boolean checkLoginIsStopListener = true;
    private boolean checkLoginIsPauseListener = false;
    private Handler collectHandler = null;
    private boolean collectIsStopListener = true;
    private  int connect_status=0;


    private CheckLoginListenerThread checkLoginListenerThread;

    public static FingerVeinCtrl getInstance() {
        if (mFingerVeinCtrl == null) {
            synchronized (FingerVeinCtrl.class) {
                if (mFingerVeinCtrl == null) {
                    mFingerVeinCtrl = new FingerVeinCtrl();
                }
            }
        }
        return mFingerVeinCtrl;

    }

    public int connect(Context mContext) {


        int fd = tryGetPermission(mContext);
        if(fd==0){
            connect_status=1;
            return connect_status;
        }

        byte m_DevList[][] = new byte[10][64];
        BioVein.FV_EnumDevice(m_DevList);

        String s_devName = "";
        for (int i = 0; i < 10; i++) {
            String i_name = new String(m_DevList[i]);
            i_name = i_name.trim();
            if (i_name.length() > 0) {
                //data_list.add(s);
                LogUtil.i("设备名称：" + i_name);
                if (i_name.contains("B")) {
                    s_devName = i_name;
                    break;
                }
            }
        }

        if (s_devName == "") {
            connect_status=2;
            return connect_status;
        }

        byte[] b_DevName = s_devName.getBytes();

        //初始化设备
        int ret_Init = BioVein.FV_InitDevice(b_DevName);

        if (ret_Init != 0) {
            connect_status=3;
            return connect_status;
        }

        //打开设备
        int ret_Open = BioVein.FV_OpenDevice(b_DevName, fd);
        if (ret_Open != 0) {
            connect_status=4;
            return connect_status;
        }

        mByteDevName=b_DevName;

        connect_status=0;
        return connect_status;
    }

    public int getConnectStatus(){
        return connect_status;
    }

    public void disConnect(Context mContext) {

        checkLoginIsStopListener = true;

        try {
           mContext.unregisterReceiver(mUsbPermissionActionReceiver);
        } catch (Exception e) {
            LogUtil.e(TAG, "取消注册广播失败");
        }

        if (mByteDevName != null) {
            BioVein.FV_CloseDevice(mByteDevName);
            BioVein.FV_RemoveDevice(mByteDevName);
        }

        LogUtil.e(TAG, "USB静脉指设备->关闭成功");
    }

    public void startCheckLogin() {

        if(checkLoginListenerThread==null) {
            checkLoginListenerThread = new CheckLoginListenerThread();
            checkLoginListenerThread.start();
        }
        else if(!(checkLoginListenerThread.isAlive())){
            checkLoginListenerThread=new CheckLoginListenerThread();
            checkLoginListenerThread.start();
        }


    }

    public void stopCheckLogin() {
       checkLoginIsStopListener=true;
    }

    public void  pauseCheckLogin(){
        checkLoginIsPauseListener=true;
    }

    public void  resumeCheckLogin(){
        checkLoginIsPauseListener=false;
    }

    private void sendCheckLoginHandlerMessage(int status, String message,byte[] result) {
        if (checkLoginHandler != null) {
            Message m = new Message();
            m.what = 1;
            Bundle data = new Bundle();
            data.putInt("status", status);
            data.putString("message", message);
            data.putByteArray("result",result);
            m.setData(data);
            checkLoginHandler.sendMessage(m);
        }
    }

    public void setCheckLoginHandler(Handler checkLoginHandler) {
        this.checkLoginHandler = checkLoginHandler;
    }

    private class CheckLoginListenerThread extends Thread {

        @Override
        public void run() {

            int status = getConnectStatus();
            if (status != 0) {
                sendCheckLoginHandlerMessage(1, "静指脉设备连接异常", null);
                return;
            }


            checkLoginIsStopListener= false;
            checkLoginIsPauseListener=false;

            while (!checkLoginIsStopListener) {
                LogUtil.i(TAG, "指静脉采检查登录监听->开始");
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if(checkLoginIsPauseListener){

                    LogUtil.i(TAG, "指静脉采检查登录监听->暂停监听");
                    continue;
                }

                byte bFingerSt[] = new byte[1];//手指状态
                int ret_FingerDetect = BioVein.FV_FingerDetect(mByteDevName, bFingerSt);
                if(ret_FingerDetect==0){
                    LogUtil.i(TAG, "指静脉采检查登录监听->获取手指状态成功");
                    if(bFingerSt[0]==0x00){
                        LogUtil.i(TAG, "指静脉采检查登录监听->获取手指状态成功,手指未放置");
                    }
                    else if(bFingerSt[0]==0x03){
                        LogUtil.i(TAG, "指静脉采检查登录监听->获取手指状态成功,手指已放置");
                        try {
                            Thread.sleep(50);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        //一枚手指特征数据
                        byte[] featureData = new byte[512];
                        int flag=0;
                        int ret_GrabFeature = BioVein.FV_GrabFeature(mByteDevName, featureData, flag);
                        if (ret_GrabFeature == 0) {
                            pauseCheckLogin();//暂停监听，若恢复 使用 resumeCheckLogin 函数
                            LogUtil.i(TAG, "指静脉采检查登录监听->获取手指信息成功");
                            sendCheckLoginHandlerMessage(2, "检测到有信息", featureData);
                        }
                        else {
                            LogUtil.i(TAG, "指静脉采检查登录监听->获取手指信息失败,结果："+ret_GrabFeature);
                        }
                    }
                }
                else {
                    LogUtil.i(TAG, "指静脉采检查登录监听->获取手指状态失败");
                }

                LogUtil.i(TAG, "指静脉采检查登录监听->结束");
            }
        }
    }

    public void startCollect() {
        CollectListenerThread collectListenerThread = new CollectListenerThread();
        collectListenerThread.start();
    }

    public void  stopCollect(){
        collectIsStopListener=true;
    }

    public void setCollectHandler(Handler collectHandler) {
        this.collectHandler = collectHandler;
    }

    private void sendCollectHandlerMessage(int status, String message,byte[] result) {
        if (collectHandler != null) {
            Message m = new Message();
            m.what = 1;
            Bundle data = new Bundle();
            data.putInt("status", status);
            data.putString("message", message);
            data.putByteArray("result",result);
            m.setData(data);
            collectHandler.sendMessage(m);
        }
    }


    private class CollectListenerThread extends Thread {

        @Override
        public void run() {

            int status = getConnectStatus();
            if (status != 0) {
                sendCollectHandlerMessage(3, "静指脉设备连接异常，点击重新采集",null);
                return;
            }

            collectIsStopListener=false;


            int reg_cur=0;
            int reg_n = 3;//当前注册枚数
            byte[] reg_feature = new byte[512 * reg_n];//当前注册数据
            boolean reg_ret = false;//当前注册结果

            while (!collectIsStopListener){
                LogUtil.i(TAG, "指静脉采集流程监听->开始");
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                byte bFingerSt[] = new byte[1];//手指状态
                int ret = BioVein.FV_FingerDetect(mByteDevName, bFingerSt);
                if(ret==0){
                    LogUtil.i(TAG, "指静脉采集流程监听->获取手指状态成功");
                    if(bFingerSt[0]==0x0){
                        LogUtil.i(TAG, "指静脉采集流程监听->获取手指状态未放置");
                        //sendCollectHandlerMessage(1, "请放下手指", null);
                    }
                    else if(bFingerSt[0]==0x3){
                       // sendCollectHandlerMessage(1, "已放下手指", null);
                        LogUtil.i(TAG, "指静脉采集流程监听->获取手指状态已放置");
                        //一枚手指特征数据
                        byte[] cur_feature = new byte[512];
                        int cur_flag=0;
                        int cur_ret_GrabFeature = BioVein.FV_GrabFeature(mByteDevName, cur_feature, cur_flag);
                        if (cur_ret_GrabFeature == 0) {
                            LogUtil.i(TAG, "指静脉采集流程监听->获取手指信息成功");

                            try {
                                Thread.sleep(50);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                            if (reg_cur > 0) {
                                byte[] featureTmp = new byte[512 * reg_cur];
                                System.arraycopy(reg_feature, 0, featureTmp, 0, 512 * reg_cur);
                                ret = BioVein.FV_IsSameFinger(cur_feature, featureTmp, reg_cur, 0x03);
                                if (ret != 0) {
                                    LogUtil.i(TAG, "指静脉采集流程监听：与前一次对比不同一手指");
                                    sendCollectHandlerMessage(3, "与前一次对比不同一手指，请重新采集", reg_feature);
                                    collectIsStopListener=true;
                                    break;
                                }
                            }

                            System.arraycopy(cur_feature, 0, reg_feature, 512 * reg_cur, 512);

                            try {
                                Thread.sleep(50);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                            ret = BioVein.FV_FingerDetect(mByteDevName, bFingerSt);

                            if(ret==0){
                                if(bFingerSt[0]==0x0) {
                                    LogUtil.i(TAG, "指静脉采集流程监听->获取手指状态未放置");

                                    reg_cur++;

                                    LogUtil.i(TAG, "指静脉采集流程监听->采集结果:成功采集" + reg_cur + "次");

                                    if(reg_cur==3) {
                                        collectIsStopListener=true;
                                        sendCollectHandlerMessage(2, "已成功采集", reg_feature);
                                        break;
                                    }
                                    else {
                                        sendCollectHandlerMessage(1, "已成功采集"+reg_cur+"次，请再次放入设备再移开，还需采集"+(reg_n-reg_cur)+"次", null);
                                    }
                                }
                                else {
                                    //sendCollectHandlerMessage(1, "手指已放入", null);
                                    LogUtil.i(TAG, "指静脉采集流程监听->获取手指状态已放置");
                                }
                            }
                        }
                        else {
                            LogUtil.i(TAG, "指静脉采集流程监听->获取手指信息失败,结果："+cur_ret_GrabFeature);
                        }
                    }
                }
                else {
                    LogUtil.i(TAG, "指静脉采集流程监听->获取手指状态失败");
                }
                LogUtil.i(TAG, "指静脉采集流程监听->结束");
            }
        }
    }

    private static final String ACTION_USB_PERMISSION = "com.template.USB_PERMISSION";//可自定义
    private BroadcastReceiver mUsbPermissionActionReceiver;
    private static UsbManager mUsbManager;

    private static int fileDescriptor = 0;
    public int tryGetPermission(Context mContext) {



        UsbDevice devicebyFingerVein = null;

        mUsbPermissionActionReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (ACTION_USB_PERMISSION.equals(action)) {
                    context.unregisterReceiver(this);//解注册
                    synchronized (this) {
                        UsbDevice usbDevice = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                        if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                            if (null != usbDevice) {
                                Log.e(TAG, "USB静脉指设备->广播已获取权限");

                                UsbManager mUsbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
                                // 打开USB，获取FileDescriptor
                                UsbDeviceConnection connection = mUsbManager.openDevice(usbDevice);
                                if (connection != null) {
                                    fileDescriptor = connection.getFileDescriptor();
                                    LogUtil.e(TAG, "USB静脉指设备->打开成功");
                                } else {
                                    LogUtil.e(TAG, "USB静脉指设备->打开失败");
                                }
                            }
                        } else {
                            Log.e(TAG, "USB静脉指设备->广播已拒绝访问");
                        }
                    }
                }
            }
        };


        UsbManager mUsbManager = (UsbManager) mContext.getSystemService(Context.USB_SERVICE);

        HashMap<String, UsbDevice> devices = mUsbManager.getDeviceList();

        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);

        if (mUsbPermissionActionReceiver != null) {
            mContext.registerReceiver(mUsbPermissionActionReceiver, filter);
        }
        PendingIntent mPermissionIntent = PendingIntent.getBroadcast(mContext, 0, new Intent(ACTION_USB_PERMISSION), 0);

        if (devices != null) {
            for (final UsbDevice usbDevice : devices.values()) {
                //静脉指设备
                if (((usbDevice.getVendorId() == 0x0481 && usbDevice.getProductId() == 0x5641) || (usbDevice.getVendorId() == 0x064B && usbDevice.getProductId() == 0x7823))) {
                    devicebyFingerVein = usbDevice;
                    break;
                }
            }
        }

        if (devicebyFingerVein == null) {
            LogUtil.e(TAG, "USB静脉指设备->未找到");
        } else {
            LogUtil.e(TAG, "USB静脉指设备->已找到");
            if (mUsbManager.hasPermission(devicebyFingerVein)) {
                LogUtil.e(TAG, "USB静脉指设备->已获取过权限");
                UsbDeviceConnection connection = mUsbManager.openDevice(devicebyFingerVein);
                if (connection != null) {
                    fileDescriptor = connection.getFileDescriptor();
                    LogUtil.e(TAG, "USB静脉指设备->打开成功");
                } else {
                    LogUtil.e(TAG, "USB静脉指设备->打开失败");
                }
            } else {
                LogUtil.e(TAG, "USB静脉指设备->请求获取权限");
                mUsbManager.requestPermission(devicebyFingerVein, mPermissionIntent);
            }
        }

        return fileDescriptor;
    }
}
