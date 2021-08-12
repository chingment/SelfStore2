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
import com.uplink.selfstore.own.AppCacheManager;
import com.uplink.selfstore.own.Config;
import com.uplink.selfstore.own.OwnFileUtil;
import com.uplink.selfstore.utils.CommonUtil;
import com.uplink.selfstore.utils.DateUtil;
import com.uplink.selfstore.utils.FileUtil;
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

    public static void  deleteTempFile(){
        LogUtil.i(TAG, "定时任务：删除临时文件");
        String picDir = OwnFileUtil.getPicSaveDir();
        delete(picDir, 7);
        String moveDir = OwnFileUtil.getMovieSaveDir();
        delete(moveDir, 7);
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

                    CabinetCtrlByDS.getInstance().connect();
                    CabinetCtrlByDS.getInstance().setCbLight(t_value);
                }
            }
        }
        // device.getLighting()

    }

    //删除dirPath文件下 前几天的文件
    public static void delete(final String dirPath, final int day) {
        FileUtil.delete(dirPath, new FilenameFilter() {

            @Override
            public boolean accept(File file, String filename) {

                boolean isFlag=false;
                try {
                    long time = file.lastModified();
                    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String result = formatter.format(time);
                    Date startTime = formatter.parse(result);
                    Date endTime = DateUtil.getNowDate();
                    long diff = endTime.getTime() - startTime.getTime();
                    long days = diff / (1000 * 60 * 60 * 24);
                    if(days>=day){
                        isFlag=true;
                    }
                }catch (Exception ex)
                {
                    isFlag=false;
                }

                return isFlag;
            }
        });
    }


    public static void deleteTripMsgs() {


        List<TripMsgBean> tripMsgs = DbManager.getInstance().getTripMsgs();

        if(tripMsgs!=null){

            for (TripMsgBean trip: tripMsgs ) {

                Map<String, Object> params = JSON.parseObject(trip.getContent(), new TypeReference<Map<String, Object>>() {
                });

                HttpClient.postByMy(Config.URL.device_EventNotify, params, null, new HttpResponseHandler() {

                    @Override
                    public void onBeforeSend() {


                    }
                    @Override
                    public void onSuccess(String response) {

                        ApiResultBean<Object> rt = JSON.parseObject(response, new TypeReference<ApiResultBean<Object>>() {
                        });

                        if(rt.getResult()== Result.SUCCESS){
                            DbManager.getInstance().deleteTripMsg(trip.getMsgId());
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
