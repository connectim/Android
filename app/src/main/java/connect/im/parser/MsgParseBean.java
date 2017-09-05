package connect.im.parser;

import com.google.gson.Gson;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;

import connect.activity.base.BaseApplication;
import connect.activity.chat.bean.ApplyGroupBean;
import connect.activity.chat.bean.MsgExtEntity;
import connect.activity.chat.bean.RecExtBean;
import connect.activity.chat.model.content.FriendChat;
import connect.activity.chat.model.content.GroupChat;
import connect.activity.chat.model.content.RobotChat;
import connect.activity.login.bean.UserBean;
import connect.database.MemoryDataManager;
import connect.database.SharedPreferenceUtil;
import connect.database.green.DaoHelper.ContactHelper;
import connect.database.green.DaoHelper.MessageHelper;
import connect.database.green.DaoHelper.ParamHelper;
import connect.database.green.DaoHelper.ParamManager;
import connect.database.green.bean.ContactEntity;
import connect.database.green.bean.GroupEntity;
import connect.database.green.bean.MessageEntity;
import connect.database.green.bean.ParamEntity;
import connect.im.bean.Session;
import connect.im.bean.UserCookie;
import connect.im.inter.InterParse;
import connect.im.model.FailMsgsManager;
import connect.ui.activity.R;
import connect.utils.StringUtil;
import protos.Connect;

/**
 * message parse
 * Created by gtq on 2016/12/14.
 */
public class MsgParseBean extends InterParse {

    private String Tag = "MsgParseBean";

    /**
     * Parsing the source 0:offline message 1:online message
     */
    private int ext = 1;

