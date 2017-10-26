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
import connect.activity.base.BaseActivity;
import connect.activity.chat.adapter.ContactCardAdapter;
import connect.activity.chat.bean.LinkMessageRow;
import connect.activity.chat.bean.MsgSend;
import connect.activity.chat.model.FriendCompara;
import connect.activity.chat.set.contract.ContactCardContract;
import connect.activity.chat.set.presenter.ContactCardPresenter;
import connect.database.green.DaoHelper.ContactHelper;
import connect.database.green.bean.ContactEntity;
import connect.ui.activity.R;
import connect.utils.ActivityUtil;
import connect.widget.SideBar;
import connect.widget.TopToolBar;

/**
 * Created by gtq on 2016/12/13.
 */
public class ContactCardActivity extends BaseActivity implements ContactCardContract.BView {

    @Bind(R.id.recyclerview)
    RecyclerView recyclerview;
    @Bind(R.id.siderbar)
    SideBar siderbar;
    @Bind(R.id.toolbar)
    TopToolBar toolbar;

    private ContactCardActivity activity;
    private String pubKey;
    private boolean move;
    private int topPosi;

    private ContactCardScrollListener scrollListener=new ContactCardScrollListener();
    private ContactCardTouchingLetterChanged letterChanged=new ContactCardTouchingLetterChanged();
    private LinearLayoutManager linearLayoutManager;
    private ContactCardContract.Presenter presenter;
    private ContactCardAdapter contactCardAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contactcard);
        ButterKnife.bind(this);
        initView();
    }

    public static void startActivity(Activity activity,String uid) {
        Bundle bundle = new Bundle();
        bundle.putSerializable("UID", uid);
        ActivityUtil.next(activity, ContactCardActivity.class, bundle);
    }

    @Override
    public void initView() {
        activity = this;
        toolbar.setBlackStyle();
        toolbar.setLeftImg(R.mipmap.back_white);
        toolbar.setTitle(getResources().getString(R.string.Chat_Send_a_namecard));
        toolbar.setLeftListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityUtil.goBack(activity);
            }
        });

        pubKey = getIntent().getStringExtra("UID");
        linearLayoutManager = new LinearLayoutManager(activity);
        List<ContactEntity> friendEntities = ContactHelper.getInstance().loadFriend(pubKey);
        Collections.sort(friendEntities, new FriendCompara());

        contactCardAdapter = new ContactCardAdapter(activity, friendEntities);
        recyclerview.setLayoutManager(linearLayoutManager);
        recyclerview.setAdapter(contactCardAdapter);
        contactCardAdapter.setItemClickListener(new ContactCardAdapter.OnItemClickListener() {
            @Override
            public void onClick(View v) {
                ContactEntity entity = (ContactEntity) v.getTag();
                MsgSend.sendOuterMsg(LinkMessageRow.Name_Card,entity.getPub_key(),entity.getUsername(),entity.getAvatar());
                ActivityUtil.goBack(activity);
            }
        });

        recyclerview.addOnScrollListener(scrollListener);
        siderbar.setOnTouchingLetterChangedListener(letterChanged);
        new ContactCardPresenter(this).start();
    }

    private class ContactCardScrollListener extends RecyclerView.OnScrollListener {

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

    private class ContactCardTouchingLetterChanged implements SideBar.OnTouchingLetterChangedListener {

        @Override
        public void onTouchingLetterChanged(String s) {
            int position = contactCardAdapter.getPositionForSection(s.charAt(0));

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
    public void setPresenter(ContactCardContract.Presenter presenter) {

    }

    @Override
    public Activity getActivity() {
        return activity;
    }
}
