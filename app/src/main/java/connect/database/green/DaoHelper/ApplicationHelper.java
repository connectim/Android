package connect.database.green.DaoHelper;

import org.greenrobot.greendao.query.DeleteQuery;
import org.greenrobot.greendao.query.QueryBuilder;

import java.util.ArrayList;
import java.util.List;

import connect.activity.workbench.data.MenuBean;
import connect.activity.workbench.data.MenuData;
import connect.database.green.BaseDao;
import connect.database.green.bean.ApplicationEntity;
import connect.database.green.dao.ApplicationEntityDao;

/**
 * Created by Administrator on 2018/1/30 0030.
 */

public class ApplicationHelper extends BaseDao {

    private static ApplicationHelper applicationHelper;
    private ApplicationEntityDao applicationEntityDao;

    public ApplicationHelper() {
        super();
        applicationEntityDao = daoSession.getApplicationEntityDao();
    }

    public static ApplicationHelper getInstance() {
        if (applicationHelper == null) {
            synchronized (ApplicationHelper.class) {
                if (applicationHelper == null) {
                    applicationHelper = new ApplicationHelper();
                }
            }
        }
        return applicationHelper;
    }

    public static void closeHelper() {
        applicationHelper = null;
    }

    public ArrayList<MenuBean> loadApplicationEntity(int category) {
        QueryBuilder<ApplicationEntity> queryBuilder = applicationEntityDao.queryBuilder();
        queryBuilder.where(ApplicationEntityDao.Properties.Category.eq(category)).build();
        List<ApplicationEntity> applicationEntities = queryBuilder.list();

        ArrayList<MenuBean> listData = new ArrayList<>();
        if (null == applicationEntities || applicationEntities.size() == 0) {
            return listData;
        }else{
            for(ApplicationEntity applicationEntity : applicationEntities){
                MenuBean menuBean = MenuData.getInstance().getData(applicationEntity.getCode());
                if(menuBean != null){
                    listData.add(menuBean);
                }
            }
            return listData;
        }
    }

    public void insertAppEntityList(List<ApplicationEntity> list) {
        if(list != null && list.size() > 0){
            for(ApplicationEntity applicationEntity : list){
                insertApplicationEntity(applicationEntity);
            }
        }
    }

    public void insertApplicationEntity(ApplicationEntity entity) {
        QueryBuilder<ApplicationEntity> qb = applicationEntityDao.queryBuilder();
        qb.where(ApplicationEntityDao.Properties.Code.eq(entity.getCode()));
        List<ApplicationEntity> list = qb.list();
        if (list.size() > 0) {
            deleteApplicationEntity(entity.getCode());
        }
        applicationEntityDao.insertOrReplace(entity);
    }

    public void deleteApplicationEntity(String code) {
        QueryBuilder<ApplicationEntity> qb = applicationEntityDao.queryBuilder();
        DeleteQuery<ApplicationEntity> bd = qb.where(ApplicationEntityDao.Properties.Code.eq(code)).buildDelete();
        bd.executeDeleteWithoutDetachingEntities();
    }

}
