package connect.activity.chat.model.content;

import android.text.TextUtils;
import com.google.gson.Gson;
import com.google.protobuf.ByteString;
import connect.activity.chat.bean.MsgExtEntity;
import connect.activity.chat.bean.RoomSession;
import connect.database.MemoryDataManager;
import connect.database.green.DaoHelper.ContactHelper;
import connect.database.green.DaoHelper.ParamHelper;
import connect.database.green.bean.ContactEntity;
import connect.database.green.bean.ParamEntity;
import connect.im.bean.MsgType;
import connect.im.bean.Session;
import connect.im.bean.SocketACK;
import connect.im.bean.UserCookie;
import connect.im.model.ChatSendManager;
import connect.utils.TimeUtil;
import connect.utils.cryption.EncryptionUtil;
import connect.utils.cryption.SupportKeyUril;
import connect.wallet.jni.AllNativeMethod;
import protos.Connect;

/**
 * friend chat
 * Created by gtq on 2016/12/19.
 */
public class FriendChat extends NormalChat {

    private String Tag = "FriendChat";
    private ContactEntity contactEntity = null;

    /** user Cookie */
    private UserCookie userCookie = null;
    /** friend Cookie */
    private UserCookie friendCookie = null;

    public enum EncryType {
        NORMAL,
        HALF,
        BOTH,
    }

    private EncryType encryType = EncryType.BOTH;

    public FriendChat(ContactEntity entity) {
        contactEntity = entity;

        ContactEntity dbEntity = ContactHelper.getInstance().loadFriendEntity(entity.getPub_key());
        isStranger = (dbEntity == null);
        if (dbEntity != null) {
            contactEntity = dbEntity;
        }
    }

    @Override
    public MsgExtEntity createBaseChat(MsgType type) {
        String mypublickey = MemoryDataManager.getInstance().getPubKey();

        MsgExtEntity msgExtEntity = new MsgExtEntity();
        msgExtEntity.setMessage_id(TimeUtil.timestampToMsgid());
        msgExtEntity.setChatType(Connect.ChatType.PRIVATE.getNumber());
        msgExtEntity.setMessage_from(mypublickey);
        msgExtEntity.setMessage_to(identify());
        msgExtEntity.setMessageType(type.type);
        msgExtEntity.setCreatetime(TimeUtil.getCurrentTimeInLong());
        msgExtEntity.setSend_status(0);
        return msgExtEntity;
    }

