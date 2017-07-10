package connect.database.green;

import android.content.Context;
import android.text.TextUtils;

import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.query.QueryBuilder;

import connect.database.MemoryDataManager;
import connect.database.SharePreferenceUser;
import connect.database.green.DaoHelper.ConversionHelper;
import connect.database.green.DaoHelper.ConversionSettingHelper;
import connect.database.green.DaoHelper.ContactHelper;
import connect.database.green.DaoHelper.MessageHelper;
import connect.database.green.DaoHelper.MigrateOpenHelper;
import connect.database.green.DaoHelper.ParamHelper;
import connect.database.green.DaoHelper.TransactionHelper;
import connect.database.green.bean.DaoMaster;
import connect.database.green.bean.DaoSession;
import connect.activity.base.BaseApplication;
import connect.utils.ConfigUtil;
import connect.utils.StringUtil;
import connect.utils.cryption.SupportKeyUril;
import connect.utils.log.LogManager;
import connect.wallet.jni.AllNativeMethod;

/**
 * The management of the database
 * Created by gtq on 2016/11/22.
 */
public class DaoManager {
    private static final String TAG = "DaoManager";
    /**
     * The database version number
     * The database upgrade need to change the version number, data migration
     * 1--->0.0.3
     * 2--->0.0.4
     * 3--->0.0.8
     * 4--->0.0.9
     */
    public static final int SCHEMA_VERSION = 4;
    private volatile static DaoManager mDaoManager;
    private static DaoMaster.DevOpenHelper mHelper;
    private static DaoMaster mDaoMaster;
    private static DaoSession mDaoSession;

    public static DaoManager getInstance() {
        if (mDaoManager == null) {
            synchronized (DaoManager.class) {
                if (mDaoManager == null) {
                    mDaoManager = new DaoManager();
                }
            }
        }
        return mDaoManager;
    }

    /**
     * Determine the existence of the database , if not  create it
     *
     * @return
     */
    public synchronized DaoMaster getDaoMaster() {
        if (null == mDaoMaster) {
            String oldSecret = SharePreferenceUser.getInstance().getStringValue("db_secret");
            String password = !TextUtils.isEmpty(oldSecret) ? OldDbPwd(oldSecret) : newDbPwd();

            String pubKey = MemoryDataManager.getInstance().getPubKey();
            String DB_NAME = "bitmain.db";
            if (!TextUtils.isEmpty(pubKey)) {
                DB_NAME = "connect_" + pubKey + ".db";
            }

            Context context = BaseApplication.getInstance().getBaseContext();
            MigrateOpenHelper helper = new MigrateOpenHelper(context, DB_NAME, null);
            Database db = null;

            if (ConfigUtil.getInstance().appMode()) {//release version
                LogManager.getLogger().d(TAG, "password =" + password);
                setDebug(false);//log
                db = helper.getEncryptedWritableDb(password);
            } else {//debug version
                LogManager.getLogger().d(TAG, "DEBUG VERSION");
                setDebug(true);
                db = helper.getWritableDb();
            }

            if (!TextUtils.isEmpty(oldSecret)) {//update password
                db.execSQL("PRAGMA key = '" + password + "';");
                db.execSQL("PRAGMA rekey = '" + newDbPwd() + "';");
                SharePreferenceUser.getInstance().putString("db_secret", "");
            }
            mDaoMaster = new DaoMaster(db);
        }
        return mDaoMaster;
    }

    public String OldDbPwd(String oldSecret) {
        String salt = SharePreferenceUser.getInstance().getStringValue(SharePreferenceUser.DB_SALT);
        String priKey = MemoryDataManager.getInstance().getPriKey();
        return SupportKeyUril.decodePri(oldSecret, salt, priKey);
    }

    public String newDbPwd() {
        String pubKeyTemp = SharePreferenceUser.getInstance().getStringValue(SharePreferenceUser.DB_PUBKEY);
        String priKey = MemoryDataManager.getInstance().getPriKey();
        if (TextUtils.isEmpty(priKey)) {
            BaseApplication.getInstance().finishActivity();
        }

        String salt = "";
        if (TextUtils.isEmpty(pubKeyTemp)) {
            salt = SupportKeyUril.cdSaltPri();
            pubKeyTemp = AllNativeMethod.cdGetPubKeyFromPrivKey(AllNativeMethod.cdCreateNewPrivKey());
            SharePreferenceUser.getInstance().putString(SharePreferenceUser.DB_PUBKEY, pubKeyTemp);
            SharePreferenceUser.getInstance().putString(SharePreferenceUser.DB_SALT, salt);
        } else {
            salt = SharePreferenceUser.getInstance().getStringValue(SharePreferenceUser.DB_SALT);
        }
        byte[] saltByte = salt.getBytes();
        byte[] ecdHkey = SupportKeyUril.rawECDHkey(priKey, pubKeyTemp);
        byte[] pbkdf = AllNativeMethod.cdxtalkPBKDF2HMACSHA512(ecdHkey, ecdHkey.length, saltByte, saltByte.length, 12, 32);
        return AllNativeMethod.cdGetHash256(StringUtil.bytesToHexString(pbkdf));
    }

    public synchronized DaoSession getDaoSession() {
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

    public void switchDataBase() {
        getDaoMaster();
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

    public void closeHelper() {
        if (mHelper != null) {
            mHelper.close();
            mHelper = null;
        }

        MessageHelper.closeHelper();
        ConversionHelper.closeHelper();
        ConversionSettingHelper.closeHelper();
        ContactHelper.closeHelper();
        ParamHelper.closeHelper();
        TransactionHelper.closeHelper();
    }
}
