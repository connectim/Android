package connect.activity.set;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import connect.database.SharedPreferenceUtil;
import connect.ui.activity.R;
import connect.activity.login.bean.UserBean;
import connect.activity.set.contract.SafetyLoginPassContract;
import connect.activity.set.presenter.SafetyLoginPassPresenter;
import connect.activity.base.BaseActivity;
import connect.utils.ActivityUtil;
import connect.utils.DialogUtil;
import connect.utils.ProgressUtil;
import connect.utils.RegularUtil;
import connect.utils.ToastUtil;
import connect.widget.TopToolBar;

/**
 * Modify the login password.
 */
public class SafetyLoginPassActivity extends BaseActivity implements SafetyLoginPassContract.View {

    @Bind(R.id.toolbar_top)
    TopToolBar toolbarTop;
    @Bind(R.id.password_et)
    EditText passwordEt;
    @Bind(R.id.password_ib)
    ImageButton passwordIb;
    @Bind(R.id.password_hint_et)
    EditText passwordHintEt;
    @Bind(R.id.password_hint_ib)
    ImageButton passwordHintIb;

    private SafetyLoginPassActivity mActivity;
    private SafetyLoginPassContract.Presenter presenter;

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
        toolbarTop.setTitle(null, R.string.Set_Change_password);
        toolbarTop.setRightText(R.string.Set_Save);
        toolbarTop.setRightTextEnable(false);

        UserBean userBean = SharedPreferenceUtil.getInstance().getUser();
        new SafetyLoginPassPresenter(this).start();

        passwordHintEt.setText(userBean.getPassHint());
        passwordEt.addTextChangedListener(passWatcherNew);
    }

    @Override
    public void setPresenter(SafetyLoginPassContract.Presenter presenter) {
        this.presenter = presenter;
    }

    @OnClick(R.id.left_img)
    void goback(View view) {
        ActivityUtil.goBack(mActivity);
    }

    @OnClick(R.id.password_ib)
    void closeEditNew(View view) {
        passwordEt.setText("");
    }

    @OnClick(R.id.password_hint_ib)
    void closeEditHint(View view) {
        passwordHintEt.setText("");
    }

    @OnClick(R.id.right_lin)
    void saveClick(View view) {
        if (RegularUtil.matches(passwordEt.getText().toString(), RegularUtil.PASSWORD)) {
            String password = passwordEt.getText().toString();
            String passwordHint = passwordHintEt.getText().toString();
            if (TextUtils.isEmpty(password)) {
                ToastUtil.getInstance().showToast(R.string.Login_Password_incorrect);
            } else {
                ProgressUtil.getInstance().showProgress(mActivity);
                presenter.requestPass(password,passwordHint);
            }
        } else {
            DialogUtil.showAlertTextView(mActivity, getString(R.string.Set_tip_title),
                    getString(R.string.Login_letter_number_and_character_must_be_included_in_your_login_password),
                    "", "", true, new DialogUtil.OnItemClickListener() {
                        @Override
                        public void confirm(String value) {}

                        @Override
                        public void cancel() {}
                    });
        }
    }

    private TextWatcher passWatcherNew = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}

        @Override
        public void afterTextChanged(Editable s) {
            String pass = s.toString();
            if(!android.text.TextUtils.isEmpty(pass)){
                toolbarTop.setRightTextEnable(true);
            }else{
                toolbarTop.setRightTextEnable(false);
            }
        }
    };

    @Override
    public Activity getActivity() {
        return mActivity;
    }

    @Override
    public void setHint(String value) {
        passwordHintEt.setText(value);
    }

    @Override
    public void modifySuccess() {
        ActivityUtil.goBack(mActivity);
    }

}
