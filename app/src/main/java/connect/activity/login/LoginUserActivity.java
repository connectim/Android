package connect.activity.login;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.protobuf.ByteString;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import connect.activity.base.BaseActivity;
import connect.activity.home.HomeActivity;
import connect.activity.login.bean.UserBean;
import connect.database.SharedPreferenceUtil;
import connect.ui.activity.R;
import connect.utils.ActivityUtil;
import connect.utils.DialogUtil;
import connect.utils.ProgressUtil;
import connect.utils.StringUtil;
import connect.utils.ToastUtil;
import connect.utils.UriUtil;
import connect.utils.okhttp.HttpRequest;
import connect.utils.okhttp.ResultCall;
import connect.wallet.jni.AllNativeMethod;
import protos.Connect;

/**
 * Login
 */
public class LoginUserActivity extends BaseActivity {

    @Bind(R.id.name_et)
    EditText nameEt;
    @Bind(R.id.password_et)
    EditText passwordEt;
    @Bind(R.id.next_btn)
    TextView nextBtn;
    @Bind(R.id.pass_text)
    TextView passText;
    @Bind(R.id.name_text)
    TextView nameText;
    @Bind(R.id.text_forget_password)
    TextView textForgetPassword;
    @Bind(R.id.image_loading)
    View imageLoading;
    @Bind(R.id.relative_login)
    RelativeLayout relativeLogin;

    private LoginUserActivity mActivity;

    public static void startActivity(Activity activity) {
        ActivityUtil.next(activity, LoginUserActivity.class);
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

        nextBtn.setEnabled(false);
        nameEt.addTextChangedListener(textWatcher);
        passwordEt.addTextChangedListener(textWatcher);
        relativeLogin.setEnabled(false);
    }

    @OnClick(R.id.name_text)
    void nameText(View view) {
        nameEt.requestFocus();
    }

    @OnClick(R.id.pass_text)
    void passText(View view) {
        passwordEt.requestFocus();
    }

    @OnClick(R.id.text_forget_password)
    void forgetPasswordClick() {
        DialogUtil.showAlertTextView(mActivity,
                mActivity.getString(R.string.Set_tip_title),
                mActivity.getString(R.string.Login_Connect_Change_Password),
                "", "", false, new DialogUtil.OnItemClickListener() {
                    @Override
                    public void confirm(String value) {

                    }

                    @Override
                    public void cancel() {

                    }
                });
    }

    @OnClick(R.id.relative_login)
    void nextBtn(View view) {
        imageLoading.setVisibility(View.VISIBLE);
        Animation loadAnimation = AnimationUtils.loadAnimation(mActivity, R.anim.loading_white);
        imageLoading.startAnimation(loadAnimation);

        String name = nameEt.getText().toString().trim();
        String password = passwordEt.getText().toString();
        Connect.LoginReq loginReq = Connect.LoginReq.newBuilder()
                .setUsername(name)
                .setPassword(password).build();
        HttpRequest.getInstance().post(UriUtil.CONNECT_V3_LOGIN, loginReq, new ResultCall<Connect.HttpNotSignResponse>() {
            @Override
            public void onResponse(Connect.HttpNotSignResponse response) {
                try {
                    imageLoading.clearAnimation();
                    imageLoading.setVisibility(View.GONE);

                    Connect.StructData structData = Connect.StructData.parseFrom(response.getBody().toByteArray());
                    Connect.UserLoginInfo userLoginInfo = Connect.UserLoginInfo.parseFrom(structData.getPlainData());
                    UserBean userBean = SharedPreferenceUtil.getInstance().getUser();
                    if (userBean == null || TextUtils.isEmpty(userLoginInfo.getPubKey())
                            || !userLoginInfo.getPubKey().equals(userBean.getPubKey())) {
                        requestUpdatePub(userLoginInfo);
                    } else {
                        UserBean userBean1 = new UserBean(userLoginInfo.getName(), userLoginInfo.getAvatar(), userLoginInfo.getUid(),
                                userLoginInfo.getOU(), userLoginInfo.getToken(), userBean.getPubKey(), userBean.getPriKey());
                        userBean1.setEmp_no(userLoginInfo.getEmpNo());
                        userBean1.setGender(userLoginInfo.getGender());
                        userBean1.setMobile(userLoginInfo.getMobile());
                        userBean1.setTips(userLoginInfo.getTips());
                        SharedPreferenceUtil.getInstance().putUser(userBean1);

                        HomeActivity.startActivity(mActivity);
                        mActivity.finish();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Connect.HttpNotSignResponse response) {
                imageLoading.clearAnimation();
                imageLoading.setVisibility(View.GONE);
                ToastUtil.getInstance().showToast(R.string.Login_Password_incorrect);
            }

            @Override
            public void onError() {
                super.onError();
                imageLoading.clearAnimation();
                imageLoading.setVisibility(View.GONE);
            }
        });
    }

    private void requestUpdatePub(final Connect.UserLoginInfo userLoginInfo) {
        final String priKey = AllNativeMethod.cdCreateNewPrivKey();
        final String pubKey1 = AllNativeMethod.cdGetPubKeyFromPrivKey(priKey);
        Connect.PubKey pubKey = Connect.PubKey.newBuilder()
                .setPubKey(pubKey1)
                .build();
        ByteString random = ByteString.copyFrom(StringUtil.getSecureRandom(16));
        Connect.StructData structData = Connect.StructData.newBuilder()
                .setRandom(random)
                .setPlainData(pubKey.toByteString())
                .build();
        Connect.HttpRequest httpRequest = Connect.HttpRequest.newBuilder()
                .setUid(userLoginInfo.getUid())
                .setBody(structData.toByteString())
                .setToken(userLoginInfo.getToken()).build();
        HttpRequest.getInstance().post(UriUtil.CONNECT_V3_PUBKEY, httpRequest, new ResultCall<Connect.HttpNotSignResponse>() {
            @Override
            public void onResponse(Connect.HttpNotSignResponse response) {
                UserBean userBean1 = new UserBean(userLoginInfo.getName(), userLoginInfo.getAvatar(), userLoginInfo.getUid(),
                        userLoginInfo.getOU(), userLoginInfo.getToken(), pubKey1, priKey);
                userBean1.setEmp_no(userLoginInfo.getEmpNo());
                userBean1.setGender(userLoginInfo.getGender());
                userBean1.setMobile(userLoginInfo.getMobile());
                userBean1.setTips(userLoginInfo.getTips());
                SharedPreferenceUtil.getInstance().putUser(userBean1);
                HomeActivity.startActivity(mActivity);
                mActivity.finish();
            }

            @Override
            public void onError(Connect.HttpNotSignResponse response) {
            }
        });
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
            String name = nameEt.getText().toString();
            String password = passwordEt.getText().toString();
            if (TextUtils.isEmpty(name) || TextUtils.isEmpty(password)) {
                relativeLogin.setEnabled(false);
            } else {
                relativeLogin.setEnabled(true);
            }
        }
    };

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

}
