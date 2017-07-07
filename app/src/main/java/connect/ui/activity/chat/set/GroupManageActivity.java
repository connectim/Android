package connect.ui.activity.chat.set;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import connect.db.green.DaoHelper.ContactHelper;
import connect.db.green.bean.GroupEntity;
import connect.ui.activity.R;
import connect.ui.base.BaseActivity;
import connect.utils.ActivityUtil;
import connect.utils.UriUtil;
import connect.utils.glide.GlideUtil;
import connect.utils.okhttp.OkHttpUtil;
import connect.utils.okhttp.ResultCall;
import connect.view.TopToolBar;
import protos.Connect;

public class GroupManageActivity extends BaseActivity {

    @Bind(R.id.toolbar)
    TopToolBar toolbar;

    private static String GROUP_KEY = "GROUP_KEY";
    private GroupManageActivity activity;

    private String groupKey = null;
    private GroupEntity groupEntity=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_manage);
        ButterKnife.bind(this);
        initView();
    }

    public static void startActivity(Activity activity, String groupkey) {
        Bundle bundle = new Bundle();
        bundle.putString(GROUP_KEY, groupkey);
        ActivityUtil.next(activity, GroupManageActivity.class, bundle);
    }

    @Override
    public void initView() {
        activity = this;
        toolbar.setBlackStyle();
        toolbar.setLeftImg(R.mipmap.back_white);
        toolbar.setTitle(getResources().getString(R.string.Link_ManageGroup));
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

        boolean verify = (groupEntity.getVerify() != null) && (1 == groupEntity.getVerify());
        seSwitchLayout(findViewById(R.id.groupset_sureinvite), getString(R.string.Link_Whether_Public), verify);
        groupManage(findViewById(R.id.groupset_introdue), getString(R.string.Link_Group_Introduction));
        groupManage(findViewById(R.id.groupset_transferto), getString(R.string.Link_Ownership_Transfer));
    }

    protected void seSwitchLayout(View view, String name, boolean state) {
        TextView txt = (TextView) view.findViewById(R.id.txt);
        txt.setText(name);

        View topToggle = view.findViewById(R.id.toggle);
        topToggle.setSelected(state);
        topToggle.setTag(name);
        topToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setSelected(!v.isSelected());
                groupSetting(groupKey,v.isSelected());
            }
        });
    }

    protected void setCheckSwitch(boolean check) {
        View topToggle = findViewById(R.id.groupset_sureinvite).findViewById(R.id.toggle);
        topToggle.setSelected(check);
    }

    protected void groupManage(View view, String title) {
        TextView txt1 = (TextView) view.findViewById(R.id.txt1);
        ImageView img1 = (ImageView) view.findViewById(R.id.img1);

        txt1.setText(title);
        img1.setBackgroundResource(R.mipmap.app_right_arrow);
        view.setTag(title);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = (String) v.getTag();
                if (getString(R.string.Link_Group_Introduction).equals(name)) {
                    GroupIntroduceActivity.startActivity(activity, groupKey);
                } else if (getString(R.string.Link_Ownership_Transfer).equals(name)) {
                    GroupOwnerToActivity.startActivity(activity, groupKey);
                }
            }
        });
    }

    protected void groupSetting(final String groupKey, final boolean verify) {
        Connect.GroupSetting setting = Connect.GroupSetting.newBuilder()
                .setIdentifier(groupKey)
                .setPublic(verify).build();
        OkHttpUtil.getInstance().postEncrySelf(UriUtil.GROUP_SETTING, setting, new ResultCall<Connect.HttpResponse>() {

            @Override
            public void onResponse(Connect.HttpResponse response) {
                GroupEntity groupEntity = ContactHelper.getInstance().loadGroupEntity(groupKey);
                if (!(groupEntity == null || TextUtils.isEmpty(groupEntity.getEcdh_key()))) {
                    groupEntity.setVerify(verify ? 1 : 0);

                    String groupName = groupEntity.getName();
                    if (TextUtils.isEmpty(groupName)) {
                        groupName = "groupname6";
                    }
                    groupEntity.setName(groupName);
                    ContactHelper.getInstance().inserGroupEntity(groupEntity);
                }
            }

            @Override
            public void onError(Connect.HttpResponse response) {
                setCheckSwitch(!verify);
            }
        });
    }
}
