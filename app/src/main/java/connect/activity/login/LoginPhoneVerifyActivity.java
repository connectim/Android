package connect.activity.login;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import connect.ui.activity.R;
import connect.activity.login.bean.UserBean;
import connect.activity.login.contract.SignInVerifyContract;
import connect.activity.login.presenter.SignInVerifyPresenter;
import connect.activity.base.BaseActivity;
import connect.utils.ActivityUtil;
import connect.utils.ToastEUtil;
import connect.widget.TopToolBar;

/**
 * Verify the sms.
 */
public class LoginPhoneVerifyActivity extends BaseActivity implements SignInVerifyContract.View {

    @Bind(R.id.toolbar_top)
    TopToolBar toolbarTop;
    @Bind(R.id.code_et)
    EditText codeEt;
    @Bind(R.id.phone_tv)
    TextView phoneTv;
    @Bind(R.id.voice_tv)
    TextView voiceTv;
    @Bind(R.id.next_btn)
    Button nextBtn;
    @Bind(R.id.textView3)
    TextView textView3;

    private LoginPhoneVerifyActivity mActivity;
    private SignInVerifyContract.Presenter presenter;

    /**
     * Check the verification code into the interface, the incoming telephone number.
     *
     * @param activity context
     * @param countryCode country code
     * @param phone phone number
     */
    public static void startActivity(Activity activity, String countryCode, String phone) {
        Bundle bundle = new Bundle();
        bundle.putString("countryCode", countryCode);
        bundle.putString("phone", phone);
        ActivityUtil.next(activity, LoginPhoneVerifyActivity.class, bundle);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_verify);
        ButterKnife.bind(this);
        initView();
    }

    @Override
    public void initView() {
        mActivity = this;
        toolbarTop.setLeftImg(R.mipmap.back_black);
        toolbarTop.setTitleImg(R.mipmap.logo_black_middle);

        Bundle bundle = getIntent().getExtras();
        String phone = bundle.getString("phone");
        String countryCode = bundle.getString("countryCode");
        new SignInVerifyPresenter(this,countryCode,phone).start();

        phoneTv.setText("+" + countryCode + " " + phone);
        codeEt.addTextChangedListener(textWatcher);
        codeEt.requestFocus();
        ToastEUtil.makeText(mActivity,R.string.Login_SMS_code_has_been_send).show();
    }

    @OnClick(R.id.left_img)
    void goBack(View view) {
        ActivityUtil.goBack(mActivity);
    }

    @OnClick(R.id.voice_tv)
    void sendVoice(View view) {
        presenter.reSendCode(2);
    }

    @OnClick(R.id.next_btn)
    void nextBtn(View view) {
        String codeStr = codeEt.getText().toString();
        if (codeStr.length() == 6) {
            presenter.requestVerifyCode();
        } else {
            presenter.reSendCode(1);
        }
    }

    @Override
    public void setPresenter(SignInVerifyContract.Presenter presenter) {
        this.presenter = presenter;
    }

    public TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}
        @Override
        public void afterTextChanged(Editable s) {
            if (s.toString().length() == 6) {
                presenter.requestVerifyCode();
            }
            if (codeEt.getText().toString().length() == 6) {
                nextBtn.setText(R.string.Login_Next);
                nextBtn.setEnabled(true);
            }
        }
    };

    @Override
    public Activity getActivity() {
        return mActivity;
    }

    @Override
    public String getCode() {
        return codeEt.getText().toString();
    }

    @Override
    public void setVoiceVisi() {
        voiceTv.setVisibility(View.INVISIBLE);
    }

    @Override
    public void goinCodeLogin(UserBean userBean) {
        LoginUserActivity.startActivity(mActivity, userBean);
        mActivity.finish();
    }

    @Override
    public void goinRandomSend(String phone, String token) {
        RegisterGetRandomActivity.startActivity(mActivity, phone, token);
    }

    @Override
    public void changeBtnTiming(long time) {
        if (codeEt.getText().toString().length() != 6) {
            nextBtn.setText(String.format(mActivity.getResources().getString(R.string.Login_Resend_Time), time));
            nextBtn.setEnabled(false);
        }
    }

    @Override
    public void changeBtnFinish() {
        if (codeEt.getText().toString().length() != 6) {
            nextBtn.setText(R.string.Login_Resend);
            nextBtn.setEnabled(true);
            textView3.setText(R.string.Set_Did_not_receive_the_verification_code);
            voiceTv.setVisibility(View.VISIBLE);
        }
    }

}
