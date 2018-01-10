package connect.instant.model;

import android.text.TextUtils;

import com.google.protobuf.InvalidProtocolBufferException;

import java.util.List;

import connect.activity.base.BaseApplication;
import connect.activity.home.bean.ConversationAction;
import connect.activity.home.bean.RoomAttrBean;
import connect.database.SharedPreferenceUtil;
import connect.database.green.DaoHelper.ContactHelper;
import connect.database.green.DaoHelper.ConversionHelper;
import connect.database.green.DaoHelper.MessageHelper;
import connect.database.green.bean.ContactEntity;
import connect.database.green.bean.ConversionEntity;
import connect.database.green.bean.GroupMemberEntity;
import connect.instant.inter.ConversationListener;
import connect.ui.activity.R;
import connect.utils.TimeUtil;
import connect.utils.UriUtil;
import connect.utils.okhttp.OkHttpUtil;
import connect.utils.okhttp.ResultCall;
import instant.bean.ChatMsgEntity;
import instant.sender.model.FriendChat;
import protos.Connect;

/**
 * Created by Administrator on 2017/10/19.
 */

public class CFriendChat extends FriendChat implements ConversationListener {

    private String userName;
    private String userAvatar;

    public CFriendChat(String uid) {
        super(uid, "");

        ContactEntity contactEntity = ContactHelper.getInstance().loadFriendEntity(uid);
        if (contactEntity == null) {
            List<RoomAttrBean> roomAttrBeen = ConversionHelper.getInstance().loadRoomEntities(uid);
            if (roomAttrBeen.size() == 0) {
                List<GroupMemberEntity> memberEntities = ContactHelper.getInstance().loadGroupMemEntitiesWithOutGroupKey(uid);
                if (memberEntities.size() > 0) {
                    GroupMemberEntity memberEntity = memberEntities.get(0);
                    userName = memberEntity.getUsername();
                    userAvatar = memberEntity.getAvatar();
                }
            } else {
                RoomAttrBean attrBean = roomAttrBeen.get(0);
                userName = attrBean.getName();
                userAvatar = attrBean.getAvatar();
            }
        } else {
            userName = contactEntity.getName();
            userAvatar = contactEntity.getAvatar();
            setFriendPublicKey(contactEntity.getPublicKey());
        }
        requestFriendInfo(uid);
    }

    public void requestFriendInfo(final String frienduid) {
        Connect.SearchUser searchUser = Connect.SearchUser.newBuilder()
                .setTyp(1)
                .setCriteria(frienduid)
                .build();

        OkHttpUtil.getInstance().postEncrySelf(UriUtil.CONNECT_V1_USER_SEARCH, searchUser, new ResultCall<Connect.HttpNotSignResponse>() {
            @Override
            public void onResponse(Connect.HttpNotSignResponse response) {
                try {
                    Connect.StructData structData = Connect.StructData.parseFrom(response.getBody());
                    Connect.UsersInfo userInfo = Connect.UsersInfo.parseFrom(structData.getPlainData());

                    Connect.UserInfo userinfo = userInfo.getUsers(0);
                    userName = userinfo.getName();
                    userAvatar = userinfo.getAvatar();
                    setFriendPublicKey(userinfo.getPubKey());
                } catch (InvalidProtocolBufferException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Connect.HttpNotSignResponse response) {

            }
        });
    }

    @Override
    public String headImg() {
        super.headImg();
        return userAvatar;
    }

    @Override
    public String nickName() {
        super.nickName();
        return userName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserAvatar() {
        return userAvatar;
    }

    public void setUserAvatar(String userAvatar) {
        this.userAvatar = userAvatar;
    }

    /**
     * 打招呼 消息
     */
    public void createWelcomeMessage() {
        String myUid = SharedPreferenceUtil.getInstance().getUser().getUid();
        String content = BaseApplication.getInstance().getBaseContext().getString(R.string.Link_Hello_I_am, nickName());
        ChatMsgEntity msgExtEntity = txtMsg(content);
        msgExtEntity.setMessage_from(friendUid);
        msgExtEntity.setMessage_to(myUid);
        updateRoomMsg("", content, TimeUtil.getCurrentTimeInLong(), -1, 1);

        MessageHelper.getInstance().insertMsgExtEntity(msgExtEntity);
    }

    public void updateRoomMsg(String draft, String showText, long msgtime) {
        updateRoomMsg(draft, showText, msgtime, -1);
    }

    public void updateRoomMsg(String draft, String showText, long msgtime, int at) {
        updateRoomMsg(draft, showText, msgtime, at, 0);
    }

    public void updateRoomMsg(String draft, String showText, long msgtime, int at, int newmsg) {
        updateRoomMsg(draft, showText, msgtime, at, newmsg, true);
    }

    public void updateRoomMsg(String draft, String showText, long msgtime, int at, int newmsg, boolean broad) {
        if (TextUtils.isEmpty(chatKey())) {
            return;
        }

        List<RoomAttrBean> roomEntities = ConversionHelper.getInstance().loadRoomEntities(chatKey());
        if (roomEntities == null || roomEntities.size() == 0) {
            ConversionEntity conversionEntity = new ConversionEntity();
            conversionEntity.setIdentifier(friendUid);
            conversionEntity.setName(nickName());
            conversionEntity.setAvatar(headImg());
            conversionEntity.setType(chatType());
            conversionEntity.setContent(TextUtils.isEmpty(showText) ? "" : showText);
            conversionEntity.setStranger(isStranger ? 1 : 0);
            conversionEntity.setUnread_count(newmsg == 0 ? 0 : 1);
            conversionEntity.setDraft(TextUtils.isEmpty(draft) ? "" : draft);
            conversionEntity.setIsAt(at);
            conversionEntity.setLast_time((msgtime < 0 ? 0 : msgtime));
            ConversionHelper.getInstance().insertRoomEntity(conversionEntity);
        } else {
            for (RoomAttrBean attrBean : roomEntities) {
                ConversionHelper.getInstance().updateRoomEntity(
                        friendUid,
                        nickName(),
                        TextUtils.isEmpty(draft) ? "" : draft,
                        TextUtils.isEmpty(showText) ? "" : showText,
                        (newmsg == 0 ? 0 : 1 + attrBean.getUnread()),
                        at,
                        (isStranger ? 1 : 0),
                        (msgtime <= 0 ? 0 : msgtime)
                );
            }
        }
        if (broad) {
            ConversationAction.conversationAction.sendEvent(ConversationAction.ConverType.LOAD_MESSAGE);
        }
    }
}
