package connect.activity.contact;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import connect.database.MemoryDataManager;
import connect.database.green.DaoHelper.ContactHelper;
import connect.database.green.bean.ContactEntity;
import connect.database.green.bean.FriendRequestEntity;
import connect.im.bean.UserOrderBean;
import connect.ui.activity.R;
import connect.activity.contact.adapter.NewRequestAdapter;
import connect.activity.contact.bean.MsgSendBean;
import connect.activity.contact.bean.SourceType;
import connect.activity.contact.contract.NewFriendContract;
import connect.activity.contact.presenter.NewFriendPresenter;
import connect.activity.home.bean.MsgNoticeBean;
import connect.activity.base.BaseActivity;
import connect.utils.ActivityUtil;
import connect.utils.ConfigUtil;
import connect.utils.ToastEUtil;
import connect.widget.TopToolBar;


/**
 * add new friend
 * Created by Administrator on 2016/12/22.
 */
public class NewFriendActivity extends BaseActivity implements NewFriendContract.View {

    @Bind(R.id.toolbar)
    TopToolBar toolbar;
    @Bind(R.id.wallet_menu_recycler)
    RecyclerView recycler;
    @Bind(R.id.list_view)
    ListView listView;

    private NewFriendActivity mActivity;
    private NewFriendContract.Presenter presenter;
    private NewRequestAdapter requestAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_add_friend);
        ButterKnife.bind(this);
        EventBus.getDefault().register(this);
        initView();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(null != presenter)
            presenter.queryFriend();
    }

    @Override
    public void initView() {
        mActivity = this;
        toolbar.setBlackStyle();
        toolbar.setLeftImg(R.mipmap.back_white);
        toolbar.setTitle(null, R.string.Link_New_friend);
        setPresenter(new NewFriendPresenter(this));

        presenter.initGrid(recycler);
        initRequestList();
        presenter.updataRequestListRead();
    }

    @Override
    public void setPresenter(NewFriendContract.Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public Activity getActivity() {
        return mActivity;
    }

    @OnClick(R.id.left_img)
    void goBack(View view) {
        ActivityUtil.goBack(mActivity);
    }

    @Override
    public void itemClick(int tag) {
        switch (tag) {
            case 0://scan qrcode
                ActivityUtil.nextBottomToTop(mActivity, ScanAddFriendActivity.class,null,-1);
                break;
            case 1:
                ActivityUtil.next(mActivity, FriendAddPhoneActivity.class);
                break;
            case 2://share
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.putExtra(Intent.EXTRA_TEXT, ConfigUtil.getInstance().shareCardAddress()
                        + "?address=" + MemoryDataManager.getInstance().getAddress());
                shareIntent.setType("text/plain");
                startActivity(Intent.createChooser(shareIntent, "share to"));
                break;
        }
    }

    @Subscribe
    public void onEventMainThread(MsgNoticeBean notice) {
        Object[] objs = null;
        if (notice.object != null) {
            objs = (Object[]) notice.object;
        }
        switch (notice.ntEnum) {
            case MSG_SEND_SUCCESS:
                MsgSendBean sendBean = (MsgSendBean) objs[0];
                if(sendBean.getType() == MsgSendBean.SendType.TypeAcceptFriendQuest){
                    presenter.updataFriendRequest(ContactHelper.getInstance().loadFriendRequest(sendBean.getAddress()));
                }else if(sendBean.getType() == MsgSendBean.SendType.TypeRecommendNoInterested){
                    ContactHelper.getInstance().removeRecommendEntity(sendBean.getPubkey());
                    presenter.queryFriend();
                }
                break;
            case MSG_SEND_FAIL:
                ToastEUtil.makeText(mActivity,R.string.Link_Operation_failed,ToastEUtil.TOAST_STATUS_FAILE).show();
                break;
        }
    }

    @Override
    public void notifyData(int sizeRecommend, ArrayList<FriendRequestEntity> listFina) {
        requestAdapter.setRecommendCount(sizeRecommend);
        requestAdapter.setDataNotify(listFina);
    }

    private void initRequestList() {
        requestAdapter = new NewRequestAdapter();
        listView.setAdapter(requestAdapter);
        requestAdapter.setOnAcceptListence(onAcceptListence);
        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                requestAdapter.closeMenu();
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

            }
        });
        presenter.requestRecommendUser();
    }

    private NewRequestAdapter.OnAcceptListence onAcceptListence = new NewRequestAdapter.OnAcceptListence(){
        @Override
        public void accept(int position, FriendRequestEntity entity) {
            if (entity.getStatus() == 4) {
                StrangerInfoActivity.startActivity(mActivity, entity.getAddress(), SourceType.RECOMMEND);
            } else {
                MsgSendBean msgSendBean = new MsgSendBean();
                msgSendBean.setType(MsgSendBean.SendType.TypeAcceptFriendQuest);
                msgSendBean.setAddress(entity.getAddress());

                UserOrderBean userOrderBean = new UserOrderBean();
                userOrderBean.acceptFriendRequest(entity.getAddress(), entity.getSource(), msgSendBean);
            }
        }

        @Override
        public void itemClick(int position, FriendRequestEntity entity) {
            if(TextUtils.isEmpty(entity.getPub_key())){//load more
                ActivityUtil.next(mActivity,RecommendActivity.class);
            }else if(entity.getStatus() == 4){//introduce
                StrangerInfoActivity.startActivity(mActivity,entity.getAddress(),SourceType.RECOMMEND);
            }else{
                if (entity.getStatus() == 1) {
                    FriendAcceptActivity.startActivity(mActivity, entity);
                    return;
                }
                ContactEntity friendEntity = ContactHelper.getInstance().loadFriendEntity(entity.getPub_key());
                if (friendEntity == null) {
                    StrangerInfoActivity.startActivity(mActivity, entity.getAddress(),
                            SourceType.getSourceType(entity.getSource()));
                } else {
                    FriendInfoActivity.startActivity(mActivity, entity.getPub_key());
                }
            }
        }

        @Override
        public void deleteItem(int position, FriendRequestEntity entity) {
            requestAdapter.closeMenu();
            if(entity.getStatus() == 4){//introduce
                MsgSendBean msgSendBean = new MsgSendBean();
                msgSendBean.setType(MsgSendBean.SendType.TypeRecommendNoInterested);
                msgSendBean.setPubkey(entity.getPub_key());

                UserOrderBean userOrderBean = new UserOrderBean();
                userOrderBean.noInterested(entity.getAddress(),msgSendBean);
            }else{
                ContactHelper.getInstance().deleteRequestEntity(entity.getPub_key());
                presenter.queryFriend();
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
