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

import com.uplink.selfstore.http.HttpClient;
import com.uplink.selfstore.ui.CameraWindow;
import com.uplink.selfstore.utils.LogUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CameraSnapService extends Service {
    private static final String TAG = "CameraSnapService";
    private Camera mCamera0;
    private boolean mCamera0IsRunning; // 是否已在监控拍照
    private String mCamera0ImgId = "";

    @Override
    public void onCreate() {
        LogUtil.d(TAG, "onCreate...");

        CommandReceiver cmdReceiver = new CommandReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.cameraSnapService");
        registerReceiver(cmdReceiver, filter);


        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_NOT_STICKY;
    }

    private void autoTakePic0(SurfaceView preview,String imgId) {
        LogUtil.d(TAG, "autoTakePic...");

        try {

            if (!mCamera0IsRunning) {
                mCamera0IsRunning = true;
                mCamera0ImgId= imgId;
                mCamera0 = Camera.open(0);
                if (mCamera0 == null) {
                    LogUtil.w(TAG, "getFacingFrontCamera return null");
                    camera0Release();
                    return;
                }
                mCamera0.setPreviewDisplay(preview.getHolder());
                mCamera0.startPreview();// 开始预览
                // 防止某些手机拍摄的照片亮度不够
                Thread.sleep(5000);
                mCamera0.takePicture(null, null, new Camera0Callback());
            } else {
                LogUtil.w(TAG, "Camera running");
            }
        } catch (Exception e) {
            e.printStackTrace();
            camera0Release();
        }
    }

    private final class Camera0Callback implements Camera.PictureCallback {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            LogUtil.d(TAG, "onPictureTaken...");
            try {
                Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                String filePath = getSaveSdCardPath() + "/" + mCamera0ImgId + ".jpg";
                File file = new File(filePath);
                FileOutputStream outputStream = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 50, outputStream);
                outputStream.close();
                camera.stopPreview();

                List<String> filePaths = new ArrayList<>();
                filePaths.add(filePath);
                Map<String, String> params = new HashMap<>();
                params.put("imgId", mCamera0ImgId);
                HttpClient.postFile("http://upload.17fanju.com/api/upload", params, filePaths, null);

                Log.e(TAG, "拍照结束");
            } catch (Exception e) {
                Log.e(TAG, e.toString());
            }

            camera0Release();
        }
    }

    public String getSaveSdCardPath() {
        boolean sdCardExist = Environment.getExternalStorageState()
                .equals(android.os.Environment.MEDIA_MOUNTED);


        if (sdCardExist) {
            String mSaveDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + "/SelfStore";
           // String path = Environment.getExternalStorageDirectory().toString() + "/SelfStoreImages";
            File dir = new File(mSaveDir);
            if (!dir.exists()) {
                dir.mkdir();
            }

            return mSaveDir;
        }

        return null;
    }

    private void camera0Release() {

        mCamera0ImgId = "";
        mCamera0IsRunning = false;
        if (mCamera0 != null) {
            LogUtil.d(TAG, "releaseCamera...");
            mCamera0.stopPreview();
            mCamera0.release();
            mCamera0 = null;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LogUtil.d(TAG, "onDestroy...");
        camera0Release();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private class CommandReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, final Intent intent) {
            LogUtil.i(TAG, "onReceive");
            int cameraId = intent.getIntExtra("cameraId", -1);
            String imgId = intent.getStringExtra("imgId");
            if (cameraId == 0 && imgId != null) {
                SurfaceView preview = CameraWindow.getDummyCameraView();
                autoTakePic0(preview, imgId);
            }
        }
    }
}
