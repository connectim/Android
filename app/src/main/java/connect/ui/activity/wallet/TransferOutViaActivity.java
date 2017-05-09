package connect.ui.activity.wallet;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import connect.db.SharedPreferenceUtil;
import connect.db.green.DaoHelper.ParamManager;
import connect.ui.activity.R;
import connect.ui.activity.login.bean.UserBean;
import connect.ui.activity.set.PayFeeActivity;
import connect.ui.activity.wallet.bean.SendOutBean;
import connect.ui.activity.wallet.bean.TransferBean;
import connect.ui.activity.wallet.support.TransaUtil;
import connect.ui.base.BaseActivity;
import connect.utils.ActivityUtil;
import connect.utils.data.RateFormatUtil;
import connect.utils.UriUtil;
import connect.utils.cryption.DecryptionUtil;
import connect.utils.okhttp.OkHttpUtil;
import connect.utils.okhttp.ResultCall;
import connect.view.MdStyleProgress;
import connect.view.TopToolBar;
import connect.view.payment.PaymentPwd;
import connect.view.transferEdit.TransferEditView;
import protos.Connect;

/**
 * Transfer to outer APP
 * Created by Administrator on 2016/12/20.
 */
public class TransferOutViaActivity extends BaseActivity {

    @Bind(R.id.toolbar_top)
    TopToolBar toolbarTop;
    @Bind(R.id.transfer_edit_view)
    TransferEditView transferEditView;
    @Bind(R.id.ok_btn)
    Button okBtn;

    private TransferOutViaActivity mActivity;
    private UserBean userBean;
    private Connect.PendingRedPackage pendingRedPackage;
    private TransaUtil transaUtil;
    private PaymentPwd paymentPwd;

    public static void startActivity(Activity activity) {
        ActivityUtil.next(activity, TransferOutViaActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet_transfer_outvia);
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
        toolbarTop.setRightText(R.string.Chat_History);
        userBean = SharedPreferenceUtil.getInstance().getUser();
        transferEditView.setEditListener(onEditListener);
        getPaddingInfo();
        transaUtil = new TransaUtil();
        paymentPwd = new PaymentPwd();
    }

    @OnClick(R.id.left_img)
    void goback(View view) {
        ActivityUtil.goBack(mActivity);
    }

    @OnClick(R.id.right_lin)
    void goHistory(View view) {
        ActivityUtil.next(mActivity, TransferOutViaHistoryActivity.class);
    }

    @OnClick(R.id.ok_btn)
    void goTransferOut(View view) {
        final long amount = RateFormatUtil.stringToLongBtc(transferEditView.getCurrentBtc());
        transaUtil.getOutputTran(mActivity, userBean.getAddress(), true, pendingRedPackage.getAddress(),
                transferEditView.getAvaAmount(),amount, new TransaUtil.OnResultCall(){
            @Override
            public void result(String inputString, String outputString) {
                checkPayPassword(amount, inputString, outputString);
            }
        });
    }

    private TransferEditView.OnEditListener onEditListener = new TransferEditView.OnEditListener() {
        @Override
        public void onEdit(String value) {
            if (TextUtils.isEmpty(value) ||
                    Double.valueOf(transferEditView.getCurrentBtc()) < 0.0001) {
                okBtn.setEnabled(false);
            } else {
                okBtn.setEnabled(true);
            }
        }

        @Override
        public void setFee() {
            PayFeeActivity.startActivity(mActivity);
        }
    };

    private void checkPayPassword(final long amount, final String inputString, final String outputString) {
        if (!TextUtils.isEmpty(outputString)) {
            paymentPwd.showPaymentPwd(mActivity, new PaymentPwd.OnTrueListener() {
                @Override
                public void onTrue() {
                    String samValue = transaUtil.getSignRawTrans(userBean.getPriKey(), inputString, outputString);
                    sendExternal(amount, samValue);
                }

                @Override
                public void onFalse() {

                }
            });
        }
    }

    private void sendExternal(final long amount, String rawStr) {
        Connect.OrdinaryBilling.Builder builder = Connect.OrdinaryBilling.newBuilder();
        builder.setMoney(amount);
        builder.setRawTx(rawStr);
        builder.setHashId(pendingRedPackage.getHashId());
        if (!TextUtils.isEmpty(transferEditView.getNote())) {
            builder.setHashId(transferEditView.getNote());
        }
        OkHttpUtil.getInstance().postEncrySelf(UriUtil.WALLET_BILLING_EXTERNAL_SEND, builder.build(),
                new ResultCall<Connect.HttpResponse>() {
            @Override
            public void onResponse(Connect.HttpResponse response) {
                try {
                    Connect.IMResponse imResponse = Connect.IMResponse.parseFrom(response.getBody().toByteArray());
                    Connect.StructData structData = DecryptionUtil.decodeAESGCMStructData(userBean.getPriKey(), imResponse.getCipherData());
                    Connect.ExternalBillingInfo billingInfo = Connect.ExternalBillingInfo.parseFrom(structData.getPlainData());

                    final SendOutBean sendOutBean = new SendOutBean();
                    sendOutBean.setType(PacketSendActivity.OUT_VIA);
                    sendOutBean.setUrl(billingInfo.getUrl());
                    sendOutBean.setDeadline(billingInfo.getDeadline());
                    sendOutBean.setHashId(billingInfo.getHash());

                    ParamManager.getInstance().putLatelyTransfer(new TransferBean(2,
                            getResources().getString(R.string.Wallet_Transfer)));
                    paymentPwd.closeStatusDialog(MdStyleProgress.Status.LoadSuccess, new PaymentPwd.OnAnimationListener() {
                        @Override
                        public void onComplete() {
                            PacketSendActivity.startActivity(mActivity, sendOutBean, userBean);
                            finish();
                        }
                    });
                } catch (InvalidProtocolBufferException e) {
                    paymentPwd.closeStatusDialog(MdStyleProgress.Status.LoadFail);
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Connect.HttpResponse response) {
                paymentPwd.closeStatusDialog(MdStyleProgress.Status.LoadFail);
            }
        });
    }

    private void getPaddingInfo() {
        OkHttpUtil.getInstance().postEncrySelf(UriUtil.WALLET_EXTERNAL_PENDING, ByteString.copyFrom(new byte[]{}),
                new ResultCall<Connect.HttpResponse>() {
                    @Override
                    public void onResponse(Connect.HttpResponse response) {
                        try {
                            Connect.IMResponse imResponse = Connect.IMResponse.parseFrom(response.getBody().toByteArray());
                            Connect.StructData structData = DecryptionUtil.decodeAESGCMStructData(userBean.getPriKey(), imResponse.getCipherData());
                            pendingRedPackage = Connect.PendingRedPackage.parseFrom(structData.getPlainData());
                        } catch (InvalidProtocolBufferException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(Connect.HttpResponse response) {

                    }
                });
    }

}
