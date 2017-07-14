package connect.activity.wallet;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import connect.activity.base.BaseActivity;
import connect.activity.chat.exts.RedPacketActivity;
import connect.activity.chat.exts.TransferToActivity;
import connect.activity.common.selefriend.SeleUsersActivity;
import connect.activity.wallet.adapter.LatelyTransferAdapter;
import connect.activity.wallet.bean.TransferBean;
import connect.database.green.DaoHelper.ParamManager;
import connect.database.green.bean.ContactEntity;
import connect.ui.activity.R;
import connect.utils.ActivityUtil;
import connect.widget.TopToolBar;

/**
 * Created by Administrator on 2016/12/10.
 */
public class TransferActivity extends BaseActivity {

    @Bind(R.id.toolbar_top)
    TopToolBar toolbarTop;
    @Bind(R.id.transfer_friend_tv)
    TextView transferFriendTv;
    @Bind(R.id.transfer_address_tv)
    TextView transferAddressTv;
    @Bind(R.id.transfer_outVia_tv)
    TextView transferOutViaTv;
    @Bind(R.id.lately_title_tv)
    TextView latelyTitleTv;
    @Bind(R.id.recyclerview)
    RecyclerView recyclerview;


    private TransferActivity mActivity;
    private LatelyTransferAdapter adapter;

    public static void startActivity(Activity activity) {
        ActivityUtil.next(activity, TransferActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet_transfer);
        ButterKnife.bind(this);
        initView();
    }

    @Override
    public void initView() {
        mActivity = this;
        toolbarTop.setBlackStyle();
        toolbarTop.setLeftImg(R.mipmap.back_white);
        toolbarTop.setTitle(null, R.string.Wallet_Transfer);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mActivity);
        recyclerview.setLayoutManager(linearLayoutManager);
        adapter = new LatelyTransferAdapter(mActivity);
        recyclerview.setAdapter(adapter);
        adapter.setItemClickListener(new LatelyTransferAdapter.OnItemClickListener() {
            @Override
            public void itemClick(TransferBean transferBean) {
                switch (transferBean.getType()) {
                    case 1:
                        PacketActivity.startActivity(mActivity);
                        break;
                    case 2:
                        TransferOutViaActivity.startActivity(mActivity);
                        break;
                    case 3:
                        TransferAddressActivity.startActivity(mActivity, transferBean.getAddress());
                        break;
                    case 4:
                        TransferToActivity.startActivity(mActivity, transferBean.getAddress());
                        break;
                    case 5:
                        RedPacketActivity.startActivity(mActivity, 1, transferBean.getAddress());
                        break;
                    default:
                        break;
                }
            }
        });
        queryTranfer();
    }

    @OnClick(R.id.left_img)
    void goback(View view) {
        ActivityUtil.goBack(mActivity);
    }

    @OnClick(R.id.transfer_friend_tv)
    void goFriend(View view) {
        SeleUsersActivity.startActivity(mActivity, SeleUsersActivity.SOURCE_FRIEND, "", null);
    }

    @OnClick(R.id.transfer_address_tv)
    void goAddress(View view) {
        TransferAddressActivity.startActivity(mActivity, "");
    }

    @OnClick(R.id.transfer_outVia_tv)
    void goOutVia(View view) {
        TransferOutViaActivity.startActivity(mActivity);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SeleUsersActivity.CODE_REQUEST && resultCode == RESULT_OK) {
            ArrayList<ContactEntity> friendList = (ArrayList<ContactEntity>) data.getExtras().getSerializable("list");
            TransferFriendActivity.startActivity(mActivity, friendList, "");
        }
    }

    private void queryTranfer() {
        new AsyncTask<Void, Void, List<TransferBean>>() {
            @Override
            protected List<TransferBean> doInBackground(Void... params) {
                return ParamManager.getInstance().getLatelyTransfer();
            }

            @Override
            protected void onPostExecute(List<TransferBean> transEntities) {
                super.onPostExecute(transEntities);
                if (transEntities.size() > 0)
                    latelyTitleTv.setVisibility(View.VISIBLE);
                adapter.setDataNotigy(transEntities);
            }
        }.execute();
    }
}
