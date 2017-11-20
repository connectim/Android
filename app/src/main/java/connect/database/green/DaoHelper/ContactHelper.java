package connect.database.green.DaoHelper;

import android.database.Cursor;
import android.text.TextUtils;

import org.greenrobot.greendao.query.DeleteQuery;
import org.greenrobot.greendao.query.QueryBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import connect.activity.base.BaseApplication;
import connect.activity.chat.bean.Talker;
import connect.activity.contact.bean.ContactNotice;
import connect.activity.home.bean.ConversationAction;
import connect.database.green.BaseDao;
import connect.database.green.bean.ContactEntity;
import connect.database.green.bean.FriendRequestEntity;
import connect.database.green.bean.GroupEntity;
import connect.database.green.bean.GroupMemberEntity;

import connect.database.green.dao.ContactEntityDao;
import connect.database.green.dao.FriendRequestEntityDao;
import connect.database.green.dao.GroupEntityDao;
import connect.database.green.dao.GroupMemberEntityDao;
import connect.ui.activity.R;
import connect.utils.FileUtil;
import protos.Connect;

/**
 * The contacts book (friends/group)
 * Created by gtq on 2016/11/22.
 */
public class ContactHelper extends BaseDao {

    private static ContactHelper contactHelper;
    private FriendRequestEntityDao friendRequestEntityDao;
    private ContactEntityDao contactEntityDao;
    private GroupEntityDao groupEntityDao;
    private GroupMemberEntityDao groupMemberEntityDao;

    private static String TAG = "_ContactHelper";

    public ContactHelper() {
        super();
        contactEntityDao = daoSession.getContactEntityDao();
        groupEntityDao = daoSession.getGroupEntityDao();
        groupMemberEntityDao = daoSession.getGroupMemberEntityDao();
        friendRequestEntityDao = daoSession.getFriendRequestEntityDao();
    }

    public synchronized static ContactHelper getInstance() {
        if (contactHelper == null) {
            contactHelper = new ContactHelper();
        }
        return contactHelper;
    }

    public static void closeHelper() {
        contactHelper = null;
    }

    public void insertContact(ContactEntity friendEntity) {
        contactEntityDao.insertOrReplace(friendEntity);
    }

    public void insertContacts(List<ContactEntity> entities) {
        contactEntityDao.insertInTx(entities);
    }

    public List<ContactEntity> loadAll() {
        return contactEntityDao.loadAll();
    }

    /**
     * load all friend（except connect）
     *
     * @return
     */
    public List<ContactEntity> loadFriend() {
        String connect = BaseApplication.getInstance().getString(R.string.app_name);
        QueryBuilder<ContactEntity> queryBuilder = contactEntityDao.queryBuilder();
        queryBuilder.where(ContactEntityDao.Properties.Uid.notEq(connect)).build();
        List<ContactEntity> friendEntities = queryBuilder.list();
        return friendEntities;
    }

    public List<ContactEntity> loadFriendBlack() {
        QueryBuilder<ContactEntity> queryBuilder = contactEntityDao.queryBuilder();
        queryBuilder.where(ContactEntityDao.Properties.Blocked.eq(true)).build();
        List<ContactEntity> friendEntities = queryBuilder.list();
        return friendEntities;
    }

    public List<ContactEntity> loadFriend(String pubkey) {
        List<String> strings = new ArrayList<>();
        strings.add(pubkey);
        return loadFriend(strings);
    }

    public List<ContactEntity> loadFriend(List<String> pubkeys) {
        String connect = BaseApplication.getInstance().getString(R.string.app_name);
        pubkeys.add(connect);
        QueryBuilder<ContactEntity> queryBuilder = contactEntityDao.queryBuilder();
        queryBuilder.where(ContactEntityDao.Properties.Uid.notIn(pubkeys)).build();
        List<ContactEntity> friendEntities = queryBuilder.list();
        return friendEntities;
    }

