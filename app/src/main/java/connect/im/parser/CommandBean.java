package connect.im.parser;

import android.content.Context;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.protobuf.ByteString;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.MemoryHandler;
import java.util.zip.GZIPInputStream;

import connect.activity.chat.bean.MsgExtEntity;
import connect.database.MemoryDataManager;
import connect.database.green.DaoHelper.ContactHelper;
import connect.database.green.DaoHelper.MessageHelper;
import connect.database.green.DaoHelper.ParamHelper;
import connect.database.green.DaoHelper.ParamManager;
import connect.database.green.bean.ContactEntity;
import connect.database.green.bean.FriendRequestEntity;
import connect.database.green.bean.GroupEntity;
import connect.database.green.bean.GroupMemberEntity;
import connect.database.green.bean.ParamEntity;
import connect.im.bean.ConnectState;
import connect.im.bean.Session;
import connect.im.bean.UserCookie;
import connect.im.bean.UserOrderBean;
import connect.im.inter.InterParse;
import connect.im.model.FailMsgsManager;
import connect.ui.activity.R;
import connect.activity.chat.bean.MsgEntity;
import connect.activity.chat.bean.MsgSender;
import connect.activity.chat.bean.RecExtBean;
import connect.activity.chat.bean.Talker;
import connect.activity.chat.model.content.FriendChat;
import connect.activity.chat.model.content.GroupChat;
import connect.activity.chat.model.content.NormalChat;
import connect.activity.chat.model.content.RobotChat;
import connect.activity.contact.bean.ContactNotice;
import connect.activity.contact.model.ConvertUtil;
import connect.activity.home.bean.HomeAction;
import connect.activity.home.bean.HttpRecBean;
import connect.activity.home.bean.MsgNoticeBean;
import connect.activity.base.BaseApplication;
import connect.utils.RegularUtil;
import connect.utils.StringUtil;
import connect.utils.TimeUtil;
import connect.utils.cryption.DecryptionUtil;
import connect.utils.cryption.SupportKeyUril;
import connect.utils.log.LogManager;
import connect.wallet.jni.AllNativeMethod;
import protos.Connect;

/**
 * order message
 * Created by pujin on 2017/4/18.
 */
public class CommandBean extends InterParse {

    private String Tag = "CommandBean";

    public CommandBean(byte ackByte, ByteBuffer byteBuffer) {
        super(ackByte, byteBuffer);
    }

    @Override
    public synchronized void msgParse() throws Exception {
        if (ackByte == 0x04) {
            receiveOffLineMsgs(byteBuffer);
        } else {
            Connect.Command command = imTransferToCommand(byteBuffer);
            String msgid = command.getMsgId();

            switch (ackByte) {
                case 0x01:
                case 0x03:
                case 0x05:
                case 0x06:
                case 0x0a:
                case 0x0b:
                case 0x0c:
                case 0x0e:
                case 0x10:
                case 0x15:
                case 0x16:
                case 0x17:
                    break;
                default:
                    backOnLineAck(4, msgid);
                    break;
            }

            switch (ackByte) {
                case 0x01://contact list
                    syncContacts(command.getDetail());
                    break;
                case 0x06://bind servicetoken
                    break;
                case 0x07://login out success
                    //HomeAction.sendTypeMsg(HomeAction.HomeType.EXIT);
                    break;
                case 0x08://receive add friend request
                    receiverAddFriendRequest(command.getDetail(), msgid, command.getErrNo());
                    break;
                case 0x09://Accept agreed to be a friend request
                    receiverAcceptAddFriend(command.getDetail(), msgid, command.getErrNo());
                    break;
                case 0x0a://delete friend
                    receiverAcceptDelFriend(command.getDetail(), msgid, command.getErrNo());
                    break;
                case 0x0b://Modify the friends remark and common friends
                    receiverSetUserInfo(command.getDetail(), msgid, command.getErrNo());
                    break;
                case 0x0d://modify group information
                    updateGroupInfo(command.getDetail(), msgid, command.getErrNo());
                    break;
                case 0x11://outer translate
                    handlerOuterTransfer(command.getDetail(), msgid, command.getErrNo());
                    break;
                case 0x12://outer red packet
                    handlerOuterRedPacket(command.getDetail(), msgid, command.getErrNo());
                    break;
                case 0x15://Not interested in
                    receiverInterested(command.getDetail(), msgid, command.getErrNo());
                    break;
                case 0x17://upload cookie
                    chatCookieInfo(command.getErrNo());
                    break;
                case 0x18://get friend chatcookie
                    friencChatCookie(command.getDetail(), msgid);
                    break;
                case 0x19:
                    reloadUserCookie();
                    break;
            }
        }
    }

