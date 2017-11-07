package connect.activity.contact;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import connect.activity.base.BaseActivity;
import connect.activity.contact.adapter.RecommendAdapter;
import connect.activity.contact.bean.SourceType;
import connect.activity.home.view.LineDecoration;
import connect.ui.activity.R;
import connect.utils.ActivityUtil;
import connect.utils.UriUtil;
import connect.utils.cryption.DecryptionUtil;
import connect.utils.okhttp.OkHttpUtil;
import connect.utils.okhttp.ResultCall;
import connect.widget.TopToolBar;
import connect.widget.pullTorefresh.EndlessScrollListener;
import protos.Connect;

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
        requestRecommendUser();
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
        //recyclerview.addOnScrollListener(endlessScrollListener);
    }

    @OnClick(R.id.left_img)
    void goBack(View view) {
        ActivityUtil.goBack(mActivity);
    }

    SwipeRefreshLayout.OnRefreshListener onRefreshListener = new SwipeRefreshLayout.OnRefreshListener(){
        @Override
        public void onRefresh() {
            page = 1;
            requestRecommendUser();
        }
    };

    EndlessScrollListener endlessScrollListener = new EndlessScrollListener(){
        @Override
        public void onLoadMore() {
            page++;
            requestRecommendUser();
        }
    };

    private RecommendAdapter.OnAddListener onAddListener = new RecommendAdapter.OnAddListener() {
        @Override
        public void add(int position, Connect.UserInfoBase entity) {
            StrangerInfoActivity.startActivity(mActivity, entity.getUid(), SourceType.RECOMMEND);
        }

        @Override
        public void itemClick(int position, Connect.UserInfoBase entity) {
            StrangerInfoActivity.startActivity(mActivity, entity.getUid(), SourceType.RECOMMEND);
        }

        @Override
        public void deleteItem(int position, Connect.UserInfoBase entity) {
            adapter.closeMenu();

            ArrayList<Connect.UserInfoBase> data = adapter.getData();
            data.remove(entity);
            adapter.notifyDataSetChanged();
        }
    };

    public void requestRecommendUser() {
        // 可能需要分页拉取推荐
        OkHttpUtil.getInstance().postEncrySelf(UriUtil.CONNEXT_V1_USERS_RECOMMEND, ByteString.copyFrom(new byte[]{}),
                new ResultCall<Connect.HttpResponse>() {
                    @Override
                    public void onResponse(Connect.HttpResponse response) {
                        try {
                            Connect.IMResponse imResponse = Connect.IMResponse.parseFrom(response.getBody().toByteArray());
                            Connect.StructData structData = DecryptionUtil.decodeAESGCMStructData(imResponse.getCipherData());
                            if(structData != null){
                                Connect.UsersInfoBase usersInfoBase = Connect.UsersInfoBase.parseFrom(structData.getPlainData());
                                refreshview.setRefreshing(false);
                                adapter.setDataNotify(usersInfoBase.getUsersList(), true);
                                /*if (page > 1) {
                                    adapter.setDataNotify(usersInfoBase.getUsersList(), false);
                                } else {
                                    adapter.setDataNotify(usersInfoBase.getUsersList(), true);
                                }*/
                            }
                        } catch (InvalidProtocolBufferException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(Connect.HttpResponse response) {}
                });
    }

}
