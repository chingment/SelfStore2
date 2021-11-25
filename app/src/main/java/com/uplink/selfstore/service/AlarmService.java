package com.uplink.selfstore.service;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.text.format.Time;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.TypeReference;
import com.uplink.selfstore.BuildConfig;
import com.uplink.selfstore.broadcast.AlarmReceiver;
import com.uplink.selfstore.db.DbManager;
import com.uplink.selfstore.deviceCtrl.CabinetCtrlByDS;
import com.uplink.selfstore.http.HttpClient;
import com.uplink.selfstore.http.HttpResponseHandler;
import com.uplink.selfstore.model.TripMsgBean;
import com.uplink.selfstore.model.api.AdBean;
import com.uplink.selfstore.model.api.ApiResultBean;
import com.uplink.selfstore.model.api.CbLightBean;
import com.uplink.selfstore.model.api.DeviceBean;
import com.uplink.selfstore.model.api.Result;
import com.uplink.selfstore.model.api.RetDeviceEventNotify;
import com.uplink.selfstore.ostCtrl.OstCtrlInterface;
import com.uplink.selfstore.own.AppCacheManager;
import com.uplink.selfstore.own.AppContext;
import com.uplink.selfstore.own.AppManager;
import com.uplink.selfstore.own.AppUtil;
import com.uplink.selfstore.own.Config;
import com.uplink.selfstore.own.OwnFileUtil;
import com.uplink.selfstore.ui.BaseFragmentActivity;
import com.uplink.selfstore.utils.CommonUtil;
import com.uplink.selfstore.utils.DateUtil;
import com.uplink.selfstore.utils.FileUtil;
import com.uplink.selfstore.utils.LocationUtil;
import com.uplink.selfstore.utils.LogUtil;
import com.uplink.selfstore.utils.NetFlowInfo;
import com.uplink.selfstore.utils.NetFlowUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FilenameFilter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class AlarmService  extends Service {
    private static final String TAG = "AlarmService";
    /**
     * 每1分钟更新一次数据
     */
    //private static final int Miniute=60*1000;
    //private static final int PENDING_REQUEST=0;


    private static final int handler1_Miniute=5*60*1000;
    private Handler handler1;
    private Runnable handler1_Runnable;

    public static int cb_Light_Value=0;

    public AlarmService() {
    }

    /**
     * 调用Service都会执行到该方法
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        synchronized (AlarmService.this) {
            if (handler1 == null) {
                handler1 = new Handler();
                handler1_Runnable = new Runnable() {
                    @Override

                    public void run() {
                        Date currentTime = new Date();
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);



                        String dateString = sdf.format(currentTime);

                        LogUtil.i(TAG, "定时任务：" + dateString);


                        sendDeviceStatus();

                        deleteTempFile();

                        deleteTripMsgs();

                        changeLighting();

                        alarmReboot();

                        handler1.postDelayed(this, handler1_Miniute);

                    }

                };
                handler1.postDelayed(handler1_Runnable, handler1_Miniute);
            }

        }

//        //这里模拟后台操作
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//
//
//            }
//        }).start();

//        //通过AlarmManager定时启动广播
//        AlarmManager alarmManager= (AlarmManager) getSystemService(ALARM_SERVICE);
//        long triggerAtTime= SystemClock.elapsedRealtime()+Miniute;//从开机到现在的毫秒书（手机睡眠(sleep)的时间也包括在内
//        Intent i=new Intent(this, AlarmReceiver.class);
//        PendingIntent pIntent=PendingIntent.getBroadcast(this,PENDING_REQUEST,i,PENDING_REQUEST);
//        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,triggerAtTime,pIntent);

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onDestroy(){
        super.onDestroy();

        if(handler1!=null&&handler1_Runnable!=null) {
            handler1.removeCallbacks(handler1_Runnable);
        }
    }

    private void  sendDeviceStatus() {

        Activity activity = AppManager.getAppManager().currentActivity();

        JSONObject params = new JSONObject();
        try {

            NetFlowInfo flowInfo = NetFlowUtil.getAppFlowInfo("com.uplink.selfstore", getApplicationContext());

            params.put("activity", activity == null ? "" : activity.getLocalClassName());
            params.put("status", AppUtil.getDeviceStatus());
            params.put("upKb", flowInfo.getUpKb());
            params.put("downKb", flowInfo.getDownKb());

        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

       BaseFragmentActivity.eventNotify("device_status","轮询定时发送状态",params);

    }

    public static void deleteTempFile() {
        LogUtil.i(TAG, "定时任务：删除临时文件");
        String picDir = OwnFileUtil.getPicSaveDir();
        FileUtil.deleteFile(picDir, 7);
        String moveDir = OwnFileUtil.getMovieSaveDir();
        FileUtil.deleteFile(moveDir, 7);
    }

    public static void changeLighting() {
        LogUtil.i(TAG, "定时任务：改变灯光");

        Activity activity = AppManager.getAppManager().currentActivity();
        if (activity != null) {
            String activityName = activity.getLocalClassName();
            if (activityName.contains(".OrderDetailsActivity")) {
                return;
            }
        }

        DeviceBean device = AppCacheManager.getDevice();
        if (device == null)
            return;

        HashMap<String, String> lights = device.getLights();
        if(lights!=null){

            if(lights.containsKey("cb")) {

                String content = lights.get("cb");

                List<CbLightBean> cbLights = JSONArray.parseObject(content, new TypeReference<List<CbLightBean>>() {
                });

                if(cbLights!=null) {

                    int value = 0;

                    for (CbLightBean cbLight :
                            cbLights) {
                        if (CommonUtil.isBelongPeriodTime(cbLight.getStart(), cbLight.getEnd())) {
                            value = cbLight.getValue();
                            break;
                        }
                    }

                    int t_value = 131;
                    switch (value) {
                        case 1:
                            t_value = 128;
                            break;
                        case 2:
                            t_value = 129;
                            break;
                        case 3:
                            t_value = 130;
                            break;
                        case 4:
                            t_value = 131;
                            break;
                        case 5:
                            t_value = 132;
                            break;
                    }

                    LogUtil.i(TAG, "cbLight.value:" + t_value);

//                    if(cb_Light_Value!=t_value) {
//                        cb_Light_Value = t_value;

                    CabinetCtrlByDS.getInstance().connect();
                    CabinetCtrlByDS.getInstance().setCbLight(t_value);
                }
            }
        }
    }

    public static void deleteTripMsgs() {
        try {
            List<TripMsgBean> tripMsgs = DbManager.getInstance().getTripMsgs();
            if (tripMsgs != null) {

                for (TripMsgBean trip : tripMsgs) {
                    com.alibaba.fastjson.JSONObject params = JSON.parseObject(trip.getContent());
                    params.put("msgId", trip.getMsgId());
                    params.put("msgMode","timer");
                    String json = params.toString();
                    HttpClient.postByMy(Config.URL.device_EventNotify, json, new HttpResponseHandler() {
                        @Override
                        public void onBeforeSend() {
                        }
                        @Override
                        public void onSuccess(String response) {

                            if (response != null) {
                                if (response.contains("\"result\":")) {

                                    ApiResultBean<RetDeviceEventNotify> rt = JSON.parseObject(response, new TypeReference<ApiResultBean<RetDeviceEventNotify>>() {
                                    });
                                    if (rt.getResult() == Result.SUCCESS) {
                                        RetDeviceEventNotify ret = rt.getData();
                                        DbManager.getInstance().deleteTripMsg(ret.getMsgId());
                                    }
                                }
                            }
                        }
                        @Override
                        public void onFailure(String msg, Exception e) {
                        }
                    });
                }
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    public static void alarmReboot(){

        SharedPreferences mPerferences = PreferenceManager.getDefaultSharedPreferences(AppContext.getInstance().getApplicationContext());
        boolean reboot = mPerferences.getBoolean("reboot", false);
        if(atTheCurrentTime(4,30,5,0)) {

            LogUtil.i(TAG, "定时任务：检测重启");

            if (!reboot) {
                Activity activity = AppManager.getAppManager().currentActivity();
                if (activity != null) {
                    String activityName = activity.getLocalClassName();
                    if (activityName.contains(".MainActivity")) {
                        SharedPreferences.Editor editor = mPerferences.edit();
                        editor.putBoolean("reboot", true);
                        editor.commit();

                        BaseFragmentActivity.eventNotify("device_reboot","重启系统", null);

                        OstCtrlInterface.getInstance().reboot(AppContext.getInstance().getApplicationContext());

                    }
                }
            }
        }
        else
        {
            SharedPreferences.Editor editor = mPerferences.edit();
            editor.putBoolean("reboot", false);
            editor.commit();
        }
    }

    public static boolean atTheCurrentTime(int beginHour, int beginMin, int endHour, int endMin) {
        boolean result = false;
        final long aDayInMillis = 1000 * 60 * 60 * 24;
        final long currentTimeMillis = System.currentTimeMillis();
        Time now = new Time();
        now.set(currentTimeMillis);
        Time startTime = new Time();
        startTime.set(currentTimeMillis);
        startTime.hour = beginHour;
        startTime.minute = beginMin;
        Time endTime = new Time();
        endTime.set(currentTimeMillis);
        endTime.hour = endHour;
        endTime.minute = endMin;
        /**跨天的特殊情况(比如23:00-2:00)*/
        if (!startTime.before(endTime)) {
            startTime.set(startTime.toMillis(true) - aDayInMillis);
            result = !now.before(startTime) && !now.after(endTime); // startTime <= now <= endTime
            Time startTimeInThisDay = new Time();
            startTimeInThisDay.set(startTime.toMillis(true) + aDayInMillis);
            if (!now.before(startTimeInThisDay)) {
                result = true;
            }
        } else {
            /**普通情况(比如5:00-10:00)*/
            result = !now.before(startTime) && !now.after(endTime); // startTime <= now <= endTime
        }
        return result;
    }
}
