package connect.ui.activity.contact;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import connect.db.green.DaoHelper.ContactHelper;
import connect.db.green.bean.ContactEntity;
import connect.im.msgdeal.SendMsgUtil;
import connect.ui.activity.R;
import connect.ui.activity.contact.bean.ContactNotice;
import connect.ui.activity.contact.bean.MsgSendBean;
import connect.ui.activity.home.bean.MsgFragmReceiver;
import connect.ui.activity.home.bean.MsgNoticeBean;
import connect.ui.base.BaseActivity;
import connect.utils.ActivityUtil;
import connect.utils.ToastUtil;
import connect.view.TopToolBar;

/**
 * Created by Administrator on 2016/12/28.
 */
public class FriendSetAliasActivity extends BaseActivity {

    @Bind(R.id.toolbar)
    TopToolBar toolbar;
    @Bind(R.id.nickname_et)
    EditText nicknameEt;
    @Bind(R.id.save_tv)
    TextView saveTv;

    private FriendSetAliasActivity mActivity;
    private ContactEntity friendEntity;

    public static void startActivity(Activity activity, ContactEntity friendEntity) {
        Bundle bundle = new Bundle();
        bundle.putSerializable("bean", friendEntity);
        ActivityUtil.next(activity, FriendSetAliasActivity.class, bundle);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_set_alias);
        ButterKnife.bind(this);
        EventBus.getDefault().register(this);
        initView();
    }

    @Override
    public void initView() {
        mActivity = this;
        toolbar.setBlackStyle();
        toolbar.setLeftImg(R.mipmap.back_white);
        toolbar.setTitle(null, R.string.Link_Set_Remark_and_Tag);

        Bundle bundle = getIntent().getExtras();
        friendEntity = (ContactEntity) bundle.getSerializable("bean");
        nicknameEt.setText(friendEntity.getRemark());
        saveTv.setEnabled(true);
    }

    @OnClick(R.id.left_img)
    void goBack(View view) {
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
                MsgSendBean sendBean = (MsgSendBean) objs[0];
                if(sendBean.getType() == MsgSendBean.SendType.TypeFriendRemark){
                    ContactHelper.getInstance().updataFriendSetEntity(friendEntity);
                    ContactNotice.receiverFriend();
                    MsgFragmReceiver.refreshRoom(MsgFragmReceiver.FragRecType.ALL);
                    ActivityUtil.goBack(mActivity);
                }
                break;
            case MSG_SEND_FAIL:
                Integer errorCode = (Integer) objs[1];
                ToastUtil.getInstance().showToast(errorCode + "");
                break;
        }
    }

    @OnClick(R.id.save_tv)
    void goSave(View view) {
        friendEntity.setRemark(nicknameEt.getText().toString());
        MsgSendBean msgSendBean = new MsgSendBean();
        msgSendBean.setType(MsgSendBean.SendType.TypeFriendRemark);
        boolean common = friendEntity.getCommon() != null && friendEntity.getCommon() == 1;
        SendMsgUtil.setFriend(friendEntity.getAddress(), friendEntity.getRemark(), common, msgSendBean);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
