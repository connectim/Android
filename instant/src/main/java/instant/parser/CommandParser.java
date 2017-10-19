package instant.parser;

import android.text.TextUtils;

import com.google.gson.Gson;
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
import instant.utils.StringUtil;
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

    private String Tag = "_CommandParser";

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
                case 0x15://Not interested in
                    receiverInterested(command.getDetail(), msgid, command.getErrNo());
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
        //ConnectState.getInstance().sendEventDelay(ConnectState.ConnectType.START);

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
                LogManager.getLogger().d(Tag, "msgList:" + msgList.size());

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
                            case 0x15://Not interested in
                                receiverInterested(transferDataByte);
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
            CommandLocalReceiver.receiver.pullContacts(relationship);
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
            if ((int) objs[1] == 1 || (int) objs[1] == 3) {
                receiptUserSendAckMsg(msgid, false, objs[1]);
            } else {
                receiptUserSendAckMsg(msgid, true);
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
                return;
            case 4:
                receiptUserSendAckMsg(objs[0], false, objs[1]);
                return;
        }

        requestFriendsByVersion();
        Connect.ReceiveAcceptFriendRequest friendRequest = Connect.ReceiveAcceptFriendRequest.parseFrom(buffer);
        CommandLocalReceiver.receiver.acceptFriendRequest(friendRequest);

        receiptUserSendAckMsg(objs[0], true);
    }

    /**
     * Remove buddy
     *
     * @param buffer
     * @throws Exception
     */
    private void receiverAcceptDelFriend(ByteString buffer, Object... objs) throws Exception {
        if ((int) objs[1] > 0) {//Delete failed When the two sides have been lifted friends relationship, there is also the local contact person
            receiptUserSendAckMsg(objs[0], false, objs[1]);
        } else {
            receiptUserSendAckMsg(objs[0], true);

            Connect.SyncRelationship relationship = Connect.SyncRelationship.parseFrom(buffer);
            CommandLocalReceiver.receiver.acceptDelFriend(relationship);
        }
    }

    /**
     * Modify the friends remark and common friends
     *
     * @param buffer
     * @throws Exception
     */
    private void receiverSetUserInfo(ByteString buffer, Object... objs) throws Exception {
        receiptUserSendAckMsg(objs[0], true);
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
     * Not interested in
     *
     * @param buffer
     * @throws Exception
     */
    private void receiverInterested(ByteString buffer, Object... objs) throws Exception {
        if ((int) objs[1] > 0) {//The operation failure Repeated friend recommended
            receiptUserSendAckMsg(objs[0], false, objs[1]);
        } else {
            receiptUserSendAckMsg(objs[0], true);
        }
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
                    //FailMsgsManager.getInstance().sendExpireMsg();
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
            String gsonCookie = SharedUtil.getInstance().getStringValue(SharedUtil.COOKIE_CHATUSER);
            if(!TextUtils.isEmpty(gsonCookie)){
                userCookie = new Gson().fromJson(gsonCookie,UserCookie.class);
            }
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
        Connect.ChatCookieData cookieData = chatCookie.getData();
        if (TextUtils.isEmpty(cookieData.getChatPubKey())) {//friend use old protocal
            return;
        }

        byte[] friendSalt = cookieData.getSalt().toByteArray();
        Map<String, Object> failMap = FailMsgsManager.getInstance().getFailMap(msgid);
        if (failMap == null) {
            return;
        }

        String friendPublickey = (String) failMap.get(FailMsgsManager.EXT);
        if (!TextUtils.isEmpty(friendPublickey)) {
            UserCookie friendCookie = new UserCookie();
            friendCookie.setPubKey(cookieData.getChatPubKey());
            friendCookie.setSalt(friendSalt);
            friendCookie.setExpiredTime(cookieData.getExpired());
            Session.getInstance().setUserCookie(friendPublickey, friendCookie);

            SharedUtil.getInstance().insertFriendCookie(friendPublickey, friendCookie);
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
                setExpired(expiredTime).build();

        String signInfo = SupportKeyUril.signHash(priKey, chatInfo.toByteArray());
        Connect.ChatCookie cookie = Connect.ChatCookie.newBuilder().
                setSign(signInfo).
                setData(chatInfo).build();

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
