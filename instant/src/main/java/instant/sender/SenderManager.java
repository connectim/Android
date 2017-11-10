package instant.sender;

import android.text.TextUtils;

import com.google.protobuf.ByteString;

import java.nio.ByteBuffer;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import instant.bean.Session;
import instant.bean.SocketACK;
import instant.sender.inter.LocalServiceListener;
import instant.utils.log.LogManager;
import instant.utils.manager.FailMsgsManager;
import instant.utils.cryption.EncryptionUtil;
import instant.utils.cryption.SupportKeyUril;
import protos.Connect;

/**
 * App 跟LocalService之间通信
 * Assembly chat interface to send message
 * Created by gtq on 2016/12/3.
 */
public class SenderManager implements LocalServiceListener{

    private static String Tag = "_SenderManager";
    public static SenderManager senderManager = getInstance();

    public static SenderManager getInstance() {
        if (senderManager == null) {
            synchronized (SenderManager.class) {
                if (senderManager == null) {
                    senderManager = new SenderManager();
                }
            }
        }
        return senderManager;
    }

    private static final int coreSize = 3;
    private static final int maxSize = 6;
    private static final int aliveSize = 1;

    private static BlockingQueue<Runnable> linkedBlockingQueue = new LinkedBlockingQueue<>();
    private static ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(coreSize, maxSize, aliveSize, TimeUnit.DAYS, linkedBlockingQueue);

    private LocalServiceListener serviceListener = null;

    public void registerListener(LocalServiceListener listener) {
        this.serviceListener = listener;
    }

    @Override
    public void messageSend(byte[] ack, ByteBuffer byteBuffer) {

    }

    @Override
    public void exitAccount(){
        if(serviceListener!=null){
            serviceListener.exitAccount();
        }
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
                    String priKey = Session.getInstance().getConnectCookie().getPriKey();
                    Connect.GcmData gcmData = EncryptionUtil.encodeAESGCMStructData(EncryptionUtil.ExtendedECDH.NONE,
                            Session.getInstance().getRandomCookie().getSalt(), bytes);
                    String signHash = SupportKeyUril.signHash(priKey, gcmData.toByteArray());
                    Connect.IMTransferData transferData = Connect.IMTransferData.newBuilder().
                            setSign(signHash).setCipherData(gcmData).build();

                    byteBuffer = ByteBuffer.wrap(transferData.toByteArray());
                    serviceListener.messageSend(ack.getOrder(),byteBuffer);
                } else {
                    byteBuffer = ByteBuffer.wrap(bytes.toByteArray());
                    serviceListener.messageSend(ack.getOrder(),byteBuffer);
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
