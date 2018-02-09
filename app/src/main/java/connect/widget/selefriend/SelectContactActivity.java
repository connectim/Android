package connect.widget.selefriend;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import connect.activity.base.BaseActivity;
import connect.activity.contact.model.ContactListManage;
import connect.activity.home.bean.ContactBean;
import connect.ui.activity.R;
import connect.utils.ActivityUtil;
import connect.utils.dialog.DialogUtil;
import connect.widget.SideBar;
import connect.widget.TopToolBar;
import connect.widget.selefriend.adapter.ContactAdapter;
import protos.Connect;

/**
 * Select the address book contacts
 */
public class SelectContactActivity extends BaseActivity {

    @Bind(R.id.toolbar)
    TopToolBar toolbar;
    @Bind(R.id.recyclerview)
    RecyclerView recyclerview;
    @Bind(R.id.side_bar)
    SideBar sideBar;

    private SelectContactActivity mActivity;
    private ContactAdapter adapter;
    private LinearLayoutManager linearLayoutManager;
    /** request activity code */
    public static final int CODE_REQUEST = 513;
    /** Common group of */
    private List<ContactBean> groupList;
    /** friend */
    private ArrayList<ContactBean> friendList;

    public static void startActivity(Activity activity, String type, Serializable... extension) {
        Bundle bundle = new Bundle();
        bundle.putString("type", type);
        if (extension != null) {
            bundle.putSerializable("extension", extension);
        }
        ActivityUtil.next(activity, SelectContactActivity.class, bundle, CODE_REQUEST);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_conversation);
        ButterKnife.bind(this);
        initView();
    }

    @Override
    public void initView() {
        mActivity = this;
        toolbar.setBlackStyle();
        toolbar.setTitle(null, R.string.Chat_Choose_contact);
        toolbar.setLeftImg(R.mipmap.back_white);

        linearLayoutManager = new LinearLayoutManager(mActivity);
        recyclerview.setLayoutManager(linearLayoutManager);
        adapter = new ContactAdapter(mActivity);
        recyclerview.setAdapter(adapter);
        adapter.setItemClickListener(onItemClickListener);

        sideBar.setOnTouchingLetterChangedListener(changedListener);
        getContactData();
    }

    @OnClick(R.id.left_img)
    void goBack(View view) {
        ActivityUtil.goBack(mActivity);
    }

    /**
     * SideBar sliding listening
     */
    SideBar.OnTouchingLetterChangedListener changedListener = new SideBar.OnTouchingLetterChangedListener(){
        @Override
        public void onTouchingLetterChanged(String s) {
            int position = adapter.getPositionForSection(s.charAt(0));
            if (position >= 0) {
                linearLayoutManager.scrollToPosition(position);
            }
        }
    };

    /**
     * After selecting confirm the correction
     */
    ContactAdapter.OnItemClickListener onItemClickListener = new ContactAdapter.OnItemClickListener(){
        @Override
        public void itemClick(final ContactBean contactBean) {
            DialogUtil.showAlertTextView(mActivity, mActivity.getString(R.string.Set_tip_title),
                    mActivity.getString(R.string.Link_Send_to, contactBean.getName()),
                    "", "", false, new DialogUtil.OnItemClickListener() {
                        @Override
                        public void confirm(String value) {
                            backActivity(contactBean.getStatus() == 2 ? Connect.ChatType.GROUP_DISCUSSION_VALUE : Connect.ChatType.PRIVATE_VALUE, contactBean.getUid());
                        }

                        @Override
                        public void cancel() {}
                    });
        }
    };

    /**
     * To obtain local contacts data
     */
    private void getContactData() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                ContactListManage contactManage = new ContactListManage();
                groupList = contactManage.getGroupData();
                friendList = contactManage.getFriendListExcludeSys("");
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                ArrayList<ContactBean> finalList = new ArrayList<>();
                finalList.addAll(groupList);
                finalList.addAll(friendList);
                adapter.setStartPosition(finalList.size() - friendList.size());
                adapter.setDataNotify(finalList);
            }
        }.execute();
    }

    /**
     * After the selected data back on an interface
     */
    public void backActivity(int type, String id) {
        Bundle bundle = new Bundle();
        bundle.putInt("type", type);
        bundle.putString("object", id);
        ActivityUtil.goBackWithResult(mActivity, RESULT_OK, bundle);
    }

}
