package connect.ui.activity.wallet;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import connect.db.green.DaoHelper.ParamManager;
import connect.db.green.DaoHelper.TransactionHelper;
import connect.db.green.bean.ContactEntity;
import connect.db.green.bean.TransactionEntity;
import connect.ui.activity.R;
import connect.ui.activity.chat.exts.RedPacketActivity;
import connect.ui.activity.chat.exts.TransferToActivity;
import connect.ui.activity.common.selefriend.SeleUsersActivity;
import connect.ui.activity.wallet.adapter.LatelyTransferAdapter;
import connect.ui.activity.wallet.bean.TransferBean;
import connect.ui.base.BaseActivity;
import connect.utils.ActivityUtil;
import connect.view.TopToolBar;

/**
 *
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
    @Bind(R.id.list_view)
    ListView listView;
    @Bind(R.id.lately_title_tv)
    TextView latelyTitleTv;

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

        adapter = new LatelyTransferAdapter();
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(clickListener);
        queryTranfer();
    }

    @OnClick(R.id.left_img)
    void goback(View view) {
        ActivityUtil.goBack(mActivity);
    }

    @OnClick(R.id.transfer_friend_tv)
    void goFriend(View view) {
        SeleUsersActivity.startActivity(mActivity, SeleUsersActivity.SOURCE_FRIEND, "",null);
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
        if(requestCode == SeleUsersActivity.CODE_REQUEST && resultCode == RESULT_OK){
            ArrayList<ContactEntity> friendList = (ArrayList<ContactEntity>) data.getExtras().getSerializable("list");
            TransferFriendActivity.startActivity(mActivity, friendList,"");
        }
    }

    private AdapterView.OnItemClickListener clickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            TransferBean transEntity = (TransferBean) parent.getAdapter().getItem(position);
            //1. outer lucky packet 2. outer transfer 3. address transfer 4. transfer to friend  5. friend lucky packet
            switch (transEntity.getType()) {
                case 1:
                    PacketActivity.startActivity(mActivity);
                    break;
                case 2:
                    TransferOutViaActivity.startActivity(mActivity);
                    break;
                case 3:
                    TransferAddressActivity.startActivity(mActivity, transEntity.getAddress());
                    break;
                case 4:
                    TransferToActivity.startActivity(mActivity, transEntity.getAddress());
                    break;
                case 5:
                    RedPacketActivity.startActivity(mActivity,1,transEntity.getAddress());
                    break;
                default:
                    break;
            }
        }
    };

    private void queryTranfer() {
        new AsyncTask<Void, Void, List<TransferBean>>() {
            @Override
            protected List<TransferBean> doInBackground(Void... params) {
                return ParamManager.getInstance().getLatelyTransfer();
            }

            @Override
            protected void onPostExecute(List<TransferBean> transEntities) {
                super.onPostExecute(transEntities);
                if(transEntities.size() > 0)
                    latelyTitleTv.setVisibility(View.VISIBLE);
                adapter.setDataNotigy(transEntities);
            }
        }.execute();
    }
}
