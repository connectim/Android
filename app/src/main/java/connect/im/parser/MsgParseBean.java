package connect.im.parser;

import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;

import connect.db.SharedPreferenceUtil;
import connect.db.green.DaoHelper.ContactHelper;
import connect.db.green.DaoHelper.MessageHelper;
import connect.db.green.DaoHelper.ParamHelper;
import connect.db.green.DaoHelper.ParamManager;
import connect.db.green.bean.ContactEntity;
import connect.db.green.bean.GroupEntity;
import connect.db.green.bean.MessageEntity;
import connect.db.green.bean.ParamEntity;
import connect.im.bean.MsgType;
import connect.im.bean.Session;
import connect.im.bean.UserCookie;
import connect.im.inter.InterParse;
import connect.im.model.FailMsgsManager;
import connect.ui.activity.R;
import connect.ui.activity.chat.bean.AdBean;
import connect.ui.activity.chat.bean.ApplyGroupBean;
import connect.ui.activity.chat.bean.CardExt1Bean;
import connect.ui.activity.chat.bean.GroupReviewBean;
import connect.ui.activity.chat.bean.MsgChatReceiver;
import connect.ui.activity.chat.bean.MsgDefinBean;
import connect.ui.activity.chat.bean.MsgEntity;
import connect.ui.activity.chat.bean.MsgSender;
import connect.ui.activity.chat.bean.RecExtBean;
import connect.ui.activity.chat.model.ChatMsgUtil;
import connect.ui.activity.chat.model.content.FriendChat;
import connect.ui.activity.chat.model.content.GroupChat;
import connect.ui.activity.chat.model.content.NormalChat;
import connect.ui.activity.chat.model.content.RobotChat;
import connect.ui.activity.login.bean.UserBean;
import connect.ui.base.BaseApplication;
import connect.utils.StringUtil;
import connect.utils.TimeUtil;
import connect.utils.cryption.SupportKeyUril;
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
    private int ext = 0;

    public MsgParseBean(byte ackByte, ByteBuffer byteBuffer) {
        super(ackByte, byteBuffer);
    }

    public MsgParseBean(byte ackByte, ByteBuffer byteBuffer, int ext) {
        super(ackByte, byteBuffer);
        this.ext = ext;

        try {
            Connect.StructData structData = imTransferToStructData(byteBuffer);
            this.byteBuffer = ByteBuffer.wrap(structData.getPlainData().toByteArray());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void msgParse() throws Exception {
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
    private void robotMsg()throws Exception{
        Connect.MSMessage msMessage = Connect.MSMessage.parseFrom(byteBuffer.array());
        backOnLineAck(5, msMessage.getMsgId());

        MsgEntity robotMsg = robotMsgDeal(msMessage);
        MessageHelper.getInstance().insertFromMsg(BaseApplication.getInstance().getString(R.string.app_name), robotMsg.getMsgDefinBean());

        MsgDefinBean definBean = robotMsg.getMsgDefinBean();
        MsgEntity roMsgEntity = RobotChat.getInstance().createBaseChat(MsgType.toMsgType(definBean.getType()));
        roMsgEntity.setMsgDefinBean(definBean);

        String robotname = BaseApplication.getInstance().getString(R.string.app_name);
        ChatMsgUtil.updateRoomInfo(robotname, 2, TimeUtil.getCurrentTimeInLong(), definBean);
        pushNoticeMsg(robotname, 2, ChatMsgUtil.showContentTxt(2, definBean));

        MsgChatReceiver receiver = new MsgChatReceiver(roMsgEntity);
        EventBus.getDefault().post(receiver);
    }

    /**
     * unavailable message
     */
    private void unavailableMsg()throws Exception{
        Connect.RejectMessage rejectMessage = Connect.RejectMessage.parseFrom(byteBuffer.array());
        backOnLineAck(5, rejectMessage.getMsgId());

        String msgid = rejectMessage.getMsgId();
        String recAddress = rejectMessage.getReceiverAddress();
        switch (rejectMessage.getStatus()) {
            case 1://The user does not exist
            case 2://Is not a friend relationship
                Map<String, Object> friendFail = FailMsgsManager.getInstance().getFailMap(msgid);
                if (friendFail != null) {
                    insertNotFriendNotice((String) friendFail.get("PUBKEY"));
                }
                break;
            case 3://in blakc list
                blackFriendNotice(rejectMessage.getReceiverAddress());
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
                saltNotMatch(msgid, recAddress, chatCookie);
                break;
            case 8://The other cookies expire, single side
                halfRandom(msgid, recAddress);
                break;
        }
        receiptMsg(msgid, 3);
    }

    /**
     * notice message
     */
    private void noticeMsg() throws Exception {
        Connect.NoticeMessage noticeMessage = Connect.NoticeMessage.parseFrom(byteBuffer.array());
        sendBackAck(noticeMessage.getMsgId());

        TransactionParseBean parseBean = new TransactionParseBean(noticeMessage);
        parseBean.msgParse();
    }

    /**
     * chat message
     * @throws Exception
     */
    private void chatMsg() throws Exception {
        Connect.MessagePost messagePost = Connect.MessagePost.parseFrom(byteBuffer.array());
        if (!SupportKeyUril.verifySign(messagePost.getSign(), messagePost.toByteArray())) {
            throw new Exception("Validation fails");
        }

        ChatParseBean parseBean = new ChatParseBean(ackByte, messagePost);
        parseBean.msgParse();
    }

    /**
     * robot notice message
     */
    private MsgEntity robotMsgDeal(Connect.MSMessage message) throws Exception {
        MsgEntity entity = null;
        switch (message.getCategory()) {
            case 1://text message
                Connect.TextMessage textMessage = Connect.TextMessage.parseFrom(message.getBody().toByteArray());
                entity = RobotChat.getInstance().txtMsg(textMessage.getContent());
                break;
            case 2://voice message
                Connect.Voice voice = Connect.Voice.parseFrom(message.getBody().toByteArray());
                entity = RobotChat.getInstance().voiceMsg(voice.getUrl(), (int) voice.getDuration(), "");
                break;
            case 3://picture message
                Connect.Image image = Connect.Image.parseFrom(message.getBody().toByteArray());
                entity = RobotChat.getInstance().photoMsg(image.getUrl(), "");
                entity.getMsgDefinBean().setImageOriginWidth(Float.parseFloat(image.getWidth()));
                entity.getMsgDefinBean().setImageOriginHeight(Float.parseFloat(image.getHeight()));
                break;
            case 15://translation message
                Connect.SystemTransferPackage transferPackage = Connect.SystemTransferPackage.parseFrom(message.getBody().toByteArray());
                entity = RobotChat.getInstance().transferMsg(transferPackage.getTxid(), transferPackage.getAmount(), transferPackage.getTips(), 0);
                break;
            case 16://system red packet message
                Connect.SystemRedPackage redPackage = Connect.SystemRedPackage.parseFrom(message.getBody().toByteArray());
                entity = RobotChat.getInstance().luckPacketMsg(redPackage.getHashId(), redPackage.getTips(), 0);
                break;
            case 101://group review
                Connect.Reviewed reviewed = Connect.Reviewed.parseFrom(message.getBody().toByteArray());

                Connect.UserInfo info = reviewed.getUserInfo();
                CardExt1Bean ext1Bean = new CardExt1Bean();
                ext1Bean.setAvatar(info.getAvatar());
                ext1Bean.setAddress(info.getAddress());
                ext1Bean.setPub_key(info.getPubKey());
                ext1Bean.setUsername(info.getUsername());

                GroupReviewBean reviewBean = new GroupReviewBean();
                reviewBean.setTips(reviewed.getTips());
                reviewBean.setVerificationCode(reviewed.getVerificationCode());
                reviewBean.setGroupKey(reviewed.getIdentifier());
                reviewBean.setSource(reviewed.getSource());
                reviewBean.setInvitor(reviewed.getUserInfo());
                reviewBean.setGroupName(reviewed.getName());

                entity = RobotChat.getInstance().groupReviewMsg(new Gson().toJson(reviewBean), new Gson().toJson(ext1Bean));
                String msgid = entity.getMsgDefinBean().getMessage_id();

                String groupApplyKey = reviewed.getIdentifier() + info.getPubKey();
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
                AdBean adBean = new AdBean();
                adBean.transSystemAd(announcement);
                entity = RobotChat.getInstance().systemAdNotice(adBean);
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
                RecExtBean.sendRecExtMsg(RecExtBean.ExtType.GROUP_REMOVE, removeGroup.getGroupId());
                break;
            case 200://External address transfer to Connect account, system notification
                Connect.AddressNotify addressNotify = Connect.AddressNotify.parseFrom(message.getBody().toByteArray());
                break;
        }


        entity.setSendstate(1);
        entity.setReadstate(0);
        if (entity.getMsgDefinBean().getSenderInfoExt() == null) {
            entity.getMsgDefinBean().setSenderInfoExt(new MsgSender(RobotChat.getInstance().roomKey(),
                    BaseApplication.getInstance().getString(R.string.app_name),
                    RobotChat.getInstance().address(), RobotChat.getInstance().headImg()));
        }
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
            NormalChat normalChat = new FriendChat(friendEntity);
            MsgEntity msgEntity = normalChat.strangerNotice();

            MsgChatReceiver.sendChatReceiver(pubkey, msgEntity);
            MessageHelper.getInstance().insertToMsg(msgEntity.getMsgDefinBean());
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
            NormalChat normalChat = new FriendChat(friendEntity);
            MsgEntity msgEntity = normalChat.blackFriendNotice();

            MsgChatReceiver.sendChatReceiver(pubkey, msgEntity);
            MessageHelper.getInstance().insertToMsg(msgEntity.getMsgDefinBean());
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
            NormalChat normalChat = new GroupChat(groupEntity);
            MsgEntity msgEntity = normalChat.notMemberNotice();

            MsgChatReceiver.sendChatReceiver(groupkey, msgEntity);
            MessageHelper.getInstance().insertToMsg(msgEntity.getMsgDefinBean());
        }
    }

    /**
     * salt not match
     * @param address
     * @param cookie
     */
    private void saltNotMatch(String msgid, String address, Connect.ChatCookie cookie) throws Exception {
        ContactEntity friendEntity= ContactHelper.getInstance().loadFriendEntity(address);
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

            RecExtBean.sendRecExtMsg(RecExtBean.ExtType.UNARRIVE_UPDATE, friendEntity.getPub_key());
        }

        MessageEntity messageEntity = MessageHelper.getInstance().loadMsgByMsgid(msgid);
        if (messageEntity != null) {
            FriendChat friendChat = new FriendChat(friendEntity);
            friendChat.setEncryType(FriendChat.EncryType.NORMAL);
            MsgEntity baseEntity = friendChat.loadEntityByMsgid(msgid);
            friendChat.sendPushMsg(baseEntity);
        }
    }


    private void halfRandom(String msgid, String address) throws Exception {
        ContactEntity friendEntity = ContactHelper.getInstance().loadFriendEntity(address);
        if (friendEntity == null) {
            return;
        }

        RecExtBean.sendRecExtMsg(RecExtBean.ExtType.UNARRIVE_HALF, friendEntity.getPub_key());

        MessageEntity messageEntity = MessageHelper.getInstance().loadMsgByMsgid(msgid);
        if (messageEntity != null) {
            FriendChat friendChat = new FriendChat(friendEntity);
            friendChat.setEncryType(FriendChat.EncryType.HALF);
            MsgEntity baseEntity = friendChat.loadEntityByMsgid(msgid);
            friendChat.sendPushMsg(baseEntity);
        }
    }
}
