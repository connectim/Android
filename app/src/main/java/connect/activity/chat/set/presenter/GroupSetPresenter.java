package connect.activity.chat.set.presenter;

import android.app.Activity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import connect.activity.chat.bean.RecExtBean;
import connect.activity.chat.set.contract.GroupSetContract;
import connect.activity.contact.bean.ContactNotice;
import connect.activity.home.HomeActivity;
import connect.activity.login.bean.UserBean;
import connect.database.SharedPreferenceUtil;
import connect.database.green.DaoHelper.ContactHelper;
import connect.database.green.DaoHelper.ConversionHelper;
import connect.database.green.DaoHelper.ConversionSettingHelper;
import connect.database.green.bean.ConversionEntity;
import connect.database.green.bean.ConversionSettingEntity;
import connect.database.green.bean.GroupEntity;
import connect.database.green.bean.GroupMemberEntity;
import connect.ui.activity.R;
import connect.utils.ActivityUtil;
import connect.utils.ProtoBufUtil;
import connect.utils.ToastEUtil;
import connect.utils.UriUtil;
import connect.utils.dialog.DialogUtil;
import connect.utils.glide.GlideUtil;
import connect.utils.okhttp.OkHttpUtil;
import connect.utils.okhttp.ResultCall;
import protos.Connect;

/**
 * Created by Administrator on 2017/8/8.
 */

public class GroupSetPresenter implements GroupSetContract.Presenter{

    private final String TAG_ADD = "GROUP_ADD";
    private GroupSetContract.BView view;

    private String roomKey;
    private Activity activity;

    public GroupSetPresenter(GroupSetContract.BView view) {
        this.view = view;
        view.setPresenter(this);
    }

    @Override
    public void start() {
        activity = view.getActivity();
        roomKey = view.getRoomKey();

        GroupEntity groupEntity = ContactHelper.getInstance().loadGroupEntity(roomKey);
        if (groupEntity == null) {
            ActivityUtil.goBack(activity);
            return;
        }

        List<GroupMemberEntity> memberEntities = ContactHelper.getInstance().loadGroupMemEntities(roomKey);
        view.countMember(memberEntities.size());

        int subPosi = memberEntities.size() >= 3 ? 3 : memberEntities.size();
        List<GroupMemberEntity> showMemberEntities = memberEntities.subList(0, subPosi);
        GroupMemberEntity addEntity = new GroupMemberEntity();
        addEntity.setIdentifier(TAG_ADD);
        addEntity.setAvatar(TAG_ADD);
        addEntity.setUid(TAG_ADD);
        showMemberEntities.add(addEntity);
        for (GroupMemberEntity entity : showMemberEntities) {
            View headerview = LayoutInflater.from(activity).inflate(R.layout.linear_contact, null);
            ImageView headimg = (ImageView) headerview.findViewById(R.id.roundimg);
            ImageView adminImg = (ImageView) headerview.findViewById(R.id.img1);
            TextView name = (TextView) headerview.findViewById(R.id.name);

            UserBean userBean = SharedPreferenceUtil.getInstance().getUser();
            if (userBean.getUid() == entity.getUid()) {
                entity.setUsername(userBean.getName());
                entity.setAvatar(userBean.getAvatar());
            }

            if (entity.getRole() != null && entity.getRole() == 1) {
                adminImg.setVisibility(View.VISIBLE);
            } else {
                adminImg.setVisibility(View.GONE);
            }

            String nameTxt = entity.getUsername();
            if (TextUtils.isEmpty(nameTxt)) {
                name.setVisibility(View.GONE);
            } else {
                name.setVisibility(View.VISIBLE);
                if (nameTxt.length() > 3) {
                    nameTxt = nameTxt.substring(0, 3) + "...";
                }
                name.setText(nameTxt);
            }

            if (TAG_ADD.equals(entity.getAvatar())) {
                GlideUtil.loadAvatarRound(headimg, R.mipmap.message_add_friends2x);
            } else {
                GlideUtil.loadAvatarRound(headimg, entity.getAvatar());
            }

            headerview.setTag(entity.getUid());
            view.memberList(headerview);
        }

        view.groupName(groupEntity.getName());

        ConversionEntity conversionEntity = ConversionHelper.getInstance().loadRoomEnitity(roomKey);
        boolean top = false;
        if (conversionEntity != null) {
            top = Integer.valueOf(1).equals(conversionEntity.getTop());
        }
        view.topSwitch(top);

        ConversionSettingEntity setEntity = ConversionSettingHelper.getInstance().loadSetEntity(roomKey);
        if (setEntity == null) {
            setEntity = new ConversionSettingEntity();
            setEntity.setIdentifier(roomKey);
            setEntity.setDisturb(0);
        }
        boolean notice = Integer.valueOf(1).equals(setEntity.getDisturb());
        view.noticeSwitch(notice);

        boolean common = Integer.valueOf(1).equals(groupEntity.getCommon());
        view.commonSwtich(common);

        view.searchGroupHistoryTxt();
        view.clearHistory();
        view.exitGroup();
    }

