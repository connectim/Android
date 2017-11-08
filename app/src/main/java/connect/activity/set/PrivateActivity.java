package connect.activity.set;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import connect.activity.base.BaseActivity;
import connect.activity.set.bean.PrivateSetBean;
import connect.database.green.DaoHelper.ParamManager;
import connect.ui.activity.R;
import connect.utils.ActivityUtil;
import connect.utils.UriUtil;
import connect.utils.okhttp.OkHttpUtil;
import connect.utils.okhttp.ResultCall;
import connect.widget.TopToolBar;
import protos.Connect;

/**
 * User privacy Settings.
 */
public class PrivateActivity extends BaseActivity {

    @Bind(R.id.toolbar_top)
    TopToolBar toolbarTop;
    @Bind(R.id.find_phone_tb)
    View findPhoneTb;
    @Bind(R.id.find_recommend_tb)
    View findRecommendTb;
    @Bind(R.id.black_list_ll)
    LinearLayout blackListLl;

    private PrivateActivity mActivity;
    private PrivateSetBean privateSetBean;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_private);
        ButterKnife.bind(this);
        initView();
    }

    @Override
    public void initView() {
        mActivity = this;
        toolbarTop.setBlackStyle();
        toolbarTop.setLeftImg(R.mipmap.back_white);
        toolbarTop.setTitle(null, R.string.Set_Privacy);

        privateSetBean = ParamManager.getInstance().getPrivateSet();
        if (privateSetBean != null) {
            findPhoneTb.setSelected(privateSetBean.getPhoneFind());
            findRecommendTb.setSelected(privateSetBean.getRecommend());
        }
        /*RotateAnimation animation = new RotateAnimation(0f,360f * 3, Animation.RELATIVE_TO_SELF,
                0.5f,Animation.RELATIVE_TO_SELF,0.5f);
        animation.setDuration(1000);
        animation.setFillAfter(false);*/
    }

    @OnClick(R.id.left_img)
    void goBack(View view) {
        ActivityUtil.goBack(mActivity);
    }

    @OnClick(R.id.black_list_ll)
    void goBlackList(View view) {
        ActivityUtil.next(mActivity, PrivateBlackActivity.class);
    }

    @OnClick(R.id.find_phone_tb)
    void switchFriendPhone(View view) {
        boolean isSelect = findPhoneTb.isSelected();
        findPhoneTb.setSelected(!isSelect);
        privateSetBean.setPhoneFind(!isSelect);
        requestPrivate();
    }

    @OnClick(R.id.find_recommend_tb)
    void switchRecommend(View view) {
        boolean isSelect = findRecommendTb.isSelected();
        findRecommendTb.setSelected(!isSelect);
        privateSetBean.setRecommend(!isSelect);
        requestPrivate();
    }

    /**
     * Update the new privacy Settings
     */
    private void requestPrivate() {
        Connect.Privacy privacy = Connect.Privacy.newBuilder()
                .setPhoneNum(privateSetBean.getPhoneFind())
                .setRecommend(privateSetBean.getRecommend())
                .build();
        OkHttpUtil.getInstance().postEncrySelf(UriUtil.SETTING_PRIVACY, privacy, new ResultCall<Connect.HttpResponse>() {
            @Override
            public void onResponse(Connect.HttpResponse response) {
                ParamManager.getInstance().putPrivateSet(privateSetBean);
            }

            @Override
            public void onError(Connect.HttpResponse response) {}
        });
    }

}
