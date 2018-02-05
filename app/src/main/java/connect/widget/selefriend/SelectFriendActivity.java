package connect.widget.selefriend;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import connect.activity.base.BaseActivity;
import connect.activity.base.compare.FriendCompara;
import connect.activity.home.view.LineDecoration;
import connect.database.SharedPreferenceUtil;
import connect.database.green.DaoHelper.ContactHelper;
import connect.database.green.bean.ContactEntity;
import connect.database.green.bean.GroupMemberEntity;
import connect.ui.activity.R;
import connect.utils.ActivityUtil;
import connect.widget.SideBar;
import connect.widget.TopToolBar;
import connect.widget.selefriend.adapter.SelectFriendAdapter;

/**
 * Choose friends/group members
 */

public class SelectFriendActivity extends BaseActivity {

    @Bind(R.id.txt1)
    TextView txt1;
    @Bind(R.id.recyclerview)
    RecyclerView recyclerview;
    @Bind(R.id.side_bar)
    SideBar sideBar;
    @Bind(R.id.toolbar_top)
    TopToolBar toolbarTop;

    private SelectFriendActivity mActivity;
    public static final String SOURCE_FRIEND = "source_friend";
    public static final String SOURCE_GROUP = "source_group_transfer";
    /** request activity code */
    public static final int CODE_REQUEST = 120;
    private LinearLayoutManager linearLayoutManager;
    private SelectFriendAdapter adapter;
    /** Already the selected data */
    private ArrayList<ContactEntity> selectFriend;
    /** source_friend/source_group_transfer */
    private String source;
    /** The sideBar is sliding */
    private boolean move;
    /** The current location of the item at the top */
    private int topPosition;