    /********************************* select ***********************************/
    public Connect.ChatType loadChatType(String uid) {
        if (TextUtils.isEmpty(uid)) {
            uid = "";
        }

        String sql = "SELECT C.UID AS CUID, G.IDENTIFIER AS GIDENTIFY FROM CONTACT_ENTITY C LEFT OUTER JOIN GROUP_ENTITY G WHERE C.UID = G.IDENTIFIER AND C.UID = ? GROUP BY C.UID LIMIT 1;";

        Connect.ChatType chatType = null;
        Cursor cursor = daoSession.getDatabase().rawQuery(sql, new String[]{uid});
        while (cursor.moveToNext()) {
            String friendUid = cursorGetString(cursor, "CUID");
            String groupIdentify = cursorGetString(cursor, "GIDENTIFY");
            if (!TextUtils.isEmpty(friendUid)) {
                chatType = Connect.ChatType.PRIVATE;
            } else if (!TextUtils.isEmpty(groupIdentify)) {
                chatType = Connect.ChatType.GROUPCHAT;
            } else if (BaseApplication.getInstance().getBaseContext().getResources().getString(R.string.app_name).equals(uid)) {
                chatType = Connect.ChatType.CONNECT_SYSTEM;
            } else {
                chatType = Connect.ChatType.UNKNOW;
            }
        }
        return chatType;
    }

    public Talker loadTalkerFriend(String uid) {
        if (TextUtils.isEmpty(uid)) {
            uid = "";
        }

        String sql = "SELECT F.USERNAME AS FNAME, F.REMARK AS FREMARK, F.AVATAR AS FAVATAR, C.NAME AS CNAME, C.AVATAR AS CAVATAR FROM CONTACT_ENTITY F LEFT OUTER JOIN CONVERSION_ENTITY C WHERE F.UID = ? AND F.UID = C.IDENTIFIER LIMIT 1;";

        Cursor cursor = daoSession.getDatabase().rawQuery(sql, new String[]{uid});

        Talker talker = new Talker(Connect.ChatType.PRIVATE, uid);
        while (cursor.moveToNext()) {
            String userName = cursorGetString(cursor, "FNAME");
            String userRemark = cursorGetString(cursor, "FREMARK");
            String userAvatar = cursorGetString(cursor, "FAVATAR");

            boolean stranger = TextUtils.isEmpty(userName);
            talker.setStranger(stranger);
            if (stranger) {
                userName = cursorGetString(cursor, "CNAME");
                userAvatar = cursorGetString(cursor, "CAVATAR");
            }

            userName = TextUtils.isEmpty(userRemark) ? userName : userRemark;
            talker.setNickName(userName);
            talker.setAvatar(userAvatar);
        }
        return talker;
    }

    public Talker loadTalkerGroup(String identify) {
        if (TextUtils.isEmpty(identify)) {
            identify = "";
        }

        String sql = "SELECT G.NAME AS GNAME, G.AVATAR AS GAVATAR, C.NAME AS CNAME, C.AVATAR AS CAVATAR FROM GROUP_ENTITY G LEFT OUTER JOIN CONVERSION_ENTITY C WHERE G.IDENTIFIER = ? AND G.IDENTIFIER = C.IDENTIFIER LIMIT 1;";

        Talker talker = new Talker(Connect.ChatType.GROUPCHAT, identify);
        Cursor cursor = daoSession.getDatabase().rawQuery(sql, new String[]{identify});
        while (cursor.moveToNext()) {
            String groupName = cursorGetString(cursor, "GNAME");
            String groupAvatar = cursorGetString(cursor, "GAVATAR");

            boolean stranger = TextUtils.isEmpty(groupName);
            talker.setStranger(stranger);
            if (stranger) {
                groupName = cursorGetString(cursor, "CNAME");
                groupAvatar = cursorGetString(cursor, "CAVATAR");
            }

            talker.setNickName(groupName);
            talker.setAvatar(groupAvatar);
        }
        return talker;
    }

