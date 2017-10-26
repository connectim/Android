package connect.activity.chat.set;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;

import butterknife.Bind;
import butterknife.ButterKnife;
import connect.activity.base.BaseActivity;
import connect.activity.chat.set.contract.GroupIntroduceContract;
import connect.activity.chat.set.presenter.GroupIntroducePresenter;
import connect.ui.activity.R;
import connect.utils.ActivityUtil;
import connect.widget.TopToolBar;

/**
 * Group introduce
 */
public class GroupIntroduceActivity extends BaseActivity implements GroupIntroduceContract.BView {

    @Bind(R.id.toolbar)
    TopToolBar toolbar;
    @Bind(R.id.edit)
    EditText edit;

    private GroupIntroduceActivity activity;
    private String groupKey = null;

    private GroupIntroduceTextWatcher textWatcher = new GroupIntroduceTextWatcher();
    private GroupIntroduceContract.Presenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_introduce);
        ButterKnife.bind(this);
        initView();
    }

    public static void startActivity(Activity activity, String groupkey) {
        Bundle bundle = new Bundle();
        bundle.putString("GROUP_KEY", groupkey);
        ActivityUtil.next(activity, GroupIntroduceActivity.class, bundle);
    }

    @Override
    public void initView() {
        activity = this;
        toolbar.setBlackStyle();
        toolbar.setLeftImg(R.mipmap.back_white);
        toolbar.setTitle(getResources().getString(R.string.Link_Group_Introduction));
        toolbar.setRightText(R.string.Set_Save);
        toolbar.setRightTextColor(R.color.color_68656f);
        toolbar.setLeftListence(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityUtil.goBack(activity);
            }
        });

        groupKey = getIntent().getStringExtra("GROUP_KEY");
        edit.addTextChangedListener(textWatcher);
        new GroupIntroducePresenter(this).start();
    }

    private class GroupIntroduceTextWatcher implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            if (s.length() == 0) {
                toolbar.setRightTextColor(R.color.color_68656f);
                toolbar.setRightListence(null);
            } else {
                toolbar.setRightTextColor(R.color.color_green);
                toolbar.setRightListence(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String introduce = edit.getText().toString();
                        presenter.requestUpdateGroupSummary(introduce);
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
    public void setPresenter(GroupIntroduceContract.Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public Activity getActivity() {
        return activity;
    }

    @Override
    public void groupIntroduce(String introduce) {
        edit.setText(introduce);
        edit.setSelection(introduce.length());
    }
}
