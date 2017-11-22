package instant.sender.model;

import instant.R;
import instant.bean.ChatMsgEntity;
import instant.bean.MessageType;
import instant.bean.Session;
import instant.ui.InstantSdk;
import instant.utils.TimeUtil;
import protos.Connect;

/**
 * Created by Administrator on 2017/11/22.
 */

public class SubscriberChat extends NormalChat{

    public static SubscriberChat subscriberChat = getInstance();

    private synchronized static SubscriberChat getInstance() {
        if (subscriberChat == null) {
            subscriberChat = new SubscriberChat();
        }
        return subscriberChat;
    }

    @Override
    public ChatMsgEntity createBaseChat(MessageType type) {
        String myUid = Session.getInstance().getConnectCookie().getUid();

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
     }

    @Override
    public String chatKey() {
        return InstantSdk.instantSdk.getBaseContext().getString(R.string.Chat_Subscriber);
    }

    @Override
    public int chatType() {
        return Connect.ChatType.SUBSCRIBER_VALUE;
    }

    @Override
    public long destructReceipt() {
        return 0L;
    }

    @Override
    public String headImg() {
        return InstantSdk.instantSdk.getBaseContext().getString(R.string.Chat_Subscriber);
    }

    @Override
    public String nickName() {
        return InstantSdk.instantSdk.getBaseContext().getString(R.string.Chat_Subscriber);
    }
}
