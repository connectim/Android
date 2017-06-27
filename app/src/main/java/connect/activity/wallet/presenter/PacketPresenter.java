package connect.activity.wallet.presenter;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;

import connect.database.green.DaoHelper.ParamManager;
import connect.ui.activity.R;
import connect.activity.wallet.PacketSendActivity;
import connect.activity.wallet.bean.SendOutBean;
import connect.activity.wallet.bean.TransferBean;
import connect.activity.wallet.contract.PacketContract;
import connect.utils.ProtoBufUtil;
import connect.utils.UriUtil;
import connect.utils.cryption.DecryptionUtil;
import connect.utils.okhttp.OkHttpUtil;
import connect.utils.okhttp.ResultCall;
import connect.utils.transfer.TransferError;
import connect.widget.MdStyleProgress;
import connect.widget.payment.PaymentPwd;
import connect.utils.transfer.TransferEditView;
import protos.Connect;

/**
 * Created by Administrator on 2017/4/18 0018.
 */

public class PacketPresenter implements PacketContract.Presenter{

    PacketContract.View mView;
    private Connect.PendingRedPackage pendingRedPackage;

    public PacketPresenter(PacketContract.View mView) {
        this.mView = mView;
    }

    @Override
    public void start() {
        getPaddingInfo();
    }

    @Override
    public TextWatcher getNumberWatcher() {
        return textWatcherNumber;
    }

    @Override
    public TransferEditView.OnEditListener getEditListener() {
        return onEditListener;
    }

    @Override
    public Connect.PendingRedPackage getPendingPackage() {
        return pendingRedPackage;
    }

    private void getPaddingInfo() {
        OkHttpUtil.getInstance().postEncrySelf(UriUtil.WALLET_PACKAGE_PENDING, ByteString.copyFrom(new byte[]{}),
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

    @Override
    public void sendPacket(long amount, String siginRaw, String note, final PaymentPwd paymentPwd) {
        Connect.OrdinaryRedPackage.Builder builder = Connect.OrdinaryRedPackage.newBuilder();
        builder.setHashId(pendingRedPackage.getHashId());
        builder.setMoney(amount);
        if (!TextUtils.isEmpty(note))
            builder.setTips(note);
        builder.setSize(Integer.valueOf(mView.getPacketNumber()));
        builder.setRawTx(siginRaw);
        builder.setCategory(2);//1 ：friend  2： group
        builder.setType(1);
        OkHttpUtil.getInstance().postEncrySelf(UriUtil.WALLET_PACKAGE_SEND, builder.build(), new ResultCall<Connect.HttpResponse>() {
            @Override
            public void onResponse(final Connect.HttpResponse response) {
                paymentPwd.closeStatusDialog(MdStyleProgress.Status.LoadSuccess, new PaymentPwd.OnAnimationListener() {
                    @Override
                    public void onComplete() {
                        try {
                            Connect.IMResponse imResponse = Connect.IMResponse.parseFrom(response.getBody().toByteArray());
                            Connect.StructData structData = DecryptionUtil.decodeAESGCMStructData(imResponse.getCipherData());
                            Connect.RedPackage redPackage = Connect.RedPackage.parseFrom(structData.getPlainData());

                            SendOutBean sendOutBean = new SendOutBean();
                            sendOutBean.setType(PacketSendActivity.RED_PACKET);
                            sendOutBean.setUrl(redPackage.getUrl());
                            sendOutBean.setNumber(Integer.valueOf(mView.getPacketNumber()));
                            sendOutBean.setDeadline(redPackage.getDeadline());

                            ParamManager.getInstance().putLatelyTransfer(new TransferBean(1,
                                    mView.getActivity().getResources().getString(R.string.Wallet_Sent_via_link_luck_packet)));
                            mView.goinPacketSend(sendOutBean);
                        } catch (InvalidProtocolBufferException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }

            @Override
            public void onError(Connect.HttpResponse response) {
                paymentPwd.closeStatusDialog(MdStyleProgress.Status.LoadFail);
                TransferError.getInstance().showError(response.getCode(),response.getMessage());
            }
        });
    }

    private TextWatcher textWatcherNumber = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            checkPay();
        }
    };

    private TransferEditView.OnEditListener onEditListener  = new TransferEditView.OnEditListener() {
        @Override
        public void onEdit(String value) {
            checkPay();
        }

        @Override
        public void setFee() {
            mView.setPayFee();
        }
    };

    private void checkPay(){
        if (!TextUtils.isEmpty(mView.getCurrentBtc())
                && Double.valueOf(mView.getCurrentBtc()) >= 0.0001
                && !TextUtils.isEmpty(mView.getPacketNumber())) {
            mView.setPayBtnEnable(true);
        } else {
            mView.setPayBtnEnable(false);
        }
    }

}
