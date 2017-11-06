package instant.parser;

import android.text.TextUtils;

import com.google.protobuf.ByteString;

import java.nio.ByteBuffer;

import instant.bean.Session;
import instant.bean.UserCookie;
import instant.parser.localreceiver.MessageLocalReceiver;
import instant.utils.StringUtil;
import instant.utils.cryption.DecryptionUtil;
import instant.utils.cryption.EncryptionUtil;
import instant.utils.cryption.SupportKeyUril;
import instant.utils.log.LogManager;
import protos.Connect;

/**
 * Created by pujin on 2017/4/19.
 */

public class ChatParser extends InterParse {

    private static String TAG = "_ChatParser";

    private byte ackByte;
    private Connect.MessagePost messagePost;

    public ChatParser(byte ackByte, Connect.MessagePost messagePost) {
        super(ackByte, ByteBuffer.wrap(messagePost.toByteArray()));
        this.ackByte = ackByte;
        this.messagePost = messagePost;
    }

    @Override
    public synchronized void msgParse() throws Exception {
        switch (ackByte) {
            case 0x01://private chat
            case 0x02://burn chat
                singleChat(messagePost);
                break;
            case 0x03://invite to join in group
                inviteJoinGroup(messagePost);
                break;
            case 0x04://group chat
                groupChat(messagePost);
                break;
        }
    }

    public synchronized void singleChat(Connect.MessagePost msgpost) throws Exception {
        Connect.MessageData messageData = msgpost.getMsgData();
        Connect.ChatSession chatSession = messageData.getChatSession();
        Connect.ChatMessage chatMessage = messageData.getChatMsg();

        String friendPubKey = msgpost.getPubKey();
        String priKey = null;
        String pubkey = null;

        LogManager.getLogger().d(TAG, "Id: " + messageData.getChatMsg().getMsgId());
        EncryptionUtil.ExtendedECDH ecdhExts = EncryptionUtil.ExtendedECDH.EMPTY;
        if (TextUtils.isEmpty(chatSession.getPubKey())) {//old protocol
            priKey = Session.getInstance().getUserCookie(Session.CONNECT_USER).getPriKey();
            pubkey = friendPubKey;
        } else if (null == chatSession.getVer() || chatSession.getVer().size() == 0) {//half random
            priKey = Session.getInstance().getUserCookie(Session.CONNECT_USER).getPriKey();

            ByteString fromSalt = chatSession.getSalt();
            pubkey = chatSession.getPubKey();
            ecdhExts = EncryptionUtil.ExtendedECDH.OTHER;
            ecdhExts.setBytes(fromSalt.toByteArray());
        } else {//both random
            ByteString fromSalt = chatSession.getSalt();
            ByteString toSalt = chatSession.getVer();

            UserCookie toCookie = Session.getInstance().getCookieBySalt(StringUtil.bytesToHexString(toSalt.toByteArray()));
            if (toCookie == null) {
                return;
            }
            priKey = toCookie.getPriKey();
            pubkey = chatSession.getPubKey();
            ecdhExts = EncryptionUtil.ExtendedECDH.OTHER;
            ecdhExts.setBytes(SupportKeyUril.xor(fromSalt.toByteArray(), toSalt.toByteArray()));
        }

        byte[] contents = DecryptionUtil.decodeAESGCM(ecdhExts, priKey, pubkey, messageData.getChatMsg().getCipherData());
        MessageLocalReceiver.localReceiver.singleChat(chatMessage, pubkey, contents);
    }

    /**
     * group chat
     *
     * @param msgpost
     */
    protected synchronized void groupChat(Connect.MessagePost msgpost) {
        MessageLocalReceiver.localReceiver.groupChat(msgpost);
    }

    /**
     * invite to join group
     *
     * @param msgpost
     * @throws Exception
     */
    protected void inviteJoinGroup(Connect.MessagePost msgpost) throws Exception {
        String prikey = Session.getInstance().getUserCookie(Session.CONNECT_USER).getPriKey();
        Connect.GcmData gcmData = msgpost.getMsgData().getChatMsg().getCipherData();
        Connect.StructData structData = DecryptionUtil.decodeAESGCMStructData(EncryptionUtil.ExtendedECDH.EMPTY,
                prikey, msgpost.getPubKey(), gcmData);

        Connect.CreateGroupMessage groupMessage = Connect.CreateGroupMessage.parseFrom(structData.getPlainData());
        MessageLocalReceiver.localReceiver.inviteJoinGroup(groupMessage);
    }
}