    public static void startActivity(Activity activity, String source, String groupId, ArrayList<ContactEntity> list) {
        Bundle bundle = new Bundle();
        bundle.putString("source", source);
        bundle.putString("groupId", groupId);
        if(list != null){
            bundle.putSerializable("list", list);
        }else{
            bundle.putSerializable("list", new ArrayList<ContactEntity>());
        }
        ActivityUtil.next(activity, SelectFriendActivity.class, bundle, CODE_REQUEST);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet_transfer_friend_sele);
        ButterKnife.bind(this);
        initView();
    }

    @Override
    public void initView() {
        mActivity = this;
        toolbarTop.setBlackStyle();
        toolbarTop.setLeftImg(R.mipmap.back_white);
        toolbarTop.setRightText(R.string.Wallet_Transfer);
        toolbarTop.setRightTextEnable(false);
        Bundle bundle = getIntent().getExtras();
        source = bundle.getString("source");
        selectFriend = (ArrayList<ContactEntity>) bundle.getSerializable("list");

        if(selectFriend.size() > 0){
            toolbarTop.setRightText(getString(R.string.Wallet_transfer_man, selectFriend.size()));
            toolbarTop.setRightTextEnable(true);
        }else{
            toolbarTop.setRightText(R.string.Common_OK);
            toolbarTop.setRightTextEnable(false);
        }

        linearLayoutManager = new LinearLayoutManager(mActivity);
        adapter = new SelectFriendAdapter(mActivity);
        adapter.setOnSelectFriendListener(onSelectFriendListener);
        recyclerview.setLayoutManager(linearLayoutManager);
        recyclerview.setAdapter(adapter);
        recyclerview.addItemDecoration(new LineDecoration(mActivity));
        recyclerview.addOnScrollListener(onScrollListener);

        sideBar.setOnTouchingLetterChangedListener(changedListener);
        adapter.setDataNotify(getDataList(), getSelectUidList(), new ArrayList<String>());
    }

    @OnClick(R.id.left_img)
    void goBack(View view) {
        ActivityUtil.goBack(mActivity);
    }

    @OnClick(R.id.right_lin)
    void goConfirm(View view) {
        Bundle bundle = new Bundle();
        bundle.putSerializable("list", adapter.getSelectList());
        ActivityUtil.goBackWithResult(mActivity, RESULT_OK, bundle);
    }

    /**
     * The adapter change after the callback
     */
    SelectFriendAdapter.OnSelectFriendListener onSelectFriendListener = new SelectFriendAdapter.OnSelectFriendListener(){
        @Override
        public void selectFriend(List<String> list) {
            if (list.size() == 0) {
                toolbarTop.setRightText(R.string.Common_OK);
                toolbarTop.setRightTextEnable(false);
            } else {
                toolbarTop.setRightText(getString(R.string.Wallet_transfer_man, list.size()));
                toolbarTop.setRightTextEnable(true);
            }
        }
    };

    /**
     * RecyclerView sliding listening
     */
    RecyclerView.OnScrollListener onScrollListener = new RecyclerView.OnScrollListener(){
        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            if (move) {
                move = false;
                int n = topPosition - linearLayoutManager.findFirstVisibleItemPosition();
                if (0 <= n && n < recyclerview.getChildCount()) {
                    int top = recyclerview.getChildAt(n).getTop();
                    recyclerview.scrollBy(0, top);
                }
            }
        }
    };

    /**
     * SideBar sliding listening
     */
    SideBar.OnTouchingLetterChangedListener changedListener = new SideBar.OnTouchingLetterChangedListener(){
        @Override
        public void onTouchingLetterChanged(String s) {
            topPosition = adapter.getPositionForSection(s.charAt(0));
            int firstItem = linearLayoutManager.findFirstVisibleItemPosition();
            int lastItem = linearLayoutManager.findLastVisibleItemPosition();
            if (topPosition <= firstItem) {
                recyclerview.scrollToPosition(topPosition);
            } else if (topPosition <= lastItem) {
                int top = recyclerview.getChildAt(topPosition - firstItem).getTop();
                recyclerview.scrollBy(0, top);
            } else {
                recyclerview.scrollToPosition(topPosition);
                move = true;
            }
        }
    };

    /**
     * Get the uid has chosen friend
     */
    private ArrayList<String> getSelectUidList(){
        ArrayList<String> list = new ArrayList<>();
        for(ContactEntity contactEntity : selectFriend){
            list.add(contactEntity.getUid());
        }
        return list;
    }

    /**
     * To obtain the local contacts and friends
     */
    private List<ContactEntity> getDataList(){
        List<ContactEntity> list = null;
        switch (source){
            case SOURCE_FRIEND:
                list = ContactHelper.getInstance().loadFriend();
                toolbarTop.setTitle(null, R.string.Wallet_Select_friends);
                txt1.setVisibility(View.GONE);
                break;
            case SOURCE_GROUP:
                list = loadGroupMember();
                toolbarTop.setTitle(null, R.string.Chat_Choose_Members);
                txt1.setText(getString(R.string.Chat_Group_Members, list.size()));
                break;
            default:
                break;
        }
        Collections.sort(list, new FriendCompara());
        return list;
    }

    /**
     * Access to the local group of members
     */
    private List<ContactEntity> loadGroupMember() {
        ArrayList<ContactEntity> list = new ArrayList<>();
        String groupId = getIntent().getExtras().getString("groupId", "");
        List<GroupMemberEntity> allMembers = ContactHelper.getInstance().loadGroupMemEntities(groupId);
        for (GroupMemberEntity groupMemEntity : allMembers) {
            if (SharedPreferenceUtil.getInstance().getUser().getUid().equals(groupMemEntity.getUid()))
                continue;
            ContactEntity friendEntity = new ContactEntity();
            friendEntity.setAvatar(groupMemEntity.getAvatar());
            friendEntity.setUid(groupMemEntity.getUid());
            String name =  groupMemEntity.getUsername();
            friendEntity.setName(name);
            list.add(friendEntity);
        }
        return list;
    }
}
