package connect.db.green.DaoHelper;

import android.database.Cursor;

import com.google.gson.Gson;

import org.greenrobot.greendao.query.DeleteQuery;
import org.greenrobot.greendao.query.QueryBuilder;

import java.util.ArrayList;
import java.util.List;

import connect.db.green.BaseDao;
import connect.db.green.bean.MessageEntity;
import connect.db.green.bean.MessageEntityDao;
import connect.ui.activity.chat.bean.MessageExtEntity;
import connect.ui.activity.chat.bean.MsgDefinBean;
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
    public List<MessageExtEntity> loadMoreMsgEntities(String pubkey, long firsttime) {
        String sql = "SELECT * FROM (SELECT C.* ,S.STATUS AS TRANS_STATUS,HASHID,PAY_COUNT,CROWD_COUNT FROM MESSAGE_ENTITY C LEFT OUTER JOIN TRANSACTION_ENTITY S ON C.MESSAGE_ID = S.MESSAGE_ID WHERE C.MESSAGE_OWER = ? " +
                ((firsttime == 0) ? "" : " AND C.CREATETIME < " + firsttime) +//load more message
                " ORDER BY C.CREATETIME DESC LIMIT 20) ORDER BY CREATETIME ASC;";

        Cursor cursor = daoSession.getDatabase().rawQuery(sql, new String[]{pubkey});
        MessageExtEntity msgEntity = null;
        List<MessageExtEntity> msgEntities = new ArrayList();
        while (cursor.moveToNext()) {
            msgEntity = new MessageExtEntity();
            msgEntity.setMessage_ower(cursorGetString(cursor, "MESSAGE_OWER"));
            msgEntity.setMessage_id(cursorGetString(cursor, "MESSAGE_ID"));
            msgEntity.setContent(cursorGetString(cursor, "CONTENT"));
            msgEntity.setCreatetime(cursorGetLong(cursor, "CREATETIME"));
            msgEntity.setSnap_time(cursorGetLong(cursor, "SNAP_TIME"));
            msgEntity.setSend_status(cursorGetInt(cursor, "SEND_STATUS"));
            msgEntity.setState(cursorGetInt(cursor, "STATE"));
            msgEntity.setRead_time(cursorGetLong(cursor, "READ_TIME"));
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

    /********************************* add ***********************************/
    public void insertMsg(MessageEntity msgEntity) {
        messageEntityDao.insertOrReplace(msgEntity);
    }

    public void insertMsg(List<MessageEntity> msgEntities) {
        messageEntityDao.insertOrReplaceInTx(msgEntities);
    }

    public void insertFromMsg(String roomid, MsgDefinBean bean) {
        insertMsg(roomid, bean, 1);
    }

    public void insertToMsg(MsgDefinBean bean) {
        insertMsg(bean.getPublicKey(), bean, 1);
    }


    public void insertMsg(String roomid, MsgDefinBean bean, int sendstate) {
        String content = new Gson().toJson(bean);
        Connect.GcmData gcmData = EncryptionUtil.encodeAESGCM(SupportKeyUril.EcdhExts.NONE, SupportKeyUril.localHashKey().getBytes(), content.getBytes());

        MessageEntity detailEntity = new MessageEntity();
        detailEntity.setMessage_ower(roomid);
        detailEntity.setMessage_id(bean.getMessage_id());
        detailEntity.setContent(StringUtil.bytesToHexString(gcmData.toByteArray()));
        detailEntity.setSend_status(sendstate);
        detailEntity.setCreatetime(bean.getSendtime());

        insertMsg(detailEntity);
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

    public void updateMsgState(String msgid, int state) {
        QueryBuilder<MessageEntity> queryBuilder = messageEntityDao.queryBuilder();
        queryBuilder.where(MessageEntityDao.Properties.Message_id.eq(msgid)).build();
        List<MessageEntity> detailEntities = queryBuilder.list();
        for (MessageEntity entity : detailEntities) {
            entity.setState(state);
        }
        messageEntityDao.updateInTx(detailEntities);
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
