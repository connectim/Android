package connect.activity.set;

import android.app.Dialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
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
import connect.utils.LoginPassCheckUtil;
import connect.widget.TopToolBar;

/**
 * Account and security.
 */
public class SafetyActivity extends BaseActivity {

    @Bind(R.id.toolbar_top)
    TopToolBar toolbarTop;
    @Bind(R.id.phone_tv)
    TextView phoneTv;
    @Bind(R.id.phone_ll)
    LinearLayout phoneLl;
    @Bind(R.id.password_ll)
    LinearLayout passwordLl;
    @Bind(R.id.payment_ll)
    LinearLayout paymentLl;
    @Bind(R.id.pattern_tv)
    TextView patternTv;
    @Bind(R.id.pattern_ll)
    LinearLayout patternLl;
    @Bind(R.id.pritkey_backup_ll)
    LinearLayout pritkeyBackupLl;

    private SafetyActivity mActivity;
    private UserBean userBean;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_safety);
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
        toolbarTop.setTitle(null, R.string.Set_Account_security);

        userBean = SharedPreferenceUtil.getInstance().getUser();
        if (userBean != null && TextUtils.isEmpty(userBean.getPhone())) {
            phoneTv.setText(R.string.Set_Phone_unbinded);
        } else {
            try {
                String phoneNum = userBean.getPhone();
                String[] spliteArr = phoneNum.split("-");
                String showpPhone = spliteArr == null || spliteArr.length <= 1 ? phoneNum : spliteArr[1];
                phoneTv.setText(showpPhone);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (userBean != null && TextUtils.isEmpty(userBean.getSalt())) {
            patternTv.setText(R.string.Set_Off);
        } else {
            patternTv.setText(R.string.Set_On);
        }
    }

    @OnClick(R.id.left_img)
    void goback(View view) {
        ActivityUtil.goBack(mActivity);
    }

    @OnClick(R.id.phone_ll)
    void goLink(View view) {
        ActivityUtil.next(mActivity,SafetyPhoneActivity.class);
    }

    @OnClick(R.id.password_ll)
    void goPassword(View view) {
        LoginPassCheckUtil.getInstance().checkLoginPass(mActivity, new LoginPassCheckUtil.OnResultListener() {
            @Override
            public void success(String priKey) {
                ActivityUtil.next(mActivity,SafetyLoginPassActivity.class);
            }

            @Override
            public void error() {}
        });
    }

    @OnClick(R.id.payment_ll)
    void goPayMent(View view) {
        ActivityUtil.next(mActivity,SafetyPayActivity.class);
    }

    @OnClick(R.id.pattern_ll)
    void goPattern(View view) {
        SafetyPatternActivity.startActivity(mActivity,SafetyPatternActivity.SET_TYPE);
    }

    @OnClick(R.id.pritkey_backup_ll)
    void goBackUp(View view) {
        LoginPassCheckUtil.getInstance().checkLoginPass(mActivity, new LoginPassCheckUtil.OnResultListener() {
            @Override
            public void success(String priKey) {
                ActivityUtil.next(mActivity,SafetyBackupActivity.class);
            }

            @Override
            public void error() {}
        });
    }

}
