package connect.activity.set;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import connect.activity.base.BaseActivity;
import connect.activity.home.bean.HomeAction;
import connect.activity.login.bean.UserBean;
import connect.database.SharedPreferenceUtil;
import connect.ui.activity.R;
import connect.utils.ActivityUtil;
import connect.utils.DialogUtil;
import connect.utils.LoginPassCheckUtil;
import connect.utils.ProgressUtil;
import connect.widget.TopToolBar;
import instant.bean.UserOrderBean;
import instant.utils.manager.FailMsgsManager;

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
    @Bind(R.id.password_tv)
    TextView passwordTv;
    @Bind(R.id.log_out_tv)
    TextView logOutTv;

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
                String[] splitArr = phoneNum.split("-");
                String phone = splitArr == null || splitArr.length <= 1 ? phoneNum : splitArr[1];
                phoneTv.setText(phone);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (userBean.isOpenPassword()) {
            passwordTv.setText(R.string.Set_On);
        } else {
            passwordTv.setText(R.string.Set_Off);
        }
    }

    @OnClick(R.id.left_img)
    void goback(View view) {
        ActivityUtil.goBack(mActivity);
    }

    @OnClick(R.id.phone_ll)
    void goLink(View view) {
        ActivityUtil.next(mActivity, SafetyPhoneActivity.class);
    }

    @OnClick(R.id.password_ll)
    void goPassword(View view) {
        ActivityUtil.next(mActivity, SafetyLoginPassActivity.class);
    }

    @OnClick(R.id.log_out_tv)
    void logOut(View view) {
        DialogUtil.showAlertTextView(mActivity,
                mActivity.getResources().getString(R.string.Set_tip_title),
                mActivity.getResources().getString(R.string.Set_Logout_delete_login_data_still_log),
                "", "", false, new DialogUtil.OnItemClickListener() {
                    @Override
                    public void confirm(String value) {
                        ProgressUtil.getInstance().showProgress(mActivity,R.string.Set_Logging_out);
                        HomeAction.getInstance().sendEvent(HomeAction.HomeType.DELAY_EXIT);

                        FailMsgsManager.getInstance().removeAllFailMsg();
                        UserOrderBean userOrderBean = new UserOrderBean();
                        userOrderBean.connectLogout();
                    }

                    @Override
                    public void cancel() {}
                });
    }

}
