package connect.instant.receiver;

import android.content.Context;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import connect.activity.base.BaseApplication;
import connect.activity.chat.bean.RecExtBean;
import connect.activity.chat.bean.Talker;
import connect.activity.contact.bean.ContactNotice;
import connect.activity.contact.bean.MsgSendBean;
import connect.activity.contact.model.ConvertUtil;
import connect.activity.home.bean.GroupRecBean;
import connect.activity.home.bean.HomeAction;
import connect.activity.home.bean.HttpRecBean;
import connect.activity.home.bean.MsgNoticeBean;
import connect.database.SharedPreferenceUtil;
import connect.database.green.DaoHelper.ContactHelper;
import connect.database.green.DaoHelper.MessageHelper;
import connect.database.green.bean.ContactEntity;
import connect.database.green.bean.FriendRequestEntity;
import connect.database.green.bean.GroupEntity;
import connect.database.green.bean.GroupMemberEntity;
import connect.database.green.bean.MessageEntity;
import connect.instant.inter.ConversationListener;
import connect.instant.model.CFriendChat;
import connect.instant.model.CGroupChat;
import connect.instant.model.CRobotChat;
import connect.ui.activity.R;
import connect.utils.RegularUtil;
import connect.utils.StringUtil;
import connect.utils.TimeUtil;
import connect.utils.cryption.DecryptionUtil;
import connect.utils.cryption.EncryptionUtil;
import connect.utils.cryption.SupportKeyUril;
import instant.bean.ChatMsgEntity;
import instant.parser.inter.CommandListener;
import instant.sender.model.NormalChat;
import instant.utils.manager.FailMsgsManager;
import protos.Connect;

/**
 * Created by Administrator on 2017/10/10.
 */
public class CommandReceiver implements CommandListener {

    private static String TAG = "_CommandReceiver";

    public static CommandReceiver receiver = getInstance();

    private synchronized static CommandReceiver getInstance() {
        if (receiver == null) {
            receiver = new CommandReceiver();
        }
        return receiver;
    }

    @Override
    public void commandReceipt(boolean isSuccess, Object reqObj, Object serviceObj) {
        MsgNoticeBean.sendMsgNotice(
                isSuccess ? MsgNoticeBean.NtEnum.MSG_SEND_SUCCESS : MsgNoticeBean.NtEnum.MSG_SEND_FAIL,
                reqObj,
                serviceObj);
    }

    @Override
    public void updateMsgSendState(String publickey, String msgid, int state) {
        MessageEntity msgEntity = MessageEntity.chatMsgToMessageEntity(MessageHelper.getInstance().loadMsgByMsgid(msgid));
        if (msgEntity != null) {
            msgEntity.setSend_status(state);
            MessageHelper.getInstance().updateMsg(msgEntity);
        }

        if (TextUtils.isEmpty(publickey)) {
            Map<String, Object> failMap = FailMsgsManager.getInstance().getFailMap(msgid);
            if (failMap != null) {
                Object object = failMap.get("EXT");
                if (object instanceof MsgSendBean) {
                    MsgNoticeBean.sendMsgNotice(MsgNoticeBean.NtEnum.MSG_SEND_SUCCESS, failMap.get("EXT"));
                }
            }
        } else {
            RecExtBean.getInstance().sendEvent(RecExtBean.ExtType.MSGSTATE, publickey, msgid, state);
        }
    }

