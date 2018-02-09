package instant.parser;

import android.text.TextUtils;

import com.google.protobuf.ByteString;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;

import instant.bean.Session;
import instant.bean.SocketACK;
import instant.bean.UserCookie;
import instant.parser.localreceiver.CommandLocalReceiver;
import instant.sender.SenderManager;
import instant.ui.InstantSdk;
import instant.utils.SharedUtil;
import instant.utils.TimeUtil;
import instant.utils.cryption.DecryptionUtil;
import instant.utils.cryption.EncryptionUtil;
import instant.utils.cryption.SupportKeyUril;
import instant.utils.manager.FailMsgsManager;
import protos.Connect;

/**
 * Created by gtq on 2016/12/14.
 */
public abstract class InterParse {

    private String TAG = "InterParse";

    protected byte ackByte;
    protected ByteBuffer byteBuffer;

    public InterParse() {
    }

    public InterParse(byte ackByte, ByteBuffer byteBuffer) {
        this.ackByte = ackByte;
        this.byteBuffer = byteBuffer;
    }

    public abstract void msgParse() throws Exception;

    /**
     * IMTransferData To StructData
     *
     * @param buffer
     * @return
     * @throws Exception
     */
    protected synchronized Connect.StructData imTransferToStructData(ByteBuffer buffer) throws Exception {
        Connect.IMTransferData imTransferData = Connect.IMTransferData.parseFrom(buffer.array());
        byte[] bytes = DecryptionUtil.decodeAESGCM(EncryptionUtil.ExtendedECDH.NONE,
                Session.getInstance().getChatCookie().getSalts(), imTransferData.getCipherData());
        return Connect.StructData.parseFrom(bytes);
    }

    /**
     * IMTransferData To Command
     *
     * @param buffer
     * @return
     * @throws Exception
     */
    protected synchronized Connect.Command imTransferToCommand(ByteBuffer buffer) throws Exception {
        Connect.StructData structData = imTransferToStructData(buffer);
        return Connect.Command.parseFrom(structData.getPlainData());
    }

    protected void backAck(SocketACK socketack, int type, String msgid) {
        Connect.Ack ack = Connect.Ack.newBuilder()
                .setType(type)
                .setMsgId(msgid)
                .build();

        UserCookie userCookie = Session.getInstance().getConnectCookie();
        Connect.GcmData gcmData = EncryptionUtil.encodeAESGCMStructData(EncryptionUtil.ExtendedECDH.NONE,
                Session.getInstance().getChatCookie().getSalts(), ack.toByteString());

        String myPrivateKey = userCookie.getPrivateKey();
        String uid = userCookie.getUid();
        String token = userCookie.getToken();
        String signHash = SupportKeyUril.signHash(myPrivateKey, gcmData.toByteArray());
        Connect.IMTransferData backAck = Connect.IMTransferData.newBuilder()
                .setCipherData(gcmData)
                .setUid(uid)
                .setToken(token)
                .setSign(signHash).build();

        SenderManager.getInstance().sendToMsg(socketack, backAck.toByteString());
    }

    protected void backOffLineAcks(List socketACKs) {
        String messageId = TimeUtil.timestampToMsgid();
        Connect.AckBatch ackBatch = Connect.AckBatch.newBuilder()
                .setMsgId(messageId)
                .addAllAcks(socketACKs)
                .build();

        UserCookie userCookie = Session.getInstance().getConnectCookie();
        Connect.GcmData gcmData = EncryptionUtil.encodeAESGCMStructData(EncryptionUtil.ExtendedECDH.NONE,
                Session.getInstance().getChatCookie().getSalts(), ackBatch.toByteString());

        String myPrivateKey = userCookie.getPrivateKey();
        String uid = userCookie.getUid();
        String token = userCookie.getToken();
        String signHash = SupportKeyUril.signHash(myPrivateKey, gcmData.toByteArray());
        Connect.IMTransferData backAck = Connect.IMTransferData.newBuilder()
                .setCipherData(gcmData)
                .setToken(token)
                .setSign(signHash)
                .setUid(uid)
                .build();

        SenderManager.getInstance().sendToMsg(SocketACK.ACK_BACK_OFFLINEBATCH, backAck.toByteString());
    }

    /**
     * Send the receipt
     *
     * @param msgid
     */
    protected void sendBackAck(String msgid) {
        backOnLineAck(5, msgid);
    }


    /**
     * Online receipt
     *
     * @param type
     * @return
     */
    protected void backOnLineAck(int type, String msgid) {
        backAck(SocketACK.ACK_BACK_ONLINE, type, msgid);
    }

    /**
     * Send offline receipt
     *
     * @param type
     * @return
     */
    protected void backOffLineAck(int type, String msgid) {
        backAck(SocketACK.ACK_BACK_OFFLINE, type, msgid);
    }

    /**
     * Update user sends command status
     * msgid sendstate
     */
    protected void receiptUserSendAckMsg(Object... objects) {
        String msgid = (String) objects[0];

        Object reqObj = null;
        Map<String, Object> failMap = FailMsgsManager.getInstance().getFailMap(msgid);
        if (failMap != null) {
            reqObj = failMap.get("EXT");
        }

        Object serverObj = null;
        if (objects.length == 3) {
            serverObj = objects[2];
        }
        CommandLocalReceiver.receiver.commandReceipt((Boolean) objects[1], reqObj, serverObj);
        FailMsgsManager.getInstance().removeFailMap(msgid);
    }

    /**
     * Update message status
     *
     * @param msgid
     */
    protected void receiptMsg(String msgid, int state) {
        String pubkey = "";
        Map failMap = FailMsgsManager.getInstance().getFailMap(msgid);
        if (failMap != null) {
            pubkey = (String) failMap.get("PUBKEY");
        }

        CommandLocalReceiver.receiver.updateMsgSendState(pubkey, msgid, state);
        FailMsgsManager.getInstance().removeFailMap(msgid);
    }

    protected void commandToIMTransfer(String msgid, SocketACK ack, ByteString byteString) {
        Connect.Command command = Connect.Command.newBuilder().setMsgId(msgid).
                setDetail(byteString)
                .build();

        SenderManager.getInstance().sendAckMsg(ack, null, msgid, command.toByteString());
    }

    /**
     * sycn contact
     *
     * @return
     */
    protected void requestFriendsByVersion() {
        String version = SharedUtil.getInstance().getStringValue(SharedUtil.CONTACTS_VERSION);
        if (TextUtils.isEmpty(version)) {
            version = "0";
        }
        if (InstantSdk.getContactsCount() == 0) {
            version = "0";
        }

        Connect.SyncRelationship syncRelationship = Connect.SyncRelationship.newBuilder()
                .setVersion(version).build();
        commandToIMTransfer(TimeUtil.timestampToMsgid(), SocketACK.CONTACT_SYNC, syncRelationship.toByteString());
    }
}
