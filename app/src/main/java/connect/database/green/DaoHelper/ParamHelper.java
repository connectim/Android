package connect.database.green.DaoHelper;

import connect.database.green.BaseDao;
import connect.database.green.bean.ParamEntity;
import connect.database.green.dao.ParamEntityDao;

import org.greenrobot.greendao.query.DeleteQuery;
import org.greenrobot.greendao.query.QueryBuilder;

import java.util.List;

/**
 * save KEY-VALUE data
 * Created by gtq on 2017/1/4.
 */
public class ParamHelper extends BaseDao {
    private static ParamHelper paramHelper;
    private ParamEntityDao paramEntityDao;

    public ParamHelper() {
        super();
        paramEntityDao = daoSession.getParamEntityDao();
    }

    public synchronized static ParamHelper getInstance() {
        if (paramHelper == null) {
            paramHelper = new ParamHelper();
        }
        return paramHelper;
    }

    public static void closeHelper() {
        paramHelper = null;
    }

    /**********************************************************************************************
     *                                      select
     *********************************************************************************************/
    public ParamEntity loadParamEntity(String key) {
        QueryBuilder<ParamEntity> queryBuilder = paramEntityDao.queryBuilder();
        queryBuilder.where(ParamEntityDao.Properties.Key.eq(key)).limit(1).build();
        List<ParamEntity> paramEntities = queryBuilder.list();
        if (null == paramEntities || paramEntities.size() == 0) {
            return null;
        }
        return paramEntities.get(0);
    }

    public List<ParamEntity> likeParamEntities(String key) {
        QueryBuilder<ParamEntity> queryBuilder = paramEntityDao.queryBuilder();
        queryBuilder.where(ParamEntityDao.Properties.Key.like("%" + key + "%")).build();
        return queryBuilder.list();
    }

    public ParamEntity likeParamEntity(String key) {
        QueryBuilder<ParamEntity> queryBuilder = paramEntityDao.queryBuilder();
        queryBuilder.where(ParamEntityDao.Properties.Key.like("%" + key + "%")).
                orderDesc(ParamEntityDao.Properties._id).limit(1).build();
        List<ParamEntity> paramEntities = queryBuilder.list();
        return paramEntities == null || paramEntities.size() == 0 ? null : paramEntities.get(0);
    }

    public ParamEntity likeParamEntityDESC(String key) {
        QueryBuilder<ParamEntity> queryBuilder = paramEntityDao.queryBuilder();
        queryBuilder.where(ParamEntityDao.Properties.Key.like("%" + key + "%")).
                orderDesc(ParamEntityDao.Properties._id).limit(1).build();
        List<ParamEntity> paramEntities = queryBuilder.list();
        return paramEntities == null || paramEntities.size() == 0 ? null : paramEntities.get(0);
    }

    public ParamEntity loadParamEntityKeyExt(String key, String ext) {
        QueryBuilder<ParamEntity> queryBuilder = paramEntityDao.queryBuilder();
        queryBuilder.where(ParamEntityDao.Properties.Key.eq(key), ParamEntityDao.Properties.Ext.eq(ext)).limit(1).build();
        List<ParamEntity> paramEntities = queryBuilder.list();
        if (null == paramEntities || paramEntities.size() == 0) {
            return null;
        }
        return paramEntities.get(0);
    }

    /**********************************************************************************************
     *                                      update
     *********************************************************************************************/
    public void insertParamEntity(ParamEntity entity){
        ParamEntity paramEntity = loadParamEntity(entity.getKey());
        if(paramEntity != null){
            deleteParamEntity(entity.getKey());
        }
        paramEntityDao.insertOrReplace(entity);
    }

    public void insertOrReplaceParamEntity(ParamEntity entity){
        paramEntityDao.insertOrReplace(entity);
    }

    public void updateParamEntities(List<ParamEntity> entities) {
        paramEntityDao.insertOrReplaceInTx(entities);
    }

    /**********************************************************************************************
     *                                      delete
     *********************************************************************************************/
    public void deleteParamEntity(String key) {
        QueryBuilder<ParamEntity> qb = paramEntityDao.queryBuilder();
        DeleteQuery<ParamEntity> bd = qb.where(ParamEntityDao.Properties.Key.eq(key)).buildDelete();
        bd.executeDeleteWithoutDetachingEntities();
    }

    public void deleteLikeParamEntity(String key){
        QueryBuilder<ParamEntity> qb = paramEntityDao.queryBuilder();
        DeleteQuery<ParamEntity> bd = qb.where(ParamEntityDao.Properties.Key.like("%" + key + "%")).buildDelete();
        bd.executeDeleteWithoutDetachingEntities();
    }
}