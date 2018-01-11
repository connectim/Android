package connect.activity.login;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
    Button nextBtn;
    @Bind(R.id.pass_text)
    TextView passText;
    @Bind(R.id.name_text)
    TextView nameText;

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
    }

    @OnClick(R.id.name_text)
    void nameText(View view) {
        nameEt.requestFocus();
    }

    @OnClick(R.id.pass_text)
    void passText(View view) {
        passwordEt.requestFocus();
    }

    @OnClick(R.id.next_btn)
    void nextBtn(View view) {
        ProgressUtil.getInstance().showProgress(mActivity);
        String name = nameEt.getText().toString().trim();
        String password = passwordEt.getText().toString();
        Connect.LoginReq loginReq = Connect.LoginReq.newBuilder()
                .setUsername(name)
                .setPassword(password).build();
        HttpRequest.getInstance().post(UriUtil.CONNECT_V3_LOGIN, loginReq, new ResultCall<Connect.HttpNotSignResponse>() {
            @Override
            public void onResponse(Connect.HttpNotSignResponse response) {
                try {
                    Connect.StructData structData = Connect.StructData.parseFrom(response.getBody().toByteArray());
                    Connect.UserLoginInfo userLoginInfo = Connect.UserLoginInfo.parseFrom(structData.getPlainData());
                    UserBean userBean = SharedPreferenceUtil.getInstance().getUser();
                    if (userBean == null || TextUtils.isEmpty(userLoginInfo.getPubKey())
                            || !userLoginInfo.getPubKey().equals(userBean.getPubKey())) {
                        requestUpdatePub(userLoginInfo);
                    } else {
                        UserBean userBean1 = new UserBean(userLoginInfo.getName(), userLoginInfo.getAvatar(), userLoginInfo.getUid(),
                                userLoginInfo.getOU(), userLoginInfo.getToken(), userBean.getPubKey(), userBean.getPriKey());
                        SharedPreferenceUtil.getInstance().putUser(userBean1);

                        ProgressUtil.getInstance().dismissProgress();
                        HomeActivity.startActivity(mActivity);
                        mActivity.finish();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Connect.HttpNotSignResponse response) {
                ProgressUtil.getInstance().dismissProgress();
                ToastUtil.getInstance().showToast(R.string.Login_Password_incorrect);
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
            String name = nameEt.getText().toString().trim();
            String password = passwordEt.getText().toString();
            if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(password)) {
                nextBtn.setEnabled(true);
            } else {
                nextBtn.setEnabled(false);
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
