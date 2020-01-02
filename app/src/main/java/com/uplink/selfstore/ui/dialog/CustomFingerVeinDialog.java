package com.uplink.selfstore.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.uplink.selfstore.R;
import com.uplink.selfstore.deviceCtrl.FingerVeinCtrl;
import com.uplink.selfstore.ui.BaseFragmentActivity;
import com.uplink.selfstore.ui.ViewHolder;

public class CustomFingerVeinDialog extends Dialog {

    private View layoutRes;// 布局文件
    private BaseFragmentActivity mContext;
    private View btn_close;
    private FingerVeinCtrl mFingerVeinCtrl;
    private Button btn_ReCollect;
    private TextView txt_Title;
    private TextView txt_Message;
    public CustomFingerVeinDialog(final Context context) {
        super(context, R.style.dialog_style);
        this.mContext =(BaseFragmentActivity) context;
        this.layoutRes = LayoutInflater.from(context).inflate(R.layout.dialog_fingervein, null);

        initView();
        initEvent();
        initData();

        mFingerVeinCtrl = FingerVeinCtrl.getInstance();
        mFingerVeinCtrl.connect(mContext);
    }


    public void setCollectHandler(Handler collectHandler){
        mFingerVeinCtrl.setCollectHandler(collectHandler);
    }

    public void setCheckLoginHandler(Handler checkLoginHandler){
        mFingerVeinCtrl.setCheckLoginHandler(checkLoginHandler);
    }

    public void startCollect(){
        mFingerVeinCtrl.startCollect();
    }

    public void startCheckLogin(){
        mFingerVeinCtrl.startCheckLogin();
    }

    public void pauseCheckLogin(){
        mFingerVeinCtrl.pauseCheckLogin();
    }

    public void resumeCheckLogin(){
        mFingerVeinCtrl.resumeCheckLogin();
    }

    public Button getBtnReCollect()
    {
        return  this.btn_ReCollect;
    }

    public TextView getTxtMessage()
    {
        return  this.txt_Message;
    }

    public void stopCheckLogin(){
        mFingerVeinCtrl.stopCheckLogin();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(layoutRes);
    }

    protected void initView() {

        btn_close= ViewHolder.get(this.layoutRes, R.id.btn_close);
        btn_ReCollect=ViewHolder.get(this.layoutRes, R.id.btn_ReCollect);
        txt_Title=ViewHolder.get(this.layoutRes, R.id.txt_Title);
        txt_Message=ViewHolder.get(this.layoutRes, R.id.txt_Message);
    }

    protected void initEvent() {
        btn_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFingerVeinCtrl.stopCheckLogin();
                mFingerVeinCtrl.stopCollect();
                dismiss();
            }
        });

        btn_ReCollect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                txt_Message.setText("请将手指放入设备,再移开");
                btn_ReCollect.setVisibility(View.VISIBLE);
                mFingerVeinCtrl.startCollect();
            }
        });
    }

    protected void initData() {


    }

    @Override
    public  void  cancel(){
        super.cancel();
        mFingerVeinCtrl.disConnect(mContext);
    }
    @Override
    public void show() {
        super.show();
    }
}
