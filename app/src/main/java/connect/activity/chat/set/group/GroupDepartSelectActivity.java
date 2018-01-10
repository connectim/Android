package connect.activity.chat.set.group;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.HorizontalScrollView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import connect.activity.base.BaseActivity;
import connect.activity.base.BaseListener;
import connect.activity.chat.ChatActivity;
import connect.activity.chat.adapter.GroupDepartSelectAdapter;
import connect.activity.chat.bean.Talker;
import connect.activity.company.adapter.NameLinear;
import connect.activity.home.view.LineDecoration;
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
import connect.utils.DialogUtil;
import connect.utils.ProtoBufUtil;
import connect.utils.RegularUtil;
import connect.utils.TimeUtil;
import connect.utils.ToastEUtil;
import connect.utils.UriUtil;
import connect.utils.okhttp.OkHttpUtil;
import connect.utils.okhttp.ResultCall;
import connect.widget.TopToolBar;
import instant.bean.ChatMsgEntity;
import instant.sender.model.GroupChat;
import protos.Connect;

public class GroupDepartSelectActivity extends BaseActivity {

    @Bind(R.id.toolbar_top)
    TopToolBar toolbarTop;
    @Bind(R.id.name_linear)
    NameLinear nameLinear;
    @Bind(R.id.scrollview)
    HorizontalScrollView scrollview;
    @Bind(R.id.recyclerview)
    RecyclerView recyclerview;

    private GroupDepartSelectActivity activity;
    private ArrayList<Connect.Department> nameList = new ArrayList<>();
    private Map<String, Object> selectDeparts = new HashMap<>();//部门 B  成员 W

