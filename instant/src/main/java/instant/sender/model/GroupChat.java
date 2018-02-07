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
        msgExtEntity.setCreatetime(TimeUtil.getCurrentTimeInLong());
        msgExtEntity.setSend_status(0);
        return msgExtEntity;
    }

    @Override
    public void sendPushMsg(ChatMsgEntity msgExtEntity) {
        Connect.ChatMessage.Builder chatMessageBuilder = msgExtEntity.transToChatMessageBuilder();
        chatMessageBuilder.setBody(ByteString.copyFrom(msgExtEntity.getContents()));

        UserCookie userCookie = Session.getInstance().getConnectCookie();
        Connect.MessageUserInfo userInfo = Connect.MessageUserInfo.newBuilder()
                .setAvatar(userCookie.getUserAvatar())
                .setUsername(userCookie.getUserName())
                .setUid(userCookie.getUid())
                .build();
        chatMessageBuilder.setSender(userInfo);

        Connect.MessageData messageData = Connect.MessageData.newBuilder()
                .setChatMsg(chatMessageBuilder)
                .build();

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
        return Connect.ChatType.GROUPCHAT_VALUE;
    }

    public void updateMyNickName() {
    }

    public void setNickName(String name) {
        this.groupName = name;
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
}
