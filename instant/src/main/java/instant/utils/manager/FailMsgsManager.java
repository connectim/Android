package instant.utils.manager;

import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import com.google.protobuf.ByteString;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import instant.bean.SocketACK;
import instant.parser.localreceiver.CommandLocalReceiver;
import instant.parser.localreceiver.MessageLocalReceiver;
import instant.sender.SenderManager;
import instant.ui.InstantSdk;
import instant.utils.TimeUtil;
import protos.Connect;

/**
 * Failure message handling
 * Created by pujin on 2017/1/22.
 */
public class FailMsgsManager {

    public static FailMsgsManager manager;

    public synchronized static FailMsgsManager getInstance() {
        if (manager == null) {
            manager = new FailMsgsManager();
        }
        return manager;
    }

    public static String PUBKEY = "PUBKEY";
    public static String ACKORDER = "ACKORDER";
    public static String OBJECT = "OBJECT";
    public static String EXT = "EXT";

    /** send fail msg info */
    private Map<String, Map<String, Object>> sendFailMap = new HashMap<>();

    /** receive fail msg info */
    private Map<String, Map<String, Object>> receiveFailMap = new HashMap<>();

    /***************************  Send failure message set  *************************************************/
    public void insertFailMsg(String roomid, String msgid, SocketACK order, ByteString msg, Object ext) {
        if (sendFailMap == null) {
            sendFailMap = new HashMap<>();
        }
        Map<String, Object> valueMap = sendFailMap.get(msgid);
        if (valueMap == null) {
            valueMap = new HashMap<>();
        }
        valueMap.put(PUBKEY, roomid);
        valueMap.put(ACKORDER, order);
        valueMap.put(OBJECT, msg);
        valueMap.put(EXT, ext);
        sendFailMap.put(msgid, valueMap);
    }

    public void insertFailMsg(String uid, String msgid) {
        if (sendFailMap == null) {
            sendFailMap = new HashMap<>();
        }
        String expireUser = "USER:EXPIRE";
        Map<String, Object> valueMap = sendFailMap.get(expireUser);
        if (valueMap == null) {
            valueMap = new HashMap<>();
        }
        valueMap.put("UID", uid);
        valueMap.put("MSGID", msgid);
        sendFailMap.put(expireUser, valueMap);
    }

    public Map<String, Object> getFailMap(String msgid) {
        if (sendFailMap == null) {
            sendFailMap = new HashMap<>();
        }
        return sendFailMap.get(msgid);
    }

    public void removeFailMap(String msgid){
        if (sendFailMap == null) {
            sendFailMap = new HashMap<>();
        }
        sendFailMap.remove(msgid);
    }

    public void removeAllFailMsg() {
        if (sendFailMap != null) {
            sendFailMap.clear();
            sendFailMap = null;
        }

        if (receiveFailMap != null) {
            receiveFailMap.clear();
        }
    }

    /**
     * After the success of the reconnection Send all the failure message
     */
    public void sendFailMsgs() {
        if (sendFailMap == null) {
            sendFailMap = new HashMap<>();
        }

        Iterator<Map.Entry<String, Map<String, Object>>> entryIterator = sendFailMap.entrySet().iterator();
        while (entryIterator.hasNext()) {
            Map.Entry<String, Map<String, Object>> entity = entryIterator.next();
            Map<String, Object> failMap = entity.getValue();

            String msgid = entity.getKey();
            Object keyObj = failMap.get(PUBKEY);
            Object object = failMap.get(ACKORDER);
            Object msgObj = failMap.get(OBJECT);
            if (object == null || msgObj == null) {
                entryIterator.remove();
            } else {
                SocketACK ack = (SocketACK) object;
                ByteString msgByte = (ByteString) msgObj;
                SenderManager.getInstance().sendAckMsg(ack, (String) keyObj, msgid, msgByte);
            }
        }
    }

    /**
     * Delay message sent failure
     *
     * @param msgid
     * @param roomkey
     */
    public void sendDelayFailMsg(String roomkey, String msgid, SocketACK order, ByteString msgbyte) {
        insertFailMsg(roomkey, msgid, order, msgbyte, null);

        long delaytime = TextUtils.isEmpty(roomkey) ? MSGTIME_OTHER : MSGTIME_CHAT;
        Message msg = new Message();
        msg.what = TimeUtil.msgidToInt(msgid);
        msg.obj = msgid;
        delayFailHandler.sendMessageDelayed(msg, delaytime);
    }

    /** Chat messages failure time */
    private final long MSGTIME_CHAT = 20 * 1000;
    /** Other messages sent failure time */
    private final long MSGTIME_OTHER = 1000;

    /**
     * Delay message sent failure
     */
    private Handler delayFailHandler = new Handler(InstantSdk.getInstance().getBaseContext().getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            String msgid = (String) msg.obj;
            Map failMap = FailMsgsManager.getInstance().getFailMap(msgid);
            if (failMap != null) {
                CommandLocalReceiver.receiver.updateMsgSendState((String) failMap.get(PUBKEY),msgid,2);
            }
        }
    };

    /***************************  Accept failure message set  *************************************************/
    public void insertReceiveMsg(String pubkey, String msgid, Object object) {
        if (receiveFailMap == null) {
            receiveFailMap = new HashMap<>();
        }

        Map<String, Object> failMap = receiveFailMap.get(pubkey);
        if (failMap == null) {
            failMap = new HashMap<>();
        }
        failMap.put(msgid, object);
        receiveFailMap.put(pubkey, failMap);
    }

    /**
     * Parse failed receive messages
     * @param pubkey
     */
    public  Map<String, Object> receiveFailMsgs(String pubkey) {
        if (receiveFailMap == null) {
            receiveFailMap = new HashMap<>();
        }

        Map<String, Object> objectMap = receiveFailMap.get(pubkey);
        return objectMap;
    }
}
