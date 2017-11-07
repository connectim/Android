package instant.sender.model;

import java.util.List;

import instant.bean.ChatMsgEntity;
import instant.bean.MessageType;
import instant.bean.Session;
import instant.bean.SocketACK;
import instant.sender.SenderManager;
import instant.utils.RegularUtil;
import instant.utils.StringUtil;
import instant.utils.TimeUtil;
import instant.utils.cryption.EncryptionUtil;
import protos.Connect;

/**
 * group chat
 * Created by gtq on 2016/12/19.
 */
public class GroupChat extends NormalChat {

    private static String TAG = "GroupChat";

    protected String groupKey;
    protected String groupName = "";
    protected String groupEcdh = "";
    protected String myGroupName = "";

    public GroupChat(String groupKey) {
        this.groupKey = groupKey;
    }

    @Override
    public ChatMsgEntity createBaseChat(MessageType type) {
        String myUid = Session.getInstance().getUserCookie(Session.CONNECT_USER).getUid();

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

        byte[] groupecdh = StringUtil.hexStringToBytes(groupEcdh);
        Connect.GcmData gcmData = EncryptionUtil.encodeAESGCM(EncryptionUtil.ExtendedECDH.NONE, groupecdh, msgExtEntity.getContents());
        chatMessageBuilder.setCipherData(gcmData);

        //messageData
        Connect.MessageData.Builder builder = Connect.MessageData.newBuilder();
        builder.setChatMsg(chatMessageBuilder);

        Connect.MessageData messageData = builder.build();
        Connect.MessagePost messagePost=normalChatMessage(messageData);

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

    public String groupEcdh() {
        return groupEcdh;
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
}
