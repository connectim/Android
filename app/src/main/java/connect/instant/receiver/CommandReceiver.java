package connect.instant.receiver;

import android.content.Context;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

import connect.activity.base.BaseApplication;
import connect.activity.chat.bean.RecExtBean;
import connect.activity.chat.bean.Talker;
import connect.activity.contact.bean.ContactNotice;
import connect.activity.contact.model.ConvertUtil;
import connect.activity.home.bean.HomeAction;
import connect.activity.home.bean.HttpRecBean;
import connect.database.MemoryDataManager;
import connect.database.green.DaoHelper.ContactHelper;
import connect.database.green.DaoHelper.MessageHelper;
import connect.database.green.DaoHelper.ParamManager;
import connect.database.green.bean.ContactEntity;
import connect.database.green.bean.FriendRequestEntity;
import connect.database.green.bean.GroupEntity;
import connect.database.green.bean.GroupMemberEntity;
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

    private String Tag = "_CommandReceiver";

    public static CommandReceiver receiver = getInstance();

    private synchronized static CommandReceiver getInstance() {
        if (receiver == null) {
            receiver = new CommandReceiver();
        }
        return receiver;
    }

    @Override
    public void commandReceipt(boolean isSuccess, Object reqObj, Object serviceObj) {

    }

    @Override
    public void updateMsgSendState(String publickey, String msgid, int state) {

    }

    @Override
    public void loadAllContacts(Connect.SyncUserRelationship userRelationship) throws Exception {
        Connect.RelationShip relationShip = userRelationship.getRelationShip();
        List<ContactEntity> friendInfoEntities = new ArrayList();
        List<Connect.FriendInfo> friendInfoList = relationShip.getFriendsList();
        for (Connect.FriendInfo friendInfo : friendInfoList) {
            String friendKey = friendInfo.getPubKey();
            ContactEntity contactEntity = ContactHelper.getInstance().loadFriendEntity(friendKey);
            if (contactEntity == null) {
                contactEntity = new ContactEntity();
                contactEntity.setPub_key(friendKey);
            }
            contactEntity.setUsername(friendInfo.getUsername());
            contactEntity.setAvatar(friendInfo.getAvatar());
            contactEntity.setPub_key(friendInfo.getPubKey());
            contactEntity.setAddress(friendInfo.getAddress());
            contactEntity.setCommon(friendInfo.getCommon() ? 1 : 0);
            contactEntity.setSource(friendInfo.getSource());
            contactEntity.setRemark(friendInfo.getRemark());

            friendInfoEntities.add(contactEntity);
        }

        //To add a system message contact
        String connect = BaseApplication.getInstance().getString(R.string.app_name);
        ContactEntity connectEntity = ContactHelper.getInstance().loadFriendEntity(connect);
        if (connectEntity == null) {
            connectEntity = new ContactEntity();
            connectEntity.setPub_key(connect);
            connectEntity.setUsername(connect);
            connectEntity.setAddress(connect);
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
                HttpRecBean.sendHttpRecMsg(HttpRecBean.HttpRecType.GroupInfo, groupKey);
            } else {// Download successful
                GroupEntity groupEntity = ContactHelper.getInstance().loadGroupEntity(groupKey);
                if (groupEntity == null) {
                    String randPubkey = collaboratives[0];
                    byte[] ecdhkey = SupportKeyUril.getRawECDHKey(MemoryDataManager.getInstance().getPriKey(), randPubkey);
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

            List<Connect.GroupMember> members = groupInfo.getMembersList();
            List<GroupMemberEntity> memberEntities = new ArrayList<>();
            for (Connect.GroupMember member : members) {
                GroupMemberEntity memberEntity = ContactHelper.getInstance().loadGroupMemberEntity(groupKey, member.getAddress());
                if (memberEntity == null) {
                    memberEntity = new GroupMemberEntity();
                    memberEntity.setIdentifier(groupKey);
                    memberEntity.setPub_key(member.getPubKey());
                    memberEntity.setAddress(member.getAddress());
                    memberEntity.setAvatar(member.getAvatar());
                    memberEntity.setUsername(member.getUsername());
                    memberEntity.setNick(member.getNick());
                    memberEntity.setRole(member.getRole());
                    memberEntity.setUsername(member.getUsername());

                    memberEntities.add(memberEntity);
                }
            }
            ContactHelper.getInstance().inserGroupMemEntity(memberEntities);
        }
    }

    @Override
    public void contactChanges(Connect.ChangeRecords changeRecords) {
        String mypublickey = MemoryDataManager.getInstance().getPubKey();
        List<Connect.ChangeRecord> recordsList = changeRecords.getChangeRecordsList();
        for (Connect.ChangeRecord record : recordsList) {
            switch (record.getCategory()) {
                case "del":
                    ContactHelper.getInstance().deleteEntity(record.getAddress());
                    break;
                case "add":
                    Connect.UserInfo userInfo = record.getUserInfo();

                    boolean newFriend = false;
                    String pubKey = userInfo.getPubKey();
                    ContactEntity entity = ContactHelper.getInstance().loadFriendEntity(pubKey);
                    if (entity == null) {
                        newFriend = true;
                        entity = new ContactEntity();
                    }
                    entity.setUsername(userInfo.getUsername());
                    entity.setAvatar(userInfo.getAvatar());
                    entity.setPub_key(pubKey);
                    entity.setAddress(userInfo.getAddress());
                    ContactHelper.getInstance().insertContact(entity);

                    if (newFriend) { // Add a welcome message
                        CFriendChat normalChat = new CFriendChat(entity);
                        String content = BaseApplication.getInstance().getBaseContext().getString(R.string.Link_Hello_I_am, entity.getUsername());
                        ChatMsgEntity msgExtEntity = normalChat.txtMsg(content);
                        msgExtEntity.setMessage_from(pubKey);
                        msgExtEntity.setMessage_to(mypublickey);
                        normalChat.updateRoomMsg("", content, TimeUtil.getCurrentTimeInLong(), -1, 1);

                        MessageHelper.getInstance().insertMsgExtEntity(msgExtEntity);
                    }
                    FailMsgsManager.getInstance().receiveFailMsgs(pubKey);
                    break;
            }
        }
    }

    @Override
    public void receiverFriendRequest(Connect.ReceiveFriendRequest friendRequest) {
        if (friendRequest != null && friendRequest.getSender() != null && !friendRequest.getSender().getPubKey().equals("")) {
            ConvertUtil convertUtil = new ConvertUtil();
            ContactHelper.getInstance().inserFriendQuestEntity(convertUtil.convertFriendRequestEntity(friendRequest));
            ContactNotice.receiverAddFriend();
        }
    }

    @Override
    public void acceptFriendRequest(Connect.ReceiveAcceptFriendRequest friendRequest) {
        FriendRequestEntity friendRequestEntity = ContactHelper.getInstance().loadFriendRequest(friendRequest.getAddress());
        friendRequestEntity.setStatus(2);
        ContactHelper.getInstance().inserFriendQuestEntity(friendRequestEntity);
    }

    @Override
    public void acceptDelFriend(Connect.SyncRelationship relationship) {
        ParamManager.getInstance().putValue(ParamManager.COUNT_FRIENDLIST, relationship.getVersion());
    }

    @Override
    public void conversationMute(Connect.ManageSession manageSession) {

    }

    @Override
    public void updateGroupChange(Connect.GroupChange groupChange) throws Exception {
        Context context = BaseApplication.getInstance().getBaseContext();
        String groupKey = "";
        String memberAddress="";
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
                    HttpRecBean.sendHttpRecMsg(HttpRecBean.HttpRecType.GroupInfo, groupKey);

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
                List<GroupMemberEntity> memEntities = new ArrayList<>();
                for (Connect.UserInfo info : userInfos) {
                    GroupMemberEntity groupMemEntity = ContactHelper.getInstance().loadGroupMemberEntity(groupKey, info.getAddress());
                    if (groupMemEntity == null) {
                        groupMemEntity = new GroupMemberEntity();
                        groupMemEntity.setIdentifier(groupKey);
                        groupMemEntity.setPub_key(info.getPubKey());
                        groupMemEntity.setUsername(info.getUsername());
                        groupMemEntity.setNick(info.getUsername());
                        groupMemEntity.setAvatar(info.getAvatar());
                        groupMemEntity.setAddress(info.getAddress());
                        groupMemEntity.setRole(0);

                        memEntities.add(groupMemEntity);
                    }
                }

                if (groupEntity == null) {//The request of details
                    HttpRecBean.sendHttpRecMsg(HttpRecBean.HttpRecType.GroupInfo, groupKey);
                } else {
                    ContactHelper.getInstance().inserGroupMemEntity(memEntities);

                    normalChat = new CGroupChat(groupEntity);
                    for (GroupMemberEntity memEntity : memEntities) {
                        String memberName = TextUtils.isEmpty(memEntity.getUsername()) ? memEntity.getNick() : memEntity.getUsername();
                        if (groupChange.hasInviteBy()) {
                            String myAddress = MemoryDataManager.getInstance().getAddress();

                            Connect.UserInfo inviteBy = groupChange.getInviteBy();
                            String inviteByName = inviteBy.getAddress().equals(myAddress) ?
                                    context.getString(R.string.Chat_You) : inviteBy.getUsername();

                            String invitorname = memEntity.getAddress().equals(myAddress) ?
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
                for (String address : quitGroup.getAddressesList()) {
                    ContactHelper.getInstance().removeMemberEntity(groupKey, address);
                }
                break;
            case 3://Group of personal information changes
                Connect.ChangeGroupNick groupNick = Connect.ChangeGroupNick.parseFrom(groupChange.getDetail());

                groupKey = groupChange.getIdentifier();
                memberAddress = groupNick.getAddress();
                String memberNick = groupNick.getNick();
                ContactHelper.getInstance().updateGroupMemberNickName(groupKey, memberAddress, memberNick);
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
                        memberAddress = member.getAddress();
                        ContactHelper.getInstance().updateGroupMemberRole(groupKey, memberAddress, 0);
                    }

                    if (member.getAddress().equals(groupAttorn.getAddress())) {//The new group manager
                        memberAddress = member.getAddress();
                        ContactHelper.getInstance().updateGroupMemberRole(groupKey, memberAddress, 1);

                        String showName = "";
                        if (groupAttorn.getAddress().equals(MemoryDataManager.getInstance().getAddress())) {
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
                    HttpRecBean.sendHttpRecMsg(HttpRecBean.HttpRecType.GroupInfo, groupKey);
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
        String mypublickey=MemoryDataManager.getInstance().getPubKey();
        if (packageInfo.getSystem()) {
            ChatMsgEntity msgExtEntity = CRobotChat.getInstance().luckPacketMsg(1,packageInfo.getHashId(), 0L,packageInfo.getTips());
            msgExtEntity.setMessage_from(BaseApplication.getInstance().getString(R.string.app_name));
            msgExtEntity.setMessage_to(mypublickey);

            MessageHelper.getInstance().insertMsgExtEntity(msgExtEntity);
            CRobotChat.getInstance().updateRoomMsg(null, msgExtEntity.showContent(), msgExtEntity.getCreatetime(), -1, 1);
            HomeAction.getInstance().sendEvent(HomeAction.HomeType.TOCHAT,
                    new Talker(Connect.ChatType.CONNECT_SYSTEM_VALUE,
                            BaseApplication.getInstance().getBaseContext().getString(R.string.app_name)));
        } else {
            Connect.UserInfo userInfo = packageInfo.getSender();

            ContactEntity friendEntity = ContactHelper.getInstance().loadFriendEntity(userInfo.getPubKey());
            if (friendEntity == null) {
                friendEntity = new ContactEntity();
                friendEntity.setPub_key(userInfo.getPubKey());
                friendEntity.setAvatar(userInfo.getAvatar());
                friendEntity.setUsername(userInfo.getUsername());
                friendEntity.setAddress(userInfo.getAddress());
            }

            CFriendChat normalChat = new CFriendChat(friendEntity);
            ChatMsgEntity msgExtEntity = normalChat.luckPacketMsg(1, packageInfo.getHashId(), 0L,packageInfo.getTips());
            msgExtEntity.setMessage_from(friendEntity.getPub_key());
            msgExtEntity.setMessage_to(mypublickey);
            msgExtEntity.setSend_status(1);

            MessageHelper.getInstance().insertMsgExtEntity(msgExtEntity);
            normalChat.updateRoomMsg(null, msgExtEntity.showContent(), msgExtEntity.getCreatetime());
            HomeAction.getInstance().sendEvent(HomeAction.HomeType.TOCHAT, new Talker(friendEntity));
        }
    }
}
