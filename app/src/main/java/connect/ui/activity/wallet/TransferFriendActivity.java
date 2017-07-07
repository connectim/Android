package connect.ui.activity.wallet;

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
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import connect.db.MemoryDataManager;
import connect.db.green.bean.ContactEntity;
import connect.ui.activity.R;
import connect.ui.activity.set.PayFeeActivity;
import connect.ui.activity.wallet.adapter.FriendGridAdapter;
import connect.ui.activity.wallet.bean.FriendSeleBean;
import connect.ui.activity.wallet.bean.TranAddressBean;
import connect.ui.activity.wallet.contract.TransferFriendContract;
import connect.ui.activity.wallet.presenter.TransferFriendPresenter;
import connect.utils.transfer.TransferUtil;
import connect.ui.base.BaseActivity;
import connect.utils.ActivityUtil;
import connect.utils.data.RateFormatUtil;
import connect.view.TopToolBar;
import connect.view.payment.PaymentPwd;
import connect.utils.transfer.TransferEditView;

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
    @Bind(R.id.listView)
    GridView listView;
    @Bind(R.id.sele_friend_img)
    ImageView seleFriendImg;

    private TransferFriendActivity mActivity;
    private TransferFriendContract.Presenter presenter;
    private TransferUtil transaUtil;
    private FriendGridAdapter friendGridAdapter;
    private final int BACK_CODE = 102;
    private final int BACK_DEL_CODE = 103;
    private PaymentPwd paymentPwd;

    public static void startActivity(Activity activity, List<ContactEntity> list) {
        Bundle bundle = new Bundle();
        bundle.putSerializable("list", new FriendSeleBean(list));
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
        transferEditView.initView();
    }

    @Override
    public void initView() {
        mActivity = this;
        toolbarTop.setBlackStyle();
        toolbarTop.setLeftImg(R.mipmap.back_white);
        toolbarTop.setTitle(null, R.string.Wallet_Transfer);
        setPresenter(new TransferFriendPresenter(this));

        Bundle bundle = getIntent().getExtras();
        FriendSeleBean friendSeleBean = (FriendSeleBean) bundle.getSerializable("list");
        List<ContactEntity> list = friendSeleBean.getList();
        presenter.setListData(list);

        friendGridAdapter = new FriendGridAdapter();
        listView.setAdapter(friendGridAdapter);
        listView.setOnItemClickListener(presenter.getItemClickListener());

        friendGridAdapter.setNotifyData(list);
        numberTv.setText(getString(R.string.Wallet_transfer_man, list.size()));
        transferEditView.setEditListener(presenter.getOnEditListener());
        presenter.horizontal_layout(listView);

        transaUtil = new TransferUtil();
        paymentPwd = new PaymentPwd();
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
        ActivityUtil.goBack(mActivity);
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
        final long amount = RateFormatUtil.stringToLongBtc(transferEditView.getCurrentBtc());
        ArrayList arrayList = new ArrayList<TranAddressBean>();
        for (ContactEntity friendEntity : presenter.getListFriend()) {
            arrayList.add(new TranAddressBean(friendEntity.getAddress(), amount));
        }
        transaUtil.getOutputTran(mActivity, MemoryDataManager.getInstance().getAddress(), false, arrayList,
                transferEditView.getAvaAmount(), amount, new TransferUtil.OnResultCall() {
                    @Override
                    public void result(String inputString, String outputString) {
                        checkPayPassword(amount, inputString, outputString);
                    }
                });
    }

    private void checkPayPassword(final long amount, final String inputString, final String outputString) {
        if (!TextUtils.isEmpty(outputString)) {
            paymentPwd.showPaymentPwd(mActivity, new PaymentPwd.OnTrueListener() {
                @Override
                public void onTrue() {
                    String samValue = transaUtil.getSignRawTrans(MemoryDataManager.getInstance().getPriKey(), inputString, outputString);
                    presenter.requestSend(amount, samValue,transferEditView.getNote(),paymentPwd);
                }
            });

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == BACK_CODE || requestCode == BACK_DEL_CODE) {
                FriendSeleBean friendSeleBean = (FriendSeleBean) data.getExtras().getSerializable("list");
                List<ContactEntity> list = friendSeleBean.getList();
                presenter.setListData(list);
                presenter.horizontal_layout(listView);
                friendGridAdapter.setNotifyData(list);
                numberTv.setText(getString(R.string.Wallet_transfer_man, list.size()));
                presenter.checkBtnEnable();
            }
        }
    }

}
