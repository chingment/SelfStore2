package com.uplink.selfstore.activity;

import android.graphics.SurfaceTexture;
import android.hardware.usb.UsbDevice;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Surface;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.serenegiant.common.BaseActivity;
import com.serenegiant.usb.CameraDialog;
import com.serenegiant.usb.USBMonitor;
import com.serenegiant.usb.UVCCamera;
import com.serenegiant.usbcameracommon.UVCCameraHandler;
import com.serenegiant.usbcameracommon.UvcCameraDataCallBack;
import com.serenegiant.widget.CameraViewInterface;
import com.serenegiant.widget.UVCCameraTextureView;
import com.uplink.selfstore.R;
import com.uplink.selfstore.utils.LogUtil;

import java.util.List;

public class TestCameraActivity extends BaseActivity{

    private static final String TAG = "TestCameraActivity";

    private static final float[] BANDWIDTH_FACTORS = {0.5f, 0.5f};

    private USBMonitor mUSBMonitor;

    private Button mOpenFirst;
    private Button mCloseFirst;
    private UVCCameraHandler mHandlerFirst;
    private CameraViewInterface mUVCCameraViewFirst;
    private ImageButton mCaptureButtonFirst;
    private Surface mFirstPreviewSurface;

    private Button mOpenSecond;
    private Button mCloseSecond;
    private UVCCameraHandler mHandlerSecond;
    private CameraViewInterface mUVCCameraViewSecond;
    private ImageButton mCaptureButtonSecond;
    private Surface mSecondPreviewSurface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_camera);

        resultFirstCamera();
        resultSecondCamera();

        mUSBMonitor = new USBMonitor(this, mOnDeviceConnectListener);

        LogUtil.e(TAG,"Create");
