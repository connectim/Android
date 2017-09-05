package connect.activity.contact.presenter;

import android.app.Activity;
import android.content.Intent;
import android.text.TextUtils;

import com.google.protobuf.InvalidProtocolBufferException;

import connect.activity.base.BaseApplication;
import connect.activity.chat.ChatActivity;
import connect.activity.chat.bean.MsgExtEntity;
import connect.activity.chat.bean.Talker;
import connect.activity.chat.model.content.FriendChat;
import connect.activity.chat.model.content.GroupChat;
import connect.activity.chat.model.content.NormalChat;
import connect.activity.contact.bean.ContactNotice;
import connect.activity.contact.bean.MsgSendBean;
import connect.activity.contact.contract.FriendInfoContract;
import connect.activity.home.bean.MsgNoticeBean;
import connect.database.green.DaoHelper.ContactHelper;
import connect.database.green.DaoHelper.ConversionHelper;
import connect.database.green.DaoHelper.MessageHelper;
import connect.database.green.bean.ContactEntity;
import connect.database.green.bean.ConversionEntity;
import connect.database.green.bean.GroupEntity;
import connect.ui.activity.R;
import connect.utils.ActivityUtil;
import connect.utils.ProtoBufUtil;
import connect.utils.ToastEUtil;
import connect.utils.ToastUtil;
import connect.utils.UriUtil;
import connect.utils.cryption.DecryptionUtil;
import connect.utils.okhttp.OkHttpUtil;
import connect.utils.okhttp.ResultCall;
import connect.widget.imagewatcher.ImageWatcher;
import connect.widget.imagewatcher.ImageWatcherUtil;
import protos.Connect;

public class FriendInfoPresenter implements FriendInfoContract.Presenter {

    private FriendInfoContract.View mView;
    private ImageWatcher vImageWatcher;

    public FriendInfoPresenter(FriendInfoContract.View mView) {
        this.mView = mView;
        mView.setPresenter(this);
    }

    @Override
    public void start() {
        vImageWatcher = ImageWatcher.Helper.with(mView.getActivity())
                .setTranslucentStatus(ImageWatcherUtil.isShowBarHeight(mView.getActivity()))
                .setErrorImageRes(R.mipmap.default_user_avatar)
                .create();
    }

    @Override
    public ImageWatcher getImageWatcher() {
        return vImageWatcher;
    }

    @Override
    public void shareFriendCard(Activity activity, Intent data, ContactEntity friendEntity) {
        int type = data.getIntExtra("type", 0);
        String pubKey = data.getStringExtra("object");
        if (TextUtils.isEmpty(pubKey)) {
            return;
        }

        Talker talker=null;
        NormalChat baseChat = null;
        if (type == 0) {
            ContactEntity acceptFriend = ContactHelper.getInstance().loadFriendEntity(pubKey);
            baseChat = new FriendChat(acceptFriend);
            talker = new Talker(acceptFriend);
        } else if (type == 1) {
            GroupEntity groupEntity = ContactHelper.getInstance().loadGroupEntity(pubKey);
            baseChat = new GroupChat(groupEntity);
            talker = new Talker(groupEntity);
        }
        MsgExtEntity msgExtEntity = baseChat.cardMsg(friendEntity.getPub_key(), friendEntity.getUsername(), friendEntity.getAvatar());
        baseChat.sendPushMsg(msgExtEntity);
        MessageHelper.getInstance().insertMsgExtEntity(msgExtEntity);
        baseChat.updateRoomMsg(null, BaseApplication.getInstance().getBaseContext().getString(R.string.Chat_Visting_card), msgExtEntity.getCreatetime());

        ChatActivity.startActivity(activity,talker);
        activity.finish();
    }

