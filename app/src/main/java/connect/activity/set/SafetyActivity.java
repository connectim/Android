package connect.activity.set;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.protobuf.ByteString;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import connect.activity.base.BaseActivity;
import connect.activity.home.bean.HomeAction;
import connect.activity.login.bean.UserBean;
import connect.database.SharedPreferenceUtil;
import connect.database.green.DaoManager;
import connect.ui.activity.R;
import connect.utils.ActivityUtil;
import connect.utils.dialog.DialogUtil;
import connect.utils.ProgressUtil;
import connect.utils.ToastEUtil;
import connect.utils.UriUtil;
import connect.utils.okhttp.OkHttpUtil;
import connect.utils.okhttp.ResultCall;
import connect.widget.TopToolBar;
import protos.Connect;

/**
 * Account and security.
 */
public class SafetyActivity extends BaseActivity {

    @Bind(R.id.toolbar_top)
    TopToolBar toolbarTop;
    @Bind(R.id.phone_tv)
    TextView phoneTv;
    @Bind(R.id.phone_ll)
    LinearLayout phoneLl;
    @Bind(R.id.password_ll)
    LinearLayout passwordLl;
    @Bind(R.id.password_tv)
    TextView passwordTv;
    @Bind(R.id.delete_tv)
    TextView deleteTv;

    private SafetyActivity mActivity;
    private UserBean userBean;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_safety);
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
        toolbarTop.setTitle(null, R.string.Set_Account_security);

        userBean = SharedPreferenceUtil.getInstance().getUser();
        if (userBean != null && TextUtils.isEmpty("")) {
            phoneTv.setText(R.string.Set_Phone_unbinded);
        } else {
            //phoneTv.setText(userBean.getPhone());
            /*try {
                String phoneNum = userBean.getPhone();
                String[] splitArr = phoneNum.split("-");
                String phone = splitArr == null || splitArr.length <= 1 ? phoneNum : splitArr[1];
                phoneTv.setText(phone);
            } catch (Exception e) {
                e.printStackTrace();
            }*/
        }

        /*if (userBean.isOpenPassword()) {
            passwordTv.setText(R.string.Set_On);
        } else {
            passwordTv.setText(R.string.Set_Off);
        }*/
    }

    @OnClick(R.id.left_img)
    void goBack(View view) {
        ActivityUtil.goBack(mActivity);
    }

    @OnClick(R.id.phone_ll)
    void goLink(View view) {
        ActivityUtil.next(mActivity, SafetyPhoneActivity.class);
    }

    @OnClick(R.id.password_ll)
    void goPassword(View view) {
        ActivityUtil.next(mActivity, SafetyLoginPassActivity.class);
    }

    @OnClick(R.id.delete_tv)
    void logOut(View view) {
        DialogUtil.showAlertTextView(mActivity,
                mActivity.getResources().getString(R.string.Set_tip_title),
                mActivity.getResources().getString(R.string.Set_delete_account_hinit),
                "", "", false, new DialogUtil.OnItemClickListener() {
                    @Override
                    public void confirm(String value) {
                        ProgressUtil.getInstance().showProgress(mActivity,R.string.Set_Logging_out);
                        deleteAccount();
                    }

                    @Override
                    public void cancel() {}
                });
    }

    /**
     * delete account
     */
    private void deleteAccount(){
        OkHttpUtil.getInstance().postEncrySelf(UriUtil.V2_SETTING_DELETE_USER, ByteString.copyFrom(new byte[]{}), new ResultCall<Connect.HttpResponse>() {
            @Override
            public void onResponse(Connect.HttpResponse response) {
                DaoManager.getInstance().deleteDataBase();
                HomeAction.getInstance().sendEvent(HomeAction.HomeType.DELAY_EXIT);
            }

            @Override
            public void onError(Connect.HttpResponse response) {
                ProgressUtil.getInstance().dismissProgress();
                if(response.getCode() == 2419){
                    ToastEUtil.makeText(mActivity, R.string.Wallet_No_match_user, ToastEUtil.TOAST_STATUS_FAILE).show();
                } else {
                    ToastEUtil.makeText(mActivity, response.getMessage(), ToastEUtil.TOAST_STATUS_FAILE).show();
                }
            }
        });
    }

}
