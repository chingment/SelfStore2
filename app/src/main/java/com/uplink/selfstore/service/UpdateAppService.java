package com.uplink.selfstore.service;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.view.WindowManager;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.uplink.selfstore.BuildConfig;
import com.uplink.selfstore.activity.MainActivity;
import com.uplink.selfstore.http.HttpClient;
import com.uplink.selfstore.http.HttpResponseHandler;
import com.uplink.selfstore.model.api.ApiResultBean;
import com.uplink.selfstore.model.api.CheckUpdateBean;
import com.uplink.selfstore.model.api.GlobalDataSetBean;
import com.uplink.selfstore.model.api.Result;
import com.uplink.selfstore.own.AppContext;
import com.uplink.selfstore.own.AppManager;
import com.uplink.selfstore.own.Config;
import com.uplink.selfstore.ui.BaseFragmentActivity;
import com.uplink.selfstore.ui.dialog.CustomDialogLoading;
import com.uplink.selfstore.utils.LogUtil;
import com.uplink.selfstore.utils.StringUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * 检测安装更新文件的助手类
 *
 * @author G.Y.Y
 *
 */

public class UpdateAppService extends Service {
    private static String TAG = "UpdateAppService";
    private DownloadManager manager;
    private DownloadCompleteReceiver receiver;
    private Handler handler_msg;
    private CustomDialogLoading customDialogLoading;
    private CommandReceiver cmdReceiver;

    private void downloadManagerApk(String downpath) {

        try {

            Message m = new Message();
            m.what = 1;
            handler_msg.sendMessage(m);

            manager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);

            receiver = new DownloadCompleteReceiver();

            //设置下载地址
            DownloadManager.Request down = new DownloadManager.Request(
                    Uri.parse(downpath));

            // 设置允许使用的网络类型，这里是移动网络和wifi都可以
            down.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE
                    | DownloadManager.Request.NETWORK_WIFI);

