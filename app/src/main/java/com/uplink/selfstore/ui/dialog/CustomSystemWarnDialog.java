package com.uplink.selfstore.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import com.uplink.selfstore.R;
import com.uplink.selfstore.activity.SmLoginActivity;
import com.uplink.selfstore.own.AppManager;
import com.uplink.selfstore.ui.ViewHolder;
import com.uplink.selfstore.utils.LogUtil;
import com.uplink.selfstore.utils.LongClickUtil;

public class CustomSystemWarnDialog extends Dialog {

    private View layoutRes;// 布局文件
    private Context mContext;
    private ImageView img_warn;
    public CustomSystemWarnDialog(final Context context) {
        super(context, R.style.dialog_style);
        this.mContext = context;
        this.layoutRes = LayoutInflater.from(context).inflate(R.layout.dialog_systemwarn, null);

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
        img_warn= ViewHolder.get(this.layoutRes, R.id.img_warn);
    }

    protected void initEvent() {

        LongClickUtil.setLongClick(new Handler(), img_warn, 500, new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                LogUtil.e("长按触发");
                Intent intent = new Intent(mContext, SmLoginActivity.class);
                mContext.startActivity(intent);
                return true;
            }
        });
    }

    protected void initData() {


    }
}
