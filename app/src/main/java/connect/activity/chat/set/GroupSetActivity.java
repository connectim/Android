package connect.activity.chat.set;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import connect.activity.base.BaseActivity;
import connect.activity.chat.SearchContentActivity;
import connect.activity.chat.bean.RecExtBean;
import connect.activity.chat.set.contract.GroupSetContract;
import connect.activity.chat.set.presenter.GroupSetPresenter;
import connect.activity.contact.ContactInfoActivity;
import connect.activity.contact.bean.ContactNotice;
import connect.activity.set.UserInfoActivity;
import connect.database.SharedPreferenceUtil;
import connect.database.green.DaoHelper.ConversionHelper;
import connect.database.green.bean.ConversionEntity;
import connect.ui.activity.R;
import connect.utils.ActivityUtil;
import connect.utils.dialog.DialogUtil;
import connect.widget.TopToolBar;

/**
 * group setting
 * Created by gtq on 2016/12/15.
 */
public class GroupSetActivity extends BaseActivity implements GroupSetContract.BView {

    @Bind(R.id.toolbar)
    TopToolBar toolbar;
    @Bind(R.id.linearlayout)
    LinearLayout linearlayout;
    @Bind(R.id.relativelayout_1)
    RelativeLayout relativelayout1;

    private GroupSetActivity activity;
    private static String TAG = "_GroupSetActivity";
    private static String GROUP_IDENTIFY = "GROUP_IDENTIFY";
    private String groupKey;
    private GroupSetContract.Presenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_groupset);
        ButterKnife.bind(this);
        initView();
    }

    public static void startActivity(Activity activity, String groupkey) {
        Bundle bundle = new Bundle();
        bundle.putString(GROUP_IDENTIFY, groupkey);
        ActivityUtil.next(activity, GroupSetActivity.class, bundle);
    }

    @Override
    public void initView() {
        activity = this;
        toolbar.setBlackStyle();
        toolbar.setLeftImg(R.mipmap.back_white);
        toolbar.setLeftListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityUtil.goBack(activity);
            }
        });

        groupKey = getIntent().getStringExtra(GROUP_IDENTIFY);
        new GroupSetPresenter(this).start();
        presenter.syncGroupInfo();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(ContactNotice contactNotice) {
        switch (contactNotice.getNotice()) {
            case RecGroup:
                initView();
                break;
        }
    }

    @OnClick(R.id.relativelayout_1)
    public void memberLayoutClickListener() {
        GroupMemberActivity.startActivity(activity, groupKey);
    }

    @Override
    public void setPresenter(GroupSetContract.Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public Activity getActivity() {
        return activity;
    }

    @Override
    public String getRoomKey() {
        return groupKey;
    }

    @Override
    public void countMember(int members) {
        toolbar.setTitle(getResources().getString(R.string.Chat_Group_Setting,members));
    }

    @Override
    public void memberList(View view) {
        LinearLayout layout = (LinearLayout) findViewById(R.id.linearlayout);
        layout.addView(view);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String uid = (String) v.getTag();
                if ("GROUP_ADD".equals(uid)) {
                    BaseGroupSelectActivity.startActivity(activity, false, groupKey);
                } else {
                    if (SharedPreferenceUtil.getInstance().getUser().getUid().equals(uid)) {
                        UserInfoActivity.startActivity(activity);
                    } else {
                        ContactInfoActivity.lunchActivity(activity, uid);
                    }
                }
            }
        });
    }

    @Override
    public void searchGroupHistoryTxt() {
        View view = findViewById(R.id.groupset_searchhistory);

        TextView searchTxt = (TextView) view.findViewById(R.id.txt1);
        ImageView imageView = (ImageView) view.findViewById(R.id.img1);

        searchTxt.setText(getResources().getString(R.string.Chat_Search_Txt));
        imageView.setImageResource(R.mipmap.group_item_arrow);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SearchContentActivity.lunchActivity(activity, 3);
            }
        });
    }

    @Override
    public void groupName(String groupname) {
        View view = findViewById(R.id.groupset_groupname);
        TextView txt1 = (TextView) view.findViewById(R.id.txt1);
        TextView txt2 = (TextView) view.findViewById(R.id.txt2);

        txt1.setText(getString(R.string.Link_Group_Name));
        if (!TextUtils.isEmpty(groupname)) {
            if (groupname.length() > 10) {
                groupname = groupname.substring(0, 10) + "...";
            }
            txt2.setText(groupname);
        }

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GroupNameActivity.startActivity(activity, groupKey);
            }
        });
    }

    @Override
    public void groupNameClickable(boolean clickable) {
        View view = findViewById(R.id.groupset_groupname);
        view.setEnabled(clickable);
    }

    @Override
    public void topSwitch(boolean top) {
        View view = findViewById(R.id.top);
        TextView txt = (TextView) view.findViewById(R.id.txt);
        txt.setText(getResources().getString(R.string.Chat_Sticky_on_Top_chat));

        View topToggle = view.findViewById(R.id.toggle);
        topToggle.setSelected(top);
        topToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setSelected(!v.isSelected());
                int top = v.isSelected() ? 1 : 0;
                ConversionEntity conversionEntity = ConversionHelper.getInstance().loadRoomEnitity(groupKey);
                if (conversionEntity == null) {
                    conversionEntity = new ConversionEntity();
                    conversionEntity.setIdentifier(groupKey);
                }
                conversionEntity.setTop(top);
                ConversionHelper.getInstance().insertRoomEntity(conversionEntity);
            }
        });
    }

    @Override
    public void noticeSwitch(boolean notice) {
        View view = findViewById(R.id.mute);
        TextView txt = (TextView) view.findViewById(R.id.txt);
        txt.setText(getResources().getString(R.string.Chat_Mute_Notification));

        View topToggle = view.findViewById(R.id.toggle);
        topToggle.setSelected(notice);
        topToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setSelected(!v.isSelected());
                boolean mutestate = v.isSelected();
                presenter.updateGroupMute(mutestate);
            }
        });
    }

    @Override
    public void commonSwtich(boolean common) {
        View view = findViewById(R.id.save);
        TextView txt = (TextView) view.findViewById(R.id.txt);
        txt.setText(getResources().getString(R.string.Link_Save_to_Contacts));

        View topToggle = view.findViewById(R.id.toggle);
        topToggle.setSelected(common);
        topToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setSelected(!v.isSelected());
                boolean common = v.isSelected();
                presenter.updateGroupCommon(common);
            }
        });
    }

    @Override
    public void clearHistory() {
        View view = findViewById(R.id.clear);
        ImageView img = (ImageView) view.findViewById(R.id.img);
        TextView txt = (TextView) view.findViewById(R.id.groupset_clear_history);
        txt.setText(getResources().getString(R.string.Link_Clear_Chat_History));
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<String> strings = new ArrayList();
                strings.add(getString(R.string.Link_Clear_Chat_History));
                DialogUtil.showBottomView(activity, strings, new DialogUtil.DialogListItemClickListener() {
                    @Override
                    public void confirm(int position) {
                        ConversionHelper.getInstance().deleteRoom(groupKey);
                        RecExtBean.getInstance().sendEvent(RecExtBean.ExtType.CLEAR_HISTORY, groupKey);
                    }
                });
            }
        });
    }

    @Override
    public void exitGroup() {
        Button view = (Button) findViewById(R.id.groupset_exit_group);
        view.setText(getResources().getString(R.string.Link_Delete_and_Leave));
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.requestExitGroup();
            }
        });
    }
}
