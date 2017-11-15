package connect.activity.set;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import connect.activity.base.BaseActivity;
import connect.activity.login.bean.UserBean;
import connect.database.SharedPreferenceUtil;
import connect.ui.activity.R;
import connect.utils.ActivityUtil;
import connect.utils.RegularUtil;
import connect.utils.ToastEUtil;
import connect.utils.UriUtil;
import connect.utils.okhttp.OkHttpUtil;
import connect.utils.okhttp.ResultCall;
import connect.widget.TopToolBar;
import protos.Connect;

/**
 * update user connect id
 */
public class UserInfoConnectIdActivity extends BaseActivity {

    @Bind(R.id.toolbar_top)
    TopToolBar toolbarTop;
    @Bind(R.id.connect_id_et)
    EditText connectIdEt;
    @Bind(R.id.close_ib)
    ImageButton closeIb;

    private UserInfoConnectIdActivity mActivity;

    public static void startActivity(Activity activity) {
        ActivityUtil.next(activity, UserInfoConnectIdActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_modify_connectid);
        ButterKnife.bind(this);
        initView();
    }

    @Override
    public void initView() {
        mActivity = this;
        toolbarTop.setBlackStyle();
        toolbarTop.setLeftImg(R.mipmap.back_white);
        toolbarTop.setRightText(R.string.Set_Save);
        toolbarTop.setRightTextEnable(false);
        toolbarTop.setTitle(null, R.string.Set_ID);

        connectIdEt.addTextChangedListener(textWatcher);
    }

    @OnClick(R.id.left_img)
    void goBack(View view) {
        ActivityUtil.goBack(mActivity);
    }

    @OnClick(R.id.right_lin)
    void saveClick(View view) {
        String editTxt = connectIdEt.getText().toString();
        requestID(editTxt);
    }

    @OnClick(R.id.close_ib)
    void closeEdit(View view) {
        connectIdEt.setText("");
    }

    private TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}

        @Override
        public void afterTextChanged(Editable s) {
            String inputStr = s.toString().trim();
            if (TextUtils.isEmpty(inputStr)) {
                closeIb.setVisibility(View.GONE);
            } else {
                closeIb.setVisibility(View.VISIBLE);
            }

            if (RegularUtil.matches(inputStr, RegularUtil.CONNECT_ID)) {
                toolbarTop.setRightTextEnable(true);
            } else {
                toolbarTop.setRightTextEnable(false);
            }
        }
    };

    public void requestID(final String value) {
        Connect.ConnectId connectId = Connect.ConnectId.newBuilder()
                .setConnectId(value)
                .build();
        OkHttpUtil.getInstance().postEncrySelf(UriUtil.CONNECT_V1_SETTING_CONNECTID, connectId,
                new ResultCall<Connect.HttpNotSignResponse>() {
                    @Override
                    public void onResponse(Connect.HttpNotSignResponse response) {
                        UserBean userBean = SharedPreferenceUtil.getInstance().getUser();
                        userBean.setConnectId(value);
                        userBean.setUpdateConnectId(true);
                        SharedPreferenceUtil.getInstance().putUser(userBean);

                        ToastEUtil.makeText(mActivity, R.string.Set_Set_success).show();
                        ActivityUtil.goBack(mActivity);
                    }

                    @Override
                    public void onError(Connect.HttpNotSignResponse response) {
                        if(response.getCode() == 2418){
                            ToastEUtil.makeText(mActivity, R.string.Set_CONNECT_ID_can_only_be_set_once).show();
                        }else if(response.getCode() == 2407){
                            ToastEUtil.makeText(mActivity, R.string.Set_CONNECT_ID_regular).show();
                        }else {
                            ToastEUtil.makeText(mActivity, response.getMessage()).show();
                        }
                    }
                });
    }

}
