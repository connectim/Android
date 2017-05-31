package connect.ui.activity.common;

import android.app.Activity;
import android.content.Intent;
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
import connect.db.green.bean.ContactEntity;
import connect.ui.activity.R;
import connect.ui.activity.common.bean.ConverType;
import connect.ui.activity.contact.adapter.ShareCardContactAdapter;
import connect.ui.activity.contact.model.ContactListManage;
import connect.ui.activity.home.bean.ContactBean;
import connect.ui.base.BaseActivity;
import connect.utils.ActivityUtil;
import connect.utils.DialogUtil;
import connect.view.SideBar;
import connect.view.TopToolBar;

/**
 * New friends reply
 */
public class NewConversationActivity extends BaseActivity {

    @Bind(R.id.toolbar)
    TopToolBar toolbar;
    @Bind(R.id.list_view)
    ListView listView;
    @Bind(R.id.siderbar)
    SideBar siderbar;

    private static final int CODE_REQUEST = 512;

    private NewConversationActivity activity;
    private List<ContactBean> groupList;
    private HashMap<String, List<ContactBean>> friendMap;
    private ShareCardContactAdapter adapter;

    private ConverType converType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_conversation);
        ButterKnife.bind(this);
        initView();
    }

    public static void startActivity(Activity activity, ConverType converType) {
        Bundle bundle = new Bundle();
        bundle.putSerializable("converType", converType);
        ActivityUtil.next(activity, NewConversationActivity.class, bundle, CODE_REQUEST);
    }

    @Override
    public void initView() {
        activity = this;
        toolbar.setBlackStyle();
        toolbar.setLeftImg(R.mipmap.back_white);
        toolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityUtil.goBack(activity);
            }
        });

        converType= (ConverType) getIntent().getSerializableExtra("converType");

        String title = "";
        switch (converType) {
            case URL:
                title = getString(R.string.Link_Share);
                break;
            case TRANSPOND:
                title = getString(R.string.Chat_Message_retweet);
                break;
        }
        toolbar.setTitle(title);

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

    private AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener(){
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            final ContactBean roomAttrBean = (ContactBean)parent.getAdapter().getItem(position);

            String title = "";
            String message = "";
            switch (converType) {
                case URL:
                    message = getString(R.string.Link_Share_to, roomAttrBean.getName());
                    break;
                case TRANSPOND:
                    message = getString(R.string.Link_Send_to, roomAttrBean.getName());
                    break;
            }

            DialogUtil.showAlertTextView(activity,title,message,
                    "", "", false, new DialogUtil.OnItemClickListener() {
                        @Override
                        public void confirm(String value) {
                            backActivity(TextUtils.isEmpty(roomAttrBean.getAddress())?1:0,roomAttrBean.getPub_key());
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
                friendMap = contactManage.getFriendListExcludeSys("");
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

    public void backActivity(int type, String pubkey) {
        Bundle bundle = new Bundle();
        bundle.putInt("type", type);
        bundle.putString("object", pubkey);
        ActivityUtil.goBackWithResult(activity,CODE_REQUEST,bundle);
    }
}
