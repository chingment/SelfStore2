package com.uplink.selfstore.service;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;

import com.alibaba.fastjson.JSON;
import com.uplink.selfstore.activity.CartActivity;
import com.uplink.selfstore.activity.OrderDetailsActivity;
import com.uplink.selfstore.model.api.DeviceBean;
import com.uplink.selfstore.model.api.MqttBean;
import com.uplink.selfstore.model.api.OrderDetailsBean;
import com.uplink.selfstore.own.AppCacheManager;
import com.uplink.selfstore.own.AppManager;
import com.uplink.selfstore.own.CommandManager;
import com.uplink.selfstore.taskexecutor.onebyone.BaseSyncTask;
import com.uplink.selfstore.taskexecutor.onebyone.TinySyncExecutor;
import com.uplink.selfstore.utils.LogUtil;
import com.uplink.selfstore.utils.NetFlowInfo;
import com.uplink.selfstore.utils.NetFlowUtil;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class MqttService extends Service {

    private static final String TAG = "MqttService";

    private static MqttAndroidClient mqttAndroidClient;
    private static MqttConnectOptions mMqttConnectOptions;

    private ScheduledExecutorService reconnectPool;//重连线程池

    private String host = "";
    private String userName = "";
    private String password = "";
    private String clientId = "";
    private String deviceName="";
    private String productName="";

    private static String topic_Subscribe="";//订阅主题
    private static String topic_Pubish="";//发布主题
    private static String topic_Response = "";//响应主题


    private Handler timHandler = new Handler();

    private Runnable timRunable = new Runnable() {
        @Override
        public void run() {
            sendDeviceStatus();
            timHandler.postDelayed(this, 5 * 1000);
        }
    };


    private void  sendDeviceStatus() {


        //LogUtil.d(TAG,"正在执行发送设备状态");

        DeviceBean device = AppCacheManager.getDevice();

        String status = "unknow";
        String activityName = "";
        Activity activity = AppManager.getAppManager().currentActivity();
        if (activity != null) {
            activityName = activity.getLocalClassName();
            if (activityName.contains(".Sm")) {
                status = "setting";
            } else {
                if (device.isExIsHas()) {
                    status = "exception";
                } else {
                    status = "running";
                }
            }
        }

        JSONObject params = new JSONObject();
        try {

            NetFlowInfo flowInfo = NetFlowUtil.getAppFlowInfo("com.uplink.selfstore", getApplicationContext());

            params.put("activity", activityName);
            params.put("deviceId", device.getDeviceId());
            params.put("status", status);
            params.put("upKb", flowInfo.getUpKb());
            params.put("downKb", flowInfo.getDownKb());

        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        publish(UUID.randomUUID().toString().replace("-", ""), "device_status", params, 1);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        buildMqttClient();
    }

    private IMqttActionListener mqttActionListener = new IMqttActionListener() {
        @Override
        public void onSuccess(IMqttToken asyncActionToken) {
            LogUtil.i(TAG,"连接成功");

            closeReconnectTask();

            try {
                mqttAndroidClient.subscribe(topic_Subscribe, 1);
            } catch (MqttException e) {
                e.printStackTrace();
            }

            if (timHandler != null && timRunable != null) {
                timHandler.removeCallbacks(timRunable);
                timHandler.postDelayed(timRunable, 1000);
            }
        }

        @Override
        public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
            LogUtil.i(TAG,"连接失败-"+exception);
            startReconnectTask();
        }
    };

    private MqttCallback mqttCallback = new MqttCallbackExtended() {  //回传
        @Override
        public void connectComplete(boolean reconnect, String serverURI) {

        }

        @Override
        public void connectionLost(Throwable cause) {
            LogUtil.d(TAG,"连接断开");
            if (cause != null) {//null表示被关闭
                startReconnectTask();
            }
        }

        @Override
        public void messageArrived(String topic, MqttMessage message) throws Exception {  // 接收的消息


            String payload = new String(message.getPayload());
            int qos = message.getQos();

            LogUtil.d(TAG, "topic:" + topic);
            LogUtil.d(TAG, "payload:" + payload);
            LogUtil.d(TAG, "qos:" + qos);


            if (topic.contains("topic_s_mch")) {

                Map map_payload = JSON.parseObject(payload);

                String id = "";
                String method = "";
                String params = "";

                if (map_payload.containsKey("id")) {
                    Object obj_id = map_payload.get("id");
                    if (obj_id != null) {
                        id = obj_id.toString();
                    }
                }

                if (map_payload.containsKey("method")) {
                    Object obj_method = map_payload.get("method");
                    if (obj_method != null) {
                        method = obj_method.toString();
                    }
                }

                if (map_payload.containsKey("params")) {
                    Object obj_params = map_payload.get("params");
                    if (obj_params != null) {
                        params = obj_params.toString();
                    }
                }

                publish(id, "msg_arrive", null, 1);


                CommandManager.Execute(id, method, params);


            }

//                BaseSyncTask task = new BaseSyncTask() {
//                    @Override
//                    public void doTask() {
//
//                        LogUtil.d(TAG,"DDD");
//
//                        Activity activity = AppManager.getAppManager().currentActivity();
//                        if(activity!=null){
//                            if (activity instanceof OrderDetailsActivity) {
//                                LogUtil.d(TAG,"有订单正在执行");
//                                return;
//                            }
//                        }
//
//
//                        Intent intent = new Intent(getApplicationContext(), OrderDetailsActivity.class);
//                        Bundle bundle = new Bundle();
//                        OrderDetailsBean orderDetails = new OrderDetailsBean();
//                        orderDetails.setOrderId("dadsdsad");
//                        orderDetails.setStatus(10000);
//                        //orderDetails.setSkus(bean.getSkus());
//                        bundle.putSerializable("dataBean", orderDetails);
//
//                        intent.putExtras(bundle);
//                        startActivity(intent);
//
//
//                        //TinySyncExecutor.getInstance().finish();
//                    }
//                };
//
//                TinySyncExecutor.getInstance().enqueue(task);
//
//            }


        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken token) {

        }
    };

   // final private String PRODUCTKEY = "a1A2Mq6w5lN";
   // private String DEVICENAME = "test";//设备名
   // final private String DEVICESECRET = "445ee6df957de4fdcba1028025c619ec";//设备密钥

    private void buildMqttClient() {

        closeMqttClient();

        DeviceBean device = AppCacheManager.getDevice();

        MqttBean mqtt = device.getMqtt();

        if (mqtt != null) {

            host = mqtt.getHost();
            userName = mqtt.getUserName();
            password = mqtt.getPassword();
            clientId = mqtt.getClientId();


            //HOST = "tcp://" + PRODUCTKEY + ".iot-as-mqtt.cn-shanghai.aliyuncs.com:443";

//            /* 获取MQTT连接信息clientId、username、password。 */
//            AiotMqttOption aiotMqttOption = new AiotMqttOption().getMqttOption(PRODUCTKEY, DEVICENAME, DEVICESECRET);
//            if (aiotMqttOption == null) {
//                LogUtil.d(TAG, "device info error");
//            } else {
//                CLIENT_ID = aiotMqttOption.getClientId();
//                USERNAME = aiotMqttOption.getUsername();
//                PASSWORD = aiotMqttOption.getPassword();
//            }

        }

        topic_Subscribe = "/topic_s_mch/" + clientId;
        topic_Pubish = "/topic_p_mch/" + clientId;


        //topic_Subscribe = "/" + productName + "/" + deviceName + "/user/get";
        //topic_Pubish = "/" + productName + "/" + deviceName + "/user/update";
        //topic_Response = "/" + productName + "/" + deviceName + "/user/get";

        mqttAndroidClient = new MqttAndroidClient(getApplicationContext(), host, clientId);

        mMqttConnectOptions = new MqttConnectOptions();
        // 在重新启动和重新连接时记住状态
        //mMqttConnectOptions.setCleanSession(true);
        // 设置连接的用户名
        mMqttConnectOptions.setUserName(userName);
        // 设置密码connect-onFailure-java
        mMqttConnectOptions.setPassword(password.toCharArray());
        // 设置超时时间，单位：秒
        //mMqttConnectOptions.setConnectionTimeout(10);
        // 心跳包发送间隔，单位：秒
        //mMqttConnectOptions.setKeepAliveInterval(20);
        //mMqttConnectOptions.setAutomaticReconnect(true);


        mqttAndroidClient.setCallback(mqttCallback);// 回调

        connectMqttClient();

    }

    private synchronized void connectMqttClient() {
        if (!mqttAndroidClient.isConnected()) {
            try {
                mqttAndroidClient.connect(mMqttConnectOptions, getApplicationContext(), mqttActionListener);
                LogUtil.d(TAG,"连接中，ClientId："+mqttAndroidClient.getClientId());
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
    }

    private synchronized void startReconnectTask(){
        LogUtil.d(TAG,"开启重连任务");
        if (reconnectPool != null)return;
        reconnectPool = Executors.newScheduledThreadPool(1);
        reconnectPool.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                connectMqttClient();
            }
        } , 0 , 5*1000 , TimeUnit.MILLISECONDS);
    }

    private synchronized void closeReconnectTask(){
        LogUtil.d(TAG,"关闭重连任务");
        if (reconnectPool != null) {
            reconnectPool.shutdownNow();
            reconnectPool = null;
        }
    }

    public void closeMqttClient(){
        LogUtil.d(TAG,"关闭连接");
        closeReconnectTask();
        if (mqttAndroidClient != null){
            try {
                mqttAndroidClient.unregisterResources();
                mqttAndroidClient.disconnect();
                LogUtil.i(TAG,"关闭连接，ClientId："+mqttAndroidClient.getClientId());
                mqttAndroidClient = null;
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
    }

    public static void publish(String payload,int qos) {
        String topic = topic_Pubish;
        Boolean retained = false;
        try {
            if (mqttAndroidClient != null) {
                if (mqttAndroidClient.isConnected()) {
                    mqttAndroidClient.publish(topic, payload.getBytes(), qos, retained);
                }
            }
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public static void publish(String id, String method,JSONObject params, int qos) {

        //UUID.randomUUID().toString().replace("-","")

        JSONObject obj_Payload = new JSONObject();

        try {
            obj_Payload.put("id", id);
            obj_Payload.put("method", method);
            obj_Payload.put("params",params);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        String str_Payload=obj_Payload.toString();

        publish(str_Payload,qos);

    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            closeMqttClient();
            if(timHandler!=null&&timRunable!=null) {
                timHandler.removeCallbacks(timRunable);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
