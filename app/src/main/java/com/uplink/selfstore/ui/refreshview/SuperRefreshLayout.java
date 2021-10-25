package com.uplink.selfstore.ui.refreshview;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

import java.lang.reflect.Field;

import com.uplink.selfstore.utils.LogUtil;

/**
 * 项目名称：Pro_selfstoreurance
 * 类描述：
 * 创建人：tuchg
 * 创建时间：17/1/18 14:13
 */
public class SuperRefreshLayout extends SwipeRefreshLayout {

    private static String TAG = "SuperRefreshLayout";

    private OnRefreshHandler onRefreshHandler;
    private RefreshAdapter adapter;
    private int mTouchSlop;
    private float mPrevX;

    public SuperRefreshLayout(Context context) {
        this(context, null);
    }

    public SuperRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        setColorSchemeColors(0xff3b93eb);
        setProgressBackgroundColorSchemeColor(0xffffffff);
        float scale = context.getResources().getDisplayMetrics().density;
        setProgressViewEndTarget(true, (int) (64 * scale + 0.5f));
        //refreshLayout.setProgressViewOffset(false,dip2px(this,-40),dip2px(this,64));
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    }

    /**
     * 监听器
     */
    public void setOnRefreshHandler(OnRefreshHandler handler) {
        onRefreshHandler = handler;
        super.setOnRefreshListener(new OnRefreshCallBack());
    }

    /**
     * 自动刷新,原生不支持,通过反射修改字段属性
     */
    public void autoRefresh() {
        try {
            setRefreshing(true);
            Field field = SwipeRefreshLayout.class.getDeclaredField("mNotify");
            field.setAccessible(true);
            field.set(this, true);
        } catch (Exception e) {
            if (onRefreshHandler != null) {
                onRefreshHandler.refresh();
            }
        }
    }

    @Override
    public void setRefreshing(boolean refreshing) {
        super.setRefreshing(refreshing);
        if (adapter != null){
            adapter.setIsRefresh(isRefreshing());
        }
    }

    /**
     * 加载完毕
     *
     * @param hasMore 是否还有下一页
     */
    public void loadComplete(boolean hasMore) {
        if (adapter == null) {
            throw new RuntimeException("must call method setAdapter to bind data");
        }
        adapter.setState(hasMore ? RefreshAdapter.STATE_MORE : RefreshAdapter.STATE_END);
    }

    /**
     * 加载出错
     */
    public void loadError() {
        if (adapter == null) {
            throw new RuntimeException("must call method setAdapter to bind data");
        }
        adapter.setState(RefreshAdapter.STATE_ERROR);
    }

    /**
     * 只支持 RecyclerView 加载更多,且需要通过此方法设置适配器
     */
    public void setAdapter(@NonNull RecyclerView recyclerView, @NonNull RefreshAdapter mAdapter) {
        adapter = mAdapter;
        recyclerView.setAdapter(adapter);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                LogUtil.d(TAG, "====>>>>>>>>addOnScrollListener loadMore()");
                if (onRefreshHandler != null
                        && !isRefreshing()
                        && (adapter.getState() == RefreshAdapter.STATE_MORE || adapter.getState() == RefreshAdapter.STATE_ERROR)
                        && newState == RecyclerView.SCROLL_STATE_IDLE
                        && !ViewCompat.canScrollVertically(recyclerView, 1)
                        ) {
                    adapter.setState(RefreshAdapter.STATE_LOAIND);
                    onRefreshHandler.loadMore();
                }
            }
        });
        adapter.setOnBottomClickListener(new MyBottomClickListener() {
            @Override
            public void onBottomClick(View v) {
                if (onRefreshHandler != null){
                    onRefreshHandler.loadMore();
                }
            }
        });
    }

    /**
     * 如果滑动控件嵌套过深,可通过该方法控制是否可以下拉
     */
    public void setRefreshEnable(boolean enable) {
        // boolean e = !ViewCompat.canScrollVertically(scrollView,-1);
        if (isEnabled() && !enable) {
            setEnabled(false);
        } else if (!isEnabled() && enable) {
            setEnabled(true);
        }
    }

    /**
     * 解决水平滑动冲突
     */
    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mPrevX = MotionEvent.obtain(event).getX();
                break;

            case MotionEvent.ACTION_MOVE:
                final float eventX = event.getX();
                float xDiff = Math.abs(eventX - mPrevX);

                if (xDiff > mTouchSlop) {
                    return false;
                }
        }
        return super.onInterceptTouchEvent(event);
    }

    /**
     * 代理回调(底层回调)
     */
    private class OnRefreshCallBack implements OnRefreshListener {

        @Override
        public void onRefresh() {
            LogUtil.d(TAG, "====>>>>>>>>OnRefreshCallBack onRefresh()");
            if (adapter != null && adapter.getState() != RefreshAdapter.STATE_MORE) {
                adapter.setState(RefreshAdapter.STATE_MORE);
            }
            if (onRefreshHandler != null) {
                onRefreshHandler.refresh();
            }
        }
    }

}
