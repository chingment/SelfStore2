package com.lgh.uvccamera;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.usb.UsbDevice;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;

import com.lgh.uvccamera.callback.ConnectCallback;
import com.lgh.uvccamera.callback.PhotographCallback;
import com.lgh.uvccamera.callback.PictureCallback;
import com.lgh.uvccamera.callback.PreviewCallback;
import com.lgh.uvccamera.config.CameraConfig;
import com.lgh.uvccamera.usb.UsbMonitor;
import com.lgh.uvccamera.utils.FileUtil;
import com.lgh.uvccamera.utils.LogUtil;
import com.serenegiant.usb.IButtonCallback;
import com.serenegiant.usb.IFrameCallback;
import com.serenegiant.usb.Size;
import com.serenegiant.usb.UVCCamera;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
/**
 * 描述：相机代理类
 * 作者：liugh
 * 日期：2018/11/16
 * 版本：v2.0.0
 */
public class UVCCameraProxy implements IUVCCamera {
    private final String TAG="UVCCameraProxy";
    private static int PICTURE_WIDTH = 640;
    private static int PICTURE_HEIGHT = 480;
    private Context mContext;
    private UsbMonitor mUsbMonitor;
    protected UVCCamera mUVCCamera;
    private View mPreviewView; // 预览view
    private Surface mSurface;
    private PictureCallback mPictureCallback; // 拍照成功回调
    private PhotographCallback mPhotographCallback; // 设备上的拍照按钮点击回调
    private PreviewCallback mPreviewCallback; // 预览回调
  //  private ConnectCallback mConnectCallback; // usb连接回调
    private CameraConfig mConfig; // 相机相关配置
    protected float mPreviewRotation; // 相机预览旋转角度
    protected boolean isTakePhoto; // 是否拍照
    private String mPictureName; // 图片名称

    private Handler mMessageHandler;//消息处理回调

    public UVCCameraProxy(Context context) {
        mContext = context;
        mConfig = new CameraConfig();
        mUsbMonitor = new UsbMonitor(context, mConfig);
    }

    /**
     * 注册usb插拔监听广播
     */
    @Override
    public void registerReceiver() {
        mUsbMonitor.registerReceiver();
    }

    /**
     * 注销usb插拔监听广播
     */
    @Override
    public void unregisterReceiver() {
        mUsbMonitor.unregisterReceiver();
    }

    /**
     * 检查是否插入了usb摄像头，用于先插入设备再打开页面的场景
     */
    @Override
    public void checkDevice() {
        mUsbMonitor.checkDevice();
    }

    /**
     * 关闭usb设备
     */
    @Override
    public void closeDevice() {
        mUsbMonitor.closeDevice();
    }

    /**
     * 打开相{}
     */
    @Override
    public void openCamera(int productId,int vendorId) {
        Log.i(TAG, "Camera->请求打开设备");
        if(mUVCCamera!=null) {
            Log.i(TAG, "Camera->请求打开设备,已打开");
            return;
        }

        UsbDevice usbDevice = mUsbMonitor.getUsbDevice(productId, vendorId);
        if (usbDevice == null) {
            Log.e(TAG, "Camera->查找不到设备,productId："+productId+",vendorId:"+vendorId);
            sendMessage(UVCCamera.CAMERA_NOFINDDEVICE, "设备为空");
            return;
        }

        Log.i(TAG, "Camera->已找到设备");

        mUVCCamera = new UVCCamera();
        mUsbMonitor.setUVCCamera(mUVCCamera);
        mUsbMonitor.openDevice(usbDevice);

    }



