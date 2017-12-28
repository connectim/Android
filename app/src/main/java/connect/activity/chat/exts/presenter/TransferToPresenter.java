package connect.activity.chat.exts.presenter;

import android.app.Activity;
import android.text.TextUtils;

import com.google.protobuf.InvalidProtocolBufferException;

import java.util.HashMap;

import connect.activity.chat.exts.contract.TransferToContract;
import connect.database.green.DaoHelper.ContactHelper;
import connect.database.green.bean.ContactEntity;
import connect.ui.activity.R;
import connect.utils.ProtoBufUtil;
import connect.utils.UriUtil;
import connect.utils.okhttp.OkHttpUtil;
import connect.utils.okhttp.ResultCall;
import protos.Connect;

/**
 * Created by Administrator on 2017/8/10.
 */

public class TransferToPresenter implements TransferToContract.Presenter{

    private TransferToContract.BView view;
    private Activity activity;
    private ContactEntity contactEntity;

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
                    Connect.StructData structData = Connect.StructData.parseFrom(imResponse.getBody());
                    Connect.UserInfo sendUserInfo = Connect.UserInfo.parseFrom(structData.getPlainData());

                    if (ProtoBufUtil.getInstance().checkProtoBuf(sendUserInfo)) {
                        contactEntity = new ContactEntity();
                        contactEntity.setUsername(sendUserInfo.getUsername());
                        contactEntity.setUid(sendUserInfo.getUid());
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
        outMap.put(contactEntity.getUid(),currentlong);
    }
}
