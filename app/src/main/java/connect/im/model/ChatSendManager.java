package connect.im.model;

import android.text.TextUtils;

import com.google.protobuf.ByteString;

import java.nio.ByteBuffer;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import connect.database.MemoryDataManager;
import connect.im.bean.Session;
import connect.im.bean.SocketACK;
import connect.service.bean.PushMessage;
import connect.service.bean.ServiceAck;
import connect.utils.cryption.EncryptionUtil;
import connect.utils.cryption.SupportKeyUril;
import connect.utils.log.LogManager;
import protos.Connect;

/**
 * Assembly chat interface to send message
 * Created by gtq on 2016/12/3.
 */
public class ChatSendManager {

    private static String Tag="_ChatSendManager";
    private static ChatSendManager chatSendManager;

    public static ChatSendManager getInstance() {
        if (chatSendManager == null) {
            synchronized (ChatSendManager.class) {
                if (chatSendManager == null) {
                    chatSendManager = new ChatSendManager();
                }
            }
        }
        return chatSendManager;
    }

    private static final int coreSize = 3;
    private static final int maxSize = 6;
    private static final int aliveSize = 1;

    private static BlockingQueue<Runnable> linkedBlockingQueue = new LinkedBlockingQueue<>();
    private static ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(coreSize, maxSize, aliveSize, TimeUnit.DAYS, linkedBlockingQueue);

    /**
     * Other messages
     */
    public void sendMsgidMsg(SocketACK order, String msgid, ByteString bytes) {
        sendAckMsg(order, null, msgid, bytes);
    }

    /**
     * Robot news
     */
    public void sendRobotAckMsg(SocketACK order, String roomkey, Connect.MSMessage data) {
        sendAckMsg(order, roomkey, data.getMsgId(), data.toByteString());
    }

    /**
     * send byte[]
     *
     * @param order
     * @param data
     */
    public void sendChatAckMsg(SocketACK order, String roomkey, Connect.MessageData data) {
        String priKey = MemoryDataManager.getInstance().getPriKey();

        //messagePost
        String postsign = SupportKeyUril.signHash(priKey, data.toByteArray());
        String mypubkey = SupportKeyUril.getPubKeyFromPriKey(priKey);
        Connect.MessagePost messagePost = Connect.MessagePost.newBuilder().
                setMsgData(data).setSign(postsign).
                setPubKey(mypubkey).build();

        sendAckMsg(order, roomkey, data.getChatMsg().getMsgId(), messagePost.toByteString());
    }

    public void sendAckMsg(SocketACK order, String roomkey, String msgid, ByteString bytes) {
        if (!TextUtils.isEmpty(msgid)) {
            LogManager.getLogger().d(Tag, msgid);
        }

        SendChatRun sendChatRun = new SendChatRun(order, bytes);
        threadPoolExecutor.execute(sendChatRun);

        boolean canFailReSend = true;
        byte[] getOrder = order.getOrder();
        switch (getOrder[0]) {
            case 0x04:
                canFailReSend = false;
                break;
            case 0x05:
                if (getOrder[1] == 0x03) {
                    canFailReSend = false;
                }
                break;
        }
        if (canFailReSend) {
            FailMsgsManager.getInstance().sendDelayFailMsg(roomkey, msgid, order, bytes);
        }
    }

    public void sendToMsg(SocketACK ack, ByteString byteString) {
        SendChatRun sendChatRun = new SendChatRun(false, ack, byteString);
        threadPoolExecutor.execute(sendChatRun);
    }

    protected class SendChatRun implements Runnable {
        private boolean transfer;
        private SocketACK ack;
        private ByteString bytes;

        SendChatRun(SocketACK ack, ByteString bytes) {
            this.transfer = true;
            this.ack = ack;
            this.bytes = bytes;
        }

        SendChatRun(boolean transfer, SocketACK ack, ByteString bytes) {
            this.transfer = transfer;
            this.ack = ack;
            this.bytes = bytes;
        }

        @Override
        public synchronized void run() {
            try {
                LogManager.getLogger().i(Tag, "sender order: " + ack.name() + "[" + ack.getOrder()[0] + ack.getOrder()[1] + "]");

                ByteBuffer byteBuffer = null;
                if (transfer) { // transferData,Encapsulating server checksum data
                    String priKey = MemoryDataManager.getInstance().getPriKey();
                    Connect.GcmData gcmData = EncryptionUtil.encodeAESGCMStructData(EncryptionUtil.ExtendedECDH.NONE,
                            Session.getInstance().getUserCookie("TEMPCOOKIE").getSalt(), bytes);
                    String signHash = SupportKeyUril.signHash(priKey, gcmData.toByteArray());
                    Connect.IMTransferData transferData = Connect.IMTransferData.newBuilder().
                            setSign(signHash).setCipherData(gcmData).build();

                    byteBuffer = ByteBuffer.wrap(transferData.toByteArray());
                    PushMessage.pushMessage(ServiceAck.MESSAGE, ack.getOrder(), byteBuffer);
                } else {
                    byteBuffer = ByteBuffer.wrap(bytes.toByteArray());
                    PushMessage.pushMessage(ServiceAck.MESSAGE, ack.getOrder(), byteBuffer);
                }
            } catch (Exception e) {
                e.printStackTrace();
                String errInfo = e.getMessage();
                if (TextUtils.isEmpty(errInfo)) {
                    errInfo = "";
                }
                LogManager.getLogger().d(Tag, "exception order: [" + ack.getOrder()[0] + "][" + ack.getOrder()[1] + "]" + errInfo);
            }
        }
    }
}
