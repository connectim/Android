package connect.activity.contact;

import android.app.Activity;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import connect.activity.base.BaseActivity;
import connect.activity.chat.ChatActivity;
import connect.activity.chat.bean.Talker;
import connect.activity.chat.exts.TransferToActivity;
import connect.activity.contact.bean.ContactNotice;
import connect.activity.contact.bean.MsgSendBean;
import connect.activity.contact.bean.SourceType;
import connect.activity.contact.contract.FriendInfoContract;
import connect.activity.contact.presenter.FriendInfoPresenter;
import connect.activity.home.bean.MsgNoticeBean;
import connect.database.green.DaoHelper.ContactHelper;
import connect.database.green.bean.ContactEntity;
import connect.ui.activity.R;
import connect.utils.ActivityUtil;
import connect.utils.DialogUtil;
import connect.utils.ToastEUtil;
import connect.utils.glide.GlideUtil;
import connect.widget.TopToolBar;
import connect.widget.selefriend.SelectRecentlyChatActivity;
import instant.bean.UserOrderBean;
import protos.Connect;

/**
 * Friends details.
 */
public class FriendInfoActivity extends BaseActivity implements FriendInfoContract.View {

    @Bind(R.id.toolbar)
    TopToolBar toolbar;
    @Bind(R.id.avatar_rimg)
    ImageView avatarRimg;
    @Bind(R.id.name_tv)
    TextView nameTv;
    @Bind(R.id.message_img)
    ImageView messageImg;
    @Bind(R.id.share_img)
    ImageView shareImg;
    @Bind(R.id.address_tv)
    TextView addressTv;
    @Bind(R.id.set_alias_rela)
    RelativeLayout setAliasRela;
    @Bind(R.id.add_favorites_tb)
    View addFavoritesTb;
    @Bind(R.id.add_block_tb)
    View addBlockTb;
    @Bind(R.id.delete_friend_tv)
    TextView deleteFriendTv;
    @Bind(R.id.source_tv)
    TextView sourceTv;
    @Bind(R.id.alias_tv)
    TextView aliasTv;
    @Bind(R.id.id_lin)
    LinearLayout idLin;

    private FriendInfoActivity mActivity;
    private FriendInfoContract.Presenter presenter;
    private ContactEntity friendEntity;
    private String uid;

