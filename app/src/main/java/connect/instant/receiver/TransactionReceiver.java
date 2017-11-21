package connect.instant.receiver;

import android.content.Context;
import android.text.TextUtils;

import connect.activity.base.BaseApplication;
import connect.activity.chat.bean.RecExtBean;
import connect.activity.chat.bean.Talker;
import connect.activity.home.bean.GroupRecBean;
import connect.activity.home.bean.HomeAction;
import connect.database.SharedPreferenceUtil;
import connect.database.green.DaoHelper.ContactHelper;
import connect.database.green.DaoHelper.MessageHelper;
import connect.database.green.DaoHelper.TransactionHelper;
import connect.database.green.bean.ContactEntity;
import connect.database.green.bean.GroupEntity;
import connect.database.green.bean.GroupMemberEntity;
import connect.database.green.bean.TransactionEntity;
import connect.instant.inter.ConversationListener;
import connect.instant.model.CFriendChat;
import connect.instant.model.CGroupChat;
import connect.ui.activity.R;
import connect.utils.TimeUtil;
import instant.bean.ChatMsgEntity;
import instant.parser.inter.TransactionListener;
import instant.sender.model.NormalChat;
import instant.utils.manager.FailMsgsManager;
import protos.Connect;

/**
 * Created by Administrator on 2017/10/10.
 */

public class TransactionReceiver implements TransactionListener {

    private static String TAG = "_TransactionReceiver";

    public static TransactionReceiver receiver = getInstance();

    private synchronized static TransactionReceiver getInstance() {
        if (receiver == null) {
            receiver = new TransactionReceiver();
        }
        return receiver;
    }

    @Override
    public void strangerTransferNotice(Connect.TransferNotice transferNotice) {
        Connect.UserInfo receiverInfo = transferNotice.getReceiver();
        Connect.UserInfo senderInfo = transferNotice.getSender();

        String senderPubkey = senderInfo.getPubKey();
        ContactEntity stranger = ContactHelper.getInstance().loadFriendEntity(senderPubkey);
        if (stranger == null) {
            stranger = new ContactEntity();
            stranger.setUsername(senderInfo.getUsername());
            stranger.setAvatar(senderInfo.getAvatar());
            stranger.setUid(senderInfo.getUid());
            stranger.setCa_pub(senderInfo.getCaPub());
        }

        CFriendChat normalChat = new CFriendChat(stranger);
        ChatMsgEntity msgExtEntity = normalChat.transferMsg(0, transferNotice.getHashId(), transferNotice.getAmount(), transferNotice.getTips());
        msgExtEntity.setMessage_from(senderPubkey);
        msgExtEntity.setMessage_to(receiverInfo.getPubKey());

        MessageHelper.getInstance().insertMsgExtEntity(msgExtEntity);
        normalChat.updateRoomMsg(null, msgExtEntity.showContent(), msgExtEntity.getCreatetime(), -1, 1);
        RecExtBean.getInstance().sendEvent(RecExtBean.ExtType.MESSAGE_RECEIVE, normalChat.chatKey(), msgExtEntity);
    }

    @Override
    public void transactionConfirmNotice(Connect.TransactionNotice notice) {
        String hashId = notice.getHashId();
        TransactionHelper.getInstance().updateTransEntity(hashId, "", notice.getStatus());
    }

