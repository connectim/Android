package connect.activity.set;

import android.app.Activity;
import android.content.Intent;
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
import connect.activity.login.LoginPhoneCountryCodeActivity;
import connect.activity.login.bean.CountryBean;
import connect.activity.login.bean.UserBean;
import connect.database.SharedPreferenceUtil;
import connect.ui.activity.R;
import connect.utils.ActivityUtil;
import connect.utils.ExCountDownTimer;
import connect.utils.ToastEUtil;
import connect.utils.UriUtil;
import connect.utils.cryption.DecryptionUtil;
import connect.utils.data.PhoneDataUtil;
import connect.utils.okhttp.OkHttpUtil;
import connect.utils.okhttp.ResultCall;
import connect.widget.TopToolBar;
import protos.Connect;

/**
 * bind a new phone number
 */

public class SafetyNewPhoneActivity extends BaseActivity {

    @Bind(R.id.toolbar_top)
    TopToolBar toolbarTop;
    @Bind(R.id.phone_edit)
    EditText phoneEdit;
    @Bind(R.id.code_verify_edit)
    EditText codeVerifyEdit;
    @Bind(R.id.send_tv)
    TextView sendTv;
    @Bind(R.id.next_btn)
    Button nextBtn;
    @Bind(R.id.country_tv)
    TextView countryTv;
    @Bind(R.id.country_ll)
    LinearLayout countryLl;

    private SafetyNewPhoneActivity mActivity;
    private final int COUNTRY_CODE = 100;
    private CountryBean countryBean;
    private String token;

    public static void startActivity(Activity activity, String token) {
        Bundle bundle = new Bundle();
        bundle.putString("token", token);
        ActivityUtil.next(activity, SafetyNewPhoneActivity.class, bundle);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_link_new);
        ButterKnife.bind(this);
    }

    @Override
    public void initView() {
        mActivity = this;
        toolbarTop.setBlackStyle();
        toolbarTop.setLeftImg(R.mipmap.back_white);
        toolbarTop.setTitle(null, R.string.Set_Verify_Phone);

        phoneEdit.addTextChangedListener(textWatcher);
        countryBean = PhoneDataUtil.getInstance().getCurrentCountryCode();
        if(countryBean != null){
            countryTv.setText("+ " + countryBean.getCode());
        }
    }

    @OnClick(R.id.left_img)
    void goBack(View view) {
        ActivityUtil.goBack(mActivity);
    }

    @OnClick(R.id.country_ll)
    void countryCodeClick(View view) {
        ActivityUtil.next(mActivity, LoginPhoneCountryCodeActivity.class, COUNTRY_CODE);
    }

    @OnClick(R.id.send_tv)
    void sendCode(View view) {
        String phone = phoneEdit.getText().toString();
        String countryCode = countryBean.getCode();
        if (!TextUtils.isEmpty(countryCode) && !TextUtils.isEmpty(phone)) {
            sendCode(countryCode + "-" + phone);
        }
    }

    @OnClick(R.id.next_btn)
    void verifyCode(View view) {
        String phone = phoneEdit.getText().toString();
        String countryCode = countryBean.getCode();
        if (!TextUtils.isEmpty(countryCode) && !TextUtils.isEmpty(phone)) {
            verifyCode(countryCode + "-" + phone);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == COUNTRY_CODE) {
            countryBean = (CountryBean) data.getExtras().getSerializable("country");
            countryTv.setText("+ " + countryBean.getCode());
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
            String code = codeVerifyEdit.getText().toString();
            String phone = phoneEdit.getText().toString();
            String countryCode = countryBean.getCode();
            if (!TextUtils.isEmpty(countryCode) && !TextUtils.isEmpty(phone) && !TextUtils.isEmpty(code)) {
                nextBtn.setEnabled(true);
            } else {
                nextBtn.setEnabled(false);
            }
        }
    };

    private void countdownTime() {
        ExCountDownTimer exCountDownTimer = new ExCountDownTimer(120 * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished, int percent) {
                sendTv.setText(String.format(mActivity.getResources().getString(R.string.Login_Resend_Time), millisUntilFinished / 1000));
                sendTv.setEnabled(false);
            }

            @Override
            public void onFinish() {
                sendTv.setText(R.string.Login_Resend);
                sendTv.setEnabled(true);
            }

            @Override
            public void onPause() {
            }
        };
        exCountDownTimer.start();
    }

    private void sendCode(String phone){
        Connect.SendMobileCode sendMobileCode = Connect.SendMobileCode.newBuilder()
                .setMobile(phone)
                .setCategory(12)
                .build();
        OkHttpUtil.getInstance().postEncrySelf(UriUtil.V2_SMS_SEND, sendMobileCode, new ResultCall<Connect.HttpResponse>() {
            @Override
            public void onResponse(Connect.HttpResponse response) {
                try{
                    Connect.IMResponse imResponse = Connect.IMResponse.parseFrom(response.getBody().toByteArray());
                    Connect.StructData structData = DecryptionUtil.decodeAESGCMStructData(imResponse.getCipherData());
                    Connect.SecurityToken securityToken = Connect.SecurityToken .parseFrom(structData.getPlainData());
                    token = securityToken.getToken();
                    countdownTime();
                } catch (Exception e){
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Connect.HttpResponse response) {
                if(response.getCode() == 2400){
                    ToastEUtil.makeText(mActivity, R.string.Link_Operation_frequent, ToastEUtil.TOAST_STATUS_FAILE).show();
                }else{
                    ToastEUtil.makeText(mActivity, R.string.Login_SMS_code_sent_failure, ToastEUtil.TOAST_STATUS_FAILE).show();
                }
            }
        });
    }

    private void verifyCode(final String phone){
        Bundle bundle = getIntent().getExtras();
        Connect.ChangeMobileVerify changeMobileVerify = Connect.ChangeMobileVerify.newBuilder()
                .setMobile(phone)
                .setToken(token)
                .setCode(codeVerifyEdit.getText().toString())
                .setCategory(12)
                .setExToken(bundle.getString("token"))
                .build();
        OkHttpUtil.getInstance().postEncrySelf(UriUtil.V2_SETTING_MOBILE_VERIFY, changeMobileVerify, new ResultCall<Connect.HttpResponse>() {
            @Override
            public void onResponse(Connect.HttpResponse response) {
                try{
                    Connect.IMResponse imResponse = Connect.IMResponse.parseFrom(response.getBody().toByteArray());
                    Connect.StructData structData = DecryptionUtil.decodeAESGCMStructData(imResponse.getCipherData());
                    Connect.SecurityToken securityToken = Connect.SecurityToken .parseFrom(structData.getPlainData());

                    UserBean userBean = SharedPreferenceUtil.getInstance().getUser();
                    userBean.setPhone(phone);
                    SharedPreferenceUtil.getInstance().putUser(userBean);
                    mActivity.finish();
                } catch (Exception e){
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Connect.HttpResponse response) {
                ToastEUtil.makeText(mActivity, response.getMessage(), ToastEUtil.TOAST_STATUS_FAILE).show();
            }
        });
    }

}
