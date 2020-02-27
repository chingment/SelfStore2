package com.uplink.selfstore.service;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FilenameFilter;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class HeartbeatService extends Service {

    private static final String TAG = "HeartbeatService";
    /**
     * 每20分钟更新一次数据
     */
    private static final int ONE_Miniute=5*60*1000;
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
                sendHeartbeatBag();

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
        LogUtil.e(TAG,"心跳包发送："+ System.currentTimeMillis());
        MachineBean machine = AppCacheManager.getMachine();

        Map<String, Object> params = new HashMap<>();
        params.put("appId", BuildConfig.APPLICATION_ID);
        params.put("deviceId", AppContext.getInstance().getDeviceId());
        params.put("machineId", machine.getId());
        params.put("type", 1);

        String status="unknow";

        Activity activity=AppManager.getAppManager().currentActivity();
        if(activity!=null) {
            String activityName =activity.getLocalClassName();
            LogUtil.e(TAG,"当前activity:"+activityName);
            if(activityName.contains(".Sm")){
                status="setting";
                params.put("status", AppContext.getInstance().getDeviceId());
            }
            else {
                status="running";
            }
        }

        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("status", status);
            params.put("content", jsonObject);
        }catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        HttpClient.postByAppSecret(BuildConfig.APPKEY, BuildConfig.APPSECRET, Config.URL.machine_EventNotify, params, null, new HttpResponseHandler() {

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
