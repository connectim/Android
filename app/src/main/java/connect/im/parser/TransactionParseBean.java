package connect.im.parser;

import android.content.Context;
import android.text.TextUtils;
import com.google.protobuf.ByteString;
import connect.activity.base.BaseApplication;
import connect.activity.chat.bean.MsgExtEntity;
import connect.activity.chat.bean.RecExtBean;
import connect.activity.chat.bean.Talker;
import connect.activity.chat.model.content.FriendChat;
import connect.activity.chat.model.content.GroupChat;
import connect.activity.chat.model.content.NormalChat;
import connect.activity.home.bean.HomeAction;
import connect.activity.home.bean.HttpRecBean;
import connect.database.MemoryDataManager;
import connect.database.green.DaoHelper.ContactHelper;
import connect.database.green.DaoHelper.MessageHelper;
import connect.database.green.DaoHelper.TransactionHelper;
import connect.database.green.bean.ContactEntity;
import connect.database.green.bean.GroupEntity;
import connect.database.green.bean.GroupMemberEntity;
import connect.database.green.bean.TransactionEntity;
import connect.im.inter.InterParse;
import connect.im.model.FailMsgsManager;
import connect.ui.activity.R;
import connect.utils.TimeUtil;
import protos.Connect;

/**
 * transaction notice
 * Created by pujin on 2017/4/19.
 */

public class TransactionParseBean extends InterParse {
    private Connect.NoticeMessage noticeMessage;

    public TransactionParseBean(Connect.NoticeMessage noticeMessage) {
        super((byte) 5, null);
        this.noticeMessage = noticeMessage;
    }

    /**
     * Transaction notice
     *
     * @throws Exception
     */
    @Override
    public synchronized void msgParse() throws Exception {
        int category = noticeMessage.getCategory();
        ByteString byteString = noticeMessage.getBody();
        switch (category) {
            case 0://The stranger transfer
                Connect.TransferNotice transferNotice = Connect.TransferNotice.parseFrom(byteString.toByteArray());
                strangerTransferNotice(transferNotice);
                break;
            case 1://Transfer transaction confirmation notice
                Connect.TransactionNotice transactionNotice = Connect.TransactionNotice.parseFrom(byteString.toByteArray());
                transactionConfirmNotice(transactionNotice);
                break;
            case 2://To receive a red packet to inform
                Connect.RedPackageNotice redPackgeNotice = Connect.RedPackageNotice.parseFrom(byteString.toByteArray());
                redpacketGetNotice(redPackgeNotice);
                break;
            case 3://The bill payment notice
            case 5://The bill payment notice
                Connect.BillNotice billNotice = Connect.BillNotice.parseFrom(byteString.toByteArray());
                singleBillPaymentNotice(billNotice);
                break;
            case 4://outer transfer
                Connect.TransferNotice notice = Connect.TransferNotice.parseFrom(byteString.toByteArray());
                outerTransfer(notice);
                break;
            case 6://The payment pay notice
                Connect.CrowdfundingNotice crowdfundingNotice = Connect.CrowdfundingNotice.parseFrom(byteString.toByteArray());
                groupBillPaymentNotice(crowdfundingNotice);
                break;
        }
    }

    private void strangerTransferNotice(Connect.TransferNotice notice) {
        Connect.UserInfo receiverInfo = notice.getReceiver();
        Connect.UserInfo senderInfo = notice.getSender();

        String senderPubkey = senderInfo.getPubKey();
        ContactEntity stranger = ContactHelper.getInstance().loadFriendEntity(senderPubkey);
        if (stranger == null) {
            stranger = new ContactEntity();
            stranger.setUsername(senderInfo.getUsername());
            stranger.setAvatar(senderInfo.getAvatar());
            stranger.setPub_key(senderInfo.getPubKey());
            stranger.setAddress(senderInfo.getAddress());
        }

        FriendChat normalChat = new FriendChat(stranger);
        MsgExtEntity msgExtEntity = normalChat.transferMsg(0, notice.getHashId(), notice.getAmount(), notice.getTips());
        msgExtEntity.setMessage_from(senderPubkey);
        msgExtEntity.setMessage_to(receiverInfo.getPubKey());

        MessageHelper.getInstance().insertMsgExtEntity(msgExtEntity);
        normalChat.updateRoomMsg(null, msgExtEntity.showContent(), msgExtEntity.getCreatetime(), -1, 1);
        RecExtBean.getInstance().sendEvent(RecExtBean.ExtType.MESSAGE_RECEIVE, normalChat.chatKey(), msgExtEntity);
    }

