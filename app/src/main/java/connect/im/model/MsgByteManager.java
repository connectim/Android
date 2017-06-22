package connect.im.model;

import java.nio.ByteBuffer;
import java.security.NoSuchAlgorithmException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.LinkedBlockingQueue;

import connect.ui.service.bean.PushMessage;
import connect.ui.service.bean.ServiceAck;
import connect.utils.StringUtil;
import connect.utils.log.LogManager;

/**
 * Created by gtq on 2016/11/30.
 */
public class MsgByteManager {

    private String Tag = "MsgByteManager";

    private Timer timer;
    private TimerTask timerTask;

    public MsgByteManager() {
        processByteBufferTimer();
    }

    private static MsgByteManager msgByteManager;

    public static MsgByteManager getInstance() {
        if (msgByteManager == null) {
            synchronized (MsgByteManager.class) {
                if (msgByteManager == null) {
                    msgByteManager = new MsgByteManager();
                }
            }
        }
        return msgByteManager;
    }

    /** header length */
    private static final int MESSAGE_LENGTH_HEADER = 13;
    /** header array */
    private byte[] header = new byte[MESSAGE_LENGTH_HEADER];
    /** message buffer */
    private ByteBuffer byteBuffer = ByteBuffer.allocate(1024 * 1024);
    /** message buffer queue */
    private static LinkedBlockingQueue<ByteBuffer> byteBuffers = new LinkedBlockingQueue();

    public void putByteMsg(ByteBuffer bytes) {
        try {
            byteBuffers.put(bytes);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * check message header
     *
     * @param bytes
     * @return
     */
    private boolean checkMsgHeader(byte[] bytes) {
        byte[] ext = null;
        try {
            ext = StringUtil.byteTomd5(bytes);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        if (ext[0] == header[11] && ext[1] == header[12]) {
            return true;
        } else {
            return false;
        }
    }

    protected void processByteBufferTimer() {
        closeProcessTimer();

        timer = new Timer();
        timerTask = new TimerTask() {
            private int bodyLen = -1;
            int lastPostion = 0;

            @Override
            public void run() {
                try {
                    ByteBuffer indexBytes = byteBuffers.take();
                    LogManager.getLogger().d(Tag, "message length:" + indexBytes.limit()
                            + ";byteBuffer postion:" + byteBuffer.position());
                    byteBuffer.position(lastPostion);
                    byteBuffer.put(indexBytes);
                    byteBuffer.flip();

                    /**
                     * Circulation processing Until you complete message processing
                     */
                    while (byteBuffer.remaining() > 0) {
                        if (byteBuffer.remaining() < MESSAGE_LENGTH_HEADER) {
                            LogManager.getLogger().d(Tag, "Buffer bytes to piece together enough in header");
                            break;
                        }

                        int posi = byteBuffer.position();
                        bodyLen = byteArrayToInt(new byte[]{byteBuffer.get(posi + 2),
                                byteBuffer.get(posi + 3),
                                byteBuffer.get(posi + 4),
                                byteBuffer.get(posi + 5)});

                        if (byteBuffer.remaining() < MESSAGE_LENGTH_HEADER + bodyLen) {
                            LogManager.getLogger().d(Tag, "Buffer bytes is not enough to piece together a complete body of the message");
                            break;
                        } else {
                            byte[] body = new byte[bodyLen];
                            byteBuffer.get(header);
                            byteBuffer.get(body, 0, bodyLen);
                            ByteBuffer head = ByteBuffer.wrap(header);
                            head.clear();

                            ByteBuffer headcheck = ByteBuffer.allocate(13);
                            byte[] front = new byte[10];
                            head.position(1);
                            head.get(front);
                            headcheck.put(front);
                            headcheck.put(StringUtil.MSG_HEADER_EXI);
                            if (!checkMsgHeader(headcheck.array())) {
                                PushMessage.pushMessage(ServiceAck.STOP_CONNECT,new byte[0],ByteBuffer.allocate(0));
                                return;
                            }

                            head.rewind();
                            ByteBuffer bodyBuf = ByteBuffer.wrap(body);
                            MsgRecManager.getInstance().sendMessage(head, bodyBuf);

                            LogManager.getLogger().d(Tag, "Take a message:");
                            LogManager.getLogger().d(Tag, "byteBuffer：   posi "
                                    + byteBuffer.position() + " limit "
                                    + byteBuffer.limit());
                        }
                    }

                    LogManager.getLogger().d(Tag, "Accept the next message");
                    LogManager.getLogger().d(Tag, "byteBuffer：   posi " + byteBuffer.position()
                            + " limit " + byteBuffer.limit());

                    lastPostion = byteBuffer.remaining();
                    byteBuffer.compact();
                    byteBuffer.clear();
                } catch (Exception e) {
                    e.printStackTrace();
                    LogManager.getLogger().d(Tag, "The news of the accept abnormalities");
                }
            }
        };
        timer.schedule(timerTask, 250, 250);
    }

    public int byteArrayToInt(byte[] buf) {
        if (buf.length != 4) {
            throw new RuntimeException("Array count greater than 4");
        }
        int result = 0;
        int size = buf.length;
        for (int i = 0; i < size; i++) {
            byte b = buf[i];
            int temp = (b & 0xff) << (8 * (3 - i));
            result = result | temp;
        }
        return result;
    }

    public void closeProcessTimer() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        if (timerTask != null) {
            timerTask.cancel();
            timerTask = null;
        }
    }
}