    /**
     * friend entity
     *
     * @param value
     * @return
     */
    public ContactEntity loadFriendEntity(String value) {
        if (TextUtils.isEmpty(value)) {
            value = "";
        }
        QueryBuilder<ContactEntity> queryBuilder = contactEntityDao.queryBuilder();
        queryBuilder.whereOr(ContactEntityDao.Properties.Ca_pub.eq(value),
                ContactEntityDao.Properties.ConnectId.eq(value),
                ContactEntityDao.Properties.Uid.eq(value))
                .limit(1).build();
        List<ContactEntity> friendEntities = queryBuilder.listLazy();
        return (friendEntities == null || friendEntities.size() == 0) ? null : friendEntities.get(0);
    }

    /**
     * Fuzzy query the contact
     *
     * @param text
     * @return
     */
    public List<ContactEntity> loadFriendEntityFromText(String text) {
        QueryBuilder<ContactEntity> queryBuilder = contactEntityDao.queryBuilder();
        queryBuilder.where(ContactEntityDao.Properties.Username.like(text + "%")).limit(1).build();
        List<ContactEntity> friendEntities = queryBuilder.list();
        return friendEntities;
    }

    /**
     * query all request
     *
     * @return
     */
    public List<FriendRequestEntity> loadFriendRequest() {
        List<FriendRequestEntity> list = friendRequestEntityDao.loadAll();
        Collections.reverse(list);
        return list;
    }

    /**
     * query friend request
     *
     * @return
     */
    public FriendRequestEntity loadFriendRequest(String address) {
        QueryBuilder<FriendRequestEntity> queryBuilder = friendRequestEntityDao.queryBuilder();
        queryBuilder.where(FriendRequestEntityDao.Properties.Uid.eq(address)).limit(1).build();
        List<FriendRequestEntity> list = queryBuilder.list();
        if (list.size() == 0) {
            return null;
        }
        return list.get(0);
    }

    /**
     * query request without read
     *
     * @return
     */
    public List<FriendRequestEntity> loadFriendRequestNew() {
        QueryBuilder<FriendRequestEntity> queryBuilder = friendRequestEntityDao.queryBuilder();
        queryBuilder.where(FriendRequestEntityDao.Properties.Read.eq(0)).limit(1).build();
        return queryBuilder.list();
    }

    /**
     * query common group
     *
     * @return
     */
    public List<GroupEntity> loadGroupCommonAll() {
        QueryBuilder<GroupEntity> queryBuilder = groupEntityDao.queryBuilder();
        queryBuilder.where(GroupEntityDao.Properties.Common.eq(1)).build();
        return queryBuilder.list();
    }

    /**
     * group entity
     *
     * @param identify
     * @return
     */
    public GroupEntity loadGroupEntity(String identify) {
        QueryBuilder<GroupEntity> queryBuilder = groupEntityDao.queryBuilder();
        queryBuilder.where(GroupEntityDao.Properties.Identifier.eq(identify)).build();
        List<GroupEntity> groupEntities = queryBuilder.list();
        return (groupEntities == null || groupEntities.size() == 0) ? null : groupEntities.get(0);
    }

