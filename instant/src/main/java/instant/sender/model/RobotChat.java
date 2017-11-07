package instant.sender.model;

import com.google.protobuf.ByteString;

import instant.R;
import instant.bean.ChatMsgEntity;
import instant.bean.MessageType;
import instant.bean.Session;
import instant.bean.SocketACK;
import instant.sender.SenderManager;
import instant.ui.InstantSdk;
import instant.utils.TimeUtil;
import protos.Connect;

/**
 * robot message
 * Created by pujin on 2017/1/19.
 */
public class RobotChat extends NormalChat {

    private static RobotChat robotChat;

    public RobotChat() {
    }

    public static RobotChat getInstance() {
        if (robotChat == null) {
            synchronized (RobotChat.class) {
                if (robotChat == null) {
                    robotChat = new RobotChat();
                }
            }
        }
        return robotChat;
    }

    @Override
    public ChatMsgEntity createBaseChat(MessageType type) {
        String myUid = Session.getInstance().getUserCookie(Session.CONNECT_USER).getUid();

        ChatMsgEntity msgExtEntity = new ChatMsgEntity();
        msgExtEntity.setMessage_id(TimeUtil.timestampToMsgid());
        msgExtEntity.setChatType(Connect.ChatType.CONNECT_SYSTEM_VALUE);
        msgExtEntity.setMessage_ower(chatKey());
        msgExtEntity.setMessage_from(myUid);
        msgExtEntity.setMessage_to(chatKey());
        msgExtEntity.setMessageType(type.type);
        msgExtEntity.setRead_time(0L);
        msgExtEntity.setSnap_time(0L);
        msgExtEntity.setCreatetime(TimeUtil.getCurrentTimeInLong());
        msgExtEntity.setSend_status(1);
        return msgExtEntity;
    }

    @Override
    public void sendPushMsg(ChatMsgEntity msgExtEntity) {
        Connect.MSMessage msMessage = Connect.MSMessage.newBuilder()
                .setCategory(msgExtEntity.getMessageType())
                .setMsgId(msgExtEntity.getMessage_id())
                .setBody(ByteString.copyFrom(msgExtEntity.getContents())).build();

        SenderManager.getInstance().sendAckMsg(SocketACK.ROBOT_CHAT, chatKey(), msMessage.getMsgId(),msMessage.toByteString());
    }

    @Override
    public String chatKey() {
        return InstantSdk.instantSdk.getBaseContext().getString(R.string.app_name);
    }

    @Override
    public int chatType() {
        return 2;
    }

    @Override
    public long destructReceipt() {
        return 0L;
    }

    @Override
    public String headImg() {
        return InstantSdk.instantSdk.getBaseContext().getString(R.string.app_name);
    }

    @Override
    public String nickName() {
        return InstantSdk.instantSdk.getBaseContext().getString(R.string.app_name);
    }

    public ChatMsgEntity groupReviewMsg(Connect.Reviewed reviewed) {
        ChatMsgEntity msgExtEntity = createBaseChat(MessageType.GROUP_REVIEW);
        msgExtEntity.setContents(reviewed.toByteArray());

        return msgExtEntity;
    }

    public ChatMsgEntity systemAdNotice(Connect.Announcement announcement) {
        ChatMsgEntity msgExtEntity = createBaseChat(MessageType.SYSTEM_AD);
        msgExtEntity.setContents(announcement.toByteArray());

        return msgExtEntity;
    }
}
