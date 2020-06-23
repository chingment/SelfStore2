package com.uplink.selfstore.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Environment;
import android.os.IBinder;
import android.os.SystemClock;

import com.uplink.selfstore.broadcast.AlarmReceiver;
import com.uplink.selfstore.own.OwnFileUtil;
import com.uplink.selfstore.utils.DateUtil;
import com.uplink.selfstore.utils.FileUtil;
import com.uplink.selfstore.utils.LogUtil;

import java.io.File;
import java.io.FilenameFilter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class AlarmService  extends Service {
    private static final String TAG = "AlarmService";
    /**
     * 每1分钟更新一次数据
     */
    private static final int ONE_Miniute=60*1000;
    private static final int PENDING_REQUEST=0;

    public AlarmService() {
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
                LogUtil.i(TAG,"定时删除文件："+ System.currentTimeMillis());
                String picDir = OwnFileUtil.getPicSaveDir();
                AlarmService.delete(picDir,7);
                String moveDir = OwnFileUtil.getMovieSaveDir();
                AlarmService.delete(moveDir,7);
            }
        }).start();

        //通过AlarmManager定时启动广播
        AlarmManager alarmManager= (AlarmManager) getSystemService(ALARM_SERVICE);
        long triggerAtTime= SystemClock.elapsedRealtime()+ONE_Miniute;//从开机到现在的毫秒书（手机睡眠(sleep)的时间也包括在内
        Intent i=new Intent(this, AlarmReceiver.class);
        PendingIntent pIntent=PendingIntent.getBroadcast(this,PENDING_REQUEST,i,PENDING_REQUEST);
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,triggerAtTime,pIntent);

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
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
}
