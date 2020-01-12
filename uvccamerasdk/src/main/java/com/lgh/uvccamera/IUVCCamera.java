package com.lgh.uvccamera;

import android.hardware.usb.UsbDevice;
import android.os.Handler;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.TextureView;

import com.lgh.uvccamera.callback.ConnectCallback;
import com.lgh.uvccamera.callback.PhotographCallback;
import com.lgh.uvccamera.callback.PictureCallback;
import com.lgh.uvccamera.callback.PreviewCallback;
import com.lgh.uvccamera.config.CameraConfig;
import com.serenegiant.usb.Size;
import com.serenegiant.usb.UVCCamera;

import java.util.List;

/**
 * 描述：uvc相机接口
 * 作者：liugh
 * 日期：2018/12/27
 * 版本：v2.0.0
 */
public interface IUVCCamera {
    void registerReceiver();
    void unregisterReceiver();
    void checkDevice();
    void closeDevice();
    void openCamera(int productId,int vendorId);
    void closeCamera();
    void setPreviewSurface(SurfaceView surfaceView);
    void setPreviewTexture(TextureView textureView);
    void setPreviewRotation(float rotation);
    void setPreviewDisplay(Surface surface);
    void setPreviewSize(int width, int height);
    void startPreview();
    void stopPreview();
    void takePicture(String pictureName);
    void setPreviewCallback(PreviewCallback callback);
    void setPictureTakenCallback(PictureCallback callback);
    UVCCamera getUVCCamera();
    boolean isCameraOpen();
    void setMessageHandler(Handler messageHandler);
}
