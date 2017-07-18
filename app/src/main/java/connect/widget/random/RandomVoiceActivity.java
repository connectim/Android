package connect.widget.random;

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
import connect.ui.activity.R;
import connect.utils.ActivityUtil;
import connect.utils.DialogUtil;
import connect.utils.StringUtil;
import connect.utils.ToastEUtil;
import connect.utils.permission.PermissionUtil;
import connect.wallet.jni.AllNativeMethod;
import connect.widget.TopToolBar;
import connect.widget.camera.CricleProgressbar;

/**
 * Created by Administrator on 2017/7/7 0007.
 */

public class RandomVoiceActivity extends BaseActivity implements RandomVoiceContract.View{

    @Bind(R.id.toolbar_top)
    TopToolBar toolbarTop;
    @Bind(R.id.start_img)
    ImageView startImg;
    @Bind(R.id.myProgressBar)
    CricleProgressbar myProgressBar;
    @Bind(R.id.status_tv)
    TextView statusTv;
    @Bind(R.id.jump_tv)
    TextView jumpTv;

    private RandomVoiceActivity mActivity;
    private RandomVoiceContract.Presenter presenter;
    private int errorMax = 3;
    public static final int REQUEST_CODE = 160;

    public static void startActivity(Activity activity,Bundle bundle) {
        ActivityUtil.next(activity, RandomVoiceActivity.class, bundle,REQUEST_CODE);
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

        new RandomVoicePresenter(this).start();
    }

    @OnClick(R.id.left_img)
    void goback(View view) {
        ActivityUtil.goBack(mActivity);
    }

    @OnClick(R.id.jump_tv)
    void goJump(View view) {
        HashMap<String, String> hashMap = new HashMap<>();
        String strForBmp = StringUtil.bytesToHexString(SecureRandom.getSeed(64));
        String hashForBmp = AllNativeMethod.cdGetHash256(strForBmp);
        presenter.finishSuccess(hashForBmp);
    }

    @OnClick(R.id.start_img)
    void startRe(View view) {
        startImg.setEnabled(false);
        presenter.start();
    }

    @Override
    public void setPresenter(RandomVoiceContract.Presenter presenter) {
        this.presenter = presenter;
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
                statusTv.setText(R.string.Login_Collecting_Sounds_as_Random_Seed);
                startImg.setImageResource(R.mipmap.generated_success2x);
                break;
            case 2:
                presenter.releaseResource();
                startImg.setEnabled(true);
                statusTv.setText("");
                myProgressBar.setEndAngle(0);
                ToastEUtil.makeText(mActivity, R.string.Login_Generated_Failure, ToastEUtil.TOAST_STATUS_FAILE).show();
                errorMax --;
                if(errorMax <= 0){
                    jumpTv.setVisibility(View.VISIBLE);
                }
                break;
        }
    }

    @Override
    public void denyPressionDialog() {
        jumpTv.setVisibility(View.VISIBLE);
        DialogUtil.showAlertTextView(mActivity, getString(R.string.Set_tip_title),
                getString(R.string.Link_Unable_to_get_the_voice_data),
                "", "", true, new DialogUtil.OnItemClickListener() {
                    @Override
                    public void confirm(String value) {

                    }

                    @Override
                    public void cancel() {

                    }
                }, false);
    }

    @Override
    public void successCollect(String random) {
        Bundle bundle = mActivity.getIntent().getExtras();
        bundle.putString("random", random);
        ActivityUtil.goBackWithResult(mActivity,RESULT_OK,bundle);
    }

    @Override
    public void setProgressBar(float value) {
        myProgressBar.setEndAngle(value);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults){
        super.onRequestPermissionsResult(requestCode,permissions,grantResults);
        PermissionUtil.getInstance().onRequestPermissionsResult(mActivity,requestCode,permissions,grantResults,presenter.getPermissomCallBack());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        presenter.releaseResource();
    }

}