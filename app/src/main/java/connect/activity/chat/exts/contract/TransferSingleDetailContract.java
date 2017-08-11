package connect.activity.chat.exts.contract;

import connect.activity.base.contract.BasePresenter;
import connect.activity.base.contract.BaseView;

/**
 * Created by Administrator on 2017/8/10.
 */

public interface TransferSingleDetailContract {

    interface BView extends BaseView<TransferSingleDetailContract.Presenter> {

        void showTips(String tips);

        void showTransferAmount(long amount);

        void showTransferTxtid(String txtid);

        void showCreateTime(long createtime);

        void showTransferState(int transferstate);

        void showUserInfo(int direct,String avatar,String name);
    }

    interface Presenter extends BasePresenter {

        void requestTransferInnerDetail(String hashid);

        void requestTransferOuterDetail(String hashid);

        void requestUserInfo(int direct, String pubkey);
    }
}
