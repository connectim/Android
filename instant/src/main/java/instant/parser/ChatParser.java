package instant.parser;

import com.google.protobuf.ByteString;

import java.nio.ByteBuffer;

import instant.bean.Session;
import instant.bean.UserCookie;
import instant.parser.localreceiver.MessageLocalReceiver;
import instant.utils.cryption.DecryptionUtil;
import instant.utils.cryption.EncryptionUtil;
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
        Connect.ChatMessage chatMessage = messageData.getChatMsg();

        String friendPublicKey = messageData.getChatSession().getPubKey();
        UserCookie userCookie = Session.getInstance().getConnectCookie();
        String myPrivateKey = userCookie.getPrivateKey();

        EncryptionUtil.ExtendedECDH ecdhExts = EncryptionUtil.ExtendedECDH.EMPTY;
        byte[] contents = DecryptionUtil.decodeAESGCM(ecdhExts, myPrivateKey, friendPublicKey, chatMessage.getCipherData());
        chatMessage = chatMessage.toBuilder().setBody(ByteString.copyFrom(contents)).build();

        MessageLocalReceiver.localReceiver.singleChat(chatMessage);
    }

    /**
     * group chat
     *
     * @param msgpost
     */
    protected synchronized void groupChat(Connect.MessagePost msgpost) {
        Connect.MessageData messageData = msgpost.getMsgData();
        Connect.ChatMessage chatMessage = messageData.getChatMsg();
        MessageLocalReceiver.localReceiver.groupChat(chatMessage);
    }

    /**
     * invite to join group
     *
     * @param msgpost
     * @throws Exception
     */
    protected void inviteJoinGroup(Connect.MessagePost msgpost) throws Exception {
//        String publicKey = msgpost.getPubKey();
//        String privateKey = Session.getInstance().getConnectCookie().getPriKey();
//
//        Connect.MessageData messageData = msgpost.getMsgData();
//        Connect.ChatMessage chatMessage = messageData.getChatMsg();
//        Connect.GcmData gcmData = chatMessage.getCipherData();
//
//        byte[] contents = DecryptionUtil.decodeAESGCM(EncryptionUtil.ExtendedECDH.EMPTY, privateKey, publicKey, gcmData);
//
//        Connect.CreateGroupMessage groupMessage = Connect.CreateGroupMessage.parseFrom(contents);
//        MessageLocalReceiver.localReceiver.inviteJoinGroup(groupMessage);
    }
}
