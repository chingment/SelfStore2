package com.uplink.selfstore.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
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
import com.uplink.selfstore.utils.StringUtil;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * 隐藏的全局窗口，用于后台拍照
 *
 * @author WuRS
 */
public class CameraWindow {

    private static final String TAG = "CameraWindow";

    private static int inSampleSize=8;

    private static WindowManager windowManager;

    private static Context applicationContext;

    private static Camera cameraJg;
    private static SurfaceView cameraViewByJg;
    private static SurfaceHolder cameraHolderByJg;

    private static Camera cameraChk;
    private static SurfaceView cameraViewByChk;
    private static SurfaceHolder cameraHolderyChk;

    private static boolean safeToTakePicByJg=false;
    private static boolean safeToTakePicByChk=false;

    private static List<Integer> mWaitActionByJg = new LinkedList<>(); //暂存拍照的队列
    private static List<Integer> mWaitActionByChk = new LinkedList<>(); //暂存拍照的队列

    private  static  Handler mHandler =null;



    public static void setInSampleSize(int size) {
        inSampleSize = size;
    }

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


                mHandler = new Handler(new Handler.Callback() {
                    @Override
                    public boolean handleMessage(Message msg) {

                        if (msg.what == 1) {
                            Bundle bundle = msg.getData();
                            String imgId = bundle.getString("imgId", "");
                            int type = bundle.getInt("type", 0);
                            if (!StringUtil.isEmptyNotNull(imgId)) {
                                if (type == 0) {
                                    doTakeActionByJg(imgId);
                                } else if(type == 1) {
                                    doTakeActionByJg(imgId);
                                }
                            }
                        }

                        return false;
                    }
                });


                LogUtil.d(TAG, TAG + " showing");
            }
        } catch (Exception ex) {

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

    public static void openCameraByJg() {
        try {
            releaseCameraByJg();
            cameraJg = Camera.open(1);
            cameraJg.setPreviewDisplay(cameraHolderByJg);
            cameraJg.startPreview();
        } catch (Exception ex) {
            cameraJg = null;
            ex.printStackTrace();
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
            ex.printStackTrace();
        }
    }

    public static boolean cameraIsRunningByJg() {

        if (cameraJg == null)
            return false;

        return true;
    }

    public static void openCameraByChk() {
        try {
            releaseCameraByChk();
            cameraChk = Camera.open(2);
            cameraChk.setPreviewDisplay(cameraHolderyChk);
            cameraChk.startPreview();
        } catch (Exception ex) {
            cameraChk = null;
            ex.printStackTrace();
        }
    }

    public static boolean cameraIsRunningByChk() {

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
            ex.printStackTrace();
        }
    }

    public static void takeCameraPicByJg(String imgId) {
        if (cameraJg != null) {
            //判断是否处于拍照，如果正在拍照，则将请求放入缓存队列
            if (safeToTakePicByJg) {
                mWaitActionByJg.add(1);
            } else {
                doTakeActionByJg(imgId);
            }
        }
    }

    private static void doTakeActionByJg(String imgId) {   //拍照方法
        safeToTakePicByJg = true;
        cameraJg.takePicture(null, null, new TakePicCallbackJg(0,imgId));
    }

    public static void takeCameraPicByChk(String imgId){


        if (cameraChk != null) {
            //判断是否处于拍照，如果正在拍照，则将请求放入缓存队列
            if (safeToTakePicByChk) {
                mWaitActionByChk.add(1);
            } else {
                doTakeActionByChk(imgId);
            }
        }
    }

    private static void doTakeActionByChk(String imgId) {   //拍照方法
        safeToTakePicByChk = true;
        cameraJg.takePicture(null, null, new TakePicCallbackChk(1,imgId));
    }

    public static class TakePicCallbackJg implements Camera.PictureCallback {
        private int type;
        private String imgId;

        public TakePicCallbackJg(int type, String imgId) {
            this.type = type;
            this.imgId = imgId;
        }

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            if (mWaitActionByJg.size() > 0) {
                mWaitActionByJg.remove(0);   //移除队列中的第一条拍照请求，并执行拍照请求
                Message msg = new Message();
                msg.what = 1;
                Bundle bundle = new Bundle();
                bundle.putInt("type", type);
                bundle.putString("imgId", imgId);
                msg.setData(bundle);
                mHandler.sendMessage(msg);  //主线程中调用拍照
            } else {
                //队列中没有拍照请求，走正常流程
                safeToTakePicByJg = false;
            }

            new SavePictureTask(imgId).execute(data);  //异步保存照片
        }
    }


    public static class TakePicCallbackChk implements Camera.PictureCallback {
        private int type;
        private String imgId;

        public TakePicCallbackChk(int type, String imgId) {
            this.type = type;
            this.imgId = imgId;
        }

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            if (mWaitActionByChk.size() > 0) {
                mWaitActionByChk.remove(0);   //移除队列中的第一条拍照请求，并执行拍照请求
                Message msg = new Message();
                msg.what = 1;
                Bundle bundle = new Bundle();
                bundle.putInt("type", type);
                bundle.putString("imgId", imgId);
                msg.setData(bundle);
                mHandler.sendMessage(msg);  //主线程中调用拍照
            } else {
                //队列中没有拍照请求，走正常流程
                safeToTakePicByChk = false;
            }

            new SavePictureTask(imgId).execute(data);  //异步保存照片
        }
    }

    public static class SavePictureTask extends AsyncTask<byte[], String, String> {

        private String imgId;

        public SavePictureTask(String imgId) {
            this.imgId = imgId;
        }

        @SuppressLint("SimpleDateFormat")
        @Override
        protected String doInBackground(byte[]... params) {
            byte[] data = params[0];   //回调的数据


            try {
                //保存在本地

                String mSaveDir = OwnFileUtil.getPicSaveDir();

                File pathFile = new File(mSaveDir);
                if (!pathFile.exists()) {
                    pathFile.mkdirs();
                }

                //   Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                Bitmap bitmap = byteToBitmap(data);
                if (bitmap == null)
                    return null;
                String filePath = mSaveDir + "/" + imgId + ".jpg";
                File file = new File(filePath);
                FileOutputStream outputStream = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 50, outputStream);
                outputStream.close();


                Map<String, String> post_params = new HashMap<>();
                post_params.put("fileName", imgId);
                post_params.put("folder", "pickup");

                Map<String, String> filePaths = new HashMap<>();
                filePaths.put("file", filePath);

                HttpClient.postFileByMy(Config.URL.uploadfile, post_params, filePaths, null);

                LogUtil.d(TAG, "拍照结束");
            } catch (Exception ex) {
                ex.printStackTrace();

                LogUtil.e(TAG, "拍照处理失败，onPictureTaken");
                LogUtil.e(TAG, ex);
                AppLogcatManager.saveLogcat2Server("logcat -d -s CameraWindow ", "CameraWindow");
            }


            return null;
        }

        private Bitmap byteToBitmap(byte[] imgByte) {
            Bitmap bitmap = null;
            try {
                InputStream input = null;
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = inSampleSize;
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
                ex.printStackTrace();
                LogUtil.e(TAG, "拍照处理失败，byteToBitmap");
                LogUtil.e(TAG, ex);
                AppLogcatManager.saveLogcat2Server("logcat -d -s CameraWindow ", "CameraWindow");
            }

            return bitmap;
        }
    }

}