package com.lgh.uvccamera.callback;

/**
 * 描述：一般uvc相机用的拍照回调接口
 * 作者：liugh
 * 日期：2018/11/20
 * 版本：v2.0.0
 */
public interface PictureCallback {
    /**
     * 拍照成功图片回调
     *
     * @param data 图片数据
     */
    void onPictureTaken(byte[] data,String fileNam);
}
