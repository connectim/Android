package connect.database.green.DaoHelper;

import android.database.Cursor;

import org.greenrobot.greendao.query.DeleteQuery;
import org.greenrobot.greendao.query.QueryBuilder;

import java.util.ArrayList;
import java.util.List;

import connect.activity.chat.bean.MsgExtEntity;
import connect.database.green.BaseDao;
import connect.database.green.bean.MessageEntity;
import connect.database.green.dao.MessageEntityDao;
import connect.utils.StringUtil;
import connect.utils.cryption.EncryptionUtil;
import connect.utils.cryption.SupportKeyUril;
import protos.Connect;

/**
 * message detail
 * Created by gtq on 2016/11/23.
 */
public class MessageHelper extends BaseDao {

    private String Tag = "MessageHelper";
    private static MessageHelper messageHelper;
    private MessageEntityDao messageEntityDao;

    public MessageHelper() {
        super();
        messageEntityDao = daoSession.getMessageEntityDao();
    }

    public static MessageHelper getInstance() {
        if (messageHelper == null) {
            synchronized (MessageHelper.class) {
                if (messageHelper == null) {
                    messageHelper = new MessageHelper();
                }
            }
        }
        return messageHelper;
    }

    public static void closeHelper() {
        messageHelper = null;
    }

    /********************************* select ***********************************/
    public List<MsgExtEntity> loadMoreMsgEntities(String pubkey, long firsttime) {
        String sql = "SELECT * FROM (SELECT C.* ,S.STATUS AS TRANS_STATUS,HASHID,PAY_COUNT,CROWD_COUNT FROM MESSAGE_ENTITY C LEFT OUTER JOIN TRANSACTION_ENTITY S ON C.MESSAGE_ID = S.MESSAGE_ID WHERE C.MESSAGE_OWER = ? " +
                ((firsttime == 0) ? "" : " AND C.CREATETIME < " + firsttime) +//load more message
                " ORDER BY C.CREATETIME DESC LIMIT 20) ORDER BY CREATETIME ASC;";

        Cursor cursor = daoSession.getDatabase().rawQuery(sql, new String[]{pubkey});
        MsgExtEntity msgEntity = null;
        List<MsgExtEntity> msgEntities = new ArrayList();
        while (cursor.moveToNext()) {
            msgEntity = new MsgExtEntity();
            msgEntity.set_id(cursorGetLong(cursor, "_id"));
            msgEntity.setMessage_ower(cursorGetString(cursor, "MESSAGE_OWER"));
            msgEntity.setMessage_id(cursorGetString(cursor, "MESSAGE_ID"));
            msgEntity.setChatType(cursorGetInt(cursor, "CHAT_TYPE"));
            msgEntity.setMessage_from(cursorGetString(cursor, "MESSAGE_FROM"));
            msgEntity.setMessage_to(cursorGetString(cursor, "MESSAGE_TO"));
            msgEntity.setMessageType(cursorGetInt(cursor, "MESSAGE_TYPE"));
            msgEntity.setContent(cursorGetString(cursor, "CONTENT"));
            msgEntity.setSnap_time(cursorGetLong(cursor, "SNAP_TIME"));
            msgEntity.setSend_status(cursorGetInt(cursor, "SEND_STATUS"));
            msgEntity.setRead_time(cursorGetLong(cursor, "READ_TIME"));
            msgEntity.setCreatetime(cursorGetLong(cursor, "CREATETIME"));

            msgEntity.setTransStatus(cursorGetInt(cursor, "TRANS_STATUS"));
            msgEntity.setHashid(cursorGetString(cursor, "HASHID"));
            msgEntity.setPayCount(cursorGetInt(cursor, "PAY_COUNT"));
            msgEntity.setCrowdCount(cursorGetInt(cursor, "CROWD_COUNT"));
            msgEntities.add(msgEntity);
        }
        if (cursor != null) {
            cursor.close();
        }
        return msgEntities;
    }

    public MessageEntity loadMsgByMsgid(String msgid) {
        QueryBuilder<MessageEntity> queryBuilder = messageEntityDao.queryBuilder();
        queryBuilder.where(MessageEntityDao.Properties.Message_id.eq(msgid)).limit(1).build();
        List<MessageEntity> detailEntities = queryBuilder.list();
        if (detailEntities.size() == 0) {
            return null;
        }
        return detailEntities.get(0);
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

    /********************************* add ***********************************/
    public void insertMessageEntity(MessageEntity msgEntity) {
        messageEntityDao.insertOrReplace(msgEntity);
    }

    public MsgExtEntity insertMessageEntity(String messageid, String messageowner, int chattype, int messagetype, String from, String to, byte[] contents, long createtime, int sendstate) {
        Connect.GcmData gcmData = EncryptionUtil.encodeAESGCM(SupportKeyUril.EcdhExts.NONE, SupportKeyUril.localHashKey().getBytes(), contents);

        MessageEntity messageEntity = new MessageEntity();
        messageEntity.setMessage_id(messageid);
        messageEntity.setMessage_ower(messageowner);
        messageEntity.setChatType(chattype);
        messageEntity.setMessageType(messagetype);
        messageEntity.setMessage_from(from);
        messageEntity.setMessage_to(to);
        messageEntity.setContent(StringUtil.bytesToHexString(gcmData.toByteArray()));
        messageEntity.setCreatetime(createtime);
        messageEntity.setSend_status(sendstate);
        insertMessageEntity(messageEntity);

        MsgExtEntity msgExtEntity = messageEntity.transToExtEntity();
        msgExtEntity.setContents(contents);
        return msgExtEntity;
    }

    public void insertMsgExtEntity(MsgExtEntity msgExtEntity) {
        MessageEntity messageEntity = msgExtEntity.transToMessageEntity();
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

    public void clearChatMsgs(){
        messageEntityDao.deleteAll();
    }

    /********************************* update ***********************************/
    public void updateMsg(MessageEntity msgEntity) {
        messageEntityDao.update(msgEntity);
    }

    public void updateMsg(List<MessageEntity> msgEntities) {
        messageEntityDao.updateInTx(msgEntities);
    }

    public void updateBurnMsg(String msgid, long time) {
        QueryBuilder<MessageEntity> queryBuilder = messageEntityDao.queryBuilder();
        queryBuilder.where(MessageEntityDao.Properties.Message_id.eq(msgid)).build();
        List<MessageEntity> detailEntities = queryBuilder.list();
        for (MessageEntity entity : detailEntities) {
            entity.setSnap_time(time);
        }
        messageEntityDao.updateInTx(detailEntities);
    }
}
