package connect.activity.chat.exts.presenter;

import android.app.Activity;
import android.text.TextUtils;

import com.google.protobuf.InvalidProtocolBufferException;
import com.wallet.bean.CurrencyEnum;

import java.util.HashMap;

import connect.activity.chat.bean.LinkMessageRow;
import connect.activity.chat.bean.MsgExtEntity;
import connect.activity.chat.bean.MsgSend;
import connect.activity.chat.exts.TransferToActivity;
import connect.activity.chat.exts.contract.TransferToContract;
import connect.activity.wallet.bean.TransferBean;
import connect.activity.wallet.manager.TransferManager;
import connect.database.green.DaoHelper.ContactHelper;
import connect.database.green.DaoHelper.MessageHelper;
import connect.database.green.DaoHelper.ParamManager;
import connect.database.green.DaoHelper.TransactionHelper;
import connect.database.green.bean.ContactEntity;
import connect.instant.model.CFriendChat;
import connect.ui.activity.R;
import connect.utils.ActivityUtil;
import connect.utils.ProtoBufUtil;
import connect.utils.TimeUtil;
import connect.utils.ToastEUtil;
import connect.utils.UriUtil;
import instant.bean.ChatMsgEntity;
import instant.sender.model.FriendChat;
import instant.sender.model.NormalChat;
import instant.utils.cryption.DecryptionUtil;
import connect.utils.okhttp.OkHttpUtil;
import connect.utils.okhttp.ResultCall;
import com.wallet.inter.WalletListener;
import protos.Connect;

/**
 * Created by Administrator on 2017/8/10.
 */

public class TransferToPresenter implements TransferToContract.Presenter{

    private TransferToContract.BView view;
    private Activity activity;
    private ContactEntity contactEntity;
    private TransferManager transferManager;

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

        transferManager = new TransferManager(activity, CurrencyEnum.BTC);
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
        transferManager.transferConnectUser(null, outMap, new WalletListener<String>() {
            @Override
            public void success(String value) {
                long amount = view.getCurrentAmount();
                ParamManager.getInstance().putLatelyTransfer(new TransferBean(4, contactEntity.getAvatar(),
                        contactEntity.getUsername(), contactEntity.getAddress()));
                if (view.getTransType() == TransferToActivity.TransferType.CHAT) {
                    MsgSend.sendOuterMsg(LinkMessageRow.Transfer,0,value, amount, view.getTransferNote());
                } else if (view.getTransType() == TransferToActivity.TransferType.ADDRESS) {
                    CFriendChat friendChat = new CFriendChat(contactEntity);
                    ChatMsgEntity msgExtEntity = friendChat.transferMsg(0,value, amount, view.getTransferNote());
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
