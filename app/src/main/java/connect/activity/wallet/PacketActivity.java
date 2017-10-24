package connect.activity.wallet;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import com.wallet.bean.CurrencyEnum;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import connect.activity.base.BaseActivity;
import connect.activity.set.SafetyPayFeeActivity;
import connect.activity.wallet.bean.SendOutBean;
import connect.activity.wallet.bean.TransferBean;
import connect.activity.wallet.contract.PacketContract;
import connect.activity.wallet.manager.TransferManager;
import connect.activity.wallet.presenter.PacketPresenter;
import connect.database.green.DaoHelper.ParamManager;
import connect.ui.activity.R;
import connect.utils.ActivityUtil;
import connect.utils.ToastEUtil;
import connect.activity.wallet.manager.TransferEditView;
import com.wallet.inter.WalletListener;
import connect.widget.TopToolBar;
import connect.widget.random.RandomVoiceActivity;

/**
 * lucky packet
 */
public class PacketActivity extends BaseActivity implements PacketContract.View{

    @Bind(R.id.toolbar_top)
    TopToolBar toolbarTop;
    @Bind(R.id.packet_number_et)
    EditText packetNumberEt;
    @Bind(R.id.pay)
    Button pay;
    @Bind(R.id.transfer_edit_view)
    TransferEditView transferEditView;

    private PacketActivity mActivity;
    private PacketContract.Presenter presenter;
    private final String defilet_num = "1";
    private TransferManager transferManager;

    public static void startActivity(Activity activity) {
        Bundle bundle = new Bundle();
        ActivityUtil.next(activity, PacketActivity.class, bundle);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        setContentView(R.layout.activity_wallet_packet);
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
        toolbarTop.setRedStyle();
        toolbarTop.setLeftImg(R.mipmap.back_white);
        toolbarTop.setTitle(null, R.string.Wallet_Sent_via_link_luck_packet);
        toolbarTop.setRightText(R.string.Chat_History);
        setPresenter(new PacketPresenter(this));

        packetNumberEt.setText(defilet_num);
        packetNumberEt.addTextChangedListener(presenter.getNumberWatcher());
        transferEditView.setNote(getString(R.string.Wallet_Best_wishes));
        transferEditView.setEditListener(presenter.getEditListener());
        presenter.start();
        transferManager = new TransferManager(mActivity, CurrencyEnum.BTC);
    }

    @Override
    public void setPresenter(PacketContract.Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public Activity getActivity() {
        return mActivity;
    }

    @OnClick(R.id.left_img)
    void goBack(View view) {
        ActivityUtil.goBack(mActivity);
    }

    @OnClick(R.id.right_lin)
    void goHistory(View view) {
        PacketHistoryActivity.startActivity(mActivity);
    }

    @OnClick(R.id.pay)
    void finish(View view) {
        transferManager.luckyPacket(null, "", 1, 0, Integer.valueOf(packetNumberEt.getText().toString()),
                transferEditView.getCurrentBtcLong(), transferEditView.getNote(), new WalletListener<String>() {
            @Override
            public void success(String hashId) {
                ParamManager.getInstance().putLatelyTransfer(new TransferBean(1,"","",""));
                presenter.getPacketDetail(hashId);
                ToastEUtil.makeText(mActivity,R.string.Link_Send_successful).show();
            }

            @Override
            public void fail(WalletError error) {
                ToastEUtil.makeText(mActivity,R.string.Login_Send_failed).show();
            }
        });
    }

    @Override
    public String getCurrentBtc() {
        return transferEditView.getCurrentBtc();
    }

    @Override
    public void setPayBtnEnable(boolean isEnable) {
        pay.setEnabled(isEnable);
    }

    @Override
    public String getPacketNumber() {
        return packetNumberEt.getText().toString();
    }

    @Override
    public void setPayFee() {
        SafetyPayFeeActivity.startActivity(mActivity);
    }

    @Override
    public void goPacketView(SendOutBean sendOutBean) {
        PacketSendActivity.startActivity(mActivity, sendOutBean);
    }
}
