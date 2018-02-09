package connect.activity.login;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.security.SecureRandom;
import java.util.HashMap;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import connect.activity.base.BaseActivity;
import connect.activity.login.bean.UserBean;
import connect.activity.login.contract.RegisterGetRandomContract;
import connect.activity.login.presenter.RegisterGetRandomPresenter;
import connect.ui.activity.R;
import connect.utils.ActivityUtil;
import connect.utils.dialog.DialogUtil;
import connect.utils.StringUtil;
import connect.utils.ToastEUtil;
import connect.utils.permission.PermissionUtil;
import connect.widget.TopToolBar;
import connect.widget.camera.CircleProgressbar;

/**
 * Voice to generate random number.
 */
public class RegisterGetRandomActivity extends BaseActivity implements RegisterGetRandomContract.View {

    @Bind(R.id.toolbar_top)
    TopToolBar toolbarTop;
    @Bind(R.id.start_img)
    ImageView startImg;
    @Bind(R.id.myProgressBar)
    CircleProgressbar myProgressBar;
    @Bind(R.id.status_tv)
    TextView statusTv;
    @Bind(R.id.jump_tv)
    TextView jumpTv;

    private RegisterGetRandomActivity mActivity;
    private RegisterGetRandomContract.Presenter presenter;
    private int errorMax = 3;

    /**
     * No binding number registered accounts.
     *
     * @param activity
     */
    public static void startActivity(Activity activity) {
        startActivity(activity, "", "");
    }

    /**
     * The binding number registered accounts.
     *
     * @param activity
     * @param phone phone number
     * @param token The token to verify the phone number
     */
    public static void startActivity(Activity activity, String phone, String token) {
        Bundle bundle = new Bundle();
        bundle.putString("phone", phone);
        bundle.putString("token", token);
        ActivityUtil.next(activity, RegisterGetRandomActivity.class, bundle);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_randomsend);
        ButterKnife.bind(this);
        initView();
    }

    @Override
    public void initView() {
        mActivity = this;
        toolbarTop.setBlackStyle();
        toolbarTop.setLeftImg(R.mipmap.back_white);
        toolbarTop.setTitle(null, R.string.Login_creat_account);

        myProgressBar.setLineWidth(4);
        startImg.setEnabled(false);

        new RegisterGetRandomPresenter(this).start();
    }

    @OnClick(R.id.left_img)
    void goBack(View view) {
        ActivityUtil.goBack(mActivity);
    }

    @OnClick(R.id.jump_tv)
    void goJump(View view) {
        HashMap<String, String> hashMap = new HashMap<>();
        String strForBmp = StringUtil.bytesToHexString(SecureRandom.getSeed(64));
//        String random = SupportKeyUril.xor(strForBmp, StringUtil.bytesToHexString(SecureRandom.getSeed(64)));
//        String prikey = AllNativeMethod.cdGetPrivKeyFromSeedBIP44(random, 44, 0, 0, 0, 0);
//        String pubKey = AllNativeMethod.cdGetPubKeyFromPrivKey(prikey);

        hashMap.put("priKey", "");
        hashMap.put("pubKey", "");
        presenter.finishSuccess(hashMap);
    }

    @OnClick(R.id.start_img)
    void startRe(View view) {
        startImg.setEnabled(false);
        presenter.start();
    }

    @Override
    public void setPresenter(RegisterGetRandomContract.Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode,permissions,grantResults);
        PermissionUtil.getInstance().onRequestPermissionsResult(mActivity,requestCode,permissions,grantResults,presenter.getPermissomCallBack());
    }

    @Override
    public Activity getActivity() {
        return mActivity;
    }

    @Override
    public void denyPression() {
        jumpTv.setVisibility(View.VISIBLE);
    }

    @Override
    public void changeViewStatus(int status) {
        switch (status) {
            case 0:
                statusTv.setText(R.string.Login_Collecting_Sounds_as_Random_Seed);
                startImg.setEnabled(false);
                break;
            case 1:
                statusTv.setText(R.string.Login_Generating_Private_and_Public_Key);
                break;
            case 2:
                statusTv.setText(R.string.Login_Generating_Private_and_Public_Key);
                break;
            case 3:
                statusTv.setText(R.string.Login_Generated_Successful);
                startImg.setImageResource(R.mipmap.generated_success2x);
                break;
            case 4:
                presenter.releaseResource();
                startImg.setEnabled(true);
                statusTv.setText("");
                myProgressBar.setEndAngle(0);
                ToastEUtil.makeText(mActivity, R.string.Login_Generated_Failure, ToastEUtil.TOAST_STATUS_FAILE).show();
                errorMax --;
                if (errorMax <= 0) {
                    jumpTv.setVisibility(View.VISIBLE);
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void denyPressionDialog() {
        jumpTv.setVisibility(View.VISIBLE);
        DialogUtil.showAlertTextView(mActivity, getString(R.string.Set_tip_title), getString(R.string.Link_Unable_to_get_the_voice_data),
                "", "", true, false, new DialogUtil.OnItemClickListener() {
                    @Override
                    public void confirm(String value) {}
                    @Override
                    public void cancel() {}
                });
    }

    @Override
    public void goinRegister(UserBean userBean) {
        Bundle bundle = getIntent().getExtras();
        String phone = bundle.getString("phone", "");
        String token = bundle.getString("token", "");

        RegisterActivity.startActivity(mActivity, userBean, token);
        finish();
    }

    @Override
    public void setProgressBar(float value) {
        myProgressBar.setEndAngle(value);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        presenter.releaseResource();
    }
}
