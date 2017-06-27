package connect.ui.activity.chat.set;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.util.Collections;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import connect.db.MemoryDataManager;
import connect.db.green.DaoHelper.ContactHelper;
import connect.db.green.bean.GroupMemberEntity;
import connect.ui.activity.R;
import connect.ui.activity.chat.model.GroupComPara;
import connect.ui.activity.home.view.LineDecoration;
import connect.ui.activity.chat.adapter.GroupMemberAdapter;
import connect.ui.base.BaseActivity;
import connect.utils.ActivityUtil;
import connect.view.SideBar;
import connect.view.TopToolBar;

/**
 * remove Group member
 * Created by gtq on 2016/12/15.
 */
public class GroupMemberActivity extends BaseActivity {

    @Bind(R.id.toolbar_top)
    TopToolBar toolbarTop;
    @Bind(R.id.recordview)
    RecyclerView recordview;

    private static String GROUP_KEY = "GROUP_KEY";
    @Bind(R.id.siderbar)
    SideBar siderbar;
    private String groupKey;

    private GroupComPara groupComPara = new GroupComPara();

    private GroupMemberActivity activity;
    private LinearLayoutManager layoutManager;
    private GroupMemberAdapter adapter;

    private List<GroupMemberEntity> memEntities;

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
                ContactSelectActivity.startInviteGroupActivity(activity, groupKey);
            }
        });

        groupKey = getIntent().getStringExtra(GROUP_KEY);
        GroupMemberEntity myMember = ContactHelper.getInstance().loadGroupMemByAds(groupKey, MemoryDataManager.getInstance().getAddress());

        layoutManager = new LinearLayoutManager(activity);
        recordview.setLayoutManager(layoutManager);
        adapter = new GroupMemberAdapter(activity, recordview);
        adapter.setCanScroll(myMember.getRole() == 1);
        recordview.setAdapter(adapter);
        recordview.addItemDecoration(new LineDecoration(activity));
        recordview.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                adapter.closeMenu();
            }
        });

        memEntities = ContactHelper.getInstance().loadGroupMemEntity(groupKey);
        Collections.sort(memEntities, groupComPara);
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
}
