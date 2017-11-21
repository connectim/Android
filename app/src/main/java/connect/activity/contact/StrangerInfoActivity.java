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
import connect.database.SharedPreferenceUtil;
import connect.database.green.DaoHelper.ContactHelper;
import connect.database.green.bean.FriendRequestEntity;
import connect.ui.activity.R;
import connect.activity.chat.exts.TransferToActivity;
import connect.activity.contact.bean.MsgSendBean;
import connect.activity.contact.bean.SourceType;
import connect.activity.home.bean.MsgNoticeBean;
import connect.activity.base.BaseActivity;
import connect.utils.ActivityUtil;
import connect.utils.DialogUtil;
import connect.utils.ProtoBufUtil;
import connect.utils.ToastEUtil;
import connect.utils.UriUtil;
import connect.utils.cryption.DecryptionUtil;
import instant.bean.UserOrderBean;
import connect.utils.glide.GlideUtil;
import connect.utils.okhttp.OkHttpUtil;
import connect.utils.okhttp.ResultCall;
import connect.widget.TopToolBar;
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
    @Bind(R.id.source_tv)
    TextView sourceTv;

    private StrangerInfoActivity mActivity;
    private Connect.UserInfo sendUserInfo;
    private SourceType sourceType;
    private String uid;

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
        nameTv.setText(sendUserInfo.getUsername());
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
        DialogUtil.showEditView(mActivity, mActivity.getResources().getString(R.string.Link_Send_friend_request),
                "", mActivity.getResources().getString(R.string.Link_Send), "", "", getString(R.string.Link_Hello_I_am, SharedPreferenceUtil.getInstance().getUser().getName()),
                false,-1,new DialogUtil.OnItemClickListener() {
                    @Override
                    public void confirm(String value) {
                        MsgSendBean msgSendBean = new MsgSendBean();
                        msgSendBean.setType(MsgSendBean.SendType.TypeSendFriendQuest);
                        msgSendBean.setTips(value);

                        UserOrderBean userOrderBean = new UserOrderBean();
                        userOrderBean.requestAddFriend(sendUserInfo.getUid(),sendUserInfo.getPubKey(),value,sourceType.getType(),msgSendBean);
                    }

                    @Override
                    public void cancel() {}
                });
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
                if (msgSendBean.getType() == MsgSendBean.SendType.TypeSendFriendQuest) {
                    FriendRequestEntity requestEntity = new FriendRequestEntity();
                    requestEntity.setAvatar(sendUserInfo.getAvatar());
                    requestEntity.setUsername(sendUserInfo.getUsername());
                    requestEntity.setUid(sendUserInfo.getUid());
                    requestEntity.setSource(sourceType.getType());
                    requestEntity.setTips(msgSendBean.getTips());
                    requestEntity.setStatus(3);
                    requestEntity.setRead(1);
                    ContactHelper.getInstance().inserFriendQuestEntity(requestEntity);

                    ToastEUtil.makeText(mActivity,R.string.Link_Send_successful).show();
                    ActivityUtil.goBack(mActivity);
                }
                break;
            case MSG_SEND_FAIL:
                ToastEUtil.makeText(mActivity,R.string.Login_Send_failed,ToastEUtil.TOAST_STATUS_FAILE).show();
                break;
        }
    }

    private void requestUserInfo(String uid) {
        final Connect.SearchUser searchUser = Connect.SearchUser.newBuilder()
                .setCriteria(uid)
                .setTyp(1)
                .build();
        OkHttpUtil.getInstance().postEncrySelf(UriUtil.CONNECT_V1_USER_SEARCH, searchUser, new ResultCall<Connect.HttpResponse>() {
            @Override
            public void onResponse(Connect.HttpResponse response) {
                try {
                    Connect.IMResponse imResponse = Connect.IMResponse.parseFrom(response.getBody().toByteArray());
                    Connect.StructData structData = DecryptionUtil.decodeAESGCMStructData(imResponse.getCipherData());
                    if(structData == null || structData.getPlainData() == null){
                        return;
                    }
                    sendUserInfo = Connect.UserInfo.parseFrom(structData.getPlainData());
                    if(ProtoBufUtil.getInstance().checkProtoBuf(sendUserInfo)){
                        updataView();
                    }
                } catch (InvalidProtocolBufferException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Connect.HttpResponse response) {
                ToastEUtil.makeText(mActivity, response.getMessage(), ToastEUtil.TOAST_STATUS_FAILE).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
