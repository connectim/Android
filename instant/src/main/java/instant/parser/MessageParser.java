package instant.parser;

import com.google.protobuf.ByteString;

import java.nio.ByteBuffer;
import java.util.Map;

import instant.parser.localreceiver.RobotLocalReceiver;
import instant.parser.localreceiver.UnreachableLocalReceiver;
import instant.utils.log.LogManager;
import instant.utils.manager.FailMsgsManager;
import protos.Connect;

/**
 * message parse
 * Created by gtq on 2016/12/14.
 */
public class MessageParser extends InterParse {

    private static String TAG = "_MessageParser";

    /**
     * Parsing the source 0:offline message 1:online message
     */
    private int ext = 1;

    public MessageParser(byte ackByte, ByteBuffer byteBuffer) {
        super(ackByte, byteBuffer);
        ext = 1;

        try {
            Connect.StructData structData = imTransferToStructData(byteBuffer);
            this.byteBuffer = ByteBuffer.wrap(structData.getPlainData().toByteArray());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public MessageParser(byte ackByte, ByteBuffer byteBuffer, int ext) {
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
            case 0x06://subscribe messgae
                //subscribePull();
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

        switch (msMessage.getCategory()) {
            case 1://text message
                Connect.TextMessage textMessage = Connect.TextMessage.parseFrom(msMessage.getBody().toByteArray());
                RobotLocalReceiver.localReceiver.textMessage(textMessage);
                break;
            case 2://voice message
                Connect.VoiceMessage voiceMessage = Connect.VoiceMessage.parseFrom(msMessage.getBody().toByteArray());
                RobotLocalReceiver.localReceiver.voiceMessage(voiceMessage);
                break;
            case 3://picture message
                Connect.PhotoMessage photoMessage = Connect.PhotoMessage.parseFrom(msMessage.getBody().toByteArray());
                RobotLocalReceiver.localReceiver.photoMessage(photoMessage);
                break;
            case 15://translation message
                Connect.SystemTransferPackage transferPackage = Connect.SystemTransferPackage.parseFrom(msMessage.getBody().toByteArray());
                RobotLocalReceiver.localReceiver.translationMessage(transferPackage);
                break;
            case 16://system red packet message
                Connect.SystemRedPackage redPackage = Connect.SystemRedPackage.parseFrom(msMessage.getBody().toByteArray());
                RobotLocalReceiver.localReceiver.systemRedPackageMessage(redPackage);
                break;
            case 20://访客申请
                Connect.VisitorNotify  unRegisterNotify = Connect.VisitorNotify .parseFrom(msMessage.getBody().toByteArray());
                RobotLocalReceiver.localReceiver.warehouseMessage(20, unRegisterNotify.toByteArray());
                break;
            case 21://仓库异常
                Connect.UnRegisterNotify  visitorNotify = Connect.UnRegisterNotify .parseFrom(msMessage.getBody().toByteArray());
                RobotLocalReceiver.localReceiver.warehouseMessage(21, visitorNotify.toByteArray());
                break;
            case 102://announce message
                Connect.Announcement announcement = Connect.Announcement.parseFrom(msMessage.getBody().toByteArray());
                RobotLocalReceiver.localReceiver.announcementMessage(announcement);
                break;
            case 103://red packet has get notice
                Connect.SystemRedpackgeNotice packgeNotice = Connect.SystemRedpackgeNotice.parseFrom(msMessage.getBody().toByteArray());
                RobotLocalReceiver.localReceiver.systemRedpackgeNoticeMessage(packgeNotice);
                break;
            case 104://apply group agree/refuse
                Connect.ReviewedResponse reviewedResponse = Connect.ReviewedResponse.parseFrom(msMessage.getBody().toByteArray());
                RobotLocalReceiver.localReceiver.reviewedResponseMessage(reviewedResponse);
                break;
            case 105://Registered mobile phone in the new account binding The original account automatically lift and notice
                Connect.UpdateMobileBind mobileBind = Connect.UpdateMobileBind.parseFrom(msMessage.getBody().toByteArray());
                RobotLocalReceiver.localReceiver.updateMobileBindMessage(mobileBind);
                break;
            case 106://Groups will be dissolved
                Connect.RemoveGroup removeGroup = Connect.RemoveGroup.parseFrom(msMessage.getBody().toByteArray());
                RobotLocalReceiver.localReceiver.removeGroupMessage(removeGroup);
                break;
            case 200://External address transfer to Connect account, system notification
                Connect.AddressNotify addressNotify = Connect.AddressNotify.parseFrom(msMessage.getBody().toByteArray());
                RobotLocalReceiver.localReceiver.addressNotifyMessage(addressNotify);
                break;
        }
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

        LogManager.getLogger().d(TAG, "unavailableMsg: " + rejectMessage.getStatus());

        String msgid = rejectMessage.getMsgId();
        String rejectUid = rejectMessage.getUid();
        ByteString data = rejectMessage.getData();
        switch (rejectMessage.getStatus()) {
            case 1://The user does not exist
            case 2://Is not a friend relationship
                Map<String, Object> friendFail = FailMsgsManager.getInstance().getFailMap(msgid);
                if (friendFail != null) {
                    String friendkey= (String) friendFail.get("PUBKEY");
                    FailMsgsManager.getInstance().receiveFailMsgs(friendkey);

                    UnreachableLocalReceiver.localReceiver.notFriendNotice(friendkey);
                }
                break;
            case 3://in blakc list
                UnreachableLocalReceiver.localReceiver.blackFriendNotice(rejectMessage.getUid());
                break;
            case 4://Not in the group
                Map<String, Object> groupFail = FailMsgsManager.getInstance().getFailMap(msgid);
                if (groupFail != null) {
                    String groupkey= (String) groupFail.get("PUBKEY");
                    UnreachableLocalReceiver.localReceiver.notGroupMemberNotice(groupkey);
                }
                break;
            case 5://Chat information is empty
            case 6://Error accessing chat information
            case 7://Chat messages do not match
                Connect.ChatCookie chatCookie = Connect.ChatCookie.parseFrom(rejectMessage.getData());
                UnreachableLocalReceiver.localReceiver.saltNotMatch(msgid, rejectUid, chatCookie);
                break;
            case 8://upload cookie salt expire
                UnreachableLocalReceiver.localReceiver.friendCookieExpired(rejectUid);
                break;
            case 9://upload cookie salt not match
                reloadUserCookie(msgid, rejectUid);
                break;
            case 10://PUBKEY_NOT_MATCH
                Connect.PubKey pubKey = Connect.PubKey.parseFrom(data);
                UnreachableLocalReceiver.localReceiver.publicKeyNotMatch(msgid, rejectUid,pubKey);
                break;
        }
        receiptMsg(msgid, 2);
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

        TransactionParser parseBean = new TransactionParser(noticeMessage);
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

        ChatParser parseBean = new ChatParser(ackByte, messagePost);
        parseBean.msgParse();
    }

    private void reloadUserCookie(String msgid, String uid) throws Exception {
        FailMsgsManager.getInstance().insertFailMsg(uid, msgid);

        CommandParser commandBean = new CommandParser((byte) 0x00, null);
        //commandBean.reloadUserCookie();
    }
}
