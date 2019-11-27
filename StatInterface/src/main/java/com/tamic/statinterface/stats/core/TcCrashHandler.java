package com.tamic.statinterface.stats.core;

import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import com.tamic.statinterface.stats.BuildConfig;
import com.tamic.statinterface.stats.bean.body.DataBlock;
import com.tamic.statinterface.stats.db.helper.DataConstruct;
import com.tamic.statinterface.stats.db.helper.StaticsAgent;
import com.tamic.statinterface.stats.bean.body.ExceptionInfo;
import com.tamic.statinterface.stats.util.DeviceUtil;

import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by null on 2016/9/22.
 */
public class TcCrashHandler implements Thread.UncaughtExceptionHandler {
    private Context context;
    public static TcCrashHandler INSTANCE;
    private Thread.UncaughtExceptionHandler uncaughtExceptionHandler;

    private TcCrashHandler() {
    }

    public void init(Context context,ExceptionHandler exceptionHandler) {
        this.context = context;
        this.exceptionHandler=exceptionHandler;
        uncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);
    }


    public static TcCrashHandler getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new TcCrashHandler();
        }
        return INSTANCE;
    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        if (ex != null) {
            StackTraceElement[] stackTraceElements = ex.getStackTrace();
            Log.e("jiangTest", stackTraceElements.length + "---");
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append(ex.getMessage()).append("\n");
            for (int i = stackTraceElements.length - 1; i >= 0; i--) {
                stringBuffer.append(stackTraceElements[i].getFileName()).append(":").append(stackTraceElements[i].getClassName()).append(stackTraceElements[i].getMethodName()).append("(").append(stackTraceElements[i].getLineNumber()).append(")").append("\n");
            }
            Log.e("jiangTest", stringBuffer.toString());

            saveToSdCard(stringBuffer.toString());

            StaticsAgent.storeObject(new ExceptionInfo(DeviceUtil.getPhoneModel(), DeviceUtil.getSystemModel(), String.valueOf(DeviceUtil.getSystemVersion()), stringBuffer.toString()));


            //DataConstruct.storeEvents();
            //DataConstruct.storePage();
            DataBlock dataBlock = StaticsAgent.getDataBlock();

            TcUpLoadManager.getInstance(context).report(dataBlock);

            Log.i("jiangTest", dataBlock.toString());
        }

        exceptionHandler.Handler();
        //android.os.Process.killProcess(android.os.Process.myPid());
        //System.exit(0);
    }

    public  void  saveToSdCard(String content) {
        try {

            StringBuilder sb = new StringBuilder();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss", Locale.CHINA);
            String now = sdf.format(new Date());

            sb.append("TIME:").append(now);//崩溃时间
            //程序信息
            sb.append("\nAPPLICATION_ID:").append(BuildConfig.APPLICATION_ID);//软件APPLICATION_ID
            sb.append("\nVERSION_CODE:").append(BuildConfig.VERSION_CODE);//软件版本号
            sb.append("\nVERSION_NAME:").append(BuildConfig.VERSION_NAME);//VERSION_NAME
            sb.append("\nBUILD_TYPE:").append(BuildConfig.BUILD_TYPE);//是否是DEBUG版本
            //设备信息
            sb.append("\nMODEL:").append(android.os.Build.MODEL);
            sb.append("\nRELEASE:").append(Build.VERSION.RELEASE);
            sb.append("\nSDK:").append(Build.VERSION.SDK_INT);
            sb.append("\nSTACK_TRACE:").append(content);

            String path = Environment.getExternalStorageDirectory().getCanonicalPath() + "/CrashLog/";
            File file = new File(path);
            if (!file.exists()) {
                file.mkdirs();
            }
            String savePath=path+ now + ".log";
            FileWriter writer = new FileWriter(savePath,true);
            writer.write(sb.toString());
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private ExceptionHandler exceptionHandler = null;

    public interface ExceptionHandler {
        void Handler();
    }
}