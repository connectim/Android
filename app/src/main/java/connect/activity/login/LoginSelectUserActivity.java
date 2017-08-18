package connect.activity.login;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.io.File;
import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import connect.activity.base.BaseActivity;
import connect.activity.home.view.LineDecoration;
import connect.activity.login.adapter.SelectUserAdapter;
import connect.activity.login.bean.UserBean;
import connect.database.SharedPreferenceUtil;
import connect.ui.activity.R;
import connect.utils.ActivityUtil;
import connect.widget.TopToolBar;

/**
 * Select the local user.
 */
public class LoginSelectUserActivity extends BaseActivity {

    @Bind(R.id.toolbar_top)
    TopToolBar toolbarTop;
    @Bind(R.id.recycler_view)
    RecyclerView recyclerView;

    private LoginSelectUserActivity mActivity;
    private SelectUserAdapter selectUserAdapter;
    private ArrayList<UserBean> listUser;

    public static void startActivity(Activity activity,UserBean userBean, int code) {
        Bundle bundle = new Bundle();
        bundle.putSerializable("bean", userBean);
        ActivityUtil.next(activity, LoginSelectUserActivity.class, bundle,code);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_seleuser);
        ButterKnife.bind(this);
        initView();
    }

    @Override
    public void initView() {
        mActivity = this;
        toolbarTop.setLeftImg(R.mipmap.back_black);
        toolbarTop.setTitle(null, R.string.Login_Select_User);
        Bundle bundle = getIntent().getExtras();
        UserBean userBean = (UserBean)bundle.getSerializable("bean");
        listUser = SharedPreferenceUtil.getInstance().getUserList();

        selectUserAdapter = new SelectUserAdapter(mActivity,onItemClickListener);
        recyclerView.setAdapter(selectUserAdapter);

        recyclerView.setLayoutManager(new LinearLayoutManager(mActivity));
        recyclerView.addItemDecoration(new LineDecoration(mActivity));
        recyclerView.addOnScrollListener(onScrollListener);
        selectUserAdapter.setData(listUser,userBean);
    }

    @OnClick(R.id.left_img)
    void goBack(View view) {
        ActivityUtil.goBack(mActivity);
    }

    RecyclerView.OnScrollListener onScrollListener = new RecyclerView.OnScrollListener(){
        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            selectUserAdapter.closeMenu();
        }
    };

    private SelectUserAdapter.OnSelectItemListener onItemClickListener = new SelectUserAdapter.OnSelectItemListener() {
        @Override
        public void onClick(Object object, int position) {
            Bundle bundle = new Bundle();
            bundle.putSerializable("bean",(UserBean)object);
            ActivityUtil.goBackWithResult(mActivity,RESULT_OK,bundle);
        }

        @Override
        public void remove(Object object, int position) {
            UserBean userBean = (UserBean)object;
            listUser.remove(userBean);
            SharedPreferenceUtil.getInstance().putUserList(userBean,SharedPreferenceUtil.USER_LIST_DEL);

            mActivity.deleteDatabase("connect_" + userBean.getPubKey() + ".db");
            File file = new File("/data/data/" + getPackageName().toString() + "/shared_prefs", "sp_" + userBean.getPubKey() + ".xml");
            if (file.exists()) {
                file.delete();
            }
        }
    };
}
