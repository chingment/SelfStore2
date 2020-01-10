package com.lgh.uvccamera.config;

import com.lgh.uvccamera.bean.PicturePath;
import com.lgh.uvccamera.utils.LogUtil;

/**
 * 描述：相关配置
 * 作者：liugh
 * 日期：2018/12/27
 * 版本：v2.0.0
 */
public class CameraConfig {
    private PicturePath mPicturePath = PicturePath.APPCACHE; // 图片保存路径
    private String mDirName = "uvccamera"; // 图片保存目录名称

    public CameraConfig isDebug(boolean debug) {
        LogUtil.allowD = debug;
        LogUtil.allowE = debug;
        LogUtil.allowI = debug;
        LogUtil.allowV = debug;
        LogUtil.allowW = debug;
        LogUtil.allowWtf = debug;
        return this;
    }

    public PicturePath getPicturePath() {
        return mPicturePath;
    }

    public CameraConfig setPicturePath(PicturePath mPicturePath) {
        this.mPicturePath = mPicturePath;
        return this;
    }

    public String getDirName() {
        return mDirName;
    }

    public CameraConfig setDirName(String mDirName) {
        this.mDirName = mDirName;
        return this;
    }

    @Override
    public String toString() {
        return "CameraConfig{" +
                "mPicturePath=" + mPicturePath +
                ", mDirName='" + mDirName + '\''+
                '}';
    }
}