    @Override
    public void checkOnEvent(MsgNoticeBean notice) {
        Object[] objs = null;
        if (notice.object != null) {
            objs = (Object[]) notice.object;
        }
        MsgSendBean sendBean = (MsgSendBean) objs[0];
        switch (notice.ntEnum) {
            case MSG_SEND_SUCCESS:
                if (sendBean.getType() == MsgSendBean.SendType.TypeAddFavorites) {
                    mView.setCommon(sendBean.getCommon());
                } else if (sendBean.getType() == MsgSendBean.SendType.TypeDeleteFriend) {
                    ContactHelper.getInstance().deleteEntity(sendBean.getAddress());
                    ContactHelper.getInstance().deleteRequestEntity(sendBean.getPubkey());
                    ContactHelper.getInstance().removeFriend(sendBean.getPubkey());

                    ContactNotice.receiverContact();
                    ToastEUtil.makeText(mView.getActivity(), R.string.Link_Delete_Successful).show();
                    ActivityUtil.goBack(mView.getActivity());
                }
                break;
            case MSG_SEND_FAIL:
                Integer errorCode = (Integer) objs[1];
                if (sendBean.getType() == MsgSendBean.SendType.TypeDeleteFriend) {
                    ToastEUtil.makeText(mView.getActivity(),R.string.Link_Delete_Failed,ToastEUtil.TOAST_STATUS_FAILE).show();
                    ContactHelper.getInstance().deleteEntity(sendBean.getAddress());
                    ContactHelper.getInstance().deleteRequestEntity(sendBean.getPubkey());
                    ContactHelper.getInstance().removeFriend(sendBean.getPubkey());
                    ContactNotice.receiverContact();
                    ActivityUtil.goBack(mView.getActivity());
                } else {
                    ToastUtil.getInstance().showToast(errorCode + "");
                }
                break;
        }
    }

    /**
     * Get the latest information from friends
     *
     * @param address friend address
     * @param friendEntity
     */
    @Override
    public void requestUserInfo(String address, final ContactEntity friendEntity) {
        final Connect.SearchUser searchUser = Connect.SearchUser.newBuilder()
                .setCriteria(address)
                .build();
        OkHttpUtil.getInstance().postEncrySelf(UriUtil.CONNECT_V1_USER_SEARCH, searchUser, new ResultCall<Connect.HttpResponse>() {
            @Override
            public void onResponse(Connect.HttpResponse response) {
                try {
                    Connect.IMResponse imResponse = Connect.IMResponse.parseFrom(response.getBody().toByteArray());
                    Connect.StructData structData = DecryptionUtil.decodeAESGCMStructData(imResponse.getCipherData());
                    Connect.UserInfo userInfo = Connect.UserInfo.parseFrom(structData.getPlainData());
                    if(!ProtoBufUtil.getInstance().checkProtoBuf(userInfo)){
                        return;
                    }
                    if (friendEntity.getAvatar().equals(userInfo.getAvatar()) && friendEntity.getUsername().equals(userInfo.getUsername())) {
                        return;
                    }
                    // Update the database information
                    friendEntity.setUsername(userInfo.getUsername());
                    friendEntity.setAvatar(userInfo.getAvatar());
                    mView.updateView(friendEntity);
                    ContactHelper.getInstance().insertContact(friendEntity);
                    // Update the message list user information
                    ConversionEntity conversionEntity = ConversionHelper.getInstance().loadRoomEnitity(friendEntity.getPub_key());
                    if (conversionEntity != null) {
                        FriendChat friendChat = new FriendChat(friendEntity);
                        friendChat.updateRoomMsg(null, "", -1, -1, -1);
                    }
                } catch (InvalidProtocolBufferException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Connect.HttpResponse response) {}
        });
    }

    /**
     * Set friends blacklist
     *
     * @param block
     * @param address friend address
     */
    @Override
    public void requestBlock(final boolean block,String address) {
        Connect.UserIdentifier userIdentifier = Connect.UserIdentifier.newBuilder()
                .setAddress(address)
                .build();
        String url;
        if (block) {
            url = UriUtil.CONNEXT_V1_BLACKLIST;
        } else {
            url = UriUtil.CONNEXT_V1_BLACKLIST_REMOVE;
        }
        OkHttpUtil.getInstance().postEncrySelf(url, userIdentifier, new ResultCall<Connect.HttpResponse>() {
            @Override
            public void onResponse(Connect.HttpResponse response) {
                mView.setBlock(block);
            }

            @Override
            public void onError(Connect.HttpResponse response) {}
        });
    }

}
