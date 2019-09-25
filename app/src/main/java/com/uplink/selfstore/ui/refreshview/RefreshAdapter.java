package com.uplink.selfstore.ui.refreshview;

import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.uplink.selfstore.R;
import com.uplink.selfstore.utils.LogUtil;

/**
 * 项目名称：Pro_selfstoreurance
 * 类描述：
 * 创建人：tuchg
 * 创建时间：17/1/20 11:32
 * 支持加载更多的代理适配器
 */
public abstract class RefreshAdapter extends RecyclerView.Adapter {

    private String TAG = "RefreshAdapter";

    static final int STATE_MORE = 0, STATE_LOAIND = 1, STATE_END = 2, STATE_ERROR = 3;
    int state = STATE_MORE;

    private MyBottomClickListener listenter;

    protected void setOnBottomClickListener(MyBottomClickListener listenter) {
        this.listenter = listenter;
    }

    public void setState(int state) {
        if (this.state != state) {
            this.state = state;
            notifyItemChanged(getItemCount() - 1);
        }
    }

    public int getState() {
        return state;
    }

    @Override
    public int getItemViewType(int position) {
        if (showLoadMore && position == getItemCount() - 1) {
            return -99;
        }
        return getItemType(position);
    }

    private boolean showLoadMore = true;

    public void showLoadMoreEnable(boolean enable) {
        this.showLoadMore = enable;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == -99) {
            return new RecyclerView.ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.loadmore_default_footer, parent, false)) {
            };
        } else {
            return onCreateItemHolder(parent, viewType);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == -99) {
            ProgressBar progressBar = (ProgressBar) holder.itemView.findViewById(R.id.loadmore_default_footer_progressbar);
            TextView textView = (TextView) holder.itemView.findViewById(R.id.loadmore_default_footer_tv);
            if (state == STATE_END) {
                progressBar.setVisibility(View.GONE);
                textView.setText("没有更多了");
            } else if (state == STATE_MORE) {
                progressBar.setVisibility(View.GONE);
                textView.setText("点击加载");
            } else if (state == STATE_LOAIND) {
                progressBar.setVisibility(View.VISIBLE);
                textView.setText("加载中...");
            } else if (state == STATE_ERROR) {
                progressBar.setVisibility(View.GONE);
                textView.setText("加载失败,点击重新加载");
            }
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    LogUtil.e(TAG, "====>>>>>>>>bottom onClick()");
                    if (listenter != null && !isRefresh && (state == STATE_MORE || state == STATE_ERROR)) {
                        setState(STATE_LOAIND);
                        listenter.onBottomClick(view);
                    }
                }
            });
        } else {
            onBindItemHolder(holder, position);
        }
    }

    @Override
    public int getItemCount() {
        return getCount() == 0 ? 0 : showLoadMore ? getCount() + 1 : getCount();
    }

    public int getItemType(int position) {
        return super.getItemViewType(position);
    }

    public abstract MyViewHolder onCreateItemHolder(ViewGroup parent, int viewType);

    public void onBindItemHolder(RecyclerView.ViewHolder holder, int position) {
    }

    public abstract int getCount();

    @Override
    public void onViewAttachedToWindow(RecyclerView.ViewHolder holder) {
        // 处理瀑布流模式 最后的 item 占整行
        if (showLoadMore)
            if (holder.getLayoutPosition() == getItemCount() - 1) {
                ViewGroup.LayoutParams lp = holder.itemView.getLayoutParams();
                if (lp != null && lp instanceof StaggeredGridLayoutManager.LayoutParams) {
                    StaggeredGridLayoutManager.LayoutParams p = (StaggeredGridLayoutManager.LayoutParams) lp;
                    p.setFullSpan(true);
                }
            }
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        // 处理网格布局模式 最后的 item 占整行
        final RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        if (layoutManager instanceof GridLayoutManager) {
            GridLayoutManager gridManager = ((GridLayoutManager) layoutManager);
            final GridLayoutManager.SpanSizeLookup spanSizeLookup = gridManager.getSpanSizeLookup();
            final int lastSpanCount = gridManager.getSpanCount();
            gridManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    if (!showLoadMore) {
                        return spanSizeLookup.getSpanSize(position);
                    } else
                        return position == getItemCount() - 1 ? lastSpanCount :
                                (spanSizeLookup == null ? 1 : spanSizeLookup.getSpanSize(position));
                }
            });
        }
    }

    private boolean isRefresh = false;

    public void setIsRefresh(boolean isRefresh) {
        this.isRefresh = isRefresh;
    }

    public boolean isRefresh(boolean isRefresh) {
        return this.isRefresh;
    }
}
