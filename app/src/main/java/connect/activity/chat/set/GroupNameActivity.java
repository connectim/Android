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
import connect.activity.chat.set.contract.GroupNameContract;
import connect.activity.chat.set.presenter.GroupNamePresenter;
import connect.ui.activity.R;
import connect.utils.ActivityUtil;
import connect.widget.TopToolBar;

public class GroupNameActivity extends BaseActivity implements GroupNameContract.BView{

    @Bind(R.id.toolbar)
    TopToolBar toolbar;
    @Bind(R.id.txt1)
    TextView txt1;
    @Bind(R.id.edittxt1)
    EditText edittxt1;

    private GroupNameActivity activity;
    private static String TAG = "_GroupNameActivity";
    private static String GROUP_IDENTIFY = "GROUP_IDENTIFY";
    private String groupKey = null;

    private GroupNameTextWatcher textWatcher=new GroupNameTextWatcher();
    private GroupNameContract.Presenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_name);
        ButterKnife.bind(this);
        initView();
    }

    public static void startActivity(Activity activity, String groupkey) {
        Bundle bundle = new Bundle();
        bundle.putString(GROUP_IDENTIFY, groupkey);
        ActivityUtil.next(activity, GroupNameActivity.class, bundle);
    }

    @Override
    public void initView() {
        activity = this;
        toolbar.setBlackStyle();
        toolbar.setLeftImg(R.mipmap.back_white);
        toolbar.setTitle(getResources().getString(R.string.Link_Group));
        toolbar.setLeftListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityUtil.goBack(activity);
            }
        });
        toolbar.setRightText(R.string.Chat_Complete);
        toolbar.setRightTextEnable(false);
        toolbar.setRightListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String groupName = edittxt1.getText().toString();
                if (groupName.length() >= 2) {
                    presenter.updateGroupName(groupName);
                }
            }
        });

        groupKey = getIntent().getStringExtra(GROUP_IDENTIFY);
        edittxt1.addTextChangedListener(textWatcher);
        new GroupNamePresenter(this).start();
    }

    private class GroupNameTextWatcher implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            if (s.length() <2) {
                toolbar.setRightTextEnable(false);
                toolbar.setRightTextColor(R.color.color_68656f);
            } else {
                toolbar.setRightTextEnable(true);
                toolbar.setRightTextColor(R.color.color_green);
            }
        }
    }

    @Override
    public String getRoomKey() {
        return groupKey;
    }

    @Override
    public void setPresenter(GroupNameContract.Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public Activity getActivity() {
        return activity;
    }

    @Override
    public void groupName(String groupname) {
        edittxt1.setText(groupname);
    }
}
