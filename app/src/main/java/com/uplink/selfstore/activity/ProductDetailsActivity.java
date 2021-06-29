package com.uplink.selfstore.activity;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.uplink.selfstore.R;
import com.uplink.selfstore.activity.adapter.BannerAdapter;
import com.uplink.selfstore.activity.handler.CarOperateHandler;
import com.uplink.selfstore.model.api.CartOperateType;
import com.uplink.selfstore.model.api.CartSkuBean;
import com.uplink.selfstore.model.api.CartStatisticsBean;
import com.uplink.selfstore.model.api.ImgSetBean;
import com.uplink.selfstore.model.api.SkuBean;
import com.uplink.selfstore.ui.loopviewpager.AutoLoopViewPager;
import com.uplink.selfstore.ui.swipebacklayout.SwipeBackActivity;
import com.uplink.selfstore.ui.viewpagerindicator.CirclePageIndicator;
import com.uplink.selfstore.utils.CommonUtil;
import com.uplink.selfstore.utils.NoDoubleClickUtil;
import com.uplink.selfstore.utils.ToastUtil;

import java.net.URL;
import java.util.List;

public class ProductDetailsActivity extends SwipeBackActivity implements View.OnClickListener {
    private static final String TAG = "ProductDetailsActivity";
    private View btn_back;
    private View btn_cart;
    private View btn_increase;
    private View btn_buy;

    private AutoLoopViewPager banner_pager;//banner 页面
    private CirclePageIndicator banner_indicator;//banner 底部小图标

    private TextView txt_name;
    private TextView txt_briefInfo;
    private TextView txt_price_currencySymbol;
    private TextView txt_price_integer;
    private TextView txt_price_decimal;
    private WebView webview;
    private SkuBean sku;
    private TextView txt_cart_sumquantity;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_productdetails);

        setNavTtile(this.getResources().getString(R.string.aty_productdetails_navtitle));
        //setScannerCtrl(ProductDetailsActivity.this);
        sku = (SkuBean) getIntent().getSerializableExtra("dataBean");
        initView();
        initEvent();
        initData();
        checkIsHasExHappen();

        useClosePageCountTimer();
    }

    private void initView() {
        btn_back = findViewById(R.id.btn_back);
        btn_cart = findViewById(R.id.btn_cart);
        btn_increase = findViewById(R.id.btn_increase);
        btn_buy = findViewById(R.id.btn_buy);

        banner_pager = (AutoLoopViewPager) findViewById(R.id.banner_pager);
        banner_indicator = (CirclePageIndicator) findViewById(R.id.banner_indicator);

        txt_name = (TextView) findViewById(R.id.txt_name);
        txt_briefInfo = (TextView) findViewById(R.id.txt_briefInfo);
        txt_price_currencySymbol = (TextView) findViewById(R.id.txt_price_currencySymbol);
        txt_price_integer = (TextView) findViewById(R.id.txt_price_integer);
        txt_price_decimal = (TextView) findViewById(R.id.txt_price_decimal);

        webview = (WebView) findViewById(R.id.webview);

        banner_pager.setFocusable(true);
        banner_pager.setFocusableInTouchMode(true);
        banner_pager.requestFocus();
        banner_pager.setInterval(5000);

        banner_indicator.setPadding(5, 5, 10, 5);

        txt_cart_sumquantity = (TextView) findViewById(R.id.txt_cart_sumquantity);
    }

    private void initEvent() {

        btn_back.setOnClickListener(this);
        btn_cart.setOnClickListener(this);
        btn_increase.setOnClickListener(this);
        btn_buy.setOnClickListener(this);
    }

    private void initData() {


        if(sku.getDisplayImgUrls()!=null) {

            BannerAdapter  banner_adapter = new BannerAdapter(getAppContext(), sku.getDisplayImgUrls(), ImageView.ScaleType.CENTER_INSIDE);
            banner_pager.setAdapter(banner_adapter);
            banner_indicator.setViewPager(banner_pager);
        }

        txt_name.setText(sku.getName());
        txt_briefInfo.setText(sku.getBriefDes());


        txt_price_currencySymbol.setText(this.getGlobalDataSet().getDevice().getCurrencySymbol());

        String[] price = CommonUtil.getPrice(String.valueOf(sku.getSalePrice()));
        txt_price_integer.setText(price[0]);
        txt_price_decimal.setText(price[1]);


        if(sku.getDetailsDes()!=null) {
            String detailsDes = "";

            List<ImgSetBean> arr_DetailsDes=sku.getDetailsDes();

            if(arr_DetailsDes.size()>0){
                for (int i=0;i<arr_DetailsDes.size();i++){

                    detailsDes+="<p><image width=\"100%\" src=\""+arr_DetailsDes.get(i).getUrl()+"\" /></p>";
                }
            }

            String html = "<html><head><title></title></head><body>"
                    + detailsDes
                    + "</body></html>";

            webview.loadData(html, "text/html", "uft-8");
        }

        CartStatisticsBean cartStatistics = CartActivity.getStatistics();
        txt_cart_sumquantity.setText(cartStatistics.getSumQuantity() + "");
    }

    @Override
    public void onClick(View v) {
        if (!NoDoubleClickUtil.isDoubleClick()) {
            switch (v.getId()) {
                case R.id.btn_back:
                    finish();
                    break;
                case R.id.btn_cart:
                    Intent intent = new Intent(getAppContext(), CartActivity.class);
                    startActivity(intent);
                    break;
                case R.id.btn_increase:
                    if(sku.isOffSell())
                    {
                        ToastUtil.showMessage(ProductDetailsActivity.this, "商品已下架", Toast.LENGTH_LONG);
                        return;
                    }
                    CartActivity.operate(CartOperateType.INCREASE,sku.getSkuId(), new CarOperateHandler() {
                        @Override
                        public void onSuccess(String response) {

                        }
                    });
                    break;
                case R.id.btn_buy:


                    if(sku.isOffSell())
                    {
                        ToastUtil.showMessage(ProductDetailsActivity.this, "商品已下架", Toast.LENGTH_LONG);
                        return;
                    }

                    CartSkuBean cartSku=new CartSkuBean();
                    cartSku.setSkuId(sku.getSkuId());
                    cartSku.setQuantity(1);


                    CartActivity.operate(CartOperateType.INCREASE, sku.getSkuId(), new CarOperateHandler() {
                        @Override
                        public void onSuccess(String response) {
                            Intent intent2 = new Intent(getAppContext(), CartActivity.class);
                            startActivity(intent2);
                        }
                    });


                    break;
            }
        }
    }


    Html.ImageGetter imgGetter = new Html.ImageGetter() {
        public Drawable getDrawable(String source) {

            Drawable drawable = null;
            URL url;
            try {
                url = new URL(source);

                drawable = Drawable.createFromStream(url.openStream(), ""); // 获取网路图片
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
            drawable.setBounds(0, 0, drawable.getIntrinsicWidth(),
                    drawable.getIntrinsicHeight());

            return drawable;
        }
    };
}
