package connect.activity.set;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import connect.ui.activity.R;
import connect.activity.set.contract.UserInfoNameContract;
import connect.activity.set.presenter.UserInfoNamePresenter;
import connect.activity.base.BaseActivity;
import connect.utils.ActivityUtil;
import connect.utils.ToastEUtil;
import connect.widget.TopToolBar;

/**
 * Modify the user nickname and connect Id.
 */
public class UserInfoNameActivity extends BaseActivity implements UserInfoNameContract.View{

    @Bind(R.id.toolbar_top)
    TopToolBar toolbarTop;
    @Bind(R.id.name_et)
    EditText nameEt;
    @Bind(R.id.close_ib)
    ImageButton closeIb;
    @Bind(R.id.id_tip_tv)
    TextView idTipTv;

    private UserInfoNameActivity mActivity;
    public static final String TYPE_NAME = "name";
    public static final String TYPE_NUMBER = "id";
    private String type;
    private UserInfoNameContract.Presenter presenter;

    public static void startActivity(Activity activity, String type) {
        Bundle bundle = new Bundle();
        bundle.putString("type", type);
        ActivityUtil.next(activity, UserInfoNameActivity.class, bundle);
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

        Bundle bundle = getIntent().getExtras();
        type = bundle.getString("type");
        new UserInfoNamePresenter(this,type).start();
    }

    @OnClick(R.id.left_img)
    void goback(View view) {
        ActivityUtil.goBack(mActivity);
    }

    @OnClick(R.id.right_lin)
    void saveClick(View view) {
        String editTxt = nameEt.getText().toString();
        if (type.equals(TYPE_NAME)) {
            presenter.requestName(editTxt);
        } else if (type.equals(TYPE_NUMBER)) {
            presenter.requestID(editTxt);
        }
    }

    @OnClick(R.id.close_ib)
    void closeEdit(View view) {
        nameEt.setText("");
    }

    @Override
    public void setPresenter(UserInfoNameContract.Presenter presenter) {
        this.presenter = presenter;
    }

    private TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}

        @Override
        public void afterTextChanged(Editable s) {
            String inputStr = s.toString().trim();
            checkEditInput(inputStr);
        }
    };

    @Override
    public Activity getActivity() {
        return mActivity;
    }

    @Override
    public void setInitView(int titleResId, int hintResId, String text) {
        nameEt.addTextChangedListener(textWatcher);
        toolbarTop.setTitle(null, titleResId);
        nameEt.setHint(hintResId);
        nameEt.setText(text);
        if (type.equals(TYPE_NUMBER)) {
            idTipTv.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void setFinish() {
        ToastEUtil.makeText(mActivity, R.string.Set_Set_success).show();
        ActivityUtil.goBack(mActivity);
    }

    private void checkEditInput(String inputStr){
        if (TextUtils.isEmpty(inputStr)) {
            closeIb.setVisibility(View.GONE);
            toolbarTop.setRightTextEnable(false);
            return;
        } else {
            closeIb.setVisibility(View.VISIBLE);
            toolbarTop.setRightTextEnable(true);
        }

        switch (type){
            case UserInfoNameActivity.TYPE_NUMBER:
                if (inputStr.getBytes().length >= 6) {
                    toolbarTop.setRightTextEnable(true);
                } else {
                    toolbarTop.setRightTextEnable(false);
                }
                break;
            default:
                break;
        }
    }

}