    /**
     * Transfer transaction confirmation
     *
     * @param notice
     */
    private void transactionConfirmNotice(Connect.TransactionNotice notice) {
        String hashId = notice.getHashId();
        TransactionHelper.getInstance().updateTransEntity(hashId, "", notice.getStatus());
    }

    /**
     * red packet is received notice
     *
     * @param notice
     */
    private void redpacketGetNotice(Connect.RedPackageNotice notice) {
        Context context = BaseApplication.getInstance().getBaseContext();

        ContactEntity friend = null;
        GroupEntity group = null;
        NormalChat normalChat = null;

        boolean isFriendNotice = notice.getIdentifer().equals(notice.getSender()) || notice.getIdentifer().equals(notice.getReceiver());
        if (isFriendNotice) {
            friend = ContactHelper.getInstance().loadFriendEntity(notice.getIdentifer());
            if (friend == null) return;
            normalChat = new FriendChat(friend);
        } else {
            group = ContactHelper.getInstance().loadGroupEntity(notice.getIdentifer());
            if (group == null) return;
            normalChat = new GroupChat(group);
        }

        String receiverAddress = notice.getReceiver();
        String receiverName = "";
        if (MemoryDataManager.getInstance().getAddress().equals(receiverAddress)) {
            receiverName = context.getString(R.string.Chat_You);
        } else {
            if (isFriendNotice) {
                receiverName = friend.getUsername();
            } else {
                GroupMemberEntity memEntity = ContactHelper.getInstance().loadGroupMemberEntity(group.getIdentifier(), receiverAddress);
                if (memEntity != null) {
                    receiverName = TextUtils.isEmpty(memEntity.getNick()) ? memEntity.getUsername() : memEntity.getNick();
                }
            }
        }

        String senderName = context.getString(R.string.Chat_You);
        String content = context.getString(R.string.Chat_opened_Lucky_Packet_of, receiverName, senderName);

        MsgExtEntity msgExtEntity = normalChat.noticeMsg(content);
        MessageHelper.getInstance().insertMsgExtEntity(msgExtEntity);
        RecExtBean.getInstance().sendEvent(RecExtBean.ExtType.MESSAGE_RECEIVE, normalChat.chatKey(), msgExtEntity);
        normalChat.updateRoomMsg(null, msgExtEntity.showContent(), msgExtEntity.getCreatetime(), -1, 1);
    }

    private void singleBillPaymentNotice(Connect.BillNotice billNotice) {
        String hashId = billNotice.getHashId();
        Context context = BaseApplication.getInstance().getBaseContext();
        TransactionHelper.getInstance().updateTransEntity(hashId, "", 1);

        ContactEntity friendEntity = ContactHelper.getInstance().loadFriendEntity(billNotice.getSender());
        String showName = TextUtils.isEmpty(friendEntity.getRemark()) ? friendEntity.getUsername() : friendEntity.getRemark();
        String content = context.getResources().getString(R.string.Chat_paid_the_bill_to, showName, context.getString(R.string.Chat_You));

        if (friendEntity == null) {
            requestFriendsByVersion();
            FailMsgsManager.getInstance().insertReceiveMsg(billNotice.getSender(), TimeUtil.timestampToMsgid(), content);
        } else {
            String pubkey = friendEntity.getPub_key();
            NormalChat normalChat = new FriendChat(friendEntity);
            MsgExtEntity msgExtEntity = normalChat.noticeMsg(content);
            MessageHelper.getInstance().insertMsgExtEntity(msgExtEntity);
            normalChat.updateRoomMsg(null, msgExtEntity.showContent(), msgExtEntity.getCreatetime(), -1, 1);

            RecExtBean.getInstance().sendEvent(RecExtBean.ExtType.MESSAGE_RECEIVE, pubkey, msgExtEntity);
        }
    }

