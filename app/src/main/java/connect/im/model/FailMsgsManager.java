package connect.im.model;

import android.text.TextUtils;

import com.google.gson.GsonBuilder;
import com.google.protobuf.ByteString;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import connect.db.green.DaoHelper.ContactHelper;
import connect.db.green.DaoHelper.MessageHelper;
import connect.db.green.bean.GroupEntity;
import connect.im.bean.SocketACK;
import connect.ui.activity.chat.bean.MsgDefinBean;
import connect.ui.activity.chat.model.ChatMsgUtil;
import connect.utils.StringUtil;
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
        Map<String, Object> valueMap = sendFailMap.get(roomid);
        if (valueMap == null) {
            valueMap = new HashMap<>();
        }
        valueMap.put(PUBKEY, roomid);
        valueMap.put(ACKORDER, order);
        valueMap.put(OBJECT, msg);
        valueMap.put(EXT, ext);
        sendFailMap.put(msgid, valueMap);
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

            Object object = failMap.get(ACKORDER);
            Object msgObj = failMap.get(OBJECT);
            if (object == null || msgObj == null) {
                entryIterator.remove();
            } else {
                SocketACK ack = (SocketACK) object;
                ByteString msgByte = (ByteString) msgObj;
                ChatSendManager.getInstance().sendMsgidMsg(ack, null, msgByte);
            }
        }
    }

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

        GroupEntity groupEntity = ContactHelper.getInstance().loadGroupEntity(pubkey);
        if (groupEntity == null) return;
        Map<String, Object> objectMap = receiveFailMap.get(pubkey);
        if (objectMap == null) {
            return;
        }

        Iterator<Map.Entry<String, Object>> entries = objectMap.entrySet().iterator();
        while (entries.hasNext()) {
            Map.Entry<String, Object> entry = entries.next();
            Object object = entry.getValue();

            if (object instanceof String) {
                ChatMsgUtil.insertNoticeMsg(pubkey, (String) object);
            } else if (object instanceof Connect.GcmData) {
                byte[] contents = DecryptionUtil.decodeAESGCM(SupportKeyUril.EcdhExts.NONE,
                        StringUtil.hexStringToBytes(groupEntity.getEcdh_key()), (Connect.GcmData) object);
                String content = new String(contents);
                if (!TextUtils.isEmpty(content) && content.length() > 10) {//sometime parse error
                    GsonBuilder gsonBuilder = new GsonBuilder();
                    gsonBuilder.registerTypeAdapter(MsgDefinBean.class, new MsgDefTypeAdapter());
                    MsgDefinBean definBean = gsonBuilder.create().fromJson(content, MsgDefinBean.class);

                    MessageHelper.getInstance().insertFromMsg(pubkey, definBean);
                    ChatMsgUtil.updateRoomInfo(pubkey, 1, definBean.getSendtime(), definBean);
                }
            }
            entries.remove();
        }
    }
}
