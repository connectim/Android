package connect.activity.contact;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import connect.activity.base.BaseActivity;
import connect.activity.contact.adapter.FriendRecordAdapter;
import connect.database.SharedPreferenceUtil;
import connect.database.green.bean.ContactEntity;
import connect.ui.activity.R;
import connect.utils.ActivityUtil;
import connect.utils.ProtoBufUtil;
import connect.utils.UriUtil;
import connect.utils.cryption.DecryptionUtil;
import connect.utils.okhttp.OkHttpUtil;
import connect.utils.okhttp.ResultCall;
import connect.widget.TopToolBar;
import connect.widget.pullTorefresh.EndlessScrollListener;
import protos.Connect;

/**
 * Friends transfer records
 */
public class FriendInfoRecordActivity extends BaseActivity {

    @Bind(R.id.toolbar_top)
    TopToolBar toolbarTop;
    @Bind(R.id.recyclerview)
    RecyclerView recyclerview;
    @Bind(R.id.refreshview)
    SwipeRefreshLayout refreshview;
    @Bind(R.id.no_data_lin)
    LinearLayout noDataLin;

    private FriendInfoRecordActivity mActivity;
    private int page = 1;
    private int MAX_RECOMMEND_COUNT = 20;
    private ContactEntity friendEntity;
    private FriendRecordAdapter adapter;

    public static void startActivity(Activity activity, ContactEntity friendEntity) {
        Bundle bundle = new Bundle();
        bundle.putSerializable("friend", friendEntity);
        ActivityUtil.next(activity, FriendInfoRecordActivity.class, bundle);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet_transaction);
        ButterKnife.bind(this);
        initView();
    }

    @Override
    public void initView() {
        mActivity = this;
        toolbarTop.setBlackStyle();
        toolbarTop.setLeftImg(R.mipmap.back_white);
        toolbarTop.setTitle(null, R.string.Link_Tansfer_Record);

        Bundle bundle = getIntent().getExtras();
        friendEntity = (ContactEntity) bundle.getSerializable("friend");

        refreshview.setColorSchemeResources(R.color.color_ebecee, R.color.color_c8ccd5, R.color.color_lightgray);
        refreshview.setOnRefreshListener(onRefreshListener);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mActivity);
        adapter = new FriendRecordAdapter(mActivity, friendEntity);
        recyclerview.setLayoutManager(linearLayoutManager);
        recyclerview.setAdapter(adapter);
        recyclerview.addOnScrollListener(endlessScrollListener);

        adapter.setItemClickListener(onItemClickListener);
        requestRecord();
    }

    @OnClick(R.id.left_img)
    void goBack(View view) {
        ActivityUtil.goBack(mActivity);
    }

    SwipeRefreshLayout.OnRefreshListener onRefreshListener = new SwipeRefreshLayout.OnRefreshListener() {
        @Override
        public void onRefresh() {
            refreshview.setRefreshing(false);
        }
    };

    EndlessScrollListener endlessScrollListener = new EndlessScrollListener() {
        @Override
        public void onLoadMore() {
            page++;
            requestRecord();
        }
    };

    FriendRecordAdapter.OnItemClickListener onItemClickListener = new FriendRecordAdapter.OnItemClickListener() {
        @Override
        public void itemClick(Connect.FriendBill friendBill) {
        }
    };

    private void requestRecord() {
        Connect.FriendRecords friendRecords = Connect.FriendRecords.newBuilder()
                .setSelfAddress(SharedPreferenceUtil.getInstance().getUser().getUid())
                .setFriendAddress(friendEntity.getUid())
                .setPageSize(MAX_RECOMMEND_COUNT)
                .setPageIndex(page)
                .build();
        OkHttpUtil.getInstance().postEncrySelf(UriUtil.CONNEXT_V1_USERS_FRIEND_RECORDS, friendRecords, new ResultCall<Connect.HttpNotSignResponse>() {
            @Override
            public void onResponse(Connect.HttpNotSignResponse response) {
                try {
                    Connect.StructData structData = Connect.StructData.parseFrom(response.getBody());
                    Connect.FriendBillsMessage friendBillsMessage = Connect.FriendBillsMessage.parseFrom(structData.getPlainData());
                    if (friendBillsMessage.getFriendBillsList().size() == 0 && page == 1) {
                        noDataLin.setVisibility(View.VISIBLE);
                        refreshview.setVisibility(View.GONE);
                    } else {
                        ArrayList<Connect.FriendBill> listBill = new ArrayList<>();
                        for (Connect.FriendBill friendBill : friendBillsMessage.getFriendBillsList()) {
                            if (ProtoBufUtil.getInstance().checkProtoBuf(friendBill)) {
                                listBill.add(friendBill);
                            }
                        }
                        if (page > 1) {
                            adapter.setNotifyData(listBill, false);
                        } else {
                            adapter.setNotifyData(listBill, true);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Connect.HttpNotSignResponse response) {}
        });
    }

}
