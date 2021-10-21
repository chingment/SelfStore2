package com.uplink.selfstore.service;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;


import com.uplink.selfstore.R;
import com.uplink.selfstore.activity.InitDataActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * 正常的系统前台进程，会在系统通知栏显示一个Notification通知图标
 *
 * @author clock
 * @since 2016-04-12
 */
public class WhiteService extends Service {

    private final static String TAG = WhiteService.class.getSimpleName();

    private final static int FOREGROUND_ID = 1000;

    private static final int handler1_Miniute=5*1000;
    private Handler handler1;
    private Runnable handler1_Runnable;

    @Override
    public void onCreate() {
        Log.i(TAG, "WhiteService->onCreate");
        super.onCreate();

        if (handler1 == null) {
            handler1 = new Handler();
            handler1_Runnable = new Runnable() {
                @Override

                public void run() {
                    Date currentTime = new Date();
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
                    String dateString = sdf.format(currentTime);

                    Log.i(TAG, "定时任务：" + dateString);


                    handler1.postDelayed(this, handler1_Miniute);

                    if(!isForeground(getApplicationContext())){
                        Intent intent = getPackageManager().getLaunchIntentForPackage("com.uplink.selfstore");
                        startActivity(intent);
                    }
                }

            };
            handler1.postDelayed(handler1_Runnable, handler1_Miniute);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "WhiteService->onStartCommand");

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(R.mipmap.ic_launcher_round);
        builder.setContentTitle("Foreground");
        builder.setContentText("I am a foreground service");
        builder.setContentInfo("Content Info");
        builder.setWhen(System.currentTimeMillis());
        Intent activityIntent = new Intent(this, InitDataActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 1, activityIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);
        Notification notification = builder.build();
        startForeground(FOREGROUND_ID, notification);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "WhiteService->onDestroy");
        super.onDestroy();
        if(handler1!=null&&handler1_Runnable!=null) {
            handler1.removeCallbacks(handler1_Runnable);
        }
    }

    private boolean isForeground(Context context) {
        if (context != null) {
            ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningAppProcessInfo> processes = activityManager.getRunningAppProcesses();
            for (ActivityManager.RunningAppProcessInfo processInfo : processes) {

                if (processInfo.processName.equals(context.getPackageName())) {

                    Log.i(TAG, "processInfo.processName：" + processInfo.processName);
                    Log.i(TAG, "context.getPackageName()：" + context.getPackageName());
                    Log.i(TAG, "processInfo.importance：" + processInfo.importance);

                    if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
