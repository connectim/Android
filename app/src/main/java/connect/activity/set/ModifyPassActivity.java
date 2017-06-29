package connect.activity.set;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import connect.database.SharedPreferenceUtil;
import connect.ui.activity.R;
import connect.activity.login.bean.UserBean;
import connect.activity.set.contract.ModifyPassContract;
import connect.activity.set.presenter.ModifyPassPresenter;
import connect.activity.base.BaseActivity;
import connect.utils.ActivityUtil;
import connect.utils.DialogUtil;
import connect.utils.ProgressUtil;
import connect.utils.RegularUtil;
import connect.utils.ToastUtil;
import connect.widget.TopToolBar;

/**
 *
 * Created by Administrator on 2016/12/2.
 */
public class ModifyPassActivity extends BaseActivity implements ModifyPassContract.View{

    @Bind(R.id.toolbar_top)
    TopToolBar toolbarTop;
    @Bind(R.id.password_et)
    EditText passwordEt;
    @Bind(R.id.password_ib)
    ImageButton passwordIb;
    @Bind(R.id.passwordhint_et)
    EditText passwordhintEt;
    @Bind(R.id.passwordhint_ib)
    ImageButton passwordhintIb;

    private ModifyPassActivity mActivity;
    private ModifyPassContract.Presenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_changepass);
        ButterKnife.bind(this);
        initView();
    }

    @Override
    public void initView() {
        mActivity = this;
        toolbarTop.setBlackStyle();
        toolbarTop.setLeftImg(R.mipmap.back_white);
        toolbarTop.setTitle(null, R.string.Set_Change_password);
        toolbarTop.setRightText(R.string.Set_Save);
        toolbarTop.setRightTextColor(R.color.color_00c400);
        toolbarTop.setRightTextEnable(false);

        UserBean userBean = SharedPreferenceUtil.getInstance().getUser();
        new ModifyPassPresenter(this).start();

        passwordhintEt.setText(userBean.getPassHint());
        passwordEt.addTextChangedListener(presenter.getPassTextChange());
    }

    @Override
    public void setPresenter(ModifyPassContract.Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public Activity getActivity() {
        return mActivity;
    }

    @Override
    public void setRightTextEnable(boolean isEnable) {
        toolbarTop.setRightTextEnable(isEnable);
    }

    @Override
    public void setHint(String value) {
        passwordhintEt.setText(value);
    }

    @OnClick(R.id.left_img)
    void goback(View view) {
        ActivityUtil.goBack(mActivity);
    }

    @OnClick(R.id.password_ib)
    void closeEditNew(View view) {
        passwordEt.setText("");
    }

    @OnClick(R.id.passwordhint_ib)
    void closeEditHint(View view) {
        passwordhintEt.setText("");
    }

    @OnClick(R.id.right_lin)
    void saveClick(View view) {
        if(RegularUtil.matches(passwordEt.getText().toString(), RegularUtil.PASSWORD)){
            String password = passwordEt.getText().toString();
            String passwordHint = passwordhintEt.getText().toString();
            if (TextUtils.isEmpty(password)) {
                ToastUtil.getInstance().showToast(R.string.Login_Password_incorrect);
            } else {
                ProgressUtil.getInstance().showProgress(mActivity);
                presenter.requestPass(password,passwordHint);
            }
        }else{
            DialogUtil.showAlertTextView(mActivity, getString(R.string.Set_tip_title),
                    getString(R.string.Login_letter_number_and_character_must_be_included_in_your_login_password),
                    "", "", true, new DialogUtil.OnItemClickListener() {
                        @Override
                        public void confirm(String value) {

                        }

                        @Override
                        public void cancel() {

                        }
                    });
        }
    }

}
