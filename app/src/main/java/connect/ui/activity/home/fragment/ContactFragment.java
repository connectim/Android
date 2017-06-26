package connect.ui.activity.home.fragment;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import connect.db.green.bean.ContactEntity;
import connect.db.green.bean.GroupEntity;
import connect.ui.activity.R;
import connect.ui.activity.chat.ChatActivity;
import connect.ui.activity.chat.bean.Talker;
import connect.ui.activity.chat.set.GroupSetActivity;
import connect.ui.activity.contact.FriendInfoActivity;
import connect.ui.activity.contact.FriendSetAliasActivity;
import connect.ui.activity.contact.NewFriendActivity;
import connect.ui.activity.contact.ScanAddFriendActivity;
import connect.ui.activity.contact.SearchActivity;
import connect.ui.activity.contact.adapter.ContactAdapter;
import connect.ui.activity.contact.bean.ContactNotice;
import connect.ui.activity.contact.model.ContactListManage;
import connect.ui.activity.home.HomeActivity;
import connect.ui.activity.home.bean.ContactBean;
import connect.ui.base.BaseFragment;
import connect.utils.ActivityUtil;
import connect.view.SideBar;
import connect.view.TopToolBar;

/**
 * The address book contacts
 * Created by gtq on 2016/11/22.
 */
public class ContactFragment extends BaseFragment {

    @Bind(R.id.toolbar_top)
    TopToolBar toolbarTop;
    @Bind(R.id.siderbar)
    SideBar siderbar;
    @Bind(R.id.list_view)
    ListView listView;

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

        adapter = new ContactAdapter();
        listView.setAdapter(adapter);
        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                adapter.closeMenu();
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

            }
        });

        siderbar.setOnTouchingLetterChangedListener(new SideBar.OnTouchingLetterChangedListener() {
            @Override
            public void onTouchingLetterChanged(String s) {
                int position = adapter.getPositionForSection(s.charAt(0));
                if(position >= 0){
                    listView.setSelection(position);
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
        }else if (notice.getNotice() == ContactNotice.ConNotice.RecAddFriend) {
            updataRequest();
        }else if(notice.getNotice() == ContactNotice.ConNotice.RecGroup){
            updataGroup();
        }else if(notice.getNotice() == ContactNotice.ConNotice.RecFriend){
            updataFriend();
        }
    }

    @OnClick(R.id.left_img)
    void search(View view) {
        ActivityUtil.next(mActivity, SearchActivity.class,android.R.anim.fade_in,android.R.anim.fade_out);
    }

    @OnClick(R.id.right_lin)
    void goAddFriend(View view) {
        ActivityUtil.nextBottomToTop(mActivity, ScanAddFriendActivity.class,null,-1);
    }

    private ContactAdapter.OnItemChildListence onSideMenuListence = new ContactAdapter.OnItemChildListence(){
        @Override
        public void itemClick(int position, ContactBean entity) {
            switch (entity.getStatus()){
                case 1:
                    ((HomeActivity)mActivity).setFragmentDot(1,0);
                    ActivityUtil.next(mActivity, NewFriendActivity.class);
                    break;
                case 6:
                    ChatActivity.startActivity(mActivity, new Talker(2, "Connect"));
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
            switch (entity.getStatus()){
                case 2:
                    GroupSetActivity.startActivity(mActivity,entity.getPub_key());
                    break;
                case 3:
                case 4:
                    FriendSetAliasActivity.startActivity(mActivity,entity.getPub_key());
                    break;
                default:
                    break;
            }
        }
    };

    private void updataContact(){
        new AsyncTask<Void,Void,Void>(){
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

    private void updataRequest(){
        new AsyncTask<Void,Void,Void>(){
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

    private void updataGroup(){
        new AsyncTask<Void,Void,Void>(){
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

    private void updataFriend(){
        new AsyncTask<Void,Void,Void>(){
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

    private void bindData(){
        ArrayList<ContactBean> finalList = new ArrayList<>();
        int friendSize = friendMap.get("friend").size() + friendMap.get("favorite").size();
        int groupSize = groupList.size();
        finalList.addAll(listRequest);
        finalList.addAll(friendMap.get("favorite"));
        finalList.addAll(groupList);
        finalList.addAll(friendMap.get("friend"));
        adapter.setStartPosition(finalList.size() - friendMap.get("friend").size());

        String bottomTxt = "";
        if(friendSize > 0 && groupSize == 0){
            bottomTxt = mActivity.getString(R.string.Link_contact_count,friendSize,"","");
        }else if(friendSize == 0 && groupSize > 0){
            bottomTxt = mActivity.getString(R.string.Link_group_count,groupSize);
        }else if(friendSize > 0 && groupSize > 0){
            bottomTxt = String.format(mActivity.getString(R.string.Link_contact_count_group_count), friendSize, groupSize);
        }
        adapter.setDataNotify(finalList,bottomTxt);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
        EventBus.getDefault().unregister(this);
    }

}
