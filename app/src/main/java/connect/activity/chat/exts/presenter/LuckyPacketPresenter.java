package connect.activity.chat.exts.presenter;

import android.app.Activity;
import android.text.TextUtils;

import connect.activity.chat.bean.LinkMessageRow;
import connect.activity.chat.bean.MsgSend;
import connect.activity.chat.exts.contract.LuckyPacketContract;
import connect.activity.login.bean.UserBean;
import connect.activity.wallet.bean.TransferBean;
import connect.database.SharedPreferenceUtil;
import connect.database.green.DaoHelper.ContactHelper;
import connect.database.green.DaoHelper.ParamManager;
import connect.database.green.bean.ContactEntity;
import connect.database.green.bean.GroupEntity;
import connect.ui.activity.R;
import connect.utils.ActivityUtil;
import connect.utils.ToastEUtil;

/**
 * Created by Administrator on 2017/8/11.
 */

public class LuckyPacketPresenter implements LuckyPacketContract.Presenter{

    private LuckyPacketContract.BView view;
    private Activity activity;

    private String roomKey;
    private ContactEntity friendEntity;
    private GroupEntity groupEntity;

    public LuckyPacketPresenter(LuckyPacketContract.BView view) {
        this.view = view;
        view.setPresenter(this);
    }

    @Override
    public void start() {
        activity = view.getActivity();


    }

    @Override
    public void requestRoomEntity(int roomtype) {
        roomKey = view.getRoomKey();
        if (roomtype == 0) {
            friendEntity = ContactHelper.getInstance().loadFriendEntity(roomKey);
            if (friendEntity == null) {
                UserBean userBean = SharedPreferenceUtil.getInstance().getUser();
                if (userBean.getPubKey().equals(roomKey)) {
                    friendEntity = new ContactEntity();
                    friendEntity.setAvatar(userBean.getAvatar());
                    friendEntity.setUsername(userBean.getName());
                    friendEntity.setUid(userBean.getUid());
                } else {
                    ActivityUtil.goBack(activity);
                    return;
                }
            }

            String avatar = friendEntity.getAvatar();
            String nameTxt = TextUtils.isEmpty(friendEntity.getRemark()) ? friendEntity.getUsername() : friendEntity.getRemark();
            view.showUserInfo(avatar, nameTxt);
        } else if (roomtype == 1) {
            groupEntity = ContactHelper.getInstance().loadGroupEntity(roomKey);
            if (groupEntity == null) {
                ActivityUtil.goBack(activity);
                return;
            }
            int countMember = ContactHelper.getInstance().loadGroupMemEntities(roomKey).size();
            view.showGroupInfo(countMember);
        }
    }

    @Override
    public void sendLuckyPacket(final int roomtype, int packetcount, final long amount, final String tips) {
    }
}
