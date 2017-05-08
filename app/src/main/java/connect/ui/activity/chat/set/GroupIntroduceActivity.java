package connect.ui.activity.chat.set;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;

import butterknife.Bind;
import butterknife.ButterKnife;
import connect.db.green.DaoHelper.ContactHelper;
import connect.db.green.bean.GroupEntity;
import connect.ui.activity.R;
import connect.ui.base.BaseActivity;
import connect.utils.ActivityUtil;
import connect.utils.UriUtil;
import connect.utils.okhttp.OkHttpUtil;
import connect.utils.okhttp.ResultCall;
import connect.view.TopToolBar;
import protos.Connect;

/**
 * Group introduce
 */
public class GroupIntroduceActivity extends BaseActivity {

    @Bind(R.id.toolbar)
    TopToolBar toolbar;
    @Bind(R.id.edit)
    EditText edit;

    private GroupIntroduceActivity activity;
    private static String GROUP_KEY = "GROUP_KEY";
    private String groupKey = null;
    private GroupEntity groupEntity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_introduce);
        ButterKnife.bind(this);
        initView();
    }

    public static void startActivity(Activity activity, String groupkey) {
        Bundle bundle = new Bundle();
        bundle.putString(GROUP_KEY, groupkey);
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

        groupKey = getIntent().getStringExtra(GROUP_KEY);
        groupEntity = ContactHelper.getInstance().loadGroupEntity(groupKey);
        if (groupEntity == null) {
            ActivityUtil.goBack(activity);
            return;
        }

        String groupSummary = groupEntity.getSummary();
        if (TextUtils.isEmpty(groupSummary)) {
            groupSummary = groupEntity.getName();
        }
        edit.setText(groupSummary);
        edit.setSelection(groupSummary.length());
        edit.addTextChangedListener(new TextWatcher() {
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
                            groupSetting(groupEntity, edit.getText().toString());
                        }
                    });
                }
            }
        });
    }

    protected void groupSetting(final GroupEntity groupEntity, final String summary) {
        Connect.GroupSetting setting = Connect.GroupSetting.newBuilder()
                .setIdentifier(groupEntity.getIdentifier())
                .setSummary(summary)
                .setPublic(true).build();
        OkHttpUtil.getInstance().postEncrySelf(UriUtil.GROUP_SETTING, setting, new ResultCall<Connect.HttpResponse>() {
            @Override
            public void onResponse(Connect.HttpResponse response) {
                groupEntity.setSummary(summary);
                ContactHelper.getInstance().inserGroupEntity(groupEntity);

                GroupManageActivity.startActivity(activity,groupKey);
            }

            @Override
            public void onError(Connect.HttpResponse response) {

            }
        });
    }
}
