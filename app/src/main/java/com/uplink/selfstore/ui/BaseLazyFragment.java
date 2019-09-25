package com.uplink.selfstore.ui;

/**
 * 项目名称：Pro_selfstoreurance
 * 类描述：
 * 创建人：tuchg
 * 创建时间：17/1/17 10:55
 */
public abstract class BaseLazyFragment extends BaseFragment {

    /**
     * Fragment当前状态是否可见
     */
    private boolean isVisible;

    public boolean isIfVisible() {
        return isVisible;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (getUserVisibleHint()) {
            isVisible = true;
            onVisible();
        } else {
            isVisible = false;
            onInvisible();
        }
    }

    /**
     * 可见
     */
    protected void onVisible() {
        lazyLoad();
    }

    /**
     * 不可见
     */
    protected void onInvisible() {
    }

    /**
     * 延迟加载
     * 子类必须重写此方法
     */
    protected abstract void lazyLoad();
}
