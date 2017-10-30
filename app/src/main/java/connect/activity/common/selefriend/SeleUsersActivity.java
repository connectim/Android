package connect.activity.common.selefriend;

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
import connect.database.SharedPreferenceUtil;
import connect.database.green.DaoHelper.ContactHelper;
import connect.database.green.bean.ContactEntity;
import connect.database.green.bean.GroupMemberEntity;
import connect.ui.activity.R;
import connect.activity.base.compare.FriendCompara;
import connect.activity.common.adapter.MulContactAdapter;
import connect.activity.home.view.LineDecoration;
import connect.activity.base.BaseActivity;
import connect.utils.ActivityUtil;
import connect.widget.SideBar;
import connect.widget.TopToolBar;

/**
 * Choose friends to transfer
 * Created by Administrator on 2016/12/22.
 */
public class SeleUsersActivity extends BaseActivity {

    @Bind(R.id.toolbar_top)
    TopToolBar toolbarTop;
    @Bind(R.id.recyclerview)
    RecyclerView recyclerview;
    @Bind(R.id.siderbar)
    SideBar siderbar;
    @Bind(R.id.txt1)
    TextView txt1;

    private FriendCompara friendCompara = new FriendCompara();

    private boolean move;
    private int topPosi;
    private LinearLayoutManager linearLayoutManager;
    private MulContactAdapter adapter;
    private SeleUsersActivity activity;
    public static final String SOURCE_FRIEND = "source_friend";
    public static final String SOURCE_GROUP = "source_group_transfer";
    private ArrayList<ContactEntity> seledFriend;
    public static final int CODE_REQUEST = 120;
    private String source;
    private String groupKey;

    public static void startActivity(Activity activity, String source, String pubKey,ArrayList<ContactEntity> list) {
        Bundle bundle = new Bundle();
        bundle.putString("source", source);
        bundle.putString("pubKey", pubKey);
        if(list != null){
            bundle.putSerializable("list", list);
        }
        ActivityUtil.next(activity, SeleUsersActivity.class, bundle, CODE_REQUEST);
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
        activity = this;
        toolbarTop.setBlackStyle();
        toolbarTop.setLeftImg(R.mipmap.back_white);
        toolbarTop.setRightText(R.string.Wallet_Transfer);
        toolbarTop.setRightTextEnable(false);

        Bundle bundle = getIntent().getExtras();
        source = bundle.getString("source");
        groupKey = bundle.getString("pubKey", "");
        seledFriend = (ArrayList<ContactEntity>) bundle.getSerializable("list");

        List<ContactEntity> friendEntities = null;
        if (source.equals(SOURCE_FRIEND)) {
            toolbarTop.setTitle(null, R.string.Wallet_Select_friends);
            friendEntities = ContactHelper.getInstance().loadFriend();
            txt1.setVisibility(View.GONE);
            if(seledFriend != null && seledFriend.size() > 0){
                toolbarTop.setRightText(getString(R.string.Wallet_transfer_man, seledFriend.size()));
                toolbarTop.setRightTextEnable(true);
            }
        }else if (source.equals(SOURCE_GROUP)){
            toolbarTop.setTitle(null, R.string.Chat_Choose_Members);
            friendEntities = loadGropMember();
            txt1.setText(getString(R.string.Chat_Group_Members, friendEntities.size()));
            if(seledFriend != null && seledFriend.size() > 0){
                toolbarTop.setRightText(getString(R.string.Wallet_transfer_man, seledFriend.size()));
                toolbarTop.setRightTextEnable(true);
            }
        }

        Collections.sort(friendEntities, friendCompara);
        linearLayoutManager = new LinearLayoutManager(activity);
        adapter = new MulContactAdapter(activity, new ArrayList<String>(), friendEntities,seledFriend);
        adapter.setOnSeleFriendListence(new MulContactAdapter.OnSeleFriendListence() {
            @Override
            public void seleFriend(List<ContactEntity> list) {
                if (list.size() == 0) {
                    toolbarTop.setRightText(R.string.Wallet_Transfer);
                    toolbarTop.setRightTextEnable(false);
                } else {
                    toolbarTop.setRightText(getString(R.string.Wallet_transfer_man, list.size()));
                    toolbarTop.setRightTextEnable(true);
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

    @OnClick(R.id.left_img)
    void goback(View view) {
        ActivityUtil.goBack(activity);
    }

    @OnClick(R.id.right_lin)
    void goFinish(View view) {
        Bundle bundle = new Bundle();
        bundle.putSerializable("list", adapter.getSelectEntities());
        ActivityUtil.goBackWithResult(activity,RESULT_OK,bundle);
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

    private List<ContactEntity> loadGropMember() {
        ArrayList<ContactEntity> list = new ArrayList<>();
        List<GroupMemberEntity> allMembers = ContactHelper.getInstance().loadGroupMemEntities(groupKey);
        for (GroupMemberEntity groupMemEntity : allMembers) {
            if (SharedPreferenceUtil.getInstance().getUser().getPubKey().equals(groupMemEntity.getUid()))
                continue;
            ContactEntity friendEntity = new ContactEntity();
            friendEntity.setAvatar(groupMemEntity.getAvatar());
            friendEntity.setUid(groupMemEntity.getUid());
            String name = TextUtils.isEmpty(groupMemEntity.getUsername()) ? groupMemEntity.getNick() : groupMemEntity.getUsername();
            friendEntity.setUsername(name);
            list.add(friendEntity);
        }
        return list;
    }

}
