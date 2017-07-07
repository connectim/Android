package connect.im.parser;

import android.content.Context;
import android.text.TextUtils;

import com.google.protobuf.ByteString;

import connect.db.MemoryDataManager;
import connect.db.green.DaoHelper.ContactHelper;
import connect.db.green.DaoHelper.MessageHelper;
import connect.db.green.DaoHelper.TransactionHelper;
import connect.db.green.bean.ContactEntity;
import connect.db.green.bean.GroupEntity;
import connect.db.green.bean.GroupMemberEntity;
import connect.db.green.bean.TransactionEntity;
import connect.im.inter.InterParse;
import connect.im.model.FailMsgsManager;
import connect.ui.activity.R;
import connect.ui.activity.chat.bean.MsgEntity;
import connect.ui.activity.chat.bean.MsgSender;
import connect.ui.activity.chat.bean.RecExtBean;
import connect.ui.activity.chat.bean.Talker;
import connect.ui.activity.chat.model.content.FriendChat;
import connect.ui.activity.chat.model.content.GroupChat;
import connect.ui.activity.chat.model.content.NormalChat;
import connect.ui.activity.home.bean.HomeAction;
import connect.ui.activity.home.bean.HttpRecBean;
import connect.ui.base.BaseApplication;
import connect.utils.TimeUtil;
import protos.Connect;

/**
 * transaction notice
 * Created by pujin on 2017/4/19.
 */

public class TransactionParseBean extends InterParse{
    private Connect.NoticeMessage noticeMessage;

    public TransactionParseBean(Connect.NoticeMessage noticeMessage) {
        super((byte)5, null);
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

        NormalChat normalChat = new FriendChat(stranger);
        MsgEntity msgEntity = normalChat.transferMsg(notice.getHashId(), notice.getAmount(), notice.getTips(), 0);
        MsgSender msgSender = new MsgSender(stranger.getPub_key(), stranger.getUsername(), stranger.getAddress(), stranger.getAvatar());
        msgEntity.getMsgDefinBean().setSenderInfoExt(msgSender);
        MessageHelper.getInstance().insertFromMsg(senderPubkey, msgEntity.getMsgDefinBean());
        normalChat.updateRoomMsg(null,  msgEntity.getMsgDefinBean().showContentTxt(normalChat.roomType()), msgEntity.getMsgDefinBean().getSendtime(),-1,true);

        RecExtBean.sendRecExtMsg(RecExtBean.ExtType.MESSAGE_RECEIVE,normalChat.roomKey(),msgEntity);
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
                GroupMemberEntity memEntity = ContactHelper.getInstance().loadGroupMemByAds(group.getIdentifier(), receiverAddress);
                if (memEntity != null) {
                    receiverName = TextUtils.isEmpty(memEntity.getNick()) ? memEntity.getUsername() : memEntity.getNick();
                }
            }
        }

        String senderName = context.getString(R.string.Chat_You);
        String content = context.getString(R.string.Chat_opened_Lucky_Packet_of, receiverName, senderName);

        MsgEntity msgEntity = normalChat.noticeMsg(content);
        MessageHelper.getInstance().insertToMsg(msgEntity.getMsgDefinBean());
        RecExtBean.sendRecExtMsg(RecExtBean.ExtType.MESSAGE_RECEIVE,normalChat.roomKey(),msgEntity);

        normalChat.updateRoomMsg(null,msgEntity.getMsgDefinBean().showContentTxt(normalChat.roomType()),msgEntity.getMsgDefinBean().getSendtime(),-1,true);
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
            MsgEntity msgEntity = normalChat.noticeMsg(content);
            MessageHelper.getInstance().insertFromMsg(pubkey, msgEntity.getMsgDefinBean());
            normalChat.updateRoomMsg(null,  msgEntity.getMsgDefinBean().showContentTxt(normalChat.roomType()), msgEntity.getMsgDefinBean().getSendtime(),-1,true);

            RecExtBean.sendRecExtMsg(RecExtBean.ExtType.MESSAGE_RECEIVE,pubkey,msgEntity);
        }
    }

    private void groupBillPaymentNotice(Connect.CrowdfundingNotice crowdfundingNotice) {
        String hashId = crowdfundingNotice.getHashId();

        TransactionHelper.getInstance().updateTransEntity(hashId, "", -1, 0);

        String groupid = crowdfundingNotice.getGroupId();
        Context context = BaseApplication.getInstance().getBaseContext();
        GroupEntity groupEntity = ContactHelper.getInstance().loadGroupEntity(groupid);

        String receiverName = "";
        GroupMemberEntity receiverEntity = ContactHelper.getInstance().loadGroupMemByAds(groupid, crowdfundingNotice.getReceiver());
        if (receiverEntity != null) {
            receiverName = TextUtils.isEmpty(receiverEntity.getNick()) ? receiverEntity.getUsername() : receiverEntity.getNick();
        }

        String senderName = "";
        if (MemoryDataManager.getInstance().getAddress().equals(crowdfundingNotice.getSender())) {
            senderName = context.getString(R.string.Chat_You);
        } else {
            GroupMemberEntity senderEntity = ContactHelper.getInstance().loadGroupMemByAds(groupid, crowdfundingNotice.getReceiver());
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
                MsgEntity msgEntity = normalChat.noticeMsg(content);
                MessageHelper.getInstance().insertFromMsg(groupid, msgEntity.getMsgDefinBean());
                normalChat.updateRoomMsg(null, msgEntity.getMsgDefinBean().showContentTxt(normalChat.roomType()), msgEntity.getMsgDefinBean().getSendtime(),-1,true);

                RecExtBean.sendRecExtMsg(RecExtBean.ExtType.MESSAGE_RECEIVE,groupid,msgEntity);
            }

            if (transEntity.getPay_count() == transEntity.getCrowd_count()) {
                MsgEntity msgEntity = normalChat.noticeMsg(context.getString(R.string.Chat_Founded_complete));
                MessageHelper.getInstance().insertFromMsg(groupid, msgEntity.getMsgDefinBean());
                normalChat.updateRoomMsg(null, msgEntity.getMsgDefinBean().showContentTxt(normalChat.roomType()), msgEntity.getMsgDefinBean().getSendtime(),-1,true);

                RecExtBean.sendRecExtMsg(RecExtBean.ExtType.MESSAGE_RECEIVE, groupid, msgEntity);
            }
        }
    }

    /**
     * outer transaction
     */
    private void outerTransfer(Connect.TransferNotice notice) {
        ContactEntity friendEntity = null;
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
        MsgEntity msgEntity = normalChat.transferMsg(notice.getHashId(), notice.getAmount(), notice.getTips(), 1);
        msgEntity.getMsgDefinBean().setSenderInfoExt(new MsgSender(friendEntity.getPub_key(), friendEntity.getUsername(), friendEntity.getAddress(), friendEntity.getAvatar()));
        MessageHelper.getInstance().insertFromMsg(normalChat.roomKey(), msgEntity.getMsgDefinBean());
        String showTxt = msgEntity.getMsgDefinBean().showContentTxt(0);
        normalChat.updateRoomMsg(null, showTxt, TimeUtil.getCurrentTimeInLong(),-1,true);
        HomeAction.sendTypeMsg(HomeAction.HomeType.TOCHAT, new Talker(friendEntity));
    }
}