    public MsgParseBean(byte ackByte, ByteBuffer byteBuffer) {
        super(ackByte, byteBuffer);
        ext = 1;

        try {
            Connect.StructData structData = imTransferToStructData(byteBuffer);
            this.byteBuffer = ByteBuffer.wrap(structData.getPlainData().toByteArray());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public MsgParseBean(byte ackByte, ByteBuffer byteBuffer, int ext) {
        super(ackByte, byteBuffer);
        this.ext = ext;
    }

    @Override
    public synchronized void msgParse() throws Exception {
        switch (ackByte) {
            case 0x00://robot message
                robotMsg();
                break;
            case 0x05://unavailable message
                unavailableMsg();
                break;
            case 0x09://notice message
                noticeMsg();
                break;
            case 0x19://repeat apply group
                break;
            default:
                chatMsg();
                break;
        }
    }

    /**
     * robotu message
     */
    private void robotMsg() throws Exception {
        Connect.MSMessage msMessage = Connect.MSMessage.parseFrom(byteBuffer.array());
        if (ext == 0) {
            backOffLineAck(5, msMessage.getMsgId());
        } else {
            backOnLineAck(5, msMessage.getMsgId());
        }

        String robotname = BaseApplication.getInstance().getString(R.string.app_name);
        MsgExtEntity msgExtEntity = robotMsgDeal(msMessage);
        MessageHelper.getInstance().insertMsgExtEntity(msgExtEntity);

        String content = msgExtEntity.showContent();
        RobotChat.getInstance().updateRoomMsg(null, content, msgExtEntity.getCreatetime(), -1, 1);
        pushNoticeMsg(robotname, 2, content);
    }

    /**
     * unavailable message
     */
    private void unavailableMsg()throws Exception{
        Connect.RejectMessage rejectMessage = Connect.RejectMessage.parseFrom(byteBuffer.array());
        if (ext == 0) {
            backOffLineAck(5, rejectMessage.getMsgId());
        } else {
            backOnLineAck(5, rejectMessage.getMsgId());
        }

        String msgid = rejectMessage.getMsgId();
        String recPublicKey = rejectMessage.getUid();
        switch (rejectMessage.getStatus()) {
            case 1://The user does not exist
            case 2://Is not a friend relationship
                Map<String, Object> friendFail = FailMsgsManager.getInstance().getFailMap(msgid);
                if (friendFail != null) {
                    insertNotFriendNotice((String) friendFail.get("PUBKEY"));
                }
                break;
            case 3://in blakc list
                blackFriendNotice(rejectMessage.getUid());
                break;
            case 4://Not in the group
                Map<String, Object> groupFail = FailMsgsManager.getInstance().getFailMap(msgid);
                if (groupFail != null) {
                    notGroupMemberNotice((String) groupFail.get("PUBKEY"));
                }
                break;
            case 5://Chat information is empty
            case 6://Error accessing chat information
            case 7://Chat messages do not match
                Connect.ChatCookie chatCookie = Connect.ChatCookie.parseFrom(rejectMessage.getData());
                saltNotMatch(msgid, recPublicKey, chatCookie);
                break;
            case 8://The other cookies expire, single side
                halfRandom(msgid, recPublicKey);
                break;
            case 9://upload cookie expire
                reloadUserCookie(msgid, recPublicKey);
                break;
        }
        receiptMsg(msgid, 3);
    }

    /**
     * notice message
     */
    private void noticeMsg() throws Exception {
        Connect.NoticeMessage noticeMessage = Connect.NoticeMessage.parseFrom(byteBuffer.array());
        if (ext == 0) {
            backOffLineAck(5, noticeMessage.getMsgId());
        } else {
            backOnLineAck(5, noticeMessage.getMsgId());
        }

        TransactionParseBean parseBean = new TransactionParseBean(noticeMessage);
        parseBean.msgParse();
    }

    /**
     * chat message
     * @throws Exception
     */
    private synchronized void chatMsg() throws Exception {
        Connect.MessagePost messagePost = Connect.MessagePost.parseFrom(byteBuffer.array());
        Connect.MessageData messageData = messagePost.getMsgData();

        if (ext == 0) {
            backOffLineAck(5, messageData.getChatMsg().getMsgId());
        } else {
            backOnLineAck(5, messageData.getChatMsg().getMsgId());
        }

        ChatParseBean parseBean = new ChatParseBean(ackByte, messagePost);
        parseBean.msgParse();
    }

    /**
     * robot notice message
     */
    private MsgExtEntity robotMsgDeal(Connect.MSMessage message) throws Exception {
        MsgExtEntity entity = null;
        switch (message.getCategory()) {
            case 1://text message
                Connect.TextMessage textMessage = Connect.TextMessage.parseFrom(message.getBody().toByteArray());
                entity = RobotChat.getInstance().txtMsg(textMessage.getContent());
                break;
            case 2://voice message
                Connect.VoiceMessage voiceMessage = Connect.VoiceMessage.parseFrom(message.getBody().toByteArray());
                entity = RobotChat.getInstance().voiceMsg(voiceMessage.getUrl(), voiceMessage.getTimeLength());
                break;
            case 3://picture message
                Connect.PhotoMessage photoMessage = Connect.PhotoMessage.parseFrom(message.getBody().toByteArray());
                entity = RobotChat.getInstance().photoMsg(photoMessage.getThum(), photoMessage.getUrl(), photoMessage.getSize(),
                        photoMessage.getImageWidth(), photoMessage.getImageHeight());
                break;
            case 15://translation message
                Connect.SystemTransferPackage transferPackage = Connect.SystemTransferPackage.parseFrom(message.getBody().toByteArray());
                entity = RobotChat.getInstance().transferMsg(0, transferPackage.getTxid(), transferPackage.getAmount(), transferPackage.getTips());
                break;
            case 16://system red packet message
                Connect.SystemRedPackage redPackage = Connect.SystemRedPackage.parseFrom(message.getBody().toByteArray());
                entity = RobotChat.getInstance().luckPacketMsg(0, redPackage.getHashId(), redPackage.getTips(), redPackage.getAmount());
                break;
            case 101://group review
                Connect.Reviewed reviewed = Connect.Reviewed.parseFrom(message.getBody().toByteArray());
                entity = RobotChat.getInstance().groupReviewMsg(reviewed);

                String msgid = entity.getMessage_id();
                String groupApplyKey = reviewed.getIdentifier() + reviewed.getUserInfo().getPubKey();
                ApplyGroupBean applyGroupBean = ParamManager.getInstance().loadGroupApply(groupApplyKey);
                if (applyGroupBean == null) {//new apply
                    ParamManager.getInstance().updateGroupApply(groupApplyKey, reviewed.getTips(), reviewed.getSource(), 0, msgid);
                } else {//repeat apply, remove the last
                    MessageHelper.getInstance().deleteMsgByid(applyGroupBean.getMsgid());
                    ParamManager.getInstance().updateGroupApply(groupApplyKey, reviewed.getTips(), reviewed.getSource(), -1, msgid);
                }
                break;
            case 102://announce message
                Connect.Announcement announcement = Connect.Announcement.parseFrom(message.getBody().toByteArray());
                entity = RobotChat.getInstance().systemAdNotice(announcement);
                break;
            case 103://red packet has get notice
                Connect.SystemRedpackgeNotice packgeNotice = Connect.SystemRedpackgeNotice.parseFrom(message.getBody().toByteArray());
                entity = RobotChat.getInstance().outerPacketGetNoticfe(packgeNotice);
                break;
            case 104://apply group agree/refuse
                Connect.ReviewedResponse reviewedResponse = Connect.ReviewedResponse.parseFrom(message.getBody().toByteArray());
                String notice = "";
                if (reviewedResponse.getSuccess()) {
                    notice = BaseApplication.getInstance().getBaseContext().getString(R.string.Link_You_apply_to_join_has_passed, reviewedResponse.getName());
                } else {
                    notice = BaseApplication.getInstance().getBaseContext().getString(R.string.Link_You_apply_to_join_rejected, reviewedResponse.getName());
                }
                entity = RobotChat.getInstance().noticeMsg(notice);
                ParamManager.getInstance().updateGroupApplyMember(reviewedResponse.getIdentifier(), reviewedResponse.getSuccess() ? 1 : 2);
                break;
            case 105://Registered mobile phone in the new account binding The original account automatically lift and notice
                Connect.UpdateMobileBind mobileBind = Connect.UpdateMobileBind.parseFrom(message.getBody().toByteArray());
                String content = BaseApplication.getInstance().getBaseContext().getString(R.string.Chat_Your_Connect_ID_will_no_longer_be_linked_with_mobile_number,
                        mobileBind.getUsername());
                entity = RobotChat.getInstance().txtMsg(content);

                UserBean userBean = SharedPreferenceUtil.getInstance().getUser();
                userBean.setPhone("");
                SharedPreferenceUtil.getInstance().putUser(userBean);
                break;
            case 106://Groups will be dissolved
                Connect.RemoveGroup removeGroup = Connect.RemoveGroup.parseFrom(message.getBody().toByteArray());
                entity = RobotChat.getInstance().noticeMsg(BaseApplication.getInstance().getBaseContext().getString(R.string.Chat_Group_has_been_disbanded,
                        removeGroup.getName()));

                ContactHelper.getInstance().removeGroupInfos(removeGroup.getGroupId());
                RecExtBean.getInstance().sendEvent(RecExtBean.ExtType.GROUP_REMOVE, removeGroup.getGroupId());
                break;
            case 200://External address transfer to Connect account, system notification
                Connect.AddressNotify addressNotify = Connect.AddressNotify.parseFrom(message.getBody().toByteArray());
                break;
        }

        entity.setSend_status(1);
        String mypublickey = MemoryDataManager.getInstance().getPubKey();
        entity.setMessage_from(BaseApplication.getInstance().getString(R.string.app_name));
        entity.setMessage_to(mypublickey);
        return entity;
    }

    /**
     * not friend
     *
     * @param pubkey
     */
    private void insertNotFriendNotice(String pubkey) {
        ContactEntity friendEntity = ContactHelper.getInstance().loadFriendEntity(pubkey);
        if (friendEntity != null) {
            FriendChat friendChat = new FriendChat(friendEntity);
            MsgExtEntity msgExtEntity = friendChat.strangerNotice();

            MessageHelper.getInstance().insertMsgExtEntity(msgExtEntity);
            RecExtBean.getInstance().sendEvent(RecExtBean.ExtType.MESSAGE_RECEIVE, pubkey, msgExtEntity);
        }
    }

    /**
     * black friend
     *
     * @param pubkey
     */
    private void blackFriendNotice(String pubkey) {
        ContactEntity friendEntity = ContactHelper.getInstance().loadFriendEntity(pubkey);
        if (friendEntity != null) {
            FriendChat friendChat = new FriendChat(friendEntity);
            MsgExtEntity msgExtEntity = friendChat.blackFriendNotice();

            MessageHelper.getInstance().insertMsgExtEntity(msgExtEntity);
            RecExtBean.getInstance().sendEvent(RecExtBean.ExtType.MESSAGE_RECEIVE, pubkey, msgExtEntity);
        }
    }

    /**
     * not in group
     *
     * @param groupkey
     */
    private void notGroupMemberNotice(String groupkey) {
        GroupEntity groupEntity = ContactHelper.getInstance().loadGroupEntity(groupkey);
        if (groupEntity != null) {
            GroupChat groupChat = new GroupChat(groupEntity);
            MsgExtEntity msgExtEntity = groupChat.notMemberNotice();

            MessageHelper.getInstance().insertMsgExtEntity(msgExtEntity);
            RecExtBean.getInstance().sendEvent(RecExtBean.ExtType.MESSAGE_RECEIVE, groupkey, msgExtEntity);
        }
    }

    /**
     * salt not match
     * @param publickey
     * @param cookie
     */
    private void saltNotMatch(String msgid, String publickey, Connect.ChatCookie cookie) throws Exception {
        ContactEntity friendEntity= ContactHelper.getInstance().loadFriendEntity(publickey);
        if (friendEntity == null) {
            return;
        }

        String friendKey = friendEntity.getPub_key();
        String cookiePubKey = "COOKIE:" + friendKey;
        Connect.ChatCookieData cookieData = cookie.getData();

        //local cookie
        List<ParamEntity> paramEntities = ParamHelper.getInstance().likeParamEntities(cookiePubKey);
        if (paramEntities == null || paramEntities.size() < 5) {

        } else {
            int cutSize = paramEntities.size() - 4;
            for (int i = 0; i < cutSize; i++) {
                ParamEntity indexEntity = paramEntities.get(i);
                ParamHelper.getInstance().deleteParamEntity(indexEntity.getKey());
            }
        }

        String friendSaltHex = StringUtil.bytesToHexString(cookieData.getSalt().toByteArray());
        ParamEntity paramEntity = ParamHelper.getInstance().likeParamEntity(friendSaltHex);
        if (paramEntity == null) {
            ParamEntity indexEntity = new ParamEntity();

            UserCookie userCookie = new UserCookie();
            userCookie.setPubKey(cookieData.getChatPubKey());
            userCookie.setSalt(cookieData.getSalt().toByteArray());
            userCookie.setExpiredTime(cookieData.getExpired());

            Session.getInstance().setUserCookie(friendEntity.getPub_key(), userCookie);

            indexEntity.setKey(cookiePubKey + friendSaltHex);
            indexEntity.setValue(new Gson().toJson(userCookie));
            ParamHelper.getInstance().insertOrReplaceParamEntity(indexEntity);

            RecExtBean.getInstance().sendEvent(RecExtBean.ExtType.UNARRIVE_UPDATE, friendEntity.getPub_key());
        }

        MessageEntity messageEntity = MessageHelper.getInstance().loadMsgByMsgid(msgid);
        if (messageEntity != null) {
            FriendChat friendChat = new FriendChat(friendEntity);
            friendChat.setEncryType(FriendChat.EncryType.NORMAL);
            MsgExtEntity msgExtEntity = friendChat.loadEntityByMsgid(msgid);
            friendChat.sendPushMsg(msgExtEntity);
        }
    }

    private void halfRandom(String msgid, String address) throws Exception {
        ContactEntity friendEntity = ContactHelper.getInstance().loadFriendEntity(address);
        if (friendEntity == null) {
            return;
        }

        RecExtBean.getInstance().sendEvent(RecExtBean.ExtType.UNARRIVE_HALF, friendEntity.getPub_key());

        MessageEntity messageEntity = MessageHelper.getInstance().loadMsgByMsgid(msgid);
        if (messageEntity != null) {
            FriendChat friendChat = new FriendChat(friendEntity);
            friendChat.setEncryType(FriendChat.EncryType.HALF);
            MsgExtEntity msgExtEntity = friendChat.loadEntityByMsgid(msgid);
            friendChat.sendPushMsg(msgExtEntity);
        }
    }

    private void reloadUserCookie(String msgid, String address) throws Exception {
        FailMsgsManager.getInstance().insertFailMsg(address, msgid);

        CommandBean commandBean = new CommandBean((byte) 0x00, null);
        commandBean.reloadUserCookie();
    }
}
