package connect.activity.chat.set;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;

import com.google.protobuf.InvalidProtocolBufferException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import connect.activity.base.BaseActivity;
import connect.activity.chat.ChatActivity;
import connect.activity.chat.adapter.BaseGroupSelectAdapter;
import connect.activity.home.bean.GroupRecBean;
import connect.activity.login.bean.UserBean;
import connect.database.SharedPreferenceUtil;
import connect.database.green.DaoHelper.ContactHelper;
import connect.database.green.bean.ContactEntity;
import connect.database.green.bean.GroupMemberEntity;
import connect.ui.activity.R;
import connect.utils.ActivityUtil;
import connect.utils.ToastEUtil;
import connect.utils.UriUtil;
import connect.utils.okhttp.OkHttpUtil;
import connect.utils.okhttp.ResultCall;
import connect.widget.TopToolBar;
import protos.Connect;

public class BaseGroupSelectActivity extends BaseActivity {

    @Bind(R.id.toolbar)
    TopToolBar toolbar;
    @Bind(R.id.recyclerview)
    RecyclerView recyclerview;

    private BaseGroupSelectActivity activity;
    private boolean isCreateGroup = true;
    private String uid = "";
    private Map<String, Object> selectMembers = new HashMap<>();
    private BaseGroupSelectAdapter selectAdapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base_group_select);
        ButterKnife.bind(this);
        initView();
    }

    /**
     * @param activity
     * @param iscreate true: 创建群组   false:邀请入群
     * @param uid
     */
    public static void startActivity(Activity activity, boolean iscreate, String uid) {
        Bundle bundle = new Bundle();
        bundle.putBoolean("Is_Create", iscreate);
        bundle.putString("Uid", uid);
        ActivityUtil.next(activity, BaseGroupSelectActivity.class, bundle);
    }

    @Override
    public void initView() {
        activity = this;
        toolbar.setBlackStyle();
        toolbar.setLeftImg(R.mipmap.back_white);
        toolbar.setRightText(getString(R.string.Chat_Select_Count, 0));
        toolbar.setRightTextEnable(false);
        toolbar.setLeftListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityUtil.goBack(activity);
            }
        });
        toolbar.setRightListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toolbar.setRightTextEnable(false);

                ArrayList<Connect.Workmate> workmates = new ArrayList<Connect.Workmate>();
                for (Map.Entry<String, Object> it : selectMembers.entrySet()) {
                    String key = it.getKey();
                    ContactEntity entity = (ContactEntity) it.getValue();

                    Connect.Workmate workmate = Connect.Workmate.newBuilder()
                            .setPubKey(entity.getPublicKey())
                            .setUid(entity.getUid())
                            .setName(entity.getName())
                            .setAvatar(entity.getAvatar())
                            .setGender(null == entity.getGender() ? 1 : entity.getGender())
                            .setMobile(TextUtils.isEmpty(entity.getMobile()) ? "" : entity.getMobile())
                            .setOU(TextUtils.isEmpty(entity.getOu()) ? "" : entity.getOu())
                            .setRegisted(null == entity.getRegisted() ? false : entity.getRegisted())
                            .setTips(TextUtils.isEmpty(entity.getTips()) ? "" : entity.getTips())
                            .build();
                    workmates.add(workmate);
                }
                GroupCreateActivity.startActivity(activity, isCreateGroup, workmates);

                Message message = new Message();
                message.what = 100;
                handler.sendMessageDelayed(message, 3000);
            }
        });

        isCreateGroup = getIntent().getBooleanExtra("Is_Create", true);
        uid = getIntent().getStringExtra("Uid");
        if (isCreateGroup) {
            toolbar.setTitle(getResources().getString(R.string.Link_Group_Create));
        } else {
            toolbar.setTitle(getResources().getString(R.string.Link_Group_Invite));
        }

        List<ContactEntity> contactEntities = new ArrayList<>();
        if (isCreateGroup) {
            contactEntities = ContactHelper.getInstance().loadFriend();
            requestUserInfo(uid);
        }
        String showRight = getString(R.string.Chat_Select_Count, isCreateGroup ? TextUtils.isEmpty(uid) ? 0 : 1 : 0);
        toolbar.setRightText(showRight);

        //添加组织架构
        ContactEntity originEntity = new ContactEntity();
        originEntity.setName(getString(R.string.Chat_Organizational_structure));
        originEntity.setGender(3);
        contactEntities.add(0, originEntity);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(activity);
        selectAdapter = new BaseGroupSelectAdapter();
        recyclerview.setLayoutManager(linearLayoutManager);
        recyclerview.setAdapter(selectAdapter);
        selectAdapter.setData(contactEntities);
        if (isCreateGroup) {
            selectAdapter.setFriendUid(uid);
        }
        selectAdapter.setGroupSelectListener(new BaseGroupSelectAdapter.BaseGroupSelectListener() {
            @Override
            public boolean isContains(String selectKey) {
                return selectMembers.containsKey(selectKey) || (isCreateGroup && uid.equals(selectKey));
            }

            @Override
            public void organizeClick() {
                ArrayList<String> selectedUid = new ArrayList<String>();
                if (isCreateGroup) {
                    for (Map.Entry<String, Object> it : selectMembers.entrySet()) {
                        String key = it.getKey();
                        if (!TextUtils.isEmpty(key)) {
                            selectedUid.add(key);
                        }
                    }
                } else {
                    List<GroupMemberEntity> memberEntities = ContactHelper.getInstance().loadGroupMemEntities(uid);
                    for (GroupMemberEntity entity : memberEntities) {
                        String uid = entity.getUid();
                        if (!TextUtils.isEmpty(uid)) {
                            selectedUid.add(entity.getUid());
                        }
                    }
                }
                GroupDepartSelectActivity.startActivity(activity, isCreateGroup, selectedUid);
            }

            @Override
            public void itemClick(boolean isSelect, ContactEntity contactEntity) {
                String uid = contactEntity.getUid();
                if (isSelect) {
                    selectMembers.put(uid, contactEntity);
                } else {
                    selectMembers.remove(uid);
                }

                toolbar.setRightText(getString(R.string.Chat_Select_Count, selectMembers.size()));
                toolbar.setRightTextEnable(selectMembers.size() >= 2);
            }
        });
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 100:
                    toolbar.setRightTextEnable(true);
                    break;
            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 200) {
            ArrayList<Connect.Workmate> workmates = new ArrayList<Connect.Workmate>();
            for (Map.Entry<String, Object> it : selectMembers.entrySet()) {
                ContactEntity entity = (ContactEntity) it.getValue();

                Connect.Workmate workmate = Connect.Workmate.newBuilder()
                        .setName(entity.getName())
                        .setUid(entity.getUid())
                        .setAvatar(entity.getAvatar())
                        .build();
                workmates.add(workmate);
            }

            List<Connect.Workmate> organizeWorks = (List<Connect.Workmate>) data.getSerializableExtra("ArrayList");
            if (organizeWorks == null || organizeWorks.size() == 0) {
                return;
            } else {
                for (Connect.Workmate workmate : organizeWorks) {
                    String uid = workmate.getUid();
                    if (!selectMembers.containsKey(uid)) {
                        workmates.add(workmate);
                    }
                }
            }

            if (isCreateGroup) {
                toolbar.setRightText(getString(R.string.Chat_Select_Count, selectMembers.size() + organizeWorks.size()));
            } else {
                toolbar.setRightText(getString(R.string.Chat_Select_Count, organizeWorks.size()));
            }

            if (isCreateGroup) {
                GroupCreateActivity.startActivity(activity, true, workmates);
            } else {
                List<String> uids = new ArrayList<>();
                for (Connect.Workmate workmate : workmates) {
                    uids.add(workmate.getUid());
                }
                inviteJoinGroup(uid, uids);
            }
        }
    }

    public void requestUserInfo(String value) {
        final Connect.SearchUser searchUser = Connect.SearchUser.newBuilder()
                .setTyp(1)
                .setCriteria(value)
                .build();
        OkHttpUtil.getInstance().postEncrySelf(UriUtil.CONNECT_V1_USER_SEARCH, searchUser, new ResultCall<Connect.HttpNotSignResponse>() {
            @Override
            public void onResponse(Connect.HttpNotSignResponse response) {
                try {
                    Connect.StructData structData = Connect.StructData.parseFrom(response.getBody());
                    Connect.UsersInfo userInfo = Connect.UsersInfo.parseFrom(structData.getPlainData());
                    Connect.UserInfo userInfo1 = userInfo.getUsersList().get(0);

                    ContactEntity contactEntity = new ContactEntity();
                    contactEntity.setName(userInfo1.getName());
                    contactEntity.setUid(userInfo1.getUid());
                    contactEntity.setPublicKey(userInfo1.getCaPub());
                    contactEntity.setAvatar(userInfo1.getAvatar());
                    selectMembers.put(userInfo1.getUid(), contactEntity);
                } catch (InvalidProtocolBufferException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Connect.HttpNotSignResponse response) {
            }
        });
    }

    /**
     * 邀请加入群聊
     *
     * @param groupIdentify
     * @param selectUids
     */
    public void inviteJoinGroup(final String groupIdentify, List<String> selectUids) {
        toolbar.setLeftEnable(false);
        UserBean userBean = SharedPreferenceUtil.getInstance().getUser();
        Connect.GroupInviteWorkmate inviteWorkmate = Connect.GroupInviteWorkmate.newBuilder()
                .setInviteBy(userBean.getUid())
                .setIdentifier(groupIdentify)
                .addAllUids(selectUids)
                .build();

        OkHttpUtil.getInstance().postEncrySelf(UriUtil.CONNECT_V3_GROUP_INVITE, inviteWorkmate, new ResultCall<Connect.HttpNotSignResponse>() {
            @Override
            public void onResponse(Connect.HttpNotSignResponse response) {
                GroupRecBean.sendGroupRecMsg(GroupRecBean.GroupRecType.GroupInfo, groupIdentify);

                String showHint = "";
                if (isCreateGroup) {
                    showHint = getResources().getString(R.string.Login_Generated_Successful);
                } else {
                    showHint = getResources().getString(R.string.Link_Group_Invite_Success);
                }
                ToastEUtil.makeText(activity, showHint, 1, new ToastEUtil.OnToastListener() {
                    @Override
                    public void animFinish() {
                        toolbar.setLeftEnable(true);
                        ChatActivity.startActivity(activity, Connect.ChatType.GROUPCHAT, groupIdentify);
                    }
                }).show();
            }

            @Override
            public void onError(Connect.HttpNotSignResponse response) {
                toolbar.setLeftEnable(true);
                if (response.getCode() == 2430) {
                    ToastEUtil.makeText(activity, R.string.Link_Qr_code_is_invalid, ToastEUtil.TOAST_STATUS_FAILE).show();
                } else {
                    String contentTxt = response.getMessage();
                    if (TextUtils.isEmpty(contentTxt)) {
                        ToastEUtil.makeText(activity, activity.getString(R.string.Network_equest_failed_please_try_again_later), 2).show();
                    } else {
                        ToastEUtil.makeText(activity, contentTxt, 2).show();
                    }
                }
            }
        });
    }
}
