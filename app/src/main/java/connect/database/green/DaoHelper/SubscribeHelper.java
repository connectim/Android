package connect.database.green.DaoHelper;

import android.database.Cursor;

import org.greenrobot.greendao.query.DeleteQuery;
import org.greenrobot.greendao.query.QueryBuilder;

import java.util.ArrayList;
import java.util.List;

import connect.database.green.BaseDao;
import connect.database.green.bean.ContactEntity;
import connect.database.green.bean.SubscribeEntity;
import connect.database.green.dao.ContactEntityDao;
import connect.database.green.dao.SubscribeEntityDao;

/**
 * Created by Administrator on 2017/11/22.
 */

public class SubscribeHelper extends BaseDao {

    public static SubscribeHelper subscribeHelper = getInstance();
    private SubscribeEntityDao subscribeEntityDao;

    public SubscribeHelper() {
        super();
        subscribeEntityDao = daoSession.getSubscribeEntityDao();
    }

    private synchronized static SubscribeHelper getInstance() {
        if (subscribeHelper == null) {
            subscribeHelper = new SubscribeHelper();
        }
        return subscribeHelper;
    }

    public static void closeHelper() {
        subscribeHelper = null;
    }

    /********************************* select ***********************************/
    public List<SubscribeEntity> selectAllEntity() {
        List<SubscribeEntity> subscribeEntities = subscribeEntityDao.loadAll();
        if (subscribeEntities == null) {
            subscribeEntities = new ArrayList<>();
        }
        return subscribeEntities;
    }


    /********************************* Insert ***********************************/
    public void insertSubscribeEntity(SubscribeEntity subscribeEntity) {
        subscribeEntityDao.insert(subscribeEntity);
    }

    /********************************* Delete ***********************************/
    public void removeSubscribeEntity(long rssId) {
        QueryBuilder<SubscribeEntity> qb = subscribeEntityDao.queryBuilder();
        DeleteQuery<SubscribeEntity> bd = qb.where(SubscribeEntityDao.Properties.RssId.eq(rssId))
                .buildDelete();
        bd.executeDeleteWithoutDetachingEntities();
    }
}
