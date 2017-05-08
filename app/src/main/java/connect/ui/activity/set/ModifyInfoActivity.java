package connect.ui.activity.set;

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
import connect.db.SharedPreferenceUtil;
import connect.ui.activity.R;
import connect.ui.activity.login.bean.UserBean;
import connect.ui.base.BaseActivity;
import connect.utils.ActivityUtil;
import connect.utils.ToastEUtil;
import connect.utils.glide.GlideUtil;
import connect.view.TopToolBar;
import connect.view.roundedimageview.RoundedImageView;

/**
 *
 * Created by Administrator on 2016/12/1.
 */
public class ModifyInfoActivity extends BaseActivity {

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

    private ModifyInfoActivity mActivity;
    private UserBean userBean;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_userinfo);
        ButterKnife.bind(this);
    }

    public static void startActivity(Activity activity) {
        ActivityUtil.next(activity, ModifyInfoActivity.class);
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
        if(TextUtils.isEmpty(userBean.getConnectId())){
            numberTv.setText(R.string.Login_Not_set);
        }else{
            numberTv.setText(userBean.getConnectId());
        }
    }

    @OnClick(R.id.left_img)
    void goback(View view) {
        ActivityUtil.goBack(mActivity);
    }

    @OnClick(R.id.avatar_ll)
    void goAvatar(View view) {
        ModifyAvaterActivity.startActivity(mActivity);
    }

    @OnClick(R.id.name_ll)
    void goName(View view) {
        ModifyNameActivity.startActivity(mActivity,ModifyNameActivity.TYPE_NAME);
    }

    @OnClick(R.id.number_ll)
    void goNumber(View view) {
        if(TextUtils.isEmpty(userBean.getConnectId())){
            ModifyNameActivity.startActivity(mActivity,ModifyNameActivity.TYPE_NUMBER);
        }else{
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
