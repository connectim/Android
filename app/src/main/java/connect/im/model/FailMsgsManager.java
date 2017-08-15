package connect.im.model;

import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import com.google.gson.GsonBuilder;
import com.google.protobuf.ByteString;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import connect.activity.chat.bean.MsgExtEntity;
import connect.activity.chat.model.content.GroupChat;
import connect.activity.chat.model.content.RobotChat;
import connect.database.MemoryDataManager;
import connect.database.green.DaoHelper.ContactHelper;
import connect.database.green.DaoHelper.MessageHelper;
import connect.database.green.bean.ContactEntity;
import connect.database.green.bean.GroupEntity;
import connect.im.bean.SocketACK;
import connect.activity.chat.bean.MsgDefinBean;
import connect.activity.chat.bean.MsgEntity;
import connect.activity.chat.model.ChatMsgUtil;
import connect.activity.chat.model.content.FriendChat;
import connect.activity.chat.model.content.NormalChat;
import connect.activity.base.BaseApplication;
import connect.im.parser.ChatParseBean;
import connect.ui.activity.R;
import connect.utils.StringUtil;
import connect.utils.TimeUtil;
import connect.utils.cryption.DecryptionUtil;
import connect.utils.cryption.SupportKeyUril;
import connect.utils.okhttp.adapter.MsgDefTypeAdapter;
import protos.Connect;

/**
 * Failure message handling
 * Created by pujin on 2017/1/22.
 */
public class FailMsgsManager {

    public static FailMsgsManager manager;

    public static FailMsgsManager getInstance() {
        if (manager == null) {
            synchronized (FailMsgsManager.class) {
                if (manager == null) {
                    manager = new FailMsgsManager();
                }
            }
        }
        return manager;
    }

    private static String PUBKEY = "PUBKEY";
    private static String ACKORDER = "ACKORDER";
    private static String OBJECT = "OBJECT";
    private static String EXT = "EXT";

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

    public void insertFailMsg(String address, String msgid) {
        if (sendFailMap == null) {
            sendFailMap = new HashMap<>();
        }
        String expireUser = "USER:EXPIRE";
        Map<String, Object> valueMap = sendFailMap.get(expireUser);
        if (valueMap == null) {
            valueMap = new HashMap<>();
        }
        valueMap.put("ADDRESS", address);
        valueMap.put("MSGID", msgid);
        sendFailMap.put(expireUser, valueMap);
    }

    public void sendExpireMsg() throws Exception {
        if (sendFailMap == null) {
            sendFailMap = new HashMap<>();
        }

        Map valueMap = getFailMap("USER:EXPIRE");
        if (valueMap != null) {
            String address = (String) valueMap.get("ADDRESS");
            String msgid = (String) valueMap.get("MSGID");
            ContactEntity friendEntity = ContactHelper.getInstance().loadFriendEntity(address);
            if (friendEntity != null) {
                FriendChat friendChat = new FriendChat(friendEntity);
                MsgExtEntity msgExtEntity = friendChat.loadEntityByMsgid(msgid);
                friendChat.sendPushMsg(msgExtEntity);
            }
        }
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

    public void removeAllFailMsg(){
        if (sendFailMap == null) {
            sendFailMap = new HashMap<>();
        }
        sendFailMap.clear();
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
                ChatSendManager.getInstance().sendAckMsg(ack, (String) keyObj, msgid, msgByte);
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
        android.os.Message msg = new Message();
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
    private Handler delayFailHandler = new Handler(BaseApplication.getInstance().getBaseContext().getMainLooper()) {
        @Override
        public void handleMessage(android.os.Message msg) {
            super.handleMessage(msg);
            String msgid = (String) msg.obj;
            Map failMap = FailMsgsManager.getInstance().getFailMap(msgid);
            if (failMap != null) {
                ChatMsgUtil.updateMsgSendState((String) failMap.get(PUBKEY), msgid, 2);
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
    public void receiveFailMsgs(String pubkey) {
        if (receiveFailMap == null) {
            receiveFailMap = new HashMap<>();
        }

        Map<String, Object> objectMap = receiveFailMap.get(pubkey);
        if (objectMap == null) {
            return;
        }

        Iterator<Map.Entry<String, Object>> entries = objectMap.entrySet().iterator();
        while (entries.hasNext()) {
            Map.Entry<String, Object> entry = entries.next();
            Object object = entry.getValue();

            NormalChat normalChat = loadBaseChat(pubkey);
            if (normalChat != null) {
                if (object instanceof String) {
                    String mypublickey = MemoryDataManager.getInstance().getPubKey();
                    MsgExtEntity msgExtEntity = normalChat.noticeMsg((String) object);
                    msgExtEntity.setFrom(pubkey);
                    msgExtEntity.setTo(mypublickey);

                    MessageHelper.getInstance().insertMsgExtEntity(msgExtEntity);
                    normalChat.updateRoomMsg(null, msgExtEntity.showContent(), msgExtEntity.getCreatetime());
                } else if (object instanceof Connect.MessagePost) {
                    try {
                        ChatParseBean chatParseBean = new ChatParseBean((byte) (0x04), (Connect.MessagePost) object);
                        chatParseBean.msgParse();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                entries.remove();
            }
        }
    }

    public NormalChat loadBaseChat(String pubkey) {
        NormalChat normalChat = null;

        if ((BaseApplication.getInstance().getString(R.string.app_name)).equals(pubkey)) {
            normalChat = RobotChat.getInstance();
        } else {
            GroupEntity groupEntity = ContactHelper.getInstance().loadGroupEntity(pubkey);
            if (groupEntity != null) {
                normalChat = new GroupChat(groupEntity);
            } else {
                ContactEntity friendEntity = ContactHelper.getInstance().loadFriendEntity(pubkey);
                if (friendEntity != null) {
                    normalChat = new FriendChat(friendEntity);
                }
            }
        }
        return normalChat;
    }
}
