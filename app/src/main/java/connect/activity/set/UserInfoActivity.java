package connect.activity.set;

import android.app.Activity;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import connect.database.SharedPreferenceUtil;
import connect.ui.activity.R;
import connect.activity.login.bean.UserBean;
import connect.activity.base.BaseActivity;
import connect.utils.ActivityUtil;
import connect.utils.ToastEUtil;
import connect.utils.glide.GlideUtil;
import connect.widget.TopToolBar;
import connect.widget.roundedimageview.RoundedImageView;

/**
 * The user basic information.
 */
public class UserInfoActivity extends BaseActivity {

    @Bind(R.id.toolbar_top)
    TopToolBar toolbarTop;
    @Bind(R.id.avatar_ll)
    LinearLayout avatarLl;
    @Bind(R.id.name_tv)
    TextView nameTv;
    @Bind(R.id.name_ll)
    LinearLayout nameLl;
    @Bind(R.id.id_tv)
    TextView idTv;
    @Bind(R.id.id_ll)
    RelativeLayout idLl;
    @Bind(R.id.avatar_iv)
    RoundedImageView avatarIv;
    @Bind(R.id.number_tv)
    TextView numberTv;
    @Bind(R.id.number_ll)
    RelativeLayout numberLl;

    private UserInfoActivity mActivity;
    private UserBean userBean;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_userinfo);
        ButterKnife.bind(this);
    }

    public static void startActivity(Activity activity) {
        ActivityUtil.next(activity, UserInfoActivity.class);
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
        toolbarTop.setLeftImg(R.mipmap.back_white);
        toolbarTop.setTitle(null, R.string.Set_My_Profile);

        userBean = SharedPreferenceUtil.getInstance().getUser();
        GlideUtil.loadAvater(avatarIv, userBean.getAvatar());
        nameTv.setText(userBean.getName());
        idTv.setText(userBean.getAddress());
        if (TextUtils.isEmpty(userBean.getConnectId())) {
            // numberTv.setText(R.string.Login_Not_set);
            numberTv.setText(userBean.getAddress());
        } else {
            numberTv.setText(userBean.getConnectId());
        }
    }

    @OnClick(R.id.left_img)
    void goBack(View view) {
        ActivityUtil.goBack(mActivity);
    }

    @OnClick(R.id.avatar_ll)
    void goAvatar(View view) {
        UserInfoAvatarActivity.startActivity(mActivity);
    }

    @OnClick(R.id.name_ll)
    void goName(View view) {
        UserInfoNameActivity.startActivity(mActivity,UserInfoNameActivity.TYPE_NAME);
    }

    @OnClick(R.id.number_ll)
    void goNumber(View view) {
        if (TextUtils.isEmpty(userBean.getConnectId())) {
            UserInfoNameActivity.startActivity(mActivity,UserInfoNameActivity.TYPE_NUMBER);
        } else {
            ToastEUtil.makeText(mActivity,R.string.Set_CONNECT_ID_can_only_be_set_once,ToastEUtil.TOAST_STATUS_FAILE).show();
        }
    }

    @OnClick(R.id.id_ll)
    void goId(View view) {
        ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        cm.setText(userBean.getAddress());
        ToastEUtil.makeText(mActivity,R.string.Set_Copied).show();
    }

}
