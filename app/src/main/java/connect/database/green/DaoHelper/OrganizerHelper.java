package connect.database.green.DaoHelper;

import org.greenrobot.greendao.query.DeleteQuery;
import org.greenrobot.greendao.query.QueryBuilder;

import java.util.ArrayList;
import java.util.List;

import connect.database.green.BaseDao;
import connect.database.green.bean.OrganizerEntity;
import connect.database.green.dao.OrganizerEntityDao;

/**
 * Created by PuJin on 2018/1/30.
 */

public class OrganizerHelper extends BaseDao {

    private static OrganizerHelper organizerHelper;
    private OrganizerEntityDao organizerEntityDao;

    public OrganizerHelper() {
        super();
        organizerEntityDao = daoSession.getOrganizerEntityDao();
    }

    public synchronized static OrganizerHelper getInstance() {
        if (organizerHelper == null) {
            organizerHelper = new OrganizerHelper();
        }
        return organizerHelper;
    }

    public static void closeHelper() {
        organizerHelper = null;
    }

    public List<OrganizerEntity> loadParamEntityByUpperId(long id) {
        QueryBuilder<OrganizerEntity> queryBuilder = organizerEntityDao.queryBuilder();
        queryBuilder.where(OrganizerEntityDao.Properties.UpperId.eq(id)).build();
        List<OrganizerEntity> organizerEntities = queryBuilder.list();
        if (null == organizerEntities || organizerEntities.size() == 0) {
            organizerEntities = new ArrayList<>();
        }
        return organizerEntities;
    }

    public void insertOrganizerEntities(List<OrganizerEntity> organizerEntities) {
        organizerEntityDao.insertOrReplaceInTx(organizerEntities);
    }

    public void removeOrganizerEntityByUpperId(long id) {
        QueryBuilder<OrganizerEntity> qb = organizerEntityDao.queryBuilder();
        DeleteQuery<OrganizerEntity> bd = qb.where(OrganizerEntityDao.Properties.UpperId.eq(id)).buildDelete();
        bd.executeDeleteWithoutDetachingEntities();
    }
}
