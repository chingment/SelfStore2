package com.uplink.selfstore.deviceCtrl;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.view.Surface;

import com.serenegiant.common.BaseActivity;
import com.serenegiant.dialog.MessageDialogFragmentV4;
import com.serenegiant.usb.USBMonitor;
import com.serenegiant.usb.UVCCamera;
import com.serenegiant.usbcameracommon.AbstractUVCCameraHandler;
import com.serenegiant.usbcameracommon.UVCCameraHandler;
import com.serenegiant.utils.PermissionCheck;
import com.serenegiant.widget.CameraViewInterface;
import com.uplink.selfstore.ui.BaseFragmentActivity;
import com.uplink.selfstore.utils.LogUtil;

import java.util.List;

public class CameraCtrl {
    private static final String TAG = "CameraCtrl";
    private USBMonitor mUSBMonitor;
    private UVCCameraHandler mCameraHandler;

    private BaseFragmentActivity mContext;

    private UsbDevice mCameraByChuHuoKou;//出货口相机
    private UsbDevice mCameraByJiGui;//机柜相机

    public CameraCtrl(BaseFragmentActivity context,UVCCameraHandler cameraHandler) {
        mContext=context;
        mCameraHandler=cameraHandler;
        mUSBMonitor = new USBMonitor(context, mOnDeviceConnectListener);
        mUSBMonitor.unregister();
        mUSBMonitor.register();
    }

    public void registerUSB(){
        mUSBMonitor.register();
    }

    public void unRegisterUSB(){
        mUSBMonitor.unregister();
    }

    public void setCameraByChuHuoKou(int productId,int vendorId){
        mCameraByChuHuoKou=getUsbDevice(productId,vendorId);
    }

    public UsbDevice getCameraByChuHuoKou(){
      return   mCameraByChuHuoKou;
    }

    public void setCameraByJiGui(int productId,int vendorId){
        mCameraByJiGui=getUsbDevice(productId,vendorId);
    }

    public UsbDevice getCameraByJiGui() {
        return mCameraByJiGui;
    }

