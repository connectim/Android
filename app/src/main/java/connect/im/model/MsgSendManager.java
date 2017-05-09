package connect.im.model;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import connect.utils.StringUtil;
import connect.utils.log.LogManager;

/**
 * Created by gtq on 2016/11/30.
 */
public class MsgSendManager {

    private String Tag = "MsgSendManager";

    private static MsgSendManager sendManager;

    public static MsgSendManager getInstance() {
        if (sendManager == null) {
            sendManager = new MsgSendManager();
        }
        return sendManager;
    }

    /** version number */
    private static final byte MSG_VERSION = 0x01;
    /** Message length */
    private static final int MSG_BODY_LENGTH = 4;
    /** Message header length */
    private static final int MSG_HEADER_LENGTH = 13;


    private static final int coreSize = 3;
    private static final int maxSize = 6;
    private static final int aliveSize = 1;

    private static BlockingQueue<Runnable> linkedBlockingQueue = new LinkedBlockingQueue<>();
    private static ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(coreSize, maxSize, aliveSize, TimeUnit.DAYS, linkedBlockingQueue);

    /**
     * Save failed message, save the corresponding reply id (chat)
     */
    public synchronized void sendMessage(byte[] orders,byte[] msg) {
        LogManager.getLogger().d(Tag, "send message order :[" + orders[0] + "] [" + orders[1] + "]");
        ByteBuffer byteBuffer = protoToByteBuffer(orders, msg);
        sendMessage(byteBuffer);
    }

    public synchronized void sendMessage(ByteBuffer byteBuffer) {
        threadPoolExecutor.execute(new SendRun(byteBuffer));
    }

    private class SendRun implements Runnable {

        private ByteBuffer message;

        public SendRun(ByteBuffer msg) {
            this.message = msg;
        }

        @Override
        public synchronized void run() {
            if (message == null) {
                return;
            }

            boolean avaliableConnc=true;
            try {
                SocketChannel channel = ConnectManager.getInstance().getSocketChannel();
                avaliableConnc = ConnectManager.getInstance().avaliableConnect();
                if (avaliableConnc) {
                    while (message.hasRemaining()) {
                        channel.write(message);
                    }
                    LogManager.getLogger().d(Tag, "send message success");
                }
            } catch (IOException e) {
                e.printStackTrace();
                avaliableConnc = false;
            }

            if (!avaliableConnc) {
                ConnectManager.getInstance().reconDelay();
            }
        }
    }

    /**
     * Assemble Command + message to ByteBuffer
     *
     * @param orders
     * @param msgBytes
     * @return
     */
    private ByteBuffer protoToByteBuffer(byte[] orders, byte[] msgBytes) {
        byte[] bytesLength = ByteBuffer.allocate(MSG_BODY_LENGTH).putInt(msgBytes.length).array();
        byte[] headArr = new byte[MSG_HEADER_LENGTH + msgBytes.length];

        byte[] randomBytes = SecureRandom.getSeed(4);

        ByteBuffer header = ByteBuffer.allocate(MSG_HEADER_LENGTH);
        header.put(orders[0]);
        header.put(bytesLength);
        header.put(orders[1]);
        header.put(randomBytes);
        header.put(StringUtil.MSG_HEADER_EXI);

        byte[] ext = null;
        try {
            ext = StringUtil.byteTomd5(header.array());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        ByteBuffer buffer = ByteBuffer.wrap(headArr);
        buffer.put(MSG_VERSION);
        buffer.put(orders[0]);
        buffer.put(bytesLength);
        buffer.put(orders[1]);
        buffer.put(randomBytes);
        buffer.put(ext[0]);
        buffer.put(ext[1]);
        buffer.put(msgBytes);
        buffer.flip();
        return buffer;
    }
}