package connect.activity.login;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ImageView;

import butterknife.Bind;
import butterknife.ButterKnife;
import connect.activity.base.BaseActivity;
import connect.activity.home.HomeActivity;
import connect.activity.login.contract.StartContract;
import connect.activity.login.presenter.StartPagePresenter;
import connect.ui.activity.R;
import connect.utils.ActivityUtil;
import connect.utils.permission.PermissionUtil;

/**
 * The App start page.
 */
public class StartPageActivity extends BaseActivity implements StartContract.View {

    @Bind(R.id.start_img)
    ImageView startImg;

    private StartPageActivity mActivity;
    private StartContract.Presenter presenter;

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
        startImg.setImageResource(R.mipmap.bg_start_man);
        new StartPagePresenter(this).start();
    }

    @Override
    public void setPresenter(StartContract.Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void goIntoGuide() {
        //ActivityUtil.next(mActivity, GuidePageActivity.class);
    }

    @Override
    public void goIntoLoginForPhone() {
        LoginUserActivity.startActivity(mActivity);
    }

    @Override
    public void goIntoHome() {
        HomeActivity.startActivity(mActivity);
    }

    @Override
    public Activity getActivity() {
        return mActivity;
    }

    protected PermissionUtil.ResultCallBack permissomCallBack = new PermissionUtil.ResultCallBack() {
        @Override
        public void granted(String[] permissions) {

        }

        @Override
        public void deny(String[] permissions) {
            ActivityUtil.goBack(mActivity);
        }
    };

}
