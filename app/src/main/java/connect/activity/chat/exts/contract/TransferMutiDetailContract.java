package connect.activity.chat.exts.contract;

import connect.activity.base.contract.BasePresenter;
import connect.activity.base.contract.BaseView;

/**
 * Created by Administrator on 2017/8/11.
 */

public interface TransferMutiDetailContract {

    interface BView extends BaseView<TransferMutiDetailContract.Presenter> {
            void showSenderInfo(String avatar,String name);
    }

    interface Presenter extends BasePresenter {

        void requestSenderInfo(String address);

        void requestTransferDetail(String hashid);

    }
}
