package com.uplink.selfstore.ui.dialog;

import com.uplink.selfstore.R;
import com.uplink.selfstore.ui.ViewHolder;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

public class CustomDialogLoading extends Dialog {
	private static final String TAG = "CustomDialogLoading";
	private Context mContext;
	private View mLayoutRes;// 布局文件

	private ImageView ivProgress;
	private TextView tvInfo;


	public CustomDialogLoading(Context context) {
		super(context,R.style.dialog_style);

		mContext = context;
		mLayoutRes = LayoutInflater.from(context).inflate(R.layout.dialog_loading, null);

		ivProgress = ViewHolder.get(mLayoutRes,R.id.img);
		tvInfo = ViewHolder.get(mLayoutRes,R.id.tipTextView);
		// 显示文本
		tvInfo.setText("正在加载...");
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(mLayoutRes);
	}

	@Override
	public void  show(){
		Animation animation = AnimationUtils.loadAnimation(mContext,
				R.anim.dialog_load_animation);
		// 显示动画
		ivProgress.startAnimation(animation);
		super.show();

	}

	@Override
	public void cancel(){
		super.cancel();
		ivProgress.clearAnimation();
	}
	
	public void setProgressText(String text){
		tvInfo.setText(text);
	}

}
