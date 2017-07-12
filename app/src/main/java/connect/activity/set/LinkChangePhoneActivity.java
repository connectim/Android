package connect.activity.set;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import connect.ui.activity.R;
import connect.activity.login.CountryCodeActivity;
import connect.activity.login.bean.CountryBean;
import connect.activity.base.BaseActivity;
import connect.utils.ActivityUtil;
import connect.utils.ProgressUtil;
import connect.utils.data.PhoneDataUtil;
import connect.utils.StringUtil;
import connect.utils.ToastEUtil;
import connect.utils.UriUtil;
import connect.utils.okhttp.HttpRequest;
import connect.utils.okhttp.ResultCall;
import connect.widget.TopToolBar;
import protos.Connect;

/**
 * Binding mobile phone input number
 * Created by Administrator on 2017/1/6.
 */
public class LinkChangePhoneActivity extends BaseActivity {

    @Bind(R.id.country_tv)
    TextView countryTv;
    @Bind(R.id.country_rela)
    RelativeLayout countryRela;
    @Bind(R.id.phone_et)
    EditText phoneEt;
    @Bind(R.id.next_btn)
    Button nextBtn;
    @Bind(R.id.toolbar_top)
    TopToolBar toolbarTop;

    private LinkChangePhoneActivity mActivity;
    private final int COUNTRY_CODE = 100;
    private CountryBean countryBean;
    public final static String LINK_TYPE = "link";
    public final static String UNLINK_TYPE = "unlink";
    private Bundle bundle;

    public static void startActivity(Activity activity, String type) {
        Bundle bundle = new Bundle();
        bundle.putString("type", type);
        ActivityUtil.nextBottomToTop(activity, LinkChangePhoneActivity.class, bundle,-1);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_link_change);
        ButterKnife.bind(this);
        initView();
    }

    @Override
    public void initView() {
        mActivity = this;
        toolbarTop.setBlackStyle();
        toolbarTop.setTitle(null,R.string.Set_Change_Mobile);
        toolbarTop.setRightImg(R.mipmap.close_white3x);
        bundle = getIntent().getExtras();

        phoneEt.addTextChangedListener(textWatcher);
        countryBean = PhoneDataUtil.getInstance().getCurrentCountryCode();
        if(countryBean != null){
            countryTv.setText("+ " + countryBean.getCode());
        }
    }

    @OnClick(R.id.country_rela)
    void countryCodeClick(View view) {
        ActivityUtil.next(mActivity, CountryCodeActivity.class, COUNTRY_CODE);
    }

    @OnClick(R.id.right_lin)
    void close(View view){
        ActivityUtil.goBackBottom(mActivity);
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
            try {
                PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
                Phonenumber.PhoneNumber swissNumberProto = phoneUtil.parse(s.toString(), countryBean.getCountryCode());
                if(phoneUtil.isValidNumberForRegion(swissNumberProto, countryBean.getCountryCode())){
                    nextBtn.setEnabled(true);
                }else{
                    nextBtn.setEnabled(false);
                }
            } catch (NumberParseException e) {
                e.printStackTrace();
                nextBtn.setEnabled(false);
            }
        }
    };

    @OnClick(R.id.next_btn)
    void nextBtn(View view) {
        ProgressUtil.getInstance().showProgress(mActivity);
        Connect.SendMobileCode sendMobileCode = Connect.SendMobileCode.newBuilder()
                .setMobile(StringUtil.filterNumber(countryTv.getText().toString()) + "-" + phoneEt.getText().toString())
                .setCategory(1).build();
        HttpRequest.getInstance().post(UriUtil.CONNECT_V1_SMS_SEND, sendMobileCode, new ResultCall<Connect.HttpNotSignResponse>() {
            @Override
            public void onResponse(Connect.HttpNotSignResponse response) {
                ProgressUtil.getInstance().dismissProgress();
                LinkChangeVerifyActivity.startActivity(mActivity, countryBean.getCode(),phoneEt.getText().toString(), bundle.getString("type"));
            }

            @Override
            public void onError(Connect.HttpNotSignResponse response) {
                ProgressUtil.getInstance().dismissProgress();
                if(response.getCode() == 2400){
                    ToastEUtil.makeText(mActivity,R.string.Link_Operation_frequent,ToastEUtil.TOAST_STATUS_FAILE).show();
                }
            }
        });
    }

}
