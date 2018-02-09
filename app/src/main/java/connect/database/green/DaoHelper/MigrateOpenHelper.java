package connect.database.green.DaoHelper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import org.greenrobot.greendao.database.Database;

import connect.activity.home.DBUpdateActivity;
import connect.database.green.DaoHelper.mergin.MigrationHelper;
import connect.database.green.dao.ApplicationEntityDao;
import connect.database.green.dao.ContactEntityDao;
import connect.database.green.dao.ConversionEntityDao;
import connect.database.green.dao.ConversionSettingEntityDao;
import connect.database.green.dao.CurrencyEntityDao;
import connect.database.green.dao.DaoMaster;
import connect.database.green.dao.FriendRequestEntityDao;
import connect.database.green.dao.GroupEntityDao;
import connect.database.green.dao.GroupMemberEntityDao;
import connect.database.green.dao.MessageEntityDao;
import connect.database.green.dao.OrganizerEntityDao;
import connect.database.green.dao.ParamEntityDao;
import connect.database.green.dao.TransactionEntityDao;

public class MigrateOpenHelper extends DaoMaster.OpenHelper {

    private static String TAG = "_MigrateOpenHelper";
    private boolean mainTmpDirSet = false;

    public MigrateOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory) {
        super(context, name, factory);
    }

    @Override
    public void onCreate(Database db) {
        super.onCreate(db);
    }

    /**
     * BUG:SQLiteCantOpenDatabaseException: unable to open database file (code 14)
     *
     * @return
     */
    @Override
    public SQLiteDatabase getReadableDatabase() {
//        if (!mainTmpDirSet) {
//            Context context = BaseApplication.getInstance().getBaseContext();
//            String innerPath = context.getFilesDir().getParentFile().getPath();
//            boolean rs = new File(innerPath + "/databases/main").mkdirs();
//            super.getReadableDatabase().execSQL("PRAGMA temp_store_directory='/data/data/com.iwork.im/databases/main'");
//            mainTmpDirSet = true;
//        }
        return super.getReadableDatabase();
    }

    @Override
    public Database getEncryptedReadableDb(String password) {
//        if (!mainTmpDirSet) {
//            Context context = BaseApplication.getInstance().getBaseContext();
//            String innerPath = context.getFilesDir().getParentFile().getPath();
//            boolean rs = new File(innerPath + "/databases/main").mkdirs();
//            super.getReadableDatabase().execSQL("PRAGMA temp_store_directory='/data/data/com.iwork.im/databases/main'");
//            mainTmpDirSet = true;
//        }
        return super.getEncryptedReadableDb(password);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // super.onUpgrade(db, oldVersion, newVersion);

        DBUpdateActivity.startActivity();

//        MigrationHelper.migrate(db,
//                ApplicationEntityDao.class,
//                ContactEntityDao.class,
//                ConversionEntityDao.class,
//                ConversionSettingEntityDao.class,
//                CurrencyAddressEntityDao.class,
//                CurrencyEntityDao.class,
//                FriendRequestEntityDao.class,
//                GroupEntityDao.class,
//                GroupMemberEntityDao.class,
//                MessageEntityDao.class,
//                OrganizerEntityDao.class,
//                ParamEntityDao.class,
//                TransactionEntityDao.class
//        );
    }

    @Override
    public void onUpgrade(Database db, int oldVersion, int newVersion) {
        // super.onUpgrade(getWritableDatabase(), oldVersion, newVersion);

//        DaoMaster.dropAllTables(db, true);
//        onCreate(db);
//        SharedUtil.getInstance().deleteUserInfo();
        DBUpdateActivity.startActivity();

        MigrationHelper.migrate(db,
                ApplicationEntityDao.class,
                ContactEntityDao.class,
                ConversionEntityDao.class,
                ConversionSettingEntityDao.class,
                CurrencyEntityDao.class,
                FriendRequestEntityDao.class,
                GroupEntityDao.class,
                GroupMemberEntityDao.class,
                MessageEntityDao.class,
                OrganizerEntityDao.class,
                ParamEntityDao.class,
                TransactionEntityDao.class
        );
    }
}