    @Override
    public void sendPushMsg(MsgExtEntity msgExtEntity) {
        try {
            Connect.ChatMessage.Builder chatMessageBuilder = msgExtEntity.transToChatMessageBuilder();

            String priKey = null;
            byte[] randomSalt = null;
            String friendKey = null;

            loadUserCookie();
            loadFriendCookie(chatKey());
            SupportKeyUril.EcdhExts ecdhExts = null;
            Connect.ChatSession.Builder sessionBuilder = Connect.ChatSession.newBuilder();
            Connect.MessageData.Builder builder = Connect.MessageData.newBuilder();

            switch (encryType) {
                case NORMAL:
                    priKey = MemoryDataManager.getInstance().getPriKey();
                    friendKey = chatKey();
                    ecdhExts = SupportKeyUril.EcdhExts.EMPTY;
                    break;
                case HALF:
                    priKey = userCookie.getPriKey();
                    randomSalt = userCookie.getSalt();

                    friendKey = chatKey();
                    ecdhExts = SupportKeyUril.EcdhExts.OTHER;
                    ecdhExts.setBytes(randomSalt);
                    sessionBuilder.setSalt(ByteString.copyFrom(randomSalt))
                            .setPubKey(userCookie.getPubKey());
                    break;
                case BOTH:
                    priKey = userCookie.getPriKey();
                    randomSalt = userCookie.getSalt();

                    friendKey = friendCookie.getPubKey();
                    byte[] friendSalt = friendCookie.getSalt();
                    if (friendCookie == null || friendSalt == null || friendSalt.length == 0) {
                        encryType = EncryType.HALF;
                        sendPushMsg(msgExtEntity);
                        return;
                    }
                    ecdhExts = SupportKeyUril.EcdhExts.OTHER;
                    ecdhExts.setBytes(SupportKeyUril.xor(randomSalt, friendSalt));
                    sessionBuilder.setSalt(ByteString.copyFrom(randomSalt)).
                            setPubKey(userCookie.getPubKey()).
                            setVer(ByteString.copyFrom(friendCookie.getSalt()));
                    break;
            }

            Connect.GcmData gcmData = EncryptionUtil.encodeAESGCM(ecdhExts, priKey, friendKey, msgExtEntity.getContents());
            chatMessageBuilder.setCipherData(gcmData);
            builder.setChatMsg(chatMessageBuilder)
                    .setChatSession(sessionBuilder);

            Connect.MessageData messageData = builder.build();
            ChatSendManager.getInstance().sendChatAckMsg(SocketACK.SINGLE_CHAT, chatKey(), messageData);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public String headImg() {
        String avatar = TextUtils.isEmpty(contactEntity.getAvatar()) ? "" : contactEntity.getAvatar();
        return avatar;
    }

    @Override
    public String nickName() {
        String nickName = TextUtils.isEmpty(contactEntity.getRemark()) ? contactEntity.getUsername() : contactEntity.getRemark();
        return TextUtils.isEmpty(nickName) ? "" : nickName;
    }

    @Override
    public String address() {
        String address = contactEntity.getAddress();
        if (TextUtils.isEmpty(address)) {
            if (TextUtils.isEmpty(chatKey())) {
                address = "";
            } else {
                address = AllNativeMethod.cdGetBTCAddrFromPubKey(chatKey());
            }
        }
        return address;
    }

    @Override
    public String identify() {
        String pubKey = TextUtils.isEmpty(contactEntity.getPub_key()) ? "" : contactEntity.getPub_key();
        return pubKey;
    }

    @Override
    public String chatKey() {
        String pubKey = TextUtils.isEmpty(contactEntity.getPub_key()) ? "" : contactEntity.getPub_key();
        return pubKey;
    }

    @Override
    public long destructReceipt() {
        return RoomSession.getInstance().getBurntime();
    }

    public MsgExtEntity strangerNotice() {
        MsgExtEntity msgExtEntity = createBaseChat(MsgType.NOTICE_STRANGER);
        return msgExtEntity;
    }

    public MsgExtEntity blackFriendNotice() {
        MsgExtEntity msgExtEntity = createBaseChat(MsgType.NOTICE_BLACK);
        return msgExtEntity;
    }

    @Override
    public Connect.MessageUserInfo senderInfo() {
        return null;
    }

    @Override
    public int chatType() {
        return 0;
    }

    public void setFriendEntity(ContactEntity friendEntity) {
        this.contactEntity = friendEntity;
    }

    public void setEncryType(EncryType encryType) {
        this.encryType = encryType;
    }

    public void setFriendCookie(UserCookie friendCookie) {
        this.friendCookie = friendCookie;
    }

    private void loadUserCookie() {
        String pubkey = MemoryDataManager.getInstance().getPubKey();
        userCookie = Session.getInstance().getUserCookie(pubkey);
        if (userCookie == null) {
            String cookieKey = "COOKIE:" + pubkey;
            ParamEntity paramEntity = ParamHelper.getInstance().likeParamEntityDESC(cookieKey);//local cookie
            if (paramEntity != null) {
                userCookie = new Gson().fromJson(paramEntity.getValue(), UserCookie.class);
            }
        }

        if (userCookie == null) {
            encryType = EncryType.NORMAL;
        }
    }

    public void loadFriendCookie(String pubkey) {
        friendCookie = Session.getInstance().getUserCookie(pubkey);
        if (friendCookie == null) {
            String cookieFriend = "COOKIE:" + pubkey;
            ParamEntity friendEntity = ParamHelper.getInstance().likeParamEntityDESC(cookieFriend);
            if (friendEntity != null) {
                friendCookie = new Gson().fromJson(friendEntity.getValue(), UserCookie.class);
            }
        }

        if (friendCookie == null) {
            encryType = EncryType.NORMAL;
        }
    }

    public MsgExtEntity inviteJoinGroupMsg(String avatar, String name, String id, String token) {
        MsgExtEntity msgExtEntity = createBaseChat(MsgType.INVITE_GROUP);
        Connect.JoinGroupMessage.Builder builder = Connect.JoinGroupMessage.newBuilder()
                .setAvatar(avatar)
                .setGroupName(name)
                .setGroupId(id)
                .setToken(token);

        msgExtEntity.setContents(builder.build().toByteArray());
        return msgExtEntity;
    }
}
