package connect.ui.activity.chat.set;

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
import connect.db.green.DaoHelper.ContactHelper;
import connect.db.green.bean.GroupEntity;
import connect.ui.activity.R;
import connect.ui.activity.chat.bean.RecExtBean;
import connect.ui.activity.contact.bean.ContactNotice;
import connect.ui.base.BaseActivity;
import connect.utils.ActivityUtil;
import connect.utils.ToastEUtil;
import connect.utils.UriUtil;
import connect.utils.okhttp.OkHttpUtil;
import connect.utils.okhttp.ResultCall;
import connect.view.TopToolBar;
import protos.Connect;

public class GroupNameActivity extends BaseActivity {

    @Bind(R.id.toolbar)
    TopToolBar toolbar;
    @Bind(R.id.txt1)
    TextView txt1;
    @Bind(R.id.edittxt1)
    EditText edittxt1;

    private Activity activity;
    private static String GROUP_KEY = "GROUP_KEY";
    private String groupKey = null;
    private GroupEntity groupEntity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_name);
        ButterKnife.bind(this);
        initView();
    }

    public static void startActivity(Activity activity, String groupkey) {
        Bundle bundle = new Bundle();
        bundle.putString(GROUP_KEY, groupkey);
        ActivityUtil.next(activity, GroupNameActivity.class, bundle);
    }

    @Override
    public void initView() {
        activity = this;
        toolbar.setBlackStyle();
        toolbar.setLeftImg(R.mipmap.back_white);
        toolbar.setTitle(getResources().getString(R.string.Link_Group));
        toolbar.setLeftListence(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityUtil.goBack(activity);
            }
        });
        toolbar.setRightText(R.string.Chat_Complete);
        toolbar.setRightTextColor(R.color.color_68656f);

        groupKey = getIntent().getStringExtra(GROUP_KEY);
        groupEntity = ContactHelper.getInstance().loadGroupEntity(groupKey);
        if(groupEntity==null){
            ActivityUtil.goBack(activity);
            return;
        }
        edittxt1.setText(groupEntity.getName());
        edittxt1.addTextChangedListener(new TextWatcher() {
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
                            updateGroupName();
                        }
                    });
                }
            }
        });
    }

    protected void updateGroupName() {
        Connect.UpdateGroupInfo groupInfo = Connect.UpdateGroupInfo.newBuilder()
                .setName(edittxt1.getText().toString()).setIdentifier(groupEntity.getIdentifier()).build();

        OkHttpUtil.getInstance().postEncrySelf(UriUtil.GROUP_UPDATE, groupInfo, new ResultCall<Connect.HttpResponse>() {

            @Override
            public void onResponse(Connect.HttpResponse response) {
                String groupName = edittxt1.getText().toString();
                if (TextUtils.isEmpty(groupName)) {
                    return;
                }
                GroupEntity groupEntity = ContactHelper.getInstance().loadGroupEntity(groupKey);
                groupEntity.setName(groupName);
                ContactHelper.getInstance().inserGroupEntity(groupEntity);

                ContactNotice.receiverGroup();
                RecExtBean.sendRecExtMsg(RecExtBean.ExtType.GROUP_UPDATENAME, groupName);
                GroupSetActivity.startActivity(activity,groupKey);
            }

            @Override
            public void onError(Connect.HttpResponse response) {
                ToastEUtil.makeText(activity,R.string.Link_Update_Group_Name_Failed,ToastEUtil.TOAST_STATUS_FAILE).show();
            }
        });
    }
}
