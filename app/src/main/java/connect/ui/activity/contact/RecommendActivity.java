package connect.ui.activity.contact;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import connect.db.green.DaoHelper.ContactHelper;
import connect.db.green.bean.RecommandFriendEntity;
import connect.im.bean.UserOrderBean;
import connect.ui.activity.R;
import connect.ui.activity.contact.adapter.RecommendAdapter;
import connect.ui.activity.contact.bean.SourceType;
import connect.ui.base.BaseActivity;
import connect.utils.ActivityUtil;
import connect.view.TopToolBar;
import connect.view.pullTorefresh.XListView;

/**
 * Recommend friends
 * Created by Administrator on 2017/1/21.
 */

public class RecommendActivity extends BaseActivity {

    @Bind(R.id.toolbar)
    TopToolBar toolbar;
    @Bind(R.id.list_view)
    XListView listView;

    private RecommendActivity mActivity;
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

        adapter = new RecommendAdapter();
        adapter.setOnAddListence(onAddListence);
        listView.setPullRefreshEnable(false);
        listView.setAdapter(adapter);
        listView.setXListViewListener(new XListView.IXListViewListener() {
            @Override
            public void onRefresh() {

            }

            @Override
            public void onLoadMore() {
                page ++;
                queryRecommend();
            }
        });
    }

    @OnClick(R.id.left_img)
    void goBack(View view) {
        ActivityUtil.goBack(mActivity);
    }

    private RecommendAdapter.OnAddListence onAddListence = new RecommendAdapter.OnAddListence(){
        @Override
        public void add(int position, RecommandFriendEntity entity) {
            StrangerInfoActivity.startActivity(mActivity,entity.getAddress(), SourceType.RECOMMEND);
        }

        @Override
        public void itemClick(int position, RecommandFriendEntity entity) {
            StrangerInfoActivity.startActivity(mActivity,entity.getAddress(), SourceType.RECOMMEND);
        }

        @Override
        public void deleteItem(int position, RecommandFriendEntity entity) {
            UserOrderBean userOrderBean = new UserOrderBean();
            userOrderBean.noInterested(entity.getAddress(),"Not interested in");

            ContactHelper.getInstance().removeRecommendEntity(entity.getPub_key());
            adapter.closeMenu();

            ArrayList<RecommandFriendEntity> data = adapter.getData();
            data.remove(entity);
            adapter.notifyDataSetChanged();
        }
    };


    private void queryRecommend(){
        new AsyncTask<Void,Void,List<RecommandFriendEntity>>(){
            @Override
            protected List<RecommandFriendEntity> doInBackground(Void... params) {
                List<RecommandFriendEntity> list = ContactHelper.getInstance().loadRecommendEntity(page,MAX_RECOMMEND_COUNT);
                return list;
            }

            @Override
            protected void onPostExecute(List<RecommandFriendEntity> recommendEntities) {
                super.onPostExecute(recommendEntities);
                if(recommendEntities.size() >= MAX_RECOMMEND_COUNT){
                    listView.setPullLoadEnable(true);
                }else{
                    listView.setPullLoadEnable(false);
                }
                if(page > 1){
                    adapter.setDataNotify(recommendEntities,false);
                }else{
                    adapter.setDataNotify(recommendEntities,true);
                }
            }
        }.execute();
    }

}
