package connect.activity.contact;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import connect.activity.base.BaseActivity;
import connect.activity.contact.adapter.DepartmentAdapter;
import connect.activity.home.view.LineDecoration;
import connect.activity.login.bean.UserBean;
import connect.activity.set.UserInfoActivity;
import connect.database.SharedPreferenceUtil;
import connect.database.green.DaoHelper.OrganizerHelper;
import connect.database.green.bean.ContactEntity;
import connect.database.green.bean.OrganizerEntity;
import connect.ui.activity.R;
import connect.utils.ActivityUtil;
import connect.utils.ToastEUtil;
import connect.utils.UriUtil;
import connect.utils.okhttp.OkHttpUtil;
import connect.utils.okhttp.ResultCall;
import connect.widget.NameLinear;
import connect.widget.TopToolBar;
import protos.Connect;

/**
 *  组织架构
 */
public class DepartmentActivity extends BaseActivity {

    @Bind(R.id.toolbar_top)
    TopToolBar toolbarTop;
    @Bind(R.id.recyclerview)
    RecyclerView recyclerview;
    @Bind(R.id.name_linear)
    NameLinear nameLinear;
    @Bind(R.id.scrollview)
    HorizontalScrollView scrollview;
    @Bind(R.id.left_img)
    ImageView leftImg;
    @Bind(R.id.search_edit)
    EditText searchEdit;
    @Bind(R.id.del_tv)
    ImageView delTv;

    private DepartmentActivity mActivity;
    private DepartmentAdapter adapter;
    private ArrayList<Connect.Department> nameList = new ArrayList<>();
    private UserBean userBean;

