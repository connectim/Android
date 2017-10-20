package instant.sender.model;

import com.google.protobuf.ByteString;

import instant.bean.ChatMsgEntity;
import instant.bean.MessageType;
import instant.bean.Session;
import instant.bean.SocketACK;
import instant.bean.UserCookie;
import instant.bean.UserOrderBean;
import instant.parser.localreceiver.MessageLocalReceiver;
import instant.sender.SenderManager;
import instant.parser.localreceiver.CommandLocalReceiver;
import instant.utils.SharedUtil;
import instant.utils.TimeUtil;
import instant.utils.cryption.EncryptionUtil;
import instant.utils.cryption.SupportKeyUril;
import protos.Connect;

/**
 * friend chat
 * Created by gtq on 2016/12/19.
 */
public class FriendChat extends NormalChat {

    private String Tag = "FriendChat";

    /** user Cookie */
    private UserCookie userCookie = null;
    /** friend Cookie */
    private UserCookie friendCookie = null;

    protected String friendKey = null;

    public enum EncryType {
        NORMAL,
        HALF,
        BOTH,
    }

    private EncryType encryType = EncryType.BOTH;

    public FriendChat(String friendKey) {
        this.friendKey = friendKey;

        UserOrderBean userOrderBean = new UserOrderBean();
        userOrderBean.friendChatCookie(friendKey);
    }

    @Override
    public ChatMsgEntity createBaseChat(MessageType type) {
        String mypublickey = Session.getInstance().getUserCookie(Session.CONNECT_USER).getPubKey();

        ChatMsgEntity msgExtEntity = new ChatMsgEntity();
        msgExtEntity.setMessage_id(TimeUtil.timestampToMsgid());
        msgExtEntity.setChatType(Connect.ChatType.PRIVATE.getNumber());
        msgExtEntity.setMessage_ower(chatKey());
        msgExtEntity.setMessage_from(mypublickey);
        msgExtEntity.setMessage_to(chatKey());
        msgExtEntity.setMessageType(type.type);
        msgExtEntity.setRead_time(0L);
        msgExtEntity.setSnap_time(0L);
        msgExtEntity.setCreatetime(TimeUtil.getCurrentTimeInLong());
        msgExtEntity.setSend_status(0);
        return msgExtEntity;
    }

    @Override
    public void sendPushMsg(ChatMsgEntity msgExtEntity) {
        try {
            Connect.ChatMessage.Builder chatMessageBuilder = msgExtEntity.transToChatMessageBuilder();

            String priKey = null;
            byte[] randomSalt = null;
            String friendKey = null;

            loadUserCookie();
            loadFriendCookie(chatKey());
            EncryptionUtil.ExtendedECDH ecdhExts = null;
            Connect.ChatSession.Builder sessionBuilder = Connect.ChatSession.newBuilder();
            Connect.MessageData.Builder builder = Connect.MessageData.newBuilder();

            switch (encryType) {
                case NORMAL:
                    priKey = Session.getInstance().getUserCookie(Session.CONNECT_USER).getPriKey();
                    friendKey = chatKey();
                    ecdhExts = EncryptionUtil.ExtendedECDH.EMPTY;
                    break;
                case HALF:
                    priKey = userCookie.getPriKey();
                    randomSalt = userCookie.getSalt();

                    friendKey = chatKey();
                    ecdhExts = EncryptionUtil.ExtendedECDH.OTHER;
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
                    ecdhExts = EncryptionUtil.ExtendedECDH.OTHER;
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
            Connect.MessagePost messagePost = normalChatMessage(messageData);

            SenderManager.getInstance().sendAckMsg(SocketACK.SINGLE_CHAT, chatKey(), messageData.getChatMsg().getMsgId(), messagePost.toByteString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public String headImg() {
        return "";
    }

    @Override
    public String nickName() {
        return "";
    }

    @Override
    public String chatKey() {
        return friendKey;
    }

    @Override
    public long destructReceipt() {
        return MessageLocalReceiver.localReceiver.chatBurnTime(friendKey);
    }

    @Override
    public int chatType() {
        return 0;
    }

    public void setEncryType(EncryType encryType) {
        this.encryType = encryType;
    }

    public void setFriendCookie(UserCookie friendCookie) {
        this.friendCookie = friendCookie;
    }

    private void loadUserCookie() {
        String pubkey = Session.getInstance().getUserCookie(Session.CONNECT_USER).getPubKey();
        userCookie = Session.getInstance().getUserCookie(pubkey);
        if (userCookie == null) {
            userCookie = SharedUtil.getInstance().loadLastChatUserCookie();
        }

        if (userCookie == null) {
            encryType = EncryType.NORMAL;
        }
    }

    public void loadFriendCookie(String pubkey) {
        friendCookie = Session.getInstance().getUserCookie(pubkey);
        if (friendCookie == null) {
            friendCookie =  SharedUtil.getInstance().loadFriendCookie(pubkey);
        }

        if (friendCookie == null) {
            encryType = EncryType.NORMAL;
        }
    }

    public ChatMsgEntity inviteJoinGroupMsg(String avatar, String name, String id, String token) {
        ChatMsgEntity msgExtEntity = createBaseChat(MessageType.INVITE_GROUP);
        Connect.JoinGroupMessage.Builder builder = Connect.JoinGroupMessage.newBuilder()
                .setAvatar(avatar)
                .setGroupName(name)
                .setGroupId(id)
                .setToken(token);

        msgExtEntity.setContents(builder.build().toByteArray());
        return msgExtEntity;
    }
}
