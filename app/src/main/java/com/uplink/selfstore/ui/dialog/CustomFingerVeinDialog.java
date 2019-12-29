package com.uplink.selfstore.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import com.uplink.selfstore.R;
import com.uplink.selfstore.deviceCtrl.VeinLockCtrl;
import com.uplink.selfstore.ui.BaseFragmentActivity;
import com.uplink.selfstore.ui.ViewHolder;

public class CustomFingerVeinDialog extends Dialog {

    private View layoutRes;// 布局文件
    private BaseFragmentActivity mContext;
    private View btn_close;
    private VeinLockCtrl veinLockCtrl;
    private Button btn_ReCollect;
    public CustomFingerVeinDialog(final Context context) {
        super(context, R.style.dialog_style);
        this.mContext =(BaseFragmentActivity) context;
        this.layoutRes = LayoutInflater.from(context).inflate(R.layout.dialog_fingervein, null);

        initView();
        initEvent();
        initData();

        veinLockCtrl = new VeinLockCtrl(context);
        veinLockCtrl.connect();
    }


    public void setCollectHandler(Handler collectHandler){
        veinLockCtrl.setCollectHandler(collectHandler);
    }

    public void setCheckLoginHandler(Handler checkLoginHandler){
        veinLockCtrl.setCheckLoginHandler(checkLoginHandler);
    }

    public void startCollect(){
        veinLockCtrl.startCollect();
    }

    public void startCheckLogin(){
        veinLockCtrl.startCheckLogin();
    }

    public Button BtnReCollect()
    {
        return  this.btn_ReCollect;
    }
    public void stopCheckLogin(){
        veinLockCtrl.stopCheckLogin();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(layoutRes);
    }

    protected void initView() {

        btn_close= ViewHolder.get(this.layoutRes, R.id.btn_close);
        btn_ReCollect=ViewHolder.get(this.layoutRes, R.id.btn_ReCollect);
    }

    protected void initEvent() {
        btn_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                veinLockCtrl.stopCheckLogin();
                veinLockCtrl.stopCollect();
                dismiss();
            }
        });

        btn_ReCollect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btn_ReCollect.setVisibility(View.GONE);
                veinLockCtrl.startCheckLogin();
            }
        });
    }

    protected void initData() {


    }

    @Override
    public  void  cancel(){
        super.cancel();
        veinLockCtrl.disConnect();
    }
    @Override
    public void show() {
        super.show();
    }
}
