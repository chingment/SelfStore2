package com.uplink.selfstore.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.uplink.selfstore.R;
import com.uplink.selfstore.activity.adapter.SlotSkuSearchAdapter;
import com.uplink.selfstore.http.HttpResponseHandler;
import com.uplink.selfstore.model.api.ApiResultBean;
import com.uplink.selfstore.model.api.MachineBean;
import com.uplink.selfstore.model.api.ProductSkuSearchResultBean;
import com.uplink.selfstore.model.api.Result;
import com.uplink.selfstore.model.api.SearchProductSkuBean;
import com.uplink.selfstore.model.api.SlotBean;
import com.uplink.selfstore.own.AppCacheManager;
import com.uplink.selfstore.own.Config;
import com.uplink.selfstore.ui.BaseFragmentActivity;
import com.uplink.selfstore.ui.ViewHolder;
import com.uplink.selfstore.utils.CommonUtil;
import com.uplink.selfstore.utils.StringUtil;

import java.util.HashMap;
import java.util.Map;

public class CustomSlotEditDialog extends Dialog {

    private View layoutRes;// 布局文件
    private BaseFragmentActivity context;
    private View btn_close;
    private TextView txt_val;
    private ImageView img_SkuImg;
    private TextView txt_SlotName;
    private TextView txt_SkuId;
    private TextView txt_SkuName;
    private TextView txt_SellQty;
    private TextView txt_LockQty;
    private TextView txt_SumQty;
    private TextView txt_MaxQty;
    private ImageButton btn_keydelete;
    private Button btn_delete;
    private Button btn_fill;
    private View btn_decrease;
    private View btn_increase;
    private ListView list_search_skus;

    public CustomSlotEditDialog(final Context context) {
        super(context, R.style.dialog_style);
        this.context = (BaseFragmentActivity)context;
        this.layoutRes = LayoutInflater.from(context).inflate(R.layout.dialog_slotedit, null);

        initView();
        initEvent();
        initData();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(layoutRes);
    }

    protected void initView() {
        btn_close = ViewHolder.get(this.layoutRes, R.id.btn_close);
        txt_val = ViewHolder.get(this.layoutRes, R.id.txt_val);

        img_SkuImg = ViewHolder.get(this.layoutRes, R.id.img_SkuImg);
        txt_SlotName = ViewHolder.get(this.layoutRes, R.id.txt_SlotName);
        txt_SkuId = ViewHolder.get(this.layoutRes, R.id.txt_SkuId);
        txt_SkuName = ViewHolder.get(this.layoutRes, R.id.txt_SkuName);
        txt_SellQty = ViewHolder.get(this.layoutRes, R.id.txt_SellQty);
        txt_LockQty = ViewHolder.get(this.layoutRes, R.id.txt_LockQty);
        txt_SumQty = ViewHolder.get(this.layoutRes, R.id.txt_SumQty);
        txt_MaxQty = ViewHolder.get(this.layoutRes, R.id.txt_MaxQty);
        list_search_skus = ViewHolder.get(this.layoutRes, R.id.list_search_skus);
        btn_keydelete =  ViewHolder.get(this.layoutRes, R.id.btn_keydelete);
        btn_delete = ViewHolder.get(this.layoutRes, R.id.btn_delete);
        btn_fill = ViewHolder.get(this.layoutRes, R.id.btn_fill);
        btn_decrease = ViewHolder.get(this.layoutRes, R.id.btn_decrease);
        btn_increase = ViewHolder.get(this.layoutRes, R.id.btn_increase);
    }

