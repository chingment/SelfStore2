package com.uplink.selfstore.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.uplink.selfstore.R;
import com.uplink.selfstore.utils.BitmapUtil;
import com.uplink.selfstore.utils.LogUtil;
import com.uplink.selfstore.utils.StringUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chingment on 2019/9/26.
 */

public class CustomScanPayDialog extends Dialog {

    private View layoutRes;// 布局文件
    private Context mContext;


    private TextView txt_payamount;
    private ImageView img_payqrcode;
    private TextView txt_paytips;
    private TextView txt_payseconds;
    private View btn_close;
    private View icon_payway_z_wechat;
    private View icon_payway_z_zhifubao;

    public View getBtnClose() {
        return this.btn_close;
    }

    public TextView getPayAmountText() {
        return this.txt_payamount;
    }

    public TextView getPayTipsText() {
        return this.txt_paytips;
    }

    public TextView getPaySecondsText() {
        return this.txt_payseconds;
    }

    public ImageView getPayQrCodeImage() {
        return this.img_payqrcode;
    }


    public void setPayWayQrcode(int payCaller,String payUrl,String chargeAmount) {


        this.txt_payamount.setText(chargeAmount);
        switch (payCaller) {
            case 10:
                this.img_payqrcode.setImageBitmap(BitmapUtil.createQrCodeBitmapAndLogo(payUrl, BitmapFactory.decodeResource(mContext.getResources(), R.drawable.icon_payway_wechat3)));
                this.icon_payway_z_wechat.setVisibility(View.VISIBLE);
                this.icon_payway_z_zhifubao.setVisibility(View.GONE);
                break;
            case 20:
                this.img_payqrcode.setImageBitmap(BitmapUtil.createQrCodeBitmapAndLogo(payUrl, BitmapFactory.decodeResource(mContext.getResources(), R.drawable.icon_payway_zhifubao3)));
                this.icon_payway_z_wechat.setVisibility(View.GONE);
                this.icon_payway_z_zhifubao.setVisibility(View.VISIBLE);
                break;
            case 30:
                this.img_payqrcode.setImageBitmap(BitmapUtil.createQrCodeBitmap(payUrl));
                this.icon_payway_z_wechat.setVisibility(View.VISIBLE);
                this.icon_payway_z_zhifubao.setVisibility(View.VISIBLE);
                break;
            default:
                this.icon_payway_z_wechat.setVisibility(View.GONE);
                this.icon_payway_z_zhifubao.setVisibility(View.GONE);
                break;
        }

    }

    public CustomScanPayDialog(Context context) {
        super(context, R.style.dialog_style);
        this.mContext = context;
        this.layoutRes = LayoutInflater.from(context).inflate(R.layout.dialog_scanpay, null);

        txt_payamount = (TextView) this.layoutRes.findViewById(R.id.txt_payamount);
        img_payqrcode = (ImageView) this.layoutRes.findViewById(R.id.img_payqrcode);
        txt_paytips = (TextView) this.layoutRes.findViewById(R.id.txt_paytips);
        txt_payseconds = (TextView) this.layoutRes.findViewById(R.id.txt_payseconds);
        btn_close = (View) this.layoutRes.findViewById(R.id.btn_close);
        icon_payway_z_wechat = findViewById(R.id.icon_payway_z_wechat);
        icon_payway_z_zhifubao = findViewById(R.id.icon_payway_z_zhifubao);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(layoutRes);


        final Dialog _this=this;


        final LinearLayout btn_close = (LinearLayout) this.layoutRes.findViewById(R.id.btn_close);


//        btn_close.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                _this.dismiss();
//            }
//        });


    }

}
