package com.uplink.selfstore.deviceCtrl;

import android.app.Activity;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.usb.UsbDevice;
import android.view.Surface;

import com.serenegiant.common.BaseActivity;
import com.serenegiant.usb.USBMonitor;
import com.serenegiant.usb.UVCCamera;
import com.serenegiant.usbcameracommon.AbstractUVCCameraHandler;
import com.serenegiant.usbcameracommon.UVCCameraHandler;
import com.serenegiant.widget.CameraViewInterface;
import com.uplink.selfstore.ui.BaseFragmentActivity;
import com.uplink.selfstore.utils.LogUtil;

import java.util.List;

public class CameraCtrl {
    private static final String TAG = "CameraCtrl";
    private USBMonitor mUSBMonitor;
    private BaseFragmentActivity mContext;

    private UsbDevice mCameraByChuHuoKou;//出货口相机
    private UVCCameraHandler mCameraHandlerByChuHuoKou;

    private UsbDevice mCameraByJiGui;//机柜相机
    private UVCCameraHandler mCameraHandlerByJiGui;

    public CameraCtrl(BaseFragmentActivity context) {
        mContext=context;
        mUSBMonitor = new USBMonitor(context, mOnDeviceConnectListener);
        mUSBMonitor.register();
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
        List<UsbDevice> deviceMap = mUSBMonitor.getDeviceList();
        if (deviceMap != null) {
            for (UsbDevice usbDevice : deviceMap) {
                if (isUsbCamera(usbDevice)) {
                    if(productId==0) {
                        return usbDevice;
                    }
                    else if(vendorId==0){
                        return usbDevice;
                    }
                    else if(usbDevice.getVendorId()==vendorId&&usbDevice.getProductId()==productId)
                    {
                        return usbDevice;
                    }
                }
            }
        }
        return  null;
    }

    private boolean isUsbCamera(UsbDevice usbDevice) {
        return usbDevice != null && 239 == usbDevice.getDeviceClass() && 2 == usbDevice.getDeviceSubclass();
    }


