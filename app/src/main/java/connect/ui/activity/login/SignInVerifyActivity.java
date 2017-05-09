package connect.ui.activity.login;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.Timer;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import connect.ui.activity.R;
import connect.ui.activity.login.bean.UserBean;
import connect.ui.activity.login.contract.SignInVerifyContract;
import connect.ui.activity.login.presenter.SignInVerifyPresenter;
import connect.ui.base.BaseActivity;
import connect.utils.ActivityUtil;
import connect.utils.ToastEUtil;
import connect.view.TopToolBar;

/**
 * Verify the sms
 * Created by john on 2016/11/22.
 */

public class SignInVerifyActivity extends BaseActivity implements SignInVerifyContract.View{

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

    private SignInVerifyActivity mActivity;
    private SignInVerifyContract.Presenter presenter;

    public static void startActivity(Activity activity, String countrycode, String phone) {
        Bundle bundle = new Bundle();
        bundle.putString("countrycode", countrycode);
        bundle.putString("phone", phone);
        ActivityUtil.next(activity, SignInVerifyActivity.class, bundle);
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
        String countryCode = bundle.getString("countrycode");
        setPresenter(new SignInVerifyPresenter(this,countryCode,phone));

        phoneTv.setText("+" + countryCode + " " + phone);
        codeEt.addTextChangedListener(presenter.getEditChange());
        codeEt.requestFocus();
        ToastEUtil.makeText(mActivity,R.string.Login_SMS_code_has_been_send).show();
        presenter.start();
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
        CodeLoginActivity.startActivity(mActivity, userBean);
        mActivity.finish();
    }

    @Override
    public void goinRandomSend(String phone, String token) {
        RandomSendActivity.startActivity(mActivity, phone, token);
    }

    @Override
    public void changeTime(int time,Timer timer) {
        if (codeEt.getText().toString().length() == 6) {
            nextBtn.setText(R.string.Login_Next);
            nextBtn.setEnabled(true);
        } else {
            if (time > 0) {
                nextBtn.setText(String.format(mActivity.getResources().getString(R.string.Login_Resend_Time), time));
                nextBtn.setEnabled(false);
            } else {
                nextBtn.setText(R.string.Login_Resend);
                nextBtn.setEnabled(true);
                textView3.setText(R.string.Set_Did_not_receive_the_verification_code);
                voiceTv.setVisibility(View.VISIBLE);
                timer.cancel();
            }
        }
    }

}
