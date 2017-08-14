package connect.activity.wallet;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import connect.activity.base.BaseActivity;
import connect.activity.base.BaseApplication;
import connect.activity.chat.bean.MsgSend;
import connect.database.green.bean.ContactEntity;
import connect.im.bean.MsgType;
import connect.ui.activity.R;
import connect.activity.common.selefriend.SeleUsersActivity;
import connect.activity.set.PayFeeActivity;
import connect.activity.wallet.adapter.FriendGridAdapter;
import connect.activity.wallet.bean.FriendSeleBean;
import connect.activity.wallet.contract.TransferFriendContract;
import connect.activity.wallet.presenter.TransferFriendPresenter;
import connect.utils.ActivityUtil;
import connect.utils.ToastEUtil;
import connect.wallet.cwallet.business.TransferEditView;
import connect.wallet.cwallet.bean.CurrencyEnum;
import connect.wallet.cwallet.business.BaseBusiness;
import connect.wallet.cwallet.inter.WalletListener;
import connect.widget.TopToolBar;
import connect.widget.random.RandomVoiceActivity;

/**
 * Transfer to friend
 * Created by Administrator on 2016/12/20.
 */
public class TransferFriendActivity extends BaseActivity implements TransferFriendContract.View {

    @Bind(R.id.toolbar_top)
    TopToolBar toolbarTop;
    @Bind(R.id.transfer_edit_view)
    TransferEditView transferEditView;
    @Bind(R.id.ok_btn)
    Button okBtn;
    @Bind(R.id.number_tv)
    TextView numberTv;
    @Bind(R.id.gridview)
    GridView gridview;
    @Bind(R.id.sele_friend_img)
    ImageView seleFriendImg;

    private TransferFriendActivity mActivity;
    private TransferFriendContract.Presenter presenter;
    private FriendGridAdapter friendGridAdapter;
    private final int BACK_CODE = 102;
    private final int BACK_DEL_CODE = 103;
    private String pubGroup;
    private BaseBusiness baseBusiness;

    public static void startActivity(Activity activity, List<ContactEntity> list,String pubGroup) {
        Bundle bundle = new Bundle();
        bundle.putSerializable("list", new FriendSeleBean(list));
        bundle.putString("pubGroup",pubGroup);
        ActivityUtil.next(activity, TransferFriendActivity.class, bundle);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet_transfer_friend);
        ButterKnife.bind(this);
        initView();
    }

    @Override
    protected void onStart() {
        super.onStart();
        transferEditView.initView(mActivity);
    }

    @Override
    public void initView() {
        mActivity = this;
        toolbarTop.setBlackStyle();
        toolbarTop.setLeftImg(R.mipmap.back_white);
        toolbarTop.setTitle(null, R.string.Wallet_Transfer);
        setPresenter(new TransferFriendPresenter(this));

        Bundle bundle = getIntent().getExtras();
        pubGroup = bundle.getString("pubGroup","");
        FriendSeleBean friendSeleBean = (FriendSeleBean) bundle.getSerializable("list");
        List<ContactEntity> list = friendSeleBean.getList();
        presenter.setListData(list);

        friendGridAdapter = new FriendGridAdapter();
        gridview.setAdapter(friendGridAdapter);
        gridview.setOnItemClickListener(presenter.getItemClickListener());

        friendGridAdapter.setNotifyData(list);
        numberTv.setText(getString(R.string.Wallet_transfer_man, list.size()));
        transferEditView.setEditListener(presenter.getOnEditListener());
        presenter.horizontal_layout(gridview);

        baseBusiness = new BaseBusiness(mActivity, CurrencyEnum.BTC);
    }

    @Override
    public void setPresenter(TransferFriendContract.Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public Activity getActivity() {
        return mActivity;
    }

    @Override
    public void addTranferFriend() {
        ArrayList<ContactEntity> list = new ArrayList<ContactEntity>();
        list.addAll(presenter.getListFriend());
        if(TextUtils.isEmpty(pubGroup)){
            SeleUsersActivity.startActivity(mActivity, SeleUsersActivity.SOURCE_FRIEND, "",list);
        }else{
            SeleUsersActivity.startActivity(mActivity, SeleUsersActivity.SOURCE_GROUP, pubGroup,list);
        }
    }

    @Override
    public void setPayFee() {
        PayFeeActivity.startActivity(mActivity);
    }

    @Override
    public String getCurrentBtc() {
        return transferEditView.getCurrentBtc();
    }

    @Override
    public void setBtnEnabled(boolean isEnable) {
        okBtn.setEnabled(isEnable);
    }

    @OnClick(R.id.left_img)
    void goback(View view) {
        ActivityUtil.goBack(mActivity);
    }

    @OnClick(R.id.sele_friend_img)
    void goSeleFriend(View view) {
        TransferFriendDelActivity.startActivity(mActivity, BACK_DEL_CODE, presenter.getListFriend());
    }

    @OnClick(R.id.ok_btn)
    void goTransferOut(View view) {
        final HashMap<String,Long> outMap = new HashMap<>();
        for (ContactEntity friendEntity : presenter.getListFriend()) {
            outMap.put(friendEntity.getPub_key(),transferEditView.getCurrentBtcLong());
        }
        baseBusiness.transferConnectUser(null, outMap, new WalletListener<String>() {
            @Override
            public void success(String value) {
                ToastEUtil.makeText(mActivity,R.string.Link_Send_successful).show();

                if (!TextUtils.isEmpty(pubGroup)) {
                    MsgSend.sendOuterMsg(MsgType.Transfer, value, transferEditView.getCurrentBtcLong(), transferEditView.getNote(), 2);
                }
                for (HashMap.Entry<String, Long> entry : outMap.entrySet()) {
                    presenter.sendTransferMessage(value, entry.getKey(), "");
                }
                List<Activity> list = BaseApplication.getInstance().getActivityList();
                for (Activity activity : list) {
                    if (activity.getClass().getName().equals(TransferActivity.class.getName())) {
                        activity.finish();
                    }
                }
                finish();
            }

            @Override
            public void fail(WalletError error) {
                ToastEUtil.makeText(mActivity,R.string.Login_Send_failed).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == BACK_CODE || requestCode == BACK_DEL_CODE) {
                FriendSeleBean friendSeleBean = (FriendSeleBean) data.getExtras().getSerializable("list");
                List<ContactEntity> list = friendSeleBean.getList();
                presenter.setListData(list);
                presenter.horizontal_layout(gridview);
                friendGridAdapter.setNotifyData(list);
                numberTv.setText(getString(R.string.Wallet_transfer_man, list.size()));
                presenter.checkBtnEnable();
            }else if(requestCode == SeleUsersActivity.CODE_REQUEST){
                ArrayList<ContactEntity> friendList = (ArrayList<ContactEntity>) data.getExtras().getSerializable("list");
                presenter.setListData(friendList);
                presenter.horizontal_layout(gridview);
                friendGridAdapter.setNotifyData(friendList);
                numberTv.setText(getString(R.string.Wallet_transfer_man, friendList.size()));
                presenter.checkBtnEnable();
            } else if(requestCode == RandomVoiceActivity.REQUEST_CODE){
                transferEditView.createWallet(data);
            }
        }
    }
}
