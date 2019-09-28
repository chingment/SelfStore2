package com.uplink.selfstore.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import com.uplink.selfstore.R;
import com.uplink.selfstore.activity.adapter.CartSkuAdapter;
import com.uplink.selfstore.activity.handler.CarOperateHandler;
import com.uplink.selfstore.http.HttpResponseHandler;
import com.uplink.selfstore.model.api.ApiResultBean;
import com.uplink.selfstore.model.api.CartSkuBean;
import com.uplink.selfstore.model.api.CartOperateType;
import com.uplink.selfstore.model.api.CartStatisticsBean;
import com.uplink.selfstore.model.api.MachineBean;
import com.uplink.selfstore.model.api.OrderPayStatusQueryResultBean;
import com.uplink.selfstore.model.api.OrderReserveResultBean;
import com.uplink.selfstore.model.api.ProductBean;
import com.uplink.selfstore.model.api.Result;
import com.uplink.selfstore.own.AppCacheManager;
import com.uplink.selfstore.own.AppManager;
import com.uplink.selfstore.own.Config;
import com.uplink.selfstore.ui.dialog.CustomConfirmDialog;
import com.uplink.selfstore.ui.dialog.CustomScanPayDialog;
import com.uplink.selfstore.ui.my.MyListView;
import com.uplink.selfstore.ui.my.MyTimeTask;
import com.uplink.selfstore.ui.swipebacklayout.SwipeBackActivity;
import com.uplink.selfstore.utils.BitmapUtil;
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
import java.util.TimerTask;

