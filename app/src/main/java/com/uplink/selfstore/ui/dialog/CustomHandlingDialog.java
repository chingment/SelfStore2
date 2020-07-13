package com.uplink.selfstore.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.uplink.selfstore.R;
import com.uplink.selfstore.ui.ViewHolder;
import com.uplink.selfstore.utils.LogUtil;

public class CustomHandlingDialog extends Dialog {
    private static final String TAG = "CustomHandlingDialog";
    private View mLayoutRes;// 布局文件
    private Context mContext;
    private CustomHandlingDialog mThis;

    private TextView txt_tips;//等待提示
    private TextView txt_seconds;//等待秒数
    private LinearLayout btn_close;
    private CustomConfirmDialog dialog_ConfirmClose;
    private CountDownTimer countDownTimer;

    private IHanldeListener mHanldeListener;
    public CustomHandlingDialog(Context context, int seconds, String tips, IHanldeListener hanldeListener) {
        super(context, R.style.dialog_style);

        mThis=this;
        mContext = context;
        mLayoutRes = LayoutInflater.from(context).inflate(R.layout.dialog_handling, null);
        mHanldeListener=hanldeListener;

        btn_close =  ViewHolder.get(mLayoutRes,R.id.btn_close);
        txt_seconds = ViewHolder.get(mLayoutRes,R.id.txt_seconds);
        txt_seconds.setText(String.valueOf(seconds) + "'");
        txt_tips =  ViewHolder.get(mLayoutRes,R.id.txt_tips);
        txt_tips.setText(tips);

        dialog_ConfirmClose = new CustomConfirmDialog(context,"确定要退出等候？" , true);
        dialog_ConfirmClose.getTipsImage().setVisibility(View.GONE);
        dialog_ConfirmClose.getBtnSure().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog_ConfirmClose.hide();
                mThis.hide();
                hanldeListener.onCancle();
            }
        });

        dialog_ConfirmClose.getBtnCancle().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog_ConfirmClose.hide();
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
                LogUtil.i("处理倒计时倒计时:" + seconds);
                txt_seconds.setText(seconds + "'");
            }

            @Override
            public void onFinish() {
                mThis.hide();
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
        mHanldeListener.onShow();

    }

    @Override
    public void hide() {
        super.hide();

        if(dialog_ConfirmClose!=null) {
            dialog_ConfirmClose.hide();
        }

        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

    }

    @Override
    public void cancel(){
        super.cancel();

        if(dialog_ConfirmClose!=null) {
            dialog_ConfirmClose.cancel();
        }

        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

    }

    public  interface IHanldeListener{
        void onCancle();
        void onShow();
    }

}
