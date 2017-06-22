package connect.ui.activity.login;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.security.SecureRandom;
import java.util.HashMap;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import connect.ui.activity.R;
import connect.ui.activity.login.bean.UserBean;
import connect.ui.activity.login.contract.RendomSendContract;
import connect.ui.activity.login.presenter.RendomSendPresenter;
import connect.ui.base.BaseActivity;
import connect.utils.ActivityUtil;
import connect.utils.DialogUtil;
import connect.utils.permission.PermissionUtil;
import connect.utils.StringUtil;
import connect.utils.ToastEUtil;
import connect.utils.cryption.SupportKeyUril;
import connect.view.TopToolBar;
import connect.view.camera.CricleProgressbar;
import connect.wallet.jni.AllNativeMethod;

/**
 * Voice to generate random number
 * Created by Administrator on 2017/3/14.
 */

public class RandomSendActivity extends BaseActivity implements RendomSendContract.View {

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

    private RandomSendActivity mActivity;
    private RendomSendContract.Presenter presenter;
    private int errorMax = 3;

    public static void startActivity(Activity activity) {
        startActivity(activity, "", "");
    }

    public static void startActivity(Activity activity, String phone, String token) {
        Bundle bundle = new Bundle();
        bundle.putString("phone", phone);
        bundle.putString("token", token);
        ActivityUtil.next(activity, RandomSendActivity.class, bundle);
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

        setPresenter(new RendomSendPresenter(this));
        presenter.start();
    }

    @Override
    public Activity getActivity() {
        return mActivity;
    }

    @Override
    public void setPresenter(RendomSendContract.Presenter presenter) {
        this.presenter = presenter;
    }

    @OnClick(R.id.left_img)
    void goback(View view) {
        ActivityUtil.goBack(mActivity);
    }

    @OnClick(R.id.jump_tv)
    void goJump(View view) {
        HashMap<String, String> hashMap = new HashMap<>();
        String strForBmp = StringUtil.bytesToHexString(SecureRandom.getSeed(64));
        String random = SupportKeyUril.createrPriKeyRandom(strForBmp);
        String prikey = AllNativeMethod.cdGetPrivKeyFromSeedBIP44(random, 44, 0, 0, 0, 0);
        String pubKey = AllNativeMethod.cdGetPubKeyFromPrivKey(prikey);
        String address = AllNativeMethod.cdGetBTCAddrFromPubKey(pubKey);

        hashMap.put("priKey", prikey);
        hashMap.put("pubKey", pubKey);
        hashMap.put("address", address);
        finishSuccess(hashMap);
    }

    @OnClick(R.id.start_img)
    void startRe(View view) {
        startImg.setEnabled(false);
        presenter.start();
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
                statusTv.setText(R.string.Login_Generating_Bitcoin_address);
                break;
            case 3:
                statusTv.setText(R.string.Login_Generated_Successful);
                break;
            case 4:
                presenter.releaseResource();
                startImg.setEnabled(true);
                statusTv.setText("");
                myProgressBar.setEndAngle(0);
                /*if (hashMap != null)
                    hashMap.clear();*/
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
    public void finishSuccess(final HashMap<String, String> hashMap) {
        if (hashMap != null && hashMap.size() == 3) {
            changeViewStatus(3);
            startImg.setImageResource(R.mipmap.generated_success2x);
            Handler handler = new Handler(Looper.getMainLooper()) {
                @Override
                public void handleMessage(Message msg) {
                    Bundle bundle = getIntent().getExtras();
                    UserBean userBean = new UserBean();
                    userBean.setPhone(bundle.getString("phone", ""));
                    userBean.setPriKey(hashMap.get("priKey"));
                    userBean.setPubKey(hashMap.get("pubKey"));
                    userBean.setAddress(hashMap.get("address"));
                    RegisterActivity.startActivity(mActivity, userBean, bundle.getString("token", ""));
                    finish();
                }
            };
            handler.sendEmptyMessageDelayed(1, 1000);
        } else {
            changeViewStatus(4);
        }
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
