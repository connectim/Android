package connect.database.green.DaoHelper;

import org.greenrobot.greendao.query.QueryBuilder;

import connect.database.green.BaseDao;
import connect.database.green.bean.ConversionSettingEntity;
import connect.database.green.bean.ConversionSettingEntityDao;

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

    public static ConversionSettingHelper getInstance() {
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
     * @return
     */
    public ConversionSettingEntity loadSetEntity(String pubkey) {
        QueryBuilder<ConversionSettingEntity> queryBuilder = conversionSettingEntityDao.queryBuilder();
        queryBuilder.where(ConversionSettingEntityDao.Properties.Identifier.eq(pubkey)).build();
        return queryBuilder.unique();
    }

    /************************  update *****************************************/
    public void insertSetEntity(ConversionSettingEntity entity) {
        conversionSettingEntityDao.insertOrReplace(entity);
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
}
