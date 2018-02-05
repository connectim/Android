package instant.parser;

import android.text.TextUtils;

import com.google.protobuf.ByteString;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import instant.bean.Session;
import instant.parser.localreceiver.CommandLocalReceiver;
import instant.parser.localreceiver.ConnectLocalReceiver;
import instant.utils.SharedUtil;
import instant.utils.StringUtil;
import instant.utils.log.LogManager;
import instant.utils.manager.FailMsgsManager;
import protos.Connect;

/**
 * order message
 * Created by pujin on 2017/4/18.
 */
public class CommandParser extends InterParse {

    private static String TAG = "_CommandParser";

    public CommandParser(byte ackByte, ByteBuffer byteBuffer) {
        super(ackByte, byteBuffer);
    }

    @Override
    public synchronized void msgParse() throws Exception {
        if (ackByte == 0x04) {
            receiveOffLineMsgs(byteBuffer);
        } else {
            Connect.Command command = imTransferToCommand(byteBuffer);
            String msgid = command.getMsgId();

            switch (ackByte) {
                case 0x01:
                case 0x03:
                case 0x05:
                case 0x06:
                case 0x0a:
                case 0x0b:
                case 0x0c:
                case 0x0e:
                case 0x10:
                case 0x15:
                case 0x16:
                case 0x17:
                case 0x1a:
                case 0x1b:
                    break;
                default:
                    backOnLineAck(4, msgid);
                    break;
            }

            switch (ackByte) {
                case 0x01://contact list
                    syncContacts(command.getDetail());
                    break;
                case 0x06://bind servicetoken
                    break;
                case 0x07://login out success
                    //HomeAction.sendTypeMsg(HomeAction.HomeType.EXIT);
                    break;
                case 0x08://receive add friend request
                    // receiverAddFriendRequest(command.getDetail(), command.getErrNo(),msgid);
                    break;
                case 0x09://Accept agreed to be a friend request
                    // receiverAcceptAddFriend(command.getDetail(), msgid, command.getErrNo());
                    break;
                case 0x0a://delete friend
                    // receiverAcceptDelFriend(command.getDetail(), msgid, command.getErrNo());
                    break;
                case 0x0b://Modify the friends remark and common friends
                    receiverSetUserInfo(command.getDetail(), msgid, command.getErrNo());
                    break;
                case 0x0c://conversation mute notify
                    conversationMute(command.getDetail());
                    break;
                case 0x0d://modify group information
                    updateGroupInfo(command.getDetail(), msgid, command.getErrNo());
                    break;
                case 0x11://outer translate
                    handlerOuterTransfer(command.getDetail(), msgid, command.getErrNo());
                    break;
                case 0x12://outer red packet
                    handlerOuterRedPacket(command.getDetail(), msgid, command.getErrNo());
                    break;
                case 0x17://upload cookie
                    chatCookieInfo(command.getErrNo());
                    break;
                case 0x18://get friend chatcookie
                    //friencChatCookie(command.getDetail(), msgid);
                    break;
                case 0x19:
                    //reloadUserCookie();
                    break;
                case 0x1a://burn reading setting
                    // burnReadingSetting(command.getDetail(), msgid);
                    break;
                case 0x1b://burn reading receipt
                    // burnReadingReceipt(command.getDetail(), msgid);
                    break;
            }
        }
    }

    /**
     * Batch processing offline messages
     *
     * @param buffer
     * @throws Exception
     */
    private void receiveOffLineMsgs(ByteBuffer buffer) throws Exception {
        Connect.StructData structData = imTransferToStructData(buffer);
        byte[] unGzip = unGZip(structData.getPlainData().toByteArray());
        //Whether offline news has been exhausted
        boolean offComplete = false;
        if (unGzip.length == 0 || unGzip.length < 20) {
            offComplete = true;
        } else {
            List<Connect.Ack> ackList = new ArrayList<>();
            try {
                Connect.OfflineMsgs offlineMsgs = Connect.OfflineMsgs.parseFrom(unGzip);
                List<Connect.OfflineMsg> msgList = offlineMsgs.getOfflineMsgsList();

                for (Connect.OfflineMsg offlineMsg : msgList) {

                    String messageId = offlineMsg.getMsgId();
                    Connect.ProducerMsgDetail msgDetail = offlineMsg.getBody();
                    int extension = msgDetail.getExt();

                    LogManager.getLogger().d(TAG, "messageId:" + messageId);
                    Connect.Ack ack = Connect.Ack.newBuilder()
                            .setType(msgDetail.getType())
                            .setMsgId(messageId)
                            .build();
                    ackList.add(ack);

                    switch ((byte) msgDetail.getType()) {
                        case 0x04://Offline command processing
                            Connect.Command command = Connect.Command.parseFrom(msgDetail.getData());
                            ByteString transferDataByte = command.getDetail();

                            int errorNumber = command.getErrNo();
                            switch (extension) {
                                case 0x01://contact list
                                    syncContacts(transferDataByte);
                                    break;
                                case 0x06://bind servicetoken
                                    break;
                                case 0x07://login out success
                                    //HomeAction.sendTypeMsg(HomeAction.HomeType.EXIT);
                                    break;
                                case 0x08://receive add friend request
                                    // receiverAddFriendRequest(transferDataByte, errorNumber);
                                    break;
                                case 0x09://Accept agreed to be a friend request
                                    // receiverAcceptAddFriend(transferDataByte, messageId, errorNumber);
                                    break;
                                case 0x0a://delete friend
                                    // receiverAcceptDelFriend(transferDataByte, messageId, errorNumber);
                                    break;
                                case 0x0b://Modify the friends remark and common friends
                                    receiverSetUserInfo(transferDataByte, messageId, errorNumber);
                                    break;
                                case 0x0d://modify group information
                                    updateGroupInfo(transferDataByte, messageId, errorNumber);
                                    break;
                                case 0x11://outer translate
                                    handlerOuterTransfer(transferDataByte, messageId, errorNumber);
                                    break;
                                case 0x12://outer red packet
                                    handlerOuterRedPacket(transferDataByte, messageId, errorNumber);
                                    break;
                            }
                            break;
                        case 0x05://Offline notification
                            InterParse interParse = new MessageParser((byte) extension, ByteBuffer.wrap(msgDetail.getData().toByteArray()), 0);
                            interParse.msgParse();
                            break;
                    }
                }

                offComplete = offlineMsgs.getCompleted();
            } catch (Exception e) {
                e.printStackTrace();
                offComplete = true;
            } finally {
                backOffLineAcks(ackList);
            }
        }

        if (offComplete) {
            ConnectLocalReceiver.receiver.connectSuccess();

            String pubKey = Session.getInstance().getConnectCookie().getUid();
            Session.getInstance().setUpFailTime(pubKey, 0);
        }
    }