    @Override
    public void loadAllContacts(Connect.SyncUserRelationship userRelationship) throws Exception {
        Connect.RelationShip relationShip = userRelationship.getRelationShip();
        List<Connect.FriendInfo> friendInfoList = relationShip.getFriendsList();

        Map<String, ContactEntity> contactEntityMap = new HashMap<>();
        for (Connect.FriendInfo friendInfo : friendInfoList) {
            String friendUid = friendInfo.getUid();

            ContactEntity contactEntity = new ContactEntity();
            contactEntity.setUid(friendInfo.getUid());
            contactEntity.setCa_pub(friendInfo.getCaPub());
            contactEntity.setConnectId(friendInfo.getConnectId());
            contactEntity.setUsername(friendInfo.getUsername());
            contactEntity.setAvatar(friendInfo.getAvatar());
            contactEntity.setCommon(friendInfo.getCommon() ? 1 : 0);
            contactEntity.setSource(friendInfo.getSource());
            contactEntity.setRemark(friendInfo.getRemark());

            contactEntityMap.put(friendUid,contactEntity);
        }
        Collection<ContactEntity> contactEntityCollection = contactEntityMap.values();
        List<ContactEntity> friendInfoEntities=new ArrayList<ContactEntity>(contactEntityCollection);


        //To add a system message contact
        String connect = BaseApplication.getInstance().getString(R.string.app_name);
        ContactEntity connectEntity = ContactHelper.getInstance().loadFriendEntity(connect);
        if (connectEntity == null) {
            connectEntity = new ContactEntity();
            connectEntity.setUid(connect);
            connectEntity.setCa_pub(connect);
            connectEntity.setUsername(connect);
            connectEntity.setUid(connect);
            connectEntity.setSource(-1);

            friendInfoEntities.add(connectEntity);
        }
        ContactHelper.getInstance().insertContacts(friendInfoEntities);

        //Synchronous common group
        Connect.UserCommonGroups commonGroups = userRelationship.getUserCommonGroups();
        List<Connect.GroupInfo> groupInfos = commonGroups.getGroupsList();
        for (Connect.GroupInfo groupInfo : groupInfos) {
            Connect.Group group = groupInfo.getGroup();

            String groupKey = group.getIdentifier();
            String[] collaboratives = groupInfo.getEcdh().split("/");
            if (collaboratives.length < 2) {//Download failed
                GroupRecBean.sendGroupRecMsg(GroupRecBean.GroupRecType.GroupInfo, groupKey);
            } else {// Download successful
                GroupEntity groupEntity = ContactHelper.getInstance().loadGroupEntity(groupKey);
                if (groupEntity == null) {
                    String randPubkey = collaboratives[0];
                    byte[] ecdhkey = SupportKeyUril.getRawECDHKey(SharedPreferenceUtil.getInstance().getUser().getPriKey(), randPubkey);
                    Connect.GcmData gcmData = Connect.GcmData.parseFrom(StringUtil.hexStringToBytes(collaboratives[1]));
                    byte[] ecdhbytes = DecryptionUtil.decodeAESGCM(EncryptionUtil.ExtendedECDH.EMPTY, ecdhkey, gcmData);
                    String groupEcdh = StringUtil.bytesToHexString(ecdhbytes);

                    groupEntity = new GroupEntity();
                    groupEntity.setIdentifier(groupKey);
                    groupEntity.setVerify(group.getPublic() ? 1 : 0);
                    String groupname = group.getName();
                    if (TextUtils.isEmpty(groupname)) {
                        groupname = "groupname1";
                    }
                    groupEntity.setName(groupname);
                    groupEntity.setCommon(1);
                    groupEntity.setAvatar(RegularUtil.groupAvatar(groupKey));
                    groupEntity.setEcdh_key(groupEcdh);
                    ContactHelper.getInstance().inserGroupEntity(groupEntity);
                }
            }

            Map<String, GroupMemberEntity> memberEntityMap = new HashMap<>();
            List<Connect.GroupMember> members = groupInfo.getMembersList();
            for (Connect.GroupMember member : members) {
                GroupMemberEntity memberEntity = new GroupMemberEntity();
                memberEntity.setIdentifier(groupKey);
                memberEntity.setUid(member.getUid());
                memberEntity.setAvatar(member.getAvatar());
                memberEntity.setUsername(member.getUsername());
                memberEntity.setNick(member.getNick());
                memberEntity.setRole(member.getRole());
                memberEntity.setUsername(member.getUsername());
                memberEntityMap.put(member.getUid(), memberEntity);
            }
            Collection<GroupMemberEntity> memberEntityCollection = memberEntityMap.values();
            List<GroupMemberEntity> memEntities = new ArrayList<GroupMemberEntity>(memberEntityCollection);
            ContactHelper.getInstance().inserGroupMemEntity(memEntities);
        }
    }