    /**
     * group member
     *
     * @param pukkey
     * @return
     */
    public List<GroupMemberEntity> loadGroupMemEntities(String pukkey) {
        if (TextUtils.isEmpty(pukkey)) {
            pukkey = "";
        }
        String sql = "SELECT M.* , F.REMARK AS REMARK  FROM GROUP_MEMBER_ENTITY M LEFT OUTER JOIN CONTACT_ENTITY F ON M.UID = F.UID " +
                "WHERE M.IDENTIFIER = ? GROUP BY M.IDENTIFIER ,M.UID ORDER BY M.ROLE DESC;";
        Cursor cursor = daoSession.getDatabase().rawQuery(sql, new String[]{pukkey});

        GroupMemberEntity groupMemEntity = null;
        List<GroupMemberEntity> groupMemEntities = new ArrayList<>();
        while (cursor.moveToNext()) {
            groupMemEntity = new GroupMemberEntity();
            groupMemEntity.set_id(cursorGetLong(cursor, "_ID"));
            groupMemEntity.setIdentifier(cursorGetString(cursor, "IDENTIFIER"));
            groupMemEntity.setUid(cursorGetString(cursor, "UID"));
            groupMemEntity.setNick(cursorGetString(cursor, "NICK"));
            groupMemEntity.setUsername(cursorGetString(cursor, "USERNAME"));
            groupMemEntity.setRole(cursorGetInt(cursor, "ROLE"));
            groupMemEntity.setAvatar(cursorGetString(cursor, "AVATAR"));

            String remark = cursorGetString(cursor, "REMARK");
            if (!TextUtils.isEmpty(remark)) {
                groupMemEntity.setNick(remark);
            }
            groupMemEntities.add(groupMemEntity);
        }
        if (cursor != null) {
            cursor.close();
        }
        return groupMemEntities;
    }

    /**
     * group member entity
     *
     * @param identify
     * @param memberkey
     * @return
     */
    public List<GroupMemberEntity> loadGroupMemEntities(String identify, String memberkey) {
        String sql = "SELECT M.* , F.REMARK AS REMARK  FROM GROUP_MEMBER_ENTITY M LEFT OUTER JOIN CONTACT_ENTITY F ON M.UID = F.UID " +
                "WHERE M.IDENTIFIER = ? AND M.UID ==? GROUP BY M.UID ORDER BY M.ROLE DESC;";
        Cursor cursor = daoSession.getDatabase().rawQuery(sql, new String[]{identify, memberkey});

        GroupMemberEntity groupMemEntity = null;
        List<GroupMemberEntity> groupMemEntities = new ArrayList<>();
        while (cursor.moveToNext()) {
            groupMemEntity = new GroupMemberEntity();
            groupMemEntity.set_id(cursorGetLong(cursor, "_ID"));
            groupMemEntity.setIdentifier(cursorGetString(cursor, "IDENTIFIER"));
            groupMemEntity.setUid(cursorGetString(cursor, "UID"));
            groupMemEntity.setNick(cursorGetString(cursor, "NICK"));//friend mark
            groupMemEntity.setUsername(cursorGetString(cursor, "USERNAME"));
            groupMemEntity.setRole(cursorGetInt(cursor, "ROLE"));
            groupMemEntity.setAvatar(cursorGetString(cursor, "AVATAR"));

            String remark = cursorGetString(cursor, "REMARK");
            if (!TextUtils.isEmpty(remark)) {
                groupMemEntity.setNick(remark);
            }
            groupMemEntities.add(groupMemEntity);
        }
        if (cursor != null) {
            cursor.close();
        }
        return groupMemEntities;
    }

    public GroupMemberEntity loadGroupMemberEntity(String identify, String publickey) {
        List<GroupMemberEntity> entities = loadGroupMemEntities(identify, publickey);
        return entities == null || entities.size() == 0 ? null : entities.get(0);
    }


    /**
     * group member entity
     *
     * @param identify
     * @param memberkey
     * @return
     */
    public List<GroupMemberEntity> loadGroupMemberEntitiesExcept(String identify, String memberkey) {
        String sql = "SELECT M.* , F.REMARK AS REMARK  FROM GROUP_MEMBER_ENTITY M LEFT OUTER JOIN CONTACT_ENTITY F ON M.UID = F.UID " +
                "WHERE M.IDENTIFIER = ? AND M.UID !=? GROUP BY M.UID ORDER BY M.ROLE DESC;";
        Cursor cursor = daoSession.getDatabase().rawQuery(sql, new String[]{identify, memberkey});

        GroupMemberEntity groupMemEntity = null;
        List<GroupMemberEntity> groupMemEntities = new ArrayList<>();
        while (cursor.moveToNext()) {
            groupMemEntity = new GroupMemberEntity();
            groupMemEntity.set_id(cursorGetLong(cursor, "_ID"));
            groupMemEntity.setIdentifier(cursorGetString(cursor, "IDENTIFIER"));
            groupMemEntity.setUid(cursorGetString(cursor, "UID"));
            groupMemEntity.setNick(cursorGetString(cursor, "NICK"));//friend mark
            groupMemEntity.setUsername(cursorGetString(cursor, "USERNAME"));
            groupMemEntity.setRole(cursorGetInt(cursor, "ROLE"));
            groupMemEntity.setAvatar(cursorGetString(cursor, "AVATAR"));

            String remark = cursorGetString(cursor, "REMARK");
            if (!TextUtils.isEmpty(remark)) {
                groupMemEntity.setNick(remark);
            }
            groupMemEntities.add(groupMemEntity);
        }
        if (cursor != null) {
            cursor.close();
        }
        return groupMemEntities;
    }
    /*********************************  update ***********************************/

