package connect.activity.set;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import java.util.ArrayList;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import connect.activity.base.BaseActivity;
import connect.activity.login.bean.UserBean;
import connect.database.SharedPreferenceUtil;
import connect.ui.activity.R;
import connect.utils.ActivityUtil;
import connect.utils.DialogUtil;
import connect.utils.ProgressUtil;
import connect.utils.UriUtil;
import connect.utils.okhttp.OkHttpUtil;
import connect.utils.okhttp.ResultCall;
import connect.widget.TopToolBar;
import protos.Connect;

/**
 * binding mobile phone number
 */
public class SafetyPhoneActivity extends BaseActivity {

    @Bind(R.id.toolbar_top)
    TopToolBar toolbarTop;
    @Bind(R.id.hint_tv)
    TextView hintTv;
    @Bind(R.id.mobile_tv)
    TextView mobileTv;
    @Bind(R.id.link_btn)
    Button linkBtn;

    private SafetyPhoneActivity mActivity;
    private UserBean userBean;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_link);
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
        toolbarTop.setLeftImg(R.mipmap.back_white);
        toolbarTop.setTitle(null, R.string.Set_Link_Mobile);

        userBean = SharedPreferenceUtil.getInstance().getUser();
        if (TextUtils.isEmpty(userBean.getPhone())) {
            // Are not binding mobile phone number
            hintTv.setText(R.string.Set_Not_connected_to_mobile_network);
            linkBtn.setText(R.string.Set_Add_mobile);
            toolbarTop.setRightTextEnable(false);
            mobileTv.setText("");
        } else {
            // Has been binding mobile phone number
            hintTv.setText(R.string.Set_Your_cell_phone_number);
            linkBtn.setText(R.string.Set_Change_Mobile);
            toolbarTop.setRightTextEnable(true);
            mobileTv.setText("+" + userBean.getPhone());
        }
    }

    @OnClick(R.id.left_img)
    void goback(View view) {
        ActivityUtil.goBack(mActivity);
    }

    @OnClick(R.id.link_btn)
    void linkBtn(View view) {
        SafetyPhoneNumberActivity.startActivity(mActivity, SafetyPhoneNumberActivity.LINK_TYPE);
    }

    /*private void requestBindMobile() {
        ProgressUtil.getInstance().showProgress(mActivity);
        String[] phoneArray;
        // To obtain the binding mobile phone number
        if (userBean.getPhone().contains("-")) {
            phoneArray = userBean.getPhone().split("-");
        } else if (userBean.getPhone().contains("**")) {
            phoneArray = userBean.getPhone().split("\\*\\*");
        } else {
            ProgressUtil.getInstance().dismissProgress();
            return;
        }
        Connect.MobileVerify mobileVerify = Connect.MobileVerify.newBuilder()
                .setCountryCode(Integer.valueOf(phoneArray[0]))
                .setNumber(phoneArray[1])
                .build();
        OkHttpUtil.getInstance().postEncrySelf(UriUtil.SETTING_UNBIND_MOBILE, mobileVerify, new ResultCall<Connect.HttpNotSignResponse>() {
            @Override
            public void onResponse(Connect.HttpNotSignResponse response) {
                userBean.setPhone("");
                SharedPreferenceUtil.getInstance().putUser(userBean);
                ProgressUtil.getInstance().dismissProgress();
                initView();
            }

            @Override
            public void onError(Connect.HttpNotSignResponse response) {
                ProgressUtil.getInstance().dismissProgress();
            }
        });
    }*/
}