    public static void startActivity(Activity activity, String uid) {
        Bundle bundle = new Bundle();
        bundle.putSerializable("uid", uid);
        ActivityUtil.next(activity, FriendInfoActivity.class, bundle);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_friend_info);
        ButterKnife.bind(this);
        EventBus.getDefault().register(this);
        initView();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (null != presenter) {
            friendEntity = ContactHelper.getInstance().loadFriendEntity(uid);
            updateView(friendEntity);
        }
    }

    @Override
    public void initView() {
        mActivity = this;
        toolbar.setBlackStyle();
        toolbar.setLeftImg(R.mipmap.back_white);
        toolbar.setTitle(null, R.string.Link_Profile);

        Bundle bundle = getIntent().getExtras();
        uid = bundle.getString("uid");
        friendEntity = ContactHelper.getInstance().loadFriendEntity(uid);

        new FriendInfoPresenter(this).start();
        presenter.requestUserInfo(friendEntity.getUid(), friendEntity);
    }

    @Override
    public void updateView(ContactEntity friendEntity) {
        this.friendEntity = friendEntity;
        GlideUtil.loadAvatarRound(avatarRimg, (null == friendEntity || null == friendEntity.getAvatar()) ? "" : friendEntity.getAvatar());
        nameTv.setText(friendEntity.getUsername());
        addressTv.setText(friendEntity.getUid());
        aliasTv.setText(TextUtils.isEmpty(friendEntity.getRemark()) ? "" : friendEntity.getRemark());
        addBlockTb.setSelected(friendEntity.getBlocked() == null ? false : friendEntity.getBlocked());
    }

    @OnClick(R.id.left_img)
    void goBack(View view) {
        ActivityUtil.goBack(mActivity);
    }

    @OnClick(R.id.avatar_rimg)
    void goImage(View view) {
        presenter.getImageWatcher().showSingle((ImageView) view, avatarRimg, friendEntity.getAvatar() + "?size=400");
    }

    @OnClick(R.id.id_lin)
    void copyAddress(View view) {
        ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        cm.setText(friendEntity.getConnectId());
        ToastEUtil.makeText(mActivity, R.string.Set_Copied).show();
    }

    @OnClick(R.id.message_img)
    void goSendMessage(View view) {
        ChatActivity.startActivity(mActivity, new Talker(Connect.ChatType.PRIVATE, friendEntity.getUid()));
    }

    @OnClick(R.id.share_img)
    void goSendShare(View view) {
        SelectRecentlyChatActivity.startActivity(mActivity, SelectRecentlyChatActivity.SHARE_CARD, friendEntity);
    }

    @OnClick(R.id.set_alias_rela)
    void goSetAlias(View view) {
        FriendInfoAliasActivity.startActivity(mActivity, friendEntity.getUid());
    }

    @OnClick(R.id.add_favorites_tb)
    void switchFavorites(View view) {
        boolean isSelect = addFavoritesTb.isSelected();
        MsgSendBean msgSendBean = new MsgSendBean();
        msgSendBean.setType(MsgSendBean.SendType.TypeAddFavorites);
        msgSendBean.setCommon(!isSelect);

        UserOrderBean userOrderBean = new UserOrderBean();
        userOrderBean.settingFriend(friendEntity.getUid(), !isSelect ? "common" : "common_del", !isSelect, "", msgSendBean);
    }

    @OnClick(R.id.add_block_tb)
    void switchBlock(View view) {
        boolean isSelect = addBlockTb.isSelected();
        MsgSendBean msgSendBean = new MsgSendBean();
        msgSendBean.setType(MsgSendBean.SendType.TypeFriendBlock);
        msgSendBean.setBlack(!isSelect);

        UserOrderBean userOrderBean = new UserOrderBean();
        userOrderBean.settingFriend(friendEntity.getUid(), !isSelect ? "black" : "black_del", !isSelect, "", msgSendBean);
    }

    @OnClick(R.id.delete_friend_tv)
    void goDeleteFriend(View view) {
        ArrayList<String> list = new ArrayList<>();
        list.add(mActivity.getResources().getString(R.string.Link_Delete_This_Friend));
        DialogUtil.showBottomView(mActivity, list, new DialogUtil.DialogListItemClickListener() {
            @Override
            public void confirm(int position) {
                switch (position) {
                    case 0:
                        MsgSendBean msgSendBean = new MsgSendBean();
                        msgSendBean.setUid(friendEntity.getUid());
                        msgSendBean.setType(MsgSendBean.SendType.TypeDeleteFriend);

                        UserOrderBean userOrderBean = new UserOrderBean();
                        userOrderBean.removeRelation(friendEntity.getUid(), msgSendBean);
                        break;
                    default:
                        break;
                }
            }
        });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(MsgNoticeBean notice) {
        presenter.checkOnEvent(notice);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SelectRecentlyChatActivity.CODE_REQUEST && resultCode == RESULT_OK) {
            presenter.shareFriendCard(mActivity, data, friendEntity);
        }
    }

    @Override
    public void setPresenter(FriendInfoContract.Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public Activity getActivity() {
        return mActivity;
    }

    @Override
    public void setCommon(boolean isCommon) {
        friendEntity.setCommon(isCommon ? 1 : 0);
        addFavoritesTb.setSelected(isCommon);
        ContactHelper.getInstance().updataFriendSetEntity(friendEntity);
        ContactNotice.receiverFriend();
    }

    @Override
    public void setBlack(boolean black) {
        addBlockTb.setSelected(black);
        friendEntity.setBlocked(black);
        ContactHelper.getInstance().updataFriendSetEntity(friendEntity);
        ContactNotice.receiverFriend();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

}
