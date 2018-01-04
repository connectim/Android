package connect.activity.chat.exts.presenter;

import android.app.Activity;

import com.google.protobuf.InvalidProtocolBufferException;

import connect.activity.chat.exts.contract.TransferMutiDetailContract;
import connect.utils.ProtoBufUtil;
import connect.utils.ToastUtil;
import connect.utils.UriUtil;
import connect.utils.okhttp.OkHttpUtil;
import connect.utils.okhttp.ResultCall;
import protos.Connect;

/**
 * Created by Administrator on 2017/8/11.
 */

public class TransferMutiDetailPresenter implements TransferMutiDetailContract.Presenter{

    private TransferMutiDetailContract.BView view;
    private Activity activity;
    private Connect.Bill bill;


    public TransferMutiDetailPresenter(TransferMutiDetailContract.BView view) {
        this.view = view;
        view.setPresenter(this);
    }

    @Override
    public void start() {
        activity = view.getActivity();
    }

    @Override
    public void requestSenderInfo(String address) {
        Connect.SearchUser searchUser = Connect.SearchUser.newBuilder()
                .setCriteria(address)
                .build();
        OkHttpUtil.getInstance().postEncrySelf(UriUtil.CONNECT_V1_USER_SEARCH, searchUser,
                new ResultCall<Connect.HttpResponse>() {
                    @Override
                    public void onResponse(Connect.HttpResponse response) {
                        try {
                            Connect.HttpNotSignResponse imResponse = Connect.HttpNotSignResponse.parseFrom(response.getBody().toByteArray());
                            Connect.StructData structData = Connect.StructData.parseFrom(imResponse.getBody());
                            Connect.UserInfo userInfo = Connect.UserInfo.parseFrom(structData.getPlainData());
                            if (ProtoBufUtil.getInstance().checkProtoBuf(userInfo)) {
                                String avatar = userInfo.getAvatar();
                                String name = userInfo.getUsername();
                                view.showSenderInfo(avatar, name);
                            }
                        } catch (InvalidProtocolBufferException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(Connect.HttpResponse response) {
                        ToastUtil.getInstance().showToast(response.getCode() + response.getMessage());
                    }
                });
    }

    @Override
    public void requestTransferDetail(String hashid) {
        Connect.BillHashId hashId = Connect.BillHashId.newBuilder().setHash(hashid).build();
        OkHttpUtil.getInstance().postEncrySelf(UriUtil.TRANSFER_INNER, hashId, new ResultCall<Connect.HttpResponse>() {
            @Override
            public void onResponse(Connect.HttpResponse response) {
                try {
                    Connect.HttpNotSignResponse imResponse = Connect.HttpNotSignResponse.parseFrom(response.getBody().toByteArray());
                    Connect.StructData structData = Connect.StructData.parseFrom(imResponse.getBody());
                    bill = Connect.Bill.parseFrom(structData.getPlainData().toByteArray());
                    if (ProtoBufUtil.getInstance().checkProtoBuf(bill)) {
                        String sender = bill.getSender();
                        String[] receivers = bill.getReceiver().split(",");
                        String tips = bill.getTips();
                        long amount = bill.getAmount();
                        int transferstate = bill.getStatus();
                        long createtime = bill.getCreatedAt() * 1000;

                        view.showTransferDetail(sender, receivers, tips, amount, transferstate, createtime);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Connect.HttpResponse response) {

            }
        });
    }

    @Override
    public String getTransferTxtid() {
        return bill.getTxid();
    }
}
