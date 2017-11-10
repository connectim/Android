package connect.database.green.DaoHelper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import org.greenrobot.greendao.database.Database;
import java.io.File;
import connect.activity.base.BaseApplication;
import connect.activity.home.DBUpdateActivity;
import connect.database.green.DaoHelper.mergin.MigrateVersionFourHelper;
import connect.database.green.DaoHelper.mergin.MigrateVersionThreeHelper;
import connect.database.green.DaoHelper.mergin.MigrationHelper;
import connect.database.green.dao.ContactEntityDao;
import connect.database.green.dao.ConversionEntityDao;
import connect.database.green.dao.ConversionSettingEntityDao;
import connect.database.green.dao.DaoMaster;
import connect.database.green.dao.FriendRequestEntityDao;
import connect.database.green.dao.GroupEntityDao;
import connect.database.green.dao.GroupMemberEntityDao;
import connect.database.green.dao.MessageEntityDao;
import connect.database.green.dao.ParamEntityDao;
import connect.database.green.dao.TransactionEntityDao;

public class MigrateOpenHelper extends DaoMaster.OpenHelper {

    private String Tag = "_MigrateOpenHelper";
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
        if (!mainTmpDirSet) {
            Context context = BaseApplication.getInstance().getBaseContext();
            String innerPath = context.getFilesDir().getParentFile().getPath();
            boolean rs = new File(innerPath + "/databases/main").mkdirs();
            super.getReadableDatabase().execSQL("PRAGMA temp_store_directory='/data/data/connect.im/databases/main'");
            mainTmpDirSet = true;
        }
        return super.getReadableDatabase();
    }

    @Override
    public Database getEncryptedReadableDb(String password) {
        if (!mainTmpDirSet) {
            Context context = BaseApplication.getInstance().getBaseContext();
            String innerPath = context.getFilesDir().getParentFile().getPath();
            boolean rs = new File(innerPath + "/databases/main").mkdirs();
            super.getReadableDatabase().execSQL("PRAGMA temp_store_directory='/data/data/connect.im/databases/main'");
            mainTmpDirSet = true;
        }
        return super.getEncryptedReadableDb(password);
    }

    @Override
    public void onUpgrade(Database db, int oldVersion, int newVersion) {
        DBUpdateActivity.startActivity();

        switch (oldVersion) {
            case 3:
                new MigrateVersionThreeHelper(db).migrate();
            case 5:
                new MigrateVersionFourHelper(db).migrate();
            default:
                break;
        }

        MigrationHelper.migrate(db,
                ParamEntityDao.class,
                ContactEntityDao.class,
                GroupEntityDao.class,
                GroupMemberEntityDao.class,
                FriendRequestEntityDao.class,
                ConversionEntityDao.class,
                ConversionSettingEntityDao.class,
                MessageEntityDao.class,
                TransactionEntityDao.class
        );
    }
}
