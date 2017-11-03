package connect.activity.login;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import connect.activity.base.BaseActivity;
import connect.activity.base.BaseApplication;
import connect.activity.home.HomeActivity;
import connect.activity.login.bean.UserBean;
import connect.activity.login.contract.LoginUserContract;
import connect.activity.login.presenter.LoginUserPresenter;
import connect.ui.activity.R;
import connect.utils.ActivityUtil;
import connect.utils.ProgressUtil;
import connect.utils.glide.GlideUtil;
import connect.widget.TopToolBar;

/**
 * Login phone number verification.
 */
public class LoginUserActivity extends BaseActivity implements LoginUserContract.View {

    @Bind(R.id.toolbar_top)
    TopToolBar toolbarTop;
    @Bind(R.id.userhead_img)
    ImageView userheadImg;
    @Bind(R.id.nickname_et)
    TextView nicknameEt;
    @Bind(R.id.password_et)
    EditText passwordEt;
    @Bind(R.id.passwordhint_tv)
    TextView passwordhintTv;
    @Bind(R.id.next_btn)
    Button nextBtn;

    private LoginUserActivity mActivity;
    private LoginUserContract.Presenter presenter;
    private UserBean userBean;

    /**
     * Login phone number verification.
     *
     * @param activity
     * @param userBean The user information
     */
    public static void startActivity(Activity activity, UserBean userBean) {
        Bundle bundle = new Bundle();
        bundle.putSerializable("user", userBean);
        ActivityUtil.next(activity, LoginUserActivity.class, bundle);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_code);
        ButterKnife.bind(this);
        initView();
    }

    @Override
    public void initView() {
        mActivity = this;
        toolbarTop.setLeftImg(R.mipmap.back_black);
        toolbarTop.setTitle(R.mipmap.logo_black_middle, null);
        Bundle bundle = getIntent().getExtras();
        userBean = (UserBean) bundle.getSerializable("user");
        new LoginUserPresenter(this).start();

        passwordEt.addTextChangedListener(textWatcher);
        nicknameEt.setText(userBean.getName());
        GlideUtil.loadAvatarRound(userheadImg, userBean.getAvatar());
        /*if (!TextUtils.isEmpty(userBean.getPassHint())) {
            passwordhintTv.setText(getString(R.string.Login_Password_Hint, userBean.getPassHint()));
        }*/
        passwordEt.setHint(R.string.Login_Password);
    }

    @OnClick(R.id.left_img)
    void goBack(View view) {
        ActivityUtil.goBack(mActivity);
    }

    @OnClick(R.id.next_btn)
    void nextBtn(View view) {
        ProgressUtil.getInstance().showProgress(mActivity);
        //presenter.checkPassWord(userBean.getTalkKey(), passwordEt.getText().toString(),userBean);
    }

    private TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}
        @Override
        public void afterTextChanged(Editable s) {
            presenter.passEditChange(passwordEt.getText().toString(), nicknameEt.getText().toString());
        }
    };

    @Override
    public void setPresenter(LoginUserContract.Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public Activity getActivity() {
        return mActivity;
    }

    @Override
    public void setNextBtnEnable(boolean isEnable) {
        nextBtn.setEnabled(isEnable);
    }

    @Override
    public void launchHome() {
        List<Activity> list = BaseApplication.getInstance().getActivityList();
        for (Activity activity : list) {
            if (!activity.getClass().getName().equals(mActivity.getClass().getName())) {
                activity.finish();
            }
        }
        HomeActivity.startActivity(mActivity);
        mActivity.finish();
    }

}
