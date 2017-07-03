package connect.ui.activity.login;

import android.app.Activity;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ImageView;

import butterknife.Bind;
import butterknife.ButterKnife;
import connect.ui.activity.R;
import connect.ui.activity.home.HomeActivity;
import connect.ui.activity.login.contract.StartContract;
import connect.ui.activity.login.presenter.StartPresenter;
import connect.ui.base.BaseActivity;
import connect.utils.ActivityUtil;

public class StartActivity extends BaseActivity implements StartContract.View{

    @Bind(R.id.start_img)
    ImageView startImg;

    private StartActivity mActivity;
    private StartContract.Presenter startPresenter;

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
        setPresenter(new StartPresenter(this));
        startPresenter.start();
    }

    @Override
    public void setPresenter(StartContract.Presenter presenter) {
        startPresenter = presenter;
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
    public void goinGuide() {
        ActivityUtil.next(mActivity, GuideActivity.class);
    }

    @Override
    public void goinLoginForPhone() {
        ActivityUtil.next(mActivity, LoginForPhoneActivity.class);
    }

    @Override
    public void goinLoginPatter() {
        LoginPatterActivity.startActivity(mActivity);
    }

    @Override
    public void goinHome() {
        HomeActivity.startActivity(mActivity);
    }

    @Override
    public Activity getActivity() {
        return mActivity;
    }
}
