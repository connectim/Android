package connect.activity.chat.set.presenter;

import android.app.Activity;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

import connect.activity.chat.ChatActivity;
import connect.activity.chat.bean.Talker;
import connect.activity.chat.set.contract.GroupCreateContract;
import connect.activity.home.bean.HttpRecBean;
import connect.activity.login.bean.UserBean;
import connect.database.SharedPreferenceUtil;
import connect.database.green.DaoHelper.ContactHelper;
import connect.database.green.DaoHelper.ConversionHelper;
import connect.database.green.DaoHelper.MessageHelper;
import connect.database.green.bean.ContactEntity;
import connect.database.green.bean.ConversionEntity;
import connect.database.green.bean.GroupEntity;
import connect.database.green.bean.GroupMemberEntity;
import connect.ui.activity.R;
import connect.utils.ProtoBufUtil;
import connect.utils.RegularUtil;
import connect.utils.StringUtil;
import connect.utils.TimeUtil;
import connect.utils.ToastEUtil;
import connect.utils.UriUtil;
import connect.utils.cryption.DecryptionUtil;
import connect.utils.cryption.EncryptionUtil;
import connect.utils.cryption.SupportKeyUril;
import connect.utils.okhttp.OkHttpUtil;
import connect.utils.okhttp.ResultCall;
import instant.bean.ChatMsgEntity;
import instant.bean.SocketACK;
import instant.sender.SenderManager;
import instant.sender.model.GroupChat;
import protos.Connect;

/**
 * Created by Administrator on 2017/8/9.
 */

public class GroupCreatePresenter implements GroupCreateContract.Presenter{

    private GroupCreateContract.BView view;
    private String pubKey;
    private Activity activity;


    public GroupCreatePresenter(GroupCreateContract.BView view){
        this.view=view;
        view.setPresenter(this);
    }

    @Override
    public void start() {
        pubKey = view.getRoomKey();
        activity = view.getActivity();
    }

    private String groupKey;
    private List<ContactEntity> contactEntities;
    private String groupName;
    private String groupEcdh;

