package com.uplink.selfstore.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.uplink.selfstore.R;
import com.uplink.selfstore.activity.SmLoginActivity;
import com.uplink.selfstore.deviceCtrl.VeinLockCtrl;
import com.uplink.selfstore.ui.BaseFragmentActivity;
import com.uplink.selfstore.ui.ViewHolder;
import com.uplink.selfstore.utils.BitmapUtil;
import com.uplink.selfstore.utils.LogUtil;
import com.uplink.selfstore.utils.LongClickUtil;
import com.uplink.selfstore.utils.StringUtil;

public class CustomVienLockDialog extends Dialog {

    private View layoutRes;// 布局文件
    private BaseFragmentActivity mContext;
    private View btn_close;
    private VeinLockCtrl veinLockCtrl;
    public CustomVienLockDialog(final Context context) {
        super(context, R.style.dialog_style);
        this.mContext =(BaseFragmentActivity) context;
        this.layoutRes = LayoutInflater.from(context).inflate(R.layout.dialog_vienlock, null);

        initView();
        initEvent();
        initData();

        veinLockCtrl = new VeinLockCtrl(context);

        veinLockCtrl.setCheckLoginHandler(new Handler(new Handler.Callback() {
                    @Override
                    public boolean handleMessage(Message msg) {
                        Bundle bundle = msg.getData();
                        int status = bundle.getInt("status");
                        String message = bundle.getString("message");
                        switch (status) {
                            case 1://消息提示
                                mContext.showToast(message);
                                break;
                        }
                        return false;
                    }
                })
        );
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(layoutRes);
    }

    protected void initView() {
        btn_close= ViewHolder.get(this.layoutRes, R.id.btn_close);
    }

    protected void initEvent() {
        btn_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                veinLockCtrl.disConnect();
                dismiss();
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

        veinLockCtrl.startCheckLogin();
    }
}
