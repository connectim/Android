package connect.ui.activity.contact;

import android.app.Activity;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
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
import connect.db.SharedPreferenceUtil;
import connect.db.green.DaoHelper.ContactHelper;
import connect.db.green.bean.ContactEntity;
import connect.im.bean.UserOrderBean;
import connect.ui.activity.R;
import connect.ui.activity.chat.ChatActivity;
import connect.ui.activity.chat.bean.Talker;
import connect.ui.activity.chat.exts.TransferToActivity;
import connect.ui.activity.contact.bean.ContactNotice;
import connect.ui.activity.contact.bean.MsgSendBean;
import connect.ui.activity.contact.bean.SourceType;
import connect.ui.activity.contact.contract.FriendInfoContract;
import connect.ui.activity.contact.presenter.FriendInfoPresenter;
import connect.ui.activity.home.bean.MsgNoticeBean;
import connect.ui.activity.login.bean.UserBean;
import connect.ui.base.BaseActivity;
import connect.utils.ActivityUtil;
import connect.utils.DialogUtil;
import connect.utils.ToastEUtil;
import connect.utils.glide.GlideUtil;
import connect.view.TopToolBar;
import connect.view.imgviewer.ImageViewerActivity;
import connect.view.roundedimageview.RoundedImageView;

/**
 *
 * Created by Administrator on 2016/12/28.
 */
public class FriendInfoActivity extends BaseActivity implements FriendInfoContract.View {

    @Bind(R.id.toolbar)
    TopToolBar toolbar;
    @Bind(R.id.avater_rimg)
    RoundedImageView avaterRimg;
    @Bind(R.id.name_tv)
    TextView nameTv;
    @Bind(R.id.message_img)
    ImageView messageImg;
    @Bind(R.id.bitcoin_imgs)
    ImageView bitcoinImgs;
    @Bind(R.id.contact_img)
    ImageView contactImg;
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
    @Bind(R.id.tansfer_record_rela)
    RelativeLayout tansferRecordRela;

    private FriendInfoActivity mActivity;
    private FriendInfoContract.Presenter presenter;
    private ContactEntity friendEntity;

