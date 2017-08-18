package connect.activity.chat.exts.presenter;

import android.app.Activity;
import android.text.TextUtils;

import connect.activity.chat.bean.MsgSend;
import connect.activity.chat.exts.contract.LuckyPacketContract;
import connect.activity.wallet.bean.TransferBean;
import connect.database.MemoryDataManager;
import connect.database.green.DaoHelper.ContactHelper;
import connect.database.green.DaoHelper.ParamManager;
import connect.database.green.bean.ContactEntity;
import connect.database.green.bean.GroupEntity;
import connect.im.bean.MsgType;
import connect.ui.activity.R;
import connect.utils.ActivityUtil;
import connect.utils.ToastEUtil;
import connect.wallet.cwallet.inter.WalletListener;

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
                String mypubkey = MemoryDataManager.getInstance().getPubKey();
                if (mypubkey.equals(roomKey)) {
                    friendEntity = new ContactEntity();
                    friendEntity.setAvatar(MemoryDataManager.getInstance().getAvatar());
                    friendEntity.setUsername(MemoryDataManager.getInstance().getName());
                    friendEntity.setAddress(MemoryDataManager.getInstance().getAddress());
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
            int countMember = ContactHelper.getInstance().loadGroupMemEntity(roomKey).size();
            view.showGroupInfo(countMember);
        }
    }

    @Override
    public void sendLuckyPacket(final int roomtype, int packetcount, final long amount, final String tips) {
        view.getBusiness().luckyPacket(null, roomKey, 0, roomtype, packetcount, amount, tips, new WalletListener<String>() {
            @Override
            public void success(String hashId) {
                if (roomtype == 0) {
                    ParamManager.getInstance().putLatelyTransfer(new TransferBean(5, friendEntity.getAvatar(),
                            friendEntity.getUsername(), friendEntity.getAddress()));
                }

                MsgSend.sendOuterMsg(MsgType.Lucky_Packet, 0, hashId, tips, amount);
                ToastEUtil.makeText(activity, R.string.Link_Send_successful).show();
                ActivityUtil.goBack(activity);
            }

            @Override
            public void fail(WalletError error) {
                ToastEUtil.makeText(activity, R.string.Login_Send_failed).show();
            }
        });
    }
}
