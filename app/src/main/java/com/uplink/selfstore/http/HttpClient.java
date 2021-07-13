package com.uplink.selfstore.http;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.text.TextUtils;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.uplink.selfstore.BuildConfig;
import com.uplink.selfstore.R;
import com.uplink.selfstore.own.AppCacheManager;
import com.uplink.selfstore.own.AppContext;
import com.uplink.selfstore.own.Config;
import com.uplink.selfstore.utils.LogUtil;
import com.uplink.selfstore.utils.AbFileUtil;
import com.uplink.selfstore.utils.ToastUtil;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by tiansj on 15/2/27.
 */
public class HttpClient {

    private static final int CONNECT_TIME_OUT = 10;
    private static final int WRITE_TIME_OUT = 60;
    private static final int READ_TIME_OUT = 60;
    private static final int MAX_REQUESTS_PER_HOST = 10;
    private static final String TAG = HttpClient.class.getSimpleName();
    private static final String UTF_8 = "UTF-8";
    private static OkHttpClient client;
    private static final MediaType MediaType_JSON = MediaType.parse("application/json; charset=utf-8");

    static {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.connectTimeout(CONNECT_TIME_OUT, TimeUnit.SECONDS);
        builder.writeTimeout(WRITE_TIME_OUT, TimeUnit.SECONDS);
        builder.readTimeout(READ_TIME_OUT, TimeUnit.SECONDS);
        builder.networkInterceptors().add(new LoggingInterceptor());
        builder.addInterceptor((new RetryIntercepter(3)));
        client = builder.build();
        client.dispatcher().setMaxRequestsPerHost(MAX_REQUESTS_PER_HOST);
    }

    /**
     * LoggingInterceptor
     */
    static class LoggingInterceptor implements Interceptor {
        @Override
        public Response intercept(Interceptor.Chain chain) throws IOException {
            Request request = chain.request();

            //long t1 = System.nanoTime();
            //LogUtil.i(TAG, String.format("Sending request %s on %s%n%s",
            //request.url(), chain.connection(), request.headers()));

            Response response = chain.proceed(request);

            //long t2 = System.nanoTime();
            //LogUtil.i(TAG, String.format("Received response for %s in %.1fms%n%s",
            //response.request().url(), (t2 - t1) / 1e6d, response.headers()));
            return response;
        }
    }

    static class RetryIntercepter implements Interceptor {

        public int maxRetry;//最大重试次数
        private int retryNum = 0;//假如设置为3次重试的话，则最大可能请求4次（默认1次+3次重试）

        public RetryIntercepter(int maxRetry) {
            this.maxRetry = maxRetry;
        }

        @Override
        public Response intercept(Chain chain) throws IOException {
            Request request = chain.request();
            System.out.println("retryNum=" + retryNum);
            Response response = chain.proceed(request);
            LogUtil.d("response.isSuccessful():"+response.isSuccessful());
            while (!response.isSuccessful() && retryNum < maxRetry) {
                retryNum++;
                System.out.println("retryNum=" + retryNum);
                response = chain.proceed(request);
            }
            return response;
        }
    }

    /**
     * 网络判断
     *
     * @return
     */
    public static boolean isNetworkAvailable() {
        try {
            ConnectivityManager connectivityManager = (ConnectivityManager) AppContext.getInstance().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            return networkInfo != null && networkInfo.isAvailable() && networkInfo.isConnected();
        } catch (Exception e) {
            LogUtil.v("ConnectivityManager", e.getMessage());
        }
        return false;
    }


