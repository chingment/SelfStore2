package com.uplink.selfstore.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.hyphenate.EMCallBack;
import com.hyphenate.chat.EMClient;
import com.uplink.selfstore.R;
import com.uplink.selfstore.activity.adapter.CartSkuAdapter;
import com.uplink.selfstore.activity.adapter.ImSeatAdapter;
import com.uplink.selfstore.activity.handler.CarOperateHandler;
import com.uplink.selfstore.http.HttpResponseHandler;
import com.uplink.selfstore.model.api.ApiResultBean;
import com.uplink.selfstore.model.api.CartSkuBean;
import com.uplink.selfstore.model.api.CartOperateType;
import com.uplink.selfstore.model.api.CartStatisticsBean;
import com.uplink.selfstore.model.api.ImSeatBean;
import com.uplink.selfstore.model.api.ImServiceSeatsRealtBean;
import com.uplink.selfstore.model.api.MachineBean;
import com.uplink.selfstore.model.api.OrderBuildPayParamsResultBean;
import com.uplink.selfstore.model.api.OrderDetailsBean;
import com.uplink.selfstore.model.api.OrderPayStatusQueryResultBean;
import com.uplink.selfstore.model.api.OrderReserveResultBean;
import com.uplink.selfstore.model.api.ProductSkuBean;
import com.uplink.selfstore.model.api.Result;
import com.uplink.selfstore.model.api.TerminalPayOptionBean;
import com.uplink.selfstore.own.AppCacheManager;
import com.uplink.selfstore.own.AppManager;
import com.uplink.selfstore.own.Config;
import com.uplink.selfstore.ui.dialog.CustomConfirmDialog;
import com.uplink.selfstore.ui.dialog.CustomImSeatListDialog;
import com.uplink.selfstore.ui.dialog.CustomScanPayDialog;
import com.uplink.selfstore.ui.my.MyListView;
import com.uplink.selfstore.ui.swipebacklayout.SwipeBackActivity;
import com.uplink.selfstore.utils.CommonUtil;
import com.uplink.selfstore.utils.LogUtil;
import com.uplink.selfstore.utils.NoDoubleClickUtil;
import com.uplink.selfstore.utils.StringUtil;
import com.uplink.selfstore.utils.ToastUtil;
import com.uplink.selfstore.utils.runtimepermissions.PermissionsManager;
import com.uplink.selfstore.utils.runtimepermissions.PermissionsResultAction;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
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
    private CustomScanPayDialog dialog_ScanPay;
    private CustomConfirmDialog dialog_ScanPay_ConfirmClose;
    private CountDownTimer taskByCheckPayTimeout;
    public static String LAST_ORDERID;
    private Map<String,Boolean> ordersPaySuccess=new HashMap<String, Boolean>();
    private CustomImSeatListDialog customImSeatListDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);
        setNavTtile(this.getResources().getString(R.string.aty_cart_navtitle));
        setScannerCtrl();
        initView();
        initEvent();
        initData();
        useClosePageCountTimer();
    }

    private void initView() {
        btn_back = findViewById(R.id.btn_back);
        btn_goshopping = findViewById(R.id.btn_goshopping);
        btn_pay_z_wechat = findViewById(R.id.btn_pay_z_wechat);
        btn_pay_z_zhifubao = findViewById(R.id.btn_pay_z_zhifubao);
        btn_pay_z_aggregate = findViewById(R.id.btn_pay_z_aggregate);


        List<TerminalPayOptionBean> payOptions = this.getMachine().getPayOptions();
        if (payOptions != null) {
            for (int i = 0; i < payOptions.size(); i++) {
                if (payOptions.get(i).getCaller() == 10) {
                    btn_pay_z_wechat.setTag(payOptions.get(i));
                    btn_pay_z_wechat.setVisibility(View.VISIBLE);
                } else if (payOptions.get(i).getCaller() == 20) {
                    btn_pay_z_zhifubao.setTag(payOptions.get(i));
                    btn_pay_z_zhifubao.setVisibility(View.VISIBLE);
                } else if (payOptions.get(i).getCaller() == 90) {
                    btn_pay_z_aggregate.setTag(payOptions.get(i));
                    btn_pay_z_aggregate.setVisibility(View.VISIBLE);
                }
            }
        }


        list_skus = (MyListView) findViewById(R.id.list_skus);
        list_skus.setFocusable(false);
        list_skus.setClickable(false);
        list_skus.setPressed(false);
        list_skus.setEnabled(false);
        list_empty_tip = findViewById(R.id.list_empty_tip);
        dialog_ScanPay = new CustomScanPayDialog(CartActivity.this);

        dialog_ScanPay.getBtnClose().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog_ScanPay_ConfirmClose.show();
            }
        });


        dialog_ScanPay_ConfirmClose = new CustomConfirmDialog(CartActivity.this, getAppContext().getString(R.string.aty_cart_confirmtips_payclose), true);
        dialog_ScanPay_ConfirmClose.getTipsImage().setVisibility(View.GONE);

        dialog_ScanPay_ConfirmClose.getBtnSure().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closePageCountTimerStart();
                dialog_ScanPay_ConfirmClose.dismiss();
                dialog_ScanPay.dismiss();
                orderCancle(LAST_ORDERID,1, "取消订单");
                taskByCheckPayTimeout.cancel();
                LAST_ORDERID = "";
            }
        });

        dialog_ScanPay_ConfirmClose.getBtnCancle().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closePageCountTimerStart();
                dialog_ScanPay_ConfirmClose.dismiss();
            }
        });

        taskByCheckPayTimeout = new CountDownTimer(120 * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long seconds = (millisUntilFinished / 1000);
                LogUtil.i("支付倒计时:" + seconds);
                dialog_ScanPay.getPaySecondsText().setText(seconds + "'");
                payStatusQuery();
            }

            @Override
            public void onFinish() {
                closePageCountTimerStart();

                orderCancle(LAST_ORDERID,1,"支付超时");

                if (dialog_ScanPay != null && dialog_ScanPay.isShowing()) {
                    dialog_ScanPay.dismiss();
                }
                if (dialog_ScanPay_ConfirmClose != null && dialog_ScanPay_ConfirmClose.isShowing()) {
                    dialog_ScanPay_ConfirmClose.dismiss();
                }
            }
        };


        customImSeatListDialog = new CustomImSeatListDialog(CartActivity.this);
        customImSeatListDialog.setOnLinster(new CustomImSeatListDialog.OnLinster() {
            @Override
            public void setSeats(MyListView v) {

                LinkedHashMap<String, CartSkuBean> cartSkus = AppCacheManager.getCartSkus();

                Map<String, Object> params = new HashMap<>();
                params.put("machineId", getMachine().getId() + "");
                JSONArray json_Skus = new JSONArray();

                try {
                    for(String key : cartSkus.keySet()) {
                        CartSkuBean bean=cartSkus.get(key);
                        JSONObject json_Sku = new JSONObject();
                        json_Sku.put("id", bean.getId());
                        json_Sku.put("quantity", bean.getQuantity());
                        json_Skus.put(json_Sku);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    return;
                }

                params.put("productSkus",json_Skus);

                postByMy(Config.URL.imservice_Seats, params, null, true, getAppContext().getString(R.string.tips_hanlding), new HttpResponseHandler() {
                    @Override
                    public void onSuccess(String response) {

                        ApiResultBean<ImServiceSeatsRealtBean> rt = JSON.parseObject(response, new TypeReference<ApiResultBean<ImServiceSeatsRealtBean>>() {
                        });

                        if (rt.getResult() == Result.SUCCESS) {

                            ImServiceSeatsRealtBean d = rt.getData();

                            ImSeatAdapter imSeatAdapter = new ImSeatAdapter(CartActivity.this, d.getSeats());
                            imSeatAdapter.setOnLinster(new ImSeatAdapter.OnItemListener(){
                                @Override
                                public void call(ImSeatBean v){

                                    EMClient.getInstance().login("MH_202004220011", "1a2b3c4d", new EMCallBack() {

                                        @Override
                                        public void onSuccess() {
                                            Log.d(TAG, "login: onSuccess");


                                            JSONObject jsonExMessage = new JSONObject();

                                            try {

                                            jsonExMessage.put("type","buyinfo");
                                            jsonExMessage.put("machineId",getMachine().getId());
                                            jsonExMessage.put("storeName",getMachine().getStoreName());

                                            JSONArray json_Skus = new JSONArray();

                                            try {
                                                for(String key : cartSkus.keySet()) {
                                                    CartSkuBean bean=cartSkus.get(key);
                                                    JSONObject json_Sku = new JSONObject();
                                                    json_Sku.put("id", bean.getId());
                                                    json_Sku.put("name", bean.getName());
                                                    json_Sku.put("mainImgUrl", bean.getMainImgUrl());
                                                    json_Sku.put("quantity", bean.getQuantity());
                                                    json_Skus.put(json_Sku);
                                                }
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }

                                            jsonExMessage.put("skus",json_Skus);
                                            } catch (JSONException e) {

                                            }

                                            LogUtil.i(TAG,"jsonExMessage:"+jsonExMessage.toString());
                                            Intent intent=new Intent(CartActivity.this, EmVideoCallActivity.class);
                                            intent.putExtra("username",v.getImUserName());
                                            intent.putExtra("isComingCall",false);
                                            intent.putExtra("ex_nickName",v.getNickName());
                                            intent.putExtra("ex_message",jsonExMessage.toString());
                                            startActivity(intent);
                                        }

                                        @Override
                                        public void onProgress(int progress, String status) {
                                            Log.d(TAG, "login: onProgress");
                                        }

                                        @Override
                                        public void onError(final int code, final String message) {
                                            Log.d(TAG, "login: onError: " + code);
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
                CartSkuAdapter cartSkuAdapter = new CartSkuAdapter(CartActivity.this, cartSkus);
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

                    boolean isHasVieoService=false;
                    LinkedHashMap<String, CartSkuBean> cartSkus = AppCacheManager.getCartSkus();
                    for (String key : cartSkus.keySet()) {
                        CartSkuBean bean = cartSkus.get(key);
                        if (bean.isTrgVideoService()) {
                            isHasVieoService = true;
                            break;
                        }
                    }

                    if(isHasVieoService){
                        customImSeatListDialog.show();
                        return;
                    }

                    TerminalPayOptionBean payOption=(TerminalPayOptionBean)v.getTag();
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
        if (dialog_ScanPay != null && dialog_ScanPay.isShowing()) {
            dialog_ScanPay.cancel();
        }

        if (dialog_ScanPay_ConfirmClose != null && dialog_ScanPay_ConfirmClose.isShowing()) {
            dialog_ScanPay_ConfirmClose.cancel();
        }

        if (taskByCheckPayTimeout != null) {
            taskByCheckPayTimeout.cancel();
        }
    }

    private  void  buildBayParams(final String orderId, final TerminalPayOptionBean payOption) {
        Map<String, Object> params = new HashMap<>();
        params.put("orderId", orderId);
        params.put("payPartner", payOption.getPartner() + "");
        params.put("payCaller", payOption.getCaller() + "");
        postByMy(Config.URL.order_BuildPayParams, params, null, true, getAppContext().getString(R.string.tips_hanlding), new HttpResponseHandler() {
            @Override
            public void onSuccess(String response) {

                ApiResultBean<OrderBuildPayParamsResultBean> rt = JSON.parseObject(response, new TypeReference<ApiResultBean<OrderBuildPayParamsResultBean>>() {
                });

                if (rt.getResult() == Result.SUCCESS) {

                    OrderBuildPayParamsResultBean d = rt.getData();

                    taskByCheckPayTimeout.start();
                    LAST_ORDERID=orderId;
                    dialog_ScanPay.setPayWayQrcode(payOption,d.getPayUrl(),d.getChargeAmount());
                    closePageCountTimerStop();
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

    private  void  paySend(final TerminalPayOptionBean payOption) {

        LinkedHashMap<String, CartSkuBean>  cartSkus = AppCacheManager.getCartSkus();
        if (cartSkus == null || cartSkus.size() <= 0) {
            showToast(getAppContext().getString(R.string.aty_cart_tips_cartisnull));
            return;
        }

        Map<String, Object> params = new HashMap<>();
        params.put("machineId", this.getMachine().getId() + "");
        params.put("payPartner", payOption.getPartner() + "");
        params.put("payCaller", payOption.getCaller() + "");

        JSONArray json_Skus = new JSONArray();

        try {
            for(String key : cartSkus.keySet()) {
                CartSkuBean bean=cartSkus.get(key);
                JSONObject json_Sku = new JSONObject();
                json_Sku.put("id", bean.getId());
                json_Sku.put("quantity", bean.getQuantity());
                json_Skus.put(json_Sku);
            }

        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        params.put("productSkus", json_Skus);

        postByMy(Config.URL.order_Reserve, params, null, true, getAppContext().getString(R.string.tips_hanlding), new HttpResponseHandler() {
            @Override
            public void onSuccess(String response) {

                ApiResultBean<OrderReserveResultBean> rt = JSON.parseObject(response, new TypeReference<ApiResultBean<OrderReserveResultBean>>() {
                });

                if (rt.getResult() == Result.SUCCESS) {

                    OrderReserveResultBean d = rt.getData();
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

        if(StringUtil.isEmptyNotNull(LAST_ORDERID))
            return;

        Map<String, String> params = new HashMap<>();
        params.put("machineId", this.getMachine().getId());
        params.put("orderId", LAST_ORDERID);

        getByMy(Config.URL.order_PayStatusQuery, params, false,"", new HttpResponseHandler() {
            @Override
            public void onSuccess(String response) {
                super.onSuccess(response);

                ApiResultBean<OrderPayStatusQueryResultBean> rt = JSON.parseObject(response, new TypeReference<ApiResultBean<OrderPayStatusQueryResultBean>>() {
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

    public  void  doPaySuccess(OrderPayStatusQueryResultBean bean) {
        if (bean == null)
            return;
        //4 为 已完成支付
        if (bean.getStatus() == 3000) {

            synchronized(CartActivity.class) {
                if (!ordersPaySuccess.containsKey(LAST_ORDERID)) {
                    ordersPaySuccess.put(LAST_ORDERID, true);
                    if (taskByCheckPayTimeout != null) {
                        taskByCheckPayTimeout.cancel();
                    }
                    AppCacheManager.setCartSkus(null);
                    Intent intent = new Intent(CartActivity.this, OrderDetailsActivity.class);
                    Bundle bundle = new Bundle();

                    OrderDetailsBean orderDetails = new OrderDetailsBean();
                    orderDetails.setId(bean.getId());
                    orderDetails.setStatus(bean.getStatus());
                    orderDetails.setProductSkus(bean.getProductSkus());
                    bundle.putSerializable("dataBean", orderDetails);
                    intent.putExtras(bundle);
                    startActivity(intent);
                    finish();
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

    public static void operate(int type,String productSkuId, final CarOperateHandler handler) {

        MachineBean machine = AppCacheManager.getMachine();

        LinkedHashMap<String, CartSkuBean> cartSkus = AppCacheManager.getCartSkus();
        HashMap<String, ProductSkuBean> productSkus = AppCacheManager.getGlobalDataSet().getProductSkus();
        CartSkuBean cartSku = cartSkus.get(productSkuId);
        ProductSkuBean productSku = productSkus.get(productSkuId);

        if (cartSku==null) {
            cartSku = new CartSkuBean();
            cartSku.setId(productSkuId);
            cartSku.setMainImgUrl(productSku.getMainImgUrl());
            cartSku.setTrgVideoService(productSku.isTrgVideoService());
            cartSku.setCurrencySymbol("");
            cartSku.setName(productSku.getName());
            cartSku.setSalePrice(productSku.getSalePrice());
            cartSku.setQuantity(0);
            cartSkus.put(productSkuId, cartSku);
        }

        int cur_Quantity=cartSku.getQuantity();

        switch (type) {
            case CartOperateType.INCREASE:

                //判断总数量是否大于
                int mSumQuantity = 0;
                for(String key : cartSkus.keySet()) {
                    mSumQuantity += cartSkus.get(key).getQuantity();
                }

                if ((mSumQuantity + 1) > machine.getMaxBuyNumber()) {
                    ToastUtil.showMessage(AppManager.getAppManager().currentActivity(), "商品购买总量不能超过" + machine.getMaxBuyNumber() + "个", Toast.LENGTH_LONG);
                    return;
                }

                handler.callAnimation();

                cartSkus.get(productSkuId).setQuantity(cur_Quantity + 1);

                break;
            case CartOperateType.DECREASE:
                if (cur_Quantity > 1) {
                    cartSkus.get(productSkuId).setQuantity(cur_Quantity - 1);
                }
                break;
            case CartOperateType.DELETE:
                cartSkus.remove(productSkuId);
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
                    ac.reSetProductKindBodyAdapter();
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
