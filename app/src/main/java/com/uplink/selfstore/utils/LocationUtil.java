package com.uplink.selfstore.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;

import java.util.List;

public class LocationUtil {

    private volatile static LocationUtil uniqueInstance;
    private String TAG="LocationUtils";
    private LocationManager locationManager;
    private String locationProvider;
    private Location location;
    private Context mContext;

    public  static double LAT=0;
    public  static double LNG=0;
    private LocationUtil(Context context) {
        mContext = context;
        laodLocation();
    }

    //采用Double CheckLock(DCL)实现单例
    public static LocationUtil getInstance(Context context) {
        if (uniqueInstance == null) {
            synchronized (LocationUtil.class) {
                if (uniqueInstance == null) {
                    uniqueInstance = new LocationUtil( context );
                }
            }
        }
        return uniqueInstance;
    }

    private void laodLocation() {
        try {
            //1.获取位置管理器
            locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
            //2.获取位置提供器，GPS或是NetWork
            List<String> providers = locationManager.getProviders(true);
            if (providers.contains(LocationManager.NETWORK_PROVIDER)) {
                //如果是网络定位
                LogUtil.d(TAG, "如果是网络定位");
                locationProvider = LocationManager.NETWORK_PROVIDER;
            } else if (providers.contains(LocationManager.GPS_PROVIDER)) {
                //如果是GPS定位
                LogUtil.d(TAG, "如果是GPS定位");
                locationProvider = LocationManager.GPS_PROVIDER;
            } else {
                LogUtil.d(TAG, "没有可用的位置提供器");
                return;
            }
            // 需要检查权限,否则编译报错,想抽取成方法都不行,还是会报错。只能这样重复 code 了。
            if (Build.VERSION.SDK_INT >= 23 &&
                    ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            //3.获取上次的位置，一般第一次运行，此值为null
            Location location = locationManager.getLastKnownLocation(locationProvider);
            if (location != null) {
                setLocation(location);
            }
            // 监视地理位置变化，第二个和第三个参数分别为更新的最短时间minTime和最短距离minDistace
            locationManager.requestLocationUpdates(locationProvider, 0, 0, locationListener);
        }
        catch (Exception ex){
            LogUtil.e(TAG, ex);
        }
    }

    private void setLocation(Location location) {
        if(location!=null) {
            this.location = location;
            LocationUtil.LAT = location.getLatitude();
            LocationUtil.LNG = location.getLongitude();
            String address = "纬度：" + location.getLatitude() + "经度：" + location.getLongitude();
            LogUtil.d(TAG, address);
        }
    }

    //获取经纬度
    public Location getLocation() {
        return location;
    }

    // 移除定位监听
    public void removeLocationUpdatesListener() {
        // 需要检查权限,否则编译不过
        if (Build.VERSION.SDK_INT >= 23 &&
                ActivityCompat.checkSelfPermission( mContext, Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission( mContext, Manifest.permission.ACCESS_COARSE_LOCATION ) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        if (locationManager != null) {
            uniqueInstance = null;
            locationManager.removeUpdates( locationListener );
        }
    }

    /**
     * LocationListern监听器
     * 参数：地理位置提供器、监听位置变化的时间间隔、位置变化的距离间隔、LocationListener监听器
     */

    LocationListener locationListener = new LocationListener() {

        /**
         * 当某个位置提供者的状态发生改变时
         */
        @Override
        public void onStatusChanged(String provider, int status, Bundle arg2) {

           // LogUtil.e(TAG,"1");
        }

        /**
         * 某个设备打开时
         */
        @Override
        public void onProviderEnabled(String provider) {

            //LogUtil.e(TAG,"2");
        }

        /**
         * 某个设备关闭时
         */
        @Override
        public void onProviderDisabled(String provider) {

            //LogUtil.e(TAG,"3");
        }

        /**
         * 手机位置发生变动
         */
        @Override
        public void onLocationChanged(Location location) {
            if(location!=null) {
                location.getAccuracy();//精确度
                setLocation(location);
            }
        }
    };

}