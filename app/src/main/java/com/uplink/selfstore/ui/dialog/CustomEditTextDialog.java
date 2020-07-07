package com.uplink.selfstore.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.uplink.selfstore.R;
import com.uplink.selfstore.ui.ViewHolder;

/**
 * Created by chingment on 2017/12/22.
 */

public class CustomEditTextDialog extends Dialog  {
    private static final String TAG = "CustomEditTextDialog";
    private View mLayoutRes;// 布局文件
    private Context mContext;

    private Button btnSure;
    private Button btnCancle;
    private TextView txtTitle;
    private TextView txtTriggerView;
    private EditText txtEdit;
    public Button getBtnSure(){
        return this.btnSure;
    }

    public Button getBtnCancle(){
        return this.btnCancle;
    }

    public TextView getTxtTtile(){
        return this.txtTitle;
    }

    public TextView getTriggerView(){
        return this.txtTriggerView;
    }


    public void setTriggerView(TextView txtTriggerView){
         this.txtTriggerView=txtTriggerView;
    }

    public TextView getTxtEdit(){
        return this.txtEdit;
    }

    public CustomEditTextDialog(Context context, String tips) {
        super(context,R.style.dialog_style);
        mContext = context;
        mLayoutRes = LayoutInflater.from(context).inflate(R.layout.dialog_edittext, null);

        txtTitle= ViewHolder.get(mLayoutRes,R.id.dialog_title);
        btnSure= ViewHolder.get(mLayoutRes,R.id.dialog_confirm_btn_sure);
        btnCancle= ViewHolder.get(mLayoutRes,R.id.dialog_confirm_btn_cancle);
        txtEdit= ViewHolder.get(mLayoutRes,R.id.dialog_txt_eidt);

        txtTitle.setText(tips);
    }



    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (backListener != null) {
            backListener.onBackClick();
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(mLayoutRes);
    }

    public CustomEditTextDialog.OnDialogBackKeyDown backListener;

    public void setOnBackListener(CustomEditTextDialog.OnDialogBackKeyDown backListener) {
        this.backListener = backListener;
    }

    public interface OnDialogBackKeyDown{
        void onBackClick();
    }
}
