package connect.activity.login;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import connect.database.SharedPreferenceUtil;
import connect.ui.activity.R;
import connect.activity.home.HomeActivity;
import connect.activity.login.bean.UserBean;
import connect.activity.login.contract.LocalLoginContract;
import connect.activity.login.presenter.LocalLoginPresenter;
import connect.activity.set.SafetyPatternActivity;
import connect.activity.base.BaseActivity;
import connect.utils.ActivityUtil;
import connect.utils.ProgressUtil;
import connect.utils.glide.GlideUtil;
import connect.widget.TopToolBar;

/**
 * local login
 * Created by john on 2016/11/23.
 */

public class LoginLocalActivity extends BaseActivity implements LocalLoginContract.View {

    @Bind(R.id.toolbar_top)
    TopToolBar toolbarTop;
    @Bind(R.id.userhead_img)
    ImageView userheadImg;
    @Bind(R.id.nickname_tv)
    TextView nicknameTv;
    @Bind(R.id.password_et)
    EditText passwordEt;
    @Bind(R.id.passwordhint_tv)
    TextView passwordhintTv;
    @Bind(R.id.next_btn)
    Button nextBtn;
    @Bind(R.id.nickname_rela)
    RelativeLayout nicknameLin;

    private LoginLocalActivity mActivity;
    private LocalLoginContract.Presenter presenter;
    private UserBean userBean;
    public static final int SELECT_USER_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_local);
        ButterKnife.bind(this);
        initView();
    }

    @Override
    public void initView() {
        mActivity = this;
        toolbarTop.setLeftImg(R.mipmap.back_black);
        toolbarTop.setTitleImg(R.mipmap.logo_black_middle);
        toolbarTop.setRightText(R.string.Login_Sign_Up);
        passwordEt.addTextChangedListener(textWatcher);

        List<UserBean> list = SharedPreferenceUtil.getInstance().getUserList();
        if(list != null && list.get(0) != null){
            userBean = list.get(0);
            nicknameTv.setText(userBean.getName());
            if (!TextUtils.isEmpty(userBean.getPassHint())) {
                passwordhintTv.setText(getString(R.string.Login_Password_Hint, userBean.getPassHint()));
            }
            GlideUtil.loadAvatarRound(userheadImg,userBean.getAvatar());
        }
        new LocalLoginPresenter(this).start();
    }

    private TextWatcher textWatcher = new TextWatcher(){
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}

        @Override
        public void afterTextChanged(Editable s) {
            if(!TextUtils.isEmpty(passwordEt.getText().toString()) && !TextUtils.isEmpty(nicknameTv.getText().toString())){
                nextBtn.setEnabled(true);
            }else{
                nextBtn.setEnabled(false);
            }
        }
    };

    @OnClick(R.id.left_img)
    void goBack(View view) {
        ActivityUtil.goBack(mActivity);
    }

    @OnClick(R.id.right_lin)
    void signUp(View view) {
        RegisterGetRandomActivity.startActivity(mActivity);
    }

    @OnClick(R.id.next_btn)
    void nextBtn(View view){
        ProgressUtil.getInstance().showProgress(mActivity);
        presenter.checkTalkKey(userBean.getTalkKey(),passwordEt.getText().toString(),userBean);
    }

    @OnClick(R.id.nickname_rela)
    void selectLocalAccount(View view){
        LoginLocalSelectActivity.startActivity(mActivity,userBean,SELECT_USER_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK && requestCode == SELECT_USER_CODE){
            Bundle bundle = data.getExtras();
            userBean = (UserBean)bundle.getSerializable("bean");
            nicknameTv.setText(userBean.getName());
            if (!TextUtils.isEmpty(userBean.getPassHint())) {
                passwordhintTv.setText(getString(R.string.Login_Password_Hint, userBean.getPassHint()));
            } else {
                passwordhintTv.setText("");
            }
            GlideUtil.loadAvatarRound(userheadImg,userBean.getAvatar());
        }else if(requestCode == SELECT_USER_CODE){

            List<UserBean> list = SharedPreferenceUtil.getInstance().getUserList();
            if (list == null || list.size() == 0) {
                ActivityUtil.goBack(mActivity);
            } else {
                userBean = list.get(0);
                nicknameTv.setText(userBean.getName());
                if (!TextUtils.isEmpty(userBean.getPassHint())) {
                    passwordhintTv.setText(getString(R.string.Login_Password_Hint, userBean.getPassHint()));
                }else{
                    passwordhintTv.setText("");
                }
                GlideUtil.loadAvatarRound(userheadImg,userBean.getAvatar());
            }

        }
    }

    @Override
    public void setPresenter(LocalLoginContract.Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public Activity getActivity() {
        return mActivity;
    }

    @Override
    public void complete(boolean isBack) {
        if(isBack){
            HomeActivity.startActivity(mActivity);
        }else{
            SafetyPatternActivity.startActivity(mActivity,SafetyPatternActivity.LOGIN_TYPE);
        }
        mActivity.finish();
    }

}
