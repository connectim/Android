package connect.activity.wallet;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.CycleInterpolator;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import connect.activity.login.LoginPhoneActivity;
import connect.database.SharedPreferenceUtil;
import connect.ui.activity.R;
import connect.activity.home.HomeActivity;
import connect.activity.login.bean.UserBean;
import connect.utils.LoginPassCheckUtil;
import connect.activity.base.BaseActivity;
import connect.utils.ActivityUtil;
import connect.widget.TopToolBar;
import connect.widget.lockview.GestureLockViewGroup;

/**
 * Verify the gesture password.
 */

public class SafetyPatterVerifyActivity extends BaseActivity {

    @Bind(R.id.toolbar_top)
    TopToolBar toolbarTop;
    @Bind(R.id.id_gestureLockViewGroup)
    GestureLockViewGroup idGestureLockViewGroup;
    @Bind(R.id.password_tv)
    TextView passwordTv;
    @Bind(R.id.hint_tv)
    TextView hintTv;

    private SafetyPatterVerifyActivity mActivity;

    public static void startActivity(Activity activity) {
        ActivityUtil.next(activity, SafetyPatterVerifyActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patter_verify);
        ButterKnife.bind(this);
        initView();
    }

    @Override
    public void initView() {
        mActivity = this;
        toolbarTop.setTitle(R.mipmap.logo_black_middle, null);

        UserBean userBean = SharedPreferenceUtil.getInstance().getUser();
        //idGestureLockViewGroup.setAnswer(userBean.getPatterStr());
        idGestureLockViewGroup.setOnGestureLockViewListener(onGestureLockViewListener);
    }

    @OnClick(R.id.password_tv)
    void checkPass(View view) {
        LoginPassCheckUtil.getInstance().checkLoginPass(mActivity, new LoginPassCheckUtil.OnResultListener() {
            @Override
            public void success(String priKey) {
                // 验证正确 需要跳转
                // launchHome();
            }

            @Override
            public void error() {}
        });
    }

    GestureLockViewGroup.OnGestureLockViewListener onGestureLockViewListener = new GestureLockViewGroup.OnGestureLockViewListener(){
        @Override
        public void onBlockSelected(int cId) {}
        @Override
        public void onGestureEvent(boolean matched) {
            if (matched) {
                //launchHome(idGestureLockViewGroup.getPriKey());
                // 验证正确 需要跳转

            } else {
                // 密码错误
                hintTv.setText(getString(R.string.Set_Password_incorrect_you_have_chance,idGestureLockViewGroup.getUnMatchExceedBoundary()));
                Animation animationInto = AnimationUtils.loadAnimation(mActivity,R.anim.text_shake);
                animationInto.setInterpolator(new CycleInterpolator(5));
                hintTv.setAnimation(animationInto);
            }
        }
        @Override
        public void onUnmatchedExceedBoundary() {
            // 4次密码输入都不正确
            SharedPreferenceUtil.getInstance().remove(SharedPreferenceUtil.USER_INFO);
            ActivityUtil.next(mActivity, LoginPhoneActivity.class);
            finish();
        }
    };

    private void launchHome(){
        HomeActivity.startActivity(mActivity);
        mActivity.finish();
    }

}
