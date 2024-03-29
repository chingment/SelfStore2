package com.uplink.selfstore.service;

import java.math.BigInteger;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class AiotMqttOption {
    private String username = "";
    private String password = "";
    private String clientId = "";

    public String getUsername() { return this.username;}
    public String getPassword() { return this.password;}
    public String getClientId() { return this.clientId;}

    /**
     * 鑾峰彇Mqtt寤鸿繛閫夐」瀵硅薄
     * @param productKey 浜у搧绉橀挜
     * @param deviceName 璁惧鍚嶇О
     * @param deviceSecret 璁惧鏈哄瘑
     * @return AiotMqttOption瀵硅薄鎴栬€匩ULL
     */
    public AiotMqttOption getMqttOption(String productKey, String deviceName, String deviceSecret) {
        if (productKey == null || deviceName == null || deviceSecret == null) {
            return null;
        }

        try {
            String timestamp = Long.toString(System.currentTimeMillis());

            // clientId
            this.clientId = productKey + "." + deviceName + "|timestamp=" + timestamp +
                    ",_v=paho-android-1.0.0,securemode=2,signmethod=hmacsha256|";

            // userName
            this.username = deviceName + "&" + productKey;

            // password
            String macSrc = "clientId" + productKey + "." + deviceName + "deviceName" +
                    deviceName + "productKey" + productKey + "timestamp" + timestamp;
            String algorithm = "HmacSHA256";
            Mac mac = Mac.getInstance(algorithm);
            SecretKeySpec secretKeySpec = new SecretKeySpec(deviceSecret.getBytes(), algorithm);
            mac.init(secretKeySpec);
            byte[] macRes = mac.doFinal(macSrc.getBytes());
            password = String.format("%064x", new BigInteger(1, macRes));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return this;
    }
}