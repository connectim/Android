package connect.activity.wallet;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import connect.database.MemoryDataManager;
import connect.ui.activity.R;
import connect.activity.set.PayFeeActivity;
import connect.activity.wallet.bean.SendOutBean;
import connect.activity.wallet.contract.PacketContract;
import connect.activity.wallet.presenter.PacketPresenter;
import connect.utils.transfer.TransferUtil;
import connect.activity.base.BaseActivity;
import connect.utils.ActivityUtil;
import connect.utils.data.RateFormatUtil;
import connect.widget.TopToolBar;
import connect.widget.payment.PaymentPwd;
import connect.utils.transfer.TransferEditView;

/**
 * lucky packet
 * Created by Administrator on 2016/12/10.
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
    private TransferUtil transaUtil;
    private PaymentPwd paymentPwd;
    private final String defilet_num = "1";

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
        transferEditView.initView();
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
        transaUtil = new TransferUtil();

        presenter.start();
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
    void goback(View view) {
        ActivityUtil.goBack(mActivity);
    }

    @OnClick(R.id.right_lin)
    void goHistory(View view) {
        PacketHistoryActivity.startActivity(mActivity);
    }

    @OnClick(R.id.pay)
    void finish(View view) {
        final long amount = RateFormatUtil.stringToLongBtc(transferEditView.getCurrentBtc());
        if(null == presenter.getPendingPackage())
            return;
        transaUtil.getOutputTran(mActivity, MemoryDataManager.getInstance().getAddress(), true,
                presenter.getPendingPackage().getAddress(), transferEditView.getAvaAmount(),amount,
                new TransferUtil.OnResultCall(){
            @Override
            public void result(String inputString, String outputString) {
                checkPayPassword(amount, inputString, outputString);
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
        PayFeeActivity.startActivity(mActivity);
    }

    @Override
    public void goinPacketSend(SendOutBean sendOutBean) {
        PacketSendActivity.startActivity(mActivity, sendOutBean);
    }

    private void checkPayPassword(final long amount, final String inputString, final String outputString) {
        if (!TextUtils.isEmpty(outputString)) {
            paymentPwd = new PaymentPwd();
            paymentPwd.showPaymentPwd(mActivity, new PaymentPwd.OnTrueListener() {
                @Override
                public void onTrue() {
                    String samValue = transaUtil.getSignRawTrans(MemoryDataManager.getInstance().getPriKey(), inputString, outputString);
                    presenter.sendPacket(amount, samValue,transferEditView.getNote(),paymentPwd);
                }
            });
        }
    }

}
