package connect.activity.set;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import connect.activity.base.BaseActivity;
import connect.activity.contact.bean.ContactNotice;
import connect.activity.contact.bean.MsgSendBean;
import connect.activity.home.bean.MsgNoticeBean;
import connect.activity.home.view.LineDecoration;
import connect.activity.set.adapter.BlackListAdapter;
import connect.database.green.DaoHelper.ContactHelper;
import connect.database.green.bean.ContactEntity;
import connect.ui.activity.R;
import connect.utils.ActivityUtil;
import connect.utils.ToastUtil;
import connect.widget.TopToolBar;
import instant.bean.UserOrderBean;

/**
 * The user black list
 */
public class PrivateBlackActivity extends BaseActivity {

    @Bind(R.id.toolbar_top)
    TopToolBar toolbarTop;
    @Bind(R.id.recyclerview)
    RecyclerView recyclerview;
    @Bind(R.id.no_data_lin)
    LinearLayout noDataLin;

    private PrivateBlackActivity mActivity;
    private BlackListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_black_list);
        ButterKnife.bind(this);
        EventBus.getDefault().register(this);
        initView();
    }

    @Override
    public void initView() {
        mActivity = this;
        toolbarTop.setBlackStyle();
        toolbarTop.setLeftImg(R.mipmap.back_white);
        toolbarTop.setTitle(null, R.string.Link_Black_List);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mActivity);
        recyclerview.setLayoutManager(linearLayoutManager);
        adapter = new BlackListAdapter(mActivity);
        recyclerview.addItemDecoration(new LineDecoration(mActivity));
        adapter.setOnItemChildListence(childClickListener);
        recyclerview.setAdapter(adapter);

        List<ContactEntity> list = ContactHelper.getInstance().loadFriendBlack();
        if (list == null || list.size() == 0) {
            noDataLin.setVisibility(View.VISIBLE);
            recyclerview.setVisibility(View.GONE);
        }else{
            noDataLin.setVisibility(View.GONE);
            recyclerview.setVisibility(View.VISIBLE);
            adapter.setDataNotify(list);
        }
    }

    @OnClick(R.id.left_img)
    void goBack(View view) {
        ActivityUtil.goBack(mActivity);
    }

    private BlackListAdapter.OnItemChildClickListener childClickListener = new BlackListAdapter.OnItemChildClickListener() {
        @Override
        public void remove(int position, ContactEntity userInfo) {
            MsgSendBean msgSendBean = new MsgSendBean();
            msgSendBean.setType(MsgSendBean.SendType.TypeFriendBlock);
            msgSendBean.setUid(userInfo.getUid());
            msgSendBean.setTips(position + "");

            UserOrderBean userOrderBean = new UserOrderBean();
            userOrderBean.settingFriend(userInfo.getUid(), "black_del", false, "", msgSendBean);
        }
    };

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(MsgNoticeBean notice) {
        Object[] objs = null;
        if (notice.object != null) {
            objs = (Object[]) notice.object;
        }
        switch (notice.ntEnum) {
            case MSG_SEND_SUCCESS:
                MsgSendBean sendBean = (MsgSendBean) objs[0];
                if (sendBean.getType() == MsgSendBean.SendType.TypeFriendBlock) {
                    ContactEntity friendEntity = ContactHelper.getInstance().loadFriendEntity(sendBean.getUid());
                    if (null != friendEntity) {
                        friendEntity.setBlocked(false);
                        ContactHelper.getInstance().updataFriendSetEntity(friendEntity);
                        ContactNotice.receiverFriend();
                    }
                    adapter.removeDataNotify(Integer.valueOf(sendBean.getTips()));
                }
                break;
            case MSG_SEND_FAIL:
                Integer errorCode = (Integer) objs[1];
                ToastUtil.getInstance().showToast(errorCode + "");
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

}
