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
import connect.utils.cryption.SupportKeyUril;
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
        Connect.BillHashId billHashId = Connect.BillHashId.newBuilder().setHash(hashId).build();
        OkHttpUtil.getInstance().postEncrySelf(UriUtil.TRANSFER_OUTER, billHashId, new ResultCall<Connect.HttpResponse>() {
            @Override
            public void onResponse(Connect.HttpResponse response) {
                try {
                    Connect.IMResponse imResponse = Connect.IMResponse.parseFrom(response.getBody().toByteArray());
                    if (!SupportKeyUril.verifySign(imResponse.getSign(), imResponse.getCipherData().toByteArray())) {
                        throw new Exception("Validation fails");
                    }
                    Connect.StructData structData = DecryptionUtil.decodeAESGCMStructData(imResponse.getCipherData());
                    final Connect.ExternalBillingInfo billingInfo = Connect.ExternalBillingInfo.parseFrom(structData.getPlainData().toByteArray());
                    if(!ProtoBufUtil.getInstance().checkProtoBuf(billingInfo)){
                        return;
                    }
                    SendOutBean sendOutBean = new SendOutBean();
                    sendOutBean.setType(PacketSendActivity.RED_PACKET);
                    sendOutBean.setUrl(billingInfo.getUrl());
                    sendOutBean.setNumber(Integer.valueOf(mView.getPacketNumber()));
                    sendOutBean.setDeadline(billingInfo.getDeadline());
                    mView.goPacketView(sendOutBean);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Connect.HttpResponse response) {
                int a = 0;
            }
        });
    }



}