    @Override
    public void redpacketGetNotice(Connect.RedPackageNotice notice) {
        Context context = BaseApplication.getInstance().getBaseContext();

        NormalChat normalChat = null;
        Connect.ChatType chatType = ContactHelper.getInstance().loadChatType(notice.getIdentifer());
        if (chatType == Connect.ChatType.PRIVATE) {
            ContactEntity contactEntity = ContactHelper.getInstance().loadFriendEntity(notice.getIdentifer());
            normalChat = new CFriendChat(contactEntity);
        } else if (chatType == Connect.ChatType.GROUPCHAT) {
            GroupEntity groupEntity = ContactHelper.getInstance().loadGroupEntity(notice.getIdentifer());
            normalChat = new CGroupChat(groupEntity);
        }

        if (normalChat != null) {
            String receiverAddress = notice.getReceiver();
            String receiverName = "";
            if (SharedPreferenceUtil.getInstance().getUser().getUid().equals(receiverAddress)) {
                receiverName = context.getString(R.string.Chat_You);
            } else {
                if (normalChat instanceof CFriendChat) {
                    receiverName = normalChat.nickName();
                } else if (normalChat instanceof CGroupChat) {
                    receiverName = "*";
                    GroupMemberEntity memEntity = ContactHelper.getInstance().loadGroupMemberEntity(notice.getIdentifer(), receiverAddress);
                    if (memEntity != null) {
                        receiverName = TextUtils.isEmpty(memEntity.getNick()) ? memEntity.getUsername() : memEntity.getNick();
                    }
                }
            }

            String senderName = context.getString(R.string.Chat_You);
            String content = context.getString(R.string.Chat_opened_Lucky_Packet_of, receiverName, senderName);

            ChatMsgEntity msgExtEntity = normalChat.noticeMsg(3, content, notice.getHashId());
            MessageHelper.getInstance().insertMsgExtEntity(msgExtEntity);
            RecExtBean.getInstance().sendEvent(RecExtBean.ExtType.MESSAGE_RECEIVE, normalChat.chatKey(), msgExtEntity);
            ((ConversationListener) normalChat).updateRoomMsg(null, msgExtEntity.showContent(), msgExtEntity.getCreatetime(), -1, 1);
        }
    }

    @Override
    public void singleBillPaymentNotice(Connect.BillNotice billNotice) {
        String hashId = billNotice.getHashId();
        Context context = BaseApplication.getInstance().getBaseContext();
        TransactionHelper.getInstance().updateTransEntity(hashId, "", 1);

        ContactEntity friendEntity = ContactHelper.getInstance().loadFriendEntity(billNotice.getSender());
        String showName = TextUtils.isEmpty(friendEntity.getRemark()) ? friendEntity.getUsername() : friendEntity.getRemark();
        String content = context.getResources().getString(R.string.Chat_paid_the_bill_to, showName, context.getString(R.string.Chat_You));

        if (friendEntity == null) {
            FailMsgsManager.getInstance().insertReceiveMsg(billNotice.getSender(), TimeUtil.timestampToMsgid(), content);
        } else {
            String uid = friendEntity.getUid();
            CFriendChat normalChat = new CFriendChat(friendEntity);
            ChatMsgEntity msgExtEntity = normalChat.noticeMsg(1, content, hashId);
            MessageHelper.getInstance().insertMsgExtEntity(msgExtEntity);
            normalChat.updateRoomMsg(null, msgExtEntity.showContent(), msgExtEntity.getCreatetime(), -1, 1);

            RecExtBean.getInstance().sendEvent(RecExtBean.ExtType.MESSAGE_RECEIVE, uid, msgExtEntity);
        }
    }

