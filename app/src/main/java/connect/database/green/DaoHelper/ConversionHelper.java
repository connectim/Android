package connect.database.green.DaoHelper;

import android.database.Cursor;
import android.text.TextUtils;

import org.greenrobot.greendao.query.DeleteQuery;
import org.greenrobot.greendao.query.QueryBuilder;

import java.util.ArrayList;
import java.util.List;

import connect.activity.home.bean.RoomAttrBean;
import connect.database.green.BaseDao;
import connect.database.green.bean.ConversionEntity;
import connect.database.green.dao.ConversionEntityDao;

/**
 * Created by gtq on 2016/12/1.
 */
public class ConversionHelper extends BaseDao {
    private static ConversionHelper conversionHelper;
    private ConversionEntityDao conversionEntityDao;

    public ConversionHelper() {
        super();
        conversionEntityDao = daoSession.getConversionEntityDao();
    }

    public synchronized static ConversionHelper getInstance() {
        if (conversionHelper == null) {
            conversionHelper = new ConversionHelper();
        }
        return conversionHelper;
    }

    public static void closeHelper() {
        conversionHelper = null;
    }

    /************************  select *****************************************/
    public int countUnReads() {
        String sql = "SELECT SUM(C.UNREAD_COUNT) AS UNREAD_COUNT FROM CONVERSION_ENTITY C LEFT JOIN CONVERSION_SETTING_ENTITY S ON C.IDENTIFIER = S.IDENTIFIER WHERE C.TYPE !=4 AND (S.DISTURB == 0 OR S.DISTURB IS NULL);";

        int unRead = 0;
        Cursor cursor = daoSession.getDatabase().rawQuery(sql, null);
        while (cursor.moveToNext()) {
            unRead += cursorGetInt(cursor, "UNREAD_COUNT");
        }
        if (cursor != null) {
            cursor.close();
        }
        return unRead;
    }

    public int countUnReadAt() {
        String sql = "SELECT SUM(C.UNREAD_AT) AS UNREAD_AT FROM CONVERSION_ENTITY C;";

        int unRead = 0;
        Cursor cursor = daoSession.getDatabase().rawQuery(sql, null);
        while (cursor.moveToNext()) {
            unRead += cursorGetInt(cursor, "UNREAD_AT");
        }
        if (cursor != null) {
            cursor.close();
        }
        return unRead;
    }

    public int countUnReadAttention() {
        String sql = "SELECT SUM(C.UNREAD_ATTENTION) AS UNREAD_ATTENTION FROM CONVERSION_ENTITY C;";

        int unRead = 0;
        Cursor cursor = daoSession.getDatabase().rawQuery(sql, null);
        while (cursor.moveToNext()) {
            unRead += cursorGetInt(cursor, "UNREAD_ATTENTION");
        }
        if (cursor != null) {
            cursor.close();
        }
        return unRead;
    }

    /**
     * chat message list
     *
     * @return
     */
    public List<RoomAttrBean> loadRoomEntites() {
        return loadRoomEntities("");
    }

    public List<RoomAttrBean> loadRoomEntities(String identifier) {
        if (TextUtils.isEmpty(identifier)) {
            identifier = "";
        }

        String sql = "SELECT R.*, S.DISTURB FROM CONVERSION_ENTITY R " +
                " LEFT OUTER JOIN CONVERSION_SETTING_ENTITY S ON R.IDENTIFIER = S.IDENTIFIER " +
                (TextUtils.isEmpty(identifier) ? "" : " WHERE R.IDENTIFIER = " + "\"" + identifier + "\"") +
                " GROUP BY R.IDENTIFIER ORDER BY IFNULL(R.TOP, 0) DESC,IFNULL(R.LAST_TIME, 0) DESC;";
        Cursor cursor = daoSession.getDatabase().rawQuery(sql, null);

        List<RoomAttrBean> attrBeanList = new ArrayList<>();
        while (cursor.moveToNext()) {
            RoomAttrBean attrBean = new RoomAttrBean();
            attrBean.setRoomid(cursorGetString(cursor, "IDENTIFIER"));
            attrBean.setRoomtype(cursorGetInt(cursor, "TYPE"));
            attrBean.setDisturb(cursorGetInt(cursor, "DISTURB"));
            attrBean.setTop(cursorGetInt(cursor, "TOP"));
            attrBean.setTimestamp(cursorGetLong(cursor, "LAST_TIME"));
            attrBean.setDraft(cursorGetString(cursor, "DRAFT"));
            attrBean.setContent(cursorGetString(cursor, "CONTENT"));
            attrBean.setName(cursorGetString(cursor, "NAME"));
            attrBean.setAvatar(cursorGetString(cursor, "AVATAR"));
            attrBean.setUnread(cursorGetInt(cursor, "UNREAD_COUNT"));
            attrBean.setUnreadAt(cursorGetInt(cursor, "UNREAD_AT"));
            attrBean.setUnreadAttention(cursorGetInt(cursor, "UNREAD_ATTENTION"));
            attrBeanList.add(attrBean);
        }
        if (cursor != null) {
            cursor.close();
        }
        return attrBeanList;
    }