    public static void startActivity(Activity activity, String pubKey) {
        Bundle bundle = new Bundle();
        bundle.putSerializable("pubKey", pubKey);
        ActivityUtil.next(activity, FriendInfoActivity.class, bundle);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_friend_info);
        ButterKnife.bind(this);
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        initView();
    }

    @Override
    public void initView() {
        mActivity = this;
        toolbar.setBlackStyle();
        toolbar.setLeftImg(R.mipmap.back_white);
        toolbar.setTitle(null, R.string.Link_Profile);
        setPresenter(new FriendInfoPresenter(this));

        Bundle bundle = getIntent().getExtras();
        String pubKey = bundle.getString("pubKey");
        friendEntity = ContactHelper.getInstance().loadFriendEntity(pubKey);
        bindDataView();
        presenter.requestUserInfo(friendEntity.getAddress(), friendEntity);
    }

    private void bindDataView() {
        GlideUtil.loadAvater(avaterRimg, (null == friendEntity || null == friendEntity.getAvatar()) ? "" : friendEntity.getAvatar());
        nameTv.setText(friendEntity.getUsername());
        addressTv.setText(friendEntity.getAddress());
        if (friendEntity.getSource() == null) {
            sourceTv.setText(SourceType.UNKOWN.getString());
        } else {
            sourceTv.setText(SourceType.getString(friendEntity.getSource()));
        }

        if (!TextUtils.isEmpty(friendEntity.getRemark())) {
            aliasTv.setText(friendEntity.getRemark());
        } else {
            aliasTv.setText("");
        }

        if (friendEntity.getCommon() == null) {
            addFavoritesTb.setSelected(false);
        } else {
            addFavoritesTb.setSelected(friendEntity.getCommon() == 1);
        }

        if (friendEntity.getBlocked() == null) {
            addBlockTb.setSelected(false);
        } else {
            addBlockTb.setSelected(friendEntity.getBlocked());
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
    public void updataView(ContactEntity friendEntity) {
        this.friendEntity = friendEntity;
        bindDataView();
    }

    @OnClick(R.id.left_img)
    void goBack(View view) {
        ActivityUtil.goBack(mActivity);
    }

    @OnClick(R.id.avater_rimg)
    void goimage(View view) {
        ArrayList<String> imgs = new ArrayList<>();
        imgs.add(friendEntity.getAvatar() + "?size=400");
        ImageViewerActivity.startActivity(mActivity, imgs.get(0), imgs);
    }

    @OnClick(R.id.id_lin)
    void copyAddress(View view) {
        ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        cm.setText(friendEntity.getAddress());
        ToastEUtil.makeText(mActivity, R.string.Set_Copied).show();
    }

    @OnClick(R.id.message_img)
    void goSendMessage(View view) {
        ChatActivity.startActivity(mActivity, new Talker(friendEntity));
    }

    @OnClick(R.id.bitcoin_imgs)
    void goSendBitcoin(View view) {
        TransferToActivity.startActivity(mActivity, friendEntity.getAddress(), null);
    }

    @OnClick(R.id.contact_img)
    void goSendContact(View view) {
        ShareCardActivity.startActivity(mActivity, friendEntity);
    }

    @OnClick(R.id.set_alias_rela)
    void goSetAlias(View view) {
        FriendSetAliasActivity.startActivity(mActivity, friendEntity.getPub_key());
    }

    @OnClick(R.id.tansfer_record_rela)
    void goFriaendRecord(View view) {
        FriendRecordActivity.startActivity(mActivity, friendEntity);
    }

    @OnClick(R.id.add_favorites_tb)
    void switchFavorites(View view) {
        boolean isSele = addFavoritesTb.isSelected();
        MsgSendBean msgSendBean = new MsgSendBean();
        msgSendBean.setType(MsgSendBean.SendType.TypeAddFavorites);
        msgSendBean.setCommon(!isSele);
        String remark = friendEntity.getRemark() == null ? "" : friendEntity.getRemark();

        UserOrderBean userOrderBean = new UserOrderBean();
        userOrderBean.setFriend(friendEntity.getAddress(), remark, !isSele, msgSendBean);
    }

    @OnClick(R.id.add_block_tb)
    void switchBlock(View view) {
        boolean isSele = addBlockTb.isSelected();
        presenter.requestBlock(!isSele, friendEntity.getAddress());
    }

    @OnClick(R.id.delete_friend_tv)
    void goDeleteFriend(View view) {
        ArrayList<String> list = new ArrayList<>();
        list.add(mActivity.getResources().getString(R.string.Link_Delete_This_Friend));
        DialogUtil.showBottomListView(mActivity, list, new DialogUtil.DialogListItemClickListener() {
            @Override
            public void confirm(AdapterView<?> parent, View view, int position) {
                switch (position) {
                    case 0:
                        MsgSendBean msgSendBean = new MsgSendBean();
                        msgSendBean.setPubkey(friendEntity.getPub_key());
                        msgSendBean.setAddress(friendEntity.getAddress());
                        msgSendBean.setType(MsgSendBean.SendType.TypeDeleteFriend);

                        UserOrderBean userOrderBean = new UserOrderBean();
                        userOrderBean.removeRelation(friendEntity.getAddress(), msgSendBean);
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
    public void setCommon(boolean isCommon) {
        friendEntity.setCommon(isCommon ? 1 : 0);
        addFavoritesTb.setSelected(isCommon);
        ContactHelper.getInstance().updataFriendSetEntity(friendEntity);
        ContactNotice.receiverFriend();
    }

    @Override
    public void setBlock(boolean block) {
        addBlockTb.setSelected(block);
        friendEntity.setBlocked(block);
        ContactHelper.getInstance().updataFriendSetEntity(friendEntity);
        ContactNotice.receiverFriend();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

}
