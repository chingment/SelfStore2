package com.uplink.selfstore.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;

import com.uplink.selfstore.R;

/**
 * <p>
 * Title: CustomDialog
 * </p>
 * <p>
 * Description:自定义Dialog（参数传入Dialog样式文件，Dialog布局文件）
 * </p>
 * <p>
 * Copyright: Copyright (c) 2013
 * </p>
 * 
 * @author archie
 * @version 1.0
 */
public class CustomDialog extends Dialog {

	private static final String TAG = "CustomDialog";
	View mLayoutRes;// 布局文件
	Context mContext;

	public CustomDialog(Context context) {
		super(context, R.style.dialog_style);
		this.mContext = context;
	}

	/**
	 * 自定义布局的构造方法
	 * 
	 * @param context
	 * @param resLayout
	 */
	public CustomDialog(Context context, View resLayout) {
		super(context);
		this.mContext = context;
		this.mLayoutRes = resLayout;
	}

	/**
	 * 自定义主题及布局的构造方法
	 * 
	 * @param context
	 * @param theme
	 * @param resLayout
	 */
	public CustomDialog(Context context, int theme, View resLayout) {
		super(context, theme);
		this.mContext = context;
		this.mLayoutRes = resLayout;
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
	
	public OnDialogBackKeyDown backListener;
	
	public void setOnBackListener(OnDialogBackKeyDown backListener) {
		this.backListener = backListener;
	}

	public interface OnDialogBackKeyDown{
		void onBackClick();
	}
	
}
