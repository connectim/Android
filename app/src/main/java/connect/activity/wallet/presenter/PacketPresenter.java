package connect.activity.wallet.presenter;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;

import com.google.protobuf.InvalidProtocolBufferException;

import connect.activity.wallet.PacketSendActivity;
import connect.activity.wallet.bean.SendOutBean;
import connect.activity.wallet.contract.PacketContract;
import connect.utils.UriUtil;
import connect.utils.cryption.DecryptionUtil;
import connect.utils.okhttp.OkHttpUtil;
import connect.utils.okhttp.ResultCall;
import connect.wallet.cwallet.business.TransferEditView;
import protos.Connect;

/**
 * Created by Administrator on 2017/4/18 0018.
 */

public class PacketPresenter implements PacketContract.Presenter{

    PacketContract.View mView;
    public PacketPresenter(PacketContract.View mView) {
        this.mView = mView;
    }

    @Override
    public void start() {}

    @Override
    public TextWatcher getNumberWatcher() {
        return textWatcherNumber;
    }

    @Override
    public TransferEditView.OnEditListener getEditListener() {
        return onEditListener;
    }

    private TextWatcher textWatcherNumber = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}
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

    @Override
    public void getPacketDetail(String hashId) {
        Connect.RedPackageHash redPackageHash = Connect.RedPackageHash.newBuilder()
                .setId(hashId)
                .build();
        OkHttpUtil.getInstance().postEncrySelf(UriUtil.WALLET_PACKAGE_INFO, redPackageHash, new ResultCall<Connect.HttpResponse>() {
            @Override
            public void onResponse(Connect.HttpResponse response) {
                try {
                    Connect.IMResponse imResponse = Connect.IMResponse.parseFrom(response.getBody().toByteArray());
                    Connect.StructData structData = DecryptionUtil.decodeAESGCMStructData(imResponse.getCipherData());
                    Connect.RedPackageInfo redPackageInfo = Connect.RedPackageInfo.parseFrom(structData.getPlainData());
                    SendOutBean sendOutBean = new SendOutBean();
                    sendOutBean.setType(PacketSendActivity.RED_PACKET);
                    sendOutBean.setUrl(redPackageInfo.getRedpackage().getUrl());
                    sendOutBean.setNumber(Integer.valueOf(mView.getPacketNumber()));
                    sendOutBean.setDeadline(redPackageInfo.getRedpackage().getDeadline());
                    mView.goPacketView(sendOutBean);
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
