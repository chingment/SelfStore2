package com.uplink.selfstore.own;

import android.util.Base64;

import com.uplink.selfstore.*;
import com.uplink.selfstore.utils.SHA256Encrypt;
import com.uplink.selfstore.utils.StringUtil;


public class Config {
    public static final boolean IS_BUILD_DEBUG = BuildConfig.DEBUG;//打包模式
    public static final boolean IS_APP_DEBUG = BuildConfig.ISAPPDEBUG;//调试模式
    public static String getSign(String appId, String appKey, String appSecret, String data, String currenttime) {
        // 待加密
        String queryStr =appId+ appKey + appSecret + currenttime + data;
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
        public static final String machine_EventNotify = BuildConfig.ENVIRONMENT + "/api/Machine/EventNotify";
        public static final String machine_GetRunExHandleItems= BuildConfig.ENVIRONMENT + "/api/Machine/GetRunExHandleItems";
        public static final String machine_HandleRunExItems= BuildConfig.ENVIRONMENT + "/api/Machine/HandleRunExItems";
        public static final String order_Reserve = BuildConfig.ENVIRONMENT + "/api/Order/Reserve";
        public static final String order_BuildPayParams = BuildConfig.ENVIRONMENT + "/api/Order/BuildPayParams";
        public static final String order_Cancle = BuildConfig.ENVIRONMENT + "/api/Order/Cancle";
        public static final String order_PayStatusQuery = BuildConfig.ENVIRONMENT + "/api/Order/PayStatusQuery";
        public static final String order_SearchByPickupCode = BuildConfig.ENVIRONMENT + "/api/Order/SearchByPickupCode";
        public static final String stockSetting_GetCabinetSlots= BuildConfig.ENVIRONMENT + "/api/StockSetting/GetCabinetSlots";
        public static final String stockSetting_SaveCabinetSlot= BuildConfig.ENVIRONMENT + "/api/StockSetting/SaveCabinetSlot";
        public static final String stockSetting_SaveCabinetRowColLayout= BuildConfig.ENVIRONMENT + "/api/StockSetting/SaveCabinetRowColLayout";
        public static final String productSku_Search= BuildConfig.ENVIRONMENT + "/api/ProductSku/Search";
        public static final String own_LoginByAccount= BuildConfig.ENVIRONMENT + "/api/Own/LoginByAccount";
        public static final String own_LoginByFingerVein= BuildConfig.ENVIRONMENT + "/api/Own/LoginByFingerVein";
        public static final String own_Logout= BuildConfig.ENVIRONMENT + "/api/Own/Logout";
        public static final String own_GetInfo= BuildConfig.ENVIRONMENT + "/api/Own/GetInfo";
        public static final String own_UploadFingerVeinData= BuildConfig.ENVIRONMENT + "/api/Own/UploadFingerVeinData";
        public static final String own_DeleteFingerVeinData= BuildConfig.ENVIRONMENT + "/api/Own/DeleteFingerVeinData";
        public static final String uploadfile="http://upload.17fanju.com/api/upload";
        public static final String imservice_Seats= BuildConfig.ENVIRONMENT + "/api/ImService/Seats";
    }
}