    @Override
    public void groupBillPaymentNotice(Connect.CrowdfundingNotice crowdfundingNotice) {
        String hashId = crowdfundingNotice.getHashId();

        TransactionHelper.getInstance().updateTransEntity(hashId, "", -1, 0);

        String groupid = crowdfundingNotice.getGroupId();
        Context context = BaseApplication.getInstance().getBaseContext();
        GroupEntity groupEntity = ContactHelper.getInstance().loadGroupEntity(groupid);

        String receiverName = "";
        GroupMemberEntity receiverEntity = ContactHelper.getInstance().loadGroupMemberEntity(groupid, crowdfundingNotice.getReceiver());
        if (receiverEntity != null) {
            receiverName = TextUtils.isEmpty(receiverEntity.getNick()) ? receiverEntity.getUsername() : receiverEntity.getNick();
        }

        String senderName = "";
        if (SharedPreferenceUtil.getInstance().getUser().getUid().equals(crowdfundingNotice.getSender())) {
            senderName = context.getString(R.string.Chat_You);
        } else {
            GroupMemberEntity senderEntity = ContactHelper.getInstance().loadGroupMemberEntity(groupid, crowdfundingNotice.getReceiver());
            if (senderEntity != null) {
                senderName = TextUtils.isEmpty(senderEntity.getNick()) ? senderEntity.getUsername() : senderEntity.getNick();
            }
        }

        String content = context.getResources().getString(R.string.Chat_paid_the_bill_to,
                receiverName, senderName);
        TransactionEntity transEntity = TransactionHelper.getInstance().loadTransEntity(hashId);
        if (groupEntity == null) {
            GroupRecBean.sendGroupRecMsg(GroupRecBean.GroupRecType.GroupInfo, groupid);

            FailMsgsManager.getInstance().insertReceiveMsg(groupid, TimeUtil.timestampToMsgid(), content);
            if (transEntity.getPay_count() == transEntity.getCrowd_count()) {
                FailMsgsManager.getInstance().insertReceiveMsg(groupid, TimeUtil.timestampToMsgid(), context.getString(R.string.Chat_Founded_complete));
            }
        } else {
            CGroupChat normalChat = new CGroupChat(groupEntity);
            if (!TextUtils.isEmpty(content)) {
                ChatMsgEntity msgExtEntity = normalChat.noticeMsg(2, content, hashId);
                MessageHelper.getInstance().insertMsgExtEntity(msgExtEntity);
                normalChat.updateRoomMsg(null, msgExtEntity.showContent(), msgExtEntity.getCreatetime(), -1, 1);
                RecExtBean.getInstance().sendEvent(RecExtBean.ExtType.MESSAGE_RECEIVE, groupid, msgExtEntity);
            }

            if (transEntity.getPay_count() == transEntity.getCrowd_count()) {
                ChatMsgEntity msgExtEntity = normalChat.noticeMsg(2, context.getString(R.string.Chat_Founded_complete), hashId);
                MessageHelper.getInstance().insertMsgExtEntity(msgExtEntity);
                normalChat.updateRoomMsg(null, msgExtEntity.showContent(), msgExtEntity.getCreatetime(), -1, 1);
                RecExtBean.getInstance().sendEvent(RecExtBean.ExtType.MESSAGE_RECEIVE, groupid, msgExtEntity);
            }
        }
    }

    @Override
    public void outerTransfer(Connect.TransferNotice notice) {
        String mypublickey = SharedPreferenceUtil.getInstance().getUser().getPubKey();
        Connect.UserInfo sender = notice.getSender();
        Connect.UserInfo receiver = notice.getReceiver();

        boolean isMySendTo = sender.getPubKey().equals(mypublickey);
        Connect.UserInfo friendInfo = isMySendTo ? receiver : sender;

        ContactEntity friendEntity = ContactHelper.getInstance().loadFriendEntity(friendInfo.getPubKey());
        if (friendEntity == null) {
            friendEntity = new ContactEntity();
            friendEntity.setUid(friendInfo.getUid());
            friendEntity.setCa_pub(friendInfo.getCaPub());
            friendEntity.setAvatar(friendInfo.getAvatar());
            friendEntity.setUsername(friendInfo.getUsername());
        }

        NormalChat normalChat = new CFriendChat(friendEntity);
        ChatMsgEntity msgExtEntity = normalChat.transferMsg(2, notice.getHashId(), notice.getAmount(), notice.getTips());
        if (isMySendTo) {
            msgExtEntity.setSend_status(1);
        } else {
            msgExtEntity.setMessage_from(friendEntity.getCa_pub());
            msgExtEntity.setMessage_to(mypublickey);
        }

        MessageHelper.getInstance().insertMsgExtEntity(msgExtEntity);
        ((ConversationListener) normalChat).updateRoomMsg(null, msgExtEntity.showContent(), msgExtEntity.getCreatetime(), -1, 1);
        HomeAction.getInstance().sendEvent(HomeAction.HomeType.TOCHAT, new Talker(Connect.ChatType.PRIVATE,friendEntity.getUid()));
    }
}
