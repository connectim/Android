package connect.ui.activity.common.selefriend;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.io.Serializable;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import connect.db.green.DaoHelper.ConversionHelper;
import connect.db.green.bean.ContactEntity;
import connect.ui.activity.R;
import connect.ui.activity.common.bean.ConverType;
import connect.ui.activity.common.adapter.ConversationAdapter;
import connect.ui.activity.home.bean.RoomAttrBean;
import connect.ui.base.BaseActivity;
import connect.utils.ActivityUtil;
import connect.utils.DialogUtil;
import connect.view.TopToolBar;

/**
 * The recent session selection
 */
public class ConversationActivity extends BaseActivity {

    @Bind(R.id.toolbar)
    TopToolBar toolbar;
    @Bind(R.id.create_chat_lin)
    LinearLayout createChatLin;
    @Bind(R.id.lately_title_tv)
    TextView latelyTitleTv;
    @Bind(R.id.list_view)
    ListView listView;

    public static final int CODE_REQUEST = 512;

    private ConversationActivity activity;
    private ConverType converType;
    private Serializable serializables;

    private ConversationAdapter conversationAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation);
        ButterKnife.bind(this);
        initView();
    }

    public static void startActivity(Activity activity, ConverType converType, Serializable... serializables) {
        Bundle bundle = new Bundle();
        bundle.putSerializable("converType", converType);
        if (serializables != null) {
            bundle.putSerializable("Serializable", serializables);
        }
        ActivityUtil.next(activity, ConversationActivity.class, bundle, CODE_REQUEST);
    }

    @Override
    public void initView() {
        activity=this;
        toolbar.setBlackStyle();
        toolbar.setLeftImg(R.mipmap.back_white);
        toolbar.setLeftListence(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityUtil.goBack(activity);
            }
        });

        converType = (ConverType) getIntent().getSerializableExtra("converType");
        serializables = getIntent().getSerializableExtra("Serializable");
        switch (converType) {
            case URL://share
                toolbar.setTitle(getString(R.string.Link_Share));
                break;
            case TRANSPOND://transpond
                toolbar.setTitle(getString(R.string.Chat_Message_retweet));
                break;
            case CAED:
                toolbar.setTitle(getString(R.string.Chat_Share_contact));
                break;
        }

        conversationAdapter = new ConversationAdapter();
        listView.setAdapter(conversationAdapter);
        listView.setOnItemClickListener(itemClickListener);
        loadConverChats();
    }

    /**
     * Query the recent chats
     */
    private void loadConverChats() {
        new AsyncTask<Void, Void, List<RoomAttrBean>>() {
            @Override
            protected List<RoomAttrBean> doInBackground(Void... params) {
                return ConversionHelper.getInstance().loadRecentRoomEntities();
            }

            @Override
            protected void onPostExecute(List<RoomAttrBean> roomAttrBeen) {
                super.onPostExecute(roomAttrBeen);
                conversationAdapter.setDataNotify(roomAttrBeen);
            }
        }.execute();
    }

    private AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            final RoomAttrBean roomAttrBean = (RoomAttrBean) parent.getAdapter().getItem(position);

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
                    Object[] objects = (Object[]) serializables;
                    ContactEntity contactEntity  = (ContactEntity)objects[0];
                    message = getString(R.string.Chat_Share_contact_to, contactEntity.getUsername(), roomAttrBean.getName());
                    break;
                default:
                    break;
            }

            DialogUtil.showAlertTextView(activity, title, message,
                    "", "", false, new DialogUtil.OnItemClickListener() {
                        @Override
                        public void confirm(String value) {
                            backActivity(roomAttrBean.getRoomtype(), roomAttrBean.getRoomid());
                        }

                        @Override
                        public void cancel() {

                        }
                    });
        }
    };

    @OnClick(R.id.create_chat_lin)
    public void OnClickListener(View view) {
        switch (view.getId()) {
            case R.id.create_chat_lin:
                NewConversationActivity.startActivity(activity, converType,serializables);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == CODE_REQUEST) {
            backActivity(data.getIntExtra("type", 0), data.getStringExtra("object"));
        }
    }

    /**
     *
     * @param type 0:friend 1:group
     * @param pubkey
     */
    public void backActivity(int type, String pubkey) {
        Bundle bundle = new Bundle();
        bundle.putInt("type", type);
        bundle.putString("object", pubkey);
        if (serializables != null) {
            bundle.putSerializable("Serializable", serializables);
        }
        ActivityUtil.goBackWithResult(activity, CODE_REQUEST, bundle);
    }
}