public class CartActivity extends SwipeBackActivity implements View.OnClickListener {
    private static final String TAG = "CartActivity";
    private View btn_back;
    private View btn_goshopping;
    private View btn_gopay;
    private View btn_payway_wechat;
    private View btn_payway_zhifubao;
    private MyListView list_skus;
    private View list_empty_tip;
    private CustomScanPayDialog dialog_ScanPay;
    private CustomConfirmDialog dialog_ScanPay_ConfirmClose;
    private CountDownTimer taskByCheckPayTimeout;
    private String lastOrderId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);
        setNavTtile(this.getResources().getString(R.string.activity_cart_navtitle));
        initView();
        initEvent();
        initData();
    }

    private void initView() {
        btn_back = findViewById(R.id.btn_back);
        btn_goshopping = findViewById(R.id.btn_goshopping);
        // btn_gopay = findViewById(R.id.btn_gopay);
        btn_payway_wechat = findViewById(R.id.btn_payway_wechat);
        btn_payway_zhifubao = findViewById(R.id.btn_payway_zhifubao);
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


        dialog_ScanPay_ConfirmClose = new CustomConfirmDialog(CartActivity.this, getAppContext().getString(R.string.activity_cart_tips_payclose_confirm), true);
        dialog_ScanPay_ConfirmClose.getTipsImage().setVisibility(View.GONE);

        dialog_ScanPay_ConfirmClose.getBtnSure().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                taskByCheckPayTimeout.cancel();
                dialog_ScanPay_ConfirmClose.dismiss();
                dialog_ScanPay.dismiss();
                orderCancle(lastOrderId, "取消订单");
                lastOrderId = "";
            }
        });

        dialog_ScanPay_ConfirmClose.getBtnCancle().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dialog_ScanPay_ConfirmClose.dismiss();
            }
        });

        taskByCheckPayTimeout = new CountDownTimer(120 * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long seconds = (millisUntilFinished / 1000);
                LogUtil.i("支付倒计时:" + seconds);
                dialog_ScanPay.getPaySecondsText().setText(seconds+ "'");
                payStatusQuery();
            }

            @Override
            public void onFinish() {
                dialog_ScanPay.getPaySecondsText().setText("支付超时，请返回重新下单");
                finish();
            }
        };
    }

    private void initEvent() {
        btn_back.setOnClickListener(this);
        btn_goshopping.setOnClickListener(this);
        btn_payway_wechat.setOnClickListener(this);
        btn_payway_zhifubao.setOnClickListener(this);
        //btn_gopay.setOnClickListener(this);
    }

    private void initData() {

        setList();

    }

    public void setList() {
        List<CartSkuBean> cartSkusByCache = AppCacheManager.getCartSkus();
        //检查当前机器商品库存是否存在，不存在的过滤
        List<CartSkuBean> cartSkus = new ArrayList<>();

        if (this.getGlobalDataSet() != null) {
            if (this.getGlobalDataSet().getProducts() != null) {

                for (CartSkuBean bean :
                        cartSkusByCache) {
                    ProductBean product = this.getGlobalDataSet().getProducts().get(bean.getProductId());
                    if (product != null) {

                        CartSkuBean cartSku = new CartSkuBean();
                        cartSku.setId(product.getRefSku().getId());
                        cartSku.setProductId(product.getId());
                        cartSku.setMainImgUrl(product.getMainImgUrl());
                        cartSku.setQuantity(bean.getQuantity());
                        cartSku.setName(product.getName());
                        cartSku.setSalePrice(product.getRefSku().getSalePrice());

                        cartSkus.add(cartSku);

                    }
                }
            }
        }


        if (cartSkus.size() == 0) {
            list_skus.setVisibility(View.GONE);
            list_empty_tip.setVisibility(View.VISIBLE);
        } else {
            CartSkuAdapter cartSkuAdapter = new CartSkuAdapter(CartActivity.this, this.getGlobalDataSet(), cartSkus);
            list_skus.setAdapter(cartSkuAdapter);
            list_skus.setVisibility(View.VISIBLE);
            list_empty_tip.setVisibility(View.GONE);

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
                case R.id.btn_payway_wechat:
                    paySend(1,10);
                    break;
                case R.id.btn_payway_zhifubao:
                    paySend(2,20);
                    break;
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (dialog_ScanPay != null&&dialog_ScanPay.isShowing()) {
            dialog_ScanPay.cancel();
        }

        if (dialog_ScanPay_ConfirmClose != null&&dialog_ScanPay_ConfirmClose.isShowing()) {
            dialog_ScanPay_ConfirmClose.cancel();
        }
    }

    private  void  paySend(final int payWay,int payCaller) {
        MachineBean machine = AppCacheManager.getMachine();
        List<CartSkuBean> cartSkus = AppCacheManager.getCartSkus();
        if (cartSkus == null || cartSkus.size() <= 0) {
            showToast(getAppContext().getString(R.string.activity_cart_tips_cartisnull));
            return;
        }


        Map<String, Object> params = new HashMap<>();
        params.put("machineId", machine.getId() + "");
        params.put("payWay", payWay + "");
        params.put("payCaller", payCaller + "");

        HashMap<String, ProductBean> products = AppCacheManager.getGlobalDataSet().getProducts();


        JSONArray json_Skus = new JSONArray();

        try {
            for (CartSkuBean bean : cartSkus) {
                ProductBean sku = products.get(bean.getProductId());
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
                    taskByCheckPayTimeout.start();
                    OrderReserveResultBean d = rt.getData();
                    lastOrderId=d.getOrderId();
                    dialog_ScanPay.getPayAmountText().setText(d.getChargeAmount());

                    switch (payWay) {
                        case 1:
                            dialog_ScanPay.getPayQrCodeImage().setImageBitmap(BitmapUtil.createQrCodeBitmapAndLogo(d.getPayUrl(), BitmapFactory.decodeResource(getResources(), R.drawable.icon_payway_wechat3)));
                            dialog_ScanPay.getPayTipsText().setText("请使用微信扫码支付");
                            break;
                        case 2:
                            dialog_ScanPay.getPayQrCodeImage().setImageBitmap(BitmapUtil.createQrCodeBitmapAndLogo(d.getPayUrl(), BitmapFactory.decodeResource(getResources(), R.drawable.icon_payway_zhifubao3)));
                            dialog_ScanPay.getPayTipsText().setText("请使用支付宝扫码支付");
                            break;
                    }

                    dialog_ScanPay.show();

                } else {
                    showToast(rt.getMessage());
                }
            }

            @Override
            public void onFailure(String msg, Exception e) {

            }
        });

    }

    public void payStatusQuery() {

        if(StringUtil.isEmptyNotNull(lastOrderId))
            return;

        Map<String, String> params = new HashMap<>();
        MachineBean machine = AppCacheManager.getMachine();
        params.put("machineId", machine.getId());
        params.put("orderId", lastOrderId);

        getByMy(Config.URL.order_PayStatusQuery, params, false,"", new HttpResponseHandler() {
            @Override
            public void onSuccess(String response) {
                super.onSuccess(response);

                ApiResultBean<OrderPayStatusQueryResultBean> rt = JSON.parseObject(response, new TypeReference<ApiResultBean<OrderPayStatusQueryResultBean>>() {
                });


                if (rt.getResult() == Result.SUCCESS) {
                    OrderPayStatusQueryResultBean d = rt.getData();
                    //4 为 已完成支付
                    if (d.getStatus() == 3000) {
                        taskByCheckPayTimeout.cancel();
                        AppCacheManager.setCartSkus(null);
                        Intent intent= new Intent(CartActivity.this, OrderDetailsActivity.class);
                        Bundle bundle=new Bundle();
                        bundle.putSerializable("dataBean", d.getOrderDetails());
                        intent.putExtras(bundle);
                        startActivity(intent);
                        finish();
                    }
                }
            }
        });
    }

    public static CartStatisticsBean getStatistics() {
        List<CartSkuBean> cartSkus = AppCacheManager.getCartSkus();
        HashMap<String, ProductBean> products = AppCacheManager.getGlobalDataSet().getProducts();


        CartStatisticsBean statistics = new CartStatisticsBean();
        int sumQuantity = 0;
        float sumSalesPrice = 0;
        for (CartSkuBean bean : cartSkus) {
            if (products != null) {
                ProductBean product = products.get(bean.getProductId());
                if (product != null) {
                    sumQuantity += bean.getQuantity();
                    sumSalesPrice += bean.getQuantity() * product.getRefSku().getSalePrice();
                }
            }
        }

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

    public static void operate(int type,String productId, String productSkuId, final CarOperateHandler handler) {

        LogUtil.e("productId:" + productId,",productSkuId:"+productSkuId);

        List<CartSkuBean> cartSkus = AppCacheManager.getCartSkus();

        HashMap<String, ProductBean> products = AppCacheManager.getGlobalDataSet().getProducts();

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

                if ((mSumQuantity + 1) > 99) {
                    ToastUtil.showMessage(AppManager.getAppManager().currentActivity(), "商品购买总量不能超过99个", Toast.LENGTH_LONG);
                    return;
                }


                handler.callAnimation();

                if (postion > -1) {
                    cartSkus.get(postion).setQuantity(cur_Quantity + 1);
                } else {
                    cartSkus.add(new CartSkuBean(productSkuId,productId, 1));
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


        CartStatisticsBean statistics = new CartStatisticsBean();
        int sumQuantity = 0;
        float sumSalesPrice = 0;
        for (CartSkuBean bean : cartSkus) {
            ProductBean product = products.get(bean.getProductId());
            if (product != null) {
                sumQuantity += bean.getQuantity();
                sumSalesPrice += bean.getQuantity() * product.getRefSku().getSalePrice();
            }
        }


        LinkedList<Activity> activityStack = AppManager.getAppManager().getActivityStack();

        for (Activity activity : activityStack) {

            if (activity instanceof ProductKindActivity) {
                ProductKindActivity ac = (ProductKindActivity) activity;
                ac.reSetProductKindBodyAdapter();
                TextView txt_cart_sumquantity = (TextView) ac.findViewById(R.id.txt_cart_sumquantity);
                TextView txt_cart_sumsalesprice = (TextView) ac.findViewById(R.id.txt_cart_sumsalesprice);
                txt_cart_sumquantity.setText(String.valueOf(sumQuantity));
                txt_cart_sumsalesprice.setText(CommonUtil.ConvertPrice(sumSalesPrice));


            } else if (activity instanceof ProductDetailsActivity) {
                TextView txt_cart_sumquantity = (TextView) activity.findViewById(R.id.txt_cart_sumquantity);
                txt_cart_sumquantity.setText(String.valueOf(sumQuantity));
            } else if (activity instanceof CartActivity) {
                CartActivity ac = (CartActivity) activity;
                ac.setList();
            }
        }

        handler.onSuccess("");

        //getSumQuantity();
    }

}
