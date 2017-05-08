package connect.ui.activity.contact;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import connect.db.green.DaoHelper.ContactHelper;
import connect.db.green.DaoHelper.ConversionHelper;
import connect.db.green.bean.ContactEntity;
import connect.db.green.bean.GroupEntity;
import connect.ui.activity.R;
import connect.ui.activity.chat.model.ChatOuterHelper;
import connect.ui.activity.contact.adapter.ShareCardAdapter;
import connect.ui.activity.home.bean.RoomAttrBean;
import connect.ui.base.BaseActivity;
import connect.utils.ActivityUtil;
import connect.utils.DialogUtil;
import connect.view.TopToolBar;

/**
 * Created by Administrator on 2017/2/8.
 */

public class ShareCardActivity extends BaseActivity {

    @Bind(R.id.toolbar)
    TopToolBar toolbar;
    @Bind(R.id.create_chat_lin)
    LinearLayout createChatLin;
    @Bind(R.id.lately_title_tv)
    TextView latelyTitleTv;
    @Bind(R.id.list_view)
    ListView listView;

    private ShareCardActivity mActivity;
    private ContactEntity friendEntity;
    private ShareCardAdapter shareCardAdapter;

    public static void startActivity(Activity activity, ContactEntity friendEntity) {
        Bundle bundle = new Bundle();
        bundle.putSerializable("bean", friendEntity);
        ActivityUtil.next(activity, ShareCardActivity.class, bundle);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_shared_card);
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
        shareCardAdapter = new ShareCardAdapter();
        listView.setAdapter(shareCardAdapter);
        listView.setOnItemClickListener(itemClickListener);
        loadRoomChat();
    }

    @OnClick(R.id.left_img)
    void goback(View view) {
        ActivityUtil.goBack(mActivity);
    }

    @OnClick(R.id.create_chat_lin)
    void goCreateNewChat(View view) {
        ShareCardContactActivity.startActivity(mActivity,friendEntity);
    }

    private AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener(){
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            final RoomAttrBean roomAttrBean = (RoomAttrBean)parent.getAdapter().getItem(position);
            DialogUtil.showAlertTextView(mActivity,
                    mActivity.getResources().getString(R.string.Chat_Share_contact),
                    mActivity.getString(R.string.Chat_Share_contact_to,friendEntity.getUsername(),roomAttrBean.getName()),
                    "", "", false, new DialogUtil.OnItemClickListener() {
                        @Override
                        public void confirm(String value) {
                            if(roomAttrBean.getRoomtype() == 0){
                                ContactEntity acceptFriend = ContactHelper.getInstance().loadFriendEntity(roomAttrBean.getRoomid());
                                ChatOuterHelper.sendCardTo(0,friendEntity,acceptFriend);
                            }else if(roomAttrBean.getRoomtype() == 1){
                                GroupEntity groupEntity = ContactHelper.getInstance().loadGroupEntity(roomAttrBean.getRoomid());
                                ChatOuterHelper.sendCardTo(1,friendEntity,groupEntity);
                            }
                            ActivityUtil.goBack(mActivity);
                        }

                        @Override
                        public void cancel() {

                        }
                    });
        }
    };

    private void loadRoomChat(){
        new AsyncTask<Void,Void,List<RoomAttrBean>>(){
            @Override
            protected List<RoomAttrBean> doInBackground(Void... params) {
                List<RoomAttrBean> list = ConversionHelper.getInstance().loadRecentRoomEntities();
                if(list.size() > 10){
                    return list.subList(0,9);
                }
                return list;
            }

            @Override
            protected void onPostExecute(List<RoomAttrBean> roomAttrBeen) {
                super.onPostExecute(roomAttrBeen);
                shareCardAdapter.setDataNotify(roomAttrBeen);
            }
        }.execute();
    }

}
