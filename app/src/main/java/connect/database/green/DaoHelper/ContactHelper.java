package connect.database.green.DaoHelper;

import android.database.Cursor;
import android.text.TextUtils;

import org.greenrobot.greendao.query.DeleteQuery;
import org.greenrobot.greendao.query.QueryBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import connect.database.green.BaseDao;
import connect.database.green.bean.ContactEntity;
import connect.database.green.bean.FriendRequestEntity;
import connect.database.green.bean.GroupEntity;
import connect.database.green.bean.GroupMemberEntity;
import connect.database.green.bean.RecommandFriendEntity;
import connect.database.green.dao.ContactEntityDao;
import connect.database.green.dao.FriendRequestEntityDao;
import connect.database.green.dao.GroupEntityDao;
import connect.database.green.dao.GroupMemberEntityDao;
import connect.database.green.dao.RecommandFriendEntityDao;
import connect.ui.activity.R;
import connect.activity.contact.bean.ContactNotice;
import connect.activity.home.bean.MsgFragmReceiver;
import connect.activity.base.BaseApplication;
import connect.utils.FileUtil;
import protos.Connect;

/**
 * The address book (friends/group)
 * Created by gtq on 2016/11/22.
 */
public class ContactHelper extends BaseDao {

    private static ContactHelper contactHelper;
    private FriendRequestEntityDao friendRequestEntityDao;
    private RecommandFriendEntityDao recommandFriendEntityDao;
    private ContactEntityDao contactEntityDao;
    private GroupEntityDao groupEntityDao;
    private GroupMemberEntityDao groupMemberEntityDao;

    private static String Tag = "ContactHelper";

    public ContactHelper() {
        super();
        contactEntityDao = daoSession.getContactEntityDao();
        groupEntityDao = daoSession.getGroupEntityDao();
        groupMemberEntityDao = daoSession.getGroupMemberEntityDao();
        friendRequestEntityDao = daoSession.getFriendRequestEntityDao();
        recommandFriendEntityDao = daoSession.getRecommandFriendEntityDao();
    }