    private GroupDepartSelectAdapter departSelectAdapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_depart_select);
        ButterKnife.bind(this);
        initView();
    }

    public static void startActivity(Activity activity, String uid) {
        Bundle bundle = new Bundle();
        bundle.putString("Uid", uid);
        ActivityUtil.next(activity, GroupDepartSelectActivity.class, bundle);
    }

    @Override
    public void initView() {
        activity = this;
        toolbarTop.setBlackStyle();
        toolbarTop.setLeftImg(R.mipmap.back_white);
        toolbarTop.setTitle(null, R.string.Chat_set_Create_New_Group);
        toolbarTop.setRightText(getString(R.string.Common_OK));
        toolbarTop.setLeftListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActivityUtil.goBack(activity);
            }
        });
        toolbarTop.setRightListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createGroupDialog();
            }
        });

        String friendUid = getIntent().getStringExtra("Uid");

        nameLinear.setVisibility(View.VISIBLE);
        nameList.clear();
        final Connect.Department department = Connect.Department.newBuilder()
                .setId(2)
                .setName("比特大陆")
                .build();
        nameList.add(department);
        nameLinear.notifyAddView(nameList, scrollview);
        nameLinear.setItemClickListener(onItemClickListener);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(activity);
        recyclerview.setLayoutManager(linearLayoutManager);
        recyclerview.addItemDecoration(new LineDecoration(activity));
        departSelectAdapter = new GroupDepartSelectAdapter(activity);
        recyclerview.setAdapter(departSelectAdapter);
        departSelectAdapter.setFriendUid(friendUid);
        departSelectAdapter.setItemClickListener(new GroupDepartSelectAdapter.GroupDepartSelectListener() {
            @Override
            public boolean isContains(String selectKey) {
                return selectDeparts.containsKey(selectKey);
            }

            @Override
            public void itemClick(Connect.Department department) {
                requestDepartmentInfoShow(department.getId());
            }

            @Override
            public void departmentClick(boolean isSelect, Connect.Department department) {
                final long departmentId = department.getId();
                final String departmentKey = "B" + departmentId;
                if (isSelect) {
                    departmentCount = 0;
                    departSelectBeanList.clear();
                    requestDepartmentAllInfo(departmentId, new BaseListener<List<DepartSelectBean>>() {
                        @Override
                        public void Success(List<DepartSelectBean> ts) {
                            selectDeparts.put(departmentKey, ts);
                        }

                        @Override
                        public void fail(Object... objects) {

                        }
                    });
                } else {
                    selectDeparts.remove(departmentKey);
                }
            }

            @Override
            public void workmateClick(boolean isSelect, Connect.Workmate workmate) {
                final String workmateId = workmate.getUid();
                final String workmateKey = "W" + workmateId;
                if (isSelect) {
                    selectDeparts.put(workmateKey, workmate);
                } else {
                    selectDeparts.remove(workmateKey);
                }
            }
        });

        requestDepartmentInfoShow(department.getId());
    }


    /**
     * 查询该部门下所有的成员信息
     *
     * @param id
     * @param baseListener
     */
    private int departmentCount = 0;
    final List<DepartSelectBean> departSelectBeanList = new ArrayList<>();

    public void requestDepartmentAllInfo(long id, final BaseListener<List<DepartSelectBean>> baseListener) {
        requestDepartmentInfo(id, new BaseListener<Connect.SyncWorkmates>() {
            @Override
            public void Success(Connect.SyncWorkmates syncWorkmates) {
                List<Connect.Department> departments = syncWorkmates.getDepts().getListList();
                List<Connect.Workmate> workmates = syncWorkmates.getWorkmates().getListList();

                for (Connect.Department department1 : departments) {
                    DepartSelectBean selectBean = new DepartSelectBean();
                    selectBean.setDepartment(department1);
                    departSelectBeanList.add(selectBean);
                    departmentCount++;
                    requestDepartmentAllInfo(department1.getId(), baseListener);
                }

                for (Connect.Workmate workmate : workmates) {
                    DepartSelectBean selectBean = new DepartSelectBean();
                    selectBean.setWorkmate(workmate);
                    departSelectBeanList.add(selectBean);
                }

                if (departments == null || departments.size() == 0) { //部门遍历
                    departmentCount--;
                    if (departmentCount <= 0) {
                        baseListener.Success(departSelectBeanList);
                    }
                } else {
                    departmentCount++;
                }
            }

            @Override
            public void fail(Object... objects) {
                baseListener.fail();
            }
        });
    }

    /**
     * 查询部门成员信息
     *
     * @param id
     */
    private void requestDepartmentInfo(long id, final BaseListener<Connect.SyncWorkmates> baseListener) {
        Connect.Department department = Connect.Department.newBuilder()
                .setId(id)
                .build();

        OkHttpUtil.getInstance().postEncrySelf(UriUtil.CONNECT_V3_DEPARTMENT, department, new ResultCall<Connect.HttpNotSignResponse>() {
            @Override
            public void onResponse(Connect.HttpNotSignResponse response) {
                try {
                    Connect.StructData structData = Connect.StructData.parseFrom(response.getBody());
                    Connect.SyncWorkmates syncWorkmates = Connect.SyncWorkmates.parseFrom(structData.getPlainData());
                    baseListener.Success(syncWorkmates);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Connect.HttpNotSignResponse response) {
                baseListener.fail();
            }
        });
    }


    NameLinear.OnItemClickListener onItemClickListener = new NameLinear.OnItemClickListener() {
        @Override
        public void itemClick(int position) {
            for (int i = nameList.size() - 1; i > position; i--) {
                nameList.remove(i);
            }
            nameLinear.notifyAddView(nameList, scrollview);
            long id = nameList.get(position).getId();
            requestDepartmentInfoShow(id);
        }
    };

    protected void requestDepartmentInfoShow(long id) {
        requestDepartmentInfo(id, new BaseListener<Connect.SyncWorkmates>() {
            @Override
            public void Success(Connect.SyncWorkmates syncWorkmates) {
                List<Connect.Department> departments = syncWorkmates.getDepts().getListList();
                List<Connect.Workmate> workmates = syncWorkmates.getWorkmates().getListList();

                List<DepartSelectBean> departSelectBeanList = new ArrayList();
                for (Connect.Department department1 : departments) {
                    DepartSelectBean selectBean = new DepartSelectBean();
                    selectBean.setDepartment(department1);
                    departSelectBeanList.add(selectBean);
                }

                for (Connect.Workmate workmate : workmates) {
                    DepartSelectBean selectBean = new DepartSelectBean();
                    selectBean.setWorkmate(workmate);
                    departSelectBeanList.add(selectBean);
                }
                departSelectAdapter.notifyData(departSelectBeanList);
            }

            @Override
            public void fail(Object... objects) {

            }
        });
    }


    protected void createGroupDialog() {
        UserBean userBean = SharedPreferenceUtil.getInstance().getUser();
        final String defaultGroupName = String.format(activity.getString(R.string.Link_user_friends), userBean.getName());
        DialogUtil.showEditView(activity, "创建群组", getString(R.string.Common_Cancel), getString(R.string.Chat_Complete),
                "", defaultGroupName, "", false, -1, new DialogUtil.OnItemClickListener() {
                    @Override
                    public void confirm(String value) {
                        if (TextUtils.isEmpty(value)) {
                            value = defaultGroupName;
                        }
                        createGroup(value);
                    }

                    @Override
                    public void cancel() {

                    }
                });
    }

    /**
     * @param groupName
     */
    public void createGroup(String groupName) {
        List<Connect.AddGroupUserInfo> groupUserInfos = new ArrayList<>();
        for (Map.Entry<String, Object> it : selectDeparts.entrySet()) {
            String key = it.getKey();
            Object object = it.getValue();

            if (key.contains("B")) {

            } else if (key.contains("W")) {
                DepartSelectBean selectBean = (DepartSelectBean) object;
                Connect.AddGroupUserInfo userInfo = Connect.AddGroupUserInfo.newBuilder()
                        .setUid(selectBean.getWorkmate().getUid())
                        .build();
                groupUserInfos.add(userInfo);
            }
        }
        Connect.CreateGroup createGroup = Connect.CreateGroup.newBuilder()
                .setName(groupName)
                .setCategory(1)
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
//        for (ContactEntity contact : contactEntities) {
//            GroupMemberEntity memEntity = new GroupMemberEntity();
//            memEntity.setIdentifier(groupKey);
//            memEntity.setUid(contact.getUid());
//            memEntity.setAvatar(contact.getAvatar());
//            memEntity.setNick(contact.getUsername());
//            memEntity.setRole(0);
//            memEntity.setUsername(contact.getUsername());
//            memEntities.add(memEntity);
//            stringMems = stringMems + contact.getUsername() + ",";
//        }
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
                ChatActivity.startActivity(activity, new Talker(Connect.ChatType.GROUP_DISCUSSION, groupKey));
            }
        }).show();
    }
}
