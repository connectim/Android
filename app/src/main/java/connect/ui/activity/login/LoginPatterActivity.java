package connect.ui.activity.login;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.CycleInterpolator;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import connect.db.MemoryDataManager;
import connect.db.SharedPreferenceUtil;
import connect.ui.activity.R;
import connect.ui.activity.home.HomeActivity;
import connect.ui.activity.login.bean.UserBean;
import connect.ui.activity.set.BackUpActivity;
import connect.utils.LoginPassCheckUtil;
import connect.ui.base.BaseActivity;
import connect.utils.ActivityUtil;
import connect.utils.DialogUtil;
import connect.utils.ProgressUtil;
import connect.utils.ToastEUtil;
import connect.utils.cryption.DecryptionUtil;
import connect.utils.cryption.SupportKeyUril;
import connect.view.TopToolBar;
import connect.view.lockview.GestureLockViewGroup;

/**
 * Login validation gestures or password
 * Created by Administrator on 2016/12/8.
 */
public class LoginPatterActivity extends BaseActivity {

    @Bind(R.id.toolbar_top)
    TopToolBar toolbarTop;
    @Bind(R.id.id_gestureLockViewGroup)
    GestureLockViewGroup idGestureLockViewGroup;
    @Bind(R.id.password_tv)
    TextView passwordTv;
    @Bind(R.id.hint_tv)
    TextView hintTv;

    private LoginPatterActivity mActivity;

    public static void startActivity(Activity activity) {
        ActivityUtil.next(activity, LoginPatterActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_patter);
        ButterKnife.bind(this);
        initView();
    }

    @Override
    public void initView() {
        mActivity = this;
        toolbarTop.setTitleImg(R.mipmap.logo_black_middle);

        UserBean userBean = SharedPreferenceUtil.getInstance().getUser();
        idGestureLockViewGroup.setAnswer(userBean.getPriKey(),userBean.getSalt());
        idGestureLockViewGroup.setOnGestureLockViewListener(new GestureLockViewGroup.OnGestureLockViewListener() {
            @Override
            public void onBlockSelected(int cId) {

            }

            @Override
            public void onGestureEvent(boolean matched) {
                if (matched) {
                    goinHome(idGestureLockViewGroup.getPriKey());
                } else {
                    hintTv.setText(getString(R.string.Set_Password_incorrect_you_have_chance,idGestureLockViewGroup.getUnMatchExceedBoundary()));

                    Animation animationInto = AnimationUtils.loadAnimation(mActivity,R.anim.text_shake);
                    animationInto.setInterpolator(new CycleInterpolator(5));
                    hintTv.setAnimation(animationInto);
                }
            }

            @Override
            public void onUnmatchedExceedBoundary() {
                SharedPreferenceUtil.getInstance().remove(SharedPreferenceUtil.USER_INFO);
                ActivityUtil.next(mActivity, LoginForPhoneActivity.class);
                finish();
            }
        });
    }

    @OnClick(R.id.password_tv)
    void goBack(View view) {
        LoginPassCheckUtil.getInstance().checkLoginPass(mActivity, new LoginPassCheckUtil.OnResultListence() {
            @Override
            public void success(String priKey) {
                goinHome(priKey);
            }

            @Override
            public void error() {

            }
        });
    }

    private void goinHome(String priKey){
        MemoryDataManager.getInstance().putPriKey(priKey);
        HomeActivity.startActivity(mActivity);
        mActivity.finish();
    }

}
