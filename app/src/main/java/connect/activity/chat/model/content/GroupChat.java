package connect.activity.chat.model.content;

import android.text.TextUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import connect.activity.chat.bean.MsgExtEntity;
import connect.activity.chat.bean.RoomSession;
import connect.database.MemoryDataManager;
import connect.database.green.DaoHelper.ContactHelper;
import connect.database.green.bean.GroupEntity;
import connect.database.green.bean.GroupMemberEntity;
import connect.im.bean.MsgType;
import connect.im.bean.SocketACK;
import connect.im.model.ChatSendManager;
import connect.utils.StringUtil;
import connect.utils.TimeUtil;
import connect.utils.cryption.EncryptionUtil;
import connect.utils.cryption.SupportKeyUril;
import protos.Connect;

/**
 * group chat
 * Created by gtq on 2016/12/19.
 */
public class GroupChat extends NormalChat {
    private static String Tag = "GroupChat";

    private GroupEntity groupEntity;
    private GroupMemberEntity myGroupMember;

    public GroupChat(GroupEntity entity) {
        groupEntity = entity;
        GroupEntity dbEntity = ContactHelper.getInstance().loadGroupEntity(entity.getIdentifier());
        isStranger = (dbEntity == null);
        if (dbEntity != null) {
            groupEntity = dbEntity;
            RoomSession.getInstance().setGroupEcdh(groupEntity.getEcdh_key());
        }

        myGroupMember = ContactHelper.getInstance().loadGroupMemberEntity(groupEntity.getIdentifier(), MemoryDataManager.getInstance().getAddress());
        if (myGroupMember == null) {
            myGroupMember = new GroupMemberEntity();
            myGroupMember.setPub_key(MemoryDataManager.getInstance().getPubKey());
            myGroupMember.setUsername(MemoryDataManager.getInstance().getName());
        }
    }

    @Override
    public MsgExtEntity createBaseChat(MsgType type) {
        String mypublickey = MemoryDataManager.getInstance().getPubKey();

        MsgExtEntity msgExtEntity = new MsgExtEntity();
        msgExtEntity.setMessage_id(TimeUtil.timestampToMsgid());
        msgExtEntity.setChatType(Connect.ChatType.GROUPCHAT.getNumber());
        msgExtEntity.setFrom(mypublickey);
        msgExtEntity.setTo(identify());
        msgExtEntity.setMessageType(type.type);
        msgExtEntity.setCreatetime(TimeUtil.getCurrentTimeInLong());
        msgExtEntity.setSend_status(0);
        return msgExtEntity;
    }

    @Override
    public void sendPushMsg(MsgExtEntity msgExtEntity) {
        Connect.ChatMessage.Builder chatMessageBuilder = msgExtEntity.transToChatMessageBuilder();

        byte[] groupecdh = StringUtil.hexStringToBytes(groupEntity.getEcdh_key());
        Connect.GcmData gcmData = EncryptionUtil.encodeAESGCM(SupportKeyUril.EcdhExts.NONE, groupecdh, msgExtEntity.getContents());
        chatMessageBuilder.setCipherData(gcmData);

        //messageData
        Connect.MessageData.Builder builder = Connect.MessageData.newBuilder();
        builder.setChatMsg(chatMessageBuilder);

        Connect.MessageData messageData = builder.build();
        ChatSendManager.getInstance().sendChatAckMsg(SocketACK.GROUP_CHAT, groupEntity.getIdentifier(), messageData);
    }

    @Override
    public String headImg() {
        if (groupEntity == null) return "";
        String groupAvatar = TextUtils.isEmpty(groupEntity.getAvatar()) ? "" : groupEntity.getAvatar();
        return groupAvatar;
    }

    @Override
    public String nickName() {
        if (groupEntity == null) return "";
        String groupName = TextUtils.isEmpty(groupEntity.getName()) ? "" : groupEntity.getName();
        return groupName;
    }

    @Override
    public String identify() {
        if (groupEntity == null) return "";
        String groupKey = TextUtils.isEmpty(groupEntity.getIdentifier()) ? "" : groupEntity.getIdentifier();
        return groupKey;
    }

    @Override
    public String address() {
        if (groupEntity == null) return "";
        String groupAddress = TextUtils.isEmpty(groupEntity.getIdentifier()) ? "" : groupEntity.getIdentifier();
        return groupAddress;
    }

    @Override
    public String roomKey() {
        if (groupEntity == null) return "";
        String groupKey = TextUtils.isEmpty(groupEntity.getIdentifier()) ? "" : groupEntity.getIdentifier();
        return groupKey;
    }

    @Override
    public int roomType() {
        return 1;
    }

    @Override
    public long destructReceipt() {
        return 0L;
    }

    @Override
    public Connect.MessageUserInfo senderInfo() {
        Connect.MessageUserInfo userInfo = Connect.MessageUserInfo.newBuilder()
                .setAvatar(myGroupMember.getAvatar())
                .setUsername(myGroupMember.getUsername())
                .setUid(myGroupMember.getPub_key()).build();
        return userInfo;
    }

    public String groupEcdh() {
        if (groupEntity == null) return "";
        String ecdh = TextUtils.isEmpty(groupEntity.getEcdh_key()) ? "" : groupEntity.getEcdh_key();
        return ecdh;
    }

    public void setGroupEntity(GroupEntity groupEntity) {
        this.groupEntity = groupEntity;
    }

    public void updateMyNickName(){
        myGroupMember = ContactHelper.getInstance().loadGroupMemberEntity(groupEntity.getIdentifier(), MemoryDataManager.getInstance().getAddress());
    }

    private Map<String, GroupMemberEntity> memEntityMap = null;

    public GroupMemberEntity loadGroupMember(String memberkey) {
        if (memEntityMap == null) {
            memEntityMap = new HashMap<>();
            List<GroupMemberEntity> groupMemEntities = ContactHelper.getInstance().loadGroupMemEntity(roomKey());
            for (GroupMemberEntity memEntity : groupMemEntities) {
                memEntityMap.put(memEntity.getPub_key(), memEntity);
            }
        }
        return memEntityMap.get(memberkey);
    }

    public String nickName(String pubkey) {
        String memberName = "";
        GroupMemberEntity groupMemEntity = loadGroupMember(pubkey);
        if (groupMemEntity != null) {
            memberName = TextUtils.isEmpty(groupMemEntity.getNick()) ? groupMemEntity.getUsername() : groupMemEntity.getNick();
        }
        return memberName;
    }

    public void setNickName(String name){
        if (!TextUtils.isEmpty(name)) {
            groupEntity.setName(name);
        }
    }

    public void setHeadimg(String path) {
        groupEntity.setAvatar(path);
    }

    public MsgExtEntity notMemberNotice() {
        MsgExtEntity msgExtEntity =  createBaseChat(MsgType.NOTICE_NOTMEMBER);
        return msgExtEntity;
    }

    public MsgExtEntity inviteNotice(String tips) {
        MsgExtEntity msgExtEntity = createBaseChat(MsgType.GROUP_INVITE);
        Connect.NotifyMessage.Builder builder = Connect.NotifyMessage.newBuilder()
                .setContent(tips);

        msgExtEntity.setContents(builder.build().toByteArray());
        return msgExtEntity;
    }

    public MsgExtEntity addUserMsg(String tips) {
        MsgExtEntity msgExtEntity = createBaseChat(MsgType.GROUP_ADDUSER_TO);
        Connect.NotifyMessage.Builder builder = Connect.NotifyMessage.newBuilder()
                .setContent(tips);

        msgExtEntity.setContents(builder.build().toByteArray());
        return msgExtEntity;
    }
}
