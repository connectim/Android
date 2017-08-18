package connect.activity.login;

import android.app.Activity;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ImageView;

import butterknife.Bind;
import butterknife.ButterKnife;
import connect.activity.base.BaseActivity;
import connect.activity.home.HomeActivity;
import connect.activity.login.contract.StartContract;
import connect.activity.login.presenter.StartPresenter;
import connect.ui.activity.R;
import connect.utils.ActivityUtil;

/**
 * The App start page.
 */
public class StartActivity extends BaseActivity implements StartContract.View {

    @Bind(R.id.start_img)
    ImageView startImg;

    private StartActivity mActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        ButterKnife.bind(this);
        initView();
    }

    @Override
    public void initView() {
        mActivity = this;
        new StartPresenter(this).start();
    }

    @Override
    public void setPresenter(StartContract.Presenter presenter) {

    }

    @Override
    public void setImage(String path) {
        if (TextUtils.isEmpty(path)) {
            startImg.setImageResource(R.mipmap.bg_start_man);
        } else {
            startImg.setImageBitmap(BitmapFactory.decodeFile(path));
        }
    }

    @Override
    public void goIntoGuide() {
        ActivityUtil.next(mActivity, GuideActivity.class);
    }

    @Override
    public void goIntoLoginForPhone() {
        ActivityUtil.next(mActivity, LoginForPhoneActivity.class);
    }

    @Override
    public void goIntoLoginPatter() {
        LoginPatterActivity.startActivity(mActivity);
    }

    @Override
    public void goIntoHome() {
        HomeActivity.startActivity(mActivity);
    }

    @Override
    public Activity getActivity() {
        return mActivity;
    }

}
