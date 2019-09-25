package com.uplink.selfstore.utils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * 项目名称：Pro_selfstoreurance
 * 类描述：
 * 创建人：tuchg
 * 创建时间：17/1/10 03:29
 */

public class GsonUtil {



    public synchronized static <T> T getData(String jsonString, Class<T> cls) {

        T t = null;
        try {
            Gson gson = new Gson();
            t = gson.fromJson(jsonString, cls);
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
        return t;
    }

    /**
     * 获取List<bean>
     *
     * @param jsonString
     * @param cls
     * @return
     */
    public synchronized static <T> List<T> getListData(String jsonString, Class<T> cls) {
        List<T> list = new ArrayList<T>();
        try {
            Gson gson = new Gson();
            list = gson.fromJson(jsonString, new TypeToken<List<T>>() {
            }.getType());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * 获取Map<String Object>
     *
     * @param jsonString
     * @return
     */
    public synchronized static List<Map<String, Object>> getListMapDat(String jsonString) {
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        try {
            Gson gson = new Gson();
            list = gson.fromJson(jsonString, new TypeToken<List<Map<String, Object>>>() {
            }.getType());
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
        return list;
    }

    /**
     * 获取Map<String Object>
     *
     * @param jsonString
     * @return
     */
    public synchronized static Map<String, Object> getMapDat(String jsonString) {
        Map<String, Object> list = new HashMap<String, Object>();
        try {
            Gson gson = new Gson();
            list = gson.fromJson(jsonString, new TypeToken<Map<String, Object>>() {
            }.getType());
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
        return list;
    }


    /**
     * 解析 List<String>
     *
     * @param str
     * @return List<String>
     * @throws JSONException
     */
    public synchronized static List<String> parseImageList(String str) throws JSONException {
        List<String> data = new ArrayList<String>();
        JSONArray array = new JSONArray(str);
        for (int i = 0; i < array.length(); i++) {
            String object = array.get(i) + "";
            data.add(object);
        }
        return data;
    }

    /**
     * @param jsonObject
     * @return
     */
    public static String getValueFromKey(String jsonObject, String key) {
        // TODO Auto-generated method stub
        if (StringUtil.isEmpty(jsonObject)) {
            return "";
        }
        try {
            JSONObject json = new JSONObject(jsonObject);
            return json.getString(key);
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return "";
        }
    }

    public static int getResultCode(String response) {
        // TODO Auto-generated method stub
        if (StringUtil.isEmpty(response)) {
            return -1;
        }
        try {
            JSONObject json = new JSONObject(response);
            return json.getInt("result");
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return -1;
        }
    }

    public static String getMessage(String response, String defalut_str) {
        // TODO Auto-generated method stub
        if (StringUtil.isEmpty(response)) {
            return defalut_str;
        }
        try {
            JSONObject json = new JSONObject(response);
            return json.getString("message");
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return defalut_str;
        }
    }
}
