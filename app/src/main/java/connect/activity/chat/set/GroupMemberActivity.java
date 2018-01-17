package connect.activity.chat.set;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.util.Collections;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import connect.activity.base.BaseActivity;
import connect.activity.base.compare.GroupComPara;
import connect.activity.chat.adapter.GroupMemberAdapter;
import connect.activity.chat.set.contract.GroupMemberContract;
import connect.activity.chat.set.presenter.GroupMemberPresenter;
import connect.activity.home.view.LineDecoration;
import connect.database.SharedPreferenceUtil;
import connect.database.green.DaoHelper.ContactHelper;
import connect.database.green.bean.GroupMemberEntity;
import connect.ui.activity.R;
import connect.utils.ActivityUtil;
import connect.widget.SideBar;
import connect.widget.TopToolBar;

/**
 * Group member list(remove member)
 * Created by gtq on 2016/12/15.
 */
public class GroupMemberActivity extends BaseActivity implements GroupMemberContract.BView {

    @Bind(R.id.toolbar_top)
    TopToolBar toolbarTop;
    @Bind(R.id.recordview)
    RecyclerView recordview;
    @Bind(R.id.siderbar)
    SideBar siderbar;
    private String groupKey;

    private GroupMemberActivity activity;
    private static String TAG = "_GroupMemberActivity";
    private static String GROUP_IDENTIFY = "GROUP_IDENTIFY";

    private GroupMemberOnscrollListener onscrollListener = new GroupMemberOnscrollListener();
    private GroupMemberLetterChanged letterChanged = new GroupMemberLetterChanged();
    private LinearLayoutManager layoutManager;
    private GroupMemberContract.Presenter presenter;
    private GroupMemberAdapter memberAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_groupmemre);
        ButterKnife.bind(this);
        initView();
    }

    public static void startActivity(Activity activity, String groupkey) {
        Bundle bundle = new Bundle();
        bundle.putString(GROUP_IDENTIFY, groupkey);
        ActivityUtil.next(activity, GroupMemberActivity.class, bundle);
    }

    @Override
    public void initView() {
        activity = this;
        toolbarTop.setBlackStyle();
        toolbarTop.setLeftImg(R.mipmap.back_white);
        toolbarTop.setRightText(R.string.Link_Invite);
        toolbarTop.setLeftListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityUtil.goBack(activity);
            }
        });
        toolbarTop.setRightListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BaseGroupSelectActivity.startActivity(activity, false, groupKey);
            }
        });

        groupKey = getIntent().getStringExtra(GROUP_IDENTIFY);
        String myUid = SharedPreferenceUtil.getInstance().getUser().getUid();
        GroupMemberEntity myMember = ContactHelper.getInstance().loadGroupMemberEntity(groupKey, myUid);

        layoutManager = new LinearLayoutManager(activity);
        recordview.setLayoutManager(layoutManager);
        memberAdapter = new GroupMemberAdapter(activity, recordview);

        boolean canScroll = myMember != null && myMember.getRole() == 1;
        memberAdapter.setCanScroll(canScroll);
        recordview.setAdapter(memberAdapter);
        recordview.addItemDecoration(new LineDecoration(activity));
        recordview.addOnScrollListener(onscrollListener);

        final List<GroupMemberEntity> memEntities = ContactHelper.getInstance().loadGroupMemberEntities(groupKey);
        Collections.sort(memEntities, new GroupComPara());
        toolbarTop.setTitle(getString(R.string.Chat_Group_Members, memEntities.size()));

        memberAdapter.setData(memEntities);
        memberAdapter.setItemRemoveListener(new GroupMemberAdapter.OnItemRemoveListener() {
            @Override
            public void itemRemove(GroupMemberEntity entity) {
                memEntities.remove(entity);
                GroupSetActivity.startActivity(activity, groupKey);
            }
        });

        siderbar.setOnTouchingLetterChangedListener(letterChanged);
        new GroupMemberPresenter(this).start();
    }

    private class GroupMemberOnscrollListener extends RecyclerView.OnScrollListener {

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            memberAdapter.closeMenu();
        }
    }

    private class GroupMemberLetterChanged implements SideBar.OnTouchingLetterChangedListener {

        @Override
        public void onTouchingLetterChanged(String s) {
            int position = memberAdapter.getPositionForSection(s.charAt(0));

            int firstItem = layoutManager.findFirstVisibleItemPosition();
            int lastItem = layoutManager.findLastVisibleItemPosition();
            if (position <= firstItem) {
                recordview.scrollToPosition(position);
            } else if (position <= lastItem) {
                int top = recordview.getChildAt(position - firstItem).getTop();
                recordview.scrollBy(0, top);
            } else {
                recordview.scrollToPosition(position);
            }
        }
    }

    @Override
    public String getRoomKey() {
        return groupKey;
    }

    @Override
    public void setPresenter(GroupMemberContract.Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public Activity getActivity() {
        return activity;
    }
}
