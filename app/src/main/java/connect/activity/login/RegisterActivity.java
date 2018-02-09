package connect.activity.login;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import connect.activity.base.BaseActivity;
import connect.activity.base.BaseApplication;
import connect.activity.home.HomeActivity;
import connect.activity.login.bean.UserBean;
import connect.activity.login.contract.RegisterContract;
import connect.activity.login.presenter.RegisterPresenter;
import connect.ui.activity.R;
import connect.utils.ActivityUtil;
import connect.utils.ProgressUtil;
import connect.utils.ToastEUtil;
import connect.utils.glide.GlideUtil;
import connect.widget.TopToolBar;
import connect.widget.takepicture.TakePictureActivity;

/**
 * The new user registration.
 */
public class RegisterActivity extends BaseActivity implements RegisterContract.View {
    @Bind(R.id.toolbar_top)
    TopToolBar toolbarTop;
    @Bind(R.id.userhead_img)
    ImageView userheadImg;
    @Bind(R.id.nickname_et)
    EditText nicknameEt;
    @Bind(R.id.next_btn)
    Button nextBtn;

    private RegisterActivity mActivity;
    private RegisterContract.Presenter presenter;

    /**
     * Sweep the private key is registered.
     *
     * @param activity
     * @param userBean priKey pubKey address
     */
    public static void startActivity(Activity activity, UserBean userBean) {
        startActivity(activity, userBean, "");
    }

    /**
     * Mobile phone number registered accounts.
     *
     * @param activity
     * @param userBean priKey pubKey address
     * @param token    Check the phone number token
     */
    public static void startActivity(Activity activity, UserBean userBean, String token) {
        Bundle bundle = new Bundle();
        bundle.putSerializable("user", userBean);
        bundle.putString("token", token);
        ActivityUtil.next(activity, RegisterActivity.class, bundle);
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
        toolbarTop.setTitle(R.mipmap.logo_black_middle, "");
        nicknameEt.addTextChangedListener(textWatcher);

        new RegisterPresenter(this).start();
    }

    @OnClick(R.id.left_img)
    void goBack(View view) {
        ActivityUtil.goBack(mActivity);
    }

    @OnClick(R.id.userhead_img)
    void seleAvatar(View view) {
        TakePictureActivity.startActivity(mActivity);
    }

    @OnClick(R.id.next_btn)
    void registerAccount(View view) {
        ProgressUtil.getInstance().showProgress(mActivity);
        String nicName = nicknameEt.getText().toString();
        String smsToken = getIntent().getExtras().getString("token", "");
        UserBean userBean = (UserBean) getIntent().getExtras().getSerializable("user");
        presenter.registerUser(nicName, smsToken, userBean);
    }

    private TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}
        @Override
        public void afterTextChanged(Editable s) {
            if (TextUtils.isEmpty(nicknameEt.getText().toString().trim())) {
                nextBtn.setEnabled(false);
            } else {
                nextBtn.setEnabled(true);
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
                ToastEUtil.makeText(mActivity, R.string.Login_Avatar_upload_failed, ToastEUtil.TOAST_STATUS_FAILE).show();
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
    public void showAvatar(String path) {
        GlideUtil.loadAvatarRound(userheadImg, path);
    }

    @Override
    public void launchHome() {
        List<Activity> list = BaseApplication.getInstance().getActivityList();
        for (Activity activity : list) {
            if (!activity.getClass().getName().equals(mActivity.getClass().getName())) {
                activity.finish();
            }
        }
        HomeActivity.startActivity(mActivity);
        mActivity.finish();
    }

}
