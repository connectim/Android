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
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import connect.activity.base.BaseActivity;
import connect.activity.base.compare.FriendCompara;
import connect.activity.chat.set.contract.GroupCreateContract;
import connect.activity.chat.set.presenter.GroupCreatePresenter;
import connect.activity.common.adapter.MulContactAdapter;
import connect.database.green.DaoHelper.ContactHelper;
import connect.database.green.bean.ContactEntity;
import connect.ui.activity.R;
import connect.utils.ActivityUtil;
import connect.widget.SideBar;
import connect.widget.TopToolBar;

public class GroupCreateActivity extends BaseActivity implements GroupCreateContract.BView{

    @Bind(R.id.toolbar)
    TopToolBar toolbar;
    @Bind(R.id.recyclerview)
    RecyclerView recyclerview;
    @Bind(R.id.siderbar)
    SideBar siderbar;

    private GroupCreateActivity activity;
    private static String TAG = "_GroupCreateActivity";
    private static String UID = "UID";
    private String pubKey;
    private int topPosi;
    private boolean move;

    private GroupCreateOnscrollListener onscrollListener = new GroupCreateOnscrollListener();
    private GroupCreateFriendSelectListener friendSelectListener = new GroupCreateFriendSelectListener();
    private GroupCreateLetterChanged letterChanged = new GroupCreateLetterChanged();
    private LinearLayoutManager linearLayoutManager;
    private MulContactAdapter adapter;
    private GroupCreateContract.Presenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_create);
        ButterKnife.bind(this);
        initView();
    }

    public static void startActivity(Activity activity, String pubkey) {
        Bundle bundle = new Bundle();
        bundle.putString(UID, pubkey);
        ActivityUtil.next(activity, GroupCreateActivity.class, bundle);
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

        pubKey = getIntent().getStringExtra(UID);
        linearLayoutManager = new LinearLayoutManager(activity);
        List<String> oldMembers = new ArrayList<>();
        oldMembers.add(pubKey);
        List<ContactEntity> friendEntities = ContactHelper.getInstance().loadFriend();
        Collections.sort(friendEntities, new FriendCompara());

        adapter = new MulContactAdapter(activity, oldMembers, friendEntities, null);
        recyclerview.setLayoutManager(linearLayoutManager);
        recyclerview.setAdapter(adapter);
        recyclerview.addOnScrollListener(onscrollListener);
        adapter.setOnSeleFriendListence(friendSelectListener);
        siderbar.setOnTouchingLetterChangedListener(letterChanged);
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

    private class GroupCreateOnscrollListener extends RecyclerView.OnScrollListener {

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

    private class GroupCreateFriendSelectListener implements MulContactAdapter.OnSeleFriendListence {

        @Override
        public void seleFriend(List<ContactEntity> list) {
            if (list == null || list.size() < 2) {
                toolbar.setRightTextColor(R.color.color_6d6e75);
                toolbar.setRightListener(null);
            } else {
                toolbar.setRightTextColor(R.color.color_green);
                toolbar.setRightListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        List<ContactEntity> selectEntities = adapter.getSelectEntities();
                        if (selectEntities == null || selectEntities.size() < 1) {
                            toolbar.setRightTextColor(R.color.color_6d6e75);
                            return;
                        }
                        presenter.requestGroupCreate(selectEntities);

                        toolbar.setRightListener(null);
                        Message message = new Message();
                        message.what = 100;
                        handler.sendMessageDelayed(message, 3000);
                    }
                });
            }
        }
    }

    private class GroupCreateLetterChanged implements SideBar.OnTouchingLetterChangedListener{

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
        return pubKey;
    }

    @Override
    public void setPresenter(GroupCreateContract.Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public Activity getActivity() {
        return activity;
    }
}
