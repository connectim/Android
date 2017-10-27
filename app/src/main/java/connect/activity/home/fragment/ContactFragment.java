package connect.activity.home.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import connect.activity.base.BaseFragment;
import connect.activity.chat.ChatActivity;
import connect.activity.chat.bean.Talker;
import connect.activity.chat.set.GroupSetActivity;
import connect.activity.contact.AddFriendActivity;
import connect.activity.contact.FriendInfoActivity;
import connect.activity.contact.FriendInfoAliasActivity;
import connect.activity.contact.ScanAddFriendActivity;
import connect.activity.contact.SearchFriendActivity;
import connect.activity.contact.bean.ContactNotice;
import connect.activity.home.HomeActivity;
import connect.activity.home.adapter.ContactAdapter;
import connect.activity.home.bean.ContactBean;
import connect.database.green.bean.GroupEntity;
import connect.ui.activity.R;
import connect.utils.ActivityUtil;
import connect.widget.SideBar;
import connect.widget.TopToolBar;
import protos.Connect;

/**
 * The address book contacts
 */
public class ContactFragment extends BaseFragment {

    @Bind(R.id.toolbar_top)
    TopToolBar toolbarTop;
    @Bind(R.id.side_bar)
    SideBar sideBar;
    @Bind(R.id.recyclerview)
    RecyclerView recyclerview;

    private FragmentActivity mActivity;
    private ContactAdapter adapter;

    public static ContactFragment startFragment() {
        ContactFragment contactFragment = new ContactFragment();
        return contactFragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_contact, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mActivity = getActivity();
        initView();
        EventBus.getDefault().register(this);
    }

    private void initView() {
        toolbarTop.setBlackStyle();
        toolbarTop.setTitle(null, R.string.Link_Contacts);
        toolbarTop.setRightImg(R.mipmap.add_white3x);
        toolbarTop.setLeftImg(R.mipmap.search3x);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mActivity);
        recyclerview.setLayoutManager(linearLayoutManager);
        adapter = new ContactAdapter(mActivity);
        recyclerview.setAdapter(adapter);
        recyclerview.addOnScrollListener(onScrollListener);
        sideBar.setOnTouchingLetterChangedListener(changedListener);

        adapter.setOnSideMenuListener(onSideMenuListener);
        adapter.updateContact(adapter.updateTypeContact);
    }

    @OnClick(R.id.left_img)
    void search(View view) {
        ActivityUtil.next(mActivity, SearchFriendActivity.class, android.R.anim.fade_in, android.R.anim.fade_out);
    }

    @OnClick(R.id.right_lin)
    void goAddFriend(View view) {
        ActivityUtil.nextBottomToTop(mActivity, ScanAddFriendActivity.class, null, -1);
    }

    RecyclerView.OnScrollListener onScrollListener = new RecyclerView.OnScrollListener(){
        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            adapter.closeMenu();
        }
    };

    SideBar.OnTouchingLetterChangedListener changedListener = new SideBar.OnTouchingLetterChangedListener(){
        @Override
        public void onTouchingLetterChanged(String s) {
            int position = adapter.getPositionForSection(s.charAt(0));
            if (position >= 0) {
                recyclerview.scrollToPosition(position);
            }
        }
    };

    private ContactAdapter.OnItemChildListener onSideMenuListener = new ContactAdapter.OnItemChildListener() {
        @Override
        public void itemClick(int position, ContactBean entity) {
            switch (entity.getStatus()) {
                case 1:
                    ((HomeActivity) mActivity).setFragmentDot(1, 0);
                    ActivityUtil.next(mActivity, AddFriendActivity.class);
                    break;
                case 6:
                    ChatActivity.startActivity(mActivity, new Talker(Connect.ChatType.CONNECT_SYSTEM, "Connect"));
                    break;
                case 2:
                    ChatActivity.startActivity(mActivity, new Talker(Connect.ChatType.GROUPCHAT,entity.getPub_key()));
                    break;
                case 3:
                case 4:
                    FriendInfoActivity.startActivity(mActivity, entity.getPub_key());
                    break;
                default:
                    break;
            }
        }

        @Override
        public void setFriend(int position, ContactBean entity) {
            switch (entity.getStatus()) {
                case 2:
                    GroupSetActivity.startActivity(mActivity, entity.getPub_key());
                    break;
                case 3:
                case 4:
                    FriendInfoAliasActivity.startActivity(mActivity, entity.getPub_key());
                    break;
                default:
                    break;
            }
        }
    };

    @Subscribe
    public void onEventMainThread(ContactNotice notice) {
        if (notice.getNotice() == ContactNotice.ConNotice.RecContact) {
            adapter.updateContact(adapter.updateTypeContact);
        } else if (notice.getNotice() == ContactNotice.ConNotice.RecAddFriend) {
            adapter.updateContact(adapter.updateTypeRequest);
        } else if (notice.getNotice() == ContactNotice.ConNotice.RecGroup) {
            adapter.updateContact(adapter.updateTypeGroup);
        } else if (notice.getNotice() == ContactNotice.ConNotice.RecFriend) {
            adapter.updateContact(adapter.updateTypeFriend);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
        EventBus.getDefault().unregister(this);
    }

}
