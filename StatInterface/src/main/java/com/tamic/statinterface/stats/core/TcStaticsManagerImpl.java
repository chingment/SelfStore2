/*
 *    Copyright (C) 2016 Tamic
 *
 *    link :https://github.com/Tamicer
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.tamic.statinterface.stats.core;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.tamic.statinterface.stats.bean.body.AppActionType;
import com.tamic.statinterface.stats.bean.body.ViewPath;
import com.tamic.statinterface.stats.constants.StaticsConfig;
import com.tamic.statinterface.stats.db.DbManager;
import com.tamic.statinterface.stats.db.helper.DataConstruct;
import com.tamic.statinterface.stats.db.helper.StaticsAgent;
import com.tamic.statinterface.stats.bean.body.DataBlock;
import com.tamic.statinterface.stats.util.LogUtil;
import com.tamic.statinterface.stats.util.NetworkUtil;

import java.io.InputStream;
import java.util.HashMap;

import cz.msebera.android.httpclient.util.EncodingUtils;



/**
 * StaticsManagerImpl core
 * Created by Tamic on 2016-03-24.
 * #{https://github.com/Tamicer/SkyMonitoring}
 */
public class TcStaticsManagerImpl implements TcStaticsManager, TcObserverPresenter.ScheduleListener {
    private static final String TAG = TcStaticsManagerImpl.class.getSimpleName();
    private static final boolean debug = true;
    /**
     * context
     */
    private Context mContext;
    /**
     * sInstance
     */
    private static TcStaticsManager sInstance;

    private static TcObserverPresenter paObserverPresenter;

    private UploadThread mUploadThread = null;

    private StaticsListener eventInterface;

    private TcStatiPollMgr statiPollMgr;

    HashMap<String, String> pageIdMaps = new HashMap<String, String>();
    /**
     * Log TAG
     */
    private static final String LOG_TAG = TcStatiPollMgr.class.getSimpleName();

    public TcStaticsManagerImpl(Context mContext) {
        this.mContext = mContext;
    }

    @Override
    public boolean onInit(String appId, String channel, String fileName) {

        if (mUploadThread == null) {
            mUploadThread = new UploadThread();
            mUploadThread.start();
        }

        // init DbManager
        DbManager.getInstance().init(mContext);

        // init  ObserverPresenter
        paObserverPresenter = new TcObserverPresenter(this);

        // init StaticsAgent
        StaticsAgent.init();

        // load pageIdMaps
        pageIdMaps = getStatIdMaps(fileName);

        // init  StatiPoll
        statiPollMgr = new TcStatiPollMgr(this);
        // init Header
        return initHeader(appId, channel);
    }

    @Override
    public void onSend() {

        StaticsAgent.getDataBlock(mUploadThread.mHandler);
        // report data to server
//        Platform.get().execute(new Runnable() {
//            @Override
//            public void run() {
//                DataBlock dataBlock = StaticsAgent.getDataBlock();
//
//                if (dataBlock.getAppActions().isEmpty() &&
//                        dataBlock.getEvent().isEmpty() &&
//                        dataBlock.getPage().isEmpty()) {
//                    return;
//                }
//                LogUtil.d(TAG, "TcStatfacr >> report is Start");
//                String jsonString = JsonUtil.toJSONString(dataBlock);
//                LogUtil.d(TAG, "TcStatfacr >> report is sendding"+jsonString);
//                TcUpLoadManager.getInstance(mContext).report(dataBlock);
//            }
//        });
    }

    @Override
    public void onStore() {
        DataConstruct.storeEvents();
        DataConstruct.storePage();
    }

    @Override
    public void onRelease() {
        if (paObserverPresenter != null) {
            paObserverPresenter.destroy();
        }

        stopSchedule();

    }

    @Override
    public void onRecordAppStart() {
        //send
        onSend();
        // store appAction
        DataConstruct.storeAppAction(AppActionType.APPSTART);
    }

    @Override
    public void onRrecordPageEnd() {
        DataConstruct.storeEvents();
        DataConstruct.storePage();
        if (paObserverPresenter != null) {
            paObserverPresenter.onStop(mContext);
        }
        stopSchedule();
    }

    @Override
    public void onRecordPageStart(Context context) {

        if (context == null) {
            return;
        }

        //startSchedule
        startSchedule();

        String pageId = checkValidId(context.getClass().getName());
        if (pageId == null) {
            pageId = context.getClass().getName();
        }

        // init page
        onInitPage(pageId, null);

        if (paObserverPresenter != null) {
            paObserverPresenter.init(mContext);
        }

        if (paObserverPresenter != null) {
            paObserverPresenter.onStart(mContext);
        }
    }


