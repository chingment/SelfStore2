package com.uplink.selfstore.activity;

import android.graphics.SurfaceTexture;
import android.location.Location;
import android.os.Bundle;
import android.os.Environment;
import android.view.Surface;
import android.view.View;
import android.widget.Button;

import com.serenegiant.usb.USBMonitor;
import com.serenegiant.usb.UVCCamera;
import com.serenegiant.usbcameracommon.AbstractUVCCameraHandler;
import com.serenegiant.usbcameracommon.UVCCameraHandler;
import com.serenegiant.widget.CameraViewInterface;
import com.uplink.selfstore.R;
import com.uplink.selfstore.deviceCtrl.CameraCtrl;
import com.uplink.selfstore.ui.swipebacklayout.SwipeBackActivity;
import com.uplink.selfstore.utils.LocationUtil;
import com.uplink.selfstore.utils.LogUtil;
import com.uplink.selfstore.utils.NoDoubleClickUtil;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class SmHardwareActivity extends SwipeBackActivity implements View.OnClickListener {

    private static final String TAG = "SmHardwareActivity";
    private static final float[] BANDWIDTH_FACTORS = {0.1f, 0.2f};
    private UVCCameraHandler mCameraHandlerByChuHuoKou;
    private CameraViewInterface mCameraViewByChuHuoKou;
    private Button mCameraOpenByChuHuoKou;
    private Button mCameraCloseByChuHuoKou;
    private Button mCameraCaptureStillByChuHuoKou;
    private Button mCameraRecordByChuHuoKou;

    private UVCCameraHandler mCameraHandlerByJiGui;
    private CameraViewInterface mCameraViewByJiGui;
    private Button mCameraOpenByJiGui;
    private Button mCameraCloseByJiGui;
    private Button mCameraCaptureStillByJiGui;
    private Button mCameraRecordByJiGui;


    private CameraCtrl cameraCtrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_smhardware);

        String path1=Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)+"/SelfStore";

        String path= Environment.getExternalStorageDirectory().getAbsolutePath()+"/SelfStore/Images";

        File file=new File(path,"test.txt");

        try {

            File pathFile=new File(path);
            if(!pathFile.exists()) {
                pathFile.mkdirs();
            }
            //判断文件是否存在
            if (file.exists()){
                file.delete();
            }
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            //开始向文件中写内容
            FileWriter fileWriter=new FileWriter(file);
            fileWriter.write("testing,,,,");
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }


        setNavTtile(this.getResources().getString(R.string.aty_smhardware_navtitle));
        setNavBackVisible(true);
        setNavBtnVisible(true);

        mCameraViewByChuHuoKou = (CameraViewInterface) findViewById(R.id.cameraViewByChuHuoKou);
        mCameraViewByChuHuoKou.setAspectRatio(UVCCamera.DEFAULT_PREVIEW_WIDTH / (float) UVCCamera.DEFAULT_PREVIEW_HEIGHT);

        mCameraHandlerByChuHuoKou = UVCCameraHandler.createHandler(this, mCameraViewByChuHuoKou, UVCCamera.DEFAULT_PREVIEW_WIDTH, UVCCamera.DEFAULT_PREVIEW_HEIGHT, BANDWIDTH_FACTORS[0]);


        mCameraOpenByChuHuoKou= (Button) findViewById(R.id.cameraOpenByChuHuoKou);
        mCameraOpenByChuHuoKou.setOnClickListener(this);
        mCameraCloseByChuHuoKou= (Button) findViewById(R.id.cameraCloseByChuHuoKou);
        mCameraCloseByChuHuoKou.setOnClickListener(this);
        mCameraCaptureStillByChuHuoKou= (Button) findViewById(R.id.cameraCaptureStillByChuHuoKou);
        mCameraCaptureStillByChuHuoKou.setOnClickListener(this);
        mCameraRecordByChuHuoKou= (Button) findViewById(R.id.cameraRecordByChuHuoKou);
        mCameraRecordByChuHuoKou.setOnClickListener(this);

        mCameraViewByJiGui = (CameraViewInterface) findViewById(R.id.cameraViewByJiGui);

        mCameraViewByJiGui.setAspectRatio(UVCCamera.DEFAULT_PREVIEW_WIDTH / (float) UVCCamera.DEFAULT_PREVIEW_HEIGHT);
        mCameraHandlerByJiGui = UVCCameraHandler.createHandler(this, mCameraViewByJiGui, UVCCamera.DEFAULT_PREVIEW_WIDTH, UVCCamera.DEFAULT_PREVIEW_HEIGHT, BANDWIDTH_FACTORS[1]);


        mCameraOpenByJiGui= (Button) findViewById(R.id.cameraOpenByJiGui);
        mCameraOpenByJiGui.setOnClickListener(this);
        mCameraCloseByJiGui= (Button) findViewById(R.id.cameraCloseByJiGui);
        mCameraCloseByJiGui.setOnClickListener(this);
        mCameraCaptureStillByJiGui= (Button) findViewById(R.id.cameraCaptureStillByJiGui);
        mCameraCaptureStillByJiGui.setOnClickListener(this);
        mCameraRecordByJiGui= (Button) findViewById(R.id.cameraRecordByJiGui);
        mCameraRecordByJiGui.setOnClickListener(this);

        cameraCtrl=new CameraCtrl(SmHardwareActivity.this);

        cameraCtrl.setOnConnectLister(new CameraCtrl.OnConnectLister(){

            @Override
            public void onConnectByChuHuoKou(UVCCameraHandler mCameraHandlerByChuHuoKou) {

                if (mCameraHandlerByChuHuoKou == null) {
                    showToast("mCameraViewInterfaceByJiGui 为空");
                } else {
                    final SurfaceTexture st = mCameraViewByChuHuoKou.getSurfaceTexture();
                    if (st == null) {
                        showToast("st 为空");
                    } else {

                        mCameraHandlerByChuHuoKou.startPreview(new Surface(st));
                    }
                }
            }

            @Override
            public void onConnectByJiGui(UVCCameraHandler mCameraHandlerByJiGui) {
                if (mCameraHandlerByJiGui == null) {
                    showToast("mCameraViewInterfaceByJiGui 为空");
                } else {
                    final SurfaceTexture st2 = mCameraViewByJiGui.getSurfaceTexture();
                    if (st2 == null) {
                        showToast("st 为空");
                    } else {
                        new SurfaceTexture(0);
                        mCameraHandlerByJiGui.startPreview(new Surface(st2));
                    }
                }
            }
        } );
        cameraCtrl.setCameraByChuHuoKou(37424,1443);
        cameraCtrl.setCameraByJiGui(42694,1137);