    private void groupBillPaymentNotice(Connect.CrowdfundingNotice crowdfundingNotice) {
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
        if (MemoryDataManager.getInstance().getAddress().equals(crowdfundingNotice.getSender())) {
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
        if (groupEntity == null || TextUtils.isEmpty(groupEntity.getEcdh_key())) {
            HttpRecBean.sendHttpRecMsg(HttpRecBean.HttpRecType.GroupInfo, groupid);

            FailMsgsManager.getInstance().insertReceiveMsg(groupid, TimeUtil.timestampToMsgid(), content);
            if (transEntity.getPay_count() == transEntity.getCrowd_count()) {
                FailMsgsManager.getInstance().insertReceiveMsg(groupid, TimeUtil.timestampToMsgid(), context.getString(R.string.Chat_Founded_complete));
            }
        } else {
            NormalChat normalChat = new GroupChat(groupEntity);
            if (!TextUtils.isEmpty(content)) {
                MsgExtEntity msgExtEntity = normalChat.noticeMsg(content);
                MessageHelper.getInstance().insertMsgExtEntity(msgExtEntity);
                normalChat.updateRoomMsg(null, msgExtEntity.showContent(), msgExtEntity.getCreatetime(), -1, 1);
                RecExtBean.getInstance().sendEvent(RecExtBean.ExtType.MESSAGE_RECEIVE, groupid, msgExtEntity);
            }

            if (transEntity.getPay_count() == transEntity.getCrowd_count()) {
                MsgExtEntity msgExtEntity = normalChat.noticeMsg(context.getString(R.string.Chat_Founded_complete));
                MessageHelper.getInstance().insertMsgExtEntity(msgExtEntity);
                normalChat.updateRoomMsg(null, msgExtEntity.showContent(), msgExtEntity.getCreatetime(), -1, 1);
                RecExtBean.getInstance().sendEvent(RecExtBean.ExtType.MESSAGE_RECEIVE, groupid, msgExtEntity);
            }
        }
    }

    /**
     * outer transaction
     */
    private void outerTransfer(Connect.TransferNotice notice) {
        ContactEntity friendEntity = null;
        String mypublickey = MemoryDataManager.getInstance().getPubKey();
        Connect.UserInfo userInfo = notice.getReceiver();

        if (MemoryDataManager.getInstance().getPubKey().equals(userInfo.getPubKey())) {
            userInfo = notice.getSender();
        } else if (MemoryDataManager.getInstance().getPubKey().equals(notice.getSender().getPubKey())) {
            userInfo = notice.getReceiver();
        }

        if (MemoryDataManager.getInstance().getPubKey().equals(userInfo.getPubKey())) {
            return;
        }

        friendEntity = ContactHelper.getInstance().loadFriendEntity(userInfo.getPubKey());
        if (friendEntity == null) {
            friendEntity = new ContactEntity();
            friendEntity.setPub_key(userInfo.getPubKey());
            friendEntity.setAvatar(userInfo.getAvatar());
            friendEntity.setUsername(userInfo.getUsername());
            friendEntity.setAddress(userInfo.getAddress());
        }

        NormalChat normalChat = new FriendChat(friendEntity);
        MsgExtEntity msgExtEntity = normalChat.transferMsg(1, notice.getHashId(), notice.getAmount(), notice.getTips());
        msgExtEntity.setMessage_from(friendEntity.getPub_key());
        msgExtEntity.setMessage_to(mypublickey);

        MessageHelper.getInstance().insertMsgExtEntity(msgExtEntity);
        normalChat.updateRoomMsg(null, msgExtEntity.showContent(), msgExtEntity.getCreatetime(), -1, 1);
        HomeAction.getInstance().sendEvent(HomeAction.HomeType.TOCHAT, new Talker(friendEntity));
    }
}
