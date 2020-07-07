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
import com.uplink.selfstore.deviceCtrl.FingerVeinnerCtrl;
import com.uplink.selfstore.ui.BaseFragmentActivity;
import com.uplink.selfstore.ui.ViewHolder;

public class CustomFingerVeinDialog extends Dialog {
    private static final String TAG = "CustomFingerVeinDialog";
    private View mLayoutRes;// 布局文件
    private BaseFragmentActivity mContext;

    private View btn_close;
    private FingerVeinnerCtrl mFingerVeinnerCtrl;
    private Button btn_ReCollect;
    private TextView txt_Title;
    private TextView txt_Message;
    public CustomFingerVeinDialog(final Context context) {
        super(context, R.style.dialog_style);
        mContext =(BaseFragmentActivity) context;
        mLayoutRes = LayoutInflater.from(context).inflate(R.layout.dialog_fingervein, null);

        initView();
        initEvent();
        initData();

        mFingerVeinnerCtrl = FingerVeinnerCtrl.getInstance();
        mFingerVeinnerCtrl.connect(mContext);
    }


    public void setCollectHandler(Handler collectHandler){
        mFingerVeinnerCtrl.setCollectHandler(collectHandler);
    }

    public void setCheckLoginHandler(Handler checkLoginHandler){
        mFingerVeinnerCtrl.setCheckLoginHandler(checkLoginHandler);
    }

    public void startCollect(){
        mFingerVeinnerCtrl.startCollect();
    }

    public void startCheckLogin(){
        mFingerVeinnerCtrl.startCheckLogin();
    }

    public void pauseCheckLogin(){
        mFingerVeinnerCtrl.pauseCheckLogin();
    }

    public void resumeCheckLogin(){
        mFingerVeinnerCtrl.resumeCheckLogin();
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
        mFingerVeinnerCtrl.stopCheckLogin();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(mLayoutRes);
    }

    protected void initView() {

        btn_close= ViewHolder.get(mLayoutRes, R.id.btn_close);
        btn_ReCollect=ViewHolder.get(mLayoutRes, R.id.btn_ReCollect);
        txt_Title=ViewHolder.get(mLayoutRes, R.id.txt_Title);
        txt_Message=ViewHolder.get(mLayoutRes, R.id.txt_Message);
    }

    protected void initEvent() {
        btn_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFingerVeinnerCtrl.stopCheckLogin();
                mFingerVeinnerCtrl.stopCollect();
                dismiss();
            }
        });

        btn_ReCollect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                txt_Message.setText("请将手指放入设备,再移开");
                btn_ReCollect.setVisibility(View.VISIBLE);
                mFingerVeinnerCtrl.startCollect();
            }
        });
    }

    protected void initData() {


    }

    @Override
    public  void  cancel(){
        super.cancel();
        mFingerVeinnerCtrl.disConnect(mContext);
    }
    @Override
    public void show() {
        super.show();
    }
}
