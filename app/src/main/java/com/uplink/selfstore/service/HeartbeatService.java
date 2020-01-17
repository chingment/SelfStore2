package com.uplink.selfstore.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;

import com.uplink.selfstore.BuildConfig;
import com.uplink.selfstore.broadcast.AlarmReceiver;
import com.uplink.selfstore.broadcast.HeartbeatRecevier;
import com.uplink.selfstore.http.HttpClient;
import com.uplink.selfstore.http.HttpResponseHandler;
import com.uplink.selfstore.model.api.MachineBean;
import com.uplink.selfstore.own.AppCacheManager;
import com.uplink.selfstore.own.AppContext;
import com.uplink.selfstore.own.AppManager;
import com.uplink.selfstore.own.Config;
import com.uplink.selfstore.utils.DateUtil;
import com.uplink.selfstore.utils.FileUtil;
import com.uplink.selfstore.utils.LogUtil;
import com.uplink.selfstore.utils.StringUtil;

import java.io.File;
import java.io.FilenameFilter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class HeartbeatService extends Service {

    private static final String TAG = "HeartbeatService";
    /**
     * 每20分钟更新一次数据
     */
    private static final int ONE_Miniute=10*1000;
    private static final int PENDING_REQUEST=1;

    public HeartbeatService() {
    }

    /**
     * 调用Service都会执行到该方法
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        //这里模拟后台操作
        new Thread(new Runnable() {
            @Override
            public void run() {
                LogUtil.e(TAG,"心跳包发送成功："+ System.currentTimeMillis());
                HeartbeatService.sendHeartbeatBag();

            }
        }).start();

        //通过AlarmManager定时启动广播
        AlarmManager alarmManager= (AlarmManager) getSystemService(ALARM_SERVICE);
        long triggerAtTime= SystemClock.elapsedRealtime()+ONE_Miniute;//从开机到现在的毫秒书（手机睡眠(sleep)的时间也包括在内
        Intent i=new Intent(this, HeartbeatRecevier.class);
        PendingIntent pIntent=PendingIntent.getBroadcast(this,PENDING_REQUEST,i,0);
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,triggerAtTime,pIntent);

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public static void sendHeartbeatBag(){

        Map<String, Object> params = new HashMap<>();
        params.put("deviceId", AppContext.getInstance().getDeviceId());

        HttpClient.postByAppSecret(BuildConfig.APPKEY, BuildConfig.APPSECRET, Config.URL.machine_SendHeartbeatBag, params, null, new HttpResponseHandler() {

            @Override
            public void onBeforeSend() {

            }

            @Override
            public void onSuccess(String response) {

                LogUtil.e(TAG,"心跳包发送成功");
            }

            @Override
            public void onFailure(String msg, Exception e) {
            }
        });

    }
}
