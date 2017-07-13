package connect.activity.chat.model.content;

import android.text.TextUtils;

import com.google.gson.Gson;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import connect.database.MemoryDataManager;
import connect.database.green.DaoHelper.ContactHelper;
import connect.database.green.bean.GroupEntity;
import connect.database.green.bean.GroupMemberEntity;
import connect.im.bean.MsgType;
import connect.im.bean.SocketACK;
import connect.im.model.ChatSendManager;
import connect.activity.chat.bean.ExtBean;
import connect.activity.chat.bean.MsgDefinBean;
import connect.activity.chat.bean.MsgEntity;
import connect.activity.chat.bean.MsgSender;
import connect.activity.chat.bean.RoomSession;
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

        myGroupMember = ContactHelper.getInstance().loadGroupMemByAds(groupEntity.getIdentifier(), MemoryDataManager.getInstance().getAddress());
        if (myGroupMember == null) {
            myGroupMember = new GroupMemberEntity();
            myGroupMember.setPub_key(MemoryDataManager.getInstance().getPubKey());
            myGroupMember.setUsername(MemoryDataManager.getInstance().getName());
        }
    }

    public MsgEntity inviteNotice(String str) {
        MsgEntity bean = createBaseChat(MsgType.GROUP_INVITE);
        bean.getMsgDefinBean().setContent(str);
        return bean;
    }

    public MsgEntity addUserMsg(String str) {
        MsgEntity bean = createBaseChat(MsgType.GROUP_ADDUSER_TO);
        bean.getMsgDefinBean().setExt(str);
        return bean;
    }

    @Override
    public MsgEntity createBaseChat(MsgType type) {
        MsgDefinBean msgDefinBean = new MsgDefinBean();
        msgDefinBean.setType(type.type);
        msgDefinBean.setUser_name(groupEntity.getName());
        msgDefinBean.setSendtime(TimeUtil.getCurrentTimeInLong());
        msgDefinBean.setMessage_id(TimeUtil.timestampToMsgid());
        msgDefinBean.setPublicKey(groupEntity.getIdentifier());
        msgDefinBean.setUser_id(groupEntity.getEcdh_key());
        msgDefinBean.setSenderInfoExt(new MsgSender(myGroupMember.getPub_key(),
                TextUtils.isEmpty(myGroupMember.getNick()) ? myGroupMember.getUsername() : myGroupMember.getNick(),
                myGroupMember.getAddress(), MemoryDataManager.getInstance().getAvatar()));

        long burntime = RoomSession.getInstance().getBurntime();
        if (burntime > 0) {
            ExtBean extBean = new ExtBean();
            extBean.setLuck_delete(burntime);
            msgDefinBean.setExt(new Gson().toJson(extBean));
        }

        MsgEntity chatBean = new MsgEntity();
        chatBean.setMsgDefinBean(msgDefinBean);
        chatBean.setPubkey(groupEntity.getIdentifier());
        chatBean.setRecAddress(groupEntity.getIdentifier());
        chatBean.setSendstate(0);
        return chatBean;
    }

    @Override
    public void sendPushMsg(Object bean) {
        MsgDefinBean definBean = ((MsgEntity) bean).getMsgDefinBean();
        String msgStr = new Gson().toJson(definBean);
        Connect.GcmData gcmData = EncryptionUtil.encodeAESGCM(SupportKeyUril.EcdhExts.NONE, StringUtil.hexStringToBytes(groupEntity.getEcdh_key()), msgStr.getBytes());

        //messageData
        Connect.MessageData messageData = Connect.MessageData.newBuilder().
                setCipherData(gcmData).
                setMsgId(definBean.getMessage_id()).
                setTyp(definBean.getType()).
                setReceiverAddress(((MsgEntity) bean).getRecAddress()).build();

        ChatSendManager.getInstance().sendChatAckMsg(SocketACK.GROUP_CHAT, groupEntity.getIdentifier(), messageData);
    }

    @Override
    public String headImg() {
        if (groupEntity == null) return "";
        return groupEntity.getAvatar();
    }

    @Override
    public String nickName() {
        if (groupEntity == null) return "";
        return groupEntity.getName();
    }

    @Override
    public String address() {
        if (groupEntity == null) return "";
        return groupEntity.getIdentifier();
    }

    @Override
    public String roomKey() {
        if (groupEntity == null) return "";
        return groupEntity.getIdentifier();
    }

    @Override
    public int roomType() {
        return 1;
    }

    public String groupEcdh() {
        if (groupEntity == null) return "";
        return groupEntity.getEcdh_key();
    }

    public void setGroupEntity(GroupEntity groupEntity) {
        this.groupEntity = groupEntity;
    }

    public void updateMyNickName(){
        myGroupMember = ContactHelper.getInstance().loadGroupMemByAds(groupEntity.getIdentifier(), MemoryDataManager.getInstance().getAddress());
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
}