    /**
     * Batch processing offline messages
     *
     * @param buffer
     * @throws Exception
     */
    private void receiveOffLineMsgs(ByteBuffer buffer) throws Exception {
        ConnectState.getInstance().sendEvent(ConnectState.ConnectType.OFFLINE_PULL);

        Connect.StructData structData = imTransferToStructData(buffer);
        byte[] unGzip = unGZip(structData.getPlainData().toByteArray());
        //Whether offline news has been exhausted
        boolean offComplete = false;
        if (unGzip.length == 0 || unGzip.length < 20) {
            offComplete = true;
        } else {
            Connect.OfflineMsgs offlineMsgs = Connect.OfflineMsgs.parseFrom(unGzip);
            List<Connect.OfflineMsg> msgList = offlineMsgs.getOfflineMsgsList();

            for (Connect.OfflineMsg offlineMsg : msgList) {
                LogManager.getLogger().d(Tag, "msgList:" + msgList.size());

                Connect.ProducerMsgDetail msgDetail = offlineMsg.getBody();
                int extension = msgDetail.getExt();
                backOffLineAck(msgDetail.getType(), offlineMsg.getMsgId());

                switch ((byte) msgDetail.getType()) {
                    case 0x04://Offline command processing
                        Connect.IMTransferData imTransferData = Connect.IMTransferData.parseFrom(msgDetail.getData());
                        ByteString transferDataByte = imTransferData.getCipherData().toByteString();
                        switch (extension) {
                            case 0x01://contact list
                                syncContacts(transferDataByte);
                                break;
                            case 0x06://bind servicetoken
                                break;
                            case 0x07://login out success
                                //HomeAction.sendTypeMsg(HomeAction.HomeType.EXIT);
                                break;
                            case 0x08://receive add friend request
                                receiverAddFriendRequest(transferDataByte);
                                break;
                            case 0x09://Accept agreed to be a friend request
                                receiverAcceptAddFriend(transferDataByte);
                                break;
                            case 0x0a://delete friend
                                receiverAcceptDelFriend(transferDataByte);
                                break;
                            case 0x0b://Modify the friends remark and common friends
                                receiverSetUserInfo(transferDataByte);
                                break;
                            case 0x0d://modify group information
                                updateGroupInfo(transferDataByte);
                                break;
                            case 0x11://outer translate
                                handlerOuterTransfer(transferDataByte);
                                break;
                            case 0x12://outer red packet
                                handlerOuterRedPacket(transferDataByte);
                                break;
                            case 0x15://Not interested in
                                receiverInterested(transferDataByte);
                                break;
                        }
                        break;
                    case 0x05://Offline notification
                        InterParse interParse = new MsgParseBean((byte) extension, ByteBuffer.wrap(msgDetail.getData().toByteArray()), 0);
                        interParse.msgParse();
                        break;
                }
            }

            offComplete = offlineMsgs.getCompleted();
        }

        if (offComplete) {
            Session.getInstance().setUpFailTime(MemoryDataManager.getInstance().getPubKey(), 0);
            uploadRandomCookie();
            ConnectState.getInstance().sendEventDelay(ConnectState.ConnectType.CONNECT);
        }
    }

