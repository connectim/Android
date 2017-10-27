package connect.activity.chat.exts;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import connect.activity.base.BaseActivity;
import connect.activity.chat.adapter.GroupAtAdapter;
import connect.activity.chat.bean.RecExtBean;
import connect.activity.chat.exts.contract.GroupAtContract;
import connect.activity.chat.exts.presenter.GroupAtPresenter;
import connect.activity.chat.model.GroupMemberCompara;
import connect.database.SharedPreferenceUtil;
import connect.database.green.DaoHelper.ContactHelper;
import connect.database.green.bean.GroupMemberEntity;
import connect.ui.activity.R;
import connect.utils.ActivityUtil;
import connect.widget.SideBar;
import connect.widget.TopToolBar;

/**
 * group At ,select group member
 */
public class GroupAtActivity extends BaseActivity implements GroupAtContract.BView {

    @Bind(R.id.toolbar)
    TopToolBar toolbar;
    @Bind(R.id.recyclerview)
    RecyclerView recyclerview;
    @Bind(R.id.siderbar)
    SideBar siderbar;

    private GroupAtActivity activity;
    private static String TAG = "_GroupAtActivity";
    private static String GROUP_IDENTIFY = "GROUP_IDENTIFY";
    private String groupKey = null;
    private boolean move;
    private int topPosi;

    private GroupAtOnscrollListener onscrollListener = new GroupAtOnscrollListener();
    private GroupAtLetterChanged letterChanged = new GroupAtLetterChanged();
    private LinearLayoutManager linearLayoutManager;
    private GroupAtAdapter adapter;
    private GroupAtContract.Presenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_at);
        ButterKnife.bind(this);
        initView();
    }

    public static void startActivity(Activity activity, String groupkey) {
        Bundle bundle = new Bundle();
        bundle.putString(GROUP_IDENTIFY, groupkey);
        ActivityUtil.next(activity, GroupAtActivity.class, bundle);
    }

    @Override
    public void initView() {
        activity = this;
        toolbar.setBlackStyle();
        toolbar.setLeftImg(R.mipmap.back_white);
        toolbar.setTitle(getResources().getString(R.string.Chat_Choose_Members));
        toolbar.setLeftListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityUtil.goBack(activity);
            }
        });

        groupKey = getIntent().getStringExtra(GROUP_IDENTIFY);

        String myPublicKey = SharedPreferenceUtil.getInstance().getUser().getPubKey();
        List<GroupMemberEntity> groupMemEntities = ContactHelper.getInstance().loadGroupMemEntities(groupKey);
        Iterator<GroupMemberEntity> iterator = groupMemEntities.iterator();
        while (iterator.hasNext()) {
            GroupMemberEntity memberEntity = iterator.next();
            if (memberEntity.getUid().equals(myPublicKey)) {
                iterator.remove();
            }
        }
        Collections.sort(groupMemEntities, new GroupMemberCompara());

        linearLayoutManager = new LinearLayoutManager(activity);
        adapter = new GroupAtAdapter(activity, groupMemEntities);
        recyclerview.setLayoutManager(linearLayoutManager);
        recyclerview.setAdapter(adapter);
        recyclerview.addOnScrollListener(onscrollListener);
        adapter.setGroupAtListener(groupAtListener);
        siderbar.setOnTouchingLetterChangedListener(letterChanged);
        new GroupAtPresenter(this).start();
    }

    private class GroupAtOnscrollListener extends RecyclerView.OnScrollListener {

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

    private GroupAtAdapter.GroupAtListener groupAtListener = new GroupAtAdapter.GroupAtListener() {

        @Override
        public void groupAt(GroupMemberEntity memEntity) {
            RecExtBean.getInstance().sendEvent(RecExtBean.ExtType.GROUP_AT, memEntity);
            ActivityUtil.goBack(activity);
        }
    };

    private class GroupAtLetterChanged implements SideBar.OnTouchingLetterChangedListener {

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
    public String getGroupKey() {
        return groupKey;
    }

    @Override
    public void setPresenter(GroupAtContract.Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public Activity getActivity() {
        return activity;
    }
}
