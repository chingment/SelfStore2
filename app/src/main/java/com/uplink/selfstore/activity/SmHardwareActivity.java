package com.uplink.selfstore.activity;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.graphics.YuvImage;
import android.location.Location;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.widget.Button;

import com.serenegiant.encoder.MediaMuxerWrapper;
import com.serenegiant.usb.USBMonitor;
import com.serenegiant.usb.UVCCamera;
import com.serenegiant.usbcameracommon.AbstractUVCCameraHandler;
import com.serenegiant.usbcameracommon.UVCCameraHandler;
import com.serenegiant.widget.CameraViewInterface;
import com.uplink.selfstore.R;
import com.uplink.selfstore.deviceCtrl.CameraCtrl;
import com.uplink.selfstore.own.Config;
import com.uplink.selfstore.ui.swipebacklayout.SwipeBackActivity;
import com.uplink.selfstore.utils.BitmapUtil;
import com.uplink.selfstore.utils.LocationUtil;
import com.uplink.selfstore.utils.LogUtil;
import com.uplink.selfstore.utils.NoDoubleClickUtil;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.UUID;

public class SmHardwareActivity extends SwipeBackActivity implements View.OnClickListener {

    private static final String TAG = "SmHardwareActivity";

    private CameraViewInterface mCameraView;

    private UVCCameraHandler mCameraHandler;
    private Button mCameraOpenByChuHuoKou;
    private Button mCameraOpenByJiGui;
    private Button mCameraClose;
    private Button mCameraCaptureStill;
    private Button mCameraRecord;
    private Button mCameraTest;

    private  SurfaceTexture st;

    private CameraCtrl cameraCtrl;

  //  private int mCameraFrameWidth = 320;
  //  private int mCameraFrameheight = 240;

    private int mCameraFrameWidth = 640;
    private int mCameraFrameheight = 480;

   // private int mCameraFrameWidth=1280;
   // private int mCameraFrameheight = 720;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_smhardware);

        setNavTtile(this.getResources().getString(R.string.aty_smhardware_navtitle));
        setNavBackVisible(true);
        setNavBtnVisible(true);


        mCameraView = (CameraViewInterface) findViewById(R.id.cameraView);

        mCameraView.setAspectRatio(mCameraFrameWidth / mCameraFrameheight);

        mCameraHandler = UVCCameraHandler.createHandler(this, mCameraView, mCameraFrameWidth , mCameraFrameheight, 0.3f);


        mCameraOpenByChuHuoKou= (Button) findViewById(R.id.cameraOpenByChuHuoKou);
        mCameraOpenByChuHuoKou.setOnClickListener(this);
        mCameraOpenByJiGui= (Button) findViewById(R.id.cameraOpenByJiGui);
        mCameraOpenByJiGui.setOnClickListener(this);
        mCameraClose= (Button) findViewById(R.id.cameraClose);
        mCameraClose.setOnClickListener(this);
        mCameraCaptureStill= (Button) findViewById(R.id.cameraCaptureStill);
        mCameraCaptureStill.setOnClickListener(this);
        mCameraRecord= (Button) findViewById(R.id.cameraRecord);
        mCameraRecord.setOnClickListener(this);

        if(Config.DEBUG) {
            mCameraTest = (Button) findViewById(R.id.cameraTest);
            mCameraTest.setVisibility(View.VISIBLE);
            mCameraTest.setOnClickListener(this);
        }

        cameraCtrl=new CameraCtrl(SmHardwareActivity.this,mCameraHandler);
        cameraCtrl.setOnConnectLister(new CameraCtrl.OnConnectLister(){

            @Override
            public void onConnect() {
                if (mCameraHandler == null) {
                    showToast("mCameraHandler 为空");
                } else {
                    if(mCameraView!=null) {
                        st = mCameraView.getSurfaceTexture();
                        if (st == null) {
                            showToast("st 为空");
                        } else {
                            mCameraHandler.startPreview(new Surface(st));

                        }
                    }
                }
            }
        } );

