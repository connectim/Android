package connect.activity.chat.set;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import connect.database.MemoryDataManager;
import connect.database.green.DaoHelper.ContactHelper;
import connect.database.green.bean.GroupMemberEntity;
import connect.ui.activity.R;
import connect.activity.chat.bean.RecExtBean;
import connect.activity.base.BaseActivity;
import connect.utils.ActivityUtil;
import connect.utils.ToastEUtil;
import connect.utils.UriUtil;
import connect.utils.okhttp.OkHttpUtil;
import connect.utils.okhttp.ResultCall;
import connect.widget.TopToolBar;
import protos.Connect;

public class GroupMyNameActivity extends BaseActivity {

    @Bind(R.id.toolbar)
    TopToolBar toolbar;
    @Bind(R.id.edittxt2)
    EditText edittxt2;
    @Bind(R.id.txt1)
    TextView txt1;

    private Activity activity;
    private static String GROUP_KEY = "GROUP_KEY";
    private String groupKey = null;
    private GroupMemberEntity groupMemEntity = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_my_name);
        ButterKnife.bind(this);
        initView();
    }

    public static void startActivity(Activity activity, String groupkey) {
        Bundle bundle = new Bundle();
        bundle.putString(GROUP_KEY, groupkey);
        ActivityUtil.next(activity, GroupMyNameActivity.class, bundle);
    }

    @Override
    public void initView() {
        activity = this;
        toolbar.setBlackStyle();
        toolbar.setLeftImg(R.mipmap.back_white);
        toolbar.setTitle(getResources().getString(R.string.Link_Group));
        toolbar.setRightText(R.string.Chat_Complete);
        toolbar.setRightTextColor(R.color.color_68656f);
        toolbar.setLeftListence(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityUtil.goBack(activity);
            }
        });

        groupKey = getIntent().getStringExtra(GROUP_KEY);
        groupMemEntity = ContactHelper.getInstance().loadGroupMemByAds(groupKey, MemoryDataManager.getInstance().getAddress());
        if (null != groupMemEntity) {
            edittxt2.setText(TextUtils.isEmpty(groupMemEntity.getNick()) ? groupMemEntity.getUsername() : groupMemEntity.getNick());
        }
        edittxt2.addTextChangedListener(new TextWatcher() {
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
                            updateMyName(edittxt2.getText().toString());
                        }
                    });
                }
            }
        });
    }

    private void updateMyName(final String myname) {
        Connect.UpdateGroupMemberInfo memberInfo = Connect.UpdateGroupMemberInfo.newBuilder()
                .setNick(myname).setIdentifier(groupKey).build();
        OkHttpUtil.getInstance().postEncrySelf(UriUtil.GROUP_MEMUPDATE, memberInfo, new ResultCall<Connect.HttpResponse>() {
            @Override
            public void onResponse(Connect.HttpResponse response) {
                groupMemEntity.setUsername(myname);
                ContactHelper.getInstance().inserGroupMemEntity(groupMemEntity);
                RecExtBean.getInstance().sendEvent(RecExtBean.ExtType.GROUP_UPDATEMYNAME);
                GroupSetActivity.startActivity(activity,groupKey);
            }

            @Override
            public void onError(Connect.HttpResponse response) {
                ToastEUtil.makeText(activity,R.string.Link_An_error_occurred_change_nickname,ToastEUtil.TOAST_STATUS_FAILE).show();
            }
        });
    }
}