    /**
     * GZip decompression
     *
     * @param data
     * @return
     */
    private byte[] unGZip(byte[] data) {
        byte[] b = null;
        try {
            ByteArrayInputStream bis = new ByteArrayInputStream(data);
            GZIPInputStream gzip = new GZIPInputStream(bis);
            byte[] buf = new byte[1024];
            int num = -1;
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            while ((num = gzip.read(buf, 0, buf.length)) != -1) {
                baos.write(buf, 0, num);
            }
            b = baos.toByteArray();
            baos.flush();
            baos.close();
            gzip.close();
            bis.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return b;
    }

    /**
     * Sync contacts list
     *
     * @param buffer
     * @throws Exception
     */
    private void syncContacts(ByteString buffer) throws Exception {
        String version = SharedUtil.getInstance().getStringValue(SharedUtil.CONTACTS_VERSION);
        if (TextUtils.isEmpty(version)) {
            Connect.SyncCompany relationship = Connect.SyncCompany.parseFrom(buffer);
            version = relationship.getWorkmatesVersion().getVersion();
            CommandLocalReceiver.receiver.loadAllContacts(relationship);
        } else {
            Connect.WorkmateChangeRecords changeRecords = Connect.WorkmateChangeRecords.parseFrom(buffer);
            version = changeRecords.getVersion();
            CommandLocalReceiver.receiver.contactChanges(changeRecords);
        }
        SharedUtil.getInstance().putValue(SharedUtil.CONTACTS_VERSION, version);
    }

    /**
     * Modify the friends remark and common friends
     *
     * @param buffer
     * @throws Exception
     */
    private void receiverSetUserInfo(ByteString buffer, Object... objs) throws Exception {
        boolean setState = false;
        if (objs.length <= 0) {
            setState = true;
        }

        switch ((int) objs[1]) {
            case 0:
                setState = true;
                break;
            default:
                break;
        }
        receiptUserSendAckMsg(objs[0], setState);
    }

    /**
     * Conversation mute
     *
     * @param buffer
     */
    private void conversationMute(ByteString buffer) throws Exception {
        Connect.ManageSession manageSession = Connect.ManageSession.parseFrom(buffer);
        CommandLocalReceiver.receiver.conversationMute(manageSession);
    }

    /**
     * Group of information change
     *
     * @param buffer
     */
    private void updateGroupInfo(ByteString buffer, Object... objs) throws Exception {
        Connect.GroupChange groupChange = Connect.GroupChange.parseFrom(buffer);
        CommandLocalReceiver.receiver.updateGroupChange(groupChange);
    }

    /**
     * Upload the cookie state
     *
     * @param errNum
     */
    public void chatCookieInfo(int errNum) {
        String pubKey = Session.getInstance().getConnectCookie().getUid();
        switch (errNum) {
            case 0://Save the generated temporary cookies
                Session.getInstance().setUpFailTime(pubKey, 0);

                try {
                    FailMsgsManager.getInstance().sendFailMsgs();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case 2:
            case 3:
            case 4://cookie is overdue ,user old protocal
                int failTime = Session.getInstance().getUpFailTime(pubKey);
                if (failTime <= 2) {
                    //uploadRandomCookie();
                } else {
                    ConnectLocalReceiver.receiver.exceptionConnect();
                }
                Session.getInstance().setUpFailTime(pubKey, ++failTime);
                break;
        }
    }

    /**
     * External transfer
     *
     * @param buffer
     * @throws Exception
     */
    private void handlerOuterTransfer(ByteString buffer, Object... objs) throws Exception {
        switch ((int) objs[1]) {
            case 0://Get the success
                break;
            case 1://There is no
                break;
            case 2://To receive your transfer
                break;
        }

        if ((int) objs[1] > 0) {//Get the failure
            receiptUserSendAckMsg(objs[0], false, objs[1]);
        } else {
            receiptUserSendAckMsg(objs[0], true);
        }
    }

    /**
     * Outside a red envelope
     *
     * @param buffer
     * @throws Exception
     */
    private void handlerOuterRedPacket(ByteString buffer, Object... objs) throws Exception {
        Connect.ExternalRedPackageInfo packageInfo = null;
        switch ((int) objs[1]) {
            case 0://Get the success
                packageInfo = Connect.ExternalRedPackageInfo.parseFrom(buffer);
                CommandLocalReceiver.receiver.handlerOuterRedPacket(packageInfo);
                break;
            case 1:
                break;
            case 2://It is to receive
                break;
            case 3://Red packets suspended
                break;
        }

        if ((int) objs[1] > 0) {
            receiptUserSendAckMsg(objs[0], false, objs[1]);
        } else {
            receiptUserSendAckMsg(objs[0], true);
        }
    }
}
