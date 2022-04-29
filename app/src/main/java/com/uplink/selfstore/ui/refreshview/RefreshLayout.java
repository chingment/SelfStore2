package com.uplink.selfstore.ui.refreshview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.GridView;
import android.widget.ListView;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.uplink.selfstore.R;
import com.uplink.selfstore.utils.LogUtil;

/**
 * 继承自SwipeRefreshLayout,从而实现滑动到底部时上拉加载更多的功能.
 *
 * @author mrsimple
 */
public class RefreshLayout extends SwipeRefreshLayout implements OnScrollListener {
    private String TAG = "RefreshLayout";
    /**
     * 滑动到最下面时的上拉操作
     */

    private int mTouchSlop;
    /**
     * listview实例
     */
    private ListView mListView;
    /**
     * gridview实例
     */
    private GridView mGridView;

    /**
     * 上拉监听器, 到了最底部的上拉加载操作
     */
    private OnLoadListener mOnLoadListener;

    /**
     * 拉动监听器
     */
    private OnRMoveListener mMoveListenter;

    /**
     * ListView的加载中footer
     */
    private View mListViewFooter;

    /**
     * 按下时的y坐标
     */
    private int mYDown;
    /**
     * 抬起时的y坐标, 与mYDown一起用于滑动到底部时判断是上拉还是下拉
     */
    private int mLastY;
    /**
     * 是否在加载中 ( 上拉加载更多 )
     */
    private boolean isLoading = false;

    /**
     * @param context
     */
    public RefreshLayout(Context context) {
        this(context, null);
    }

    @SuppressLint("InflateParams")
    public RefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);

        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();

        mListViewFooter = LayoutInflater.from(context).inflate(R.layout.listview_footer, null, false);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        // 初始化ListView对象
        if (mListView == null || mGridView == null) {
            getListView();
        }
    }

    /**
     * 获取ListView对象
     */
    private void getListView() {
        int childs = getChildCount();
        if (childs > 0) {
            View childView = getChildAt(0);
            if (childView instanceof ListView) {
                mListView = (ListView) childView;
                // 设置滚动监听器给ListView, 使得滚动的情况下也可以自动加载
                mListView.setOnScrollListener(this);
                LogUtil.d(VIEW_LOG_TAG, "### 找到listview");
            }else if (childView instanceof GridView) {
                mGridView = (GridView) childView;
                // 设置滚动监听器给ListView, 使得滚动的情况下也可以自动加载
                mGridView.setOnScrollListener(this);
                LogUtil.d(VIEW_LOG_TAG, "### 找到listview");
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see android.view.ViewGroup#dispatchTouchEvent(android.view.MotionEvent)
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        final int action = event.getAction();

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                // 按下
                mYDown = (int) event.getRawY();
                break;

            case MotionEvent.ACTION_MOVE:
                // 移动
                mLastY = (int) event.getRawY();
                if (mMoveListenter != null) {
                    mMoveListenter.onMove();
                }
                break;
            case MotionEvent.ACTION_UP:
//			LogUtil.d(TAG, "dispatchTouchEvent===============>>>>>>>>>");
                // 抬起
                if (canLoad()) {
                    loadData();
                }
                break;
            default:
                break;
        }

        return super.dispatchTouchEvent(event);
    }

    private boolean canLoadMore = true;

    public void setLoadMoreEnable(boolean loadEnable) {
        this.canLoadMore = loadEnable;
    }

    /**
     * 是否可以加载更多, 条件是到了最底部, listview不在加载中, 且为上拉操作.
     *
     * @return
     */
    private boolean canLoad() {
//        LogUtil.d(TAG, "canLoadMore=>" + canLoadMore + "<isBottom()=>" + isBottom() + "<isLoading=>" + isLoading + "<isPullUp()=>" + isPullUp());
        return canLoadMore && isBottom() && !isLoading && isPullUp();
    }

    /**
     * 判断是否到了最底部
     */
    private boolean isBottom() {

        try {
            if (mListView != null && mListView.getAdapter() != null) {
                return mListView.getLastVisiblePosition() == (mListView.getAdapter().getCount() - 1);
            }else if(mGridView != null && mGridView.getAdapter() != null){
                return mGridView.getLastVisiblePosition() == (mGridView.getAdapter().getCount() - 1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 是否是上拉操作
     *
     * @return
     */
    private boolean isPullUp() {
        return (mYDown - mLastY) >= mTouchSlop;
    }

    /**
     * 如果到了最底部,而且是上拉操作.那么执行onLoad方法
     */
    private void loadData() {
        if (mOnLoadListener != null) {
            // 设置状态
            setLoading(true);
            //
            mOnLoadListener.onLoad();
        }
    }

    /**
     * @param loading
     */
    public void setLoading(boolean loading) {
        isLoading = loading;
        if (isLoading) {
            if (mListView != null){
                mListView.addFooterView(mListViewFooter);
            }
        } else {
            if (mListView != null && mListView instanceof ListView && mListView.getFooterViewsCount() > 0) {
                try {
                    mListView.removeFooterView(mListViewFooter);
                } catch (Exception e) {
                }
                mYDown = 0;
                mLastY = 0;
            }else if (mGridView != null && mGridView instanceof GridView) {
                mYDown = 0;
                mLastY = 0;
            }
        }
    }

    /**
     * @param loadListener
     */
    public void setOnLoadListener(OnLoadListener loadListener) {
        mOnLoadListener = loadListener;
    }

    /**
     * @param moveListener
     */
    public void setOnMoveListener(OnRMoveListener moveListener) {
        mMoveListenter = moveListener;
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (mMoveListenter != null) {
            mMoveListenter.onMove();
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        // 滚动时到了最底部也可以加载更多
        if (canLoad()) {
            loadData();
        }
//		LogUtil.d(TAG, "onScroll===============>>>>>>>>>");
        // if (mMoveListenter != null) {
        // mMoveListenter.onMove();
        // }
    }

    /**
     * 设置刷新
     */
    public static void setRefreshing(SwipeRefreshLayout refreshLayout, boolean refreshing, boolean notify) {
        Class<? extends SwipeRefreshLayout> refreshLayoutClass = refreshLayout.getClass();
        if (refreshLayoutClass != null) {

            try {
                Method setRefreshing = refreshLayoutClass.getDeclaredMethod("setRefreshing", boolean.class, boolean.class);
                setRefreshing.setAccessible(true);
                setRefreshing.invoke(refreshLayout, refreshing, notify);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 加载更多的监听器
     *
     * @author mrsimple
     */
    public interface OnRMoveListener {
        public void onMove();
    }

    /**
     * 加载更多的监听器
     *
     * @author mrsimple
     */
    public interface OnLoadListener {
        public void onLoad();
    }
}