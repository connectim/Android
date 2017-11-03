package connect.activity.wallet;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import connect.activity.base.BaseActivity;
import connect.activity.login.bean.UserBean;
import connect.database.SharedPreferenceUtil;
import connect.ui.activity.R;
import connect.utils.ActivityUtil;
import connect.widget.TopToolBar;

/**
 * wallet account safety setting.
 */
public class SafetySetActivity extends BaseActivity {

    @Bind(R.id.toolbar_top)
    TopToolBar toolbarTop;
    @Bind(R.id.payment_ll)
    LinearLayout paymentLl;
    @Bind(R.id.pattern_tv)
    TextView patternTv;
    @Bind(R.id.pattern_ll)
    LinearLayout patternLl;

    private SafetySetActivity mActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet_safety);
        ButterKnife.bind(this);

    }

    @Override
    public void initView() {
        mActivity = this;
        toolbarTop.setBlackStyle();
        toolbarTop.setLeftImg(R.mipmap.back_white);
        toolbarTop.setTitle(null, R.string.Set_Account_security);

        UserBean userBean = SharedPreferenceUtil.getInstance().getUser();
        /*if (userBean != null && TextUtils.isEmpty(userBean.getPatterStr())) {
            patternTv.setText(R.string.Set_Off);
        } else {
            patternTv.setText(R.string.Set_On);
        }*/
    }

    @OnClick(R.id.left_img)
    void goback(View view) {
        ActivityUtil.goBack(mActivity);
    }

    @OnClick(R.id.payment_ll)
    void goPayMent(View view) {
        ActivityUtil.next(mActivity, SafetyPayActivity.class);
    }

    @OnClick(R.id.pattern_ll)
    void goPattern(View view) {
        SafetyPatternActivity.startActivity(mActivity);
    }

}
