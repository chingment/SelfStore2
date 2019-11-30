package com.uplink.selfstore.ui;


import android.content.Context;
import android.content.Intent;
import android.os.CountDownTimer;

import com.uplink.selfstore.activity.MainActivity;
import com.uplink.selfstore.utils.LogUtil;

public class ClosePageCountTimer extends CountDownTimer {
    private Context context;

    /**
     * 参数 millisInFuture       倒计时总时间（如60S，120s等）
     * 参数 countDownInterval    渐变时间（每次倒计1s）
     */
    public ClosePageCountTimer(Context context,long millisInFuture) {
        super(millisInFuture*1000, 1000);
        this.context=context;
    }

    public ClosePageCountTimer(Context context,long millisInFuture,OnPageCountLinster onPageCountLinster) {
        super(millisInFuture*1000, 1000);
        this.context=context;
        this.onPageCountLinster=onPageCountLinster;
    }
    // 计时完毕时触发
    @Override
    public void onFinish() {
        LogUtil.d("跳转到主页");
        Intent intent = new Intent(context, MainActivity.class);
        context.startActivity(intent);
    }
    // 计时过程显示
    @Override
    public void onTick(long millisUntilFinished) {
        long seconds = (millisUntilFinished / 1000);
        if(onPageCountLinster!=null) {
            onPageCountLinster.onTick(seconds);
        }
        LogUtil.i("主页倒计时:" + seconds);
    }


    public OnPageCountLinster onPageCountLinster = null;
    public interface OnPageCountLinster {
        public void onTick(long seconds);
    }

}