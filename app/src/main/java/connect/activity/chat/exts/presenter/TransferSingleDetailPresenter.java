package connect.activity.chat.exts.presenter;

import android.text.TextUtils;

import com.google.protobuf.InvalidProtocolBufferException;

import connect.activity.chat.exts.contract.TransferSingleDetailContract;
import connect.activity.login.bean.UserBean;
import connect.database.SharedPreferenceUtil;
import connect.database.green.DaoHelper.ContactHelper;
import connect.database.green.bean.ContactEntity;
import connect.utils.ProtoBufUtil;
import connect.utils.ToastUtil;
import connect.utils.UriUtil;
import connect.utils.cryption.DecryptionUtil;
import connect.utils.cryption.SupportKeyUril;
import connect.utils.okhttp.OkHttpUtil;
import connect.utils.okhttp.ResultCall;
import protos.Connect;

/**
 * Created by Administrator on 2017/8/10.
 */

public class TransferSingleDetailPresenter implements TransferSingleDetailContract.Presenter{

    private TransferSingleDetailContract.BView view;

    public TransferSingleDetailPresenter(TransferSingleDetailContract.BView view) {
        this.view = view;
        view.setPresenter(this);
    }

    @Override
    public void start() {

    }

    @Override
    public void requestTransferInnerDetail(String hashid) {
        Connect.BillHashId hashId = Connect.BillHashId.newBuilder().setHash(hashid).build();
        OkHttpUtil.getInstance().postEncrySelf(UriUtil.TRANSFER_INNER, hashId, new ResultCall<Connect.HttpResponse>() {
            @Override
            public void onResponse(Connect.HttpResponse response) {
                try {
                    Connect.IMResponse imResponse = Connect.IMResponse.parseFrom(response.getBody().toByteArray());
                    if (!SupportKeyUril.verifySign(imResponse.getSign(), imResponse.getCipherData().toByteArray())) {
                        throw new Exception("Validation fails");
                    }
                    Connect.StructData structData = DecryptionUtil.decodeAESGCMStructData(imResponse.getCipherData());
                    final Connect.Bill bill = Connect.Bill.parseFrom(structData.getPlainData().toByteArray());
                    if (ProtoBufUtil.getInstance().checkProtoBuf(bill)) {
                        view.showTips(bill.getTips());

                        long amount = bill.getAmount();
                        view.showTransferAmount(amount);

                        String txtid = bill.getTxid();
                        view.showTransferTxtid(txtid);

                        long createtime = bill.getCreatedAt();
                        view.showCreateTime(createtime);

                        int transferstate = bill.getStatus();
                        view.showTransferState(transferstate);
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
    public void requestTransferOuterDetail(String hashid) {
        Connect.BillHashId hashId = Connect.BillHashId.newBuilder().setHash(hashid).build();
        OkHttpUtil.getInstance().postEncrySelf(UriUtil.TRANSFER_OUTER, hashId,
                new ResultCall<Connect.HttpResponse>() {
                    @Override
                    public void onResponse(Connect.HttpResponse response) {
                        try {
                            Connect.IMResponse imResponse = Connect.IMResponse.parseFrom(response.getBody().toByteArray());
                            if (!SupportKeyUril.verifySign(imResponse.getSign(), imResponse.getCipherData().toByteArray())) {
                                throw new Exception("Validation fails");
                            }
                            Connect.StructData structData = DecryptionUtil.decodeAESGCMStructData(imResponse.getCipherData());
                            final Connect.ExternalBillingInfo billingInfo = Connect.ExternalBillingInfo.parseFrom(structData.getPlainData().toByteArray());
                            if (ProtoBufUtil.getInstance().checkProtoBuf(billingInfo)) {
                                String tips = billingInfo.getTips();
                                view.showTips(tips);

                                long amount = billingInfo.getAmount();
                                view.showTransferAmount(amount);

                                String txtid = billingInfo.getTxid();
                                view.showTransferTxtid(txtid);

                                long createtime = billingInfo.getCreatedAt();
                                view.showCreateTime(createtime);

                                int transferstate = billingInfo.getStatus();
                                view.showTransferState(transferstate);
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
    public void requestUserInfo(final int direct, String pubkey) {
        String avatar = "";
        String name = "";
        UserBean userBean = SharedPreferenceUtil.getInstance().getUser();
        if (userBean.getUid().equals(pubkey)) {
            avatar = userBean.getAvatar();
            name = userBean.getName();
        } else {
            ContactEntity friendEntity = ContactHelper.getInstance().loadFriendEntity(pubkey);
            if (friendEntity != null) {
                avatar = friendEntity.getAvatar();
                name = friendEntity.getUsername();
            } else {
                String address = SupportKeyUril.getAddressFromPubKey(pubkey);
                Connect.SearchUser searchUser = Connect.SearchUser.newBuilder()
                        .setCriteria(address)
                        .build();
                OkHttpUtil.getInstance().postEncrySelf(UriUtil.CONNECT_V1_USER_SEARCH, searchUser,
                        new ResultCall<Connect.HttpResponse>() {
                            @Override
                            public void onResponse(Connect.HttpResponse response) {
                                try {
                                    Connect.IMResponse imResponse = Connect.IMResponse.parseFrom(response.getBody().toByteArray());
                                    Connect.StructData structData = DecryptionUtil.decodeAESGCMStructData(imResponse.getCipherData());
                                    Connect.UserInfo userInfo = Connect.UserInfo.parseFrom(structData.getPlainData());
                                    if (ProtoBufUtil.getInstance().checkProtoBuf(userInfo)) {
                                        String avatar = userInfo.getAvatar();
                                        String name = userInfo.getUsername();
                                        view.showUserInfo(direct, avatar, name);
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
        }

        if (!(TextUtils.isEmpty(avatar) || TextUtils.isEmpty(name))) {
            view.showUserInfo(direct, avatar, name);
        }
    }
}
