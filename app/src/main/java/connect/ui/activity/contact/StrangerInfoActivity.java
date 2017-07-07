package connect.ui.activity.contact;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.protobuf.InvalidProtocolBufferException;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import connect.db.MemoryDataManager;
import connect.db.SharedPreferenceUtil;
import connect.db.green.DaoHelper.ContactHelper;
import connect.db.green.bean.FriendRequestEntity;
import connect.im.bean.UserOrderBean;
import connect.ui.activity.R;
import connect.ui.activity.chat.exts.TransferToActivity;
import connect.ui.activity.contact.bean.MsgSendBean;
import connect.ui.activity.contact.bean.SourceType;
import connect.ui.activity.home.bean.MsgNoticeBean;
import connect.ui.activity.login.bean.UserBean;
import connect.ui.base.BaseActivity;
import connect.utils.ActivityUtil;
import connect.utils.DialogUtil;
import connect.utils.ProtoBufUtil;
import connect.utils.ToastEUtil;
import connect.utils.UriUtil;
import connect.utils.cryption.DecryptionUtil;
import connect.utils.glide.GlideUtil;
import connect.utils.okhttp.OkHttpUtil;
import connect.utils.okhttp.ResultCall;
import connect.view.TopToolBar;
import connect.view.roundedimageview.RoundedImageView;
import protos.Connect;

/**
 * Created by Administrator on 2016/12/27.
 */
public class StrangerInfoActivity extends BaseActivity {

    @Bind(R.id.toolbar)
    TopToolBar toolbar;
    @Bind(R.id.avater_rimg)
    RoundedImageView avaterRimg;
    @Bind(R.id.name_tv)
    TextView nameTv;
    @Bind(R.id.address_tv)
    TextView addressTv;
    @Bind(R.id.addFriend_btn)
    Button addFriendBtn;
    @Bind(R.id.transfer_btn)
    Button transferBtn;
    @Bind(R.id.source_tv)
    TextView sourceTv;

    private StrangerInfoActivity mActivity;
    private Connect.UserInfo sendUserInfo;
    private SourceType sourceType;
    private String address;

    public static void startActivity(Activity activity, String address, SourceType sourceType) {
        Bundle bundle = new Bundle();
        bundle.putString("address", address);
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
        address = bundle.getString("address");
        sourceType = (SourceType) bundle.getSerializable("source");
        addressTv.setText(address);
        requestUserInfo(address);
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
        GlideUtil.loadAvater(avaterRimg, sendUserInfo.getAvatar());
        nameTv.setText(sendUserInfo.getUsername());
    }

    @OnClick(R.id.left_img)
    void goback(View view) {
        ActivityUtil.goBack(mActivity);
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
                    requestEntity.setPub_key(sendUserInfo.getPubKey());
                    requestEntity.setSource(sourceType.getType());
                    requestEntity.setAddress(sendUserInfo.getAddress());
                    requestEntity.setTips(msgSendBean.getTips());
                    requestEntity.setStatus(3);
                    requestEntity.setRead(1);
                    ContactHelper.getInstance().inserFriendQuestEntity(requestEntity);
                    ContactHelper.getInstance().updataRecommendFriend(requestEntity.getPub_key());

                    ToastEUtil.makeText(mActivity,R.string.Link_Send_successful).show();
                    ActivityUtil.goBack(mActivity);
                }
                break;
            case MSG_SEND_FAIL:
                ToastEUtil.makeText(mActivity,R.string.Login_Send_failed,ToastEUtil.TOAST_STATUS_FAILE).show();
                break;
        }
    }

    @OnClick(R.id.addFriend_btn)
    void goAddFriend(View view) {
        if (sendUserInfo == null) {
            return;
        }
        DialogUtil.showEditView(mActivity, mActivity.getResources().getString(R.string.Link_Send_friend_request),
                "", mActivity.getResources().getString(R.string.Link_Send), "", "", getString(R.string.Link_Hello_I_am, MemoryDataManager.getInstance().getName()), false,-1,new DialogUtil.OnItemClickListener() {
                    @Override
                    public void confirm(String value) {
                        MsgSendBean msgSendBean = new MsgSendBean();
                        msgSendBean.setType(MsgSendBean.SendType.TypeSendFriendQuest);
                        msgSendBean.setTips(value);

                        UserOrderBean userOrderBean = new UserOrderBean();
                        userOrderBean.requestAddFriend(sendUserInfo.getAddress(),sendUserInfo.getPubKey(),value,sourceType.getType(),msgSendBean);
                    }

                    @Override
                    public void cancel() {

                    }
                });
    }

    @OnClick(R.id.transfer_btn)
    void goTransfer(View view) {
        if(sendUserInfo != null){
            TransferToActivity.startActivity(mActivity, sendUserInfo.getAddress());
        }
    }

    private void requestUserInfo(String address) {
        final Connect.SearchUser searchUser = Connect.SearchUser.newBuilder()
                .setCriteria(address)
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

            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
