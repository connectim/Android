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
import android.widget.ImageView;
import android.widget.TextView;

import com.google.protobuf.InvalidProtocolBufferException;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import connect.activity.base.BaseActivity;
import connect.activity.company.adapter.DepartmentAdapter;
import connect.activity.company.adapter.DepartmentBean;
import connect.activity.contact.adapter.SearchResultAdapter;
import connect.activity.contact.bean.SourceType;
import connect.activity.home.view.LineDecoration;
import connect.activity.login.bean.UserBean;
import connect.activity.set.UserInfoActivity;
import connect.database.SharedPreferenceUtil;
import connect.database.green.DaoHelper.ContactHelper;
import connect.database.green.bean.ContactEntity;
import connect.ui.activity.R;
import connect.utils.ActivityUtil;
import connect.utils.ToastEUtil;
import connect.utils.UriUtil;
import connect.utils.okhttp.OkHttpUtil;
import connect.utils.okhttp.ResultCall;
import connect.widget.TopToolBar;
import protos.Connect;

/**
 * Search for friends results
 */
public class SearchFriendResultActivity extends BaseActivity {

    @Bind(R.id.toolbar)
    TopToolBar toolbar;
    @Bind(R.id.search_edit)
    EditText searchEdit;
    @Bind(R.id.del_tv)
    ImageView delTv;
    @Bind(R.id.no_result_tv)
    TextView noResultTv;
    @Bind(R.id.recyclerview)
    RecyclerView recyclerview;

    private SearchFriendResultActivity mActivity;
    private DepartmentAdapter adapter;
    private UserBean userBean;

    public static void startActivity(Activity activity, String text) {
        Bundle bundle = new Bundle();
        bundle.putString("text", text);
        ActivityUtil.next(activity, SearchFriendResultActivity.class, bundle);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_search_server);
        ButterKnife.bind(this);
        initView();
    }

    @Override
    public void initView() {
        mActivity = this;
        toolbar.setBlackStyle();
        toolbar.setLeftImg(R.mipmap.back_white);
        toolbar.setTitle(null, R.string.Link_Search_friends);
        //toolbar.setRightImg(R.mipmap.search3x);
        userBean = SharedPreferenceUtil.getInstance().getUser();

        searchEdit.setOnKeyListener(keyListener);
        searchEdit.addTextChangedListener(textWatcher);
        String text = getIntent().getExtras().getString("text");
        searchEdit.setText(text);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mActivity);
        recyclerview.setLayoutManager(linearLayoutManager);
        adapter = new DepartmentAdapter(mActivity);
        recyclerview.addItemDecoration(new LineDecoration(mActivity));
        adapter.setItemClickListener(onItemChildClickListener);
        recyclerview.setAdapter(adapter);

        requestSearch();
    }

    @OnClick(R.id.left_img)
    void goBack(View view) {
        ActivityUtil.goBack(mActivity);
    }

    /*@OnClick(R.id.right_lin)
    void searchUser(View view) {
        requestSearch();
    }*/

    @OnClick(R.id.del_tv)
    void delEdit(View view) {
        searchEdit.setText("");
    }

    private View.OnKeyListener keyListener = new View.OnKeyListener() {
        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
                ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE))
                        .hideSoftInputFromWindow(SearchFriendResultActivity.this.getCurrentFocus()
                                .getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                requestSearch();
            }
            return false;
        }
    };

    private TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (TextUtils.isEmpty(s.toString())) {
                delTv.setVisibility(View.GONE);
            } else {
                delTv.setVisibility(View.VISIBLE);
            }
        }
    };

    DepartmentAdapter.OnItemClickListener onItemChildClickListener = new DepartmentAdapter.OnItemClickListener() {
        @Override
        public void itemClick(DepartmentBean departmentBean) {
            if(userBean.getUid().equals(departmentBean.getUid())){
                UserInfoActivity.startActivity(mActivity);
            }else{
                ContactEntity contactEntity = new ContactEntity();
                contactEntity.setName(departmentBean.getName());
                contactEntity.setAvatar(departmentBean.getAvatar());
                contactEntity.setOu(departmentBean.getO_u());
                contactEntity.setPublicKey(departmentBean.getPub_key());
                contactEntity.setEmpNo(departmentBean.getEmpNo());
                contactEntity.setMobile(departmentBean.getMobile());
                contactEntity.setGender(departmentBean.getGender());
                contactEntity.setTips(departmentBean.getTips());
                contactEntity.setRegisted(departmentBean.getRegisted());
                contactEntity.setUid(departmentBean.getUid());
                ContactInfoActivity.lunchActivity(mActivity, contactEntity);
            }
            mActivity.finish();
        }
    };

    private void requestSearch() {
        String searchContext = searchEdit.getText().toString().trim();
        if (TextUtils.isEmpty(searchContext)) {
            recyclerview.setVisibility(View.GONE);
            noResultTv.setVisibility(View.VISIBLE);
            return;
        }

        Connect.SearchUser searchUser = Connect.SearchUser.newBuilder()
                .setCriteria(searchContext)
                .build();
        OkHttpUtil.getInstance().postEncrySelf(UriUtil.CONNECT_V3_WORKMATE_SEARCH, searchUser, new ResultCall<Connect.HttpNotSignResponse>() {
            @Override
            public void onResponse(Connect.HttpNotSignResponse response) {
                try {
                    Connect.StructData structData = Connect.StructData.parseFrom(response.getBody());
                    Connect.Workmates workmates = Connect.Workmates.parseFrom(structData.getPlainData());
                    if (workmates.getListList().size() > 0) {
                        noResultTv.setVisibility(View.GONE);
                        recyclerview.setVisibility(View.VISIBLE);
                        ArrayList<DepartmentBean> list = new ArrayList<>();
                        for (Connect.Workmate workmate : workmates.getListList()) {
                            list.add(getContactBean(workmate));
                        }
                        adapter.setNotify(list);
                    } else {
                        noResultTv.setVisibility(View.VISIBLE);
                        recyclerview.setVisibility(View.GONE);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Connect.HttpNotSignResponse response) {
                noResultTv.setVisibility(View.VISIBLE);
                recyclerview.setVisibility(View.GONE);

                String errorMessage = response.getMessage();
                if (TextUtils.isEmpty(errorMessage)) {
                    errorMessage = mActivity.getString(R.string.Network_equest_failed_please_try_again_later);
                }
                ToastEUtil.makeText(mActivity, errorMessage, 2).show();
            }
        });
    }

    private DepartmentBean getContactBean(Connect.Workmate workmate){
        DepartmentBean departmentBean = new DepartmentBean();
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
