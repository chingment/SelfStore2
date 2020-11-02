package com.uplink.selfstore.service;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;

import com.alibaba.fastjson.JSON;
import com.uplink.selfstore.model.api.MachineBean;
import com.uplink.selfstore.model.api.MqttBean;
import com.uplink.selfstore.own.AppCacheManager;
import com.uplink.selfstore.own.AppManager;
import com.uplink.selfstore.own.PushUpdateUtil;
import com.uplink.selfstore.utils.LogUtil;

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


public class MqttServer extends Service {

    private static final String TAG = "MqttServer";

    private static MqttAndroidClient mqttAndroidClient;
    private MqttConnectOptions mMqttConnectOptions;
    public String HOST = "tcp://112.74.179.185:1883";//服务器地址（协议+地址+端口号）
    //public String HOST = "tcp://120.24.57.24:1883";//服务器地址（协议+地址+端口号）
    public String USERNAME = "admin";//用户名
    public String PASSWORD = "public";//密码

    public static String SUBSCRIBE_TOPIC_A = "";//订阅主题
    public static String PUBLISH_TOPIC_A = "";//发布主题
    public static String RESPONSE_TOPIC_A = "";//响应主题
    public String CLIENT_ID = "";

    private Handler timHandler = new Handler();

    private Runnable timRunable = new Runnable() {
        @Override
        public void run() {

            LogUtil.d(TAG,"正在执行发送机器状态");

            MachineBean machine = AppCacheManager.getMachine();

            String status = "unknow";
            String activityName="";
            Activity activity = AppManager.getAppManager().currentActivity();
            if (activity != null) {
                activityName = activity.getLocalClassName();
                if (activityName.contains(".Sm")) {
                    status = "setting";
                } else {
                    if (machine.isExIsHas()) {
                        status = "exception";
                    } else {
                        status = "running";
                    }
                }
            }

            JSONObject msg = new JSONObject();

            JSONObject msg_content = new JSONObject();
            try {

                msg.put("msg_id", UUID.randomUUID().toString().replace("-",""));
                msg.put("type", "machine_status");
                msg.put("status", "0");

                msg_content.put("machineId",machine.getMachineId());
                msg_content.put("status", status);
                msg_content.put("activity", activityName);

                msg.put("content",msg_content);


            } catch (JSONException e) {
                e.printStackTrace();
                return;
            }

            String str_msg=msg.toString();

            publish(str_msg);

            timHandler.postDelayed(this, 5*1000);
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();

        connectionMQTTServer();

    }


    /**
     * 进行连接操作
     */

    private void connectionMQTTServer() { // 连接操作

        MachineBean machine = AppCacheManager.getMachine();

        CLIENT_ID = "mch_" + machine.getMachineId();

        MqttBean mqtt=machine.getMqtt();

//        if(mqtt!=null) {
//            HOST = mqtt.getHost();
//            USERNAME = mqtt.getUserName();
//            PASSWORD = mqtt.getPassword();
//        }

        SUBSCRIBE_TOPIC_A = "topic_s_mch/" + machine.getMachineId();//订阅主题
        PUBLISH_TOPIC_A = "topic_p_mch/" + machine.getMachineId();//发布主题
        RESPONSE_TOPIC_A = "topic_r_mch/" + machine.getMachineId();//响应主题


        mqttAndroidClient = new MqttAndroidClient(this, HOST, CLIENT_ID);

        mMqttConnectOptions = new MqttConnectOptions();
        // 在重新启动和重新连接时记住状态
        mMqttConnectOptions.setCleanSession(true);
        // 设置连接的用户名
        mMqttConnectOptions.setUserName(USERNAME);
        // 设置密码
        mMqttConnectOptions.setPassword(PASSWORD.toCharArray());
        // 设置超时时间，单位：秒
        mMqttConnectOptions.setConnectionTimeout(10);
        // 心跳包发送间隔，单位：秒
        mMqttConnectOptions.setKeepAliveInterval(20);
        //设置服务质量
        MqttMessage message = new MqttMessage("PayLoad".getBytes());
        message.setQos(1);

        mqttAndroidClient.setCallback(mqttCallback);// 回调

        new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    //进行服务器连接
                    /***
                     * mMqttConnectOptions MQTT设置
                     * iMqttActionListener MQTT连接监听
                     */
                    mqttAndroidClient.connect(mMqttConnectOptions, null, new IMqttActionListener() {
                        @Override
                        public void onSuccess(IMqttToken asyncActionToken) { // 连接成功

                            LogUtil.d(TAG, "连接成功");

                            try {
                                //订阅公有主题和私有主题
                                mqttAndroidClient.subscribe(SUBSCRIBE_TOPIC_A, 1);//订阅主题，参数：主题、服务质量
                            } catch (MqttException e) {
                                e.printStackTrace();
                            }


                            if(timHandler!=null&&timRunable!=null) {
                                timHandler.removeCallbacks(timRunable);
                            }

                            //主线程中调用：
                            timHandler.postDelayed(timRunable, 1000);//延时100毫秒
                        }

                        @Override
                        public void onFailure(IMqttToken asyncActionToken, Throwable exception) { //连接失败
                            LogUtil.d(TAG, "连接失败！正在重新连接");

                            exception.printStackTrace();
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    connectionMQTTServer(); // ReConnection
                                }
                            }, 5000);   //延时5秒重新连接MQTT服务器
                        }
                    });

                } catch (MqttException e) {
                    LogUtil.d(TAG, "连接异常");
                    e.fillInStackTrace();
                }
            }
        }).run();


    }


    private MqttCallback mqttCallback = new MqttCallbackExtended() {  //回传
        @Override
        public void connectComplete(boolean reconnect, String serverURI) {
            /**
             *与服务器的连接成功完成时调用。
             * @param reconnect如果为true，则连接是自动重新连接的结果。
             * @param serverURI建立连接的服务器URI。
             **/

        }

        @Override
        public void connectionLost(Throwable cause) {
            LogUtil.d(TAG,"连接断开");
            connectionMQTTServer(); // ReConnection
        }

        @Override
        public void messageArrived(String topic, MqttMessage message) throws Exception {  // 接收的消息

            String msg = new String(message.getPayload());

            LogUtil.d(TAG, "主题:" + topic + ",内容:" + msg);

            if (topic.contains("topic_s_mch")) {

                Map msg_map = JSON.parseObject(msg);

                String msg_id = "";
                String type = "";
                String content = "";
                if (msg_map.containsKey("msg_id")) {
                    msg_id = msg_map.get("msg_id").toString();
                }

                if (msg_map.containsKey("type")) {
                    type = msg_map.get("type").toString();
                }

                if (msg_map.containsKey("content")) {
                    content = msg_map.get("content").toString();
                }


                LogUtil.d(TAG, "=>msg_id:" + msg_id);
                LogUtil.d(TAG, "=>type:" + type);
                LogUtil.d(TAG, "=>content:" + content);


                PushUpdateUtil.receive(type, content);


                response("{\"msg_id\":\""+msg_id+"\",\"status\":1}");

            }

        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken token) {

        }
    };

    /**
     * 发布消息 publish(主题,消息的字节数组,服务质量,是否在服务器保留断开连接后的最后一条消息);
     *
     * @param message
     */

    /**
     * 将消息发布到服务器上的主题。
     * <p>
     * 一种方便的方法，它将创建一个新的{@link MqttMessage}对象
     * 具有字节数组有效负载和指定的QoS，然后将其发布。
     * </ p>
     *
     * @throws IllegalArgumentException 如果QoS的值不为0、1或2。
     * @param主题 将消息传递到例如“ finance / stock / ibm”。
     * @参数有效负载 用作有效载荷的字节数组
     * @参数qos 提供消息的服务质量。有效值是0、1或2。
     * @param保留 服务器是否应保留此消息。
     * @return令牌用于跟踪并等待发布完成。的 令牌将传递给已设置的任何回调。
     * @抛出MqttPersistenceException 发生问题时存储消息
     * @抛出MqttException 用于发布消息时遇到的其他错误。
     * 例如，正在处理太多消息。
     * @see #publish（String topic, byte[] payload, int qos,boolean retained）
     **/
    public static void publish(String message) {
        String topic = PUBLISH_TOPIC_A;
        Boolean retained = false;
        try {
            if (mqttAndroidClient != null) {
                if (mqttAndroidClient.isConnected()) {
                    //参数分别为：主题、消息的字节数组、服务质量、是否在服务器保留断开连接后的最后一条消息
                    mqttAndroidClient.publish(topic, message.getBytes(), 1, retained.booleanValue());
                }
            }
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }


    public void response(String message) {
        Integer qos = 1;
        Boolean retained = false;
        try {
            //参数分别为：主题、消息的字节数组、服务质量、是否在服务器保留断开连接后的最后一条消息
            mqttAndroidClient.publish(RESPONSE_TOPIC_A, message.getBytes(), qos.intValue(), retained.booleanValue());
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    /**
     * 将通信通道返回到服务。如果可能返回null
     * 客户端无法绑定到服务。返回的
     * {@link IBinder}通常用于复杂的界面
     * 已<a href="{@docRoot}guide/components/aidl.html">描述为aidl </a>。
     * <p> <em>请注意，与其他应用程序组件不同，调用
     * 此处返回的IBinder接口可能不会在主线程上发生
     * 的过程</ em>。有关主线程的更多信息，请参见
     * <a href="{@docRoot}guide/topics/fundamentals/processes-and-threads.html">流程和线程</a>。</ p>
     *
     * @param intent 用于绑定到此服务的Intent，
     *               如{@link Context＃bindServiceContext.bindService}。请注意，随附的所有其他功能此时的意图不会在这里显示。
     * @return返回IBinder，客户端可以通过该IBinder调用 服务。
     **/
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            mqttAndroidClient.disconnect();
            if(timHandler!=null&&timRunable!=null) {
                timHandler.removeCallbacks(timRunable);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
