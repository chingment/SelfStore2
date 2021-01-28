package com.uplink.selfstore.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.os.Build;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;

import com.uplink.selfstore.BuildConfig;
import com.uplink.selfstore.http.HttpClient;
import com.uplink.selfstore.own.AppLogcatManager;
import com.uplink.selfstore.own.Config;
import com.uplink.selfstore.own.OwnFileUtil;
import com.uplink.selfstore.utils.LogUtil;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 隐藏的全局窗口，用于后台拍照
 *
 * @author WuRS
 */
public class CameraWindow {

    private static final String TAG = "CameraWindow";

    private static WindowManager windowManager;

    private static Context applicationContext;

    private static Camera cameraJg;
    private static SurfaceView cameraViewByJg;
    private static SurfaceHolder cameraHolderByJg;

    private static Camera cameraChk;
    private static SurfaceView cameraViewByChk;
    private static SurfaceHolder cameraHolderyChk;
    /**
     * 显示全局窗口
     *
     * @param context
     */
    public static void show(Context context) {
        try {


            if (applicationContext == null) {
                applicationContext = context.getApplicationContext();
                windowManager = (WindowManager) applicationContext
                        .getSystemService(Context.WINDOW_SERVICE);


                cameraViewByJg = new SurfaceView(applicationContext);
                cameraHolderByJg = cameraViewByJg.getHolder();
                LayoutParams params = new LayoutParams();
                params.width = 1;
                params.height = 1;
                params.alpha = 0;
                params.type = LayoutParams.TYPE_SYSTEM_ALERT;
                // 屏蔽点击事件
                params.flags = LayoutParams.FLAG_NOT_TOUCH_MODAL
                        | LayoutParams.FLAG_NOT_FOCUSABLE
                        | LayoutParams.FLAG_NOT_TOUCHABLE;

                windowManager.addView(cameraViewByJg, params);


                cameraViewByChk = new SurfaceView(applicationContext);
                cameraHolderyChk = cameraViewByChk.getHolder();

                windowManager.addView(cameraViewByChk, params);

                LogUtil.d(TAG, TAG + " showing");
            }
        }
        catch (Exception ex){

        }
    }


    /**
     * 隐藏窗口
     */
    public static void dismiss() {
        try {
            if (windowManager != null && cameraViewByJg != null) {
                windowManager.removeView(cameraViewByJg);
            }

            if (windowManager != null && cameraViewByChk != null) {
                windowManager.removeView(cameraViewByChk);
            }

            LogUtil.d(TAG, TAG + " dismissed");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void openCameraByJg(){
        try {
            cameraJg = Camera.open(1);
            cameraJg.setPreviewDisplay(cameraHolderByJg);
            cameraJg.startPreview();
        }
        catch (Exception ex) {
            LogUtil.e(TAG,"失败->openCameraByJg");
            LogUtil.e(TAG,ex);
            AppLogcatManager.saveLogcat2Server("logcat -d -s symvdio CameraWindow ","camera");
            cameraJg = null;
        }
    }

    public static void releaseCameraByJg(){
        try {

            if (cameraJg != null) {
                cameraJg.stopPreview();
                cameraJg.release();
                cameraJg = null;
            }
        }
        catch (Exception ex){
            cameraJg=null;
        }
    }

    public static boolean  cameraIsRunningByJg() {

        if (cameraJg == null)
            return false;

        return true;
    }

    public static void openCameraByChk(){
        try {
            cameraChk = Camera.open(2);
            cameraChk.setPreviewDisplay(cameraHolderyChk);
            cameraChk.startPreview();
        }
        catch (Exception ex) {
            ex.printStackTrace();
            cameraChk = null;
            LogUtil.e(TAG, "失败->openCameraByChk");
            LogUtil.e(TAG, ex);
            AppLogcatManager.saveLogcat2Server("logcat -d -s symvdio CameraWindow ","camera");
        }
    }

    public static boolean  cameraIsRunningByChk() {

        if (cameraChk == null)
            return false;

        return true;
    }

    public static void releaseCameraByChk(){
        try {

            if (cameraChk!= null) {
                cameraChk.stopPreview();
                cameraChk.release();
                cameraChk = null;
            }
        }
        catch (Exception ex){
            cameraChk=null;
        }
    }



    public static void takeCameraPicByJg(String imgId){
        try {
            if(cameraJg!=null) {
                cameraJg.takePicture(null, null, new TakePicCallback(imgId));
            }

        }
        catch (Exception ex){
            ex.printStackTrace();
            LogUtil.e(TAG,"失败->takeCameraPicByJg");
            LogUtil.e(TAG, ex);
            AppLogcatManager.saveLogcat2Server("logcat -d -s symvdio CameraWindow ","camera");
        }
    }



    public static void takeCameraPicByChk(String imgId){
        try {
            if(cameraChk!=null) {
                cameraChk.takePicture(null, null, new TakePicCallback(imgId));
            }
        }
        catch (Exception ex){
            ex.printStackTrace();
            LogUtil.e(TAG,"失败->takeCameraPicByChk");
            LogUtil.e(TAG, ex);
            AppLogcatManager.saveLogcat2Server("logcat -d -s symvdio CameraWindow ","camera");
        }
    }

    public static class TakePicCallback implements Camera.PictureCallback {

        private String imgId;
        public  TakePicCallback(String imgId){
            this.imgId=imgId;
        }

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {

            try {
                //保存在本地

                String mSaveDir = OwnFileUtil.getPicSaveDir();

                File pathFile = new File(mSaveDir);
                if (!pathFile.exists()) {
                    pathFile.mkdirs();
                }

             //   Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                Bitmap bitmap = byteToBitmap(data);
                if(bitmap==null)
                    return;
                String filePath = mSaveDir + "/" + imgId + ".jpg";
                File file = new File(filePath);
                FileOutputStream outputStream = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 50, outputStream);
                outputStream.close();


                Map<String, String> params = new HashMap<>();
                params.put("fileName", imgId);
                params.put("folder", "pickup");

                Map<String, String> filePaths = new HashMap<>();
                filePaths.put("file", filePath);

                HttpClient.postFile(BuildConfig.APPKEY, BuildConfig.APPSECRET, Config.URL.uploadfile, params, filePaths, null);

                LogUtil.e(TAG, "拍照结束");
            } catch (Exception ex) {
                ex.printStackTrace();
                LogUtil.e(TAG,"失败->TakePicCallback");
                LogUtil.e(TAG, ex);

                AppLogcatManager.saveLogcat2Server("logcat -d -s symvdio CameraWindow ","camera");
            }
        }


        public static Bitmap byteToBitmap(byte[] imgByte) {
            Bitmap bitmap = null;
            try {
                InputStream input = null;
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 8;
                input = new ByteArrayInputStream(imgByte);
                SoftReference softRef = new SoftReference(BitmapFactory.decodeStream(
                        input, null, options));
                bitmap = (Bitmap) softRef.get();
                if (imgByte != null) {
                    imgByte = null;
                }

                if (input != null)
                    input.close();

            } catch (IOException ex) {
                LogUtil.e(TAG,"失败->byteToBitmap");
                AppLogcatManager.saveLogcat2Server("logcat -d -s symvdio CameraWindow ","camera");
                // TODO Auto-generated catch block
                ex.printStackTrace();
            }

            return bitmap;
        }

    }
}