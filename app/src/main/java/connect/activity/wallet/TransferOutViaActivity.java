package connect.activity.wallet;

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
import connect.database.MemoryDataManager;
import connect.database.green.DaoHelper.ParamManager;
import connect.ui.activity.R;
import connect.activity.set.PayFeeActivity;
import connect.activity.wallet.bean.SendOutBean;
import connect.activity.wallet.bean.TransferBean;
import connect.utils.ProtoBufUtil;
import connect.utils.transfer.TransferError;
import connect.utils.transfer.TransferUtil;
import connect.activity.base.BaseActivity;
import connect.utils.ActivityUtil;
import connect.utils.data.RateFormatUtil;
import connect.utils.UriUtil;
import connect.utils.cryption.DecryptionUtil;
import connect.utils.okhttp.OkHttpUtil;
import connect.utils.okhttp.ResultCall;
import connect.widget.MdStyleProgress;
import connect.widget.TopToolBar;
import connect.widget.payment.PaymentPwd;
import connect.utils.transfer.TransferEditView;
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
    private Connect.PendingRedPackage pendingRedPackage;
    private TransferUtil transaUtil;
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
        transferEditView.setEditListener(onEditListener);
        getPaddingInfo();
        transaUtil = new TransferUtil();
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
        if(null == pendingRedPackage){
            return;
        }
        transaUtil.getOutputTran(mActivity, MemoryDataManager.getInstance().getAddress(), true, pendingRedPackage.getAddress(),
                transferEditView.getAvaAmount(),amount, new TransferUtil.OnResultCall(){
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
                    String samValue = transaUtil.getSignRawTrans(MemoryDataManager.getInstance().getPriKey(), inputString, outputString);
                    sendExternal(amount, samValue);
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
                    Connect.StructData structData = DecryptionUtil.decodeAESGCMStructData(imResponse.getCipherData());
                    Connect.ExternalBillingInfo billingInfo = Connect.ExternalBillingInfo.parseFrom(structData.getPlainData());

                    if(ProtoBufUtil.getInstance().checkProtoBuf(billingInfo)){
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
                                PacketSendActivity.startActivity(mActivity, sendOutBean);
                                finish();
                            }
                        });
                    }
                } catch (InvalidProtocolBufferException e) {
                    paymentPwd.closeStatusDialog(MdStyleProgress.Status.LoadFail);
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Connect.HttpResponse response) {
                paymentPwd.closeStatusDialog(MdStyleProgress.Status.LoadFail);
                TransferError.getInstance().showError(response.getCode(),response.getMessage());
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
                            Connect.StructData structData = DecryptionUtil.decodeAESGCMStructData(imResponse.getCipherData());
                            Connect.PendingRedPackage pending = Connect.PendingRedPackage.parseFrom(structData.getPlainData());
                            if(ProtoBufUtil.getInstance().checkProtoBuf(pending)){
                                pendingRedPackage = pending;
                            }
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
