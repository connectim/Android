package connect.activity.chat.set;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import connect.activity.chat.set.contract.GroupManagerContract;
import connect.activity.chat.set.presenter.GroupManagerPresenter;
import connect.database.green.DaoHelper.ContactHelper;
import connect.database.green.bean.GroupEntity;
import connect.ui.activity.R;
import connect.activity.base.BaseActivity;
import connect.utils.ActivityUtil;
import connect.utils.UriUtil;
import connect.utils.okhttp.OkHttpUtil;
import connect.utils.okhttp.ResultCall;
import connect.widget.TopToolBar;
import protos.Connect;

public class GroupManageActivity extends BaseActivity implements GroupManagerContract.BView{

    @Bind(R.id.toolbar)
    TopToolBar toolbar;

    private static String GROUP_KEY = "GROUP_KEY";
    private GroupManageActivity activity;

    private String groupKey = null;
    private GroupManagerContract.Presenter presenter;

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
        new GroupManagerPresenter(this).start();
    }

    @Override
    public String getRoomKey() {
        return groupKey;
    }

    @Override
    public void setPresenter(GroupManagerContract.Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public Activity getActivity() {
        return activity;
    }

    @Override
    public void inviteSwitch(boolean avaliable) {
        View view = findViewById(R.id.groupset_sureinvite);
        TextView txt = (TextView) view.findViewById(R.id.txt);
        txt.setText(getString(R.string.Link_Whether_Public));

        View topToggle = view.findViewById(R.id.toggle);
        topToggle.setSelected(avaliable);
        topToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setSelected(!v.isSelected());
                boolean verify = v.isSelected();
                presenter.requestGroupVerify(verify);
            }
        });
    }

    @Override
    public void groupIntroduce() {
        View view = findViewById(R.id.groupset_introdue);
        TextView txt1 = (TextView) view.findViewById(R.id.txt1);
        ImageView img1 = (ImageView) view.findViewById(R.id.img1);

        txt1.setText(getString(R.string.Link_Group_Introduction));
        img1.setBackgroundResource(R.mipmap.app_right_arrow);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GroupIntroduceActivity.startActivity(activity, groupKey);
            }
        });
    }

    @Override
    public void groupNewOwner() {
        View view = findViewById(R.id.groupset_transferto);
        TextView txt1 = (TextView) view.findViewById(R.id.txt1);
        ImageView img1 = (ImageView) view.findViewById(R.id.img1);

        txt1.setText(getString(R.string.Link_Ownership_Transfer));
        img1.setBackgroundResource(R.mipmap.app_right_arrow);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GroupOwnerToActivity.startActivity(activity, groupKey);
            }
        });
    }
}
