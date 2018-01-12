package connect.activity.contact;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.protobuf.InvalidProtocolBufferException;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import connect.activity.base.BaseActivity;
import connect.activity.chat.model.GroupMemberUtil;
import connect.activity.contact.bean.MsgSendBean;
import connect.activity.contact.bean.SourceType;
import connect.activity.home.bean.MsgNoticeBean;
import connect.database.SharedPreferenceUtil;
import connect.database.green.DaoHelper.ContactHelper;
import connect.database.green.bean.FriendRequestEntity;
import connect.ui.activity.R;
import connect.utils.ActivityUtil;
import connect.utils.ToastEUtil;
import connect.utils.UriUtil;
import connect.utils.glide.GlideUtil;
import connect.utils.okhttp.OkHttpUtil;
import connect.utils.okhttp.ResultCall;
import connect.widget.TopToolBar;
import instant.bean.UserOrderBean;
import protos.Connect;

/**
 * Stranger details
 */
public class StrangerInfoActivity extends BaseActivity {

    @Bind(R.id.toolbar)
    TopToolBar toolbar;
    @Bind(R.id.avatar_rimg)
    ImageView avatarRimg;
    @Bind(R.id.name_tv)
    TextView nameTv;
    @Bind(R.id.address_tv)
    TextView addressTv;
    @Bind(R.id.addFriend_btn)
    Button addFriendBtn;
    @Bind(R.id.department_tv)
    TextView departmentTv;

    private StrangerInfoActivity mActivity;
    private Connect.UserInfo sendUserInfo;
    private SourceType sourceType;
    private String uid;
    private boolean isHaveFriend;

    public static void startActivity(Activity activity, String uid, SourceType sourceType) {
        Bundle bundle = new Bundle();
        bundle.putString("uid", uid);
        bundle.putSerializable("source", sourceType);
        ActivityUtil.next(activity, StrangerInfoActivity.class, bundle);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_stranger_info);
        ButterKnife.bind(this);
        initView();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Bundle bundle = getIntent().getExtras();
        uid = bundle.getString("uid");
        sourceType = (SourceType) bundle.getSerializable("source");
        addressTv.setText(uid);
        requestUserInfo(uid);
    }

    @Override
    public void initView() {
        mActivity = this;
        toolbar.setBlackStyle();
        toolbar.setLeftImg(R.mipmap.back_white);
        toolbar.setTitle(null, R.string.Link_Profile);
    }

    private void updataView() {
        if (sendUserInfo == null)
            return;
        GlideUtil.loadAvatarRound(avatarRimg, sendUserInfo.getAvatar());
        nameTv.setText(sendUserInfo.getName());
        departmentTv.setText(sendUserInfo.getOu());
        if (ContactHelper.getInstance().loadFriendByUid(uid) == null) {
            isHaveFriend = false;
        } else {
            isHaveFriend = true;
        }

        String friendUid = sendUserInfo.getUid();
        String userName = sendUserInfo.getName();
        String userAvatar = sendUserInfo.getAvatar();
        ContactHelper.getInstance().updateGroupMemberNameAndAvatar(
                friendUid,
                userName,
                userAvatar);

        GroupMemberUtil.groupMemberUtil.updateGroupMemberEntity(
                friendUid,
                userName,
                userAvatar);
    }

    @OnClick(R.id.left_img)
    void goback(View view) {
        ActivityUtil.goBack(mActivity);
    }

    @OnClick(R.id.addFriend_btn)
    void goAddFriend(View view) {
        if (sendUserInfo == null) {
            return;
        }
        String tips = getString(R.string.Link_Hello_I_am, SharedPreferenceUtil.getInstance().getUser().getName());
        MsgSendBean msgSendBean = new MsgSendBean();
        msgSendBean.setType(MsgSendBean.SendType.TypeSendFriendQuest);
        msgSendBean.setTips(tips);

        UserOrderBean userOrderBean = new UserOrderBean();
        userOrderBean.requestAddFriend(sendUserInfo.getUid(), sendUserInfo.getPubKey(), tips, sourceType.getType(), msgSendBean);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(MsgNoticeBean notice) {
        Object[] objs = null;
        if (notice.object != null) {
            objs = (Object[]) notice.object;
        }
        switch (notice.ntEnum) {
            case MSG_SEND_SUCCESS:
                MsgSendBean msgSendBean = (MsgSendBean) objs[0];
                Integer errorNum = (Integer) objs[1];
                if (msgSendBean.getType() == MsgSendBean.SendType.TypeSendFriendQuest) {
                    FriendRequestEntity requestEntity = new FriendRequestEntity();
                    requestEntity.setAvatar(sendUserInfo.getAvatar());
                    requestEntity.setUsername(sendUserInfo.getName());
                    requestEntity.setUid(sendUserInfo.getUid());
                    requestEntity.setSource(sourceType.getType());
                    requestEntity.setTips(msgSendBean.getTips());
                    if (isHaveFriend) {
                        requestEntity.setStatus(3);
                    } else {
                        if (errorNum == 100) {
                            requestEntity.setStatus(2);
                        } else {
                            requestEntity.setStatus(3);
                        }
                        requestEntity.setRead(1);
                        ContactHelper.getInstance().inserFriendQuestEntity(requestEntity);
                    }
                    ToastEUtil.makeText(mActivity, R.string.Link_Send_successful).show();
                    ActivityUtil.goBack(mActivity);
                }
                break;
            case MSG_SEND_FAIL:
                ToastEUtil.makeText(mActivity, R.string.Login_Send_failed, ToastEUtil.TOAST_STATUS_FAILE).show();
                break;
        }
    }

    private void requestUserInfo(String uid) {
        final Connect.SearchUser searchUser = Connect.SearchUser.newBuilder()
                .setCriteria(uid)
                .setTyp(1)
                .build();
        /*OkHttpUtil.getInstance().postEncrySelf(UriUtil.CONNECT_V1_USER_SEARCH, searchUser, new ResultCall<Connect.HttpNotSignResponse>() {
            @Override
            public void onResponse(Connect.HttpNotSignResponse response) {
                try {
                    Connect.StructData structData = Connect.StructData.parseFrom(response.getBody());
                    if (structData == null || structData.getPlainData() == null) {
                        return;
                    }
                    Connect.UsersInfo usersInfo = Connect.UsersInfo.parseFrom(structData.getPlainData());
                    sendUserInfo = usersInfo.getUsers(0);
                    if (sendUserInfo != null && !TextUtils.isEmpty(sendUserInfo.getUid())) {
                        updataView();
                    }
                } catch (InvalidProtocolBufferException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Connect.HttpNotSignResponse response) {
                ToastEUtil.makeText(mActivity, response.getMessage(), ToastEUtil.TOAST_STATUS_FAILE).show();
            }
        });*/
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
