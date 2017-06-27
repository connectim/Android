package connect.activity.wallet.contract;

import connect.activity.base.BasePresenter;
import connect.activity.base.BaseView;
import protos.Connect;

/**
 * Created by Administrator on 2017/4/18 0018.
 */

public interface PacketDetailContract {

    interface View extends BaseView<PacketDetailContract.Presenter> {
        void updataView(int status,long openMoney,long bestAmount,Connect.RedPackageInfo redPackageInfo);

        void updataSendView(Connect.UserInfo sendUserInfo);
    }

    interface Presenter extends BasePresenter {
        void requestRedDetail(String hashId,int type);
    }

}