    @Override
    public void contactChanges(Connect.ChangeRecords changeRecords) {
        List<Connect.ChangeRecord> recordsList = changeRecords.getChangeRecordsList();
        for (Connect.ChangeRecord record : recordsList) {
            Connect.FriendInfo friendInfo = record.getFriendInfo();
            String uid = friendInfo.getUid();

            ContactEntity entity = ContactHelper.getInstance().loadFriendEntity(uid);
            switch (record.getCategory()) {
                case "del":
                    ContactHelper.getInstance().deleteEntity(uid);
                    break;
                case "add":
                    boolean newFriend = false;
                    if (entity == null) {
                        newFriend = true;
                        entity = new ContactEntity();
                    }

                    entity.setUid(uid);
                    entity.setCa_pub(friendInfo.getCaPub());
                    entity.setConnectId(friendInfo.getConnectId());
                    entity.setUsername(friendInfo.getUsername());
                    entity.setAvatar(friendInfo.getAvatar());
                    entity.setCommon(friendInfo.getCommon() ? 1 : 0);
                    entity.setBlocked(friendInfo.getBlackList());
                    entity.setRemark(friendInfo.getRemark());
                    entity.setSource(friendInfo.getSource());
                    ContactHelper.getInstance().insertContact(entity);

                    if (newFriend) { // Add a welcome message
                        CFriendChat normalChat = new CFriendChat(entity);
                        normalChat.createWelcomeMessage();
                    }
                    FailMsgsManager.getInstance().receiveFailMsgs(uid);
                    break;
                case "common":
                    if (entity != null) {
                        ContactHelper.getInstance().updataFriendCommon(uid, 1);
                    }
                    break;
                case "common_del":
                    if (entity != null) {
                        ContactHelper.getInstance().updataFriendCommon(uid, 0);
                    }
                    break;
                case "black":
                    if (entity != null) {
                        ContactHelper.getInstance().updataFriendBlack(uid, true);
                    }
                    break;
                case "black_del":
                    if (entity != null) {
                        ContactHelper.getInstance().updataFriendBlack(uid, false);
                    }
                    break;
                case "remark":
                    if (entity != null) {
                        ContactHelper.getInstance().updataFriendRemark(uid, friendInfo.getRemark());
                    }
                    break;
            }
        }
    }

    @Override
    public void receiverFriendRequest(Connect.ReceiveFriendRequest friendRequest) {
        if (friendRequest != null && friendRequest.getSender() != null && !friendRequest.getSender().getUid().equals("")) {
            ConvertUtil convertUtil = new ConvertUtil();
            ContactHelper.getInstance().inserFriendQuestEntity(convertUtil.convertFriendRequestEntity(friendRequest));
            ContactNotice.receiverAddFriend();
        }
    }

    @Override
    public void acceptFriendRequest(Connect.FriendListChange listChange) {
        Connect.FriendInfo friendInfo = listChange.getChange().getFriendInfo();

        boolean newFriend = false;
        FriendRequestEntity friendRequestEntity = ContactHelper.getInstance().loadFriendRequest(friendInfo.getUid());
        if (friendRequestEntity == null) {
            newFriend = true;
        } else {
            friendRequestEntity.setStatus(2);
            ContactHelper.getInstance().inserFriendQuestEntity(friendRequestEntity);
        }

        ContactEntity contactEntity = new ContactEntity();
        contactEntity.setUid(friendInfo.getUid());
        contactEntity.setUsername(friendInfo.getUsername());
        contactEntity.setAvatar(friendInfo.getAvatar());
        contactEntity.setCa_pub(friendInfo.getCaPub());
        contactEntity.setConnectId(friendInfo.getConnectId());
        contactEntity.setRemark(friendInfo.getRemark());
        contactEntity.setBlocked(friendInfo.getBlackList());
        contactEntity.setCommon(friendInfo.getCommon()?1:0);
        contactEntity.setSource(friendInfo.getSource());
        ContactHelper.getInstance().insertContact(contactEntity);

        ContactNotice.receiverFriend();

        if (newFriend) {
            CFriendChat normalChat = new CFriendChat(contactEntity);
            normalChat.createWelcomeMessage();
        }
        FailMsgsManager.getInstance().receiveFailMsgs(contactEntity.getUid());
    }

