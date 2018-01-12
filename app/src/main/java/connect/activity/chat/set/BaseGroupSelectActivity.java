package connect.activity.chat.set;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import connect.activity.base.BaseActivity;
import connect.activity.chat.adapter.BaseGroupSelectAdapter;
import connect.activity.chat.set.group.GroupDepartSelectActivity;
import connect.database.green.DaoHelper.ContactHelper;
import connect.database.green.bean.ContactEntity;
import connect.database.green.bean.GroupMemberEntity;
import connect.ui.activity.R;
import connect.utils.ActivityUtil;
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
        toolbar.setTitle(getResources().getString(R.string.Chat_Choose_contact));
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
                            .setName(entity.getName())
                            .setAvatar(entity.getAvatar())
                            .setGender(entity.getGender())
                            .setMobile(entity.getMobile())
                            .setOU(entity.getOu())
                            .setRegisted(entity.getRegisted())
                            .setTips(entity.getTips())
                            .build();
                    workmates.add(workmate);
                }
                TalkGroupCreateActivity.startActivity(activity, workmates);

                Message message = new Message();
                message.what = 100;
                handler.sendMessageDelayed(message, 3000);
            }
        });

        isCreateGroup = getIntent().getBooleanExtra("Is_Create", true);
        uid = getIntent().getStringExtra("Uid");

        List<ContactEntity> contactEntities = new ArrayList<>();
        if (isCreateGroup) {
            contactEntities = ContactHelper.getInstance().loadFriend();
        }

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
        selectAdapter.setGroupSelectListener(new BaseGroupSelectAdapter.BaseGroupSelectListener() {
            @Override
            public boolean isContains(String selectKey) {
                return selectMembers.containsKey(selectKey);
            }

            @Override
            public void organizeClick() {
                ArrayList<String> selectedUid = new ArrayList<String>();
                if (isCreateGroup) {
                    for (Map.Entry<String, Object> it : selectMembers.entrySet()) {
                        String key = it.getKey();
                        selectedUid.add(key);
                    }
                } else {
                    List<GroupMemberEntity> memberEntities = ContactHelper.getInstance().loadGroupMemEntities(uid);
                    List<String> memberUids = new ArrayList<>();
                    for (GroupMemberEntity entity : memberEntities) {
                        selectedUid.add(entity.getUid());
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
                toolbar.setRightTextEnable(selectMembers.size() >= 3);
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
                        .setPubKey(entity.getPublicKey())
                        .setName(entity.getName())
                        .setAvatar(entity.getAvatar())
                        .setGender(entity.getGender())
                        .setMobile(entity.getMobile())
                        .setOU(entity.getOu())
                        .setRegisted(entity.getRegisted())
                        .setTips(entity.getTips())
                        .build();
                workmates.add(workmate);
            }

            List<Connect.Workmate> organizeWorks = (List<Connect.Workmate>) data.getSerializableExtra("ArrayList");
            workmates.addAll(organizeWorks);
            TalkGroupCreateActivity.startActivity(activity, workmates);
        }
    }
}
