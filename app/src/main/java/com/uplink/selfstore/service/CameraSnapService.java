package com.uplink.selfstore.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

import com.uplink.selfstore.ui.CameraWindow;
import com.uplink.selfstore.utils.LogUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;

public class CameraSnapService extends Service implements Camera.PictureCallback {
    private static final String TAG = "CameraSnapService";
    private Camera mCamera;
    private boolean mCameraIsRunning; // 是否已在监控拍照
    private CommandReceiver cmdReceiver;
    private String mUniqueId="";
    @Override
    public void onCreate() {
        LogUtil.d(TAG, "onCreate...");

        cmdReceiver = new CommandReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.cameraSnapService");
        registerReceiver(cmdReceiver, filter);


        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_NOT_STICKY;
    }

    private void autoTakePic(SurfaceView preview,int cameraId,String uniqueId) {
        LogUtil.d(TAG, "autoTakePic...");

        try {

            if(!mCameraIsRunning) {
                mCameraIsRunning = true;
                mUniqueId=uniqueId;
                mCamera = Camera.open(cameraId);
                if (mCamera == null) {
                    LogUtil.w(TAG, "getFacingFrontCamera return null");
                    releaseCamera();
                    return;
                }
                mCamera.setPreviewDisplay(preview.getHolder());
                mCamera.startPreview();// 开始预览
                // 防止某些手机拍摄的照片亮度不够
                Thread.sleep(500);
                mCamera.takePicture(null, null, this);
            }
            else
            {
                LogUtil.w(TAG, "Camera running");
            }
        } catch (Exception e) {
            e.printStackTrace();
            releaseCamera();
        }
    }

    @Override
    public void onPictureTaken(byte[] data, Camera camera) {
        LogUtil.d(TAG, "onPictureTaken...");
        try {
            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
            String path;
            path = getSDPath();
            if (path != null) {
                File dir = new File(path + "/cameratest");
                if (!dir.exists()) {
                    dir.mkdir();
                }
                path = dir.toString();
            } else {
                path = "/sdcard";
            }

            File file = new File(path, mUniqueId+".jpg");
            FileOutputStream outputStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            outputStream.close();
            camera.stopPreview();
            camera.startPreview();//����������֮�����Ԥ��

            Log.e(TAG, "拍照结束");
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }

        releaseCamera();
    }

    public String getSDPath() {
        File sdDir = null;
        boolean sdCardExist = Environment.getExternalStorageState()
                .equals(android.os.Environment.MEDIA_MOUNTED);
        if (sdCardExist) {
            sdDir = Environment.getExternalStorageDirectory();
            return sdDir.toString();
        }

        return null;
    }

    private void releaseCamera() {

        mUniqueId="";
        mCameraIsRunning=false;
        if (mCamera != null) {
            LogUtil.d(TAG, "releaseCamera...");
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LogUtil.d(TAG, "onDestroy...");
        releaseCamera();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private class CommandReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context,final Intent intent) {
            LogUtil.i(TAG, "onReceive");
            //if ( intent.getAction().equals("android.intent.action.cmdservice") ){
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {

                    int cameraId = intent.getIntExtra("cameraId", -1);
                    String uniqueId=intent.getStringExtra("uniqueId");

                    if(cameraId>=0) {
                        SurfaceView preview = CameraWindow.getDummyCameraView();
                        autoTakePic(preview,cameraId,uniqueId);
                    }
                }
            }, 1000);//3秒后执行Runnable中的run方法
        }
    }
}