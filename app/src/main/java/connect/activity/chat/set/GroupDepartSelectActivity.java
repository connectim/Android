package connect.activity.chat.set;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.HorizontalScrollView;

import com.google.protobuf.InvalidProtocolBufferException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import connect.activity.base.BaseActivity;
import connect.activity.base.BaseListener;
import connect.activity.chat.adapter.GroupDepartSelectAdapter;
import connect.activity.home.view.LineDecoration;
import connect.activity.login.bean.UserBean;
import connect.database.SharedPreferenceUtil;
import connect.database.green.DaoHelper.OrganizerHelper;
import connect.database.green.bean.OrganizerEntity;
import connect.ui.activity.R;
import connect.utils.ActivityUtil;
import connect.utils.UriUtil;
import connect.utils.okhttp.OkHttpUtil;
import connect.utils.okhttp.ResultCall;
import connect.widget.NameLinear;
import connect.widget.TopToolBar;
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
    private boolean isCreate = true;
    private List<String> selectedUids = new ArrayList();
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

    public static void startActivity(Activity activity, boolean iscreate, ArrayList<String> uids) {
        Bundle bundle = new Bundle();
        bundle.putBoolean("Is_Create", iscreate);
        bundle.putSerializable("Uids", uids);
        ActivityUtil.next(activity, GroupDepartSelectActivity.class, bundle, 200);
    }

    @Override
    public void initView() {
        activity = this;
        toolbarTop.setBlackStyle();
        toolbarTop.setLeftImg(R.mipmap.back_white);
        toolbarTop.setRightText(getString(R.string.Chat_Select_Count, 0));
        toolbarTop.setLeftListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActivityUtil.goBack(activity);
            }
        });
        toolbarTop.setRightListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ArrayList<Connect.Workmate> workmates = new ArrayList<Connect.Workmate>();
                for (Map.Entry<String, Object> it : selectDeparts.entrySet()) {
                    String key = it.getKey();
                    Object object = it.getValue();

                    if (key.startsWith("B")) {

                    } else if (key.startsWith("W")) {
                        OrganizerEntity entity = (OrganizerEntity) object;
                        Connect.Workmate workmate = Connect.Workmate.newBuilder()
                                .setName(entity.getName())
                                .setMobile(entity.getMobile())
                                .setAvatar(entity.getAvatar())
                                .setGender(entity.getGender())
                                .setEmpNo(entity.getEmpNo())
                                .setUid(entity.getUid())
                                .build();

                        workmates.add(workmate);
                    } else {
                        OrganizerEntity entity = (OrganizerEntity) object;
                        Connect.Workmate workmate = Connect.Workmate.newBuilder()
                                .setName(entity.getName())
                                .setMobile(entity.getMobile())
                                .setAvatar(entity.getAvatar())
                                .setGender(entity.getGender())
                                .setEmpNo(entity.getEmpNo())
                                .setUid(entity.getUid())
                                .build();

                        workmates.add(workmate);
                    }
                }

                Bundle bundle = new Bundle();
                bundle.putSerializable("ArrayList", workmates);
                ActivityUtil.goBackWithResult(activity, 200, bundle);
            }
        });

        isCreate = getIntent().getBooleanExtra("Is_Create", true);
        selectedUids = (List<String>) getIntent().getSerializableExtra("Uids");
        if (isCreate) {
            toolbarTop.setTitle(getResources().getString(R.string.Link_Group_Create));
            if (selectedUids.size() >= 2) {
                toolbarTop.setRightTextEnable(true);
            } else {
                toolbarTop.setRightTextEnable(false);
            }
            toolbarTop.setRightText(getString(R.string.Chat_Select_Count, selectedUids.size()));
        } else {
            toolbarTop.setTitle(getResources().getString(R.string.Link_Group_Invite));
            toolbarTop.setRightTextEnable(false);
            toolbarTop.setRightText(getString(R.string.Chat_Select_Count, 0));
        }

        nameLinear.setVisibility(View.VISIBLE);
        nameList.clear();
        Connect.Department department = Connect.Department.newBuilder()
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
        departSelectAdapter.setFriendUid(selectedUids);
        departSelectAdapter.setItemClickListener(new GroupDepartSelectAdapter.GroupDepartSelectListener() {

            UserBean userBean = SharedPreferenceUtil.getInstance().getUser();

            @Override
            public boolean isContains(String selectKey) {
                return selectDeparts.containsKey(selectKey) || selectedUids.contains(selectKey.substring(1));
            }

            @Override
            public void itemClick(OrganizerEntity department) {
                if (department != null || department.getId() != null) {
                    requestDepartmentInfoShow(department.getId());

                    Connect.Department department1 = Connect.Department.newBuilder()
                            .setId(department.getId())
                            .setCount(department.getCount())
                            .setName(department.getName())
                            .build();
                    nameList.add(department1);
                    nameLinear.notifyAddView(nameList, scrollview);
                }
            }

            @Override
            public void departmentClick(boolean isSelect, OrganizerEntity department) {
                final long departmentId = department.getId();
                final String departmentKey = "B" + departmentId;
                if (isSelect) {
                    requestDepartmentWorksById(departmentId, new BaseListener<Connect.Workmates>() {
                        @Override
                        public void Success(Connect.Workmates workmates) {
                            selectDeparts.put(departmentKey, "");

                            for (Connect.Workmate workmate : workmates.getListList()) {
                                if (workmate.getRegisted()) {
                                    String uid = workmate.getUid();
                                    String myUid =userBean.getUid();
                                    if (!selectedUids.contains(uid) && !uid.equals(myUid)) {
                                        String workmateKey = "W" + uid;
                                        OrganizerEntity organizerEntity = getOrganizerEntity(workmate);
                                        selectDeparts.put(workmateKey, organizerEntity);
                                    }
                                }
                            }

                            int countSelect = 0;
                            for (String key : selectDeparts.keySet()) {
                                if (key.contains("W")) {
                                    countSelect++;
                                }
                            }
                            if (isCreate) {
                                countSelect = countSelect + selectedUids.size();
                            }
                            toolbarTop.setRightText(getString(R.string.Chat_Select_Count, countSelect));
                            toolbarTop.setRightTextEnable(isCreate?countSelect >= 2:countSelect >= 1);
                        }

                        @Override
                        public void fail(Object... objects) {

                        }
                    });
                } else {
                    requestDepartmentWorksById(departmentId, new BaseListener<Connect.Workmates>() {
                        @Override
                        public void Success(Connect.Workmates workmates) {
                            selectDeparts.remove(departmentKey);
                            for (Connect.Workmate workmate : workmates.getListList()) {
                                String workmateKey = "W" + workmate.getUid();
                                selectDeparts.remove(workmateKey);
                            }

                            int countSelect = 0;
                            for (String key : selectDeparts.keySet()) {
                                if (key.contains("W")) {
                                    countSelect++;
                                }
                            }
                            if (isCreate) {
                                countSelect = countSelect + selectedUids.size();
                            }
                            toolbarTop.setRightText(getString(R.string.Chat_Select_Count, countSelect));
                            toolbarTop.setRightTextEnable(isCreate?countSelect >= 2:countSelect >= 1);
                        }

                        @Override
                        public void fail(Object... objects) {

                        }
                    });
                }
            }

            @Override
            public void workmateClick(boolean isSelect, OrganizerEntity workmate) {
                final String workmateId = workmate.getUid();
                final String workmateKey = "W" + workmateId;
                if (isSelect) {
                    if (workmate.getRegisted()) {
                        selectDeparts.put(workmateKey, workmate);
                    }
                } else {
                    selectDeparts.remove(workmateKey);
                }

                int countSelect = 0;
                for (String key : selectDeparts.keySet()) {
                    if (key.contains("W")) {
                        countSelect++;
                    }
                }
                if (isCreate) {
                    countSelect = countSelect + selectedUids.size();
                }
                toolbarTop.setRightText(getString(R.string.Chat_Select_Count, countSelect));
                if (countSelect >= 1) {
                    toolbarTop.setRightTextEnable(true);
                } else {
                    toolbarTop.setRightTextEnable(false);
                }
            }
        });

        requestDepartmentInfoShow(2);
        if (selectedUids != null && isCreate && selectedUids.size() > 0) {
            String id = selectedUids.get(0);
            if (!TextUtils.isEmpty(id)) {
                requestUserInfo(id);
            }
        }
    }

    /**
     * 查询部门信息
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

    /**
     * 查询该部门所有成员
     *
     * @param id
     */
    private void requestDepartmentWorksById(long id, final BaseListener<Connect.Workmates> baseListener) {
        Connect.Department department = Connect.Department.newBuilder()
                .setId(id)
                .build();

        OkHttpUtil.getInstance().postEncrySelf(UriUtil.CONNECT_V3_DEPAERTMENT_WORKMATES, department, new ResultCall<Connect.HttpNotSignResponse>() {
            @Override
            public void onResponse(Connect.HttpNotSignResponse response) {
                try {
                    Connect.StructData structData = Connect.StructData.parseFrom(response.getBody());
                    Connect.Workmates workmates = Connect.Workmates.parseFrom(structData.getPlainData());
                    baseListener.Success(workmates);
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
            Connect.Department department = nameList.get(position);
            if(department!=null){
                long id = department.getId();
                requestDepartmentInfoShow(id);
            }
        }
    };

    protected void requestDepartmentInfoShow(final long id) {
        List<OrganizerEntity> entities = OrganizerHelper.getInstance().loadParamEntityByUpperId(id);
        departSelectAdapter.notifyData(entities);

        requestDepartmentInfo(id, new BaseListener<Connect.SyncWorkmates>() {
            @Override
            public void Success(Connect.SyncWorkmates syncWorkmates) {
                List<Connect.Department> departments = syncWorkmates.getDepts().getListList();
                List<Connect.Workmate> workmates = syncWorkmates.getWorkmates().getListList();

                List<OrganizerEntity> departSelectBeanList = new ArrayList();
                for (Connect.Department department1 : departments) {
                    OrganizerEntity organizerEntity = new OrganizerEntity();
                    organizerEntity.setUpperId(id);
                    organizerEntity.setId(department1.getId());
                    organizerEntity.setName(department1.getName());
                    organizerEntity.setCount(department1.getCount());
                    departSelectBeanList.add(organizerEntity);
                }

                for (Connect.Workmate workmate : workmates) {
                    if (workmate.getRegisted()) {
                        OrganizerEntity organizerEntity = getOrganizerEntity(workmate);
                        organizerEntity.setUpperId(id);
                        departSelectBeanList.add(organizerEntity);
                    }
                }
                departSelectAdapter.notifyData(departSelectBeanList);

                OrganizerHelper.getInstance().removeOrganizerEntityByUpperId(id);
                OrganizerHelper.getInstance().insertOrganizerEntities(departSelectBeanList);
            }

            @Override
            public void fail(Object... objects) {

            }
        });
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
                    Connect.Workmate workmate = Connect.Workmate.newBuilder()
                            .setAvatar(userInfo1.getAvatar())
                            .setName(userInfo1.getName())
                            .setUid(userInfo1.getUid())
                            .setPubKey(userInfo1.getCaPub())
                            .build();

                    OrganizerEntity organizerEntity = getOrganizerEntity(workmate);
                    selectDeparts.put("F", organizerEntity);
                } catch (InvalidProtocolBufferException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Connect.HttpNotSignResponse response) {
            }
        });
    }

    private OrganizerEntity getOrganizerEntity(Connect.Workmate workmate){
        OrganizerEntity entity = new OrganizerEntity();
        entity.setUid(workmate.getUid());
        entity.setName(workmate.getName());
        entity.setAvatar(workmate.getAvatar());
        entity.setO_u(workmate.getOU());
        entity.setPub_key(workmate.getPubKey());
        entity.setRegisted(workmate.getRegisted());
        entity.setEmpNo(workmate.getEmpNo());
        entity.setMobile(workmate.getMobile());
        entity.setGender(workmate.getGender());
        entity.setTips(workmate.getTips());
        return entity;
    }
}
