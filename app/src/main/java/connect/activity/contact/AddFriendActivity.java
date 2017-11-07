package connect.activity.contact;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import connect.activity.base.BaseActivity;
import connect.activity.contact.adapter.NewRequestAdapter;
import connect.activity.contact.bean.ContactNotice;
import connect.activity.contact.bean.MsgSendBean;
import connect.activity.contact.bean.SourceType;
import connect.activity.contact.contract.AddFriendContract;
import connect.activity.contact.presenter.AddFriendPresenter;
import connect.activity.home.bean.MsgNoticeBean;
import connect.activity.home.view.LineDecoration;
import connect.database.SharedPreferenceUtil;
import connect.database.green.DaoHelper.ContactHelper;
import connect.database.green.bean.ContactEntity;
import connect.database.green.bean.FriendRequestEntity;
import connect.ui.activity.R;
import connect.utils.ActivityUtil;
import connect.utils.ConfigUtil;
import connect.utils.ToastEUtil;
import connect.widget.TopToolBar;
import instant.bean.UserOrderBean;

/**
 * add new friend.
 */
public class AddFriendActivity extends BaseActivity implements AddFriendContract.View {

    @Bind(R.id.toolbar)
    TopToolBar toolbar;
    @Bind(R.id.wallet_menu_recycler)
    RecyclerView recycler;
    @Bind(R.id.recyclerview)
    RecyclerView recyclerview;

    private AddFriendActivity mActivity;
    private AddFriendContract.Presenter presenter;
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
        if (null != presenter) {
            presenter.queryFriend();
        }
    }

    @Override
    public void initView() {
        mActivity = this;
        toolbar.setBlackStyle();
        toolbar.setLeftImg(R.mipmap.back_white);
        toolbar.setTitle(null, R.string.Link_New_friend);
        new AddFriendPresenter(this).start();

        presenter.initGrid(recycler);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mActivity);
        requestAdapter = new NewRequestAdapter(mActivity);
        recyclerview.setLayoutManager(linearLayoutManager);
        recyclerview.addItemDecoration(new LineDecoration(mActivity));
        recyclerview.setAdapter(requestAdapter);
        requestAdapter.setOnAcceptListener(onAcceptListener);
        recyclerview.addOnScrollListener(onScrollListener);

        presenter.requestRecommendUser();
        presenter.updateRequestListStatus();
    }

    @OnClick(R.id.left_img)
    void goBack(View view) {
        ActivityUtil.goBack(mActivity);
    }

    @Override
    public void itemClick(int tag) {
        switch (tag) {
            case 0://scan qrcode
                ActivityUtil.nextBottomToTop(mActivity, ScanAddFriendActivity.class, null, -1);
                break;
            case 1:
                ActivityUtil.next(mActivity, AddFriendPhoneActivity.class);
                break;
            case 2://share
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.putExtra(Intent.EXTRA_TEXT, ConfigUtil.getInstance().shareCardAddress()
                        + "?address=" + SharedPreferenceUtil.getInstance().getUser().getUid());
                shareIntent.setType("text/plain");
                startActivity(Intent.createChooser(shareIntent, "share to"));
                break;
            default:
                break;
        }
    }

    RecyclerView.OnScrollListener onScrollListener = new RecyclerView.OnScrollListener(){
        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            requestAdapter.closeMenu();
        }
    };

    private NewRequestAdapter.OnAcceptListener onAcceptListener = new NewRequestAdapter.OnAcceptListener() {
        @Override
        public void accept(int position, FriendRequestEntity entity) {
            if (entity.getStatus() == 4) {
                StrangerInfoActivity.startActivity(mActivity, entity.getUid(), SourceType.RECOMMEND);
            } else {
                MsgSendBean msgSendBean = new MsgSendBean();
                msgSendBean.setType(MsgSendBean.SendType.TypeAcceptFriendQuest);
                msgSendBean.setUid(entity.getUid());
                UserOrderBean userOrderBean = new UserOrderBean();
                userOrderBean.acceptFriendRequest(entity.getUid(), entity.getSource(), msgSendBean);
            }
        }

        @Override
        public void itemClick(int position, FriendRequestEntity entity) {
            if (TextUtils.isEmpty(entity.getUid())) {
                //load more
                ActivityUtil.next(mActivity, AddFriendRecommendActivity.class);
            } else if (entity.getStatus() == 4) {
                //introduce
                StrangerInfoActivity.startActivity(mActivity, entity.getUid(), SourceType.RECOMMEND);
            } else {
                if (entity.getStatus() == 1) {
                    AddFriendAcceptActivity.startActivity(mActivity, entity);
                    return;
                }
                ContactEntity friendEntity = ContactHelper.getInstance().loadFriendEntity(entity.getUid());
                if (friendEntity == null) {
                    StrangerInfoActivity.startActivity(mActivity, entity.getUid(),
                            SourceType.getSourceType(entity.getSource()));
                } else {
                    FriendInfoActivity.startActivity(mActivity, entity.getUid());
                }
            }
        }

        @Override
        public void deleteItem(int position, FriendRequestEntity entity) {
            requestAdapter.closeMenu();
            if (entity.getStatus() == 4) {
                //introduce
                //qwert
            } else {
                ContactHelper.getInstance().deleteRequestEntity(entity.getUid());
                presenter.queryFriend();
                ContactNotice.receiverAddFriend();
            }
        }
    };

    /**
     * The message returns the result
     *
     * @param notice result
     */
    @Subscribe
    public void onEventMainThread(MsgNoticeBean notice) {
        Object[] objs = null;
        if (notice.object != null) {
            objs = (Object[]) notice.object;
        }
        switch (notice.ntEnum) {
            case MSG_SEND_SUCCESS:
                MsgSendBean sendBean = (MsgSendBean) objs[0];
                if (sendBean.getType() == MsgSendBean.SendType.TypeAcceptFriendQuest) {
                    presenter.updateRequestAddSuccess(ContactHelper.getInstance().loadFriendRequest(sendBean.getUid()));
                }
                break;
            case MSG_SEND_FAIL:
                ToastEUtil.makeText(mActivity, R.string.Link_Operation_failed, ToastEUtil.TOAST_STATUS_FAILE).show();
                break;
            default:
                break;
        }
    }

    @Override
    public void setPresenter(AddFriendContract.Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public Activity getActivity() {
        return mActivity;
    }

    /**
     * After the access to the data update interface
     *
     * @param sizeRecommend recommend count
     * @param listFina list data
     */
    @Override
    public void notifyData(int sizeRecommend, ArrayList<FriendRequestEntity> listFina) {
        requestAdapter.setRecommendCount(sizeRecommend);
        requestAdapter.setDataNotify(listFina);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
