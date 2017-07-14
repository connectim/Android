package connect.widget.pullTorefresh;

/**
 * Created by Administrator on 2017/7/14.
 */

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

public abstract class EndlessScrollListener extends  RecyclerView.OnScrollListener{

    private LinearLayoutManager mLinearLayoutManager;
    private int totalItemCount;
    private int previousTotal = 0;
    private int visibleItemCount;
    private int firstVisibleItem;
    private boolean loading = true;

    public EndlessScrollListener(LinearLayoutManager linearLayoutManager) {
        this.mLinearLayoutManager = linearLayoutManager;
    }

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);
        visibleItemCount = recyclerView.getChildCount();
        totalItemCount = mLinearLayoutManager.getItemCount();
        firstVisibleItem = mLinearLayoutManager.findFirstVisibleItemPosition();

        if (loading) {
            if (totalItemCount > previousTotal) {
                loading = false;
                previousTotal = totalItemCount;
            }
        }

        if (!loading && totalItemCount - visibleItemCount <= firstVisibleItem) {
            onLoadMore();
            loading = true;
        }
    }

    public abstract void onLoadMore();
}