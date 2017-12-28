package connect.instant.receiver;

import connect.activity.base.BaseApplication;
import connect.activity.chat.bean.RecExtBean;
import connect.activity.home.bean.GroupRecBean;
import connect.database.SharedPreferenceUtil;
import connect.database.green.DaoHelper.ContactHelper;
import connect.database.green.DaoHelper.ConversionSettingHelper;
import connect.database.green.DaoHelper.MessageHelper;
import connect.database.green.bean.ContactEntity;
import connect.database.green.bean.ConversionSettingEntity;
import connect.database.green.bean.GroupEntity;
import connect.instant.inter.ConversationListener;
import connect.instant.model.CFriendChat;
import connect.instant.model.CGroupChat;
import connect.ui.activity.R;
import connect.utils.NotificationBar;
import instant.bean.ChatMsgEntity;
import instant.bean.MessageType;
import instant.parser.inter.MessageListener;
import protos.Connect;

/**
 * Created by Administrator on 2017/10/10.
 */
public class MessageReceiver implements MessageListener {

    private static String TAG = "_MessageReceiver";

    public static MessageReceiver receiver = getInstance();

    private synchronized static MessageReceiver getInstance() {
        if (receiver == null) {
            receiver = new MessageReceiver();
        }
        return receiver;
    }

    @Override
    public long chatBurnTime(String publicKey) {
        ConversionSettingEntity settingEntity = ConversionSettingHelper.getInstance().loadSetEntity(publicKey);
        return settingEntity == null || settingEntity.getSnap_time() == null ?
                0 : settingEntity.getSnap_time();
    }

    @Override
    public void singleChat(Connect.ChatMessage chatMessage) throws Exception {
        String friendUid = chatMessage.getFrom();
        ContactEntity contactEntity = ContactHelper.getInstance().loadFriendEntity(friendUid);
        if (contactEntity == null) {
            return;
        }

        CFriendChat friendChat = new CFriendChat(contactEntity);
        ChatMsgEntity chatMsgEntity = ChatMsgEntity.transToMessageEntity(chatMessage.getMsgId(),
                chatMessage.getFrom(), chatMessage.getChatType().getNumber(), chatMessage.getMsgType(),
                chatMessage.getFrom(), chatMessage.getTo(),
                chatMessage.getBody().toByteArray(), chatMessage.getMsgTime(), 1);

        MessageHelper.getInstance().insertMsgExtEntity(chatMsgEntity);
        friendChat.updateRoomMsg(null, chatMsgEntity.showContent(), chatMessage.getMsgTime(), -1, 1, false);

        RecExtBean.getInstance().sendEvent(RecExtBean.ExtType.MESSAGE_RECEIVE, chatMsgEntity.getMessage_from(), chatMsgEntity);
        NotificationBar.notificationBar.noticeBarMsg(chatMsgEntity.getMessage_from(), Connect.ChatType.PRIVATE_VALUE, chatMsgEntity.showContent());

    }

    @Override
    public void groupChat(Connect.ChatMessage chatMessage) {
        String groupIdentify = chatMessage.getTo();
        GroupEntity groupEntity = ContactHelper.getInstance().loadGroupEntity(groupIdentify);

        ChatMsgEntity msgExtEntity = MessageHelper.getInstance().insertMessageEntity(chatMessage.getMsgId(), groupIdentify,
                chatMessage.getChatType().getNumber(), chatMessage.getMsgType(), chatMessage.getFrom(),
                chatMessage.getTo(), chatMessage.getBody().toByteArray(), chatMessage.getMsgTime(), 1);
        MessageHelper.getInstance().insertMsgExtEntity(msgExtEntity);

        if (groupEntity == null) {//group backup
            GroupRecBean.sendGroupRecMsg(GroupRecBean.GroupRecType.GroupInfo, groupIdentify);
        } else {
            ConversationListener conversationListener = new CGroupChat(groupEntity);
            conversationListener.updateRoomMsg(null, msgExtEntity.showContent(), chatMessage.getMsgTime(), -1, 1, false);
            RecExtBean.getInstance().sendEvent(RecExtBean.ExtType.MESSAGE_RECEIVE, groupIdentify, msgExtEntity);

            String content = msgExtEntity.showContent();
            String myUid = SharedPreferenceUtil.getInstance().getUser().getUid();
            if (chatMessage.getMsgType() == MessageType.Text.type) {
                try {
                    Connect.TextMessage textMessage = Connect.TextMessage.parseFrom(chatMessage.getBody());
                    if (textMessage.getAtUidsList().lastIndexOf(myUid) != -1) {
                        content = BaseApplication.getInstance().getBaseContext().getString(R.string.Chat_Someone_note_me);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            NotificationBar.notificationBar.noticeBarMsg(groupIdentify, Connect.ChatType.GROUPCHAT_VALUE, content);
        }
    }

    @Override
    public void inviteJoinGroup(Connect.CreateGroupMessage groupMessage) {
    }
}
