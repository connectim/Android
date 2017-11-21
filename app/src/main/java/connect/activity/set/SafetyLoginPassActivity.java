package connect.activity.set;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import connect.activity.base.BaseActivity;
import connect.activity.login.bean.UserBean;
import connect.activity.set.contract.SafetyLoginPassContract;
import connect.activity.set.presenter.SafetyLoginPassPresenter;
import connect.database.SharedPreferenceUtil;
import connect.ui.activity.R;
import connect.utils.ActivityUtil;
import connect.utils.RegularUtil;
import connect.utils.ToastEUtil;
import connect.widget.TopToolBar;

/**
 * Modify the login password.
 */
public class SafetyLoginPassActivity extends BaseActivity implements SafetyLoginPassContract.View {

    @Bind(R.id.toolbar_top)
    TopToolBar toolbarTop;
    @Bind(R.id.password_edit)
    EditText passwordEdit;
    @Bind(R.id.phone_ll)
    LinearLayout phoneLl;
    @Bind(R.id.password_confirm_edit)
    EditText passwordConfirmEdit;
    @Bind(R.id.password_ll)
    LinearLayout passwordLl;
    @Bind(R.id.code_verify_edit)
    EditText codeVerifyEdit;
    @Bind(R.id.code_verify_ll)
    LinearLayout codeVerifyLl;
    @Bind(R.id.next_btn)
    Button nextBtn;
    @Bind(R.id.send_tv)
    TextView sendTv;
    @Bind(R.id.password_edit_ll)
    LinearLayout passwordEditLl;

    private SafetyLoginPassActivity mActivity;
    private SafetyLoginPassContract.Presenter presenter;
    private UserBean userBean;
    private boolean isClickSendCode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_changepass);
        ButterKnife.bind(this);
        initView();
    }

    @Override
    public void initView() {
        mActivity = this;
        toolbarTop.setBlackStyle();
        toolbarTop.setLeftImg(R.mipmap.back_white);

        new SafetyLoginPassPresenter(this).start();
        userBean = SharedPreferenceUtil.getInstance().getUser();

        if (userBean.isOpenPassword()) {
            passwordEditLl.setVisibility(View.GONE);
            toolbarTop.setTitle(null, R.string.Set_Close_password);
        } else {
            passwordEditLl.setVisibility(View.VISIBLE);
            toolbarTop.setTitle(null, R.string.Set_Open_password);
        }

        passwordEdit.addTextChangedListener(textWatcher);
        passwordConfirmEdit.addTextChangedListener(textWatcher);
        codeVerifyEdit.addTextChangedListener(textWatcher);
    }

    @Override
    public void setPresenter(SafetyLoginPassContract.Presenter presenter) {
        this.presenter = presenter;
    }

    @OnClick(R.id.left_img)
    void goBack(View view) {
        ActivityUtil.goBack(mActivity);
    }

    @OnClick(R.id.send_tv)
    void sendCode(View view) {
        presenter.requestSendCode(userBean.getPhone());
    }

    @OnClick(R.id.next_btn)
    void confirmPassword(View view) {
        String password = passwordEdit.getText().toString();
        String passwordConfirm = passwordConfirmEdit.getText().toString();
        String code = codeVerifyEdit.getText().toString();
        if (userBean.isOpenPassword()) {
            presenter.requestPassword("", code, 2);
        }else{
            if (!RegularUtil.matches(password, RegularUtil.PASSWORD)) {
                ToastEUtil.makeText(mActivity, R.string.Login_letter_number_and_character_must_be_included_in_your_login_password, ToastEUtil.TOAST_STATUS_FAILE).show();
            } else if (!password.equals(passwordConfirm)) {
                ToastEUtil.makeText(mActivity, R.string.Wallet_Payment_Password_do_not_match, ToastEUtil.TOAST_STATUS_FAILE).show();
            } else {
                presenter.requestPassword(password, code, 1);
            }
        }
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
            String password = passwordEdit.getText().toString();
            String passwordConfirm = passwordConfirmEdit.getText().toString();
            String code = codeVerifyEdit.getText().toString();
            if (userBean.isOpenPassword()) {
                if (!TextUtils.isEmpty(code) && isClickSendCode) {
                    nextBtn.setEnabled(true);
                } else {
                    nextBtn.setEnabled(false);
                }
            }else {
                if (!TextUtils.isEmpty(password) && !TextUtils.isEmpty(passwordConfirm) && !TextUtils.isEmpty(code) && isClickSendCode) {
                    nextBtn.setEnabled(true);
                } else {
                    nextBtn.setEnabled(false);
                }
            }
        }
    };

    @Override
    public Activity getActivity() {
        return mActivity;
    }

    @Override
    public void modifySuccess(int type) {
        if (type == 1) {
            userBean.setOpenPassword(true);
        }else{
            userBean.setOpenPassword(false);
        }
        SharedPreferenceUtil.getInstance().putUser(userBean);
        ActivityUtil.goBack(mActivity);
    }

    @Override
    public void changeBtnTiming(long time) {
        sendTv.setText(String.format(mActivity.getResources().getString(R.string.Login_Resend_Time), time));
        sendTv.setEnabled(false);
    }

    @Override
    public void changeBtnFinish() {
        sendTv.setText(R.string.Login_Resend);
        sendTv.setEnabled(true);
    }

    @Override
    public void setSendCodeStatus(boolean status) {
        isClickSendCode = true;
    }
}
