package connect.activity.chat.set;

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
import connect.activity.chat.adapter.GroupMemberSelectAdapter;
import connect.activity.chat.model.GroupMemberCompara;
import connect.activity.chat.set.contract.GroupOwnerContract;
import connect.activity.chat.set.presenter.GroupOwnerPresenter;
import connect.database.SharedPreferenceUtil;
import connect.database.green.DaoHelper.ContactHelper;
import connect.database.green.bean.GroupMemberEntity;
import connect.ui.activity.R;
import connect.utils.ActivityUtil;
import connect.utils.DialogUtil;
import connect.widget.SideBar;
import connect.widget.TopToolBar;

/**
 * Select New Group Owner
 */
public class GroupOwnerToActivity extends BaseActivity implements GroupOwnerContract.BView{

    @Bind(R.id.toolbar)
    TopToolBar toolbar;
    @Bind(R.id.recyclerview)
    RecyclerView recyclerview;
    @Bind(R.id.siderbar)
    SideBar siderbar;

    private GroupOwnerToActivity activity;
    private static String TAG = "_GroupOwnerToActivity";
    private static String GROUP_IDENTIFY = "GROUP_IDENTIFY";
    private String groupKey = null;

    private GroupOwnerToOnscrollListener onscrollListener = new GroupOwnerToOnscrollListener();
    private GroupOwnerTransferToListener transferToListener = new GroupOwnerTransferToListener();
    private GroupOwnerLetterChanged letterChanged = new GroupOwnerLetterChanged();
    private GroupOwnerContract.Presenter presenter;

    private boolean move;
    private int topPosi;
    private LinearLayoutManager linearLayoutManager;
    private GroupMemberSelectAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_ownerto);
        ButterKnife.bind(this);
        initView();
    }

    public static void startActivity(Activity activity, String groupkey) {
        Bundle bundle = new Bundle();
        bundle.putString(GROUP_IDENTIFY, groupkey);
        ActivityUtil.next(activity, GroupOwnerToActivity.class, bundle);
    }

    @Override
    public void initView() {
        activity = this;
        toolbar.setBlackStyle();
        toolbar.setLeftImg(R.mipmap.back_white);
        toolbar.setTitle(getResources().getString(R.string.Link_Select_new_owner));
        toolbar.setLeftListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityUtil.goBack(activity);
            }
        });

        groupKey = getIntent().getStringExtra(GROUP_IDENTIFY);
        List<GroupMemberEntity> groupMemEntities = ContactHelper.getInstance().loadGroupMemEntities(groupKey);

        String myPublicKey = SharedPreferenceUtil.getInstance().getUser().getPubKey();
        Iterator<GroupMemberEntity> iterator = groupMemEntities.iterator();
        while (iterator.hasNext()) {
            GroupMemberEntity memberEntity = iterator.next();
            if (memberEntity.getUid().equals(myPublicKey)) {
                iterator.remove();
            }
        }
        Collections.sort(groupMemEntities, new GroupMemberCompara());

        adapter = new GroupMemberSelectAdapter(activity, groupMemEntities);
        linearLayoutManager = new LinearLayoutManager(activity);
        recyclerview.setLayoutManager(linearLayoutManager);
        recyclerview.setAdapter(adapter);
        recyclerview.addOnScrollListener(onscrollListener);
        adapter.setTransferToListener(transferToListener);
        siderbar.setOnTouchingLetterChangedListener(letterChanged);
        new GroupOwnerPresenter(this).start();
    }

    private class GroupOwnerToOnscrollListener extends RecyclerView.OnScrollListener {

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

    private class GroupOwnerTransferToListener implements GroupMemberSelectAdapter.GroupTransferToListener{

        @Override
        public void transferTo(final GroupMemberEntity memEntity) {
            DialogUtil.showAlertTextView(activity,
                    activity.getString(R.string.Set_tip_title),
                    activity.getString(R.string.Link_Selecting__new_owner__release_your_ownership,memEntity.getUsername()),
                    "", "", false, new DialogUtil.OnItemClickListener() {
                        @Override
                        public void confirm(String value) {
                            String memberKey = memEntity.getIdentifier();
                            String uid = memEntity.getUid();
                            presenter.groupOwnerTo(memberKey, uid);
                        }

                        @Override
                        public void cancel() {

                        }
                    });
        }
    }

    private class GroupOwnerLetterChanged implements SideBar.OnTouchingLetterChangedListener{

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
    public void setPresenter(GroupOwnerContract.Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public Activity getActivity() {
        return activity;
    }
}
