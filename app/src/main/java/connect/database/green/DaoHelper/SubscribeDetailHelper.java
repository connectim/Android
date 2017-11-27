package connect.database.green.DaoHelper;

import android.database.Cursor;

import org.greenrobot.greendao.query.QueryBuilder;

import java.util.ArrayList;
import java.util.List;

import connect.database.green.BaseDao;
import connect.database.green.bean.SubscribeDetailEntity;
import connect.database.green.dao.SubscribeDetailEntityDao;

/**
 * Created by Administrator on 2017/11/22.
 */
public class SubscribeDetailHelper extends BaseDao {

    public static SubscribeDetailHelper subscribeDetailHelper = getInstance();
    private SubscribeDetailEntityDao subscribeDetailEntityDao;

    public SubscribeDetailHelper() {
        super();
        subscribeDetailEntityDao = daoSession.getSubscribeDetailEntityDao();
    }

    private synchronized static SubscribeDetailHelper getInstance() {
        if (subscribeDetailHelper == null) {
            subscribeDetailHelper = new SubscribeDetailHelper();
        }
        return subscribeDetailHelper;
    }

    public static void closeHelper() {
        subscribeDetailHelper = null;
    }

    /********************************* select ***********************************/
    public int countUnReads() {
        String sql = "SELECT SUM(S.UNREAD) AS UNREAD FROM SubscribeDetailEntityDao S;";

        int unRead = 0;
        Cursor cursor = daoSession.getDatabase().rawQuery(sql, null);
        while (cursor.moveToNext()) {
            unRead += cursorGetInt(cursor, "UNREAD");
        }
        if (cursor != null) {
            cursor.close();
        }
        return unRead;
    }

    public List<SubscribeDetailEntity> selectAllEntity() {
        List<SubscribeDetailEntity> subscribeDetailEntities = subscribeDetailEntityDao.loadAll();
        if (subscribeDetailEntities == null) {
            subscribeDetailEntities = new ArrayList<>();
        }
        return subscribeDetailEntities;
    }

    public boolean selectLastSubscribeDetailEntityUnRead(Long rssId) {
        QueryBuilder<SubscribeDetailEntity> queryBuilder = subscribeDetailEntityDao.queryBuilder();
        queryBuilder.where(SubscribeDetailEntityDao.Properties.RssId.eq(rssId),
                SubscribeDetailEntityDao.Properties.Unread.eq(1)).build();
        List<SubscribeDetailEntity> subscribeDetailEntities = queryBuilder.list();
        if(subscribeDetailEntities != null && subscribeDetailEntities.size() > 0){
            return true;
        }else{
            return false;
        }
    }

    public List<SubscribeDetailEntity> selectLastSubscribeDetailEntity(Long rssId) {
        QueryBuilder<SubscribeDetailEntity> queryBuilder = subscribeDetailEntityDao.queryBuilder();
        queryBuilder.where(SubscribeDetailEntityDao.Properties.RssId.eq(rssId))
                .limit(20)
                .orderDesc(SubscribeDetailEntityDao.Properties._id)
                .build();
        List<SubscribeDetailEntity> subscribeDetailEntities = queryBuilder.list();
        return subscribeDetailEntities;
    }

    public List<SubscribeDetailEntity> selectLastSubscribeDetailEntity(Long rssId, long messageId) {
        QueryBuilder<SubscribeDetailEntity> queryBuilder = subscribeDetailEntityDao.queryBuilder();
        queryBuilder.where(SubscribeDetailEntityDao.Properties.RssId.eq(rssId),
                SubscribeDetailEntityDao.Properties.MessageId.lt(messageId))
                .limit(12)
                .orderDesc(SubscribeDetailEntityDao.Properties._id)
                .build();
        List<SubscribeDetailEntity> subscribeDetailEntities = queryBuilder.list();
        return subscribeDetailEntities;
    }

    /********************************* Insert ***********************************/
    public void insertSubscribeEntity(SubscribeDetailEntity subscribeDetailEntity) {
        subscribeDetailEntityDao.insert(subscribeDetailEntity);
    }

    public void insertSubscribeEntities(List<SubscribeDetailEntity> subscribeDetailEntities) {
        subscribeDetailEntityDao.insertOrReplaceInTx(subscribeDetailEntities);
    }

    /********************************* Delete ***********************************/

    public void updateSubscribeMessageRead(Long rssId){
        QueryBuilder<SubscribeDetailEntity> queryBuilder = subscribeDetailEntityDao.queryBuilder();
        queryBuilder.where(SubscribeDetailEntityDao.Properties.RssId.eq(rssId), SubscribeDetailEntityDao.Properties.Unread.eq(1))
                .orderDesc(SubscribeDetailEntityDao.Properties._id)
                .build();
        List<SubscribeDetailEntity> list = queryBuilder.list();
        for(SubscribeDetailEntity entity : list){
            entity.setUnread(0);
            subscribeDetailEntityDao.update(entity);
        }
    }

}
