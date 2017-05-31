package connect.ui.activity.contact;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import connect.db.green.DaoHelper.ContactHelper;
import connect.db.green.DaoHelper.MessageHelper;
import connect.db.green.bean.ContactEntity;
import connect.db.green.bean.GroupEntity;
import connect.ui.activity.R;
import connect.ui.activity.chat.bean.MsgEntity;
import connect.ui.activity.chat.model.content.BaseChat;
import connect.ui.activity.chat.model.content.FriendChat;
import connect.ui.activity.chat.model.content.GroupChat;
import connect.ui.activity.contact.adapter.ShareCardContactAdapter;
import connect.ui.activity.contact.model.ContactListManage;
import connect.ui.activity.home.bean.ContactBean;
import connect.ui.base.BaseActivity;
import connect.ui.base.BaseApplication;
import connect.utils.ActivityUtil;
import connect.utils.DialogUtil;
import connect.view.SideBar;
import connect.view.TopToolBar;

/**
 * Contacts and friends to share name card
 * Created by Administrator on 2017/2/20.
 */

public class ShareCardContactActivity extends BaseActivity {

    @Bind(R.id.toolbar)
    TopToolBar toolbar;
    @Bind(R.id.list_view)
    ListView listView;
    @Bind(R.id.siderbar)
    SideBar siderbar;

    private ShareCardContactActivity mActivity;
    private List<ContactBean> groupList;
    private HashMap<String, List<ContactBean>> friendMap;
    private ShareCardContactAdapter adapter;
    private ContactEntity friendEntity;

    public static void startActivity(Activity activity, ContactEntity friendEntity) {
        Bundle bundle = new Bundle();
        bundle.putSerializable("bean", friendEntity);
        ActivityUtil.next(activity, ShareCardContactActivity.class, bundle);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_shared_card_friend);
        ButterKnife.bind(this);
        initView();
    }

    @Override
    public void initView() {
        mActivity = this;
        toolbar.setBlackStyle();
        toolbar.setLeftImg(R.mipmap.back_white);
        toolbar.setTitle(null, R.string.Chat_Share_contact);

        friendEntity = (ContactEntity)getIntent().getExtras().getSerializable("bean");
        adapter = new ShareCardContactAdapter();
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(itemClickListener);
        siderbar.setOnTouchingLetterChangedListener(new SideBar.OnTouchingLetterChangedListener() {
            @Override
            public void onTouchingLetterChanged(String s) {
                int position = adapter.getPositionForSection(s.charAt(0));
                if(position >= 0){
                    listView.setSelection(position);
                }
            }
        });
        updataContact();
    }

    @OnClick(R.id.left_img)
    void goback(View view) {
        ActivityUtil.goBack(mActivity);
    }

    private AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener(){
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            final ContactBean contactBean = (ContactBean) parent.getAdapter().getItem(position);
            DialogUtil.showAlertTextView(mActivity,
                    mActivity.getResources().getString(R.string.Chat_Share_contact),
                    mActivity.getString(R.string.Chat_Share_contact_to,friendEntity.getUsername(),contactBean.getName()),
                    "", "", false, new DialogUtil.OnItemClickListener() {
                        @Override
                        public void confirm(String value) {
                            BaseChat baseChat;
                            if (contactBean.getStatus() == 2) {
                                GroupEntity groupEntity = ContactHelper.getInstance().loadGroupEntity(contactBean.getPub_key());
                                baseChat=new GroupChat(groupEntity);
                            }else{
                                ContactEntity acceptFriend = ContactHelper.getInstance().loadFriendEntity(contactBean.getPub_key());
                                baseChat=new FriendChat(acceptFriend);
                            }
                            MsgEntity msgEntity = (MsgEntity) baseChat.cardMsg(friendEntity);
                            baseChat.sendPushMsg(msgEntity);
                            MessageHelper.getInstance().insertToMsg(msgEntity.getMsgDefinBean());
                            baseChat.updateRoomMsg(null, BaseApplication.getInstance().getBaseContext().getString(R.string.Chat_Visting_card), msgEntity.getMsgDefinBean().getSendtime());

                            List<Activity> list = BaseApplication.getInstance().getActivityList();
                            for (Activity activity : list) {
                                if (activity.getClass().getName().equals(ShareCardActivity.class.getName())){
                                    activity.finish();
                                }
                            }
                            ActivityUtil.goBack(mActivity);
                        }

                        @Override
                        public void cancel() {

                        }
                    });
        }
    };

    private void updataContact() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                ContactListManage contactManage = new ContactListManage();
                groupList = contactManage.getGroupData();
                friendMap = contactManage.getFriendListExcludeSys(friendEntity.getPub_key());
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
        finalList.addAll(friendMap.get("favorite"));
        finalList.addAll(groupList);
        finalList.addAll(friendMap.get("friend"));
        adapter.setStartPosition(finalList.size() - friendMap.get("friend").size());
        adapter.setDataNotify(finalList);
    }

}
