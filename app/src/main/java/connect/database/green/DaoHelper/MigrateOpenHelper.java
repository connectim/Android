package connect.database.green.DaoHelper;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import org.greenrobot.greendao.database.Database;

import java.io.File;

import connect.database.green.DaoHelper.mergin.MigrationHelper;
import connect.database.green.bean.ContactEntityDao;
import connect.database.green.bean.ConversionEntityDao;
import connect.database.green.bean.ConversionSettingEntityDao;
import connect.database.green.bean.DaoMaster;
import connect.database.green.bean.FriendRequestEntityDao;
import connect.database.green.bean.GroupEntityDao;
import connect.database.green.bean.GroupMemberEntityDao;
import connect.database.green.bean.MessageEntityDao;
import connect.database.green.bean.ParamEntityDao;
import connect.database.green.bean.RecommandFriendEntityDao;
import connect.database.green.bean.TransactionEntityDao;
import connect.utils.log.LogManager;
import connect.activity.login.DBUpdateActivity;
import connect.activity.base.BaseApplication;

public class MigrateOpenHelper extends DaoMaster.OpenHelper {
    private String Tag = "MigrateOpenHelper";
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
            case 3://The database upgrade
                migrateRecommandFriend(db);
                migrateFriendRequest(db);
                migrateGroup(db);
                migrateGroupMember(db);
                migrateTransaction(db);
                migrateConversion(db);
                migrateConversionSetting(db);
                migrateMessage(db);
                migrateContact(db);
                break;
        }

        MigrationHelper.migrate(db,
                ParamEntityDao.class,
                ContactEntityDao.class,
                GroupEntityDao.class,
                GroupMemberEntityDao.class,
                RecommandFriendEntityDao.class,
                FriendRequestEntityDao.class,
                ConversionEntityDao.class,
                ConversionSettingEntityDao.class,
                MessageEntityDao.class,
                TransactionEntityDao.class);
    }

    protected void migrateRecommandFriend(Database db) {
        String oldTableName = "RECOMMEND_ENTITY";
        String tableName = RecommandFriendEntityDao.TABLENAME;
        String tempTableName = tableName + "temp";
        StringBuilder insertTableStringBuilder = new StringBuilder();

        try {
            if (MigrationHelper.isTableExists(db, false, tableName)) {
                return;
            }

            //Create a temporary table
            RecommandFriendEntityDao.createTable(db, true);
            insertTableStringBuilder.append("CREATE TEMPORARY TABLE ").append(tempTableName);
            insertTableStringBuilder.append(" AS SELECT * FROM ").append(oldTableName).append(";");
            db.execSQL(insertTableStringBuilder.toString());
        } catch (SQLException e) {
            LogManager.getLogger().d(Tag, "[Failed to generate temp table]" + tempTableName);
        }

        try {
            //Migrate the temporary table data to the new table
            String sqlSyntax = "INSERT INTO " + tableName +
                    "(username,address,avatar,pub_key,status) SELECT USERNAME ,ADDRESS,AVATAR,PUBKEY,STATUS FROM " +
                    tempTableName + ";";
            db.execSQL(sqlSyntax);

            //Delete the previous table
            String dropTable = "DROP TABLE " + oldTableName + ";";
            db.execSQL(dropTable);
        } catch (SQLException e) {
            LogManager.getLogger().d(Tag, "[Failed to restore data from temp table ]" + tempTableName);
        }
    }

    protected void migrateFriendRequest(Database db) {
        String oldTableName = "FRIEND_REQUEST_ENTITY";
        String tableName = FriendRequestEntityDao.TABLENAME + "new";//the previous table name repetition
        String tempTableName = tableName + "temp";
        StringBuilder insertTableStringBuilder = new StringBuilder();

        try {
            if (MigrationHelper.isTableExists(db, false, tableName)) {
                return;
            }
            //Create don't watch
            db.execSQL("CREATE TABLE IF NOT EXISTS " + tableName + " (" + //
                    "\"_ID\" INTEGER PRIMARY KEY AUTOINCREMENT ," + // 0: _id
                    "\"PUB_KEY\" TEXT NOT NULL ," + // 1: pub_key
                    "\"ADDRESS\" TEXT NOT NULL ," + // 2: address
                    "\"AVATAR\" TEXT," + // 3: avatar
                    "\"USERNAME\" TEXT," + // 4: username
                    "\"TIPS\" TEXT," + // 5: tips
                    "\"SOURCE\" INTEGER," + // 6: source
                    "\"STATUS\" INTEGER," + // 7: status
                    "\"READ\" INTEGER);"); // 8: read

            insertTableStringBuilder.append("CREATE TEMPORARY TABLE ").append(tempTableName);
            insertTableStringBuilder.append(" AS SELECT * FROM ").append(oldTableName).append(";");
            db.execSQL(insertTableStringBuilder.toString());
        } catch (SQLException e) {
            LogManager.getLogger().d(Tag, "[Failed to generate temp table]" + tempTableName);
        }

        try {
            String sqlSyntax = "INSERT INTO " + tableName +
                    "(address,pub_key,avatar,username,source,status,read,tips) SELECT ADDRESS,PUBKEY,AVATAR,USERNAME,SOURCE,STATUS,READ,TIPS FROM " +
                    tempTableName + ";";
            db.execSQL(sqlSyntax);

            String dropTable = "DROP TABLE " + oldTableName + ";";
            db.execSQL(dropTable);

            String alterTable = "ALTER TABLE " + tableName + " RENAME TO " + FriendRequestEntityDao.TABLENAME;
            db.execSQL(alterTable);
        } catch (SQLException e) {
            LogManager.getLogger().d(Tag, "[Failed to restore data from temp table ]" + tempTableName);
        }
    }

    protected void migrateGroup(Database db) {
        String oldTableName = "GROUP_ENTITY";
        String tableName = GroupEntityDao.TABLENAME + "new";
        String tempTableName = tableName + "temp";
        StringBuilder insertTableStringBuilder = new StringBuilder();

        try {
            if (MigrationHelper.isTableExists(db, false, tableName)) {
                return;
            }
            db.execSQL("CREATE TABLE IF NOT EXISTS " + tableName + " (" + //
                    "\"_ID\" INTEGER PRIMARY KEY AUTOINCREMENT ," + // 0: _id
                    "\"IDENTIFIER\" TEXT NOT NULL UNIQUE ," + // 1: identifier
                    "\"NAME\" TEXT," + // 2: name
                    "\"ECDH_KEY\" TEXT," + // 3: ecdh_key
                    "\"COMMON\" INTEGER," + // 4: common
                    "\"VERIFY\" INTEGER," + // 5: verify
                    "\"PUB\" INTEGER," + // 6: pub
                    "\"AVATAR\" TEXT," + // 7: avatar
                    "\"SUMMARY\" TEXT);"); // 8: summary

            insertTableStringBuilder.append("CREATE TEMPORARY TABLE ").append(tempTableName);
            insertTableStringBuilder.append(" AS SELECT * FROM ").append(oldTableName).append(";");
            db.execSQL(insertTableStringBuilder.toString());
        } catch (SQLException e) {
            LogManager.getLogger().d(Tag, "[Failed to generate temp table]" + tempTableName);
        }

        try {
            String sqlSyntax = "INSERT INTO " + tableName +
                    "(identifier,name,ecdh_key,common,verify,pub,avatar,summary) SELECT PUBKEY ,NAME,ECDHKEY,COMMON,GROUPVERIFY,0,AVATAR,SUMMARY FROM " +
                    tempTableName + ";";
            db.execSQL(sqlSyntax);

            String dropTable = "DROP TABLE " + oldTableName + ";";
            db.execSQL(dropTable);

            String alterTable = "ALTER TABLE " + tableName + " RENAME TO " + GroupEntityDao.TABLENAME;
            db.execSQL(alterTable);
        } catch (SQLException e) {
            LogManager.getLogger().d(Tag, "[Failed to restore data from temp table ]" + tempTableName);
        }
    }

    protected void migrateGroupMember(Database db) {
        String oldTableName = "GROUP_MEM_ENTITY";
        String tableName = GroupMemberEntityDao.TABLENAME;
        String tempTableName = tableName + "temp";
        StringBuilder insertTableStringBuilder = new StringBuilder();

        try {
            if (MigrationHelper.isTableExists(db, false, tableName)) {
                return;
            }
            GroupMemberEntityDao.createTable(db, true);
            insertTableStringBuilder.append("CREATE TEMPORARY TABLE ").append(tempTableName);
            insertTableStringBuilder.append(" AS SELECT * FROM ").append(oldTableName).append(";");
            db.execSQL(insertTableStringBuilder.toString());
        } catch (SQLException e) {
            LogManager.getLogger().d(Tag, "[Failed to generate temp table]" + tempTableName);
        }

        try {
            String sqlSyntax = "INSERT INTO " + tableName +
                    "(identifier ,username,avatar,address,role,nick,pub_key) SELECT GROUPKEY ,(CASE WHEN USERNAME IS NULL THEN 'NAME' ELSE USERNAME END),AVATAR,ADDRESS,ROLE,NICK,PUBKEY FROM " +
                    tempTableName + ";";
            db.execSQL(sqlSyntax);

            String dropTable = "DROP TABLE " + oldTableName + ";";
            db.execSQL(dropTable);
        } catch (SQLException e) {
            LogManager.getLogger().d(Tag, "[Failed to restore data from temp table ]" + tempTableName);
        }
    }

    protected void migrateTransaction(Database db) {
        String oldTableName = "TRANS_ENTITY";
        String tableName = TransactionEntityDao.TABLENAME;
        String tempTableName = tableName + "temp";
        StringBuilder insertTableStringBuilder = new StringBuilder();

        try {
            if (MigrationHelper.isTableExists(db, false, tableName)) {
                return;
            }
            TransactionEntityDao.createTable(db, true);
            insertTableStringBuilder.append("CREATE TEMPORARY TABLE ").append(tempTableName);
            insertTableStringBuilder.append(" AS SELECT * FROM ").append(oldTableName).append(";");
            db.execSQL(insertTableStringBuilder.toString());
        } catch (SQLException e) {
            LogManager.getLogger().d(Tag, "[Failed to generate temp table]" + tempTableName);
        }

        try {
            String sqlSyntax = "INSERT INTO " + tableName +
                    "(message_id,hashid,status,pay_count,crowd_count) SELECT '0',HASHID,0,PAYCOUNT,REQUESTCOUNT FROM " +
                    tempTableName + " GROUP BY HASHID" + ";";
            db.execSQL(sqlSyntax);

            String dropTable = "DROP TABLE " + oldTableName + ";";
            db.execSQL(dropTable);
        } catch (SQLException e) {
            LogManager.getLogger().d(Tag, "[Failed to restore data from temp table ]" + tempTableName + ",info==>" + e.getMessage());
        }
    }

    protected void migrateConversion(Database db) {
        String oldTableName = "CHAT_ROOM_ENTITY";
        String tableName = ConversionEntityDao.TABLENAME;
        String tempTableName = tableName + "temp";
        StringBuilder insertTableStringBuilder = new StringBuilder();

        try {
            if (MigrationHelper.isTableExists(db, false, tableName)) {
                return;
            }
            ConversionEntityDao.createTable(db, true);
            insertTableStringBuilder.append("CREATE TEMPORARY TABLE ").append(tempTableName);
            insertTableStringBuilder.append(" AS SELECT * FROM ").append(oldTableName).append(";");
            db.execSQL(insertTableStringBuilder.toString());
        } catch (SQLException e) {
            LogManager.getLogger().d(Tag, "[Failed to generate temp table]" + tempTableName);
        }

        try {
            String sqlSyntax = "INSERT INTO " + tableName +
                    "(identifier,name,avatar,draft,stranger,last_time,unread_count,top,notice,type,content) " +
                    "SELECT ROOMID,USERNAME,AVATAR,DRAFT,STRANGER,TIMESTAMP,UNREAD,TOP,AT,ROOMTYPE,SHOWTXT FROM " +
                    tempTableName + ";";
            db.execSQL(sqlSyntax);

            String dropTable = "DROP TABLE " + oldTableName + ";";
            db.execSQL(dropTable);
        } catch (SQLException e) {
            LogManager.getLogger().d(Tag, "[Failed to restore data from temp table ]" + tempTableName);
        }
    }

    protected void migrateConversionSetting(Database db) {
        String oldTableName = "CHAT_SET_ENTITY";
        String tableName = ConversionSettingEntityDao.TABLENAME;
        String tempTableName = tableName + "temp";
        StringBuilder insertTableStringBuilder = new StringBuilder();

        try {
            if (MigrationHelper.isTableExists(db, false, tableName)) {
                return;
            }
            ConversionSettingEntityDao.createTable(db, true);
            insertTableStringBuilder.append("CREATE TEMPORARY TABLE ").append(tempTableName);
            insertTableStringBuilder.append(" AS SELECT * FROM ").append(oldTableName).append(";");
            db.execSQL(insertTableStringBuilder.toString());
        } catch (SQLException e) {
            LogManager.getLogger().d(Tag, "[Failed to generate temp table]" + tempTableName);
        }

        try {
            String sqlSyntax = "INSERT INTO " + tableName +
                    "(identifier,snap_time,disturb) SELECT ROOMID,BURNCOUNT,DISTURB FROM " +
                    tempTableName + ";";
            db.execSQL(sqlSyntax);

            String dropTable = "DROP TABLE " + oldTableName + ";";
            db.execSQL(dropTable);
        } catch (SQLException e) {
            LogManager.getLogger().d(Tag, "[Failed to restore data from temp table ]" + tempTableName);
        }
    }

    protected void migrateMessage(Database db) {
        String oldTableName = "CHAT_MSG_ENTITY";
        String tableName = MessageEntityDao.TABLENAME;
        String tempTableName = tableName + "temp";
        StringBuilder insertTableStringBuilder = new StringBuilder();

        try {
            if (MigrationHelper.isTableExists(db, false, tableName)) {
                return;
            }
            MessageEntityDao.createTable(db, true);
            insertTableStringBuilder.append("CREATE TEMPORARY TABLE ").append(tempTableName);
            insertTableStringBuilder.append(" AS SELECT * FROM ").append(oldTableName).append(";");
            db.execSQL(insertTableStringBuilder.toString());
        } catch (SQLException e) {
            LogManager.getLogger().d(Tag, "[Failed to generate temp table]" + tempTableName);
        }

        try {
            String sqlSyntax = "INSERT INTO " + tableName +
                    "(message_id,message_ower,content,send_status,snap_time,read_time,state,createtime) SELECT MSGID,ROOMID,CONTENT,SENDSTATE,0,0,0,SENDTIME FROM " +
                    tempTableName + ";";
            db.execSQL(sqlSyntax);

            String dropTable = "DROP TABLE " + oldTableName + ";";
            db.execSQL(dropTable);
        } catch (SQLException e) {
            LogManager.getLogger().d(Tag, "[Failed to restore data from temp table ]" + tempTableName);
        }
    }

    protected void migrateContact(Database db) {
        String oldTableName = "FRIEND_ENTITY";
        String tableName = ContactEntityDao.TABLENAME;
        String tempTableName = tableName + "temp";
        StringBuilder insertTableStringBuilder = new StringBuilder();

        try {
            if (MigrationHelper.isTableExists(db, false, tableName)) {
                return;
            }
            ContactEntityDao.createTable(db, true);
            insertTableStringBuilder.append("CREATE TEMPORARY TABLE ").append(tempTableName);
            insertTableStringBuilder.append(" AS SELECT * FROM ").append(oldTableName).append(";");
            db.execSQL(insertTableStringBuilder.toString());
        } catch (SQLException e) {
            LogManager.getLogger().d(Tag, "[Failed to generate temp table]" + tempTableName);
        }

        try {
            String sqlSyntax = "INSERT INTO " + tableName +
                    "(address,pub_key,avatar,username,remark,blocked,common) SELECT ADDRESS,PUBKEY,AVATAR,USERNAME,REMARK,BLOCK,COMMON FROM " +
                    tempTableName + ";";
            db.execSQL(sqlSyntax);

            String dropTable = "DROP TABLE " + oldTableName + ";";
            db.execSQL(dropTable);
        } catch (SQLException e) {
            LogManager.getLogger().d(Tag, "[Failed to restore data from temp table ]" + tempTableName);
        }
    }
}
