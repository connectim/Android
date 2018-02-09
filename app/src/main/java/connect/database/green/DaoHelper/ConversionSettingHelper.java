package connect.database.green.DaoHelper;

import org.greenrobot.greendao.query.DeleteQuery;
import org.greenrobot.greendao.query.QueryBuilder;

import java.util.List;

import connect.database.green.BaseDao;
import connect.database.green.bean.ConversionEntity;
import connect.database.green.bean.ConversionSettingEntity;
import connect.database.green.dao.ConversionEntityDao;
import connect.database.green.dao.ConversionSettingEntityDao;

/**
 * Created by gtq on 2016/12/13.
 */
public class ConversionSettingHelper extends BaseDao {

    private static ConversionSettingHelper conversionSettingHelper;
    private ConversionSettingEntityDao conversionSettingEntityDao;

    public ConversionSettingHelper() {
        super();
        conversionSettingEntityDao = daoSession.getConversionSettingEntityDao();
    }

    public synchronized static ConversionSettingHelper getInstance() {
        if (conversionSettingHelper == null) {
            conversionSettingHelper = new ConversionSettingHelper();
        }
        return conversionSettingHelper;
    }

    public static void closeHelper() {
        conversionSettingHelper = null;
    }

    /************************  select *****************************************/
    /**
     * Pub_key query friend
     *
     * @return
     */
    public ConversionSettingEntity loadSetEntity(String pubkey) {
        QueryBuilder<ConversionSettingEntity> queryBuilder = conversionSettingEntityDao.queryBuilder();
        queryBuilder.where(ConversionSettingEntityDao.Properties.Identifier.eq(pubkey)).build();
        List<ConversionSettingEntity> settingEntities = queryBuilder.list();
        return settingEntities.size() == 0 ? null : settingEntities.get(0);
    }

    /************************  update *****************************************/
    public void insertSetEntity(ConversionSettingEntity entity) {
        ConversionSettingEntity setEntity = loadSetEntity(entity.getIdentifier());
        if (setEntity != null) {
            entity.set_id(setEntity.get_id());
        }
        setEntity = entity;
        conversionSettingEntityDao.insertOrReplace(setEntity);
    }

    public void updateBurnTime(String roomkey, long time) {
        ConversionSettingEntity setEntity = loadSetEntity(roomkey);
        if (setEntity == null) {
            setEntity = new ConversionSettingEntity();
            setEntity.setIdentifier(roomkey);
        }
        setEntity.setSnap_time(time);
        insertSetEntity(setEntity);
    }

    public void updateDisturb(String roomkey, int state) {
        ConversionSettingEntity setEntity = loadSetEntity(roomkey);
        if (setEntity == null) {
            setEntity = new ConversionSettingEntity();
            setEntity.setIdentifier(roomkey);
        }
        setEntity.setDisturb(state);
        insertSetEntity(setEntity);
    }


    public void deleteConverstionSet(String roomid) {
        QueryBuilder<ConversionSettingEntity> qb = conversionSettingEntityDao.queryBuilder();
        DeleteQuery<ConversionSettingEntity> bd = qb.where(ConversionSettingEntityDao.Properties.Identifier.eq(roomid)).buildDelete();
        bd.executeDeleteWithoutDetachingEntities();
    }
}
