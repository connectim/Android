package connect.database.green.DaoHelper;

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
    public List<SubscribeDetailEntity> selectAllEntity() {
        List<SubscribeDetailEntity> subscribeDetailEntities = subscribeDetailEntityDao.loadAll();
        if (subscribeDetailEntities == null) {
            subscribeDetailEntities = new ArrayList<>();
        }
        return subscribeDetailEntities;
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
}
