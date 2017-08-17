package connect.database.green.DaoHelper.mergin;

import android.database.SQLException;

import org.greenrobot.greendao.database.Database;

import connect.database.green.dao.ContactEntityDao;
import connect.database.green.dao.ConversionEntityDao;
import connect.database.green.dao.ConversionSettingEntityDao;
import connect.database.green.dao.FriendRequestEntityDao;
import connect.database.green.dao.GroupEntityDao;
import connect.database.green.dao.GroupMemberEntityDao;
import connect.database.green.dao.MessageEntityDao;
import connect.database.green.dao.RecommandFriendEntityDao;
import connect.database.green.dao.TransactionEntityDao;
import connect.utils.log.LogManager;

/**
 * Created by Administrator on 2017/8/17.
 */

public class MigrateVersionThreeHelper extends MigrateVerisonHelper{

    private String Tag = "MigrateOpenHelper";
    private Database database;

    public MigrateVersionThreeHelper(Database database) {
        this.database = database;
    }

    @Override
    public void migrate() {
        migrateRecommandFriend(database);
        migrateFriendRequest(database);
        migrateGroup(database);
        migrateGroupMember(database);
        migrateTransaction(database);
        migrateConversion(database);
        migrateConversionSetting(database);
        migrateMessage(database);
        migrateContact(database);
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
