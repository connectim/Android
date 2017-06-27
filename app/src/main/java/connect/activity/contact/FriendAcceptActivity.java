package connect.activity.contact;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import connect.database.green.DaoHelper.ContactHelper;
import connect.database.green.bean.FriendRequestEntity;
import connect.im.bean.UserOrderBean;
import connect.ui.activity.R;
import connect.activity.contact.bean.MsgSendBean;
import connect.activity.contact.bean.SourceType;
import connect.activity.home.bean.MsgNoticeBean;
import connect.activity.base.BaseActivity;
import connect.utils.ActivityUtil;
import connect.utils.ToastEUtil;
import connect.utils.glide.GlideUtil;
import connect.widget.TopToolBar;
import connect.widget.roundedimageview.RoundedImageView;

/**
 * Created by Administrator on 2016/12/29.
 */
public class FriendAcceptActivity extends BaseActivity {

    @Bind(R.id.toolbar)
    TopToolBar toolbar;
    @Bind(R.id.source_tv)
    TextView sourceTv;
    @Bind(R.id.avater_rimg)
    RoundedImageView avaterRimg;
    @Bind(R.id.name_tv)
    TextView nameTv;
    @Bind(R.id.tips_tv)
    TextView tipsTv;
    @Bind(R.id.address_tv)
    TextView addressTv;
    @Bind(R.id.addFriend_btn)
    Button addFriendBtn;
    @Bind(R.id.tips_rela)
    RelativeLayout tipsRela;

    private FriendAcceptActivity mActivity;
    private FriendRequestEntity requestEntity;

    public static void startActivity(Activity activity, FriendRequestEntity requestEntity) {
        Bundle bundle = new Bundle();
        bundle.putSerializable("bean", requestEntity);
        ActivityUtil.next(activity, FriendAcceptActivity.class, bundle);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_add_accept);
        ButterKnife.bind(this);
        EventBus.getDefault().register(this);
        initView();
    }

    @Override
    public void initView() {
        mActivity = this;
        toolbar.setBlackStyle();
        toolbar.setLeftImg(R.mipmap.back_white);
        toolbar.setTitle(null, R.string.Link_New_Friends);

        requestEntity = (FriendRequestEntity) getIntent().getExtras().getSerializable("bean");
        GlideUtil.loadAvater(avaterRimg, requestEntity.getAvatar());
        nameTv.setText(requestEntity.getUsername());
        sourceTv.setText(SourceType.getString(requestEntity.getSource()));
        if (!TextUtils.isEmpty(requestEntity.getTips())) {
            tipsRela.setVisibility(View.VISIBLE);
            tipsRela.setBackgroundResource(R.mipmap.message_box_white2x);
            tipsTv.setText(requestEntity.getTips());
        }

        addressTv.setText(requestEntity.getAddress());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(MsgNoticeBean notice) {
        Object[] objs = null;
        if (notice.object != null) {
            objs = (Object[]) notice.object;
        }
        switch (notice.ntEnum) {
            case MSG_SEND_SUCCESS:
                MsgSendBean sendBean = (MsgSendBean) objs[0];
                if (sendBean.getType() == MsgSendBean.SendType.TypeAcceptFriendQuest) {
                    requestEntity.setRead(1);
                    requestEntity.setStatus(2);
                    ContactHelper.getInstance().inserFriendQuestEntity(requestEntity);
                    ToastEUtil.makeText(mActivity,R.string.Link_Add_Successful).show();
                    ActivityUtil.goBack(mActivity);
                }
                break;
            case MSG_SEND_FAIL:
                ToastEUtil.makeText(mActivity,R.string.Link_Add_Failed,ToastEUtil.TOAST_STATUS_FAILE).show();
                break;
        }
    }

    @OnClick(R.id.left_img)
    void goback(View view) {
        ActivityUtil.goBack(mActivity);
    }

    @OnClick(R.id.addFriend_btn)
    void goAccept(View view) {
        MsgSendBean msgSendBean = new MsgSendBean();
        msgSendBean.setType(MsgSendBean.SendType.TypeAcceptFriendQuest);

        UserOrderBean userOrderBean = new UserOrderBean();
        userOrderBean.acceptFriendRequest(requestEntity.getAddress(), requestEntity.getSource(), msgSendBean);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
