package connect.activity.common.selefriend;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import butterknife.Bind;
import butterknife.ButterKnife;
import connect.activity.base.BaseActivity;
import connect.activity.common.adapter.NewConversationAdapter;
import connect.activity.common.bean.ConverType;
import connect.activity.contact.model.ContactListManage;
import connect.activity.home.bean.ContactBean;
import connect.database.green.bean.ContactEntity;
import connect.ui.activity.R;
import connect.utils.ActivityUtil;
import connect.utils.DialogUtil;
import connect.widget.SideBar;
import connect.widget.TopToolBar;

/**
 * New friends reply
 */
public class NewConversationActivity extends BaseActivity {

    @Bind(R.id.toolbar)
    TopToolBar toolbar;
    @Bind(R.id.siderbar)
    SideBar siderbar;
    @Bind(R.id.recyclerview)
    RecyclerView recyclerview;

    private static final int CODE_REQUEST = 512;

    private NewConversationActivity activity;
    private List<ContactBean> groupList;
    private HashMap<String, List<ContactBean>> friendMap;
    private NewConversationAdapter adapter;

    private LinearLayoutManager linearLayoutManager;
    private ConverType converType;
    private Serializable serializable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_conversation);
        ButterKnife.bind(this);
        initView();
    }

    public static void startActivity(Activity activity, ConverType converType, Serializable serializable) {
        Bundle bundle = new Bundle();
        bundle.putSerializable("converType", converType);
        if (serializable != null) {
            bundle.putSerializable("Serializable", serializable);
        }
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

        converType = (ConverType) getIntent().getSerializableExtra("converType");
        serializable = getIntent().getSerializableExtra("Serializable");

        String title = "";
        switch (converType) {
            case URL:
                title = getString(R.string.Link_Share);
                break;
            case TRANSPOND:
                title = getString(R.string.Chat_Message_retweet);
                break;
            case CAED:
                title = getString(R.string.Chat_Share_contact);
                break;
            default:
                break;
        }
        toolbar.setTitle(title);

        linearLayoutManager = new LinearLayoutManager(activity);
        recyclerview.setLayoutManager(linearLayoutManager);
        adapter = new NewConversationAdapter(activity);
        recyclerview.setAdapter(adapter);
        adapter.setItemClickListener(new NewConversationAdapter.OnItemClickListener() {
            @Override
            public void itemClick(final ContactBean roomAttrBean) {
                String title = "";
                String message = "";
                switch (converType) {
                    case URL:
                        message = getString(R.string.Link_Share_to, roomAttrBean.getName());
                        break;
                    case TRANSPOND:
                        message = getString(R.string.Link_Send_to, roomAttrBean.getName());
                        break;
                    case CAED:
                        Object[] objects = (Object[]) serializable;
                        ContactEntity contactEntity = (ContactEntity) objects[0];
                        message = getString(R.string.Chat_Share_contact_to, contactEntity.getUsername(), roomAttrBean.getName());
                        break;
                    default:
                        break;
                }

                DialogUtil.showAlertTextView(activity, title, message,
                        "", "", false, new DialogUtil.OnItemClickListener() {
                            @Override
                            public void confirm(String value) {
                                backActivity(TextUtils.isEmpty(roomAttrBean.getUid()) ? 1 : 0, roomAttrBean.getPub_key());
                            }

                            @Override
                            public void cancel() {

                            }
                        });
            }
        });

        siderbar.setOnTouchingLetterChangedListener(new SideBar.OnTouchingLetterChangedListener() {
            @Override
            public void onTouchingLetterChanged(String s) {
                int position = adapter.getPositionForSection(s.charAt(0));
                if (position >= 0) {
                    linearLayoutManager.scrollToPosition(position);
                }
            }
        });
        updataContact();
    }

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
        ActivityUtil.goBackWithResult(activity, CODE_REQUEST, bundle);
    }
}
