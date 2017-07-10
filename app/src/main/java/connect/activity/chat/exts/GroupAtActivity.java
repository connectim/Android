package connect.activity.chat.exts;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.util.Collections;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import connect.database.MemoryDataManager;
import connect.database.green.DaoHelper.ContactHelper;
import connect.database.green.bean.GroupMemberEntity;
import connect.ui.activity.R;
import connect.activity.chat.bean.RecExtBean;
import connect.activity.chat.model.GroupMemberCompara;
import connect.activity.chat.adapter.GroupMemberSelectAdapter;
import connect.activity.base.BaseActivity;
import connect.utils.ActivityUtil;
import connect.widget.SideBar;
import connect.widget.TopToolBar;

/**
 * group At ,select group member
 */
public class GroupAtActivity extends BaseActivity {

    @Bind(R.id.toolbar)
    TopToolBar toolbar;
    @Bind(R.id.recyclerview)
    RecyclerView recyclerview;
    @Bind(R.id.siderbar)
    SideBar siderbar;

    private String Tag = "GroupTransferToActivity";

    private GroupAtActivity activity;

    private String groupKey = null;
    private boolean move;
    private int topPosi;
    private LinearLayoutManager linearLayoutManager;
    private GroupMemberSelectAdapter adapter;

    private GroupMemberCompara compara = new GroupMemberCompara();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_at);
        ButterKnife.bind(this);
        initView();
    }

    public static void startActivity(Activity activity, String groupkey) {
        Bundle bundle = new Bundle();
        bundle.putString("GROUP_KEY", groupkey);
        ActivityUtil.next(activity, GroupAtActivity.class, bundle);
    }

    @Override
    public void initView() {
        activity = this;
        toolbar.setBlackStyle();
        toolbar.setLeftImg(R.mipmap.back_white);
        toolbar.setTitle(getResources().getString(R.string.Chat_Choose_Members));
        toolbar.setLeftListence(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityUtil.goBack(activity);
            }
        });

        groupKey = getIntent().getStringExtra("GROUP_KEY");
        linearLayoutManager = new LinearLayoutManager(activity);
        List<GroupMemberEntity> groupMemEntities = ContactHelper.getInstance().loadGroupMemEntity(groupKey, MemoryDataManager.getInstance().getAddress());
        Collections.sort(groupMemEntities, compara);

        adapter = new GroupMemberSelectAdapter(activity, groupMemEntities);
        recyclerview.setLayoutManager(linearLayoutManager);
        recyclerview.setAdapter(adapter);
        recyclerview.addOnScrollListener(new RecyclerView.OnScrollListener() {
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
        });
        adapter.setTransferToListener(new GroupMemberSelectAdapter.GroupTransferToListener() {
            @Override
            public void transferTo(GroupMemberEntity memEntity) {
                groupAtMember(memEntity);
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
        this.topPosi = posi;
        int firstItem = linearLayoutManager.findFirstVisibleItemPosition();
        int lastItem = linearLayoutManager.findLastVisibleItemPosition();
        if (posi <= firstItem) {
            recyclerview.scrollToPosition(posi);
        } else if (posi <= lastItem) {
            int top = recyclerview.getChildAt(posi - firstItem).getTop();
            recyclerview.scrollBy(0, top);
        } else {
            recyclerview.scrollToPosition(posi);
            move = true;
        }
    }

    public void groupAtMember(GroupMemberEntity groupMemEntity) {
        RecExtBean.sendRecExtMsg(RecExtBean.ExtType.GROUP_AT, groupMemEntity);
        ActivityUtil.goBack(activity);
    }
}
