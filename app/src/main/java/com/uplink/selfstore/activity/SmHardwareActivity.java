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
    private static final float[] BANDWIDTH_FACTORS = {0.5f, 0.5f};

    private CameraViewInterface mCameraView;

    private UVCCameraHandler mCameraHandler;
    private Button mCameraOpenByChuHuoKou;
    private Button mCameraOpenByJiGui;
    private Button mCameraClose;
    private Button mCameraCaptureStill;
    private Button mCameraRecord;


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

        mCameraView = (CameraViewInterface) findViewById(R.id.cameraView);
        mCameraView.setAspectRatio(320 / 240);
        mCameraHandler = UVCCameraHandler.createHandler(this, mCameraView, 320, 240, BANDWIDTH_FACTORS[0]);


        mCameraOpenByChuHuoKou= (Button) findViewById(R.id.cameraOpenByChuHuoKou);
        mCameraOpenByChuHuoKou.setOnClickListener(this);
        mCameraOpenByJiGui= (Button) findViewById(R.id.cameraOpenByJiGui);
        mCameraOpenByJiGui.setOnClickListener(this);
        mCameraClose= (Button) findViewById(R.id.cameraClose);
        mCameraClose.setOnClickListener(this);
        mCameraCaptureStill= (Button) findViewById(R.id.cameraCaptureStill);
        mCameraCaptureStill.setOnClickListener(this);
        mCameraRecord= (Button) findViewById(R.id.cameraRecord);




        cameraCtrl=new CameraCtrl(SmHardwareActivity.this,mCameraHandler);

        cameraCtrl.setOnConnectLister(new CameraCtrl.OnConnectLister(){

            @Override
            public void onConnect(UVCCameraHandler mCameraHandler) {
                if (mCameraHandler == null) {
                    showToast("mCameraHandler 为空");
                } else {
                    final SurfaceTexture st = mCameraView.getSurfaceTexture();
                    if (st == null) {
                        showToast("st 为空");
                    } else {
                        mCameraHandler.startPreview(new Surface(st));
                    }
                }
            }
        } );
        cameraCtrl.setCameraByJiGui(37424,1443);
        cameraCtrl.setCameraByChuHuoKou(42694,1137);
//        MyThread myThread=new MyThread();
//        myThread.start();
    }

    private class MyThread extends Thread {

        @Override
        public void run() {

            while (true) {


//                cameraCtrl.openCameraByChuHuoKou(mCameraHandlerByChuHuoKou);
//                try {
//                    Thread.sleep(200);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//
//              //  mCameraViewByJiGui.onResume();
//                cameraCtrl.openCameraByJiGui(mCameraHandlerByJiGui);
//
//                try {
//                    Thread.sleep(3000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//
//
//                    //Log.i(TAG, "stopPreview: ");
//                    cameraCtrl.closeCameraByChuHuoKou();
//
//
////                try {
////                    Thread.sleep(1000);
////                } catch (InterruptedException e) {
////                    e.printStackTrace();
////                }
//
//              //      mCameraViewByJiGui.onPause();
//                    cameraCtrl.closeCameraByJiGui();

            }

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
        mCameraView=null;
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
                    cameraCtrl.close();
                    try {
                        Thread.sleep(500);
                    }
                    catch (Exception ex)
                    {
                        ex.printStackTrace();
                    }
                    cameraCtrl.openCameraByChuHuoKou();
                    break;
                case R.id.cameraOpenByJiGui:
                    if(cameraCtrl.getCameraByJiGui()==null) {
                        showToast("找不到机柜摄像头设备");
                    }
                    cameraCtrl.close();
                    try {
                        Thread.sleep(500);
                    }
                    catch (Exception ex)
                    {
                        ex.printStackTrace();
                    }
                    cameraCtrl.openCameraByJiGui();
                    break;
                case R.id.cameraCaptureStill:
                    cameraCtrl.captureStill(new AbstractUVCCameraHandler.OnCaptureStillListener() {
                        @Override
                        public void onResult(final byte[] data) {
                            if(data!=null)
                                showToast("拍照成功");
                        }
                    });
                    break;
                case R.id.cameraRecord:
                    break;
                case R.id.cameraClose:
                    cameraCtrl.close();
                    break;
            }
        }
    }
}
