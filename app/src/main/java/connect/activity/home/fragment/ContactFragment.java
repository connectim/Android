package connect.activity.home.fragment;

import android.os.AsyncTask;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import connect.activity.base.BaseFragment;
import connect.activity.chat.ChatActivity;
import connect.activity.chat.bean.Talker;
import connect.activity.chat.set.GroupSetActivity;
import connect.activity.contact.FriendInfoActivity;
import connect.activity.contact.FriendSetAliasActivity;
import connect.activity.contact.NewFriendActivity;
import connect.activity.contact.ScanAddFriendActivity;
import connect.activity.contact.SearchActivity;
import connect.activity.contact.adapter.ContactAdapter;
import connect.activity.contact.bean.ContactNotice;
import connect.activity.contact.model.ContactListManage;
import connect.activity.home.HomeActivity;
import connect.activity.home.bean.ContactBean;
import connect.database.green.bean.GroupEntity;
import connect.ui.activity.R;
import connect.utils.ActivityUtil;
import connect.widget.SideBar;
import connect.widget.TopToolBar;
import protos.Connect;

/**
 * The address book contacts
 * Created by gtq on 2016/11/22.
 */
public class ContactFragment extends BaseFragment {

    @Bind(R.id.toolbar_top)
    TopToolBar toolbarTop;
    @Bind(R.id.siderbar)
    SideBar siderbar;
    @Bind(R.id.recyclerview)
    RecyclerView recyclerview;

    private FragmentActivity mActivity;
    private ContactAdapter adapter;
    private ContactListManage contactManage;
    private List<ContactBean> listRequest;
    private List<ContactBean> groupList;
    private HashMap<String, List<ContactBean>> friendMap;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_contact, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    public static ContactFragment startFragment() {
        ContactFragment contactFragment = new ContactFragment();
        return contactFragment;
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
        recyclerview.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                adapter.closeMenu();
            }
        });

        siderbar.setOnTouchingLetterChangedListener(new SideBar.OnTouchingLetterChangedListener() {
            @Override
            public void onTouchingLetterChanged(String s) {
                int position = adapter.getPositionForSection(s.charAt(0));
                if (position >= 0) {
                    recyclerview.scrollToPosition(position);
                }
            }
        });

        adapter.setOnSideMenuListence(onSideMenuListence);
        contactManage = new ContactListManage();
        updataContact();
    }

    @Subscribe
    public void onEventMainThread(ContactNotice notice) {
        if (notice.getNotice() == ContactNotice.ConNotice.RecContact) {
            updataContact();
        } else if (notice.getNotice() == ContactNotice.ConNotice.RecAddFriend) {
            updataRequest();
        } else if (notice.getNotice() == ContactNotice.ConNotice.RecGroup) {
            updataGroup();
        } else if (notice.getNotice() == ContactNotice.ConNotice.RecFriend) {
            updataFriend();
        }
    }

    @OnClick(R.id.left_img)
    void search(View view) {
        ActivityUtil.next(mActivity, SearchActivity.class, android.R.anim.fade_in, android.R.anim.fade_out);
    }

    @OnClick(R.id.right_lin)
    void goAddFriend(View view) {
        ActivityUtil.nextBottomToTop(mActivity, ScanAddFriendActivity.class, null, -1);
    }

    private ContactAdapter.OnItemChildListence onSideMenuListence = new ContactAdapter.OnItemChildListence() {
        @Override
        public void itemClick(int position, ContactBean entity) {
            switch (entity.getStatus()) {
                case 1:
                    ((HomeActivity) mActivity).setFragmentDot(1, 0);
                    ActivityUtil.next(mActivity, NewFriendActivity.class);
                    break;
                case 6:
                    ChatActivity.startActivity(mActivity, new Talker(Connect.ChatType.CONNECT_SYSTEM_VALUE, "Connect"));
                    break;
                case 2:
                    GroupEntity groupEntity = new GroupEntity();
                    groupEntity.setIdentifier(entity.getPub_key());
                    groupEntity.setAvatar(entity.getAvatar());
                    groupEntity.setName(entity.getName());
                    ChatActivity.startActivity(mActivity, new Talker(groupEntity));
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
                    FriendSetAliasActivity.startActivity(mActivity, entity.getPub_key());
                    break;
                default:
                    break;
            }
        }
    };

    private void updataContact() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                listRequest = contactManage.getContactRequest();
                groupList = contactManage.getGroupData();
                friendMap = contactManage.getFriendList();
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                bindData();
            }
        }.execute();
    }

    private void updataRequest() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                listRequest = contactManage.getContactRequest();
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                bindData();
            }
        }.execute();
    }

    private void updataGroup() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                groupList = contactManage.getGroupData();
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                bindData();
            }
        }.execute();
    }

    private void updataFriend() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                friendMap = contactManage.getFriendList();
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                bindData();
            }
        }.execute();
    }

    private void bindData() {
        ArrayList<ContactBean> finalList = new ArrayList<>();
        int friendSize = friendMap.get("friend").size() + friendMap.get("favorite").size();
        int groupSize = groupList.size();
        finalList.addAll(listRequest);
        finalList.addAll(friendMap.get("favorite"));
        finalList.addAll(groupList);
        finalList.addAll(friendMap.get("friend"));
        adapter.setStartPosition(finalList.size() - friendMap.get("friend").size());

        String bottomTxt = "";
        if (friendSize > 0 && groupSize == 0) {
            bottomTxt = mActivity.getString(R.string.Link_contact_count, friendSize, "", "");
        } else if (friendSize == 0 && groupSize > 0) {
            bottomTxt = mActivity.getString(R.string.Link_group_count, groupSize);
        } else if (friendSize > 0 && groupSize > 0) {
            bottomTxt = String.format(mActivity.getString(R.string.Link_contact_count_group_count), friendSize, groupSize);
        }
        adapter.setDataNotify(finalList, bottomTxt);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
        EventBus.getDefault().unregister(this);
    }

}
