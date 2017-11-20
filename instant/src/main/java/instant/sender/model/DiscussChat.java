package instant.sender.model;

import instant.bean.ChatMsgEntity;
import instant.bean.MessageType;
import instant.bean.Session;
import instant.utils.TimeUtil;
import protos.Connect;

/**
 * Discuss group Chat
 * Created by puin on 17-11-21.
 */
public class DiscussChat extends NormalChat {

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

    }

    @Override
    public String chatKey() {
        return null;
    }

    @Override
    public int chatType() {
        return 0;
    }

    @Override
    public String headImg() {
        return null;
    }

    @Override
    public String nickName() {
        return null;
    }

    @Override
    public long destructReceipt() {
        return 0;
    }
}
