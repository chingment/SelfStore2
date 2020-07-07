package com.uplink.selfstore.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.uplink.selfstore.R;
import com.uplink.selfstore.model.api.TerminalPayOptionBean;
import com.uplink.selfstore.ui.ViewHolder;
import com.uplink.selfstore.utils.BitmapUtil;
import com.uplink.selfstore.utils.LogUtil;
import com.uplink.selfstore.utils.StringUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chingment on 2019/9/26.
 */

public class CustomScanPayDialog extends Dialog {
    private static final String TAG = "CustomScanPayDialog";
    private View mLayoutRes;
    private Context mContext;
    private Dialog mThis;

    private TextView txt_payamount;
    private ImageView img_payqrcode;
    private TextView txt_paytips;
    private TextView txt_payseconds;
    private View btn_close;
    private View icon_payway_z_wechat;
    private View icon_payway_z_zhifubao;
    private CustomConfirmDialog dialog_ConfirmClose;
    private CountDownTimer countDownTimer;

    private IHanldeListener myHanldeListener;

    public void setPayWayQrcode(TerminalPayOptionBean payOption, String payUrl, String chargeAmount) {


        this.icon_payway_z_wechat.setVisibility(View.GONE);
        this.icon_payway_z_zhifubao.setVisibility(View.GONE);

        this.txt_payamount.setText(chargeAmount);
        this.img_payqrcode.setImageBitmap(BitmapUtil.createQrCodeBitmap(payUrl));

        int[] supportWays=payOption.getSupportWays();

        if(supportWays!=null)
        {

            for (int i=0;i<supportWays.length;i++)
            {
                if(supportWays[i]==1)
                {
                    this.icon_payway_z_wechat.setVisibility(View.VISIBLE);
                }
                else if(supportWays[i]==2)
                {
                    this.icon_payway_z_zhifubao.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    public CustomScanPayDialog(Context context,int seconds,IHanldeListener hanldeListener) {
        super(context, R.style.dialog_style);
        mThis=this;
        mContext = context;
        myHanldeListener=hanldeListener;
        mLayoutRes = LayoutInflater.from(context).inflate(R.layout.dialog_scanpay, null);

        btn_close = ViewHolder.get(mLayoutRes,R.id.btn_close);
        txt_payamount = ViewHolder.get(mLayoutRes,R.id.txt_payamount);
        img_payqrcode =ViewHolder.get(mLayoutRes,R.id.img_payqrcode);
        txt_paytips = ViewHolder.get(mLayoutRes,R.id.txt_paytips);
        icon_payway_z_wechat = ViewHolder.get(mLayoutRes,R.id.icon_payway_z_wechat);
        icon_payway_z_zhifubao = ViewHolder.get(mLayoutRes,R.id.icon_payway_z_zhifubao);
        txt_payseconds = ViewHolder.get(mLayoutRes,R.id.txt_payseconds);


        dialog_ConfirmClose = new CustomConfirmDialog(context,"确定要取消支付？" , true);
        dialog_ConfirmClose.getTipsImage().setVisibility(View.GONE);
        dialog_ConfirmClose.getBtnSure().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog_ConfirmClose.dismiss();
                mThis.dismiss();
                myHanldeListener.onSureClose();
            }
        });

        dialog_ConfirmClose.getBtnCancle().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog_ConfirmClose.dismiss();
                myHanldeListener.onCancleClose();
            }
        });

        btn_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog_ConfirmClose.show();
            }
        });

        countDownTimer = new CountDownTimer(seconds * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long seconds = (millisUntilFinished / 1000);
                LogUtil.i("支付倒计时:" + seconds);
                txt_payseconds.setText(seconds + "'");
                myHanldeListener.onTimeTick();
            }

            @Override
            public void onFinish() {
                mThis.dismiss();
                myHanldeListener.onTimeFinish();
            }
        };

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(mLayoutRes);
    }

    @Override
    public void show() {
        super.show();
        countDownTimer.cancel();
        countDownTimer.start();
        if(myHanldeListener!=null){
            myHanldeListener.onShow();
        }
    }

    @Override
    public void dismiss() {
        super.dismiss();

        if(dialog_ConfirmClose!=null&&dialog_ConfirmClose.isShowing()) {
            dialog_ConfirmClose.dismiss();
        }

        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }

    @Override
    public void cancel(){
        super.cancel();

        if(dialog_ConfirmClose!=null&&dialog_ConfirmClose.isShowing()) {
            dialog_ConfirmClose.cancel();
        }

        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

    }

    public  interface IHanldeListener{
         void onCancleClose();
         void onSureClose();
         void onTimeTick();
         void onTimeFinish();
         void onShow();
    }

}
