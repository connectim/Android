package instant.sender.model;

import instant.bean.ChatMsgEntity;
import instant.bean.MessageType;
import instant.bean.Session;
import instant.bean.SocketACK;
import instant.bean.UserCookie;
import instant.sender.SenderManager;
import instant.utils.TimeUtil;
import instant.utils.cryption.EncryptionUtil;
import protos.Connect;

/**
 * friend chat
 * Created by gtq on 2016/12/19.
 */
public class FriendChat extends NormalChat {

    private static String TAG = "_FriendChat";

    protected String friendUid = null;
    protected String friendPublicKey = null;

    public FriendChat(String uid, String publicKey) {
        this.friendUid = uid;
        this.friendPublicKey = publicKey;
    }

    @Override
    public ChatMsgEntity createBaseChat(MessageType type) {
        String myUid = Session.getInstance().getConnectCookie().getUid();

        ChatMsgEntity msgExtEntity = new ChatMsgEntity();
        msgExtEntity.setMessage_id(TimeUtil.timestampToMsgid());
        msgExtEntity.setChatType(Connect.ChatType.PRIVATE.getNumber());
        msgExtEntity.setMessage_ower(chatKey());
        msgExtEntity.setMessage_from(myUid);
        msgExtEntity.setMessage_to(chatKey());
        msgExtEntity.setMessageType(type.type);
        msgExtEntity.setRead_time(0L);
        msgExtEntity.setCreatetime(TimeUtil.getCurrentTimeInLong());
        msgExtEntity.setSend_status(0);
        return msgExtEntity;
    }

    @Override
    public void sendPushMsg(ChatMsgEntity msgExtEntity) {
        try {
            Connect.ChatMessage.Builder chatMessageBuilder = msgExtEntity.transToChatMessageBuilder();

            UserCookie userCookie = Session.getInstance().getConnectCookie();
            Connect.MessageUserInfo.Builder userInfo = Connect.MessageUserInfo.newBuilder()
                    .setAvatar(userCookie.getUserAvatar())
                    .setUsername(userCookie.getUserName())
                    .setUid(userCookie.getUid());
            chatMessageBuilder.setSender(userInfo);

            String userPrivate = userCookie.getPrivateKey();
            String userPublicKey = userCookie.getPublicKey();
            EncryptionUtil.ExtendedECDH ecdhExts = EncryptionUtil.ExtendedECDH.EMPTY;
            Connect.GcmData gcmData = EncryptionUtil.encodeAESGCM(ecdhExts, userPrivate, friendPublicKey, msgExtEntity.getContents());
            chatMessageBuilder.setCipherData(gcmData);

            Connect.ChatSession chatSession = Connect.ChatSession.newBuilder()
                    .setPubKey(userPublicKey)
                    .setFriendPubkey(friendPublicKey)
                    .build();
            Connect.MessageData messageData = Connect.MessageData.newBuilder()
                    .setChatSession(chatSession)
                    .setChatMsg(chatMessageBuilder.build())
                    .build();

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
        return friendUid;
    }

    @Override
    public int chatType() {
        return Connect.ChatType.PRIVATE_VALUE;
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

    @Override
    public String friendPublicKey() {
        return friendPublicKey;
    }

    public void setFriendPublicKey(String friendPublicKey) {
        this.friendPublicKey = friendPublicKey;
    }
}
