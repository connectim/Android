package connect.database.green.DaoHelper;

import org.greenrobot.greendao.query.DeleteQuery;
import org.greenrobot.greendao.query.QueryBuilder;

import java.util.ArrayList;
import java.util.List;

import connect.database.green.BaseDao;
import connect.database.green.bean.ContactEntity;
import connect.database.green.bean.SubscribeConversationEntity;
import connect.database.green.dao.ContactEntityDao;
import connect.database.green.dao.SubscribeConversationEntityDao;
import connect.database.green.dao.SubscribeEntityDao;

/**
 * Created by Administrator on 2017/11/22.
 */

public class SubscribeConversationHelper  extends BaseDao {

    public static SubscribeConversationHelper subscribeConversationHelper = getInstance();
    private SubscribeConversationEntityDao subscribeConversationEntityDao;

    public SubscribeConversationHelper() {
        super();
        subscribeConversationEntityDao = daoSession.getSubscribeConversationEntityDao();
    }

    private synchronized static SubscribeConversationHelper getInstance() {
        if (subscribeConversationHelper == null) {
            subscribeConversationHelper = new SubscribeConversationHelper();
        }
        return subscribeConversationHelper;
    }

    public static void closeHelper() {
        subscribeConversationHelper = null;
    }

    /********************************* select ***********************************/
    public List<SubscribeConversationEntity> selectAllEntity() {
        List<SubscribeConversationEntity> subscribeEntities = subscribeConversationEntityDao.loadAll();
        if (subscribeEntities == null) {
            subscribeEntities = new ArrayList<>();
        }
        return subscribeEntities;
    }

    public List<SubscribeConversationEntity> loadSubscribeConversationEntities(long rssId) {
        QueryBuilder<SubscribeConversationEntity> queryBuilder = subscribeConversationEntityDao.queryBuilder();
        queryBuilder.where(SubscribeConversationEntityDao.Properties.RssId.eq(rssId))
                .limit(1)
                .build();
        List<SubscribeConversationEntity> conversationEntities = queryBuilder.list();
        return conversationEntities;
    }

    /********************************* Insert ***********************************/
    public void insertConversationEntity(SubscribeConversationEntity conversationEntity) {
        subscribeConversationEntityDao.insert(conversationEntity);
    }

    /********************************* Delete ***********************************/
    public void removeConversationEntity(long rssId) {
        QueryBuilder<SubscribeConversationEntity> qb = subscribeConversationEntityDao.queryBuilder();
        DeleteQuery<SubscribeConversationEntity> bd = qb.where(SubscribeEntityDao.Properties.RssId.eq(rssId))
                .buildDelete();
        bd.executeDeleteWithoutDetachingEntities();
    }

    /********************************* Update ***********************************/
    public void updataConversationEntity(long rssId, String content, long time, int unread) {
        String sql = "UPDATE SUBSCRIBE_CONVERSATION_ENTITY SET CONTENT = ?, TIME = ?, UN_READ = ? WHERE RSS_ID = ? ;";
        daoSession.getDatabase().execSQL(sql, new Object[]{content, time, unread, rssId});
    }
}