            // 下载时，通知栏显示途中
            down.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);

            // 显示下载界面
            down.setVisibleInDownloadsUi(true);

            String path = Environment.getExternalStorageDirectory() + "/Download";

            // 设置下载后文件存放的位置
            down.setDestinationInExternalFilesDir(this, path, "fanju.apk");

            // 将下载请求放入队列
            manager.enqueue(down);

            //注册下载广播
            registerReceiver(receiver, new IntentFilter(
                    DownloadManager.ACTION_DOWNLOAD_COMPLETE));
        }
        catch (Exception ex)
        {
            Message m = new Message();
            m.what = 2;
            handler_msg.sendMessage(m);
        }

    }

    @Override
    public void onCreate() {
        LogUtil.d(TAG, "onCreate...");

        handler_msg = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {

                switch (msg.what)
                {
                    case  1:
                        if(customDialogLoading==null) {
                            customDialogLoading = new CustomDialogLoading(AppManager.getAppManager().currentActivity());
                        }
                        customDialogLoading.setProgressText("系统正在更新中....");
                        if(!customDialogLoading.isShowing()) {
                            customDialogLoading.showDialog();

                            Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    if (customDialogLoading != null && customDialogLoading.isShowing()) {
                                        customDialogLoading.cancelDialog();
                                    }
                                }
                            }, 30*60*1000);
                        }
                        break;
                    case 2:
                        if(customDialogLoading!=null) {
                            customDialogLoading.cancelDialog();
                        }
                        break;
                    case 3:
                        if(from==2) {
                            BaseFragmentActivity act = (BaseFragmentActivity) AppManager.getAppManager().currentActivity();
                            act.showToast("已经是最新版本");
                        }
                        break;
                }
                return  false;
            }
        });

        cmdReceiver = new CommandReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.updateAppService");
        registerReceiver(cmdReceiver, filter);

        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LogUtil.d(TAG, "onStartCommand...");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {

        return null;
    }

    @Override
    public void onDestroy() {

        // 注销下载广播
        if (receiver != null)
            unregisterReceiver(receiver);

        super.onDestroy();
    }

    public static int compareVersion(String version1, String version2) {
        if (version1.equals(version2)) {
            return 0;
        }
        String[] version1Array = version1.split("\\.");
        String[] version2Array = version2.split("\\.");
        int index = 0;
        // 获取最小长度值
        int minLen = Math.min(version1Array.length, version2Array.length);
        int diff = 0;
        // 循环判断每位的大小
        while (index < minLen
                && (diff = Integer.parseInt(version1Array[index])
                - Integer.parseInt(version2Array[index])) == 0) {
            index++;
        }
        if (diff == 0) {
            // 如果位数不一致，比较多余位数
            for (int i = index; i < version1Array.length; i++) {
                if (Integer.parseInt(version1Array[i]) > 0) {
                    return 1;
                }
            }

            for (int i = index; i < version2Array.length; i++) {
                if (Integer.parseInt(version2Array[i]) > 0) {
                    return -1;
                }
            }
            return 0;
        } else {
            return diff > 0 ? 1 : -1;
        }
    }

    private class CheckUpdateThread extends Thread {

        @Override
        public void run() {
            super.run();


            Map<String, String> params = new HashMap<>();
            params.put("appId", BuildConfig.APPLICATION_ID);
            params.put("appKey", BuildConfig.APPKEY);
            HttpClient.getByAppSecret(BuildConfig.APPKEY, BuildConfig.APPSECRET, Config.URL.machine_CheckUpdate, params, new HttpResponseHandler() {

                @Override
                public void onBeforeSend() {

                }

                @Override
                public void onSuccess(String response) {
                    ApiResultBean<CheckUpdateBean> rt = JSON.parseObject(response, new TypeReference<ApiResultBean<CheckUpdateBean>>() {
                    });

                    if (rt.getResult() == Result.SUCCESS) {
                        CheckUpdateBean d = rt.getData();
                        if (d != null) {
                            if (d.getVersionName() != null && d.getApkDownloadUrl() != null) {
                                int c = compareVersion(d.getVersionName(), BuildConfig.VERSION_NAME);
                                if (c == 1) {
                                    downloadManagerApk(d.getApkDownloadUrl());
                                }
                                else {
                                    Message m = new Message();
                                    m.what = 3;
                                    handler_msg.sendMessage(m);
                                }
                            }
                        }
                    }
                }

                @Override
                public void onFailure(String msg, Exception e) {

                }
            });


            //判断App是否为最新版本，若不是进行下载
            //downloadManagerApk();

//                LogUtil.i(TAG,"判断App是否能进行安装，只能在当前页面是MainActivity情况下更新app");
//                Activity act=AppManager.getAppManager().currentActivity();
//                if(act instanceof MainActivity) {
//                    LogUtil.i(TAG,"当前Activity is MainActivity");
//                    final Message m = new Message();
//                    m.what = 1;
//                    handler_msg.sendMessage(m);
//
//                }
        }
    }

    public  static int from=-1;
    private class CommandReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context,Intent intent) {
            LogUtil.i(TAG, "CommandReceiver.onReceive");

            from = intent.getIntExtra("from",-1);

            CheckUpdateThread checkUpdateThread=new CheckUpdateThread();

            checkUpdateThread.start();

        }
    }

    // 接受下载完成后的intent
    class DownloadCompleteReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            //判断是否下载完成的广播
            if (intent.getAction().equals(
                    DownloadManager.ACTION_DOWNLOAD_COMPLETE)) {

                //获取下载的文件id
                long downId = intent.getLongExtra(
                        DownloadManager.EXTRA_DOWNLOAD_ID, -1);

                Uri uri=manager.getUriForDownloadedFile(downId);
                installAPK(uri);
            }
        }

        private void installAPK(Uri apk) {
            Message m = new Message();
            m.what = 2;
            handler_msg.sendMessage(m);
            if(apk!=null) {
                String paht = apk.getPath();
                Intent intent = new Intent();
                intent.setAction("android.intent.action.installslient");
                intent.putExtra("uri", paht);
                intent.putExtra("component", "com.uplink.selfstore/.activity.InitDataActivity");
                sendBroadcast(intent);
            }
        }
        /**
         * 安装apk文件
         */
//        private void installAPK(Uri apk) {
//
//            // 通过Intent安装APK文件
//            Intent intents = new Intent();
//
//            intents.setAction("android.intent.action.VIEW");
//            intents.addCategory("android.intent.category.DEFAULT");
//            intents.setType("application/vnd.android.package-archive");
//            intents.setData(apk);
//            intents.setDataAndType(apk,"application/vnd.android.package-archive");
//            intents.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            android.os.Process.killProcess(android.os.Process.myPid());
//            // 如果不加上这句的话在apk安装完成之后点击单开会崩溃
//
//            startActivity(intents);
//
//        }

    }
}