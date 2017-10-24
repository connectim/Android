package connect.activity.wallet.contract;

import connect.activity.base.contract.BasePresenter;
import connect.activity.base.contract.BaseView;
import protos.Connect;

/**
 * Created by Administrator on 2017/4/18 0018.
 */

public interface PacketDetailContract {

    interface View extends BaseView<PacketDetailContract.Presenter> {
        void updateView(int status,long openMoney,long bestAmount,Connect.RedPackageInfo redPackageInfo);

        void updateSendView(Connect.UserInfo sendUserInfo);
    }

    interface Presenter extends BasePresenter {
        void requestRedDetail(String hashId,int type);
    }

}
