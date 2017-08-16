package connect.activity.chat.exts.presenter;

import android.app.Activity;
import android.text.TextUtils;

import com.google.protobuf.InvalidProtocolBufferException;

import java.util.HashMap;

import connect.activity.chat.bean.MsgExtEntity;
import connect.activity.chat.bean.MsgSend;
import connect.activity.chat.exts.TransferToActivity;
import connect.activity.chat.exts.contract.TransferToContract;
import connect.activity.chat.model.content.FriendChat;
import connect.activity.chat.model.content.NormalChat;
import connect.activity.wallet.bean.TransferBean;
import connect.database.green.DaoHelper.ContactHelper;
import connect.database.green.DaoHelper.MessageHelper;
import connect.database.green.DaoHelper.ParamManager;
import connect.database.green.DaoHelper.TransactionHelper;
import connect.database.green.bean.ContactEntity;
import connect.im.bean.MsgType;
import connect.ui.activity.R;
import connect.utils.ActivityUtil;
import connect.utils.ProtoBufUtil;
import connect.utils.TimeUtil;
import connect.utils.ToastEUtil;
import connect.utils.UriUtil;
import connect.utils.cryption.DecryptionUtil;
import connect.utils.okhttp.OkHttpUtil;
import connect.utils.okhttp.ResultCall;
import connect.wallet.cwallet.bean.CurrencyEnum;
import connect.wallet.cwallet.business.BaseBusiness;
import connect.wallet.cwallet.inter.WalletListener;
import protos.Connect;

/**
 * Created by Administrator on 2017/8/10.
 */

public class TransferToPresenter implements TransferToContract.Presenter{

    private TransferToContract.BView view;
    private Activity activity;
    private ContactEntity contactEntity;
    private  BaseBusiness baseBusiness;

    public TransferToPresenter(TransferToContract.BView view) {
        this.view = view;
        view.setPresenter(this);
    }

    @Override
    public void start() {
        activity = view.getActivity();

        String transferaddress = view.getTransferAddress();
        contactEntity = ContactHelper.getInstance().loadFriendEntity(transferaddress);
        if (contactEntity == null) {
            requestContactInfo(transferaddress);
        } else {
            String avatar = contactEntity.getAvatar();
            String username = TextUtils.isEmpty(contactEntity.getRemark()) ? contactEntity.getUsername() : contactEntity.getRemark();
            String transferinfo = activity.getString(R.string.Wallet_Transfer_To_User, username);
            view.showTransferInfo(avatar, transferinfo);
        }

        baseBusiness = new BaseBusiness(activity, CurrencyEnum.BTC);
    }

    public void requestContactInfo(String address) {
        Connect.SearchUser searchUser = Connect.SearchUser.newBuilder()
                .setCriteria(address)
                .build();
        OkHttpUtil.getInstance().postEncrySelf(UriUtil.CONNECT_V1_USER_SEARCH, searchUser, new ResultCall<Connect.HttpResponse>() {
            @Override
            public void onResponse(Connect.HttpResponse response) {
                try {
                    Connect.IMResponse imResponse = Connect.IMResponse.parseFrom(response.getBody().toByteArray());
                    Connect.StructData structData = DecryptionUtil.decodeAESGCMStructData(imResponse.getCipherData());
                    Connect.UserInfo sendUserInfo = Connect.UserInfo.parseFrom(structData.getPlainData());

                    if (ProtoBufUtil.getInstance().checkProtoBuf(sendUserInfo)) {
                        contactEntity = new ContactEntity();
                        contactEntity.setPub_key(sendUserInfo.getPubKey());
                        contactEntity.setUsername(sendUserInfo.getUsername());
                        contactEntity.setAddress(sendUserInfo.getAddress());
                        contactEntity.setAvatar(sendUserInfo.getAvatar());

                        String avatar = contactEntity.getAvatar();
                        String username = TextUtils.isEmpty(contactEntity.getRemark()) ? contactEntity.getUsername() : contactEntity.getRemark();
                        String transferinfo = activity.getString(R.string.Wallet_Transfer_To_User, username);
                        view.showTransferInfo(avatar, transferinfo);
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
    public void requestSingleTransfer(Long currentlong) {
        HashMap<String,Long> outMap = new HashMap();
        outMap.put(contactEntity.getPub_key(),currentlong);
        baseBusiness.transferConnectUser(null, outMap, new WalletListener<String>() {
            @Override
            public void success(String value) {
                long amount = view.getCurrentAmount();
                ParamManager.getInstance().putLatelyTransfer(new TransferBean(4, contactEntity.getAvatar(),
                        contactEntity.getUsername(), contactEntity.getAddress()));
                if (view.getTransType() == TransferToActivity.TransferType.CHAT) {
                    MsgSend.sendOuterMsg(MsgType.Transfer,0,value, amount, view.getTransferNote());
                } else if (view.getTransType() == TransferToActivity.TransferType.ADDRESS) {
                    NormalChat friendChat = new FriendChat(contactEntity);
                    MsgExtEntity msgExtEntity = friendChat.transferMsg(0,value, amount, view.getTransferNote());
                    MessageHelper.getInstance().insertMsgExtEntity(msgExtEntity);
                    friendChat.updateRoomMsg(null, msgExtEntity.showContent(), TimeUtil.getCurrentTimeInLong());
                    TransactionHelper.getInstance().updateTransEntity(value, msgExtEntity.getMessage_id(), 1);
                }

                ActivityUtil.goBack(activity);
            }

            @Override
            public void fail(WalletError error) {
                ToastEUtil.makeText(activity,R.string.Login_Send_failed).show();
            }
        });
    }
}