//        MyThread myThread=new MyThread();
//        myThread.start();
    }

    private class MyThread extends Thread {

        @Override
        public void run() {

            //cameraCtrl.openCameraByChuHuoKou();

           try {
                Thread.sleep(1300);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

          //  cameraCtrl.openCameraByJiGui();

        }
    }


    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {

        super.onStop();
    }

    @Override
    protected void onDestroy() {
        cameraCtrl.destroy();


        mCameraViewByChuHuoKou=null;
        mCameraViewByJiGui=null;

        super.onDestroy();


    }

    @Override
    public void onClick(View v) {
        super.onClick(v);

        if (!NoDoubleClickUtil.isDoubleClick()) {
            switch (v.getId()) {
                case R.id.nav_back:
                    finish();
                    break;
                case R.id.cameraOpenByChuHuoKou:
                    if(cameraCtrl.getCameraByChuHuoKou()==null) {
                        showToast("找不到出口货摄像头设备");
                    }
                    mCameraOpenByChuHuoKou.setVisibility(View.INVISIBLE);
                    mCameraCloseByChuHuoKou.setVisibility(View.VISIBLE);
                    cameraCtrl.openCameraByChuHuoKou(mCameraHandlerByChuHuoKou);
                    break;
                case R.id.cameraCloseByChuHuoKou:
                    if(cameraCtrl.getCameraByChuHuoKou()==null) {
                        showToast("找不到出口货摄像头设备");
                    }
                    mCameraOpenByChuHuoKou.setVisibility(View.VISIBLE);
                    mCameraCloseByChuHuoKou.setVisibility(View.INVISIBLE);
                    cameraCtrl.closeCameraByChuHuoKou();
                    break;
                case R.id.cameraCaptureStillByChuHuoKou:
                    if(cameraCtrl.getCameraByChuHuoKou()==null) {
                        showToast("找不到出口货摄像头设备");
                    }
                    cameraCtrl.captureStillByChuHuoKou(new AbstractUVCCameraHandler.OnCaptureStillListener() {
                        @Override
                        public void onResult(final byte[] data) {
                            if(data!=null)
                                showToast("拍照成功");
                        }
                    });
                    break;
                case R.id.cameraRecordByChuHuoKou:
                    break;
                case R.id.cameraOpenByJiGui:
                    if(cameraCtrl.getCameraByJiGui()==null) {
                        showToast("找不到机柜摄像头设备");
                    }
                    mCameraOpenByJiGui.setVisibility(View.INVISIBLE);
                    mCameraCloseByJiGui.setVisibility(View.VISIBLE);
                    cameraCtrl.openCameraByJiGui(mCameraHandlerByJiGui);
                    break;
                case R.id.cameraCloseByJiGui:
                    if(cameraCtrl.getCameraByJiGui()==null) {
                        showToast("找不到机柜摄像头设备");
                    }
                    mCameraOpenByJiGui.setVisibility(View.VISIBLE);
                    mCameraCloseByJiGui.setVisibility(View.INVISIBLE);
                    cameraCtrl.closeCameraByJiGui();
                    break;
                case R.id.cameraCaptureStillByJiGui:
                    if(cameraCtrl.getCameraByJiGui()==null) {
                        showToast("找不到机柜摄像头设备");
                    }

                    cameraCtrl.captureStillByJiGui(new AbstractUVCCameraHandler.OnCaptureStillListener() {
                        @Override
                        public void onResult(final byte[] data) {
                            if(data!=null)
                                showToast("拍照成功");
                        }
                    });

                    break;
                case R.id.cameraRecordByJiGui:
                    break;

            }
        }

//        class myRunnable implements Runnable {
//            USBMonitor.UsbControlBlock usbControlBlock;
//
//            public myRunnable(USBMonitor.UsbControlBlock usbControlBlock) {
//                this.usbControlBlock = usbControlBlock;
//            }
//
//            @Override
//            public void run() {
//                final UVCCamera camera = new UVCCamera();
//                try {
//                    camera.open(usbControlBlock);
//                } catch (Exception e) {
//                    LogUtil.d(TAG, "开启相机错误！！！！" + camera.getDeviceName());
//                    return;
//                }
//
//                //根据不同相机接入name  或者根据pid vid 指定相机在那个view显示
//                if (usbControlBlock.getDeviceName().contains("usb/001/004")) {
//                    camera.setPreviewTexture(mCameraViewByJiGui.getSurfaceTexture());
//                } else if (usbControlBlock.getDeviceName().contains("usb/001/003")) {
//                    camera.setPreviewTexture(mTexture2.getSurfaceTexture());
//                } else if (usbControlBlock.getDeviceName().contains("usb/003/004")) {
//                    camera.setPreviewTexture(mTexture3.getSurfaceTexture());
//                }
//                camera.startPreview();
//            }
//        }
    }
}
