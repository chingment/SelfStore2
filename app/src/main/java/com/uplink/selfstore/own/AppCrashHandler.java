package com.uplink.selfstore.own;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.nfc.Tag;
import android.os.Build;
import android.os.Environment;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

import com.uplink.selfstore.BuildConfig;
import com.uplink.selfstore.activity.InitDataActivity;
import com.uplink.selfstore.http.HttpClient;
import com.uplink.selfstore.http.HttpResponseHandler;
import com.uplink.selfstore.utils.LogUtil;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AppCrashHandler implements Thread.UncaughtExceptionHandler {

    public static final String TAG = "AppCrashHandler";

    // 系统默认的UncaughtException处理类
    private Thread.UncaughtExceptionHandler mDefaultHandler;
    // CrashHandler实例
    private static AppCrashHandler INSTANCE = new AppCrashHandler();
    // 程序的Context对象
    private Context mContext;
    // 用来存储设备信息和异常信息
    private Map<String, String> infos = new HashMap<String, String>();
    // 用于格式化日期,作为日志文件名的一部分
    private DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");

    /** 保证只有一个CrashHandler实例 */
    private AppCrashHandler() {
    }

    /** 获取CrashHandler实例 ,单例模式 */
    public static AppCrashHandler getInstance() {
        return INSTANCE;
    }

    /**
     * 初始化
     *
     * @param context
     */
    public void init(Context context, HandlerResult handlerResult) {
        mContext = context;
        // 获取系统默认的UncaughtException处理器
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        //处理结果器
        mHandlerResult=handlerResult;
        // 设置该CrashHandler为程序的默认处理器
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    /**
     * 当UncaughtException发生时会转入该函数来处理
     */
    @Override
    public void uncaughtException(Thread thread, Throwable ex) {

        handleException(ex);

        if(mHandlerResult==null){
            mDefaultHandler.uncaughtException(thread, ex);
        }
        else {
            mHandlerResult.complete(thread, ex);
        }
    }

    /**
     * 自定义错误处理,收集错误信息 发送错误报告等操作均在此完成.
     *
     * @param ex
     * @return true:如果处理了该异常信息;否则返回false.
     */
    private boolean handleException(Throwable ex) {
        if (ex == null) {
            return false;
        }

        // 使用Toast来显示异常信息
        new Thread() {
            @Override
            public void run() {
                Looper.prepare();
                Toast.makeText(mContext, "很抱歉,程序出现异常即将退出", Toast.LENGTH_LONG).show();
                Looper.loop();
            }
        }.start();


        // 收集设备参数信息
        collectDeviceInfo(mContext);
        // 保存日志文件
        String filePath = saveCrashInfo2Local(ex);
        // 上传到服务器
        saveCrashInfo2Server(filePath,ex);
        return true;
    }

    /**
     * 收集设备参数信息
     *
     * @param ctx
     */
    public void collectDeviceInfo(Context ctx) {
        try {
            PackageManager pm = ctx.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(ctx.getPackageName(), PackageManager.GET_ACTIVITIES);
            if (pi != null) {
                String versionName = pi.versionName == null ? "null" : pi.versionName;
                String versionCode = pi.versionCode + "";
                infos.put("versionName", versionName);
                infos.put("versionCode", versionCode);
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "an error occured when collect package info", e);
        }
        Field[] fields = Build.class.getDeclaredFields();
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                infos.put(field.getName(), field.get(null).toString());
                Log.d(TAG, field.getName() + " : " + field.get(null));
            } catch (Exception e) {
                Log.e(TAG, "an error occured when collect crash info", e);
            }
        }
    }

    /**
     * 保存错误信息到文件中
     *
     * @param ex
     * @return 返回文件名称,便于将文件传送到服务器
     */
    private String saveCrashInfo2Local(Throwable ex) {

        StringBuffer sb = new StringBuffer();
        for (Map.Entry<String, String> entry : infos.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            sb.append(key + "=" + value + "\n");
        }


        Writer writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        ex.printStackTrace(printWriter);
        Throwable cause = ex.getCause();
        while (cause != null) {
            cause.printStackTrace(printWriter);
            cause = cause.getCause();
        }
        printWriter.close();
        String result = writer.toString();
        sb.append(result);
        String filePath=null;
        try {
            long timestamp = System.currentTimeMillis();
            String time = formatter.format(new Date());

            String fileName = "crash-" + time + "-" + timestamp + ".log";
            String path =OwnFileUtil.getLogDir();
            File dir = new File(path);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            filePath=path +"/"+ fileName;
            FileOutputStream fos = new FileOutputStream(filePath);
            fos.write(sb.toString().getBytes());
            fos.flush();
            fos.close();
            return filePath;
        } catch (Exception e) {
            Log.e(TAG, "an error occured while writing file...", e);
        }
        return null;
    }

    private void  saveCrashInfo2Server(String filePath, Throwable ex){

        if(filePath!=null) {

            File file = new File(filePath);

            HashMap<String, String> fields=new HashMap<>();

            fields.put("folder","SelfStoreCrashLog");
            fields.put("fileName",file.getName());
            List<String> filePaths = new ArrayList<>();
            filePaths.add(filePath);

            HttpClient.postFile(Config.URL.uploadfile, fields, filePaths, new HttpResponseHandler() {
                @Override
                public void onSuccess(String response) {

                }
            });
        }
    }

    private HandlerResult mHandlerResult = null;

    public interface HandlerResult {
        void complete(Thread thread, Throwable ex);
    }

    public void saveLogcat2Server(String cmd) {


        new Thread(new Runnable(){

            @Override
            public void run(){



                long timestamp = System.currentTimeMillis();
                String time = formatter.format(new Date());

                String fileName = "logcat-" + time + "-" + timestamp + ".log";
                String path =OwnFileUtil.getLogDir();
                File dir = new File(path);
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                String  filePath=path +"/"+ fileName;

                StringBuilder log = new StringBuilder();
                try {
                    Process process = Runtime.getRuntime().exec(cmd+" -f "+ filePath);


                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                    HashMap<String, String> fields=new HashMap<>();
                    fields.put("folder","SelfStoreLogcatLog");
                    fields.put("fileName",fileName);
                    List<String> filePaths = new ArrayList<>();
                    filePaths.add(filePath);

                    HttpClient.postFile(Config.URL.uploadfile, fields, filePaths, new HttpResponseHandler() {
                        @Override
                        public void onSuccess(String response) {

                        }
                    });


                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }
}