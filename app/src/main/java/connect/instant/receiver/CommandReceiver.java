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
import connect.activity.contact.bean.ContactNotice;
import connect.activity.contact.bean.MsgSendBean;
import connect.activity.home.bean.ConversationAction;
import connect.activity.home.bean.GroupRecBean;
import connect.activity.home.bean.MsgNoticeBean;
import connect.database.SharedPreferenceUtil;
import connect.database.green.DaoHelper.ContactHelper;
import connect.database.green.DaoHelper.ConversionHelper;
import connect.database.green.DaoHelper.MessageHelper;
import connect.database.green.bean.ContactEntity;
import connect.database.green.bean.GroupEntity;
import connect.database.green.bean.GroupMemberEntity;
import connect.instant.inter.ConversationListener;
import connect.instant.model.CFriendChat;
import connect.instant.model.CGroupChat;
import connect.ui.activity.R;
import connect.utils.RegularUtil;
import connect.utils.TimeUtil;
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
        MessageHelper.getInstance().updateMessageSendState(msgid, state);

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
    public void loadAllContacts(Connect.SyncCompany userRelationship) throws Exception {
        Connect.WorkmatesVersion relationShip = userRelationship.getWorkmatesVersion();
        List<Connect.Workmate> friendInfoList = relationShip.getListList();

        Map<String, ContactEntity> contactEntityMap = new HashMap<>();
        for (Connect.Workmate friendInfo : friendInfoList) {
            String friendUid = friendInfo.getUid();

            if (TextUtils.isEmpty(friendUid)) {
                continue;
            }

            ContactEntity contactEntity = new ContactEntity();
            contactEntity.setUid(friendUid);
            contactEntity.setName(friendInfo.getName());
            contactEntity.setAvatar(friendInfo.getAvatar());
            contactEntity.setOu(friendInfo.getOU());
            contactEntity.setPublicKey(friendInfo.getPubKey());
            contactEntity.setRegisted(true);
            contactEntity.setEmpNo(friendInfo.getEmpNo());
            contactEntity.setMobile(friendInfo.getMobile());
            contactEntity.setGender(friendInfo.getGender());
            contactEntity.setTips(friendInfo.getTips());
            contactEntityMap.put(friendUid, contactEntity);
        }
        Collection<ContactEntity> contactEntityCollection = contactEntityMap.values();
        List<ContactEntity> friendInfoEntities = new ArrayList<ContactEntity>(contactEntityCollection);

        //To add a system message contact
        String connect = BaseApplication.getInstance().getString(R.string.app_name);
        ContactEntity connectEntity = ContactHelper.getInstance().loadFriendEntity(connect);
        if (connectEntity == null) {
            connectEntity = new ContactEntity();
            connectEntity.setUid(connect);
            connectEntity.setName(connect);

            friendInfoEntities.add(connectEntity);
        }
        ContactHelper.getInstance().insertContacts(friendInfoEntities);

        //Synchronous common group
        Map<String, GroupEntity> groupEntityMap = new HashMap<>();
        Map<String, GroupMemberEntity> memberEntityMap = new HashMap<>();
        Connect.UserCommonGroups commonGroups = userRelationship.getUserCommonGroups();
        List<Connect.GroupInfo> groupInfos = commonGroups.getGroupsList();
        for (Connect.GroupInfo groupInfo : groupInfos) {
            Connect.Group group = groupInfo.getGroup();

            String groupIdentifier = group.getIdentifier();
            GroupEntity groupEntity = new GroupEntity();
            groupEntity.setIdentifier(groupIdentifier);
            groupEntity.setAvatar(RegularUtil.groupAvatar(groupIdentifier));
            groupEntity.setName(group.getName());
            groupEntity.setCategory(group.getCategory());
            groupEntity.setSummary(group.getSummary());
            groupEntity.setCommon(1);
            groupEntityMap.put(groupIdentifier, groupEntity);

            List<Connect.GroupMember> members = groupInfo.getMembersList();
            for (Connect.GroupMember member : members) {
                GroupMemberEntity memberEntity = new GroupMemberEntity();
                memberEntity.setIdentifier(groupIdentifier);
                memberEntity.setUid(member.getUid());
                memberEntity.setAvatar(member.getAvatar());
                memberEntity.setUsername(member.getName());
                memberEntity.setRole(member.getRole());
                String memberIdentifyKey = groupIdentifier + member.getUid();
                memberEntityMap.put(memberIdentifyKey, memberEntity);
            }
        }

        Collection<GroupEntity> groupEntityCollection = groupEntityMap.values();
        List<GroupEntity> groupEntities = new ArrayList<GroupEntity>(groupEntityCollection);
        ContactHelper.getInstance().inserGroupEntity(groupEntities);

        Collection<GroupMemberEntity> memberEntityCollection = memberEntityMap.values();
        List<GroupMemberEntity> memEntities = new ArrayList<GroupMemberEntity>(memberEntityCollection);
        ContactHelper.getInstance().inserGroupMemEntity(memEntities);

        ContactNotice.receiverContact();
    }

    @Override
    public void contactChanges(Connect.WorkmateChangeRecords changeRecords) {
        List<Connect.WorkmateChangeRecord> recordsList = changeRecords.getWorkmateChangeRecordsList();
        for (Connect.WorkmateChangeRecord record : recordsList) {
            Connect.Workmate friendInfo = record.getWorkmate();
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
                    entity.setName(friendInfo.getName());
                    entity.setAvatar(friendInfo.getAvatar());
                    entity.setOu(friendInfo.getOU());
                    entity.setPublicKey(friendInfo.getPubKey());
                    entity.setRegisted(true);

                    entity.setEmpNo(friendInfo.getEmpNo());
                    entity.setMobile(friendInfo.getMobile());
                    entity.setGender(friendInfo.getGender());
                    entity.setTips(friendInfo.getTips());
                    ContactHelper.getInstance().insertContact(entity);

                    if (newFriend) { // Add a welcome message
                        CFriendChat normalChat = new CFriendChat(uid);
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
                        ContactHelper.getInstance().updataFriendRemark(uid, "");
                    }
                    break;
            }
        }
    }

    @Override
    public void conversationMute(Connect.ManageSession manageSession) {

    }

    @Override
    public void updateGroupChange(Connect.GroupChange groupChange) throws Exception {
        Context context = BaseApplication.getInstance().getBaseContext();
        String groupKey = "";
        String memberUid = "";
        String noticeStr = "";

        GroupEntity groupEntity = null;
        NormalChat normalChat = null;
        List<GroupMemberEntity> groupMemEntities = null;
        switch (groupChange.getChangeType()) {
            case 0://Group of information change
                Connect.Group group = Connect.Group.parseFrom(groupChange.getDetail());
                groupKey = group.getIdentifier();
                groupEntity = ContactHelper.getInstance().loadGroupEntity(groupKey);
                if (groupEntity == null || TextUtils.isEmpty(group.getName())) {
                    GroupRecBean.sendGroupRecMsg(GroupRecBean.GroupRecType.GroupInfo, groupKey);

                    FailMsgsManager.getInstance().insertReceiveMsg(group.getIdentifier(), TimeUtil.timestampToMsgid(), context.getString(R.string.Link_Join_Group));
                } else {
                    String groupname = group.getName();
                    groupEntity.setName(groupname);
                    ContactHelper.getInstance().inserGroupEntity(groupEntity);

                    ConversionHelper.getInstance().updateRoomEntityName(groupKey, groupname);
                    ConversationAction.conversationAction.sendEvent(ConversationAction.ConverType.LOAD_MESSAGE);

                    RecExtBean.getInstance().sendEvent(RecExtBean.ExtType.UPDATENAME, groupKey, groupname);
                    ContactNotice.receiverGroup();
                }
                break;
            case 1://Add members
                groupKey = groupChange.getIdentifier();
                groupEntity = ContactHelper.getInstance().loadGroupEntity(groupKey);
                Connect.UsersInfo usersInfo = Connect.UsersInfo.parseFrom(groupChange.getDetail());
                List<Connect.UserInfo> userInfos = usersInfo.getUsersList();

                Map<String, GroupMemberEntity> memberEntityMap = new HashMap<>();
                for (Connect.UserInfo info : userInfos) {
                    GroupMemberEntity groupMemEntity = new GroupMemberEntity();
                    groupMemEntity.setIdentifier(groupKey);
                    groupMemEntity.setUid(info.getUid());
                    groupMemEntity.setUsername(info.getName());
                    groupMemEntity.setAvatar(info.getAvatar());
                    groupMemEntity.setRole(0);
                    memberEntityMap.put(info.getUid(), groupMemEntity);
                }
                Collection<GroupMemberEntity> memberEntityCollection = memberEntityMap.values();
                List<GroupMemberEntity> memEntities = new ArrayList<GroupMemberEntity>(memberEntityCollection);
                ContactHelper.getInstance().inserGroupMemEntity(memEntities);

                StringBuffer stringBuffer =new StringBuffer();
                for (GroupMemberEntity memEntity : memEntities) {
                    String memberName = memEntity.getUsername();
                    stringBuffer.append(memberName);
                    stringBuffer.append(",");
                }

                String membersName = stringBuffer.substring(0, stringBuffer.length() - 1);
                if (groupChange.hasInviteBy()) {
                    String myUid = SharedPreferenceUtil.getInstance().getUser().getUid();

                    Connect.UserInfo inviteBy = groupChange.getInviteBy();
                    String inviteByName = inviteBy.getUid().equals(myUid) ?
                            context.getString(R.string.Chat_You) : inviteBy.getName();
                    noticeStr = context.getString(R.string.Link_invited_to_the_group_chat, inviteByName, membersName);
                } else {
                    noticeStr = context.getString(R.string.Link_enter_the_group, membersName);
                }

                if (groupEntity == null) {
                    FailMsgsManager.getInstance().insertReceiveMsg(groupKey, TimeUtil.timestampToMsgid(), noticeStr);
                } else {
                    normalChat = new CGroupChat(groupEntity);
                    ChatMsgEntity msgExtEntity = normalChat.noticeMsg(0, noticeStr, "");
                    MessageHelper.getInstance().insertMsgExtEntity(msgExtEntity);

                    RecExtBean.getInstance().sendEvent(RecExtBean.ExtType.MESSAGE_RECEIVE, groupKey, msgExtEntity);
                    ((ConversationListener) normalChat).updateRoomMsg(null, msgExtEntity.showContent(), msgExtEntity.getCreatetime(), -1, 1);
                }
                break;
            case 2://Remove the group members
                groupKey = groupChange.getIdentifier();
                Connect.QuitGroupUserAddress quitGroup = Connect.QuitGroupUserAddress.parseFrom(groupChange.getDetail());
                for (String uid : quitGroup.getUidsList()) {
                    GroupMemberEntity memberEntity = ContactHelper.getInstance().loadGroupMemberEntity(groupKey, uid);
                    if (memberEntity != null) {
                        String memberName = memberEntity.getUsername();
                        normalChat = new CGroupChat(groupKey);
                        noticeStr = context.getString(R.string.Link_exit_the_group, memberName);
                        ChatMsgEntity msgExtEntity = normalChat.noticeMsg(0, noticeStr, "");
                        MessageHelper.getInstance().insertMsgExtEntity(msgExtEntity);

                        RecExtBean.getInstance().sendEvent(RecExtBean.ExtType.MESSAGE_RECEIVE, groupKey, msgExtEntity);
                        ((ConversationListener) normalChat).updateRoomMsg(null, msgExtEntity.showContent(), msgExtEntity.getCreatetime(), -1, 1);
                    }

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
                groupEntity = ContactHelper.getInstance().loadGroupEntity(groupKey);
                if (groupEntity == null) {
                    break;
                }

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
                            showName = member.getUsername();
                        }
                        noticeStr = context.getString(R.string.Link_become_new_group_owner, showName);

                        normalChat = new CGroupChat(groupEntity);
                        ChatMsgEntity msgExtEntity = normalChat.noticeMsg(0, noticeStr, "");
                        MessageHelper.getInstance().insertMsgExtEntity(msgExtEntity);

                        RecExtBean.getInstance().sendEvent(RecExtBean.ExtType.MESSAGE_RECEIVE, groupKey, msgExtEntity);
                        ((ConversationListener) normalChat).updateRoomMsg(null, msgExtEntity.showContent(), msgExtEntity.getCreatetime(), -1, 1);
                    }
                }
                break;
            case 5://Group set the switch
                Connect.GroupSetting groupSetting = Connect.GroupSetting.parseFrom(groupChange.getDetail());

                groupKey = groupSetting.getIdentifier();
                groupEntity = ContactHelper.getInstance().loadGroupEntity(groupKey);
                if (groupEntity == null || TextUtils.isEmpty(groupEntity.getName())) {
                    GroupRecBean.sendGroupRecMsg(GroupRecBean.GroupRecType.GroupInfo, groupKey);
                } else {
                    groupEntity.setSummary(groupSetting.getSummary());
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
}
