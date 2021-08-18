package com.uplink.selfstore.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;

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
import com.uplink.selfstore.own.AppCacheManager;
import com.uplink.selfstore.own.Config;
import com.uplink.selfstore.own.OwnFileUtil;
import com.uplink.selfstore.utils.CommonUtil;
import com.uplink.selfstore.utils.DateUtil;
import com.uplink.selfstore.utils.FileUtil;
import com.uplink.selfstore.utils.LocationUtil;
import com.uplink.selfstore.utils.LogUtil;

import java.io.File;
import java.io.FilenameFilter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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


                        deleteTempFile();

                        deleteTripMsgs();

                        changeLighting();

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

    public static void deleteTempFile() {
        LogUtil.i(TAG, "定时任务：删除临时文件");
        String picDir = OwnFileUtil.getPicSaveDir();
        FileUtil.deleteFile(picDir, 7);
        String moveDir = OwnFileUtil.getMovieSaveDir();
        FileUtil.deleteFile(moveDir, 7);
    }

    public static void changeLighting() {
        LogUtil.i(TAG, "定时任务：改变灯光");

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


        List<TripMsgBean> tripMsgs = DbManager.getInstance().getTripMsgs();

        if(tripMsgs!=null){

            for (TripMsgBean trip: tripMsgs ) {

                com.alibaba.fastjson.JSONObject params = JSON.parseObject(trip.getContent());

                params.put("msgId",trip.getMsgId());

                String json=params.toString();

                HttpClient.postByMy(Config.URL.device_EventNotify, json, new HttpResponseHandler() {

                    @Override
                    public void onBeforeSend() {


                    }
                    @Override
                    public void onSuccess(String response) {

                        ApiResultBean<RetDeviceEventNotify> rt = JSON.parseObject(response, new TypeReference<ApiResultBean<RetDeviceEventNotify>>() {
                        });

                        if(rt.getResult()==Result.SUCCESS){
                            RetDeviceEventNotify ret=rt.getData();
                            DbManager.getInstance().deleteTripMsg(ret.getMsgId());
                        }
                    }

                    @Override
                    public void onFailure(String msg, Exception e) {

                    }
                });
            }
        }
    }
}
