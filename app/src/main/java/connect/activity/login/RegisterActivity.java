package connect.activity.login;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import connect.ui.activity.R;
import connect.activity.home.HomeActivity;
import connect.activity.login.bean.UserBean;
import connect.activity.login.contract.RegisterContract;
import connect.activity.login.presenter.RegisterPresenter;
import connect.activity.base.BaseActivity;
import connect.utils.ActivityUtil;
import connect.utils.DialogUtil;
import connect.utils.ProgressUtil;
import connect.utils.RegularUtil;
import connect.utils.ToastEUtil;
import connect.utils.filter.NameLengthFilter;
import connect.utils.glide.GlideUtil;
import connect.widget.TopToolBar;
import connect.widget.takepicture.TakePictureActivity;

/**
 * The new user registration.
 */
public class RegisterActivity extends BaseActivity implements RegisterContract.View{

    @Bind(R.id.toolbar_top)
    TopToolBar toolbarTop;
    @Bind(R.id.userhead_img)
    ImageView userheadImg;
    @Bind(R.id.password_et)
    EditText userpasswordEt;
    @Bind(R.id.passwordhint_tv)
    TextView passwordhintTv;
    @Bind(R.id.passwordedit_tv)
    TextView passwordeditTv;
    @Bind(R.id.next_btn)
    Button nextBtn;
    @Bind(R.id.nickname_et)
    EditText nicnameEt;

    private RegisterActivity mActivity;
    private RegisterContract.Presenter presenter;
    public String talkKey;

    /**
     * Sweep the private key is registered.
     *
     * @param activity
     * @param userBean priKey pubKey address
     */
    public static void startActivity(Activity activity, UserBean userBean) {
        startActivity(activity, userBean,"");
    }

    /**
     * Mobile phone number registered accounts.
     *
     * @param activity
     * @param userBean priKey pubKey address
     * @param token Check the phone number token
     */
    public static void startActivity(Activity activity, UserBean userBean,String token) {
        Bundle bundle = new Bundle();
        bundle.putSerializable("user",userBean);
        bundle.putString("token",token);
        ActivityUtil.next(activity, RegisterActivity.class,bundle);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_register);
        ButterKnife.bind(this);
        initView();
    }

    @Override
    public void initView() {
        mActivity = this;
        toolbarTop.setLeftImg(R.mipmap.back_black);
        toolbarTop.setTitleImg(R.mipmap.logo_black_middle);

        passwordhintTv.setText(getString(R.string.Login_Password_Hint,""));
        nicnameEt.addTextChangedListener(textWatcher);
        userpasswordEt.addTextChangedListener(textWatcher);
        InputFilter[] filters = { new NameLengthFilter(20) };
        nicnameEt.setFilters(filters);

        new RegisterPresenter(this).start();
    }

    @OnClick(R.id.left_img)
    void goBack(View view) {
        ActivityUtil.goBack(mActivity);
    }

    @OnClick(R.id.passwordedit_tv)
    void editPasswordHint(View view) {
        DialogUtil.showEditView(mActivity, mActivity.getResources().getString(R.string.Login_Login_Password_Hint_Title), "", "", "", "", "",
                false, 15,new DialogUtil.OnItemClickListener() {
            @Override
            public void confirm(String value) {
                presenter.setPasswordHintData(value);
                passwordhintTv.setText(getString(R.string.Login_Password_Hint, value));
            }

            @Override
            public void cancel() {}
        });
    }

    @OnClick(R.id.userhead_img)
    void seleAvatar(View view) {
        TakePictureActivity.startActivity(mActivity);
    }

    @OnClick(R.id.next_btn)
    void registerAccount(View view) {
        if (RegularUtil.matches(userpasswordEt.getText().toString(), RegularUtil.PASSWORD)) {
            ProgressUtil.getInstance().showProgress(mActivity);
            presenter.registerUser(nicnameEt.getText().toString(),
                    userpasswordEt.getText().toString(),
                    getIntent().getExtras().getString("token",""),
                    (UserBean)getIntent().getExtras().getSerializable("user"));
        } else {
            DialogUtil.showAlertTextView(mActivity, getString(R.string.Set_tip_title),
                    getString(R.string.Login_letter_number_and_character_must_be_included_in_your_login_password),
                    "", "", true, new DialogUtil.OnItemClickListener() {
                        @Override
                        public void confirm(String value) {}

                        @Override
                        public void cancel() {}
                    });
        }
    }

    private TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}

        @Override
        public void afterTextChanged(Editable s) {
            if (!TextUtils.isEmpty(userpasswordEt.getText().toString()) && !TextUtils.isEmpty(nicnameEt.getText().toString().trim())) {
                nextBtn.setEnabled(true);
            }else{
                nextBtn.setEnabled(false);
            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == TakePictureActivity.REQUEST_CODE) {
            String pathLocal = data.getExtras().getString("path");
            if (!TextUtils.isEmpty(pathLocal)) {
                presenter.requestUserHead(pathLocal);
            } else {
                ToastEUtil.makeText(mActivity,R.string.Login_Avatar_upload_failed,ToastEUtil.TOAST_STATUS_FAILE).show();
            }
        }
    }

    @Override
    public void setPresenter(RegisterContract.Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public Activity getActivity() {
        return mActivity;
    }

    @Override
    public void setPasswordhint(String text) {
        passwordhintTv.setText(text);
    }

    @Override
    public void showAvatar(String path) {
        GlideUtil.loadAvatarRound(userheadImg,path);
    }

    @Override
    public void complete(boolean isBack) {
        if (isBack) {
            HomeActivity.startActivity(mActivity);
        } else {
            ActivityUtil.next(mActivity,BackupPrivateKeyActivity.class);
        }
        mActivity.finish();
    }

}
