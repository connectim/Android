package connect.ui.activity.common;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import connect.db.green.DaoHelper.ContactHelper;
import connect.db.green.bean.ContactEntity;
import connect.ui.activity.R;
import connect.ui.activity.chat.model.FriendCompara;
import connect.ui.activity.home.view.LineDecoration;
import connect.ui.adapter.MulContactAdapter;
import connect.ui.base.BaseActivity;
import connect.utils.ActivityUtil;
import connect.view.SideBar;
import connect.view.TopToolBar;

public class FriendSelectActivity extends BaseActivity {

    @Bind(R.id.toolbar_top)
    TopToolBar toolbarTop;
    @Bind(R.id.recyclerview)
    RecyclerView recyclerview;
    @Bind(R.id.siderbar)
    SideBar siderbar;

    private FriendSelectActivity activity;
    private LinearLayoutManager linearLayoutManager;

    private FriendCompara friendCompara = new FriendCompara();
    private boolean move;
    private int topPosi;
    private MulContactAdapter adapter;


    private SelectEnum selectEnum;

    public enum SelectEnum {
        SELECT,
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_select);
        ButterKnife.bind(this);
        initView();
    }

    public static void startActivity(Activity activity, SelectEnum selectEnum) {
        Bundle bundle = new Bundle();
        bundle.putSerializable("enum", selectEnum);
        ActivityUtil.next(activity, FriendSelectActivity.class, bundle, 120);
    }

    @Override
    public void initView() {
        activity = this;
        toolbarTop.setBlackStyle();
        toolbarTop.setLeftImg(R.mipmap.back_white);
        toolbarTop.setLeftListence(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityUtil.goBack(activity);
            }
        });
        toolbarTop.setRightListence(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishSelect();
            }
        });

        selectEnum = (SelectEnum) getIntent().getSerializableExtra("enum");
        switch (selectEnum) {
            case SELECT:
                toolbarTop.setTitle(getString(R.string.Wallet_Select_friends));
                toolbarTop.setRightText(R.string.Chat_Complete);
                break;
        }

        List<ContactEntity> friendEntities = ContactHelper.getInstance().loadFriend();
        Collections.sort(friendEntities, friendCompara);

        linearLayoutManager = new LinearLayoutManager(activity);
        adapter = new MulContactAdapter(activity, new ArrayList<String>(), friendEntities);
        adapter.setOnSeleFriendListence(new MulContactAdapter.OnSeleFriendListence() {
            @Override
            public void seleFriend(List<ContactEntity> list) {
                if (list.size() == 0) {
                    toolbarTop.setRightTextEnable(false);
                    switch (selectEnum) {
                        case SELECT:
                            toolbarTop.setRightText(R.string.Chat_Complete);
                            toolbarTop.setRightTextColor(R.color.color_68656f);
                            break;
                    }
                } else {
                    toolbarTop.setRightTextEnable(true);
                    switch (selectEnum) {
                        case SELECT:
                            toolbarTop.setRightText(getString(R.string.Chat_Select_Count, list.size()));
                            toolbarTop.setRightTextColor(R.color.color_00c400);
                            break;
                    }
                }
            }
        });
        recyclerview.setLayoutManager(linearLayoutManager);
        recyclerview.setAdapter(adapter);
        recyclerview.addItemDecoration(new LineDecoration(activity));
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

    private void finishSelect() {
        Bundle bundle = new Bundle();
        bundle.putSerializable("list", (ArrayList) adapter.getSelectEntities());
        ActivityUtil.goBackWithResult(activity,120,bundle);
    }
}
