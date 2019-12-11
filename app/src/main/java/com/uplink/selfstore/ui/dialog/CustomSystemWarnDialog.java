package com.uplink.selfstore.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import com.uplink.selfstore.R;

public class CustomSystemWarnDialog extends Dialog {

    private View layoutRes;// 布局文件
    private Context mContext;
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

    }

    protected void initEvent() {

    }

    protected void initData() {


    }
}
