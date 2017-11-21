package connect.activity.chat.set;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import connect.activity.base.BaseActivity;
import connect.activity.base.compare.FriendCompara;
import connect.activity.chat.set.contract.GroupInviteContract;
import connect.activity.chat.set.presenter.GroupInvitePresenter;
import connect.database.green.DaoHelper.ContactHelper;
import connect.database.green.bean.ContactEntity;
import connect.database.green.bean.GroupMemberEntity;
import connect.ui.activity.R;
import connect.utils.ActivityUtil;
import connect.widget.SideBar;
import connect.widget.TopToolBar;
import connect.widget.selefriend.adapter.SelectFriendAdapter;

public class GroupInviteActivity extends BaseActivity implements GroupInviteContract.BView{

    @Bind(R.id.toolbar)
    TopToolBar toolbar;
    @Bind(R.id.recyclerview)
    RecyclerView recyclerview;
    @Bind(R.id.siderbar)
    SideBar siderbar;

    private GroupInviteActivity activity;
    private static String TAG = "_GroupInviteActivity";
    private static String GROUP_IDENTIFY = "GROUP_IDENTIFY";
    private boolean move;
    private int topPosi;
    private String groupKey;
    private List<String> oldMemberUids = new ArrayList<>();

    private GroupInviteOnscrollListener onscrollListener = new GroupInviteOnscrollListener();
    private GroupInviteFriendSelectListener friendSelectListener = new GroupInviteFriendSelectListener();
    private GroupInviteLetterChanged letterChanged = new GroupInviteLetterChanged();
    private LinearLayoutManager linearLayoutManager;
    private GroupInviteContract.Presenter presenter;
    private SelectFriendAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_invite);
        ButterKnife.bind(this);
        initView();
    }

    public static void startActivity(Activity activity, String groupkey) {
        Bundle bundle = new Bundle();
        bundle.putString(GROUP_IDENTIFY, groupkey);
        ActivityUtil.next(activity, GroupInviteActivity.class, bundle);
    }

    @Override
    public void initView() {
        activity = this;
        toolbar.setBlackStyle();
        toolbar.setLeftImg(R.mipmap.back_white);
        toolbar.setTitle(getResources().getString(R.string.Chat_Choose_contact));
        toolbar.setRightText(R.string.Chat_Complete);
        toolbar.setRightTextEnable(false);

        toolbar.setLeftListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityUtil.goBack(activity);
            }
        });
        toolbar.setRightListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<ContactEntity> selectEntities = adapter.getSelectList();
                if (selectEntities == null || selectEntities.size() < 1) {
                    toolbar.setRightTextEnable(false);
                    return;
                }

                Iterator<ContactEntity> it = selectEntities.iterator();
                while (it.hasNext()) {
                    ContactEntity contact = it.next();
                    if (oldMemberUids.contains(contact.getUid())) {
                        it.remove();
                    }
                }

                presenter.requestGroupMemberInvite(selectEntities);

                toolbar.setRightTextEnable(false);
                Message message = new Message();
                message.what = 100;
                handler.sendMessageDelayed(message, 3000);
            }
        });

        groupKey = getIntent().getStringExtra(GROUP_IDENTIFY);
        List<GroupMemberEntity> memberEntities = ContactHelper.getInstance().loadGroupMemEntities(groupKey);
        for (GroupMemberEntity memEntity : memberEntities) {
            oldMemberUids.add(memEntity.getUid());
        }
        List<ContactEntity> friendEntities = ContactHelper.getInstance().loadFriend();
        Collections.sort(friendEntities, new FriendCompara());

        linearLayoutManager = new LinearLayoutManager(activity);
        adapter = new SelectFriendAdapter(activity);
        recyclerview.setLayoutManager(linearLayoutManager);
        recyclerview.setAdapter(adapter);

        recyclerview.addOnScrollListener(onscrollListener);
        adapter.setOnSelectFriendListener(friendSelectListener);
        siderbar.setOnTouchingLetterChangedListener(letterChanged);
        adapter.setDataNotify(friendEntities, null, oldMemberUids);
        new GroupInvitePresenter(this).start();
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

    private class GroupInviteOnscrollListener extends RecyclerView.OnScrollListener{

        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            if (move) {
                move = false;
                int n = topPosi - linearLayoutManager.findFirstVisibleItemPosition();
                if (0 <= n && n < recyclerview.getChildCount()) {
                    int top = recyclerview.getChildAt(n).getTop();
                    recyclerview.scrollBy(0, top);
                }
            }
        }
    }

    private class GroupInviteFriendSelectListener implements SelectFriendAdapter.OnSelectFriendListener{
        @Override
        public void selectFriend(List<String> list) {
            if (list == null || list.size() <= 0) {
                toolbar.setRightTextEnable(false);
                toolbar.setRightTextColor(R.color.color_6d6e75);
            } else {
                toolbar.setRightTextEnable(true);
                toolbar.setRightTextColor(R.color.color_green);
            }
        }
    }

    private class GroupInviteLetterChanged implements SideBar.OnTouchingLetterChangedListener{

        @Override
        public void onTouchingLetterChanged(String s) {
            int position = adapter.getPositionForSection(s.charAt(0));

            topPosi = position;
            int firstItem = linearLayoutManager.findFirstVisibleItemPosition();
            int lastItem = linearLayoutManager.findLastVisibleItemPosition();
            if (position <= firstItem) {
                recyclerview.scrollToPosition(position);
            } else if (position <= lastItem) {
                int top = recyclerview.getChildAt(position - firstItem).getTop();
                recyclerview.scrollBy(0, top);
            } else {
                recyclerview.scrollToPosition(position);
                move = true;
            }
        }
    }

    @Override
    public String getRoomKey() {
        return groupKey;
    }

    @Override
    public void setPresenter(GroupInviteContract.Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public Activity getActivity() {
        return activity;
    }
}
