package connect.database.green;

import android.content.Context;

import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.query.QueryBuilder;

import connect.activity.base.BaseApplication;
import connect.activity.home.bean.HomeAction;
import connect.activity.login.bean.UserBean;
import connect.database.SharedPreferenceUtil;
import connect.database.green.DaoHelper.ApplicationHelper;
import connect.database.green.DaoHelper.ContactHelper;
import connect.database.green.DaoHelper.ConversionHelper;
import connect.database.green.DaoHelper.ConversionSettingHelper;
import connect.database.green.DaoHelper.MessageHelper;
import connect.database.green.DaoHelper.MigrateOpenHelper;
import connect.database.green.DaoHelper.OrganizerHelper;
import connect.database.green.DaoHelper.ParamHelper;
import connect.database.green.DaoHelper.TransactionHelper;
import connect.database.green.dao.DaoMaster;
import connect.database.green.dao.DaoSession;
import connect.utils.ConfigUtil;
import connect.utils.StringUtil;
import instant.utils.SharedUtil;

/**
 * The management of the database
 * Created by gtq on 2016/11/22.
 */
public class DaoManager {

    private static final String TAG = "_DaoManager";

    private volatile static DaoManager mDaoManager;
    private static DaoMaster.DevOpenHelper mHelper;
    private static DaoMaster mDaoMaster;
    private static DaoSession mDaoSession;

    public synchronized static DaoManager getInstance() {
        if (mDaoManager == null) {
            mDaoManager = new DaoManager();
        }
        return mDaoManager;
    }

    /**
     * Determine the existence of the database , if not  create it
     *
     * @return
     */
    public synchronized DaoMaster getDaoMaster(){
        if (null == mDaoMaster) {
            String DB_NAME = null;
            String DB_PWD = null;

            UserBean userBean = SharedPreferenceUtil.getInstance().getUser();
            if (userBean == null) {
                DB_NAME = "connect_exception.db";
                DB_PWD = "connect_exception_pwd";
            } else {
                String uid = userBean.getUid();
                DB_NAME = "connect_" + StringUtil.bytesToHexString(StringUtil.digest(StringUtil.MD5,
                        StringUtil.hexStringToBytes(uid))) + ".db";
                DB_PWD = "connect_" + StringUtil.bytesToHexString(StringUtil.digest(StringUtil.SHA_256,
                        StringUtil.hexStringToBytes(uid)));
            }

            Context context = BaseApplication.getInstance().getBaseContext();
            MigrateOpenHelper helper = new MigrateOpenHelper(
                    context,
                    DB_NAME,
                    null);

            Database db = null;
            boolean appMode = ConfigUtil.getInstance().appMode();
            if (appMode) {//release version
                setDebug(false);//log
                try {
                    db = helper.getEncryptedWritableDb(DB_PWD);
                } catch (Exception e) {
                    e.printStackTrace();
                    context.deleteDatabase(DB_NAME);
                    SharedUtil.getInstance().deleteUserInfo();
                    HomeAction.getInstance().sendEvent(HomeAction.HomeType.DELAY_EXIT);
                    return null;
                }
            } else {//debug version
                setDebug(true);
                db = helper.getWritableDb();
            }
            mDaoMaster = new DaoMaster(db);
        }
        return mDaoMaster;
    }

    public DaoSession getDaoSession() {
        if (null == mDaoSession) {
            if (null == mDaoMaster) {
                mDaoMaster = getDaoMaster();
            }
            mDaoSession = mDaoMaster.newSession();
        }
        return mDaoSession;
    }

    public void setDebug(boolean flag) {
        QueryBuilder.LOG_SQL = flag;
        QueryBuilder.LOG_VALUES = flag;
    }

    public void closeDataBase() {
        closeHelper();
        closeDaoSession();
    }

    public void closeDaoSession() {
        try {
            if (null != mDaoSession) {
                mDaoSession.getDatabase().close();
                mDaoSession.clear();
                mDaoSession = null;
            }

            if (null != mDaoMaster) {
                mDaoMaster.getDatabase().close();
                mDaoMaster = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deleteDataBase() {
        UserBean userBean = SharedPreferenceUtil.getInstance().getUser();
        String dataBaseName = "connect_" + StringUtil.bytesToHexString(StringUtil.digest(StringUtil.MD5,
                StringUtil.hexStringToBytes(userBean.getUid())));
        Context context = BaseApplication.getInstance().getBaseContext();
        context.deleteDatabase(dataBaseName);
    }

    public void closeHelper() {
        if (mHelper != null) {
            mHelper.close();
            mHelper = null;
        }

        ApplicationHelper.closeHelper();
        ContactHelper.closeHelper();
        ConversionHelper.closeHelper();
        ConversionSettingHelper.closeHelper();
        MessageHelper.closeHelper();
        OrganizerHelper.closeHelper();
        ParamHelper.closeHelper();
        TransactionHelper.closeHelper();
    }
}
