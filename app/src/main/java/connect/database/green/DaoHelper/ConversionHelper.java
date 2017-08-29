package connect.database.green.DaoHelper;

import android.database.Cursor;
import android.text.TextUtils;

import org.greenrobot.greendao.query.DeleteQuery;
import org.greenrobot.greendao.query.QueryBuilder;

import java.util.ArrayList;
import java.util.List;

import connect.database.green.BaseDao;
import connect.database.green.bean.ConversionEntity;
import connect.activity.home.bean.RoomAttrBean;
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

    public static ConversionHelper getInstance() {
        if (conversionHelper == null) {
            conversionHelper = new ConversionHelper();
        }
        return conversionHelper;
    }

    public static void closeHelper() {
        conversionHelper = null;
    }

    /************************  select *****************************************/
    /**
     * chat message list
     *
     * @return
     */
    public List<RoomAttrBean> loadRoomEntites() {
        return loadRoomEntities("");
    }

    public List<RoomAttrBean> loadRoomEntities(String publicKey) {
        String sql = "SELECT R.*, S.DISTURB FROM CONVERSION_ENTITY R " +
                " LEFT OUTER JOIN CONVERSION_SETTING_ENTITY S ON R.IDENTIFIER = S.IDENTIFIER " +
                (TextUtils.isEmpty(publicKey) ? "" : " WHERE R.IDENTIFIER = " + publicKey) +
                " GROUP BY R.IDENTIFIER ORDER BY IFNULL(R.TOP, 0) DESC,IFNULL(R.LAST_TIME, 0) DESC;";
        Cursor cursor = daoSession.getDatabase().rawQuery(sql, null);

        List<RoomAttrBean> attrBeanList = new ArrayList<>();
        while (cursor.moveToNext()) {
            RoomAttrBean attrBean = new RoomAttrBean();
            attrBean.setRoomid(cursorGetString(cursor, "IDENTIFIER"));
            attrBean.setRoomtype(cursorGetInt(cursor, "TYPE"));
            attrBean.setDisturb(cursorGetInt(cursor, "DISTURB"));
            attrBean.setStranger(cursorGetInt(cursor, "STRANGER"));
            attrBean.setTop(cursorGetInt(cursor, "TOP"));
            attrBean.setUnread(cursorGetInt(cursor, "UNREAD_COUNT"));
            attrBean.setTimestamp(cursorGetLong(cursor, "LAST_TIME"));
            attrBean.setDraft(cursorGetString(cursor, "DRAFT"));
            attrBean.setContent(cursorGetString(cursor, "CONTENT"));
            attrBean.setName(cursorGetString(cursor, "NAME"));
            attrBean.setAvatar(cursorGetString(cursor, "AVATAR"));
            attrBean.setAt(cursorGetInt(cursor, "NOTICE"));
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
                " WHERE R.TYPE != 2 and (R.STRANGER IS NULL OR R.STRANGER = 0) ORDER BY R.TOP DESC,R.LAST_TIME DESC LIMIT 10;";
        Cursor cursor = daoSession.getDatabase().rawQuery(sql, null);

        RoomAttrBean attrBean = null;
        List<RoomAttrBean> attrBeanList = new ArrayList<>();
        while (cursor.moveToNext()) {
            attrBean = new RoomAttrBean();
            attrBean.setRoomid(cursorGetString(cursor, "IDENTIFIER"));
            attrBean.setRoomtype(cursorGetInt(cursor, "TYPE"));
            attrBean.setDisturb(cursorGetInt(cursor, "DISTURB"));
            attrBean.setStranger(cursorGetInt(cursor, "STRANGER"));
            attrBean.setTop(cursorGetInt(cursor, "TOP"));
            attrBean.setUnread(cursorGetInt(cursor, "UNREAD_COUNT"));
            attrBean.setTimestamp(cursorGetLong(cursor, "LAST_TIME"));
            attrBean.setDraft(cursorGetString(cursor, "DRAFT"));
            attrBean.setContent(cursorGetString(cursor, "CONTENT"));
            attrBean.setName(cursorGetString(cursor, "NAME"));
            attrBean.setAvatar(cursorGetString(cursor, "AVATAR"));
            attrBean.setAt(cursorGetInt(cursor, "NOTICE"));
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
