package com.uplink.selfstore.service;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.SystemClock;
import com.uplink.selfstore.broadcast.HeartbeatRecevier;
import com.uplink.selfstore.model.api.MachineBean;
import com.uplink.selfstore.own.AppCacheManager;
import com.uplink.selfstore.own.AppManager;
import com.uplink.selfstore.ui.BaseFragmentActivity;
import com.uplink.selfstore.utils.LogUtil;
import org.json.JSONException;
import org.json.JSONObject;
public class HeartbeatService extends Service {

    private static final String TAG = "HeartbeatService";
    /**
     * 每20分钟更新一次数据
     */
    private static final int ONE_Miniute=20*60*1000;
    //private static final int ONE_Miniute=1000;
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

//                ActivityManager am = (ActivityManager)getSystemService(ACTIVITY_SERVICE);
//                ComponentName cn = am.getRunningTasks(1).get(0).topActivity;
//                LogUtil.d(TAG, "pkg:"+cn.getPackageName());//包名
//                LogUtil.d(TAG, "cls:"+cn.getClassName());//包名加类名

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
        try {


            LogUtil.i(TAG, "心跳包发送：" + System.currentTimeMillis());
            MachineBean machine = AppCacheManager.getMachine();

            String status = "unknow";

            Activity activity = AppManager.getAppManager().currentActivity();
            if (activity != null) {
                String activityName = activity.getLocalClassName();
                LogUtil.e(TAG, "当前activity:" + activityName);
                if (activityName.contains(".Sm")) {
                    status = "setting";
                } else {
                    status = "running";
                }
            }

            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("status", status);
            } catch (JSONException e) {
                e.printStackTrace();
                return;
            }

            BaseFragmentActivity.eventNotify("HeartbeatBag","发送心跳包",jsonObject);

        }
        catch (Exception ex){
           ex.printStackTrace();
        }

    }
}
