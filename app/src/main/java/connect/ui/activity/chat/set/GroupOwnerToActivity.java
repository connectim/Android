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
import connect.db.SharedPreferenceUtil;
import connect.db.green.DaoHelper.ContactHelper;
import connect.db.green.bean.GroupMemberEntity;
import connect.ui.activity.R;
import connect.ui.activity.chat.model.GroupMemberCompara;
import connect.ui.adapter.GroupMemberSelectAdapter;
import connect.ui.base.BaseActivity;
import connect.utils.ActivityUtil;
import connect.utils.DialogUtil;
import connect.utils.UriUtil;
import connect.utils.okhttp.OkHttpUtil;
import connect.utils.okhttp.ResultCall;
import connect.view.SideBar;
import connect.view.TopToolBar;
import protos.Connect;

/**
 * Select New Group Owner
 */
public class GroupOwnerToActivity extends BaseActivity {

    @Bind(R.id.toolbar)
    TopToolBar toolbar;
    @Bind(R.id.recyclerview)
    RecyclerView recyclerview;
    @Bind(R.id.siderbar)
    SideBar siderbar;

    private String Tag = "GroupTransferToActivity";

    private static String GROUP_KEY = "GROUP_KEY";
    private GroupOwnerToActivity activity;
    private String groupKey = null;

    private boolean move;
    private int topPosi;
    private LinearLayoutManager linearLayoutManager;
    private GroupMemberSelectAdapter adapter;

    private GroupMemberCompara compara = new GroupMemberCompara();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_ownerto);
        ButterKnife.bind(this);
        initView();
    }

    public static void startActivity(Activity activity, String groupkey) {
        Bundle bundle = new Bundle();
        bundle.putString(GROUP_KEY, groupkey);
        ActivityUtil.next(activity, GroupOwnerToActivity.class, bundle);
    }

    @Override
    public void initView() {
        activity = this;
        toolbar.setBlackStyle();
        toolbar.setLeftImg(R.mipmap.back_white);
        toolbar.setTitle(getResources().getString(R.string.Link_Select_new_owner));
        toolbar.setLeftListence(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityUtil.goBack(activity);
            }
        });

        groupKey = getIntent().getStringExtra(GROUP_KEY);

        List<GroupMemberEntity> groupMemEntities = ContactHelper.getInstance().loadGroupMemEntity(groupKey, MemoryDataManager.getInstance().getAddress());
        Collections.sort(groupMemEntities, compara);

        adapter = new GroupMemberSelectAdapter(activity, groupMemEntities);
        linearLayoutManager = new LinearLayoutManager(activity);
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
            public void transferTo(final GroupMemberEntity memEntity) {
                DialogUtil.showAlertTextView(activity,
                        activity.getString(R.string.Set_tip_title),
                        activity.getString(R.string.Link_Selecting__new_owner__release_your_ownership,memEntity.getUsername()),
                        "", "", false, new DialogUtil.OnItemClickListener() {
                            @Override
                            public void confirm(String value) {
                                groupOwnerTransferTo(memEntity);
                            }

                            @Override
                            public void cancel() {

                            }
                        });
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

    /**
     * Group manager change
     */
    protected void groupOwnerTransferTo(final GroupMemberEntity memEntity) {
        Connect.GroupAttorn attorn = Connect.GroupAttorn.newBuilder()
                .setIdentifier(memEntity.getIdentifier())
                .setAddress(memEntity.getAddress()).build();
        OkHttpUtil.getInstance().postEncrySelf(UriUtil.GROUP_ATTORN, attorn, new ResultCall<Connect.HttpResponse>() {
            @Override
            public void onResponse(Connect.HttpResponse response) {
                GroupMemberEntity myMember = ContactHelper.getInstance().loadGroupMemByAds(groupKey, MemoryDataManager.getInstance().getAddress());
                myMember.setRole(0);
                ContactHelper.getInstance().inserGroupMemEntity(myMember);

                GroupMemberEntity ownerMember = ContactHelper.getInstance().loadGroupMemByAds(groupKey, memEntity.getAddress());
                ownerMember.setRole(1);
                ContactHelper.getInstance().inserGroupMemEntity(ownerMember);

                GroupSetActivity.startActivity(activity,groupKey);
            }

            @Override
            public void onError(Connect.HttpResponse response) {

            }
        });
    }
}
