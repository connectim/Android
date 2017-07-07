package connect.ui.activity.set;

import android.app.Dialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import connect.db.SharedPreferenceUtil;
import connect.ui.activity.R;
import connect.ui.activity.login.bean.UserBean;
import connect.utils.LoginPassCheckUtil;
import connect.ui.base.BaseActivity;
import connect.utils.ActivityUtil;
import connect.view.TopToolBar;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Account and security
 * Created by Administrator on 2016/12/1.
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
    private Dialog dialogPass;

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
        if(userBean != null && TextUtils.isEmpty(userBean.getPhone())){
            phoneTv.setText(R.string.Set_Phone_unbinded);
        }else{
            try {
                String phone = userBean.getPhone().split("-")[1];
                phoneTv.setText(phone);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        if(userBean != null && TextUtils.isEmpty(userBean.getSalt())){
            patternTv.setText(R.string.Set_Off);
        }else{
            patternTv.setText(R.string.Set_On);
        }
    }

    @OnClick(R.id.left_img)
    void goback(View view){
        ActivityUtil.goBack(mActivity);
    }

    @OnClick(R.id.phone_ll)
    void goLink(View view){
        ActivityUtil.next(mActivity,LinkMobileActivity.class);
    }

    @OnClick(R.id.password_ll)
    void goPassword(View view){
        LoginPassCheckUtil.getInstance().checkLoginPass(mActivity, new LoginPassCheckUtil.OnResultListence() {
            @Override
            public void success(String priKey) {
                ActivityUtil.next(mActivity,ModifyPassActivity.class);
            }

            @Override
            public void error() {

            }
        });
    }

    @OnClick(R.id.payment_ll)
    void goPayMent(View view){
        ActivityUtil.next(mActivity,PaymentActivity.class);
    }

    @OnClick(R.id.pattern_ll)
    void goPattern(View view){
        PatternActivity.startActivity(mActivity,PatternActivity.SET_STYPE);
    }

    @OnClick(R.id.pritkey_backup_ll)
    void goBackUp(View view){
        LoginPassCheckUtil.getInstance().checkLoginPass(mActivity, new LoginPassCheckUtil.OnResultListence() {
            @Override
            public void success(String priKey) {
                ActivityUtil.next(mActivity,BackUpActivity.class);
            }

            @Override
            public void error() {

            }
        });
    }

}
