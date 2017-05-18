package connect.im.bean;

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
import java.util.zip.GZIPInputStream;

import connect.db.MemoryDataManager;
import connect.db.SharedPreferenceUtil;
import connect.db.green.DaoHelper.ContactHelper;
import connect.db.green.DaoHelper.ConversionHelper;
import connect.db.green.DaoHelper.MessageHelper;
import connect.db.green.DaoHelper.ParamHelper;
import connect.db.green.DaoHelper.ParamManager;
import connect.db.green.bean.ContactEntity;
import connect.db.green.bean.ConversionEntity;
import connect.db.green.bean.FriendRequestEntity;
import connect.db.green.bean.GroupEntity;
import connect.db.green.bean.GroupMemberEntity;
import connect.db.green.bean.ParamEntity;
import connect.im.inter.InterParse;
import connect.im.model.FailMsgsManager;
import connect.im.msgdeal.SendMsgUtil;
import connect.ui.activity.R;
import connect.ui.activity.chat.bean.MsgChatReceiver;
import connect.ui.activity.chat.bean.MsgEntity;
import connect.ui.activity.chat.bean.MsgSender;
import connect.ui.activity.chat.bean.RecExtBean;
import connect.ui.activity.chat.bean.RoMsgEntity;
import connect.ui.activity.chat.bean.Talker;
import connect.ui.activity.chat.model.ChatMsgUtil;
import connect.ui.activity.chat.model.content.FriendChat;
import connect.ui.activity.chat.model.content.GroupChat;
import connect.ui.activity.chat.model.content.NormalChat;
import connect.ui.activity.chat.model.content.RobotChat;
import connect.ui.activity.contact.bean.ContactNotice;
import connect.ui.activity.contact.model.ConvertUtil;
import connect.ui.activity.home.bean.HomeAction;
import connect.ui.activity.home.bean.HttpRecBean;
import connect.ui.activity.home.bean.MsgNoticeBean;
import connect.ui.base.BaseApplication;
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
    public void msgParse() throws Exception {
        if (ackByte == 0x04) {
            receiveOffLineMsgs(byteBuffer);
        } else if (ackByte == 0x07) {
            HomeAction.sendTypeMsg(HomeAction.HomeType.EXIT);
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
                    HomeAction.sendTypeMsg(HomeAction.HomeType.EXIT);
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
                case 0x19://repeat apply group
                    applyGroupRepeatInfo(command.getDetail(), msgid, command.getErrNo());
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
        Connect.StructData structData = imTransferToStructData(buffer);
        //GZIP
        byte[] unGzip = unGZip(structData.getPlainData().toByteArray());

        //Whether offline news has been exhausted
        boolean offComplete = false;
        if (unGzip.length == 0 || unGzip.length < 20) {
            offComplete = true;
        } else {
            Connect.OfflineMsgs offlineMsgs = Connect.OfflineMsgs.parseFrom(unGzip);
            List<Connect.OfflineMsg> msgList = offlineMsgs.getOfflineMsgsList();

            for (Connect.OfflineMsg offlineMsg : msgList) {
                Connect.ProducerMsgDetail msgDetail = offlineMsg.getBody();
                int extension = msgDetail.getExt();
                backOffLineAck(msgDetail.getType(), offlineMsg.getMsgId());

                switch ((byte) msgDetail.getType()) {
                    case 0x04://Offline command processing
                        if (extension == 0x08) {//Receipt of a request add buddy
                            Connect.IMTransferData imTransferData = Connect.IMTransferData.parseFrom(msgDetail.getData());
                            if (!SupportKeyUril.verifySign(imTransferData.getSign(), imTransferData.getCipherData().toByteArray())) {
                                throw new Exception("Validation fails");
                            }
                            receiverAddFriendRequest(imTransferData.getCipherData().toByteString());
                        } else if (extension == 0x09) {//Accept the other party agreed to a friend request
                            Connect.IMTransferData imTransferData = Connect.IMTransferData.parseFrom(msgDetail.getData());
                            if (!SupportKeyUril.verifySign(imTransferData.getSign(), imTransferData.getCipherData().toByteArray())) {
                                throw new Exception("Validation fails");
                            }
                            receiverAcceptAddFriend(imTransferData.getCipherData().toByteString());
                        } else if (extension == 0x0d) {//Group of related information changes
                            Connect.IMTransferData imTransferData = Connect.IMTransferData.parseFrom(msgDetail.getData());
                            if (!SupportKeyUril.verifySign(imTransferData.getSign(), imTransferData.getCipherData().toByteArray())) {
                                throw new Exception("Validation fails");
                            }
                            updateGroupInfo(imTransferData.getCipherData().toByteString());
                        }
                        break;
                    case 0x05://Offline notification
                        InterParse interParse = new MsgParseBean((byte) extension, ByteBuffer.wrap(msgDetail.getData().toByteArray()));
                        interParse.msgParse();
                        break;
                }
            }

            offComplete = offlineMsgs.getCompleted();
        }

        if (offComplete) {
            Session.getInstance().setUpFailTime(MemoryDataManager.getInstance().getPubKey(), 0);
            uploadRandomCookie();
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
     *20,465.61
     * @param buffer
     * @throws Exception
     */
    private void syncContacts(ByteString buffer) throws Exception {
        String version = ParamManager.getInstance().getString(ParamManager.COUNT_FRIENDLIST);
        if (TextUtils.isEmpty(version)) {
            Connect.SyncUserRelationship relationship = Connect.SyncUserRelationship.parseFrom(buffer);
            //Synchronous buddy list
            Connect.RelationShip friendShip = relationship.getRelationShip();
            version = TextUtils.isEmpty(friendShip.getVersion()) ? "" : friendShip.getVersion();
            ParamManager.getInstance().putValue(ParamManager.COUNT_FRIENDLIST, version);
            List<Connect.FriendInfo> friendInfoList = friendShip.getFriendsList();
            if (friendInfoList == null || friendInfoList.size() == 0) return;

            List<ContactEntity> friendInfoEntities = new ArrayList();
            ContactEntity entity = null;
            for (Connect.FriendInfo friendInfo : friendInfoList) {
                if (TextUtils.isEmpty(friendInfo.getPubKey()) || TextUtils.isEmpty(friendInfo.getAddress())) {
                    continue;
                }

                entity = ContactHelper.getInstance().loadFriendEntity(friendInfo.getPubKey());
                if (entity == null) {
                    entity = new ContactEntity();
                }
                entity.setUsername(friendInfo.getUsername());
                entity.setAvatar(friendInfo.getAvatar());
                entity.setPub_key(friendInfo.getPubKey());
                entity.setAddress(friendInfo.getAddress());
                entity.setCommon(friendInfo.getCommon() ? 1 : 0);
                entity.setSource(friendInfo.getSource());
                entity.setRemark(friendInfo.getRemark());
                friendInfoEntities.add(entity);
            }

            //To add a system message contact
            String connect = BaseApplication.getInstance().getString(R.string.app_name);
            entity = new ContactEntity();
            entity.setPub_key(connect);
            entity.setUsername(connect);
            entity.setAddress(connect);
            entity.setSource(-1);
            friendInfoEntities.add(entity);

            ContactHelper.getInstance().insertContacts(friendInfoEntities);
            //Synchronous common group
            Connect.UserCommonGroups commonGroups = relationship.getUserCommonGroups();
            if (commonGroups.getGroupsList() != null) {
                List<Connect.GroupInfo> groupInfos = commonGroups.getGroupsList();
                for (Connect.GroupInfo groupInfo : groupInfos) {
                    Connect.Group group = groupInfo.getGroup();
                    if (TextUtils.isEmpty(group.getIdentifier()) || TextUtils.isEmpty(group.getName())) {
                        continue;
                    }

                    GroupEntity groupEntity = ContactHelper.getInstance().loadGroupEntity(group.getIdentifier());
                    if (groupEntity == null) {
                        groupEntity = new GroupEntity();
                    }

                    groupEntity.setIdentifier(group.getIdentifier());
                    groupEntity.setVerify(group.getPublic() ? 1 : 0);
                    groupEntity.setName(group.getName());
                    groupEntity.setCommon(1);
                    groupEntity.setAvatar(RegularUtil.groupAvatar(group.getIdentifier()));

                    String[] collaboratives = groupInfo.getEcdh().split("/");
                    if (collaboratives.length < 2) {//Download failed
                        HttpRecBean.sendHttpRecMsg(HttpRecBean.HttpRecType.DownBackUp, group.getIdentifier());
                    } else {//Download successful
                        String randPubkey = collaboratives[0];
                        byte[] ecdhkey = SupportKeyUril.rawECDHkey(MemoryDataManager.getInstance().getPriKey(), randPubkey);
                        Connect.GcmData gcmData = Connect.GcmData.parseFrom(StringUtil.hexStringToBytes(collaboratives[1]));
                        byte[] ecdhbytes = DecryptionUtil.decodeAESGCM(SupportKeyUril.EcdhExts.EMPTY, ecdhkey, gcmData);
                        String groupEcdh = StringUtil.bytesToHexString(ecdhbytes);
                        LogManager.getLogger().d(Tag, "Retrieve the backup group ECDH :" + groupEcdh);
                        groupEntity.setEcdh_key(groupEcdh);
                        ContactHelper.getInstance().inserGroupEntity(groupEntity);
                    }

                    List<Connect.GroupMember> members = groupInfo.getMembersList();
                    List<GroupMemberEntity> memberEntities = new ArrayList<>();
                    for (Connect.GroupMember member : members) {
                        GroupMemberEntity memberEntity = new GroupMemberEntity();
                        memberEntity.setIdentifier(groupEntity.getIdentifier());
                        memberEntity.setPub_key(member.getPubKey());
                        memberEntity.setAddress(member.getAddress());
                        memberEntity.setAvatar(member.getAvatar());
                        memberEntity.setUsername(member.getUsername());
                        memberEntity.setNick(member.getNick());
                        memberEntity.setRole(member.getRole());
                        memberEntity.setUsername(member.getUsername());
                        memberEntities.add(memberEntity);
                    }
                    ContactHelper.getInstance().inserGroupMemEntity(memberEntities);
                }
            }
        } else {
            Connect.ChangeRecords changeRecords = Connect.ChangeRecords.parseFrom(buffer);
            ParamManager.getInstance().putValue(ParamManager.COUNT_FRIENDLIST, changeRecords.getVersion());
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
                            entity = new ContactEntity();
                            newFriend = true;
                        }
                        entity.setUsername(userInfo.getUsername());
                        entity.setAvatar(userInfo.getAvatar());
                        entity.setPub_key(pubKey);
                        entity.setAddress(userInfo.getAddress());
                        ContactHelper.getInstance().insertContact(entity);

                        if (newFriend) { //Add a welcome message
                            NormalChat normalChat = new FriendChat(entity);
                            MsgSender msgSender = new MsgSender(entity.getPub_key(), entity.getUsername(), entity.getAddress(), entity.getAvatar());
                            String content = BaseApplication.getInstance().getBaseContext().getString(R.string.Link_Hello_I_am, entity.getUsername());
                            MsgEntity msgEntity = normalChat.txtMsg(content);
                            msgEntity.getMsgDefinBean().setSenderInfoExt(msgSender);
                            MessageHelper.getInstance().insertFromMsg(entity.getPub_key(), msgEntity.getMsgDefinBean());

                            ConversionEntity roomEntity = ConversionHelper.getInstance().loadRoomEnitity(entity.getPub_key());
                            if (roomEntity == null) {
                                ChatMsgUtil.updateRoomInfo(entity.getPub_key(), 0, TimeUtil.getCurrentTimeInLong(), msgEntity.getMsgDefinBean());
                            }
                        }

                        FailMsgsManager.getInstance().receiveFailMsgs(pubKey);
                        break;
                }
            }
        }
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
            if ((int) objs[1] == 1) {
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
        }

        SendMsgUtil.requestFriendsByVersion();
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
        String groupKey = groupChange.getIdentifier();
        GroupEntity groupEntity = ContactHelper.getInstance().loadGroupEntity(groupKey);

        String noticeStr = "";
        NormalChat normalChat = null;
        List<GroupMemberEntity> groupMemEntities = null;
        switch (groupChange.getChangeType()) {
            case 0://Group of information change
                Connect.Group group = Connect.Group.parseFrom(groupChange.getDetail());
                if (groupEntity == null || TextUtils.isEmpty(groupEntity.getEcdh_key())) {
                    HttpRecBean.sendHttpRecMsg(HttpRecBean.HttpRecType.GroupInfo, group.getIdentifier());

                    FailMsgsManager.getInstance().insertReceiveMsg(group.getIdentifier(),TimeUtil.timestampToMsgid(), context.getString(R.string.Link_Join_Group));
                } else {
                    if (!TextUtils.isEmpty(group.getName())) {
                        groupEntity.setName(group.getName());
                    }
                    groupEntity.setSummary(TextUtils.isEmpty(group.getSummary()) ? "" : group.getSummary());
                    ContactHelper.getInstance().inserGroupEntity(groupEntity);
                    ContactNotice.receiverGroup();
                }
                break;
            case 1://Add members
                Connect.UsersInfo usersInfo = Connect.UsersInfo.parseFrom(groupChange.getDetail());
                List<Connect.UserInfo> userInfos = usersInfo.getUsersList();
                List<GroupMemberEntity> memEntities = new ArrayList<>();
                for (Connect.UserInfo info : userInfos) {
                    GroupMemberEntity groupMemEntity = ContactHelper.getInstance().loadGroupMemByAds(groupKey, info.getAddress());
                    if (groupMemEntity == null) {
                        groupMemEntity = new GroupMemberEntity();
                        groupMemEntity.setIdentifier(groupKey);
                    }

                    groupMemEntity.setPub_key(info.getPubKey());
                    groupMemEntity.setUsername(info.getUsername());
                    groupMemEntity.setNick(info.getUsername());
                    groupMemEntity.setAvatar(info.getAvatar());
                    groupMemEntity.setAddress(info.getAddress());
                    groupMemEntity.setRole(0);
                    memEntities.add(groupMemEntity);
                }

                if (groupEntity == null) {//The request of details
                    HttpRecBean.sendHttpRecMsg(HttpRecBean.HttpRecType.GroupInfo, groupKey);
                } else {
                    ContactHelper.getInstance().inserGroupMemEntity(memEntities);
                    normalChat = new GroupChat(groupEntity);
                }

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
                        FailMsgsManager.getInstance().insertReceiveMsg(groupKey,TimeUtil.timestampToMsgid(), noticeStr);
                    } else {
                        MsgEntity msgEntity = normalChat.noticeMsg(noticeStr);
                        MessageHelper.getInstance().insertFromMsg(groupKey, msgEntity.getMsgDefinBean());
                        MsgChatReceiver.sendChatReceiver(groupKey, msgEntity);
                        ChatMsgUtil.updateRoomInfo(groupKey, 1, TimeUtil.getCurrentTimeInLong(), msgEntity.getMsgDefinBean());
                    }
                }
                break;
            case 2://Remove the group members
                Connect.QuitGroupUserAddress quitGroup = Connect.QuitGroupUserAddress.parseFrom(groupChange.getDetail());
                for (String address : quitGroup.getAddressesList()) {
                    ContactHelper.getInstance().removeMemberEntity(groupChange.getIdentifier(), address);
                }
                break;
            case 3://Group of personal information changes
                Connect.ChangeGroupNick groupNick = Connect.ChangeGroupNick.parseFrom(groupChange.getDetail());
                GroupMemberEntity memEntity = ContactHelper.getInstance().loadGroupMemByAds(groupChange.getIdentifier(), groupNick.getAddress());
                memEntity.setNick(groupNick.getNick());
                ContactHelper.getInstance().inserGroupMemEntity(memEntity);
                break;
            case 4://Group change
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
                        MsgEntity msgEntity = normalChat.noticeMsg(noticeStr);
                        MessageHelper.getInstance().insertFromMsg(groupKey, msgEntity.getMsgDefinBean());
                        MsgChatReceiver.sendChatReceiver(groupKey, msgEntity);
                        ChatMsgUtil.updateRoomInfo(groupKey, 1, TimeUtil.getCurrentTimeInLong(), msgEntity.getMsgDefinBean());
                    }
                }
                break;
            case 5://Group set the switch
                Connect.GroupSetting groupSetting = Connect.GroupSetting.parseFrom(groupChange.getDetail());
                groupEntity = ContactHelper.getInstance().loadGroupEntity(groupSetting.getIdentifier());
                if (groupEntity != null) {
                    groupEntity.setVerify(groupSetting.getPublic() ? 1 : 0);
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
                Session.getInstance().setUpFailTime(pubKey, 0);
                UserCookie userCookie = Session.getInstance().getUserCookie(pubKey);

                ParamEntity entity = new ParamEntity();
                entity.setKey("COOKIE:" + pubKey + StringUtil.bytesToHexString(userCookie.getSalt()));
                entity.setValue(new Gson().toJson(userCookie));
                ParamHelper.getInstance().insertOrReplaceParamEntity(entity);
                LogManager.getLogger().d(Tag, "The user save COOKIE:" + entity.getKey());
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
        String cookieKey = "COOKIE:" + MemoryDataManager.getInstance().getPubKey();

        long curTime = TimeUtil.getCurrentTimeSecond();
        ParamEntity paramEntity = ParamHelper.getInstance().likeParamEntityDESC(cookieKey);//local cookie
        boolean needUpload = true;//If you want to generate a temporary session cookies
        UserCookie userCookie = null;

        if (paramEntity == null) {
            needUpload = true;
        } else {
            userCookie = new Gson().fromJson(paramEntity.getValue(), UserCookie.class);
            if (curTime < userCookie.getExpiredTime()) {
                needUpload = false;
                LogManager.getLogger().d(Tag, "user local SALT:" + StringUtil.bytesToHexString(userCookie.getSalt()));
            } else {
                needUpload = true;
            }
        }

        if (needUpload) {
            String priKey = MemoryDataManager.getInstance().getPriKey();
            //Generate temporary private key and Salt
            String randomPriKey = AllNativeMethod.cdCreateNewPrivKey();
            String randomPubKey = AllNativeMethod.cdGetPubKeyFromPrivKey(randomPriKey);
            byte[] randomSalt = AllNativeMethod.cdCreateSeed(16, 4).getBytes();

            LogManager.getLogger().d(Tag, "user create salt:" + StringUtil.bytesToHexString(randomSalt));

            long expiredTime = curTime + 24 * 60 * 60;
            Connect.ChatCookieData chatInfo = Connect.ChatCookieData.newBuilder().
                    setChatPubKey(randomPubKey).
                    setSalt(ByteString.copyFrom(randomSalt)).
                    setExpired(expiredTime).build();

            String signInfo = SupportKeyUril.signHash(priKey, chatInfo.toByteArray());
            Connect.ChatCookie cookie = Connect.ChatCookie.newBuilder().
                    setSign(signInfo).
                    setData(chatInfo).build();

            SendMsgUtil.uploadRandomCookie(cookie);

            //save random prikey and salt
            userCookie = new UserCookie();
            userCookie.setPriKey(randomPriKey);
            userCookie.setPubKey(randomPubKey);
            userCookie.setSalt(randomSalt);
            userCookie.setExpiredTime(expiredTime);
        }

        Session.getInstance().setUserCookie(MemoryDataManager.getInstance().getPubKey(), userCookie);
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

        //Access to local cookies
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
        RecExtBean.sendRecExtMsg(RecExtBean.ExtType.UNARRIVE_UPDATE, pubkey);
    }

    /**
     * Repeat application group
     *
     * @param buffer
     * @throws Exception
     */
    private void applyGroupRepeatInfo(ByteString buffer, Object... objs) throws Exception {
        Connect.GroupApplyChange applyChange = Connect.GroupApplyChange.parseFrom(buffer);
        ParamManager.getInstance().updateGroupApply(applyChange.getVerificationCode(), applyChange.getTips(), applyChange.getSource(), -1, (String) objs[0]);
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
        Connect.ExternalRedPackageInfo packageInfo = null;
        switch ((int) objs[1]) {
            case 0://Get the success
                packageInfo = Connect.ExternalRedPackageInfo.parseFrom(buffer);
                if (packageInfo.getSystem()) {
                    RoMsgEntity msgEntity = RobotChat.getInstance().luckPacketMsg(packageInfo.getHashId(), packageInfo.getTips(), 1);
                    msgEntity.getMsgDefinBean().setMessage_id(packageInfo.getMsgId());
                    msgEntity.getMsgDefinBean().setSenderInfoExt(new MsgSender(RobotChat.getInstance().roomKey(),
                            BaseApplication.getInstance().getString(R.string.app_name),
                            RobotChat.getInstance().address(), RobotChat.getInstance().headImg()));
                    MessageHelper.getInstance().insertFromMsg(BaseApplication.getInstance().getString(R.string.app_name), msgEntity.getMsgDefinBean());
                    ChatMsgUtil.updateRoomInfo(BaseApplication.getInstance().getString(R.string.app_name), 2, msgEntity.getMsgDefinBean().getSendtime(), msgEntity.getMsgDefinBean());
                    HomeAction.sendTypeMsg(HomeAction.HomeType.TOCHAT, new Talker(2, BaseApplication.getInstance().getBaseContext().getString(R.string.app_name)));
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
                    MsgEntity msgEntity = normalChat.luckPacketMsg(packageInfo.getHashId(), packageInfo.getTips(), 1);
                    msgEntity.getMsgDefinBean().setSenderInfoExt(new MsgSender(friendEntity.getPub_key(), friendEntity.getUsername(), friendEntity.getAddress(), friendEntity.getAvatar()));
                    MessageHelper.getInstance().insertFromMsg(normalChat.roomKey(), msgEntity.getMsgDefinBean());
                    ChatMsgUtil.updateRoomInfo(normalChat.roomKey(), 0, msgEntity.getMsgDefinBean().getSendtime(), msgEntity.getMsgDefinBean());
                    HomeAction.sendTypeMsg(HomeAction.HomeType.TOCHAT, new Talker(friendEntity));
                }
                break;
            case 1:
                break;
            case 2://It is to receive
                break;
        }

        if ((int) objs[1] > 0) {
            receiptUserSendAckMsg(objs[0], MsgNoticeBean.NtEnum.MSG_SEND_FAIL, objs[1]);
        } else {
            receiptUserSendAckMsg(objs[0], MsgNoticeBean.NtEnum.MSG_SEND_SUCCESS);
        }
    }
}