    /**
     * modify local friend
     *
     * @param entity
     */
    public void updataFriendSetEntity(ContactEntity entity) {
        contactEntityDao.insertOrReplace(entity);
    }

    /**
     * modify request to be read
     */
    public void updataFriendRequestList() {
        QueryBuilder<FriendRequestEntity> qb = friendRequestEntityDao.queryBuilder();
        qb.where(FriendRequestEntityDao.Properties.Read.eq(0));
        List<FriendRequestEntity> list = qb.list();
        for (FriendRequestEntity requestEntity : list) {
            requestEntity.setRead(1);
            friendRequestEntityDao.update(requestEntity);
        }
    }

    /**
     * modify black list
     *
     * @param uid
     */
    public void updataFriendBlack(String uid, boolean black) {
        String sql = "UPDATE CONTACT_ENTITY SET BLOCKED = " +
                "\"" + (black ? 1 : 0) + "\"" +
                " WHERE UID = ? ;";
        daoSession.getDatabase().execSQL(sql, new Object[]{uid});
    }

    /**
     * modify friend common
     *
     * @param uid
     */
    public void updataFriendCommon(String uid, int common) {
        String sql = "UPDATE CONTACT_ENTITY SET COMMON = " +
                "\"" + common + "\"" +
                " WHERE UID = ? ;";
        daoSession.getDatabase().execSQL(sql, new Object[]{uid});
    }

    /**
     * modify friend common
     *
     * @param uid
     */
    public void updataFriendRemark(String uid, String remark) {
        String sql = "UPDATE CONTACT_ENTITY SET REMARK =  " +
                (TextUtils.isEmpty(remark) ? "''" : "\"" + remark + "\"") +
                " WHERE UID = ? ;";
        daoSession.getDatabase().execSQL(sql, new Object[]{uid});
    }

    public void updateGroupMemberRole(String identify, String uid, Integer role) {
        String sql = "UPDATE GROUP_MEMBER_ENTITY SET ROLE = " +
                (role == null ? 0 : "\"" + role + "\"") +
                " WHERE IDENTIFIER = ? AND (UID = ? OR CONNECT_ID = ?);";
        daoSession.getDatabase().execSQL(sql, new Object[]{identify, uid, uid});
    }

    public void updateGroupMemberNickName(String identify, String uid, String nickname) {
        String sql = "UPDATE GROUP_MEMBER_ENTITY SET NICK = " +
                (TextUtils.isEmpty(nickname) ? "''" : "\"" + nickname + "\"") +
                " WHERE IDENTIFIER = ? AND (UID = ? OR CONNECT_ID = ?);";
        daoSession.getDatabase().execSQL(sql, new Object[]{identify, uid, uid});
    }

    /*********************************  add ***********************************/
    /**
     * friend request (one friend one request
     *
     * @param entity
     */
    public void inserFriendQuestEntity(FriendRequestEntity entity) {
        QueryBuilder<FriendRequestEntity> qb = friendRequestEntityDao.queryBuilder();
        qb.where(FriendRequestEntityDao.Properties.Uid.eq(entity.getUid()));
        List<FriendRequestEntity> list = qb.list();
        if (list.size() > 0) {
            deleteRequestEntity(entity.getUid());
        }
        friendRequestEntityDao.insertOrReplace(entity);
    }

