package connect.activity.chat.set.presenter;

import android.app.Activity;

import java.util.ArrayList;
import java.util.List;

import connect.activity.chat.ChatActivity;
import connect.activity.chat.bean.Talker;
import connect.activity.chat.set.contract.GroupCreateContract;
import connect.activity.login.bean.UserBean;
import connect.database.SharedPreferenceUtil;
import connect.database.green.DaoHelper.ContactHelper;
import connect.database.green.DaoHelper.ConversionHelper;
import connect.database.green.DaoHelper.MessageHelper;
import connect.database.green.bean.ConversionEntity;
import connect.database.green.bean.GroupEntity;
import connect.database.green.bean.GroupMemberEntity;
import connect.ui.activity.R;
import connect.utils.ActivityUtil;
import connect.utils.ProtoBufUtil;
import connect.utils.RegularUtil;
import connect.utils.TimeUtil;
import connect.utils.ToastEUtil;
import connect.utils.UriUtil;
import connect.utils.okhttp.OkHttpUtil;
import connect.utils.okhttp.ResultCall;
import instant.bean.ChatMsgEntity;
import instant.sender.model.GroupChat;
import protos.Connect;

/**
 * Created by PuJin on 2018/1/11.
 */

public class GroupCreatePresenter implements GroupCreateContract.Presenter {

    private Activity activity;
    private GroupCreateContract.BView view;
    private List<Connect.Workmate> contactEntities = new ArrayList<>();

    public GroupCreatePresenter(GroupCreateContract.BView view) {
        this.view = view;
        view.setPresenter(this);
    }

    @Override
    public void start() {
        activity = view.getActivity();
        contactEntities = view.groupMemberList();
    }

    /**
     * @param groupName
     */
    @Override
    public void createGroup(String groupName) {
        view.setLeftEnanle(false);

        List<Connect.AddGroupUserInfo> groupUserInfos = new ArrayList<>();
        for (Connect.Workmate entity : contactEntities) {
            Connect.AddGroupUserInfo userInfo = Connect.AddGroupUserInfo.newBuilder()
                    .setUid(entity.getUid())
                    .build();
            groupUserInfos.add(userInfo);
        }

        Connect.CreateGroup createGroup = Connect.CreateGroup.newBuilder()
                .setName(groupName)
                .addAllUsers(groupUserInfos)
                .build();

        OkHttpUtil.getInstance().postEncrySelf(UriUtil.CREATE_GROUP, createGroup, new ResultCall<Connect.HttpResponse>() {
            @Override
            public void onResponse(Connect.HttpResponse response) {
                try {
                    Connect.StructData structData = Connect.StructData.parseFrom(response.getBody());
                    Connect.GroupInfo groupInfo = Connect.GroupInfo.parseFrom(structData.getPlainData());
                    if (ProtoBufUtil.getInstance().checkProtoBuf(groupInfo)) {
                        insertLocalData(groupInfo);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Connect.HttpResponse response) {
                // - 2421 groupinfo error
                // - 2422 group create failed
                view.setLeftEnanle(true);
                if (response.getCode() == 2421) {
                    ToastEUtil.makeText(activity, R.string.Link_Group_create_information_error, ToastEUtil.TOAST_STATUS_FAILE).show();
                } else if (response.getCode() == 2422) {
                    ToastEUtil.makeText(activity, R.string.Network_equest_failed_please_try_again_later, ToastEUtil.TOAST_STATUS_FAILE).show();
                } else {
                    ToastEUtil.makeText(activity, response.getMessage(), ToastEUtil.TOAST_STATUS_FAILE).show();
                }
            }
        });
    }

    public void insertLocalData(Connect.GroupInfo groupInfo) {
        final String groupKey = groupInfo.getGroup().getIdentifier();
        String groupName = groupInfo.getGroup().getName();

        ConversionEntity roomEntity = new ConversionEntity();
        roomEntity.setType(Connect.ChatType.GROUP_DISCUSSION_VALUE);
        roomEntity.setIdentifier(groupKey);
        roomEntity.setName(groupName);
        roomEntity.setAvatar(groupInfo.getGroup().getAvatar());
        roomEntity.setLast_time(TimeUtil.getCurrentTimeInLong());
        roomEntity.setContent(activity.getString(R.string.Chat_Tips));
        ConversionHelper.getInstance().insertRoomEntity(roomEntity);

        GroupEntity groupEntity = new GroupEntity();
        groupEntity.setName(groupName);
        groupEntity.setIdentifier(groupKey);
        groupEntity.setAvatar(RegularUtil.groupAvatar(groupKey));
        ContactHelper.getInstance().inserGroupEntity(groupEntity);

        String stringMems = "";
        List<GroupMemberEntity> memEntities = new ArrayList<>();
        for (Connect.Workmate contact : contactEntities) {
            GroupMemberEntity memEntity = new GroupMemberEntity();
            memEntity.setIdentifier(groupKey);
            memEntity.setUid(contact.getUid());
            memEntity.setAvatar(contact.getAvatar());
            memEntity.setRole(0);
            memEntity.setUsername(contact.getName());
            memEntities.add(memEntity);
            stringMems = stringMems + contact.getName() + ",";
        }
        UserBean userBean = SharedPreferenceUtil.getInstance().getUser();
        GroupMemberEntity memEntity = new GroupMemberEntity();
        memEntity.setIdentifier(groupKey);
        memEntity.setUid(userBean.getUid());
        memEntity.setAvatar(userBean.getAvatar());
        memEntity.setRole(1);
        memEntity.setUsername(userBean.getName());
        memEntities.add(memEntity);
        ContactHelper.getInstance().inserGroupMemEntity(memEntities);

        GroupChat groupChat = new GroupChat(groupKey);
        stringMems = String.format(activity.getString(R.string.Link_enter_the_group), stringMems);

        ChatMsgEntity invite = groupChat.noticeMsg(0, stringMems, "");
        MessageHelper.getInstance().insertMsgExtEntity(invite);

        ToastEUtil.makeText(activity, activity.getString(R.string.Chat_Create_Group_Success), 1, new ToastEUtil.OnToastListener() {
            @Override
            public void animFinish() {
                ChatActivity.startActivity(activity, Connect.ChatType.GROUPCHAT, groupKey);
            }
        }).show();
    }
}
