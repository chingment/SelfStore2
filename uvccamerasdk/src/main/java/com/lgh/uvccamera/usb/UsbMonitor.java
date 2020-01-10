package com.lgh.uvccamera.usb;

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

import com.lgh.uvccamera.callback.ConnectCallback;
import com.lgh.uvccamera.config.CameraConfig;
import com.lgh.uvccamera.utils.LogUtil;
import com.serenegiant.usb.UVCCamera;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 描述：usb插拔监听、连接工具类
 * 作者：liugh
 * 日期：2018/9/17
 * 版本：v2.0.0
 */
public class UsbMonitor implements IMonitor {
    private static final String ACTION_USB_DEVICE_PERMISSION = "ACTION_USB_DEVICE_PERMISSION";
    private Context mContext;
    private UsbManager mUsbManager;
    private USBReceiver mUsbReceiver;
    private UsbController mUsbController;
    private Handler mMesssageHandler;
    private CameraConfig mConfig;

    public UsbMonitor(Context context, CameraConfig config) {
        this.mContext = context;
        this.mConfig = config;
        this.mUsbManager = (UsbManager) context.getSystemService(context.USB_SERVICE);
    }

    /**
     * 注册usb插拔监听广播
     */
    @Override
    public void registerReceiver() {
        LogUtil.i("registerReceiver");
        if (mUsbReceiver == null) {
            IntentFilter filter = new IntentFilter();
            filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
            filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
            filter.addAction(ACTION_USB_DEVICE_PERMISSION);
            mUsbReceiver = new USBReceiver();
            mContext.registerReceiver(mUsbReceiver, filter);
        }
    }

    /**
     * 注销usb插拔监听广播
     */
    @Override
    public void unregisterReceiver() {
        LogUtil.i("unregisterReceiver");
        if (mUsbReceiver != null) {
            mContext.unregisterReceiver(mUsbReceiver);
            mUsbReceiver = null;
        }
    }

    @Override
    public void checkDevice() {
        LogUtil.i("checkDevice");
       // UsbDevice usbDevice = getUsbCameraDevice();
//        if (isTargetDevice(usbDevice) && mConnectCallback != null) {
//            mConnectCallback.onAttached(usbDevice);
//        }
    }

    @Override
    public void requestPermission(UsbDevice usbDevice, UVCCamera mUVCCamera) {
        LogUtil.i("requestPermission-->" + usbDevice);
        if (mUsbManager.hasPermission(usbDevice)) {
            if(mMesssageHandler!=null) {
                Message msg = new Message();
                msg.what = 1;
                Bundle data = new Bundle();
                data.putInt("status", 1);
                data.putString("message", "已经授权");
                msg.setData(data);
                mMesssageHandler.sendMessage(msg);
            }

            mUsbController = new UsbController(mUsbManager, usbDevice);
            mUsbController.open();

            mUVCCamera.open(mUsbController);

            UsbDeviceConnection  usbDeviceConnection=mUsbManager.openDevice(usbDevice);
            if(usbDeviceConnection!=null){
                Message msg = new Message();
                msg.what = 2;
                Bundle data = new Bundle();
                data.putInt("status", 2);
                data.putString("message", "连接成功");
                msg.setData(data);
                mMesssageHandler.sendMessage(msg);
            }
           // mMesssageHandler
           // if (mConnectCallback != null) {
              //  mConnectCallback.onGranted(usbDevice, true);
           // }
        } else {
            PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, 0, new Intent(ACTION_USB_DEVICE_PERMISSION), 0);
            mUsbManager.requestPermission(usbDevice, pendingIntent);
        }
    }

    @Override
    public void connectDevice(UsbDevice usbDevice) {
        LogUtil.i("connectDevice-->" + usbDevice);
        mUsbController = new UsbController(mUsbManager, usbDevice);

//        if (mUsbController.open() != null && mConnectCallback != null) {
//            mConnectCallback.onConnected(usbDevice);
//        }
    }

    @Override
    public void closeDevice() {
        LogUtil.i("closeDevice");
        if (mUsbController != null) {
            mUsbController.close();
            mUsbController = null;
        }
    }

    @Override
    public UsbController getUsbController() {
        return mUsbController;
    }

    @Override
    public UsbDeviceConnection getConnection() {
        if (mUsbController != null) {
            return mUsbController.getConnection();
        }
        return null;
    }

    public void setMesssageHandler(Handler messsageHandler ) {
        this.mMesssageHandler = messsageHandler;
    }

    /**
     * 判断某usb设备是否摄像头，usb摄像头的大小类是239-2
     *
     * @param usbDevice
     * @return
     */
    public boolean isUsbCamera(UsbDevice usbDevice) {
        return usbDevice != null && 239 == usbDevice.getDeviceClass() && 2 == usbDevice.getDeviceSubclass();
    }


    public UsbDevice getUsbDevice(int productId, int vendorId){
        if(mUsbManager!=null) {
            HashMap<String, UsbDevice> deviceMap = mUsbManager.getDeviceList();
            if (deviceMap != null) {
                for (UsbDevice usbDevice : deviceMap.values()) {
                    if (isUsbCamera(usbDevice)) {
                        if (usbDevice.getVendorId() == vendorId && usbDevice.getProductId() == productId) {
                            return usbDevice;
                        }
                    }
                }
            }
        }
        return  null;
    }


    /**
     * usb插拔广播监听类
     */
    private class USBReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            UsbDevice usbDevice = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
            LogUtil.i("usbDevice-->" + usbDevice);
            if (mMesssageHandler == null) {
                return;
            }

            switch (intent.getAction()) {
                case UsbManager.ACTION_USB_DEVICE_ATTACHED:
                    LogUtil.i("onAttached");
                   // mConnectCallback.onAttached(usbDevice);
                    break;

                case ACTION_USB_DEVICE_PERMISSION:
                    boolean granted = intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false);
                   // mConnectCallback.onGranted(usbDevice, granted);
                    LogUtil.i("onGranted-->" + granted);
                    break;

                case UsbManager.ACTION_USB_DEVICE_DETACHED:
                    LogUtil.i("onDetached");
                  //  mConnectCallback.onDetached(usbDevice);
                    break;

                default:
                    break;
            }
        }
    }

}