    @Override
    public void syncGroupInfo() {
        Connect.GroupId groupId = Connect.GroupId.newBuilder()
                .setIdentifier(roomKey)
                .build();

        OkHttpUtil.getInstance().postEncrySelf(UriUtil.GROUP_SETTING_INFO, groupId, new ResultCall<Connect.HttpResponse>() {
            @Override
            public void onResponse(Connect.HttpResponse response) {
                try {
                    Connect.StructData structData = Connect.StructData.parseFrom(response.getBody());
                    Connect.GroupSettingInfo settingInfo = Connect.GroupSettingInfo.parseFrom(structData.getPlainData());
                    if(ProtoBufUtil.getInstance().checkProtoBuf(settingInfo)){
                        view.noticeSwitch(settingInfo.getMute());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Connect.HttpResponse response) {

            }
        });
    }

    @Override
    public void updateGroupMute(final boolean state) {
        Connect.UpdateGroupMute groupMute = Connect.UpdateGroupMute.newBuilder()
                .setIdentifier(roomKey)
                .setMute(state)
                .build();

        OkHttpUtil.getInstance().postEncrySelf(UriUtil.CONNECT_GROUP_MUTE, groupMute, new ResultCall<Connect.HttpResponse>() {
            @Override
            public void onResponse(Connect.HttpResponse response) {
                int disturb = state ? 1 : 0;
                ConversionSettingEntity setEntity = ConversionSettingHelper.getInstance().loadSetEntity(roomKey);
                if (setEntity == null) {
                    setEntity = new ConversionSettingEntity();
                    setEntity.setIdentifier(roomKey);
                }
                setEntity.setDisturb(disturb);
                ConversionSettingHelper.getInstance().insertSetEntity(setEntity);
            }

            @Override
            public void onError(Connect.HttpResponse response) {
                view.noticeSwitch(!state);
            }
        });
    }

    @Override
    public void updateGroupCommon(final boolean state) {
        Connect.GroupId groupId = Connect.GroupId.newBuilder().setIdentifier(roomKey).build();
        String coomonUrl = state ? UriUtil.GROUP_COMMON : UriUtil.GROUP_RECOMMON;
        OkHttpUtil.getInstance().postEncrySelf(coomonUrl, groupId, new ResultCall<Connect.HttpResponse>() {
            @Override
            public void onResponse(Connect.HttpResponse response) {
                int common = state ? 1 : 0;
                GroupEntity groupEntity = ContactHelper.getInstance().loadGroupEntity(roomKey);
                if (!(groupEntity == null || TextUtils.isEmpty(groupEntity.getName()))) {
                    groupEntity.setCommon(common);

                    String groupName = groupEntity.getName();
                    if (TextUtils.isEmpty(groupName)) {
                        groupName = "groupname8";
                    }
                    groupEntity.setName(groupName);

                    ContactHelper.getInstance().inserGroupEntity(groupEntity);
                }
                ContactNotice.receiverGroup();
            }

            @Override
            public void onError(Connect.HttpResponse response) {
                view.commonSwtich(!state);
            }
        });
    }

    @Override
    public void requestExitGroup() {
        DialogUtil.showAlertTextView(activity,
                activity.getResources().getString(R.string.Set_tip_title),
                activity.getResources().getString(R.string.Link_Delete_and_Leave),
                "", "", false, new DialogUtil.OnItemClickListener() {
                    @Override
                    public void confirm(String value) {
                        Connect.GroupId groupId = Connect.GroupId.newBuilder().setIdentifier(roomKey).build();
                        OkHttpUtil.getInstance().postEncrySelf(UriUtil.GROUP_QUIT, groupId, new ResultCall<Connect.HttpResponse>() {
                            @Override
                            public void onResponse(Connect.HttpResponse response) {
                                RecExtBean.getInstance().sendEvent(RecExtBean.ExtType.CLEAR_HISTORY,roomKey);

                                ContactHelper.getInstance().removeGroupInfos(roomKey);
                                //FileUtil.deleteDirectory();
                                ActivityUtil.backActivityWithClearTop(activity, HomeActivity.class);
                            }

                            @Override
                            public void onError(Connect.HttpResponse response) {
                                if(response.getCode() == 2424){
                                    ToastEUtil.makeText(activity, R.string.Link_Already_delete_and_Leave, ToastEUtil.TOAST_STATUS_FAILE).show();
                                } else {
                                    ToastEUtil.makeText(activity, response.getMessage(), ToastEUtil.TOAST_STATUS_FAILE).show();
                                }
                            }
                        });
                    }

                    @Override
                    public void cancel() {

                    }
                });
    }
}
