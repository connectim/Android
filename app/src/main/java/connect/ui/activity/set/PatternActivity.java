package connect.ui.activity.set;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import connect.db.SharedPreferenceUtil;
import connect.ui.activity.R;
import connect.ui.activity.home.HomeActivity;
import connect.ui.activity.login.bean.UserBean;
import connect.ui.base.BaseActivity;
import connect.ui.base.BaseApplication;
import connect.utils.ActivityUtil;
import connect.view.TopToolBar;

/**
 * Closed, open, change the gesture
 */
public class PatternActivity extends BaseActivity {

    @Bind(R.id.toolbar_top)
    TopToolBar toolbarTop;
    @Bind(R.id.patter_password_iv)
    ImageView patterPasswordIv;
    @Bind(R.id.changepatter_ll)
    LinearLayout changepatterLl;
    @Bind(R.id.next_btn)
    Button nextBtn;

    private PatternActivity mActivity;
    private UserBean userBean;
    public static final String LOGIN_STYPE = "login";
    public static final String SET_STYPE = "set";
    private String type;

    public static void startActivity(Activity activity, String type) {
        Bundle bundle = new Bundle();
        bundle.putString("type", type);
        ActivityUtil.next(activity, PatternActivity.class, bundle);
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
        Bundle bundle = getIntent().getExtras();
        toolbarTop.setLeftImg(R.mipmap.back_white);
        type = bundle.getString("type");

        userBean = SharedPreferenceUtil.getInstance().getUser();
        if (type.equals(LOGIN_STYPE)) {
            nextBtn.setVisibility(View.VISIBLE);
            if(!TextUtils.isEmpty(userBean.getSalt())){
                List<Activity> list = BaseApplication.getInstance().getActivityList();
                for (Activity activity : list) {
                    if (!activity.getClass().getName().equals(PatternActivity.class.getName())){
                        activity.finish();
                    }
                }
                HomeActivity.startActivity(mActivity);
                mActivity.finish();
            }
        }

        if (TextUtils.isEmpty(userBean.getSalt())) {
            patterPasswordIv.setImageResource(R.mipmap.switch_off);
            changepatterLl.setVisibility(View.GONE);
        } else {
            patterPasswordIv.setImageResource(R.mipmap.switch_on);
            changepatterLl.setVisibility(View.VISIBLE);
        }
    }

    @OnClick(R.id.next_btn)
    void goNext(View view) {
        UserBean userBean = SharedPreferenceUtil.getInstance().getUser();
        userBean.setBack(true);
        SharedPreferenceUtil.getInstance().putUser(userBean);

        List<Activity> list = BaseApplication.getInstance().getActivityList();
        for (Activity activity : list) {
            if (!activity.getClass().getName().equals(PatternActivity.class.getName())){
                activity.finish();
            }
        }
        HomeActivity.startActivity(mActivity);
        mActivity.finish();
    }

    @OnClick(R.id.left_img)
    void goback(View view) {
        ActivityUtil.goBack(mActivity);
    }

    @OnClick(R.id.changepatter_ll)
    void goChangePatter(View view) {
        PatterDrawActivity.startActivity(mActivity, PatterDrawActivity.TYPE_CHANGE, userBean.getSalt());
    }

    @OnClick(R.id.patter_password_iv)
    void goDrawPatter(View view) {
        if (TextUtils.isEmpty(userBean.getSalt())) {
            PatterDrawActivity.startActivity(mActivity, PatterDrawActivity.TYPE_NEW, "");
        } else {
            PatterDrawActivity.startActivity(mActivity, PatterDrawActivity.TYPE_CLOSE, userBean.getSalt());
        }
    }

}
