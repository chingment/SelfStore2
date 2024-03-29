package com.uplink.selfstore.activity;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.os.Bundle;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.uplink.selfstore.R;
import com.uplink.selfstore.activity.adapter.KindBodyAdapter;
import com.uplink.selfstore.activity.adapter.KindNameAdapter;
import com.uplink.selfstore.activity.adapter.KindSkuAdapter;
import com.uplink.selfstore.model.api.CartSkuBean;
import com.uplink.selfstore.model.api.CartStatisticsBean;
import com.uplink.selfstore.model.api.CustomDataByVendingBean;
import com.uplink.selfstore.model.api.KindBean;
import com.uplink.selfstore.model.api.SkuBean;
import com.uplink.selfstore.app.AppCacheManager;
import com.uplink.selfstore.ui.XCRoundImageView;
import com.uplink.selfstore.ui.swipebacklayout.SwipeBackActivity;
import com.uplink.selfstore.utils.CommonUtil;
import com.uplink.selfstore.utils.NoDoubleClickUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

public class ProductKindActivity extends SwipeBackActivity implements View.OnClickListener {
    private static final String TAG = "ProductKindActivity";
    private View btn_back;
    private View btn_cart;
    private View btn_gosettlement;
    private ListView list_kind_name;
    private GridView list_kind_body;
    private TextView txt_cart_sumquantity;
    private TextView txt_cart_sumsalesprice;

    private KindSkuAdapter kindSkuAdapter;
    private KindBodyAdapter list_kind_body_adapter;
    private ImageView mCart;
    private RelativeLayout layout_parentroot;
    private PathMeasure mPathMeasure;
    private float[] mCurrentPosition = new float[2];
    private List<KindBean> kinds;
    private static int cur_Kind_Position = 0;