    @Override
    public void acceptDelFriend(Connect.FriendListChange listChange) {
        Connect.FriendInfo friendInfo = listChange.getChange().getFriendInfo();

        ContactHelper.getInstance().deleteEntity(friendInfo.getUid());
        ContactNotice.receiverFriend();
    }

    @Override
    public void conversationMute(Connect.ManageSession manageSession) {

    }

    @Override
    public void updateGroupChange(Connect.GroupChange groupChange) throws Exception {
        Context context = BaseApplication.getInstance().getBaseContext();
        String groupKey = "";
        String memberUid ="";
        String noticeStr = "";

        GroupEntity groupEntity = null;
        NormalChat normalChat = null;
        List<GroupMemberEntity> groupMemEntities = null;
        switch (groupChange.getChangeType()) {
            case 0://Group of information change
                Connect.Group group = Connect.Group.parseFrom(groupChange.getDetail());
                groupKey = group.getIdentifier();
                groupEntity = ContactHelper.getInstance().loadGroupEntity(groupKey);
                if (groupEntity == null || TextUtils.isEmpty(group.getName()) || TextUtils.isEmpty(groupEntity.getEcdh_key())) {
                    GroupRecBean.sendGroupRecMsg(GroupRecBean.GroupRecType.GroupInfo, groupKey);

                    FailMsgsManager.getInstance().insertReceiveMsg(group.getIdentifier(), TimeUtil.timestampToMsgid(), context.getString(R.string.Link_Join_Group));
                } else {
                    String groupname = group.getName();
                    groupEntity.setName(groupname);
                    ContactHelper.getInstance().inserGroupEntity(groupEntity);
                    CGroupChat groupChat = new CGroupChat(groupEntity);
                    groupChat.updateRoomMsg(null, "", -1, -1, -1);

                    RecExtBean.getInstance().sendEvent(RecExtBean.ExtType.GROUP_UPDATENAME, groupKey, groupname);
                    ContactNotice.receiverGroup();
                }
                break;
            case 1://Add members
                groupKey = groupChange.getIdentifier();
                groupEntity = ContactHelper.getInstance().loadGroupEntity(groupKey);
                Connect.UsersInfo usersInfo = Connect.UsersInfo.parseFrom(groupChange.getDetail());
                List<Connect.UserInfo> userInfos = usersInfo.getUsersList();

                if (groupEntity == null) {//The request of details
                    GroupRecBean.sendGroupRecMsg(GroupRecBean.GroupRecType.GroupInfo, groupKey);
                } else {
                    Map<String, GroupMemberEntity> memberEntityMap = new HashMap<>();
                    for (Connect.UserInfo info : userInfos) {
                        GroupMemberEntity groupMemEntity = new GroupMemberEntity();
                        groupMemEntity.setIdentifier(groupKey);
                        groupMemEntity.setUid(info.getUid());
                        groupMemEntity.setUsername(info.getUsername());
                        groupMemEntity.setNick(info.getUsername());
                        groupMemEntity.setAvatar(info.getAvatar());
                        groupMemEntity.setRole(0);
                        memberEntityMap.put(info.getUid(), groupMemEntity);
                    }
                    Collection<GroupMemberEntity> memberEntityCollection = memberEntityMap.values();
                    List<GroupMemberEntity> memEntities = new ArrayList<GroupMemberEntity>(memberEntityCollection);
                    ContactHelper.getInstance().inserGroupMemEntity(memEntities);

                    normalChat = new CGroupChat(groupEntity);
                    for (GroupMemberEntity memEntity : memEntities) {
                        String memberName = TextUtils.isEmpty(memEntity.getUsername()) ? memEntity.getNick() : memEntity.getUsername();
                        if (groupChange.hasInviteBy()) {
                            String myUid = SharedPreferenceUtil.getInstance().getUser().getUid();

                            Connect.UserInfo inviteBy = groupChange.getInviteBy();
                            String inviteByName = inviteBy.getUid().equals(myUid) ?
                                    context.getString(R.string.Chat_You) : inviteBy.getUsername();

                            String invitorname = memEntity.getUid().equals(myUid) ?
                                    context.getString(R.string.Chat_You) : memberName;
                            noticeStr = context.getString(R.string.Link_invited_to_the_group_chat, inviteByName, invitorname);
                        } else {
                            noticeStr = context.getString(R.string.Link_enter_the_group, memberName);
                        }

                        if (normalChat == null) {
                            FailMsgsManager.getInstance().insertReceiveMsg(groupKey, TimeUtil.timestampToMsgid(), noticeStr);
                        } else {
                            ChatMsgEntity msgExtEntity = normalChat.noticeMsg(0,noticeStr,"");
                            MessageHelper.getInstance().insertMsgExtEntity(msgExtEntity);

                            RecExtBean.getInstance().sendEvent(RecExtBean.ExtType.MESSAGE_RECEIVE, groupKey, msgExtEntity);
                            ((ConversationListener)normalChat).updateRoomMsg(null, msgExtEntity.showContent(), msgExtEntity.getCreatetime(), -1, 1);
                        }
                    }
                }
                break;
            case 2://Remove the group members
                groupKey = groupChange.getIdentifier();
                Connect.QuitGroupUserAddress quitGroup = Connect.QuitGroupUserAddress.parseFrom(groupChange.getDetail());
                for (String uid : quitGroup.getUidsList()) {
                    ContactHelper.getInstance().removeMemberEntity(groupKey, uid);
                }
                break;
            case 3://Group of personal information changes
                Connect.ChangeGroupNick groupNick = Connect.ChangeGroupNick.parseFrom(groupChange.getDetail());
                groupKey = groupChange.getIdentifier();
                memberUid = groupNick.getUid();
                String memberNick = groupNick.getNick();
                ContactHelper.getInstance().updateGroupMemberNickName(groupKey, memberUid, memberNick);
                break;
            case 4://Group change
                groupKey = groupChange.getIdentifier();
                Connect.GroupAttorn groupAttorn = Connect.GroupAttorn.parseFrom(groupChange.getDetail());
                groupMemEntities = ContactHelper.getInstance().loadGroupMemEntities(groupKey);
                if (groupMemEntities == null) {
                    break;
                }
                for (GroupMemberEntity member : groupMemEntities) {
                    if (member.getRole() == 1) {//The old group manager
                        memberUid = member.getUid();
                        ContactHelper.getInstance().updateGroupMemberRole(groupKey, memberUid, 0);
                    }

                    if (member.getUid().equals(groupAttorn.getUid())) {//The new group manager
                        memberUid = member.getUid();
                        ContactHelper.getInstance().updateGroupMemberRole(groupKey, memberUid, 1);

                        String showName = "";
                        if (groupAttorn.getUid().equals(SharedPreferenceUtil.getInstance().getUser().getUid())) {
                            showName = context.getString(R.string.Chat_You);
                        } else {
                            showName = TextUtils.isEmpty(member.getNick()) ? member.getUsername() : member.getNick();
                        }
                        noticeStr = context.getString(R.string.Link_become_new_group_owner, showName);

                        normalChat = new CGroupChat(groupEntity);
                        ChatMsgEntity msgExtEntity = normalChat.noticeMsg(0,noticeStr,"");
                        MessageHelper.getInstance().insertMsgExtEntity(msgExtEntity);

                        RecExtBean.getInstance().sendEvent(RecExtBean.ExtType.MESSAGE_RECEIVE, groupKey, msgExtEntity);
                        ((ConversationListener)normalChat).updateRoomMsg(null, msgExtEntity.showContent(), msgExtEntity.getCreatetime(), -1, 1);
                    }
                }
                break;
            case 5://Group set the switch
                Connect.GroupSetting groupSetting = Connect.GroupSetting.parseFrom(groupChange.getDetail());

                groupKey = groupSetting.getIdentifier();
                groupEntity = ContactHelper.getInstance().loadGroupEntity(groupKey);
                if (groupEntity == null || TextUtils.isEmpty(groupEntity.getName()) || TextUtils.isEmpty(groupEntity.getEcdh_key())) {
                    GroupRecBean.sendGroupRecMsg(GroupRecBean.GroupRecType.GroupInfo, groupKey);
                } else {
                    groupEntity.setSummary(groupSetting.getSummary());
                    groupEntity.setVerify(groupSetting.getPublic() ? 1 : 0);

                    String groupname = groupEntity.getName();
                    if (TextUtils.isEmpty(groupname)) {
                        groupname = "groupname3";
                    }
                    groupEntity.setName(groupname);
                    ContactHelper.getInstance().inserGroupEntity(groupEntity);
                }
                break;
        }
    }