    @Override
    public void requestGroupCreate(List<ContactEntity> contactEntities) {
        this.contactEntities=contactEntities;
        UserBean userBean = SharedPreferenceUtil.getInstance().getUser();
        this.groupName = String.format(activity.getString(R.string.Link_user_friends), userBean.getName());

        String ranprikey = SupportKeyUril.getNewPriKey();
        String randpubkey = SupportKeyUril.getPubKeyFromPriKey(ranprikey);

        byte[] groupecdhkey = SupportKeyUril.getRawECDHKey(ranprikey, randpubkey);
        this.groupEcdh = StringUtil.bytesToHexString(groupecdhkey);
        Connect.CreateGroupMessage createGroupMessage = Connect.CreateGroupMessage.newBuilder()
                .setSecretKey(groupEcdh).build();

        List<Connect.AddGroupUserInfo> groupUserInfos = new ArrayList<>();
        for (ContactEntity entity : contactEntities) {
            String prikey = userBean.getPriKey();
            byte[] memberecdhkey = SupportKeyUril.getRawECDHKey(prikey, entity.getCa_pub());
            Connect.GcmData gcmData = EncryptionUtil.encodeAESGCMStructData(EncryptionUtil.ExtendedECDH.EMPTY, memberecdhkey, createGroupMessage.toByteString());

            String pubkey = userBean.getPubKey();
            String groupHex = StringUtil.bytesToHexString(gcmData.toByteArray());
            String backup = String.format("%1$s/%2$s", pubkey, groupHex);

            Connect.AddGroupUserInfo groupUserInfo = Connect.AddGroupUserInfo.newBuilder()
                    .setUid(entity.getUid())
                    .setBackup(backup).build();
            groupUserInfos.add(groupUserInfo);
        }

        Connect.CreateGroup createGroup = Connect.CreateGroup.newBuilder()
                .setName(groupName)
                .addAllUsers(groupUserInfos).build();
        OkHttpUtil.getInstance().postEncrySelf(UriUtil.CREATE_GROUP, createGroup, new ResultCall<Connect.HttpResponse>() {
            @Override
            public void onResponse(Connect.HttpResponse response) {
                try {
                    Connect.IMResponse imResponse = Connect.IMResponse.parseFrom(response.getBody().toByteArray());
                    if (!SupportKeyUril.verifySign(imResponse.getSign(), imResponse.getCipherData().toByteArray())) {
                        throw new Exception("Validation fails");
                    }

                    Connect.StructData structData = DecryptionUtil.decodeAESGCMStructData(imResponse.getCipherData());
                    Connect.GroupInfo groupInfo = Connect.GroupInfo.parseFrom(structData.getPlainData());
                    if (ProtoBufUtil.getInstance().checkProtoBuf(groupInfo)) {
                        insertLocalData(groupInfo);
                        groupCreateBroadcast();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Connect.HttpResponse response) {
                String contentTxt = response.getMessage();
                if (TextUtils.isEmpty(contentTxt)) {
                    ToastEUtil.makeText(activity, activity.getString(R.string.Network_equest_failed_please_try_again_later), 2).show();
                } else {
                    ToastEUtil.makeText(activity, contentTxt, 2).show();
                }
            }
        });
    }

    public void insertLocalData(Connect.GroupInfo groupInfo){
        this.groupKey = groupInfo.getGroup().getIdentifier();

        ConversionEntity roomEntity = new ConversionEntity();
        roomEntity.setType(1);
        roomEntity.setIdentifier(groupKey);
        roomEntity.setAvatar(RegularUtil.groupAvatar(groupKey));
        roomEntity.setLast_time(TimeUtil.getCurrentTimeInLong());
        roomEntity.setContent(activity.getString(R.string.Chat_Tips));
        ConversionHelper.getInstance().insertRoomEntity(roomEntity);

        GroupEntity groupEntity = new GroupEntity();
        groupEntity.setName(groupName);
        groupEntity.setIdentifier(groupKey);
        groupEntity.setEcdh_key(groupEcdh);
        groupEntity.setAvatar(RegularUtil.groupAvatar(groupKey));
        ContactHelper.getInstance().inserGroupEntity(groupEntity);

        HttpRecBean.sendHttpRecMsg(HttpRecBean.HttpRecType.UpLoadBackUp, groupKey, groupEcdh);
        String stringMems = "";
        List<GroupMemberEntity> memEntities = new ArrayList<>();
        for (Connect.GroupMember member : groupInfo.getMembersList()) {
            GroupMemberEntity memEntity = ContactHelper.getInstance().loadGroupMemberEntity(groupKey, member.getUid());
            if (memEntity == null) {
                memEntity = new GroupMemberEntity();
            }
            memEntity.setIdentifier(groupKey);
            memEntity.setUid(member.getPubKey());
            memEntity.setAvatar(member.getAvatar());
            memEntity.setNick(member.getUsername());
            memEntity.setRole(member.getRole());
            memEntity.setUsername(member.getUsername());
            memEntities.add(memEntity);
            stringMems = stringMems + member.getUsername() + ",";
        }
        ContactHelper.getInstance().inserGroupMemEntity(memEntities);

        GroupChat groupChat = new GroupChat(groupKey);
        stringMems = String.format(activity.getString(R.string.Link_enter_the_group), stringMems);

        ChatMsgEntity invite = groupChat.noticeMsg(0, stringMems, "");
        MessageHelper.getInstance().insertMsgExtEntity(invite);

        ToastEUtil.makeText(activity, activity.getString(R.string.Link_Send_successful), 1, new ToastEUtil.OnToastListener() {
            @Override
            public void animFinish() {
                GroupEntity tempEntity = ContactHelper.getInstance().loadGroupEntity(groupKey);
                ChatActivity.startActivity(activity, new Talker(Connect.ChatType.GROUPCHAT,tempEntity.getIdentifier()));
            }
        }).show();
    }

    public void groupCreateBroadcast() {
        Connect.CreateGroupMessage groupMessage = Connect.CreateGroupMessage.newBuilder().setSecretKey(groupEcdh)
                .setIdentifier(groupKey).build();

        String prikey = SharedPreferenceUtil.getInstance().getUser().getPriKey();
        for (ContactEntity member : contactEntities) {
            String msgid = TimeUtil.timestampToMsgid();
            byte[] groupecdhkey = SupportKeyUril.getRawECDHKey(prikey, member.getCa_pub());
            Connect.GcmData gcmData = EncryptionUtil.encodeAESGCMStructData(EncryptionUtil.ExtendedECDH.EMPTY, groupecdhkey, groupMessage.toByteString());

            Connect.ChatMessage chatMessage = Connect.ChatMessage.newBuilder()
                    .setMsgId(msgid)
                    .setTo(member.getUid())
                    .setCipherData(gcmData).build();

            Connect.MessageData messageData = Connect.MessageData.newBuilder()
                    .setChatMsg(chatMessage)
                    .build();

            SenderManager.senderManager.sendAckMsg(SocketACK.GROUP_INVITE, groupKey,msgid, messageData.toByteString());
        }
    }
}
