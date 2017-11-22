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

    private static String TAG = "_FriendChat";

    /** user Cookie */
    private UserCookie userCookie = null;
    /** friend Cookie */
    private UserCookie friendCookie = null;
    protected String friendUid = null;

    public FriendChat(String uid) {
        this.friendUid = uid;

        UserOrderBean userOrderBean = new UserOrderBean();
        userOrderBean.friendChatCookie(friendUid);
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
            loadFriendCookie();
            EncryptionUtil.ExtendedECDH ecdhExts = null;
            Connect.ChatSession.Builder sessionBuilder = Connect.ChatSession.newBuilder();
            Connect.MessageData.Builder builder = Connect.MessageData.newBuilder();

            priKey = userCookie.getPriKey();
            randomSalt = userCookie.getSalt();
            friendKey = friendCookie.getPubKey();
            byte[] friendSalt = friendCookie.getSalt();
            if (friendCookie == null || friendSalt == null || friendSalt.length == 0) {
                return;
            }
            ecdhExts = EncryptionUtil.ExtendedECDH.OTHER;
            ecdhExts.setBytes(SupportKeyUril.xor(randomSalt, friendSalt));
            sessionBuilder.setSalt(ByteString.copyFrom(randomSalt)).
                    setPubKey(userCookie.getPubKey()).
                    setVer(ByteString.copyFrom(friendCookie.getSalt()));

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
        return friendUid;
    }

    @Override
    public long destructReceipt() {
        return MessageLocalReceiver.localReceiver.chatBurnTime(friendUid);
    }

    @Override
    public int chatType() {
        return Connect.ChatType.PRIVATE_VALUE;
    }

    public void setFriendCookie(UserCookie friendCookie) {
        this.friendCookie = friendCookie;
    }

    private void loadUserCookie() {
        userCookie = Session.getInstance().getChatCookie();
        if (userCookie == null) {
            userCookie = SharedUtil.getInstance().loadLastChatUserCookie();
        }
    }

    public void loadFriendCookie() {
        friendCookie = Session.getInstance().getFriendCookie(friendUid);
        if (friendCookie == null) {
            friendCookie =  SharedUtil.getInstance().loadFriendCookie(friendUid);
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

    public void createGroupBroadToMember(String groupIdentify, String publicKey, Connect.CreateGroupMessage groupMessage) {
        String msgid = instant.utils.TimeUtil.timestampToMsgid();
        String privateKey = Session.getInstance().getConnectCookie().getPriKey();
        byte[] groupecdhkey = SupportKeyUril.getRawECDHKey(privateKey, publicKey);
        Connect.GcmData gcmData = EncryptionUtil.encodeAESGCM(
                EncryptionUtil.ExtendedECDH.EMPTY,
                groupecdhkey,
                groupMessage.toByteArray());

        Connect.ChatMessage chatMessage = Connect.ChatMessage.newBuilder()
                .setMsgId(msgid)
                .setTo(friendUid)
                .setCipherData(gcmData)
                .build();

        Connect.MessageData messageData = Connect.MessageData.newBuilder()
                .setChatMsg(chatMessage)
                .build();

        Connect.MessagePost messagePost = normalChatMessage(messageData);
        SenderManager.senderManager.sendAckMsg(
                SocketACK.GROUP_INVITE,
                groupIdentify,
                msgid,
                messagePost.toByteString());
    }
}
