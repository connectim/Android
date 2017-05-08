package connect.ui.activity.contact;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import connect.db.SharedPreferenceUtil;
import connect.db.green.bean.ContactEntity;
import connect.ui.activity.R;
import connect.ui.activity.contact.adapter.FriendRecordAdapter;
import connect.ui.activity.login.bean.UserBean;
import connect.ui.activity.wallet.BlockchainActivity;
import connect.ui.base.BaseActivity;
import connect.utils.ActivityUtil;
import connect.utils.UriUtil;
import connect.utils.cryption.DecryptionUtil;
import connect.utils.okhttp.OkHttpUtil;
import connect.utils.okhttp.ResultCall;
import connect.view.TopToolBar;
import connect.view.pullTorefresh.XListView;
import protos.Connect;

/**
 * Created by Administrator on 2017/4/26 0026.
 */

public class FriendRecordActivity extends BaseActivity {

    @Bind(R.id.toolbar_top)
    TopToolBar toolbarTop;
    @Bind(R.id.list_view)
    XListView listView;
    private FriendRecordActivity mActivity;
    private int page = 1;
    private int MAX_RECOMMEND_COUNT = 20;
    private ContactEntity friendEntity;
    private FriendRecordAdapter adapter;

    public static void startActivity(Activity activity, ContactEntity friendEntity) {
        Bundle bundle = new Bundle();
        bundle.putSerializable("friend", friendEntity);
        ActivityUtil.next(activity, FriendRecordActivity.class, bundle);
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

        adapter = new FriendRecordAdapter(friendEntity);
        listView.setPullRefreshEnable(false);
        listView.setAdapter(adapter);
        listView.setXListViewListener(new XListView.IXListViewListener() {
            @Override
            public void onRefresh() {

            }

            @Override
            public void onLoadMore() {
                page++;
                requestRecord();
            }
        });
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Connect.FriendBill friendBill = (Connect.FriendBill) parent.getAdapter().getItem(position);
                BlockchainActivity.startActivity(mActivity, friendBill.getTxId());
            }
        });

        requestRecord();
    }

    @OnClick(R.id.left_img)
    void goBack(View view) {
        ActivityUtil.goBack(mActivity);
    }

    private void requestRecord() {
        Connect.FriendRecords friendRecords = Connect.FriendRecords.newBuilder()
                .setSelfAddress(SharedPreferenceUtil.getInstance().getAddress())
                .setFriendAddress(friendEntity.getAddress())
                .setPageSize(MAX_RECOMMEND_COUNT)
                .setPageIndex(page)
                .build();
        OkHttpUtil.getInstance().postEncrySelf(UriUtil.CONNEXT_V1_USERS_FRIEND_RECORDS, friendRecords, new ResultCall<Connect.HttpResponse>() {
            @Override
            public void onResponse(Connect.HttpResponse response) {
                try {
                    Connect.IMResponse imResponse = Connect.IMResponse.parseFrom(response.getBody().toByteArray());
                    Connect.StructData structData = DecryptionUtil.decodeAESGCMStructData(SharedPreferenceUtil.getInstance().getPriKey(),
                            imResponse.getCipherData());
                    Connect.FriendBillsMessage friendBillsMessage = Connect.FriendBillsMessage.parseFrom(structData.getPlainData());
                    List<Connect.FriendBill> listBill = friendBillsMessage.getFriendBillsList();
                    if (listBill.size() >= MAX_RECOMMEND_COUNT) {
                        listView.setPullLoadEnable(true);
                    } else {
                        listView.setPullLoadEnable(false);
                    }
                    if (page > 1) {
                        adapter.setNotifyData(listBill, false);
                    } else {
                        adapter.setNotifyData(listBill, true);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Connect.HttpResponse response) {

            }
        });
    }

}
