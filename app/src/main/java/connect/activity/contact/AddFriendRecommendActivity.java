package connect.activity.contact;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import connect.activity.base.BaseActivity;
import connect.activity.contact.adapter.RecommendAdapter;
import connect.activity.home.view.LineDecoration;
import connect.ui.activity.R;
import connect.utils.ActivityUtil;
import connect.utils.UriUtil;
import connect.utils.okhttp.OkHttpUtil;
import connect.utils.okhttp.ResultCall;
import connect.widget.TopToolBar;
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
    }

    @OnClick(R.id.left_img)
    void goBack(View view) {
        ActivityUtil.goBack(mActivity);
    }

    SwipeRefreshLayout.OnRefreshListener onRefreshListener = new SwipeRefreshLayout.OnRefreshListener(){
        @Override
        public void onRefresh() {
            requestRecommendUser();
        }
    };

    private RecommendAdapter.OnAddListener onAddListener = new RecommendAdapter.OnAddListener() {
        @Override
        public void add(int position, Connect.UserInfoBase entity) {
            ContactInfoActivity.lunchActivity(mActivity, entity.getUid());
        }

        @Override
        public void itemClick(int position, Connect.UserInfoBase entity) {
            ContactInfoActivity.lunchActivity(mActivity, entity.getUid());
        }

        @Override
        public void deleteItem(int position, Connect.UserInfoBase entity) {
            requestNoInterest(entity);
        }
    };

    public void requestRecommendUser() {
        /*OkHttpUtil.getInstance().postEncrySelf(UriUtil.CONNEXT_V1_USERS_RECOMMEND, ByteString.copyFrom(new byte[]{}),
                new ResultCall<Connect.HttpResponse>() {
                    @Override
                    public void onResponse(Connect.HttpResponse response) {
                        try {
                            Connect.HttpNotSignResponse imResponse = Connect.HttpNotSignResponse.parseFrom(response.getBody().toByteArray());
                            Connect.StructData structData = Connect.StructData.parseFrom(imResponse.getBody());
                            if(structData != null){
                                Connect.UsersInfoBase usersInfoBase = Connect.UsersInfoBase.parseFrom(structData.getPlainData());
                                refreshview.setRefreshing(false);
                                adapter.setDataNotify(usersInfoBase.getUsersList(), true);
                            }
                        } catch (InvalidProtocolBufferException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(Connect.HttpResponse response) {}
                });*/
    }

    public void requestNoInterest(final Connect.UserInfoBase entity) {
        Connect.NOInterest noInterest = Connect.NOInterest.newBuilder()
                .setUid(entity.getUid())
                .build();
        OkHttpUtil.getInstance().postEncrySelf(UriUtil.CONNEXT_V1_USERS_DISINCLINE, noInterest, new ResultCall<Connect.HttpResponse>() {
            @Override
            public void onResponse(Connect.HttpResponse response) {
                adapter.closeMenu();
                ArrayList<Connect.UserInfoBase> data = adapter.getData();
                data.remove(entity);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onError(Connect.HttpResponse response) {}
        });
    }

}