    @Override
    public void handlerOuterRedPacket(Connect.ExternalRedPackageInfo packageInfo) {
        String mypublickey = SharedPreferenceUtil.getInstance().getUser().getPubKey();
        if (packageInfo.getSystem()) {
            ChatMsgEntity msgExtEntity = CRobotChat.getInstance().luckPacketMsg(1,packageInfo.getHashId(), 0L,packageInfo.getTips());
            msgExtEntity.setMessage_from(BaseApplication.getInstance().getString(R.string.app_name));
            msgExtEntity.setMessage_to(mypublickey);

            MessageHelper.getInstance().insertMsgExtEntity(msgExtEntity);
            CRobotChat.getInstance().updateRoomMsg(null, msgExtEntity.showContent(), msgExtEntity.getCreatetime(), -1, 1);
            HomeAction.getInstance().sendEvent(HomeAction.HomeType.TOCHAT,
                    new Talker(Connect.ChatType.CONNECT_SYSTEM, BaseApplication.getInstance().getBaseContext().getString(R.string.app_name)));
        } else {
            Connect.UserInfo userInfo = packageInfo.getSender();

            ContactEntity friendEntity = ContactHelper.getInstance().loadFriendEntity(userInfo.getPubKey());
            if (friendEntity == null) {
                friendEntity = new ContactEntity();
                friendEntity.setCa_pub(userInfo.getCaPub());
                friendEntity.setAvatar(userInfo.getAvatar());
                friendEntity.setUsername(userInfo.getUsername());
                friendEntity.setUid(userInfo.getUid());
            }

            CFriendChat normalChat = new CFriendChat(friendEntity);
            ChatMsgEntity msgExtEntity = normalChat.luckPacketMsg(1, packageInfo.getHashId(), 0L,packageInfo.getTips());
            msgExtEntity.setMessage_from(friendEntity.getCa_pub());
            msgExtEntity.setMessage_to(mypublickey);
            msgExtEntity.setSend_status(1);

            MessageHelper.getInstance().insertMsgExtEntity(msgExtEntity);
            normalChat.updateRoomMsg(null, msgExtEntity.showContent(), msgExtEntity.getCreatetime());
            HomeAction.getInstance().sendEvent(HomeAction.HomeType.TOCHAT, new Talker(Connect.ChatType.PRIVATE,friendEntity.getUid()));
        }
    }
}
