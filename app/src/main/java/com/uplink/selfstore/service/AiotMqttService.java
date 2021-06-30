package com.uplink.selfstore.service;


import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

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

public class AiotMqttService extends Service {
    private static final String TAG = "AiotMqttService";
    final private String PRODUCTKEY = "a1A2Mq6w5lN";
    final private String DEVICENAME = "test";//设备名
    final private String DEVICESECRET = "445ee6df957de4fdcba1028025c619ec";//设备密钥
    private String HOST="";
    private String clientId="";
    private String userName="";
    private String passWord="";

    private static MqttAndroidClient mqttAndroidClient;
    private static MqttConnectOptions mqttConnectOptions;
    
    @Override
    public void onCreate() {
        super.onCreate();

        HOST="tcp://" + PRODUCTKEY + ".iot-as-mqtt.cn-shanghai.aliyuncs.com:443";

        /* 获取MQTT连接信息clientId、username、password。 */
        AiotMqttOption aiotMqttOption = new AiotMqttOption().getMqttOption(PRODUCTKEY, DEVICENAME, DEVICESECRET);
        if (aiotMqttOption == null) {
            LogUtil.d(TAG, "device info error");
        } else {
            clientId = aiotMqttOption.getClientId();
            userName = aiotMqttOption.getUsername();
            passWord = aiotMqttOption.getPassword();
        }

        /* 创建MqttConnectOptions对象，并配置username和password。 */
        mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setUserName(userName);
        mqttConnectOptions.setPassword(passWord.toCharArray());


        buildMqttClient();
        connectMqttClient();
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private void buildMqttClient(){

        mqttAndroidClient = new MqttAndroidClient(getApplicationContext(), HOST, clientId);
        mqttAndroidClient.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                LogUtil.d(TAG, "connection lost");
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                LogUtil.d(TAG,"topic: " + topic + ", msg: " + new String(message.getPayload()));
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                LogUtil.d(TAG,"msg delivered");
            }
        });

    }


    private void  connectMqttClient(){
        /* 建立MQTT连接。 */
        try {
            mqttAndroidClient.connect(mqttConnectOptions, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    LogUtil.d(TAG, "connect succeed");

                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    LogUtil.d(TAG, "connect failed,"+exception.getMessage());
                }
            });

        } catch (MqttException e) {
            e.printStackTrace();
        }
    }


    public void closeMqttClient(){
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            closeMqttClient();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
