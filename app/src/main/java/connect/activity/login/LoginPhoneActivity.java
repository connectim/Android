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
import connect.activity.base.BaseActivity;
import connect.activity.base.BaseApplication;
import connect.activity.login.bean.CountryBean;
import connect.activity.login.bean.UserBean;
import connect.activity.login.contract.LoginPhoneContract;
import connect.activity.login.presenter.LoginPhonePresenter;
import connect.database.SharedPreferenceUtil;
import connect.ui.activity.R;
import connect.utils.ActivityUtil;
import connect.utils.StringUtil;
import connect.utils.data.PhoneDataUtil;

/**
 * Login interface verify phone number.
 */
public class LoginPhoneActivity extends BaseActivity implements LoginPhoneContract.View{

    @Bind(R.id.phone_et)
    EditText phoneEt;
    @Bind(R.id.next_btn)
    Button nextBtn;
    @Bind(R.id.country_tv)
    TextView countryTv;
    @Bind(R.id.country_rela)
    RelativeLayout countryRela;

    private LoginPhoneActivity mActivity;
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
        phoneEt.addTextChangedListener(textWatcher);
    }

    @OnClick(R.id.country_rela)
    void countryCodeClick(View view) {
        ActivityUtil.next(mActivity, LoginPhoneCountryCodeActivity.class, COUNTRY_CODE);
    }

    @OnClick(R.id.next_btn)
    public void nextBtn(View view) {
        //presenter.request(StringUtil.filterNumber(countryTv.getText().toString()) + "-" + phoneEt.getText().toString());

        //test
        UserBean userBean = new UserBean();
        userBean.setPubKey("3343434");
        userBean.setPriKey("ewewewe");
        userBean.setAvatar("ere");
        userBean.setCaPublicKey("sdsdsd");
        userBean.setUid("12321231231");
        SharedPreferenceUtil.getInstance().putUser(userBean);
        BaseApplication.getInstance().initRegisterAccount();
    }

    /**
     * Listening to the EditView input, verify the phone number.
     */
    private TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}
        @Override
        public void afterTextChanged(Editable s) {
            try {
                PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
                Phonenumber.PhoneNumber swissNumberProto = phoneUtil.parse(s.toString(), countryBean.getCountryCode());
                if(phoneUtil.isValidNumberForRegion(swissNumberProto, countryBean.getCountryCode())){
                    nextBtn.setEnabled(true);
                }else{
                    nextBtn.setEnabled(false);
                }
            } catch (Exception e) {
                e.printStackTrace();
                nextBtn.setEnabled(false);
            }
        }
    };

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
    public void verifySuccess() {
        LoginPhoneVerifyActivity.startActivity(mActivity, countryBean.getCode(),phoneEt.getText().toString());
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
