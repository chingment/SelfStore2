package com.tamic.statinterface.stats.core;

import android.content.Context;
import android.util.Log;

import com.tamic.statinterface.stats.bean.body.DataBlock;
import com.tamic.statinterface.stats.db.helper.DataConstruct;
import com.tamic.statinterface.stats.db.helper.StaticsAgent;
import com.tamic.statinterface.stats.bean.body.ExceptionInfo;
import com.tamic.statinterface.stats.util.DeviceUtil;

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

    private ExceptionHandler exceptionHandler = null;

    public interface ExceptionHandler {
        void Handler();
    }
}