    protected void initEvent() {


        final Dialog _this=this;

        btn_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _this.dismiss();
            }
        });

        btn_keydelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String val= txt_val.getText().toString();
                if(val.length()>=1) {
                    val = val.substring(0, val.length() - 1);
                }
                txt_val.setText(val);
            }
        });


        btn_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                txt_SkuId.setText("");
                txt_SkuName.setText("暂无设置");
                txt_SellQty.setText("0");
                txt_LockQty.setText("0");
                txt_SumQty.setText("0");
                img_SkuImg.setImageResource(R.drawable.default_image);
            }
        });


        btn_fill.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(StringUtil.isEmptyNotNull(txt_SkuId.getText()+"")) {
                    ((BaseFragmentActivity) context).showToast("请先设置商品");
                    return;
                }
                int sumQty=Integer.valueOf(txt_MaxQty.getText()+"");
                int lockQty=Integer.valueOf(txt_LockQty.getText()+"");
                int sellQty=sumQty-lockQty;
                txt_SellQty.setText(String.valueOf(sellQty));
                txt_SumQty.setText(String.valueOf(sumQty));
            }
        });





        //点击减去
        btn_decrease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(StringUtil.isEmptyNotNull(txt_SkuId.getText()+"")) {
                    ((BaseFragmentActivity) context).showToast("请先设置商品");
                    return;
                }

                int sumQty=Integer.valueOf(txt_SumQty.getText()+"");
                int lockQty=Integer.valueOf(txt_LockQty.getText()+"");

                if(sumQty>lockQty) {
                    sumQty -= 1;
                    int sellQty = sumQty - lockQty;
                    txt_SellQty.setText(String.valueOf(sellQty));
                    txt_SumQty.setText(String.valueOf(sumQty));

                }
            }
        });


        //点击添加
        btn_increase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(StringUtil.isEmptyNotNull(txt_SkuId.getText()+"")) {
                    ((BaseFragmentActivity) context).showToast("请先设置商品");
                    return;
                }

                int sumQty=Integer.valueOf(txt_SumQty.getText()+"");
                int lockQty=Integer.valueOf(txt_LockQty.getText()+"");
                sumQty+=1;

                int sellQty=sumQty-lockQty;
                txt_SellQty.setText(String.valueOf(sellQty));
                txt_SumQty.setText(String.valueOf(sumQty));
            }
        });

        LinearLayout all_key =ViewHolder.get(this.layoutRes, R.id.all_key);
        for (int i = 0; i < all_key.getChildCount(); i++) {
            LinearLayout viewchild = (LinearLayout)all_key.getChildAt(i);

            for (int j=0;j<viewchild.getChildCount();j++){

                Button key_btn = (Button)viewchild.getChildAt(j);

                key_btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String key = v.getTag().toString();
                        getKey(key);
                    }
                });
            }

        }
    }

    protected void initData() {


    }

    public void setSlot(SlotBean slot) {

        txt_SlotName.setText(slot.getId());

        if (StringUtil.isEmptyNotNull(slot.getProductSkuId())) {
            txt_SkuId.setText("");
            txt_SkuName.setText("暂无设置");
            txt_SellQty.setText("0");
            txt_LockQty.setText("0");
            txt_SumQty.setText("0");
            img_SkuImg.setImageResource(R.drawable.default_image);
        } else {

            txt_SkuId.setText(slot.getProductSkuId());
            txt_SkuName.setText(slot.getProductSkuName());
            txt_SellQty.setText(String.valueOf(slot.getSellQuantity()));
            txt_LockQty.setText(String.valueOf(slot.getLockQuantity()));
            txt_SumQty.setText(String.valueOf(slot.getSumQuantity()));
            txt_MaxQty.setText(String.valueOf(slot.getMaxQuantity()));

            CommonUtil.loadImageFromUrl(context, img_SkuImg, slot.getProductSkuMainImgUrl());
        }
    }

    private void getKey(String key) {
        String val=txt_val.getText()+key;
        search(val);
        txt_val.setText(val);
    }

    private void search(String key) {
        Map<String, String> params = new HashMap<>();

        MachineBean machine = AppCacheManager.getMachine();

        params.put("machineId", machine.getId());
        params.put("key", key);

       context.getByMy(Config.URL.productSku_Search, params, true,"正在寻找", new HttpResponseHandler() {
            @Override
            public void onSuccess(String response) {
                super.onSuccess(response);


                ApiResultBean<ProductSkuSearchResultBean> rt = JSON.parseObject(response, new TypeReference<ApiResultBean<ProductSkuSearchResultBean>>() {
                });


                if (rt.getResult() == Result.SUCCESS) {
                    ProductSkuSearchResultBean d=rt.getData();

                    SlotSkuSearchAdapter slotSkuSearchAdapter = new SlotSkuSearchAdapter(context, d.getProductSkus());
                    slotSkuSearchAdapter.setCallBackListener(new SlotSkuSearchAdapter.CallBackListener() {
                        @Override
                        public void setSlot(SearchProductSkuBean skuBean) {

                            txt_SkuId.setText(skuBean.getId());
                            txt_SkuName.setText(skuBean.getName());
                            txt_SellQty.setText("0");
                            txt_LockQty.setText("0");
                            txt_SumQty.setText("0");

                            CommonUtil.loadImageFromUrl(context, img_SkuImg, skuBean.getMainImgUrl());

                        }
                    });
                    list_search_skus.setAdapter(slotSkuSearchAdapter);
                }
            }
        });
    }
}