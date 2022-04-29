package com.uplink.selfstore.utils;

import android.app.Activity;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;

/**
 * 图片移动的动画效果
 *
 * @Description: 图片移动的动画效果
 * @File: ImageAnimatioin.java
 * @Package com.image.indicator.utility
 * @Author Hanyonglu
 * @Date 2012-6-17 下午11:57:29
 * @Version V1.0
 */
public class AnimationUtil {
    /**
     * 设置图像移动动画效果
     *
     * @param v
     * @param startX
     * @param toX
     * @param startY
     * @param toY
     */
    public static void SetImageSlide(View v, int startX, int toX, int startY, int toY) {
        TranslateAnimation anim = new TranslateAnimation(startX, toX, startY, toY);
        anim.setDuration(200);
        anim.setFillAfter(true);
        v.startAnimation(anim);
    }

    public static void SetTabImageSlide(View v, int tabLastSelectPisition, int tabCurrentSelectPisition, int width) {
        TranslateAnimation anim = new TranslateAnimation(tabLastSelectPisition * width, tabCurrentSelectPisition * width, 0, 0);
        anim.setDuration(200);
        anim.setFillAfter(true);
        v.startAnimation(anim);
    }

    public static void SetTab1ImageSlide(RadioGroup group,View bottomView, Activity activity) {

        if (group == null)
            return;

        if (group.getTag() == null)
            return;

       // Activity activity = AppManager.getAppManager().currentActivity();

        //LogUtil.i("tab当前选择getCheckedRadioButtonId:"+group.getCheckedRadioButtonId());



        RadioButton currentCheckedRadio = (RadioButton) activity.findViewById(group.getCheckedRadioButtonId());

        if (currentCheckedRadio == null) {
            //LogUtil.i("tab当前选择:null");
            return;
        }
        if (currentCheckedRadio.getTag() == null) {
            //LogUtil.i("tab当前选择tag:null");
            return;
        }

        int tabCurrentSelectPisition = Integer.parseInt(currentCheckedRadio.getTag().toString());
        int tabLastSelectPisition = Integer.parseInt(group.getTag().toString());

        //LogUtil.i("tab当前选择:" + tabCurrentSelectPisition);
        //LogUtil.i("tab当前选择的宽度:" + currentCheckedRadio.getWidth());

        int currentWidth = currentCheckedRadio.getWidth();

        //View v = bottomView;

        RelativeLayout.LayoutParams linearParams = new RelativeLayout.LayoutParams(currentWidth, 2);
        linearParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);

        bottomView.setLayoutParams(linearParams); //使设置好的布局参数应用到控件

        TranslateAnimation anim = new TranslateAnimation(tabLastSelectPisition * currentWidth, tabCurrentSelectPisition * currentWidth, 0, 0);
        anim.setDuration(200);
        anim.setFillAfter(true);
        bottomView.startAnimation(anim);

        group.setTag(tabCurrentSelectPisition);

        //group.check(group.getCheckedRadioButtonId());

    }

    /**
     * 定义动画的时间.
     */
    public final static long aniDurationMillis = 1L;

    /**
     * 用来改变当前选中区域的放大动画效果
     * 从1.0f放大1.2f倍数
     *
     * @param view  the view
     * @param scale the scale
     */
    public static void largerView(View view, float scale) {
        if (view == null)
            return;

        // 置于所有view最上层
        view.bringToFront();
        int width = view.getWidth();
        float animationSize = 1 + scale / width;
        scaleView(view, animationSize);
    }

    /**
     * 用来还原当前选中区域的还原动画效果.
     *
     * @param view  the view
     * @param scale the scale
     */
    public static void restoreLargerView(View view, float scale) {
        if (view == null)
            return;
        int width = view.getWidth();
        float toSize = 1 + scale / width;
        // 从1.2f缩小1.0f倍数
        scaleView(view, -1 * toSize);
    }

    /**
     * 缩放View的显示.
     *
     * @param view   需要改变的View
     * @param toSize 缩放的大小，其中正值代表放大，负值代表缩小，数值代表缩放的倍数
     */
    private static void scaleView(final View view, float toSize) {
        ScaleAnimation scale = null;
        if (toSize == 0) {
            return;
        } else if (toSize > 0) {
            scale = new ScaleAnimation(1.0f, toSize, 1.0f, toSize,
                    Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF, 0.5f);
        } else {
            scale = new ScaleAnimation(toSize * (-1), 1.0f, toSize * (-1),
                    1.0f, Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF, 0.5f);
        }
        scale.setDuration(aniDurationMillis);
        scale.setInterpolator(new AccelerateDecelerateInterpolator());
        scale.setFillAfter(true);
        view.startAnimation(scale);
    }

    /**
     * 跳动-跳起动画.
     *
     * @param view    the view
     * @param offsetY the offset y
     */
    private void playJumpAnimation(final View view, final float offsetY) {
        float originalY = 0;
        float finalY = -offsetY;
        AnimationSet animationSet = new AnimationSet(true);
        animationSet.addAnimation(new TranslateAnimation(0, 0, originalY, finalY));
        animationSet.setDuration(300);
        animationSet.setInterpolator(new AccelerateDecelerateInterpolator());
        animationSet.setFillAfter(true);

        animationSet.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                playLandAnimation(view, offsetY);
            }
        });

        view.startAnimation(animationSet);
    }

    /**
     * 跳动-落下动画.
     *
     * @param view    the view
     * @param offsetY the offset y
     */
    private void playLandAnimation(final View view, final float offsetY) {
        float originalY = -offsetY;
        float finalY = 0;
        AnimationSet animationSet = new AnimationSet(true);
        animationSet.addAnimation(new TranslateAnimation(0, 0, originalY, finalY));
        animationSet.setDuration(200);
        animationSet.setInterpolator(new AccelerateInterpolator());
        animationSet.setFillAfter(true);

        animationSet.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                //两秒后再调
                view.postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        playJumpAnimation(view, offsetY);
                    }
                }, 2000);
            }
        });

        view.startAnimation(animationSet);
    }

    /**
     * 旋转动画
     *
     * @param v
     * @param durationMillis
     * @param repeatCount    Animation.INFINITE
     * @param repeatMode     Animation.RESTART
     */
    public static void playRotateAnimation(View v, long durationMillis, int repeatCount, int repeatMode) {

        //创建AnimationSet对象
        AnimationSet animationSet = new AnimationSet(true);
        //创建RotateAnimation对象
        RotateAnimation rotateAnimation = new RotateAnimation(0f, +360f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        //设置动画持续
        rotateAnimation.setDuration(durationMillis);
        rotateAnimation.setRepeatCount(repeatCount);
        //Animation.RESTART
        rotateAnimation.setRepeatMode(repeatMode);
        //动画插入器
        rotateAnimation.setInterpolator(v.getContext(), android.R.anim.decelerate_interpolator);
        //添加到AnimationSet
        animationSet.addAnimation(rotateAnimation);

        //开始动画
        v.startAnimation(animationSet);
    }
}
