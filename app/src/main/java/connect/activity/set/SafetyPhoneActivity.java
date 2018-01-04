package connect.activity.set;

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
import connect.database.SharedPreferenceUtil;
import connect.ui.activity.R;
import connect.utils.ActivityUtil;
import connect.utils.ExCountDownTimer;
import connect.utils.ToastEUtil;
import connect.utils.UriUtil;
import connect.utils.okhttp.OkHttpUtil;
import connect.utils.okhttp.ResultCall;
import connect.widget.TopToolBar;
import protos.Connect;

/**
 * binding mobile phone number
 */
public class SafetyPhoneActivity extends BaseActivity {

    @Bind(R.id.toolbar_top)
    TopToolBar toolbarTop;
    @Bind(R.id.phone_tv)
    TextView phoneTv;
    @Bind(R.id.code_verify_edit)
    EditText codeVerifyEdit;
    @Bind(R.id.send_tv)
    TextView sendTv;
    @Bind(R.id.next_btn)
    Button nextBtn;

    private SafetyPhoneActivity mActivity;
    private UserBean userBean;
    private String token = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_link);
        ButterKnife.bind(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        initView();
    }

    @Override
    public void initView() {
        mActivity = this;
        toolbarTop.setBlackStyle();
        toolbarTop.setLeftImg(R.mipmap.back_white);
        toolbarTop.setTitle(null, R.string.Set_Verify_Phone);

        userBean = SharedPreferenceUtil.getInstance().getUser();
        /*if (!TextUtils.isEmpty(userBean.getPhone())) {
            phoneTv.setText(userBean.getPhone());
        }*/
        codeVerifyEdit.addTextChangedListener(textWatcher);
    }

    @OnClick(R.id.left_img)
    void goBack(View view) {
        ActivityUtil.goBack(mActivity);
    }

    @OnClick(R.id.send_tv)
    void sendCode(View view) {
        sendCode();
    }

    @OnClick(R.id.next_btn)
    void verifyCode(View view) {
        verifyCode();
    }

    private TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}
        @Override
        public void afterTextChanged(Editable s) {
            String code = codeVerifyEdit.getText().toString();
            /*if (!TextUtils.isEmpty(userBean.getPhone()) && !TextUtils.isEmpty(code)) {
                nextBtn.setEnabled(true);
            } else {
                nextBtn.setEnabled(false);
            }*/
        }
    };

    private void countdownTime(){
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

    private void sendCode(){
        Connect.SendMobileCode sendMobileCode = Connect.SendMobileCode.newBuilder()
                .setCategory(11)
                .build();
        OkHttpUtil.getInstance().postEncrySelf(UriUtil.V2_SMS_SEND, sendMobileCode, new ResultCall<Connect.HttpResponse>() {
            @Override
            public void onResponse(Connect.HttpResponse response) {
                try{
                    Connect.HttpNotSignResponse imResponse = Connect.HttpNotSignResponse.parseFrom(response.getBody().toByteArray());
                    Connect.StructData structData = Connect.StructData.parseFrom(imResponse.getBody());
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

    private void verifyCode(){
        Connect.ChangeMobileVerify changeMobileVerify = Connect.ChangeMobileVerify.newBuilder()
                .setToken(token)
                .setCode(codeVerifyEdit.getText().toString())
                .setCategory(11)
                .build();
        OkHttpUtil.getInstance().postEncrySelf(UriUtil.V2_SETTING_MOBILE_VERIFY, changeMobileVerify, new ResultCall<Connect.HttpResponse>() {
            @Override
            public void onResponse(Connect.HttpResponse response) {
                try{
                    Connect.HttpNotSignResponse imResponse = Connect.HttpNotSignResponse.parseFrom(response.getBody().toByteArray());
                    Connect.StructData structData = Connect.StructData.parseFrom(imResponse.getBody());
                    Connect.SecurityToken securityToken = Connect.SecurityToken .parseFrom(structData.getPlainData());
                    SafetyNewPhoneActivity.startActivity(mActivity, securityToken.getToken());
                    mActivity.finish();
                } catch (Exception e){
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Connect.HttpResponse response) {
                if(response.getCode() == 2409){
                    ToastEUtil.makeText(mActivity, R.string.Login_Verification_code_error, ToastEUtil.TOAST_STATUS_FAILE).show();
                }else {
                    ToastEUtil.makeText(mActivity, response.getMessage(), ToastEUtil.TOAST_STATUS_FAILE).show();
                }
            }
        });
    }

}
