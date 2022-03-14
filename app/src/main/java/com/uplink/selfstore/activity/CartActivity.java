package com.uplink.selfstore.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.hyphenate.EMCallBack;
import com.hyphenate.EMMessageListener;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMCustomMessageBody;
import com.hyphenate.chat.EMMessage;
import com.uplink.selfstore.R;
import com.uplink.selfstore.activity.adapter.CartSkuAdapter;
import com.uplink.selfstore.activity.adapter.ImSeatAdapter;
import com.uplink.selfstore.activity.handler.CarOperateHandler;
import com.uplink.selfstore.http.HttpResponseHandler;
import com.uplink.selfstore.model.api.ApiResultBean;
import com.uplink.selfstore.model.api.CartSkuBean;
import com.uplink.selfstore.model.api.CartOperateType;
import com.uplink.selfstore.model.api.CartStatisticsBean;
import com.uplink.selfstore.model.api.CustomDataByVendingBean;
import com.uplink.selfstore.model.api.ImBean;
import com.uplink.selfstore.model.api.ImSeatBean;
import com.uplink.selfstore.model.api.RetImServiceSeats;
import com.uplink.selfstore.model.api.DeviceBean;
import com.uplink.selfstore.model.api.RetOrderBuildPayParams;
import com.uplink.selfstore.model.api.OrderDetailsBean;
import com.uplink.selfstore.model.api.RetOrderPayStatusQuery;
import com.uplink.selfstore.model.api.RetOrderReserve;
import com.uplink.selfstore.model.api.SkuBean;
import com.uplink.selfstore.model.api.Result;
import com.uplink.selfstore.model.api.TerminalPayOptionBean;
import com.uplink.selfstore.model.chat.MsgContentByBuyInfo;
import com.uplink.selfstore.own.AppCacheManager;
import com.uplink.selfstore.own.AppManager;
import com.uplink.selfstore.own.Config;
import com.uplink.selfstore.taskexecutor.onebyone.BaseSyncTask;
import com.uplink.selfstore.taskexecutor.onebyone.TinySyncExecutor;
import com.uplink.selfstore.ui.dialog.CustomDialogHandling;
import com.uplink.selfstore.ui.dialog.CustomDialogImSeatList;
import com.uplink.selfstore.ui.dialog.CustomDialogScanPay;
import com.uplink.selfstore.ui.my.MyListView;
import com.uplink.selfstore.ui.swipebacklayout.SwipeBackActivity;
import com.uplink.selfstore.utils.CommonUtil;
import com.uplink.selfstore.utils.LogUtil;
import com.uplink.selfstore.utils.NoDoubleClickUtil;
import com.uplink.selfstore.utils.StringUtil;
import com.uplink.selfstore.utils.ToastUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class CartActivity extends SwipeBackActivity implements View.OnClickListener {
    private static final String TAG = "CartActivity";
    private View btn_back;
    private View btn_goshopping;
    private View btn_pay_z_wechat;//微信支付 手机扫二维码
    private View btn_pay_z_zhifubao;//支付宝支付 手机扫二维码
    private View btn_pay_z_aggregate;//第三聚合支付  手机扫二维码
    private MyListView list_skus;
    private View list_empty_tip;
    private CustomDialogScanPay dialog_ScanPay;
    private CustomDialogImSeatList dialog_ImSeatList;
    private CustomDialogHandling dialog_Handling;
    private boolean isWaitHandling=false;
    public static String LAST_PAYTRANSID;
    public static String LAST_ORDERID;
    private Map<String,Boolean> ordersPaySuccess=new HashMap<String, Boolean>();
    private  TerminalPayOptionBean payOption;
    private CartSkuAdapter cartSkuAdapter;

    private String currentSvcConsulterId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);
        setNavTtile(this.getResources().getString(R.string.aty_cart_navtitle));
        initView();
        initEvent();
        initData();
        useClosePageCountTimer();
        EMClient.getInstance().chatManager().addMessageListener(msgListener);
    }

    EMMessageListener msgListener = new EMMessageListener() {

        @Override
        public void onMessageReceived(List<EMMessage> messages) {

            runOnUiThread(new Runnable() {

                @Override
                public void run() {

                    if(!isWaitHandling)
                        return;

                    //收到消息
                    LogUtil.d(TAG, "EMClient->EMMessage: onMessageReceived");
                    for (int i = 0; i < messages.size(); i++) {
                        int msgType = messages.get(i).getType().ordinal();
                        LogUtil.d("EMClient->EMMessage: onMessageReceived:msgType:" + msgType);
                        if (msgType == EMMessage.Type.CUSTOM.ordinal()) {
                            EMCustomMessageBody body = (EMCustomMessageBody) messages.get(i).getBody();
                            String type = body.getParams().get("type");
                            String content = body.getParams().get("content");
                            LogUtil.d(TAG, "EMClient->EMMessage: onMessageReceived:type:" + type);
                            LogUtil.d(TAG, "EMClient->EMMessage: onMessageReceived:content:" + content);

                            dialog_Handling.hide();

                            if (type.equals("buyinfo")) {
                                MsgContentByBuyInfo rt = JSON.parseObject(content, new TypeReference<MsgContentByBuyInfo>() {
                                });

                                if (rt != null) {
                                    if (rt.getHandleStatus() == 1) {
                                        paySend(payOption);
                                    }
                                    else if(rt.getHandleStatus()==2) {
                                        showToast("不同意购买");
                                    }
                                }
                            }
                        }
                    }

                }

            });


        }

        @Override
        public void onCmdMessageReceived(List<EMMessage> messages) {
            //收到透传消息
            LogUtil.d("EMClient->EMMessage: onCmdMessageReceived");
        }

        @Override
        public void onMessageRead(List<EMMessage> messages) {
            //收到已读回执
            LogUtil.d("EMClient->EMMessage: onMessageRead");
        }

        @Override
        public void onMessageDelivered(List<EMMessage> message) {
            //收到已送达回执
            LogUtil.d("EMClient->EMMessage: onMessageDelivered");
        }
        @Override
        public void onMessageRecalled(List<EMMessage> messages) {
            //消息被撤回
            LogUtil.d("EMClient->EMMessage: onMessageRecalled");
        }

        @Override
        public void onMessageChanged(EMMessage message, Object change) {
            //消息状态变动
            LogUtil.d("EMClient->EMMessage: onMessageChanged");
        }
    };

    private void initView() {
        btn_back = findViewById(R.id.btn_back);
        btn_goshopping = findViewById(R.id.btn_goshopping);
        btn_pay_z_wechat = findViewById(R.id.btn_pay_z_wechat);
        btn_pay_z_zhifubao = findViewById(R.id.btn_pay_z_zhifubao);
        btn_pay_z_aggregate = findViewById(R.id.btn_pay_z_aggregate);


        List<TerminalPayOptionBean> payOptions = this.getDevice().getPayOptions();
        if (payOptions != null) {
            for (int i = 0; i < payOptions.size(); i++) {
                if (payOptions.get(i).getCaller() == 10) {
                    btn_pay_z_wechat.setTag(payOptions.get(i));
                    btn_pay_z_wechat.setVisibility(View.VISIBLE);
                } else if (payOptions.get(i).getCaller() == 20) {
                    btn_pay_z_zhifubao.setTag(payOptions.get(i));
                    btn_pay_z_zhifubao.setVisibility(View.VISIBLE);
                }
                else if (payOptions.get(i).getCaller() == 30) {
                    btn_pay_z_aggregate.setTag(payOptions.get(i));
                    btn_pay_z_aggregate.setVisibility(View.VISIBLE);
                }
                else if (payOptions.get(i).getCaller() == 90) {
                    btn_pay_z_aggregate.setTag(payOptions.get(i));
                    btn_pay_z_aggregate.setVisibility(View.VISIBLE);
                }
            }
        }


        list_skus = (MyListView) findViewById(R.id.list_skus);
//        list_skus.setFocusable(false);
//        list_skus.setClickable(false);
//        list_skus.setPressed(false);
//        list_skus.setEnabled(false);
        list_empty_tip = findViewById(R.id.list_empty_tip);

        dialog_ScanPay = new CustomDialogScanPay(CartActivity.this, 120, new CustomDialogScanPay.IHanldeListener() {
            @Override
            public void onShow() {
                closePageCountTimerStop();
            }

            @Override
            public void onCancle() {
                closePageCountTimerStart();
            }

            @Override
            public void onSure() {
                closePageCountTimerStart();
                orderCancle(LAST_ORDERID, 1, "取消订单");
                LAST_PAYTRANSID = "";
                LAST_ORDERID="";
            }

            @Override
            public void onTimeTick() {
                payStatusQuery();
            }

            @Override
            public void onTimeFinish() {
                closePageCountTimerStart();
                orderCancle(LAST_ORDERID, 1, "支付超时");
                LAST_PAYTRANSID = "";
                LAST_ORDERID="";
            }

        });

        dialog_ImSeatList = new CustomDialogImSeatList(CartActivity.this);
        dialog_ImSeatList.setOnLinster(new CustomDialogImSeatList.OnLinster() {
            @Override
            public void setSeats(MyListView v) {

                LinkedHashMap<String, CartSkuBean> cartSkus = AppCacheManager.getCartSkus();

                Map<String, Object> params = new HashMap<>();
                params.put("deviceId", getDevice().getDeviceId() + "");
                JSONArray json_Skus = new JSONArray();

                try {
                    for (String key : cartSkus.keySet()) {
                        CartSkuBean bean = cartSkus.get(key);
                        JSONObject json_Sku = new JSONObject();
                        json_Sku.put("skuId", bean.getSkuId());
                        json_Sku.put("quantity", bean.getQuantity());
                        json_Skus.put(json_Sku);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    return;
                }

                params.put("skus", json_Skus);

                postByMy(CartActivity.this,Config.URL.imservice_Seats, null, true, getAppContext().getString(R.string.tips_hanlding), new HttpResponseHandler() {
                    @Override
                    public void onSuccess(String response) {

                        ApiResultBean<RetImServiceSeats> rt = JSON.parseObject(response, new TypeReference<ApiResultBean<RetImServiceSeats>>() {
                        });

                        if (rt.getResult() == Result.SUCCESS) {

                            RetImServiceSeats d = rt.getData();

                            ImSeatAdapter imSeatAdapter = new ImSeatAdapter(CartActivity.this, d.getSeats());
                            imSeatAdapter.setOnLinster(new ImSeatAdapter.OnItemListener() {
                                @Override
                                public void call(ImSeatBean v) {

                                   ImBean im= getDevice().getIm();

                                    EMClient.getInstance().login(im.getUserName(), im.getPassword(), new EMCallBack() {

                                        @Override
                                        public void onSuccess() {
                                            Log.d(TAG, "EMClient->login: onSuccess");

                                            currentSvcConsulterId=v.getUserId();

                                            JSONObject jsonExMessage = new JSONObject();

                                            try {

                                                jsonExMessage.put("type", "buyinfo");

                                                JSONObject jsonExMessageContent = new JSONObject();
                                                jsonExMessageContent.put("deviceId", getDevice().getDeviceId());
                                                jsonExMessageContent.put("storeName", getDevice().getStoreName());
                                                JSONArray json_Skus = new JSONArray();
                                                for (String key : cartSkus.keySet()) {
                                                    CartSkuBean bean = cartSkus.get(key);
                                                    JSONObject json_Sku = new JSONObject();
                                                    json_Sku.put("skuId", bean.getSkuId());
                                                    json_Sku.put("name", bean.getName());
                                                    json_Sku.put("mainImgUrl", bean.getMainImgUrl());
                                                    json_Sku.put("quantity", bean.getQuantity());
                                                    json_Skus.put(json_Sku);
                                                }
                                                jsonExMessageContent.put("skus", json_Skus);
                                                jsonExMessage.put("content", jsonExMessageContent);
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }

                                            LogUtil.i(TAG, "jsonExMessage:" + jsonExMessage.toString());
                                            Intent intent = new Intent(CartActivity.this, EmVideoCallActivity.class);
                                            intent.putExtra("username", v.getImUserName());
                                            intent.putExtra("isComingCall", false);
                                            intent.putExtra("ex_nickName", v.getNickName());
                                            intent.putExtra("ex_message", jsonExMessage.toString());

                                            startActivityForResult(intent, 0x002);

                                        }

                                        @Override
                                        public void onProgress(int progress, String status) {
                                            Log.d(TAG, "EMClient->login: onProgress");
                                        }

                                        @Override
                                        public void onError(final int code, final String message) {
                                            Log.d(TAG, "EMClient->login: onError: " + code);
                                        }
                                    });
                                }
                            });

                            v.setAdapter(imSeatAdapter);

                        } else {
                            showToast(rt.getMessage());
                        }

                    }

                    @Override
                    public void onFailure(String msg, Exception e) {
                        showToast(msg);
                    }
                });


            }
        });
        dialog_Handling = new CustomDialogHandling(CartActivity.this, 60, "咨询结果正在处理中...请耐心等候",new CustomDialogHandling.IHanldeListener(){
            @Override
            public void onShow() {
                isWaitHandling=true;
            }
            @Override
            public void onCancle() {
                isWaitHandling = false;
            }
        });
    }

    private void initEvent() {
        btn_back.setOnClickListener(this);
        btn_goshopping.setOnClickListener(this);
        btn_pay_z_wechat.setOnClickListener(this);
        btn_pay_z_zhifubao.setOnClickListener(this);
        btn_pay_z_aggregate.setOnClickListener(this);
    }

    private void initData() {
        setList();
    }

    public void setList() {

        LinkedHashMap<String, CartSkuBean> cartSkus = AppCacheManager.getCartSkus();


        if (cartSkus.size() == 0) {
            if(list_skus!=null) {
                list_skus.setVisibility(View.GONE);
            }
            if(list_empty_tip!=null){
                list_empty_tip.setVisibility(View.VISIBLE);
            }
        } else {

            if(list_skus!=null) {
                cartSkuAdapter = new CartSkuAdapter(CartActivity.this, cartSkus);
                list_skus.setAdapter(cartSkuAdapter);
                list_skus.setVisibility(View.VISIBLE);
            }

            if(list_empty_tip!=null) {
                list_empty_tip.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onClick(View v) {
        if (!NoDoubleClickUtil.isDoubleClick()) {
            switch (v.getId()) {
                case R.id.btn_back:
                    finish();
                    break;
                case R.id.btn_goshopping:
                    Intent intent = new Intent(getAppContext(), ProductKindActivity.class);
                    startActivity(intent);
                    break;
                case R.id.btn_pay_z_wechat:
                case R.id.btn_pay_z_zhifubao:
                case R.id.btn_pay_z_aggregate:

                    payOption=(TerminalPayOptionBean)v.getTag();

                    if(getDevice().getIm().isUse()) {
                        boolean isHasVieoService = false;
                        LinkedHashMap<String, CartSkuBean> cartSkus = AppCacheManager.getCartSkus();
                        for (String key : cartSkus.keySet()) {
                            CartSkuBean bean = cartSkus.get(key);
                            if (bean.isTrgVideoService()) {
                                isHasVieoService = true;
                                break;
                            }
                        }
                        if (isHasVieoService) {
                            dialog_ImSeatList.show();
                            return;
                        }
                    }

                    paySend(payOption);
                    break;
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        checkIsHasExHappen();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (dialog_ScanPay != null) {
            dialog_ScanPay.cancel();
        }

        if (dialog_ImSeatList != null) {
            dialog_ImSeatList.cancel();
        }

        if (dialog_Handling != null) {
            dialog_Handling.cancel();
        }

        if (cartSkuAdapter != null) {
            cartSkuAdapter.dismiss();
        }

        if (msgListener != null) {
            EMClient.getInstance().chatManager().removeMessageListener(msgListener);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==0x002) {
            if (resultCode == 0x001) {
                int surface_state = data.getIntExtra("surface_state",0);
                LogUtil.d(TAG,"requestCode:"+requestCode);
                LogUtil.d(TAG,"resultCode:"+resultCode);
                LogUtil.d(TAG,"surface_state:"+surface_state);
                if(surface_state==0) {//表示有通话记录
                    dialog_ImSeatList.hide();
                    dialog_Handling.show();
                }
            }
        }

    }

    private void buildBayParams(final String orderId, final TerminalPayOptionBean payOption) {
        Map<String, Object> params = new HashMap<>();

        params.put("orderId", orderId);
        params.put("payPartner", payOption.getPartner() + "");
        params.put("payCaller", payOption.getCaller() + "");
        postByMy(CartActivity.this, Config.URL.order_BuildPayParams, params, true, getAppContext().getString(R.string.tips_hanlding), new HttpResponseHandler() {
            @Override
            public void onSuccess(String response) {

                ApiResultBean<RetOrderBuildPayParams> rt = JSON.parseObject(response, new TypeReference<ApiResultBean<RetOrderBuildPayParams>>() {
                });

                if (rt.getResult() == Result.SUCCESS) {

                    RetOrderBuildPayParams d = rt.getData();

                    //taskByCheckPayTimeout.start();
                    LAST_ORDERID=orderId;
                    LAST_PAYTRANSID=d.getPayTransId();
                    dialog_ScanPay.setPayWayQrcode(payOption,d.getParamType(), d.getParamData(),d.getChargeAmount());
                    dialog_ScanPay.show();

                } else {
                    showToast(rt.getMessage());
                }
            }

            @Override
            public void onFailure(String msg, Exception e) {
                showToast(msg);
            }
        });


    }

    private void paySend(final TerminalPayOptionBean payOption) {

        LinkedHashMap<String, CartSkuBean>  cartSkus = AppCacheManager.getCartSkus();
        if (cartSkus == null || cartSkus.size() <= 0) {
            showToast(getAppContext().getString(R.string.aty_cart_tips_cartisnull));
            return;
        }

        Map<String, Object> params = new HashMap<>();
        params.put("deviceId", getDevice().getDeviceId() + "");
        params.put("shopMethod", 1);
        JSONArray json_Skus = new JSONArray();

        try {
            for(String key : cartSkus.keySet()) {
                CartSkuBean bean=cartSkus.get(key);
                JSONObject json_Sku = new JSONObject();
                json_Sku.put("skuId", bean.getSkuId());
                json_Sku.put("quantity", bean.getQuantity());
                if(currentSvcConsulterId!=null) {
                    json_Sku.put("svcConsulterId", currentSvcConsulterId);
                }
                json_Skus.put(json_Sku);
            }

        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        params.put("skus", json_Skus);

        postByMy(CartActivity.this, Config.URL.order_Reserve, params, true, getAppContext().getString(R.string.tips_hanlding), new HttpResponseHandler() {
            @Override
            public void onSuccess(String response) {

                ApiResultBean<RetOrderReserve> rt = JSON.parseObject(response, new TypeReference<ApiResultBean<RetOrderReserve>>() {
                });

                if (rt.getResult() == Result.SUCCESS) {

                    RetOrderReserve d = rt.getData();
                    buildBayParams(d.getOrderId(),payOption);

                } else {
                    showToast(rt.getMessage());
                }
            }

            @Override
            public void onFailure(String msg, Exception e) {
                showToast(msg);
            }
        });

    }

    public void payStatusQuery() {

        if (StringUtil.isEmptyNotNull(LAST_PAYTRANSID) && StringUtil.isEmptyNotNull(LAST_ORDERID))
            return;

        Map<String, Object> params = new HashMap<>();
        params.put("deviceId", this.getDevice().getDeviceId());
        params.put("payTransId", LAST_PAYTRANSID);
        params.put("orderId", LAST_ORDERID);

        postByMy(CartActivity.this, Config.URL.order_PayStatusQuery, params, false, "", new HttpResponseHandler() {
            @Override
            public void onSuccess(String response) {
                super.onSuccess(response);

                ApiResultBean<RetOrderPayStatusQuery> rt = JSON.parseObject(response, new TypeReference<ApiResultBean<RetOrderPayStatusQuery>>() {
                });


                if (rt.getResult() == Result.SUCCESS) {
                    doPaySuccess(rt.getData());
                }
            }

            @Override
            public void onFailure(String msg, Exception e) {
                showToast(msg);
            }
        });
    }

    public void orderCancle(String orderId, int type, String reason) {

        DeviceBean device = AppCacheManager.getDevice();

        Map<String, Object> params = new HashMap<>();
        params.put("deviceId", device.getDeviceId() + "");
        params.put("orderId", orderId);
        params.put("type", type);
        params.put("reason", reason);

        postByMy(CartActivity.this,Config.URL.order_Cancle, params, true, "", new HttpResponseHandler() {
            @Override
            public void onSuccess(String response) {
            }
        });
    }

    public void doPaySuccess(RetOrderPayStatusQuery bean) {
        if (bean == null)
            return;
        //3 为 已支付成功
        if (bean.getPayStatus() == 3) {
            synchronized(CartActivity.class) {

                if (!ordersPaySuccess.containsKey(LAST_PAYTRANSID)) {
                    ordersPaySuccess.put(LAST_PAYTRANSID, true);

                    BaseSyncTask task = new BaseSyncTask() {
                        @Override
                        public void doTask() {
                            AppCacheManager.setCartSkus(null);
                            Intent intent = new Intent(CartActivity.this, OrderDetailsActivity.class);
                            Bundle bundle = new Bundle();
                            OrderDetailsBean orderDetails = new OrderDetailsBean();
                            orderDetails.setOrderId(bean.getOrderId());
                            orderDetails.setPayStatus(bean.getPayStatus());
                            orderDetails.setStatus(bean.getOrderStatus());
                            orderDetails.setSkus(bean.getSkus());
                            bundle.putSerializable("dataBean", orderDetails);
                            intent.putExtras(bundle);
                            startActivity(intent);

                            if (dialog_ScanPay != null) {
                                dialog_ScanPay.cancel();
                            }

                            if (dialog_ImSeatList != null) {
                                dialog_ImSeatList.cancel();
                            }

                            if (dialog_Handling != null) {
                                dialog_Handling.cancel();
                            }


                            finish();
                        }
                    };
                    TinySyncExecutor.getInstance().enqueue(task);
                }
            }
        }
    }

    public static CartStatisticsBean getStatistics() {

        CartStatisticsBean statistics = new CartStatisticsBean();

        int sumQuantity = 0;
        float sumSalesPrice = 0;

        LinkedHashMap<String, CartSkuBean>  cartSkus = AppCacheManager.getCartSkus();

        if(cartSkus!=null&&cartSkus.size()>0) {
            for (String key : cartSkus.keySet()) {
                CartSkuBean bean = cartSkus.get(key);
                sumQuantity += bean.getQuantity();
                sumSalesPrice += bean.getQuantity() * bean.getSalePrice();
            }
        }

        statistics.setSumQuantity(sumQuantity);
        statistics.setSumSalesPrice(sumSalesPrice);
        return statistics;
    }

    public static int getSkuQuantity(String skuId) {

        int quantity = 0;

        LinkedHashMap<String, CartSkuBean> cartSkus = AppCacheManager.getCartSkus();

        if (cartSkus == null || cartSkus.size() == 0)
            return quantity;

        CartSkuBean cartSku = cartSkus.get(skuId);

        if (cartSku == null)
            return quantity;

        quantity = cartSku.getQuantity();

        return quantity;
    }

    public static void operate(int type,String skuId, final CarOperateHandler handler) {

        DeviceBean device = AppCacheManager.getDevice();
        CustomDataByVendingBean customDataByVending = AppCacheManager.getCustomDataByVending();
        LinkedHashMap<String, CartSkuBean> cartSkus = AppCacheManager.getCartSkus();
        HashMap<String, SkuBean> skus = customDataByVending.getSkus();
        CartSkuBean cartSku = cartSkus.get(skuId);
        SkuBean sku = skus.get(skuId);

        if (cartSku==null) {
            cartSku = new CartSkuBean();
            cartSku.setSkuId(skuId);
            cartSku.setMainImgUrl(sku.getMainImgUrl());
            cartSku.setTrgVideoService(sku.isTrgVideoService());
            cartSku.setCurrencySymbol("");
            cartSku.setName(sku.getName());
            cartSku.setSalePrice(sku.getSalePrice());
            cartSku.setQuantity(0);
            cartSkus.put(skuId, cartSku);
        }

        int cur_Quantity=cartSku.getQuantity();

        switch (type) {
            case CartOperateType.INCREASE:

                //判断总数量是否大于
                int mSumQuantity = 0;
                for (String key : cartSkus.keySet()) {
                    mSumQuantity += cartSkus.get(key).getQuantity();
                }

                if ((mSumQuantity + 1) > customDataByVending.getMaxBuyNumber()) {
                    ToastUtil.showMessage(AppManager.getAppManager().currentActivity(), "商品购买总量不能超过" + customDataByVending.getMaxBuyNumber() + "个", Toast.LENGTH_LONG);
                    return;
                }

                handler.callAnimation();

                cartSkus.get(skuId).setQuantity(cur_Quantity + 1);

                break;
            case CartOperateType.DECREASE:
                cur_Quantity -= 1;
                if (cur_Quantity == 0) {
                    cartSkus.remove(skuId);
                } else {
                    cartSkus.get(skuId).setQuantity(cur_Quantity);
                }
                break;
            case CartOperateType.DELETE:
                cartSkus.remove(skuId);
                break;
        }

        AppCacheManager.setCartSkus(cartSkus);

        int sumQuantity = 0;
        float sumSalesPrice = 0;

        for(String key : cartSkus.keySet()) {
            CartSkuBean bean = cartSkus.get(key);
            sumQuantity += bean.getQuantity();
            sumSalesPrice += bean.getQuantity() * bean.getSalePrice();
        }

        LinkedList<Activity> activityStack = AppManager.getAppManager().getActivityStack();
        if (activityStack != null) {
            for (Activity activity : activityStack) {

                if (activity instanceof ProductKindActivity) {
                    ProductKindActivity ac = (ProductKindActivity) activity;
                    ac.reSetKindBodyAdapter();
                    TextView txt_cart_sumquantity = (TextView) ac.findViewById(R.id.txt_cart_sumquantity);
                    TextView txt_cart_sumsalesprice = (TextView) ac.findViewById(R.id.txt_cart_sumsalesprice);
                    if (txt_cart_sumquantity != null) {
                        txt_cart_sumquantity.setText(String.valueOf(sumQuantity));
                    }
                    if (txt_cart_sumsalesprice != null) {
                        txt_cart_sumsalesprice.setText(CommonUtil.ConvertPrice(sumSalesPrice));
                    }

                } else if (activity instanceof ProductDetailsActivity) {
                    TextView txt_cart_sumquantity = (TextView) activity.findViewById(R.id.txt_cart_sumquantity);
                    txt_cart_sumquantity.setText(String.valueOf(sumQuantity));
                } else if (activity instanceof CartActivity) {
                    CartActivity ac = (CartActivity) activity;
                    ac.setList();
                }
            }
        }

        handler.onSuccess("");

    }

}
