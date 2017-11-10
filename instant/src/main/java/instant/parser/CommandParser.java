package instant.parser;

import android.text.TextUtils;

import com.google.protobuf.ByteString;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import connect.wallet.jni.AllNativeMethod;
import instant.bean.Session;
import instant.bean.UserCookie;
import instant.bean.UserOrderBean;
import instant.parser.localreceiver.CommandLocalReceiver;
import instant.parser.localreceiver.ConnectLocalReceiver;
import instant.utils.SharedUtil;
import instant.utils.TimeUtil;
import instant.utils.cryption.SupportKeyUril;
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
                    receiverAddFriendRequest(command.getDetail(), msgid, command.getErrNo());
                    break;
                case 0x09://Accept agreed to be a friend request
                    receiverAcceptAddFriend(command.getDetail(), msgid, command.getErrNo());
                    break;
                case 0x0a://delete friend
                    receiverAcceptDelFriend(command.getDetail(), msgid, command.getErrNo());
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
                    friencChatCookie(command.getDetail(), msgid);
                    break;
                case 0x19:
                    reloadUserCookie();
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
            Connect.OfflineMsgs offlineMsgs = Connect.OfflineMsgs.parseFrom(unGzip);
            List<Connect.OfflineMsg> msgList = offlineMsgs.getOfflineMsgsList();

            for (Connect.OfflineMsg offlineMsg : msgList) {
                LogManager.getLogger().d(TAG, "msgList:" + msgList.size());

                Connect.ProducerMsgDetail msgDetail = offlineMsg.getBody();
                int extension = msgDetail.getExt();
                backOffLineAck(msgDetail.getType(), offlineMsg.getMsgId());

                switch ((byte) msgDetail.getType()) {
                    case 0x04://Offline command processing
                        Connect.IMTransferData imTransferData = Connect.IMTransferData.parseFrom(msgDetail.getData());
                        ByteString transferDataByte = imTransferData.getCipherData().toByteString();
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
                                receiverAddFriendRequest(transferDataByte);
                                break;
                            case 0x09://Accept agreed to be a friend request
                                receiverAcceptAddFriend(transferDataByte);
                                break;
                            case 0x0a://delete friend
                                receiverAcceptDelFriend(transferDataByte);
                                break;
                            case 0x0b://Modify the friends remark and common friends
                                receiverSetUserInfo(transferDataByte);
                                break;
                            case 0x0d://modify group information
                                updateGroupInfo(transferDataByte);
                                break;
                            case 0x11://outer translate
                                handlerOuterTransfer(transferDataByte);
                                break;
                            case 0x12://outer red packet
                                handlerOuterRedPacket(transferDataByte);
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
        }

        if (offComplete) {
            ConnectLocalReceiver.receiver.connectSuccess();

            String pubKey = Session.getInstance().getUserCookie(Session.CONNECT_USER).getPubKey();
            Session.getInstance().setUpFailTime(pubKey, 0);
            uploadRandomCookie();
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
            Connect.SyncUserRelationship relationship = Connect.SyncUserRelationship.parseFrom(buffer);
            version = relationship.getRelationShip().getVersion();
            CommandLocalReceiver.receiver.loadAllContacts(relationship);
        } else {
            Connect.ChangeRecords changeRecords = Connect.ChangeRecords.parseFrom(buffer);
            version = changeRecords.getVersion();
            CommandLocalReceiver.receiver.contactChanges(changeRecords);
        }
        SharedUtil.getInstance().putValue(SharedUtil.CONTACTS_VERSION, version);
    }

    /**
     * Add Friend request
     *
     * @param buffer
     * @throws Exception
     */
    private void receiverAddFriendRequest(ByteString buffer, Object... objs) throws Exception {
        boolean isMySend = false;
        String msgid = null;

        if (objs.length == 2) {
            msgid = (String) objs[0];
            Map<String, Object> failMap = FailMsgsManager.getInstance().getFailMap(msgid);
            if (failMap != null) {
                isMySend = true;
            }
        }

        if (isMySend) {//youself send add Friend request
            switch ((int) objs[1]) {
                case 0:
                    receiptUserSendAckMsg(msgid, true);
                    break;
                default:
                    receiptUserSendAckMsg(msgid, false, objs[1]);
                    break;
            }
        } else {
            Connect.ReceiveFriendRequest friendRequest = Connect.ReceiveFriendRequest.parseFrom(buffer);
            CommandLocalReceiver.receiver.receiverFriendRequest(friendRequest);
        }
    }

    /**
     * Agree to add buddy request
     *
     * @param buffer
     * @throws Exception
     */
    private void receiverAcceptAddFriend(ByteString buffer, Object... objs) throws Exception {
        switch ((int) objs[1]) {
            case 1:
                receiptUserSendAckMsg(objs[0], false, objs[1]);
                break;
            case 4:
                receiptUserSendAckMsg(objs[0], false, objs[1]);
                break;
            default:
                Connect.FriendListChange listChange = Connect.FriendListChange.parseFrom(buffer);

                String version = listChange.getVersion();
                SharedUtil.getInstance().putValue(SharedUtil.CONTACTS_VERSION, version);

                CommandLocalReceiver.receiver.acceptFriendRequest(listChange);
                receiptUserSendAckMsg(objs[0], true);
                break;
        }
    }

    /**
     * Remove buddy
     *
     * @param buffer
     * @throws Exception
     */
    private void receiverAcceptDelFriend(ByteString buffer, Object... objs) throws Exception {
        switch ((int) objs[1]) {
            case 0:
                Connect.FriendListChange listChange = Connect.FriendListChange.parseFrom(buffer);

                String version = listChange.getVersion();
                SharedUtil.getInstance().putValue(SharedUtil.CONTACTS_VERSION, version);

                CommandLocalReceiver.receiver.acceptDelFriend(listChange);

                receiptUserSendAckMsg(objs[0], true);
                break;
            default://
                receiptUserSendAckMsg(objs[0], false, objs[1]);
                break;
        }
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
        String pubKey = Session.getInstance().getUserCookie(Session.CONNECT_USER).getPubKey();
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
                int failTime = Session.getInstance().getUpFailTime(pubKey);
                if (failTime <= 2) {
                    uploadRandomCookie();
                } else {
                    Session.getInstance().setUserCookie(pubKey, null);
                }
                Session.getInstance().setUpFailTime(pubKey, ++failTime);
                break;
            case 4://cookie is overdue ,user old protocal
                Session.getInstance().setUserCookie(pubKey, null);
                break;
        }
    }

    /**
     * Upload a local public key
     */
    private void uploadRandomCookie() {
        long curTime = TimeUtil.getCurrentTimeSecond();
        boolean needUpload = true;//If you want to generate a temporary session cookies
        String pubkey = Session.getInstance().getUserCookie(Session.CONNECT_USER).getPubKey();
        UserCookie userCookie = Session.getInstance().getUserCookie(pubkey);

        if (userCookie == null) {
            userCookie = SharedUtil.getInstance().loadLastChatUserCookie();
        }

        if (userCookie != null) {
            if (curTime < userCookie.getExpiredTime()) {
                needUpload = false;
            }
        }

        if (needUpload) {
            reloadUserCookie();
        }
    }

    /**
     * Good friend chat of cookies
     *
     * @param buffer
     * @throws Exception
     */
    private void friencChatCookie(ByteString buffer, String msgid) throws Exception {
        Connect.ChatCookie chatCookie = Connect.ChatCookie.parseFrom(buffer);
        if (!SupportKeyUril.verifySign(chatCookie.getCaPub(), chatCookie.getSign(), chatCookie.getData().toByteArray())) {
            throw new Exception(TAG + ":  Validation fails");
        }

        Connect.ChatCookieData chatCookieData = chatCookie.getData();
        if (TextUtils.isEmpty(chatCookieData.getChatPubKey())) {//friend use old protocal
            return;
        }

        byte[] friendSalt = chatCookieData.getSalt().toByteArray();
        Map<String, Object> failMap = FailMsgsManager.getInstance().getFailMap(msgid);
        if (failMap == null) {
            return;
        }

        String friendCaPublickey = chatCookie.getCaPub();
        UserCookie friendCookie = new UserCookie();
        String caPublicKey = chatCookieData.getChatPubKey();
        friendCookie.setPubKey(caPublicKey);
        friendCookie.setSalt(friendSalt);
        friendCookie.setExpiredTime(chatCookieData.getExpired());
        Session.getInstance().setUserCookie(friendCaPublickey, friendCookie);

        SharedUtil.getInstance().insertFriendCookie(friendCaPublickey, friendCookie);
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

    public void reloadUserCookie() {
        long curTime = TimeUtil.getCurrentTimeSecond();
        String pubkey = Session.getInstance().getUserCookie(Session.CONNECT_USER).getPubKey();

        boolean reGenerate = true;
        UserCookie userCookie = Session.getInstance().getUserCookie(pubkey);
        if (userCookie == null) {
            userCookie = SharedUtil.getInstance().loadLastChatUserCookie();
        }

        if (userCookie != null) {
            if (curTime < userCookie.getExpiredTime()) {
                reGenerate = false;
            }
        }

        String priKey = Session.getInstance().getUserCookie(Session.CONNECT_USER).getPriKey();
        String randomPriKey = null;
        String randomPubKey = null;
        byte[] randomSalt = null;
        long expiredTime = 0;

        if (reGenerate) {
            randomPriKey = AllNativeMethod.cdCreateNewPrivKey();
            randomPubKey = AllNativeMethod.cdGetPubKeyFromPrivKey(randomPriKey);
            randomSalt = AllNativeMethod.cdCreateSeed(16, 4).getBytes();
            expiredTime = TimeUtil.getCurrentTimeSecond() + 24 * 60 * 60;
        } else {
            randomPriKey = userCookie.getPriKey();
            randomPubKey = userCookie.getPubKey();
            randomSalt = userCookie.getSalt();
            expiredTime = TimeUtil.getCurrentTimeSecond() + 24 * 60 * 60;
        }

        Connect.ChatCookieData chatInfo = Connect.ChatCookieData.newBuilder().
                setChatPubKey(randomPubKey).
                setSalt(ByteString.copyFrom(randomSalt)).
                setExpired(expiredTime)
                .build();

        String caPublicKey = Session.getInstance().getUserCookie(Session.CONNECT_USER).getPubKey();
        String signInfo = SupportKeyUril.signHash(priKey, chatInfo.toByteArray());
        Connect.ChatCookie cookie = Connect.ChatCookie.newBuilder()
                .setCaPub(caPublicKey)
                .setSign(signInfo)
                .setData(chatInfo)
                .build();

        UserOrderBean userOrderBean = new UserOrderBean();
        userOrderBean.uploadRandomCookie(cookie);

        userCookie = new UserCookie();
        userCookie.setPriKey(randomPriKey);
        userCookie.setPubKey(randomPubKey);
        userCookie.setSalt(randomSalt);
        userCookie.setExpiredTime(expiredTime);
        Session.getInstance().setUserCookie(pubkey, userCookie);

        SharedUtil.getInstance().insertChatUserCookie(userCookie);
    }
}
