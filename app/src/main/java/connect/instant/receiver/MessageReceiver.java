package connect.instant.receiver;

import com.google.protobuf.ByteString;

import connect.activity.base.BaseApplication;
import connect.activity.chat.bean.RecExtBean;
import connect.activity.home.bean.GroupRecBean;
import connect.database.SharedPreferenceUtil;
import connect.database.green.DaoHelper.ContactHelper;
import connect.database.green.DaoHelper.MessageHelper;
import connect.database.green.bean.ContactEntity;
import connect.database.green.bean.GroupEntity;
import connect.instant.model.CFriendChat;
import connect.instant.model.CGroupChat;
import connect.ui.activity.R;
import connect.utils.NotificationBar;
import connect.utils.ProtoBufUtil;
import connect.utils.RegularUtil;
import connect.utils.UriUtil;
import connect.utils.okhttp.OkHttpUtil;
import connect.utils.okhttp.ResultCall;
import instant.bean.ChatMsgEntity;
import instant.bean.MessageType;
import instant.bean.Session;
import instant.bean.UserCookie;
import instant.parser.inter.MessageListener;
import instant.utils.cryption.DecryptionUtil;
import instant.utils.cryption.EncryptionUtil;
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
    public void singleChat(Connect.MessageData messageData) throws Exception {
        Connect.ChatMessage chatMessage = messageData.getChatMsg();

        String friendUid = chatMessage.getFrom();
        String friendPublicKey = messageData.getChatSession().getPubKey();

        UserCookie userCookie = Session.getInstance().getConnectCookie();
        String myPrivateKey = userCookie.getPrivateKey();
        EncryptionUtil.ExtendedECDH ecdhExts = EncryptionUtil.ExtendedECDH.EMPTY;
        Connect.GcmData gcmData = chatMessage.getCipherData();
        byte[] contents = DecryptionUtil.decodeAESGCM(ecdhExts, myPrivateKey, friendPublicKey, gcmData);
        if (contents.length <= 2) {
            return;
        }

        Connect.MessageUserInfo senderInfo = chatMessage.getSender();
        String senderAvatar = senderInfo == null ? "" : senderInfo.getAvatar();
        String senderName = senderInfo == null ? "" : senderInfo.getUsername();

        chatMessage = chatMessage.toBuilder().setBody(ByteString.copyFrom(contents)).build();
        ChatMsgEntity chatMsgEntity = ChatMsgEntity.transToMessageEntity(chatMessage.getMsgId(),
                chatMessage.getFrom(), chatMessage.getChatType().getNumber(), chatMessage.getMsgType(),
                chatMessage.getFrom(), chatMessage.getTo(),
                chatMessage.getBody().toByteArray(), chatMessage.getMsgTime(), 1);
        MessageHelper.getInstance().insertMsgExtEntity(chatMsgEntity);

        long messageTime = chatMessage.getMsgTime();
        String content = chatMsgEntity.showContent();

        CFriendChat friendChat = new CFriendChat(friendUid);
        friendChat.setUserName(senderName);
        friendChat.setUserAvatar(senderAvatar);
        friendChat.updateRoomMsg(null, content, messageTime, -1, 1, false);

        RecExtBean.getInstance().sendEvent(RecExtBean.ExtType.MESSAGE_RECEIVE, chatMsgEntity.getMessage_from(), chatMsgEntity);
        NotificationBar.notificationBar.noticeBarMsg(friendUid, Connect.ChatType.PRIVATE_VALUE, senderName, content);
    }

    @Override
    public void groupChat(Connect.ChatMessage chatMessage) {
        String groupIdentify = chatMessage.getTo();
        GroupEntity groupEntity = ContactHelper.getInstance().loadGroupEntity(groupIdentify);

        // 显示文本的具体内容
        String txtContent = "";
        if (chatMessage.getMsgType() == MessageType.Text.type) {
            try {
                Connect.TextMessage textMessage = Connect.TextMessage.parseFrom(chatMessage.getBody());
                txtContent = textMessage.getContent();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        ChatMsgEntity msgExtEntity = MessageHelper.getInstance().insertMessageEntity(chatMessage.getMsgId(), groupIdentify,
                chatMessage.getChatType().getNumber(), chatMessage.getMsgType(), chatMessage.getFrom(),
                chatMessage.getTo(), chatMessage.getBody().toByteArray(), txtContent, chatMessage.getMsgTime(), 1);
        MessageHelper.getInstance().insertMsgExtEntity(msgExtEntity);

        String content = msgExtEntity.showContent();
        String myUid = SharedPreferenceUtil.getInstance().getUser().getUid();

        // At消息
        int isAtMe = -1;
        if (chatMessage.getMsgType() == MessageType.Text.type) {
            try {
                Connect.TextMessage textMessage = Connect.TextMessage.parseFrom(chatMessage.getBody());
                if (textMessage.getAtUidsList().lastIndexOf(myUid) != -1) {
                    isAtMe = 1;
                    content = BaseApplication.getInstance().getBaseContext().getString(R.string.Chat_Someone_note_me);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        int isMyAttention = -1;
        ContactEntity contactEntity = ContactHelper.getInstance().loadFriendEntity(chatMessage.getFrom());
        isMyAttention = contactEntity == null ? -1 : 1;

        Connect.MessageUserInfo senderInfo = chatMessage.getSender();
        String senderAvatar = senderInfo == null ? "" : senderInfo.getAvatar();
        String senderName = senderInfo == null ? "" : senderInfo.getUsername();
        String showContent = senderName + ": " + content;
        final GroupUpdate groupUpdate = new GroupUpdate(groupIdentify, showContent, chatMessage.getMsgTime(), isAtMe, isMyAttention, msgExtEntity);

        if (groupEntity == null) {//group backup
            GroupRecBean.sendGroupRecMsg(GroupRecBean.GroupRecType.GroupInfo, groupIdentify);
            Connect.GroupId groupId = Connect.GroupId.newBuilder()
                    .setIdentifier(groupIdentify)
                    .build();

            OkHttpUtil.getInstance().postEncrySelf(UriUtil.GROUP_PULLINFO, groupId, new ResultCall<Connect.HttpResponse>() {
                @Override
                public void onResponse(Connect.HttpResponse response) {
                    try {
                        Connect.StructData structData = Connect.StructData.parseFrom(response.getBody());
                        Connect.GroupInfo groupInfo = Connect.GroupInfo.parseFrom(structData.getPlainData());
                        if (ProtoBufUtil.getInstance().checkProtoBuf(groupInfo)) {
                            Connect.Group group = groupInfo.getGroup();
                            String groupIdentifier = group.getIdentifier();

                            GroupEntity groupEntity = new GroupEntity();
                            groupEntity.setIdentifier(groupIdentifier);
                            String groupname = group.getName();
                            groupEntity.setCategory(group.getCategory());
                            groupEntity.setName(groupname);
                            groupEntity.setAvatar(RegularUtil.groupAvatar(group.getIdentifier()));

                            groupUpdate.updateRoomMessage(groupEntity);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onError(Connect.HttpResponse response) {

                }
            });
        } else {
            groupUpdate.updateRoomMessage(groupEntity);
        }
    }

    private class GroupUpdate {

        String identify;
        String content;
        long messageTime;
        int isAtMe;
        int isMyAttention;
        ChatMsgEntity msgEntity;

        public GroupUpdate(String identify, String content, long messageTime, int isAtMe, int isMyAttention, ChatMsgEntity msgEntity) {
            this.identify = identify;
            this.content = content;
            this.messageTime = messageTime;
            this.isAtMe = isAtMe;
            this.isMyAttention = isMyAttention;
            this.msgEntity = msgEntity;
        }

        void updateRoomMessage(GroupEntity groupEntity) {
            CGroupChat cGroupChat = new CGroupChat(groupEntity);
            cGroupChat.updateRoomMsg(null, content, messageTime, isAtMe, 1, false, isMyAttention);
            RecExtBean.getInstance().sendEvent(RecExtBean.ExtType.MESSAGE_RECEIVE, identify, msgEntity);


            NotificationBar.notificationBar.noticeBarMsg(identify, Connect.ChatType.GROUPCHAT_VALUE, groupEntity.getName(), content);
        }
    }
}
