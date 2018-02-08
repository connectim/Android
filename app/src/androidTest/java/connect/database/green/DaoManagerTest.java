package connect.database.green;

import android.content.Context;
import android.database.Cursor;

import org.greenrobot.greendao.database.Database;
import org.junit.Assert;
import org.junit.Test;

import connect.activity.base.BaseApplication;
import connect.database.green.bean.ContactEntity;
import connect.database.green.bean.ConversionEntity;
import connect.database.green.bean.ConversionSettingEntity;
import connect.database.green.bean.FriendRequestEntity;
import connect.database.green.bean.GroupEntity;
import connect.database.green.bean.GroupMemberEntity;
import connect.database.green.bean.MessageEntity;
import connect.database.green.bean.ParamEntity;
import connect.database.green.bean.TransactionEntity;
import connect.database.green.dao.DaoMaster;
import connect.database.green.dao.DaoSession;
import connect.utils.log.LogManager;

/**
 * Created by Administrator on 2017/8/1.
 */
public class DaoManagerTest {

    private String Tag = "_DaoManagerTest";

    private String dbName = "connect.db";
    private String dbPwd = "connectpwd";
    private Context context= BaseApplication.getInstance();
    private DaoMaster daoMaster;
    private DaoSession daoSession;
    private Database database;

    public DaoManagerTest(){
        createConnectDb();
    }

    @Test
    public void greenDaoCentreTest() throws Exception {
        // support
        //dataBaseTest();

        //Contact
        //ContactHelperTest contactHelperTest = new ContactHelperTest();
        //contactHelperTest.loadChatType(null);
        //contactHelperTest.loadTalkerFriend(null);
        //contactHelperTest.loadTalkerGroup(null);
        //contactHelperTest.loadGroupMemEntities(null);
        //contactHelperTest.loadGroupMemEntities(null, null);
        //contactHelperTest.updateGroupMember(null,null,null,null,0,null);


        //ConversionHelperTest
        //ConversionHelperTest conversionHelperTest = new ConversionHelperTest();
        //conversionHelperTest.countUnReads();
        //conversionHelperTest.loadRoomEntities(null);
        //conversionHelperTest.loadRecentRoomEntities();

        //MessageHelperTest
        //MessageHelperTest messageHelperTest = new MessageHelperTest();
        //messageHelperTest.loadMoreMsgEntities(null, 0);
    }


    @Test
    public void dataBaseTest(){
        updateConnectDb();
        deleteConnectDb();
        createTableTest();
        deleteTest();
        contactEntityTest();
        conversionEntityTest();
        conversionSettingEntityTest();
        friendRequestEntityTest();
        groupEntityTest();
        groupMemberEntityTest();
        messageEntityTest();
        paramEntityTest();
        transactionEntityTest();
    }


    @Test
    public void createConnectDb() {
        DaoMaster.OpenHelper openHelper = new DaoMaster.DevOpenHelper(context, dbName);
        // database = openHelper.getEncryptedWritableDb(dbPwd);
        database = openHelper.getWritableDb();
        daoMaster = new DaoMaster(database);
        daoSession = daoMaster.newSession();
    }

    @Test
    public void deleteConnectDb() {
        context.deleteDatabase(dbName);
    }

    @Test
    public void updateConnectDb() {
        database.execSQL("PRAGMA key = '" + dbPwd + "';");
        database.execSQL("PRAGMA rekey = '" + (dbPwd + dbPwd) + "';");
    }

    @Test
    public void renameConnectDb() {
    }

    @Test
    public void createTableTest() {
        String tablename = "connecttable";
        database.execSQL("CREATE TABLE IF NOT EXISTS " + tablename + " (" +
                "\"_ID\" INTEGER PRIMARY KEY AUTOINCREMENT ," +
                "\"IDENTIFIER\" TEXT NOT NULL UNIQUE ," +
                "\"NAME\" TEXT," +
                "\"SUMMARY\" TEXT);");
    }

    @Test
    public void deleteTest() {
        String tablename = "connecttable";
        database.execSQL("DROP TABLE " + tablename);
    }

