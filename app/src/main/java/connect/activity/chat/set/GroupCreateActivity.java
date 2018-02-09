package connect.activity.chat.set;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import connect.activity.base.BaseActivity;
import connect.activity.chat.adapter.GroupCreateAdapter;
import connect.activity.chat.set.contract.GroupCreateContract;
import connect.activity.chat.set.presenter.GroupCreatePresenter;
import connect.activity.home.view.LineDecoration;
import connect.activity.login.bean.UserBean;
import connect.database.SharedPreferenceUtil;
import connect.ui.activity.R;
import connect.utils.ActivityUtil;
import connect.widget.TopToolBar;
import protos.Connect;

public class GroupCreateActivity extends BaseActivity implements GroupCreateContract.BView {

    @Bind(R.id.toolbar)
    TopToolBar toolbar;
    @Bind(R.id.edittxt1)
    EditText edittxt1;
    @Bind(R.id.recyclerview)
    RecyclerView recyclerview;

    private static String CONTACT_LIST = "CONTACT_LIST";
    private GroupCreateActivity activity;
    boolean isCreate = true;
    private List<Connect.Workmate> workmates;
    private GroupCreateContract.Presenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_create);
        ButterKnife.bind(this);
        initView();
    }

    public static void startActivity(Activity activity, boolean isCreate, ArrayList<Connect.Workmate> workmates) {
        Bundle bundle = new Bundle();
        bundle.putBoolean("Is_Create", isCreate);
        bundle.putSerializable(CONTACT_LIST, workmates);
        ActivityUtil.next(activity, GroupCreateActivity.class, bundle);
    }

    @Override
    public void initView() {
        activity = this;
        toolbar.setBlackStyle();
        toolbar.setLeftImg(R.mipmap.back_white);
        toolbar.setTitle(getResources().getString(R.string.Chat_set_Create_New_Group));
        toolbar.setRightText(R.string.Chat_Complete);
        toolbar.setRightTextEnable(true);
        toolbar.setLeftListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityUtil.goBack(activity);
            }
        });
        toolbar.setRightListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toolbar.setRightTextEnable(false);

                String groupName = edittxt1.getText().toString();
                if (TextUtils.isEmpty(groupName)) {
                    groupName = edittxt1.getHint().toString();
                }
                if(isCreate){
                    presenter.createGroup(groupName);
                }

                Message message = new Message();
                message.what = 100;
                handler.sendMessageDelayed(message, 3000);
            }
        });

        UserBean userBean = SharedPreferenceUtil.getInstance().getUser();
        edittxt1.setHint(String.format(activity.getString(R.string.Link_user_friends), userBean.getName()));

        isCreate = getIntent().getBooleanExtra("Is_Create", true);
        workmates = (List<Connect.Workmate>) getIntent().getSerializableExtra(CONTACT_LIST);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(activity);
        recyclerview.setLayoutManager(linearLayoutManager);
        recyclerview.addItemDecoration(new LineDecoration(activity));
        GroupCreateAdapter adapter = new GroupCreateAdapter();
        adapter.setData(workmates);
        recyclerview.setAdapter(adapter);

        new GroupCreatePresenter(this).start();
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 100:
                    toolbar.setRightTextEnable(true);
                    break;
            }
        }
    };

    @Override
    public List<Connect.Workmate> groupMemberList() {
        return workmates;
    }

    @Override
    public void setLeftEnanle(boolean b) {
        toolbar.setLeftEnable(b);
    }

    @Override
    public void setPresenter(GroupCreateContract.Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public Activity getActivity() {
        return activity;
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return false;
    }
}