    private CustomDataByVendingBean customDataByVending=null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_productkind);
        setNavTtile(this.getResources().getString(R.string.aty_productkind_navtitle));
        //setScannerCtrl(ProductKindActivity.this);
        initView();
        initEvent();
        initData();
        useClosePageCountTimer();
    }

    private void initView() {
        btn_back = findViewById(R.id.btn_back);
        btn_cart = findViewById(R.id.btn_cart);
        btn_gosettlement = findViewById(R.id.btn_gosettlement);
        list_kind_name = (ListView) findViewById(R.id.list_kind_name);
        list_kind_body = (GridView) findViewById(R.id.list_kind_body);
        txt_cart_sumquantity = (TextView) findViewById(R.id.txt_cart_sumquantity);
        txt_cart_sumsalesprice = (TextView) findViewById(R.id.txt_cart_sumsalesprice);

        list_kind_body.setFocusable(false);
        list_kind_body.setClickable(false);

        //list_kind_body.setPressed(false);
        //list_kind_body.setEnabled(false);


        layout_parentroot = (RelativeLayout) findViewById(R.id.layout_parentroot);
        mCart = (ImageView) findViewById(R.id.img_test);
    }

    private void initEvent() {

        btn_back.setOnClickListener(this);
        btn_cart.setOnClickListener(this);
        btn_gosettlement.setOnClickListener(this);

        list_kind_name.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {
                cur_Kind_Position = position;
                loadKindData(customDataByVending);
            }
        });


    }

    public void loadKindData(CustomDataByVendingBean customDataByVending) {

        this.customDataByVending=customDataByVending;
        if(this.customDataByVending==null)
            return;

        kinds = this.customDataByVending.getKinds();

        if (kinds == null)
            return;

        if (kinds.size() <= 0)
            return;

        if(cur_Kind_Position<=-1){
            return;
        }

        KindBean kind = kinds.get(cur_Kind_Position);

        if (kind == null)
            return;

        KindNameAdapter list_kind_name_adapter = new KindNameAdapter(getAppContext(), kinds, cur_Kind_Position);
        list_kind_name.setAdapter(list_kind_name_adapter);

        if(this.customDataByVending.isHiddenKind()) {
            list_kind_name.setVisibility(View.GONE);
        }

        List<SkuBean> skusByKind = new ArrayList<>();

        HashMap<String, SkuBean> skus = this.customDataByVending.getSkus();

        for (String skuId : kind.getChilds()) {
            if(skus!=null) {
                SkuBean sku = skus.get(skuId);
                if (sku != null) {
                    skusByKind.add(sku);
                }
            }
        }


        kindSkuAdapter = new KindSkuAdapter(ProductKindActivity.this, skusByKind,getDevice(),this.getCustomDataByVending());
        kindSkuAdapter.setCallBackListener(new KindSkuAdapter.CallBackListener() {
            @Override
            public void callBackImg(ImageView goodsImg) {
                // 添加商品到购物车
                addGoodsToCart(goodsImg);
            }
        });
        list_kind_body.setAdapter(kindSkuAdapter);


    }

    private void initData() {
        customDataByVending = this.getCustomDataByVending();
        loadKindData(customDataByVending);
        CartStatisticsBean cartStatistics = CartActivity.getStatistics();
        if (cartStatistics != null) {
            txt_cart_sumquantity.setText(String.valueOf(cartStatistics.getSumQuantity()));
            txt_cart_sumsalesprice.setText(CommonUtil.ConvertPrice(cartStatistics.getSumSalesPrice()));
        }
    }

    public void reSetKindBodyAdapter() {
        if(kindSkuAdapter!=null) {
            kindSkuAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onClick(View v) {
        if (!NoDoubleClickUtil.isDoubleClick()) {
            Intent intent = null;
            switch (v.getId()) {
                case R.id.btn_back:
                    intent = new Intent(getAppContext(), MainActivity.class);
                    startActivity(intent);
                    break;
                case R.id.btn_cart:
                case R.id.btn_gosettlement:
                    LinkedHashMap<String, CartSkuBean> cartSkus = AppCacheManager.getCartSkus();

                    if (cartSkus == null || cartSkus.size() <= 0) {
                        showToast(getAppContext().getString(R.string.aty_cart_tips_cartismust));
                        return;
                    }
                    intent = new Intent(getAppContext(), CartActivity.class);
                    startActivity(intent);
                    break;
            }
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        loadKindData(customDataByVending);
        checkIsHasExHappen();
    }

    private void addGoodsToCart(ImageView goodsImg) {
        // 创造出执行动画的主题goodsImg（这个图片就是执行动画的图片,从开始位置出发,经过一个抛物线（贝塞尔曲线）,移动到购物车里）
        final XCRoundImageView goods = new XCRoundImageView(this);
        goods.setImageDrawable(goodsImg.getDrawable());
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(100, 100);
        layout_parentroot.addView(goods, params);

        // 得到父布局的起始点坐标（用于辅助计算动画开始/结束时的点的坐标）
        int[] parentLocation = new int[2];
        layout_parentroot.getLocationInWindow(parentLocation);

        // 得到商品图片的坐标（用于计算动画开始的坐标）
        int startLoc[] = new int[2];
        goodsImg.getLocationInWindow(startLoc);

        // 得到购物车图片的坐标(用于计算动画结束后的坐标)
        int endLoc[] = new int[2];
        mCart.getLocationInWindow(endLoc);

        // 开始掉落的商品的起始点：商品起始点-父布局起始点+该商品图片的一半
        float startX = startLoc[0] - parentLocation[0] + goodsImg.getWidth() / 2;
        float startY = startLoc[1] - parentLocation[1] + goodsImg.getHeight() / 2;

        // 商品掉落后的终点坐标：购物车起始点-父布局起始点+购物车图片的1/5
        float toX = endLoc[0] - parentLocation[0] + mCart.getWidth() / 5;
        float toY = endLoc[1] - parentLocation[1];

        // 开始绘制贝塞尔曲线
        Path path = new Path();
        // 移动到起始点（贝塞尔曲线的起点）
        path.moveTo(startX, startY);
        // 使用二阶贝塞尔曲线：注意第一个起始坐标越大，贝塞尔曲线的横向距离就会越大，一般按照下面的式子取即可
        path.quadTo((startX + toX) / 2, startY, toX, toY);
        // mPathMeasure用来计算贝塞尔曲线的曲线长度和贝塞尔曲线中间插值的坐标，如果是true，path会形成一个闭环
        mPathMeasure = new PathMeasure(path, false);

        // 属性动画实现（从0到贝塞尔曲线的长度之间进行插值计算，获取中间过程的距离值）
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, mPathMeasure.getLength());
        valueAnimator.setDuration(2200);

        // 匀速线性插值器
        valueAnimator.setInterpolator(new LinearInterpolator());
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                // 当插值计算进行时，获取中间的每个值，
                // 这里这个值是中间过程中的曲线长度（下面根据这个值来得出中间点的坐标值）
                float value = (Float) animation.getAnimatedValue();
                // 获取当前点坐标封装到mCurrentPosition
                // boolean getPosTan(float distance, float[] pos, float[] tan) ：
                // 传入一个距离distance(0<=distance<=getLength())，然后会计算当前距离的坐标点和切线，pos会自动填充上坐标，这个方法很重要。
                // mCurrentPosition此时就是中间距离点的坐标值
                mPathMeasure.getPosTan(value, mCurrentPosition, null);
                // 移动的商品图片（动画图片）的坐标设置为该中间点的坐标
                goods.setTranslationX(mCurrentPosition[0]);
                goods.setTranslationY(mCurrentPosition[1]);
            }
        });

        // 开始执行动画
        valueAnimator.start();

        // 动画结束后的处理
        valueAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                layout_parentroot.removeView(goods);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
