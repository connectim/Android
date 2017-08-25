package connect.activity.chat.model.content;

import android.text.TextUtils;

import com.google.protobuf.InvalidProtocolBufferException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import connect.activity.chat.bean.BaseListener;
import connect.activity.chat.bean.MsgExtEntity;
import connect.activity.chat.bean.RoomSession;
import connect.database.MemoryDataManager;
import connect.database.green.DaoHelper.ContactHelper;
import connect.database.green.bean.GroupEntity;
import connect.database.green.bean.GroupMemberEntity;
import connect.im.bean.MsgType;
import connect.im.bean.SocketACK;
import connect.im.model.ChatSendManager;
import connect.utils.ProtoBufUtil;
import connect.utils.StringUtil;
import connect.utils.TimeUtil;
import connect.utils.UriUtil;
import connect.utils.cryption.DecryptionUtil;
import connect.utils.cryption.EncryptionUtil;
import connect.utils.cryption.SupportKeyUril;
import connect.utils.okhttp.OkHttpUtil;
import connect.utils.okhttp.ResultCall;
import protos.Connect;

/**
 * group chat
 * Created by gtq on 2016/12/19.
 */
public class GroupChat extends NormalChat {
    private static String Tag = "GroupChat";

    private GroupEntity groupEntity;
    private GroupMemberEntity myGroupMember;
    private Map<String, GroupMemberEntity> memEntityMap = null;

    public GroupChat(GroupEntity entity) {
        groupEntity = entity;
        GroupEntity dbEntity = ContactHelper.getInstance().loadGroupEntity(entity.getIdentifier());
        isStranger = (dbEntity == null);
        if (dbEntity != null) {
            groupEntity = dbEntity;
            RoomSession.getInstance().setGroupEcdh(groupEntity.getEcdh_key());
        }

        loadGroupMembersMap();
        myGroupMember = memEntityMap.get(MemoryDataManager.getInstance().getPubKey());
    }

    @Override
    public MsgExtEntity createBaseChat(MsgType type) {
        String mypublickey = MemoryDataManager.getInstance().getPubKey();

        MsgExtEntity msgExtEntity = new MsgExtEntity();
        msgExtEntity.setMessage_id(TimeUtil.timestampToMsgid());
        msgExtEntity.setChatType(Connect.ChatType.GROUPCHAT.getNumber());
        msgExtEntity.setMessage_ower(identify());
        msgExtEntity.setMessage_from(mypublickey);
        msgExtEntity.setMessage_to(identify());
        msgExtEntity.setMessageType(type.type);
        msgExtEntity.setRead_time(0L);
        msgExtEntity.setSnap_time(0L);
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
    public String chatKey() {
        if (groupEntity == null) return "";
        String groupKey = TextUtils.isEmpty(groupEntity.getIdentifier()) ? "" : groupEntity.getIdentifier();
        return groupKey;
    }

    @Override
    public int chatType() {
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

    public void loadGroupMembersMap() {
        if (memEntityMap == null) {
            memEntityMap = new HashMap<>();
        }
        List<GroupMemberEntity> groupMemEntities = ContactHelper.getInstance().loadGroupMemEntities(chatKey());
        for (GroupMemberEntity memEntity : groupMemEntities) {
            memEntityMap.put(memEntity.getPub_key(), memEntity);
        }
    }

    public void loadGroupMember(String memberkey, BaseListener<GroupMemberEntity> baseListener) {
        if (memEntityMap == null) {
            loadGroupMembersMap();
        }

        GroupMemberEntity memberEntity = memEntityMap.get(memberkey);
        if (memberEntity == null) {
            requestUserDetailInfo(memberkey, baseListener);
        } else {
            baseListener.Success(memberEntity);
        }
    }

    public void setNickName(String name){
        if (!TextUtils.isEmpty(name)) {
            groupEntity.setName(name);
        }
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

    public MsgExtEntity groupTxtMsg(String string, List<String> address) {
        MsgExtEntity msgExtEntity = createBaseChat(MsgType.Text);
        Connect.TextMessage.Builder builder = Connect.TextMessage.newBuilder()
                .setContent(string);

        for (String memberaddress : address) {
            builder.addAtAddresses(memberaddress);
        }
        msgExtEntity.setContents(builder.build().toByteArray());
        return msgExtEntity;
    }

    public void requestUserDetailInfo(String publickey, final BaseListener<GroupMemberEntity> baseListener) {
        Connect.SearchUser searchUser = Connect.SearchUser.newBuilder()
                .setCriteria(publickey)
                .build();

        OkHttpUtil.getInstance().postEncrySelf(UriUtil.CONNECT_V1_USER_SEARCH, searchUser, new ResultCall<Connect.HttpResponse>() {
            @Override
            public void onResponse(Connect.HttpResponse response) {
                try {
                    Connect.IMResponse imResponse = Connect.IMResponse.parseFrom(response.getBody().toByteArray());
                    Connect.StructData structData = DecryptionUtil.decodeAESGCMStructData(imResponse.getCipherData());
                    Connect.UserInfo userInfo = Connect.UserInfo.parseFrom(structData.getPlainData());
                    if (ProtoBufUtil.getInstance().checkProtoBuf(userInfo)) {
                        GroupMemberEntity memberEntity = new GroupMemberEntity();
                        memberEntity.setAvatar(userInfo.getAvatar());
                        memberEntity.setUsername(userInfo.getUsername());
                        memEntityMap.put(userInfo.getPubKey(), memberEntity);
                        baseListener.Success(memberEntity);
                    }
                } catch (InvalidProtocolBufferException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Connect.HttpResponse response) {
                baseListener.fail("");
            }
        });
    }
}
