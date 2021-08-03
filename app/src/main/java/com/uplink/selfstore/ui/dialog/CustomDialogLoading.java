package com.uplink.selfstore.ui.dialog;

import com.uplink.selfstore.R;

import android.app.Dialog;
import android.content.Context;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

public class CustomDialogLoading extends Dialog {

	private ImageView iv_TipsImage;
	private TextView tv_TipsText;
	private Context context;

	public CustomDialogLoading(Context context) {
		super(context,R.style.dialog_loading_style);
		this.context = context;

		setContentView(R.layout.dialog_loading);
		iv_TipsImage = (ImageView)findViewById(R.id.iv_TipsImage);
		tv_TipsText = (TextView)findViewById(R.id.tv_TipsText);
		// 显示文本
		tv_TipsText.setText("正在加载...");
	}

	@Override
	public void show(){
		Animation animation = AnimationUtils.loadAnimation(context,
				R.anim.dialog_load_animation);
	// 显示动画
		iv_TipsImage.startAnimation(animation);
		super.show();
	}

	@Override
	public void cancel(){
		iv_TipsImage.clearAnimation();
		super.cancel();
	}
	
	public void setTipsText(String text){
		tv_TipsText.setText(text);
	}

}