    @Test
    public void groupMembers() {
        String groupkey = "";
        String sql = "SELECT M.* , F.REMARK AS REMARK  FROM GROUP_MEMBER_ENTITY M LEFT OUTER JOIN CONTACT_ENTITY F ON M.PUB_KEY = F.PUB_KEY WHERE M.IDENTIFIER = ? GROUP BY M.IDENTIFIER ,M.ADDRESS ORDER BY M.ROLE DESC;";
        Cursor cursor = daoSession.getDatabase().rawQuery(sql, new String[]{groupkey});
        while (cursor.moveToNext()) {
            LogManager.getLogger().d(Tag, cursor.getString(cursor.getColumnIndex("IDENTIFIER")));
        }
        if (cursor != null) {
            cursor.close();
        }
    }

    @Test
    public void loadRoomEntities() {
        String sql = "SELECT R.*, S.DISTURB FROM CONVERSION_ENTITY R " +
                " LEFT OUTER JOIN CONVERSION_SETTING_ENTITY S ON R.IDENTIFIER = S.IDENTIFIER " +
                " GROUP BY R.IDENTIFIER ORDER BY IFNULL(R.TOP, 0) DESC,IFNULL(R.LAST_TIME, 0) DESC;";
        Cursor cursor = daoSession.getDatabase().rawQuery(sql, null);
        while (cursor.moveToNext()) {
            LogManager.getLogger().d(Tag, cursor.getString(cursor.getColumnIndex("IDENTIFIER")));
        }
        if (cursor != null) {
            cursor.close();
        }
    }

    @Test
    public void recentRoomEntities() {
        String sql = "SELECT R.*, S.DISTURB FROM CONVERSION_ENTITY R \n" +
                " LEFT OUTER JOIN CONVERSION_SETTING_ENTITY S ON R.IDENTIFIER = S.IDENTIFIER \n" +
                " WHERE R.TYPE != 2 and (R.STRANGER IS NULL OR R.STRANGER = 0) ORDER BY R.TOP DESC,R.LAST_TIME DESC LIMIT 10;";
        Cursor cursor = daoSession.getDatabase().rawQuery(sql, null);
        while (cursor.moveToNext()) {
            LogManager.getLogger().d(Tag, cursor.getString(cursor.getColumnIndex("IDENTIFIER")));
        }
        if (cursor != null) {
            cursor.close();
        }
    }

    @Test
    public void loadMoreMsgEntities() {
        String pubkey = "";
        long firsttime = 0;
        String sql = "SELECT * FROM (SELECT C.* ,S.STATUS AS TRANS_STATUS,HASHID,PAY_COUNT,CROWD_COUNT FROM MESSAGE_ENTITY C LEFT OUTER JOIN TRANSACTION_ENTITY S ON C.MESSAGE_ID = S.MESSAGE_ID WHERE C.MESSAGE_OWER = ? " +
                ((firsttime == 0) ? "" : " AND C.CREATETIME < " + firsttime) +
                " ORDER BY C.CREATETIME DESC LIMIT 20) ORDER BY CREATETIME ASC;";
        Cursor cursor = daoSession.getDatabase().rawQuery(sql, new String[]{pubkey});
        while (cursor.moveToNext()) {
            LogManager.getLogger().d(Tag, cursor.getString(cursor.getColumnIndex("CONTENT")));
        }
        if (cursor != null) {
            cursor.close();
        }
    }

    @Test
    public void contactEntityTest() {
        ContactEntity contactEntity = new ContactEntity();
        contactEntity.set_id(100001L);
        contactEntity.setUid("Pub_key");
        contactEntity.setUid("Address");
        //daoSession.getContactEntityDao().insert(contactEntity);
        daoSession.getContactEntityDao().insert(contactEntity);

        ContactEntity tempEntity = daoSession.getContactEntityDao().load(100001L);
        String pubkey = tempEntity.getUid();
        Assert.assertSame(pubkey, "Contact");
    }

    @Test
    public void conversionEntityTest() {
        ConversionEntity conversionEntity = new ConversionEntity();
        conversionEntity.set_id(100001L);
        conversionEntity.setIdentifier("ConversionEntity");
        //daoSession.getConversionEntityDao().insert(conversionEntity);
        daoSession.getConversionEntityDao().insert(conversionEntity);


        ConversionEntity tempEntity = daoSession.getConversionEntityDao().load(100001L);
        String pubkey = tempEntity.getIdentifier();
        Assert.assertSame(pubkey, "ConversionEntity");
    }

