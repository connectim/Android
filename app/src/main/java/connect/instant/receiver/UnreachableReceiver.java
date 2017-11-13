package connect.instant.receiver;

import connect.activity.base.BaseApplication;
import connect.activity.chat.bean.RecExtBean;
import connect.database.green.DaoHelper.ContactHelper;
import connect.database.green.DaoHelper.MessageHelper;
import connect.database.green.bean.ContactEntity;
import connect.database.green.bean.GroupEntity;
import connect.instant.model.CFriendChat;
import connect.instant.model.CGroupChat;
import connect.ui.activity.R;
import connect.utils.log.LogManager;
import instant.bean.ChatMsgEntity;
import instant.bean.Session;
import instant.bean.UserCookie;
import instant.parser.inter.UnreachableListener;
import instant.sender.model.FriendChat;
import instant.utils.SharedUtil;
import protos.Connect;

/**
 * Created by Administrator on 2017/10/10.
 */
public class UnreachableReceiver implements UnreachableListener {

    private static String TAG = "_UnreachableReceiver";

    public static UnreachableReceiver receiver = getInstance();

    private synchronized static UnreachableReceiver getInstance() {
        if (receiver == null) {
            receiver = new UnreachableReceiver();
        }
        return receiver;
    }

    @Override
    public void notFriendNotice(String publicKey) {
        RecExtBean.getInstance().sendEvent(RecExtBean.ExtType.NOTICE_NOTFRIEND,publicKey);
    }

    @Override
    public void blackFriendNotice(String publicKey) {
        ContactEntity friendEntity = ContactHelper.getInstance().loadFriendEntity(publicKey);
        if (friendEntity != null) {
            CFriendChat friendChat = new CFriendChat(friendEntity);

            String content = BaseApplication.getInstance().getBaseContext().getString(R.string.Chat_Message_Black_Friend_Notice);
            ChatMsgEntity msgExtEntity = friendChat.noticeMsg(5, content, publicKey);

            MessageHelper.getInstance().insertMsgExtEntity(msgExtEntity);
            RecExtBean.getInstance().sendEvent(RecExtBean.ExtType.MESSAGE_RECEIVE, publicKey, msgExtEntity);
        }
    }

    @Override
    public void notGroupMemberNotice(String groupKey) {
        GroupEntity groupEntity = ContactHelper.getInstance().loadGroupEntity(groupKey);
        if (groupEntity != null) {
            CGroupChat groupChat = new CGroupChat(groupEntity);

            String content = BaseApplication.getInstance().getBaseContext().getString(R.string.Message_send_fail_not_in_group);
            ChatMsgEntity msgExtEntity = groupChat.noticeMsg(0, content, "");

            MessageHelper.getInstance().insertMsgExtEntity(msgExtEntity);
            RecExtBean.getInstance().sendEvent(RecExtBean.ExtType.MESSAGE_RECEIVE, groupKey, msgExtEntity);
        }
    }

    @Override
    public void saltNotMatch(String msgid, String publicKey, Connect.ChatCookie cookie) throws Exception {
        Connect.ChatCookieData cookieData = cookie.getData();

        LogManager.getLogger().d(TAG, "saltNotMatch :" + msgid);
        UserCookie userCookie = new UserCookie();
        userCookie.setPubKey(cookieData.getChatPubKey());
        userCookie.setSalt(cookieData.getSalt().toByteArray());
        userCookie.setExpiredTime(cookieData.getExpired());
        Session.getInstance().setFriendCookie(publicKey, userCookie);
        SharedUtil.getInstance().insertFriendCookie(publicKey, userCookie);

        ContactEntity friendEntity= ContactHelper.getInstance().loadFriendEntity(publicKey);
        if (friendEntity == null) {
            return;
        }
        RecExtBean.getInstance().sendEvent(RecExtBean.ExtType.UNARRIVE_UPDATE, friendEntity.getCa_pub());

        ChatMsgEntity messageEntity = MessageHelper.getInstance().loadMsgByMsgid(msgid);
        if (messageEntity != null) {
            CFriendChat friendChat = new CFriendChat(friendEntity);
            friendChat.setEncryType(FriendChat.EncryType.NORMAL);
            friendChat.sendPushMsg(messageEntity);
        }
    }

    @Override
    public void halfRandom(String msgid, String publicKey) throws Exception {
        ContactEntity friendEntity = ContactHelper.getInstance().loadFriendEntity(publicKey);
        if (friendEntity == null) {
            return;
        }

        RecExtBean.getInstance().sendEvent(RecExtBean.ExtType.UNARRIVE_HALF, friendEntity.getCa_pub());

        LogManager.getLogger().d(TAG, "halfRandom :" + msgid);
        ChatMsgEntity messageEntity = MessageHelper.getInstance().loadMsgByMsgid(msgid);
        if (messageEntity != null) {
            CFriendChat friendChat = new CFriendChat(friendEntity);
            friendChat.setEncryType(FriendChat.EncryType.HALF);
            friendChat.sendPushMsg(messageEntity);
        }
    }
}