    public static void postByMy(String url, Map<String, Object> params, Map<String, String> filePaths, final HttpResponseHandler handler) {

        try
        {
            if (!isNetworkAvailable()) {
                handler.sendFailureMessage("网络连接不可用,请检查设置",null);
                return;
            }

            if(handler!=null) {
                handler.sendBeforeSendMessage();
            }

            JSONObject json = new JSONObject();
            try {
                if(params!=null) {
                    for (Map.Entry<String, Object> entry : params.entrySet()) {
                        json.put(entry.getKey(), entry.getValue());
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
                if(handler!=null) {
                    handler.sendFailureMessage("提交数据转换发生异常", null);
                }
                return;
            }


            Request.Builder requestBuilder = new Request.Builder().url(url);

            requestBuilder.addHeader("appId", "" + BuildConfig.APPLICATION_ID);
            requestBuilder.addHeader("appKey", "" + BuildConfig.APPKEY);
            String currenttime = (System.currentTimeMillis() / 1000) + "";
            requestBuilder.addHeader("timestamp", currenttime);
            String sign = Config.getSign(BuildConfig.APPLICATION_ID, BuildConfig.APPKEY, BuildConfig.APPSECRET, json.toString(), currenttime);
            requestBuilder.addHeader("sign", "" + sign);
            requestBuilder.addHeader("version", com.uplink.selfstore.BuildConfig.VERSION_NAME);

            if(AppCacheManager.getOpUserInfo()!=null) {
                requestBuilder.addHeader("X-Token", "" + AppCacheManager.getOpUserInfo().getToken());
            }

            try {
                JSONObject jsonImgData = new JSONObject();
                if (filePaths != null) {
                    if (filePaths.size() > 0) {
                        for (Map.Entry<String, String> entry : filePaths.entrySet()) {
                            JSONObject jsonImgItem = new JSONObject();
                            String filePath = entry.getValue();
                            LogUtil.e(TAG, "filePath>>==>>" + filePath);
                            jsonImgItem.put("type", filePath.substring(filePath.lastIndexOf(".")));
                            String base64ImgStr = AbFileUtil.GetBase64ImageStr(filePath);
                            LogUtil.e(TAG, "filePath>>==>>" + base64ImgStr.length());
                            jsonImgItem.put("data", base64ImgStr);
                            jsonImgData.put(entry.getKey(), jsonImgItem);
                        }
                        json.put("imgData", jsonImgData);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
                if(handler!=null) {
                    handler.handleFailureMessage("提交数据转换发生异常", e);
                }
                return;
            }

            String data = json.toString();

            LogUtil.i("Request.url:" + url);
            LogUtil.i("Request.postData:" + data);


            RequestBody body = RequestBody.create(MediaType_JSON, data);

            requestBuilder.post(body);


            client.newCall(requestBuilder.build()).enqueue(new Callback() {
                @Override
                public void onResponse(Call call, Response response) {

                    try {
                        String body = response.body().string();
                        LogUtil.i(TAG, "Request.onSuccess====>>>" + body);
                        if(handler!=null) {
                            handler.sendSuccessMessage(body);
                        }

                    } catch (Exception e) {
                        LogUtil.i(TAG, "Request.onFailure====>>>" + response);
                        if(handler!=null) {
                            handler.sendFailureMessage("读取数据发生异常", e);
                        }
                    }

                }

                @Override
                public void onFailure(Call call, IOException e) {
                    String msg = "读取数据服务发生异常";

                    if (e instanceof SocketTimeoutException) {
                        msg = "读取数据服务连接超时";
                    } else if (e instanceof ConnectException) {
                        msg = "读取数据服务连接失败";

                    } else if (e instanceof UnknownHostException) {
                        msg = "读取数据服务网络异常或连接不存在";
                    }

                    if(handler!=null) {
                        handler.sendFailureMessage(msg, e);
                    }
                }
            });
        } catch (Exception ex) {
            LogUtil.e(TAG,ex);
            if(handler!=null) {
                handler.sendFailureMessage("数据提交发生异常", ex);
            }
        }
    }

    public static void postFileByMy(String url, Map<String, String> fields, Map<String, String> filePaths, final HttpResponseHandler handler) {

        try {
            if (!isNetworkAvailable()) {
                if(handler!=null) {
                    handler.sendFailureMessage("网络连接不可用,请检查设置", null);
                }
                return;
            }

            if (handler != null) {
                handler.sendBeforeSendMessage();
            }



            Request.Builder requestBuilder = new Request.Builder().url(url);

            MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);



            String fields_data="";
            if (fields != null) {
                if (fields.size() > 0) {
                    for (Map.Entry<String, String> entry : fields.entrySet()) {
                        builder.addFormDataPart(entry.getKey(), entry.getValue());
                    }

                    fields_data = mapToQueryString(fields);
                }
            }

            if (filePaths == null)
                return;

            if (filePaths.size() < 1)
                return;


            for (Map.Entry<String, String> entry : filePaths.entrySet()) {

                File file = new File( entry.getValue());
                String fileName = file.getName();
                RequestBody fileBody = RequestBody.create(MediaType.parse("image/png"), file);
                builder.addFormDataPart(entry.getKey(), fileName, fileBody);
            }


            RequestBody requestBody = builder.build();

            requestBuilder.post(requestBody);

            requestBuilder.addHeader("appId", "" + BuildConfig.APPLICATION_ID);
            requestBuilder.addHeader("appKey", "" + BuildConfig.APPKEY);
            String currenttime = (System.currentTimeMillis() / 1000) + "";
            requestBuilder.addHeader("timestamp", currenttime);
            String sign = Config.getSign(BuildConfig.APPLICATION_ID,BuildConfig.APPKEY, BuildConfig.APPSECRET, fields_data, currenttime);
            requestBuilder.addHeader("sign", "" + sign);
            requestBuilder.addHeader("version", com.uplink.selfstore.BuildConfig.VERSION_NAME);


            client.newCall(requestBuilder.build()).enqueue(new Callback() {
                @Override
                public void onResponse(Call call, Response response) {

                    try {
                        String body = response.body().string();
                        LogUtil.i(TAG, "Request.onSuccess====>>>" + body);
                        if (handler != null) {
                            handler.sendSuccessMessage(body);
                        }

                    } catch (Exception e) {
                        LogUtil.i(TAG, "Request.onFailure====>>>" + response);
                        if (handler != null) {
                            handler.sendFailureMessage("读取数据发生异常", e);
                        }
                    }

                }

                @Override
                public void onFailure(Call call, IOException e) {
                    String msg = "读取数据服务发生异常";

                    if (e instanceof SocketTimeoutException) {
                        msg = "读取数据服务连接超时";
                    } else if (e instanceof ConnectException) {
                        msg = "读取数据服务连接失败";

                    } else if (e instanceof UnknownHostException) {
                        msg = "读取数据服务网络异常或连接不存在";
                    }

                    if (handler != null) {
                        handler.sendFailureMessage(msg, e);
                    }
                }
            });
        } catch (Exception ex) {
            if (handler != null) {
                handler.sendFailureMessage("数据提交发生异常", ex);
            }
        }
    }

    public static String mapToQueryString(Map<String, String> map) {
        StringBuilder string = new StringBuilder();
        /*if(map.size() > 0) {
            string.append("?");
        }*/
        try {
            for (Map.Entry<String, String> entry : map.entrySet()) {
                string.append(entry.getKey());
                string.append("=");
                string.append(URLEncoder.encode(entry.getValue(), UTF_8));
                string.append("&");
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return string.toString().substring(0, string.length() - 1);
    }

}
