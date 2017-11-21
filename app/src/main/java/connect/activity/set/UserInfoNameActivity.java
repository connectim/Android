package connect.activity.set;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import connect.activity.base.BaseActivity;
import connect.activity.login.bean.UserBean;
import connect.database.SharedPreferenceUtil;
import connect.ui.activity.R;
import connect.utils.ActivityUtil;
import connect.utils.ToastEUtil;
import connect.utils.UriUtil;
import connect.utils.okhttp.OkHttpUtil;
import connect.utils.okhttp.ResultCall;
import connect.widget.TopToolBar;
import protos.Connect;

/**
 * Modify the user nickname
 */
public class UserInfoNameActivity extends BaseActivity{

    @Bind(R.id.toolbar_top)
    TopToolBar toolbarTop;
    @Bind(R.id.name_et)
    EditText nameEt;
    @Bind(R.id.close_ib)
    ImageButton closeIb;

    private UserInfoNameActivity mActivity;

    public static void startActivity(Activity activity) {
        ActivityUtil.next(activity, UserInfoNameActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_modifyname);
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
        toolbarTop.setTitle(null, R.string.Set_Name);

        UserBean userBean = SharedPreferenceUtil.getInstance().getUser();
        nameEt.setText(userBean.getName());
        nameEt.addTextChangedListener(textWatcher);
    }

    @OnClick(R.id.left_img)
    void goBack(View view) {
        ActivityUtil.goBack(mActivity);
    }

    @OnClick(R.id.right_lin)
    void saveClick(View view) {
        String editTxt = nameEt.getText().toString();
        requestName(editTxt);
    }

    @OnClick(R.id.close_ib)
    void closeEdit(View view) {
        nameEt.setText("");
    }

    private TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            String inputStr = s.toString().trim();
            if (TextUtils.isEmpty(inputStr)) {
                closeIb.setVisibility(View.GONE);
                toolbarTop.setRightTextEnable(false);
                return;
            } else {
                closeIb.setVisibility(View.VISIBLE);
                toolbarTop.setRightTextEnable(true);
            }
        }
    };

    public void requestName(final String value) {
        Connect.SettingUserInfo avatar = Connect.SettingUserInfo.newBuilder()
                .setUsername(value.trim())
                .build();
        OkHttpUtil.getInstance().postEncrySelf(UriUtil.CONNECT_V1_SETTING_USERINFO, avatar, new ResultCall<Connect.HttpNotSignResponse>() {
            @Override
            public void onResponse(Connect.HttpNotSignResponse response) {
                UserBean userBean = SharedPreferenceUtil.getInstance().getUser();
                userBean.setName(value.trim());
                SharedPreferenceUtil.getInstance().putUser(userBean);

                ToastEUtil.makeText(mActivity, R.string.Set_Set_success).show();
                ActivityUtil.goBack(mActivity);
            }

            @Override
            public void onError(Connect.HttpNotSignResponse response) {
                if (response.getCode() == 2102) {
                    Toast.makeText(mActivity, R.string.Login_username_already_exists, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

}
