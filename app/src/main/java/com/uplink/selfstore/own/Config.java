package com.uplink.selfstore.own;

import android.util.Base64;

import com.uplink.selfstore.*;
import com.uplink.selfstore.utils.SHA256Encrypt;
import com.uplink.selfstore.utils.StringUtil;


public class Config {
    public static final boolean showDebug = BuildConfig.SHOWDEBUG;

    public static String getSign(String key, String secret, String data, String currenttime) {
        // 待加密
        String queryStr = key + secret + currenttime + data;
//        LogUtil.e(TAG, "queryStr>>==>>" + queryStr);
        String sortedStr = StringUtil.sortString(queryStr);
//        LogUtil.e(TAG, "sortedStr>>==>>" + sortedStr);
        String sha256edStr = SHA256Encrypt.bin2hex(sortedStr).toLowerCase();
//        LogUtil.e(TAG, "sha256edStr>>==>>" + sha256edStr);
        String base64Str = Base64.encodeToString(sha256edStr.getBytes(), Base64.NO_WRAP);
//        String base64Str = StringUtils.replaceEnter(Base64.encodeToString(sha256edStr.getBytes(), Base64.NO_WRAP), "");
//        LogUtil.e(TAG, "加密后>>==>>" + base64Str);
        return base64Str;
    }

    public class URL {
        public static final String machine_InitData= BuildConfig.ENVIRONMENT + "/api/machine/InitData";
        public static final String machine_CheckUpdate= BuildConfig.ENVIRONMENT + "/api/machine/CheckUpdate";
        public static final String order_Reserve = BuildConfig.ENVIRONMENT + "/api/Order/Reserve";
        public static final String order_BuildPayParams = BuildConfig.ENVIRONMENT + "/api/Order/BuildPayParams";
        public static final String order_Cancle = BuildConfig.ENVIRONMENT + "/api/Order/Cancle";
        public static final String order_PayStatusQuery = BuildConfig.ENVIRONMENT + "/api/Order/PayStatusQuery";
        public static final String order_PickupStatusQuery = BuildConfig.ENVIRONMENT + "/api/Order/PickupStatusQuery";
        public static final String order_PickupEventNotify = BuildConfig.ENVIRONMENT + "/api/Order/PickupEventNotify";
        public static final String order_Search = BuildConfig.ENVIRONMENT + "/api/Order/Search";
        public static final String order_Details = BuildConfig.ENVIRONMENT + "/api/Order/Details";
        public static final String order_PayUrlBuild = BuildConfig.ENVIRONMENT + "/api/Order/PayUrlBuild";
        public static final String machine_Login= BuildConfig.ENVIRONMENT + "/api/Machine/Login";
        public static final String machine_Logout= BuildConfig.ENVIRONMENT + "/api/Machine/Logout";
        public static final String machine_UpLoadTraceLog= BuildConfig.ENVIRONMENT + "/api/Machine/UpLoadTraceLog";
        public static final String stockSetting_GetCabinetSlots= BuildConfig.ENVIRONMENT + "/api/StockSetting/GetCabinetSlots";
        public static final String stockSetting_SaveCabinetSlot= BuildConfig.ENVIRONMENT + "/api/StockSetting/SaveCabinetSlot";
        public static final String stockSetting_SaveCabinetRowColLayout= BuildConfig.ENVIRONMENT + "/api/StockSetting/SaveCabinetRowColLayout";
        public static final String stockSetting_TestPickupEventNotify= BuildConfig.ENVIRONMENT + "/api/StockSetting/TestPickupEventNotify";
        public static final String machine_SendRunStatus = BuildConfig.ENVIRONMENT + "/api/Machine/SendRunStatus";
        public static final String productSku_Search= BuildConfig.ENVIRONMENT + "/api/ProductSku/Search";
    }
}
