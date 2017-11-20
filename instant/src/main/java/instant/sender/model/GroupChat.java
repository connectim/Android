package instant.sender.model;

import com.google.protobuf.ByteString;

import java.util.List;

import instant.bean.ChatMsgEntity;
import instant.bean.MessageType;
import instant.bean.Session;
import instant.bean.SocketACK;
import instant.bean.UserCookie;
import instant.sender.SenderManager;
import instant.utils.RegularUtil;
import instant.utils.TimeUtil;
import instant.utils.cryption.EncryptionUtil;
import instant.utils.cryption.SupportKeyUril;
import protos.Connect;

/**
 * group chat
 * Created by gtq on 2016/12/19.
 */
public class GroupChat extends NormalChat {

    private static String TAG = "_GroupChat";

    protected String groupKey;
    protected String groupName = "";
    protected String myGroupName = "";

    private String myUid;
    /** user Cookie */
    private UserCookie userCookie = null;
    /** friend Cookie */
    private UserCookie groupMemberCookie = null;

    public GroupChat(String groupKey) {
        this.groupKey = groupKey;
        myUid = Session.getInstance().getConnectCookie().getUid();
    }

    @Override
    public ChatMsgEntity createBaseChat(MessageType type) {
        ChatMsgEntity msgExtEntity = new ChatMsgEntity();
        msgExtEntity.setMessage_id(TimeUtil.timestampToMsgid());
        msgExtEntity.setChatType(Connect.ChatType.GROUPCHAT.getNumber());
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
        friendKey = groupMemberCookie.getPubKey();
        byte[] friendSalt = groupMemberCookie.getSalt();
        if (groupMemberCookie == null || friendSalt == null || friendSalt.length == 0) {
            return;
        }
        ecdhExts = EncryptionUtil.ExtendedECDH.OTHER;
        ecdhExts.setBytes(SupportKeyUril.xor(randomSalt, friendSalt));
        sessionBuilder.setSalt(ByteString.copyFrom(randomSalt))
                .setPubKey(userCookie.getPubKey())
                .setVer(ByteString.copyFrom(groupMemberCookie.getSalt()));

        Connect.GcmData gcmData = EncryptionUtil.encodeAESGCM(ecdhExts, priKey, friendKey, msgExtEntity.getContents());
        chatMessageBuilder.setCipherData(gcmData);
        builder.setChatMsg(chatMessageBuilder)
                .setChatSession(sessionBuilder);

        Connect.MessageData messageData = builder.build();
        Connect.MessagePost messagePost = normalChatMessage(messageData);
        SenderManager.getInstance().sendAckMsg(SocketACK.GROUP_CHAT, groupKey, messageData.getChatMsg().getMsgId(), messagePost.toByteString());
    }

    @Override
    public String headImg() {
        return RegularUtil.groupAvatar(groupKey);
    }

    @Override
    public String nickName() {
        return groupName;
    }

    @Override
    public String chatKey() {
        return groupKey;
    }

    @Override
    public int chatType() {
        return 1;
    }

    @Override
    public long destructReceipt() {
        return 0L;
    }

    public void updateMyNickName(){
    }

    public void setNickName(String name){
        this.myGroupName=name;
    }

    public ChatMsgEntity groupTxtMsg(String string, List<String> address) {
        ChatMsgEntity msgExtEntity = createBaseChat(MessageType.Text);
        Connect.TextMessage.Builder builder = Connect.TextMessage.newBuilder()
                .setContent(string);

        for (String memberaddress : address) {
            builder.addAtUids(memberaddress);
        }
        msgExtEntity.setContents(builder.build().toByteArray());
        return msgExtEntity;
    }

    private void loadUserCookie() {
        userCookie = Session.getInstance().getChatCookie();
        if (userCookie == null) {
        }
    }

    public void loadFriendCookie() {
        groupMemberCookie = Session.getInstance().getGroupMemberCookie(groupKey, myUid);
        if (groupMemberCookie == null) {//reload Group Member Cookie
        }
    }
}
