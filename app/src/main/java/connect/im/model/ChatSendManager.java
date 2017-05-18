package connect.im.model;

import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import com.google.protobuf.ByteString;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import connect.db.MemoryDataManager;
import connect.db.SharedPreferenceUtil;
import connect.im.bean.Session;
import connect.im.bean.SocketACK;
import connect.ui.activity.chat.model.ChatMsgUtil;
import connect.ui.base.BaseApplication;
import connect.utils.cryption.EncryptionUtil;
import connect.utils.cryption.SupportKeyUril;
import connect.utils.log.LogManager;
import protos.Connect;

/**
 * Assembly chat interface to send message
 * Created by gtq on 2016/12/3.
 */
public class ChatSendManager {

    private static String Tag="ChatSendManager";
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

        sendAckMsg(order, roomkey, data.getMsgId(), messagePost.toByteString());
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
        }
        if (canFailReSend) {
            sendDelayFailMsg(roomkey, msgid, order, bytes);
        }
    }

    private class SendChatRun implements Runnable {
        private SocketACK order;
        private ByteString bytes;

        SendChatRun(SocketACK order, ByteString bytes) {
            this.order = order;
            this.bytes = bytes;
        }

        @Override
        public synchronized void run() {
            try {
                String priKey = MemoryDataManager.getInstance().getPriKey();

                //transferData
                Connect.GcmData gcmData = EncryptionUtil.encodeAESGCMStructData(SupportKeyUril.EcdhExts.NONE,
                        Session.getInstance().getUserCookie("TEMPCOOKIE").getSalt(), bytes);
                String signHash = SupportKeyUril.signHash(priKey, gcmData.toByteArray());
                Connect.IMTransferData transferData = Connect.IMTransferData.newBuilder().
                        setSign(signHash).setCipherData(gcmData).build();

                MsgSendManager.getInstance().sendMessage(order.getOrder(), transferData.toByteArray());
            } catch (Exception e) {
                e.printStackTrace();
                String errInfo = e.getMessage();
                if (TextUtils.isEmpty(errInfo)) {
                    errInfo = "";
                }
                LogManager.getLogger().d(Tag, "exception order: [" + order.getOrder()[0] + "][" + order.getOrder()[1] + "]" + e.getMessage());
            }
        }
    }

    public void sendDelayFailMsg(String roomkey, String msgid) {
        sendDelayFailMsg(roomkey, msgid, null, null);
    }

    /**
     * Delay message sent failure
     *
     * @param msgid
     * @param roomkey
     */
    public void sendDelayFailMsg(String roomkey, String msgid, SocketACK order, ByteString msgbyte) {
        long delaytime = 0;
        if (TextUtils.isEmpty(roomkey)) {
            delaytime = MSGTIME_OTHER;
        } else {
            delaytime = MSGTIME_CHAT;
        }
        FailMsgsManager.getInstance().insertFailMsg(roomkey, msgid, order, msgbyte, null);

        if (!TextUtils.isEmpty(msgid)) {
            android.os.Message msg = new Message();
            msg.what = MSG_FAIL;
            msg.obj = msgid;
            delayFailHandler.sendMessageDelayed(msg, delaytime);
        }
    }

    /** Chat messages failure time */
    private final long MSGTIME_CHAT = 10 * 1000;
    /** Other messages sent failure time */
    private final long MSGTIME_OTHER = 1000;
    /** Failure message */
    private final int MSG_FAIL = 100;

    /**
     * Delay message sent failure
     */
    private Handler delayFailHandler = new Handler(BaseApplication.getInstance().getBaseContext().getMainLooper()) {
        @Override
        public void handleMessage(android.os.Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_FAIL:
                    String msgid = (String) msg.obj;
                    Map failMap = FailMsgsManager.getInstance().getFailMap(msgid);
                    if (failMap != null) {
                        ChatMsgUtil.updateMsgSendState("", msgid, 2);
                    }
                    break;
            }
        }
    };
}
