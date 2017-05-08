package connect.ui.activity.set;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
 * Created by Administrator on 2017/1/6.
 */
public class LinkChangeVerifyActivity extends BaseActivity implements SignInVerifyContract.View{

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

    private LinkChangeVerifyActivity mActivity;
    private SignInVerifyContract.Presenter presenter;
    private String type;

    public static void startActivity(Activity activity, String countrycode, String phone, String type) {
        Bundle bundle = new Bundle();
        bundle.putString("countrycode",countrycode);
        bundle.putString("phone",phone);
        bundle.putString("type",type);
        ActivityUtil.next(activity, LinkChangeVerifyActivity.class,bundle);
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
        toolbarTop.setBlackStyle();
        toolbarTop.setTitle(null,R.string.Set_Change_Mobile);
        toolbarTop.setRightImg(R.mipmap.close_white3x);
        Bundle bundle = getIntent().getExtras();
        type = bundle.getString("type");

        String phone = bundle.getString("phone");
        String countryCode = bundle.getString("countrycode");
        phoneTv.setText(countryCode + " " + phone);
        codeEt.addTextChangedListener(textWatcher);
        ToastEUtil.makeText(mActivity,R.string.Login_SMS_code_has_been_send).show();

        setPresenter(new SignInVerifyPresenter(this,countryCode,phone));
    }

    @Override
    public void setPresenter(SignInVerifyContract.Presenter presenter) {
        this.presenter = presenter;
        presenter.start();
    }

    @OnClick(R.id.right_lin)
    void close(View view){
        ActivityUtil.goBack(mActivity);
    }

    @OnClick(R.id.next_btn)
    void nextBtn(View view) {
        String codeStr = codeEt.getText().toString();
        if (codeStr.length() == 6) {
            presenter.requestBindMobile(type);
        } else {
            presenter.reSendCode(1);
        }
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

    }

    @Override
    public void goinRandomSend(String phone, String token) {

    }

    @Override
    public void changeTime(int time,Timer timer) {
        if (codeEt.getText().toString().length() == 6) {
            nextBtn.setText(R.string.Common_OK);
            nextBtn.setEnabled(true);
        } else {
            if (time > 0) {
                nextBtn.setText(String.format(mActivity.getResources().getString(R.string.Login_Resend_Time),time));
                nextBtn.setEnabled(false);
            } else {
                nextBtn.setText(R.string.Login_Resend);
                nextBtn.setEnabled(true);
                voiceTv.setVisibility(View.VISIBLE);
                timer.cancel();
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
            presenter.showChangeText();
        }
    };

}
