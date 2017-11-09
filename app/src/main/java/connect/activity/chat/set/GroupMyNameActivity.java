package connect.activity.chat.set;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import connect.activity.base.BaseActivity;
import connect.activity.chat.set.contract.GroupMyAliasContract;
import connect.activity.chat.set.presenter.GroupMyAliasPresenter;
import connect.ui.activity.R;
import connect.utils.ActivityUtil;
import connect.widget.TopToolBar;

public class GroupMyNameActivity extends BaseActivity implements GroupMyAliasContract.BView {

    @Bind(R.id.toolbar)
    TopToolBar toolbar;
    @Bind(R.id.edittxt2)
    EditText edittxt2;
    @Bind(R.id.txt1)
    TextView txt1;

    private GroupMyNameActivity activity;
    private static String TAG = "_GroupMyNameActivity";
    private static String GROUP_IDENTIFY = "GROUP_IDENTIFY";
    private String groupKey = null;

    private GroupMyNameTextWatcher textWatcher = new GroupMyNameTextWatcher();
    private GroupMyAliasContract.Presenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_my_name);
        ButterKnife.bind(this);
        initView();
    }

    public static void startActivity(Activity activity, String groupkey) {
        Bundle bundle = new Bundle();
        bundle.putString(GROUP_IDENTIFY, groupkey);
        ActivityUtil.next(activity, GroupMyNameActivity.class, bundle);
    }

    @Override
    public void initView() {
        activity = this;
        toolbar.setBlackStyle();
        toolbar.setLeftImg(R.mipmap.back_white);
        toolbar.setTitle(getResources().getString(R.string.Link_Group));
        toolbar.setRightText(R.string.Chat_Complete);
        toolbar.setRightTextEnable(false);
        toolbar.setLeftListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityUtil.goBack(activity);
            }
        });

        groupKey = getIntent().getStringExtra(GROUP_IDENTIFY);
        edittxt2.addTextChangedListener(textWatcher);
        new GroupMyAliasPresenter(this).start();
    }

    private class GroupMyNameTextWatcher implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            if (s.length() == 0) {
                toolbar.setRightTextEnable(false);
                toolbar.setRightTextColor(R.color.color_68656f);
                toolbar.setRightListener(null);
            } else {
                toolbar.setRightTextEnable(true);
                toolbar.setRightTextColor(R.color.color_green);
                toolbar.setRightListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String myalias = edittxt2.getText().toString();
                        presenter.updateMyAliasInGroup(myalias);
                    }
                });
            }
        }
    }

    @Override
    public String getRoomKey() {
        return groupKey;
    }

    @Override
    public void setPresenter(GroupMyAliasContract.Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public Activity getActivity() {
        return activity;
    }

    @Override
    public void myNameInGroup(String myalias) {
        edittxt2.setText(myalias);
    }
}
