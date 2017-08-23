package connect.activity.set;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import connect.activity.base.BaseActivity;
import connect.activity.base.BaseApplication;
import connect.activity.home.HomeActivity;
import connect.activity.login.bean.UserBean;
import connect.database.SharedPreferenceUtil;
import connect.ui.activity.R;
import connect.utils.ActivityUtil;
import connect.widget.TopToolBar;

/**
 * Closed, open, change the gesture.
 */
public class SafetyPatternActivity extends BaseActivity {

    @Bind(R.id.toolbar_top)
    TopToolBar toolbarTop;
    @Bind(R.id.patter_password_iv)
    ImageView patterPasswordIv;
    @Bind(R.id.change_patter_ll)
    LinearLayout changePatterLl;
    @Bind(R.id.next_btn)
    Button nextBtn;

    private SafetyPatternActivity mActivity;
    private UserBean userBean;
    public static final String LOGIN_TYPE = "login";
    public static final String SET_TYPE = "set";
    private String type;

    public static void startActivity(Activity activity, String type) {
        Bundle bundle = new Bundle();
        bundle.putString("type", type);
        ActivityUtil.next(activity, SafetyPatternActivity.class, bundle);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_pattern);
        ButterKnife.bind(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        initView();
    }

    @Override
    public void initView() {
        mActivity = this;
        toolbarTop.setBlackStyle();
        toolbarTop.setTitle(null, R.string.Set_Pattern_Password);
        toolbarTop.setLeftImg(R.mipmap.back_white);
        Bundle bundle = getIntent().getExtras();
        type = bundle.getString("type");
        userBean = SharedPreferenceUtil.getInstance().getUser();

        if (type.equals(LOGIN_TYPE)) {
            showLoginPatternView();
        }

        if (TextUtils.isEmpty(userBean.getSalt())) {
            patterPasswordIv.setImageResource(R.mipmap.switch_off);
            changePatterLl.setVisibility(View.GONE);
        } else {
            patterPasswordIv.setImageResource(R.mipmap.switch_on);
            changePatterLl.setVisibility(View.VISIBLE);
        }
    }

    @OnClick(R.id.left_img)
    void goback(View view) {
        List<Activity> list = BaseApplication.getInstance().getActivityList();
        if (list.size() == 1) {
            goNext(view);
        } else {
            ActivityUtil.goBack(mActivity);
        }
    }

    @OnClick(R.id.next_btn)
    void goNext(View view) {
        UserBean userBean = SharedPreferenceUtil.getInstance().getUser();
        userBean.setBack(true);
        SharedPreferenceUtil.getInstance().putUser(userBean);

        List<Activity> list = BaseApplication.getInstance().getActivityList();
        for (Activity activity : list) {
            if (!activity.getClass().getName().equals(SafetyPatternActivity.class.getName())) {
                activity.finish();
            }
        }
        HomeActivity.startActivity(mActivity);
        mActivity.finish();
    }

    @OnClick(R.id.change_patter_ll)
    void goChangePatter(View view) {
        SafetyPatterDrawActivity.startActivity(mActivity, SafetyPatterDrawActivity.TYPE_CHANGE, userBean.getSalt());
    }

    @OnClick(R.id.patter_password_iv)
    void goDrawPatter(View view) {
        if (TextUtils.isEmpty(userBean.getSalt())) {
            SafetyPatterDrawActivity.startActivity(mActivity, SafetyPatterDrawActivity.TYPE_NEW, "");
        } else {
            SafetyPatterDrawActivity.startActivity(mActivity, SafetyPatterDrawActivity.TYPE_CLOSE, userBean.getSalt());
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            List<Activity> list = BaseApplication.getInstance().getActivityList();
            if (list.size() > 1) {
                ActivityUtil.goBack(mActivity);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void showLoginPatternView(){
        nextBtn.setVisibility(View.VISIBLE);
        if (!TextUtils.isEmpty(userBean.getSalt())) {
            List<Activity> list = BaseApplication.getInstance().getActivityList();
            for (Activity activity : list) {
                if (!activity.getClass().getName().equals(SafetyPatternActivity.class.getName())) {
                    activity.finish();
                }
            }
            HomeActivity.startActivity(mActivity);
            mActivity.finish();
        }
    }

}