    /**
     * GZip decompression
     *
     * @param data
     * @return
     */
    private byte[] unGZip(byte[] data) {
        byte[] b = null;
        try {
            ByteArrayInputStream bis = new ByteArrayInputStream(data);
            GZIPInputStream gzip = new GZIPInputStream(bis);
            byte[] buf = new byte[1024];
            int num = -1;
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            while ((num = gzip.read(buf, 0, buf.length)) != -1) {
                baos.write(buf, 0, num);
            }
            b = baos.toByteArray();
            baos.flush();
            baos.close();
            gzip.close();
            bis.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return b;
    }

    /**
     * Sync contacts list
     * 20,465.61
     *
     * @param buffer
     * @throws Exception
     */
    private void syncContacts(ByteString buffer) throws Exception {
        String version = ParamManager.getInstance().getString(ParamManager.COUNT_FRIENDLIST);
        if (TextUtils.isEmpty(version)) {
            Connect.SyncUserRelationship relationship = Connect.SyncUserRelationship.parseFrom(buffer);
            Connect.RelationShip friendShip = relationship.getRelationShip();

            version = friendShip.getVersion();

            List<ContactEntity> friendInfoEntities = new ArrayList();
            List<Connect.FriendInfo> friendInfoList = friendShip.getFriendsList();
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
            Connect.UserCommonGroups commonGroups = relationship.getUserCommonGroups();
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
                        byte[] ecdhkey = SupportKeyUril.rawECDHkey(MemoryDataManager.getInstance().getPriKey(), randPubkey);
                        Connect.GcmData gcmData = Connect.GcmData.parseFrom(StringUtil.hexStringToBytes(collaboratives[1]));
                        byte[] ecdhbytes = DecryptionUtil.decodeAESGCM(SupportKeyUril.EcdhExts.EMPTY, ecdhkey, gcmData);
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
                    GroupMemberEntity memberEntity = ContactHelper.getInstance().loadGroupMemByAds(groupKey, member.getAddress());
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
        } else {
            Connect.ChangeRecords changeRecords = Connect.ChangeRecords.parseFrom(buffer);
            version = changeRecords.getVersion();

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
                            NormalChat normalChat = new FriendChat(entity);
                            String content = BaseApplication.getInstance().getBaseContext().getString(R.string.Link_Hello_I_am, entity.getUsername());
                            MsgExtEntity msgExtEntity = normalChat.txtMsg(content);
                            msgExtEntity.setFrom(pubKey);
                            msgExtEntity.setTo(mypublickey);

                            MessageHelper.getInstance().insertMsgExtEntity(msgExtEntity);
                            normalChat.updateRoomMsg(null, msgExtEntity.showContent(), msgExtEntity.getCreatetime(), -1, true);
                        }
                        FailMsgsManager.getInstance().receiveFailMsgs(pubKey);
                        break;
                }
            }
        }

        ParamManager.getInstance().putValue(ParamManager.COUNT_FRIENDLIST, version);
        ContactNotice.receiverContact();
    }

    /**
     * Add Friend request
     *
     * @param buffer
     * @throws Exception
     */
    private void receiverAddFriendRequest(ByteString buffer, Object... objs) throws Exception {
        boolean isMySend = false;
        String msgid = null;

        if (objs.length == 2) {
            msgid = (String) objs[0];
            Map<String, Object> failMap = FailMsgsManager.getInstance().getFailMap(msgid);
            if (failMap != null) {
                isMySend = true;
            }
        }

        if (isMySend) {//youself send add Friend request
            if ((int) objs[1] == 1 || (int) objs[1] == 3) {
                receiptUserSendAckMsg(msgid, MsgNoticeBean.NtEnum.MSG_SEND_FAIL, objs[1]);
            } else {
                receiptUserSendAckMsg(msgid, MsgNoticeBean.NtEnum.MSG_SEND_SUCCESS);
            }
        } else {
            Connect.ReceiveFriendRequest receiver = Connect.ReceiveFriendRequest.parseFrom(buffer);
            if (receiver != null && receiver.getSender() != null && !receiver.getSender().getPubKey().equals("")) {
                ConvertUtil convertUtil = new ConvertUtil();
                ContactHelper.getInstance().inserFriendQuestEntity(convertUtil.convertFriendRequestEntity(receiver));
                ContactNotice.receiverAddFriend();
            }
        }
    }

    /**
     * Agree to add buddy request
     *
     * @param buffer
     * @throws Exception
     */
    private void receiverAcceptAddFriend(ByteString buffer, Object... objs) throws Exception {
        switch ((int) objs[1]) {
            case 1:
                receiptUserSendAckMsg(objs[0], MsgNoticeBean.NtEnum.MSG_SEND_FAIL, objs[1]);
                return;
            case 4:
                receiptUserSendAckMsg(objs[0], MsgNoticeBean.NtEnum.MSG_SEND_FAIL, objs[1]);
                return;
        }

        requestFriendsByVersion();
        Connect.ReceiveAcceptFriendRequest friendRequest = Connect.ReceiveAcceptFriendRequest.parseFrom(buffer);

        FriendRequestEntity friendRequestEntity = ContactHelper.getInstance().loadFriendRequest(friendRequest.getAddress());
        friendRequestEntity.setStatus(2);
        ContactHelper.getInstance().inserFriendQuestEntity(friendRequestEntity);

        receiptUserSendAckMsg(objs[0], MsgNoticeBean.NtEnum.MSG_SEND_SUCCESS);
    }

    /**
     * Remove buddy
     *
     * @param buffer
     * @throws Exception
     */
    private void receiverAcceptDelFriend(ByteString buffer, Object... objs) throws Exception {
        if ((int) objs[1] > 0) {//Delete failed When the two sides have been lifted friends relationship, there is also the local contact person
            receiptUserSendAckMsg(objs[0], MsgNoticeBean.NtEnum.MSG_SEND_FAIL, objs[1]);
        } else {
            receiptUserSendAckMsg(objs[0], MsgNoticeBean.NtEnum.MSG_SEND_SUCCESS);

            Connect.SyncRelationship relationship = Connect.SyncRelationship.parseFrom(buffer);
            ParamManager.getInstance().putValue(ParamManager.COUNT_FRIENDLIST, relationship.getVersion());
        }
    }

    /**
     * Modify the friends remark and common friends
     *
     * @param buffer
     * @throws Exception
     */
    private void receiverSetUserInfo(ByteString buffer, Object... objs) throws Exception {
        receiptUserSendAckMsg(objs[0], MsgNoticeBean.NtEnum.MSG_SEND_SUCCESS);
    }

    /**
     * Group of information change
     *
     * @param buffer
     */
    private void updateGroupInfo(ByteString buffer, Object... objs) throws Exception {
        Connect.GroupChange groupChange = Connect.GroupChange.parseFrom(buffer);

        Context context = BaseApplication.getInstance().getBaseContext();
        String groupKey = "";
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
                    if (TextUtils.isEmpty(groupname)) {
                        groupname = "groupname2";
                    }
                    groupEntity.setName(groupname);
                    ContactHelper.getInstance().inserGroupEntity(groupEntity);
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
                    GroupMemberEntity groupMemEntity = ContactHelper.getInstance().loadGroupMemByAds(groupKey, info.getAddress());
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

                    normalChat = new GroupChat(groupEntity);
                    for (GroupMemberEntity memEntity : memEntities) {
                        String memberName = TextUtils.isEmpty(memEntity.getUsername()) ? memEntity.getNick() : memEntity.getUsername();
                        if (groupChange.hasInviteBy()) {
                            String invitorname = memEntity.getAddress().equals(MemoryDataManager.getInstance().getAddress()) ?
                                    context.getString(R.string.Chat_You) : memberName;
                            noticeStr = context.getString(R.string.Link_invited_to_the_group_chat, groupChange.getInviteBy().getUsername(), invitorname);
                        } else {
                            noticeStr = context.getString(R.string.Link_enter_the_group, memberName);
                        }

                        if (normalChat == null) {
                            FailMsgsManager.getInstance().insertReceiveMsg(groupKey, TimeUtil.timestampToMsgid(), noticeStr);
                        } else {
                            MsgExtEntity msgExtEntity = normalChat.noticeMsg(noticeStr);
                            MessageHelper.getInstance().insertMsgExtEntity(msgExtEntity);

                            RecExtBean.getInstance().sendEvent(RecExtBean.ExtType.MESSAGE_RECEIVE, groupKey, msgExtEntity);
                            normalChat.updateRoomMsg(null, msgExtEntity.showContent(), msgExtEntity.getCreatetime(), -1, true);
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
                groupKey = groupChange.getIdentifier();
                Connect.ChangeGroupNick groupNick = Connect.ChangeGroupNick.parseFrom(groupChange.getDetail());
                GroupMemberEntity memEntity = ContactHelper.getInstance().loadGroupMemByAds(groupKey, groupNick.getAddress());
                memEntity.setNick(groupNick.getNick());
                ContactHelper.getInstance().inserGroupMemEntity(memEntity);
                break;
            case 4://Group change
                groupKey = groupChange.getIdentifier();
                Connect.GroupAttorn groupAttorn = Connect.GroupAttorn.parseFrom(groupChange.getDetail());
                groupMemEntities = ContactHelper.getInstance().loadGroupMemEntity(groupKey);
                if (groupMemEntities == null) {
                    break;
                }
                for (GroupMemberEntity member : groupMemEntities) {
                    if (member.getRole() == 1) {//The old group manager
                        member.setRole(0);
                        ContactHelper.getInstance().inserGroupMemEntity(member);
                    }
                    if (member.getAddress().equals(groupAttorn.getAddress())) {//The new group manager
                        member.setRole(1);
                        ContactHelper.getInstance().inserGroupMemEntity(member);

                        String showName = "";
                        if (groupAttorn.getAddress().equals(MemoryDataManager.getInstance().getAddress())) {
                            showName = context.getString(R.string.Chat_You);
                        } else {
                            showName = TextUtils.isEmpty(member.getNick()) ? member.getUsername() : member.getNick();
                        }
                        noticeStr = context.getString(R.string.Link_become_new_group_owner, showName);

                        normalChat = new GroupChat(groupEntity);
                        MsgExtEntity msgExtEntity = normalChat.noticeMsg(noticeStr);
                        MessageHelper.getInstance().insertMsgExtEntity(msgExtEntity);

                        RecExtBean.getInstance().sendEvent(RecExtBean.ExtType.MESSAGE_RECEIVE, groupKey, msgExtEntity);
                        normalChat.updateRoomMsg(null, msgExtEntity.showContent(), msgExtEntity.getCreatetime(), -1, true);
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

    /**
     * Not interested in
     *
     * @param buffer
     * @throws Exception
     */
    private void receiverInterested(ByteString buffer, Object... objs) throws Exception {
        if ((int) objs[1] > 0) {//The operation failure Repeated friend recommended
            receiptUserSendAckMsg(objs[0], MsgNoticeBean.NtEnum.MSG_SEND_FAIL, objs[1]);
        } else {
            receiptUserSendAckMsg(objs[0], MsgNoticeBean.NtEnum.MSG_SEND_SUCCESS);
        }
    }

    /**
     * Upload the cookie state
     *
     * @param errNum
     */
    public void chatCookieInfo(int errNum) {
        String pubKey = MemoryDataManager.getInstance().getPubKey();
        switch (errNum) {
            case 0://Save the generated temporary cookies
                ConnectState.getInstance().sendEventDelay(ConnectState.ConnectType.CONNECT);
                Session.getInstance().setUpFailTime(pubKey, 0);
                UserCookie userCookie = Session.getInstance().getUserCookie(pubKey);

                ParamEntity entity = new ParamEntity();
                entity.setKey("COOKIE:" + pubKey + StringUtil.bytesToHexString(userCookie.getSalt()));
                entity.setValue(new Gson().toJson(userCookie));
                ParamHelper.getInstance().insertOrReplaceParamEntity(entity);

                try {
                    FailMsgsManager.getInstance().sendExpireMsg();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case 2:
            case 3:
                int failTime = Session.getInstance().getUpFailTime(MemoryDataManager.getInstance().getPubKey());
                if (failTime <= 2) {
                    uploadRandomCookie();
                } else {
                    Session.getInstance().setUserCookie(pubKey, null);
                }
                Session.getInstance().setUpFailTime(MemoryDataManager.getInstance().getPubKey(), ++failTime);
                break;
            case 4://cookie is overdue ,user old protocal
                Session.getInstance().setUserCookie(pubKey, null);
                break;
        }
    }

    /**
     * Upload a local public key
     */
    private void uploadRandomCookie() {
        long curTime = TimeUtil.getCurrentTimeSecond();
        boolean needUpload = true;//If you want to generate a temporary session cookies
        String pubkey = MemoryDataManager.getInstance().getPubKey();

        UserCookie userCookie = Session.getInstance().getUserCookie(pubkey);
        if (userCookie == null) {
            String cookieKey = "COOKIE:" + pubkey;
            ParamEntity paramEntity = ParamHelper.getInstance().likeParamEntityDESC(cookieKey);//local cookie
            if (paramEntity != null) {
                userCookie = new Gson().fromJson(paramEntity.getValue(), UserCookie.class);
            }
        }

        if (userCookie != null) {
            if (curTime < userCookie.getExpiredTime()) {
                needUpload = false;
            }
        }

        if (needUpload) {
            reloadUserCookie();
        }
    }

    /**
     * Good friend chat of cookies
     *
     * @param buffer
     * @throws Exception
     */
    private void friencChatCookie(ByteString buffer, String msgid) throws Exception {
        Connect.ChatCookie cookie = Connect.ChatCookie.parseFrom(buffer);
        Connect.ChatCookieData cookieData = cookie.getData();
        if (TextUtils.isEmpty(cookieData.getChatPubKey())) {//friend use old protocal
            return;
        }

        byte[] friendSalt = cookieData.getSalt().toByteArray();
        Map<String, Object> failMap = FailMsgsManager.getInstance().getFailMap(msgid);
        if (failMap == null) {
            return;
        }

        String pubkey = (String) failMap.get("EXT");
        String cookiePubKey = "COOKIE:" + pubkey;
        List<ParamEntity> paramEntities = ParamHelper.getInstance().likeParamEntities(cookiePubKey);
        if (paramEntities == null || paramEntities.size() < 5) {

        } else {
            int cutSize = paramEntities.size() - 4;
            for (int i = 0; i < cutSize; i++) {
                ParamEntity indexEntity = paramEntities.get(i);
                ParamHelper.getInstance().deleteParamEntity(indexEntity.getKey());
            }
        }

        UserCookie friendCookie = null;
        String friendSaltHex = StringUtil.bytesToHexString(friendSalt);
        ParamEntity paramEntity = ParamHelper.getInstance().likeParamEntity(friendSaltHex);
        if (paramEntity == null) {
            friendCookie = new UserCookie();
            friendCookie.setPubKey(cookieData.getChatPubKey());
            friendCookie.setSalt(friendSalt);
            friendCookie.setExpiredTime(cookieData.getExpired());

            ParamEntity entity = new ParamEntity();
            entity.setKey(cookiePubKey + friendSaltHex);
            entity.setValue(new Gson().toJson(friendCookie));
            ParamHelper.getInstance().insertOrReplaceParamEntity(entity);
        } else {
            friendCookie = new Gson().fromJson(paramEntity.getValue(), UserCookie.class);
        }

        Session.getInstance().setUserCookie(pubkey, friendCookie);
        RecExtBean.getInstance().sendEvent(RecExtBean.ExtType.UNARRIVE_UPDATE, pubkey);
    }

    /**
     * External transfer
     *
     * @param buffer
     * @throws Exception
     */
    private void handlerOuterTransfer(ByteString buffer, Object... objs) throws Exception {
        switch ((int) objs[1]) {
            case 0://Get the success
                break;
            case 1://There is no
                break;
            case 2://To receive your transfer
                break;
        }

        if ((int) objs[1] > 0) {//Get the failure
            receiptUserSendAckMsg(objs[0], MsgNoticeBean.NtEnum.MSG_SEND_FAIL, objs[1]);
        } else {
            receiptUserSendAckMsg(objs[0], MsgNoticeBean.NtEnum.MSG_SEND_SUCCESS);
        }
    }

    /**
     * Outside a red envelope
     *
     * @param buffer
     * @throws Exception
     */
    private void handlerOuterRedPacket(ByteString buffer, Object... objs) throws Exception {
        String mypublickey = MemoryDataManager.getInstance().getPubKey();
        Connect.ExternalRedPackageInfo packageInfo = null;
        switch ((int) objs[1]) {
            case 0://Get the success
                packageInfo = Connect.ExternalRedPackageInfo.parseFrom(buffer);
                if (packageInfo.getSystem()) {
                    MsgExtEntity msgExtEntity = RobotChat.getInstance().luckPacketMsg(1,packageInfo.getHashId(), packageInfo.getTips(),0L);
                    msgExtEntity.setFrom(BaseApplication.getInstance().getString(R.string.app_name));
                    msgExtEntity.setTo(mypublickey);

                    MessageHelper.getInstance().insertMsgExtEntity(msgExtEntity);
                    RobotChat.getInstance().updateRoomMsg(null, msgExtEntity.showContent(), msgExtEntity.getCreatetime(), -1, true);
                    HomeAction.getInstance().sendEvent(HomeAction.HomeType.TOCHAT, new Talker(2, BaseApplication.getInstance().getBaseContext().getString(R.string.app_name)));
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

                    NormalChat normalChat = new FriendChat(friendEntity);
                    MsgExtEntity msgExtEntity = normalChat.luckPacketMsg(1, packageInfo.getHashId(), packageInfo.getTips(), 0L);
                    msgExtEntity.setFrom(friendEntity.getPub_key());
                    msgExtEntity.setTo(mypublickey);

                    MessageHelper.getInstance().insertMsgExtEntity(msgExtEntity);
                    normalChat.updateRoomMsg(null, msgExtEntity.showContent(), msgExtEntity.getCreatetime(), -1, true);
                    HomeAction.getInstance().sendEvent(HomeAction.HomeType.TOCHAT, new Talker(friendEntity));
                }
                break;
            case 1:
                break;
            case 2://It is to receive
                break;
            case 3://Red packets suspended
                break;
        }

        if ((int) objs[1] > 0) {
            receiptUserSendAckMsg(objs[0], MsgNoticeBean.NtEnum.MSG_SEND_FAIL, objs[1]);
        } else {
            receiptUserSendAckMsg(objs[0], MsgNoticeBean.NtEnum.MSG_SEND_SUCCESS);
        }
    }

    public void reloadUserCookie() {
        long curTime = TimeUtil.getCurrentTimeSecond();
        String pubkey = MemoryDataManager.getInstance().getPubKey();

        boolean reGenerate = true;
        UserCookie userCookie = Session.getInstance().getUserCookie(pubkey);
        if (userCookie == null) {
            String cookieKey = "COOKIE:" + pubkey;
            ParamEntity paramEntity = ParamHelper.getInstance().likeParamEntityDESC(cookieKey);//local cookie
            if (paramEntity != null) {
                userCookie = new Gson().fromJson(paramEntity.getValue(), UserCookie.class);
            }
        }

        if (userCookie != null) {
            if (curTime < userCookie.getExpiredTime()) {
                reGenerate = false;
            }
        }

        String priKey = MemoryDataManager.getInstance().getPriKey();
        String randomPriKey = null;
        String randomPubKey = null;
        byte[] randomSalt = null;
        long expiredTime = 0;

        if (reGenerate) {
            priKey = MemoryDataManager.getInstance().getPriKey();
            randomPriKey = AllNativeMethod.cdCreateNewPrivKey();
            randomPubKey = AllNativeMethod.cdGetPubKeyFromPrivKey(randomPriKey);
            randomSalt = AllNativeMethod.cdCreateSeed(16, 4).getBytes();
            expiredTime = TimeUtil.getCurrentTimeSecond() + 24 * 60 * 60;
        } else {
            randomPriKey = userCookie.getPriKey();
            randomPubKey = userCookie.getPubKey();
            randomSalt = userCookie.getSalt();
            expiredTime = TimeUtil.getCurrentTimeSecond() + 24 * 60 * 60;
        }

        Connect.ChatCookieData chatInfo = Connect.ChatCookieData.newBuilder().
                setChatPubKey(randomPubKey).
                setSalt(ByteString.copyFrom(randomSalt)).
                setExpired(expiredTime).build();

        String signInfo = SupportKeyUril.signHash(priKey, chatInfo.toByteArray());
        Connect.ChatCookie cookie = Connect.ChatCookie.newBuilder().
                setSign(signInfo).
                setData(chatInfo).build();

        UserOrderBean userOrderBean = new UserOrderBean();
        userOrderBean.uploadRandomCookie(cookie);

        userCookie = new UserCookie();
        userCookie.setPriKey(randomPriKey);
        userCookie.setPubKey(randomPubKey);
        userCookie.setSalt(randomSalt);
        userCookie.setExpiredTime(expiredTime);
        Session.getInstance().setUserCookie(MemoryDataManager.getInstance().getPubKey(), userCookie);
    }
}