    /**
     * 关闭相机
     */
    @Override
    public void closeCamera() {
        try {
            if (mUVCCamera != null) {
                mUVCCamera.destroy();
                mUVCCamera = null;
            }
            mUsbMonitor.closeDevice();
            LogUtil.i("closeCamera");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 设置相机预览控件，这里封装了相关注册注销广播、检测设备、释放资源等操作
     *
     * @param surfaceView
     */
    @Override
    public void setPreviewSurface(SurfaceView surfaceView) {
        this.mPreviewView = surfaceView;
        if (surfaceView != null && surfaceView.getHolder() != null) {
            surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
                @Override
                public void surfaceCreated(SurfaceHolder holder) {
                    LogUtil.i("surfaceCreated");
                    mSurface = holder.getSurface();
                    checkDevice();
                    registerReceiver();
                }

                @Override
                public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                    LogUtil.i("surfaceChanged");
                }

                @Override
                public void surfaceDestroyed(SurfaceHolder holder) {
                    LogUtil.i("surfaceDestroyed");
                    mSurface = null;
                    unregisterReceiver();
                    closeCamera();
                }
            });
        }
    }

    /**
     * 设置相机预览控件，这里封装了相关注册注销广播、检测设备、释放资源等操作
     *
     * @param surfaceView
     */
    @Override
    public void setPreviewTexture(TextureView textureView) {
        this.mPreviewView = textureView;
        if (textureView != null) {
            if (mPreviewRotation != 0) {
                textureView.setRotation(mPreviewRotation);
            }
            textureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
                @Override
                public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                    LogUtil.i("onSurfaceTextureAvailable");
                    mSurface = new Surface(surface);
                    checkDevice();
                    registerReceiver();
                }

                @Override
                public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
                    LogUtil.i("onSurfaceTextureSizeChanged");
                }

                @Override
                public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                    LogUtil.i("onSurfaceTextureDestroyed");
                    mSurface = null;
                    unregisterReceiver();
                    closeCamera();
                    return false;
                }

                @Override
                public void onSurfaceTextureUpdated(SurfaceTexture surface) {
                }
            });
        }
    }

    /**
     * 设置相机预览旋转角度，暂时只支持TextureView
     *
     * @param rotation
     */
    @Override
    public void setPreviewRotation(float rotation) {
        if (mPreviewView != null && mPreviewView instanceof TextureView) {
            this.mPreviewRotation = rotation;
            mPreviewView.setRotation(rotation);
        }
    }

    /**
     * 设置相机预览Surface
     *
     * @param surface
     */
    @Override
    public void setPreviewDisplay(Surface surface) {
        mSurface = surface;
        try {
           // if (mUVCCamera != null && mSurface != null) {
                mUVCCamera.setPreviewDisplay(mSurface);
            //}
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 设置预览尺寸
     *
     * @param width
     * @param height
     */
    @Override
    public void setPreviewSize(int width, int height) {
        try {
            if (mUVCCamera != null) {
                this.PICTURE_WIDTH = width;
                this.PICTURE_HEIGHT = height;
                mUVCCamera.setPreviewSize(width, height);
                LogUtil.i("setPreviewSize-->" + width + " * " + height);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 开始预览
     */
    @Override
    public void startPreview() {
        try {
            if (mUVCCamera != null) {
                LogUtil.i("startPreview");


                // 图片预览流回调
                mUVCCamera.setFrameCallback(new IFrameCallback() {
                    @Override
                    public void onFrame(ByteBuffer frame) {
                        int lenght = frame.capacity();
                        byte[] yuv = new byte[lenght];
                        frame.get(yuv);
                        if (mPreviewCallback != null) {
                            mPreviewCallback.onPreviewFrame(yuv);
                        }
                        if (isTakePhoto) {
                            LogUtil.i("take picture");
                            isTakePhoto = false;
                            savePicture(yuv, PICTURE_WIDTH, PICTURE_HEIGHT, mPreviewRotation);
                        }
                    }
                }, UVCCamera.PIXEL_FORMAT_YUV420SP);

                if (mSurface != null) {
                    mUVCCamera.setPreviewDisplay(mSurface);
                }
                mUVCCamera.updateCameraParams();
                mUVCCamera.startPreview();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 保存图片
     *
     * @param yuv
     * @param width
     * @param height
     * @param rotation
     */
    public void savePicture(final byte[] yuv, final int width, final int height, final float rotation) {
        if (mPictureCallback == null) {
            return;
        }
        mPictureCallback.onPictureTaken(yuv,mPictureName);
    }

    @Override
    public void stopPreview() {
        try {
            if (mUVCCamera != null) {
                LogUtil.i("stopPreview");
                mUVCCamera.setButtonCallback(null);
                mUVCCamera.setFrameCallback(null, 0);
                mUVCCamera.stopPreview();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void takePicture(String pictureName) {
        isTakePhoto = true;
        mPictureName = pictureName;
    }

    @Override
    public void setPreviewCallback(PreviewCallback callback) {
        this.mPreviewCallback = callback;
    }

    @Override
    public void setPictureTakenCallback(PictureCallback callback) {
        this.mPictureCallback = callback;
    }

    @Override
    public UVCCamera getUVCCamera() {
        return mUVCCamera;
    }

    @Override
    public boolean isCameraOpen() {
        return mUVCCamera != null;
    }

    public void  setMessageHandler(Handler messageHandler){
        this.mMessageHandler=messageHandler;
        this.mUsbMonitor.setMesssageHandler(messageHandler);
    }

    private void sendMessage(int what, String message) {
        if (this.mMessageHandler != null) {
            Message m = new Message();
            m.what = what;
            Bundle data = new Bundle();
            data.putString("message", message);
            m.setData(data);
            mMessageHandler.sendMessage(m);
        }
    }
}