    public void openCameraByChuHuoKou(UVCCameraHandler cameraHandlerByChuHuoKou) {

        try {
            if (mCameraByChuHuoKou != null) {
                mCameraHandlerByChuHuoKou=cameraHandlerByChuHuoKou;
                mUSBMonitor.requestPermission(mCameraByChuHuoKou);
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }


    }

    public void closeCameraByChuHuoKou() {

        try {
            if (mCameraByChuHuoKou != null) {
                if (mCameraHandlerByChuHuoKou != null) {
                    if (mCameraHandlerByChuHuoKou.isOpened()) {
                        mCameraHandlerByChuHuoKou.close();
                    }
                }
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    public void captureStillByChuHuoKou(AbstractUVCCameraHandler.OnCaptureStillListener listener) {
        if(mCameraByChuHuoKou!=null) {
           if(mCameraHandlerByChuHuoKou.isOpened())
           {
               mCameraHandlerByChuHuoKou.captureStill(listener);
           }
        }
    }

    public void startRecordByChuHuoKou() {
        if(mCameraByChuHuoKou!=null) {
            if(mCameraHandlerByChuHuoKou.isOpened())
            {
                if (!mCameraHandlerByChuHuoKou.isRecording()) {
                    mCameraHandlerByChuHuoKou.startRecording();
                }
            }
        }
    }

    public void stopRecordByChuHuoKou() {
        if(mCameraByChuHuoKou!=null) {
            if(mCameraHandlerByChuHuoKou.isOpened())
            {
                if (mCameraHandlerByChuHuoKou.isRecording()) {
                    mCameraHandlerByChuHuoKou.stopRecording();
                }
            }
        }
    }


    public boolean isOpenCameraByChuHuoKou(){
        if(mCameraByChuHuoKou==null)
            return false;

        if(mCameraHandlerByChuHuoKou==null)
            return false;

        return  mCameraHandlerByChuHuoKou.isOpened();
    }

    public void  openCameraByJiGui(UVCCameraHandler cmeraHandlerByJiGui) {
        try {
            if (mCameraByJiGui != null) {
                mCameraHandlerByJiGui=cmeraHandlerByJiGui;
                mUSBMonitor.requestPermission(mCameraByJiGui);
            }
        }
        catch (Exception ex){
            ex.printStackTrace();
        }
    }

    public void closeCameraByJiGui() {
        try {
            if (mCameraByJiGui != null) {
                if (mCameraHandlerByJiGui != null) {
                    if (mCameraHandlerByJiGui.isOpened()) {
                        mCameraHandlerByJiGui.close();
                    }
                }
            }
        }catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    public boolean isOpenCameraByJiGui(){


        if(mCameraByJiGui==null)
            return false;

        if(mCameraHandlerByJiGui==null)
            return false;

        return  mCameraHandlerByJiGui.isOpened();
    }

    public void captureStillByJiGui(AbstractUVCCameraHandler.OnCaptureStillListener listener) {
        if(mCameraByJiGui!=null) {
            if(mCameraHandlerByJiGui.isOpened())
            {
                mCameraHandlerByJiGui.captureStill(listener);
            }
        }
    }

    public void startRecordByJiGui() {
        if(mCameraByJiGui!=null) {
            if(mCameraHandlerByJiGui.isOpened())
            {
                if (!mCameraHandlerByJiGui.isRecording()) {
                    mCameraHandlerByJiGui.startRecording();
                }
            }
        }
    }

    public void stopRecordByJiGui() {
        if(mCameraByJiGui!=null) {
            if(mCameraHandlerByJiGui.isOpened())
            {
                if (mCameraHandlerByJiGui.isRecording()) {
                    mCameraHandlerByJiGui.stopRecording();
                }
            }
        }
    }

    public  void destroy(){
        if(mUSBMonitor!=null) {
            mUSBMonitor.destroy();
            mUSBMonitor = null;
        }

        if(mCameraHandlerByJiGui!=null) {
            mCameraHandlerByJiGui=null;
        }

        if(mCameraByChuHuoKou!=null) {
            mCameraByChuHuoKou=null;
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


            if(getCameraByChuHuoKou()!=null) {
                if (device.getVendorId() == getCameraByChuHuoKou().getVendorId()) {
                    if (!mCameraHandlerByChuHuoKou.isOpened()) {
                        mCameraHandlerByChuHuoKou.open(ctrlBlock);
                        mOnConnectLister.onConnectByChuHuoKou(mCameraHandlerByChuHuoKou);
                    }
                }
            }

            if(getCameraByJiGui()!=null) {
                if (device.getVendorId() == getCameraByJiGui().getVendorId()) {
                    if (!mCameraHandlerByJiGui.isOpened()) {
                        mCameraHandlerByJiGui.open(ctrlBlock);
                        mOnConnectLister.onConnectByJiGui(mCameraHandlerByJiGui);
                    }
                }
            }
        }

        @Override
        public void onDisconnect(final UsbDevice device, final USBMonitor.UsbControlBlock ctrlBlock) {
            LogUtil.i(TAG, "USB_DEVICE_DISCONNECT->pid:" + device.getProductId() + ",vid:" + device.getVendorId() + ",class:" + device.getDeviceClass());

            if(getCameraByChuHuoKou()!=null) {
                if (device.getVendorId() == getCameraByChuHuoKou().getVendorId()) {
                    mContext.queueEvent(new Runnable() {
                        @Override
                        public void run() {
                            mCameraHandlerByChuHuoKou.close();
                        }
                    }, 0);
                }
            }

            if(getCameraByJiGui()!=null) {
                if (device.getVendorId() == getCameraByJiGui().getVendorId()) {
                    mContext.queueEvent(new Runnable() {
                        @Override
                        public void run() {
                            mCameraHandlerByJiGui.close();
                        }
                    }, 0);
                }
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
        void onConnectByChuHuoKou(UVCCameraHandler mCameraHandlerByChuHuoKou);
        void onConnectByJiGui(UVCCameraHandler mCameraHandlerByJiGui);
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
}
