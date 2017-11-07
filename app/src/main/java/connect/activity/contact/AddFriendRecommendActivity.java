package connect.activity.contact;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import connect.activity.base.BaseActivity;
import connect.activity.contact.adapter.RecommendAdapter;
import connect.activity.contact.bean.SourceType;
import connect.activity.home.view.LineDecoration;
import connect.database.green.DaoHelper.ContactHelper;
import connect.database.green.bean.RecommandFriendEntity;
import connect.ui.activity.R;
import connect.utils.ActivityUtil;
import connect.widget.TopToolBar;
import connect.widget.pullTorefresh.EndlessScrollListener;

/**
 * Recommend friends
 */
public class AddFriendRecommendActivity extends BaseActivity {

    @Bind(R.id.toolbar)
    TopToolBar toolbar;
    @Bind(R.id.recyclerview)
    RecyclerView recyclerview;
    @Bind(R.id.refreshview)
    SwipeRefreshLayout refreshview;

    private AddFriendRecommendActivity mActivity;
    private RecommendAdapter adapter;
    private int MAX_RECOMMEND_COUNT = 20;
    private int page = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_recommend);
        ButterKnife.bind(this);
        initView();
    }

    @Override
    protected void onStart() {
        super.onStart();
        page = 1;
        queryRecommend();
    }

    @Override
    public void initView() {
        mActivity = this;
        toolbar.setBlackStyle();
        toolbar.setLeftImg(R.mipmap.back_white);
        toolbar.setTitle(null, R.string.Link_Friend_Recommendation);

        refreshview.setColorSchemeResources(R.color.color_ebecee, R.color.color_c8ccd5, R.color.color_lightgray);
        refreshview.setOnRefreshListener(onRefreshListener);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mActivity);
        recyclerview.setLayoutManager(linearLayoutManager);
        recyclerview.addItemDecoration(new LineDecoration(mActivity));
        adapter = new RecommendAdapter(mActivity);
        adapter.setOnAddListener(onAddListener);
        recyclerview.setAdapter(adapter);
        recyclerview.addOnScrollListener(endlessScrollListener);
    }

    SwipeRefreshLayout.OnRefreshListener onRefreshListener = new SwipeRefreshLayout.OnRefreshListener(){
        @Override
        public void onRefresh() {
            refreshview.setRefreshing(false);
        }
    };

    EndlessScrollListener endlessScrollListener = new EndlessScrollListener(){
        @Override
        public void onLoadMore() {
            page++;
            queryRecommend();
        }
    };

    private RecommendAdapter.OnAddListener onAddListener = new RecommendAdapter.OnAddListener() {
        @Override
        public void add(int position, RecommandFriendEntity entity) {
            StrangerInfoActivity.startActivity(mActivity, entity.getAddress(), SourceType.RECOMMEND);
        }

        @Override
        public void itemClick(int position, RecommandFriendEntity entity) {
            StrangerInfoActivity.startActivity(mActivity, entity.getAddress(), SourceType.RECOMMEND);
        }

        @Override
        public void deleteItem(int position, RecommandFriendEntity entity) {
            //qwert

            ContactHelper.getInstance().removeRecommendEntity(entity.getPub_key());
            adapter.closeMenu();

            ArrayList<RecommandFriendEntity> data = adapter.getData();
            data.remove(entity);
            adapter.notifyDataSetChanged();
        }
    };

    @OnClick(R.id.left_img)
    void goBack(View view) {
        ActivityUtil.goBack(mActivity);
    }

    private void queryRecommend() {
        new AsyncTask<Void, Void, List<RecommandFriendEntity>>() {
            @Override
            protected List<RecommandFriendEntity> doInBackground(Void... params) {
                List<RecommandFriendEntity> list = ContactHelper.getInstance().loadRecommendEntity(page, MAX_RECOMMEND_COUNT);
                return list;
            }

            @Override
            protected void onPostExecute(List<RecommandFriendEntity> recommendEntities) {
                super.onPostExecute(recommendEntities);
                refreshview.setRefreshing(false);
                if (page > 1) {
                    adapter.setDataNotify(recommendEntities, false);
                } else {
                    adapter.setDataNotify(recommendEntities, true);
                }
            }
        }.execute();
    }

}
