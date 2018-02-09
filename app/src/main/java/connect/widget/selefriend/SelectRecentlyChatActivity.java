package connect.widget.selefriend;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.Serializable;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import connect.activity.base.BaseActivity;
import connect.activity.home.bean.RoomAttrBean;
import connect.database.green.DaoHelper.ConversionHelper;
import connect.ui.activity.R;
import connect.utils.ActivityUtil;
import connect.utils.dialog.DialogUtil;
import connect.widget.TopToolBar;
import connect.widget.selefriend.adapter.RecentlyChatAdapter;

/**
 * 选择最近聊天
 */

public class SelectRecentlyChatActivity extends BaseActivity {

    @Bind(R.id.toolbar)
    TopToolBar toolbar;
    @Bind(R.id.create_chat_lin)
    LinearLayout createChatLin;
    @Bind(R.id.lately_title_tv)
    TextView latelyTitleTv;
    @Bind(R.id.recyclerview)
    RecyclerView recyclerview;

    private SelectRecentlyChatActivity mActivity;
    private RecentlyChatAdapter adapter;
    public static final String SHARE_CARD = "share_card";
    public static final String TRANSPOND = "transpond";
    /** request activity code */
    public static final int CODE_REQUEST = 512;
    /** share_card/transpond*/
    private String type;

    public static void startActivity(Activity activity, String type, Serializable... extension) {
        Bundle bundle = new Bundle();
        bundle.putString("type", type);
        if (extension != null) {
            bundle.putSerializable("extension", extension);
        }
        ActivityUtil.next(activity, SelectRecentlyChatActivity.class, bundle, CODE_REQUEST);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation);
        ButterKnife.bind(this);
        initView();
    }

    @Override
    public void initView() {
        mActivity = this;
        toolbar.setBlackStyle();
        toolbar.setTitle(null, R.string.Chat_Choose_contact);
        toolbar.setLeftImg(R.mipmap.back_white);

        type = getIntent().getExtras().getString("type");
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mActivity);
        recyclerview.setLayoutManager(linearLayoutManager);
        adapter = new RecentlyChatAdapter(mActivity);
        recyclerview.setAdapter(adapter);
        adapter.setItemClickListener(onItemClickListener);
        loadRecentlyChats();
    }

    @OnClick(R.id.left_img)
    void goBack(View view) {
        ActivityUtil.goBack(mActivity);
    }

    @OnClick(R.id.create_chat_lin)
    void selectContact(View view) {
        Serializable extension = getIntent().getSerializableExtra("extension");
        SelectContactActivity.startActivity(mActivity, type, extension);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SelectContactActivity.CODE_REQUEST && resultCode == RESULT_OK) {
            backActivity(data.getIntExtra("type", 0), data.getStringExtra("object"));
        }
    }

    /**
     * After selecting confirm the correction
     */
    RecentlyChatAdapter.OnItemClickListener onItemClickListener = new RecentlyChatAdapter.OnItemClickListener(){
        @Override
        public void itemClick(final RoomAttrBean attrBean) {
            DialogUtil.showAlertTextView(mActivity, mActivity.getString(R.string.Set_tip_title),
                    mActivity.getString(R.string.Link_Send_to, attrBean.getName()),
                    "", "", false, new DialogUtil.OnItemClickListener() {
                        @Override
                        public void confirm(String value) {
                            backActivity(attrBean.getRoomtype(), attrBean.getRoomid());
                        }

                        @Override
                        public void cancel() {}
                    });
        }
    };

    /**
     * For recent chats
     */
    private void loadRecentlyChats() {
        new AsyncTask<Void, Void, List<RoomAttrBean>>() {
            @Override
            protected List<RoomAttrBean> doInBackground(Void... params) {
                return ConversionHelper.getInstance().loadRecentRoomEntities();
            }

            @Override
            protected void onPostExecute(List<RoomAttrBean> roomAttrBeen) {
                super.onPostExecute(roomAttrBeen);
                adapter.setDataNotify(roomAttrBeen);
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
        Serializable extension = getIntent().getSerializableExtra("extension");
        if (extension != null) {
            bundle.putSerializable("Serializable", extension);
        }
        ActivityUtil.goBackWithResult(mActivity, RESULT_OK, bundle);
    }

}
