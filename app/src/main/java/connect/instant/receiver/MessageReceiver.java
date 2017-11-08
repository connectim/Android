package connect.instant.receiver;

import android.text.TextUtils;

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
import connect.instant.model.CFriendChat;
import connect.instant.model.CGroupChat;
import connect.ui.activity.R;
import connect.utils.NotificationBar;
import connect.utils.StringUtil;
import connect.utils.TimeUtil;
import connect.utils.cryption.DecryptionUtil;
import connect.utils.cryption.EncryptionUtil;
import connect.utils.log.LogManager;
import instant.bean.ChatMsgEntity;
import instant.bean.MessageType;
import instant.parser.inter.MessageListener;
import instant.utils.manager.FailMsgsManager;
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
        return settingEntity == null ? 0 : settingEntity.getSnap_time();
    }

    @Override
    public void singleChat(Connect.ChatMessage chatMessage, byte[] contents) throws Exception {
        String friendUid = chatMessage.getFrom();
        ContactEntity contactEntity = ContactHelper.getInstance().loadFriendEntity(friendUid);
        if (contactEntity == null) {
            return;
        }

        CFriendChat friendChat = new CFriendChat(contactEntity);
        if (contents.length < 3) {
            LogManager.getLogger().d(TAG, "decode fail");

           if (contactEntity != null) {
                String showTxt = BaseApplication.getInstance().getString(R.string.Chat_Notice_New_Message);
                ChatMsgEntity msgExtEntity = friendChat.noticeMsg(0, showTxt, "");

                friendChat.updateRoomMsg(null, showTxt, chatMessage.getMsgTime(), -1, 1, false);

                MessageHelper.getInstance().insertMsgExtEntity(msgExtEntity);
                RecExtBean.getInstance().sendEvent(RecExtBean.ExtType.MESSAGE_RECEIVE, friendChat.chatKey(), msgExtEntity);
            }
        } else {
            ChatMsgEntity messageEntity = ChatMsgEntity.transToMessageEntity(chatMessage.getMsgId(),
                    chatMessage.getFrom(), chatMessage.getChatType().getNumber(),
                    chatMessage.getMsgType(), chatMessage.getFrom(),
                    chatMessage.getTo(), contents, chatMessage.getMsgTime(), 1);

            MessageType msgType = MessageType.toMessageType(chatMessage.getMsgType());
            switch (msgType) {
                case Self_destruct_Notice:
                    MessageHelper.getInstance().insertMsgExtEntity(messageEntity);

                    Connect.DestructMessage destructMessage = Connect.DestructMessage.parseFrom(contents);
                    ConversionSettingHelper.getInstance().updateBurnTime(friendUid, destructMessage.getTime());
                    break;
                case Self_destruct_Receipt:
                    Connect.ReadReceiptMessage readReceiptMessage = Connect.ReadReceiptMessage.parseFrom(contents);
                    MessageHelper.getInstance().updateBurnMsg(readReceiptMessage.getMessageId(), TimeUtil.getCurrentTimeInLong());
                    break;
                default:
                    MessageHelper.getInstance().insertMsgExtEntity(messageEntity);

                    friendChat.updateRoomMsg(null, messageEntity.showContent(), chatMessage.getMsgTime(), -1, 1, false);
                    break;
            }

            RecExtBean.getInstance().sendEvent(RecExtBean.ExtType.MESSAGE_RECEIVE, messageEntity.getMessage_from(), messageEntity);
            NotificationBar.notificationBar.noticeBarMsg(messageEntity.getMessage_from(), Connect.ChatType.PRIVATE_VALUE, messageEntity.showContent());
        }
    }

    @Override
    public void groupChat(Connect.MessagePost messagePost) {
        Connect.MessageData messageData = messagePost.getMsgData();
        Connect.ChatMessage chatMessage = messageData.getChatMsg();

        String groupIdentify = chatMessage.getTo();
        GroupEntity groupEntity = ContactHelper.getInstance().loadGroupEntity(groupIdentify);
        Connect.GcmData gcmData = chatMessage.getCipherData();

        if (groupEntity == null || TextUtils.isEmpty(groupEntity.getEcdh_key())) {//group backup
            FailMsgsManager.getInstance().insertReceiveMsg(groupIdentify, chatMessage.getMsgId(), messagePost);
            GroupRecBean.sendGroupRecMsg(GroupRecBean.GroupRecType.GroupInfo, groupIdentify);
        } else {
            byte[] contents = DecryptionUtil.decodeAESGCM(EncryptionUtil.ExtendedECDH.NONE, StringUtil.hexStringToBytes(groupEntity.getEcdh_key()), gcmData);
            if (contents.length < 3) {
                GroupRecBean.sendGroupRecMsg(GroupRecBean.GroupRecType.GroupInfo, groupIdentify);
            } else {
                ChatMsgEntity msgExtEntity = MessageHelper.getInstance().insertMessageEntity(chatMessage.getMsgId(), groupIdentify,
                        chatMessage.getChatType().getNumber(), chatMessage.getMsgType(), chatMessage.getFrom(),
                        chatMessage.getTo(), contents, chatMessage.getMsgTime(), 1);
                MessageHelper.getInstance().insertMsgExtEntity(msgExtEntity);

                CGroupChat groupChat = new CGroupChat(groupEntity);
                groupChat.updateRoomMsg(null, msgExtEntity.showContent(), chatMessage.getMsgTime(), -1, 1, false);

                RecExtBean.getInstance().sendEvent(RecExtBean.ExtType.MESSAGE_RECEIVE, groupIdentify, msgExtEntity);

                String content = msgExtEntity.showContent();
                String myUid = SharedPreferenceUtil.getInstance().getUser().getUid();
                if (chatMessage.getMsgType() == MessageType.Text.type) {
                    try {
                        Connect.TextMessage textMessage = Connect.TextMessage.parseFrom(contents);
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
    }

    @Override
    public void inviteJoinGroup(Connect.CreateGroupMessage groupMessage) {
        GroupRecBean.sendGroupRecMsg(GroupRecBean.GroupRecType.GroupInfo, groupMessage.getIdentifier());
    }
}