    @Override
    public void onRrecordAppEnd() {

        //recard APP exit
        DataConstruct.storeAppAction(AppActionType.APPEXIT);
        onSend();
        onRelease();
    }

    @Override
    public void onInitPage(String... strings) {
        DataConstruct.initPage(mContext, eventInterface, strings[0], strings[1]);
    }

    @Override
    public void onPageParameter(String... strings) {
        DataConstruct.initPageParameter(strings[0], strings[1]);
    }


    @Override
    public void onInitEvent(String eventName,String viewValue) {
        DataConstruct.initEvent(eventName,viewValue);
    }

    @Override
    public void onInitEvent(ViewPath viewPath) {
        DataConstruct.initEvent(viewPath);
    }

    @Override
    public void onEventParameter(String... strings) {
        DataConstruct.onEvent(strings[0], strings[1]);
    }

    @Override
    public void onEvent(String eventName, HashMap<String, String> parameters) {
        DataConstruct.initEvent(eventName, parameters);
    }

    /**
     * init header
     */
    private boolean initHeader(String appId, String channel) {

        if (!TcHeadrHandle.isInit()) {
            return TcHeadrHandle.initHeader(mContext, appId, channel);
        }
        return false;

    }

    /**
     * onScheduleTimeOut
     */
    void onScheduleTimeOut() {

      //  LogUtil.d(LOG_TAG, "onScheduleTimeOut  is sendData");
        onSend();
    }

    /**
     * startSchedule
     */
    public void startSchedule() {
        // if debug  time is 5 min
        if (StaticsConfig.DEBUG &&
                TcStatInterface.uploadPolicy == TcStatInterface.UploadPolicy.UPLOAD_POLICY_DEVELOPMENT) {
            statiPollMgr.start(5 * 1000);
            LogUtil.d(LOG_TAG, "Schedule is start");
        } else {
            if (NetworkUtil.isWifi(mContext)) {
                statiPollMgr.start(TcStatInterface.getIntervalRealtime() * 60 * 1000);
            } else {
                statiPollMgr.start(TcStatInterface.UPLOAD_TIME_THIRTY * 60 * 1000);
            }
        }
    }

    /**
     * checkValidId
     *
     * @param name activitiyname
     * @return pageId
     */
    private String checkValidId(String name) {
        if (TextUtils.isEmpty(name)) {
            return null;
        }
        if (name.length() <= 0) {
            return null;
        }

        return getPageId(name);
    }


    /**
     * getPageId
     *
     * @param clazz
     * @return
     */
    private String getPageId(String clazz) {
        if (mContext == null) {
            return null;
        }
        return pageIdMaps.get(clazz);
    }

    /**
     * stop Schedule
     */
    public void stopSchedule() {

        LogUtil.d(LOG_TAG, "stopSchedule()");

        statiPollMgr.stop();
    }

    @Override
    public void onStart() {
        LogUtil.d(LOG_TAG, "startSchedule");

        startSchedule();

    }

    @Override
    public void onStop() {
        stopSchedule();
    }

    @Override
    public void onReStart() {
        // stopSchedule
        stopSchedule();
        // startSchedule
        startSchedule();
    }


    public HashMap<String, String> getStatIdMaps(String jsonName) {

        HashMap<String, String> map = null;
        if (getFromAsset(jsonName) != null) {
            map = (HashMap<String, String>) JSON.parseObject(getFromAsset(jsonName), HashMap.class);
        }
        return map;
    }

    public String getFromAsset(String fileName) {
        String result = "";
        try {
            InputStream in = mContext.getResources().getAssets().open(fileName);
            int length = in.available();
            byte[] buffer = new byte[length];
            in.read(buffer);
            result = EncodingUtils.getString(buffer, "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    class UploadThread extends Thread {

        private Handler mHandler = null;

        @Override
        public void run() {
            Looper.prepare();

            mHandler = new Handler(Looper.myLooper()) {

                @Override
                public void handleMessage(Message msg) {

                    if (msg.obj != null && msg.obj instanceof DataBlock) {
                        DataBlock dataBlock = (DataBlock) msg.obj;
                        if (dataBlock.getAppActions().isEmpty() &&
                                dataBlock.getEvents().isEmpty() &&
                                dataBlock.getPages().isEmpty()) {
                            return;
                        }
                        LogUtil.d(TAG, "TcStatfacr >> report is Start");
                        TcUpLoadManager.getInstance(mContext).report(dataBlock);
                    }
                }
            };
            Looper.loop();
        }
    }
}
