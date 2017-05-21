package connect.im.inter;

import android.text.TextUtils;

import com.google.protobuf.ByteString;

import java.nio.ByteBuffer;
import java.util.Map;

import connect.db.MemoryDataManager;
import connect.db.SharedPreferenceUtil;
import connect.db.green.DaoHelper.ParamManager;
import connect.im.bean.Session;
import connect.im.bean.SocketACK;
import connect.im.model.ChatSendManager;
import connect.im.model.FailMsgsManager;
import connect.im.model.NotificationManager;
import connect.ui.activity.chat.model.ChatMsgUtil;
import connect.ui.activity.home.bean.MsgNoticeBean;
import connect.utils.TimeUtil;
import connect.utils.cryption.DecryptionUtil;
import connect.utils.cryption.EncryptionUtil;
import connect.utils.cryption.SupportKeyUril;
import protos.Connect;

/**
 *
 * Created by gtq on 2016/12/14.
 */
public abstract class InterParse {

    private String Tag = "InterParse";

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
    protected Connect.StructData imTransferToStructData(ByteBuffer buffer) throws Exception {
        Connect.IMTransferData imTransferData = Connect.IMTransferData.parseFrom(buffer.array());
        if (!SupportKeyUril.verifySign(imTransferData.getSign(), imTransferData.getCipherData().toByteArray())) {
            throw new Exception("Validation fails");
        }

        byte[] bytes = DecryptionUtil.decodeAESGCM(SupportKeyUril.EcdhExts.NONE,
                Session.getInstance().getUserCookie("TEMPCOOKIE").getSalt(), imTransferData.getCipherData());
        return Connect.StructData.parseFrom(bytes);
    }

    /**
     * IMTransferData To Command
     *
     * @param buffer
     * @return
     * @throws Exception
     */
    protected Connect.Command imTransferToCommand(ByteBuffer buffer) throws Exception {
        Connect.StructData structData = imTransferToStructData(buffer);
        return Connect.Command.parseFrom(structData.getPlainData());
    }

    protected void backAck(SocketACK socketack, int type, String msgid) {
        Connect.Ack ack = Connect.Ack.newBuilder().setType(type).setMsgId(msgid).build();
        String priKey = MemoryDataManager.getInstance().getPriKey();

        Connect.GcmData gcmData = EncryptionUtil.encodeAESGCMStructData(SupportKeyUril.EcdhExts.NONE,
                Session.getInstance().getUserCookie("TEMPCOOKIE").getSalt(), ack.toByteString());

        String signHash = SupportKeyUril.signHash(priKey, gcmData.toByteArray());
        Connect.IMTransferData backAck = Connect.IMTransferData.newBuilder()
                .setCipherData(gcmData)
                .setSign(signHash).build();

        ChatSendManager.getInstance().sendToMsg(socketack, backAck.toByteString());
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

        MsgNoticeBean.sendMsgNotice((MsgNoticeBean.NtEnum) objects[1], reqObj, serverObj);
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
        ChatMsgUtil.updateMsgSendState(pubkey, msgid, state);
        FailMsgsManager.getInstance().removeFailMap(msgid);
    }

    protected void pushNoticeMsg(String pubkey,int type,String content) {
        NotificationManager.getInstance().pushNoticeMsg(pubkey,type,content);
    }

    protected void commandToIMTransfer(String msgid, SocketACK ack, ByteString byteString) {
        Connect.Command command = Connect.Command.newBuilder().setMsgId(msgid).
                setDetail(byteString).build();
        ChatSendManager.getInstance().sendMsgidMsg(ack, msgid, command.toByteString());
    }

    /**
     * sycn contact
     *
     * @return
     */
    protected void requestFriendsByVersion() {
        String version = ParamManager.getInstance().getString(ParamManager.COUNT_FRIENDLIST);
        if (TextUtils.isEmpty(version)) {
            version = "0";
        }
        Connect.SyncRelationship syncRelationship = Connect.SyncRelationship.newBuilder()
                .setVersion(version).build();
        commandToIMTransfer(TimeUtil.timestampToMsgid(), SocketACK.CONTACT_SYNC, syncRelationship.toByteString());
    }
}