    @Test
    public void conversionSettingEntityTest() {
        ConversionSettingEntity settingEntity = new ConversionSettingEntity();
        settingEntity.set_id(100001L);
        settingEntity.setIdentifier("ConversionSetting");
        // daoSession.getConversionSettingEntityDao().insert(settingEntity);
        daoSession.getConversionSettingEntityDao().insert(settingEntity);

        ConversionSettingEntity tempEntity = daoSession.getConversionSettingEntityDao().load(100001L);
        String pubkey = tempEntity.getIdentifier();
        Assert.assertSame(pubkey, "ConversionSetting");
    }

    @Test
    public void friendRequestEntityTest() {
        FriendRequestEntity requestEntity = new FriendRequestEntity();
        requestEntity.set_id(100001L);
        requestEntity.setUid("Pub_key");
        //requestEntity.set("Address");
        // daoSession.getFriendRequestEntityDao().insert(requestEntity);
        daoSession.getFriendRequestEntityDao().insert(requestEntity);

        FriendRequestEntity tempEntity = daoSession.getFriendRequestEntityDao().load(100001L);
        String pubkey = tempEntity.getUid();
        Assert.assertSame(pubkey, "Pub_key");
    }

    @Test
    public void groupEntityTest() {
        GroupEntity groupEntity = new GroupEntity();
        groupEntity.set_id(100001L);
        groupEntity.setIdentifier("Identifier");
        // daoSession.getGroupEntityDao().insert(groupEntity);
        daoSession.getGroupEntityDao().insert(groupEntity);

        GroupEntity tempEntity = daoSession.getGroupEntityDao().load(100001L);
        String identifier = tempEntity.getIdentifier();
        Assert.assertSame(identifier, "Identifier");
    }

    @Test
    public void groupMemberEntityTest() {
        GroupMemberEntity memberEntity = new GroupMemberEntity();
        memberEntity.set_id(100001L);
        memberEntity.setIdentifier("Identifier");
        memberEntity.setUsername("Username");
        memberEntity.setUid("Address");
        memberEntity.setAvatar("Avatar");
        // daoSession.getGroupMemberEntityDao().insert(memberEntity);
        daoSession.getGroupMemberEntityDao().insert(memberEntity);

        GroupMemberEntity tempEntity = daoSession.getGroupMemberEntityDao().load(100001L);
        String identifier = tempEntity.getIdentifier();
        Assert.assertSame(identifier, "Identifier");
    }

    @Test
    public void messageEntityTest() {
        MessageEntity messageEntity = new MessageEntity();
        messageEntity.set_id(100001L);
        messageEntity.setMessage_ower("Message_ower");
        messageEntity.setMessage_id("Message_id");
        // daoSession.getMessageEntityDao().insert(messageEntity);
        daoSession.getMessageEntityDao().insert(messageEntity);

        MessageEntity tempEntity = daoSession.getMessageEntityDao().load(100001L);
        String message_id = tempEntity.getMessage_id();
        Assert.assertSame(message_id, "Message_id");
    }

    @Test
    public void paramEntityTest() {
        ParamEntity paramEntity = new ParamEntity();
        paramEntity.set_id(100001L);
        paramEntity.setKey("Key");
        // daoSession.getParamEntityDao().insert(paramEntity);
        daoSession.getParamEntityDao().insert(paramEntity);

        ParamEntity tempEntity = daoSession.getParamEntityDao().load(100001L);
        String key = tempEntity.getKey();
        Assert.assertSame(key, "Key");
    }

    @Test
    public void transactionEntityTest() {
        TransactionEntity transactionEntity = new TransactionEntity();
        transactionEntity.set_id(100001L);
        transactionEntity.setMessage_id("Message_id");
        transactionEntity.setHashid("Hashid");
        // daoSession.getRecommandFriendEntityDao().insert(friendEntity);
        daoSession.getTransactionEntityDao().insert(transactionEntity);

        TransactionEntity tempEntity = daoSession.getTransactionEntityDao().load(100001L);
        String message_id = tempEntity.getMessage_id();
        Assert.assertSame(message_id, "Message_id");
    }
}