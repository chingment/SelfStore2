package com.uplink.selfstore.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

import android.content.Context;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class LocationUtil2 {
    public String cityName;
    // 此对象能通过经纬度来获取相应的城市等信息
    private Geocoder geocoder;
    private LocationManager locationManager;
    private String provider;
    private Location location;
    private Context context;
    /*
     * 通过地理坐标获取城市名 其中CN分别是city和name的首字母缩写
     */
    public String getCNBylocation(Context context) {
        geocoder = new Geocoder(context);
        String serviceName = Context.LOCATION_SERVICE;
        // 实例化一个LocationManager对象
        locationManager = (LocationManager) context.getSystemService(serviceName);
        // provider的类型
        // String provider = LocationManager.NETWORK_PROVIDER;
        getProvider();
        //openGPS();
        // 通过最后一次的地理位置来获得Location对象
        location = locationManager.getLastKnownLocation(provider);
        if(location == null){
            locationManager.requestLocationUpdates("gps", 60000, 1, locationListener);
        }
        String queryed_name = updateWithNewLocation(location);
        if ((queryed_name != null) && (0 != queryed_name.length())) {
            cityName = queryed_name;
        }
        /**
         * 第二个参数表示更新的周期，单位为毫秒；第三个参数表示最小距离间隔，单位是米 设定每30秒进行一次自动定位
         */
        locationManager.requestLocationUpdates(provider, 30000, 50,
                locationListener);
        return cityName;
    }

    /**
     * 方位改变时触发，进行调用
     */
    private final LocationListener locationListener = new LocationListener() {
        String tempCityName;

        public void onLocationChanged(Location location) {
            tempCityName = updateWithNewLocation(location);
            if ((tempCityName != null) && (tempCityName.length() != 0)) {
                cityName = tempCityName;
            }
        }

        public void onProviderDisabled(String provider) {
            tempCityName = updateWithNewLocation(null);
            if ((tempCityName != null) && (tempCityName.length() != 0)) {
                cityName = tempCityName;
            }
        }
        public void onProviderEnabled(String provider) {
        }
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    };

    /**
     * 更新location
     */
    private String updateWithNewLocation(Location location1) {
        String mcityName = "";
        double lat = 0;
        double lng = 0;
        List<Address> addList = null;
        if (location1 != null) {
            lat = location1.getLatitude();
            lng = location1.getLongitude();
        } else {
            System.out.println("无法获取地理信息");
        }
        try {
            addList = geocoder.getFromLocation(lat, lng, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (addList != null && addList.size() > 0) {
            for (int i = 0; i < addList.size(); i++) {
                Address add = addList.get(i);
                mcityName += add.getLocality();
            }
        }
        if (mcityName.length() != 0) {
            return mcityName.substring(0, (mcityName.length() - 1));
        } else {
            return mcityName;
        }
    }

    /**
     * 通过经纬度获取地址信息的另一种方法
     */
    public String GetAddr(String latitude, String longitude) {
        String addr = "";
		/*
		 * 也可以是http://maps.google.cn/maps/geo?output=csv&key=abcdef&q=%s,%s，
		 * 不过解析出来的是英文地址 密钥可以随便写一个key=abc
		 * output=csv,也可以是xml或json，不过使用csv返回的数据最简洁方便解析
		 */
        String url = String.format(
                "http://maps.google.cn/maps/geo?output=csv&key=abcdef&q=%s,%s",
                latitude, longitude);
        URL myURL = null;
        URLConnection httpsConn = null;
        try {
            myURL = new URL(url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }
        try {
            httpsConn = (URLConnection) myURL.openConnection();
            if (httpsConn != null) {
                InputStreamReader insr = new InputStreamReader(
                        httpsConn.getInputStream(), "UTF-8");
                BufferedReader br = new BufferedReader(insr);
                String data = null;
                if ((data = br.readLine()) != null) {
                    String[] retList = data.split(",");
                    if (retList.length > 2 && ("200".equals(retList[0]))) {
                        addr = retList[2];
                    } else {
                        addr = "";
                    }
                }
                insr.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return addr;
    }

    private void openGPS() {

        if (locationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(android.location.LocationManager.NETWORK_PROVIDER)
                ){
            Toast.makeText(context, " 位置源已设置！ ", Toast.LENGTH_SHORT).show();
            return;
        }
        Toast.makeText(context, " 位置源未设置！", Toast.LENGTH_SHORT).show();
    }

    private void getProvider() {
        // TODO Auto-generated method stub
        // 构建位置查询条件
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);  //高精度
        criteria.setAltitudeRequired(false);  //不查询海拔
        criteria.setBearingRequired(false);  //不查询方位
        criteria.setCostAllowed(true);  //不允许付费
        criteria.setPowerRequirement(Criteria.POWER_LOW);  //低耗
        // 返回最合适的符合条件的 provider ，第 2 个参数为 true 说明 , 如果只有一个 provider 是有效的 , 则返回当前  provider
        provider = locationManager.getBestProvider(criteria, true);
    }
}