    public static ContactHelper getInstance() {
        if (contactHelper == null) {
            synchronized (ContactHelper.class) {
                if (contactHelper == null) {
                    contactHelper = new ContactHelper();
                }
            }
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
        queryBuilder.where(ContactEntityDao.Properties.Pub_key.notEq(connect)).build();
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
        queryBuilder.where(ContactEntityDao.Properties.Pub_key.notIn(pubkeys)).build();
        List<ContactEntity> friendEntities = queryBuilder.list();
        return friendEntities;
    }

    /*********************************  select ***********************************/
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
        queryBuilder.whereOr(ContactEntityDao.Properties.Pub_key.eq(value), ContactEntityDao.Properties.Address.eq(value)).limit(1).build();
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
        queryBuilder.where(FriendRequestEntityDao.Properties.Address.eq(address)).limit(1).build();
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
     * @param pukkey
     * @return
     */
    public GroupEntity loadGroupEntity(String pukkey) {
        QueryBuilder<GroupEntity> queryBuilder = groupEntityDao.queryBuilder();
        queryBuilder.where(GroupEntityDao.Properties.Identifier.eq(pukkey)).build();
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
        String sql = "SELECT M.* , F.REMARK AS REMARK  FROM GROUP_MEMBER_ENTITY M LEFT OUTER JOIN CONTACT_ENTITY F ON M.PUB_KEY = F.PUB_KEY " +
                "WHERE M.IDENTIFIER = ? GROUP BY M.IDENTIFIER ,M.ADDRESS ORDER BY M.ROLE DESC;";
        Cursor cursor = daoSession.getDatabase().rawQuery(sql, new String[]{pukkey});

        GroupMemberEntity groupMemEntity = null;
        List<GroupMemberEntity> groupMemEntities = new ArrayList<>();
        while (cursor.moveToNext()) {
            groupMemEntity = new GroupMemberEntity();
            groupMemEntity.set_id(cursorGetLong(cursor, "_ID"));
            groupMemEntity.setIdentifier(cursorGetString(cursor, "IDENTIFIER"));
            groupMemEntity.setPub_key(cursorGetString(cursor, "PUB_KEY"));
            groupMemEntity.setNick(cursorGetString(cursor, "NICK"));
            groupMemEntity.setUsername(cursorGetString(cursor, "USERNAME"));
            groupMemEntity.setAddress(cursorGetString(cursor, "ADDRESS"));
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
     * group member entity(except yourself)
     *
     * @param identify
     * @param memberkey
     * @return
     */
    public List<GroupMemberEntity> loadGroupMemEntities(String identify,String memberkey) {
        String sql = "SELECT M.* , F.REMARK AS REMARK  FROM GROUP_MEMBER_ENTITY M LEFT OUTER JOIN CONTACT_ENTITY F ON M.PUB_KEY = F.PUB_KEY " +
                "WHERE M.IDENTIFIER = ? AND ( M.ADDRESS == ? OR M.PUB_KEY ==? ) GROUP BY M.PUB_KEY ORDER BY M.ROLE DESC;";
        Cursor cursor = daoSession.getDatabase().rawQuery(sql, new String[]{identify, memberkey, memberkey});

        GroupMemberEntity groupMemEntity = null;
        List<GroupMemberEntity> groupMemEntities = new ArrayList<>();
        while (cursor.moveToNext()) {
            groupMemEntity = new GroupMemberEntity();
            groupMemEntity.set_id(cursorGetLong(cursor, "_ID"));
            groupMemEntity.setIdentifier(cursorGetString(cursor, "IDENTIFIER"));
            groupMemEntity.setPub_key(cursorGetString(cursor, "PUB_KEY"));
            groupMemEntity.setNick(cursorGetString(cursor, "NICK"));//friend mark
            groupMemEntity.setUsername(cursorGetString(cursor, "USERNAME"));
            groupMemEntity.setAddress(cursorGetString(cursor, "ADDRESS"));
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
     * query recommand friend
     *
     * @return
     */
    public List<RecommandFriendEntity> loadRecommendEntity(int page, int pageSize) {
        QueryBuilder<RecommandFriendEntity> queryBuilder = recommandFriendEntityDao.queryBuilder();
        queryBuilder.where(RecommandFriendEntityDao.Properties.Status.eq(0)).build();
        queryBuilder.offset((page - 1) * pageSize);
        queryBuilder.limit(pageSize);
        List<RecommandFriendEntity> list = queryBuilder.list();
        Collections.reverse(list);
        return list;
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
     * @param pubKey
     */
    public void updataFriendBlack(String pubKey) {
        QueryBuilder<ContactEntity> qb = contactEntityDao.queryBuilder();
        qb.where(ContactEntityDao.Properties.Pub_key.eq(pubKey));
        List<ContactEntity> list = qb.list();
        if (list.size() > 0) {
            ContactEntity friendEntity = list.get(0);
            friendEntity.setBlocked(true);
            contactEntityDao.insertOrReplace(friendEntity);
        }
    }

    /**
     * modify recommend friend
     */
    public void updataRecommendFriend(String pubKey) {
        QueryBuilder<RecommandFriendEntity> qb = recommandFriendEntityDao.queryBuilder();
        qb.where(RecommandFriendEntityDao.Properties.Pub_key.eq(pubKey));
        List<RecommandFriendEntity> list = qb.list();
        if (list.size() > 0) {
            RecommandFriendEntity recommendEntity = list.get(0);
            recommendEntity.setStatus(1);
            recommandFriendEntityDao.insertOrReplace(recommendEntity);
        }
    }

    public void updateGroupMemberRole(String identify, String publickey, Integer role) {
        updateGroupMember(identify, publickey, null, null, role, null);
    }

    public void updateGroupMemberNickName(String identify, String publickey, String nickname) {
        updateGroupMember(identify, publickey, null, null, null, nickname);
    }

    public void updateGroupMember(String identify, String publickey, String username, String avatar, Integer role, String nickname) {
        String sql = "UPDATE GROUP_MEMBER_ENTITY SET " +
                (TextUtils.isEmpty(username) ? " " : "USERNAME = " + username + " , ") +
                (TextUtils.isEmpty(avatar) ? " " : "AVATAR = " + avatar + " , ") +
                (role == null ? " " : "ROLE =  " + role + " ") +
                (TextUtils.isEmpty(nickname) ? " " : "NICK = '" + nickname + "' ") +
                "WHERE IDENTIFIER = ? AND (PUB_KEY = ? OR ADDRESS = ?);";
        daoSession.getDatabase().execSQL(sql, new Object[]{identify, publickey, publickey});
    }

    /*********************************  add ***********************************/
    /**
     * friend request (one friend one request
     *
     * @param entity
     */
    public void inserFriendQuestEntity(FriendRequestEntity entity) {
        QueryBuilder<FriendRequestEntity> qb = friendRequestEntityDao.queryBuilder();
        qb.where(FriendRequestEntityDao.Properties.Pub_key.eq(entity.getPub_key()));
        List<FriendRequestEntity> list = qb.list();
        if (list.size() > 0) {
            deleteRequestEntity(entity.getPub_key());
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

    /**
     * add group recommend friend
     *
     * @param entities
     */
    public void inserRecommendEntity(List<Connect.UserInfo> entities) {
        for (Connect.UserInfo userInfo : entities) {
            RecommandFriendEntity recommendEntity = new RecommandFriendEntity();
            recommendEntity.setPub_key(userInfo.getPubKey());
            recommendEntity.setUsername(userInfo.getUsername());
            recommendEntity.setAddress(userInfo.getAddress());
            recommendEntity.setAvatar(userInfo.getAvatar());
            recommendEntity.setStatus(userInfo.getRecommend() ? 1 : 0);
            inserRecommendEntity(recommendEntity);
        }
    }

    public void inserRecommendEntity(RecommandFriendEntity entity) {
        QueryBuilder<RecommandFriendEntity> qb = recommandFriendEntityDao.queryBuilder();
        qb.where(RecommandFriendEntityDao.Properties.Pub_key.eq(entity.getPub_key()));
        List<RecommandFriendEntity> list = qb.list();
        if (list.size() > 0) {
            return;
        }
        entity.setStatus(0);
        recommandFriendEntityDao.insert(entity);
    }

    /********************************* delete ***********************************/
    /**
     * remove friend and remove room list/message/file
     *
     * @param pubkey
     */
    public void removeFriend(String pubkey) {
        ConversionHelper.getInstance().deleteRoom(pubkey);
        MessageHelper.getInstance().deleteRoomMsg(pubkey);
        FileUtil.deleteContactFile(pubkey);

        MsgFragmReceiver.refreshRoom();
    }

    public void deleteEntity(String address) {
        QueryBuilder<ContactEntity> qb = contactEntityDao.queryBuilder();
        DeleteQuery<ContactEntity> bd = qb.where(ContactEntityDao.Properties.Address.eq(address))
                .buildDelete();
        bd.executeDeleteWithoutDetachingEntities();
    }

    /**
     * remove friend
     *
     * @param pubKey
     */
    public void deleteRequestEntity(String pubKey) {
        QueryBuilder<FriendRequestEntity> qb = friendRequestEntityDao.queryBuilder();
        DeleteQuery<FriendRequestEntity> bd = qb.where(FriendRequestEntityDao.Properties.Pub_key.eq(pubKey))
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
        MsgFragmReceiver.refreshRoom();
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
     * @param pubkey
     */
    public void removeMemberEntity(String groupkey, String pubkey) {
        QueryBuilder<GroupMemberEntity> qb = groupMemberEntityDao.queryBuilder();
        DeleteQuery<GroupMemberEntity> bd = qb.where(GroupMemberEntityDao.Properties.Identifier.eq(groupkey),
                qb.or(GroupMemberEntityDao.Properties.Identifier.eq(pubkey), GroupMemberEntityDao.Properties.Address.eq(pubkey))).buildDelete();
        bd.executeDeleteWithoutDetachingEntities();
    }

    /**
     * remove recommend entity
     *
     * @param pubkey
     */
    public void removeRecommendEntity(String pubkey) {
        QueryBuilder<RecommandFriendEntity> qb = recommandFriendEntityDao.queryBuilder();
        DeleteQuery<RecommandFriendEntity> bd = qb.where(RecommandFriendEntityDao.Properties.Pub_key.eq(pubkey)).buildDelete();
        bd.executeDeleteWithoutDetachingEntities();
    }

}
