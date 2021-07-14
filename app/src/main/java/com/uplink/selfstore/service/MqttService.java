package com.uplink.selfstore.service;

import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.JSONToken;
import com.uplink.selfstore.model.api.DeviceBean;
import com.uplink.selfstore.model.api.MqttBean;
import com.uplink.selfstore.own.AppCacheManager;
import com.uplink.selfstore.own.AppManager;
import com.uplink.selfstore.own.CommandManager;
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

import java.util.Dictionary;
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


    private String deviceName="";
    private String deviceClass="";

    private static String topic_Subscribe="";//订阅主题
    private static String topic_Pubish="";//发布主题


    private Handler timHandler = new Handler();

    private Runnable timRunable = new Runnable() {
        @Override
        public void run() {
            sendDeviceStatus();
            timHandler.postDelayed(this, 5 * 1000);
        }
    };

    private Handler handler_msg;

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


        handler_msg = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                Bundle bundle=msg.getData();
                String topic = bundle.getString("topic", "");
                String payload = bundle.getString("payload", "");

                LogUtil.d(TAG, "topic:" + topic);
                LogUtil.d(TAG, "payload:" + payload);

                if (topic.contains("/user/get")) {

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

                return  false;
            }
        });

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

            final Message m = new Message();
            m.what = 1;
            Bundle bundle = new Bundle();
            bundle.putString("topic", topic);
            bundle.putString("payload", payload);
            m.setData(bundle);
            handler_msg.sendMessage(m);

            //int qos = message.getQos();

            //LogUtil.d(TAG, "topic:" + topic);
            //LogUtil.d(TAG, "payload:" + payload);
            //LogUtil.d(TAG, "qos:" + qos);


        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken token) {
            try {

                MqttMessage msg=token.getMessage();

                String payload=new String(msg.getPayload());
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

                //LogUtil.d(TAG,"delivery.id:"+id);
                //LogUtil.d(TAG,"delivery.method:"+id);
                //LogUtil.d(TAG,"delivery.params:"+params);
                //LogUtil.d(TAG,"delivery.isComplete---------"+token.isComplete());

            }
            catch (Exception ex){

            }
        }
    };

   // final private String PRODUCTKEY = "a1A2Mq6w5lN";
   // private String DEVICENAME = "test";//设备名
   // final private String DEVICESECRET = "445ee6df957de4fdcba1028025c619ec";//设备密钥

    private void buildMqttClient() {

        closeMqttClient();

        DeviceBean device = AppCacheManager.getDevice();

        MqttBean mqtt = device.getMqtt();

        String clientId="";
        String host = "";
        String userName = "";
        String password = "";

        if (mqtt != null) {
            if(mqtt.getType().equals("exmq")) {
                com.alibaba.fastjson.JSONObject pms = (com.alibaba.fastjson.JSONObject) mqtt.getParams();
                host = pms.getString("host");
                userName = pms.getString("userName");
                password = pms.getString("password");
                clientId = pms.getString("clientId");
                deviceClass=pms.getString("deviceClass");
                deviceName=pms.getString("clientId");
            }
            else if(mqtt.getType().equals("almq")) {
                com.alibaba.fastjson.JSONObject pms = (com.alibaba.fastjson.JSONObject) mqtt.getParams();
                String productKey = pms.getString("productKey");
                deviceClass = productKey;
                deviceName = pms.getString("deviceName");
                String deviceSecret = pms.getString("deviceSecret");

                host = "tcp://" + productKey + ".iot-as-mqtt.cn-shanghai.aliyuncs.com:443";

                AiotMqttOption aiotMqttOption = new AiotMqttOption().getMqttOption(productKey, deviceName, deviceSecret);

                userName = aiotMqttOption.getUsername();
                password = aiotMqttOption.getPassword();
                clientId = aiotMqttOption.getClientId();
            }

        }

        topic_Subscribe ="/" + deviceClass + "/" + deviceName + "/user/get";
        topic_Pubish = "/" + deviceClass + "/" + deviceName + "/user/update";


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