    public static void lunchActivity(Activity activity) {
        Bundle bundle = new Bundle();
        ActivityUtil.next(activity, DepartmentActivity.class, bundle);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_company_department);
        ButterKnife.bind(this);
        initView();
    }

    @Override
    public void initView() {
        mActivity = this;
        toolbarTop.setBlackStyle();
        toolbarTop.setLeftImg(R.mipmap.back_white);
        toolbarTop.setTitle(null, R.string.Chat_Organizational_structure);
        userBean = SharedPreferenceUtil.getInstance().getUser();

        nameLinear.setItemClickListener(onItemClickListener);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mActivity);
        recyclerview.setLayoutManager(linearLayoutManager);
        recyclerview.addItemDecoration(new LineDecoration(mActivity));
        adapter = new DepartmentAdapter(mActivity);
        adapter.setItemClickListener(onItemListener);
        recyclerview.setAdapter(adapter);

        searchEdit.setHint(" " + getString(R.string.Link_Search));
        searchEdit.setOnKeyListener(keyListener);
        searchEdit.addTextChangedListener(textWatcher);

        initData();
    }

    private void initData() {
        nameLinear.setVisibility(View.VISIBLE);
        nameList.clear();
        Connect.Department department = Connect.Department.newBuilder()
                .setId(2)
                .setName("组织架构")
                .build();
        nameList.add(department);
        nameLinear.notifyAddView(nameList, scrollview);
        requestDepartment(2L);
    }

    @OnClick(R.id.left_img)
    void goBack(View view) {
        if(nameList.size() == 1){
            ActivityUtil.goBack(mActivity);
        }else{
            nameList.remove(nameList.size() - 1);
            nameLinear.notifyAddView(nameList, scrollview);
            requestDepartment(nameList.get(nameList.size() - 1).getId());
        }
    }

    @OnClick(R.id.del_tv)
    void delEdit(View view) {
        searchEdit.setText("");
    }

    private View.OnKeyListener keyListener = new View.OnKeyListener() {
        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
                ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE))
                        .hideSoftInputFromWindow(DepartmentActivity.this.getCurrentFocus()
                                .getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                requestSearch(searchEdit.getText().toString().trim());
            }
            return false;
        }
    };

    TextWatcher textWatcher = new TextWatcher(){
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count){}
        @Override
        public void afterTextChanged(Editable s) {
            if (TextUtils.isEmpty(s.toString())){
                delTv.setVisibility(View.GONE);
                initData();
            }else{
                delTv.setVisibility(View.VISIBLE);
            }
        }
    };

    NameLinear.OnItemClickListener onItemClickListener = new NameLinear.OnItemClickListener() {
        @Override
        public void itemClick(int position) {
            for (int i = nameList.size() - 1; i > position; i--) {
                nameList.remove(i);
            }
            nameLinear.notifyAddView(nameList, scrollview);
            requestDepartment(nameList.get(position).getId());
        }
    };

    DepartmentAdapter.OnItemClickListener onItemListener = new DepartmentAdapter.OnItemClickListener() {
        @Override
        public void itemClick(OrganizerEntity departmentBean) {
            if (departmentBean.getId() != null) {
                Connect.Department department = Connect.Department.newBuilder()
                        .setId(departmentBean.getId())
                        .setName(departmentBean.getName())
                        .build();
                nameList.add(department);
                nameLinear.notifyAddView(nameList, scrollview);
                requestDepartment(departmentBean.getId());
            } else {
                if(userBean.getUid().equals(departmentBean.getUid())){
                    UserInfoActivity.startActivity(mActivity);
                }else{
                    String department = TextUtils.isEmpty(departmentBean.getO_u()) ? nameList.get(nameList.size()-1).getName() : departmentBean.getO_u();
                    ContactEntity contactEntity = new ContactEntity();
                    contactEntity.setName(departmentBean.getName());
                    contactEntity.setAvatar(departmentBean.getAvatar());
                    contactEntity.setPublicKey(departmentBean.getPub_key());
                    contactEntity.setEmpNo(departmentBean.getEmpNo());
                    contactEntity.setMobile(departmentBean.getMobile());
                    contactEntity.setGender(departmentBean.getGender());
                    contactEntity.setTips(departmentBean.getTips());
                    contactEntity.setRegisted(departmentBean.getRegisted());
                    contactEntity.setUid(departmentBean.getUid());
                    contactEntity.setOu(department);
                    ContactInfoActivity.lunchActivity(mActivity, contactEntity, "");
                }
            }
        }
    };

    private void requestDepartment(final Long id) {
        List<OrganizerEntity> entities = OrganizerHelper.getInstance().loadParamEntityByUpperId(id);
        adapter.setNotify(entities);

        final Connect.Department department = Connect.Department.newBuilder()
                .setId(id)
                .build();
        OkHttpUtil.getInstance().postEncrySelf(UriUtil.CONNECT_V3_DEPARTMENT, department, new ResultCall<Connect.HttpNotSignResponse>() {
            @Override
            public void onResponse(Connect.HttpNotSignResponse response) {
                try {
                    Connect.StructData structData = Connect.StructData.parseFrom(response.getBody());
                    Connect.SyncWorkmates syncWorkmates = Connect.SyncWorkmates.parseFrom(structData.getPlainData());
                    ArrayList<OrganizerEntity> list = new ArrayList<>();
                    for (Connect.Department department1 : syncWorkmates.getDepts().getListList()) {
                        OrganizerEntity departmentBean = new OrganizerEntity();
                        departmentBean.setUpperId(id);
                        departmentBean.setId(department1.getId());
                        departmentBean.setName(department1.getName());
                        departmentBean.setCount(department1.getCount());
                        list.add(departmentBean);
                    }
                    for (Connect.Workmate workmate : syncWorkmates.getWorkmates().getListList()) {
                        OrganizerEntity organizerEntity = getContactBean(workmate);
                        organizerEntity.setUpperId(id);
                        list.add(organizerEntity);
                    }
                    adapter.setNotify(list);

                    OrganizerHelper.getInstance().removeOrganizerEntityByUpperId(id);
                    OrganizerHelper.getInstance().insertOrganizerEntities(list);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onError(Connect.HttpNotSignResponse response) {

            }
        });
    }

    private void requestSearch(String name){
        if(TextUtils.isEmpty(name)){
            return;
        }
        Connect.SearchUser searchUser = Connect.SearchUser.newBuilder()
                .setCriteria(name)
                .build();
        OkHttpUtil.getInstance().postEncrySelf(UriUtil.CONNECT_V3_WORKMATE_SEARCH, searchUser, new ResultCall<Connect.HttpNotSignResponse>() {
            @Override
            public void onResponse(Connect.HttpNotSignResponse response) {
                try {
                    Connect.StructData structData = Connect.StructData.parseFrom(response.getBody());
                    Connect.Workmates workmates = Connect.Workmates.parseFrom(structData.getPlainData());
                    if(workmates.getListList().size() == 0){
                        ToastEUtil.makeText(mActivity, R.string.Wallet_No_match_user).show();
                        return;
                    }
                    ArrayList<OrganizerEntity> list = new ArrayList<>();
                    for (Connect.Workmate workmate : workmates.getListList()) {
                        OrganizerEntity organizerEntity = getContactBean(workmate);
                        list.add(organizerEntity);
                    }
                    adapter.setNotify(list);
                    nameLinear.setVisibility(View.GONE);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Connect.HttpNotSignResponse response) {}
        });
    }

    private OrganizerEntity getContactBean(Connect.Workmate workmate){
        OrganizerEntity departmentBean = new OrganizerEntity();
        departmentBean.setUid(workmate.getUid());
        departmentBean.setName(workmate.getName());
        departmentBean.setAvatar(workmate.getAvatar());
        departmentBean.setO_u(workmate.getOU());
        departmentBean.setPub_key(workmate.getPubKey());
        departmentBean.setRegisted(workmate.getRegisted());
        departmentBean.setEmpNo(workmate.getEmpNo());
        departmentBean.setMobile(workmate.getMobile());
        departmentBean.setGender(workmate.getGender());
        departmentBean.setTips(workmate.getTips());
        return departmentBean;
    }

}
