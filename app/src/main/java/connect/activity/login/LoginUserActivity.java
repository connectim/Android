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

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import connect.activity.base.BaseActivity;
import connect.activity.home.HomeActivity;
import connect.activity.login.bean.UserBean;
import connect.activity.login.contract.LoginUserContract;
import connect.activity.login.presenter.LoginUserPresenter;
import connect.activity.set.SafetyPatternActivity;
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
        toolbarTop.setTitleImg(R.mipmap.logo_black_middle);
        Bundle bundle = getIntent().getExtras();
        userBean = (UserBean) bundle.getSerializable("user");
        new LoginUserPresenter(this).start();

        passwordEt.addTextChangedListener(textWatcher);
        nicknameEt.setText(userBean.getName());
        GlideUtil.loadAvatarRound(userheadImg, userBean.getAvatar());
        if (!TextUtils.isEmpty(userBean.getPassHint())) {
            passwordhintTv.setText(getString(R.string.Login_Password_Hint, userBean.getPassHint()));
        }
        passwordEt.setHint(R.string.Login_Password);

        /*if (TextUtils.isEmpty(token)) {
            if (!TextUtils.isEmpty(userBean.getPassHint())) {
                passwordhintTv.setText(getString(R.string.Login_Password_Hint, userBean.getPassHint()));
            }
            passwordeditTv.setVisibility(View.GONE);
            passwordEt.setHint(R.string.Login_Password);
        } else {
            passwordhintTv.setText(getString(R.string.Login_Password_Hint, getString(R.string.Login_Not_set)));
            nextBtn.setText(R.string.Login_Reset_Password_And_Login);
        }*/
    }

    @OnClick(R.id.left_img)
    void goBack(View view) {
        ActivityUtil.goBack(mActivity);
    }

    @OnClick(R.id.next_btn)
    void nextBtn(View view) {
        ProgressUtil.getInstance().showProgress(mActivity);
        presenter.checkPassWord(userBean.getTalkKey(), passwordEt.getText().toString(),userBean);
        /*if (TextUtils.isEmpty(token)) {//Login password directly
            Progre-ssUtil.getInstance().showProgress(mActivity);
            presenter.checkTalkKey(userBean.getTalkKey(), passwordEt.getText().toString(),userBean);
        } else {//Scan the private key to log in
            if(RegularUtil.matches(passwordEt.getText().toString(), RegularUtil.PASSWORD)){
                ProgressUtil.getInstance().showProgress(mActivity);
                presenter.requestSetPassword(passwordEt.getText().toString(),userBean,token);
            }else{
                DialogUtil.showAlertTextView(mActivity, getString(R.string.Set_tip_title),

                        getString(R.string.Login_letter_number_and_character_must_be_included_in_your_login_password),
                        "", "", true, new DialogUtil.OnItemClickListener() {
                            @Override
                            public void confirm(String value) {}
                            @Override
                            public void cancel() {}
                        });
            }
        }*/
    }

    /*@OnClick(R.id.passwordedit_tv)
    void editPasswordHint(View view){
        DialogUtil.showEditView(mActivity, mActivity.getResources().getString(R.string.Login_Login_Password_Hint_Title),"", "", "", "", "", false,15,
                new DialogUtil.OnItemClickListener() {
            @Override
            public void confirm(String value) {
                presenter.setPasswordHintData(value);
                passwordhintTv.setText(getString(R.string.Login_Password_Hint,value));
            }
            @Override
            public void cancel() {}
        });
    }*/

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
    public void setPasswordHint(String text) {
        passwordhintTv.setText(text);
    }

    @Override
    public void launchHome(boolean isBack) {
        if(isBack){
            HomeActivity.startActivity(mActivity);
        }else{
            SafetyPatternActivity.startActivity(mActivity,SafetyPatternActivity.LOGIN_TYPE);
        }
        mActivity.finish();
    }
}
