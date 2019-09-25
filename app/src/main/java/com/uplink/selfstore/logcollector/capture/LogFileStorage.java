package com.uplink.selfstore.logcollector.capture;

import android.content.Context;
import android.util.Log;

import com.uplink.selfstore.logcollector.utils.LogCollectorUtility;
import com.uplink.selfstore.logcollector.utils.LogHelper;
import com.uplink.selfstore.utils.LogUtil;

import java.io.File;
import java.io.FileOutputStream;

/**
 * @author jiabin
 */
public class LogFileStorage {

    private static final String TAG = LogFileStorage.class.getName();

    public static final String LOG_SUFFIX = ".log";

    private static final String CHARSET = "UTF-8";

    private static LogFileStorage sInstance;

    private Context mContext;

    private LogFileStorage(Context ctx) {
        mContext = ctx.getApplicationContext();
    }

    public static synchronized LogFileStorage getInstance(Context ctx) {
        if (ctx == null) {
            LogHelper.e(TAG, "Context is null");
            return null;
        }
        if (sInstance == null) {
            sInstance = new LogFileStorage(ctx);
        }
        return sInstance;
    }

    public File getUploadLogFile() {
        File dir = mContext.getFilesDir();
        File logFile = new File(dir, LogCollectorUtility.getMid(mContext)
                + LOG_SUFFIX);
        if (logFile.exists()) {
            return logFile;
        } else {
            return null;
        }
    }

    public boolean deleteUploadLogFile() {
        //File dir =LogCollectorUtility.getExternalDir(mContext, "Log");// mContext.getFilesDir();

        File dir = mContext.getFilesDir();

        String path= dir.getPath();
        //LogUtil.i("HttpManager：path" + path);
        //LogUtil.i("HttpManager：getMid" + LogCollectorUtility.getMid(mContext));
        File logFile = new File(dir, LogCollectorUtility.getMid(mContext)
                + LOG_SUFFIX);

        Boolean isDelete = logFile.delete();
        //LogUtil.i("HttpManager：删除文件" + isDelete);
        return isDelete;
    }

    public boolean saveLogFile2Internal(String logString) {
        try {
            File dir = mContext.getFilesDir();
            if (!dir.exists()) {
                dir.mkdirs();
            }
            File logFile = new File(dir, LogCollectorUtility.getMid(mContext)
                    + LOG_SUFFIX);
            FileOutputStream fos = new FileOutputStream(logFile, true);
            fos.write(logString.getBytes(CHARSET));
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
            LogHelper.e(TAG, "saveLogFile2Internal failed!");
            return false;
        }
        return true;
    }

    public boolean saveLogFile2SDcard(String logString, boolean isAppend) {
        if (!LogCollectorUtility.isSDcardExsit()) {
            LogHelper.e(TAG, "sdcard not exist");
            return false;
        }
        try {
            File logDir = getExternalLogDir();
            if (!logDir.exists()) {
                logDir.mkdirs();
            }

            File logFile = new File(logDir, LogCollectorUtility.getMid(mContext)
                    + LOG_SUFFIX);
            /*if (!isAppend) {
				if (logFile.exists() && !logFile.isFile())
					logFile.delete();
			}*/
            LogHelper.d(TAG, logFile.getPath());

            FileOutputStream fos = new FileOutputStream(logFile, isAppend);
            fos.write(logString.getBytes(CHARSET));
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "saveLogFile2SDcard failed!");
            return false;
        }
        return true;
    }

    private File getExternalLogDir() {
        File logDir = LogCollectorUtility.getExternalDir(mContext, "Log");
        LogHelper.d(TAG, logDir.getPath());
        return logDir;
    }
}
