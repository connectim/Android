package connect.database.green.DaoHelper;

import android.database.Cursor;
import android.text.TextUtils;

import org.greenrobot.greendao.query.DeleteQuery;
import org.greenrobot.greendao.query.QueryBuilder;

import java.util.ArrayList;
import java.util.List;

import connect.database.green.BaseDao;
import connect.database.green.bean.MessageEntity;
import connect.database.green.dao.MessageEntityDao;
import connect.utils.StringUtil;
import instant.bean.ChatMsgEntity;

/**
 * message detail
 * Created by gtq on 2016/11/23.
 */
public class MessageHelper extends BaseDao {

    private static String TAG = "MessageHelper";

    private static MessageHelper messageHelper;
    private MessageEntityDao messageEntityDao;

    public MessageHelper() {
        super();
        messageEntityDao = daoSession.getMessageEntityDao();
    }

    public synchronized static MessageHelper getInstance() {
        if (messageHelper == null) {
            messageHelper = new MessageHelper();
        }
        return messageHelper;
    }

    public static void closeHelper() {
        messageHelper = null;
    }

    /********************************* select ***********************************/
    public List<ChatMsgEntity> loadMessageBySearchTxt(String message_ower, String searchTxt) {
        daoSession.clear();

        if (TextUtils.isEmpty(message_ower)) {
            message_ower = "";
        }

        String sql = "SELECT M.* FROM MESSAGE_ENTITY M,(SELECT * FROM MESSAGE_ENTITY TEMP WHERE TEMP.TXT_CONTENT LIKE ? ORDER BY TEMP._id ASC LIMIT 1) AS LAST  WHERE M.MESSAGE_OWER = ? AND M.CREATETIME >= LAST.CREATETIME;";
        Cursor cursor = daoSession.getDatabase().rawQuery(sql, new String[]{"%" + searchTxt + "%", message_ower});

        ChatMsgEntity msgEntity = null;
        List<ChatMsgEntity> msgEntities = new ArrayList();

        while (cursor.moveToNext()) {
            msgEntity = new ChatMsgEntity();
            msgEntity.set_id(cursorGetLong(cursor, "_id"));
            msgEntity.setMessage_ower(cursorGetString(cursor, "MESSAGE_OWER"));
            msgEntity.setMessage_id(cursorGetString(cursor, "MESSAGE_ID"));
            msgEntity.setChatType(cursorGetInt(cursor, "CHAT_TYPE"));
            msgEntity.setMessage_from(cursorGetString(cursor, "MESSAGE_FROM"));
            msgEntity.setMessage_to(cursorGetString(cursor, "MESSAGE_TO"));
            msgEntity.setMessageType(cursorGetInt(cursor, "MESSAGE_TYPE"));
            msgEntity.setContent(cursorGetString(cursor, "CONTENT"));
            msgEntity.setSend_status(cursorGetInt(cursor, "SEND_STATUS"));
            msgEntity.setRead_time(cursorGetLong(cursor, "READ_TIME"));
            msgEntity.setCreatetime(cursorGetLong(cursor, "CREATETIME"));

            msgEntity.setTransStatus(cursorGetInt(cursor, "TRANS_STATUS"));
            msgEntity.setHashid(cursorGetString(cursor, "HASHID"));
            msgEntity.setPayCount(cursorGetInt(cursor, "PAY_COUNT"));
            msgEntity.setCrowdCount(cursorGetInt(cursor, "CROWD_COUNT"));

            String content = msgEntity.getContent();
            if (!TextUtils.isEmpty(content)) {
                msgEntity.setContents(StringUtil.hexStringToBytes(content));
            }
            msgEntities.add(msgEntity);
        }
        if (cursor != null) {
            cursor.close();
        }
        return msgEntities;
    }


    public List<ChatMsgEntity> loadMoreMsgEntities(String message_ower, long firsttime) {
        daoSession.clear();

        if (TextUtils.isEmpty(message_ower)) {
            message_ower = "";
        }

        String sql = "SELECT * FROM (SELECT C.* ,S.STATUS AS TRANS_STATUS,HASHID,PAY_COUNT,CROWD_COUNT FROM MESSAGE_ENTITY C LEFT OUTER JOIN TRANSACTION_ENTITY S ON C.MESSAGE_ID = S.MESSAGE_ID WHERE C.MESSAGE_OWER = ? " +
                " AND C.CREATETIME < " + firsttime +
                " ORDER BY C.CREATETIME DESC LIMIT 20) ORDER BY CREATETIME ASC;";

        Cursor cursor = daoSession.getDatabase().rawQuery(sql, new String[]{message_ower});
        ChatMsgEntity msgEntity = null;
        List<ChatMsgEntity> msgEntities = new ArrayList();

        while (cursor.moveToNext()) {
            msgEntity = new ChatMsgEntity();
            msgEntity.set_id(cursorGetLong(cursor, "_id"));
            msgEntity.setMessage_ower(cursorGetString(cursor, "MESSAGE_OWER"));
            msgEntity.setMessage_id(cursorGetString(cursor, "MESSAGE_ID"));
            msgEntity.setChatType(cursorGetInt(cursor, "CHAT_TYPE"));
            msgEntity.setMessage_from(cursorGetString(cursor, "MESSAGE_FROM"));
            msgEntity.setMessage_to(cursorGetString(cursor, "MESSAGE_TO"));
            msgEntity.setMessageType(cursorGetInt(cursor, "MESSAGE_TYPE"));
            msgEntity.setContent(cursorGetString(cursor, "CONTENT"));
            msgEntity.setSend_status(cursorGetInt(cursor, "SEND_STATUS"));
            msgEntity.setRead_time(cursorGetLong(cursor, "READ_TIME"));
            msgEntity.setCreatetime(cursorGetLong(cursor, "CREATETIME"));

            msgEntity.setTransStatus(cursorGetInt(cursor, "TRANS_STATUS"));
            msgEntity.setHashid(cursorGetString(cursor, "HASHID"));
            msgEntity.setPayCount(cursorGetInt(cursor, "PAY_COUNT"));
            msgEntity.setCrowdCount(cursorGetInt(cursor, "CROWD_COUNT"));

            String content = msgEntity.getContent();
            if (!TextUtils.isEmpty(content)) {
                msgEntity.setContents(StringUtil.hexStringToBytes(content));
            }
            msgEntities.add(msgEntity);
        }
        if (cursor != null) {
            cursor.close();
        }
        return msgEntities;
    }

