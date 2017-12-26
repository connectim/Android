package connect.activity.login;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import connect.activity.base.BaseActivity;
import connect.ui.activity.R;
import connect.utils.ActivityUtil;
import connect.widget.TopToolBar;

/**
 * Login
 */
public class LoginUserActivity extends BaseActivity {

    @Bind(R.id.toolbar_top)
    TopToolBar toolbarTop;
    @Bind(R.id.name_et)
    EditText nameEt;
    @Bind(R.id.password_et)
    EditText passwordEt;
    @Bind(R.id.next_btn)
    Button nextBtn;

    private LoginUserActivity mActivity;

    public static void startActivity(Activity activity) {
        ActivityUtil.next(activity, LoginUserActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_code);
        ButterKnife.bind(this);
        initView();
    }

    @Override
    public void initView() {
        mActivity = this;
        toolbarTop.setTitle(R.mipmap.logo_black_middle, null);

        nextBtn.setEnabled(false);
        nameEt.addTextChangedListener(textWatcher);
        passwordEt.addTextChangedListener(textWatcher);
    }

    @OnClick(R.id.next_btn)
    void nextBtn(View view){

    }

    private TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}
        @Override
        public void afterTextChanged(Editable s) {
            String name = nameEt.getText().toString();
            String password = passwordEt.getText().toString();
            if(!TextUtils.isEmpty(name) && !TextUtils.isEmpty(password)){
                nextBtn.setEnabled(true);
            }else{
                nextBtn.setEnabled(false);
            }
        }
    };

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

}