    /**
     * last 10 chat reocreds
     *
     * @return
     */
    public List<RoomAttrBean> loadRecentRoomEntities() {
        String sql = "SELECT R.*, S.DISTURB FROM CONVERSION_ENTITY R \n" +
                " LEFT OUTER JOIN CONVERSION_SETTING_ENTITY S ON R.IDENTIFIER = S.IDENTIFIER \n" +
                " WHERE R.TYPE != 2 ORDER BY R.TOP DESC,R.LAST_TIME DESC LIMIT 10;";
        Cursor cursor = daoSession.getDatabase().rawQuery(sql, null);

        RoomAttrBean attrBean = null;
        List<RoomAttrBean> attrBeanList = new ArrayList<>();
        while (cursor.moveToNext()) {
            attrBean = new RoomAttrBean();
            attrBean.setRoomid(cursorGetString(cursor, "IDENTIFIER"));
            attrBean.setRoomtype(cursorGetInt(cursor, "TYPE"));
            attrBean.setDisturb(cursorGetInt(cursor, "DISTURB"));
            attrBean.setTop(cursorGetInt(cursor, "TOP"));
            attrBean.setTimestamp(cursorGetLong(cursor, "LAST_TIME"));
            attrBean.setDraft(cursorGetString(cursor, "DRAFT"));
            attrBean.setContent(cursorGetString(cursor, "CONTENT"));
            attrBean.setName(cursorGetString(cursor, "NAME"));
            attrBean.setAvatar(cursorGetString(cursor, "AVATAR"));
            attrBean.setUnread(cursorGetInt(cursor, "UNREAD_COUNT"));
            attrBean.setUnreadAt(cursorGetInt(cursor, "UNREAD_AT"));
            attrBean.setUnreadAttention(cursorGetInt(cursor, "UNREAD_ATTENTION"));
            attrBeanList.add(attrBean);
        }
        if (cursor != null) {
            cursor.close();
        }
        return attrBeanList;
    }

    public ConversionEntity loadRoomEnitity(String roomid) {
        QueryBuilder<ConversionEntity> queryBuilder = conversionEntityDao.queryBuilder();
        queryBuilder.where(ConversionEntityDao.Properties.Identifier.eq(roomid)).limit(1).build();
        List<ConversionEntity> roomEntities = queryBuilder.list();
        return (roomEntities == null || roomEntities.size() == 0) ? null : roomEntities.get(0);
    }

    /************************ add *****************************************/
    public void insertRoomEntity(ConversionEntity entity) {
        conversionEntityDao.insertOrReplace(entity);
    }

    /************************ update *****************************************/
    public void updateRoomEntity(ConversionEntity entity) {
        conversionEntityDao.update(entity);
    }

    public void updateRoomEntity(String identify, String draf, String content, long messagetime) {
        updateRoomEntity(identify, draf, content, 0, 0, messagetime);
    }

    public void updateRoomEntity(String identify, String draf, String content, int unRead, int isAt, long messagetime) {
        updateRoomEntity(identify, "","", draf, content, unRead, isAt, messagetime,0);
    }

    public void updateRoomEntity(String identify, String roomName,String avatar, String draf, String content, int unRead, int unAt, long messagetime,int isAttention) {
        String sql = "UPDATE CONVERSION_ENTITY SET DRAFT = ? ,UNREAD_COUNT = ? ,UNREAD_AT = ? ,UNREAD_ATTENTION = ? " +
                (TextUtils.isEmpty(avatar) ? " " : ", AVATAR = \"" + avatar + "\"") +
                (TextUtils.isEmpty(content) ? " " : ", CONTENT = \"" + content + "\"") +
                (messagetime == 0 ? " " : ", LAST_TIME = " + messagetime) +
                (TextUtils.isEmpty(roomName) ? "" : ", NAME = \"" + roomName + "\" ") +
                " WHERE IDENTIFIER = ?;";
        daoSession.getDatabase().execSQL(sql, new Object[]{draf, unRead, unAt,isAttention, identify});
    }

    public void updateRoomEntityName(String identify, String name) {
        String sql = "UPDATE CONVERSION_ENTITY SET NAME = ?  WHERE IDENTIFIER = ?;";
        daoSession.getDatabase().execSQL(sql, new Object[]{name, identify});
    }

    public void updateRoomEntityAvatar(String identify, String avatar) {
        String sql = "UPDATE CONVERSION_ENTITY SET AVATAR = ?  WHERE IDENTIFIER = ?;";
        daoSession.getDatabase().execSQL(sql, new Object[]{avatar, identify});
    }

    /************************ delete *****************************************/
    /**
     * remove room
     * @param roomid
     */
    public void deleteRoom(String roomid) {
        QueryBuilder<ConversionEntity> qb = conversionEntityDao.queryBuilder();
        DeleteQuery<ConversionEntity> bd = qb.where(ConversionEntityDao.Properties.Identifier.eq(roomid)).buildDelete();
        bd.executeDeleteWithoutDetachingEntities();
    }

    /**
     * clear room
     */
    public void clearRooms(){
        conversionEntityDao.deleteAll();
    }
}
