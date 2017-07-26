package connect.activity.set;

import android.app.Dialog;
import android.app.WallpaperInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import connect.activity.wallet.bean.WalletBean;
import connect.database.MemoryDataManager;
import connect.database.SharePreferenceUser;
import connect.database.SharedPreferenceUtil;
import connect.database.green.DaoHelper.CurrencyHelper;
import connect.database.green.bean.CurrencyEntity;
import connect.ui.activity.R;
import connect.activity.login.bean.UserBean;
import connect.utils.LoginPassCheckUtil;
import connect.activity.base.BaseActivity;
import connect.utils.ActivityUtil;
import connect.utils.cryption.SupportKeyUril;
import connect.wallet.cwallet.NativeWallet;
import connect.wallet.cwallet.bean.CurrencyEnum;
import connect.wallet.jni.AllNativeMethod;
import connect.widget.TopToolBar;

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