        //cameraCtrl.setCameraByJiGui(37424,1443);
        //cameraCtrl.setCameraByChuHuoKou(42694,1137);
    }

    private class MyThread extends Thread {

        int i=0;
        @Override
        public void run() {
            super.run();

            while (i<100) {

//                cameraCtrl.openCameraByJiGui();
//
//                try {
//                    Thread.sleep(3000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
////
////                cameraCtrl.startRecord();
////
////                try {
////                    Thread.sleep(20*1000);
////                } catch (InterruptedException e) {
////                    e.printStackTrace();
////                }
////
////                cameraCtrl.stopRecord();;
////
////                try {
////                    Thread.sleep(5*1000);
////                } catch (InterruptedException e) {
////                    e.printStackTrace();
////                }
//
//                cameraCtrl.captureStill(new AbstractUVCCameraHandler.OnCaptureStillListener() {
//                    @Override
//                    public void onResult(final byte[] data) {
//                        //Bitmap bitmap=  mCameraView.captureStillImage();
//                        if(data!=null) {
//                            showToast("拍照成功");
//                            String  uniqueID = UUID.randomUUID().toString();
//                            saveCaptureStill(data,"SelfStore",uniqueID);
//                        }
//                    }
//                });
//
//
//                try {
//                    Thread.sleep(2000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//
//                cameraCtrl.close();
//
//
//                try {
//                    Thread.sleep(2000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//


//                cameraCtrl.openCameraByChuHuoKou();
//                Log.e(TAG,"出货口->开启摄像头");
//
//                try {
//                    Thread.sleep(1000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//
//                Log.e(TAG,"出货口->摄像头成功");
//
//                Log.e(TAG,"出货口->开启拍照");
//                cameraCtrl.captureStill(new AbstractUVCCameraHandler.OnCaptureStillListener() {
//                    @Override
//                    public void onResult(final byte[] data) {
//                        //Bitmap bitmap=  mCameraView.captureStillImage();
//                        if(data!=null) {
//                            showToast("出货口->拍照成功");
//                            String  uniqueID = UUID.randomUUID().toString();
//                            saveCaptureStill(data,"SelfStore",i+"-"+uniqueID);
//                        }
//                    }
//                });
//                Log.e(TAG,"出货口->拍照结束");
             //   mCameraHandler.isCaptureStill
//
//
//                try {
//                    Thread.sleep(1000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }

//                Log.e(TAG,"出货口->开始关闭");
//                try {
//                    Thread.sleep(1000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//
//
//                cameraCtrl.close();
//
//                Log.e(TAG,"出货口->关闭结束");
//
//                if(mCameraHandler.isPreviewing()){
//                    Log.e(TAG,"拍照-》正在拍照，延迟关闭");
//                    try {
//                        Thread.sleep(5000);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//
//                    cameraCtrl.close();
//                }
//                else {
//                    Log.e(TAG,"拍照-》结束关闭");
//                    cameraCtrl.close();
//                }


//                try {
//                    Thread.sleep(1000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//
//                Log.e(TAG,"机柜->开启摄像头");

                cameraCtrl.openCameraByJiGui();


                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                cameraCtrl.startRecord();

                Log.e(TAG,"机柜->开始录制");

                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                cameraCtrl.stopRecord();

                Log.e(TAG,"机柜->录制结束");

//                cameraCtrl.captureStill(new AbstractUVCCameraHandler.OnCaptureStillListener() {
//                    @Override
//                    public void onResult(final byte[] data) {
//                        //Bitmap bitmap=  mCameraView.captureStillImage();
//                        if(data!=null) {
//                            showToast("拍照成功");
//                            String  uniqueID = UUID.randomUUID().toString();
//                            saveCaptureStill(data,"SelfStore",uniqueID);
//                        }
//                    }
//                });

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                cameraCtrl.close();

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                i++;

                LogUtil.e(TAG,"第"+(i+1)+"次完成");
            }
        }
    }
    @Override
    protected void onStart() {
        super.onStart();
    }


    @Override
    protected void onDestroy() {
        if(cameraCtrl!=null) {
            cameraCtrl.destroy();
        }


        if(mCameraHandler!=null) {
            //mCameraHandler.close();
            mCameraHandler=null;
        }

        if(mCameraView!=null) {
            //mCameraView.onPause();
            mCameraView=null;
        }

        if(st!=null){
            st=null;
        }
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
                    cameraCtrl.openCameraByChuHuoKou();
                    break;
                case R.id.cameraOpenByJiGui:
                    cameraCtrl.openCameraByJiGui();
                    break;
                case R.id.cameraCaptureStill:
                    if(!mCameraHandler.isOpened()) {
                        showToast("请打开相机");
                        return;
                    }
                    cameraCtrl.captureStill(new AbstractUVCCameraHandler.OnCaptureStillListener() {
                        @Override
                        public void onResult(final byte[] data) {
                            //Bitmap bitmap=  mCameraView.captureStillImage();
                            if(data!=null) {
                                showToast("拍照成功");
                                String  uniqueID = UUID.randomUUID().toString();
                                saveCaptureStill(data,"SelfStore",uniqueID);
                            }
                        }
                    });
                    break;
                case R.id.cameraRecord:
                    if(!mCameraHandler.isOpened()) {
                        showToast("请打开相机");
                        return;
                    }
                    if(!mCameraHandler.isRecording()) {
                        cameraCtrl.startRecord();
                        mCameraRecord.setText("停止录像");
                    }
                    else {
                        cameraCtrl.stopRecord();
                        mCameraRecord.setText("开始录像");
                    }
                    break;
                case R.id.cameraClose:
                    cameraCtrl.close();
                    break;
                case R.id.cameraTest:
                    MyThread myThread=new MyThread();
                    myThread.start();

                    break;
            }
        }
    }

    public  void  saveCaptureStill(byte[] data,String saveDir,String uniqueId) {
        try {
            if (data == null)
                return;
            if (saveDir == null)
                return;
            if (uniqueId == null)
                return;

            YuvImage yuvImage = new YuvImage(data, ImageFormat.NV21, mCameraFrameWidth, mCameraFrameheight, null);
            ByteArrayOutputStream bos = new ByteArrayOutputStream(data.length);
            yuvImage.compressToJpeg(new Rect(0, 0, mCameraFrameWidth, mCameraFrameheight), 100, bos);
            byte[] buffer = bos.toByteArray();

            Bitmap bitmap = BitmapFactory.decodeByteArray(buffer, 0, buffer.length);

            String mSaveDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + "/" + saveDir;

            File pathFile = new File(mSaveDir);
            if (!pathFile.exists()) {
                pathFile.mkdirs();
            }

            String filePath = mSaveDir + "/" + uniqueId + ".jpg";
            File outputFile = new File(filePath);
            final BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(outputFile));
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);
            os.flush();
            os.close();

        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }

    }
}
