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
import connect.activity.chat.set.contract.GroupMemberContract;
import connect.activity.chat.set.presenter.GroupMemberPresenter;
import connect.database.MemoryDataManager;
import connect.database.green.DaoHelper.ContactHelper;
import connect.database.green.bean.GroupMemberEntity;
import connect.ui.activity.R;
import connect.activity.chat.model.GroupComPara;
import connect.activity.home.view.LineDecoration;
import connect.activity.chat.adapter.GroupMemberAdapter;
import connect.activity.base.BaseActivity;
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

    private static String GROUP_KEY = "GROUP_KEY";
    @Bind(R.id.siderbar)
    SideBar siderbar;
    private String groupKey;

    private GroupMemberActivity activity;
    private LinearLayoutManager layoutManager;
    private GroupMemberContract.Presenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_groupmemre);
        ButterKnife.bind(this);
        initView();
    }

    public static void startActivity(Activity activity, String groupkey) {
        Bundle bundle = new Bundle();
        bundle.putString(GROUP_KEY, groupkey);
        ActivityUtil.next(activity, GroupMemberActivity.class, bundle);
    }

    @Override
    public void initView() {
        activity = this;
        toolbarTop.setBlackStyle();
        toolbarTop.setLeftImg(R.mipmap.back_white);
        toolbarTop.setRightText(R.string.Link_Invite);
        toolbarTop.setRightTextColor(R.color.color_white);
        toolbarTop.setLeftListence(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityUtil.goBack(activity);
            }
        });
        toolbarTop.setRightListence(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GroupInviteActivity.startActivity(activity, groupKey);
            }
        });

        groupKey = getIntent().getStringExtra(GROUP_KEY);
        // qwert
        // GroupMemberEntity myMember = ContactHelper.getInstance().loadGroupMemberEntity(groupKey, MemoryDataManager.getInstance().getAddress());

        layoutManager = new LinearLayoutManager(activity);
        recordview.setLayoutManager(layoutManager);
        final GroupMemberAdapter adapter = new GroupMemberAdapter(activity, recordview);
        // qwert
        // adapter.setCanScroll(myMember.getRole() == 1);
        recordview.setAdapter(adapter);
        recordview.addItemDecoration(new LineDecoration(activity));
        recordview.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                adapter.closeMenu();
            }
        });

        final List<GroupMemberEntity> memEntities = ContactHelper.getInstance().loadGroupMemEntities(groupKey);
        Collections.sort(memEntities, new GroupComPara());
        toolbarTop.setTitle(getString(R.string.Chat_Group_Members, memEntities.size()));

        adapter.setData(memEntities);
        adapter.setItemRemoveListener(new GroupMemberAdapter.OnItemRemoveListener() {
            @Override
            public void itemRemove(GroupMemberEntity entity) {
                memEntities.remove(entity);
                GroupSetActivity.startActivity(activity, groupKey);
            }
        });

        siderbar.setOnTouchingLetterChangedListener(new SideBar.OnTouchingLetterChangedListener() {
            @Override
            public void onTouchingLetterChanged(String s) {
                int position = adapter.getPositionForSection(s.charAt(0));
                moveToPosition(position);
            }
        });

        new GroupMemberPresenter(this).start();
    }

    private void moveToPosition(int posi) {
        int firstItem = layoutManager.findFirstVisibleItemPosition();
        int lastItem = layoutManager.findLastVisibleItemPosition();
        if (posi <= firstItem) {
            recordview.scrollToPosition(posi);
        } else if (posi <= lastItem) {
            int top = recordview.getChildAt(posi - firstItem).getTop();
            recordview.scrollBy(0, top);
        } else {
            recordview.scrollToPosition(posi);
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
