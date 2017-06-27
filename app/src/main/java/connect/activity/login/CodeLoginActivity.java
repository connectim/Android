package connect.activity.login;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import connect.ui.activity.R;
import connect.activity.home.HomeActivity;
import connect.activity.login.bean.UserBean;
import connect.activity.login.contract.CodeLoginContract;
import connect.activity.login.presenter.CodeLoginPresenter;
import connect.activity.set.PatternActivity;
import connect.activity.base.BaseActivity;
import connect.utils.ActivityUtil;
import connect.utils.DialogUtil;
import connect.utils.ProgressUtil;
import connect.utils.RegularUtil;
import connect.utils.glide.GlideUtil;
import connect.widget.TopToolBar;
import connect.widget.roundedimageview.RoundedImageView;

/**
 * Login phone number verification
 * Created by john on 2016/11/29.
 */
public class CodeLoginActivity extends BaseActivity implements CodeLoginContract.View {

    @Bind(R.id.toolbar_top)
    TopToolBar toolbarTop;
    @Bind(R.id.userhead_img)
    RoundedImageView userheadImg;
    @Bind(R.id.nickname_et)
    TextView nicknameEt;
    @Bind(R.id.password_et)
    EditText passwordEt;
    @Bind(R.id.passwordhint_tv)
    TextView passwordhintTv;
    @Bind(R.id.next_btn)
    Button nextBtn;
    @Bind(R.id.passwordedit_tv)
    TextView passwordeditTv;

    private CodeLoginActivity mActivity;
    private CodeLoginContract.Presenter presenter;
    private UserBean userBean;
    private String token = "";

    public static void startActivity(Activity activity, UserBean userBean) {
        Bundle bundle = new Bundle();
        bundle.putSerializable("user", userBean);
        ActivityUtil.next(activity, CodeLoginActivity.class, bundle);
    }

    public static void startActivity(Activity activity, UserBean userBean, String token) {
        Bundle bundle = new Bundle();
        bundle.putSerializable("user", userBean);
        bundle.putString("token", token);
        ActivityUtil.next(activity, CodeLoginActivity.class, bundle);
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
        token = bundle.getString("token", "");
        setPresenter(new CodeLoginPresenter(this));

        passwordEt.addTextChangedListener(textWatcher);
        nicknameEt.setText(userBean.getName());
        GlideUtil.loadAvater(userheadImg, userBean.getAvatar());
        if (TextUtils.isEmpty(token)) {
            passwordhintTv.setText(getString(R.string.Login_Password_Hint, userBean.getPassHint()));
            passwordeditTv.setVisibility(View.GONE);
            passwordEt.setHint(R.string.Login_Password);
        } else {
            passwordhintTv.setText(getString(R.string.Login_Password_Hint, getString(R.string.Login_Not_set)));
            nextBtn.setText(R.string.Login_Reset_Password_And_Login);
        }
    }

    @Override
    public Activity getActivity() {
        return mActivity;
    }

    @Override
    public void setPresenter(CodeLoginContract.Presenter presenter) {
        this.presenter = presenter;
    }

    @OnClick(R.id.left_img)
    void goBack(View view) {
        ActivityUtil.goBack(mActivity);
    }

    @OnClick(R.id.next_btn)
    void nextBtn(View view) {
        if (TextUtils.isEmpty(token)) {//Login password directly
            ProgressUtil.getInstance().showProgress(mActivity);
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
                            public void confirm(String value) {

                            }

                            @Override
                            public void cancel() {

                            }
                        });
            }
        }
    }

    @OnClick(R.id.passwordedit_tv)
    void editPasswordHint(View view){
        DialogUtil.showEditView(mActivity, mActivity.getResources().getString(R.string.Login_Login_Password_Hint_Title),"", "", "", "", "",
                false,15,new DialogUtil.OnItemClickListener() {
            @Override
            public void confirm(String value) {
                presenter.setPasswordHintData(value);
                passwordhintTv.setText(getString(R.string.Login_Password_Hint,value));
            }

            @Override
            public void cancel() {

            }
        });
    }

    @Override
    public void setNextBtnEnable(boolean isEnable) {
        nextBtn.setEnabled(isEnable);
    }

    @Override
    public void setPasswordhint(String text) {
        passwordhintTv.setText(text);
    }

    @Override
    public void goinHome(boolean isBack) {
        if(isBack){
            HomeActivity.startActivity(mActivity);
        }else{
            PatternActivity.startActivity(mActivity,PatternActivity.LOGIN_STYPE);
        }
        mActivity.finish();
    }

    private TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            presenter.passEditChange(passwordEt.getText().toString(), nicknameEt.getText().toString());
        }
    };
}