    /**
     * add group
     *
     * @param entity
     */
    public void inserGroupEntity(GroupEntity entity) {
        groupEntityDao.insertOrReplace(entity);
    }

    /**
     * add group member list
     *
     * @param entities
     */
    public void inserGroupMemEntity(List<GroupMemberEntity> entities) {
        groupMemberEntityDao.insertOrReplaceInTx(entities);
    }

    /********************************* delete ***********************************/
    /**
     * remove friend and remove room list/message/file
     *
     * @param uid
     */
    public void removeFriend(String uid) {
        ConversionHelper.getInstance().deleteRoom(uid);
        MessageHelper.getInstance().deleteRoomMsg(uid);
        FileUtil.deleteContactFile(uid);

        ConversationAction.conversationAction.sendEvent(ConversationAction.ConverType.LOAD_MESSAGE);
    }

    public void deleteEntity(String uid) {
        QueryBuilder<ContactEntity> qb = contactEntityDao.queryBuilder();
        DeleteQuery<ContactEntity> bd = qb.where(ContactEntityDao.Properties.Uid.eq(uid))
                .buildDelete();
        bd.executeDeleteWithoutDetachingEntities();
    }

    /**
     * remove friend
     *
     * @param uid
     */
    public void deleteRequestEntity(String uid) {
        QueryBuilder<FriendRequestEntity> qb = friendRequestEntityDao.queryBuilder();
        DeleteQuery<FriendRequestEntity> bd = qb.where(FriendRequestEntityDao.Properties.Uid.eq(uid))
                .buildDelete();
        bd.executeDeleteWithoutDetachingEntities();
    }

    /**
     * remove group(exit group/dissolution group)
     */
    public void removeGroupInfos(String groupKey) {
        ConversionHelper.getInstance().deleteRoom(groupKey);
        MessageHelper.getInstance().clearMsgByRoomkey(groupKey);
        quitGroup(groupKey);

        ContactNotice.receiverGroup();
        ConversationAction.conversationAction.sendEvent(ConversationAction.ConverType.LOAD_MESSAGE);
    }

    /**
     * exit/be removed group
     *
     * @param groupkey
     */
    public void quitGroup(String groupkey) {
        removeGroupEntity(groupkey);
        removeMemberEntity(groupkey);
    }

    /**
     * remove group
     *
     * @param groupkey
     */
    public void removeGroupEntity(String groupkey) {
        QueryBuilder<GroupEntity> qb = groupEntityDao.queryBuilder();
        DeleteQuery<GroupEntity> bd = qb.where(GroupEntityDao.Properties.Identifier.eq(groupkey)).buildDelete();
        bd.executeDeleteWithoutDetachingEntities();
    }

    /**
     * remove all member
     *
     * @param groupkey
     */
    public void removeMemberEntity(String groupkey) {
        QueryBuilder<GroupMemberEntity> qb = groupMemberEntityDao.queryBuilder();
        DeleteQuery<GroupMemberEntity> bd = qb.where(GroupMemberEntityDao.Properties.Identifier.eq(groupkey)).buildDelete();
        bd.executeDeleteWithoutDetachingEntities();
    }

    /**
     * remove member
     *
     * @param groupkey
     * @param uid
     */
    public void removeMemberEntity(String groupkey, String uid) {
        QueryBuilder<GroupMemberEntity> qb = groupMemberEntityDao.queryBuilder();
        DeleteQuery<GroupMemberEntity> bd = qb.where(
                GroupMemberEntityDao.Properties.Identifier.eq(groupkey),
                GroupMemberEntityDao.Properties.Uid.eq(uid))
                .buildDelete();
        bd.executeDeleteWithoutDetachingEntities();
    }

}
