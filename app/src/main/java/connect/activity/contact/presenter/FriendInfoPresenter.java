package connect.activity.contact.presenter;

import android.app.Activity;
import android.content.Intent;
import android.text.TextUtils;

import com.google.protobuf.InvalidProtocolBufferException;

import org.greenrobot.eventbus.EventBus;

import connect.activity.base.BaseApplication;
import connect.activity.chat.ChatActivity;
import connect.activity.chat.bean.ContactUpdateBean;
import connect.activity.chat.bean.Talker;
import connect.activity.chat.model.GroupMemberUtil;
import connect.activity.contact.bean.ContactNotice;
import connect.activity.contact.bean.MsgSendBean;
import connect.activity.contact.contract.FriendInfoContract;
import connect.activity.home.bean.ConversationAction;
import connect.activity.home.bean.MsgNoticeBean;
import connect.database.green.DaoHelper.ContactHelper;
import connect.database.green.DaoHelper.ConversionHelper;
import connect.database.green.DaoHelper.MessageHelper;
import connect.database.green.bean.ContactEntity;
import connect.database.green.bean.ConversionEntity;
import connect.database.green.bean.GroupEntity;
import connect.instant.inter.ConversationListener;
import connect.instant.model.CFriendChat;
import connect.instant.model.CGroupChat;
import connect.ui.activity.R;
import connect.utils.ActivityUtil;
import connect.utils.ToastEUtil;
import connect.utils.ToastUtil;
import connect.utils.UriUtil;
import connect.utils.okhttp.OkHttpUtil;
import connect.utils.okhttp.ResultCall;
import connect.widget.imagewatcher.ImageWatcher;
import connect.widget.imagewatcher.ImageWatcherUtil;
import instant.bean.ChatMsgEntity;
import instant.sender.model.NormalChat;
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
            baseChat = new CFriendChat(acceptFriend);
            talker = new Talker(Connect.ChatType.PRIVATE,pubKey);
        } else if (type == 1||type == 3) {
            GroupEntity groupEntity = ContactHelper.getInstance().loadGroupEntity(pubKey);
            baseChat = new CGroupChat(groupEntity);
            talker = new Talker(Connect.ChatType.GROUPCHAT,pubKey);
        }
        ChatMsgEntity msgExtEntity = baseChat.cardMsg(friendEntity.getUid(), friendEntity.getUsername(), friendEntity.getAvatar());
        baseChat.sendPushMsg(msgExtEntity);
        MessageHelper.getInstance().insertMsgExtEntity(msgExtEntity);
        ((ConversationListener)baseChat).updateRoomMsg(null, BaseApplication.getInstance().getBaseContext().getString(R.string.Chat_Visting_card), msgExtEntity.getCreatetime());

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
                    ContactHelper.getInstance().deleteEntity(sendBean.getUid());
                    ContactHelper.getInstance().deleteRequestEntity(sendBean.getUid());
                    ContactHelper.getInstance().removeFriend(sendBean.getUid());
                    ContactNotice.receiverContact();

                    ToastEUtil.makeText(mView.getActivity(), R.string.Link_Delete_Successful).show();
                    ActivityUtil.goBack(mView.getActivity());
                } else if(sendBean.getType() == MsgSendBean.SendType.TypeFriendBlock){
                    mView.setBlack(sendBean.getBlack());
                }
                break;
            case MSG_SEND_FAIL:
                Integer errorCode = (Integer) objs[1];
                if(sendBean.getType() == MsgSendBean.SendType.TypeAddFavorites) {
                    ToastUtil.getInstance().showToast(errorCode + "");
                }else if (sendBean.getType() == MsgSendBean.SendType.TypeDeleteFriend) {
                    ToastEUtil.makeText(mView.getActivity(),R.string.Link_Delete_Failed,ToastEUtil.TOAST_STATUS_FAILE).show();
                }else if(sendBean.getType() == MsgSendBean.SendType.TypeFriendBlock) {
                    ToastUtil.getInstance().showToast(errorCode + "");
                }
                break;
        }
    }

    /**
     * Get the latest information from friends
     */
    @Override
    public void requestUserInfo(String value, final ContactEntity friendEntity) {
        final Connect.SearchUser searchUser = Connect.SearchUser.newBuilder()
                .setTyp(1)
                .setCriteria(value)
                .build();
        OkHttpUtil.getInstance().postEncrySelf(UriUtil.CONNECT_V1_USER_SEARCH, searchUser, new ResultCall<Connect.HttpNotSignResponse>() {
            @Override
            public void onResponse(Connect.HttpNotSignResponse response) {
                try {
                    Connect.StructData structData = Connect.StructData.parseFrom(response.getBody());
                    Connect.UsersInfo userInfo = Connect.UsersInfo.parseFrom(structData.getPlainData());
                    if (friendEntity.getAvatar().equals(userInfo.getUsers(0).getAvatar())) {
                        return;
                    }
                    // Update the database information
                    friendEntity.setAvatar(userInfo.getUsers(0).getAvatar());
                    friendEntity.setUsername(userInfo.getUsers(0).getName());
                    mView.updateView(friendEntity);
                    ContactHelper.getInstance().insertContact(friendEntity);
                    // Update the message list user information
                    ConversionEntity conversionEntity = ConversionHelper.getInstance().loadRoomEnitity(friendEntity.getUid());
                    if (conversionEntity != null) {
                        CFriendChat friendChat = new CFriendChat(friendEntity);
                        ConversionHelper.getInstance().updateRoomEntityAvatar(friendEntity.getUid(), userInfo.getUsers(0).getAvatar());
                        friendChat.updateRoomMsg(null, "", -1, -1, -1);
                    }

                    String friendUid =  friendEntity.getUid();
                    String userName =  userInfo.getUsers(0).getName();
                    String userAvatar = userInfo.getUsers(0).getAvatar();
                    ContactHelper.getInstance().updateGroupMemberNameAndAvatar(
                            friendUid,
                            userName,
                            userAvatar);

                    GroupMemberUtil.groupMemberUtil.updateGroupMemberEntity(
                            friendUid,
                            userName,
                            userAvatar);

                    ContactUpdateBean updateBean = new ContactUpdateBean(friendUid, userAvatar);
                    EventBus.getDefault().post(updateBean);

                    ContactNotice.receiverFriend();
                    ConversationAction.conversationAction.sendEvent(ConversationAction.ConverType.LOAD_MESSAGE);
                } catch (InvalidProtocolBufferException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Connect.HttpNotSignResponse response) {}
        });
    }

}
