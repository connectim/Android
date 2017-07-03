package connect.activity.login;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import connect.ui.activity.R;
import connect.activity.login.bean.CountryBean;
import connect.activity.login.contract.LoginPhoneContract;
import connect.activity.login.presenter.LoginPhonePresenter;
import connect.activity.base.BaseActivity;
import connect.utils.ActivityUtil;
import connect.utils.permission.PermissionUtil;
import connect.utils.data.PhoneDataUtil;
import connect.utils.StringUtil;


/**
 * Login interface verify phone number
 */
public class LoginForPhoneActivity extends BaseActivity implements LoginPhoneContract.View{

    @Bind(R.id.phone_et)
    EditText phoneEt;
    @Bind(R.id.next_btn)
    Button nextBtn;
    @Bind(R.id.country_tv)
    TextView countryTv;
    @Bind(R.id.country_rela)
    RelativeLayout countryRela;
    @Bind(R.id.backup_local_tv)
    TextView backupLocalTv;

    private LoginForPhoneActivity mActivity;
    private LoginPhoneContract.Presenter presenter;
    private final int COUNTRY_CODE = 100;
    private CountryBean countryBean;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_forphone);
        ButterKnife.bind(this);
        initView();
    }

    @Override
    public void initView() {
        mActivity = this;
        countryBean = PhoneDataUtil.getInstance().getCurrentCountryCode();
        if(countryBean != null){
            countryTv.setText("+ " + countryBean.getCode());
        }
        new LoginPhonePresenter(this,countryBean).start();
        phoneEt.addTextChangedListener(presenter.getPhoneTextWatcher());
    }

    @OnClick(R.id.country_rela)
    void countryCodeClick(View view) {
        ActivityUtil.next(mActivity, CountryCodeActivity.class, COUNTRY_CODE);
    }

    @OnClick(R.id.backup_local_tv)
    void otherLoginClick(View view) {
        presenter.showMore();
    }

    @OnClick(R.id.next_btn)
    public void nextBtn(View view) {
        presenter.request(StringUtil.filterNumber(countryTv.getText().toString()) + "-" + phoneEt.getText().toString());
    }

    private PermissionUtil.ResultCallBack permissomCallBack = new PermissionUtil.ResultCallBack(){
        @Override
        public void granted(String[] permissions) {
            ActivityUtil.next(mActivity, ScanLoginActivity.class);
        }

        @Override
        public void deny(String[] permissions) {

        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionUtil.getInstance().onRequestPermissionsResult(mActivity,requestCode,permissions,grantResults,permissomCallBack);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == COUNTRY_CODE) {
            countryBean = (CountryBean) data.getExtras().getSerializable("country");
            countryTv.setText("+ " + countryBean.getCode());
        }
    }

    @Override
    public void setPresenter(LoginPhoneContract.Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void setBtnEnabled(boolean isEnabled) {
        nextBtn.setEnabled(isEnabled);
    }

    @Override
    public void verifySuccess() {
        SignInVerifyActivity.startActivity(mActivity, countryBean.getCode(),phoneEt.getText().toString());
    }

    @Override
    public void scanPermission() {
        PermissionUtil.getInstance().requestPermissom(mActivity,new String[]{PermissionUtil.PERMISSIM_CAMERA}, permissomCallBack);
    }

    @Override
    public void goinRandomSend() {
        RandomSendActivity.startActivity(mActivity);
    }

    @Override
    public void goinLocalLogin() {
        ActivityUtil.next(mActivity, LocalLoginActivity.class);
    }

    @Override
    public Activity getActivity() {
        return this;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
