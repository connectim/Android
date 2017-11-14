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

        String priKey = null;
        String pubkey = null;
        LogManager.getLogger().d(TAG, "Id: " + chatMessage.getMsgId());
        ByteString fromSalt = chatSession.getSalt();
        ByteString toSalt = chatSession.getVer();

        UserCookie toCookie = Session.getInstance().getCookieBySalt(StringUtil.bytesToHexString(toSalt.toByteArray()));
        if (toCookie == null) {
            return;
        }
        priKey = toCookie.getPriKey();
        pubkey = chatSession.getPubKey();
        EncryptionUtil.ExtendedECDH ecdhExts = EncryptionUtil.ExtendedECDH.OTHER;
        ecdhExts.setBytes(SupportKeyUril.xor(fromSalt.toByteArray(), toSalt.toByteArray()));

        byte[] contents = DecryptionUtil.decodeAESGCM(ecdhExts, priKey, pubkey, messageData.getChatMsg().getCipherData());
        MessageLocalReceiver.localReceiver.singleChat(chatMessage, contents);
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
        String publicKey = msgpost.getPubKey();
        String privateKey = Session.getInstance().getConnectCookie().getPriKey();

        Connect.MessageData messageData = msgpost.getMsgData();
        Connect.ChatMessage chatMessage = messageData.getChatMsg();
        Connect.GcmData gcmData = chatMessage.getCipherData();

        byte[] contents = DecryptionUtil.decodeAESGCM(EncryptionUtil.ExtendedECDH.EMPTY, privateKey, publicKey, gcmData);

        Connect.CreateGroupMessage groupMessage = Connect.CreateGroupMessage.parseFrom(contents);
        MessageLocalReceiver.localReceiver.inviteJoinGroup(groupMessage);
    }
}
