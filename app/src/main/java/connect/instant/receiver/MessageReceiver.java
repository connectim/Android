package connect.instant.receiver;

import org.greenrobot.eventbus.EventBus;

import connect.utils.StringUtil;
import connect.utils.log.LogManager;
import instant.bean.ChatMsgEntity;
import instant.parser.inter.MessageListener;
import protos.Connect;

/**
 * Created by Administrator on 2017/10/10.
 */
public class MessageReceiver implements MessageListener {

    private String Tag = "_InstantChatReceiver";

    public static MessageReceiver receiver = getInstance();

    private synchronized static MessageReceiver getInstance() {
        if (receiver == null) {
            receiver = new MessageReceiver();
        }
        return receiver;
    }

    @Override
    public void singleChat(Connect.ChatMessage chatMessages, String publicKey, byte[] contents) throws Exception {
        if (contents.length < 3) {
            LogManager.getLogger().d(Tag, "decode fail");
        } else {
            ChatMsgEntity messageEntity = ChatMsgEntity.transToMessageEntity(chatMessages.getMsgId(),
                    chatMessages.getFrom(), chatMessages.getChatType().getNumber(),
                    chatMessages.getMsgType(), chatMessages.getFrom(),
                    chatMessages.getTo(), contents, chatMessages.getMsgTime(), 1);

            LogManager.getLogger().d(Tag, "decode success" + chatMessages.getMsgId() + ";" + StringUtil.bytesToHexString(contents));
            EventBus.getDefault().post(messageEntity);
        }
    }

    @Override
    public void groupChat(Connect.MessagePost messagePost) {
        Connect.MessageData messageData = messagePost.getMsgData();
        Connect.ChatMessage chatMessage = messageData.getChatMsg();

        String groupIdentify = chatMessage.getTo();
        Connect.GcmData gcmData = chatMessage.getCipherData();
    }

    @Override
    public void inviteJoinGroup(Connect.CreateGroupMessage groupMessage) {

    }
}