    private UsbDevice getUsbDevice(int productId, int vendorId){
        if(mUSBMonitor!=null) {
            List<UsbDevice> deviceMap = mUSBMonitor.getDeviceList();
            if (deviceMap != null) {
                for (UsbDevice usbDevice : deviceMap) {
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

    private boolean isUsbCamera(UsbDevice usbDevice) {
        return usbDevice != null && 239 == usbDevice.getDeviceClass() && 2 == usbDevice.getDeviceSubclass();
    }


    public void openCameraByChuHuoKou() {

       // 321,6257
       // 42694, 1137
        mCameraByChuHuoKou = getUsbDevice(37424,1443);
        if (mCameraByChuHuoKou != null) {

            mUSBMonitor.requestPermission(mCameraByChuHuoKou);
        }
    }

    public void close() {
        if(mCameraHandler!=null) {
            if (mCameraHandler.isOpened()) {
                mCameraHandler.close();
            }
        }
        if(mCameraHandler!=null) {
            if (mCameraHandler.isOpened()) {
                mCameraHandler.close();
            }
        }
    }

    public void captureStill(AbstractUVCCameraHandler.OnCaptureStillListener listener) {
        if(mCameraHandler!=null) {
           if(mCameraHandler.isOpened())
           {
               mCameraHandler.captureStill(listener);
           }
        }
    }

    public void startRecord() {
        if(mCameraHandler!=null) {
            if(mCameraHandler.isOpened())
            {
                if (!mCameraHandler.isRecording()) {
                    mCameraHandler.startRecording();
                }
            }
        }
    }

    public void stopRecord() {
        if(mCameraHandler!=null) {
            if(mCameraHandler.isOpened())
            {
                if (mCameraHandler.isRecording()) {
                    mCameraHandler.stopRecording();
                }
            }
        }
    }

    RecordByJiGuiThread recordByJiGuiThread=null;
    public void startRecordByJiGui(){
        recordByJiGuiThread=new RecordByJiGuiThread();
        recordByJiGuiThread.start();
    }

    public void stopRecordByJiGui(){
        if(recordByJiGuiThread!=null) {
            recordByJiGuiThread.stopRecord();
        }
    }

    public boolean isOpen(){
        if(mCameraHandler==null)
            return false;

        return  mCameraHandler.isOpened();
    }

    public void  openCameraByJiGui() {


        mCameraByJiGui = getUsbDevice(42694, 1137);
        if (mCameraByJiGui != null) {

            mUSBMonitor.requestPermission(mCameraByJiGui);
        }

    }

    public  void destroy(){
        if(mUSBMonitor!=null) {
            mUSBMonitor.destroy();
            mUSBMonitor = null;
        }

        if(mCameraHandler!=null) {
            mCameraHandler=null;
        }

        if(mCameraByChuHuoKou!=null) {
            mCameraByChuHuoKou=null;
        }

        if(mCameraByJiGui!=null) {
            mCameraByJiGui=null;
        }
    }

    private final USBMonitor.OnDeviceConnectListener mOnDeviceConnectListener = new USBMonitor.OnDeviceConnectListener() {
        @Override
        public void onAttach(final UsbDevice device) {
            LogUtil.i(TAG, "USB_DEVICE_ATTACHED->pid:" + device.getProductId() + ",vid:" + device.getVendorId() + ",class:" + device.getDeviceClass());
        }

        @Override
        public void onConnect(final UsbDevice device, final USBMonitor.UsbControlBlock ctrlBlock, final boolean createNew) {
            LogUtil.i(TAG, "USB_DEVICE_CONNECT->pid:" + device.getProductId() + ",vid:" + device.getVendorId() + ",class:" + device.getDeviceClass());


            if (getCameraByChuHuoKou() != null || getCameraByJiGui() != null) {
                if(mCameraHandler!=null&&ctrlBlock!=null) {
                    if (!mCameraHandler.isOpened()) {

                        mCameraHandler.open(ctrlBlock);
                        mOnConnectLister.onConnect();
                    }
                }
            }
        }

        @Override
        public void onDisconnect(final UsbDevice device, final USBMonitor.UsbControlBlock ctrlBlock) {
            LogUtil.i(TAG, "USB_DEVICE_DISCONNECT->pid:" + device.getProductId() + ",vid:" + device.getVendorId() + ",class:" + device.getDeviceClass());

            ctrlBlock.close();

            if(getCameraByChuHuoKou()!=null||getCameraByJiGui()!=null) {
                mContext.queueEvent(new Runnable() {
                    @Override
                    public void run() {
                        if (mCameraHandler != null) {
                            if(mCameraHandler.isOpened()) {
                                mCameraHandler.close();
                            }
                        }
                    }
                }, 0);
            }
        }

        @Override
        public void onDettach(final UsbDevice device) {
            LogUtil.i(TAG, "USB_DEVICE_DETACHED->pid:" + device.getProductId() + ",vid:" + device.getVendorId() + ",class:" + device.getDeviceClass());
        }

        @Override
        public void onCancel(final UsbDevice device) {
            LogUtil.i(TAG, "USB_DEVICE_CANCEL->pid:" + device.getProductId() + ",vid:" + device.getVendorId() + ",class:" + device.getDeviceClass());
        }
    };

    private OnConnectLister mOnConnectLister;
    public void  setOnConnectLister(OnConnectLister lister){
        mOnConnectLister=lister;
    }

    public  interface OnConnectLister{
        void onConnect();
    }

    private void setCameraParameter(UVCCamera camera) {
        try {
            //设置预览尺寸 根据设备自行设置
            camera.setPreviewSize(640,
                    480,
                    1,
                    15,
                    UVCCamera.FRAME_FORMAT_YUYV,
// UVCCamera.FRAME_FORMAT_MJPEG,//此格式设置15帧生效
                    0.4f);
        } catch (final IllegalArgumentException e) {
            return;
        }
    }

    private class RecordByJiGuiThread extends Thread {
        private boolean isStop = true;
        private boolean isOpen = false;
        private boolean isStartRecord = false;
        private boolean isStopRecord = false;
        private boolean isRecording = false;

        public void stopRecord() {
            isStopRecord = true;
        }

        @Override
        public void run() {
            super.run();

            if (mCameraByJiGui == null) {
                LogUtil.i(TAG, "摄像头监控流程->机柜摄像头找不到");
                return;
            }

            LogUtil.i(TAG, "摄像头监控流程->机柜摄像头已找到");

            isStop = false;

            while (!isStop) {

                if (!isOpen) {

                    openCameraByChuHuoKou();

                    long nStart = System.currentTimeMillis();
                    long nEnd = System.currentTimeMillis();
                    boolean bTryAgain = false;
                    for (; (nEnd - nStart <= (long) 60 * 1000 || bTryAgain); nEnd = System.currentTimeMillis()) {

                        if (mCameraHandler.isOpened()) {
                            isOpen = true;
                            break;
                        }
                    }

                    if (isOpen) {

                        LogUtil.i(TAG, "摄像头监控流程->机柜摄像头已打开");
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        mCameraHandler.startRecording();

                        isStartRecord = true;

                        LogUtil.i(TAG, "摄像头监控流程->机柜摄像头开始录制");

                    } else {
                        LogUtil.i(TAG, "摄像头监控流程->机柜摄像头未打开");
                    }

                } else {
                    if (mCameraHandler.isRecording()) {
                        //LogUtil.i(TAG, "摄像头监控流程->机柜摄像头正在录制");
                        if (isStopRecord) {
                            mCameraHandler.stopRecording();
                            LogUtil.i(TAG, "摄像头监控流程->机柜摄像头停止录制");
                            isStop = true;
                        }
                    }
                }

            }
        }
    }
}