    public ChatMsgEntity loadMsgByMsgid(String msgid) {
        QueryBuilder<MessageEntity> queryBuilder = messageEntityDao.queryBuilder();
        queryBuilder.where(MessageEntityDao.Properties.Message_id.eq(msgid)).limit(1).build();
        List<MessageEntity> detailEntities = queryBuilder.list();
        if (detailEntities.size() == 0) {
            return null;
        }

        ChatMsgEntity chatMsgEntity = null;
        try {
            MessageEntity messageEntity = detailEntities.get(0);
            byte[] contents = StringUtil.hexStringToBytes(messageEntity.getContent());

            chatMsgEntity = messageEntity.messageToChatEntity();
            chatMsgEntity.setContents(contents);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return chatMsgEntity;
    }

    public MessageEntity loadMsgLessMsgid(String msgid) {
        QueryBuilder<MessageEntity> queryBuilder = messageEntityDao.queryBuilder();
        queryBuilder.where(MessageEntityDao.Properties.Message_id.le(msgid)).limit(1).build();
        List<MessageEntity> detailEntities = queryBuilder.list();
        if (detailEntities.size() == 0) {
            return null;
        }
        return detailEntities.get(0);
    }

    public MessageEntity loadMsgLastOne(String owner) {
        QueryBuilder<MessageEntity> queryBuilder = messageEntityDao.queryBuilder();
        queryBuilder.where(MessageEntityDao.Properties.Message_ower.eq(owner))
                .orderDesc(MessageEntityDao.Properties.Message_id)
                .limit(1).build();
        List<MessageEntity> detailEntities = queryBuilder.list();
        if (detailEntities.size() == 0) {
            return null;
        }
        return detailEntities.get(0);
    }

    /********************************* add ***********************************/
    public void insertMessageEntity(MessageEntity msgEntity) {
        messageEntityDao.insertOrReplaceInTx(msgEntity);
    }

    public ChatMsgEntity insertMessageEntity(String messageid, String messageowner, int chattype, int messagetype, String from, String to, byte[] contents,String  txtcontent,long createtime, int sendstate) {
        MessageEntity messageEntity = new MessageEntity();
        messageEntity.setMessage_id(messageid);
        messageEntity.setMessage_ower(messageowner);
        messageEntity.setChatType(chattype);
        messageEntity.setMessageType(messagetype);
        messageEntity.setMessage_from(from);
        messageEntity.setMessage_to(to);
        messageEntity.setContent(StringUtil.bytesToHexString(contents));
        messageEntity.setTxtContent(txtcontent);
        messageEntity.setCreatetime(createtime);
        messageEntity.setSend_status(sendstate);
        messageEntity.setRead_time(0L);

        ChatMsgEntity msgExtEntity = messageEntity.messageToChatEntity();
        msgExtEntity.setContents(contents);
        return msgExtEntity;
    }

    public void insertMsgExtEntity(ChatMsgEntity chatMsgEntity) {
        MessageEntity messageEntity = MessageEntity.chatMsgToMessageEntity(chatMsgEntity);
        insertMessageEntity(messageEntity);
    }

    /********************************* delete ***********************************/
    public void deleteRoomMsg(String pukkey) {
        QueryBuilder<MessageEntity> qb = messageEntityDao.queryBuilder();
        DeleteQuery<MessageEntity> bd = qb.where(MessageEntityDao.Properties.Message_ower.eq(pukkey)).buildDelete();
        bd.executeDeleteWithoutDetachingEntities();
    }

    public void deleteMsgByid(String msgid) {
        QueryBuilder<MessageEntity> qb = messageEntityDao.queryBuilder();
        DeleteQuery<MessageEntity> bd = qb.where(MessageEntityDao.Properties.Message_id.eq(msgid)).buildDelete();
        bd.executeDeleteWithoutDetachingEntities();
    }

    public void clearMsgByRoomkey(String roomkey) {
        QueryBuilder<MessageEntity> qb = messageEntityDao.queryBuilder();
        DeleteQuery<MessageEntity> bd = qb.where(MessageEntityDao.Properties.Message_ower.eq(roomkey)).buildDelete();
        bd.executeDeleteWithoutDetachingEntities();
    }

    public void clearChatMsgs() {
        messageEntityDao.deleteAll();
    }

    /********************************* update ***********************************/
    public void updateMsg(MessageEntity msgEntity) {
        messageEntityDao.update(msgEntity);
    }

    public void updateMsg(List<MessageEntity> msgEntities) {
        messageEntityDao.updateInTx(msgEntities);
    }

    public void updateMessageSendState(String messageid, int state) {
        String sql = "UPDATE MESSAGE_ENTITY SET SEND_STATUS = ? WHERE MESSAGE_ID = ? AND SEND_STATUS != 1;";
        daoSession.getDatabase().execSQL(sql, new Object[]{state, messageid});
    }
}
