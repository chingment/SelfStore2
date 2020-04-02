package com.uplink.selfstore.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.tamic.statinterface.stats.core.TcStatInterface;
import com.uplink.selfstore.R;
import com.uplink.selfstore.activity.adapter.CartSkuAdapter;
import com.uplink.selfstore.activity.handler.CarOperateHandler;
import com.uplink.selfstore.deviceCtrl.CabinetCtrlByDS;
import com.uplink.selfstore.http.HttpResponseHandler;
import com.uplink.selfstore.model.api.ApiResultBean;
import com.uplink.selfstore.model.api.CartSkuBean;
import com.uplink.selfstore.model.api.CartOperateType;
import com.uplink.selfstore.model.api.CartStatisticsBean;
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
import com.uplink.selfstore.ui.dialog.CustomScanPayDialog;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class CartActivity extends SwipeBackActivity implements View.OnClickListener {
    private static final String TAG = "CartActivity";
    private View btn_back;
    private View btn_goshopping;
    private View btn_pay_z_wechat;//微信官方支付 手机扫二维码
    private View btn_pay_z_zhifubao;//支付宝官方支付 手机扫二维码
    private View btn_pay_z_aggregate;//第三聚合支付  手机扫二维码
    private MyListView list_skus;
    private View list_empty_tip;
    private CustomScanPayDialog dialog_ScanPay;
    private CustomConfirmDialog dialog_ScanPay_ConfirmClose;
    private CountDownTimer taskByCheckPayTimeout;
    public static String LAST_ORDERID;

    private Map<String,Boolean> ordersPaySuccess=new HashMap<String, Boolean>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);
        setNavTtile(this.getResources().getString(R.string.aty_cart_navtitle));
        initView();
        initEvent();
        initData();
        useClosePageCountTimer();
    }

    protected void initView() {
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
                orderCancle(LAST_ORDERID, "取消订单");
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
                if (dialog_ScanPay != null && dialog_ScanPay.isShowing()) {
                    dialog_ScanPay.dismiss();
                }
                if (dialog_ScanPay_ConfirmClose != null && dialog_ScanPay_ConfirmClose.isShowing()) {
                    dialog_ScanPay_ConfirmClose.dismiss();
                }
            }
        };
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


        List<CartSkuBean> cartSkusByCache = AppCacheManager.getCartSkus();
        //检查当前机器商品库存是否存在，不存在的过滤
        List<CartSkuBean> cartSkus = new ArrayList<>();

        if (this.getGlobalDataSet() != null) {
            if (this.getGlobalDataSet().getProductSkus() != null) {

                for (CartSkuBean bean :
                        cartSkusByCache) {
                    ProductSkuBean productSku = this.getGlobalDataSet().getProductSkus().get(bean.getId());
                    if (productSku != null) {

                        CartSkuBean cartSku = new CartSkuBean();
                        cartSku.setId(productSku.getId());
                        cartSku.setMainImgUrl(productSku.getMainImgUrl());
                        cartSku.setQuantity(bean.getQuantity());
                        cartSku.setName(productSku.getName());
                        cartSku.setSalePrice(productSku.getSalePrice());
                        cartSkus.add(cartSku);
                    }
                }
            }
        }


        if (cartSkus.size() == 0) {
            if(list_skus!=null) {
                list_skus.setVisibility(View.GONE);
            }
            if(list_empty_tip!=null){
                list_empty_tip.setVisibility(View.VISIBLE);
            }
        } else {
            if(list_skus!=null) {
                CartSkuAdapter cartSkuAdapter = new CartSkuAdapter(CartActivity.this, this.getGlobalDataSet(), cartSkus);
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
                    TcStatInterface.onEvent("btn_paypartner_z_wechat", null);
                    TerminalPayOptionBean payOption10=(TerminalPayOptionBean)v.getTag();
                    paySend(payOption10);
                    break;
                case R.id.btn_pay_z_zhifubao:
                    TcStatInterface.onEvent("btn_paypartner_z_zhifubao", null);
                    TerminalPayOptionBean payOption20=(TerminalPayOptionBean)v.getTag();
                    paySend(payOption20);
                    break;
                case R.id.btn_pay_z_aggregate:
                    TcStatInterface.onEvent("btn_pay_z_aggregate", null);
                    TerminalPayOptionBean payOption30=(TerminalPayOptionBean)v.getTag();
                    paySend(payOption30);
                    break;
            }
        }
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

        List<CartSkuBean> cartSkus = AppCacheManager.getCartSkus();
        if (cartSkus == null || cartSkus.size() <= 0) {
            showToast(getAppContext().getString(R.string.aty_cart_tips_cartisnull));
            return;
        }

        Map<String, Object> params = new HashMap<>();
        params.put("machineId", this.getMachine().getId() + "");
        params.put("payPartner", payOption.getPartner() + "");
        params.put("payCaller", payOption.getCaller() + "");

        HashMap<String, ProductSkuBean> productSkus = AppCacheManager.getGlobalDataSet().getProductSkus();

        JSONArray json_Skus = new JSONArray();

        try {
            for (CartSkuBean bean : cartSkus) {
                ProductSkuBean sku = productSkus.get(bean.getId());
                if (sku != null) {
                    JSONObject json_Sku = new JSONObject();
                    json_Sku.put("id", bean.getId());
                    json_Sku.put("quantity", bean.getQuantity());
                    json_Skus.put(json_Sku);
                }
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

                    synchronized(CartActivity.class) {
                        if (!ordersPaySuccess.containsKey(LAST_ORDERID)) {
                            ordersPaySuccess.put(LAST_ORDERID,true);
                            doPaySuccess(rt.getData());
                        }
                    }
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
            if (taskByCheckPayTimeout != null) {
                taskByCheckPayTimeout.cancel();
            }
            AppCacheManager.setCartSkus(null);
            Intent intent = new Intent(CartActivity.this, OrderDetailsActivity.class);
            Bundle bundle = new Bundle();

            OrderDetailsBean orderDetails = new OrderDetailsBean();
            orderDetails.setId(bean.getId());
            orderDetails.setSn(bean.getSn());
            orderDetails.setStatus(bean.getStatus());
            orderDetails.setProductSkus(bean.getProductSkus());
            bundle.putSerializable("dataBean", orderDetails);
            intent.putExtras(bundle);
            startActivity(intent);
            finish();
        }
    }

    public static CartStatisticsBean getStatistics() {
        List<CartSkuBean> cartSkus = AppCacheManager.getCartSkus();
        HashMap<String, ProductSkuBean> productSkus = AppCacheManager.getGlobalDataSet().getProductSkus();

        List<CartSkuBean> new_cartSkus=new ArrayList<>();

        CartStatisticsBean statistics = new CartStatisticsBean();
        int sumQuantity = 0;
        float sumSalesPrice = 0;
        for (CartSkuBean bean : cartSkus) {
            if (productSkus != null) {
                ProductSkuBean productSku = productSkus.get(bean.getId());
                if (productSku != null) {
                    sumQuantity += bean.getQuantity();
                    sumSalesPrice += bean.getQuantity() * productSku.getSalePrice();

                    new_cartSkus.add(bean);
                }
            }
        }

        AppCacheManager.setCartSkus(new_cartSkus);


        statistics.setSumQuantity(sumQuantity);
        statistics.setSumSalesPrice(sumSalesPrice);
        return statistics;
    }

    public static int getQuantity(String skuId) {

        List<CartSkuBean> beans = AppCacheManager.getCartSkus();
        int quantity = 0;
        for (int i = 0; i < beans.size(); i++) {
            if (beans.get(i).getId().equals(skuId)) {
                quantity = beans.get(i).getQuantity();
                break;
            }
        }

        return quantity;
    }

    public static void operate(int type,String productSkuId, final CarOperateHandler handler) {

        MachineBean machine=AppCacheManager.getMachine();

        List<CartSkuBean> cartSkus = AppCacheManager.getCartSkus();

        HashMap<String, ProductSkuBean> productSkus = AppCacheManager.getGlobalDataSet().getProductSkus();

        int postion = -1;
        for (int i = 0; i < cartSkus.size(); i++) {
            if (cartSkus.get(i).getId().equals(productSkuId)) {
                postion = i;
                break;
            }
        }

        int cur_Quantity = 0;

        if (postion > -1) {
            CartSkuBean bean = cartSkus.get(postion);
            cur_Quantity = bean.getQuantity();
        }

        switch (type) {
            case CartOperateType.INCREASE:


                int mSumQuantity = 0;
                for (CartSkuBean mBean : cartSkus) {
                    mSumQuantity += mBean.getQuantity();
                }


                if ((mSumQuantity+1) > machine.getMaxBuyNumber()) {
                    ToastUtil.showMessage(AppManager.getAppManager().currentActivity(), "商品购买总量不能超过"+machine.getMaxBuyNumber()+"个", Toast.LENGTH_LONG);
                    return;
                }


                handler.callAnimation();

                if (postion > -1) {
                    cartSkus.get(postion).setQuantity(cur_Quantity + 1);
                } else {
                    cartSkus.add(new CartSkuBean(productSkuId, 1));
                }

                break;
            case CartOperateType.DECREASE:

                if (cur_Quantity >= 1) {
                    cartSkus.get(postion).setQuantity(cur_Quantity - 1);
                    if (cur_Quantity == 1) {
                        cartSkus.remove(postion);
                    }
                }
                break;
            case CartOperateType.DELETE:
                cartSkus.remove(postion);
                break;
        }


        AppCacheManager.setCartSkus(cartSkus);


        int sumQuantity = 0;
        float sumSalesPrice = 0;
        for (CartSkuBean bean : cartSkus) {
            ProductSkuBean productSku = productSkus.get(bean.getId());
            if (productSku != null) {
                sumQuantity += bean.getQuantity();
                sumSalesPrice += bean.getQuantity() * productSku.getSalePrice();
            }
        }


        LinkedList<Activity> activityStack = AppManager.getAppManager().getActivityStack();
        if(activityStack!=null) {
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
