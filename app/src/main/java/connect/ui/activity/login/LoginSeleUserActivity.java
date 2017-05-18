package connect.ui.activity.login;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import connect.db.SharedPreferenceUtil;
import connect.ui.activity.R;
import connect.ui.activity.home.view.LineDecoration;
import connect.ui.activity.login.adapter.OnItemClickListence;
import connect.ui.activity.login.adapter.SeleUserAdapter;
import connect.ui.activity.login.bean.UserBean;
import connect.ui.base.BaseActivity;
import connect.utils.ActivityUtil;
import connect.view.TopToolBar;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Select the local user
 * Created by Administrator on 2016/12/8.
 */
public class LoginSeleUserActivity extends BaseActivity {

    @Bind(R.id.toolbar_top)
    TopToolBar toolbarTop;
    @Bind(R.id.recycler_view)
    RecyclerView recyclerView;

    private LoginSeleUserActivity mActivity;
    private SeleUserAdapter seleUserAdapter;
    private ArrayList<UserBean> listUser;
    private UserBean userBean;

    public static void startActivity(Activity activity,UserBean userBean, int code) {
        Bundle bundle = new Bundle();
        bundle.putSerializable("bean", userBean);
        ActivityUtil.next(activity, LoginSeleUserActivity.class, bundle,code);
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
        listUser = SharedPreferenceUtil.getInstance().getUserList();
        userBean = (UserBean)bundle.getSerializable("bean");

        seleUserAdapter = new SeleUserAdapter(mActivity,onItemClickListence);
        recyclerView.setAdapter(seleUserAdapter);

        recyclerView.setLayoutManager(new LinearLayoutManager(mActivity));
        recyclerView.addItemDecoration(new LineDecoration(mActivity));
        recyclerView.addItemDecoration(new LineDecoration(mActivity));
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                seleUserAdapter.closeMenu();
            }
        });
        seleUserAdapter.setData(listUser,userBean);
    }

    @OnClick(R.id.left_img)
    void goBack(View view) {
        ActivityUtil.goBack(mActivity);
    }

    private OnItemClickListence onItemClickListence = new OnItemClickListence(){
        @Override
        public void onClick(Object object, int position) {
            Bundle bundle = new Bundle();
            bundle.putSerializable("bean",(UserBean)object);
            ActivityUtil.goBackWithResult(mActivity,LocalLoginActivity.SELE_USER_CODE,bundle);
        }

        @Override
        public void itemOnClick(Object object, int position) {
            UserBean userBean = (UserBean)object;
            listUser.remove(userBean);
            SharedPreferenceUtil.getInstance().putUserList(userBean,SharedPreferenceUtil.USER_LIST_DEL);
        }
    };
}
