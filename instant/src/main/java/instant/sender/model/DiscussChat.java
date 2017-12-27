package instant.sender.model;

import com.google.protobuf.ByteString;

import instant.bean.ChatMsgEntity;
import instant.bean.MessageType;
import instant.bean.Session;
import instant.bean.SocketACK;
import instant.sender.SenderManager;
import instant.utils.RegularUtil;
import instant.utils.TimeUtil;
import protos.Connect;

/**
 * Discuss group Chat
 * Created by puin on 17-11-21.
 */
public class DiscussChat extends NormalChat {

    private static String TAG = "_GroupChat";

    protected String groupIdentify;
    protected String groupName = "";
    protected String myGroupName = "";

    public DiscussChat(String groupIdentify) {
        this.groupIdentify = groupIdentify;
    }

    @Override
    public ChatMsgEntity createBaseChat(MessageType type) {
        String myUid = Session.getInstance().getConnectCookie().getUid();

        ChatMsgEntity msgExtEntity = new ChatMsgEntity();
        msgExtEntity.setMessage_id(TimeUtil.timestampToMsgid());
        msgExtEntity.setChatType(Connect.ChatType.GROUP_DISCUSSION.getNumber());
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
    public void sendPushMsg(ChatMsgEntity chatMsgEntity) {
        try {
            Connect.ChatMessage.Builder chatMessageBuilder = chatMsgEntity.transToChatMessageBuilder();
            chatMessageBuilder.setOriginMsg(ByteString.copyFrom(chatMsgEntity.getContents()));

            Connect.MessageData messageData = Connect.MessageData.newBuilder()
                    .setChatMsg(chatMessageBuilder)
                    .build();

            Connect.MessagePost messagePost = normalChatMessage(messageData);
            SenderManager.getInstance().sendAckMsg(SocketACK.GROUP_CHAT, chatKey(), messageData.getChatMsg().getMsgId(), messagePost.toByteString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public String chatKey() {
        return groupIdentify;
    }

    @Override
    public int chatType() {
        return Connect.ChatType.GROUP_DISCUSSION_VALUE;
    }

    @Override
    public String headImg() {
        return RegularUtil.groupAvatar(groupIdentify);
    }

    @Override
    public String nickName() {
        return "";
    }

    @Override
    public long destructReceipt() {
        return 0;
    }
}
