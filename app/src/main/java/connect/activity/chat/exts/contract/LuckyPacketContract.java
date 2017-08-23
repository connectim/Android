package connect.activity.chat.exts.contract;

import connect.activity.base.contract.BasePresenter;
import connect.activity.base.contract.BaseView;
import connect.wallet.cwallet.business.BaseBusiness;

/**
 * Created by Administrator on 2017/8/11.
 */

public interface LuckyPacketContract {

    interface BView extends BaseView<LuckyPacketContract.Presenter> {

        String getRoomKey();

        void showUserInfo(String avatar,String name);

        void showGroupInfo(int count);

        BaseBusiness getBusiness();
    }

    interface Presenter extends BasePresenter {

        void requestRoomEntity(int roomtype);

        void sendLuckyPacket(int roomtype,int packetcount,long amount,String tips);
    }
}