//        UsbDevice usbDevice= getUsbDevice(37424,1443);
//
//        mUSBMonitor.register();
//        mUSBMonitor.requestPermission(usbDevice);
//
//
//        UsbDevice usbDevice2= getUsbDevice(42694,1137);
//        mUSBMonitor.requestPermission(usbDevice2);

    }

    public  UsbDevice getUsbDevice(int productId,int vendorId){
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

    public boolean isUsbCamera(UsbDevice usbDevice) {
        return usbDevice != null && 239 == usbDevice.getDeviceClass() && 2 == usbDevice.getDeviceSubclass();
    }

    private void resultFirstCamera() {
        mUVCCameraViewFirst = (CameraViewInterface) findViewById(R.id.camera_view_first);
        mOpenFirst = (Button) findViewById(R.id.camera_open_first);
        mOpenFirst.setOnClickListener(mOnClickListener);
        mCloseFirst = (Button) findViewById(R.id.camera_close_first);
        mCloseFirst.setOnClickListener(mOnClickListener);
        //设置显示宽高
        mUVCCameraViewFirst.setAspectRatio(UVCCamera.DEFAULT_PREVIEW_WIDTH / (float) UVCCamera.DEFAULT_PREVIEW_HEIGHT);
        ((UVCCameraTextureView) mUVCCameraViewFirst).setOnClickListener(mOnClickListener);
        mCaptureButtonFirst = (ImageButton) findViewById(R.id.capture_button_first);
        mCaptureButtonFirst.setOnClickListener(mOnClickListener);

        mHandlerFirst = UVCCameraHandler.createHandler(this, mUVCCameraViewFirst
                , UVCCamera.DEFAULT_PREVIEW_WIDTH, UVCCamera.DEFAULT_PREVIEW_HEIGHT
                , BANDWIDTH_FACTORS[0], firstDataCallBack);
    }


    private void resultSecondCamera() {
        mUVCCameraViewSecond = (CameraViewInterface) findViewById(R.id.camera_view_second);
        mOpenSecond = (Button) findViewById(R.id.camera_open_second);
        mOpenSecond.setOnClickListener(mOnClickListener);
        mCloseSecond = (Button) findViewById(R.id.camera_close_second);
        mCloseSecond.setOnClickListener(mOnClickListener);
        mUVCCameraViewSecond.setAspectRatio(UVCCamera.DEFAULT_PREVIEW_WIDTH / (float) UVCCamera.DEFAULT_PREVIEW_HEIGHT);
        ((UVCCameraTextureView) mUVCCameraViewSecond).setOnClickListener(mOnClickListener);
        mCaptureButtonSecond = (ImageButton) findViewById(R.id.capture_button_second);
        mCaptureButtonSecond.setOnClickListener(mOnClickListener);

        mHandlerSecond = UVCCameraHandler.createHandler(this, mUVCCameraViewSecond, UVCCamera.DEFAULT_PREVIEW_WIDTH, UVCCamera.DEFAULT_PREVIEW_HEIGHT, BANDWIDTH_FACTORS[1]);
    }

    UvcCameraDataCallBack firstDataCallBack = new UvcCameraDataCallBack() {
        @Override
        public void getData(byte[] data) {

        }
    };

    private final View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(final View view) {
            switch (view.getId()) {
                case R.id.capture_button_first:
                    if (mHandlerFirst != null) {
                        if (mHandlerFirst.isOpened()) {
                            // if (checkPermissionWriteExternalStorage() && checkPermissionAudio()) {
                            if (!mHandlerFirst.isRecording()) {
                                mCaptureButtonFirst.setColorFilter(0xffff0000);    // turn red
                                mHandlerFirst.startRecording();
                            } else {
                                mCaptureButtonFirst.setColorFilter(0);    // return to default color
                                mHandlerFirst.stopRecording();
                            }
                            //}
                        }
                    }
                    break;
                case R.id.capture_button_second:
                    if (mHandlerSecond != null) {
                        if (mHandlerSecond.isOpened()) {
                            mHandlerSecond.captureStill();
                        }
                    }
                    break;
                case R.id.camera_open_first:
                    UsbDevice usbDevice= getUsbDevice(37424,1443);
                    mUSBMonitor.requestPermission(usbDevice);

                    break;
                case R.id.camera_close_first:
                    mHandlerFirst.close();
                    break;
                case R.id.camera_open_second:
                    UsbDevice usbDevice2= getUsbDevice(42694,1137);
                    mUSBMonitor.requestPermission(usbDevice2);
                    break;
                case R.id.camera_close_second:
                    mHandlerSecond.close();
                    break;

            }
        }
    };


    private final USBMonitor.OnDeviceConnectListener mOnDeviceConnectListener = new USBMonitor.OnDeviceConnectListener() {
        @Override
        public void onAttach(final UsbDevice device) {
            LogUtil.i(TAG, "USB_DEVICE_ATTACHED->pid:" + device.getProductId() + ",vid:" + device.getVendorId() + ",class:" + device.getDeviceClass());
        }

        @Override
        public void onConnect(final UsbDevice device, final USBMonitor.UsbControlBlock ctrlBlock, final boolean createNew) {

            LogUtil.i(TAG, "USB_DEVICE_CONNECT->pid:" + device.getProductId() + ",vid:" + device.getVendorId() + ",class:" + device.getDeviceClass());


            //设备连接成功
            try {
                Thread.sleep(1300);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
//
            if(device.getVendorId()==1443) {
                if (!mHandlerFirst.isOpened()) {
                    mHandlerFirst.open(ctrlBlock);
                    SurfaceTexture st = mUVCCameraViewFirst.getSurfaceTexture();
                    mHandlerFirst.startPreview(new Surface(st));
                }
            }
            else if(device.getVendorId()==1137) {
                if (!mHandlerSecond.isOpened()) {

                    mHandlerSecond.open(ctrlBlock);
                    SurfaceTexture st = mUVCCameraViewSecond.getSurfaceTexture();
                    mHandlerSecond.startPreview(new Surface(st));
                }
            }
        }

        @Override
        public void onDisconnect(final UsbDevice device, final USBMonitor.UsbControlBlock ctrlBlock) {

            LogUtil.i(TAG, "USB_DEVICE_DISCONNECT->pid:" + device.getProductId() + ",vid:" + device.getVendorId() + ",class:" + device.getDeviceClass());

            if ((mHandlerFirst != null) && !mHandlerFirst.isEqual(device)) {
                queueEvent(new Runnable() {
                    @Override
                    public void run() {
                        mHandlerFirst.close();
                        if (mFirstPreviewSurface != null) {
                            mFirstPreviewSurface.release();
                            mFirstPreviewSurface = null;
                        }
                    }
                }, 0);
            } else if ((mHandlerSecond != null) && !mHandlerSecond.isEqual(device)) {
                queueEvent(new Runnable() {
                    @Override
                    public void run() {
                        mHandlerSecond.close();
                        if (mSecondPreviewSurface != null) {
                            mSecondPreviewSurface.release();
                            mSecondPreviewSurface = null;
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

    @Override
    protected void onStart() {
        super.onStart();
        mUSBMonitor.register();

        if (mUVCCameraViewFirst != null)
            mUVCCameraViewFirst.onResume();

    }

    @Override
    protected void onStop() {
        mHandlerFirst.close();
        if (mUVCCameraViewFirst != null)
            mUVCCameraViewFirst.onPause();
        mCaptureButtonFirst.setVisibility(View.INVISIBLE);



        mUSBMonitor.unregister();//usb管理器解绑
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if (mHandlerFirst != null) {
            mHandlerFirst = null;
        }


        if (mUSBMonitor != null) {
            mUSBMonitor.destroy();
            mUSBMonitor = null;
        }

        mUVCCameraViewFirst = null;
        mCaptureButtonFirst = null;


        super.onDestroy();
    }
}